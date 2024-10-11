/*
 * Mars Simulation Project
 * LandmarkMapLayer.java
 * @date 2023-04-29
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.tool.map;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;

import com.mars_sim.core.environment.Landmark;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.map.location.IntPoint;
import com.mars_sim.core.map.location.SurfaceManager;

/**
 * The LandmarkMapLayer is a graphics layer to display landmarks.
 */
public class LandmarkMapLayer extends SurfaceFeatureLayer<Landmark> {

	/** Diameter of marking circle. */
	private static final int CIRCLE_DIAMETER = 4;
	/** Diameter of marking circle for artificial objects. */
	private static final int AO_CIRCLE_DIAMETER = 4;
	/** Horizontal offset for label. */
	private static final int LABEL_HORIZONTAL_OFFSET = 2;
	/** Horizontal offset for artificial objects. */
	private static final int AO_LABEL_HORIZONTAL_OFFSET = 1;
	
	/** Light pink color for landmarks on surface map. */
	private static final Color SURFACE_COLOR = new Color(230, 186, 186);
	/** Dark pink color for landmarks on topo map. */
	private static final Color TOPO_COLOR = new Color(95, 60, 60);
	/** Light violet color for artificial objects on surface map. */
	private static final Color AO_COLOR_0 = new Color(127, 127, 255);
	/** Gray color for artificial objects on topo map. */
	private static final Color AO_COLOR_1 = new Color(173, 173, 173);
	/** Label font for landmarks. */
	private static final Font MAP_LABEL_FONT = new Font("Monospaced", Font.PLAIN, 18);
	/** Label font for artificial object. */
	private static final Font AO_LABEL_FONT = new Font("Dialog", Font.ITALIC, 10);
	
	private SurfaceManager<Landmark> landmarks;

	public LandmarkMapLayer(MapPanel panel) {
		super("Landmark Layer");
		landmarks = panel.getDesktop().getSimulation().getConfig()
							.getLandmarkConfiguration().getLandmarks();
	}

    /**
     * Return a list of landmarks that are within the focus specified
     * @param center Center of the viewpoint
     * @param arcAngle Angle of the viewpoint
     * @return
     */
	@Override
    protected List<Landmark> getFeatures(Coordinates center, double arcAngle) {
		return landmarks.getFeatures(center, arcAngle);
	}

    /**
     * Setup the graphic context to draw this landmarks to control the font rendering.
     * @param g2d
     */
	@Override
    protected void prepareGraphics(Graphics2D g2d) {
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
	}

	/**
     * Display a feature on the map using a Graphic at a particular point.
     * @param f Feature to display
     * @param location Locatino on the Graphic
     * @param g2d Graphic for drawing
	 * @param isColourful Is the destination a colourful map
     */
	@Override
    protected void displayFeature(Landmark landmark, IntPoint location, Graphics2D g2d,
								boolean isColourful) {
		// Determine circle location.
		int locX = location.getiX() - (CIRCLE_DIAMETER / 2);
		int locY = location.getiY() - (CIRCLE_DIAMETER / 2);

		int locLabelX = 0;
		int locLabelY = 0;

		if (landmark.getType().equalsIgnoreCase(Landmark.AO_TYPE)) {
			// Find location to display label.
			locLabelX = location.getiX() + (AO_CIRCLE_DIAMETER / 2) + AO_LABEL_HORIZONTAL_OFFSET;
			locLabelY = location.getiY() +  AO_CIRCLE_DIAMETER;
			// Set the label font.
			g2d.setFont(AO_LABEL_FONT);
			// Set the label color.
			if (isColourful)
				g2d.setColor(AO_COLOR_0);
			else
				g2d.setColor(AO_COLOR_1);
			
			// Draw a circle at the location.
			g2d.drawOval(locX, locY, AO_CIRCLE_DIAMETER, AO_CIRCLE_DIAMETER);
			
			// Draw the landmark name.
			g2d.drawString(landmark.getName(), locLabelX, locLabelY);
		}
		
		else {

			// Find location to display label.
			locLabelX = location.getiX() + (CIRCLE_DIAMETER / 2) + LABEL_HORIZONTAL_OFFSET;
			locLabelY = location.getiY() - (CIRCLE_DIAMETER / 2) - LABEL_HORIZONTAL_OFFSET;
			// Set the label font.
			g2d.setFont(MAP_LABEL_FONT);
			// Set the label color.
			if (isColourful)
				g2d.setColor(SURFACE_COLOR);
			else
				g2d.setColor(TOPO_COLOR);
			
			// Draw a circle at the location.
			g2d.drawOval(locX, locY, CIRCLE_DIAMETER, CIRCLE_DIAMETER);
			
			// Draw the landmark name.
			g2d.drawString(landmark.getName(), locLabelX, locLabelY);
		}
	}
}
