//****************** Display Component for Navigation Buttons ****************
// Last Modified: 2/22/00

// The NavButtonDisplay class is a component that displays and implements the 
// behavior of the navigation buttons which control the globe and map.

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

public class NavButtonDisplay extends JComponent implements MouseListener {

	// Constant data members

	static double degree = Math.PI / 180D; // Circular degree unit

	// Data members

	private NavigatorWindow parentNavigator;  // Parent NavigatorWindow
	private int buttonLight;                  // Button currently lit, -1 otherwise
	private Image navMain;                    // Unlit buttons image
	private Image[] lightUpButtons;           // Lit button images
	private Coordinates centerCoords;         // Current coordinates

	// Constructor

	public NavButtonDisplay(NavigatorWindow parentNavigator) {
		super();
		
		// Set component size
		
		setPreferredSize(new Dimension(150, 150));
		setMaximumSize(getPreferredSize());
		setMinimumSize(getPreferredSize());
		
		// Set mouse listener
		
		addMouseListener(this);

		// Initialize globals

		centerCoords = new Coordinates(Math.PI / 2D, 0D);
		buttonLight = -1;
		lightUpButtons = new Image[9];
		this.parentNavigator = parentNavigator;

		// Load Button Images

		Toolkit myToolkit = Toolkit.getDefaultToolkit();
		navMain = myToolkit.getImage("NavMain.gif");
		lightUpButtons[0] = myToolkit.getImage("NavMainPlus.gif");
		lightUpButtons[1] = myToolkit.getImage("NavNorth.gif");
		lightUpButtons[2] = myToolkit.getImage("NavSouth.gif");
		lightUpButtons[3] = myToolkit.getImage("NavEast.gif");
		lightUpButtons[4] = myToolkit.getImage("NavWest.gif");
		lightUpButtons[5] = myToolkit.getImage("NavNorthPlus.gif");
		lightUpButtons[6] = myToolkit.getImage("NavSouthPlus.gif");
		lightUpButtons[7] = myToolkit.getImage("NavEastPlus.gif");
		lightUpButtons[8] = myToolkit.getImage("NavWestPlus.gif");

		MediaTracker mtrack = new MediaTracker(this);
		
		mtrack.addImage(navMain, 0);
		for (int x = 0; x < 9; x++) mtrack.addImage(lightUpButtons[x], x+1);

		try { mtrack.waitForAll(); }
		catch (InterruptedException e) 
			{ System.out.println("NavButtonDisplay Media Tracker Error " + e); } 
	}
	
	// Update coordinates
	
	public void updateCoords(Coordinates newCenter) { centerCoords.setCoords(newCenter); }
	
	// Override paintComponent method
	// Paints buttons and lit button

	public void paintComponent(Graphics g) {

		// Paint black background
		
		g.setColor(Color.black);
		g.fillRect(0, 0, 150, 150);
		
		// Draw main button image
		
		g.drawImage(navMain, 0, 0, this);
		
		// Draw lit button over top
		
		if (buttonLight >= 0) g.drawImage(lightUpButtons[buttonLight], 0, 0, this);
	}
	
	// MouseListener methods overridden

	// Light navigation button on mouse press

	public void mousePressed(MouseEvent event) { lightButton(event.getX(), event.getY()); }

	// Perform appropriate action on mouse release.

	public void mouseReleased(MouseEvent event) { 

		unlightButtons();

		// Use Image Map Technique to Determine Which Button was Selected

		int spot = findHotSpot(event.getX(), event.getY());

		// Results Based on Button Selected

		switch(spot) {
			case 0: // Zoom Button
				parentNavigator.updateCoords(centerCoords);
				break;
			case 1: // Inner Top Arrow			
				centerCoords.setPhi(centerCoords.getPhi() - (5D * degree));
				if (centerCoords.getPhi() < 0D) centerCoords.setPhi(0D);
				break;
			case 2: // Inner Bottom Arrow
				centerCoords.setPhi(centerCoords.getPhi() + (5D * degree));
				if (centerCoords.getPhi() > Math.PI) centerCoords.setPhi(Math.PI);
				break;
			case 3: // Inner Right Arrow
				centerCoords.setTheta(centerCoords.getTheta() + (5D * degree));
				if (centerCoords.getTheta() > (2D * Math.PI)) centerCoords.setTheta(centerCoords.getTheta() - (2D * Math.PI));
				break;
			case 4: // Inner Left Arrow
				centerCoords.setTheta(centerCoords.getTheta() - (5D * degree));
				if (centerCoords.getTheta() < 0D) centerCoords.setTheta(centerCoords.getTheta() + (2D * Math.PI));
				break;
			case 5: // Outer Top Arrow
				centerCoords.setPhi(centerCoords.getPhi() - (30D * degree));
				if (centerCoords.getPhi() < 0D) centerCoords.setPhi(0D);
				break;
			case 6: // Outer Bottom Arrow
				centerCoords.setPhi(centerCoords.getPhi() + (30D * degree));
				if (centerCoords.getPhi() > Math.PI) centerCoords.setPhi(Math.PI);
				break;
			case 7: // Outer Right Arrow
				centerCoords.setTheta(centerCoords.getTheta() + (30D * degree));
				if (centerCoords.getTheta() >= (2D * Math.PI)) centerCoords.setTheta(centerCoords.getTheta() - (2D * Math.PI));
				break;
			case 8: // Outer Left Arrow
				centerCoords.setTheta(centerCoords.getTheta() - (30D * degree));
				if (centerCoords.getTheta() < 0D) centerCoords.setTheta(centerCoords.getTheta() + (2D * Math.PI));
				break;
		}

		// Reposition Globe If Non-Zoom Button is Selected

		if (spot > 0) parentNavigator.updateGlobeOnly(centerCoords); 	
	}

	public void mouseClicked(MouseEvent event) {}
	public void mouseEntered(MouseEvent event) {}
	public void mouseExited(MouseEvent event) {}
	
	// Light button if mouse is on button

	private void lightButton(int x, int y) {
		buttonLight = findHotSpot(x, y);
		if (buttonLight >= 0) { repaint(); }
	}
	
	// Unlight buttons if any are lighted

	private void unlightButtons() {
		if (buttonLight >= 0) {
			buttonLight = -1;
			repaint();
		}
	}

	// Returns button number if mouse is on button
	// Returns -1 if not on button
	// Uses rectangular image mapping

	private int findHotSpot(int x, int y) {

		Rectangle[] hotSpots = new Rectangle[9];
		hotSpots[0] = new Rectangle(45, 45, 60, 60);
		hotSpots[1] = new Rectangle(38, 16, 74, 21);
		hotSpots[2] = new Rectangle(38, 112, 74, 21);
		hotSpots[3] = new Rectangle(113, 38, 21, 74);
		hotSpots[4] = new Rectangle(17, 38, 21, 74);
		hotSpots[5] = new Rectangle(60, 0, 29, 14);
		hotSpots[6] = new Rectangle(60, 134, 29, 14);
		hotSpots[7] = new Rectangle(135, 61, 15, 28);
		hotSpots[8] = new Rectangle(0, 61, 15, 28);

		int result = -1;

		for (int i = 0; i < 9; i++) 
			if (hotSpots[i].contains(x, y)) result = i;

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