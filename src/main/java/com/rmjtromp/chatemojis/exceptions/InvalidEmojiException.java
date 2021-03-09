package com.rmjtromp.chatemojis.exceptions;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public final class InvalidEmojiException extends ConfigException {

	public InvalidEmojiException(@NotNull String message, @NotNull ConfigurationSection section) {
        super(message, section);
    }

}
