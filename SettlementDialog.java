//************************** Settlement Detail Window **************************
// Last Modified: 5/22/00

// The SettlementDialog class is a detail window for a settlement.
// It displays information about the settlement and its status.

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

public class SettlementDialog extends UnitDialog implements MouseListener {

	// Data members

	private Settlement settlement;          // Settlement which the dialog window is about.
	private JList vehicleList;              // List of parked vehicles
	private FacilityPanel[] facilityPanes;  // Panes for each of the settlement's facilities.
	
	// Constructor

	public SettlementDialog(MainDesktopPane parentDesktop, Settlement settlement) {

		// Use UnitDialog constructor

		super(parentDesktop, settlement);
	}
	
	// Complete update (overridden)

	protected void generalUpdate() {
		
		updateVehicles();
		
		for (int x=0; x < facilityPanes.length;	x++) facilityPanes[x].updateInfo();
	}

	// Update vehicle list

	protected void updateVehicles() {
		
		DefaultListModel model = (DefaultListModel) vehicleList.getModel();
		boolean match = false;
		int numVehicles = settlement.getVehicleNum();
		
		// Check if vehicle list matches settlement's parked vehicles
		
		if (model.getSize() == numVehicles) {
			match = true;
			for (int x=0; x < numVehicles; x++) 
				if (!((String) model.getElementAt(x)).equals(settlement.getVehicle(x).getName())) match = false;
		}
		
		// If no match, update vehicle list
		
		if (!match) {
			model.removeAllElements();
			for (int x=0; x < numVehicles; x++) {
				Vehicle tempVehicle = settlement.getVehicle(x);
				if (tempVehicle != null) model.addElement(tempVehicle.getName());
			}
			validate();
		}
	}
	
	// Load image icon
	
	public ImageIcon getIcon() { return new ImageIcon("SettlementIcon.gif"); }

	// Implement MouseListener Methods
	
	public void mouseClicked(MouseEvent event) {
		
		if (event.getClickCount() >= 2) {
			int index = vehicleList.locationToIndex(event.getPoint());
			if (index > -1) {
				try { parentDesktop.openUnitWindow(settlement.getVehicle(index).getID()); }
				catch(NullPointerException e) {}
			}
		}
	}
	public void mousePressed(MouseEvent event) {}
	public void mouseReleased(MouseEvent event) {}
	public void mouseEntered(MouseEvent event) {}
	public void mouseExited(MouseEvent event) {}
	
	// Set window size
	
	protected Dimension setWindowSize() { return new Dimension(300, 410); }
	
	// Prepare new components
	
	protected void setupComponents() {
	
		super.setupComponents();

		// Initialize settlement

		settlement = (Settlement) parentUnit;
		
		// Prepare tab pane
		
		JTabbedPane tabPane = new JTabbedPane();
		mainPane.add(tabPane, "Center");
		
		// Prepare and add location pane
		
		tabPane.add(setupLocationPane(), "Location");
		
		// Prepare and add primary pane
		
		tabPane.add(setupVehiclesPane(), "Vehicles");
		
		// Prepare and add each facility pane
		
		facilityPanes = settlement.getFacilityManager().getFacilityPanels(parentDesktop);
		for (int x=0; x < facilityPanes.length; x++) tabPane.add(facilityPanes[x], facilityPanes[x].getTabName());
	}
	
	// Prepare vehicles pane
	
	protected JPanel setupVehiclesPane() {

		// Preapre primary pane
		
		JPanel vehiclesPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
		vehiclesPane.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));

		// Prepare inner vehicles pane
		
		JPanel innerVehiclesPane = new JPanel(new BorderLayout());
		vehiclesPane.add(innerVehiclesPane);

		// Prepare vehicle label
		
		JLabel vehicleLabel = new JLabel("Parked Vehicles:", JLabel.CENTER);
		vehicleLabel.setForeground(Color.black);
		innerVehiclesPane.add(vehicleLabel, "North");
		
		// Prepare vehicle list pane
		
		JPanel vehicleListPane = new JPanel();
		innerVehiclesPane.add(vehicleListPane, "Center");
		
		// Prepare inner vehicle list pane
		
		JPanel innerVehicleListPane = new JPanel(new BorderLayout());
		innerVehicleListPane.setPreferredSize(new Dimension(150, 100));
		vehicleListPane.add(innerVehicleListPane);

		// Prepare vehicle list
		
		DefaultListModel vehicleListModel = new DefaultListModel();
		for (int x=0; x < settlement.getVehicleNum(); x++) vehicleListModel.addElement(settlement.getVehicle(x).getName());
		vehicleList = new JList(vehicleListModel);
		vehicleList.setVisibleRowCount(6);
		vehicleList.addMouseListener(this);
		innerVehicleListPane.add(new JScrollPane(vehicleList), "Center");
		
		// Return primary pane
		
		return vehiclesPane;
	}
	
	// Prepare location pane
	
	protected JPanel setupLocationPane() {
		
		// Prepare main location pane
		
		JPanel mainLocationPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
		mainLocationPane.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
		
		// Prepare location pane
		
		JPanel locationPane = new JPanel(new BorderLayout());
		mainLocationPane.add(locationPane);

		// Preparing location label pane

		JPanel locationLabelPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
		locationPane.add(locationLabelPane, "North");

		// Prepare center map button
		
		centerMapButton = new JButton(new ImageIcon("CenterMap.gif"));
		centerMapButton.setMargin(new Insets(1, 1, 1, 1));
		centerMapButton.addActionListener(this);
		locationLabelPane.add(centerMapButton);
		
		// Prepare location label
		
		JLabel locationLabel = new JLabel("Location:", JLabel.CENTER);
		locationLabel.setForeground(Color.black);
		locationLabelPane.add(locationLabel);

		// Prepare location coordinates pane
		
		JPanel locationCoordsPane = new JPanel(new GridLayout(1, 2, 0, 0));
		locationPane.add(locationCoordsPane, "Center");

		// Prepare latitude label

		JLabel latitudeLabel = new JLabel("Latitude: " + settlement.getCoordinates().getFormattedLatitudeString(), JLabel.LEFT);
		latitudeLabel.setForeground(Color.black);
		locationCoordsPane.add(latitudeLabel);

		// Prepare longitude label

		JLabel longitudeLabel = new JLabel("Longitude: " + settlement.getCoordinates().getFormattedLongitudeString(), JLabel.LEFT);
		longitudeLabel.setForeground(Color.black);
		locationCoordsPane.add(longitudeLabel);
		
		// Return location pane
		
		return mainLocationPane;
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
