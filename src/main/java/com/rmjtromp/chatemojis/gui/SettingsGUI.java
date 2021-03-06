package com.rmjtromp.chatemojis.gui;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.rmjtromp.chatemojis.ChatEmojis;
import com.rmjtromp.chatemojis.Settings;
import com.rmjtromp.chatemojis.utils.Lang;
import com.rmjtromp.chatemojis.utils.bukkit.ExtendedItemStack;
import com.rmjtromp.chatemojis.utils.bukkit.Version;

import net.md_5.bungee.api.ChatColor;

/**
 * Settings GUI is an inventory. This inventory contains items
 * that opens other inventories upon interaction to manipulate {@link ChatEmojis}' plugins {@link Settings}
 * @author Melvin
 * @since 2.2.1
 * @see {@link Settings}
 * @see {@link WorldSelectorGUI}
 */
@SuppressWarnings("deprecation")
public final class SettingsGUI {
	
	final WorldSelectorGUI worldSelectorGUI = new WorldSelectorGUI();
	public final Inventory inventory;
	final ExtendedItemStack close, pluginSettings, worldSettings;
	
	private static final ItemStack BARRIER;
	private static final Material CHEST, ENDER_CHEST;
	
	static {
		BARRIER = Version.getServerVersion().isOlderThan(Version.V1_8) ? new ItemStack(Material.valueOf("STAINED_GLASS_PANE"), 1, (short) 14) : new ItemStack(Material.BARRIER);

		CHEST = Material.CHEST;
		ENDER_CHEST = Material.ENDER_CHEST;
	}

	public SettingsGUI() {
		inventory = Bukkit.createInventory(null, 9, ChatColor.DARK_GRAY+Lang.translate("gui.settings.title"));
		
		pluginSettings = new ExtendedItemStack(ENDER_CHEST);
		pluginSettings.setDisplayName("&6&l"+Lang.translate("gui.settings.plugin.name"));
		Arrays.asList(Lang.translate("gui.settings.plugin.description").split("\n")).stream().map(lore -> "&7"+lore).forEach(pluginSettings::addLore);
		
		worldSettings = new ExtendedItemStack(CHEST);
		worldSettings.setDisplayName("&6&l"+Lang.translate("gui.settings.world.name"));
		Arrays.asList(Lang.translate("gui.settings.world.description").split("\n")).stream().map(lore -> "&7"+lore).forEach(worldSettings::addLore);
		
		close = new ExtendedItemStack(BARRIER);
		close.setDisplayName("&c"+Lang.translate("gui.general.close"));

		inventory.setItem(0, pluginSettings);
		inventory.setItem(1, worldSettings);
		inventory.setItem(8, close);
		
		Bukkit.getPluginManager().registerEvents(listener, ChatEmojis.getInstance());
	}

	/**
	 * a <code>shallow</code> disable will close every open inventories.
	 * where as a deep disable will close every open inventories and unregister all events tied to the inventories.
	 * @param deep Whether or not events should be unregistered
	 */
	public void disable(boolean deep) {
		// convert viewers to Player list instead, because immediately closing the inventory from the HumanEntity list
		// will throw ConcurrentModificationException
		inventory.getViewers().stream().map(v -> (Player) v).forEach(Player::closeInventory);
		if(deep) HandlerList.unregisterAll(listener);
		worldSelectorGUI.disable(deep);
	}
	
	private Listener listener = new Listener() {

		@EventHandler
	    public void onInventoryDrag(InventoryDragEvent e) {
	    	if(inventory.equals(e.getInventory())) e.setCancelled(true);
	    }
	    
	    @EventHandler
	    public void onInventoryInteract(InventoryInteractEvent e) {
	    	if(inventory.equals(e.getInventory())) e.setCancelled(true);
	    }
	    
	    @EventHandler
	    public void onInventoryMove(InventoryMoveItemEvent e) {
	    	if(inventory.equals(e.getInitiator())) e.setCancelled(true);
	    }

	    @EventHandler
	    public void onInventoryClick(InventoryClickEvent e) {
	    	if(inventory.equals(e.getInventory())) {
	    		e.setCancelled(true);
	    		if(e.getCurrentItem() != null) {
	    			if(close.equals(e.getCurrentItem())) e.getWhoClicked().closeInventory();
	    			else if(worldSettings.equals(e.getCurrentItem())) e.getWhoClicked().openInventory(worldSelectorGUI.inventories.get(0));
	    		}
	    	}
	    }
	    
	};

}
