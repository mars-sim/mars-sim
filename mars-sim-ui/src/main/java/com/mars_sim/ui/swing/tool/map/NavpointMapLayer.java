/*
 * Mars Simulation Project
 * NavpointMapLayer.java
 * @date 2023-04-29
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.tool.map;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.Icon;

import com.mars_sim.core.map.MapMetaData;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.map.location.IntPoint;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.MissionManager;
import com.mars_sim.core.person.ai.mission.NavPoint;
import com.mars_sim.core.person.ai.mission.VehicleMission;
import com.mars_sim.ui.swing.ImageLoader;

/**
 * The NavpointMapLayer is a graphics layer to display mission navpoints.
 */
public class NavpointMapLayer implements MapLayer {
	private class NavpointHotspot extends MapHotspot {

		private Mission parent;
		private NavPoint point;

		protected NavpointHotspot(IntPoint center, Mission parent, NavPoint point) {
			super(center, 5);
			this.parent = parent;
			this.point = point;
		}

		/**
		 * Create a structured text summary for a tooltip of the Mission navpoint
		 */
		@Override
		public String getTooltipText() {
			return "<html>Mission: " + parent.getName()
					+ "<br>Nav Point: " + point.getDescription()
					+ "</html>";
		}	
	}

	// Static members
	private static final String BLUE_ICON_NAME = "map/flag_blue";
	private static final String WHITE_ICON_NAME = "map/flag_white";
	private static final String GREEN_ICON_NAME = "map/flag_green";
	
	// Domain members
	private static final int MAP_X_OFFSET = 5;
	private static final int MAP_Y_OFFSET = 5;
	
	private Component displayComponent;
	
	private Icon navpointIconColor;
	private Icon navpointIconWhite;
	private Icon navpointIconSelected;
	
	private Mission singleMission;
	private NavPoint selectedNavpoint;
	private MissionManager missionManager;

	/**
	 * Constructor
	 * 
	 * @param parent the display component.
	 */
	public NavpointMapLayer(MapPanel parent) {

		// Initialize domain data.
		this.displayComponent = parent;

		missionManager = parent.getDesktop().getSimulation().getMissionManager();
		
		navpointIconColor = ImageLoader.getIconByName(BLUE_ICON_NAME);
		navpointIconWhite = ImageLoader.getIconByName(WHITE_ICON_NAME);
		navpointIconSelected = ImageLoader.getIconByName(GREEN_ICON_NAME);
	}

	/**
	 * Sets the single mission to display navpoints for. Set to null to display all
	 * mission navpoints.
	 * 
	 * @param singleMission the mission to display navpoints for.
	 */
	public void setSingleMission(Mission singleMission) {
		this.singleMission = singleMission;
	}

	/**
	 * Sets a navpoint to be selected and displayed differently than the others.
	 * 
	 * @param selectedNavpoint the selected navpoint.
	 */
	public void setSelectedNavpoint(NavPoint selectedNavpoint) {
		this.selectedNavpoint = selectedNavpoint;
	}

	/**
	 * Displays the layer on the map image.
	 * 
	 * @param mapCenter the location of the center of the map.
	 * @param mapType   the type of map.
	 * @param g         graphics context of the map display.
	 */
	@Override
	public List<MapHotspot> displayLayer(Coordinates mapCenter, MapDisplay baseMap, Graphics2D g, Dimension d) {
		List<MapHotspot> results;
		if (singleMission != null) {
			if (singleMission instanceof VehicleMission vm)
				results = displayMission(vm, mapCenter, baseMap, g, d);
			else
				results = Collections.emptyList();
		} else {
			results = new ArrayList<>();
			for (Mission mission : missionManager.getMissions()) {
				if (mission instanceof VehicleMission vm)
					results.addAll(displayMission(vm, mapCenter, baseMap, g, d));
			}
		}

		return results;
	}

	/**
	 * Displays the navpoints in a travel mission.
	 * 
	 * @param mission   the travel mission to display.
	 * @param mapCenter the location of the center of the map.
	 * @param baseMap   the type of map.
	 * @param g         graphics context of the map display.
	 */
	private List<MapHotspot> displayMission(VehicleMission mission, Coordinates mapCenter, MapDisplay baseMap, Graphics g,
			Dimension d) {
		List<MapHotspot> results = new ArrayList<>();
		for (NavPoint np : mission.getNavpoints()) {
			var hotspot = displayNavpoint(mission, np, mapCenter, baseMap, g, d);
			if (hotspot != null) {
				results.add(hotspot);
			}
		}

		return results;
	}

	/**
	 * Displays a navpoint.
	 *
	 * @param mission   The Mission of the nav point
	 * @param navpoint  the navpoint to display.
	 * @param mapCenter the location of the center of the map.
	 * @param baseMap   the type of map.
	 * @param g         graphics context of the map display.
	 * @return 
	 */
	private NavpointHotspot displayNavpoint(Mission mission, NavPoint navpoint, Coordinates mapCenter, MapDisplay baseMap,
					Graphics g, Dimension displaySize) {

		if (mapCenter.getAngle(navpoint.getLocation()) < baseMap.getHalfAngle()) {
			MapMetaData mapType = baseMap.getMapMetaData();
			
			// Chose a navpoint icon based on the map type.
			Icon navIcon = null;
			if (navpoint == selectedNavpoint)
				navIcon = navpointIconSelected;
			else if (mapType.isColourful())
				navIcon = navpointIconWhite;
			else
				navIcon = navpointIconColor;

			// Determine the draw location for the icon.
			IntPoint location = MapUtils.getRectPosition(navpoint.getLocation(), mapCenter, baseMap, displaySize);
			IntPoint drawLocation = new IntPoint(location.getiX() + MAP_X_OFFSET, 
					(location.getiY() + MAP_Y_OFFSET - navIcon.getIconHeight()));

			// Draw the navpoint icon.
			navIcon.paintIcon(displayComponent, g, drawLocation.getiX(), drawLocation.getiY());

			return new NavpointHotspot(location, mission, navpoint);
		}

		return null;
	}
}
