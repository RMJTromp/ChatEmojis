package com.rmjtromp.chatemojis;

import be.seeseemelk.mockbukkit.entity.PlayerMock;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EmojiGroupTest extends TestBase {

    private EmojiGroup loadGroup(String yaml) throws Exception {
        YamlConfiguration config = new YamlConfiguration();
        config.loadFromString(yaml);
        return EmojiGroup.init(config);
    }

    @Test
    void rootGroup_nameIsNull() throws Exception {
        EmojiGroup group = loadGroup("heart:\n  emoticon: '<3'\n  emoji: '❤'");
        assertNull(group.getName());
    }

    @Test
    void rootGroup_permissionIsWildcard() throws Exception {
        EmojiGroup group = loadGroup("heart:\n  emoticon: '<3'\n  emoji: '❤'");
        assertEquals("chatemojis.use.*", group.getPermission().getName());
    }

    @Test
    void nestedGroup_nameExtractedFromPath() throws Exception {
        EmojiGroup root = loadGroup("mygroup:\n  heart:\n    emoticon: '<3'\n    emoji: '❤'");
        assertEquals(1, root.getGroups().size());
        assertEquals("mygroup", root.getGroups().get(0).getName());
    }

    @Test
    void getEmojis_notRecursive_returnsOnlyDirectChildren() throws Exception {
        EmojiGroup root = loadGroup(
            "direct:\n  emoticon: ':direct:'\n  emoji: 'D'\n" +
            "group1:\n  nested:\n    emoticon: ':nested:'\n    emoji: 'N'"
        );
        List<Emoji> direct = root.getEmojis(false);
        assertEquals(1, direct.size());
        assertEquals("direct", direct.get(0).getName());
    }

    @Test
    void getEmojis_recursive_returnsAllDescendants() throws Exception {
        EmojiGroup root = loadGroup(
            "direct:\n  emoticon: ':direct:'\n  emoji: 'D'\n" +
            "group1:\n  nested:\n    emoticon: ':nested:'\n    emoji: 'N'"
        );
        List<Emoji> all = root.getEmojis(true);
        assertEquals(2, all.size());
    }

    @Test
    void getEmojis_recursiveCacheReturnsSameList() throws Exception {
        EmojiGroup root = loadGroup("heart:\n  emoticon: '<3'\n  emoji: '❤'");
        List<Emoji> first = root.getEmojis(true);
        List<Emoji> second = root.getEmojis(true);
        assertSame(first, second, "Cached list should be the same reference");
    }

    @Test
    void getEmojis_recursiveCacheIsUnmodifiable() throws Exception {
        EmojiGroup root = loadGroup("heart:\n  emoticon: '<3'\n  emoji: '❤'");
        List<Emoji> cached = root.getEmojis(true);
        assertThrows(UnsupportedOperationException.class, () -> cached.add(null));
    }

    @Test
    void parse_replacesMatchingEmojis() throws Exception {
        PlayerMock player = createOpPlayer();
        EmojiGroup root = loadGroup("heart:\n  emoticon: '<3'\n  emoji: '❤'");

        ParsingContext ctx = ParsingContext.builder()
            .player(player)
            .message("I <3 you")
            .build();

        root.parse(ctx);
        assertTrue(ctx.getMessage().contains("❤"), "Should contain emoji: " + ctx.getMessage());
        assertFalse(ctx.getMessage().contains("<3"), "Emoticon should be gone: " + ctx.getMessage());
    }

    @Test
    void parse_skipsDisabledEmojis() throws Exception {
        PlayerMock player = createOpPlayer();
        EmojiGroup root = loadGroup("heart:\n  emoticon: '<3'\n  emoji: '❤'\n  enabled: false");

        ParsingContext ctx = ParsingContext.builder()
            .player(player)
            .message("I <3 you")
            .build();

        root.parse(ctx);
        assertEquals("I <3 you", ctx.getMessage());
    }

    @Test
    void parse_skipsEmojisWithoutPermission() throws Exception {
        PlayerMock player = createPlayer(); // no permissions, not op
        EmojiGroup root = loadGroup("heart:\n  emoticon: '<3'\n  emoji: '❤'");

        ParsingContext ctx = ParsingContext.builder()
            .player(player)
            .message("I <3 you")
            .build();

        root.parse(ctx);
        assertEquals("I <3 you", ctx.getMessage());
    }

    @Test
    void parse_forcedMode_bypassesPermissions() throws Exception {
        PlayerMock player = createPlayer(); // no permissions
        EmojiGroup root = loadGroup("heart:\n  emoticon: '<3'\n  emoji: '❤'");

        ParsingContext ctx = ParsingContext.builder()
            .player(player)
            .message("I <3 you")
            .forced(true)
            .build();

        root.parse(ctx);
        assertTrue(ctx.getMessage().contains("❤"));
    }

    @Test
    void parse_multipleEmojis_replacesAll() throws Exception {
        PlayerMock player = createOpPlayer();
        EmojiGroup root = loadGroup(
            "star:\n  emoticon: ':star:'\n  emoji: '★'\n" +
            "heart:\n  emoticon: '<3'\n  emoji: '❤'"
        );

        ParsingContext ctx = ParsingContext.builder()
            .player(player)
            .message("I <3 :star:")
            .build();

        root.parse(ctx);
        assertTrue(ctx.getMessage().contains("★"), "Should contain star: " + ctx.getMessage());
        assertTrue(ctx.getMessage().contains("❤"), "Should contain heart: " + ctx.getMessage());
    }

    @Test
    void configParsing_sectionWithBothEmoticonAndEmoji_treatedAsEmoji() throws Exception {
        EmojiGroup root = loadGroup("test:\n  emoticon: ':test:'\n  emoji: 'T'");
        assertEquals(1, root.getEmojis().size());
        assertEquals(0, root.getGroups().size());
    }

    @Test
    void configParsing_sectionWithoutBothKeys_treatedAsGroup() throws Exception {
        EmojiGroup root = loadGroup("mygroup:\n  child:\n    emoticon: ':child:'\n    emoji: 'C'");
        assertEquals(0, root.getEmojis().size());
        assertEquals(1, root.getGroups().size());
    }

    @Test
    void configParsing_reservedKeysSkippedInSubGroups() throws Exception {
        // At non-root level, reserved names (emoticon, emoji, regex, enabled) are skipped
        // even if they happen to be section keys
        EmojiGroup root = loadGroup("mygroup:\n  child:\n    emoticon: ':child:'\n    emoji: 'C'\n    enabled: true");
        EmojiGroup subgroup = root.getGroups().get(0);
        // "enabled" shouldn't create a sub-group or emoji, just be read as config
        assertEquals(1, subgroup.getEmojis().size());
        assertEquals(0, subgroup.getGroups().size());
    }

    @Test
    void configParsing_settingsAtRootIsTreatedAsGroup() throws Exception {
        // "settings" is NOT in RESERVED_NAMES, so at root level it IS processed as a sub-section
        // The condition: !RESERVED_NAMES.contains(key) || (parent == null && !key.equals("settings"))
        // For "settings" at root: !false || (true && false) = true || false = true -> processed
        EmojiGroup root = loadGroup("settings:\n  use:\n    books: true\nheart:\n  emoticon: '<3'\n  emoji: '❤'");
        // "settings" gets processed as a group (it has sub-keys but no emoticon+emoji)
        assertEquals(1, root.getGroups().size());
        assertEquals(1, root.getEmojis().size());
    }
}
