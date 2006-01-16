/**
 * Mars Simulation Project
 * ShadingMapLayer.java
 * @version 2.76 2004-06-02
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.tool.navigator;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.image.MemoryImageSource;
import org.mars_sim.msp.simulation.Coordinates;
import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.simulation.mars.Mars;
import org.mars_sim.msp.simulation.mars.SurfaceFeatures;

/**
 * The ShadingMapLayer is a graphics layer to display twilight and night time shading.
 */
class ShadingMapLayer implements MapLayer {
    
    // Domain data
    private SurfaceFeatures surfaceFeatures;
    private MapDisplay mapDisplay;
    private int[] shadingArray;

    /**
     * Constructor
     * @param mapDisplay the mapDisplay to use.
     */
    ShadingMapLayer(MapDisplay mapDisplay) {
        
        Mars mars = Simulation.instance().getMars();
        surfaceFeatures = mars.getSurfaceFeatures();
        this.mapDisplay = mapDisplay;
        shadingArray = new int[mapDisplay.getWidth() * mapDisplay.getHeight()];
    }
    
    /**
     * Displays the layer on the map image.
     *
     * @param g graphics context of the map display.
     */
    public void displayLayer(Graphics g) {
        
        int centerX = 150;
        int centerY = 150;
        int width = mapDisplay.getWidth();
        int height = mapDisplay.getHeight();

        // Coordinates sunDirection = orbitInfo.getSunDirection();

        double rho = MapDisplay.NORMAL_PIXEL_RHO;
        if (mapDisplay.isUsgs()) rho = MapDisplay.USGS_PIXEL_RHO;

        boolean nightTime = true;
        boolean dayTime = true;
        Coordinates location = new Coordinates(0D, 0D);
        for (int x = 0; x < width; x+=2) {
            for (int y = 0; y < height; y+=2) {
                mapDisplay.getMapCenter().convertRectToSpherical(x - centerX, y - centerY, rho, location);
                double sunlight = surfaceFeatures.getSurfaceSunlight(location);
                int sunlightInt = (int) (127 * sunlight);
                int shadeColor = ((127 - sunlightInt) << 24) & 0xFF000000;
               
                shadingArray[x + (y * width)] = shadeColor;
                shadingArray[x + 1 + (y * width)] = shadeColor;
                if (y < height -1) {
                    shadingArray[x + ((y + 1) * width)] = shadeColor;
                    shadingArray[x + 1 + ((y + 1) * width)] = shadeColor;
                }
       
                if (sunlight > 0) nightTime = false;
                if (sunlight < 127) dayTime = false;
            }
        }
        
        if (nightTime) {
            g.setColor(new Color(0, 0, 0, 128));
            g.fillRect(0, 0, width, height);
        }
        else if (!dayTime) {
            // Create shading image for map
            Image shadingMap = mapDisplay.createImage(new MemoryImageSource(width, height, shadingArray, 0, width));

            MediaTracker mt = new MediaTracker(mapDisplay);
            mt.addImage(shadingMap, 0);
            try {
                mt.waitForID(0);
            }
            catch (InterruptedException e) {
                System.out.println("ShadingMapLayer interrupted: " + e);
            }

            // Draw the shading image
            g.drawImage(shadingMap, 0, 0, mapDisplay);
        }
    }
}