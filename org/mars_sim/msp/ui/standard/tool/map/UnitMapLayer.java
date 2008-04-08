/**
 * Mars Simulation Project
 * UnitMapLayer.java
 * @version 2.80 2006-10-29
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.tool.map;

import java.awt.Graphics;
import java.util.Iterator;

import org.mars_sim.msp.simulation.Coordinates;
import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.simulation.Unit;
import org.mars_sim.msp.ui.standard.unit_display_info.UnitDisplayInfo;
import org.mars_sim.msp.ui.standard.unit_display_info.UnitDisplayInfoFactory;

/**
 * The UnitMapLayer is an abstract graphics layer to display units.
 */
abstract class UnitMapLayer implements MapLayer {
    
    // Domain data
    private boolean blinkFlag;
	
    public UnitMapLayer() {
    	blinkFlag = false;
    }
    
    /**
     * Gets the blink flag.
     * @return blink flag
     */
    protected boolean getBlinkFlag() {
    	return blinkFlag;
    }
    
    /**
     * Displays the layer on the map image.
     * @param mapCenter the location of the center of the map.
     * @param mapType the type of map.
     * @param g graphics context of the map display.
     */
	public void displayLayer(Coordinates mapCenter, String mapType, Graphics g) {
		
		Iterator<Unit> i = Simulation.instance().getUnitManager().getUnits().iterator();
        while (i.hasNext()) {
            Unit unit = i.next();
            UnitDisplayInfo displayInfo = UnitDisplayInfoFactory.getUnitDisplayInfo(unit);
            
            if (displayInfo.isMapDisplayed(unit)) {
                double angle = 0D;
                if (USGSMarsMap.TYPE.equals(mapType)) angle = USGSMarsMap.HALF_MAP_ANGLE;
                else angle = CannedMarsMap.HALF_MAP_ANGLE;
                if (mapCenter.getAngle(unit.getCoordinates()) < angle) 
                    displayUnit(unit, mapCenter, mapType, g);
            }
        }
        
        blinkFlag = !blinkFlag;
	}
	
	/**
	 * Displays a unit on the map.
	 * @param unit the unit to display.
	 * @param mapCenter the location center of the map.
	 * @param mapType the type of map.
	 * @param g the graphics context.
	 */
	protected abstract void displayUnit(Unit unit, Coordinates mapCenter, String mapType, Graphics g);
}