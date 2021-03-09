package com.rmjtromp.chatemojis.exceptions;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public class InvalidEmoticonException extends ConfigException {

	public InvalidEmoticonException(@NotNull String message, @NotNull ConfigurationSection section) {
        super(message, section);
    }

}
