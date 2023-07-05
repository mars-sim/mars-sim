/*
 * Mars Simulation Project
 * AppVersion.java
 * @date 2021-08-15
 * @author Manny Kung
 */

package org.mars.sim.mapdata;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.Properties;

import org.tukaani.xz.XZInputStream;


public class AppVersion {

	private static final String path = ClassLoader.getSystemClassLoader().getResource(".").getPath() + "/map.properties";
	private static final String xzFilename = ClassLoader.getSystemClassLoader().getResource(".").getPath() + "SurfaceMarsMap.xz";
	
  	static ClassLoader loader = AppVersion.class.getClassLoader();

    // Note: the base folder is ./, the root of the main.properties file  

    public static void main(String[] args) {
    	
		String version = null;
		
		try{
		    version = getAppVersion();
		     
		    System.out.println("version : " + version);
		    System.out.println("path : " + path);
		    
		    String p = xzFilename.substring(1, xzFilename.length());
		    
		    decompressMapData(p);
		    	
		}
		catch (IOException ioe) {
			System.err.println(ioe);
		}
    }
    
	/**
	 * Gets the app.version property value from
	 * the ./map.properties file of the base folder
	 *
	 * @return app.version string
	 * @throws IOException
	 */
	public static String getAppVersion() throws IOException{
		
	    String versionString = null;

	    // Load application's properties, we use this class
	    Properties mainProperties = new Properties();

	    // Load the file handle for main.properties
	    try (FileInputStream file = new FileInputStream(path)) {
		    // Load all the properties from this file
		    mainProperties.load(file);	
		    
		    file.close();
	    }

	    // Retrieve the property we are interested, the app.version
	    versionString = mainProperties.getProperty("app.version");

	    return versionString;
	}
	
  
	private static void decompressMapData(String xzFilename) throws IOException {
      
	    String datFilename = xzFilename.replace(".xz", ".dat");
	    System.out.println("dat filename : " + datFilename);
	    System.out.println("xz Filename : " + xzFilename);
	    
	    // Load map data from map_data jar file.
	    byte[] buf = new byte[8192];
	    String name = xzFilename;

      
      try {
          if (xzFilename.length() == 0) {
              name = "standard input";
              InputStream in = new XZInputStream(System.in);

              int size;
              while ((size = in.read(buf)) != -1)
                  System.out.write(buf, 0, size);
              
              in.close();

          } else {
              // Or try read from files given on the command line. for (int i = 0; i < args.length; ++i) name = args[i];

              InputStream in = new FileInputStream(name); 
              // not loader.getResourceAsStream(name);

              try {
                  // Since XZInputStream does some buffering internally
                  // anyway, BufferedInputStream doesn't seem to be
                  // needed here to improve performance. in = new BufferedInputStream(in);
                  in = new XZInputStream(in);
           
      	        Files.copy(in, Paths.get(datFilename));//, StandardCopyOption.REPLACE_EXISTING);

              } catch (NullPointerException e) {
                  System.err.println("XZDecDemo: Cannot open " + name + ": "
                                     + e.getMessage());
                  System.exit(1);

              } finally {
                  // Close FileInputStream (directly or indirectly via XZInputStream, it doesn't matter).
                  in.close();
              }

          }
          
          
      } catch (FileNotFoundException e) {
          System.err.println("XZDecDemo: Cannot open " + name + ": "
                             + e.getMessage());
          System.exit(1);

      } catch (EOFException e) {
          System.err.println("XZDecDemo: Unexpected end of input on "
                             + name);
          System.exit(1);

      } catch (IOException e) {
          System.err.println("XZDecDemo: Error decompressing from "
                             + name + ": " + e.getMessage());
          System.exit(1);
      }           
      
  }
	
}
