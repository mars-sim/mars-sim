/**
 * Mars Simulation Project
 * DayNightMapLayer.java
 * @version 3.1.0 2017-09-01
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.swing.tool.settlement;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;

/**
 * The DayNightMapLayer is a graphics layer to display twilight and night time shading of the settlement
 */
public class DayNightMapLayer implements SettlementMapLayer {

    private static Logger logger = Logger.getLogger(DayNightMapLayer.class.getName());
    private static final int LIGHT_THRESHOLD = 196;
    
	private int heightCache = 0;
	private int widthCache = 0;
    private double sunlightCache = -1;

    private int[] shadingArray;
    private int[] darkArray;
    
	private SettlementMapPanel mapPanel;
	private Coordinates location;
	private BufferedImage shadingImage;
	private BufferedImage darkImage;

    public DayNightMapLayer(SettlementMapPanel mapPanel) {
		// Initialize data members.
		this.mapPanel = mapPanel;
    }

	@Override
	public void displayLayer(Graphics2D g2d, Settlement settlement,
			Building building, double xPos, double yPos, int width,
			int height, double rotation, double scale) {

		// TODO: overlay a red/orange mask if having local dust storm

		if (mapPanel.isDaylightTrackingOn()) {

			// NOTE: whenever the user uses the combobox to switch to another settlement in Settlement Map Tool,
			// the corresponding location instance of the new settlement will be reloaded
			// in order to get the correct day light effect.

			location = settlement.getCoordinates(); // new Coordinates(0D, 0D);

	        // normalized to 590 W/m2
	        double sunlight = surfaceFeatures.getSunlightRatio(location);
            int sunlightInt = (int) (LIGHT_THRESHOLD * sunlight);
 
            if (sunlightCache != sunlight || heightCache != height || widthCache != width) {
                //logger.info("sunlight : " + sunlight + "    sunlightInt : " + sunlightInt + "    sunlightCache : " + sunlightCache);	
            	sunlightCache = sunlight;
       	
            	if (heightCache != height || widthCache != width) {
					widthCache = width ;
					heightCache = height;
		        	shadingArray = new int[width * height];
            	}
   	         	
		        if (sunlight <= .2D) {
		        	// create a grey mask to cover the settlement map, simulating the darkness of the night
		        	//TODO: during dust storm, use a red/orange mask to cover the map
		            //g2d.setColor(new Color(0, 0, 0, LIGHT_THRESHOLD)); //(0, 0, 0, 196));
		            //g2d.fillRect(0, 0, width, height);

		        	if (darkArray == null) {
			            darkArray = new int[width * height];
	
				        for (int x = 0; x < width; x += 2) {
				        	
				            for (int y = 0; y < height; y += 2) {
								
				                int shadeColor = (LIGHT_THRESHOLD << 24) & 0xFF000000; // 0xFF000000 is 255, the alpha mask
	
				                int i = x + (y * width);
				                darkArray[i] = shadeColor;
				                i++;
				                if (i < darkArray.length) {
				                	darkArray[x + 1 + (y * width)] = shadeColor;
				                }
	
				                if (y < height - 1) {
				                    int j = x + ((y + 1) * width);
				                    darkArray[j] = shadeColor;
				                    j++;
				                    if (j < darkArray.length) {
				                    	darkArray[x + 1 + ((y + 1) * width)] = shadeColor;
				                    }
				                }
				            }
				        }
		            }
		            
		            shadingArray = darkArray;
		            
//		            if (darkImage == null) {
//		            	darkImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);//.TYPE_USHORT_GRAY); //
//
//		                int shadeColor = LIGHT_THRESHOLD;//(LIGHT_THRESHOLD << 24) & 0xFF000000; // 0xFF000000 is 255, the alpha mask
//		            	
//				        for (int x = 0; x < width; x++) {
//				        	for (int y = 0; y < height; y++) {
//				        		       		
//				                darkImage.setRGB(x, y, shadeColor);
//				        	}
//				        }
//		            }
//		            
//		            shadingImage = darkImage;
		            
		        }

		        else if (sunlight >= .9)
		        	return;
	
		        else {
		
//			        shadingImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);//.TYPE_USHORT_GRAY); //
//				       
//	                int shadeColor = LIGHT_THRESHOLD - sunlightInt;//((LIGHT_THRESHOLD - sunlightInt) << 24) & 0xFF000000; // 0xFF000000 is 255, the alpha mask
//	            	
//			        for (int x = 0; x < width; x++) {
//			        	for (int y = 0; y < height; y++) {
//
//				            shadingImage.setRGB(x, y, shadeColor);
//			        	}
//			        }
			        

			        for (int x = 0; x < width; x += 2) {
	
			            for (int y = 0; y < height; y += 2) {
	
			                int shadeColor = ((LIGHT_THRESHOLD - sunlightInt) << 24) & 0xFF000000; // 0xFF000000 is the alpha mask
	
			                int index1 = x + (y * width);
			                shadingArray[index1] = shadeColor;
			                index1++;
			                if (index1 < shadingArray.length) {
			                    shadingArray[x + 1 + (y * width)] = shadeColor;
			                }
	
			                if (y < height - 1) {
			                    int index2 = x + ((y + 1) * width);
			                    shadingArray[index2] = shadeColor;
			                    index2++;
			                    if (index2 < shadingArray.length) {
			                        shadingArray[x + 1 + ((y + 1) * width)] = shadeColor;
			                    }
			                }
			            }
			        }
	
		        }
            }
            
            //else 
            //	logger.info("same");
		        
			    	
            Image shadingMap = mapPanel.createImage(
            	new MemoryImageSource(width, height, shadingArray, 0, width));

            MediaTracker mt = new MediaTracker(mapPanel);
            mt.addImage(shadingMap, 0);

            try {
                mt.waitForID(0);
                Thread.sleep(20);
            }
            catch (InterruptedException e) {
                logger.log(Level.SEVERE,"ShadingMapLayer interrupted: " + e);
                mt.removeImage(shadingMap, 0);
            }
            if (!mt.isErrorID(0) )
            	// Draw the shading image
            	g2d.drawImage(shadingMap, 0, 0, mapPanel);
                
	        //g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0));
            //g2d.fillRect(0, 0, width, height);
	        //g2d.drawImage(shadingImage, 0, 0, mapPanel);
		}
	}

	@Override
	public void destroy() {
	    shadingArray = null;
		mapPanel = null;
		location = null;
	}
}