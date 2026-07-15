/*
 * Mars Simulation Project
 * UnitMapLayer.java
 * @date 2026-07-14
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.tool.map;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.Icon;

import com.mars_sim.core.Unit;
import com.mars_sim.core.UnitManager;
import com.mars_sim.core.location.LocationStateType;
import com.mars_sim.core.map.MapMetaData;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.map.location.IntPoint;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.displayinfo.EntityDisplayInfo;
import com.mars_sim.ui.swing.displayinfo.EntityDisplayInfoFactory;
import com.mars_sim.ui.swing.displayinfo.MapEntityDisplayInfo;

/**
 * The UnitMapLayer is an abstract graphics layer to display units.
 */
public class UnitMapLayer implements FilteredMapLayer {
	
	/**
	 * This represents a clickable hotspot for a Unit on the surface.
	 */
	private class UnitHotspot extends MapHotspot {

		private Unit target;

		protected UnitHotspot(IntPoint center, Unit target) {
			super(center, 5);
			this.target = target;
		}

		/**
		 * Delegates to the desktop to display the unit window.
		 */
		@Override
		public void clicked() {
			displayComponent.getDesktop().showDetails(target);
		}
	}

	private static final int LABEL_HORIZONTAL_OFFSET = 2;
	
	private static final String LABEL_SETTLEMENTS = "Settlement(s)";
	private static final String LABEL_VEHICLES = "Vehicle(s)";
	private static final String LAYER_NAME = "Units";

	// Domain data
	private boolean blinkFlag = false;
	private boolean displayLabel = true;
	private long blinkTime = 0L;
	
	private Icon labelIcon = ImageLoader.getIconByName("map/text_small");

	private UnitManager unitManager;
	private MapPanel displayComponent;

	private Collection<Settlement> unitsToDisplay;
	private Set<String> displayedFilters = new HashSet<>();

	/**
	 * Constructor.
	 * 
	 * @param panel
	 */
	public UnitMapLayer(MapPanel panel) {
		unitManager = panel.getDesktop().getSimulation().getUnitManager();
		displayComponent = panel;
		
		displayedFilters.add(LABEL_SETTLEMENTS);
		displayedFilters.add(LABEL_VEHICLES);
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
		List<Vehicle> vehicles = new ArrayList<>();
				
		if (settlements == null) {
			settlements = unitManager.getSettlements();
			for (Settlement s: settlements) {
				for (Vehicle v: s.getMissionVehicles()) {
					vehicles.add(v);
				}
			}
		}

		// Display Settlements first
		settlements.forEach(s -> {
			if (isFilterActive(LABEL_SETTLEMENTS)) {
				renderUnit(s, s.getCoordinates(), mapCenter, baseMap, g2d, d, hotspots);
			}
		});

		if (vehicles != null) {
			for (Vehicle v : vehicles) {
				if (v.isOutsideOnMarsMission() 
						&& v.getLocationStateType() == LocationStateType.MARS_SURFACE) {
					// Check against filters
					if (isFilterActive(LABEL_VEHICLES)) {
						renderUnit(v, v.getCoordinates(), mapCenter, baseMap, g2d, d, hotspots);
					}
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
	 * Renders a unit in the layer if it is within the range` of the map center.
	 * 
	 * @param unit
	 * @param unitPosn
	 * @param mapCenter
	 * @param baseMap
	 * @param g
	 * @param hotspots 
	 */
	private void renderUnit(Unit unit, Coordinates unitPosn, Coordinates mapCenter, MapDisplay baseMap, Graphics2D g,
							Dimension displaySize, List<MapHotspot> hotspots) {
		
		EntityDisplayInfo i = EntityDisplayInfoFactory.getDisplayInfo(unit);
		if (i instanceof MapEntityDisplayInfo mui && mui.isMapDisplayed(unit)
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
	 * @param location  Location on the map of this unit
	 * @param baseMap   the type of map.
	 * @param g         the graphics context.
	 */
	private MapHotspot displayUnit(Unit unit, MapEntityDisplayInfo displayInfo, IntPoint location,
							MapDisplay baseMap, Graphics2D g) {

		if (isFilterActive(LABEL_SETTLEMENTS) && unit instanceof Settlement
			|| isFilterActive(LABEL_VEHICLES) && unit instanceof Vehicle) {
			
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
		}
		
		return null;
	}

	@Override
	public List<MapFilter> getFilterDetails() {
		List<MapFilter> filters = new ArrayList<>();
//		if (isFilterActive(LABEL_SETTLEMENTS)) {
			filters.add(new MapFilter(LABEL_SETTLEMENTS, LABEL_SETTLEMENTS, labelIcon));
//		}
//		if (isFilterActive(LABEL_VEHICLES)) {
			filters.add(new MapFilter(LABEL_VEHICLES, LABEL_VEHICLES, labelIcon));
//		}
		return filters;
	}

	@Override
	public void displayFilter(String name, boolean display) {
		if (display) {
			displayedFilters.add(name);
			displayLabel = display;
		}
		else {
			displayedFilters.remove(name);
		}
	}

	@Override
	public boolean isFilterActive(String filterName) {
		return displayedFilters.contains(filterName);
	}
}
