package com.rmjtromp.chatemojis;

import com.earth2me.essentials.User;
import net.ess3.api.events.PrivateMessagePreSendEvent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.inventory.meta.BookMeta;

import java.util.ArrayList;
import java.util.List;

class PluginListeners implements Listener {

    private static final ChatEmojis PLUGIN = ChatEmojis.getInstance();

    private final Listener ESSENTIALS_LISTENER = new Listener() {

        @EventHandler(ignoreCancelled = true)
        public void onPrivateMessagePreSend(PrivateMessagePreSendEvent e) {
            if(e.getSender() instanceof User) {
                Player sender = ((User) e.getSender()).getBase();
                String resetColor = ChatColor.RESET + ChatColor.getLastColors(e.getMessage());
                e.setMessage(PLUGIN.emojis.parse(sender, resetColor, e.getMessage(), false));
            }
        }

    };

    @EventHandler(ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent e) {
        String resetColor = ChatColor.RESET + ChatColor.getLastColors(e.getMessage());
        e.setMessage(PLUGIN.emojis.parse(e.getPlayer(), resetColor, e.getMessage(), false));
    }

    @EventHandler
    public void onPluginEnable(PluginEnableEvent e) {
        switch(e.getPlugin().getName()) {
            case "PlaceholderAPI":
                PLUGIN.papiIsLoaded = true;
                break;
            case "Essentials":
                PLUGIN.getServer().getPluginManager().registerEvents(ESSENTIALS_LISTENER, PLUGIN);
                break;
        }
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent e) {
        switch(e.getPlugin().getName()) {
            case "PlaceholderAPI":
                PLUGIN.papiIsLoaded = false;
                break;
            case "Essentials":
                HandlerList.unregisterAll(ESSENTIALS_LISTENER);
                break;
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onSignChange(SignChangeEvent e) {
        if(Boolean.TRUE.equals(PLUGIN.useOnSigns.getValue())) {
            for(int i = 0; i < e.getLines().length; i++) {
                String line = e.getLine(i);
                assert line != null;
                String resetColor = ChatColor.RESET + ChatColor.getLastColors(line);
                e.setLine(i, PLUGIN.emojis.parse(e.getPlayer(), resetColor, line, false));
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerBookEdit(PlayerEditBookEvent e) {
        if(Boolean.TRUE.equals(PLUGIN.useInBooks.getValue())) {
            List<String> newContent = new ArrayList<>();
            BookMeta meta = e.getNewBookMeta();
            meta.getPages().forEach(string -> {
                String resetColor = ChatColor.RESET + ChatColor.getLastColors(string);
                newContent.add(PLUGIN.emojis.parse(e.getPlayer(), resetColor, string, false));
            });
            meta.setPages(newContent);
            e.setNewBookMeta(meta);
        }
    }

}
