//************************** Maintenance Garage Facility Panel **************************
// Last Modified: 5/22/00

// The MaintenanceGarageFacilityPanel class displays information about a settlement's maintenance garage facility in the user interface.

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

public class MaintenanceGarageFacilityPanel extends FacilityPanel {

	// Data members
	
	private MaintenanceGarageFacility maintenanceGarage;  // The maintenance garage facility this panel displays.
	
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
		add(contentPane, "North");
		
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
		
		JLabel maxSizeLabel = new JLabel("Maximum Vehicle Size Allowable: " + maintenanceGarage.getMaxVehicleSize(), JLabel.CENTER);
		maxSizeLabel.setForeground(Color.black);
		labelPane.add(maxSizeLabel);
		
		// Prepare size capacity label
		
		JLabel sizeCapacityLabel = new JLabel("Vehicle Size Total Capacity: " + maintenanceGarage.getMaxSizeCapacity(), JLabel.CENTER);
		sizeCapacityLabel.setForeground(Color.black);
		labelPane.add(sizeCapacityLabel);
		
		// Prepare size total label
		
		JLabel sizeTotalLabel = new JLabel("Vehicle Size Total Currently: " + maintenanceGarage.getTotalSize(), JLabel.CENTER);
		sizeTotalLabel.setForeground(Color.black);
		labelPane.add(sizeTotalLabel);
		
		// Prepare parked vehicles pane
		
		JPanel parkedVehiclesPane = new JPanel(new BorderLayout());
		parkedVehiclesPane.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
		infoPane.add(parkedVehiclesPane, "Center");
		
		// Prepare parked name label
		
		JLabel parkedVehiclesLabel = new JLabel("Vehicles Parked In Garage:", JLabel.CENTER);
		parkedVehiclesLabel.setForeground(Color.black);
		parkedVehiclesPane.add(parkedVehiclesLabel, "North");
		
		// Prepare vehicle list pane
		
		JPanel vehicleListPane = new JPanel();
		parkedVehiclesPane.add(vehicleListPane, "Center");
		
		// Prepare inner vehicle list pane
		
		JPanel innerVehicleListPane = new JPanel(new BorderLayout());
		innerVehicleListPane.setPreferredSize(new Dimension(150, 100));
		vehicleListPane.add(innerVehicleListPane);
		
		// Prepare vehicle list
		
		String[] vehicleNames = maintenanceGarage.getVehicleNames();
		DefaultListModel vehicleListModel = new DefaultListModel();
		for (int x=0; x < vehicleNames.length; x++) vehicleListModel.addElement(vehicleNames[x]);
		JList vehicleList = new JList(vehicleListModel);
		vehicleList.setVisibleRowCount(6);
		// vehicleList.addMouseListener(this);
		innerVehicleListPane.add(new JScrollPane(vehicleList), "Center");
	}
	
	// Updates the facility panel's information
	
	public void updateInfo() { 
		// Implement later	
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