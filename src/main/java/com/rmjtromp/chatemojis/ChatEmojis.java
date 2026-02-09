package com.rmjtromp.chatemojis;

import com.rmjtromp.chatemojis.exceptions.ConfigException;
import com.rmjtromp.chatemojis.utils.BukkitUtils;
import com.rmjtromp.chatemojis.utils.Config;
import com.rmjtromp.chatemojis.utils.Config.ConfigurationReference;
import com.rmjtromp.chatemojis.windows.SettingsWindow;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPluginLoader;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.regex.Pattern;

public final class ChatEmojis extends JavaPlugin {

    static final List<String> RESERVED_NAMES = Arrays.asList("emoticon", "emoji", "regex", "enabled");
    static final Pattern NAME_PATTERN = Pattern.compile("(?<=\\.)?([^.]+?)$", Pattern.CASE_INSENSITIVE);

    private final Config config;
    EmojiGroup emojis = null;
    private static ChatEmojis plugin;
    boolean papiIsLoaded = false;
    public final ConfigurationReference<Boolean> useOnSigns, useInBooks, useInAnvils;
    public final ConfigurationReference<Integer> maxEmojisPerMessage, maxDuplicateEmojis;
    SettingsWindow settingsWindow = null;

    public ChatEmojis() throws IOException, InvalidConfigurationException {
        plugin = this;
        BukkitUtils.init(this);

        config = Config.init(new File(getDataFolder(), "config.yml"), "config.yml");

        useInBooks = config.reference("settings.use.books", true);
        useOnSigns = config.reference("settings.use.signs", true);
        useInAnvils = config.reference("settings.use.anvils", true);

        // disabled until stable & working
        maxEmojisPerMessage = config.reference("settings.max-emojis-per-message", -1);
        maxDuplicateEmojis = config.reference("settings.max-duplicate-emojis", -1);
    }

    // Constructor for MockBukkit compatibility
    protected ChatEmojis(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file)
            throws IOException, InvalidConfigurationException {
        super(loader, description, dataFolder, file);
        plugin = this;
        BukkitUtils.init(this);

        config = Config.init(new File(getDataFolder(), "config.yml"), "config.yml");

        useInBooks = config.reference("settings.use.books", true);
        useOnSigns = config.reference("settings.use.signs", true);
        useInAnvils = config.reference("settings.use.anvils", true);

        maxEmojisPerMessage = config.reference("settings.max-emojis-per-message", -1);
        maxDuplicateEmojis = config.reference("settings.max-duplicate-emojis", -1);
    }

    @Override
    public void onLoad() {
        try {
            emojis = EmojiGroup.init(getConfig());
        } catch (ConfigException e) {
            getLogger().log(Level.SEVERE, "Failed to load emoji configuration", e);
        }
    }

    @Override
	public void onEnable() {
        settingsWindow = new SettingsWindow(this);
        papiIsLoaded = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;

        PluginListeners listeners = new PluginListeners();
        getServer().getPluginManager().registerEvents(listeners, this);
        listeners.registerAnvilListeners();

        CommandHandler commandHandler = new CommandHandler();
        PluginCommand command = getCommand("emoji");

        Objects.requireNonNull(command, "Command 'emoji' not found in plugin.yml");
        command.setExecutor(commandHandler);
        command.setTabCompleter(commandHandler);
    }

    @NotNull
    @Override
    public Config getConfig() {
        return config;
    }

    static ChatEmojis getInstance() {
        return plugin;
    }

    /**
     * Replaces emoticons to emojis in the message
     * <br><br><i>(<b>NOTE</b>: Not forced; Limitations and permissions will be checked)</i>
     * @param player the player who sent the message
     * @param message the message to parse
     * @return the parsed message
     */
    public String parseEmojis(@NonNull Player player, @NonNull String message) {
        return parseEmojis(player, message, false);
    }

    /**
     * Replaces emoticons to emojis in the message
     * @param player The player who sent the message
     * @param message The message to parse
     * @param force Whether to bypass permissions and limitations
     * @return The parsed message
     */
    public String parseEmojis(@NonNull Player player, @NonNull String message, boolean force) {
        return emojis.parse(
                ParsingContext.builder()
                    .player(player)
                    .message(message)
                    .forced(force)
                    .build()
                ).getMessage();
    }

}
