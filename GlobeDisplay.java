//******************* Small Globe of Mars *******************
// Last Modified: 2/21/00

// The Globe Display class displays a graphical globe of Mars in the
// "Mars Navigator" tool.

import java.awt.*;
import java.util.*;
import javax.swing.*;

class GlobeDisplay extends JComponent implements Runnable {

	// Data members

	private NavigatorWindow navWindow;      // Navigator Tool Window
	private MarsGlobe marsSphere;           // Real surface sphere object
	private MarsGlobe topoSphere;           // Topographical sphere object
	private Coordinates centerCoords;       // Spherical coordinates for globe center
	private Thread showThread;              // Refresh thread
	private boolean topo;                   // True if in topographical mode, false if in real surface mode
	private boolean recreate;               // True if globe needs to be regenerated

	// Constructor

	public GlobeDisplay(NavigatorWindow navWindow) {

		super();
		
		// Set component size
		
		setPreferredSize(new Dimension(150, 150));
		setMaximumSize(getPreferredSize());
		setMinimumSize(getPreferredSize());

		// Construct sphere objects for both real and topographical modes

		marsSphere = new MarsGlobe("surface", this);
		topoSphere = new MarsGlobe("topo", this); 

		// Initialize global variables

		centerCoords = new Coordinates(Math.PI / 2D, 0D);
		topo = false;
		recreate = true;
		this.navWindow = navWindow;
		
		// Initially show real surface globe
		
		showReal();
	}

	// Displays real surface globe, regenerating if necessary

	public void showReal() {
		if (topo) recreate = true;
		topo = false;
		showGlobe(centerCoords);
	}
	
	// Displays topographical globe, regenerating if necessary

	public void showTopo() {
		if (!topo) recreate = true;
		topo = true;
		showGlobe(centerCoords);
	}

	// Displays globe at given center regardless of mode, regenerating if necessary

	public void showGlobe(Coordinates newCenter) {

		if (!centerCoords.equals(newCenter)) {
			recreate = true;
			centerCoords.setCoords(newCenter);
		}
		start();
	}

	// Starts display update thread, and creates a new one if necessary

	public void start() {
		if ((showThread == null) || (!showThread.isAlive())) {
			showThread = new Thread(this, "Globe");
			showThread.start();
		}
	}

	// Display update thread runner

	public void run() {

		while(true) {  // Endless refresh loop

			if (recreate) {

				// Regenerate globe if recreate is true, then display

				if (topo) topoSphere.drawSphere(centerCoords);
				else marsSphere.drawSphere(centerCoords);
				recreate = false;
				repaint();
			}
			else {

				// Pause for 2 seconds between display refreshs

				try { showThread.sleep(2000); }
				catch (InterruptedException e) {}
				repaint();
			}
		}
	}	

	// Overrides paintComponent method
	// Displays globe, green lines, longitude and latitude

	public void paintComponent(Graphics g) {

		// Paint black background

		g.setColor(Color.black);
		g.fillRect(0, 0, 150, 150);

		// Draw real or topo globe

		boolean image_done = false;
		MarsGlobe tempGlobe;
		if (topo) tempGlobe = topoSphere;
		else tempGlobe = marsSphere;
			
		if (tempGlobe.image_done) {
			image_done = true;
			g.drawImage(tempGlobe.getGlobeImage(), 0, 0, this);
		}

		// Display dots for moving vehicles

		if (topo) g.setColor(Color.black);
		else g.setColor(Color.white);
		
		UnitInfo[] vehicleInfo = navWindow.getMovingVehicleInfo();
		for (int x=0; x < vehicleInfo.length; x++) {
			if (centerCoords.getAngle(vehicleInfo[x].getCoords()) < (Math.PI / 2D)) {
				int[] tempLocation = getUnitDrawLocation(vehicleInfo[x].getCoords());
				g.fillRect(tempLocation[0], tempLocation[1], 1, 1);
			}
		}

		// Display dots for settlements

		if (!topo) g.setColor(Color.green);
		
		UnitInfo[] settlementInfo = navWindow.getSettlementInfo();
		for (int x=0; x < settlementInfo.length; x++) {
			if (centerCoords.getAngle(settlementInfo[x].getCoords()) < (Math.PI / 2D)) {
				int[] tempLocation = getUnitDrawLocation(settlementInfo[x].getCoords());
				g.fillRect(tempLocation[0], tempLocation[1], 1, 1);
			}
		}

		// Draw green rectanges and lines

		g.setColor(Color.green);
		
		g.drawRect(57, 57, 31, 31);
		g.drawLine(0, 73, 57, 73);
		g.drawLine(90, 73, 149, 73);
		g.drawLine(73, 0, 73, 57);
		g.drawLine(73, 90, 73, 149);

		// Prepare font

		Font positionFont = new Font("Helvetica", Font.PLAIN, 10);
		FontMetrics positionMetrics = getFontMetrics(positionFont);
		g.setFont(positionFont);

		// Draw longitude and latitude strings

		int leftWidth = positionMetrics.stringWidth("Latitude:");
		int rightWidth = positionMetrics.stringWidth("Longitude:");

		g.drawString("Latitude:", 5, 130);
		g.drawString("Longitude:", 145 - rightWidth, 130);

		String latString = centerCoords.getFormattedLatitudeString();
		String longString = centerCoords.getFormattedLongitudeString();
		
		int latWidth = positionMetrics.stringWidth(latString);
		int longWidth = positionMetrics.stringWidth(longString);

		int latPosition = ((leftWidth - latWidth) / 2) + 5;
		int longPosition = 145 - rightWidth + ((rightWidth - longWidth) / 2);

		g.drawString(latString, latPosition, 142);
		g.drawString(longString, longPosition, 142);
	}
	
	// Returns unit x, y position on globe panel
	
	private int[] getUnitDrawLocation(Coordinates unitCoords) {
		double rho = 150D / Math.PI;
		int half_map = 75;
		int low_edge = 0;
		return Coordinates.findRectPosition(unitCoords, centerCoords, rho, half_map, low_edge);
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