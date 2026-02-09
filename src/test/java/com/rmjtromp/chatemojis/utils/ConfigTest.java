package com.rmjtromp.chatemojis.utils;

import com.rmjtromp.chatemojis.TestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ConfigTest extends TestBase {

    @TempDir
    Path tempDir;

    @Test
    void init_withFile_createsEmptyFileAndLoads() throws Exception {
        File file = tempDir.resolve("test.yml").toFile();
        assertFalse(file.exists());

        Config config = Config.init(file);
        assertTrue(file.exists());
        assertNotNull(config);
    }

    @Test
    void init_withFileAndResource_copiesResourceIfFileMissing() throws Exception {
        File file = tempDir.resolve("config.yml").toFile();
        assertFalse(file.exists());

        Config config = Config.init(file, "config.yml");
        assertTrue(file.exists());
        assertTrue(file.length() > 0, "Config file should have content from resource");
        assertNotNull(config);
    }

    @Test
    void init_withNullFile_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () ->
            Config.init(null, "config.yml")
        );
    }

    @Test
    void configurationReference_getValue_returnsConfigValue() throws Exception {
        File file = tempDir.resolve("ref-test.yml").toFile();
        Config config = Config.init(file);
        config.set("test.key", "hello");

        Config.ConfigurationReference<String> ref = config.reference("test.key", "default");
        assertEquals("hello", ref.getValue());
    }

    @Test
    void configurationReference_getNonNullValue_returnsDefaultWhenNotSet() throws Exception {
        File file = tempDir.resolve("ref-default.yml").toFile();
        Config config = Config.init(file);

        Config.ConfigurationReference<String> ref = config.reference("missing.key", "default");
        assertEquals("default", ref.getNonNullValue());
    }

    @Test
    void configurationReference_setValue_setsValue() throws Exception {
        File file = tempDir.resolve("ref-set.yml").toFile();
        Config config = Config.init(file);

        Config.ConfigurationReference<Integer> ref = config.reference("num", 10);
        assertEquals(10, ref.getNonNullValue());

        ref.setValue(42);
        assertEquals(42, ref.getNonNullValue());
    }

    @Test
    void configurationReference_isSet_reflectsState() throws Exception {
        File file = tempDir.resolve("ref-isset.yml").toFile();
        Config config = Config.init(file);

        Config.ConfigurationReference<?> ref = config.reference("unset.key");
        assertFalse(ref.isSet());

        config.set("unset.key", "value");
        assertTrue(ref.isSet());
    }

    @Test
    void reload_picksUpFileChanges() throws Exception {
        File file = tempDir.resolve("reload-test.yml").toFile();
        Config config = Config.init(file);

        // Write content directly to file
        Files.write(file.toPath(), "new-key: new-value\n".getBytes());

        config.reload();
        assertEquals("new-value", config.getString("new-key"));
    }
}
