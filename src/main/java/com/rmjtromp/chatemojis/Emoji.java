package com.rmjtromp.chatemojis;

import static com.rmjtromp.chatemojis.ChatEmojis.NAME_PATTERN;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import com.rmjtromp.chatemojis.utils.ComponentUtils;

import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class Emoji {

    private static final Pattern REGEX_PATTERN = Pattern.compile("^/(.+)/(i)?$", Pattern.CASE_INSENSITIVE);
    private static final ChatEmojis plugin = ChatEmojis.getInstance();

    private final String name;
    private final EmojiGroup parent;
    private final Permission permission;
    private final List<String> emoticons = new ArrayList<>();
    private final Pattern pattern;
    private final List<String> emojis = new ArrayList<>();
    private boolean enabled = true;

    public static Emoji init(EmojiGroup parent, ConfigurationSection section) throws ConfigException {
        return new Emoji(Objects.requireNonNull(parent), Objects.requireNonNull(section));
    }

    private Emoji(EmojiGroup parent, ConfigurationSection section) throws ConfigException {
        this.parent = parent;
        Matcher matcher = NAME_PATTERN.matcher(section.getCurrentPath());
        if(matcher.find()) {
            name = matcher.group(1).replaceAll("[_\\s]", "-").replaceAll("[^0-9a-zA-Z-]", "");
            if(name.isEmpty()) throw new ConfigException("Emoji name can not be empty", section);
        } else throw new ConfigException("Invalid emoji name", section);
        permission = parent.parentNames.isEmpty() ? new Permission(String.format("chatemojis.use.%s", name.toLowerCase())) : new Permission(String.format("chatemojis.use.%s.%s", String.join(".", parent.parentNames).toLowerCase(), name.toLowerCase()));
        permission.setDefault(PermissionDefault.OP);
        permission.setDescription(String.format("Permission to use '%s' emoji", name));
        permission.addParent(parent.getPermission(), true);
        
        boolean emoticonFound = false;
        for(String key : section.getKeys(false)) {
            if(key.toLowerCase().matches("^emoticons?$")) {
                if(section.isString(key) || section.isInt(key)) {
                    String emoticon = section.isString(key) ? section.getString(key) : Integer.toString(section.getInt(key));
                    if(!emoticon.isEmpty()) emoticons.add(emoticon);
                    else throw new InvalidEmoticonException("Emoticon can not be empty", section);
                } else if(section.isList(key)) {
                    for(Object obj : section.getList(key)) {
                        String emoticon;
                        if(obj instanceof String) emoticon = (String) obj;
                        else if(obj instanceof Integer) emoticon = Integer.toString((Integer) obj);
                        else if(obj instanceof Boolean) emoticon = (Boolean) obj ? "true" : "false";
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
                    if(!emoji.isEmpty()) emojis.add(ChatColor.translateAlternateColorCodes('&', StringEscapeUtils.unescapeJava(emoji)));
                    else throw new InvalidEmojiException("Emoji can not be empty", section);
                } else if(section.isList(key)) {
                    for(Object obj : section.getList(key)) {
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

    public String getName() {
        return name;
    }

    public List<String> getEmoticons() {
        return new ArrayList<>(emoticons);
    }

    public List<String> getEmojis() {
        return new ArrayList<>(emojis);
    }

    private static final Random random = new Random();
    public String getEmoji() {
        return getEmojis().size() > 1 ? getEmojis().get(random.nextInt(getEmojis().size())) : getEmojis().get(0);
    }

    public Pattern getPattern() {
        return pattern;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Permission getPermission() {
        return permission;
    }

    public EmojiGroup getParent() {
        return parent;
    }

    public String parse(Player player, String resetColor, String message) {
        if(isEnabled() && player.hasPermission(getPermission())) {
            Matcher matcher = getPattern().matcher(message);
            while(matcher.find()) {
            	if(getEmojis().size() > 1) {
            		message = matcher.replaceFirst(ChatColor.RESET + (!plugin.isPapiLoaded() ? getEmoji() : PlaceholderAPI.setPlaceholders(player, getEmoji())) + resetColor);
            		matcher = getPattern().matcher(message);
            	} else message = matcher.replaceAll(ChatColor.RESET + (!plugin.isPapiLoaded() ? getEmoji() : PlaceholderAPI.setPlaceholders(player, getEmoji())) + resetColor);
            }
        }
        return message;
    }

    @SuppressWarnings("deprecation")
	public List<TextComponent> getComponent(Player player) {
        List<TextComponent> components = new ArrayList<>();
        TextComponent prefix = ComponentUtils.createComponent("&7  - ");
        emoticons.forEach(emoticon -> {
            TextComponent comp = ComponentUtils.createComponent(String.format("&e%s &7- &f%s", emoticon, getPattern().matcher(emoticon).replaceAll(PlaceholderAPI.setPlaceholders(player, getEmoji()))));
            if(player.isOp()) {
                comp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, ComponentUtils.createBaseComponent(String.format("&fPermission Node:\n&7%s\n\n&eClick To Copy!", getPermission().getName()))));
                comp.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, getPermission().toString()));
            }
            components.add(ComponentUtils.mergeComponents(prefix, comp));
        });
        return components;
    }
}
