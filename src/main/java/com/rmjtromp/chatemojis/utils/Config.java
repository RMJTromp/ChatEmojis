package com.rmjtromp.chatemojis.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Timer;
import java.util.TimerTask;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

/**
 * Custom configuration files manager class.
 * @author Melvin
 * @since 2.2.1
 * @see YamlConfiguration
 */
public class Config extends YamlConfiguration {

    /**
     * Creates a new config file or uses existing one
     * @param file The file which the config should load
     * @throws IOException Thrown if an I/O exception occurs
     * @throws InvalidConfigurationException Thrown when the given file is not a valid Configuration.
     */
    public static Config init(File file) throws IOException, InvalidConfigurationException {
        if(!file.exists()) file.createNewFile();
        return new Config(file);
    }

    /**
     * Creates a new config file and uses default input from resource if file doesn't exist
     * @param file The file which the config should load
     * @param resource The resource which should be used as default if file doesn't exist
     * @throws IOException Thrown if an I/O exception occurs
     * @throws InvalidConfigurationException Thrown when the given file is not a valid Configuration.
     */
    public static Config init(File file, String resource) throws IOException, InvalidConfigurationException {
        if(file == null) throw new IllegalArgumentException("File can not be null");
        if(!file.exists()) {
            file.getParentFile().mkdirs();

            if(file.createNewFile()) {
                if(resource != null && !resource.isEmpty()) {
                    URL url = Config.class.getClassLoader().getResource(resource);

                    if (url == null) throw new NullPointerException("Resource doesn't exist? Resource URL is null.");

                    URLConnection connection = url.openConnection();
                    connection.setUseCaches(false);

                    try(OutputStream out = new FileOutputStream(file);
                        InputStream in = connection.getInputStream()) {
                        byte[] buf = new byte[1024];
                        int len;
                        while ((len = in.read(buf)) > 0) {
                            out.write(buf, 0, len);
                        }
                    }
                } else throw new IllegalArgumentException("Resource can not be null or empty");
            }
        }

        return new Config(file);
    }

    private final File file;

    public Config(@NotNull File file) throws IOException, InvalidConfigurationException {
        this.file = file;

        load(file);
    }

    private boolean timerIsRunning = false;
    private long lastSave = System.currentTimeMillis();

    /**
     * Saves the config file after 5 seconds of inactivity.
     * If the config file is saved again, the 5 second timer
     * resets.
     */
    public void save() {
        lastSave = System.currentTimeMillis();
        if(!timerIsRunning) {
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    // if update happened more than 5 seconds ago then save
                    // otherwise keep checking every 1 second;
                    // this timer is to prevent spam saving to config
                    if(System.currentTimeMillis() - lastSave >= 5000L) {
                        try {
                            save(file);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        timer.cancel();
                        timerIsRunning = false;
                    }
                }
            }, 5100, 1000);
        }
    }

    /**
     * force saves the config file,
     * skipping the 5 second timer
     */
    public void forceSave() {
        try {
            save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reloads the config file
     * @throws IOException Thrown if an I/O exception occurs
     * @throws InvalidConfigurationException Thrown when the given file is not a valid Configuration.
     */
    public void reload() throws IOException, InvalidConfigurationException {
        if(file.exists() || (!file.exists() && file.createNewFile())) load(file);
    }

}