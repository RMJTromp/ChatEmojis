package com.rmjtromp.chatemojis;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;

import com.rmjtromp.chatemojis.exceptions.ConfigException;
import com.rmjtromp.chatemojis.utils.Config;
import com.rmjtromp.chatemojis.utils.GZIPUtils;
import com.rmjtromp.chatemojis.utils.Lang;
import com.rmjtromp.chatemojis.utils.bukkit.Version;

/**
 * {@link ChatEmojis} main plugin class.
 * @author Melvin
 */
public final class ChatEmojis extends JavaPlugin {

    static final List<String> RESERVED_NAMES = Arrays.asList("emoticon", "emoji", "regex", "enabled");
    static final Pattern NAME_PATTERN = Pattern.compile("(?<=\\.)?([^\\.]+?)$", Pattern.CASE_INSENSITIVE);
    static final Random RANDOM = new Random();


    private static ChatEmojis plugin = null;
    private EmojiGroup emojis = null;
    private Settings settings = null;
    
    boolean papiIsLoaded = false;
    boolean essentialsIsLoaded = false;
    
    // inventories
    Config config = null;
    Config emojisConfig = null;

    public ChatEmojis() throws IOException {
        plugin = this;

//		getDataFolder().mkdirs();
//		File content = new File(getDataFolder(), "content.txt");
//		File compressed = new File(getDataFolder(), "compressed.txt.gz");
//		File uncompressed = new File(getDataFolder(), "uncompressed.txt");
//		try {
//			if(!content.exists()) content.createNewFile();
//			if(!compressed.exists()) compressed.createNewFile();
//			if(!uncompressed.exists()) uncompressed.createNewFile();
//			
//			try(OutputStreamWriter writer =  new OutputStreamWriter(new FileOutputStream(content), StandardCharsets.UTF_8);
//	            InputStream in = getResource("test.txt")) {
//				String body = CharStreams.toString(new InputStreamReader(in, StandardCharsets.UTF_8));
//	            writer.write(body);
//	            
//	            GZIPUtils.compressToFile(compressed, body);
//	            
//	            try(OutputStreamWriter writer2 =  new OutputStreamWriter(new FileOutputStream(uncompressed), StandardCharsets.UTF_8)) {
//	            	writer2.write(GZIPUtils.decompress(compressed));
//	            }
//	        }
//		} catch(IOException e) {
//			e.printStackTrace();
//		}

//        List<String> resources = Arrays.asList("lang/en_US.yml", "emojis.yml", "config.yml");
//        resources.forEach(resource -> {
//    		try(InputStream in = getResource(resource)) {
//    			GZIPUtils.compressToFile(new File(getDataFolder(), resource.replace("/", File.separator)+".gz"), in);
//            } catch (IOException e) {
//				e.printStackTrace();
//			}
//        });

        // load default language
		try(InputStream in = getResource("lang/en_US.yml.gz")) {
			String con = GZIPUtils.decompress(in);
	    	Lang.load(new StringReader(con));
	    	System.out.println(con);
		}
    }
    
    @Override
    public void onLoad() {
        try {
        	// load configs
			config = Config.init(new File(getDataFolder(), "config.yml"), "config.yml.gz");
			emojisConfig = Config.init(new File(getDataFolder(), "emojis.yml"), "emojis.yml.gz");
			
			// load settings
	        settings = new Settings();
	        
	        // load emojis
            emojis = EmojiGroup.init(emojisConfig);
            
            // load layered language
            if(config.isString("lang")) {
            	String lang = config.getString("lang");
            	if(!lang.equals("en_US")) {
            		// TODO check for a language & load it if available
            	}
            }
		} catch (IOException | InvalidConfigurationException e) {
			System.out.println("[ChatEmojis] "+Lang.translate("error.load.config"));
			getServer().getPluginManager().disablePlugin(this);
			e.printStackTrace();
		} catch (ConfigException e) {
			System.out.println("[ChatEmojis] "+Lang.translate("error.load.emojis"));
			getServer().getPluginManager().disablePlugin(this);
			e.printStackTrace();
		}
    }

	@Override
	public void onEnable() {
		if(Version.getServerVersion() == null) {
			System.out.println("[ChatEmojis] "+Lang.translate("error.load.unsupported-version"));
			System.out.println("[ChatEmojis] "+Lang.translate("error.load.disabling-plugin"));
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		
		// check for soft dependencies
        papiIsLoaded = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
        essentialsIsLoaded = Bukkit.getPluginManager().getPlugin("Essentials") != null;
        
        // load handlers
        EventHandler.init();
        CommandHandler.init();
    }
	
	@Override
	public void onDisable() {
		config.forceSave();
		CommandHandler.disable();
		
		/*
		 * emojis config is not being modified by the plugin right now
		 * therefore saving it is pointless
		 * emojisConfig.forceSave();
		 */
	}
    
	/**
	 * Returns {@link ChatEmoji} settings instance. The settings contains everything that is toggle-able.
	 * @return the {@link ChatEmoji} plugin's {@link Settings}
	 */
    public Settings getSettings() {
    	return settings;
    }
    
    /**
     * Returns the {@link EmojiGroup} which is parent to all emojis.
     * This group does not have a name, and will return <code>null</code>.
     * @return The parent {@link EmojiGroup} 
     */
    public EmojiGroup getEmojis() {
    	return emojis;
    }
    
    /**
     * Reloads emojis config, then reloads all emojis from config
     */
    private void reloadEmojis() {
    	try {
    		emojisConfig.reload();
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        } finally {
            try {
				emojis = EmojiGroup.init(emojisConfig);
			} catch (ConfigException e) {
				e.printStackTrace();
			}
        }
    }
    
    /**
     * Reloads everything that is configurable
     */
    void reload() {
    	// reload configuration files
    	try {
			config.reload();
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		} finally {
	    	// reload layered language
	        if(config.isString("lang")) {
	        	String lang = config.getString("lang");
	        	if(!lang.equals("en_US")) {
	        		// TODO check for a language & load it if available
	        	}
	        }
	        
	        // reload emojis
	    	reloadEmojis();
		}
    	
    	// reload settings
    	settings.reload();
    	
    	
    	// reload command executor, tab-completer, and GUIs
        CommandHandler.reload();
    }
    
    /**
     * Returns the {@link Config} instance of {@link ChatEmoji}'s <code>config.yml</code>
     * @see {@link YamlConfiguration}
     * @return {@link Config}
     */
    @Override
    public Config getConfig() {
		return config;
	}

    /**
     * @return {@link ChatEmoji} Instance
     */
    public static ChatEmojis getInstance() {
        return plugin;
    }
    
    /**
     * {@link AbstractEmoji} interface implemented by {@link Emoji} and {@link EmojiGroup},
     * and is used in {@link EmojiGroup} to iterate over all child instances
     * @since 2.2.1
     * @author Melvin
     * @see {@link Emoji}
     * @see {@link EmojiGroup}
     */
    interface AbstractEmoji {

    	/**
    	 * @return the name of the {@link AbstractEmoji}
    	 * @see {@link Emoji}
    	 * @see {@link EmojiGroup}
    	 */
    	String getName();
    	
    	/**
    	 * @return The required {@link Permission} node required to use this {@link AbstractEmoji}
    	 * @see {@link Emoji}
    	 * @see {@link EmojiGroup}
    	 */
    	Permission getPermission();
    	
    }

}
