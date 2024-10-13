/*
 * Mars Simulation Project
 * ExploredSiteMapLayer.java
 * @date 2023-06-30
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.tool.map;

import java.awt.Component;
import java.awt.Graphics2D;
import java.util.List;

import javax.swing.Icon;

import com.mars_sim.core.environment.ExploredLocation;
import com.mars_sim.core.environment.SurfaceFeatures;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.map.location.IntPoint;
import com.mars_sim.ui.swing.ImageLoader;

public class ExploredSiteMapLayer extends SurfaceFeatureLayer<ExploredLocation> {

	/**
	 * Map hotspot for any explroed sites visible
	 */
	private class SiteHotspot extends MapHotspot {

		private ExploredLocation site;

		protected SiteHotspot(IntPoint center, ExploredLocation site) {
			super(center, 5);
			this.site = site;
		}

		@Override
		public String getTooltipText() {
			return "Certainty: " + site.getAverageCertainty();
		}	
	}

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
	private SurfaceFeatures surfaceFeatures;

	/**
	 * Constructor.
	 * 
	 * @param displayComponent the display component.
	 */
	public ExploredSiteMapLayer(MapPanel displayComponent) {
		super("Explored Site");

		// Initialize domain data.
		this.displayComponent = displayComponent;
		navpointIconExplored = ImageLoader.getIconByName(EXPLORED_ICON_NAME);
		navpointIconClaimed = ImageLoader.getIconByName(CLAIMED_ICON_NAME);
		navpointIconSelected = ImageLoader.getIconByName(SELECTED_ICON_NAME);
		displayClaimed = true;
		displayReserved = true;
		selectedSite = null;

		surfaceFeatures = displayComponent.getDesktop().getSimulation().getSurfaceFeatures();
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
     * Return a list of features that are within the focus specified
     * @param center Center of the viewpoint
     * @param arcAngle Angle of the viewpoint
     * @return
     */
    protected List<ExploredLocation> getFeatures(Coordinates center, double arcAngle) {
		return surfaceFeatures.getAllPossibleRegionOfInterestLocations();
	}
	
	/**
     * Display a explored site on the map using a Graphic at a particular point.
     * @param site Feature to display
     * @param location Location on the Graphic
     * @param g Graphic for drawing
     * @param isColourful Is the destination a colourful map
	 * @return Return a site hotspot is visible
     */
	@Override
    protected MapHotspot displayFeature(ExploredLocation site, IntPoint location, Graphics2D g, boolean isColourful) {
		// Check layer filters
		boolean displaySite = !site.isReserved() || displayReserved;
		if (!site.isClaimed() && !displayClaimed)
			displaySite = false;
		// Need to add this back in
		// if (!site.isExplored())
		// 	displaySite = false;
		if (!displaySite) {
			return null;
		}

		// Chose a navpoint icon based on the map type.
		Icon navIcon = navpointIconExplored;
		if (site.equals(selectedSite))
			navIcon = navpointIconSelected;
		else if (site.isMinable())
			navIcon = navpointIconClaimed;

		// Determine the draw location for the icon.
		IntPoint drawLocation = new IntPoint(location.getiX(), (location.getiY() - navIcon.getIconHeight()));

		// Draw the navpoint icon.
		navIcon.paintIcon(displayComponent, g, drawLocation.getiX(), drawLocation.getiY());

		return new SiteHotspot(drawLocation, site);
	}

	public int getIconWidth() {
		return navpointIconExplored.getIconWidth();
	}

	public int getIconHeight() {
		return navpointIconExplored.getIconHeight();
	}
}
