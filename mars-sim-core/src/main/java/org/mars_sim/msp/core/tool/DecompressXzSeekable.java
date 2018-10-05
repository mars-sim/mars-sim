package org.mars_sim.msp.core.tool;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.tukaani.xz.*;

public class DecompressXzSeekable {

	// DecompressXzSeekable (partial)
	public static void main(String[] args) throws Exception {
	    String from = args[0];
	    String to = args[1];
	    int offset = Integer.parseInt(args[2]);
	    int size = Integer.parseInt(args[3]);
	    try (SeekableInputStream fileStream = new SeekableFileInputStream(from);
	         SeekableXZInputStream xzStream = new SeekableXZInputStream(fileStream, BasicArrayCache.getInstance())) {

	        xzStream.seek(offset);
	        byte[] buf = new byte[size];
	        if (size != xzStream.read(buf)) {
	            xzStream.available(); // let it throw the last exception, if any
	            throw new IOException("Truncated stream");
	        }
	        Files.write(Paths.get(to), buf);
	    }
	}
}
