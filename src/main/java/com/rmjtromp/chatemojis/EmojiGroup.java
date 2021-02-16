package com.rmjtromp.chatemojis;

import com.rmjtromp.chatemojis.ChatEmojis.Emojis;
import com.rmjtromp.chatemojis.ChatEmojis.EmojiGroups;
import com.rmjtromp.chatemojis.exceptions.ConfigException;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.util.*;
import java.util.regex.Matcher;

import static com.rmjtromp.chatemojis.ChatEmojis.NAME_PATTERN;
import static com.rmjtromp.chatemojis.ChatEmojis.RESERVED_NAMES;

public class EmojiGroup {

    private final Emojis emojis = new Emojis();
    private final EmojiGroups groups = new EmojiGroups();

    private final String name;
    private final Permission permission;
    private final EmojiGroup parent;

    public final List<String> parentNames = new ArrayList<>();

    public static EmojiGroup init(ConfigurationSection section) throws ConfigException {
        return new EmojiGroup(null, Objects.requireNonNull(section));
    }

    private EmojiGroup(EmojiGroup parent, ConfigurationSection section) throws ConfigException {
        this.parent = parent;
        String _permission = "chatemojis.use";
        if(parent != null) {
            Matcher matcher = NAME_PATTERN.matcher(section.getCurrentPath());
            if(matcher.find()) {
                name = matcher.group(1).replaceAll("[_\\s]+", "-").replaceAll("[^0-9a-zA-Z-]", "");
                if(name.isEmpty()) throw new ConfigException("Group name can not be empty", section);
            } else throw new ConfigException("Invalid group name", section);

            parentNames.add(this.name);
            EmojiGroup emoji = this;
            while(emoji.parent != null && emoji.parent.name != null) {
                parentNames.add(emoji.parent.name);
                emoji = emoji.parent;
            }
            Collections.reverse(parentNames);

            _permission = String.format("%s.%s", _permission, String.join(".", parentNames).toLowerCase());
        } else name = null;
        permission = new Permission(String.format("%s.*", _permission));
        permission.setDefault(PermissionDefault.OP);
        permission.setDescription(String.format("Permission to use all emojis listed in '%s'", name));
        if(parent != null) permission.addParent(parent.getPermission(), true);
        
        for(String key : section.getKeys(false)) {
            if(!RESERVED_NAMES.contains(key.toLowerCase())) {
                ConfigurationSection s = section.getConfigurationSection(key);
                if(s == null) continue;
                if(isSetKey(s, "^emoticons?$") && isSetKey(s, "^emojis?$")) {
                    try {
                        emojis.add(Emoji.init(this, s));
                    } catch (ConfigException e) {
                        System.out.println("[ChatEmoji] There was an error loading emoji: "+e.getMessage());
                    }
                } else groups.add(new EmojiGroup(this, s));
            }
        }
    }

    public String getName() {
        return name;
    }

    public Permission getPermission() {
        return permission;
    }

    public EmojiGroups getGroups() {
        return groups;
    }

    public Emojis getEmojis() {
        return emojis;
    }

    public String parse(Player player, String resetColor, String message) {
        for(EmojiGroup group : getGroups()) message = group.parse(player, resetColor, message);
        for(Emoji emoji : getEmojis()) message = emoji.parse(player, resetColor, message);
        return message;

    }

    public List<TextComponent> getComponents(Player player) {
        List<TextComponent> components = new ArrayList<>();
        getGroups().forEach(group -> components.addAll(group.getComponents(player)));
        getEmojis().forEach(emoji -> components.addAll(emoji.getComponent(player)));
        return components;
    }

    private boolean isSetKey(ConfigurationSection section, String regex) {
        for(String key : section.getKeys(false)) {
            if(key.toLowerCase().matches(regex)) return true;
        }
        return false;
    }

}
