/*
 * Mars Simulation Project
 * FileLocator.java
 * @date 2023-05-09
 * @author Barry Evans
 */
package org.mars_sim.msp.common;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A static helper class to locate files for the configuration of the system
 */
public final class FileLocator {
    private static Logger logger = Logger.getLogger(FileLocator.class.getName());

    private static final String DOWNLOAD_DIR = "/downloads";
    private static File localBase = new File(System.getProperty("user.home")
                                                +  "/.mars-sim" + DOWNLOAD_DIR);
    private static String baseURL = "https://raw.githubusercontent.com/mars-sim/mars-sim/master/content";

    private  FileLocator() {
    }
    
    /**
     * Set teh base directory where files are cached
     * @param newBase Folder to cache downloaded files
     * 
     */
    public static void setBaseDir(String newBase) {
        localBase = new File(newBase, DOWNLOAD_DIR);
    }

    public static void setBaseURL(String newBaseURL) {
        logger.info("Content URL changed to " + newBaseURL);
        baseURL = newBaseURL;
    }

    /**
     * Locate an external file used in the configuration.
     * @param name Name will be a partial path
     * @return
     */
    public static File locateFile(String name) {
        // Check file is already downloaded
        File localFile = new File(localBase, name);
        if (!localFile.exists()) {
            File folder = localFile.getParentFile();
            folder.mkdirs();

            // Attempt to find the file to download
            String fileURL = baseURL + name;
            downloadFile(fileURL, localFile);
        }

        return localFile;
    }

    /**
     * Download a file from the remote repository
     * @param url
     * @param dest
     */
    private static void downloadFile(String url, File dest) {
        logger.info("Downloading " + url + " -> " + dest);

        try (BufferedInputStream in = new BufferedInputStream(new URL(url).openStream());
            FileOutputStream fileOutputStream = new FileOutputStream(dest)) {
                byte dataBuffer[] = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                    fileOutputStream.write(dataBuffer, 0, bytesRead);
                }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Problem downloading " + url, e);
        }
    }
}
