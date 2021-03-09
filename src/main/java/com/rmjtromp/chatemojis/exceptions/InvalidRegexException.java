package com.rmjtromp.chatemojis.exceptions;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public class InvalidRegexException extends ConfigException {

	public InvalidRegexException(@NotNull String message, @NotNull ConfigurationSection section) {
        super(message, section);
    }

}
