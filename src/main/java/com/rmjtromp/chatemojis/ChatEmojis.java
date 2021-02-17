package com.rmjtromp.chatemojis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import com.rmjtromp.chatemojis.exceptions.ConfigException;
import com.rmjtromp.chatemojis.utils.ComponentUtils;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

public final class ChatEmojis extends JavaPlugin {

    protected static final List<String> RESERVED_NAMES = Arrays.asList("emoticon", "emoji", "regex", "enabled");
    protected static final Pattern NAME_PATTERN = Pattern.compile("(?<=\\.)?([^\\.]+?)$", Pattern.CASE_INSENSITIVE);

    private EmojiGroup emojis = null;
    private static ChatEmojis plugin;
    private boolean papiIsLoaded = false;
    private boolean useOnSigns = true;
    private boolean useInBooks = true;
    private Inventory settingsGui = null;

    public ChatEmojis() {
        plugin = this;
    }

	@Override
	public void onEnable() {
        saveDefaultConfig();
        
        if(getConfig().isSet("settings.use.books") && getConfig().isBoolean("settings.use.books")) {
        	if(getConfig().getBoolean("settings.use.books")) getConfig().set("settings.use.books", null);
        	else useInBooks = false;
        }

        if(getConfig().isSet("settings.use.signs") && getConfig().isBoolean("settings.use.signs")) {
        	if(getConfig().getBoolean("settings.use.signs")) getConfig().set("settings.use.signs", null);
        	else useOnSigns = false;
        }
        
        try {
            emojis = EmojiGroup.init(getConfig());
        } catch (ConfigException e) {
            e.printStackTrace();
        }
        
        papiIsLoaded = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
        settingsGui = Bukkit.createInventory(null, InventoryType.HOPPER, ChatColor.DARK_GRAY+"ChatEmojis Settings");
        
        ItemStack barrier = new ItemStack(Material.BARRIER);
        ItemMeta barrierMeta = barrier.getItemMeta();
        barrierMeta.setDisplayName(ChatColor.RED+"Close");
        barrier.setItemMeta(barrierMeta);
        
        ItemStack book = new ItemStack(Material.BOOK);
        ItemMeta bookMeta = book.getItemMeta();
        bookMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6&lBooks"));
        List<String> bookLore = new ArrayList<>();
        Arrays.asList("&7Click this item to toggle whether or not", "&7emojis can be used in books.").forEach(lore -> bookLore.add(ChatColor.translateAlternateColorCodes('&', lore)));
        bookMeta.setLore(bookLore);
        if(useInBooks) bookMeta.addEnchant(Enchantment.LURE, 1, true);
    	bookMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        book.setItemMeta(bookMeta);
        
        ItemStack sign = new ItemStack(Material.OAK_SIGN);
        ItemMeta signMeta = sign.getItemMeta();
        signMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6&lSigns"));
        List<String> signLore = new ArrayList<>();
        Arrays.asList("&7Click this item to toggle whether or not", "&7emojis can be used on signs.").forEach(lore -> signLore.add(ChatColor.translateAlternateColorCodes('&', lore)));
        signMeta.setLore(signLore);
        if(useOnSigns) signMeta.addEnchant(Enchantment.LURE, 1, true);
    	signMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        sign.setItemMeta(signMeta);

        settingsGui.setItem(0, book);
        settingsGui.setItem(1, sign);
        settingsGui.setItem(4, barrier);

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
            
            @EventHandler
            public void onSignChange(SignChangeEvent e) {
            	if(useOnSigns) {
                	for(int i = 0; i < e.getLines().length; i++) {
                		e.setLine(i, emojis.parse(e.getPlayer(), ChatColor.RESET + ChatColor.getLastColors(e.getLine(i)), e.getLine(i)));
                	}
            	}
            }
            
            @EventHandler
            public void onPlayerBookEdit(PlayerEditBookEvent e) {
            	if(useInBooks) {
                	List<String> newContent = new ArrayList<>();
                	BookMeta meta = e.getNewBookMeta();
                	meta.getPages().forEach(string -> newContent.add(emojis.parse(e.getPlayer(), ChatColor.RESET + ChatColor.getLastColors(string), string)));
                	meta.setPages(newContent);
                	e.setNewBookMeta(meta);
            	}
            }
            
            @EventHandler
            public void onInventoryDrag(InventoryDragEvent e) {
            	if(e.getInventory().equals(settingsGui)) e.setCancelled(true);
            }
            
            @EventHandler
            public void onInventoryInteract(InventoryInteractEvent e) {
            	if(e.getInventory().equals(settingsGui)) e.setCancelled(true);
            }
            
            @EventHandler
            public void onInventoryMove(InventoryMoveItemEvent e) {
            	if(e.getInitiator().equals(settingsGui)) e.setCancelled(true);
            }

            private long lastUpdate = 0L;
            private boolean timerIsRunning = false;
            @EventHandler
            public void onInventoryClick(InventoryClickEvent e) {
            	if(e.getInventory().equals(settingsGui)) {
            		e.setCancelled(true);
            		if(e.getCurrentItem() != null) {
            			if(e.getCurrentItem().equals(barrier)) e.getWhoClicked().closeInventory();
            			else if(e.getCurrentItem().equals(book) || e.getCurrentItem().equals(sign)) {
            				if(e.getCurrentItem().equals(book)) {
                				useInBooks = !useInBooks;
                				
                				ItemMeta bookMeta = book.getItemMeta();
                				if(useInBooks) bookMeta.addEnchant(Enchantment.LURE, 1, true);
                				else if(bookMeta.hasEnchant(Enchantment.LURE)) bookMeta.removeEnchant(Enchantment.LURE);
                				book.setItemMeta(bookMeta);
                				settingsGui.setItem(0, book);
                				
                				e.getWhoClicked().sendMessage(ChatColor.translateAlternateColorCodes('&', useInBooks ? "&7You can now use emojis in books." : "&7You can no longer use emojis in books."));
                				((Player) e.getWhoClicked()).playSound(e.getWhoClicked().getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
            				} else {
            					useOnSigns = !useOnSigns;
            					
                				ItemMeta signMeta = sign.getItemMeta();
                				if(useOnSigns) signMeta.addEnchant(Enchantment.LURE, 1, true);
                				else if(signMeta.hasEnchant(Enchantment.LURE)) signMeta.removeEnchant(Enchantment.LURE);
                				sign.setItemMeta(signMeta);
                				settingsGui.setItem(1, sign);
                				
                				e.getWhoClicked().sendMessage(ChatColor.translateAlternateColorCodes('&', useOnSigns ? "&7You can now use emojis on signs." : "&7You can no longer use emojis on signs."));
                				((Player) e.getWhoClicked()).playSound(e.getWhoClicked().getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
            				}
            				
            				lastUpdate = System.currentTimeMillis();
            				if(!timerIsRunning) {
            					timerIsRunning = true;
                				Timer timer = new Timer();
                				timer.schedule(new TimerTask() {
    								@Override
    								public void run() {
    									// if update happened more than 5 seconds ago then save
    									// otherwise keep checking every 1 second;
    									// this timer is to prevent spam saving to config
    									if(System.currentTimeMillis() - lastUpdate >= 5000L) {
    			            				if(!useInBooks) getConfig().set("settings.use.books", false);
    			            				else if(getConfig().isSet("settings.use.books")) getConfig().set("settings.use.books", null);
    			            				
    			            				if(!useOnSigns) getConfig().set("settings.use.signs", false);
    			            				else if(getConfig().isSet("settings.use.signs")) getConfig().set("settings.use.signs", null);
    			            				
    			            				saveConfig();
    			            				timer.cancel();
    			            				timerIsRunning = false;
    									}
    								}
                				}, 5100, 1000);
            				}
            			}
            		}
            	}
            }

        }, this);

        getCommand("emoji").setExecutor((sender, command, label, args) -> {
            if(sender instanceof Player) {
                if(sender.hasPermission("chatemojis.command") || sender.hasPermission("chatemojis.list")) {
                    if(args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase("list"))) {
                        TextComponent header = ComponentUtils.createComponent("&6ChatEmojis &7(v"+getDescription().getVersion()+")\n");
                        header.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ComponentUtils.createBaseComponent("&6ChatEmojis\n&7Version: &e"+getDescription().getVersion()+"\n&7Author: &eRMJTromp\n\n&eClick to open spigot resource page."))));
                        header.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.spigotmc.org/resources/chatemojis.88027/"));
                        
                        TextComponent body = ComponentUtils.joinComponents("\n", emojis.getComponents((Player) sender));
                        TextComponent component = ComponentUtils.mergeComponents(header, body);
                        sender.spigot().sendMessage(component);
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
                    		if(sender.hasPermission("chatemojis.admin")) {
                        		if(sender instanceof Player) ((Player) sender).openInventory(settingsGui);
                        		else sender.sendMessage(ChatColor.RED + "You must be a player to use this command.");
                    		} else sender.sendMessage(ChatColor.RED + "You don't have enough permission to use this command.");
                    	} else if(args[0].toLowerCase().matches("^v(?:er(?:sion)?)?$")) sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7This server is currently running &eChatEmojis &7(v"+getDescription().getVersion()+")"));
                    	else sender.sendMessage(ChatColor.RED + "Unknown argument. Try \"/emoji help\" for a list of commands.");
                    } else sender.sendMessage(ChatColor.RED + "Too many arguments. Try \"/emoji help\" for a list of commands.");
                } else sender.sendMessage(ChatColor.RED + "You don't have enough permission to use this command.");
            } else sender.sendMessage(ChatColor.RED + "Emojis are only available to players.");
            return true;
        });
    }
    
    boolean isPapiLoaded() {
    	return papiIsLoaded;
    }

    static ChatEmojis getInstance() {
        return plugin;
    }

    static class EmojiGroups extends ArrayList<EmojiGroup> {
		private static final long serialVersionUID = 603062735230254276L;
	}
    
    static class Emojis extends ArrayList<Emoji> {
		private static final long serialVersionUID = 5307065755119322875L;
	}

}
