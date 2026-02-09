package com.rmjtromp.chatemojis.utils;

import com.rmjtromp.chatemojis.TestBase;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BukkitUtilsTest extends TestBase {

    @Test
    void getPlugin_returnsNonNull() {
        assertNotNull(BukkitUtils.getPlugin());
    }

    @Test
    void getServerVersion_returnsVersionString() {
        String version = BukkitUtils.getServerVersion();
        assertNotNull(version);
        assertTrue(version.startsWith("v"), "Server version should start with 'v', got: " + version);
    }

    @Test
    void colorEncode_singleString_translatesColorCodes() {
        String result = BukkitUtils.colorEncode("&cRed");
        assertEquals("§cRed", result);
    }

    @Test
    void colorEncode_collection_encodesAllElements() {
        List<String> input = Arrays.asList("&cRed", "&aGreen");
        List<String> result = BukkitUtils.colorEncode(input);
        assertEquals(2, result.size());
        assertEquals("§cRed", result.get(0));
        assertEquals("§aGreen", result.get(1));
    }

    @Test
    void colorEncode_varargs_encodesAllStrings() {
        List<String> result = BukkitUtils.colorEncode("&cRed", "&aGreen", "&9Blue");
        assertEquals(3, result.size());
        assertEquals("§cRed", result.get(0));
        assertEquals("§aGreen", result.get(1));
        assertEquals("§9Blue", result.get(2));
    }
}
