package com.rmjtromp.chatemojis.exceptions;

import org.bukkit.configuration.ConfigurationSection;

public final class InvalidEmojiException extends ConfigException {

	private static final long serialVersionUID = 1512370625216965862L;

	public InvalidEmojiException(String message, ConfigurationSection section) {
        super(message, section);
    }

}
