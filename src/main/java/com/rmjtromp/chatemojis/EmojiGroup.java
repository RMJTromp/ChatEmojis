package com.rmjtromp.chatemojis;

import com.rmjtromp.chatemojis.exceptions.ConfigException;
import lombok.NonNull;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static com.rmjtromp.chatemojis.ChatEmojis.NAME_PATTERN;
import static com.rmjtromp.chatemojis.ChatEmojis.RESERVED_NAMES;

public class EmojiGroup {

    private final ArrayList<Emoji> emojis = new  ArrayList<>();
    private final ArrayList<EmojiGroup> groups = new ArrayList<>();

    private final String name;
    private final Permission permission;
    private final EmojiGroup parent;

    final List<String> parentNames = new ArrayList<>();

    static EmojiGroup init(ConfigurationSection section) throws ConfigException {
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
            if(!RESERVED_NAMES.contains(key.toLowerCase()) || (parent == null && !key.equalsIgnoreCase("settings"))) {
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

    public ArrayList<EmojiGroup> getGroups() {
        return groups;
    }

    public ArrayList<Emoji> getEmojis() {
        return getEmojis(false);
    }

    /**
     * Get all emojis recursively
     * @param recursively Whether to get emojis recursively
     * @return All emojis
     */
    public ArrayList<Emoji> getEmojis(boolean recursively) {
        ArrayList<Emoji> emojis = new ArrayList<>(this.emojis);

        if(recursively) {
            for(EmojiGroup group : getGroups()) emojis.addAll(group.getEmojis(true));
        }

        return emojis;
    }

    public ParsingContext parse(@NonNull ParsingContext ctx) {
        HashMap<Emoji, Integer> hits = new HashMap<>();

        for (Emoji emoji : getEmojis(true)) {
            if(!emoji.isEnabled()) continue;
            if(!ctx.isForced() && !ctx.getPlayer().hasPermission(emoji.getPermission())) continue;

            int res = emoji.messureHit(ctx.getMessage());
            if(res > 0) hits.put(emoji, res);
        }

        if(hits.isEmpty()) return ctx;

        List<Emoji> emojis = hits.entrySet().stream()
            .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()))
            .map(Entry::getKey)
            .collect(Collectors.toList());

        while(!emojis.isEmpty()) {
            emojis.remove(0)._parse(ctx);
            if(ctx.hasReachedGlobalLimit()) break;
        }

        return ctx;
    }

    @Deprecated
    public String parse(@NotNull Player player, @NotNull String resetColor, @NotNull String message, boolean forced) {
        for(EmojiGroup group : getGroups()) message = group.parse(player, resetColor, message, forced);
        for(Emoji emoji : getEmojis()) message = emoji.parse(player, resetColor, message, forced);
        return message;
    }

    public List<BaseComponent[]> getComponents(@NotNull Player player) {
        List<BaseComponent[]> components = new ArrayList<>();
        getGroups().forEach(group -> components.addAll(group.getComponents(player)));
        getEmojis().forEach(emoji -> components.addAll(emoji.getComponents(player)));
        return components;
    }

    private boolean isSetKey(ConfigurationSection section, String regex) {
        for(String key : section.getKeys(false)) {
            if(key.toLowerCase().matches(regex)) return true;
        }
        return false;
    }

}
