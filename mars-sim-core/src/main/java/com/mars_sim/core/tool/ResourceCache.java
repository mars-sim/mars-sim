/*
 * Mars Simulation Project
 * ResourceCache.java
 * @date 2024-03-16
 * @author Barry Evans
 */
package com.mars_sim.core.tool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;

/**
 * This caches resources from the classpath and extracts them to a folder.
 * This can support checksuming the source and the file version for differences.
 * Also files can be excluded from the checksum. 
 */
public class ResourceCache {

    private static final Logger logger = Logger.getLogger(ResourceCache.class.getName());

    static final String BACKUP_DIR = "backup";

	/** The exception.txt denotes any user modified xml to be included to bypass the checksum. */
	public static final String EXCEPTION_FILE = "exception.txt";

    private static final String EXCEPTION_CONTENT = "# Add files to this file to bypass the checksum.\n"
                                                    + "# Files are one per line.";

    private File location;
    private boolean doChecksum;
    private Set<String> alreadyChecked = new HashSet<>();
    private Set<String> excludedFiles = new HashSet<>();

    /**
     * This creates a cache in the given location which populated from resources in the classpath.
     * The found resources can be checked against the file system version.

     * @param location
     * @param doChecksum
     */
    public ResourceCache(File location, boolean doChecksum) {
        this.location = location;
        this.doChecksum = doChecksum;
    
		File exceptionFile = new File(location, EXCEPTION_FILE);
        if (exceptionFile.exists()) {
            // Read the exception.txt file to see if it mentions this particular xml file
            try (BufferedReader buffer = new BufferedReader(new FileReader(exceptionFile))) {
                String nextLine = buffer.readLine();
                // Support commenting out lines
                if (nextLine != null && !nextLine.startsWith("#")) {
                    excludedFiles.add(nextLine);
                }
            } catch (IOException e) {
                logger.log(Level.WARNING, "Problem loading the exception file ", e);
            }
        }
        else {
            exceptionFile.getParentFile().mkdirs();
            try (PrintWriter writer = new PrintWriter(exceptionFile)) {
                writer.println(EXCEPTION_CONTENT);
            }
            catch(FileNotFoundException e) {
                logger.warning("Cannot create the new default exception file " + e.getMessage());
            }
        }
    }

    /**
     * Extract a resource from the classpath to a local file. If the file already exists,
     * it will not be overwritten unless there is a checksum difference.
     * Checksum check can be bypassed on a per file basis via use of the exception file.
     * @param resourceName Name of resoruce to find.
     * @param destName Name of the file destination.
     * @return
     * @throws IOException
     */
    public File extractContent(String resourceName, String destName) throws IOException {	
		// Check existing file
		File existingFile = new File(location, destName);
		if (existingFile.exists()) {
            // Already checked or excluded
            if (alreadyChecked.contains(resourceName) || excludedFiles.contains(destName)) {
                return existingFile;
            }

            // Checksum the bundled resoruce from a new stream
            if (doChecksum) {
                try (InputStream checkStream = ResourceCache.class.getResourceAsStream(resourceName)) {
                    String existingChecksum = Hash.MD5.getChecksumString(existingFile);
                    String resourceChecksum = Hash.MD5.getChecksumString(checkStream);
                    if (existingChecksum.equals(resourceChecksum)) {
                        return existingFile;
                    }
                }
            }
		}

		// Need to extract file to folder
		try (InputStream stream = ResourceCache.class.getResourceAsStream(resourceName)) {
            // Take backup
            if (existingFile.exists()) {
                File dir = getBackupDir();

                // Backup this old (checksum failed) xml file
                FileUtils.copyFileToDirectory(existingFile, dir, true);
                FileUtils.deleteQuietly(existingFile);
            }

			// Copy the xml files from within the jar to user home's xml directory
			FileUtils.copyToFile(stream, existingFile);
		}
        alreadyChecked.add(resourceName); // Don't check it again
        return existingFile;
    }

    private File getBackupDir() {
        File backup = new File(location, BACKUP_DIR);
        backup.mkdirs();

        return backup;
    }

    /**
     * Get the directory controlled by this cache
     * @return
     */
    public File getLocation() {
        return location;
    }
}
