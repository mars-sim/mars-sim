//***************** Vehicle Direction Display *****************
// Last Modified: 2/27/00

// The VehicleDirectionDisplay class displays a UI graph of the direction of travel
// for a ground vehicle's detail window.

import java.awt.*;
import javax.swing.*;

public class VehicleDirectionDisplay extends JComponent {

	// Data members

	private double direction;  // Direction of travel (0 = north, clockwise)
	private boolean showLine;  // True if direction line is to be shown

	// Constructor

	public VehicleDirectionDisplay(double direction, boolean park) {
		super();
		
		// Set component size
		
		setPreferredSize(new Dimension(50, 50));
		setMaximumSize(getPreferredSize());
		setMinimumSize(getPreferredSize());

		// Initialize data members

		this.direction = direction;
		if (park) showLine = false;
		else showLine = true;
	}

	// Update direction and redraw if necessary

	public void updateDirection(double newDirection, boolean park) {

		if (newDirection != direction) {
			direction = newDirection;
			if (park) showLine = false;
			else showLine = true;
			
			repaint();
		}
	}

	// Override paintComponent method

	public void paintComponent(Graphics g) {

		// Draw black background

		g.setColor(Color.black);
		g.fillRect(0, 0, 50, 50);

		// Draw dark green background circle 

		g.setColor(new Color(0, 62, 0));
		g.fillOval(3, 3, 43, 43);

		// Draw bright green out circle

		g.setColor(Color.green);
		g.drawOval(3, 3, 43, 43);

		// Draw center dot

		g.drawRect(25, 25, 1, 1);

		// Prepare letter font

		Font tempFont = new Font("Helvetica", Font.PLAIN, 9);
		g.setFont(tempFont);
		FontMetrics tempMetrics = getFontMetrics(tempFont);
		int fontHeight = tempMetrics.getAscent();

		// Draw 'N'

		int nWidth = tempMetrics.charWidth('N');
		g.drawString("N", 25 - (nWidth / 2), (fontHeight / 2) + 10);

		// Draw 'S'

		int sWidth = tempMetrics.charWidth('S');
		g.drawString("S", 25 - (sWidth / 2), 39 + (fontHeight / 2));

		// Draw 'W'

		int wWidth = tempMetrics.charWidth('W');
		g.drawString("W", 10 - (wWidth / 2), 25 + (fontHeight / 2));

		// Draw 'E'

		int eWidth = tempMetrics.charWidth('E');
		g.drawString("E", 39 - (eWidth / 2), 25 + (fontHeight / 2));

		// Draw direction line if necessary

		if (showLine) {
			double hyp = (double)(22);
			int newX = (int)Math.round(hyp * Math.sin(direction));
			int newY = -1 * (int)Math.round(hyp * Math.cos(direction));
			g.drawLine(25, 25, newX + 25, newY + 25);
		}
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