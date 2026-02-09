package com.rmjtromp.chatemojis;

import be.seeseemelk.mockbukkit.entity.PlayerMock;
import com.rmjtromp.chatemojis.exceptions.InvalidEmojiException;
import com.rmjtromp.chatemojis.exceptions.InvalidEmoticonException;
import com.rmjtromp.chatemojis.exceptions.InvalidRegexException;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class EmojiTest extends TestBase {

    private EmojiGroup loadGroup(String yaml) throws Exception {
        YamlConfiguration config = new YamlConfiguration();
        config.loadFromString(yaml);
        return EmojiGroup.init(config);
    }

    /**
     * Directly create an Emoji by initializing a group and calling Emoji.init on the section.
     * This lets exceptions propagate instead of being caught by EmojiGroup.
     */
    private Emoji initEmoji(String yaml, String key) throws Exception {
        YamlConfiguration config = new YamlConfiguration();
        config.loadFromString(yaml);
        EmojiGroup root = EmojiGroup.init(new YamlConfiguration()); // empty root group
        return Emoji.init(root, config.getConfigurationSection(key));
    }

    // --- Creation & Config ---

    @Test
    void init_validConfig_createsEmojiWithCorrectName() throws Exception {
        EmojiGroup group = loadGroup("heart:\n  emoticon: '<3'\n  emoji: '❤'");
        Emoji emoji = group.getEmojis().get(0);
        assertEquals("heart", emoji.getName());
    }

    @Test
    void init_nameWithUnderscoresAndSpaces_sanitized() throws Exception {
        EmojiGroup group = loadGroup("my_cool_emoji:\n  emoticon: ':cool:'\n  emoji: '😎'");
        Emoji emoji = group.getEmojis().get(0);
        assertEquals("my-cool-emoji", emoji.getName());
    }

    @Test
    void init_singleEmoticonString_parsedCorrectly() throws Exception {
        EmojiGroup group = loadGroup("test:\n  emoticon: ':test:'\n  emoji: 'T'");
        Emoji emoji = group.getEmojis().get(0);
        assertEquals(1, emoji.getEmoticons().size());
        assertEquals(":test:", emoji.getEmoticons().get(0));
    }

    @Test
    void init_multipleEmoticonsList_parsedCorrectly() throws Exception {
        EmojiGroup group = loadGroup("joy:\n  emoticon: ['^_^', '^-^']\n  emoji: 'J'");
        Emoji emoji = group.getEmojis().get(0);
        assertEquals(2, emoji.getEmoticons().size());
        assertEquals("^_^", emoji.getEmoticons().get(0));
        assertEquals("^-^", emoji.getEmoticons().get(1));
    }

    @Test
    void init_intEmoticon_convertedToString() throws Exception {
        EmojiGroup group = loadGroup("num:\n  emoticon: 42\n  emoji: 'N'");
        Emoji emoji = group.getEmojis().get(0);
        assertEquals(1, emoji.getEmoticons().size());
        assertEquals("42", emoji.getEmoticons().get(0));
    }

    @Test
    void init_emptyEmoticon_throwsInvalidEmoticonException() {
        assertThrows(InvalidEmoticonException.class, () ->
            initEmoji("test:\n  emoticon: ''\n  emoji: 'T'", "test")
        );
    }

    @Test
    void init_missingEmoticonField_throwsInvalidEmoticonException() {
        assertThrows(InvalidEmoticonException.class, () ->
            initEmoji("test:\n  emoji: 'T'", "test")
        );
    }

    @Test
    void init_singleEmojiWithColorCode_translatesAlternateColors() throws Exception {
        EmojiGroup group = loadGroup("heart:\n  emoticon: '<3'\n  emoji: '&c❤'");
        Emoji emoji = group.getEmojis().get(0);
        assertEquals(1, emoji.getEmojis().size());
        assertEquals("§c❤", emoji.getEmojis().get(0));
    }

    @Test
    void init_multipleEmojisList_allStored() throws Exception {
        EmojiGroup group = loadGroup("music:\n  emoticon: ':music:'\n  emoji: ['A', 'B', 'C']");
        Emoji emoji = group.getEmojis().get(0);
        assertEquals(3, emoji.getEmojis().size());
    }

    @Test
    void init_emptyEmoji_throwsInvalidEmojiException() {
        assertThrows(InvalidEmojiException.class, () ->
            initEmoji("test:\n  emoticon: ':test:'\n  emoji: ''", "test")
        );
    }

    @Test
    void init_missingEmojiField_throwsInvalidEmojiException() {
        assertThrows(InvalidEmojiException.class, () ->
            initEmoji("test:\n  emoticon: ':test:'", "test")
        );
    }

    // --- Regex ---

    @Test
    void init_validRegex_createsPattern() throws Exception {
        EmojiGroup group = loadGroup("test:\n  emoticon: ':test:'\n  emoji: 'T'\n  regex: '/:tests?:/'");
        Emoji emoji = group.getEmojis().get(0);
        assertNotNull(emoji.getPattern());
        assertTrue(emoji.getPattern().matcher(":test:").find());
        assertTrue(emoji.getPattern().matcher(":tests:").find());
    }

    @Test
    void init_caseInsensitiveRegex_hasCaseInsensitiveFlag() throws Exception {
        EmojiGroup group = loadGroup("test:\n  emoticon: ':test:'\n  emoji: 'T'\n  regex: '/:test:/i'");
        Emoji emoji = group.getEmojis().get(0);
        assertTrue((emoji.getPattern().flags() & Pattern.CASE_INSENSITIVE) != 0);
    }

    @Test
    void init_invalidRegex_throwsInvalidRegexException() {
        assertThrows(InvalidRegexException.class, () ->
            initEmoji("test:\n  emoticon: ':test:'\n  emoji: 'T'\n  regex: 'invalid'", "test")
        );
    }

    @Test
    void init_noRegex_autoPatternFromEmoticons() throws Exception {
        EmojiGroup group = loadGroup("test:\n  emoticon: ':test:'\n  emoji: 'T'");
        Emoji emoji = group.getEmojis().get(0);
        assertNotNull(emoji.getPattern());
        assertTrue(emoji.getPattern().matcher(":test:").find());
        assertFalse(emoji.getPattern().matcher(":other:").find());
    }

    // --- Enabled/Disabled ---

    @Test
    void init_defaultEnabled_isTrue() throws Exception {
        EmojiGroup group = loadGroup("test:\n  emoticon: ':test:'\n  emoji: 'T'");
        assertTrue(group.getEmojis().get(0).isEnabled());
    }

    @Test
    void init_enabledFalse_isDisabled() throws Exception {
        EmojiGroup group = loadGroup("test:\n  emoticon: ':test:'\n  emoji: 'T'\n  enabled: false");
        assertFalse(group.getEmojis().get(0).isEnabled());
    }

    @Test
    void init_enabledZero_isDisabled() throws Exception {
        EmojiGroup group = loadGroup("test:\n  emoticon: ':test:'\n  emoji: 'T'\n  enabled: 0");
        assertFalse(group.getEmojis().get(0).isEnabled());
    }

    @Test
    void init_enabledNegativeOne_isDisabled() throws Exception {
        EmojiGroup group = loadGroup("test:\n  emoticon: ':test:'\n  emoji: 'T'\n  enabled: -1");
        assertFalse(group.getEmojis().get(0).isEnabled());
    }

    @Test
    void enableDisable_togglingWorks() throws Exception {
        EmojiGroup group = loadGroup("test:\n  emoticon: ':test:'\n  emoji: 'T'");
        Emoji emoji = group.getEmojis().get(0);

        assertTrue(emoji.isEnabled());
        emoji.disable();
        assertFalse(emoji.isEnabled());
        emoji.enable();
        assertTrue(emoji.isEnabled());
    }

    // --- Parsing ---

    @Test
    void parse_singleEmoji_replacesAllOccurrences() throws Exception {
        PlayerMock player = createOpPlayer();
        EmojiGroup group = loadGroup("heart:\n  emoticon: '<3'\n  emoji: '❤'");
        Emoji emoji = group.getEmojis().get(0);

        ParsingContext ctx = ParsingContext.builder()
            .player(player)
            .message("I <3 you")
            .build();

        emoji.parse(ctx);
        assertTrue(ctx.getMessage().contains("❤"), "Message should contain emoji: " + ctx.getMessage());
        assertFalse(ctx.getMessage().contains("<3"), "Emoticon should be replaced: " + ctx.getMessage());
    }

    @Test
    void parse_replacementIncludesReset() throws Exception {
        PlayerMock player = createOpPlayer();
        EmojiGroup group = loadGroup("heart:\n  emoticon: '<3'\n  emoji: '❤'");
        Emoji emoji = group.getEmojis().get(0);

        ParsingContext ctx = ParsingContext.builder()
            .player(player)
            .message("<3")
            .build();

        emoji.parse(ctx);
        assertTrue(ctx.getMessage().contains(ChatColor.RESET.toString()),
            "Replacement should include RESET: " + ctx.getMessage());
    }

    @Test
    void parse_disabledEmoji_returnsUnchanged() throws Exception {
        PlayerMock player = createOpPlayer();
        EmojiGroup group = loadGroup("heart:\n  emoticon: '<3'\n  emoji: '❤'\n  enabled: false");
        Emoji emoji = group.getEmojis().get(0);

        ParsingContext ctx = ParsingContext.builder()
            .player(player)
            .message("I <3 you")
            .build();

        emoji.parse(ctx);
        assertEquals("I <3 you", ctx.getMessage());
    }

    @Test
    void parse_noPermission_returnsUnchanged() throws Exception {
        PlayerMock player = createPlayer(); // no permissions, not op
        EmojiGroup group = loadGroup("heart:\n  emoticon: '<3'\n  emoji: '❤'");
        Emoji emoji = group.getEmojis().get(0);

        ParsingContext ctx = ParsingContext.builder()
            .player(player)
            .message("I <3 you")
            .build();

        emoji.parse(ctx);
        assertEquals("I <3 you", ctx.getMessage());
    }

    @Test
    void parse_forced_bypassesPermission() throws Exception {
        PlayerMock player = createPlayer(); // no permissions
        EmojiGroup group = loadGroup("heart:\n  emoticon: '<3'\n  emoji: '❤'");
        Emoji emoji = group.getEmojis().get(0);

        ParsingContext ctx = ParsingContext.builder()
            .player(player)
            .message("I <3 you")
            .forced(true)
            .build();

        emoji.parse(ctx);
        assertTrue(ctx.getMessage().contains("❤"));
    }

    @Test
    void parse_updatesUseMap() throws Exception {
        PlayerMock player = createOpPlayer();
        EmojiGroup group = loadGroup("heart:\n  emoticon: '<3'\n  emoji: '❤'");
        Emoji emoji = group.getEmojis().get(0);

        ParsingContext ctx = ParsingContext.builder()
            .player(player)
            .message("<3")
            .build();

        emoji.parse(ctx);
        assertTrue(ctx.getUseMap().containsKey(emoji));
        assertTrue(ctx.getUseMap().get(emoji) >= 1);
    }

    // --- messureHit ---

    @Test
    void messureHit_matchFound_returnsMaxMatchLength() throws Exception {
        EmojiGroup group = loadGroup("heart:\n  emoticon: '<3'\n  emoji: '❤'");
        Emoji emoji = group.getEmojis().get(0);
        assertEquals(2, emoji.messureHit("I <3 you"));
    }

    @Test
    void messureHit_disabled_returnsNegativeOne() throws Exception {
        EmojiGroup group = loadGroup("heart:\n  emoticon: '<3'\n  emoji: '❤'\n  enabled: false");
        Emoji emoji = group.getEmojis().get(0);
        assertEquals(-1, emoji.messureHit("I <3 you"));
    }

    @Test
    void messureHit_noMatch_returnsNegativeOne() throws Exception {
        EmojiGroup group = loadGroup("heart:\n  emoticon: '<3'\n  emoji: '❤'");
        Emoji emoji = group.getEmojis().get(0);
        assertEquals(-1, emoji.messureHit("no match here"));
    }

    // --- Permissions ---

    @Test
    void permission_rootLevel_formatCorrect() throws Exception {
        EmojiGroup group = loadGroup("heart:\n  emoticon: '<3'\n  emoji: '❤'");
        Emoji emoji = group.getEmojis().get(0);
        assertEquals("chatemojis.use.heart", emoji.getPermission().getName());
    }

    @Test
    void permission_nested_formatCorrect() throws Exception {
        EmojiGroup group = loadGroup("mygroup:\n  heart:\n    emoticon: '<3'\n    emoji: '❤'");
        Emoji emoji = group.getGroups().get(0).getEmojis().get(0);
        assertEquals("chatemojis.use.mygroup.heart", emoji.getPermission().getName());
    }

    // --- Limits ---

    @Test
    void getLimit_withSpecificLimit_returnsEmojiLimit() throws Exception {
        EmojiGroup group = loadGroup("heart:\n  emoticon: '<3'\n  emoji: '❤'\n  limit: 5");
        Emoji emoji = group.getEmojis().get(0);
        assertEquals(5, emoji.getLimit());
    }

    @Test
    void getLimit_withoutLimit_fallsBackToGlobal() throws Exception {
        EmojiGroup group = loadGroup("heart:\n  emoticon: '<3'\n  emoji: '❤'");
        Emoji emoji = group.getEmojis().get(0);
        assertEquals(-1, emoji.getLimit());
    }

    @Test
    void hasLimit_withNegativeOne_returnsFalse() throws Exception {
        EmojiGroup group = loadGroup("heart:\n  emoticon: '<3'\n  emoji: '❤'");
        Emoji emoji = group.getEmojis().get(0);
        assertFalse(emoji.hasLimit());
    }

    @Test
    void hasLimit_withPositiveLimit_returnsTrue() throws Exception {
        EmojiGroup group = loadGroup("heart:\n  emoticon: '<3'\n  emoji: '❤'\n  limit: 2");
        Emoji emoji = group.getEmojis().get(0);
        assertTrue(emoji.hasLimit());
    }

    // --- getEmoji ---

    @Test
    void getEmoji_singleEmoji_returnsThatEmoji() throws Exception {
        EmojiGroup group = loadGroup("heart:\n  emoticon: '<3'\n  emoji: '❤'");
        Emoji emoji = group.getEmojis().get(0);
        assertEquals("❤", emoji.getEmoji());
    }

    @Test
    void getEmoji_multipleEmojis_returnsOneOfThem() throws Exception {
        EmojiGroup group = loadGroup("music:\n  emoticon: ':music:'\n  emoji: ['A', 'B', 'C']");
        Emoji emoji = group.getEmojis().get(0);

        String result = emoji.getEmoji();
        assertTrue("A".equals(result) || "B".equals(result) || "C".equals(result),
            "Expected one of A, B, C but got: " + result);
    }
}
