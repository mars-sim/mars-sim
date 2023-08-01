/*
 * Mars Simulation Project
 * NavpointMapLayer.java
 * @date 2023-04-29
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.map;

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

import org.mars.sim.mapdata.MapMetaData;
import org.mars.sim.mapdata.location.Coordinates;
import org.mars.sim.mapdata.location.IntPoint;
import org.mars.sim.mapdata.map.Map;
import org.mars.sim.mapdata.map.MapLayer;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.NavPoint;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.tool.SimulationConstants;
import org.mars_sim.msp.ui.swing.ImageLoader;

/**
 * The NavpointMapLayer is a graphics layer to display mission navpoints.
 */
public class NavpointMapLayer implements MapLayer, SimulationConstants {

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

	/**
	 * Constructor
	 * 
	 * @param displayComponent the display component.
	 */
	public NavpointMapLayer(Component displayComponent) {

		// Initialize domain data.
		this.displayComponent = displayComponent;

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
	public void displayLayer(Coordinates mapCenter, Map baseMap, Graphics g) {
		if (singleMission != null) {
			if (singleMission instanceof VehicleMission)
				displayMission((VehicleMission) singleMission, mapCenter, baseMap, g);
		} else {
			for (Mission mission : missionManager.getMissions()) {
				if (mission instanceof VehicleMission)
					displayMission((VehicleMission) mission, mapCenter, baseMap, g);
			}
		}

		// Make sure selected navpoint is always on top.
		if (selectedNavpoint != null)
			displayNavpoint(selectedNavpoint, mapCenter, baseMap, g);
	}

	/**
	 * Displays the navpoints in a travel mission.
	 * 
	 * @param mission   the travel mission to display.
	 * @param mapCenter the location of the center of the map.
	 * @param baseMap   the type of map.
	 * @param g         graphics context of the map display.
	 */
	private void displayMission(VehicleMission mission, Coordinates mapCenter, Map baseMap, Graphics g) {
		for (NavPoint np : mission.getNavpoints()) {
			displayNavpoint(np, mapCenter, baseMap, g);
		}
	}

	/**
	 * Displays a navpoint.
	 * 
	 * @param navpoint  the navpoint to display.
	 * @param mapCenter the location of the center of the map.
	 * @param baseMap   the type of map.
	 * @param g         graphics context of the map display.
	 */
	private void displayNavpoint(NavPoint navpoint, Coordinates mapCenter, Map baseMap, Graphics g) {

		if (mapCenter != null && mapCenter.getAngle(navpoint.getLocation()) < baseMap.getHalfAngle()) {
			MapMetaData mapType = baseMap.getType();
			
			// Chose a navpoint icon based on the map type.
			Icon navIcon = null;
			if (navpoint == selectedNavpoint)
				navIcon = navpointIconSelected;
			else if (mapType.isColourful())
				navIcon = navpointIconWhite;
			else
				navIcon = navpointIconColor;

			// Determine the draw location for the icon.
			IntPoint location = MapUtils.getRectPosition(navpoint.getLocation(), mapCenter, baseMap);
			IntPoint drawLocation = new IntPoint(location.getiX() + MAP_X_OFFSET, 
					(location.getiY() + MAP_Y_OFFSET - navIcon.getIconHeight()));

			// Draw the navpoint icon.
			navIcon.paintIcon(displayComponent, g, drawLocation.getiX(), drawLocation.getiY());
		}
	}
}
