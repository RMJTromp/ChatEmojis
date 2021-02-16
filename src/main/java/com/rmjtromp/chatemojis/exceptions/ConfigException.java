package com.rmjtromp.chatemojis.exceptions;

import org.bukkit.configuration.ConfigurationSection;

public class ConfigException extends Exception {

	private static final long serialVersionUID = 7126649646035356471L;
	
	private final ConfigurationSection section;

    public ConfigException(String message, ConfigurationSection section) {
        super(message + " At: "+section.getCurrentPath());
        this.section = section;
    }

    public ConfigurationSection getConfigurationSection() {
        return section;
    }

}
