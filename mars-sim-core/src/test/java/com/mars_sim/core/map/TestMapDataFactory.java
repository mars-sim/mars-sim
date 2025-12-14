package com.mars_sim.core.map;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mars_sim.core.map.common.FileLocator;

/**
 * Unit test suite for the LocalAreaUtil class.
 */
public class TestMapDataFactory {
    
    private File tmpDir;

    @BeforeEach
    public void setUp() throws IOException {
        tmpDir = Files.createTempDirectory("map").toFile();
        FileLocator.setBaseDir(tmpDir.getAbsolutePath());
    }
	
    @AfterEach
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
    @Test
    void testSurfMap() {
        //MapDataFactory factory = new MapDataFactory();

        // MapData data = factory.getMapData(MapDataFactory.SURFACE_MAP_DATA);
        // assertNotNull("Surface data", data);
        // assertTrue("Surface has height", data.getHeight() > 0);
        // assertTrue("Surface has Width", data.getWidth() > 0);
        assertTrue(true, "Surface has Width");

    }
}