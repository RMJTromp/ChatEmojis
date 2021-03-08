package com.rmjtromp.chatemojis;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.inventory.meta.BookMeta;

import com.earth2me.essentials.User;
import com.rmjtromp.chatemojis.Settings.Service;

import net.ess3.api.events.PrivateMessagePreSendEvent;

@SuppressWarnings("deprecation")
final class EventHandler implements Listener {

	private static final ChatEmojis PLUGIN = ChatEmojis.getInstance();
	
	static void init() {
		new EventHandler();
	}
	
	private EventHandler() {
		PLUGIN.getServer().getPluginManager().registerEvents(this, PLUGIN);
		if(PLUGIN.essentialsIsLoaded) PLUGIN.getServer().getPluginManager().registerEvents(essentialsListener, PLUGIN);
	}
	
	/*
	 * Essentials listener, only gets registered if essentials is loaded
	 */
	private final Listener essentialsListener = new Listener() {
		
		@org.bukkit.event.EventHandler
		public void onPrivateMessagePreSend(PrivateMessagePreSendEvent e) {
			if(e.getSender() instanceof User) {
				Player sender = ((User) e.getSender()).getBase();
				
				if(PLUGIN.getSettings().canUtilize(Service.CHAT, sender.getWorld())) {
					e.setMessage(PLUGIN.getEmojis().parse(sender, ChatColor.getLastColors(e.getMessage()), e.getMessage()));
				}
			}
		}
		
	};

	@org.bukkit.event.EventHandler(priority = EventPriority.MONITOR)
    public void onPluginEnable(PluginEnableEvent e) {
    	if(e.getPlugin().getName().equals("PlaceholderAPI")) PLUGIN.papiIsLoaded = true;
    	if(e.getPlugin().getName().equals("Essentials") && !PLUGIN.essentialsIsLoaded) {
    		PLUGIN.essentialsIsLoaded = true;
    		PLUGIN.getServer().getPluginManager().registerEvents(essentialsListener, PLUGIN);
    	}
    }

	@org.bukkit.event.EventHandler(priority = EventPriority.MONITOR)
    public void onPluginDisable(PluginDisableEvent e) {
    	if(e.getPlugin().getName().equals("PlaceholderAPI")) PLUGIN.papiIsLoaded = false;
    	if(e.getPlugin().getName().equals("Essentials") && PLUGIN.essentialsIsLoaded) {
    		PLUGIN.essentialsIsLoaded = false;
    		HandlerList.unregisterAll(essentialsListener);
    	}
    }
	

	/*
	 * Parse texts in book (only if enabled).
	 * Priority is set to lowest as low priorities gets called first
	 * Therefore should convert emoticon before it's handled by other
	 * plugins (for example DiscordSRV)
	 */
	@org.bukkit.event.EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(AsyncPlayerChatEvent e) {
		if(PLUGIN.getSettings().canUtilize(Service.CHAT, e.getPlayer().getWorld())) {
	        String resetColor = ChatColor.RESET + ChatColor.getLastColors(e.getMessage());
	        e.setMessage(PLUGIN.getEmojis().parse(e.getPlayer(), resetColor, e.getMessage()));
		}
    }

	/*
	 * Parse texts on sign (only if enabled).
	 * Priority is set to lowest as low priorities gets called first
	 */
	@org.bukkit.event.EventHandler(priority = EventPriority.LOWEST)
    public void onSignChange(SignChangeEvent e) {
		if(PLUGIN.getSettings().canUtilize(Service.SIGNS, e.getPlayer().getWorld())) {
			String[] lines = e.getLines();
        	for(int i = 0; i < lines.length; i++) {
        		e.setLine(i, PLUGIN.getEmojis().parse(e.getPlayer(), ChatColor.RESET + ChatColor.getLastColors(lines[i]), lines[i]));
        	}
    	}
    }

	/*
	 * Parse texts in book (only if enabled).
	 * Priority is set to lowest as low priorities gets called first
	 */
	@org.bukkit.event.EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerBookEdit(PlayerEditBookEvent e) {
		if(PLUGIN.getSettings().canUtilize(Service.BOOKS, e.getPlayer().getWorld())) {
        	List<String> newContent = new ArrayList<>();
        	BookMeta meta = e.getNewBookMeta();
        	meta.getPages().forEach(string -> newContent.add(PLUGIN.getEmojis().parse(e.getPlayer(), ChatColor.RESET + ChatColor.getLastColors(string), string)));
        	meta.setPages(newContent);
        	e.setNewBookMeta(meta);
    	}
    }

	/*
	 * Auto complete emojis (only if chat emojis are enabled)
	 * This will automatically complete an emoticon on version that support Chat Tab-Completions
	 */
	public void onPlayerChatTabComplete(PlayerChatTabCompleteEvent e) {
		if(PLUGIN.getSettings().canUtilize(Service.CHAT, e.getPlayer().getWorld())) {
			if(!e.getLastToken().isEmpty()) {
				final String lastToken = e.getLastToken().toLowerCase();
				PLUGIN.getEmojis().forEach(abstractEmoji -> {
					if(abstractEmoji instanceof Emoji) {
						Emoji emoji = (Emoji) abstractEmoji;
						if(e.getPlayer().hasPermission(emoji.getPermission())) {
							emoji.getEmoticons().forEach(emoticon -> {
								if(lastToken.startsWith(emoticon.toLowerCase())) {
									e.getTabCompletions().add(emoticon);
								}
							});
						}
					}
				});
			}
		}
	}
	
}
