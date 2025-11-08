package com.mars_sim.core.map.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FileLocatorTest {
        private static final String REMOTE_MAP = "/maps/geo_region_2880.jpg";
        private File tmpDir;
        private File returnedFile;

    @BeforeEach
    public void setUp() throws IOException {
        tmpDir = Files.createTempDirectory("map").toFile();
        FileLocator.setBaseDir(tmpDir.getAbsolutePath());
    }

	@AfterEach
    public void tearDown() {
        deleteFolder(tmpDir);
    }

    /**
     * Surely there must be a built in method for this
     * @param folder
     */
    private static void deleteFolder(File folder) {
        File[] allContents = folder.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteFolder(file);
            }
        }
        folder.delete();
    }

    @Test
    void testLocalMapFile() {
        String localName = "/maps/geo_region_1200.jpg"; // This may have to change if packaging changes
        var localVersion = FileLocator.locateFile(localName);

        assertTrue(localVersion.exists(), "Local copy created");
        assertTrue(localVersion.length() > 0, "Local copy content");
        assertTrue(FileLocator.isLocallyAvailable(localName), "File reports as local");
    }

    @Test
    void testSyncRemoteMapFile() {
        String localName = REMOTE_MAP; // This may have to change if packaging changes
        var localVersion = FileLocator.locateFile(localName);

        assertTrue(localVersion.exists(), "Local copy created");
        assertTrue(localVersion.length() > 0, "Local copy content");
        assertTrue(FileLocator.isLocallyAvailable(localName), "File reports as local");

        localVersion.delete();
        assertFalse(localVersion.exists(), "Cleanup worked");

    }

    @Test
    void testASyncRemoteMapFile() throws InterruptedException {
        String localName = REMOTE_MAP; // This may have to change if packaging changes
        var localVersion = FileLocator.locateFileAsync(localName, f -> callback(f));
        assertNull("File not available", localVersion);
        
        // Wait for file return
        int count = 100;
        while(count-- > 0) {
            if (returnedFile != null) {
                break;
            }
            Thread.sleep(2000);
        }
        assertNotEquals("Async call did not timeout", 0, count);

        assertTrue(returnedFile.exists(), "Local copy created");
        assertTrue(returnedFile.length() > 0, "Local copy content");
        assertTrue(FileLocator.isLocallyAvailable(localName), "File reports as local");

        // Call a 2nd time and should return immediately
        localVersion = FileLocator.locateFileAsync(localName, f -> callback(f));
        assertNotNull("2nd attempt file immediately", localVersion);
        assertEquals(localVersion, returnedFile, "Local and async file are the same");

        localVersion.delete();
        returnedFile = null;
        assertFalse(localVersion.exists(), "Cleanup worked");

    }

    /**
     * Invoked once the async call is completed
     * @param f
     */
    private void callback(File f) {
        assertNotNull("Callback file is present", f);
        assertNull("Callback only once", returnedFile);
        returnedFile = f;
    }

    @Test
    void testLocalXZFile() {
        String localName = "/elevation/megt90n000eb.img"; // This may have to change if packaging changes
        var localVersion = FileLocator.locateFile(localName);

        assertTrue(localVersion.exists(), "Local copy created");
        assertTrue(localVersion.length() > 0, "Local copy content");
        assertTrue(FileLocator.isLocallyAvailable(localName), "File reports as local");
    }

    @Test
    void testLocalZIPFile() {
        String localName = "/elevation/megt90n000cb.img"; // This may have to change if packaging changes
        var localVersion = FileLocator.locateFile(localName);

        assertTrue(localVersion.exists(), "Local copy created");
        assertTrue(localVersion.length() > 0, "Local copy content");
        assertTrue(FileLocator.isLocallyAvailable(localName), "File reports as local");
    }
}
