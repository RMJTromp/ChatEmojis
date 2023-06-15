package com.rmjtromp.chatemojis;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Value;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

@Builder
@Value
public class ParsingContext {

    private static final ChatEmojis PLUGIN = ChatEmojis.getInstance();

    @Getter
    Player player;

    String message;

    @Builder.Default
    ChatColor resetColor = null;

    @Getter
    @Builder.Default
    boolean forced = false;

    HashMap<Emoji, Integer> useMap = new HashMap<>();

    AtomicReference<String> msg = new AtomicReference<>(null);

    public String getMessage() {
        if(msg.get() == null) msg.set(message);
        return msg.get();
    }

    public String getResetColor() {
        return Objects.isNull(resetColor)
            ? ChatColor.RESET + ChatColor.getLastColors(getMessage())
            : resetColor.toString();
    }

    public void setMessage(@NonNull String message) {
        msg.set(message);
    }

    public boolean hasReachedLimit(@NonNull Emoji emoji) {
        return emoji.hasLimit() && useMap.getOrDefault(emoji, 0) >= emoji.getLimit();
    }

    public boolean hasReachedGlobalLimit() {
        return PLUGIN.maxEmojisPerMessage.getNonNullValue() != -1 && useMap.values().stream().mapToInt(Integer::intValue).sum() >= PLUGIN.maxEmojisPerMessage.getNonNullValue();
    }

}
