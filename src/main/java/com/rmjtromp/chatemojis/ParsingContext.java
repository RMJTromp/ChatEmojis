package com.rmjtromp.chatemojis;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Objects;

@Builder
public class ParsingContext {

    private static final ChatEmojis PLUGIN = ChatEmojis.getInstance();

    @Getter
    private final Player player;

    private final String message;

    @Builder.Default
    private final ChatColor resetColor = null;

    @Getter
    @Builder.Default
    private final boolean forced = false;

    @Getter
    @Builder.Default
    private final HashMap<Emoji, Integer> useMap = new HashMap<>();

    private String currentMessage;

    public String getMessage() {
        if(currentMessage == null) currentMessage = message;
        return currentMessage;
    }

    public String getResetColor() {
        return Objects.isNull(resetColor)
            ? ChatColor.RESET + ChatColor.getLastColors(getMessage())
            : resetColor.toString();
    }

    public void setMessage(@NonNull String message) {
        currentMessage = message;
    }

    public boolean hasReachedLimit(@NonNull Emoji emoji) {
        return emoji.hasLimit() && useMap.getOrDefault(emoji, 0) >= emoji.getLimit();
    }

    public boolean hasReachedGlobalLimit() {
        return PLUGIN.maxEmojisPerMessage.getNonNullValue() != -1 && useMap.values().stream().mapToInt(Integer::intValue).sum() >= PLUGIN.maxEmojisPerMessage.getNonNullValue();
    }

}
