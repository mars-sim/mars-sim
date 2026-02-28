/*
 * Mars Simulation Project
 * RoutePathLayer.java
 * @date 2023-04-29
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.tool.map;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.Icon;

import com.mars_sim.core.map.MapMetaData;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.map.location.IntPoint;
import com.mars_sim.core.map.location.SurfacePOI;
import com.mars_sim.ui.swing.ImageLoader;

/**
 * This is a graphics layer to display a route across a map in terms of a number of SurfacePOIs
 * grouped together into a RoutePath.
 */
public class RoutePathLayer implements MapLayer {
	/**
	 * This is a hotspot for a navigation point on the route.
	 */
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
					+ "<br>Coordinates: " + point.getCoordinates().getFormattedString()
					+ "</html>";
		}	
	}

	// Static members
	private static final String BLUE_ICON_NAME = "map/flag_blue";
	private static final String WHITE_ICON_NAME = "map/flag_white";
	private static final String GREEN_ICON_NAME = "map/flag_green";
	
	private static final float[] DASH_PATTERN = {10.0f, 5.0f};
    private static final BasicStroke DASHED = new BasicStroke(1.0f,
                        BasicStroke.CAP_BUTT,
                        BasicStroke.JOIN_MITER,
                        10.0f, DASH_PATTERN, 0.0f);
	private static Polygon arrowHead;
	
	private Component displayComponent;
	
	private Icon navpointIconColor;
	private Icon navpointIconWhite;
	private Icon navpointIconSelected;
	
	private SurfacePOI selectedNavpoint;
	private List<RoutePath> paths = new CopyOnWriteArrayList<>();

	/**
	 * Constructor
	 * 
	 * @param parent the display component.
	 */
	public RoutePathLayer(MapPanel parent) {

		// Initialize domain data.
		this.displayComponent = parent;

		navpointIconColor = ImageLoader.getIconByName(BLUE_ICON_NAME);
		navpointIconWhite = ImageLoader.getIconByName(WHITE_ICON_NAME);
		navpointIconSelected = ImageLoader.getIconByName(GREEN_ICON_NAME);
	}

	/**
	 * Add a route path to display
	 * @param path
	 */
	public void addPath(RoutePath path) {
		paths.add(path);
	}

	/**
	 * Remove a route path from display
	 * @param path
	 */
	public void removePath(RoutePath path) {
		paths.remove(path);
	}
	
    /**
     * Gets the list of route paths.
     * @return List of route paths.
     */
    public List<RoutePath> getPaths() {
        return paths;
    }

	/**
	 * Sets a navpoint to be selected and displayed differently than the others.
	 * 
	 * @param selectedNavpoint the selected navpoint.
	 */
	public void setSelectedNavpoint(SurfacePOI selectedNavpoint) {
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
		g.setColor((baseMap.getMapMetaData().isColourful() ? Color.BLACK : Color.WHITE));
		g.setStroke(DASHED);
		
		List<MapHotspot> results = new ArrayList<>();
		paths.forEach(entry ->
					results.addAll(displayPath(entry, mapCenter, baseMap, g, d)));
	
		return results;
	}

	/**
	 * Displays a path of navpoints with a context string for the tooltip.
	 * @param path the list of navpoints to display.
	 * @param mapCenter the location of the center of the map.
	 * @param baseMap the type of map.
	 * @param g graphics context of the map display.
	 * @param d Dimension of the map display.
	 * @return List of MapHotspots created for the navpoints.
	 */
	private List<MapHotspot> displayPath(RoutePath path, Coordinates mapCenter,
											MapDisplay baseMap, Graphics g, Dimension d) {
		Coordinates previous = path.getStart();
		List<MapHotspot> results = new ArrayList<>();
		for (var np : path.getNavpoints()) {
			var hotspot = displayNavpoint(np, path.getContext(), previous, mapCenter, baseMap, g, d);
			if (hotspot != null) {
				results.add(hotspot);
			}
			previous = np.getCoordinates();
		}

		return results;
	}

	/**
	 * Displays a navpoint.
	 *
	 * @param navpoint  the navpoint to display.
	 * @param context   the context for the navpoint; used in hotspot.
	 * @param mapCenter the location of the center of the map.
	 * @param previous the location of the previous navpoint; used to determine if this navpoint is within the map angle. 
	 * @param baseMap   the type of map.
	 * @param g         graphics context of the map display.
	 * @return 
	 */
	private NavpointHotspot displayNavpoint(SurfacePOI navpoint, String context, Coordinates previous,
							Coordinates mapCenter, MapDisplay baseMap, Graphics g, Dimension displaySize) {

		if (mapCenter.getAngle(navpoint.getCoordinates()) < baseMap.getHalfAngle()) {
			MapMetaData mapType = baseMap.getMapMetaData();
			
			// Chose a navpoint icon based on the map type.
			Icon navIcon = null;
			if (navpoint.equals(selectedNavpoint))
				navIcon = navpointIconSelected;
			else if (mapType.isColourful())
				navIcon = navpointIconWhite;
			else
				navIcon = navpointIconColor;

			// Determine the draw location for the icon.
			IntPoint endLocn = MapUtils.getRectPosition(navpoint.getLocation(), mapCenter, baseMap, displaySize);
			var startLocn = MapUtils.getRectPosition(previous, mapCenter, baseMap, displaySize);
			g.drawLine(startLocn.getX(), startLocn.getY(), endLocn.getX(), endLocn.getY());

			// Draw arrows
			double angle = startLocn.getRadians(endLocn);

			IntPoint arrowLocation = new IntPoint(
					(int) (startLocn.getX() + (((endLocn.getX() - startLocn.getX()) / 2))),
					(int) (startLocn.getY() + (((endLocn.getY() - startLocn.getY()) / 2)))); 
			drawArrow(g, arrowLocation, angle);

			// Draw the navpoint icon.
			IntPoint iconLocation = new IntPoint(endLocn.getX(), 
					(endLocn.getY() - navIcon.getIconHeight()));
			navIcon.paintIcon(displayComponent, g, iconLocation.getX(), iconLocation.getY());

			return new NavpointHotspot(endLocn, context, navpoint);
		}

		return null;
	}

	private static void drawArrow(Graphics g1, IntPoint arrowLocation, double angle ) {
		Graphics2D ga = (Graphics2D) g1.create();

		// convert Degree to Radians 
		AffineTransform at = new AffineTransform(); 
		at.translate(arrowLocation.getX(), arrowLocation.getY());// transport cursor to draw arrowhead position.
		at.rotate(angle);
		ga.transform(at); 

		Polygon arrowHead = getArrowHead();
		ga.fill(arrowHead);
		ga.drawPolygon(arrowHead); 

		ga.dispose();
	}

	private static Polygon getArrowHead() {
		if (arrowHead == null) {
			arrowHead = new Polygon();
			arrowHead.addPoint(5, 0); 
			arrowHead.addPoint(-5, 5); 
			arrowHead.addPoint(-2, -0); 
			arrowHead.addPoint(-5, -5); 
		}
		return arrowHead;
	}
}