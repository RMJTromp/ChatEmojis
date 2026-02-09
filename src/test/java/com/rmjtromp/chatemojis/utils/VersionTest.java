package com.rmjtromp.chatemojis.utils;

import com.rmjtromp.chatemojis.TestBase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VersionTest extends TestBase {

    @Test
    void isOlderThan_olderVersion_returnsTrue() {
        assertTrue(Version.V1_7.isOlderThan(Version.V1_16));
    }

    @Test
    void isOlderThan_newerVersion_returnsFalse() {
        assertFalse(Version.V1_16.isOlderThan(Version.V1_7));
    }

    @Test
    void isOlderThan_sameVersion_returnsFalse() {
        assertFalse(Version.V1_16.isOlderThan(Version.V1_16));
    }

    @Test
    void isNewerThan_newerVersion_returnsTrue() {
        assertTrue(Version.V1_16.isNewerThan(Version.V1_7));
    }

    @Test
    void isNewerThan_sameVersion_returnsFalse() {
        assertFalse(Version.V1_16.isNewerThan(Version.V1_16));
    }

    @Test
    void isLegacy_v1_12_returnsTrue() {
        assertTrue(Version.V1_12.isLegacy());
    }

    @Test
    void isLegacy_v1_13_returnsFalse() {
        assertFalse(Version.V1_13.isLegacy());
    }

    @Test
    void toString_regularVersion_returnsFormattedString() {
        assertEquals("1.16", Version.V1_16.toString());
        assertEquals("1.7", Version.V1_7.toString());
    }

    @Test
    void toString_unsupported_returnsUnsupported() {
        assertEquals("Unsupported", Version.UNSUPPORTED.toString());
    }

    @Test
    void getServerVersion_returnsNonNull() {
        assertNotNull(Version.getServerVersion());
    }
}
