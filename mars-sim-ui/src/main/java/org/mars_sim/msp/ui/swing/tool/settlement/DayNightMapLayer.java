/**
 * Mars Simulation Project
 * DayNightMapLayer.java
 * @version 3.08 2015-06-15
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.swing.tool.settlement;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.image.MemoryImageSource;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;

/**
 * The DayNightMapLayer is a graphics layer to display twilight and night time shading of the settlement
 */
public class DayNightMapLayer implements SettlementMapLayer {

    private static String CLASS_NAME = "org.mars_sim.msp.ui.swing.tool.settlement.ShadingMapLayer";

    private static Logger logger = Logger.getLogger(CLASS_NAME);

    private int[] shadingArray;

    private SurfaceFeatures surfaceFeatures;
	private SettlementMapPanel mapPanel;
	private Coordinates location;

    public DayNightMapLayer(SettlementMapPanel mapPanel) {
        surfaceFeatures = Simulation.instance().getMars().getSurfaceFeatures();

        //System.out.println("DayNightMapLayer's constructor");
		// Initialize data members.
		this.mapPanel = mapPanel;
    }

	@Override
	public void displayLayer(Graphics2D g2d, Settlement settlement,
			Building building, double xPos, double yPos, int mapWidth,
			int mapHeight, double rotation, double scale) {

		//System.out.println("DayNightMapLayer : settlement is " + settlement);
		// TODO: overlay a red/orange mask if having local dust storm

		if (mapPanel.isDaylightTrackingOn()) {
//			int width = SettlementWindow.HORIZONTAL;
//			int height = SettlementWindow.VERTICAL;

			int width = mapWidth;
			int height = mapHeight;

			// Get the map center point.
	        //int centerX =  width / 2;
	        //int centerY =  height / 2;

	        // Coordinates sunDirection = orbitInfo.getSunDirection();
	        //double rho =  (double) height * 3 / Math.PI; // the arc needs to be at least 3 times the height to be rendered realistically
	        // TODO: should take into consideration the direction of the sun when rendering the direction of the "normal" vector of the shadow wavefront

	        //boolean nightTime = true;
	        //boolean dayTime = true;

			// NOTE: whenever the user uses the combobox to switch to another settlement in Settlement Map Tool,
			// the corresponding location instance of the new settlement will be reloaded
			// in order to get the correct day light effect.
	        //if (location == null)
			location = mapPanel.getSettlement().getCoordinates(); // new Coordinates(0D, 0D);
        	//location = settlement.getCoordinates(); // new Coordinates(0D, 0D);

	        // double sunlight = surfaceFeatures.getSurfaceSunlight(location);
	        // normalized to 400 W/m2 instead of 590 W/m2 so as to make the map brighter on screen
	        double sunlight = surfaceFeatures.getSolarIrradiance(location) / SurfaceFeatures.MEAN_SOLAR_IRRADIANCE;
	        //double sunlight = surfaceFeatures.getSurfaceSunlight(location);
        	//System.out.println(" sunlight is " + sunlight);

	        //if (sunlight > 0)
	        //	nightTime = false;
            //if (sunlight < 127)
            //	dayTime = false;

	        if (sunlight <.01D) {
	        	// create a grey mask to cover the settlement map, simulating the darkness of the night
	        	//TODO: during dust storm, use a red/orange mask to cover the map
	            g2d.setColor(new Color(0, 0, 0, 172)); //(0, 0, 0, 196));
	            g2d.fillRect(0, 0, width, height);
	        }

	        else if (sunlight > .90D)
	        	return;

	        else if (sunlight >= 0.01D && sunlight <= .90D) {
	        	shadingArray = new int[width * height];
		        // TODO: how to make the shadingArray sensitive to the zooming in and out

		        for (int x = 0; x < width; x += 2) {

		            for (int y = 0; y < height; y += 2) {
		            	//mapPanel.getSettlement().getCoordinates().convertRectToSpherical(x - centerX, y - centerY, rho, location);
		                //sunlight = surfaceFeatures.getSurfaceSunlight(location);
		                int sunlightInt = (int) (127 * sunlight);
		                int shadeColor = ((127 - sunlightInt) << 24) & 0xFF000000; // 0xFF000000 is the alpha mask

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

		                //if (sunlight > 0) nightTime = false;
		                //if (sunlight < 127) dayTime = false;
		            }

		        }
	        // not working here, why was that?
            //if (sunlight > 0) nightTime = false;
            //if (sunlight < 127) dayTime = false;

	        //if (nightTime) {
	        //    g2d.setColor(new Color(0, 0, 0, 128));
	        //    g2d.fillRect(0, 0, width, height);
	        //}
	        //else if (!dayTime) {
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
	                Thread.sleep(20);
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
		mapPanel = null;
		location = null;
	}
}