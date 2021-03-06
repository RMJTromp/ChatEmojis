package com.rmjtromp.chatemojis;

import static com.rmjtromp.chatemojis.ChatEmojis.NAME_PATTERN;
import static com.rmjtromp.chatemojis.ChatEmojis.RESERVED_NAMES;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.regex.Matcher;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import com.rmjtromp.chatemojis.ChatEmojis.AbstractEmoji;
import com.rmjtromp.chatemojis.exceptions.ConfigException;
import com.rmjtromp.chatemojis.utils.Lang;
import com.rmjtromp.chatemojis.utils.Lang.Replacements;

import net.md_5.bungee.api.chat.TextComponent;

class EmojiGroup implements AbstractEmoji {
	
    private final List<Emoji> emojis = new ArrayList<>();
    private final List<EmojiGroup> groups = new ArrayList<>();

    private final String name;
    private final Permission permission;
    private final EmojiGroup parent;

    final List<String> parentNames = new ArrayList<>();

    static EmojiGroup init(ConfigurationSection section) throws ConfigException {
        return new EmojiGroup(null, Objects.requireNonNull(section));
    }

    /**
     * Creates an {@link EmojiGroup} from the {@link ConfigurationSection} provided
     * @param parent
     * @param section
     * @throws ConfigException
     */
    private EmojiGroup(EmojiGroup parent, ConfigurationSection section) throws ConfigException {
        this.parent = parent;
        String permissionBase = "chatemojis.use";
        if(parent != null) {
        	// scrape name from the ConfigurationSection path
            Matcher matcher = NAME_PATTERN.matcher(section.getCurrentPath());
            if(matcher.find()) {
                name = matcher.group(1).replaceAll("[_\\s]+", "-").replaceAll("[^0-9a-zA-Z-]", "");
                if(name.isEmpty()) throw new ConfigException(Lang.translate("error.emojigroup.name.empty"), section);
            } else throw new ConfigException(Lang.translate("error.emojigroup.name.invalid"), section);

            // build the permission node base
            parentNames.add(this.name);
            EmojiGroup emoji = this;
            while(emoji.parent != null && emoji.parent.name != null) {
                parentNames.add(emoji.parent.name);
                emoji = emoji.parent;
            }
            Collections.reverse(parentNames);

            permissionBase = String.format("%s.%s", permissionBase, String.join(".", parentNames).toLowerCase());
        } else name = null;
        
        /*
         * Set the permission node, inherit name of parent groups.
         */
        permission = new Permission(String.format("%s.*", permissionBase));
        permission.setDefault(PermissionDefault.OP);
        permission.setDescription(String.format("Permission to use all emojis listed in '%s'", name));
        if(parent != null) permission.addParent(parent.getPermission(), true);
        
        /*
         * Loops through all keys and looks for anything that is not reserved
         * in the case of the first group (default group) "settings" is also reserved
         */
        for(String key : section.getKeys(false)) {
            if(!RESERVED_NAMES.contains(key.toLowerCase())) {
                ConfigurationSection s = section.getConfigurationSection(key);
                if(s == null) continue;
                if(isSetKey(s, "^emoticons?$") && isSetKey(s, "^emojis?$")) {
                    try {
                        emojis.add(Emoji.init(this, s));
                    } catch (ConfigException e) {
                    	Replacements replacements = new Replacements();
                    	replacements.add("emoji", key);
                    	replacements.add("message", e.getMessage());
                        System.out.println(String.format("[ChatEmoji] %s", Lang.translate("error.load.emoji", replacements)));
                    }
                } else {
                    try {
                    	groups.add(new EmojiGroup(this, s));
                    } catch (ConfigException e) {
                    	Replacements replacements = new Replacements();
                    	replacements.add("emojigroup", key);
                    	replacements.add("message", e.getMessage());
                        System.out.println(String.format("[ChatEmoji] %s", Lang.translate("error.load.group", replacements)));
                    }
                }
            }
        }
    }

    /**
     * Returns the name of the {@link EmojiGroup}
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Returns the {@link Permission} node of the {@link EmojiGroup}
     */
    @Override
    public Permission getPermission() {
        return permission;
    }

    /**
     * Returns all child {@link EmojiGroup}s
     */
    public List<EmojiGroup> getGroups() {
        return groups;
    }

    /**
     * Returms all child {@link Emoji}
     */
    public List<Emoji> getEmojis() {
        return emojis;
    }
    
    /**
     * Parses a string, replaces all emoticons with emojis.
     * Does this for all sub-groups and child emojis
     * @param player
     * @param resetColor
     * @param message
     */
    String parse(Player player, String resetColor, String message) {
        for(EmojiGroup group : getGroups()) message = group.parse(player, resetColor, message);
        for(Emoji emoji : getEmojis()) message = emoji.parse(player, resetColor, message);
        return message;

    }

    /**
     * Returns {@link TextComponent} array to display in
     * list for each emoticons.
     * @param player
     */
    List<TextComponent> getComponents(Player player) {
        List<TextComponent> components = new ArrayList<>();
        getGroups().forEach(group -> components.addAll(group.getComponents(player)));
        getEmojis().forEach(emoji -> components.addAll(emoji.getComponent(player)));
        return components;
    }

    /**
     * Checks if key exists in {@link ConfigurationSecion} that matches the regular expression
     * @param section
     * @param regex
     */
    private boolean isSetKey(ConfigurationSection section, String regex) {
        for(String key : section.getKeys(false)) {
            if(key.toLowerCase().matches(regex)) return true;
        }
        return false;
    }

	public void forEach(Consumer<AbstractEmoji> action) {
		groups.forEach(action);
		emojis.forEach(action);
	}

}
