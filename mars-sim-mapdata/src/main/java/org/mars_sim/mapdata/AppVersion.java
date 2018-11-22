package org.mars_sim.mapdata;

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

//	URI uri = ClassLoader.getSystemResource("com/stackoverflow/json").toURI();
//	String mainPath = Paths.get(uri).toString();
//	Path path = Paths.get(mainPath ,"Movie.class");
	
  	static ClassLoader loader = AppVersion.class.getClassLoader();

    //the base folder is ./, the root of the main.properties file  
//    String path = "./main.properties";

    public static void main(String[] args) {
    	
		String version = null;
		
		try{
		    version = getAppVersion();
		     
		    System.out.println("version : " + version);
		    System.out.println("path : " + path);
		    
		    String p = xzFilename.substring(1, xzFilename.length());
		    
		    decompressMapData(p);
		    	
//		     System.out.println("app : " + );
		}
		catch (IOException ioe){
		    ioe.printStackTrace();
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

	    //to load application's properties, we use this class
	    Properties mainProperties = new Properties();

	    FileInputStream file;


	    //load the file handle for main.properties
	    file = new FileInputStream(path);

	    //load all the properties from this file
	    mainProperties.load(file);

	    //we have loaded the properties, so close the file handle
	    file.close();

	    //retrieve the property we are intrested, the app.version
	    versionString = mainProperties.getProperty("app.version");

	    return versionString;
	}
	
  
	private static void decompressMapData(String xzFilename) throws IOException {
      

	    String datFilename = xzFilename.replace(".xz", ".dat");//.replace(":/", "://");
	    System.out.println("dat filename : " + datFilename);
	    System.out.println("xz Filename : " + xzFilename);
	    
//	    String from = xzFilename;
//	    String to = filename;
//	    
//	    int offset = 0;//Integer.parseInt("");
//	    int size = 8192;//Integer.parseInt("");
//	    
//	    try (SeekableInputStream fileStream = new SeekableFileInputStream(from);
//	         SeekableXZInputStream xzStream = new SeekableXZInputStream(fileStream, BasicArrayCache.getInstance())) {
//
//	        xzStream.seek(offset);
//	        byte[] buf1 = new byte[size];
//	        if (size != xzStream.read(buf1)) {
//	            xzStream.available(); // let it throw the last exception, if any
//	            throw new IOException("Truncated stream");
//	        }
//	        Files.write(Paths.get(to), buf1);
//	    }

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

          } else {
              // Read from files given on the command line.
//              for (int i = 0; i < args.length; ++i) {
//                  name = args[i];

                  InputStream in = new FileInputStream(name); //  loader.getResourceAsStream(name);//

                  try {
                      // Since XZInputStream does some buffering internally
                      // anyway, BufferedInputStream doesn't seem to be
                      // needed here to improve performance.
                      // in = new BufferedInputStream(in);
                      in = new XZInputStream(in);

//                      int size;
//                      while ((size = in.read(buf)) != -1)
//                          System.out.write(buf, 0, size);
                      
          	        Files.copy(in, Paths.get(datFilename));//, StandardCopyOption.REPLACE_EXISTING);

                  } catch (NullPointerException e) {
                      System.err.println("XZDecDemo: Cannot open " + name + ": "
                                         + e.getMessage());
                      e.printStackTrace();
                      System.exit(1);

                  } finally {
                      // Close FileInputStream (directly or indirectly
                      // via XZInputStream, it doesn't matter).
                      in.close();
                  }
//              }
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
