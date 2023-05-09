package org.mars_sim.mapdata;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.After;
import org.junit.Before;
import org.mars_sim.msp.common.FileLocator;

import junit.framework.TestCase;

/**
 * Unit test suite for the LocalAreaUtil class.
 */
public class TestMapMEGDRMapReader extends TestCase {
    
    private File tmpDir;

    @Before
    public void setUp() throws IOException {
        tmpDir = Files.createTempDirectory("map").toFile();
        FileLocator.setBaseDir(tmpDir.getAbsolutePath());
    }
	
    @After
    public void tearDown() {
        deleteFolder(tmpDir);
    }

    private static void deleteFolder(File folder) {
        File[] allContents = folder.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteFolder(file);
            }
        }
        folder.delete();
    }
    /**
     * Test the locationWithinLocalBoundedObject method.
     */
    // Potentially this downloads a large file
    // public void testMEGDR() {
    //     MEGDRMapReader reader = new MEGDRMapReader();

    //     short[] data = reader.loadElevation();
    //     assertTrue("MEGDR data has content", data.length > 0);
    // }
}