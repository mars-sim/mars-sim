//********************* Display Component for Surface Map *****************
// Last Modified: 2/22/00

// The MapDisplay class is a display component for the surface map of Mars in
// the project UI.  It can show either the surface or topographical maps at
// a given point.  It uses two SurfaceMap objects to display the maps.

// It will recenter the map on the location of a mouse click, or will alternatively
// open a vehicle or settlement window if one of their icons is clicked.

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

public class MapDisplay extends JComponent implements MouseListener, Runnable {

	// Data members

	private NavigatorWindow navWindow;        // Navigator Tool Window
	private SurfaceMap marsSurface;           // Surface image object
	private SurfaceMap topoSurface;           // Topographical image object
	private boolean Wait;                     // True if map is in pause mode
	private Coordinates centerCoords;         // Spherical coordinates for center point of map
	private Thread showThread;                // Refresh thread
	private boolean topo;                     // True if in topographical mode, false if in real surface mode
	private boolean recreate;                 // True if surface needs to be regenerated
	private boolean labels;                   // True if units should display labels
	private Image mapImage;	                  // Main image
	private Image vehicleSymbol;              // Real vehicle symbol
	private Image topoVehicleSymbol;          // Topograhical vehicle symbol
	private Image settlementSymbol;           // Real settlement symbol
	private Image topoSettlementSymbol;       // Topographical settlement symbol

	// Constructor

	public MapDisplay(NavigatorWindow navWindow) {

		// Use JComponent constructor

		super();
		
		// Set component size
		
		setPreferredSize(new Dimension(300, 300));
		setMaximumSize(getPreferredSize());
		setMinimumSize(getPreferredSize());
		
		// Set mouse listener
		
		addMouseListener(this);

		// Create surface objects for both real and topographical modes

		marsSurface = new SurfaceMap("surface", this);
		topoSurface = new SurfaceMap("topo", this);

		// Initialize global variables
		
		centerCoords = new Coordinates(Math.PI / 2D, 0D);
		Wait = false;
		recreate = true;
		topo = false;
		labels = true;
		this.navWindow = navWindow;
		
		// Load vehicle and settlement images
		
		vehicleSymbol = (Toolkit.getDefaultToolkit()).getImage("VehicleSymbol.gif"); 
		topoVehicleSymbol = (Toolkit.getDefaultToolkit()).getImage("VehicleSymbolBlack.gif");
		settlementSymbol = (Toolkit.getDefaultToolkit()).getImage("SettlementSymbol.gif");
		topoSettlementSymbol = (Toolkit.getDefaultToolkit()).getImage("SettlementSymbolBlack.gif");
		
		// Initially show real surface map
		
		showReal();
	}
	
	// Change label display flag
	
	public void setLabels(boolean labels) { this.labels = labels; }

	// Displays real surface

	public void showReal() {
		if (topo) {
			Wait = true;
			recreate = true;
		}
		topo = false;
		showMap(centerCoords);
	}

	// Displays topographical surface

	public void showTopo() {
		if (!topo) {
			Wait = true;
			recreate = true;
		}
		topo = true;
		showMap(centerCoords);
	}

	// Displays surface with new coords, regenerating image if necessary

	public void showMap(Coordinates newCenter) {

		if (!centerCoords.equals(newCenter)) {
			Wait = true;
			recreate = true;
			centerCoords.setCoords(newCenter);
		}
		start();
	}

	// Starts display update thread, and creates a new one if necessary

	public void start() {
		if ((showThread == null) || (!showThread.isAlive())) {
			showThread = new Thread(this, "Map");
			showThread.start();
		}
	}

	// Display update thread runner

	public void run() {

		while(true) { // Endless refresh loop

			if (recreate) {

				// Regenerate surface if recreate is true, then display

				if (topo) topoSurface.drawMap(centerCoords);
				else marsSurface.drawMap(centerCoords);
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
	// Displays map image or "Preparing Map..." message

	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		if (Wait) {
			// If in waiting mode, display "Preparing Map..."
			
			if (mapImage != null) g.drawImage(mapImage, 0, 0, this);
			g.setColor(Color.green);
			String message = new String("Preparing Map...");
			Font alertFont = new Font("TimesRoman", Font.BOLD, 30);
			FontMetrics alertMetrics = getFontMetrics(alertFont);
			int Height = alertMetrics.getHeight();
			int Width = alertMetrics.stringWidth(message);
			int x = (300 - Width) / 2;
			int y = (300 + Height) / 2;
			g.setFont(alertFont);
			g.drawString(message, x, y);
			Wait = false;
		}
		else { 
			// Paint black background
		
			g.setColor(Color.black);
			g.fillRect(0, 0, 300, 300);
			
			// Paint topo or real surface image

			boolean image_done = false;
			SurfaceMap tempMap;
			if (topo) tempMap = topoSurface;
			else tempMap = marsSurface;
			
			if (tempMap.image_done) {
				image_done = true;
				mapImage = tempMap.getMapImage();
				g.drawImage(mapImage, 0, 0, this);
			}
			
			// Set unit label color
			
			if (topo) g.setColor(Color.black);
			else g.setColor(Color.green);
			
			// Draw a vehicle symbol for each moving vehicle within the viewing map
			
			g.setFont(new Font("Helvetica", Font.PLAIN, 9));
	
			UnitInfo[] vehicleInfo = navWindow.getMovingVehicleInfo();
			
			int counter = 0;
			
			for (int x=0; x < vehicleInfo.length; x++) {
				if (centerCoords.getAngle(vehicleInfo[x].getCoords()) < .48587D) {
					int[] rectLocation = getUnitRectPosition(vehicleInfo[x].getCoords());
					int[] imageLocation = getUnitDrawLocation(rectLocation, vehicleSymbol);
					if (topo) g.drawImage(topoVehicleSymbol, imageLocation[0], imageLocation[1], this);
					else g.drawImage(vehicleSymbol, imageLocation[0], imageLocation[1], this);
					if (labels) {
						int[] labelLocation = getLabelLocation(rectLocation, vehicleSymbol);
						g.drawString(vehicleInfo[x].getName(), labelLocation[0], labelLocation[1]);	
					}
					counter++;
				}
			}
			
			// Draw a settlement symbol for each settlement within the viewing map
			
			g.setFont(new Font("Helvetica", Font.PLAIN, 12));
			
			UnitInfo[] settlementInfo = navWindow.getSettlementInfo();
			
			for (int x=0; x < settlementInfo.length; x++) {
				if (centerCoords.getAngle(settlementInfo[x].getCoords()) < .48587D) {
					int[] rectLocation = getUnitRectPosition(settlementInfo[x].getCoords());
					int[] imageLocation = getUnitDrawLocation(rectLocation, settlementSymbol);
					if (topo) g.drawImage(topoSettlementSymbol, imageLocation[0], imageLocation[1], this);
					else g.drawImage(settlementSymbol, imageLocation[0], imageLocation[1], this);
					if (labels) {
						int[] labelLocation = getLabelLocation(rectLocation, settlementSymbol);
						g.drawString(settlementInfo[x].getName(), labelLocation[0], labelLocation[1]);
					}
				}
			}
		}
	}

	// MouseListener methods overridden

	// Perform appropriate action on mouse release.

	public void mouseReleased(MouseEvent event) { 

		Coordinates clickedPosition = centerCoords.convertRectToSpherical((double) event.getX() - 149D, (double) event.getY() - 149D);
		boolean unitsClicked = false;
		
		UnitInfo[] movingVehicleInfo = navWindow.getMovingVehicleInfo();
		
		for (int x=0; x < movingVehicleInfo.length; x++) {
			if (movingVehicleInfo[x].getCoords().getDistance(clickedPosition) < 40D) {
				navWindow.openUnitWindow(movingVehicleInfo[x].getID());
				unitsClicked = true;
			}
		}
		
		UnitInfo[] settlementInfo = navWindow.getSettlementInfo();
		
		for (int x=0; x < settlementInfo.length; x++) {
			if (settlementInfo[x].getCoords().getDistance(clickedPosition) < 90D) {
				navWindow.openUnitWindow(settlementInfo[x].getID());
				unitsClicked = true;
			}
		}
		
		if (!unitsClicked) navWindow.updateCoords(clickedPosition);
	}

	public void mousePressed(MouseEvent event) {}
	public void mouseClicked(MouseEvent event) {}
	public void mouseEntered(MouseEvent event) {}
	public void mouseExited(MouseEvent event) {}
	
	// Returns unit x, y position on map panel
	
	private int[] getUnitRectPosition(Coordinates unitCoords) {
		
		double rho = 1440D / Math.PI;
		int half_map = 720;
		int low_edge = half_map - 150;
		int[] result = Coordinates.findRectPosition(unitCoords, centerCoords, rho, half_map, low_edge);
		
		return result;
	}
	
	// Returns unit image draw position on map panel
	
	private int[] getUnitDrawLocation(int[] unitPosition, Image unitImage) {
		
		int[] result = new int[2];
		
		result[0] = unitPosition[0] - Math.round(unitImage.getWidth(this) / 2);
		result[1] = unitPosition[1] - Math.round(unitImage.getHeight(this) / 2);
		
		return result;
	}
	
	// Returns label draw postion on map panel
	
	private int[] getLabelLocation(int[] unitPosition, Image unitImage) {
		
		int[] result = new int[2];
		
		result[0] = unitPosition[0] + Math.round(unitImage.getWidth(this) / 2) + 10;
		result[1] = unitPosition[1] + Math.round(unitImage.getHeight(this) / 2);
		
		return result;
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