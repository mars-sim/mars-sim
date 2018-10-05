package org.mars_sim.mapdata;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.tukaani.xz.XZInputStream;

public class DecompressXZ {

	// /D:/eclipse/java-photon/eclipse/git/mars-sim/mars-sim-mapdata/target/classes/SurfaceMarsMap.dat
	private static final String surface = ClassLoader.getSystemClassLoader().getResource(".").getPath() + "SurfaceMarsMap.xz";
	
	// /D:/eclipse/java-photon/eclipse/git/mars-sim/mars-sim-mapdata/target/classes/TopoMarsMap.dat
	private static final String topo = ClassLoader.getSystemClassLoader().getResource(".").getPath() + "TopoMarsMap.xz";

//  	static ClassLoader loader = DecompressXZ.class.getClassLoader();

  	public DecompressXZ() {
		try {

//			// Remove the extra "/" slash at index 0
		    String s = surface.substring(1, surface.length());	    
		    decompressMapData(s, true);//s.replace("\\", "/"));
//		    
//			// Remove the extra "/" slash at index 0
		    String t = topo.substring(1, topo.length());	   
		    decompressMapData(t, true);//.replace("\\", "/"));

//		    decompressMapData(surface);
//		    decompressMapData(topo);
		    
		}
		catch (IOException ioe){
		    ioe.printStackTrace();
		} 
  	}
  	
  	public DecompressXZ(String s) {
		try {
		    decompressMapData(s, false);

		}
		catch (IOException ioe){
		    ioe.printStackTrace();
		} 
  	}
  	
  	
    public static void main(String[] args) {
    	new DecompressXZ();
    }
    
  
	private void decompressMapData(String xz, boolean isStandalone) throws IOException {
      
	    String xzFilename = xz;//xz.substring(1, xz.length());
//	    System.out.println("xz Filename : " + xzFilename);
//	    xzFilename.replaceAll("\\", "/");
		
	    String datFilename = xzFilename.replace(".xz", ".dat");
	    
//	    if (isStandalone) {
//		    System.out.println("dat filename : " + datFilename);
//		    System.out.println("xz Filename : " + xzFilename);
//	    }
	    
	    Path path = Paths.get(datFilename);

	    if (isStandalone && Files.exists(path) && Files.isRegularFile(path)){
	    	System.out.println("dat filename already existed.");
	    	return;
	    }

	    else {
	    

//	    if (Files.notExists(path)) {
//	      // file is not exist
//	    }
	    
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
	
	                  InputStream in = null;
	                  
	                  if (isStandalone) {
	                	  in = new FileInputStream(name); //  loader.getResourceAsStream(name); //
	                	  System.out.println("dat filename : " + datFilename);
		        		  System.out.println("xz Filename : " + xzFilename);
	                  }
	                  else {
	                	  System.out.println("xz Filename1 : " + xzFilename);
//	                	  System.out.println("Resource : " + getClass().getResource("/" + name));
	                	  String s = getClass().getResource("/" + name).toExternalForm();// loader.getResource(name).toString();//toExternalForm();
	                	  //s = s.substring(1, s.length());
	                	  System.out.println("xz Filename2 : " + s);
	                	  //s.trim().replaceAll("\\", "/");
	                	  s.trim().replace("file:/", "");
	                	  System.out.println("xz Filename3 : " + s);
	                	  
//	              	    path = Paths.get(s);
//
//	            	    if (Files.exists(path) && Files.isRegularFile(path)){
//	            	    	System.out.println("xz file existed : " + path);
//	            	    	return;
//	            	    }
	            	    
	                	  in = new FileInputStream(s); //loader.getResourceAsStream(name);
	                	  //in = loader.getResourceAsStream(s);
	                  }
	                  
	                  try {
		                      // Since XZInputStream does some buffering internally
		                      // anyway, BufferedInputStream doesn't seem to be
		                      // needed here to improve performance.
		                      // in = new BufferedInputStream(in);
		                      in = new XZInputStream(in);
	
		//                      int size;
		//                      while ((size = in.read(buf)) != -1)
		//                          System.out.write(buf, 0, size);
		                      
		          	        Files.copy(in, Paths.get(datFilename), StandardCopyOption.REPLACE_EXISTING);
		          	        
//		          	        if (!isStandalone) {
//			                	  System.out.println("in  : " + loader.getResourceAsStream(name).toString());
//		                  	}
	
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
	          System.err.println("XZDecDemo: Cannot find the file " + name + ": "
	                             + e.getMessage());
	          e.printStackTrace();
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
	
}
