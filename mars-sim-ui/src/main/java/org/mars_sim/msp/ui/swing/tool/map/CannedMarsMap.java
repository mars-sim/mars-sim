/**
 * Mars Simulation Project
 * CannedMarsMap.java
 * @version 3.00 2010-08-10
 * @author Greg Whelan
 */

package org.mars_sim.msp.ui.swing.tool.map;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;

import org.mars_sim.msp.core.*;

/** 
 * The CannedMarsMap class reads in data from files in the map_data
 * jar file in order to generate a map image.
 */
public abstract class CannedMarsMap extends JPanel implements Map {
    
	private static String CLASS_NAME = 
	    "org.mars_sim.msp.ui.standard.tool.map.CannedMarsMap";
	
    	private static Logger logger = Logger.getLogger(CLASS_NAME);
	// Data members
	protected List<int[]> surfaceColors = null;
	private JComponent displayArea = null;
	private Coordinates currentCenter = null;
	private Image mapImage = null;
	private boolean mapImageDone = false;

	
    /**
     * Constructor with surface colors array list parameter.
     * 
     * @param displayArea the display component.
     * @param surfaceColors the ArrayList containing all of the cached map colors.
     */
    public CannedMarsMap(JComponent displayArea, List<int[]> surfaceColors) {
    	this.displayArea = displayArea;
    	this.surfaceColors = surfaceColors;
    }
    

	/**
     * Constructor with data file parameters.
     * 
     * @param displayArea the display component.
     * @param dataFile the map data filename within map_data.jar.
     * @param indexFile the map index filename within map_data.jar.
     */
    public CannedMarsMap(JComponent displayArea, String dataFile, String indexFile, List<int[]> mapColors) {

        this.displayArea = displayArea;

    	    	
        if (mapColors == null) {
        	// Load data files
        	try {
        		int[] index = loadIndexData(indexFile);
        		surfaceColors = loadMapData(dataFile, index);
        	}
        	catch (IOException e) {
        		logger.log(Level.SEVERE,"Could not find map data files.",e);
        	}
		}
        else surfaceColors = mapColors;
    }

	/**
	 * Loads the index data from a file.
	 *
	 * @param file name
	 * @return array of index data
	 * @throws IOException if file cannot be loaded.
	 */
	private int[] loadIndexData(String filename) throws IOException {
    
		// Load index data from map_data jar file.
		ClassLoader loader = getClass().getClassLoader();
		InputStream indexStream = loader.getResourceAsStream(filename);
		if (indexStream == null) throw new IOException("Can not load " + filename);

		// Read stream into an array.
		BufferedInputStream indexBuff = new BufferedInputStream(indexStream);
		DataInputStream indexReader = new DataInputStream(indexBuff);
		int index[] = new int[MAP_HEIGHT];
		for (int x = 0; x < index.length; x++) index[x] = indexReader.readInt();
		indexReader.close();
		indexBuff.close();
       
		return index;
	}
	
	/** 
	 * Loads the map data from a file.
	 *
	 * @param filename the map data file
	 * @param index the index array
	 * @return array list of map data
	 * @throws IOException if map data cannot be loaded.
	 */
	private List<int[]> loadMapData(String filename, int[] index) throws IOException {
     
		// Load map data from map_data jar file.
		ClassLoader loader = getClass().getClassLoader();
		InputStream mapStream = loader.getResourceAsStream(filename);
		if (mapStream == null) throw new IOException("Can not load " + filename);
        
		// Read stream into an array.
		BufferedInputStream mapBuff = new BufferedInputStream(mapStream);
		DataInputStream mapReader = new DataInputStream(mapBuff);
        
		// Create map colors array list.
		List<int[]> mapColors = new ArrayList<int[]>(MAP_HEIGHT);
        
		// Create an array of colors for each pixel in map height.
		for (int x=0; x < MAP_HEIGHT; x++) {
			int[] colors = new int[index[x]];
			for (int y=0; y < colors.length; y++) {
				int red = mapReader.readByte();
				red <<= 16;
				red &= 0x00FF0000;
				int green = mapReader.readByte();
				green <<= 8;
				green &= 0x0000FF00;
				int blue = mapReader.readByte();
				blue &= 0x000000FF;
				int totalColor = 0xFF000000 | red | green | blue;
				colors[y] = (new Color(totalColor)).getRGB();
			}
			mapColors.add(colors);
		}
       
		return mapColors;
	}
	
	/**
	 * Gets an RGB color for a given location on the map.
	 * @param phi the phi value of the location.
	 * @param theta the theta value of the location.
	 * @return the RGB color encoded as an int value. 
	 */
	private int getRGBColor(double phi, double theta) {
       
		// Make sure phi is between 0 and PI.
		while (phi > Math.PI) phi-= Math.PI;
		while (phi < 0) phi+= Math.PI;
        
		// Make sure theta is between 0 and 2 PI.
		while (theta > TWO_PI) theta-= TWO_PI;
		while (theta < 0) theta+= TWO_PI;
        
		int row = (int) Math.round(phi * (MAP_HEIGHT / Math.PI));
		if (row == surfaceColors.size()) row--;
        
		int[] colorRow = surfaceColors.get(row);
		int column = (int) Math.round(theta * ((double) colorRow.length / TWO_PI));
		if (column == colorRow.length) column--;
        
		return colorRow[column];
	}
	
	/**
	 * Creates a map image for a given center location.
	 * @param center the center location of the map display.
	 * @return the map image.
	 */

/**
 * createMapImageLarge creates a set of 9 (3x3) smaller map images by using createMapImage()
 * it copies them into the image that it returns as well. 
 * 
 * **/
	int[] mapArray= new int[9*DISPLAY_WIDTH * DISPLAY_HEIGHT];;
	
	private Image createMapImageLarge(Coordinates center) {
		mapArray= new int[9*DISPLAY_WIDTH * DISPLAY_HEIGHT];
		// Create an array of int RGB color values to create the map image from. 
		System.out.println("createMapImageLarge starting ...");
		int row=0, col=0;
		int xoffset, yoffset;

		for (row = 0;row<3;row++){
			for (col=0;col<3;col++){

			yoffset = row - 1; xoffset = col - 1;
				System.out.println("yoffset = "+yoffset+" xoffset = "+xoffset);	
			System.out.println("center "+center.toString() );
					Coordinates	 tilecenter = 
			new Coordinates(center.convertRectToSpherical(xoffset*285, yoffset*285));
			System.out.println("tilecenter  "+tilecenter.toString());

				//this will put the smaller 300x300 image into the larger 9000x9000 image
				//based on the row,column designation
				this.createMapImage(tilecenter,row,col);
			}
		}
		System.out.println("createmaplarge has finished..");
		return displayArea.createImage(new MemoryImageSource(DISPLAY_WIDTH*3, DISPLAY_HEIGHT*3, mapArray, 0,DISPLAY_WIDTH*3 ));
	}
	
	private void createMapImage(Coordinates center,int row, int col) 
	{
		int newind = 0;
		int rowbase = 270000*row;
		int colbase = col*300;

		// Since the map data is PI offset from the center coords,
		// create a new center coord and offset its theta value.   	

		Coordinates correctedCenter = new Coordinates(center);
				correctedCenter.setTheta(center.getTheta() - Math.PI);
		    	
				// Create an array of int RGB color values to create the map image from. 
				//int[] mapArray = new int[DISPLAY_WIDTH * DISPLAY_HEIGHT];
		        
				// Determine phi iteration angle.
				double phiIterationPadding = 1.26D; //Derived from testing.
				double phiIterationAngle = Math.PI / ((double) MAP_HEIGHT * phiIterationPadding);
		        
				// Determine phi range.
				double phiPadding = 1.46D; // Derived from testing.
				double phiRange = ((double) DISPLAY_HEIGHT / (double) MAP_HEIGHT) * Math.PI * phiPadding;
		        
				// Determine starting and ending phi values.
				double startPhi = correctedCenter.getPhi() - (phiRange / 2D);
				if (startPhi < 0D) startPhi = 0D;
				double endPhi = correctedCenter.getPhi() + (phiRange / 2D);
				if (endPhi > Math.PI) endPhi = Math.PI;
		        
				// Loop through each phi value.
				for (double x = startPhi; x <= endPhi; x+= phiIterationAngle) {
		            
					// Determine theta iteration angle.
					double thetaIterationPadding = 1.46D; // Derived from testing.
					double thetaIterationAngle = TWO_PI / (((double) MAP_WIDTH * Math.sin(x) * thetaIterationPadding) + 1D);
		            
					// Determine theta range.
					double minThetaPadding = 1.02D;  // Derived from testing.
					double minThetaDisplay = TWO_PI * ((double) DISPLAY_WIDTH / (double) MAP_WIDTH) * minThetaPadding;			
					double thetaRange = ((1D - Math.sin(x)) * TWO_PI) + minThetaDisplay;
					double polarCapRange = Math.PI / 6.54D; // Polar cap phi values must display 2 PI theta range. (derived from testing)
					if ((x < polarCapRange) || (x > (Math.PI - polarCapRange))) thetaRange = TWO_PI;
					if (thetaRange > TWO_PI) thetaRange = TWO_PI;
		            
					// Determine the theta starting and ending values.
					double startTheta = correctedCenter.getTheta() - (thetaRange / 2D);
					double endTheta = correctedCenter.getTheta() + (thetaRange / 2D);
		            
					// Loop t hrough each theta value.
					for (double y = startTheta; y <= endTheta; y+= thetaIterationAngle) {
		               
						// Correct y value to make sure it is within bounds. (0 to 2PI)
						double yCorrected = y;
						while (yCorrected < 0) yCorrected+= TWO_PI;
						while (yCorrected > TWO_PI) yCorrected-= TWO_PI;
		               
						// Determine the rectangular offset of the pixel in the image.
						IntPoint location = correctedCenter.findRectPosition(x, yCorrected, 1440D / Math.PI, 720, 720 - 150);
						
						// Determine the display x and y coordinates for the pixel in the image.
						int displayX = DISPLAY_WIDTH - location.getiX();
						int displayY = DISPLAY_HEIGHT - location.getiY();
						
						// Check that the x and y coordinates are within the display area.
						boolean leftBounds = displayX >= 0;
						boolean rightBounds = displayX < DISPLAY_WIDTH;
						boolean topBounds = displayY >= 0;
						boolean bottomBounds = displayY < DISPLAY_HEIGHT;
				if (leftBounds && rightBounds && topBounds && bottomBounds) {
					int index = (DISPLAY_WIDTH - displayX) + ((DISPLAY_HEIGHT - displayY) * DISPLAY_WIDTH);
					newind = rowbase +(DISPLAY_HEIGHT - displayY)*900+colbase+(DISPLAY_WIDTH - displayX);
					if  (newind < 810000) mapArray[newind] = getRGBColor(x, yCorrected);
					
				}
			}
		}
	
	}	// Create new map image.			
	
	
	//original: 
	private Image createMapImageSmall(Coordinates center) {
		int lastdisplayY = 0;
		int lastindex = 0;
		// Since the map data is PI offset from the center coords,
		// create a new center coord and offset its theta value.   	
		Coordinates correctedCenter = new Coordinates(center);
		correctedCenter.setTheta(center.getTheta() - Math.PI);
    	
		// Create an array of int RGB color values to create the map image from. 
		int[] mapArray = new int[DISPLAY_WIDTH * DISPLAY_HEIGHT];
        
		// Determine phi iteration angle.
		double phiIterationPadding = 1.26D; //Derived from testing.
		double phiIterationAngle = Math.PI / ((double) MAP_HEIGHT * phiIterationPadding);
        
		// Determine phi range.
		double phiPadding = 1.46D; // Derived from testing.
		double phiRange = ((double) DISPLAY_HEIGHT / (double) MAP_HEIGHT) * Math.PI * phiPadding;
        
		// Determine starting and ending phi values.
		double startPhi = correctedCenter.getPhi() - (phiRange / 2D);
		if (startPhi < 0D) startPhi = 0D;
		double endPhi = correctedCenter.getPhi() + (phiRange / 2D);
		if (endPhi > Math.PI) endPhi = Math.PI;
        
		// Loop through each phi value.
		for (double x = startPhi; x <= endPhi; x+= phiIterationAngle) {
            
			// Determine theta iteration angle.
			double thetaIterationPadding = 1.46D; // Derived from testing.
			double thetaIterationAngle = TWO_PI / (((double) MAP_WIDTH * Math.sin(x) * thetaIterationPadding) + 1D);
            
			// Determine theta range.
			double minThetaPadding = 1.02D;  // Derived from testing.
			double minThetaDisplay = TWO_PI * ((double) DISPLAY_WIDTH / (double) MAP_WIDTH) * minThetaPadding;			
			double thetaRange = ((1D - Math.sin(x)) * TWO_PI) + minThetaDisplay;
			double polarCapRange = Math.PI / 6.54D; // Polar cap phi values must display 2 PI theta range. (derived from testing)
			if ((x < polarCapRange) || (x > (Math.PI - polarCapRange))) thetaRange = TWO_PI;
			if (thetaRange > TWO_PI) thetaRange = TWO_PI;
            
			// Determine the theta starting and ending values.
			double startTheta = correctedCenter.getTheta() - (thetaRange / 2D);
			double endTheta = correctedCenter.getTheta() + (thetaRange / 2D);
            
			// Loop t hrough each theta value.
			for (double y = startTheta; y <= endTheta; y+= thetaIterationAngle) {
               
				// Correct y value to make sure it is within bounds. (0 to 2PI)
				double yCorrected = y;
				while (yCorrected < 0) yCorrected+= TWO_PI;
				while (yCorrected > TWO_PI) yCorrected-= TWO_PI;
               
				// Determine the rectangular offset of the pixel in the image.
				IntPoint location = correctedCenter.findRectPosition(x, yCorrected, 1440D / Math.PI, 720, 720 - 150);
				
				// Determine the display x and y coordinates for the pixel in the image.
				int displayX = DISPLAY_WIDTH - location.getiX();
				int displayY = DISPLAY_HEIGHT - location.getiY();
				
				// Check that the x and y coordinates are within the display area.
				boolean leftBounds = displayX >= 0;
				boolean rightBounds = displayX < DISPLAY_WIDTH;
				boolean topBounds = displayY >= 0;
				boolean bottomBounds = displayY < DISPLAY_HEIGHT;
				if (leftBounds && rightBounds && topBounds && bottomBounds) {
                	
					// Determine array index for the display location.
					int index = (DISPLAY_WIDTH - displayX) + ((DISPLAY_HEIGHT - displayY) * DISPLAY_WIDTH);

					// Put color in array at index.
					if ((index >= 0) && (index < mapArray.length)) mapArray[index] = getRGBColor(x, yCorrected);
					 lastdisplayY = displayY;
					 lastindex = index;
	
				}
			}
		}
		
		return displayArea.createImage(new MemoryImageSource(DISPLAY_WIDTH, DISPLAY_HEIGHT, mapArray, 0, DISPLAY_WIDTH));
	}
	
	/** 
	 * Creates a 2D map at a given center point.
	 * 
	 * @param newCenter the new center location
	 */
	public void drawMap(Coordinates newCenter) {
		
		if ((newCenter != null) && (!newCenter.equals(currentCenter))) {
			mapImage = createMapImageLarge(newCenter);
			displayArea.repaint();
			MediaTracker mt = new MediaTracker(displayArea);
			mt.addImage(mapImage, 0);
			try {
				mt.waitForID(0);
			} 
			catch (InterruptedException e) {
				logger.log(Level.SEVERE,"MediaTracker interrupted " + e);
			}
			mapImageDone = true;
			currentCenter = new Coordinates(newCenter);
	    
		}
	}
	
	/** 
	 * Checks if a requested map is complete.
	 * 
	 * @return true if requested map is complete
	 */
	public boolean isImageDone() {
		return mapImageDone;
	}
	
	/** 
	 * Gets the constructed map image.
	 * 
	 * @return constructed map image
	 */
	public Image getMapImage() {
		return mapImage;
	}
}


/*
//original: 
	private Image createMapImage(Coordinates center) {
 
		// Since the map data is PI offset from the center coords,
		// create a new center coord and offset its theta value.   	
		Coordinates correctedCenter = new Coordinates(center);
		correctedCenter.setTheta(center.getTheta() - Math.PI);
    	
		// Create an array of int RGB color values to create the map image from. 
		int[] mapArray = new int[DISPLAY_WIDTH * DISPLAY_HEIGHT];
        
		// Determine phi iteration angle.
		double phiIterationPadding = 1.26D; //Derived from testing.
		double phiIterationAngle = Math.PI / ((double) MAP_HEIGHT * phiIterationPadding);
        
		// Determine phi range.
		double phiPadding = 1.46D; // Derived from testing.
		double phiRange = ((double) DISPLAY_HEIGHT / (double) MAP_HEIGHT) * Math.PI * phiPadding;
        
		// Determine starting and ending phi values.
		double startPhi = correctedCenter.getPhi() - (phiRange / 2D);
		if (startPhi < 0D) startPhi = 0D;
		double endPhi = correctedCenter.getPhi() + (phiRange / 2D);
		if (endPhi > Math.PI) endPhi = Math.PI;
        
		// Loop through each phi value.
		for (double x = startPhi; x <= endPhi; x+= phiIterationAngle) {
            
			// Determine theta iteration angle.
			double thetaIterationPadding = 1.46D; // Derived from testing.
			double thetaIterationAngle = TWO_PI / (((double) MAP_WIDTH * Math.sin(x) * thetaIterationPadding) + 1D);
            
			// Determine theta range.
			double minThetaPadding = 1.02D;  // Derived from testing.
			double minThetaDisplay = TWO_PI * ((double) DISPLAY_WIDTH / (double) MAP_WIDTH) * minThetaPadding;			
			double thetaRange = ((1D - Math.sin(x)) * TWO_PI) + minThetaDisplay;
			double polarCapRange = Math.PI / 6.54D; // Polar cap phi values must display 2 PI theta range. (derived from testing)
			if ((x < polarCapRange) || (x > (Math.PI - polarCapRange))) thetaRange = TWO_PI;
			if (thetaRange > TWO_PI) thetaRange = TWO_PI;
            
			// Determine the theta starting and ending values.
			double startTheta = correctedCenter.getTheta() - (thetaRange / 2D);
			double endTheta = correctedCenter.getTheta() + (thetaRange / 2D);
            
			// Loop t hrough each theta value.
			for (double y = startTheta; y <= endTheta; y+= thetaIterationAngle) {
               
				// Correct y value to make sure it is within bounds. (0 to 2PI)
				double yCorrected = y;
				while (yCorrected < 0) yCorrected+= TWO_PI;
				while (yCorrected > TWO_PI) yCorrected-= TWO_PI;
               
				// Determine the rectangular offset of the pixel in the image.
				IntPoint location = correctedCenter.findRectPosition(x, yCorrected, 1440D / Math.PI, 720, 720 - 150);
				
				// Determine the display x and y coordinates for the pixel in the image.
				int displayX = DISPLAY_WIDTH - location.getiX();
				int displayY = DISPLAY_HEIGHT - location.getiY();
				
				// Check that the x and y coordinates are within the display area.
				boolean leftBounds = displayX >= 0;
				boolean rightBounds = displayX < DISPLAY_WIDTH;
				boolean topBounds = displayY >= 0;
				boolean bottomBounds = displayY < DISPLAY_HEIGHT;
				if (leftBounds && rightBounds && topBounds && bottomBounds) {
                	
					// Determine array index for the display location.
					int index = (DISPLAY_WIDTH - displayX) + ((DISPLAY_HEIGHT - displayY) * DISPLAY_WIDTH);
                    
					// Put color in array at index.
					if ((index >= 0) && (index < mapArray.length)) mapArray[index] = getRGBColor(x, yCorrected);
					// if ((index >= 0) && (index < mapArray.length)) mapArray[index] = Color.WHITE.getRGB();
				}
			}
		}
        
		// Create new map image.
		
		return displayArea.createImage(new MemoryImageSource(DISPLAY_WIDTH, DISPLAY_HEIGHT, mapArray, 0, DISPLAY_WIDTH));
	}


 * */





/*					
				 if (!truncline) {
//							if (((pixcount % 300) == 0)&&(pixcount != 0)) {
							if ( ( (pixcount % 300) < 299) && ((pixcount%300) > 0)) {
								//we are not at the very end of or very beginning of a line
								if (displayY < lastdisplayY) { //data source has made a newline
										
									int t = newind;
										while ( (t%300)  <= 299) {
											//pad out the line with a loud color 
											//newind = rowbase+tilerow*900+colbase+(pixcount%300);
											if ((t) < 810000) mapArray[t]= 0x88ffbb;
											t++;
										}
										pixcount = 900;
										tilerow ++;
								} else {
									//data source has not made a new line either
									if (index > lastindex) {pixcount ++;}
								}
							} else
							{
								//we are (maybe) needing to do a new line ourselves 
								if (pixcount == 300) {
									(displayY < lastdisplayY) {} else {
										//the source has not asked for a new line yet. yet we have one scheduled
										//we must truncate --this involves doing nothing until 
										//a future round in which we see displayY < lastdisplayY
										// so we tell ourselves that: 
										truncline = true; 
										pixcount++;
									}
								} else {}
										
										 
								} else 
								{}
							}
				 } else {
					 if (displayY < lastdisplayY) {
						 truncline  = false;
						 if ((pixcount % 300) > 0) {
							 tilerow ++; pixcount = 0;
						 }
					 }; 
				 } 
*/