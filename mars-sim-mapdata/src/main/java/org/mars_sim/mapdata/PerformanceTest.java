package org.mars_sim.mapdata;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.ImageObserver;
import java.awt.image.PixelGrabber;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class PerformanceTest {

	// Static members.
//	private static final String VOLCANIC_IMG = "TopographyVolcanic.png";
//	private static URL imageMapURL = PerformanceTest.class.getResource("/images/" + VOLCANIC_IMG);//VOLCANIC_IMG);

	private static final String MAP = "geologyMOLA2880x1440.jpg";//"topo2880x1440.jpg";//"Mars2880x1440.jpg";//"Mars-Shaded-names-2k.jpg";//"rgbmars-spec-2k.jpg"; //"MarsNormalMap-2K.png";
	private static URL imageMapURL = PerformanceTest.class.getResource("/maps/" + MAP);//VOLCANIC_IMG);

	private static BufferedImage hugeImage;
	
	public static void main(String[] args) throws IOException {
		hugeImage = ImageIO.read(imageMapURL);
		int type = hugeImage.getType();
		System.out.println("Type : " + type); // TYPE_4BYTE_ABGR : 6  , // TYPE_3BYTE_BGR : 5
	
//		printArray();
		
//		test();
		
		reproduceImage(hugeImage);
	}
	
	
	public static void test() {

		System.out.println("Testing convertTo2DUsingGetRGB:");
		for (int i = 0; i < 10; i++) {
			long startTime = System.nanoTime();
			int[][] result = convertTo2DUsingGetRGB(hugeImage);
			long endTime = System.nanoTime();
			System.out.println(String.format("%-2d: %s", (i + 1), toString(endTime - startTime)));
		}

		System.out.println("");

		System.out.println("Testing FastRGB:");
		for (int i = 0; i < 10; i++) {
			long startTime = System.nanoTime();
			int[][] result = convertTo2DUsingFastRGB(hugeImage);
			long endTime = System.nanoTime();
			System.out.println(String.format("%-2d: %s", (i + 1), toString(endTime - startTime)));
		}
		
		System.out.println("");

		System.out.println("Testing convertTo2DWithoutUsingGetRGB:");
		for (int i = 0; i < 10; i++) {
			long startTime = System.nanoTime();
			int[][] result = convertTo2DWithoutUsingGetRGB(hugeImage);
			long endTime = System.nanoTime();
			System.out.println(String.format("%-2d: %s", (i + 1), toString(endTime - startTime)));
		}
        
		
//		System.out.println("Testing convertMonochromeImageToArray:");
//		for (int i = 0; i < 10; i++) {
//			long startTime = System.nanoTime();
//			convertMonochromeImageToArray(hugeImage);
//			long endTime = System.nanoTime();
//			System.out.println(String.format("%-2d: %s", (i + 1), toString(endTime - startTime)));
//		}
		
	
//		System.out.println("Testing FastRGB:");
//		int x = hugeImage.getWidth();
//		int y = hugeImage.getHeight();
//		for (int i = 0; i < 10; i++) {
//			long startTime = System.nanoTime();
//			new FastRGB(hugeImage).getRGB(x, y);
//			long endTime = System.nanoTime();
//			System.out.println(String.format("%-2d: %s", (i + 1), toString(endTime - startTime)));
//		}
		
//		System.out.println("Testing pixelGrabber:");
//		ImageIcon mapIcon = new ImageIcon(imageMapURL);
//		Image mapImage = mapIcon.getImage();
//		for (int i = 0; i < 10; i++) {
//			long startTime = System.nanoTime();
//			pixelGrabber(mapImage);
//			long endTime = System.nanoTime();
//			System.out.println(String.format("%-2d: %s", (i + 1), toString(endTime - startTime)));
//		}
//		
		
	}

	public static void printGrabberArray() {
		
		ImageIcon mapIcon = new ImageIcon(imageMapURL);
		Image mapImage = mapIcon.getImage();
		int[] array1 = pixelGrabber(mapImage);
		int w = 300;//mapImage.getWidth();
		int fullLength = array1.length;
		for (int i = 0; i < fullLength; i++) {
			if (i % w != 0)
				System.out.print(String.format("%d", array1[i]));
			else
				System.out.println();
		}
	}
	
	public static void printArray() {
//		int[][] array = convertMonochromeImageToArray(hugeImage);
		int[][] array = convertTo2DWithoutUsingGetRGB(hugeImage);
//		int[][] array = convertTo2DUsingGetRGB
		int w = array[0].length;
		int h = array.length;
		System.out.println(String.format("h is %d   w is %d ", h, w));
		
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				System.out.print(String.format("%d", array[y][x]));
			}
			System.out.println();
		}

	}
	
	private static int[][] convertTo2DUsingGetRGB(BufferedImage image) {
		int width = image.getWidth();
		int height = image.getHeight();
		int[][] result = new int[height][width];

		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				result[row][col] = image.getRGB(col, row);
			}
		}

		return result;
	}

	private static int[][] convertTo2DUsingFastRGB(BufferedImage image) {
		int width = image.getWidth();
		int height = image.getHeight();
		int[][] result = new int[height][width];

		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				result[row][col] = FastRGB.getRGB(image, col, row);
			}
		}

		return result;
	}
	
	private static int[][] useMota(BufferedImage image) {

		final byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		final int width = image.getWidth();
		final int height = image.getHeight();
		final boolean hasAlphaChannel = image.getAlphaRaster() != null;

//		System.out.println("hasAlphaChannel : " + hasAlphaChannel);
		
		int[][] result = new int[height][width];
		if (hasAlphaChannel) {
			final int pixelLength = 4;
			for (int pixel = 0, row = 0, col = 0; pixel + 3 < pixels.length; pixel += pixelLength) {
				int argb = 0;
				argb += (((int) pixels[pixel] & 0xff) << 24); // alpha
				argb += ((int) pixels[pixel + 1] & 0xff); // blue
				argb += (((int) pixels[pixel + 2] & 0xff) << 8); // green
				argb += (((int) pixels[pixel + 3] & 0xff) << 16); // red
				
//				The Red and Blue channel comments are flipped. 
//				Red should be +1 and blue should be +3 (or +0 and +2 respectively in the No Alpha code).
				
//				You could also make a final int pixel_offset = hasAlpha?1:0; and 
//				do ((int) pixels[pixel + pixel_offset + 1] & 0xff); // green; 
//				and merge the two loops into one. – Tomáš Zato Mar 23 '15 at 23:02
						
				result[row][col] = argb;
				col++;
				if (col == width) {
					col = 0;
					row++;
				}
			}
		} else {
			final int pixelLength = 3;
			for (int pixel = 0, row = 0, col = 0; pixel + 2 < pixels.length; pixel += pixelLength) {
				int argb = 0;
				argb += -16777216; // 255 alpha
				argb += ((int) pixels[pixel] & 0xff); // blue
				argb += (((int) pixels[pixel + 1] & 0xff) << 8); // green
				argb += (((int) pixels[pixel + 2] & 0xff) << 16); // red
				result[row][col] = argb;
				col++;
				if (col == width) {
					col = 0;
					row++;
				}
			}
		}

		return result;
	}
	
	private static int[][] convertTo2DWithoutUsingGetRGB(BufferedImage image) {

		final byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		final int width = image.getWidth();
		final int height = image.getHeight();
		final boolean hasAlphaChannel = image.getAlphaRaster() != null;

		System.out.println("hasAlphaChannel : " + hasAlphaChannel);
		
		int[][] result = new int[height][width];
		if (hasAlphaChannel) {
			final int pixelLength = 4;
			for (int pixel = 0, row = 0, col = 0; pixel + 3 < pixels.length; pixel += pixelLength) {
				int argb = 0;
				argb += (((int) pixels[pixel] & 0xff) << 24); // alpha
				argb += ((int) pixels[pixel + 1] & 0xff); // blue
				argb += (((int) pixels[pixel + 2] & 0xff) << 8); // green
				argb += (((int) pixels[pixel + 3] & 0xff) << 16); // red
				
//				The Red and Blue channel comments are flipped. 
//				Red should be +1 and blue should be +3 (or +0 and +2 respectively in the No Alpha code).
				
//				You could also make a final int pixel_offset = hasAlpha?1:0; and 
//				do ((int) pixels[pixel + pixel_offset + 1] & 0xff); // green; 
//				and merge the two loops into one. – Tomáš Zato Mar 23 '15 at 23:02
				
				
				result[row][col] = argb;
				col++;
				if (col == width) {
					col = 0;
					row++;
				}
			}
		} else {
			final int pixelLength = 3;
			for (int pixel = 0, row = 0, col = 0; pixel + 2 < pixels.length; pixel += pixelLength) {
				int argb = 0;
				argb += -16777216; // 255 alpha
				argb += ((int) pixels[pixel] & 0xff); // blue
				argb += (((int) pixels[pixel + 1] & 0xff) << 8); // green
				argb += (((int) pixels[pixel + 2] & 0xff) << 16); // red
				result[row][col] = argb;
				col++;
				if (col == width) {
					col = 0;
					row++;
				}
			}
		}

		return result;
	}

	/**
	 * This returns a true bitmap where each element in the grid is either a 0
	 * or a 1. A 1 means the pixel is white and a 0 means the pixel is black.
	 * 
	 * If the incoming image doesn't have any pixels in it then this method
	 * returns null;
	 * 
	 * @param image
	 * @return
	 */
	public static int[][] convertMonochromeImageToArray(BufferedImage monochromeImage)
	{

	    if (monochromeImage == null || monochromeImage.getWidth() == 0 || monochromeImage.getHeight() == 0)
	        return null;

	    // This returns bytes of data starting from the top left of the bitmap
	    // image and goes down.
	    // Top to bottom. Left to right.
	    final byte[] pixels = ((DataBufferByte) monochromeImage.getRaster()
	            .getDataBuffer()).getData();

	    final int width = monochromeImage.getWidth();
	    final int height = monochromeImage.getHeight();
		
	    int[][] result = new int[height][width];

		int w = result[0].length;
		int h = result.length;
		
//		System.out.println(String.format("h is %d   w is %d ", h, w)); // h is 1024   w is 2048 
		
		
	    boolean done = false;
	    boolean alreadyWentToNextByte = false;
	    int byteIndex = 0;
	    int row = 0;
	    int col = 0;
	    int numBits = 0;
	    byte currentByte = pixels[byteIndex];
	    while (!done)
	    {
	        alreadyWentToNextByte = false;

	        result[row][col] = (currentByte & 0x80) >> 7;
	        currentByte = (byte) (((int) currentByte) << 1);
	        numBits++;

	        if ((row == height - 1) && (col == width - 1))
	        {
	            done = true;
	        }
	        else
	        {
	            col++;

	            if (numBits == 8)
	            {
	                currentByte = pixels[++byteIndex];
	                numBits = 0;
	                alreadyWentToNextByte = true;
	            }

	            if (col == width)
	            {
	                row++;
	                col = 0;

	                if (!alreadyWentToNextByte)
	                {
	                    currentByte = pixels[++byteIndex];
	                    numBits = 0;
	                }
	            }
	        }
	    }

	    return result;
	}
	
	public static int[] pixelGrabber(Image image) {
		int W = 300;//image.getWidth();
		int H = 150;//image.getHeight();
		
		int[] mapPixels = new int[W * H];
		PixelGrabber grabber = new PixelGrabber(image, 0, 0, W, H, mapPixels, 0, W);
		try {
			grabber.grabPixels();
		} catch (InterruptedException e) {
			System.out.println("grabber error");
		}
		if ((grabber.status() & ImageObserver.ABORT) != 0)
			System.out.println("grabber error");

//		for (int x = 0; x < H; x++) {
//			for (int y = 0; y < W; y++) {
//				int pixel = mapPixels[(x * W) + y];
//				Color color = new Color(pixel);
//				if (Color.white.equals(color)) {
//					double pixel_offset = (Math.PI / 150D) / 2D;
//					double phi = (((double) x / 150D) * Math.PI) + pixel_offset;
//					double theta = (((double) y / 150D) * Math.PI) + Math.PI + pixel_offset;
//					if (theta > (2D * Math.PI))
//						theta -= (2D * Math.PI);
//				}
//			}
//		}
		
		return mapPixels;
	}
	
	
	public static void reproduceImage(BufferedImage image) {
		int pixels[][] = convertTo2DUsingFastRGB(image);//convertTo2DUsingGetRGB(image);//convertTo2DWithoutUsingGetRGB(image);
		 
		boolean withAlpha = false; // if you need the alpha channel change this to true
		
		System.out.println("withAlpha : " + withAlpha);
		
		String imgFormat = "jpg";  // if you need the alpha channel change this to png
		String imgPath   = "testImage." + imgFormat;
		 
		BufferedImage newImg = getCustomImage(pixels, withAlpha);
		 
		File location = new File(System.getProperty("user.home") + "/.mars-sim/" + imgPath);
		
		try {
			ImageIO.write(newImg, imgFormat,location);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static BufferedImage getCustomImage(int[][] pixels, final boolean withAlpha)
	{
	  // Assuming pixels was taken from convertTo2DWithoutUsingGetRGB
	  // i.e. img.length == pixels.length and img.width == pixels[x].length
//	  BufferedImage img = new BufferedImage(pixels[0].length, pixels.length, withAlpha ? BufferedImage.TYPE_4BYTE_ABGR : BufferedImage.TYPE_3BYTE_BGR);
	  BufferedImage img = new BufferedImage(pixels[0].length, pixels.length, withAlpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_BGR);
		
	  
	  for (int y = 0; y < pixels.length; y++)
	  {
	     for (int x = 0; x < pixels[y].length; x++)
	     {
	        if (withAlpha)
	           img.setRGB(x, y, pixels[y][x]);
	        else {
	           int pixel = pixels[y][x];
//	           int alpha = (pixel >> 24 & 0xff);
	           int red   = (pixel >> 16 & 0xff);
	           int green = (pixel >> 8 & 0xff);
	           int blue  = (pixel & 0xff);
	           int rgb = (red << 16) | (green << 8) | blue;
	           img.setRGB(x, y, rgb);
	        }
	     }
	  }
	  return img;
	}
	
	private static String toString(long nanoSecs) {
		int minutes = (int) (nanoSecs / 60000000000.0);
		int seconds = (int) (nanoSecs / 1000000000.0) - (minutes * 60);
		int millisecs = (int) (((nanoSecs / 1000000000.0) - (seconds + minutes * 60)) * 1000);

		if (minutes == 0 && seconds == 0)
			return millisecs + "ms";
		else if (minutes == 0 && millisecs == 0)
			return seconds + "s";
		else if (seconds == 0 && millisecs == 0)
			return minutes + "min";
		else if (minutes == 0)
			return seconds + "s " + millisecs + "ms";
		else if (seconds == 0)
			return minutes + "min " + millisecs + "ms";
		else if (millisecs == 0)
			return minutes + "min " + seconds + "s";

		return minutes + "min " + seconds + "s " + millisecs + "ms";
	}
	
}