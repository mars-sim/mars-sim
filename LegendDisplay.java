//******************* Display Component for Topographical and Distance Legends ****************
// Last Modified: 2/21/00

// The LegendDisplay class is a UI class that represents a map legend in the 
// "Mars Navigator" tool.  It can either show a distance legend, or a color elevation
// chart for the topographical map.

import java.awt.*;
import javax.swing.*;

public class LegendDisplay extends JLabel {

	// Data members

	private ImageIcon legend;  // Image icon

	// Constructor

	public LegendDisplay() { 
		super(); 
		legend = new ImageIcon("Map_Legend.jpg");
		setIcon(legend);
	}

	// Change to topographical mode

	public void showColor() { 
		legend.setImage(Toolkit.getDefaultToolkit().getImage("Color_Legend.jpg"));
		repaint();
	}

	// Change to distance mode and refresh canvas

	public void showMap() {
		legend.setImage(Toolkit.getDefaultToolkit().getImage("Map_Legend.jpg"));
		repaint();
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