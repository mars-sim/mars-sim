//************************** Maintenance Garage Facility Panel **************************
// Last Modified: 8/29/00

// The MaintenanceGarageFacilityPanel class displays information about a settlement's maintenance garage facility in the user interface.

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

public class MaintenanceGarageFacilityPanel extends FacilityPanel implements ActionListener {

	// Data members
	
	private MaintenanceGarageFacility maintenanceGarage;  // The maintenance garage facility this panel displays.
	private Vector parkedVehicles;       // A vector of vehicles currently in the garage.
	private Vector vehicleButtons;       // A vector of vehicle buttons.
	private Vector vehicleProgressBars;  // A vector of vehicle progress bars.
	private JPanel vehicleGridPane;      // A UI panel that contains the vehicle buttons and progress bars.
	private JLabel sizeTotalLabel;       // A label showing the current total of the vehicles sizes in the garage.
	
	// Cached garage data
	
	private int currentTotalSize;        // The current total size of vehicles in the garage.
	
	// Constructor
	
	public MaintenanceGarageFacilityPanel(MaintenanceGarageFacility maintenanceGarage, MainDesktopPane desktop) {
	
		// Use FacilityPanel's constructor
		
		super(desktop);
		
		// Initialize data members
		
		this.maintenanceGarage = maintenanceGarage;
		tabName = "Garage";
		
		// Set up components
		
		setLayout(new BorderLayout());
		
		// Prepare content pane
		
		JPanel contentPane = new JPanel(new BorderLayout(0, 5));
		add(contentPane, "Center");
		
		// Prepare name label
		
		JLabel nameLabel = new JLabel("Maintenance Garage", JLabel.CENTER);
		nameLabel.setForeground(Color.black);
		contentPane.add(nameLabel, "North");
		
		// Prepare info pane
		
		JPanel infoPane = new JPanel(new BorderLayout(0, 5));
		contentPane.add(infoPane, "Center");
		
		// Prepare label pane
		
		JPanel labelPane = new JPanel(new GridLayout(3, 1, 0, 5));
		labelPane.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
		infoPane.add(labelPane, "North");
		
		// Prepare max size label
		
		JLabel maxSizeLabel = new JLabel("Maximum Vehicle Size Allowed: " + maintenanceGarage.getMaxVehicleSize(), JLabel.CENTER);
		maxSizeLabel.setForeground(Color.black);
		labelPane.add(maxSizeLabel);
		
		// Prepare size capacity label
		
		JLabel sizeCapacityLabel = new JLabel("Total Vehicle Size Capacity: " + maintenanceGarage.getMaxSizeCapacity(), JLabel.CENTER);
		sizeCapacityLabel.setForeground(Color.black);
		labelPane.add(sizeCapacityLabel);
		
		// Prepare size total label
		
		currentTotalSize = maintenanceGarage.getTotalSize();
		sizeTotalLabel = new JLabel("Current Total Vehicle Size: " + currentTotalSize, JLabel.CENTER);
		sizeTotalLabel.setForeground(Color.black);
		labelPane.add(sizeTotalLabel);
		
		// Prepare parked vehicles pane
		
		JPanel parkedVehiclesPane = new JPanel(new BorderLayout());
		parkedVehiclesPane.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
		infoPane.add(parkedVehiclesPane, "Center");
		
		// Prepare parked name label
		
		JLabel parkedVehiclesLabel = new JLabel("Vehicles Undergoing Maintenance:", JLabel.CENTER);
		parkedVehiclesLabel.setForeground(Color.black);
		parkedVehiclesPane.add(parkedVehiclesLabel, "North");
		
		// Prepare vehicle list pane
		
		JPanel vehicleListPane = new JPanel(new BorderLayout());
		parkedVehiclesPane.add(new JScrollPane(vehicleListPane), "Center");
	
		// Create parked vehicles vector and vehicle buttons vector.
		
		parkedVehicles = new Vector();
		Vehicle[] parkedVehicleArray = maintenanceGarage.getVehicles();
		for (int x=0; x < parkedVehicleArray.length; x++) parkedVehicles.addElement(parkedVehicleArray[x]);
		
		// Prepare vehicle grid pane.
		
		vehicleGridPane = new JPanel(new GridLayout(parkedVehicles.size(), 1, 0, 5));
		vehicleListPane.add(vehicleGridPane, "North");
	
		// Add vehicles.
		
		vehicleButtons = new Vector();
		vehicleProgressBars = new Vector();
		for (int x=0; x < parkedVehicles.size(); x++) {
			
			Vehicle vehicle = (Vehicle) parkedVehicles.elementAt(x);
			
			JPanel vehiclePane = new JPanel(new BorderLayout());
			vehicleGridPane.add(vehiclePane);
			
			JButton vehicleButton = new JButton(vehicle.getName());
			vehicleButton.addActionListener(this);
			vehiclePane.add(vehicleButton, "West");
			vehicleButtons.addElement(vehicleButton);
			
			JProgressBar vehicleProgressBar = new JProgressBar();
			vehicleProgressBar.setStringPainted(true);
			vehicleProgressBar.setPreferredSize(new Dimension(100, 0));
			
			int maintenanceProgress = 0;
			float currentWork = (float) vehicle.getCurrentMaintenanceWork();
			float totalWork = (float) vehicle.getTotalMaintenanceWork();
			maintenanceProgress = (int) (100F * (currentWork / totalWork));
			
			vehicleProgressBar.setValue(maintenanceProgress);
			vehiclePane.add(vehicleProgressBar, "East");
			vehicleProgressBars.addElement(vehicleProgressBar);
		}
	}
	
	// ActionListener method overriden
	
	public void actionPerformed(ActionEvent event) {
		
		Object button = event.getSource();
			
		// Check to see if button is a vehicle button.
		
		for (int x=0; x < parkedVehicles.size(); x++) {
			if (button == vehicleButtons.elementAt(x)) {
				Vehicle vehicle = (Vehicle) parkedVehicles.elementAt(x);
				desktop.openUnitWindow(vehicle.getID());
			}
		}
	}
	
	// Updates the facility panel's information
	
	public void updateInfo() { 
		
		// Change vehicle size total currently if necessary.
		
		if (currentTotalSize != maintenanceGarage.getTotalSize()) {
			currentTotalSize = maintenanceGarage.getTotalSize();
			sizeTotalLabel.setText("Current Total Vehicle Size: " + currentTotalSize);
		}
		
		// Get vehicles in garage array.
		
		Vehicle[] parkedVehicleArray = maintenanceGarage.getVehicles();
		
		// Check if array matches parkedVehicles vector.
		
		boolean match = false;
		if (parkedVehicleArray.length == parkedVehicles.size()) {
			match = true;
			for (int x=0; x < parkedVehicleArray.length; x++) 
				if ((Vehicle) parkedVehicles.elementAt(x) != parkedVehicleArray[x]) match = false;
		}
		
		// If a match, update vehicle progress bars.
		
		if (match) {
			for (int x=0; x < parkedVehicles.size(); x++) {
				
				Vehicle vehicle = (Vehicle) parkedVehicles.elementAt(x);
				
				int maintenanceProgress = 0;
				float currentWork = (float) vehicle.getCurrentMaintenanceWork();
				float totalWork = (float) vehicle.getTotalMaintenanceWork();
				maintenanceProgress = (int) (100F * (currentWork / totalWork));
				
				((JProgressBar) vehicleProgressBars.elementAt(x)).setValue(maintenanceProgress);
			}
		}
		else {
			// If not a match, update parkedVehicles vector and UI.
			// Clear vectors.
			
			parkedVehicles.removeAllElements();
			vehicleButtons.removeAllElements();
			vehicleProgressBars.removeAllElements();
			
			// Populate parkedVehicles vector.
			
			for (int x=0; x < parkedVehicleArray.length; x++) parkedVehicles.addElement(parkedVehicleArray[x]);
			
			// Set up vehicle grid pane layout.
			
			vehicleGridPane.removeAll();
			vehicleGridPane.setLayout(new GridLayout(parkedVehicles.size(), 1, 0, 5));
			
			// Add vehicle UI's
			
			for (int x=0; x < parkedVehicles.size(); x++) {
				
				Vehicle vehicle = (Vehicle) parkedVehicles.elementAt(x);
				
				JPanel vehiclePane = new JPanel(new BorderLayout());
				vehicleGridPane.add(vehiclePane);
			
				JButton vehicleButton = new JButton(vehicle.getName());
				vehicleButton.addActionListener(this);
				vehiclePane.add(vehicleButton, "West");
				vehicleButtons.addElement(vehicleButton);
				
				JProgressBar vehicleProgressBar = new JProgressBar();
				vehicleProgressBar.setStringPainted(true);
				vehicleProgressBar.setPreferredSize(new Dimension(100, 0));
				
				int maintenanceProgress = 0;
				float currentWork = (float) vehicle.getCurrentMaintenanceWork();
				float totalWork = (float) vehicle.getTotalMaintenanceWork();
				maintenanceProgress = (int) (100F * (currentWork / totalWork));
				
				vehicleProgressBar.setValue(maintenanceProgress);
				vehiclePane.add(vehicleProgressBar, "East");
				vehicleProgressBars.addElement(vehicleProgressBar);
			}
			
			// Validate panel
			
			getParent().validate();
		}
	}
}	

// Mars Simulation Project
// Copyright (C) 2000 Scott Davis
//
// For questions or comments on this project, email:
// mars-sim-users@lists.sourceforge.net
//
// or visit the project's Web site at:
// http://mars-sim@sourceforge.net
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