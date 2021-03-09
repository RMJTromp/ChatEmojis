package com.rmjtromp.chatemojis;

import com.rmjtromp.chatemojis.gui.SettingsGUI;
import com.rmjtromp.chatemojis.utils.ComponentBuilder;
import com.rmjtromp.chatemojis.utils.Lang;
import com.rmjtromp.chatemojis.utils.Lang.Replacements;
import com.rmjtromp.chatemojis.utils.bukkit.ComponentUtils;
import com.rmjtromp.chatemojis.utils.bukkit.Version;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

import java.util.*;

final class CommandHandler {
	
	private static final ChatEmojis PLUGIN = ChatEmojis.getInstance();
	private static CommandHandler handler = null;
	
	static void init() {
		handler = new CommandHandler();
	}
	
	static CommandHandler getHandler() {
		return handler;
	}
	
	private final SettingsGUI settingsGui = new SettingsGUI();
	
	private final HashMap<String, SubCommand> subCommands = new HashMap<>();
	
	private CommandHandler() {
		registerSubCommands();

		PluginCommand pluginCommand = PLUGIN.getCommand("emoji");
		assert pluginCommand != null;

		pluginCommand.setExecutor((sender, command, label, args) -> {
			if(args.length == 0) {
				// if player, show list of emojis, if not player, show list of commands
				if(sender instanceof Player) subCommands.get("list").executor.onCommand(sender, command, label, args);
				else subCommands.get("help").executor.onCommand(sender, command, label, args);
			} else {
				for(SubCommand subCommand : subCommands.values()) {
					if(subCommand.name.equalsIgnoreCase(args[0])) {
						if(subCommand.permission == null || sender.hasPermission(subCommand.permission)) {
							return subCommand.executor.onCommand(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
						} else sender.sendMessage(ChatColor.RED + Lang.translate("command.general.not-enough-permissions"));
					}
				}
			}
			return true;
		});

		pluginCommand.setTabCompleter((sender, command, label, args) -> {
			List<String> suggestions = new ArrayList<>();
			if(args.length == 0 || (args.length == 1 && args[0].isEmpty())) subCommands.values().stream().filter(cmd -> cmd.permission == null || sender.hasPermission(cmd.permission)).map(cmd -> cmd.name).forEach(suggestions::add);
			else if(args.length == 1) subCommands.values().stream().filter(cmd -> cmd.name.startsWith(args[0].toLowerCase()) && (cmd.permission == null || sender.hasPermission(cmd.permission))).map(cmd -> cmd.name).forEach(suggestions::add);
			Collections.sort(suggestions);
			return suggestions;
		});
	}
	
	@SuppressWarnings("deprecation")
	private void registerSubCommands() {
		// add help command
		subCommands.put("help", new SubCommand(Lang.translate("command.sub-commands.help.name"), Lang.translate("command.sub-commands.help.description"), (sender, command, label, args) -> {
			if (args.length == 0) {
				List<String> lines = new ArrayList<>();
				lines.add(String.format("&6ChatEmojis &7- &f%s", Lang.translate("command.general.list-of-commands")));
				subCommands.values().forEach(cmd -> {
					if (cmd.permission == null || sender.hasPermission(cmd.permission)) {
						lines.add(String.format("&e/%s %s &6- &7%s", label, cmd.name, cmd.description));
					}
				});
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.join("\n", lines)));
			} else sender.sendMessage(ChatColor.RED + Lang.translate("command.general.too-many-arguments", Replacements.singleton("label", label)));
			return true;
		}));
		
		// add list command
		subCommands.put("list", new SubCommand(Lang.translate("command.sub-commands.list.name"), Lang.translate("command.sub-commands.list.description"), "chatemojis.list", (sender, command, label, args) -> {
			if (args.length == 0) {
				if (sender instanceof Player) {
					// emoji list header
					TextComponent header = ComponentUtils.createComponent("&6ChatEmojis &7(v" + PLUGIN.getDescription().getVersion() + ")");

					// emoji list header hover component
					List<String> translators = Lang.getTranslators();
					List<String> authors = PLUGIN.getDescription().getAuthors();
					String[] lines = new String[] {
							ChatColor.GOLD + "ChatEmojis",
							ChatColor.GRAY + Lang.translate("command.general.version") +": " + ChatColor.YELLOW + PLUGIN.getDescription().getVersion(),
							ChatColor.GRAY + Lang.translate(translators.size() > 1 ? "command.general.authors" : "command.general.author") + ": " + ChatColor.YELLOW + String.join(ChatColor.GRAY + ", " + ChatColor.YELLOW, authors),
							ChatColor.GRAY + Lang.translate(translators.size() > 1 ? "command.general.translators" : "command.general.translator") + ": " + ChatColor.YELLOW + String.join(ChatColor.GRAY + ", " + ChatColor.YELLOW, translators),
							"",
							ChatColor.YELLOW + Lang.translate("command.general.click-to-open-spigot-page")
					};
					BaseComponent[] hoverMessage = ComponentUtils.createBaseComponent(String.join("\n", lines));

					// set the hover component for the header
					if (Version.getServerVersion().isOlderThan(Version.V1_16)) header.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverMessage));
					else header.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(hoverMessage)));

					// set the click event for the header
					header.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.spigotmc.org/resources/chatemojis.88027/"));

					// send the header
					((Player) sender).spigot().sendMessage(header);

					// get all components for all emojis
					List<BaseComponent[]> components = PLUGIN.getEmojis().getComponents((Player) sender);

					// keep looping until the list is empty
					// Emoji messages are sent in groups of 10, because
					// if too many are sent at once, it'll kick you out of the server
					// because it cant parse it (client-side) if the message
					// is longer than Integer.MAX_VALUE.
					// And I don't know how to calculate the length.
					while(!components.isEmpty()) {
						ComponentBuilder builder = new ComponentBuilder();

						// grab the first 10 items in list and add it to the builder
						List<BaseComponent[]> comps = components.subList(0, Math.min(components.size(), 9));
						for(int i = 0; i < comps.size(); i++) {
							if(i != 0) builder.append("\n");
							builder.append(comps.get(i));
						}

						// remove items from list
						components.removeAll(comps);

						// send message
						((Player) sender).spigot().sendMessage(builder.create());
					}
				} else sender.sendMessage(ChatColor.RED + Lang.translate("command.general.must-be-player-to-use-command"));
			} else sender.sendMessage(ChatColor.RED + Lang.translate("command.general.too-many-arguments", Replacements.singleton("label", label)));
			return true;
		}));
		
		// add reload command
		subCommands.put("reload", new SubCommand(Lang.translate("command.sub-commands.reload.name"), Lang.translate("command.sub-commands.reload.description"), "chatemojis.reload", (sender, command, label, args) -> {
			if (args.length == 0) {
				long start = System.currentTimeMillis();
				PLUGIN.reload();
				long interval = System.currentTimeMillis() - start;
				sender.sendMessage(ChatColor.GRAY + Lang.translate("command.sub-commands.reload.complete") + " " + ChatColor.YELLOW + String.format("(%sms)", interval));
			} else sender.sendMessage(ChatColor.RED + Lang.translate("command.general.too-many-arguments", Replacements.singleton("label", label)));
			return true;
		}));
		
		// add settings command
		subCommands.put("settings", new SubCommand(Lang.translate("command.sub-commands.settings.name"), Lang.translate("command.sub-commands.settings.description"), "chatemojis.admin", (sender, command, label, args) -> {
			if (args.length == 0) {
				if (sender instanceof Player) ((Player) sender).openInventory(settingsGui.inventory);
				else sender.sendMessage(ChatColor.RED + Lang.translate("command.general.must-be-player-to-use-command"));
			} else sender.sendMessage(ChatColor.RED + Lang.translate("command.general.too-many-arguments", Replacements.singleton("label", label)));
			return true;
		}));

		// add info command
		// TODO Finish info command
		subCommands.put("info", new SubCommand(Lang.translate("command.sub-commands.info.name"), Lang.translate("command.sub-commands.info.description"), (sender, command, label, args) -> {
			if (args.length == 0) sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&eWork In Progress"));
			else sender.sendMessage(ChatColor.RED + Lang.translate("command.general.too-many-arguments", Replacements.singleton("label", label)));
			return true;
		}));
	}
	
	/**
	 * Close all open inventories
	 * unregister all events and command handlers
	 * re-register everything
	 */
	static void reload() {
		// unregister executor and tab-completer
		PluginCommand command = PLUGIN.getCommand("emoji");
		if(command != null) {
			command.setExecutor(null);
			command.setTabCompleter(null);
		}
		
		// close all open inventories and unregister all listeners
		if(getHandler() != null) getHandler().settingsGui.disable(true);
		
		// re-register command and tab-completer
		// also makes new GUI instances
		init();
	}
	
	/**
	 * Close all open inventories
	 */
	static void disable() {
		// only close open inventories
		if(getHandler() != null) getHandler().settingsGui.disable(false);
	}
	
	private static class SubCommand {
		
		private final String name, description;
		private final Permission permission;
		private final CommandExecutor executor;
		
		private SubCommand(String name, String description, CommandExecutor executor) {
			this(name, description, (Permission) null, executor);
		}
		
		private SubCommand(String name, String description, String permission, CommandExecutor executor) {
			this(name, description, new Permission(permission), executor);
		}
		
		private SubCommand(String name, String description, Permission permisison, CommandExecutor executor) {
			this.name = name;
			this.description = description;
			this.permission = permisison;
			this.executor = executor;
		}
		
	}

}
