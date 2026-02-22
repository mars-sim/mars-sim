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
import java.util.List;
import java.util.Map;

import javax.swing.Icon;

import com.mars_sim.core.map.MapMetaData;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.map.location.IntPoint;
import com.mars_sim.core.map.location.SurfacePOI;
import com.mars_sim.core.person.ai.mission.NavPoint;
import com.mars_sim.ui.swing.ImageLoader;

/**
 * The NavpointMapLayer is a graphics layer to display mission navpoints.
 */
public abstract class NavpointMapLayer implements MapLayer {
	private class NavpointHotspot extends MapHotspot {

		private String context;
		private SurfacePOI point;

		protected NavpointHotspot(IntPoint center, String context, SurfacePOI navpoint) {
			super(center, 5);
			this.context = context;
			this.point = navpoint;
		}

		/**
		 * Create a structured text summary for a tooltip of navpoint in context
		 */
		@Override
		public String getTooltipText() {
			return "<html>" + (context != null ? context + "<br>" : "")
					+ "Nav Point: " + point.getName()
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
	
	private NavPoint selectedNavpoint;

	/**
	 * Constructor
	 * 
	 * @param parent the display component.
	 */
	public NavpointMapLayer(MapPanel parent) {

		// Initialize domain data.
		this.displayComponent = parent;

		
		navpointIconColor = ImageLoader.getIconByName(BLUE_ICON_NAME);
		navpointIconWhite = ImageLoader.getIconByName(WHITE_ICON_NAME);
		navpointIconSelected = ImageLoader.getIconByName(GREEN_ICON_NAME);
	}


	/**
	 * Sets a navpoint to be selected and displayed differently than the others.
	 * 
	 * @param selectedNavpoint the selected navpoint.
	 */
	public void setSelectedNavpoint(NavPoint selectedNavpoint) {
		this.selectedNavpoint = selectedNavpoint;
	}

	protected abstract Map<String,List<? extends SurfacePOI>> getPaths();

	/**
	 * Displays the layer on the map image.
	 * 
	 * @param mapCenter the location of the center of the map.
	 * @param mapType   the type of map.
	 * @param g         graphics context of the map display.
	 */
	@Override
	public List<MapHotspot> displayLayer(Coordinates mapCenter, MapDisplay baseMap, Graphics2D g, Dimension d) {
		List<MapHotspot> results = new ArrayList<>();
		getPaths().entrySet().forEach(entry ->
					results.addAll(displayPath(entry.getValue(), entry.getKey(), mapCenter, baseMap, g, d)));
	
		return results;
	}

	/**
	 * Displays a path of navpoints with a context string for the tooltip.
	 * @param path the list of navpoints to display.
	 * @param context the context string for the tooltip.
	 * @param mapCenter the location of the center of the map.
	 * @param baseMap the type of map.
	 * @param g graphics context of the map display.
	 * @param d Dimension of the map display.
	 * @return List of MapHotspots created for the navpoints.
	 */
	private List<MapHotspot> displayPath(List<? extends SurfacePOI> path, String context, Coordinates mapCenter,
											MapDisplay baseMap, Graphics g, Dimension d) {
		List<MapHotspot> results = new ArrayList<>();
		for (var np : path) {
			var hotspot = displayNavpoint(np, context, mapCenter, baseMap, g, d);
			if (hotspot != null) {
				results.add(hotspot);
			}
		}

		return results;
	}

	/**
	 * Displays a navpoint.
	 *
	 * @param navpoint  the navpoint to display.
	 * @param context   the context for the navpoint; used in hotspot.
	 * @param mapCenter the location of the center of the map.
	 * @param baseMap   the type of map.
	 * @param g         graphics context of the map display.
	 * @return 
	 */
	private NavpointHotspot displayNavpoint(SurfacePOI navpoint, String context, Coordinates mapCenter,
							MapDisplay baseMap, Graphics g, Dimension displaySize) {

		if (mapCenter.getAngle(navpoint.getCoordinates()) < baseMap.getHalfAngle()) {
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

			return new NavpointHotspot(location, context, navpoint);
		}

		return null;
	}
}