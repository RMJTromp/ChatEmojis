package com.rmjtromp.chatemojis.exceptions;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public class ConfigException extends Exception {
	
	private final ConfigurationSection section;

    public ConfigException(@NotNull String message, @NotNull ConfigurationSection section) {
        super(message + " At: "+section.getCurrentPath());
        this.section = section;
    }

    public ConfigurationSection getConfigurationSection() {
        return section;
    }

}
