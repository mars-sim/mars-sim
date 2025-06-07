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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.mars_sim.core.environment.Landmark;
import com.mars_sim.core.environment.LandmarkType;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.map.location.IntPoint;
import com.mars_sim.core.map.location.SurfaceManager;

/**
 * The LandmarkMapLayer is a graphics layer to display landmarks.
 */
public class LandmarkMapLayer extends SurfaceFeatureLayer<Landmark> 
	implements FilteredMapLayer {
		
	private class LandmarkHotspot extends MapHotspot {

		private Landmark site;

		protected LandmarkHotspot(IntPoint center, Landmark site) {
			super(center, 5);
			this.site = site;
		}

		/**
		 * Create a structured text summary for a tooltip of the Landmark
		 */
		@Override
		public String getTooltipText() {
			String tooltip = "<html>Name: " + site.getName()
					+ "<br>Origin: " + site.getOrigin()
					+ "<br>Type: " + site.getType().getName();
			var desc = site.getDescription();
			if (desc != null) {
				tooltip += "<br>Desc: " + desc;
			}
			tooltip += "</html>";
			return tooltip;
		}	
	}
	
	/** Label font for landmarks. */
	private static final Font OTHERS_LABEL_FONT = new Font("Monospaced", Font.PLAIN, 18);
	private static final Color OTHERS_MONO = new Color(95, 60, 60);
	private static final Color OTHERS_COLOR = new Color(230, 186, 186);
	private static final int LABEL_HORIZONTAL_OFFSET = 2;
	private static final int CIRCLE_DIAMETER = 4;

	/** Label font for artificial object. */
	private static final Font AO_LABEL_FONT = new Font("Dialog", Font.ITALIC, 10);
	private static final Color AO_MONO = new Color(173, 173, 173);
	private static final Color AO_COLOR = new Color(127, 127, 255);
	private static final int AO_LABEL_HORIZONTAL_OFFSET = 1;
	private static final int AO_CIRCLE_DIAMETER = 4;

	private Set<LandmarkType> displayed = new HashSet<>();
	private SurfaceManager<Landmark> landmarks;
	private MapPanel displayComponent;

	/**
	 * Create a landmark layer for a Map Panel
	 * @param panel
	 */
	public LandmarkMapLayer(MapPanel panel) {
		super("Landmark Layer");
		landmarks = panel.getDesktop().getSimulation().getConfig()
							.getLandmarkConfiguration().getLandmarks();
		displayComponent = panel;

		// By default everything
		for(var l : LandmarkType.values()) {
			displayed.add(l);
		}
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
    protected MapHotspot displayFeature(Landmark landmark, IntPoint location, Graphics2D g2d,
								boolean isColourful) {
		if (!displayed.contains(landmark.getType())) {
			return null;
		}

		int locLabelX = 0;
		int locLabelY = 0;

		Font labelFont;
		int circleDim;
		Color circleColour;
		if (landmark.getType() == LandmarkType.AO) {
			// Find location to display label.
			locLabelX = location.getiX() + (AO_CIRCLE_DIAMETER / 2) + AO_LABEL_HORIZONTAL_OFFSET;
			locLabelY = location.getiY() +  AO_CIRCLE_DIAMETER;
			
			circleDim = AO_CIRCLE_DIAMETER;
			labelFont = AO_LABEL_FONT;
			circleColour = (isColourful ? AO_COLOR : AO_MONO);
		}
		else {

			// Find location to display label.
			locLabelX = location.getiX() + (CIRCLE_DIAMETER / 2) + LABEL_HORIZONTAL_OFFSET;
			locLabelY = location.getiY() - (CIRCLE_DIAMETER / 2) - LABEL_HORIZONTAL_OFFSET;

			labelFont = OTHERS_LABEL_FONT;
			circleColour = (isColourful ? OTHERS_COLOR : OTHERS_MONO);
			circleDim = CIRCLE_DIAMETER;
		}

		// Draw a circle at the location.
		g2d.setColor(circleColour);
		int locX = location.getiX() - (circleDim / 2);
		int locY = location.getiY() - (circleDim / 2);
		g2d.drawOval(locX, locY, circleDim, circleDim);
		
		// Draw the landmark name.
		g2d.setFont(labelFont);
		g2d.drawString(landmark.getName(), locLabelX, locLabelY);

		return new LandmarkHotspot(location, landmark);
	}

	@Override
	public List<MapFilter> getFilterDetails() {
		List<MapFilter> filters = new ArrayList<>();
		for(var lt : LandmarkType.values()) {
			filters.add(new MapFilter(lt.name(), lt.getName(), displayed.contains(lt), 
							ColorLegendFactory.getLegend((lt == LandmarkType.AO ? AO_COLOR : OTHERS_COLOR),
													displayComponent)));
		}

		return filters;
	}

	@Override
	public void displayFilter(String name, boolean display) {
		try {
			LandmarkType lt = LandmarkType.valueOf(name);
			if (display) {
				displayed.add(lt);
			}
			else {
				displayed.remove(lt);
			}
		}
		catch(IllegalArgumentException iae) {
			// Problem loading filter
		}
	}
}
