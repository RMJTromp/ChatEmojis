package com.rmjtromp.chatemojis;

import be.seeseemelk.mockbukkit.entity.PlayerMock;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChatEmojisTest extends TestBase {

    @Test
    void pluginLoads_instanceIsNotNull() {
        assertNotNull(ChatEmojis.getInstance());
    }

    @Test
    void pluginLoads_sameAsLoadedPlugin() {
        assertSame(plugin, ChatEmojis.getInstance());
    }

    @Test
    void defaultConfig_emojisPopulated() {
        assertNotNull(plugin.emojis);
        assertFalse(plugin.emojis.getEmojis(true).isEmpty(), "Default config should have emojis loaded");
    }

    @Test
    void defaultSettings_useOnSigns_isTrue() {
        assertTrue(plugin.useOnSigns.getNonNullValue());
    }

    @Test
    void defaultSettings_useInBooks_isTrue() {
        assertTrue(plugin.useInBooks.getNonNullValue());
    }

    @Test
    void defaultSettings_useInAnvils_isTrue() {
        assertTrue(plugin.useInAnvils.getNonNullValue());
    }

    @Test
    void defaultSettings_maxEmojisPerMessage_isNegativeOne() {
        assertEquals(-1, (int) plugin.maxEmojisPerMessage.getNonNullValue());
    }

    @Test
    void parseEmojis_withPermission_replacesEmoticon() {
        PlayerMock player = createOpPlayer();
        String result = plugin.parseEmojis(player, ":star:");
        assertNotEquals(":star:", result);
    }

    @Test
    void parseEmojis_withoutPermission_returnsUnchanged() {
        PlayerMock player = createPlayer(); // no permissions, not op
        String result = plugin.parseEmojis(player, ":star:");
        assertEquals(":star:", result);
    }

    @Test
    void parseEmojis_forced_replacesEvenWithoutPermission() {
        PlayerMock player = createPlayer(); // no permissions
        String result = plugin.parseEmojis(player, ":star:", true);
        assertNotEquals(":star:", result);
    }

    @Test
    void parseEmojis_noMatch_returnsUnchanged() {
        PlayerMock player = createOpPlayer();
        String result = plugin.parseEmojis(player, "no match here");
        assertEquals("no match here", result);
    }

    @Test
    void parseEmojis_multipleOccurrences_replacesAll() {
        PlayerMock player = createOpPlayer();
        String result = plugin.parseEmojis(player, "<3 and <3");
        assertFalse(result.contains("<3"), "All occurrences of <3 should be replaced: " + result);
    }
}
