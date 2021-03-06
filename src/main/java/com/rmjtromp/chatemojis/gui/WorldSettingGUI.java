package com.rmjtromp.chatemojis.gui;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

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
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import com.rmjtromp.chatemojis.ChatEmojis;
import com.rmjtromp.chatemojis.Settings.Service;
import com.rmjtromp.chatemojis.utils.Lang;
import com.rmjtromp.chatemojis.utils.Lang.Replacements;
import com.rmjtromp.chatemojis.utils.bukkit.ExtendedItemStack;
import com.rmjtromp.chatemojis.utils.bukkit.Version;

/**
 * World Settings GUI, an inventory with toggle-able options
 * for a world.
 * @author Melvin
 * @since 2.2.1
 */
@SuppressWarnings("deprecation")
final class WorldSettingGUI {
	
	private static final ChatEmojis PLUGIN = ChatEmojis.getInstance();
	private static final ItemStack LIME_DYE, GRAY_DYE, BARRIER, BLACK_STAINED_GLASS_PANE;
	private static final Material GRASS_BLOCK, END_STONE, NETHERRACK, OAK_SIGN, ARROW, WRITEABLE_BOOK, PAPER, MAP;
	
	private final HashMap<UUID, Inventory> inventories = new HashMap<>();
	private final ExtendedItemStack close, back, pane, sign, book, chat;
	private final WorldSelectorGUI worldSelectorGUI;

	private Inventory inventory = null;
	
	static {
		LIME_DYE = Version.getServerVersion().isOlderThan(Version.V1_13) ? new ItemStack(Material.valueOf("INK_SACK"), 1, (short) 10) : new ItemStack(Material.LIME_DYE);
		GRAY_DYE = Version.getServerVersion().isOlderThan(Version.V1_13) ? new ItemStack(Material.valueOf("INK_SACK"), 1, (short) 8) : new ItemStack(Material.GRAY_DYE);
		BARRIER = Version.getServerVersion().isOlderThan(Version.V1_8) ? new ItemStack(Material.valueOf("STAINED_GLASS_PANE"), 1, (short) 14) : new ItemStack(Material.BARRIER);
		BLACK_STAINED_GLASS_PANE = Version.getServerVersion().isOlderThan(Version.V1_13) ? new ItemStack(Material.valueOf("STAINED_GLASS_PANE"), 1, (short) 15) : new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
		
		GRASS_BLOCK = Version.getServerVersion().isOlderThan(Version.V1_13) ? Material.valueOf("GRASS") : Material.GRASS_BLOCK;
		END_STONE = Version.getServerVersion().isOlderThan(Version.V1_13) ? Material.valueOf("ENDER_STONE") : Material.END_STONE;
		OAK_SIGN = Version.getServerVersion().isOlderThan(Version.V1_14) ? Material.valueOf("SIGN") : Material.OAK_SIGN;
		WRITEABLE_BOOK = Version.getServerVersion().isOlderThan(Version.V1_13) ? Material.valueOf("BOOK_AND_QUILL") : Material.WRITABLE_BOOK;
		NETHERRACK = Material.NETHERRACK;
		ARROW = Material.ARROW;
		PAPER = Material.PAPER;
		MAP = Version.getServerVersion().isOlderThan(Version.V1_13) ? Material.valueOf("EMPTY_MAP") : Material.MAP;
	}
	
	WorldSettingGUI(@NotNull WorldSelectorGUI worldSelectorGUI) {
		close = new ExtendedItemStack(BARRIER.clone());
		close.setDisplayName("&c"+Lang.translate("gui.general.close"));
		
		back = new ExtendedItemStack(ARROW);
		back.setDisplayName("&e"+Lang.translate("gui.general.go-back"));
		
		pane = new ExtendedItemStack(BLACK_STAINED_GLASS_PANE.clone());
		pane.setDisplayName("&0");
		
		sign = new ExtendedItemStack(OAK_SIGN);
		sign.setDisplayName("&6&l"+Lang.translate("gui.world-settings.signs.name"));
		
		book = new ExtendedItemStack(WRITEABLE_BOOK);
		book.setDisplayName("&6&l"+Lang.translate("gui.world-settings.books.name"));
		
		chat = new ExtendedItemStack(PAPER);
		chat.setDisplayName("&6&l"+Lang.translate("gui.world-settings.chat.name"));
		
		this.worldSelectorGUI = worldSelectorGUI;
		
		Bukkit.getServer().getPluginManager().registerEvents(listener, PLUGIN);
	}
	
	/**
	 * Return the inventory GUI of the world requested.<br>
	 * Providing null as a parameter will return an inventory for global settings.
	 * @param uid The world's UID
	 * @return {@link Inventory} GUI settings of the world
	 */
	Inventory get(UUID uid) {
		if(uid != null) {
			Inventory inventory;
			if(!inventories.containsKey(uid)) {
				World world = Bukkit.getServer().getWorld(uid);
				if(world != null) {
					inventory = Bukkit.createInventory(null, 54, ChatColor.DARK_GRAY+Lang.translate("gui.world-settings.title"));
					inventories.put(uid, inventory);
				} else return null;
			} else inventory = inventories.get(uid);

			// only 're-draw' it if there's no viewers
			if(inventory.getViewers().isEmpty()) {
				inventory.clear();
				
				for(int i = 0; i < 54; i++) inventory.setItem(i, pane);
				inventory.setItem(0, back);
				inventory.setItem(8, close);
				
				inventory.setItem(20, chat);
				inventory.setItem(22, sign);
				inventory.setItem(24, book);

				World world = PLUGIN.getServer().getWorld(uid);
				if(world != null) {
					Material material = GRASS_BLOCK;
					if(world.getEnvironment().equals(Environment.THE_END)) material = END_STONE;
					else if(world.getEnvironment().equals(Environment.NETHER)) material = NETHERRACK;
					
					final ExtendedItemStack item = new ExtendedItemStack(material);
					item.setDisplayName("&6&l"+world.getName());
					
					inventory.setItem(4, item);

					final String enabled = "&a"+Lang.translate("gui.general.state-enabled");
					final String disabled = "&c"+Lang.translate("gui.general.state-disabled");

					final boolean chatState = PLUGIN.getSettings().canUtilize(Service.CHAT, world);
					final ExtendedItemStack chat = new ExtendedItemStack(chatState ? LIME_DYE.clone() : GRAY_DYE.clone());
					chat.setDisplayName("&6&l"+Lang.translate("gui.world-settings.chat.name"));
					Arrays.asList(Lang.translate("gui.world-settings.chat.description", Replacements.singleton("state", chatState ? enabled : disabled)).split("\n")).stream().map(lore -> "&7"+lore).forEach(chat::addLore);
					
					final boolean signState = PLUGIN.getSettings().canUtilize(Service.SIGNS, world);
					final ExtendedItemStack sign = new ExtendedItemStack(signState ? LIME_DYE.clone() : GRAY_DYE.clone());
					sign.setDisplayName("&6&l"+Lang.translate("gui.world-settings.signs.name"));
					Arrays.asList(Lang.translate("gui.world-settings.signs.description", Replacements.singleton("state", signState ? enabled : disabled)).split("\n")).stream().map(lore -> "&7"+lore).forEach(sign::addLore);
					
					final boolean bookState = PLUGIN.getSettings().canUtilize(Service.BOOKS, world);
					final ExtendedItemStack book = new ExtendedItemStack(bookState ? LIME_DYE.clone() : GRAY_DYE.clone());
					book.setDisplayName("&6&l"+Lang.translate("gui.world-settings.books.name"));
					Arrays.asList(Lang.translate("gui.world-settings.books.description", Replacements.singleton("state", bookState ? enabled : disabled)).split("\n")).stream().map(lore -> "&7"+lore).forEach(book::addLore);
					
					inventory.setItem(29, chat);
					inventory.setItem(31, sign);
					inventory.setItem(33, book);
				}
			}
			
			return inventory;
		} else {
			if(inventory == null) inventory = Bukkit.createInventory(null, 54, ChatColor.DARK_GRAY+Lang.translate("gui.world-selector.global.name"));
			
			// only 're-draw' it if there's no viewers
			if(inventory.getViewers().isEmpty()) {
				inventory.clear();
				
				for(int i = 0; i < 54; i++) inventory.setItem(i, pane);
				inventory.setItem(0, back);
				inventory.setItem(8, close);
				
				inventory.setItem(20, chat);
				inventory.setItem(22, sign);
				inventory.setItem(24, book);

				final ExtendedItemStack map = new ExtendedItemStack(MAP);
				map.setDisplayName("&6&l"+Lang.translate("gui.world-selector.global.name"));
				
				final String toggleYes = "&a"+Lang.translate("gui.general.toggle-yes");
				final String toggleNo = "&c"+Lang.translate("gui.general.toggle-no");
				
				Replacements replacements = new Replacements();
				replacements.add("chat-toggle", PLUGIN.getSettings().canUtilize(Service.CHAT, null) ? toggleYes : toggleNo);
				replacements.add("book-toggle", PLUGIN.getSettings().canUtilize(Service.BOOKS, null) ? toggleYes : toggleNo);
				replacements.add("sign-toggle", PLUGIN.getSettings().canUtilize(Service.SIGNS, null) ? toggleYes : toggleNo);
				Arrays.asList(Lang.translate("gui.world-selector.global.description", replacements).split("\n")).stream().map(lore -> "&7"+lore).forEach(map::addLore);
				
				inventory.setItem(4, map);

				final String enabled = "&a"+Lang.translate("gui.general.state-enabled");
				final String disabled = "&c"+Lang.translate("gui.general.state-disabled");

				final boolean chatState = PLUGIN.getSettings().canUtilize(Service.CHAT, null);
				final ExtendedItemStack chat = new ExtendedItemStack(chatState ? LIME_DYE.clone() : GRAY_DYE.clone());
				chat.setDisplayName("&6&l"+Lang.translate("gui.world-settings.chat.name"));
				Arrays.asList(Lang.translate("gui.world-settings.chat.description", Replacements.singleton("state", chatState ? enabled : disabled)).split("\n")).stream().map(lore -> "&7"+lore).forEach(chat::addLore);
				
				final boolean signState = PLUGIN.getSettings().canUtilize(Service.SIGNS, null);
				final ExtendedItemStack sign = new ExtendedItemStack(signState ? LIME_DYE.clone() : GRAY_DYE.clone());
				sign.setDisplayName("&6&l"+Lang.translate("gui.world-settings.signs.name"));
				Arrays.asList(Lang.translate("gui.world-settings.signs.description", Replacements.singleton("state", signState ? enabled : disabled)).split("\n")).stream().map(lore -> "&7"+lore).forEach(sign::addLore);
				
				final boolean bookState = PLUGIN.getSettings().canUtilize(Service.BOOKS, null);
				final ExtendedItemStack book = new ExtendedItemStack(bookState ? LIME_DYE.clone() : GRAY_DYE.clone());
				book.setDisplayName("&6&l"+Lang.translate("gui.world-settings.books.name"));
				Arrays.asList(Lang.translate("gui.world-settings.books.description", Replacements.singleton("state", bookState ? enabled : disabled)).split("\n")).stream().map(lore -> "&7"+lore).forEach(book::addLore);
				
				inventory.setItem(29, chat);
				inventory.setItem(31, sign);
				inventory.setItem(33, book);
			}
			
			return inventory;
		}
	}

	void disable(boolean deep) {
		// convert viewers to Player list instead, because immediately closing the inventory from the HumanEntity list
		// will throw ConcurrentModificationException
		if(inventory != null) inventory.getViewers().stream().map(v -> (Player)v).forEach(Player::closeInventory);
		inventories.values().forEach(i -> i.getViewers().stream().map(v -> (Player)v).forEach(Player::closeInventory));
		if(deep) HandlerList.unregisterAll(listener);
	}
	
	private Listener listener = new Listener() {
		
		@EventHandler
		public void onWorldUnload(WorldUnloadEvent e) {
			if(inventories.containsKey(e.getWorld().getUID())) {
				inventories.get(e.getWorld().getUID()).getViewers().forEach(viewer -> {
					viewer.closeInventory();
					viewer.sendMessage(ChatColor.RED+Lang.translate("gui.world-selector.error.world-unloaded"));
				});
				inventories.remove(e.getWorld().getUID());
			}
		}
		
		@EventHandler
	    public void onInventoryDrag(InventoryDragEvent e) {
	    	if(inventory != null && inventory.equals(e.getInventory())) e.setCancelled(true);
	    	else {
				for(Inventory inventory : inventories.values()) {
			    	if(inventory.equals(e.getInventory())) {
			    		e.setCancelled(true);
			    		break;
			    	}
				}
	    	}
	    }
	    
	    @EventHandler
	    public void onInventoryInteract(InventoryInteractEvent e) {
	    	if(inventory != null && inventory.equals(e.getInventory())) e.setCancelled(true);
	    	else {
				for(Inventory inventory : inventories.values()) {
			    	if(inventory.equals(e.getInventory())) {
			    		e.setCancelled(true);
			    		break;
			    	}
				}
	    	}
	    }
	    
	    @EventHandler
	    public void onInventoryMove(InventoryMoveItemEvent e) {
	    	if(inventory != null && inventory.equals(e.getInitiator())) e.setCancelled(true);
	    	else {
				for(Inventory inventory : inventories.values()) {
					if(inventory.equals(e.getInitiator())) {
						e.setCancelled(true);
						break;
					}
		    	}
	    	}
	    }

	    @EventHandler
	    public void onInventoryClick(InventoryClickEvent e) {
	    	if(inventory != null && inventory.equals(e.getInventory())) {
	    		e.setCancelled(true);
	    		if(close.equals(e.getCurrentItem())) e.getWhoClicked().closeInventory();
	    		else if(back.equals(e.getCurrentItem())) e.getWhoClicked().openInventory(worldSelectorGUI.inventories.get(0));
	    		else if(!pane.equals(e.getCurrentItem()) && !sign.equals(e.getCurrentItem()) && !chat.equals(e.getCurrentItem()) && !book.equals(e.getCurrentItem())) {
	    			final ExtendedItemStack item = new ExtendedItemStack(e.getCurrentItem());
	    			final boolean isLimeDye = Version.getServerVersion().isOlderThan(Version.V1_13) ? item.getType().equals(LIME_DYE.getType()) && item.getDurability() == (short) 10 : item.getType().equals(LIME_DYE.getType());
	    			final boolean isGrayDye = Version.getServerVersion().isOlderThan(Version.V1_13) ? item.getType().equals(GRAY_DYE.getType()) && item.getDurability() == (short) 8 : item.getType().equals(GRAY_DYE.getType());
	    			
	    			if(isLimeDye || isGrayDye) {
	    				if(e.getSlot() == 29) {
	    					if(e.getClick().isLeftClick() || e.getClick().isRightClick()) {
	    						if(e.getClick().isLeftClick() || !PLUGIN.getSettings().canUtilize(Service.CHAT, null)) PLUGIN.getSettings().toggleService(Service.CHAT, null);
								final boolean toggle = PLUGIN.getSettings().canUtilize(Service.CHAT, null);

								// update item
								final ExtendedItemStack chat = new ExtendedItemStack(toggle ? LIME_DYE.clone() : GRAY_DYE.clone());
								chat.setDisplayName("&6&l"+Lang.translate("gui.world-settings.chat.name"));
								Arrays.asList(Lang.translate("gui.world-settings.chat.description", Replacements.singleton("state", toggle ? "&a"+Lang.translate("gui.general.state-enabled") : "&c"+Lang.translate("gui.general.state-disabled"))).split("\n")).stream().map(lore -> "&7"+lore).forEach(chat::addLore);
								
								inventory.setItem(e.getSlot(), chat);
								
								((Player) e.getWhoClicked()).sendMessage(ChatColor.GRAY+Lang.translate("gui.world-settings.toggles.global.chat."+(toggle ? "enabled" : "disabled")));
								worldSelectorGUI.updateItem(null);
	    					}
	    				} else if(e.getSlot() == 31) {
	    					if(e.getClick().isLeftClick() || e.getClick().isRightClick()) {
	    						if(e.getClick().isLeftClick() || !PLUGIN.getSettings().canUtilize(Service.SIGNS, null)) PLUGIN.getSettings().toggleService(Service.SIGNS, null);
								final boolean toggle = PLUGIN.getSettings().canUtilize(Service.SIGNS, null);

								// update item
								final ExtendedItemStack sign = new ExtendedItemStack(toggle ? LIME_DYE.clone() : GRAY_DYE.clone());
								sign.setDisplayName("&6&l"+Lang.translate("gui.world-settings.signs.name"));
								Arrays.asList(Lang.translate("gui.world-settings.signs.description", Replacements.singleton("state", toggle ? "&a"+Lang.translate("gui.general.state-enabled") : "&c"+Lang.translate("gui.general.state-disabled"))).split("\n")).stream().map(lore -> "&7"+lore).forEach(sign::addLore);
								
								inventory.setItem(e.getSlot(), sign);
								
								((Player) e.getWhoClicked()).sendMessage(ChatColor.GRAY+Lang.translate("gui.world-settings.toggles.global.signs."+(toggle ? "enabled" : "disabled")));
								worldSelectorGUI.updateItem(null);
	    					}
	    				} else if(e.getSlot() == 33) {
	    					if(e.getClick().isLeftClick() || e.getClick().isRightClick()) {
	    						if(e.getClick().isLeftClick() || !PLUGIN.getSettings().canUtilize(Service.BOOKS, null)) PLUGIN.getSettings().toggleService(Service.BOOKS, null);
								final boolean toggle = PLUGIN.getSettings().canUtilize(Service.BOOKS, null);

								// update item
								final ExtendedItemStack book = new ExtendedItemStack(toggle ? LIME_DYE.clone() : GRAY_DYE.clone());
								book.setDisplayName("&6&l"+Lang.translate("gui.world-settings.books.name"));
								Arrays.asList(Lang.translate("gui.world-settings.books.description", Replacements.singleton("state", toggle ? "&a"+Lang.translate("gui.general.state-enabled") : "&c"+Lang.translate("gui.general.state-disabled"))).split("\n")).stream().map(lore -> "&7"+lore).forEach(book::addLore);
								
								inventory.setItem(e.getSlot(), book);
								
								((Player) e.getWhoClicked()).sendMessage(ChatColor.GRAY+Lang.translate("gui.world-settings.toggles.global.books."+(toggle ? "enabled" : "disabled")));
								worldSelectorGUI.updateItem(null);
	    					}
	    				}
	    			}
	    		}
	    	} else {
				for(Entry<UUID, Inventory> entry  : inventories.entrySet()) {
					UUID uid = entry.getKey();
					Inventory inventory = entry.getValue();
					
			    	if(inventory.equals(e.getInventory())) {
			    		e.setCancelled(true);
			    		if(e.getCurrentItem() != null) {
			    			if(close.equals(e.getCurrentItem())) e.getWhoClicked().closeInventory();
			    			else if(back.equals(e.getCurrentItem())) e.getWhoClicked().openInventory(worldSelectorGUI.inventories.get(0));
			    			else if(!pane.equals(e.getCurrentItem()) && !sign.equals(e.getCurrentItem()) && !chat.equals(e.getCurrentItem()) && !book.equals(e.getCurrentItem())) {
			    				ExtendedItemStack item = new ExtendedItemStack(e.getCurrentItem());
								World world = Bukkit.getWorld(uid);
								
								final boolean isLimeDye = Version.getServerVersion().isOlderThan(Version.V1_13) ? item.getType().equals(LIME_DYE.getType()) && item.getDurability() == (short) 10 : item.getType().equals(LIME_DYE.getType());
				    			final boolean isGrayDye = Version.getServerVersion().isOlderThan(Version.V1_13) ? item.getType().equals(GRAY_DYE.getType()) && item.getDurability() == (short) 8 : item.getType().equals(GRAY_DYE.getType());
				    			
				    			if(isLimeDye || isGrayDye) {
				    				if(e.getSlot() == 29) {
				    					if(e.getClick().isLeftClick() || e.getClick().isRightClick()) {
				    						if(e.getClick().isLeftClick() || !PLUGIN.getSettings().canUtilize(Service.CHAT, world)) PLUGIN.getSettings().toggleService(Service.CHAT, world);
											final boolean toggle = PLUGIN.getSettings().canUtilize(Service.CHAT, world);

											// update item
											final ExtendedItemStack chat = new ExtendedItemStack(toggle ? LIME_DYE.clone() : GRAY_DYE.clone());
											chat.setDisplayName("&6&l"+Lang.translate("gui.world-settings.chat.name"));
											Arrays.asList(Lang.translate("gui.world-settings.chat.description", Replacements.singleton("state", toggle ? "&a"+Lang.translate("gui.general.state-enabled") : "&c"+Lang.translate("gui.general.state-disabled"))).split("\n")).stream().map(lore -> "&7"+lore).forEach(chat::addLore);
											
											inventory.setItem(e.getSlot(), chat);
											
											((Player) e.getWhoClicked()).sendMessage(ChatColor.GRAY+Lang.translate("gui.world-settings.toggles.world.chat."+(toggle ? "enabled" : "disabled"), Replacements.singleton("world", world.getName())));
											worldSelectorGUI.updateItem(world);
				    					}
				    				} else if(e.getSlot() == 31) {
				    					if(e.getClick().isLeftClick() || e.getClick().isRightClick()) {
				    						if(e.getClick().isLeftClick() || !PLUGIN.getSettings().canUtilize(Service.SIGNS, world)) PLUGIN.getSettings().toggleService(Service.SIGNS, world);
											final boolean toggle = PLUGIN.getSettings().canUtilize(Service.SIGNS, world);

											// update item
											final ExtendedItemStack sign = new ExtendedItemStack(toggle ? LIME_DYE.clone() : GRAY_DYE.clone());
											sign.setDisplayName("&6&l"+Lang.translate("gui.world-settings.signs.name"));
											Arrays.asList(Lang.translate("gui.world-settings.signs.description", Replacements.singleton("state", toggle ? "&a"+Lang.translate("gui.general.state-enabled") : "&c"+Lang.translate("gui.general.state-disabled"))).split("\n")).stream().map(lore -> "&7"+lore).forEach(sign::addLore);
											
											inventory.setItem(e.getSlot(), sign);
											
											((Player) e.getWhoClicked()).sendMessage(ChatColor.GRAY+Lang.translate("gui.world-settings.toggles.world.signs."+(toggle ? "enabled" : "disabled"), Replacements.singleton("world", world.getName())));
											worldSelectorGUI.updateItem(world);
				    					}
				    				} else if(e.getSlot() == 33) {
				    					if(e.getClick().isLeftClick() || e.getClick().isRightClick()) {
				    						if(e.getClick().isLeftClick() || !PLUGIN.getSettings().canUtilize(Service.BOOKS, world)) PLUGIN.getSettings().toggleService(Service.BOOKS, world);
											final boolean toggle = PLUGIN.getSettings().canUtilize(Service.BOOKS, world);

											// update item
											final ExtendedItemStack book = new ExtendedItemStack(toggle ? LIME_DYE.clone() : GRAY_DYE.clone());
											book.setDisplayName("&6&l"+Lang.translate("gui.world-settings.books.name"));
											Arrays.asList(Lang.translate("gui.world-settings.books.description", Replacements.singleton("state", toggle ? "&a"+Lang.translate("gui.general.state-enabled") : "&c"+Lang.translate("gui.general.state-disabled"))).split("\n")).stream().map(lore -> "&7"+lore).forEach(book::addLore);
											
											inventory.setItem(e.getSlot(), book);
											
											((Player) e.getWhoClicked()).sendMessage(ChatColor.GRAY+Lang.translate("gui.world-settings.toggles.world.books."+(toggle ? "enabled" : "disabled"), Replacements.singleton("world", world.getName())));
											worldSelectorGUI.updateItem(world);
				    					}
				    				}
				    			}
			    			}
			    		}
			    		break;
			    	}
				}
	    	}
	    }
		
	};

}
