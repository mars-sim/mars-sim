/**
 * Mars Simulation Project
 * VehicleTerrainDisplay.java
 * @version 2.71 2000-10-23
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard; 

import java.awt.*;
import javax.swing.*;

/** The VehicleTerrainDisplay class displays a UI graph of the terrain grade
 *  in front of a ground vehicle based on its current direction.
 */
public class VehicleTerrainDisplay extends JComponent {

	// Data members
	private double terrainAngle;  // Current terrain average grade angle
	
	/** Constructs a VehicleTerrainDisplay object */
	public VehicleTerrainDisplay() {
		super();
		
		// Set component size
		setPreferredSize(new Dimension(100, 50));
		setMaximumSize(getPreferredSize());
		setMinimumSize(getPreferredSize());

		// Initialize terrain angle
		terrainAngle = 0D;
	}

	/** Update terrain angle displayed if necessary 
     *  @param angle the terrain angle
     */
	public void updateTerrainAngle(double angle) {

		if (angle != terrainAngle) {
			terrainAngle = angle;
			repaint();
		}
	}

	/** Override paintComponent method 
     *  @param g graphics context
     */
	public void paintComponent(Graphics g) {

		// Set background to black
		g.setColor(Color.black);
		g.fillRect(0, 0, 100, 50);

		// Determine y difference 
		int opp = (int) Math.round(75D * Math.sin(terrainAngle));

		// Set polygon coordinates
		int[] xPoints = { 0, 0, 100, 100, 0 };
		int[] yPoints = { 50, 25 + opp, 25 - opp, 50, 50 };

		// Draw polygon in green
		g.setColor(Color.green);
		g.fillPolygon(xPoints, yPoints, 5);

		// Draw direction arrow
		g.drawLine(40, 12, 60, 12);
		g.drawLine(60, 12, 55, 17);
		g.drawLine(60, 12, 55, 7);
	}
}
