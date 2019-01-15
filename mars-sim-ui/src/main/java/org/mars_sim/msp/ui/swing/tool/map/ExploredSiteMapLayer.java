/**
 * Mars Simulation Project
 * ExploredSiteMapLayer.java
 * @version 3.1.0 2017-10-05
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.map;

import java.awt.Component;
import java.awt.Graphics;
import javax.swing.Icon;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.IntPoint;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.mars.ExploredLocation;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.ui.swing.ImageLoader;

public class ExploredSiteMapLayer implements MapLayer {

	// Static members
	private static final String EXPLORED_ICON_NAME = Msg.getString("img.smallFlagYellow"); //$NON-NLS-1$
	private static final String MINED_ICON_NAME = Msg.getString("img.smallFlagGray"); //$NON-NLS-1$
	private static final String SELECTED_ICON_NAME = Msg.getString("img.smallFlagBlue"); //$NON-NLS-1$

	// Domain members
	private Component displayComponent;
	private Icon navpointIconExplored;
	private Icon navpointIconMined;
	private Icon navpointIconSelected;
	private boolean displayMined;
	private boolean displayReserved;
	private ExploredLocation selectedSite;

	private double angle = CannedMarsMap.HALF_MAP_ANGLE;

	private SurfaceFeatures surfaceFeatures = Simulation.instance().getMars().getSurfaceFeatures();

	/**
	 * Constructor.
	 * 
	 * @param displayComponent the display component.
	 */
	public ExploredSiteMapLayer(Component displayComponent) {

		// Initialize domain data.
		this.displayComponent = displayComponent;
		navpointIconExplored = ImageLoader.getIcon(EXPLORED_ICON_NAME);
		navpointIconMined = ImageLoader.getIcon(MINED_ICON_NAME);
		navpointIconSelected = ImageLoader.getIcon(SELECTED_ICON_NAME);
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
	 * @param mapType   the type of map.
	 * @param g         graphics context of the map display.
	 */
	public void displayLayer(Coordinates mapCenter, String mapType, Graphics g) {
		// SurfaceFeatures surfaceFeatures =
		// Simulation.instance().getMars().getSurfaceFeatures();
		for (ExploredLocation site : surfaceFeatures.getExploredLocations()) {
			boolean displaySite = true;
			if (site.isReserved() && !displayReserved)
				displaySite = false;
			if (site.isMined() && !displayMined)
				displaySite = false;
			if (!site.isExplored())
				displaySite = false;
			if (displaySite)
				displayExploredSite(site, mapCenter, mapType, g);
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
	private void displayExploredSite(ExploredLocation site, Coordinates mapCenter, String mapType, Graphics g) {

		if (mapCenter.getAngle(site.getLocation()) < angle) {

			// Chose a navpoint icon based on the map type.
			Icon navIcon = null;
			if (site.equals(selectedSite))
				navIcon = navpointIconSelected;
			else if (site.isMined())
				navIcon = navpointIconMined;
			else
				navIcon = navpointIconExplored;

			// Determine the draw location for the icon.
			IntPoint location = MapUtils.getRectPosition(site.getLocation(), mapCenter, mapType);
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