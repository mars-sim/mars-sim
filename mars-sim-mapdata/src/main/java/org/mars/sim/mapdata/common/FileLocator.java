/*
 * Mars Simulation Project
 * FileLocator.java
 * @date 2023-05-09
 * @author Barry Evans
 */
package org.mars.sim.mapdata.common;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A static helper class to locate files for the configuration of the system.
 */
public final class FileLocator {
    private static Logger logger = Logger.getLogger(FileLocator.class.getName());

    private static final String DOWNLOAD_DIR = "/downloads";
    private static File localBase = new File(System.getProperty("user.home")
                                                +  "/.mars-sim" + DOWNLOAD_DIR);
    private static String contentURL = "https://raw.githubusercontent.com/mars-sim/mars-sim/master/content";

    private  FileLocator() {
    }
    
    /**
     * Sets the base directory where files are cached.
     * 
     * @param newBase Folder to cache downloaded files
     * 
     */
    public static void setBaseDir(String newBase) {
        localBase = new File(newBase, DOWNLOAD_DIR);
    }

    /**
     * Sets the URL of the remote content store.
     * 
     * @param newURL
     */
    public static void setContentURL(String newURL) {
        logger.info("Content URL changed to " + newURL);
        contentURL = newURL;
    }

    /**
     * Is this file locally available and does not need a download ?
     * 
     * @param name
     * @return
     */
    public static boolean isLocallyAvailable(String name) {
        File localFile = new File(localBase, name);
        boolean result = localFile.exists();
        if (!result) {
            // Check the resource
            InputStream stream = FileLocator.class.getResourceAsStream(name);
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    logger.warning("Problem closing search stream");
                }
                result = true;
            }
        }
        return result;
    }

    /**
     * Locates an external file used in the configuration.
     * 
     * @param name Name will be a partial path
     * @return
     */
    public static File locateFile(String name) {
        // Check file is already downloaded
        File localFile = new File(localBase, name);
        if (!localFile.exists()) {
            File folder = localFile.getParentFile();
            folder.mkdirs();

            // Attempt to find the file in the resources
            InputStream resourceStream = FileLocator.class.getResourceAsStream(name);
            if (resourceStream != null) {
                try {
                    logger.info("Extracting from resources " + name + " -> " + localFile);
                    copyFile(resourceStream, localFile);
                } catch (IOException ioe) {
                    logger.log(Level.SEVERE, "Problem extracting file", ioe);
                }
                finally {
                    try {
                        resourceStream.close();
                    } catch (IOException e) {
                        logger.warning("Problem closing stream");
                    }
                }
            }
            else {
                // Attempt to find the file to download
                String fileURL = contentURL + name;
                logger.info("Downloading " + fileURL + " -> " + localFile);
                try (InputStream source = new URL(fileURL).openStream()) {
                    copyFile(source, localFile);
                }
                catch (IOException ioe) {
                    logger.log(Level.SEVERE, "Problem downloading file", ioe);
                }
            }
        }

        return localFile;
    }

    /**
     * Downloads a file from the remote repository.
     * 
     * @param url
     * @param dest
     * @throws IOException
     */
    private static void copyFile(InputStream source, File dest) throws IOException {

        try (BufferedInputStream in = new BufferedInputStream(source);
            FileOutputStream fileOutputStream = new FileOutputStream(dest)) {
            byte dataBuffer[] = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
        }
    }
}
