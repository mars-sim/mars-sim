/**
 * Mars Simulation Project
 * DayNightMapLayer.java
 * @version 3.08 2015-03-18

 * @author Manny Kung
 */

package org.mars_sim.msp.ui.swing.tool.settlement;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.mars.Mars;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

/**
 * The DayNightMapLayer is a graphics layer to display twilight and night time shading of the settlement
 */
public class DayNightMapLayer implements SettlementMapLayer {

    private static String CLASS_NAME = "org.mars_sim.msp.ui.swing.tool.settlement.ShadingMapLayer";
	
    private static Logger logger = Logger.getLogger(CLASS_NAME);
     
    private int[] shadingArray;
   
    private SurfaceFeatures surfaceFeatures;
	private SettlementMapPanel mapPanel;

    public DayNightMapLayer(SettlementMapPanel mapPanel) {
        Mars mars = Simulation.instance().getMars();
        surfaceFeatures = mars.getSurfaceFeatures();
		// Initialize data members.
		this.mapPanel = mapPanel;
    }

	@Override
	public void displayLayer(Graphics2D g2d, Settlement settlement,
			Building building, double xPos, double yPos, int mapWidth,
			int mapHeight, double rotation, double scale) {


		if (mapPanel.isShowDayNightLayer()) {
			int width = SettlementWindow.HORIZONTAL;
			int height = SettlementWindow.VERTICAL;

			// Get the map center point.
	        int centerX =  width / 2;
	        int centerY =  height / 2;
			
	        shadingArray = new int[width * height];

	        // Coordinates sunDirection = orbitInfo.getSunDirection();
	        double rho =  (double) height * 3 / Math.PI; // the arc needs to be at least 3 times the height to be rendered realistically
	        // TODO: should take into consideration the direction of the sun when rendering the direction of the "normal" vector of the shadow wavefront
	        
	        boolean nightTime = true;
	        boolean dayTime = true;
	        Coordinates location = new Coordinates(0D, 0D);
	        
	        // TODO: make the sahdingArray sensitive to the zooming in and out
        
	        for (int x = 0; x < width; x += 2) {
	        	
	            for (int y = 0; y < height; y += 2) {
	            	
	            	mapPanel.getSettlement().getCoordinates().convertRectToSpherical(x - centerX, y - centerY, rho, location);
	                double sunlight = surfaceFeatures.getSurfaceSunlight(location);
	                int sunlightInt = (int) (127 * sunlight);
	                int shadeColor = ((127 - sunlightInt) << 24) & 0xFF000000;
	               
	                shadingArray[x + (y * width)] = shadeColor;
	                shadingArray[x + 1 + (y * width)] = shadeColor;
	                
	                if (y < height - 1) {
	                    shadingArray[x + ((y + 1) * width)] = shadeColor;
	                    shadingArray[x + 1 + ((y + 1) * width)] = shadeColor;
	                }
	       
	                if (sunlight > 0) nightTime = false;
	                if (sunlight < 127) dayTime = false;
	            }
	        }
	            
	        if (nightTime) {
	            g2d.setColor(new Color(0, 0, 0, 128));
	            g2d.fillRect(0, 0, width, height);
	        }
	        else if (!dayTime) {
	            // Create shading image for map
	            Image shadingMap = mapPanel.createImage(
	            	new MemoryImageSource(width, height, shadingArray, 0, width));
	            // TODO : use BufferedImage instead of MemoryImageSource. which is faster ?            
	            //BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_USHORT_GRAY);

	            MediaTracker mt = new MediaTracker(mapPanel);
	            mt.addImage(shadingMap, 0);
	            
	            //Thread t = new Thread(this);            
	            //t.start();

		        // TODO: what is causing the slowness? the for-loop algorithm ?
	        	
	            try {
	                mt.waitForID(0);
	    	        //System.out.println("mt.waitForID(0)");	     
	                Thread.sleep(40);
	            }
	            catch (InterruptedException e) {
	                logger.log(Level.SEVERE,"ShadingMapLayer interrupted: " + e);
	                mt.removeImage(shadingMap, 0);
	            }
	            if (!mt.isErrorID(0) )
	            	// Draw the shading image
	            	g2d.drawImage(shadingMap, 0, 0, mapPanel);
	            
	        }

		}

	}

	@Override
	public void destroy() {
	    shadingArray = null;
	    surfaceFeatures = null;
		mapPanel= null;
	}
}