/*
 * Mars Simulation Project
 * UnitMapLayer.java
 * @date 2023-04-29
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.tool.map;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collection;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.swing.unit_display_info.UnitDisplayInfo;
import org.mars_sim.msp.ui.swing.unit_display_info.UnitDisplayInfoFactory;

/**
 * The UnitMapLayer is an abstract graphics layer to display units.
 */
abstract class UnitMapLayer implements MapLayer {

	// Domain data
	private static boolean blinkFlag;
	private static long blinkTime = 0L;
	private Collection<Unit> unitsToDisplay;

	public UnitMapLayer() {
		blinkFlag = false;
	}

	/**
	 * Gets the blink flag.
	 * 
	 * @return blink flag
	 */
	protected boolean getBlinkFlag() {
		return blinkFlag;
	}

	/**
	 * Sets the units to display in this layer.
	 * 
	 * @param unitsToDisplay collection of units to display.
	 */
	public void setUnitsToDisplay(Collection<Unit> unitsToDisplay) {
		this.unitsToDisplay = new ArrayList<Unit>(unitsToDisplay);
	}

	/**
	 * Displays the layer on the map image.
	 * 
	 * @param mapCenter the location of the center of the map.
	 * @param baseMap   the type of map.
	 * @param g         graphics context of the map display.
	 */
	@Override
	public void displayLayer(Coordinates mapCenter, Map baseMap, Graphics g) {		
		Collection<Unit> units = null;
				
		if (unitsToDisplay != null) {
			units = unitsToDisplay;
		} else {
			units = unitManager.getDisplayUnits();
		}

		for (Unit unit : units) {
			if (unit.getUnitType() == UnitType.VEHICLE
				&& !((Vehicle)unit).isOutsideOnMarsMission()) {
					continue;
			}
			
			UnitDisplayInfo i = UnitDisplayInfoFactory.getUnitDisplayInfo(unit);
			if (i != null && i.isMapDisplayed(unit)
				&& mapCenter != null && mapCenter.getAngle(unit.getCoordinates()) < Map.HALF_MAP_ANGLE) {
					displayUnit(unit, mapCenter, baseMap, g);
			}
		}


		long currentTime = System.currentTimeMillis();
		if ((currentTime - blinkTime) > 1000L) {
			blinkFlag = !blinkFlag;
			blinkTime = currentTime;
		}
	}

	/**
	 * Displays a unit on the map.
	 * 
	 * @param unit      the unit to display.
	 * @param mapCenter the location center of the map.
	 * @param baseMap   the type of map.
	 * @param g         the graphics context.
	 */
	protected abstract void displayUnit(Unit unit, Coordinates mapCenter, Map baseMap, Graphics g);
}
