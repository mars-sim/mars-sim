/*
 * Mars Simulation Project
 * UnitMapLayer.java
 * @date 2023-04-29
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.tool.map;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.mars_sim.core.Unit;
import com.mars_sim.core.UnitManager;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.ui.swing.unit_display_info.UnitDisplayInfo;
import com.mars_sim.ui.swing.unit_display_info.UnitDisplayInfoFactory;

/**
 * The UnitMapLayer is an abstract graphics layer to display units.
 */
abstract class UnitMapLayer implements MapLayer {

	// Domain data
	private static boolean blinkFlag;
	private static long blinkTime = 0L;
	private Collection<Settlement> unitsToDisplay;
	private UnitManager unitManager;

	protected UnitMapLayer(MapPanel panel) {
		blinkFlag = false;
		unitManager = panel.getDesktop().getSimulation().getUnitManager();
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
	public void setUnitsToDisplay(Collection<Settlement> unitsToDisplay) {
		this.unitsToDisplay = unitsToDisplay;
	}

	/**
	 * Displays the layer on the map image.
	 * 
	 * @param mapCenter the location of the center of the map.
	 * @param baseMap   the type of map.
	 * @param g2d         graphics context of the map display.
	 */
	@Override
	public List<MapHotspot> displayLayer(Coordinates mapCenter, MapDisplay baseMap, Graphics2D g2d) {	
		List<MapHotspot> hotspots = new ArrayList<>();
		
		Collection<Settlement> settlements = unitsToDisplay;
		Collection<Vehicle> vehicles = null;
				
		if (settlements == null) {
			settlements = unitManager.getSettlements();
			vehicles = unitManager.getVehicles();
		}

		// Display Settlements first
		settlements.forEach(s -> renderUnit(s, mapCenter, baseMap, g2d, hotspots));

		if (vehicles != null) {
			for(var v : vehicles) {
				if (v.isOutsideOnMarsMission()) {
					renderUnit(v, mapCenter, baseMap, g2d, hotspots);
				}
			}
		}

		long currentTime = System.currentTimeMillis();
		if ((currentTime - blinkTime) > 1000L) {
			blinkFlag = !blinkFlag;
			blinkTime = currentTime;
		}

		return hotspots;
	}

	/**
	 * Render a unit in the layer if it is within the range` of the map center
	 * @param unit
	 * @param mapCenter
	 * @param baseMap
	 * @param g
	 * @param hotspots 
	 */
	private void renderUnit(Unit unit, Coordinates mapCenter, MapDisplay baseMap, Graphics2D g,
				List<MapHotspot> hotspots) {
		UnitDisplayInfo i = UnitDisplayInfoFactory.getUnitDisplayInfo(unit);
		if (i != null && i.isMapDisplayed(unit)
			&& mapCenter != null && mapCenter.getAngle(unit.getCoordinates()) < baseMap.getHalfAngle()) {
				var hs = displayUnit(unit, mapCenter, baseMap, g);
				if (hs != null) {
					hotspots.add(hs);
				}
		}
	}
	/**
	 * Displays a unit on the map.
	 * 
	 * @param unit      the unit to display.
	 * @param mapCenter the location center of the map.
	 * @param baseMap   the type of map.
	 * @param g         the graphics context.
	 * @return Has this unit a hot spot?
	 */
	protected abstract MapHotspot displayUnit(Unit unit, Coordinates mapCenter, MapDisplay baseMap,
												Graphics2D g);
}
