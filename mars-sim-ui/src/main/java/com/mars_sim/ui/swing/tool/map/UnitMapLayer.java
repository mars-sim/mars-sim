/*
 * Mars Simulation Project
 * UnitMapLayer.java
 * @date 2023-04-29
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.tool.map;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;

import com.mars_sim.core.Unit;
import com.mars_sim.core.UnitManager;
import com.mars_sim.core.map.MapMetaData;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.map.location.IntPoint;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.unit_display_info.MapUnitDisplayInfo;
import com.mars_sim.ui.swing.unit_display_info.UnitDisplayInfo;
import com.mars_sim.ui.swing.unit_display_info.UnitDisplayInfoFactory;

/**
 * The UnitMapLayer is an abstract graphics layer to display units.
 */
public class UnitMapLayer implements FilteredMapLayer {
	/**
	 * Is a clickable hotspot for a Unit on the surface
	 */
	private class UnitHotspot extends MapHotspot {

		private Unit target;

		protected UnitHotspot(IntPoint center, Unit target) {
			super(center, 5);
			this.target = target;
		}

		/**
		 * Delegate to the desktop to display the unit window
		 */
		@Override
		public void clicked() {
			displayComponent.getDesktop().showDetails(target);
		}
	}

	private static final int LABEL_HORIZONTAL_OFFSET = 2;
	private static final String LABEL_FILTER = "label";

	// Domain data
	private boolean blinkFlag = false;
	private long blinkTime = 0L;
	private Collection<Settlement> unitsToDisplay;
	private UnitManager unitManager;
	private MapPanel displayComponent;
	private boolean displayLabel = true;
	private Icon labelIcon = ImageLoader.getIconByName("map/text_small");

	public UnitMapLayer(MapPanel panel) {
		unitManager = panel.getDesktop().getSimulation().getUnitManager();
		displayComponent = panel;
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
	public List<MapHotspot> displayLayer(Coordinates mapCenter, MapDisplay baseMap, Graphics2D g2d, Dimension d) {	
		List<MapHotspot> hotspots = new ArrayList<>();
		
		Collection<Settlement> settlements = unitsToDisplay;
		Collection<Vehicle> vehicles = null;
				
		if (settlements == null) {
			settlements = unitManager.getSettlements();
			vehicles = unitManager.getVehicles();
		}

		// Display Settlements first
		settlements.forEach(s -> renderUnit(s, s.getCoordinates(), mapCenter, baseMap, g2d, d, hotspots));

		if (vehicles != null) {
			for(var v : vehicles) {
				if (v.isOutsideOnMarsMission()) {
					renderUnit(v, v.getCoordinates(), mapCenter, baseMap, g2d, d, hotspots);
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
							Dimension displaySize, List<MapHotspot> hotspots) {
		
		UnitDisplayInfo i = UnitDisplayInfoFactory.getUnitDisplayInfo(unit);
		if (i instanceof MapUnitDisplayInfo mui && mui.isMapDisplayed(unit)
			&& mapCenter != null && mapCenter.getAngle(unitPosn) < baseMap.getHalfAngle()) {
				
				IntPoint locn = MapUtils.getRectPosition(unitPosn, mapCenter, baseMap, displaySize);
				var hs = displayUnit(unit, mui, locn, baseMap, g);
				hotspots.add(hs);
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
	 */
	private MapHotspot displayUnit(Unit unit, MapUnitDisplayInfo displayInfo, IntPoint location,
							MapDisplay baseMap, Graphics2D g) {


		if (!(displayInfo.isMapBlink(unit) && getBlinkFlag())) {
			MapMetaData mapType = baseMap.getMapMetaData();
			Icon displayIcon = displayInfo.getMapIcon(unit, mapType);	

			int locX = location.getiX() - (displayIcon.getIconWidth() / 2);
			int locY =  location.getiY() - (displayIcon.getIconHeight() / 2);
			displayIcon.paintIcon(displayComponent, g, locX, locY);

			//Draw label
			if (displayLabel) {
				g.setColor(displayInfo.getMapLabelColor(baseMap.getMapMetaData()));
				g.setFont(displayInfo.getMapLabelFont());
				g.drawString(unit.getName(), locX + displayIcon.getIconWidth() + LABEL_HORIZONTAL_OFFSET,
											locY + (displayIcon.getIconHeight()/2));
			}

			return new UnitHotspot(location, unit);
		}

		return null;
	}

	@Override
	public List<MapFilter> getFilterDetails() {
		List<MapFilter> filters = new ArrayList<>();
		filters.add(new MapFilter(LABEL_FILTER, "Labels", labelIcon));
		return filters;
	}

	@Override
	public void displayFilter(String name, boolean display) {
		if (name.equals(LABEL_FILTER)) {
			displayLabel = display;
		}
	}

	@Override
	public boolean isFilterActive(String filterName) {
		return (LABEL_FILTER.equals(filterName) && displayLabel);
	}
}
