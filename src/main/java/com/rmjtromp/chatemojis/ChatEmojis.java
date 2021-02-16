package com.rmjtromp.chatemojis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.rmjtromp.chatemojis.exceptions.ConfigException;
import com.rmjtromp.chatemojis.utils.ComponentUtils;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public final class ChatEmojis extends JavaPlugin {

    public static final List<String> RESERVED_NAMES = Arrays.asList("emoticon", "emoji", "regex", "enabled");
    public static final Pattern NAME_PATTERN = Pattern.compile("(?<=\\.)?([^\\.]+?)$", Pattern.CASE_INSENSITIVE);

    private EmojiGroup emojis = null;
    private static ChatEmojis plugin;
    private boolean papiIsLoaded = false;

    public ChatEmojis() {
        plugin = this;
    }

    @SuppressWarnings("deprecation")
	@Override
    public void onEnable() {
        saveDefaultConfig();
        try {
            emojis = EmojiGroup.init(getConfig());
        } catch (ConfigException e) {
            e.printStackTrace();
        }
        
        papiIsLoaded = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;

        getServer().getPluginManager().registerEvents(new Listener() {

            @EventHandler
            public void onPlayerChat(AsyncPlayerChatEvent e) {
                String resetColor = ChatColor.RESET + ChatColor.getLastColors(e.getMessage());
                e.setMessage(emojis.parse(e.getPlayer(), resetColor, e.getMessage()));
            }
            
            @EventHandler
            public void onPluginEnable(PluginEnableEvent e) {
            	if(e.getPlugin().getName().equals("PlaceholderAPI")) papiIsLoaded = true;
            }
            
            @EventHandler
            public void onPluginDisable(PluginDisableEvent e) {
            	if(e.getPlugin().getName().equals("PlaceholderAPI")) papiIsLoaded = false;
            }

        }, this);

        getCommand("emoji").setExecutor((sender, command, label, args) -> {
            if(sender instanceof Player) {
                if(sender.hasPermission("chatemojis.command")) {
                    if(args.length == 0) {
                        TextComponent header = ComponentUtils.createComponent("&6ChatEmojis &7(v"+getDescription().getVersion()+")\n");
                        header.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, ComponentUtils.createBaseComponent("&6ChatEmojis\n&7Version: &e"+getDescription().getVersion()+"\n&7Author: &eRMJTromp\n\n&eClick to open spigot resource page.")));
                        header.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.spigotmc.org/resources/chatemojis.88027/"));
                        
                        TextComponent body = ComponentUtils.joinComponents("\n", emojis.getComponents((Player) sender));
                        TextComponent component = ComponentUtils.mergeComponents(header, body);
                        sender.spigot().sendMessage(component);
                    } else sender.sendMessage(ChatColor.RED + "Too many arguments. Try \"/emoji\" for a list of emojis.");
                } else sender.sendMessage(ChatColor.RED + "You don't have enough permission to use this command.");
            } else sender.sendMessage(ChatColor.RED + "Emojis are only available to players.");
            return true;
        });
    }
    
    public boolean isPapiLoaded() {
    	return papiIsLoaded;
    }

    public static ChatEmojis getInstance() {
        return plugin;
    }

    public static class EmojiGroups extends ArrayList<EmojiGroup> {
		private static final long serialVersionUID = 603062735230254276L;
	}
    
    public static class Emojis extends ArrayList<Emoji> {
		private static final long serialVersionUID = 5307065755119322875L;
	}

}
