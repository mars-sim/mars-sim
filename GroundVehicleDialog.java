//******************** Ground Vehicle Detail Window ********************
// Last Modified: 8/27/00

// The GroundVehicleDialog class is the detail window for a ground vehicle.
// It is abstract and an appropriate detail window needs to be derived for 
// a particular type of ground vehicle. (See RoverDialog for an example)

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

public abstract class GroundVehicleDialog extends VehicleDialog {

	// Data members

	protected GroundVehicle groundVehicle;               // Ground vehicle related to this detail window
	protected VehicleTerrainDisplay terrainDisplay;      // Terrain average grade display
	protected VehicleDirectionDisplay directionDisplay;  // Direction display
	protected JLabel elevationLabel;                     // Elevation label
	
	// Cached ground vehicle data
	
	protected double elevation;                          // Cached elevation data
	protected double terrainGrade;                       // Cached terrain grade data
	protected double direction;                          // Cached direction data
	
	// Constructor

	public GroundVehicleDialog(MainDesktopPane parentDesktop, GroundVehicle groundVehicle) {
		
		// Use VehicleDialog constructor
		
		super(parentDesktop, groundVehicle);
	}

	// Initialize cached data members
	
	protected void initCachedData() {
		super.initCachedData();
		
		elevation = 0D;
		terrainGrade = 0D;
		direction = 0D;
		
	}

	// Override parent's method

	protected void setupComponents() {
		
		// Initialize ground vehicle
		
		groundVehicle = (GroundVehicle) parentUnit;
		
		super.setupComponents();
	}

	// Override setupNavigationPane

	protected JPanel setupNavigationPane() {
		
		// Prepare navigation pane
		
		JPanel navigationPane = super.setupNavigationPane();
		
		// Prepare elevation label
		
		double tempElevation = Math.round(groundVehicle.getElevation() * 100D) / 100D;
		elevationLabel = new JLabel("Elevation: " + tempElevation + " km.");
		elevationLabel.setForeground(Color.black);
		JPanel elevationLabelPane = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		elevationLabelPane.add(elevationLabel);
		navigationInfoPane.add(elevationLabelPane);
		
		// Prepare ground display pane
		
		JPanel groundDisplayPane = new JPanel();
		groundDisplayPane.setLayout(new BoxLayout(groundDisplayPane, BoxLayout.X_AXIS));
		groundDisplayPane.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
		navigationPane.add(groundDisplayPane);
		
		// Prepare terrain display
		
		terrainDisplay = new VehicleTerrainDisplay();
		JPanel terrainDisplayPane = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		terrainDisplayPane.setBorder(new CompoundBorder(new BevelBorder(BevelBorder.LOWERED), new LineBorder(Color.green)));
		terrainDisplayPane.setMaximumSize(new Dimension(106, 56));
		terrainDisplayPane.add(terrainDisplay);
		groundDisplayPane.add(terrainDisplayPane);
		
		// Add glue spacer
		
		groundDisplayPane.add(Box.createHorizontalGlue());
		
		// Prepare direction display
		
		directionDisplay = new VehicleDirectionDisplay(groundVehicle.getDirection(), (vehicle.getStatus().equals("Parked") || vehicle.getStatus().equals("Periodic Maintenance")));
		JPanel directionDisplayPane = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		directionDisplayPane.setBorder(new CompoundBorder(new BevelBorder(BevelBorder.LOWERED), new LineBorder(Color.green)));
		directionDisplayPane.setMaximumSize(new Dimension(56, 56));
		directionDisplayPane.add(directionDisplay);
		groundDisplayPane.add(directionDisplayPane);
		
		// Return navigation pane
		
		return navigationPane;
	}
	
	// Complete update (overridden)
	
	protected void generalUpdate() {
		super.generalUpdate();
		updateAveGrade();
		updateDirection();
		updateElevation();
	}

	// Update terrain display

	protected void updateAveGrade() {
		if (vehicle.getStatus().equals("Parked") || vehicle.getStatus().equals("Periodic Maintenance")) {
			if (terrainGrade != 0D) {
				terrainDisplay.updateTerrainAngle(0D);
				terrainGrade = 0D;
			}
		}
		else {
			double tempGrade = groundVehicle.getTerrainGrade();
			if (terrainGrade != tempGrade) {
				terrainDisplay.updateTerrainAngle(tempGrade);
				terrainGrade = tempGrade;
			}
		}
	}

	// Update direction display

	protected void updateDirection() {
		double tempDirection = groundVehicle.getDirection();
		if (direction != tempDirection) {
			directionDisplay.updateDirection(tempDirection, (groundVehicle.getStatus().equals("Parked") || groundVehicle.getStatus().equals("Periodic Maintenance")));
			direction = tempDirection;
		}
	}

	// Update elevation label

	protected void updateElevation() {
		double tempElevation = Math.round(groundVehicle.getElevation() * 100D) / 100D;
		if (elevation != tempElevation) {
			elevationLabel.setText("Elevation: " + tempElevation + " km.");
			elevation = tempElevation;
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

