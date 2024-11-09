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
import com.mars_sim.core.map.location.IntPoint;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.ui.swing.unit_display_info.UnitDisplayInfo;
import com.mars_sim.ui.swing.unit_display_info.UnitDisplayInfoFactory;

/**
 * The UnitMapLayer is an abstract graphics layer to display units.
 */
abstract class UnitMapLayer implements MapLayer {

	// Domain data
	private boolean blinkFlag = false;
	private long blinkTime = 0L;
	private Collection<Settlement> unitsToDisplay;
	private UnitManager unitManager;

	protected UnitMapLayer(MapPanel panel) {
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
		settlements.forEach(s -> renderUnit(s, s.getCoordinates(), mapCenter, baseMap, g2d, hotspots));

		if (vehicles != null) {
			for(var v : vehicles) {
				if (v.isOutsideOnMarsMission()) {
					renderUnit(v, v.getCoordinates(), mapCenter, baseMap, g2d, hotspots);
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
	 * @param unitPosn
	 * @param mapCenter
	 * @param baseMap
	 * @param g
	 * @param hotspots 
	 */
	private void renderUnit(Unit unit, Coordinates unitPosn, Coordinates mapCenter, MapDisplay baseMap, Graphics2D g,
				List<MapHotspot> hotspots) {
		
		UnitDisplayInfo i = UnitDisplayInfoFactory.getUnitDisplayInfo(unit);
		if (i != null && i.isMapDisplayed(unit)
			&& mapCenter != null && mapCenter.getAngle(unitPosn) < baseMap.getHalfAngle()) {
				
				IntPoint locn = MapUtils.getRectPosition(unitPosn, mapCenter, baseMap);
				var hs = displayUnit(unit, i, locn, baseMap, g);
				if (hs != null) {
					hotspots.add(hs);
				}
		}
	}

	/**
	 * Displays a unit on the map.
	 * 
	 * @param unit      the unit to display.
	 * @param info		details how to render unit
	 * @param location  Lociation on the map of this unit
	 * @param baseMap   the type of map.
	 * @param g         the graphics context.
	 * @return Has this unit a hot spot?
	 */
	protected abstract MapHotspot displayUnit(Unit unit, UnitDisplayInfo info, IntPoint location,
												MapDisplay baseMap, Graphics2D g);
}
