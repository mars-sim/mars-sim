/**
 * Mars Simulation Project
 * LandmarkMapLayer.java
 * @version 2.76 2004-05-22
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.standard.tool.navigator;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.*;
import org.mars_sim.msp.simulation.*;

/**
 * The LandmarkMapLayer is a graphics layer to display landmarks.
 */
class LandmarkMapLayer implements MapLayer {

	// Diameter of marking circle.
	private int CIRCLE_DIAMETER = 10;
	
	// Blue color for surface map.
	private Color SURFACE_COLOR = new Color(50, 50, 255);
	
	// Gray color for surface map.
	private Color TOPO_COLOR = new Color(50, 50, 50);
	
	// Label font.
	private Font MAP_LABEL_FONT = new Font("SansSerif", Font.PLAIN, 10);
	
	// Horizontal offset for label.
	private int LABEL_HORIZONTAL_OFFSET = 2;

	// Domain data
	private List landmarks;
	private MapDisplay mapDisplay;

	/**
	 * Constructor
	 * @param mars the mars instance.
	 * @param mapDisplay the mapDisplay to use.
	 */
	LandmarkMapLayer(Mars mars, MapDisplay mapDisplay) {
        
		landmarks = mars.getSurfaceFeatures().getLandmarks();
		this.mapDisplay = mapDisplay;
	}

	/**
	 * Displays the layer on the map image.
	 * @param g graphics context of the map display.
	 */
	public void displayLayer(Graphics g) {
		
		Iterator i = landmarks.iterator();
		while (i.hasNext()) {
			Landmark landmark = (Landmark) i.next();
			double angle = 0D;
			if (mapDisplay.isUsgs() && mapDisplay.isSurface()) 
				angle = MapDisplay.HALF_MAP_ANGLE_USGS;
			else angle = MapDisplay.HALF_MAP_ANGLE_STANDARD;
			if (mapDisplay.getMapCenter().getAngle(landmark.getLandmarkLocation()) < angle)
				displayLandmark(landmark, g);
		}
	}

	/**
	 * Display a landmark on the map layer.
	 * @param landmark the landmark to be displayed.
	 * @param g the graphics context.
	 */	
	private void displayLandmark(Landmark landmark, Graphics g) {
		
		// Determine display location of landmark.
		IntPoint location = mapDisplay.getRectPosition(landmark.getLandmarkLocation());
		
		// Determine circle location.
		int locX = location.getiX() - (CIRCLE_DIAMETER / 2);
		int locY = location.getiY() - (CIRCLE_DIAMETER / 2);
		
		// Set the color
		if (mapDisplay.isTopo()) g.setColor(TOPO_COLOR);
		else g.setColor(SURFACE_COLOR);
		
		// Draw a circle at the location.
		g.drawOval(locX, locY, CIRCLE_DIAMETER, CIRCLE_DIAMETER);
		
		// Find location to display label.
		int locLabelX = location.getiX() + (CIRCLE_DIAMETER / 2) + LABEL_HORIZONTAL_OFFSET;
		int locLabelY = location.getiY() + CIRCLE_DIAMETER;
		
		// Set the label font.
		g.setFont(MAP_LABEL_FONT);
		
		// Draw the landmark name.
		g.drawString(landmark.getLandmarkName(), locLabelX, locLabelY);
	}
}