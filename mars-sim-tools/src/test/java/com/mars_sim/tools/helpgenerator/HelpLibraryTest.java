package com.mars_sim.tools.helpgenerator;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import com.mars_sim.core.SimulationConfig;

class HelpLibraryTest {

    @Test
    void testCreateLibrary() throws IOException {
		// Load config files
		var config = SimulationConfig.loadConfig();

        File output = Files.createTempDirectory("generator").toFile();
        try {
            var partDetails = HelpLibrary.class.getResourceAsStream("/templates/html-help/part-detail.mustache");
            assertNotNull(partDetails, "Parts details can be found");

            var library = new HelpLibrary(config, output);
            var entryPoint = library.getPage(HelpLibrary.STARTING_PAGE);

            assertNotNull(entryPoint, "Help starting file");
        }
        finally {
            // Clean up
            FileUtils.deleteDirectory(output);
        }
    }

    @Test
    void testChangedVersion() throws IOException {
		// Load config files
		var config = SimulationConfig.loadConfig();

        File output = Files.createTempDirectory("generator").toFile();
        try {
            new HelpLibrary(config, output);

            File generatedDir = new File(output, HelpLibrary.GENERATED_DIR);
            FileUtils.deleteDirectory(generatedDir);
            assertTrue(!generatedDir.exists(), "Generated removed");

            // Check library is not recreated when version is the same
            new HelpLibrary(config, output);
            assertTrue(!generatedDir.exists(), "Generated not recreated");

            // Change version file
            try(FileWriter version = new FileWriter(new File(output, HelpLibrary.VERSION_FILE))) {
                version.write("#Any old rubbish");
            }
            new HelpLibrary(config, output);
            assertTrue(generatedDir.exists(), "Generated recreated");

        }
        finally {
            // Clean up
            FileUtils.deleteDirectory(output);
        }
    }
}
