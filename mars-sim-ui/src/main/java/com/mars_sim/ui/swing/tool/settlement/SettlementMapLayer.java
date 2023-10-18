/*
 * Mars Simulation Project
 * SettlementMapLayer.java
 * @date 2023-06-20
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.tool.settlement;

import java.awt.Graphics2D;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.UnitManager;
import com.mars_sim.core.environment.SurfaceFeatures;
import com.mars_sim.core.person.ai.mission.MissionManager;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;

/**
 * An interface for a display layer on the settlement map.
 */
public interface SettlementMapLayer {

	static Simulation sim = Simulation.instance();
	static UnitManager unitManager = sim.getUnitManager();
	static MissionManager missionManager = sim.getMissionManager();
	static SurfaceFeatures surfaceFeatures = sim.getSurfaceFeatures();

	/**
	 * Displays the settlement map layer.
	 * 
	 * @param g2d        the graphics context.
	 * @param settlement the settlement to display.
	 * @param building 	 the building of interest
	 * @param xPos       the X center position.
	 * @param yPos       the Y center position.
	 * @param mapWidth   the width of the map.
	 * @param mapHeight  the height of the map.
	 * @param rotation   the rotation (radians)
	 * @param scale      the map scale.
	 */
	public void displayLayer(Graphics2D g2d, Settlement settlement, Building building, double xPos, double yPos,
			int mapWidth, int mapHeight, double rotation, double scale);

	/**
	 * Destroy the map layer.
	 */
	public void destroy();
}
