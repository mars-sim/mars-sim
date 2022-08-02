/*
 * Mars Simulation Project
 * MapLayer.java
 * @date 2022-08-02
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.tool.map;

import java.awt.Graphics;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.environment.SurfaceFeatures;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;

/**
 * The MapLayer interface is a graphics layer painted on the map display.
 */
public interface MapLayer {

	static Simulation sim = Simulation.instance();
	static MissionManager missionManager = sim.getMissionManager();
	static UnitManager unitManager = sim.getUnitManager();
	static SurfaceFeatures surfaceFeatures = sim.getSurfaceFeatures();
	
	/**
	 * Displays the layer on the map image.
	 * 
	 * @param mapCenter the location of the center of the map.
	 * @param mapType   the type of map.
	 * @param g         graphics context of the map display.
	 */
	public void displayLayer(Coordinates mapCenter, String mapType, Graphics g);
}
