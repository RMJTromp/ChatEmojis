package com.rmjtromp.chatemojis.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.rmjtromp.chatemojis.ChatEmojis;
import com.rmjtromp.chatemojis.Settings.Service;
import com.rmjtromp.chatemojis.utils.Lang;
import com.rmjtromp.chatemojis.utils.Lang.Replacements;
import com.rmjtromp.chatemojis.utils.bukkit.ExtendedItemStack;
import com.rmjtromp.chatemojis.utils.bukkit.Version;

/**
 * World Selector GUI is an inventory manager. Every inventories are different pages 
 * containing items that represents all worlds that server contains.
 * @author Melvin
 * @since 2.2.1
 * @see {@link WorldSettingGUI}
 */
@SuppressWarnings("deprecation")
public final class WorldSelectorGUI {
	
	private static final ChatEmojis PLUGIN = ChatEmojis.getInstance();
	private static final ItemStack BARRIER, BLACK_STAINED_GLASS_PANE;
	private static final Material GRASS_BLOCK, END_STONE, NETHERRACK, ARROW, MAP;

	final List<Inventory> inventories = new ArrayList<>();
	private final WorldSettingGUI worldSettingGUI = new WorldSettingGUI(this);
	
	private final ExtendedItemStack global, close, previous, next, pane;
	
	static {
		BARRIER = Version.getServerVersion().isOlderThan(Version.V1_8) ? new ItemStack(Material.valueOf("STAINED_GLASS_PANE"), 1, (short) 14) : new ItemStack(Material.BARRIER);
		BLACK_STAINED_GLASS_PANE = Version.getServerVersion().isOlderThan(Version.V1_13) ? new ItemStack(Material.valueOf("STAINED_GLASS_PANE"), 1, (short) 15) : new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
		
		GRASS_BLOCK = Version.getServerVersion().isOlderThan(Version.V1_13) ? Material.valueOf("GRASS") : Material.GRASS_BLOCK;
		END_STONE = Version.getServerVersion().isOlderThan(Version.V1_13) ? Material.valueOf("ENDER_STONE") : Material.END_STONE;
		NETHERRACK = Material.NETHERRACK;
		ARROW = Material.ARROW;
		MAP = Version.getServerVersion().isOlderThan(Version.V1_13) ? Material.valueOf("EMPTY_MAP") : Material.MAP;
	}
	
	public WorldSelectorGUI() {
		global = new ExtendedItemStack(MAP);
		global.setDisplayName("&6&l"+Lang.translate("gui.world-selector.global.name"));

		final String toggleYes = "&a"+Lang.translate("gui.general.toggle-yes");
		final String toggleNo = "&c"+Lang.translate("gui.general.toggle-no");
		
		Replacements replacements = new Replacements();
		replacements.add("chat-toggle", PLUGIN.getSettings().canUtilize(Service.CHAT, null) ? toggleYes : toggleNo);
		replacements.add("book-toggle", PLUGIN.getSettings().canUtilize(Service.BOOKS, null) ? toggleYes : toggleNo);
		replacements.add("sign-toggle", PLUGIN.getSettings().canUtilize(Service.SIGNS, null) ? toggleYes : toggleNo);
		Arrays.asList(Lang.translate("gui.world-selector.global.description", replacements).split("\n")).stream().map(lore -> "&7"+lore).forEach(global::addLore);
		
		close = new ExtendedItemStack(BARRIER);
		close.setDisplayName("&c"+Lang.translate("gui.general.close"));

		previous = new ExtendedItemStack(ARROW);
		previous.setDisplayName("&e"+Lang.translate("gui.general.previous-page"));
		
		next = new ExtendedItemStack(ARROW);
		next.setDisplayName("&e"+Lang.translate("gui.general.next-page"));
		
		pane = new ExtendedItemStack(BLACK_STAINED_GLASS_PANE);
		pane.setDisplayName("&0");

		updateInventories();
		
		Bukkit.getServer().getPluginManager().registerEvents(listener, ChatEmojis.getInstance());
	}
	
	/**
	 * Updates the description of the item assigned to a specific world;
	 * use <code>null</code> for global.
	 * @param world
	 */
	void updateItem(World world) {
		if(world == null) {
			// literally every item needs to be updated
			// why not just update the whole inventory, its easier
			updateInventories();
		} else {
			final String toggleYes = "&a"+Lang.translate("gui.general.toggle-yes");
			final String toggleNo = "&c"+Lang.translate("gui.general.toggle-no");
			
			ExtendedItemStack item = items.get(world);
			for(Inventory inventory : inventories) {
				// i'm only doing this because if the ItemStack is compared to ExtendedItemStack, it'll return false
				// so to ensure that ExtendedItemStack is being compared to ItemStack, i'm comparing all personally
				// and keeping the ItemStack so I can grab the slot of the item.
				ItemStack ii = null;
				for(ItemStack i : inventory.getContents()) {
					if(i != null && item.equals(i)) {
						ii = i;
						break;
					}
				}
				
				if(ii != null) {
					int slot = inventory.first(ii);
					Replacements replacements = new Replacements();
					replacements.add("chat-toggle", PLUGIN.getSettings().canUtilize(Service.CHAT, world) ? toggleYes : toggleNo);
					replacements.add("book-toggle", PLUGIN.getSettings().canUtilize(Service.BOOKS, world) ? toggleYes : toggleNo);
					replacements.add("sign-toggle", PLUGIN.getSettings().canUtilize(Service.SIGNS, world) ? toggleYes : toggleNo);
					
					item.removeLores();
					Arrays.asList(Lang.translate("gui.world-selector.world.description", replacements).split("\n")).stream().map(lore -> "&7"+lore).forEach(item::addLore);
					
					inventory.setItem(slot, item);
					items.replace(world, item);
					break;
				}
			}
		}
	}
	
	/**
	 * Updates the items in all inventories
	 */
	private HashMap<World, ExtendedItemStack> items = new HashMap<>();
	private void updateInventories() {
		final List<World> worlds = Bukkit.getServer().getWorlds();
		final int pages =  (worlds.size() + 1) / 36 + ((worlds.size() + 1) % 36 == 0 ? 0 : 1);
		
		final String toggleYes = "&a"+Lang.translate("gui.general.toggle-yes");
		final String toggleNo = "&c"+Lang.translate("gui.general.toggle-no");
		
		inventories.clear();
		items.clear();
		for(int p = 0; p < pages; p++) {
			final int offset = p * 36;
			final int limit = p + 1 != pages ? 36 : worlds.size() % 36;
			
			String title = Lang.translate("gui.world-selector.title");
			if(pages > 1) title += String.format(" (%s/%s)", p+1, pages);
			final Inventory inventory = Bukkit.createInventory(null, 54, ChatColor.DARK_GRAY+title);

			inventory.setItem(0, global);
			inventory.setItem(8, close);

			for(int i = 1; i < 8; i++) inventory.setItem(i, pane);
			for(int i = 45; i < 54; i++) inventory.setItem(i, pane);

			if(p > 0) inventory.setItem(45, previous);
			if(pages > 1 && p + 1 != pages) inventory.setItem(53, next);
			
			for(int i = offset; i < (offset + limit); i++) {
				final int slot = 9 + i - offset;
				final World world = worlds.get(i);
				
				Material material = GRASS_BLOCK;
				if(world.getEnvironment().equals(Environment.NETHER)) material = NETHERRACK;
				else if(world.getEnvironment().equals(Environment.THE_END)) material = END_STONE;
				
				final ExtendedItemStack item = new ExtendedItemStack(material);
				item.setDisplayName("&6&l"+world.getName());
				
				Replacements replacements = new Replacements();
				replacements.add("chat-toggle", PLUGIN.getSettings().canUtilize(Service.CHAT, world) ? toggleYes : toggleNo);
				replacements.add("book-toggle", PLUGIN.getSettings().canUtilize(Service.BOOKS, world) ? toggleYes : toggleNo);
				replacements.add("sign-toggle", PLUGIN.getSettings().canUtilize(Service.SIGNS, world) ? toggleYes : toggleNo);
				Arrays.asList(Lang.translate("gui.world-selector.world.description", replacements).split("\n")).stream().map(lore -> "&7"+lore).forEach(item::addLore);
				
				items.put(world, item);
				
				inventory.setItem(slot, item);
			}
			inventories.add(inventory);
		}
	}

	/**
	 * Closes all open inventories
	 */
	void disable(boolean deep) {
		// convert viewers to Player list instead, because immediately closing the inventory from the HumanEntity list
		// will throw ConcurrentModificationException
		inventories.forEach(i -> i.getViewers().stream().map(v -> (Player) v).forEach(Player::closeInventory));
		if(deep) HandlerList.unregisterAll(listener);
		worldSettingGUI.disable(deep);
	}
	
	private Listener listener = new Listener() {
		
		@EventHandler
		public void onWorldLoad(WorldLoadEvent e) {
			updateInventories();
		}
		
		@EventHandler
		public void onWorldUnload(WorldUnloadEvent e) {
			updateInventories();
		}
		
		@EventHandler
	    public void onInventoryDrag(InventoryDragEvent e) {
			for(Inventory inventory : inventories) {
		    	if(inventory.equals(e.getInventory())) {
		    		e.setCancelled(true);
		    		break;
		    	}
			}
	    }
	    
	    @EventHandler
	    public void onInventoryInteract(InventoryInteractEvent e) {
			for(Inventory inventory : inventories) {
		    	if(inventory.equals(e.getInventory())) {
		    		e.setCancelled(true);
		    		break;
		    	}
			}
	    }
	    
	    @EventHandler
	    public void onInventoryMove(InventoryMoveItemEvent e) {
			for(Inventory inventory : inventories) {
				if(inventory.equals(e.getInitiator())) {
					e.setCancelled(true);
					break;
				}
	    	}
	    }

	    @EventHandler
	    public void onInventoryClick(InventoryClickEvent e) {
			for(int i = 0; i < inventories.size(); i++) {
				Inventory inventory = inventories.get(i);
		    	if(inventory.equals(e.getInventory())) {
		    		e.setCancelled(true);
		    		if(e.getCurrentItem() != null) {
		    			if(close.equals(e.getCurrentItem())) e.getWhoClicked().closeInventory();
		    			else if(previous.equals(e.getCurrentItem())) e.getWhoClicked().openInventory(inventories.get(i-1));
		    			else if(next.equals(e.getCurrentItem())) e.getWhoClicked().openInventory(inventories.get(i+1));
		    			else if(global.equals(e.getCurrentItem())) {
		    				if(e.getClick().isLeftClick()) e.getWhoClicked().openInventory(worldSettingGUI.get(null));
		    				else if(e.getClick().isRightClick()) {
		    					for(Service service : Service.values()) {
		    						if(!PLUGIN.getSettings().canUtilize(service, null)) PLUGIN.getSettings().toggleService(service, null);
		    					}
		    					updateItem(null);
		    					((Player) e.getWhoClicked()).sendMessage(ChatColor.GRAY+Lang.translate("gui.world-settings.toggles.global.restored"));
		    				}
		    			} else if(!pane.equals(e.getCurrentItem())) {
		    				if(e.getClick().isLeftClick() || e.getClick().isRightClick()) {
				    			items.entrySet().stream().filter(entry -> entry.getValue().equals(e.getCurrentItem())).findFirst().ifPresent(entry -> {
				    				if(e.getClick().isLeftClick()) {
					    				Inventory inv = worldSettingGUI.get(entry.getKey().getUID());
										if(inv != null) e.getWhoClicked().openInventory(inv);
										else ((Player) e.getWhoClicked()).sendMessage(ChatColor.RED+Lang.translate("gui.world-selector.error.world-not-found"));
				    				} else {
				    					for(Service service : Service.values()) {
				    						if(PLUGIN.getSettings().canUtilize(service, entry.getKey()) != PLUGIN.getSettings().canUtilize(service, null)) {
						    					PLUGIN.getSettings().toggleService(service, entry.getKey());
				    						}
				    					}
				    					updateItem(entry.getKey());
				    					((Player) e.getWhoClicked()).sendMessage(ChatColor.GRAY+Lang.translate("gui.world-settings.toggles.world.restored", Replacements.singleton("world", entry.getKey().getName())));
				    				}
				    			});
		    				}
		    			}
		    		}
		    		break;
		    	}
			}
	    }
		
	};
	
}
