/*
 * Mars Simulation Project
 * FileLocator.java
 * @date 2023-05-09
 * @author Barry Evans
 */
package com.mars_sim.core.map.common;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;
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

    private static ExecutorService executorService;

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
                    logger.warning("Problem closing search stream: " + e);
                }
                result = true;
            }
        }
        return result;
    }

    /**
     * Uses java.nio.file.Files#delete to safely delete a file.
     * 
     * @param path
     * @throws NoSuchFileException
     * @throws DirectoryNotEmptyException
     * @throws IOException
     */
    public void cleanUp(Path path) throws IOException {
    	  Files.delete(path);
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
        if (loadFileLocally(localFile, name)) {
            return localFile;
        }
        
        // Look remotely
        if (loadFileRemotely(localFile, name)) {
            return localFile;
        }
       
        return null;
    }
  
    /**
     * Locates an external file used in the configuration as a background job.
     * 
     * @param name Name will be a partial path
     * @param callback Invoked when the files is loaded in the background. This is not clled if the file is already available.
     * @return File reference if the contents is immediately available
     */
    public static File locateFileAsync(String name, Consumer<File> callback) {
       
        // Check file is already downloaded
        File localFile = new File(localBase, name);
        if (loadFileLocally(localFile, name)) {
            return localFile;
        }
        
        // Trigger the async
        var loader = new ASyncLoad(name, localFile, callback);
        if (executorService == null) {
            executorService = Executors.newSingleThreadExecutor();
        }
        executorService.submit(loader);

        return null;
    }

    /**
     * This represents a request to async load a file from a remote source.
     */
    private record ASyncLoad(String name, File localFile, Consumer<File> callback)
        implements Runnable {

        @Override
        public void run() {
            File result = null;

            // Look remotely, this will block
            if (loadFileRemotely(localFile, name)) {
                result = localFile;
            }

            if (callback != null) {
                callback.accept(result);
            }
        }

    }

    /**
     * Attempt to load file contents from the remote repository. Assumption is the local copy does not exist.
     * This is a blocking operation where the method wait for the remote response
     * @param localFile The destination for the copy
     * @param name Name of the file resource to load.
     */
    private static boolean loadFileRemotely(File localFile, String name) {
        var source = new StringBuilder("remote as ");
        var resourceStream = locateResource(name, source, n -> {
            try {
                return openRemoteContent(n);
            } catch (URISyntaxException e) {
                logger.warning("Problem opening remote content: " + e);
                return null;
            }
        });
            
        // Have a source location
        if (resourceStream != null) {
            logger.info(name + ": " + source.toString());
            createLocalCopy(resourceStream, localFile);
            return true;
        }
        return false;
    }

    /**
     * Attempt to load file contents from a local resources. Local could be out of the classpath
     * or a local copy already exists.
     * @param localFile The destination for the local copy
     * @param name Name of the file resoruce to load.
     */
    private static boolean loadFileLocally(File localFile, String name) {
        boolean locateFile = !localFile.exists();
        
        // Check file is not zero size
        if (!locateFile) {
            if (localFile.length() > 0) {
                // Already on the local store and ready to go
                return true;
            }

            if (localFile.delete()) {
                logger.warning("Local file " + localFile.getAbsolutePath() + " is empty. Removing.");
            }
            else {
                logger.warning("Local file " + localFile.getAbsolutePath() + " is empty but remove failed.");
            }
            locateFile = true;
        }

        // Find the file
        if (locateFile) {
            File folder = localFile.getParentFile();
            folder.mkdirs();

            // Select the location of the file
            // Attempt to find the file in the bundled resources; then remotely
            StringBuilder source = new StringBuilder("bundled as ");
            InputStream resourceStream = locateResource(name, source,
            		FileLocator.class::getResourceAsStream);
            if (resourceStream != null) {
                logger.info(name + ": " + source.toString());
                createLocalCopy(resourceStream, localFile);
                return true;
            }
        }
        return false;
    }

    /**
     * Locates the resource using different file formats. Once found a stream is returned
     * to the requested file contents. This caters for flat, ZIP & XZ files.
     * 
     * @param name Name of resource to find
     * @param source String builder holdign the source descriptiom
     * @param resolver The Function to create an InputStream for a resource name
     * @return
     */
    private static InputStream locateResource(String name, StringBuilder source,
                                                Function<String, InputStream> resolver) {
        InputStream result = resolver.apply(name);
        if (result != null) {
            source.append("file");
            return result;
        }

        // Try ZIP Format
        try {
            InputStream zipresult = resolver.apply(name + ZIP);
            if (zipresult != null) {
                result = getFileFromZip(zipresult, name);
                source.append("zip");
                return result;
            }
        }
        catch (IOException ioe) {
            logger.log(Level.SEVERE, "Problem opening zip file", ioe);
        }

        // Try XZ format
        try {
            InputStream xzresult = resolver.apply(name + XZ);
            if (xzresult != null) {
                result = new XZInputStream(xzresult, BasicArrayCache.getInstance());
                source.append("XZ file");
                return result;
            }
        }
        catch (IOException e) {
            logger.log(Level.SEVERE, "Problem opening XZ file", e);
  
        }
        return null;
    }

    /**
     * Attempts to find a file in the remote content repository.
     * 
     * @param name Name of the file to locate
     * @return
     * @throws URISyntaxException 
     */
    private static InputStream openRemoteContent(String name) throws URISyntaxException {
        // Check remote content
        String fileURL = contentURL + name;
        try {
            return (new URI(fileURL)).toURL().openStream();
        } catch (IOException e) {
            logger.warning("Problem opening stream: " + e);
            return null;
        }    
    }

    /**
     * Picks the required file from a zip file.
     * 
     * @param resourceStream Zip content
     * @param name File to extract
     * @return Stream to the file contents
     * @throws IOException
     */
    private static InputStream getFileFromZip(InputStream resourceStream, String name) throws IOException {
        String [] parts = name.split("/");
        String targetName = parts[parts.length-1];

        ZipInputStream zis = new ZipInputStream(resourceStream);
        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
            long compressedSize = entry.getCompressedSize();
            if (compressedSize == -1) {
                // Handle unknown size or skip the entry
            }
            else if (entry.getName().equals(targetName)) {
                return zis;

            }
        }
        logger.severe("Zip file does not contain file " + targetName);
        return null;
    }
    
    /**
     * Downloads a file from the stream
     * 
     * @param source Source content of the file
     * @param dest Local destination where to create the copy
     */
    private static void createLocalCopy(InputStream source, File dest) {        
        try (BufferedInputStream in = new BufferedInputStream(source);
            FileOutputStream fileOutputStream = new FileOutputStream(dest)) {
            byte[] dataBuffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, dataBuffer.length)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }   
        } catch (IOException ioe) {
            logger.log(Level.SEVERE, "Problem extracting file: ", ioe);
        }
        finally {
            try {
                source.close();
            } catch (IOException e) {
                logger.warning("Problem closing stream: " + e);
            }
        }
    }
}