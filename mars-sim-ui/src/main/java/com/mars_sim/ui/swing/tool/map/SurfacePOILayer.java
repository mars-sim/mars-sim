/*
 * Mars Simulation Project
 * SurfacePOILayer.java
 * @date 2026-02-16
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.tool.map;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;

import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.map.location.IntPoint;
import com.mars_sim.core.map.location.SurfacePOI;
import com.mars_sim.ui.swing.ImageLoader;

/**
 * This is a graphics layer to display a SurfacePOI
 */
public class SurfacePOILayer implements MapLayer {
	/**
	 * This is a hotspot for a navigation point on the route.
	 */
	private class NavpointHotspot extends MapHotspot {

		private SurfacePOI point;

		protected NavpointHotspot(IntPoint center, SurfacePOI navpoint) {
			super(center, 5);
			this.point = navpoint;
		}

		/**
		 * Create a structured text summary for a tooltip of navpoint in context
		 */
		@Override
		public String getTooltipText() {
			return "<html>Nav Point: " + point.getName()
					+ "<br>Coordinates: " + point.getCoordinates().getFormattedString()
					+ "</html>";
		}	
	}

	// Static members
	private static final String WHITE_ICON_NAME = "map/flag_white";

	private Component displayComponent;
	private Icon navpointIconWhite;
	private SurfacePOI selectedNavpoint;

	/**
	 * Constructor
	 * 
	 * @param parent the display component.
	 */
	public SurfacePOILayer(MapPanel parent) {

		// Initialize domain data.
		this.displayComponent = parent;

		navpointIconWhite = ImageLoader.getIconByName(WHITE_ICON_NAME);
	}

	/**
	 * Sets a SurfacePOI to be selected and displayed differently than the others.
	 * 
	 * @param selectedPOI the selected SurfacePOI.
	 */
	public void setSelection(SurfacePOI selectedPOI) {
		this.selectedNavpoint = selectedPOI;
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
		
		List<MapHotspot> results = new ArrayList<>();

		if (selectedNavpoint != null) {
			results.add(displayNavpoint(selectedNavpoint, mapCenter, baseMap, g, d));
		}
	
		return results;
	}

	/**
	 * Displays a navpoint.
	 *
	 * @param navpoint  the navpoint to display.
	 * @param mapCenter the location of the center of the map.
	 * @param baseMap   the type of map.
	 * @param g         graphics context of the map display.
	 * @return 
	 */
	private NavpointHotspot displayNavpoint(SurfacePOI navpoint,
							Coordinates mapCenter, MapDisplay baseMap, Graphics g, Dimension displaySize) {

		if (mapCenter.getAngle(navpoint.getCoordinates()) < baseMap.getHalfAngle()) {
			
			// Chose a navpoint icon based on the map type.
			Icon navIcon = navpointIconWhite;

			// Determine the draw location for the icon.
			IntPoint endLocn = MapUtils.getRectPosition(navpoint.getLocation(), mapCenter, baseMap, displaySize);

			// Draw the navpoint icon.
			IntPoint iconLocation = new IntPoint(endLocn.getX(), 
					(endLocn.getY() - navIcon.getIconHeight()));
			navIcon.paintIcon(displayComponent, g, iconLocation.getX(), iconLocation.getY());

			return new NavpointHotspot(endLocn, navpoint);
		}

		return null;
	}
}