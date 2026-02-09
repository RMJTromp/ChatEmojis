package com.rmjtromp.chatemojis;

import be.seeseemelk.mockbukkit.entity.PlayerMock;
import org.bukkit.Material;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class PluginListenersTest extends TestBase {

    @Test
    void asyncPlayerChat_withEmoticon_getsReplaced() {
        PlayerMock player = createOpPlayer();
        AsyncPlayerChatEvent event = new AsyncPlayerChatEvent(
            false, player, "<3", new HashSet<>(server.getOnlinePlayers())
        );
        server.getPluginManager().callEvent(event);

        assertFalse(event.getMessage().contains("<3"),
            "Emoticon should be replaced: " + event.getMessage());
    }

    @Test
    void asyncPlayerChat_withoutEmoticon_unchanged() {
        PlayerMock player = createOpPlayer();
        AsyncPlayerChatEvent event = new AsyncPlayerChatEvent(
            false, player, "hello world", new HashSet<>(server.getOnlinePlayers())
        );
        server.getPluginManager().callEvent(event);

        assertEquals("hello world", event.getMessage());
    }

    @Test
    void signChangeEvent_withEmoticon_replacedWhenEnabled() {
        PlayerMock player = createOpPlayer();

        // Ensure signs are enabled (default)
        assertTrue(plugin.useOnSigns.getNonNullValue());

        SignChangeEvent event = new SignChangeEvent(
            player.getWorld().getBlockAt(0, 64, 0),
            player,
            new String[]{"<3", "hello", "", ""}
        );
        server.getPluginManager().callEvent(event);

        assertFalse(event.getLine(0).contains("<3"),
            "Sign line should have emoji replaced: " + event.getLine(0));
        assertEquals("hello", event.getLine(1));
    }

    @Test
    void signChangeEvent_whenDisabled_notReplaced() {
        PlayerMock player = createOpPlayer();

        // Disable signs
        plugin.useOnSigns.setValue(false);
        try {
            SignChangeEvent event = new SignChangeEvent(
                player.getWorld().getBlockAt(0, 64, 0),
                player,
                new String[]{"<3", "", "", ""}
            );
            server.getPluginManager().callEvent(event);

            assertEquals("<3", event.getLine(0), "Sign should not be modified when disabled");
        } finally {
            // Restore default
            plugin.useOnSigns.setValue(true);
        }
    }

    @Test
    void playerEditBook_withEmoticon_replacedWhenEnabled() {
        PlayerMock player = createOpPlayer();

        assertTrue(plugin.useInBooks.getNonNullValue());

        ItemStack book = new ItemStack(Material.WRITABLE_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        assertNotNull(meta);
        meta.setPages(Arrays.asList("<3 page", "normal page"));

        PlayerEditBookEvent event = new PlayerEditBookEvent(
            player, 0, meta, meta, false
        );
        server.getPluginManager().callEvent(event);

        BookMeta newMeta = event.getNewBookMeta();
        assertFalse(newMeta.getPage(1).contains("<3"),
            "Book page should have emoji replaced: " + newMeta.getPage(1));
        assertEquals("normal page", newMeta.getPage(2));
    }

    @Test
    void playerEditBook_whenDisabled_notReplaced() {
        PlayerMock player = createOpPlayer();

        plugin.useInBooks.setValue(false);
        try {
            ItemStack book = new ItemStack(Material.WRITABLE_BOOK);
            BookMeta meta = (BookMeta) book.getItemMeta();
            assertNotNull(meta);
            meta.setPages(Collections.singletonList("<3 page"));

            PlayerEditBookEvent event = new PlayerEditBookEvent(
                player, 0, meta, meta, false
            );
            server.getPluginManager().callEvent(event);

            BookMeta newMeta = event.getNewBookMeta();
            assertEquals("<3 page", newMeta.getPage(1),
                "Book should not be modified when disabled");
        } finally {
            plugin.useInBooks.setValue(true);
        }
    }
}
