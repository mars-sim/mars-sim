/**
 * Mars Simulation Project
 * StandardCompressXz.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */

package org.mars.sim.mapdata.xz;

import java.io.FileOutputStream;
import java.io.InputStream;

import org.mars.sim.mapdata.common.FileLocator;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.XZOutputStream;

public class StandardCompressXz {

//	public static final String FILE = "Mars_MGS_colorhillshade_mola_2865.jpg";
	public static final String FILE = "megt90n000eb.img";
//	public static final String PATH = "/maps/";
	public static final String PATH = "/elevation/";
	
	public static final String FILE_XZ = FILE + ".xz";
	
	// StandardCompressXz
	public static void main(String[] args) throws Exception {
		
		LZMA2Options options = new LZMA2Options();
		options.setPreset(7); // play with this number: 6 is default but 7 works better for mid sized archives ( > 8mb)
		
		try (InputStream resourceStream = FileLocator.class.getResourceAsStream(PATH + FILE);

			FileOutputStream outfile = new FileOutputStream(FILE_XZ);	
			XZOutputStream out = new XZOutputStream(outfile, options)) {

			byte[] buf = new byte[8192];
			int size;
			while ((size = resourceStream.read(buf)) != -1)
			   out.write(buf, 0, size);
			
			out.finish();
			
	        out.close();
	        
	        outfile.close();
		}
	}
}
