/**
 * Mars Simulation Project
 * TerrainDisplayPanel.java
 * @version 3.1.0 2019-09-20
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.unit_window.vehicle;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Polygon;

import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import org.mars_sim.msp.core.vehicle.GroundVehicle;
import org.mars_sim.msp.core.vehicle.StatusType;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * The TerrainDisplayPanel class displays the compass direction a vehicle is
 * currently travelling.
 */
@SuppressWarnings("serial")
public class TerrainDisplayPanel extends JPanel {

	// Data members
	/** Is UI constructed. */
	private boolean uiDone = false;
	
	/** The Vehicle instance. */
	private GroundVehicle vehicle;
	
	/**
	 * Constructor
	 *
	 * @param vehicle the vehicle to track
	 */
	public TerrainDisplayPanel(GroundVehicle vehicle) {
		// Use WebPanel constructor
		super();

		// Initialize data members
		this.vehicle = vehicle;

	}

	public boolean isUIDone() {
		return uiDone;
	}
	
	public void initializeUI() {
		uiDone = true;
			
		// Set preferred component size.
		setPreferredSize(new Dimension(102, 52));

		// Add border
		setBorder(new LineBorder(Color.green));

		// Set panel to be opaque.
		setOpaque(true);

		// Set background to black
		setBackground(Color.black);
	}

	/**
	 * Update this panel.
	 */
	public void update() {
		if (!uiDone)
			initializeUI();
		
		repaint();
	}

	/**
	 * Override paintComponent method
	 * 
	 * @param g graphics context
	 */
	public void paintComponent(Graphics g) {

		super.paintComponent(g);

		// Get component height and width
		int height = getHeight();
		int width = getWidth();
		int centerX = width / 2;
		int centerY = height / 2;

		// Find terrain grade.
		double terrainGrade = 0D;
		if (vehicle.haveStatusType(StatusType.MOVING))
			terrainGrade = vehicle.getTerrainGrade();

		// Determine y difference
		int opp = (int) Math.round(75D * Math.sin(terrainGrade));

		// Set polygon coordinates
		int[] xPoints = { 0, 0, width - 1, width - 1, 0 };
		int[] yPoints = { height - 1, centerY + opp, centerY - opp, height - 1, height - 1 };
		Polygon terrainShape = new Polygon(xPoints, yPoints, 5);

		// Draw polygon in green
		g.setColor(Color.green);
		g.fillPolygon(terrainShape);

		// Draw direction arrow
		int arrowCenterY = centerY - (height / 4);
		g.drawLine(centerX - 10, arrowCenterY, centerX + 10, arrowCenterY);
		g.drawLine(centerX + 10, arrowCenterY, centerX + 5, arrowCenterY + 5);
		g.drawLine(centerX + 10, arrowCenterY, centerX + 5, arrowCenterY - 5);
	}

	public void destroy() {
		vehicle = null;
	}
}
