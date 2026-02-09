package com.rmjtromp.chatemojis;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class TestBase {

    private static ServerMock sharedServer;
    private static ChatEmojis sharedPlugin;
    private static boolean initialized = false;

    protected ServerMock server;
    protected ChatEmojis plugin;

    @BeforeAll
    void setUp() {
        if (!initialized) {
            sharedServer = MockBukkit.mock();
            sharedPlugin = MockBukkit.load(ChatEmojis.class);
            initialized = true;
        }
        server = sharedServer;
        plugin = sharedPlugin;
    }

    /**
     * Create a player with specific permissions attached.
     */
    protected PlayerMock createPlayer(String... perms) {
        PlayerMock player = server.addPlayer();
        for (String perm : perms) {
            player.addAttachment(plugin, perm, true);
        }
        return player;
    }

    /**
     * Create an OP player (has all permissions by default since emoji permissions use PermissionDefault.OP).
     */
    protected PlayerMock createOpPlayer() {
        PlayerMock player = server.addPlayer();
        player.setOp(true);
        return player;
    }
}
