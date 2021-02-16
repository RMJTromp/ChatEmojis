package com.rmjtromp.chatemojis.exceptions;

import org.bukkit.configuration.ConfigurationSection;

public class InvalidEmoticonException extends ConfigException {

	private static final long serialVersionUID = -4010441760527137007L;

	public InvalidEmoticonException(String message, ConfigurationSection section) {
        super(message, section);
    }

}
