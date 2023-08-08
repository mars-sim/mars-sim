/*
 * Mars Simulation Project
 * FileLocator.java
 * @date 2023-05-09
 * @author Barry Evans
 */
package org.mars.sim.mapdata.common;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.tukaani.xz.BasicArrayCache;
import org.tukaani.xz.XZInputStream;

/**
 * A static helper class to locate files for the configuration of the system.
 */
public final class FileLocator {

    private static Logger logger = Logger.getLogger(FileLocator.class.getName());
    private static final String ZIP = ".zip";
    private static final String XZ = ".xz";
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
     * Specialises where the remote content can be found.
     * 
     * @param baseURL
     */
    public static void setBaseURL(String baseURL) {
        contentURL = baseURL;
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

            // Select the location of the file
            // Attempt to find the file in the resources
            String source = "bundled file";
            InputStream resourceStream = FileLocator.class.getResourceAsStream(name);
            
            if (resourceStream == null) {
            	// Try xz in bundle
                resourceStream = FileLocator.class.getResourceAsStream(name + XZ);
                
                try (XZInputStream xzStream = new XZInputStream(resourceStream, BasicArrayCache.getInstance())) {
                	source = "bundled XZ";
                	
	                logger.info("Extracting from " + name + " from " + source);
                	Files.copy(xzStream, localFile.toPath(), StandardCopyOption.REPLACE_EXISTING);    	
                	
                	return localFile;

                } catch (FileNotFoundException e) {
                	 logger.warning("Problem finding the file.");
                } catch (IOException e) {
                	 logger.warning("Problem with IO.");
                }
            	
                
                if (resourceStream == null) {
	                // Try zip in bundle
	                resourceStream = FileLocator.class.getResourceAsStream(name + ZIP);
	                if (resourceStream != null) {
	                    resourceStream = getFileFromZip(resourceStream, name);
	                    source = "bundled ZIP";
	                }
	                else {
	                    // Try file in remote
	                    resourceStream = openRemoteContent(name);
	                    if (resourceStream != null) {
	                        source = "remote file";
	                    }
	                    else {
	                        resourceStream = openRemoteContent(name + ZIP);
	                        if (resourceStream != null) {
	                            resourceStream = getFileFromZip(resourceStream, name);
	                            source = "remote ZIP";
	                        }
	                        else {
	                            logger.severe("Cannot locate file " + name);
	                            return null;
	                        }
	                    }
	                }
	            }
	
	            // Have a source location
	            if (resourceStream != null) {
	                logger.info("Extracting from " + name + " from " + source);
	                try {
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
	                logger.warning("Cannot find file " + name);
	            }
            }
        }

        return localFile;
    }

    /**
     * Attempts to find a file in the remote content repository.
     * 
     * @param name Name of the file to locate
     * @return
     */
    private static InputStream openRemoteContent(String name) {
        // Check remote content
        String fileURL = contentURL + name;
        try {
            return  new URL(fileURL).openStream();
        } catch (IOException e) {
            // URL is no good
            return null;
        }    
    }

    /**
     * Picks the required file from a zip file.
     * 
     * @param resourceStream Zip content
     * @param name File to extract
     * @return Stream to the file contents
     */
    private static InputStream getFileFromZip(InputStream resourceStream, String name) {
        ZipInputStream zip = new ZipInputStream(resourceStream);
        try {
            ZipEntry ze = zip.getNextEntry();
            String [] parts = name.split("/");
            if (!ze.getName().equals(parts[parts.length-1])) {
                logger.severe("Zip file does not contain file " + name);
                return null;
            }
            return zip;
        } catch (IOException e) {
            // Problem with the ZIP
            logger.severe("Zip file can not be read " + name);
        }
        return null;
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
