package org.mars_sim.mapdata;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import me.lemire.integercompression.differential.IntegratedBinaryPacking;
import me.lemire.integercompression.differential.IntegratedIntCompressor;
import me.lemire.integercompression.differential.IntegratedVariableByte;
import me.lemire.integercompression.differential.SkippableIntegratedComposition;


public class CompressBitmap {

	private static final String FILE = "megt90n000cb.img";
	
    static IntegratedIntCompressor iic = new IntegratedIntCompressor(
            new SkippableIntegratedComposition(
                new IntegratedBinaryPacking(),
                new IntegratedVariableByte()));

	
	public static void main(String[] args) throws IOException {
		new CompressBitmap(FILE);
	}
	
	
//    public static void main(String[] args)  throws IOException {
    public CompressBitmap(String filename) {
    	
//        if(args.length == 0) {
//            System.out.println("usage: please provide the file name of a bitmap binary file.");
//            return;
//        }
    	
        System.out.println("loading file "+filename+" as a bitmap");
        
        int[] data = null;
		try {
			data = fromBitsetFileToArray(filename);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        
        System.out.println("Compressing "+data.length+" integers");
        
        int[] compressed = iic.compress(data);
        int[] recov = iic.uncompress(compressed);
        
        System.out.println("compressed from "+data.length*4/1024+"KB to "+compressed.length*4/1024+"KB");
        System.out.println("ratio: "+Math.round(data.length*1.0/compressed.length));

        if(!Arrays.equals(recov,data)) throw new RuntimeException("bug");

        long bef,aft;
        bef = System.nanoTime();
        recov = iic.uncompress(compressed);
        aft = System.nanoTime();

        System.out.println("decoding speed:"+Math.round(data.length*1000.0/(aft-bef))+" millions of integers per second");


        bef = System.nanoTime();
        compressed = iic.compress(data);
        aft = System.nanoTime();

        System.out.println("encoding speed:"+Math.round(data.length*1000.0/(aft-bef))+" millions of integers per second");
        System.out.println("note: with a bit of effort, speed can be much higher.");
        System.out.println();
        
        try {
			zipStats(filename);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


    }

    public static int[] fromBitsetFileToArray(String filename) throws IOException {
        Path path = Paths.get(filename);
        byte[] data = Files.readAllBytes(path);
        System.out.println("data has " + data.length);
        // we determine cardinality
        int[] answer = new int[data.length/2];//720*1440];
        
        for(int i = 0 ; i < answer.length; ++i) {
			// Combine the 2 bytes into a 16-bit number
        	answer[i] =  (data[2*i] << 8) | (data[2*i+1] & 0xff);
//			if (i % WIDTH == 0) System.out.println();
//			System.out.print(elevation[i] + " " + buffer[0] + " " + buffer[1] + " " + el2[i]);
			i++;
		}
		
//        int card = 0;
//        for(int k = 0 ; k < data.length; ++k) {
//            int bv = data[k] & 0xFF;
//            card += Integer.bitCount(bv);
//        }
//
//        int pos = 0;
//        for(int k = 0 ; k < data.length; ++k) {
//            int bv = data[k] & 0xFF;
//            for(int b = 0 ; b < 8; ++b)
//                if ( ( (bv >> b) & 1 ) == 1) {
//                    answer[pos++] = b + k * 8;
//                }
//        }
        
//        if(pos != card) throw new RuntimeException("bug");
        return answer;
    }

    public static void zipStats(String filename) throws IOException {
        Path path = Paths.get(filename);
        byte[] input = Files.readAllBytes(path);
        
        System.out.println("I will try to compress the original bitmap using zip.");

        long bef = System.nanoTime();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);
        zos.setLevel(9);
        ZipEntry entry = new ZipEntry(filename);
        entry.setSize(input.length);
        zos.putNextEntry(entry);
        zos.write(input);
        zos.closeEntry();
        zos.close();
        
        byte[] result = baos.toByteArray();
        long aft = System.nanoTime();
        
        System.out.println("zip encoding speed:"+input.length*1000.0/(aft-bef)+" million of bytes per second");
        System.out.println("zip compression ratio at best level : "+input.length * 1.0 / result.length);
    }
}