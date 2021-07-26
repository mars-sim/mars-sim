/**
 * Mars Simulation Project
 * StandardCompressXz.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */

package org.mars_sim.msp.core.tool;

import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.XZOutputStream;

public class StandardCompressXz {

	// StandardCompressXz
	public static void main(String[] args) throws Exception {
		
		LZMA2Options options = new LZMA2Options();
		options.setPreset(7); // play with this number: 6 is default but 7 works better for mid sized archives ( > 8mb)
		
		try (FileInputStream inFile = new FileInputStream(StandardCompressXz.class.getClassLoader().getResource("/map/SurfaceMarsMap.dat").toExternalForm());//"SurfaceMarsMap.dat");
			 FileOutputStream outfile = new FileOutputStream("/map/SurfaceMarsMap.xz");	
			 XZOutputStream out = new XZOutputStream(outfile, options)) {

			byte[] buf = new byte[8192];
			int size;
			while ((size = inFile.read(buf)) != -1)
			   out.write(buf, 0, size);
	
			
			out.finish();
			
	        out.close();
		}
	}
}
