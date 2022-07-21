package com.rmjtromp.chatemojis.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicReference;

import lombok.Getter;
import lombok.NonNull;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

                    try(OutputStream out = Files.newOutputStream(file.toPath());
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
                        } catch (NullPointerException ignore) {
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            timer.cancel();
                            timerIsRunning = false;
                        }
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

    public ConfigurationReference<?> reference(@NotNull String key) {
        return new ConfigurationReference<>(this, key);
    }

    public <T> ConfigurationReference<T> reference(@NotNull String key, @NotNull T defaultValue) {
        return new ConfigurationReference<>(this, key, defaultValue);
    }

    public static final class ConfigurationReference<T> {

        @Getter
        private final Config config;
        @Getter
        private final String key;

        private AtomicReference<T> valueReference = null;

        private ConfigurationReference(@NonNull Config config, @NonNull String key) {
            this.config = config;
            this.key = key;
        }

        private ConfigurationReference(@NonNull Config config, @NonNull String key, @NonNull T defaultValue) {
            this.config = config;
            this.key = key;

            if(!isSet()) setValue(defaultValue);
        }

        @Nullable
        @SuppressWarnings("unchecked")
        public T getValue() {
            if(valueReference == null) {
                valueReference = new AtomicReference<>();
                try { valueReference.set((T) config.get(key)); }
                catch(Exception e) {
                    valueReference = null;
                    throw new RuntimeException("Reference type and value type are not the same", e);
                }
            }
            return valueReference.get();
        }

        /**
         * @param value The new value
         * @return The old value
         */
        @Nullable
        public T setValue(T value) {
            if(valueReference == null) valueReference = new AtomicReference<>();
            valueReference.set(value);
            config.set(key, value);
            config.save();
            return valueReference != null ? valueReference.get() : null;
        }

        public boolean isSet() {
            return config.isSet(key);
        }

        @Override
        public String toString() {
            return "ConfigurationReference{" +
                "config=" + config +
                ", key='" + key + '\'' +
                ", valueReference=" + valueReference +
                '}';
        }

        @Override
        public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;

            ConfigurationReference<?> that = (ConfigurationReference<?>) o;

            if(!config.equals(that.config)) return false;
            if(!key.equals(that.key)) return false;
            return Objects.equals(valueReference, that.valueReference);
        }

        @Override
        public int hashCode() {
            int result = config.hashCode();
            result = 31 * result + key.hashCode();
            result = 31 * result + (valueReference != null ? valueReference.hashCode() : 0);
            return result;
        }
    }

}