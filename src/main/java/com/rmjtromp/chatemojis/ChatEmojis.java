package com.rmjtromp.chatemojis;

import com.rmjtromp.chatemojis.exceptions.ConfigException;
import com.rmjtromp.chatemojis.utils.BukkitUtils;
import com.rmjtromp.chatemojis.utils.ComponentBuilder;
import com.rmjtromp.chatemojis.utils.Config;
import com.rmjtromp.chatemojis.utils.Config.ConfigurationReference;
import com.rmjtromp.chatemojis.utils.Version;
import com.rmjtromp.chatemojis.windows.SettingsWindow;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public final class ChatEmojis extends JavaPlugin {

    static final List<String> RESERVED_NAMES = Arrays.asList("emoticon", "emoji", "regex", "enabled");
    static final Pattern NAME_PATTERN = Pattern.compile("(?<=\\.)?([^.]+?)$", Pattern.CASE_INSENSITIVE);

    private final Config config;
    private EmojiGroup emojis = null;
    private static ChatEmojis plugin;
    private boolean papiIsLoaded = false;
    public final ConfigurationReference<Boolean> useOnSigns, useInBooks;
    private SettingsWindow settingsWindow = null;

    public ChatEmojis() throws IOException, InvalidConfigurationException {
        plugin = this;

        BukkitUtils.init(this);

        config = Config.init(new File(getDataFolder(), "config.yml"), "config.yml");
        useInBooks = config.reference("settings.use.books", true);
        useOnSigns = config.reference("settings.use.signs", true);
    }

    @Override
    public void onLoad() {
        try {
            emojis = EmojiGroup.init(getConfig());
        } catch (ConfigException e) {
            e.printStackTrace();
        }
    }

    @Override
	public void onEnable() {
        settingsWindow = new SettingsWindow(this);
        papiIsLoaded = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;

        getServer().getPluginManager().registerEvents(new Listener() {

            @EventHandler
            public void onPlayerChat(AsyncPlayerChatEvent e) {
                String resetColor = ChatColor.RESET + ChatColor.getLastColors(e.getMessage());
                e.setMessage(emojis.parse(e.getPlayer(), resetColor, e.getMessage(), false));
            }
            
            @EventHandler
            public void onPluginEnable(PluginEnableEvent e) {
            	if(e.getPlugin().getName().equals("PlaceholderAPI")) papiIsLoaded = true;
            }
            
            @EventHandler
            public void onPluginDisable(PluginDisableEvent e) {
            	if(e.getPlugin().getName().equals("PlaceholderAPI")) papiIsLoaded = false;
            }
            
            @EventHandler
            public void onSignChange(SignChangeEvent e) {
            	if(Boolean.TRUE.equals(useOnSigns.getValue())) {
                	for(int i = 0; i < e.getLines().length; i++) {
                		e.setLine(i, emojis.parse(e.getPlayer(), ChatColor.RESET + ChatColor.getLastColors(e.getLine(i)), e.getLine(i), false));
                	}
            	}
            }
            
            @EventHandler
            public void onPlayerBookEdit(PlayerEditBookEvent e) {
            	if(Boolean.TRUE.equals(useInBooks.getValue())) {
                	List<String> newContent = new ArrayList<>();
                	BookMeta meta = e.getNewBookMeta();
                	meta.getPages().forEach(string -> newContent.add(emojis.parse(e.getPlayer(), ChatColor.RESET + ChatColor.getLastColors(string), string, false)));
                	meta.setPages(newContent);
                	e.setNewBookMeta(meta);
            	}
            }

        }, this);

        getCommand("emoji").setExecutor((sender, command, label, args) -> {
            if(sender.hasPermission("chatemojis.command") || sender.hasPermission("chatemojis.list")) {
                if(args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase("list"))) {
                    if(sender instanceof Player) {
                        ComponentBuilder builder = new ComponentBuilder("&6ChatEmojis &7(v"+getDescription().getVersion()+")\n");

                        BaseComponent[] hoverMessage = new ComponentBuilder("&6ChatEmojis\n&7Version: &e"+getDescription().getVersion()+"\n&7Author: &eRMJTromp\n\n&eClick to open spigot resource page.").create();

                        // new Text(BaseComponent[]) is not added until 1.16
                        HoverEvent hoverEvent;
                        if(Version.getServerVersion().isOlderThan(Version.V1_16)) hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverMessage);
                        else hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(hoverMessage));
                        builder.event(hoverEvent);

                        Player player = (Player) sender;
                        if(Version.getServerVersion().isOlderThan(Version.V1_8)) {
                            // idk new-lines dont work on 1.7
                            player.spigot().sendMessage(builder.create());
                            emojis.getComponents((Player) sender).forEach(baseComponents -> player.spigot().sendMessage(baseComponents));
                        } else {
                            List<BaseComponent[]> components = emojis.getComponents((Player) sender);
                            for(int i = 0; i < components.size(); i++) {
                                builder.append(components.get(i), ComponentBuilder.FormatRetention.NONE);
                                if(i != components.size() - 1) builder.append("\n", ComponentBuilder.FormatRetention.NONE);
                            }

                            player.spigot().sendMessage(builder.create());
                        }
                    } else sender.sendMessage(ChatColor.RED + "Emojis are only available to players.");
                } else if(args.length == 1) {
                    if(args[0].equalsIgnoreCase("help")) {
                        List<String> lines = new ArrayList<>();
                        lines.add("&6ChatEmojis &7- &fList of Commands");
                        lines.add("&e/emoji [list] &e- &7Shows a list of all emojis");
                        lines.add("&e/emoji help &e- &7Shows this list of commands");
                        lines.add("&e/emoji reload &e- &7Reloads all emojis");
                        lines.add("&e/emoji version &e- &7Shows the plugin version");
                        lines.add("&e/emoji settings &e- &7Toggle plugin settings");

                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.join("\n", lines)));
                    } else if(args[0].equalsIgnoreCase("reload")) {
                        if(sender.hasPermission("chatemojis.reload")) {
                            long start = System.currentTimeMillis();
                            try {
                                reloadConfig();
                                emojis = EmojiGroup.init(getConfig());
                            } catch (ConfigException e) {
                                e.printStackTrace();
                            }
                            long interval = System.currentTimeMillis() - start;
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&eAll emojis and groups have been reloaded &7("+Long.toString(interval)+"ms)"));
                        } else sender.sendMessage(ChatColor.RED + "You don't have enough permission to use this command.");
                    } else if(args[0].equalsIgnoreCase("settings")) {
                        if(sender instanceof Player) {
                            if(sender.hasPermission("chatemojis.admin")) ((Player) sender).openInventory(settingsWindow.getInventory());
                            else sender.sendMessage(ChatColor.RED + "You don't have enough permission to use this command.");
                        } else sender.sendMessage(ChatColor.RED + "Emojis are only available to players.");
                    } else if(args[0].toLowerCase().matches("^v(?:er(?:sion)?)?$")) sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7This server is currently running &eChatEmojis &7(v"+getDescription().getVersion()+")"));
                    else sender.sendMessage(ChatColor.RED + "Unknown argument. Try \"/emoji help\" for a list of commands.");
                } else sender.sendMessage(ChatColor.RED + "Too many arguments. Try \"/emoji help\" for a list of commands.");
            } else sender.sendMessage(ChatColor.RED + "You don't have enough permission to use this command.");
            return true;
        });
    }

    @NotNull
    @Override
    public Config getConfig() {
        return config;
    }

    static ChatEmojis getInstance() {
        return plugin;
    }

}
