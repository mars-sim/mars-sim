package com.mars_sim.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.jupiter.api.Test;

class VersionTest {
    @Test
    void testSaveAndReload() throws IOException {
        Version orig = new Version("A", "B", true, "date");
        byte[] content;
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            orig.store(output);
            output.close();
            content = output.toByteArray();
        }

        // Reload
        try  (ByteArrayInputStream input = new ByteArrayInputStream(content)) {
            var reloaded = Version.fromStream(input);
            assertEquals(orig, reloaded, "Versions are the same");
        }
    }

    @Test
    void testDefaultVersion() {
        var build = SimulationRuntime.VERSION;

        assertTrue(!build.getVersionTag().isEmpty(), "Default version");
        assertTrue(!build.getBuild().isEmpty(), "Default build");

    }
}
