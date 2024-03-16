package com.mars_sim.core.tool;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

class ResourceCacheTest {

    private static final String TEST_RESOURCE = "/xml/simulation.xml";
    private static final String DEST_NAME = "dest";

    @Test
    void testDoubleExtract() throws IOException {
        File output = Files.createTempDirectory("cache").toFile();
        try {
            var cache = new ResourceCache(output, true);
            var extracted = cache.extractContent(TEST_RESOURCE, DEST_NAME);
            long origTime = extracted.lastModified();

            assertNotNull("File extracted", extracted);
            assertEquals("Name of file", DEST_NAME, extracted.getName());

            // Check contents
            String extractedChecksum = Hash.MD5.getChecksumString(extracted);
            String resourceChecksum = Hash.MD5.getChecksumString(getClass().getResourceAsStream(TEST_RESOURCE));
            assertEquals("Contents match", resourceChecksum, extractedChecksum);

            // Extract against
            var extracted2 = cache.extractContent(TEST_RESOURCE, DEST_NAME);
            assertEquals("Files not changed", origTime,
                                                        extracted2.lastModified());
        }
        finally {
            // Clean up
            FileUtils.deleteDirectory(output); 
        }
    }

    @Test
    void testExtractOverwrite() throws IOException, InterruptedException {
        File output = Files.createTempDirectory("cache").toFile();
        try {
            // Write different content to file first
            File destFile = new File(output, DEST_NAME);
            PrintWriter out = new PrintWriter(destFile);
            out.println("Example text");
            out.close();
            long origSize = destFile.length();
            long origTime = destFile.lastModified();

            // Add a pause to ensure file time change
            Thread.sleep(1000);
            
            // Extract to overwrite exisitng file
            var cache = new ResourceCache(output, true);
            var extracted = cache.extractContent(TEST_RESOURCE, DEST_NAME);

            assertNotEquals("File size changed", origSize, extracted.length());
            assertNotEquals("File reextracted over change", origTime,
                                                        extracted.lastModified());

            // Check the present of a backup
            File backupCopy = new File(output, ResourceCache.BACKUP_DIR + "/" + DEST_NAME);
            assertTrue("Backup file exists", backupCopy.isFile());
        }
        finally {
            // Clean up
            FileUtils.deleteDirectory(output); 
        }
    }

    @Test
    void testExtractRepeated() throws IOException {
        File output = Files.createTempDirectory("cache").toFile();
        try {
            // Extract to file initially
            var cache = new ResourceCache(output, true);
            var extracted = cache.extractContent(TEST_RESOURCE, DEST_NAME);
            long origSize = extracted.length();
            long origTime = extracted.lastModified();

            // Extract to file again with a new ResourceCace
            var cache2 = new ResourceCache(output, true);
            var extracted2 = cache2.extractContent(TEST_RESOURCE, DEST_NAME);
            assertEquals("File size not changed", origSize, extracted2.length());
            assertEquals("File unchanged", origTime, extracted2.lastModified());
        }
        finally {
            // Clean up
            FileUtils.deleteDirectory(output); 
        }
    }

    @Test
    void testExcluded() throws IOException {
        File output = Files.createTempDirectory("cache").toFile();
        try {
            // Write different content to file first
            File destFile = new File(output, DEST_NAME);
            PrintWriter out = new PrintWriter(destFile);
            out.println("Example text");
            out.close();
            long origSize = destFile.length();
            long origTime = destFile.lastModified();

            // Add file to the excluded
            File excludedFiles = new File(output, ResourceCache.EXCEPTION_FILE);
            PrintWriter out2 = new PrintWriter(excludedFiles);
            out2.println(DEST_NAME);
            out2.close();

            // Extract to overwrite exisitng file
            var cache = new ResourceCache(output, true);
            var extracted = cache.extractContent(TEST_RESOURCE, DEST_NAME);

            // Extract files should match the original as itis excluded
            assertEquals("File size", origSize, extracted.length());
            assertEquals("File change", origTime, extracted.lastModified());


        }
        finally {
            // Clean up
            FileUtils.deleteDirectory(output); 
        }
    }
}
