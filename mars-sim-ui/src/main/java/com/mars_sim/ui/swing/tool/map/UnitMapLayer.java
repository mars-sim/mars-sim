/*
 * Mars Simulation Project
 * UnitMapLayer.java
 * @date 2023-04-29
 * @author Scott Davis
 */

package com.mars_sim.ui.swing.tool.map;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collection;

import com.mars_sim.core.Unit;
import com.mars_sim.core.UnitType;
import com.mars_sim.core.tool.SimulationConstants;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.mapdata.location.Coordinates;
import com.mars_sim.mapdata.map.Map;
import com.mars_sim.mapdata.map.MapLayer;
import com.mars_sim.ui.swing.unit_display_info.UnitDisplayInfo;
import com.mars_sim.ui.swing.unit_display_info.UnitDisplayInfoFactory;

/**
 * The UnitMapLayer is an abstract graphics layer to display units.
 */
abstract class UnitMapLayer implements MapLayer, SimulationConstants {

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
				&& mapCenter != null && mapCenter.getAngle(unit.getCoordinates()) < baseMap.getHalfAngle()) {
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
