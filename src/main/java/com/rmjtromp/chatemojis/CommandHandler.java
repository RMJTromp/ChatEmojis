package com.rmjtromp.chatemojis;

import com.rmjtromp.chatemojis.utils.BukkitUtils;
import com.rmjtromp.chatemojis.utils.ComponentBuilder;
import com.rmjtromp.chatemojis.utils.Version;
import lombok.Getter;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

class CommandHandler implements CommandExecutor, TabCompleter {

    private static final ChatEmojis PLUGIN = ChatEmojis.getInstance();

    @Getter(lazy = true)
    private static final CommandHandler handler = new CommandHandler();

    private static final String help_message = ChatColor.translateAlternateColorCodes('&', String.join("\n", Arrays.asList(
        "&6ChatEmojis &7- &fList of Commands",
        "&e/emoji [list] [page] &e- &7Shows a list of all emojis",
        "&e/emoji dump &e- &7Dump relevant debugging information",
        "&e/emoji help &e- &7Shows this list of commands",
        "&e/emoji reload &e- &7Reloads all emojis",
        "&e/emoji version &e- &7Shows the plugin version",
        "&e/emoji settings &e- &7Toggle plugin settings"
    )));

    CommandHandler() {}

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(sender.hasPermission("chatemojis.command") || sender.hasPermission("chatemojis.list")) {
            if(args.length == 0 || args.length <= 2 && args[0].equalsIgnoreCase("list")) {
                if(sender instanceof Player) {

                    ComponentBuilder builder = new ComponentBuilder("&6ChatEmojis &7(v"+PLUGIN.getDescription().getVersion()+")\n");

                    BaseComponent[] hoverMessage = new ComponentBuilder("&6ChatEmojis\n&7Version: &e"+PLUGIN.getDescription().getVersion()+"\n&7Author: &eRMJTromp\n\n&eClick to open spigot resource page.").create();

                    // new Text(BaseComponent[]) is not added until 1.16
                    HoverEvent hoverEvent;
                    if(Version.getServerVersion().isOlderThan(Version.V1_16)) hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverMessage);
                    else hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(hoverMessage));
                    builder.event(hoverEvent);

                    Player player = (Player) sender;
                    if(Version.getServerVersion().isOlderThan(Version.V1_8)) {
                        // idk new-lines dont work on 1.7
                        player.spigot().sendMessage(builder.create());
                        PLUGIN.emojis.getComponents((Player) sender).forEach(baseComponents -> player.spigot().sendMessage(baseComponents));
                    } else {
                        List<BaseComponent[]> components = PLUGIN.emojis.getComponents((Player) sender);

                        // Pages
                        final int messagesPerPage = 20;
                        int page = 1;
                        if(args.length > 1) {
                            try {
                                page = Math.max(1, Integer.parseInt(args[1]));
                            } catch (NumberFormatException ignored) {}
                        }
                        final int lastPage = (components.size() - 1) / messagesPerPage + 1;
                        page = Math.min(page, lastPage);

                        final int pageEnd = page * messagesPerPage;
                        final int pageStart = pageEnd - messagesPerPage;

                        for(int i = pageStart; i < components.size() && i < pageEnd; i++) {
                            builder.append(components.get(i), ComponentBuilder.FormatRetention.NONE);
                            if(i != components.size() - 1 && i != pageEnd - 1) builder.append("\n", ComponentBuilder.FormatRetention.NONE);
                        }

                        if (lastPage != 1) {
                            builder.append("\n", ComponentBuilder.FormatRetention.NONE);

                            ComponentBuilder paginationBuilder = new ComponentBuilder();
                            // left chevron
                            {
                                boolean firstPage = page == 1;
                                ChatColor color = firstPage ? ChatColor.GRAY : ChatColor.GOLD;
                                ComponentBuilder leftChevron = new ComponentBuilder(color + " «  ");

                                if(!firstPage) {
                                    BaseComponent[] previousPageHoverMessage = new ComponentBuilder(
                                            String.format("§f/emoji list %s", page - 1)
                                    ).create();

                                    // new Text(BaseComponent[]) is not added until 1.16
                                    HoverEvent previousPageHoverEvent;
                                    if(Version.getServerVersion().isOlderThan(Version.V1_16)) previousPageHoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, previousPageHoverMessage);
                                    else previousPageHoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(previousPageHoverMessage));
                                    leftChevron.event(previousPageHoverEvent);

                                    leftChevron.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/emoji list " + (page - 1)));
                                }

                                paginationBuilder.append(leftChevron.create(), ComponentBuilder.FormatRetention.NONE);
                            }

                            // pagination
                            paginationBuilder.append(
                                    String.format("§fPage §e%s§7/%s", page, lastPage),
                                    ComponentBuilder.FormatRetention.NONE
                            );

                            {
                                // right chevron
                                boolean isLastPage = page == lastPage;
                                ChatColor color = isLastPage ? ChatColor.GRAY : ChatColor.GOLD;
                                ComponentBuilder rightChevron = new ComponentBuilder(color + "  » ");

                                if(!isLastPage) {
                                    BaseComponent[] nextPageHoverMessage = new ComponentBuilder(
                                            String.format("§f/emoji list %s", page + 1)
                                    ).create();

                                    // new Text(BaseComponent[]) is not added until 1.16
                                    HoverEvent nextPageHoverEvent;
                                    if(Version.getServerVersion().isOlderThan(Version.V1_16)) nextPageHoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, nextPageHoverMessage);
                                    else nextPageHoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(nextPageHoverMessage));
                                    rightChevron.event(nextPageHoverEvent);

                                    rightChevron.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/emoji list " + (page + 1)));
                                }

                                paginationBuilder.append(rightChevron.create(), ComponentBuilder.FormatRetention.NONE);
                            }

                            builder.append(paginationBuilder.create(), ComponentBuilder.FormatRetention.NONE);
                        }
                        player.spigot().sendMessage(builder.create());
                    }
                } else sender.sendMessage(help_message);
            } else if(args.length == 1) {
                if(args[0].equalsIgnoreCase("help")) sender.sendMessage(help_message);
                else if(args[0].equalsIgnoreCase("dump")) {
                    if(sender.hasPermission("chatemojis.admin")) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7" + String.join("\n&7", Arrays.asList(
                                "&6ChatEmojis &7- &fDump",
                                "Versions;",
                                "  Server: &e"+ BukkitUtils.getServerVersion().replaceAll("_", ".") + " (" + Version.getServerVersion() + ")",
                                "  Plugin: &e"+PLUGIN.getDescription().getVersion(),
                                "Settings;",
                                "  Sign Manipulation: &e"+PLUGIN.useOnSigns.getNonNullValue(),
                                "  Book Manipulation: &e"+PLUGIN.useInBooks.getNonNullValue(),
                                "  Max Duplicates: &e"+PLUGIN.maxDuplicateEmojis.getNonNullValue(),
                                "  Max Emojis: &e"+PLUGIN.maxEmojisPerMessage.getNonNullValue(),
                                "Dependencies;",
                                "  PlaceholderAPI: &e"+PLUGIN.papiIsLoaded,
                                "Emojis Loaded: &e"+PLUGIN.emojis.getEmojis(true).size()
                        ))));
                    } else sender.sendMessage(ChatColor.RED + "You don't have enough permission to use this command.");
                } else if(args[0].equalsIgnoreCase("reload")) {
                    if(sender.hasPermission("chatemojis.reload")) {
                        long start = System.currentTimeMillis();
                        try {
                            PLUGIN.getConfig().reload();
                            PLUGIN.emojis = EmojiGroup.init(PLUGIN.getConfig());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        long interval = System.currentTimeMillis() - start;
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&eAll emojis and groups have been reloaded &7("+ interval +"ms)"));
                    } else sender.sendMessage(ChatColor.RED + "You don't have enough permission to use this command.");
                } else if(args[0].equalsIgnoreCase("settings")) {
                    if(sender instanceof Player) {
                        if(sender.hasPermission("chatemojis.admin")) ((Player) sender).openInventory(PLUGIN.settingsWindow.getInventory());
                        else sender.sendMessage(ChatColor.RED + "You don't have enough permission to use this command.");
                    } else sender.sendMessage(ChatColor.RED + "Emojis are only available to players.");
                } else if(args[0].toLowerCase().matches("^v(?:er(?:sion)?)?$")) sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7This server is currently running &eChatEmojis &7(v"+PLUGIN.getDescription().getVersion()+")"));
                else sender.sendMessage(ChatColor.RED + "Unknown argument. Try \"/emoji help\" for a list of commands.");
            } else sender.sendMessage(ChatColor.RED + "Too many arguments. Try \"/emoji help\" for a list of commands.");
        } else sender.sendMessage(ChatColor.RED + "You don't have enough permission to use this command.");
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return null;
    }
}
