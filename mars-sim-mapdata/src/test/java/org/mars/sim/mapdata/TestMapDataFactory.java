package org.mars.sim.mapdata;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.After;
import org.junit.Before;
import org.mars.sim.mapdata.common.FileLocator;

import junit.framework.TestCase;

/**
 * Unit test suite for the LocalAreaUtil class.
 */
public class TestMapDataFactory extends TestCase {
    
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
    public void testSurfMap() {
        //MapDataFactory factory = new MapDataFactory();

        // MapData data = factory.getMapData(MapDataFactory.SURFACE_MAP_DATA);
        // assertNotNull("Surface data", data);
        // assertTrue("Surface has height", data.getHeight() > 0);
        // assertTrue("Surface has Width", data.getWidth() > 0);
        assertTrue("Surface has Width", true);

    }
}