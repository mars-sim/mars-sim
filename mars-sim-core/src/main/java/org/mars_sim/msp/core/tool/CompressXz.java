package org.mars_sim.msp.core.tool;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.tukaani.xz.*;

public class CompressXz {

	// CompressXz
	public static void main(String[] args) throws Exception {
	    String from = CompressXz.class.getClassLoader().getResource("/map/SurfaceMarsMap.dat").toExternalForm();
	    String to = CompressXz.class.getClassLoader().getResource("/map/SurfaceMarsMap.xz").toExternalForm();
	    try (FileOutputStream fileStream = new FileOutputStream(to);
	         XZOutputStream xzStream = new XZOutputStream(
	                 fileStream, new LZMA2Options(LZMA2Options.PRESET_MAX), BasicArrayCache.getInstance())) {

	        Files.copy(Paths.get(from), xzStream);
	    }
	}
}
