/**
 * Mars Simulation Project
 * ExploredSiteMapLayer.java
 * @version 2.84 2008-05-15
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.tool.map;

import java.awt.Component;
import java.awt.Graphics;
import java.util.Iterator;

import javax.swing.Icon;

import org.mars_sim.msp.simulation.Coordinates;
import org.mars_sim.msp.simulation.IntPoint;
import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.simulation.mars.ExploredLocation;
import org.mars_sim.msp.simulation.mars.SurfaceFeatures;
import org.mars_sim.msp.ui.standard.ImageLoader;

public class ExploredSiteMapLayer implements MapLayer {

	// Static members
	private static final String EXPLORED_ICON_NAME = "SmallFlagYellow";
	private static final String MINED_ICON_NAME = "SmallFlagGray";
	
	// Domain members
	private Component displayComponent;
	private Icon navpointIconExplored;
	private Icon navpointIconMined;
	
	/**
	 * Constructor
	 * @param displayComponent the display component.
	 */
	public ExploredSiteMapLayer(Component displayComponent) {
		
		// Initialize domain data.
		this.displayComponent = displayComponent;
		navpointIconExplored = ImageLoader.getIcon(EXPLORED_ICON_NAME);
		navpointIconMined = ImageLoader.getIcon(MINED_ICON_NAME);
	}
	
	/**
     * Displays the layer on the map image.
     * @param mapCenter the location of the center of the map.
     * @param mapType the type of map.
     * @param g graphics context of the map display.
     */
	public void displayLayer(Coordinates mapCenter, String mapType, Graphics g) {
		SurfaceFeatures surfaceFeatures = Simulation.instance().getMars().getSurfaceFeatures();
		Iterator<ExploredLocation> i = surfaceFeatures.getExploredLocations().iterator();
		while (i.hasNext()) {
			ExploredLocation site = i.next();
			if (site.isExplored()) displayExploredSite(site, mapCenter, mapType, g);
		}
	}
	
	/**
	 * Displays a navpoint.
	 * @param navpoint the navpoint to display.
	 * @param mapCenter the location of the center of the map.
	 * @param mapType the type of map.
	 * @param g graphics context of the map display.
	 */
	private void displayExploredSite(ExploredLocation site, Coordinates mapCenter, String mapType, 
			Graphics g) {
		double angle = 0D;
		if (USGSMarsMap.TYPE.equals(mapType)) angle = USGSMarsMap.HALF_MAP_ANGLE;
		else angle = CannedMarsMap.HALF_MAP_ANGLE;
		
		if (mapCenter.getAngle(site.getLocation()) < angle) {
			
			// Chose a navpoint icon based on the map type.
			Icon navIcon = null;
			if (site.isMined()) navIcon = navpointIconMined;
			else navIcon = navpointIconExplored;
			
			// Determine the draw location for the icon.
			IntPoint location = MapUtils.getRectPosition(site.getLocation(), mapCenter, mapType);
			IntPoint drawLocation = new IntPoint(location.getiX(), 
					(location.getiY() - navIcon.getIconHeight()));
	        
			// Draw the navpoint icon.
	        navIcon.paintIcon(displayComponent, g, drawLocation.getiX(), drawLocation.getiY());
		}
	}
}