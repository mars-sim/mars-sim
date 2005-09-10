/**
 * Mars Simulation Project
 * LandmarkMapLayer.java
 * @version 2.78 2005-09-08
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.standard.tool.navigator;

import java.awt.Graphics;
import java.util.Iterator;
import javax.swing.Icon;

import org.mars_sim.msp.simulation.IntPoint;
import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.simulation.person.ai.mission.Mission;
import org.mars_sim.msp.simulation.person.ai.mission.MissionManager;
import org.mars_sim.msp.simulation.person.ai.mission.NavPoint;
import org.mars_sim.msp.simulation.person.ai.mission.TravelMission;
import org.mars_sim.msp.ui.standard.ImageLoader;

/**
 * The NavpointMapLayer is a graphics layer to display mission navpoints.
 */
public class NavpointMapLayer implements MapLayer {

	// Static members
	private static final String COLORED_ICON_NAME = "FlagBlue";
	private static final String WHITE_ICON_NAME = "FlagWhite";
	
	// Domain members
	private MapDisplay mapDisplay;
	private Icon navpointIconColor;
	private Icon navpointIconWhite;
	

	/**
	 * Constructor
	 * @param mapDisplay the MapDisplay to use
	 */
	public NavpointMapLayer(MapDisplay mapDisplay) {
		
		// Initialize domain data.
		this.mapDisplay = mapDisplay;
		navpointIconColor = ImageLoader.getIcon(COLORED_ICON_NAME);
		navpointIconWhite = ImageLoader.getIcon(WHITE_ICON_NAME);
	}

	/**
	 * Displays the layer on the map image.
	 * @param g graphics context of the map display.
	 */
	public void displayLayer(Graphics g) {
		MissionManager manager = Simulation.instance().getMissionManager();
		Iterator i = manager.getMissions().iterator();
		while (i.hasNext()) {
			Mission mission = (Mission) i.next();
			if (mission instanceof TravelMission) displayMission((TravelMission) mission, g);
		}
	}
	
	/**
	 * Displays the navpoints in a travel mission.
	 * @param mission the travel mission to display.
	 * @param g graphics context of the map display.
	 */
	private void displayMission(TravelMission mission, Graphics g) {
		for (int x = 0; x < mission.getNumberOfNavpoints(); x++) {
			NavPoint navpoint = mission.getNavpoint(x);
			displayNavpoint(navpoint, g);
		}
	}
	
	/**
	 * Displays a navpoint.
	 * @param navpoint the navpoint to display.
	 * @param g graphics context of the map display.
	 */
	private void displayNavpoint(NavPoint navpoint, Graphics g) {
		double angle = 0D;
		if (mapDisplay.isUsgs() && mapDisplay.isSurface()) 
			angle = MapDisplay.HALF_MAP_ANGLE_USGS;
		else angle = MapDisplay.HALF_MAP_ANGLE_STANDARD;
		if (mapDisplay.getMapCenter().getAngle(navpoint.getLocation()) < angle) {
			
			// Chose a navpoint icon based on the map type.
			Icon navIcon = navpointIconColor;
			if (mapDisplay.isTopo()) navIcon = navpointIconWhite;
			
			// Determine the draw location for the icon.
			IntPoint location = mapDisplay.getRectPosition(navpoint.getLocation());
			IntPoint drawLocation = new IntPoint(location.getiX(), (location.getiY() - navIcon.getIconHeight()));
	        
			// Draw the navpoint icon.
	        navIcon.paintIcon(mapDisplay, g, drawLocation.getiX(), drawLocation.getiY());
		}
	}
}