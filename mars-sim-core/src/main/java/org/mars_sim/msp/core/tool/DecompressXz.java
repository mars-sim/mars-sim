package org.mars_sim.msp.core.tool;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.tukaani.xz.*;

public class DecompressXz {

	// DecompressXz
	public static void main(String[] args) throws Exception {
	    String from = args[0];
	    String to = args[1];
	    try (FileInputStream fileStream = new FileInputStream(from);
	         XZInputStream xzStream = new XZInputStream(fileStream, BasicArrayCache.getInstance())) {

	        Files.copy(xzStream, Paths.get(to), StandardCopyOption.REPLACE_EXISTING);
	    }
	}
}
