//***************** Terrain Terrain Display *****************
// Last Modified: 2/24/00

package org.mars_sim.msp.ui.standard; 

// The VehicleTerrainDisplay class displays a UI graph of the terrain grade
// in front of a ground vehicle based on its current direction.

import java.awt.*;
import javax.swing.*;

public class VehicleTerrainDisplay extends JComponent {

	// Data members

	private double terrainAngle;  // Current terrain average grade angle
	
	// Constructor

	public VehicleTerrainDisplay() {
		super();
		
		// Set component size
		
		setPreferredSize(new Dimension(100, 50));
		setMaximumSize(getPreferredSize());
		setMinimumSize(getPreferredSize());

		// Initialize terrain angle

		terrainAngle = 0D;
	}

	// Update terrain angle displayed if necessary

	public void updateTerrainAngle(double angle) {

		if (angle != terrainAngle) {
			terrainAngle = angle;
			repaint();
		}
	}

	// Override paintComponent method

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

// Mars Simulation Project
// Copyright (C) 1999 Scott Davis
//
// For questions or comments on this project, contact:
//
// Scott Davis
// 1725 W. Timber Ridge Ln. #6206
// Oak Creek, WI  53154
// scud1@execpc.com
// http://www.execpc.com/~scud1/
// 
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
