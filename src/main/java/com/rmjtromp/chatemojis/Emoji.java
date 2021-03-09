package com.rmjtromp.chatemojis;

import com.rmjtromp.chatemojis.ChatEmojis.AbstractEmoji;
import com.rmjtromp.chatemojis.exceptions.ConfigException;
import com.rmjtromp.chatemojis.exceptions.InvalidEmojiException;
import com.rmjtromp.chatemojis.exceptions.InvalidEmoticonException;
import com.rmjtromp.chatemojis.exceptions.InvalidRegexException;
import com.rmjtromp.chatemojis.utils.ComponentBuilder;
import com.rmjtromp.chatemojis.utils.Lang;
import com.rmjtromp.chatemojis.utils.bukkit.ComponentUtils;
import com.rmjtromp.chatemojis.utils.bukkit.Version;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.rmjtromp.chatemojis.ChatEmojis.NAME_PATTERN;
import static com.rmjtromp.chatemojis.ChatEmojis.RANDOM;

class Emoji implements AbstractEmoji {

    private static final Pattern REGEX_PATTERN = Pattern.compile("^/(.+)/(i)?$", Pattern.CASE_INSENSITIVE);
    private static final ChatEmojis PLUGIN = ChatEmojis.getInstance();

    private final String name;
    private final EmojiGroup parent;
    private final Permission permission;
    private final List<String> emoticons = new ArrayList<>();
    private final Pattern pattern;
    private final List<String> emojis = new ArrayList<>();
    private boolean enabled = true;

    static Emoji init(@NotNull EmojiGroup parent, @NotNull ConfigurationSection section) throws ConfigException {
        return new Emoji(Objects.requireNonNull(parent), Objects.requireNonNull(section));
    }

    /**
     * Creates an emoji from the {@link ConfigurationSection} provided,
     * and links the {@link Permission} node to the specified {@link EmojiGroup}
     * @param parent The parent group of the Emoji
     * @param section The {@link ConfigurationSection} where the emoji's information lies
     * @throws ConfigException If a configuration mistake is present, a configuration exception is thrown
     */
    private Emoji(@NotNull EmojiGroup parent, @NotNull ConfigurationSection section) throws ConfigException {
        this.parent = parent;
        Matcher matcher = NAME_PATTERN.matcher(section.getCurrentPath() != null ? section.getCurrentPath() : "");
        if(matcher.find()) {
            name = matcher.group(1).replaceAll("[_\\s]", "-").replaceAll("[^0-9a-zA-Z-]", "");
            if(name.isEmpty()) throw new ConfigException(Lang.translate("error.emoji.name.empty"), section);
        } else throw new ConfigException(Lang.translate("error.emoji.name.invalid"), section);
        
        /*
         * Set the permission node, inherit name of parent groups.
         */
        permission = parent.parentNames.isEmpty() ? new Permission(String.format("chatemojis.use.%s", name.toLowerCase())) : new Permission(String.format("chatemojis.use.%s.%s", String.join(".", parent.parentNames).toLowerCase(), name.toLowerCase()));
        permission.setDefault(PermissionDefault.OP);
        permission.setDescription(String.format("Permission to use '%s' emoji", name));
        permission.addParent(parent.getPermission(), true);
        
        /*
         * Loops through all keys and checks for a key that matches /emoticons?/i
         * This allows for typos to be made
         */
        boolean emoticonFound = false;
        for(String key : section.getKeys(false)) {
            if(key.toLowerCase().matches("^emoticons?$")) {
                if(section.isString(key) || section.isInt(key)) {
                    String emoticon = section.isString(key) ? section.getString(key) : Integer.toString(section.getInt(key));
                    if(emoticon != null && !emoticon.isEmpty()) emoticons.add(emoticon);
                    else throw new InvalidEmoticonException(Lang.translate("error.emoji.emoticon.empty"), section);
                } else if(section.isList(key)) {
                    List<?> list = section.getList(key);
                    if(list != null) {
                        for(Object obj : list) {
                            String emoticon;
                            if(obj instanceof String) emoticon = (String) obj;
                            else if(obj instanceof Integer) emoticon = Integer.toString((Integer) obj);
                            else if(obj instanceof Boolean) emoticon = Boolean.toString((Boolean) obj);
                            else throw new InvalidEmoticonException(Lang.translate("error.emoji.emoticon.not-supported"), section);

                            if(!emoticon.isEmpty()) emoticons.add(emoticon);
                            else throw new InvalidEmoticonException(Lang.translate("error.emoji.emoticon.empty"), section);
                        }
                    }
                }
                emoticonFound = true;
                break;
            }
        }
        if(emoticons.isEmpty()) throw new InvalidEmoticonException(Lang.translate("error.emoji.emoticon.not-supported"), section);
        else if(!emoticonFound) throw new InvalidEmoticonException(Lang.translate("error.emoji.emoticon.not-provided"), section);

        /*
         * Loops through all keys and checks for a key that matches /regex/i
         * This allows for typos to be made
         */
        Pattern pattern = null;
        for(String key : section.getKeys(false)) {
            if(key.equalsIgnoreCase("regex")) {
                if(section.isString(key)) {
                    String value = section.getString(key);
                    if(value != null) {
                        Matcher m = REGEX_PATTERN.matcher(value);
                        if(m.matches()) pattern = m.group(2) != null ? Pattern.compile(m.group(1), Pattern.CASE_INSENSITIVE) : Pattern.compile(m.group(1));
                        else throw new InvalidRegexException(Lang.translate("error.emoji.regex.invalid"), section);
                    }
                }
                break;
            }
        }
        List<String> quotedEmoticons = new ArrayList<>();
        emoticons.forEach(emoticon -> quotedEmoticons.add(Pattern.quote(emoticon)));
        this.pattern = pattern != null ? pattern : Pattern.compile(String.format("(%s)", String.join("|", quotedEmoticons)), Pattern.CASE_INSENSITIVE);

        /*
         * Loops through all keys and checks for a key that matches /emojis?/i
         * This allows for typos to be made
         */
        boolean emojiFound = false;
        for(String key : section.getKeys(false)) {
            if(key.toLowerCase().matches("^emojis?$")) {
                if(section.isString(key) || section.isInt(key)) {
                    String emoji = section.isString(key) ? section.getString(key) : Integer.toString(section.getInt(key));
                    if(emoji != null && !emoji.isEmpty()) emojis.add(ChatColor.translateAlternateColorCodes('&', StringEscapeUtils.unescapeJava(emoji)));
                    else throw new InvalidEmojiException(Lang.translate("error.emoji.emoji.empty"), section);
                } else if(section.isList(key)) {
                    List<?> list = section.getList(key);
                    if(list != null) {
                        for(Object obj : list) {
                            String emoji;
                            if(obj instanceof String) emoji = (String) obj;
                            else if(obj instanceof Integer) emoji = Integer.toString((Integer) obj);
                            else if(obj instanceof Boolean) emoji =  Boolean.toString((Boolean) obj);
                            else throw new InvalidEmojiException(Lang.translate("error.emoji.emoji.not-supported"), section);

                            if(!emoji.isEmpty()) emojis.add(ChatColor.translateAlternateColorCodes('&', StringEscapeUtils.unescapeJava(emoji)));
                            else throw new InvalidEmojiException(Lang.translate("error.emoji.emoji.empty"), section);
                        }
                    }
                }
                emojiFound = true;
                break;
            }
        }
        if(emojis.isEmpty()) throw new InvalidEmojiException(Lang.translate("error.emoji.emoji.not-supported"), section);
        else if(!emojiFound) throw new InvalidEmojiException(Lang.translate("error.emoji.emoji.not-provided"), section);

        /*
         * Checks whether or not the emoji should be disabled
         */
        for(String key : section.getKeys(false)) {
            if(key.equalsIgnoreCase("enabled")) {
                Object obj = section.get(key);
                if(obj instanceof String) {
                    String value = (String) obj;
                    if(value.equalsIgnoreCase("false")) enabled = false;
                } else if(obj instanceof Integer) {
                    int value = (Integer) obj;
                    if(value == 0 || value == -1) enabled = false;
                } else if(obj instanceof Boolean) {
                    enabled = (Boolean) obj;
                }
                break;
            }
        }
    }

    /**
     * Returns the name of the {@link Emoji}
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Returns emoticons of the {@link Emoji}
     * (e.x. :music:)
     */
    public List<String> getEmoticons() {
        return new ArrayList<>(emoticons);
    }

    /**
     * Returns all possible emojis outcome of the {@link Emoji}
     */
    public List<String> getEmojis() {
        return new ArrayList<>(emojis);
    }

    /**
     * Returns a random emoji if there are multiple
     * returns the first one if there's only one
     */
    public String getEmoji() {
        return getEmojis().size() > 1 ? getEmojis().get(RANDOM.nextInt(getEmojis().size())) : getEmojis().get(0);
    }

    /**
     * Returns the {@link Pattern} that is used to find
     * emoticon matches
     */
    public Pattern getPattern() {
        return pattern;
    }

    /**
     * Returns whether or not an emoji is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Enabled the emoji
     */
    public void enable() {
    	enabled = true;
    }
    
    /**
     * Disables the emoji
     */
    public void disable() {
    	enabled = false;
    }

    /**
     * Returns the {@link Permission} node for the {@link Emoji}
     */
    @Override
    public Permission getPermission() {
        return permission;
    }

    /**
     * Returns the {@link EmojiGroup} for the {@link Emoji}
     */
    public EmojiGroup getParent() {
        return parent;
    }

    /**
     * Parses a string
     * replaces all emoticons with emojis
     * @param player The player which its being parsed for
     * @param resetColor The default color it should go to after the emoji is inserted
     * @param message The message which should be parsed
     */
    String parse(@NotNull Player player, @NotNull String resetColor, @NotNull String message) {
        return parse(player, resetColor, message, false);
    }

    /**
     * Parses a string
     * replaces all emoticons with emojis
     * @param player The player which its being parsed for
     * @param resetColor The default color it should go to after the emoji is inserted
     * @param message The message which should be parsed
     * @param forced Whether or not player permissions should be ignored
     */
    String parse(@NotNull Player player, @NotNull String resetColor, @NotNull String message, boolean forced) {
        if(isEnabled() && (forced || player.hasPermission(getPermission()))) {
            Matcher matcher = getPattern().matcher(message);
            while(matcher.find()) {
            	if(getEmojis().size() > 1) {
            		message = matcher.replaceFirst(ChatColor.RESET + (!PLUGIN.papiIsLoaded ? getEmoji() : PlaceholderAPI.setPlaceholders(player, getEmoji())) + resetColor);
            		matcher = getPattern().matcher(message);
            	} else message = matcher.replaceAll(ChatColor.RESET + (!PLUGIN.papiIsLoaded ? getEmoji() : PlaceholderAPI.setPlaceholders(player, getEmoji())) + resetColor);
            }
        }
        return message;
    }

    /**
     * Returns {@link BaseComponent[]} List to display in
     * list for each emoticons.
     * @param player The player which the emoticon should be parsed for
     */
	@SuppressWarnings("deprecation")
	List<BaseComponent[]> getComponent(@NotNull Player player) {
        List<BaseComponent[]> components = new ArrayList<>();
        emoticons.forEach(emoticon -> {
            // parse the message and ignore whether the player has the right permissions or not
            ComponentBuilder builder = new ComponentBuilder(String.format("&7  - &e%s &7- &f%s", emoticon, parse(player, ChatColor.RESET+"", emoticon, true)));

            // add hover and click events if player has admin permissions
            if(player.hasPermission("chatemojis.admin")) {
                String[] lines = new String[] {
                        ChatColor.WHITE+Lang.translate("command.general.permission-node")+":",
                        ChatColor.GRAY+getPermission().getName(),
                        "",
                        ChatColor.YELLOW+Lang.translate("command.general.click-to-copy")+"!"
                };
            	BaseComponent[] hoverMessage = ComponentUtils.createBaseComponent(String.join("\n", lines));

                // new Text(BaseComponent[]) is not added until 1.16
                HoverEvent hoverEvent;
                if(Version.getServerVersion().isOlderThan(Version.V1_16)) hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverMessage);
                else hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(hoverMessage));
            	builder.event(hoverEvent);
            	
            	// ClickEvent.Action.COPY_TO_CLIPBOARD is not added until 1.15
                ClickEvent clickEvent;
            	if(Version.getServerVersion().isOlderThan(Version.V1_15)) clickEvent = new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, getPermission().getName());
            	else clickEvent = new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, getPermission().getName());
            	builder.event(clickEvent);
            }
            components.add(builder.create());
        });
        return components;
    }
}
