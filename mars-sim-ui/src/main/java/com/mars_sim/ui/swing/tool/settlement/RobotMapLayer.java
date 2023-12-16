/**
 * Mars Simulation Project
 * RobotMapLayer.java
 * @date 2023-11-06
 * @author Manny Kung
 */
package com.mars_sim.ui.swing.tool.settlement;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

import com.mars_sim.core.CollectionUtils;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.structure.Settlement;

/**
 * A settlement map layer for displaying Robots.
 */
public class RobotMapLayer extends WorkerMapLayer<Robot> {

	private static final ColorChoice ROBOT_UNSELECTED = new ColorChoice(new Color(85, 77, 0), Color.white);
	private static final ColorChoice ROBOT_SELECTED = new ColorChoice(new Color(196, 178, 71), Color.white);

	// Data members
	private SettlementMapPanel mapPanel;

	/**
	 * Constructor.
	 * 
	 * @param mapPanel the settlement map panel.
	 */
	public RobotMapLayer(SettlementMapPanel mapPanel) {
		// Initialize data members.
		this.mapPanel = mapPanel;		
	}


	@Override
	public void displayLayer(
		Graphics2D g2d, Settlement settlement,
		double xPos, double yPos, int mapWidth, int mapHeight,
		double rotation, double scale) {

		List<Robot> robots = CollectionUtils.getAssociatedRobotsInSettlementVicinity(settlement);
		Robot selectedRobot = mapPanel.getSelectedRobot();
		drawWorkers(robots, selectedRobot, mapPanel.isShowRobotLabels(),
					g2d, xPos, yPos, mapWidth, mapHeight, rotation, scale);
	}

	/**
	 * Identifies the best colour to render this Robot in the Settlement Map.
	 * 
	 * @param r Robot
	 * @param selected Are they selected
	 * @return
	 */
	@Override
    protected ColorChoice getColor(Robot r, boolean selected) {
		return (selected ? ROBOT_SELECTED : ROBOT_UNSELECTED);
	}

	@Override
	public void destroy() {
		mapPanel = null;
	}
}
