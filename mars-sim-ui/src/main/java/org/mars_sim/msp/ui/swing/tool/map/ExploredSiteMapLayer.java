/*
 * Mars Simulation Project
 * ExploredSiteMapLayer.java
 * @date 2022-07-31
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.map;

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.IntPoint;
import org.mars_sim.msp.core.environment.ExploredLocation;
import org.mars_sim.msp.core.tool.SimulationConstants;
import org.mars_sim.msp.ui.swing.ImageLoader;

public class ExploredSiteMapLayer implements MapLayer, SimulationConstants {

	// Static members
	private static final String EXPLORED_ICON_NAME = "map/flag_smallyellow"; 
	private static final String MINED_ICON_NAME = "map/flag_smallgray"; 
	private static final String SELECTED_ICON_NAME ="map/flag_smallblue"; 

	// Domain members
	private Component displayComponent;
	private Icon navpointIconExplored;
	private Icon navpointIconMined;
	private Icon navpointIconSelected;
	private boolean displayMined;
	private boolean displayReserved;
	private ExploredLocation selectedSite;

	private static final double HALF_MAP_ANGLE = Map.HALF_MAP_ANGLE;

	/**
	 * Constructor.
	 * 
	 * @param displayComponent the display component.
	 */
	public ExploredSiteMapLayer(Component displayComponent) {

		// Initialize domain data.
		this.displayComponent = displayComponent;
		navpointIconExplored = ImageLoader.getIconByName(EXPLORED_ICON_NAME);
		navpointIconMined = ImageLoader.getIconByName(MINED_ICON_NAME);
		navpointIconSelected = ImageLoader.getIconByName(SELECTED_ICON_NAME);
		displayMined = true;
		displayReserved = true;
		selectedSite = null;
	}

	/**
	 * Should mined sites be displayed?
	 * 
	 * @param displayMined true if display mined sites.
	 */
	public void setDisplayMined(boolean displayMined) {
		this.displayMined = displayMined;
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
	public void displayLayer(Coordinates mapCenter, Map baseMap, Graphics g) {
		for (ExploredLocation site : surfaceFeatures.getExploredLocations()) {
			boolean displaySite = !site.isReserved() || displayReserved;
            if (site.isMined() && !displayMined)
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
	private void displayExploredSite(ExploredLocation site, Coordinates mapCenter, Map baseMap, Graphics g) {

		if (mapCenter.getAngle(site.getLocation()) < HALF_MAP_ANGLE) {

			// Chose a navpoint icon based on the map type.
			Icon navIcon = null;
			if (site.equals(selectedSite))
				navIcon = navpointIconSelected;
			else if (site.isMined())
				navIcon = navpointIconMined;
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
