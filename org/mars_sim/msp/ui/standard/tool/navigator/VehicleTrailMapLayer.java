/**
 * Mars Simulation Project
 * VehicleTrailMapLayer.java
 * @version 2.75 2003-10-13
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.tool.navigator;

import java.awt.*;
import java.util.*;
import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.vehicle.*;

/**
 * The VehicleTrailMapLayer is a graphics layer to display vehicle trails.
 */
class VehicleTrailMapLayer implements MapLayer {
    
    // Domain data
    private UnitManager manager;
    private MapDisplay mapDisplay;

    /**
     * Constructor
     *
     * @param mars the mars instance.
     * @param mapDisplay the mapDisplay to use.
     */
    VehicleTrailMapLayer(Mars mars, MapDisplay mapDisplay) {
        
        manager = mars.getUnitManager();
        this.mapDisplay = mapDisplay;
    }
    
    /**
     * Displays the layer on the map image.
     *
     * @param g graphics context of the map display.
     */
    public void displayLayer(Graphics g) {
        
        // Set trail color
        if (mapDisplay.isTopo()) g.setColor(Color.black);
        else g.setColor(new Color(0, 96, 0));
        
        // Draw trail
        VehicleIterator i = manager.getVehicles().iterator();
        while (i.hasNext()) displayTrail((Vehicle) i.next(), g);
    }
        
    /**
     * Displays the trail behind a vehicle.
     *
     * @param vehicle the vehicle to display.
     * @param g the graphics context.
     */
    private void displayTrail(Vehicle vehicle, Graphics g) {
            
        // Get map angle.
        double angle = 0D;
        if (mapDisplay.isUsgs() && mapDisplay.isSurface()) 
            angle = MapDisplay.HALF_MAP_ANGLE_USGS;
        else angle = MapDisplay.HALF_MAP_ANGLE_STANDARD;
            
        // Draw trail.
        IntPoint oldSpot = null;
        Iterator j = (new ArrayList(vehicle.getTrail())).iterator();
        while (j.hasNext()) {
            Coordinates trailSpot = (Coordinates) j.next();
            if (mapDisplay.getMapCenter().getAngle(trailSpot) < angle) {
                IntPoint spotLocation = mapDisplay.getRectPosition(trailSpot);
                if ((oldSpot == null))                            
                    g.drawRect(spotLocation.getiX(), spotLocation.getiY(), 1, 1);
                else if (!spotLocation.equals(oldSpot))
                    g.drawLine(oldSpot.getiX(), oldSpot.getiY(), spotLocation.getiX(), 
                        spotLocation.getiY());
                oldSpot = spotLocation;
            }
        }
    }
}       
