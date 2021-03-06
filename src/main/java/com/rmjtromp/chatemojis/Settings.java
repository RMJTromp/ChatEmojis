package com.rmjtromp.chatemojis;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

/**
 * The {@link ChatEmojis} plugin settings class.<br>
 * Every toggle-able options can be toggled in here.
 * @author Melvin
 * @since 2.2.1
 */
public final class Settings {
	
	private static final ChatEmojis PLUGIN = ChatEmojis.getInstance();
	
	private boolean chat, signs, books;
	private final List<String> chatExclusions = new ArrayList<>();
	private final List<String> signExclusions = new ArrayList<>();
	private final List<String> bookExclusions = new ArrayList<>();
	
	Settings() {
		reload();
	}
	
	/**
	 * Reloads the settings from config file
	 */
	void reload() {
        chat = PLUGIN.config.getBoolean("settings.use.chat.enabled", true);
        signs = PLUGIN.config.getBoolean("settings.use.signs.enabled", true);
        books = PLUGIN.config.getBoolean("settings.use.books.enabled", true);

        chatExclusions.clear();
		if(PLUGIN.config.isSet("settings.use.chat.exceptions") && PLUGIN.config.isList("settings.use.chat.exceptions")) {
			// only add them if they appear to be UUIDs
			PLUGIN.config.getStringList("settings.use.chat.exceptions").stream().filter(key -> key.matches("^[a-f0-9-]{36}$")).forEach(chatExclusions::add);
		}
		
		signExclusions.clear();
		if(PLUGIN.config.isSet("settings.use.signs.exceptions") && PLUGIN.config.isList("settings.use.signs.exceptions")) {
			// only add them if they appear to be UUIDs
			PLUGIN.config.getStringList("settings.use.signs.exceptions").stream().filter(key -> key.matches("^[a-f0-9-]{36}$")).forEach(signExclusions::add);
		}
		
		bookExclusions.clear();
		if(PLUGIN.config.isSet("settings.use.books.exceptions") && PLUGIN.config.isList("settings.use.books.exceptions")) {
			// only add them if they appear to be UUIDs
			PLUGIN.config.getStringList("settings.use.books.exceptions").stream().filter(key -> key.matches("^[a-f0-9-]{36}$")).forEach(bookExclusions::add);
		}
	}
	
	public enum Service { CHAT, BOOKS, SIGNS }
	
	/**
	 * Returns whether or not a service can be used in a specific world
	 * @param arg0 The service it should be checking for
	 * @param arg1 The world it should check if this service in enabled in for<br>
	 * <small><i><b>Note: </b>arg1 can be null, this will check is the service can be used globally</i></small>
	 * @return Whether or not a service can be used
	 */
	public boolean canUtilize(@NotNull Service arg0, World arg1) {
		// global + world = expected answer
		//
		// true + false = true
		// true + true = false
		// false + true = true
		// false + false = false
		//
		// conclusion: opposites always return true
		
		if(arg0.equals(Service.CHAT)) {
			// whether or not this feature is enabled globally
			boolean global = chat;
			
			if(arg1 != null) {
				// whether or not this feature is an exception in this world
				boolean world = chatExclusions.contains(arg1.getUID().toString());
				
				// if it is an exception (therefore not same value) parse
				return global != world;
			}
			return global;
		} else if(arg0.equals(Service.BOOKS)) {
			// whether or not this feature is enabled globally
			boolean global = books;
			
			if(arg1 != null) {
				// whether or not this feature is an exception in this world
				boolean world = bookExclusions.contains(arg1.getUID().toString());
				
				// if it is an exception (therefore not same value) parse
				return global != world;
			}
			return global;
		} else if(arg0.equals(Service.SIGNS)) {
			// whether or not this feature is enabled globally
			boolean global = signs;
			
			if(arg1 != null) {
				// whether or not this feature is an exception in this world
				boolean world = signExclusions.contains(arg1.getUID().toString());
				
				// if it is an exception (therefore not same value) parse
				return global != world;
			}
			return global;
		}
		return false;
	}
	
	/**
	 * Toggles the service provided for a specific world.
	 * @param arg0 Service which should be toggled
	 * @param arg1 The world it should be toggled in<br>
	 * <small><i><b>NOTE:</b> arg1 can be null, this will indicate that it should be toggled globally</i></small>
	 */
	public void toggleService(@NotNull Service arg0, World arg1) {
		if(arg1 != null) {
			if(arg0.equals(Service.CHAT)) {
				if(chatExclusions.contains(arg1.getUID().toString())) chatExclusions.remove(arg1.getUID().toString());
				else chatExclusions.add(arg1.getUID().toString());
				PLUGIN.config.set("settings.use.chat.exceptions", chatExclusions);
			} else if(arg0.equals(Service.SIGNS)) {
				if(signExclusions.contains(arg1.getUID().toString())) signExclusions.remove(arg1.getUID().toString());
				else signExclusions.add(arg1.getUID().toString());
				PLUGIN.config.set("settings.use.signs.exceptions", signExclusions);
			} else if(arg0.equals(Service.BOOKS)) {
				if(bookExclusions.contains(arg1.getUID().toString())) bookExclusions.remove(arg1.getUID().toString());
				else bookExclusions.add(arg1.getUID().toString());
				PLUGIN.config.set("settings.use.books.exceptions", bookExclusions);
			} else return;
			PLUGIN.config.save();
		} else {
			if(arg0.equals(Service.CHAT)) chat = !chat;
			else if(arg0.equals(Service.SIGNS)) signs = !signs;
			else if(arg0.equals(Service.BOOKS)) books = !books;
			else return;

			PLUGIN.config.set("settings.use.chat.enabled", chat);
			PLUGIN.config.set("settings.use.signs.enabled", signs);
			PLUGIN.config.set("settings.use.books.enabled", books);
			PLUGIN.config.save();
		}
	}
	
}
