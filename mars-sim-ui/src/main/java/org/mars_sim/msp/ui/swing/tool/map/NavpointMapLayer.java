/*
 * Mars Simulation Project
 * NavpointMapLayer.java
 * @date 2022-07-31
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.map;

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.IntPoint;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.NavPoint;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.ui.swing.ImageLoader;

/**
 * The NavpointMapLayer is a graphics layer to display mission navpoints.
 */
public class NavpointMapLayer implements MapLayer {

	// Static members
	private static final String BLUE_ICON_NAME = "FlagBlue";
	private static final String WHITE_ICON_NAME = "FlagWhite";
	private static final String GREEN_ICON_NAME = "FlagGreen";
	
	// Domain members
	private double angle = Map.HALF_MAP_ANGLE;
	private int MAP_X_OFFSET = 5;
	private int MAP_Y_OFFSET = 5;
	
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

		navpointIconColor = ImageLoader.getIcon(BLUE_ICON_NAME);
		navpointIconWhite = ImageLoader.getIcon(WHITE_ICON_NAME);
		navpointIconSelected = ImageLoader.getIcon(GREEN_ICON_NAME);
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
	public void displayLayer(Coordinates mapCenter, String mapType, Graphics g) {
		if (singleMission != null) {
			if (singleMission instanceof VehicleMission)
				displayMission((VehicleMission) singleMission, mapCenter, mapType, g);
		} else {
			for (Mission mission : missionManager.getMissions()) {
				if (mission instanceof VehicleMission)
					displayMission((VehicleMission) mission, mapCenter, mapType, g);
			}
		}

		// Make sure selected navpoint is always on top.
		if (selectedNavpoint != null)
			displayNavpoint(selectedNavpoint, mapCenter, mapType, g);
	}

	/**
	 * Displays the navpoints in a travel mission.
	 * 
	 * @param mission   the travel mission to display.
	 * @param mapCenter the location of the center of the map.
	 * @param mapType   the type of map.
	 * @param g         graphics context of the map display.
	 */
	private void displayMission(VehicleMission mission, Coordinates mapCenter, String mapType, Graphics g) {
		for (int x = 0; x < mission.getNumberOfNavpoints(); x++) {
			displayNavpoint(mission.getNavpoint(x), mapCenter, mapType, g);
		}
	}

	/**
	 * Displays a navpoint.
	 * 
	 * @param navpoint  the navpoint to display.
	 * @param mapCenter the location of the center of the map.
	 * @param mapType   the type of map.
	 * @param g         graphics context of the map display.
	 */
	private void displayNavpoint(NavPoint navpoint, Coordinates mapCenter, String mapType, Graphics g) {
		// double angle = CannedMarsMap.HALF_MAP_ANGLE;

		if (mapCenter != null && mapCenter.getAngle(navpoint.getLocation()) < angle) {

			// Chose a navpoint icon based on the map type.
			Icon navIcon = null;
			if (navpoint == selectedNavpoint)
				navIcon = navpointIconSelected;
			else if (TopoMarsMap.TYPE.equals(mapType) || GeologyMarsMap.TYPE.equals(mapType))
				navIcon = navpointIconWhite;
			else
				navIcon = navpointIconColor;

			// Determine the draw location for the icon.
			IntPoint location = MapUtils.getRectPosition(navpoint.getLocation(), mapCenter, mapType);
			IntPoint drawLocation = new IntPoint(location.getiX()+MAP_X_OFFSET, 
					(location.getiY()+MAP_Y_OFFSET - navIcon.getIconHeight()));

			// Draw the navpoint icon.
			navIcon.paintIcon(displayComponent, g, drawLocation.getiX(), drawLocation.getiY());
		}
	}
}
