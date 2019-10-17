/**
 * Mars Simulation Project
 * RobotMapLayer.java
 * @version 3.1.0 2017-09-01
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.tool.settlement;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;

/**
 * A settlement map layer for displaying Robots.
 */
public class RobotMapLayer implements SettlementMapLayer {

	// Static members
	private static final Color ROBOT_COLOR = LabelMapLayer.ROBOT_LABEL_COLOR;//Color.green; 
	private static final Color ROBOT_OUTLINE_COLOR = LabelMapLayer.ROBOT_LABEL_OUTLINE_COLOR;//new Color(0, 0, 0, 190);
	private static final Color SELECTED_COLOR = LabelMapLayer.SELECTED_ROBOT_LABEL_COLOR;//Color.red; 
	private static final Color SELECTED_OUTLINE_COLOR = LabelMapLayer.SELECTED_ROBOT_LABEL_OUTLINE_COLOR;//.new Color(0, 0, 0, 190);

	// Data members
//	private int size = 1;
	
	private double circleDiameter = 10D;
	private double centerX = circleDiameter / 2D;
	private double centerY = circleDiameter / 2D;

	
	private SettlementMapPanel mapPanel;

	/**
	 * Constructor
	 * @param mapPanel the settlement map panel.
	 */
	public RobotMapLayer(SettlementMapPanel mapPanel) {
		// Initialize data members.
		this.mapPanel = mapPanel;		
	}

	@Override
	// 2014-11-04 Added building parameter
	public void displayLayer(
		Graphics2D g2d, Settlement settlement, Building building,
		double xPos, double yPos, int mapWidth, int mapHeight,
		double rotation, double scale
	) {

		// Save original graphics transforms.
		AffineTransform saveTransform = g2d.getTransform();

		// Get the map center point.
		double mapCenterX = mapWidth / 2D;
		double mapCenterY = mapHeight / 2D;

		// Translate map from settlement center point.
		g2d.translate(mapCenterX + (xPos * scale), mapCenterY + (yPos * scale));

		// Rotate map from North.
		g2d.rotate(rotation, 0D - (xPos * scale), 0D - (yPos * scale));

		// Draw all robots.
		drawRobots(g2d, settlement, scale);

		// Restore original graphic transforms.
		g2d.setTransform(saveTransform);
	}


	/**
	 * Gets a list of robots to display on a settlement map.
	 * @param settlement the settlement
	 * @return list of robots to display.
	 */
	public static List<Robot> getRobotsToDisplay(Settlement settlement) {

		List<Robot> result = new ArrayList<Robot>();

		if (settlement != null) {
			Iterator<Robot> i = unitManager.getRobots().iterator();
			while (i.hasNext()) {
				Robot robot = i.next();

				// Only select functional robots.
				if (!robot.getSystemCondition().isInoperable()) {

					// Select a robot that is at the settlement location.
					Coordinates settlementLoc = settlement.getCoordinates();
					Coordinates personLoc = robot.getCoordinates();
					if (personLoc.equals(settlementLoc)) {
						result.add(robot);
					}
				}
			}
		}

		return result;
	}
	
	/**
	 * Draw robots at a settlement.
	 * @param g2d the graphics context.
	 * @param settlement the settlement to draw robots at.
	 */
	private void drawRobots(Graphics2D g2d, Settlement settlement, double scale) {

		List<Robot> robots = getRobotsToDisplay(settlement);
		Robot selectedRobot = mapPanel.getSelectedRobot();

		// Draw all robots except selected robot.
		Iterator<Robot> i = robots.iterator();
		while (i.hasNext()) {
			Robot robot = i.next();
			if (!robot.equals(selectedRobot)) {
				drawRobot(g2d, robot, ROBOT_COLOR, ROBOT_OUTLINE_COLOR, scale);
			}
		}

		// Draw selected robot.
		if (robots.contains(selectedRobot)) {
			drawRobot(g2d, selectedRobot, SELECTED_COLOR, SELECTED_OUTLINE_COLOR, scale);
		}
	}

	/**
	 * Draw a robot at a settlement.
	 * @param g2d the graphics context.
	 * @param robot the robot to draw.
	 */
	private void drawRobot(Graphics2D g2d, Robot robot, Color iconColor, Color outlineColor, double scale) {

		// Save original graphics transforms.
		AffineTransform saveTransform = g2d.getTransform();

		double translationX = (-1D * robot.getXLocation() * scale - centerX);
		double translationY = (-1D * robot.getYLocation() * scale - centerY);

		// Apply graphic transforms for label.
		AffineTransform newTransform = new AffineTransform(saveTransform);
		newTransform.translate(translationX, translationY);
		newTransform.rotate(mapPanel.getRotation() * -1D, centerX, centerY);
		g2d.setTransform(newTransform);

		// Set circle color.
		g2d.setColor(iconColor);

		int size = 0;
		if (scale > 0)
			size = (int)(scale/4.5);
		else if (scale <= 0)
			size = 2;
		
		// Draw circle
		g2d.fillOval(0, 0, size, size);

		// Restore original graphic transforms.
		g2d.setTransform(saveTransform);
	}

	@Override
	public void destroy() {
		mapPanel = null;
	}
}