package org.mars_sim.msp.core.tool;

import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.XZOutputStream;

public class StandardCompressXz {

	// StandardCompressXz
	public static void main(String[] args) throws Exception {
//		InputStream fis;
		//File file = new File(DEFAULT_DIR, TEMP_FILE);
		// This works both within Eclipse project and in runnable JAR
        //InputStream fis = StandardCompressXz.class.getResourceAsStream("SurfaceMarsMap.dat");
		// This works both within Eclipse project and in runnable JAR
        //InputStream fis = this.getClass().getClassLoader().getResourceAsStream("/map/SurfaceMarsMap.dat");
        
        //fis = this.getClass().getClassLoader().getResourceAsStream("examples/resources/verdana.ttf");
        
//        fis = StandardCompressXz.class.getClassLoader().getResourceAsStream("SurfaceMarsMap.dat.7z");
        
		FileInputStream inFile = new FileInputStream(StandardCompressXz.class.getClassLoader().getResource("/map/SurfaceMarsMap.dat").toExternalForm());//"SurfaceMarsMap.dat");
		
		FileOutputStream outfile = new FileOutputStream("/map/SurfaceMarsMap.xz");	
		
		LZMA2Options options = new LZMA2Options();

		options.setPreset(7); // play with this number: 6 is default but 7 works better for mid sized archives ( > 8mb)

		XZOutputStream out = new XZOutputStream(outfile, options);

		byte[] buf = new byte[8192];
		int size;
		while ((size = inFile.read(buf)) != -1)
		   out.write(buf, 0, size);

		out.finish();
	}
}
