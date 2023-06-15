package com.rmjtromp.chatemojis;

import static com.rmjtromp.chatemojis.ChatEmojis.NAME_PATTERN;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.rmjtromp.chatemojis.utils.BukkitUtils;
import com.rmjtromp.chatemojis.utils.ComponentBuilder;
import com.rmjtromp.chatemojis.utils.Version;
import lombok.Getter;
import lombok.NonNull;
import net.md_5.bungee.api.chat.BaseComponent;
import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import com.rmjtromp.chatemojis.exceptions.ConfigException;
import com.rmjtromp.chatemojis.exceptions.InvalidEmojiException;
import com.rmjtromp.chatemojis.exceptions.InvalidEmoticonException;
import com.rmjtromp.chatemojis.exceptions.InvalidRegexException;

import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class Emoji {

    private static final Pattern REGEX_PATTERN = Pattern.compile("^/(.+)/(i)?$", Pattern.CASE_INSENSITIVE);
    private static final ChatEmojis PLUGIN = ChatEmojis.getInstance();

    @Getter private final String name;
    @Getter private final EmojiGroup parent;
    @Getter private final Permission permission;
    @Nullable private final Integer limit;
    @Getter private final List<String> emoticons = new ArrayList<>();
    @Getter private final Pattern pattern;
    @Getter private final List<String> emojis = new ArrayList<>();
    @Getter private boolean enabled = true;

    static Emoji init(EmojiGroup parent, ConfigurationSection section) throws ConfigException {
        return new Emoji(Objects.requireNonNull(parent), Objects.requireNonNull(section));
    }

    private Emoji(@NonNull EmojiGroup parent, @NonNull ConfigurationSection section) throws ConfigException {
        this.parent = parent;
        Matcher matcher = NAME_PATTERN.matcher(Objects.requireNonNull(section.getCurrentPath()));
        if(matcher.find()) {
            name = matcher.group(1).replaceAll("[_\\s]", "-").replaceAll("[^0-9a-zA-Z-]", "");
            if(name.isEmpty()) throw new ConfigException("Emoji name can not be empty", section);
        } else throw new ConfigException("Invalid emoji name", section);
        permission = parent.parentNames.isEmpty() ? new Permission(String.format("chatemojis.use.%s", name.toLowerCase())) : new Permission(String.format("chatemojis.use.%s.%s", String.join(".", parent.parentNames).toLowerCase(), name.toLowerCase()));
        permission.setDefault(PermissionDefault.OP);
        permission.setDescription(String.format("Permission to use '%s' emoji", name));
        permission.addParent(parent.getPermission(), true);

        Object limit = section.get("limit", null);
        Integer intLimit = null;
        if(limit instanceof Number) {
            intLimit = ((Number) limit).intValue();
            if(intLimit < -1) intLimit = -1;
        }
        this.limit = intLimit;
        
        boolean emoticonFound = false;
        for(String key : section.getKeys(false)) {
            if(key.toLowerCase().matches("^emoticons?$")) {
                if(section.isString(key) || section.isInt(key)) {
                    String emoticon = section.isString(key) ? section.getString(key) : Integer.toString(section.getInt(key));
                    assert emoticon != null;
                    if(!emoticon.isEmpty()) emoticons.add(emoticon);
                    else throw new InvalidEmoticonException("Emoticon can not be empty", section);
                } else if(section.isList(key)) {
                    for(Object obj : Objects.requireNonNull(section.getList(key))) {
                        String emoticon;
                        if(obj instanceof String) emoticon = (String) obj;
                        else if(obj instanceof Integer) emoticon = Integer.toString((Integer) obj);
                        else if(obj instanceof Boolean) emoticon = Boolean.toString((Boolean) obj);
                        else throw new InvalidEmoticonException("Emoticon type not supported", section);

                        if(!emoticon.isEmpty()) emoticons.add(emoticon);
                        else throw new InvalidEmoticonException("Emoticon can not be empty", section);
                    }
                }
                emoticonFound = true;
                break;
            }
        }
        if(emoticons.isEmpty()) throw new InvalidEmoticonException("Emoticon type not supported", section);
        else if(!emoticonFound) throw new InvalidEmoticonException("No emoticon provided", section);

        Pattern pattern = null;
        for(String key : section.getKeys(false)) {
            if(key.equalsIgnoreCase("regex")) {
                if(section.isString(key)) {
                    String value = section.getString(key);
                    assert value != null;
                    Matcher m = REGEX_PATTERN.matcher(value);
                    if(m.matches()) {
                        pattern = m.group(2) != null ? Pattern.compile(m.group(1), Pattern.CASE_INSENSITIVE) : Pattern.compile(m.group(1));
                        break;
                    } else throw new InvalidRegexException("Invalid regex syntax", section);
                }
                break;
            }
        }
        List<String> quotedEmoticons = new ArrayList<>();
        emoticons.forEach(emoticon -> quotedEmoticons.add(Pattern.quote(emoticon)));
        this.pattern = pattern != null ? pattern : Pattern.compile(String.format("(%s)", String.join("|", quotedEmoticons)), Pattern.CASE_INSENSITIVE);

        boolean emojiFound = false;
        for(String key : section.getKeys(false)) {
            if(key.toLowerCase().matches("^emojis?$")) {
                if(section.isString(key) || section.isInt(key)) {
                    String emoji = section.isString(key) ? section.getString(key) : Integer.toString(section.getInt(key));
                    assert emoji != null;
                    if(!emoji.isEmpty()) emojis.add(ChatColor.translateAlternateColorCodes('&', StringEscapeUtils.unescapeJava(emoji)));
                    else throw new InvalidEmojiException("Emoji can not be empty", section);
                } else if(section.isList(key)) {
                    for(Object obj : Objects.requireNonNull(section.getList(key))) {
                        String emoji;
                        if(obj instanceof String) emoji = (String) obj;
                        else if(obj instanceof Integer) emoji = Integer.toString((Integer) obj);
                        else if(obj instanceof Boolean) emoji =  Boolean.toString((Boolean) obj);
                        else throw new InvalidEmojiException("Emoji type not supported", section);

                        if(!emoji.isEmpty()) emojis.add(ChatColor.translateAlternateColorCodes('&', StringEscapeUtils.unescapeJava(emoji)));
                        else throw new InvalidEmojiException("Emoji can not be empty", section);
                    }
                }
                emojiFound = true;
                break;
            }
        }
        if(emojis.isEmpty()) throw new InvalidEmojiException("Emoji type not supported", section);
        else if(!emojiFound) throw new InvalidEmojiException("No emoji provided", section);

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

    private static final Random random = new Random();
    public String getEmoji() {
        return getEmojis().size() > 1 ? getEmojis().get(random.nextInt(getEmojis().size())) : getEmojis().get(0);
    }
    
    public void enable() {
    	enabled = true;
    }
    
    public void disable() {
    	enabled = false;
    }

    public int getLimit() {
    	return limit != null ? limit : PLUGIN.maxDuplicateEmojis.getNonNullValue();
    }

    public boolean hasLimit() {
        return getLimit() != -1;
    }

    public ParsingContext parse(@NonNull ParsingContext ctx) {
        if(!isEnabled()) return ctx;
        if(!ctx.getPlayer().hasPermission(getPermission()) && !ctx.isForced()) return ctx;
        if(ctx.hasReachedGlobalLimit() || ctx.hasReachedLimit(this)) return ctx;

        Matcher matcher = getPattern().matcher(ctx.getMessage());
        while(matcher.find()) {
            final String replacement = ChatColor.RESET + (!PLUGIN.papiIsLoaded ? getEmoji() : PlaceholderAPI.setPlaceholders(ctx.getPlayer(), getEmoji())) + ctx.getResetColor();

            if(getEmojis().size() > 1) {
                ctx.setMessage(matcher.replaceFirst(replacement));
                matcher = getPattern().matcher(ctx.getMessage());
            } else
                ctx.setMessage(matcher.replaceAll(replacement));

            ctx.getUseMap().compute(this, (k, v) -> v == null ? 1 : v + 1);
            if((ctx.hasReachedLimit(this) || ctx.hasReachedGlobalLimit()) && !ctx.isForced()) break;
        }

        return ctx;
    }

    @Deprecated
    public String parse(@NotNull Player player, @NotNull String resetColor, @NotNull String message, boolean forced) {
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

    @SuppressWarnings("deprecation")
    public List<BaseComponent[]> getComponents(@NotNull Player player) {
        List<BaseComponent[]> components = new ArrayList<>();
        emoticons.forEach(emoticon -> {
            // parse the message and ignore whether the player has the right permissions or not
            ComponentBuilder builder = new ComponentBuilder(String.format("&7  - &e%s &7- &f%s", emoticon, parse(player, ChatColor.RESET+"", emoticon, true)));

            // add hover and click events if player has admin permissions
            if(player.hasPermission("chatemojis.admin")) {
                BaseComponent[] hoverMessage = new ComponentBuilder(String.join("\n", BukkitUtils.colorEncode(
                        "&fPermission Node:",
                        "&7" + getPermission().getName(),
                        "",
                        "&eClick to Copy!"
                ))).create();

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
