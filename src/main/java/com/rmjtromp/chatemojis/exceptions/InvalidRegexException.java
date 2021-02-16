package com.rmjtromp.chatemojis.exceptions;

import org.bukkit.configuration.ConfigurationSection;

public class InvalidRegexException extends ConfigException {

	private static final long serialVersionUID = 8609257320255308952L;

	public InvalidRegexException(String message, ConfigurationSection section) {
        super(message, section);
    }

}
