/*
 * Mars Simulation Project
 * ExploredSiteMapLayer.java
 * @date 2023-06-30
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.tool.map;

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

import com.mars_sim.core.environment.ExploredLocation;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.map.location.IntPoint;
import com.mars_sim.core.tool.SimulationConstants;
import com.mars_sim.ui.swing.ImageLoader;

public class ExploredSiteMapLayer implements MapLayer, SimulationConstants {

	// Static members
	private static final String EXPLORED_ICON_NAME = "map/flag_smallyellow"; 
	private static final String CLAIMED_ICON_NAME = "map/flag_smallgray"; 
	private static final String SELECTED_ICON_NAME ="map/flag_smallblue"; 

	// Domain members
	private Component displayComponent;
	private Icon navpointIconExplored;
	private Icon navpointIconClaimed;
	private Icon navpointIconSelected;
	private boolean displayClaimed;
	private boolean displayReserved;
	private ExploredLocation selectedSite;

	/**
	 * Constructor.
	 * 
	 * @param displayComponent the display component.
	 */
	public ExploredSiteMapLayer(Component displayComponent) {

		// Initialize domain data.
		this.displayComponent = displayComponent;
		navpointIconExplored = ImageLoader.getIconByName(EXPLORED_ICON_NAME);
		navpointIconClaimed = ImageLoader.getIconByName(CLAIMED_ICON_NAME);
		navpointIconSelected = ImageLoader.getIconByName(SELECTED_ICON_NAME);
		displayClaimed = true;
		displayReserved = true;
		selectedSite = null;
	}

	/**
	 * Should claimed sites be displayed?
	 * 
	 * @param displayClaimed true if display mined sites.
	 */
	public void setDisplayClaimed(boolean displayClaimed) {
		this.displayClaimed = displayClaimed;
	}

	/**
	 * Should reserved sites be displayed?
	 * 
	 * @param displayReserved true if display reserved sites.
	 */
	public void setDisplayReserved(boolean displayReserved) {
		this.displayReserved = displayReserved;
	}

	/**
	 * Sets the selected site.
	 * 
	 * @param selectedSite the selected site.
	 */
	public void setSelectedSite(ExploredLocation selectedSite) {
		this.selectedSite = selectedSite;
	}

	/**
	 * Displays the layer on the map image.
	 * 
	 * @param mapCenter the location of the center of the map.
	 * @param baseMap   the type of map.
	 * @param g         graphics context of the map display.
	 */
	@Override
	public void displayLayer(Coordinates mapCenter, MapDisplay baseMap, Graphics g) {
		for (ExploredLocation site : surfaceFeatures.getAllPossibleRegionOfInterestLocations()) {
			boolean displaySite = !site.isReserved() || displayReserved;
            if (!site.isClaimed() && !displayClaimed)
				displaySite = false;
			if (!site.isExplored())
				displaySite = false;
			if (displaySite)
				displayExploredSite(site, mapCenter, baseMap, g);
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
	private void displayExploredSite(ExploredLocation site, Coordinates mapCenter, MapDisplay baseMap, Graphics g) {

		if (mapCenter.getAngle(site.getLocation()) < baseMap.getHalfAngle()) {

			// Chose a navpoint icon based on the map type.
			Icon navIcon = null;
			if (site.equals(selectedSite))
				navIcon = navpointIconSelected;
			else if (site.isMinable())
				navIcon = navpointIconClaimed;
			else
				navIcon = navpointIconExplored;

			// Determine the draw location for the icon.
			IntPoint location = MapUtils.getRectPosition(site.getLocation(), mapCenter, baseMap);
			IntPoint drawLocation = new IntPoint(location.getiX(), (location.getiY() - navIcon.getIconHeight()));

			// Draw the navpoint icon.
			navIcon.paintIcon(displayComponent, g, drawLocation.getiX(), drawLocation.getiY());
		}
	}

	public int getIconWidth() {
		return navpointIconExplored.getIconWidth();
	}

	public int getIconHeight() {
		return navpointIconExplored.getIconHeight();
	}
}
