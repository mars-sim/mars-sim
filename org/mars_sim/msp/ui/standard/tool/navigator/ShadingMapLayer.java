/**
 * Mars Simulation Project
 * ShadingMapLayer.java
 * @version 2.75 2003-09-21
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.tool.navigator;

import java.awt.*;
import java.awt.image.*;
import org.mars_sim.msp.simulation.*;

/**
 * The ShadingMapLayer is a graphics layer to display twilight and night time shading.
 */
class ShadingMapLayer implements MapLayer {
    
    // Domain data
    private SurfaceFeatures surfaceFeatures;
    private OrbitInfo orbitInfo;
    private MapDisplay mapDisplay;
    private int[] shadingArray;

    /**
     * Constructor
     *
     * @param mars the mars instance.
     * @param mapDisplay the mapDisplay to use.
     */
    ShadingMapLayer(Mars mars, MapDisplay mapDisplay) {
        
        surfaceFeatures = mars.getSurfaceFeatures();
        orbitInfo = mars.getOrbitInfo();
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

        Coordinates sunDirection = orbitInfo.getSunDirection();

        double rho = MapDisplay.NORMAL_PIXEL_RHO;
        if (mapDisplay.isUsgs()) rho = MapDisplay.USGS_PIXEL_RHO;

        boolean nightTime = true;
        boolean dayTime = true;
        Coordinates location = new Coordinates(0D, 0D);
        for (int x = 0; x < width; x+=2) {
            for (int y = 0; y < height; y+=2) {
                mapDisplay.getMapCenter().convertRectToSpherical(x - centerX, y - centerY, rho, location);
                int sunlight = surfaceFeatures.getSurfaceSunlight(location);
                int shadeColor = ((127 - sunlight) << 24) & 0xFF000000;
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
