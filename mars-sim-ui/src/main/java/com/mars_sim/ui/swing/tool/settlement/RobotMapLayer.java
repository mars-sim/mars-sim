/**
 * Mars Simulation Project
 * RobotMapLayer.java
 * @date 2023-11-06
 * @author Manny Kung
 */
package com.mars_sim.ui.swing.tool.settlement;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.Iterator;
import java.util.List;

import com.mars_sim.core.CollectionUtils;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;

/**
 * A settlement map layer for displaying Robots.
 */
public class RobotMapLayer implements SettlementMapLayer {

	// Static members
	private final Color robotColor = LabelMapLayer.robotColor;//Color.green; 
	private final Color robotOutline = LabelMapLayer.robotOutline;//new Color(0, 0, 0, 190);
	private final Color robotSelected = LabelMapLayer.robotSelected;//Color.red; 
	private final Color robotSelectedOutline = LabelMapLayer.robotSelectedOutline;//.new Color(0, 0, 0, 190);

	// Data members
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
	 * Draw robots at a settlement.
	 * @param g2d the graphics context.
	 * @param settlement the settlement to draw robots at.
	 */
	private void drawRobots(Graphics2D g2d, Settlement settlement, double scale) {

		List<Robot> robots = CollectionUtils.getAssociatedRobotsInSettlementVicinity(settlement);
		Robot selectedRobot = mapPanel.getSelectedRobot();

		// Draw all robots except selected robot.
		Iterator<Robot> i = robots.iterator();
		while (i.hasNext()) {
			Robot robot = i.next();
			if (!robot.equals(selectedRobot)) {
				drawRobot(g2d, robot, robotColor, robotOutline, scale);
			}
		}

		// Draw selected robot.
		if (robots.contains(selectedRobot)) {
			drawRobot(g2d, selectedRobot, robotSelected, robotSelectedOutline, scale);
		}
	}

	/**
	 * Draw a robot at a settlement.
	 * @param g2d the graphics context.
	 * @param robot the robot to draw.
	 */
	private void drawRobot(Graphics2D g2d, Robot robot, Color iconColor, Color outlineColor, double scale) {

		int size = (int)(Math.round(scale / 3.0));
		size = Math.max(size, 1);
		
//		int size1 = (int)(Math.round(size * 1.1));
		
		double radius = size / 2.0;
		
		// Save original graphics transforms.
		AffineTransform saveTransform = g2d.getTransform();

		double translationX = -1.0 * robot.getPosition().getX() * scale - radius;
		double translationY = -1.0 * robot.getPosition().getY() * scale - radius;

		// Apply graphic transforms for label.
		AffineTransform newTransform = new AffineTransform(saveTransform);
		newTransform.translate(translationX, translationY);
		newTransform.rotate(mapPanel.getRotation() * -1D, radius, radius);
		g2d.setTransform(newTransform);

//		// Set color outline color.
//		g2d.setColor(outlineColor);
//		
//		// Draw outline circle.
//		g2d.fillOval(0,  0, size1, size1);
		
		// Set circle color.
		g2d.setColor(iconColor);

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
