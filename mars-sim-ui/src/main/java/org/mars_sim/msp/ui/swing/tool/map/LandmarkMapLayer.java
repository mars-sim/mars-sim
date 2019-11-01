/**
 * Mars Simulation Project
 * LandmarkMapLayer.java
 * @version 3.1.0 2017-09-15
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.map;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.IntPoint;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.mars.Landmark;

/**
 * The LandmarkMapLayer is a graphics layer to display landmarks.
 */
public class LandmarkMapLayer implements MapLayer {

	/** Diameter of marking circle. */
	private int CIRCLE_DIAMETER = 2; // FIXME: make proportional to actual loaded diameter.

	/** Diameter of marking circle for artificial objects. */
	private int AO_CIRCLE_DIAMETER = 3;
	
	/** Light pink color for landmarks on surface map. */
	private static final Color SURFACE_COLOR = new Color(230, 186, 186);

	/** Dark pink color for landmarks on topo map. */
	private static final Color TOPO_COLOR = new Color(95, 60, 60);

	/** Light violet color for artificial objects on surface map. */
	private static final Color AO_SURFACE_COLOR = new Color(127, 127, 255);

	/** Gray color for artificial objects on topo map. */
	private static final Color AO_TOPO_COLOR = new Color(173, 173, 173);
	
	/** Label font for landmarks. */
	private static final Font MAP_LABEL_FONT = new Font("Monospaced", Font.PLAIN, 18);
	/** Label font for artificial object. */
	private static final Font AO_LABEL_FONT = new Font("Dialog", Font.ITALIC, 10);
	
	/** Horizontal offset for label. */
	private int LABEL_HORIZONTAL_OFFSET = 2;

	/** Horizontal offset for artificial objects. */
	private int AO_LABEL_HORIZONTAL_OFFSET = 1;
	
	private static final List<Landmark> landmarks = Simulation.instance().getMars().getSurfaceFeatures().getLandmarks();

	private static final double angle = CannedMarsMap.HALF_MAP_ANGLE;
	
//	private double width = 50;
//	
//	private double height = 50;
	

	/**
	 * Displays the layer on the map image.
	 *
	 * @param mapCenter the location of the center of the map.
	 * @param mapType   the type of map.
	 * @param g         graphics context of the map display.
	 */
	public void displayLayer(Coordinates mapCenter, String mapType, Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

		for (Landmark landmark : landmarks) {
			if (mapCenter.getAngle(landmark.getLandmarkCoord()) < angle)
				displayLandmark(landmark, mapCenter, mapType, g2d);
		}
	}

	/**
	 * Display a landmark on the map layer.
	 * 
	 * @param landmark  {@link Landmark} the landmark to be displayed.
	 * @param mapCenter {@link Coordinates} the location of the center of the map.
	 * @param mapType   {@LINK String} type of map.
	 * @param g         {@link Graphics} the graphics context.
	 */
	private void displayLandmark(Landmark landmark, Coordinates mapCenter, String mapType, Graphics2D g2d) {

		// Determine display location of landmark.
		IntPoint location = MapUtils.getRectPosition(landmark.getLandmarkCoord(), mapCenter, mapType);

		// Determine circle location.
		int locX = location.getiX() - (CIRCLE_DIAMETER / 2);
		int locY = location.getiY() - (CIRCLE_DIAMETER / 2);

		int locLabelX = 0;
		int locLabelY = 0;

		if (landmark.getLandmarkType().equalsIgnoreCase("AO")) {
			// Find location to display label.
			locLabelX = location.getiX() + (AO_CIRCLE_DIAMETER / 2) + AO_LABEL_HORIZONTAL_OFFSET;
			locLabelY = location.getiY() +  AO_CIRCLE_DIAMETER;
			// Set the label font.
			g2d.setFont(AO_LABEL_FONT);
			// Set the label color.
			if (TopoMarsMap.TYPE.equals(mapType))
				g2d.setColor(AO_TOPO_COLOR);
			else
				g2d.setColor(AO_SURFACE_COLOR);
			
			// Draw a circle at the location.
			g2d.drawOval(locX, locY, AO_CIRCLE_DIAMETER, AO_CIRCLE_DIAMETER);
			
			// Draw the landmark name.
			g2d.drawString(landmark.getLandmarkName(), locLabelX, locLabelY);
		}
		
		else {

			// Find location to display label.
			locLabelX = location.getiX() + (CIRCLE_DIAMETER / 2) + LABEL_HORIZONTAL_OFFSET;
			locLabelY = location.getiY() - (CIRCLE_DIAMETER / 2) - LABEL_HORIZONTAL_OFFSET;
			// Set the label font.
			g2d.setFont(MAP_LABEL_FONT);
			// Set the label color.
			if (TopoMarsMap.TYPE.equals(mapType))
				g2d.setColor(TOPO_COLOR);
			else
				g2d.setColor(SURFACE_COLOR);
			
			// Draw a circle at the location.
			g2d.drawOval(locX, locY, CIRCLE_DIAMETER, CIRCLE_DIAMETER);
			
			// Draw the landmark name.
			g2d.drawString(landmark.getLandmarkName(), locLabelX, locLabelY);
	
//		    FontRenderContext frc = g2d.getFontRenderContext();
//		    TextLayout textTl = new TextLayout(landmark.getLandmarkName(), MAP_LABEL_FONT, frc);
//		    AffineTransform transform = new AffineTransform();
//		    Shape outline = textTl.getOutline(null);
//		    Rectangle outlineBounds = outline.getBounds();
//		    transform = g2d.getTransform();
//		    transform.translate(width / 2 - (outlineBounds.width / 2), height / 2
//		        + (outlineBounds.height / 2));
//		    g2d.transform(transform);
//					    
//		    g2d.draw(outline);
//		    g2d.setClip(outline);
		}
	}
}