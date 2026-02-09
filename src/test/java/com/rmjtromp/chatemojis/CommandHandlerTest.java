package com.rmjtromp.chatemojis;

import be.seeseemelk.mockbukkit.entity.PlayerMock;
import org.bukkit.command.Command;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CommandHandlerTest extends TestBase {

    private CommandHandler handler;
    private Command command;

    @BeforeEach
    void setUpCommand() {
        handler = new CommandHandler();
        command = server.getPluginCommand("emoji");
    }

    // --- Permission checks ---

    @Test
    void onCommand_noPermission_sendsDenied() {
        PlayerMock player = createPlayer(); // no permissions
        handler.onCommand(player, command, "emoji", new String[]{});
        String lastMsg = player.nextMessage();
        assertNotNull(lastMsg);
        assertTrue(lastMsg.contains("permission"), "Expected permission denied message: " + lastMsg);
    }

    @Test
    void onCommand_withListPermission_showsList() {
        PlayerMock player = createPlayer("chatemojis.list", "chatemojis.use.*");
        handler.onCommand(player, command, "emoji", new String[]{});
        // The player should receive spigot component messages - no assertion on nextMessage
        // since spigot().sendMessage uses a different pathway, just verify no exception
    }

    // --- Help subcommand ---

    @Test
    void onCommand_help_sendsHelpText() {
        PlayerMock player = createPlayer("chatemojis.list");
        handler.onCommand(player, command, "emoji", new String[]{"help"});
        String msg = player.nextMessage();
        assertNotNull(msg);
        assertTrue(msg.contains("ChatEmojis"), "Expected help text: " + msg);
    }

    // --- Version subcommand ---

    @Test
    void onCommand_version_sendsVersionInfo() {
        PlayerMock player = createPlayer("chatemojis.list");
        handler.onCommand(player, command, "emoji", new String[]{"version"});
        String msg = player.nextMessage();
        assertNotNull(msg);
        assertTrue(msg.contains("ChatEmojis"), "Expected version info: " + msg);
    }

    @Test
    void onCommand_v_sendsVersionInfo() {
        PlayerMock player = createPlayer("chatemojis.list");
        handler.onCommand(player, command, "emoji", new String[]{"v"});
        String msg = player.nextMessage();
        assertNotNull(msg);
        assertTrue(msg.contains("ChatEmojis"), "Expected version info: " + msg);
    }

    @Test
    void onCommand_ver_sendsVersionInfo() {
        PlayerMock player = createPlayer("chatemojis.list");
        handler.onCommand(player, command, "emoji", new String[]{"ver"});
        String msg = player.nextMessage();
        assertNotNull(msg);
        assertTrue(msg.contains("ChatEmojis"), "Expected version info: " + msg);
    }

    // --- Reload subcommand ---

    @Test
    void onCommand_reload_withPermission_reloads() {
        PlayerMock player = createPlayer("chatemojis.list", "chatemojis.reload");
        handler.onCommand(player, command, "emoji", new String[]{"reload"});
        String msg = player.nextMessage();
        assertNotNull(msg);
        assertTrue(msg.contains("reloaded"), "Expected reload confirmation: " + msg);
    }

    @Test
    void onCommand_reload_withoutPermission_denied() {
        PlayerMock player = createPlayer("chatemojis.list");
        handler.onCommand(player, command, "emoji", new String[]{"reload"});
        String msg = player.nextMessage();
        assertNotNull(msg);
        assertTrue(msg.contains("permission"), "Expected permission denied: " + msg);
    }

    // --- Dump subcommand ---

    @Test
    void onCommand_dump_withAdminPermission_sendsDump() {
        PlayerMock player = createPlayer("chatemojis.list", "chatemojis.admin");
        handler.onCommand(player, command, "emoji", new String[]{"dump"});
        String msg = player.nextMessage();
        assertNotNull(msg);
        assertTrue(msg.contains("Dump"), "Expected dump info: " + msg);
    }

    @Test
    void onCommand_dump_withoutAdmin_denied() {
        PlayerMock player = createPlayer("chatemojis.list");
        handler.onCommand(player, command, "emoji", new String[]{"dump"});
        String msg = player.nextMessage();
        assertNotNull(msg);
        assertTrue(msg.contains("permission"), "Expected permission denied: " + msg);
    }

    // --- Settings subcommand ---

    @Test
    void onCommand_settings_asPlayer_withoutAdmin_denied() {
        PlayerMock player = createPlayer("chatemojis.list");
        handler.onCommand(player, command, "emoji", new String[]{"settings"});
        String msg = player.nextMessage();
        assertNotNull(msg);
        assertTrue(msg.contains("permission"), "Expected permission denied: " + msg);
    }

    // --- Unknown argument ---

    @Test
    void onCommand_unknownArg_sendsError() {
        PlayerMock player = createPlayer("chatemojis.list");
        handler.onCommand(player, command, "emoji", new String[]{"nonexistent"});
        String msg = player.nextMessage();
        assertNotNull(msg);
        assertTrue(msg.contains("Unknown argument") || msg.contains("help"),
            "Expected error message: " + msg);
    }

    // --- Too many arguments ---

    @Test
    void onCommand_tooManyArgs_sendsError() {
        PlayerMock player = createPlayer("chatemojis.list");
        handler.onCommand(player, command, "emoji", new String[]{"arg1", "arg2", "arg3"});
        String msg = player.nextMessage();
        assertNotNull(msg);
        assertTrue(msg.contains("Too many arguments") || msg.contains("help"),
            "Expected too many args error: " + msg);
    }

    // --- Tab completion ---

    @Test
    void tabComplete_noInput_returnsBaseCompletions() {
        PlayerMock player = createPlayer("chatemojis.list");
        List<String> completions = handler.onTabComplete(player, command, "emoji", new String[]{""});
        assertNotNull(completions);
        assertTrue(completions.contains("list"));
        assertTrue(completions.contains("help"));
        assertTrue(completions.contains("version"));
    }

    @Test
    void tabComplete_withReloadPermission_includesReload() {
        PlayerMock player = createPlayer("chatemojis.list", "chatemojis.reload");
        List<String> completions = handler.onTabComplete(player, command, "emoji", new String[]{""});
        assertNotNull(completions);
        assertTrue(completions.contains("reload"));
    }

    @Test
    void tabComplete_withAdminPermission_includesDumpAndSettings() {
        PlayerMock player = createPlayer("chatemojis.list", "chatemojis.admin");
        List<String> completions = handler.onTabComplete(player, command, "emoji", new String[]{""});
        assertNotNull(completions);
        assertTrue(completions.contains("dump"));
        assertTrue(completions.contains("settings"));
    }

    @Test
    void tabComplete_partialInput_filtersResults() {
        PlayerMock player = createPlayer("chatemojis.list", "chatemojis.reload");
        List<String> completions = handler.onTabComplete(player, command, "emoji", new String[]{"re"});
        assertNotNull(completions);
        assertTrue(completions.contains("reload"));
        assertFalse(completions.contains("list"));
        assertFalse(completions.contains("help"));
    }

    @Test
    void tabComplete_secondArg_returnsEmptyList() {
        PlayerMock player = createPlayer("chatemojis.list");
        List<String> completions = handler.onTabComplete(player, command, "emoji", new String[]{"list", ""});
        assertNotNull(completions);
        assertTrue(completions.isEmpty());
    }
}
