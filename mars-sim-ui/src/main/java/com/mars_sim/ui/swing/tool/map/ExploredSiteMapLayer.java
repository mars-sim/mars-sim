/*
 * Mars Simulation Project
 * ExploredSiteMapLayer.java
 * @date 2023-06-30
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.tool.map;

import java.awt.Component;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Icon;

import com.mars_sim.core.environment.ExploredLocation;
import com.mars_sim.core.environment.SurfaceFeatures;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.map.location.IntPoint;
import com.mars_sim.ui.swing.ImageLoader;

public class ExploredSiteMapLayer extends SurfaceFeatureLayer<ExploredLocation>
	implements FilteredMapLayer {

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
			String tooltip = "<html>Mineral Certainty: " + site.getAverageCertainty() +"%"
							+ "<br>Explored: " + site.isExplored()
							+ "<br>Minable: " + site.isMinable()
							+ "<br>Reserved: " + site.isReserved();
			var owner = site.getSettlement();
			if (owner != null) {
				tooltip += "<br>Owner: " + owner.getName();
			}

			tooltip += "</html>";
			return tooltip;
		}	
	}

	// Static members
	private static final String SELECTED_ICON_NAME ="map/flag_smallblue";

	public static final String EXPLORED_FILTER = "Explored";
	public static final String CLAIMED_FILTER = "Claimed";
	public static final String RESERVED_FILTER = "Reserved";
	public static final String UNEXPLORED_FILTER = "Unexplored"; 

	private static Map<String,Icon> filterIcons = null;

	// Domain members
	private Component displayComponent;
	private Icon navpointIconSelected;
	private ExploredLocation selectedSite;
	private SurfaceFeatures surfaceFeatures;
	private Set<String> filters = new HashSet<>();

	/**
	 * Constructor.
	 * 
	 * @param displayComponent the display component.
	 */
	public ExploredSiteMapLayer(MapPanel displayComponent) {
		super("Explored Site");

		// Initialize domain data.
		this.displayComponent = displayComponent;
		navpointIconSelected = ImageLoader.getIconByName(SELECTED_ICON_NAME);

		filters.add(CLAIMED_FILTER);
		filters.add(RESERVED_FILTER);

		selectedSite = null;

		surfaceFeatures = displayComponent.getDesktop().getSimulation().getSurfaceFeatures();
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
		Icon siteIcon = null;

		// Work out the precendence of the site to select the icon
		if (site.equals(selectedSite)) {
			siteIcon = navpointIconSelected; 
		}
		else {
			String siteFilter = UNEXPLORED_FILTER;
			if (site.isReserved()) {
				siteFilter = RESERVED_FILTER;
			}
			else if (site.isClaimed()) {
				siteFilter = CLAIMED_FILTER;
			}
			else if (site.isExplored()) {
				siteFilter = EXPLORED_FILTER;
			}

			// Check against fitlers
			if (!filters.contains(siteFilter)) {
				return null;
			}
			siteIcon = getFilterIcons().get(siteFilter);
		}

		// Determine the draw location for the icon.
		IntPoint drawLocation = new IntPoint(location.getiX(), (location.getiY() - siteIcon.getIconHeight()));

		// Draw the navpoint icon.
		siteIcon.paintIcon(displayComponent, g, drawLocation.getiX(), drawLocation.getiY());

		return new SiteHotspot(drawLocation, site);
	}

	public int getIconWidth() {
		return navpointIconSelected.getIconWidth();
	}

	public int getIconHeight() {
		return navpointIconSelected.getIconHeight();
	}

	/**
	 * Get the details of the icons associated to different filters.
	 * @return
	 */
	private static final Map<String,Icon> getFilterIcons() {
		if (filterIcons == null) {
			filterIcons = new HashMap<>();
			filterIcons.put(EXPLORED_FILTER,  ImageLoader.getIconByName("map/flag_smallgreen")); 
			filterIcons.put(CLAIMED_FILTER,  ImageLoader.getIconByName("map/flag_smallgray")); 
			filterIcons.put(RESERVED_FILTER,  ImageLoader.getIconByName("map/flag_smallred")); 
			filterIcons.put(UNEXPLORED_FILTER,  ImageLoader.getIconByName("map/flag_smallblue")); 
		}
		return filterIcons;
	}

	/**
	 * Get a list of the filters the Explored layer supports.
	 * @return List of filters supported.
	 */
	@Override
	public List<MapFilter> getFilterDetails() {
		return getFilterIcons().entrySet().stream()
					.map(e -> new MapFilter(e.getKey(), e.getKey(), e.getValue()))
					.toList();
	}

	/**
	 * Update the state of a filter for explroed sites
	 * @param name Name of the filter
	 * @param display New display state of filter.
	 */
	@Override
	public void displayFilter(String name, boolean display) {
		if (display) {
			filters.add(name);
		}
		else {
			filters.remove(name);
		}
	}

	@Override
	public boolean isFilterActive(String filterName) {
		return filters.contains(filterName);
	}
}
