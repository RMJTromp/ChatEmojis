package com.rmjtromp.chatemojis.exceptions;

import org.jetbrains.annotations.NotNull;

public class UnsupportedVersionException extends RuntimeException {

    public UnsupportedVersionException(@NotNull String string) {
        super(string);
    }

}
