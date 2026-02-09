package com.rmjtromp.chatemojis;

import be.seeseemelk.mockbukkit.entity.PlayerMock;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ParsingContextTest extends TestBase {

    @Test
    void builder_createsContextWithPlayerAndMessage() {
        PlayerMock player = createPlayer("chatemojis.use.*");
        ParsingContext ctx = ParsingContext.builder()
            .player(player)
            .message("Hello world")
            .build();

        assertSame(player, ctx.getPlayer());
        assertEquals("Hello world", ctx.getMessage());
    }

    @Test
    void getMessage_returnsOriginalMessageInitially() {
        PlayerMock player = createPlayer();
        ParsingContext ctx = ParsingContext.builder()
            .player(player)
            .message("original message")
            .build();

        assertEquals("original message", ctx.getMessage());
    }

    @Test
    void setMessage_changesCurrentMessage() {
        PlayerMock player = createPlayer();
        ParsingContext ctx = ParsingContext.builder()
            .player(player)
            .message("original")
            .build();

        ctx.setMessage("modified");
        assertEquals("modified", ctx.getMessage());
    }

    @Test
    void getResetColor_withNullDefault_returnsResetPlusLastColors() {
        PlayerMock player = createPlayer();
        ParsingContext ctx = ParsingContext.builder()
            .player(player)
            .message("test message")
            .build();

        String resetColor = ctx.getResetColor();
        assertNotNull(resetColor);
        assertTrue(resetColor.startsWith(ChatColor.RESET.toString()));
    }

    @Test
    void getResetColor_withExplicitColor_returnsThatColor() {
        PlayerMock player = createPlayer();
        ParsingContext ctx = ParsingContext.builder()
            .player(player)
            .message("test")
            .resetColor(ChatColor.RED)
            .build();

        assertEquals(ChatColor.RED.toString(), ctx.getResetColor());
    }

    @Test
    void forced_defaultsFalse() {
        PlayerMock player = createPlayer();
        ParsingContext ctx = ParsingContext.builder()
            .player(player)
            .message("test")
            .build();

        assertFalse(ctx.isForced());
    }

    @Test
    void forced_canBeSetTrue() {
        PlayerMock player = createPlayer();
        ParsingContext ctx = ParsingContext.builder()
            .player(player)
            .message("test")
            .forced(true)
            .build();

        assertTrue(ctx.isForced());
    }

    @Test
    void useMap_startsEmpty() {
        PlayerMock player = createPlayer();
        ParsingContext ctx = ParsingContext.builder()
            .player(player)
            .message("test")
            .build();

        assertTrue(ctx.getUseMap().isEmpty());
    }

    @Test
    void hasReachedLimit_noLimit_returnsFalse() throws Exception {
        PlayerMock player = createPlayer("chatemojis.use.*");

        YamlConfiguration yaml = new YamlConfiguration();
        yaml.loadFromString("heart:\n  emoticon: '<3'\n  emoji: '❤'");
        EmojiGroup group = EmojiGroup.init(yaml);
        Emoji emoji = group.getEmojis().get(0);

        ParsingContext ctx = ParsingContext.builder()
            .player(player)
            .message("test")
            .build();

        assertFalse(ctx.hasReachedLimit(emoji));
    }

    @Test
    void hasReachedLimit_withLimit_returnsTrueWhenExceeded() throws Exception {
        PlayerMock player = createPlayer("chatemojis.use.*");

        YamlConfiguration yaml = new YamlConfiguration();
        yaml.loadFromString("heart:\n  emoticon: '<3'\n  emoji: '❤'\n  limit: 1");
        EmojiGroup group = EmojiGroup.init(yaml);
        Emoji emoji = group.getEmojis().get(0);

        ParsingContext ctx = ParsingContext.builder()
            .player(player)
            .message("test")
            .build();

        // Simulate usage
        ctx.getUseMap().put(emoji, 1);
        assertTrue(ctx.hasReachedLimit(emoji));
    }

    @Test
    void hasReachedGlobalLimit_disabledByDefault_returnsFalse() {
        PlayerMock player = createPlayer();
        ParsingContext ctx = ParsingContext.builder()
            .player(player)
            .message("test")
            .build();

        assertFalse(ctx.hasReachedGlobalLimit());
    }
}
