//************************** Settlement Detail Window **************************
// Last Modified: 4/9/00

// The SettlementDialog class is a detail window for a settlement.
// It displays information about the settlement and its status.

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

public class SettlementDialog extends UnitDialog implements MouseListener {

	// Data members

	protected Settlement settlement; // Settlement which the dialog window is about.
	protected JList peopleList;      // List of inhabitants
	protected JList vehicleList;     // List of parked vehicles
	
	// Constructor

	public SettlementDialog(MainDesktopPane parentDesktop, Settlement settlement) {

		// Use UnitDialog constructor

		super(parentDesktop, settlement);
	}
	
	// Complete update (overridden)

	protected void generalUpdate() {
		updatePeople();
		updateVehicles();
	}

	// Update people list

	protected void updatePeople() {
		
		DefaultListModel model = (DefaultListModel) peopleList.getModel();
		boolean match = false;
		
		// Check if people list matches settlement's population
		
		if (model.getSize() == settlement.getPeopleNum()) {
			match = true;
			for (int x=0; x < settlement.getPeopleNum(); x++) 
				if (!((String) model.getElementAt(x)).equals(settlement.getPerson(x).getName())) match = false;
		}
		
		// If no match, update people list
		
		if (!match) {
			model.removeAllElements();
			for (int x=0; x < settlement.getPeopleNum(); x++) {
				Person tempPerson = settlement.getPerson(x);
				if (tempPerson != null) model.addElement(tempPerson.getName());
			}
			validate();
		}
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
		Object object = event.getSource();

		if (object == peopleList) {
			if (event.getClickCount() >= 2) {
  	     			int index = peopleList.locationToIndex(event.getPoint());
  	     			if (index > -1) {
  	     				try { parentDesktop.openUnitWindow(settlement.getPerson(index).getID()); }
  	     				catch(NullPointerException e) {}
  	     			}
	 		}
		}
		else if (object == vehicleList) {
			if (event.getClickCount() >= 2) {
				int index = vehicleList.locationToIndex(event.getPoint());
				if (index > -1) {
					try { parentDesktop.openUnitWindow(settlement.getVehicle(index).getID()); }
					catch(NullPointerException e) {}
				}
			}
		}
	}
	public void mousePressed(MouseEvent event) {}
	public void mouseReleased(MouseEvent event) {}
	public void mouseEntered(MouseEvent event) {}
	public void mouseExited(MouseEvent event) {}
	
	// Set window size
	
	protected Dimension setWindowSize() { return new Dimension(300, 330); }
	
	// Prepare new components
	
	protected void setupComponents() {
	
		super.setupComponents();

		// initialize settlement

		settlement = (Settlement) parentUnit;
		
		// Prepare location pane
		
		JPanel locationPane = new JPanel(new BorderLayout());
		locationPane.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
		mainPane.add(locationPane);

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

		// Prepare vertical strut

		mainPane.add(Box.createVerticalStrut(10));

		// Prepare contents pane
		
		JPanel contentsPane = new JPanel(new GridLayout(1, 2, 0, 0));
		contentsPane.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
		mainPane.add(contentsPane);
		
		// Prepare inhabitants pane
		
		JPanel peoplePane = new JPanel(new BorderLayout());
		contentsPane.add(peoplePane);

		// Prepare people label
		
		JLabel peopleLabel = new JLabel("Inhabitants", JLabel.CENTER);
		peopleLabel.setForeground(Color.black);
		peoplePane.add(peopleLabel, "North");

		// Prepare people list
		
		DefaultListModel peopleListModel = new DefaultListModel();
		for (int x=0; x < settlement.getPeopleNum(); x++) peopleListModel.addElement(settlement.getPerson(x).getName());
		peopleList = new JList(peopleListModel);
		peopleList.setVisibleRowCount(7);
		peopleList.addMouseListener(this);
		peoplePane.add(new JScrollPane(peopleList), "Center");

		// Prepare vehicle pane
		
		JPanel vehiclePane = new JPanel(new BorderLayout());
		contentsPane.add(vehiclePane);

		// Prepare vehicle label
		
		JLabel vehicleLabel = new JLabel("Parked Vehicles", JLabel.CENTER);
		vehicleLabel.setForeground(Color.black);
		vehiclePane.add(vehicleLabel, "North");

		// Prepare vehicle list
		
		DefaultListModel vehicleListModel = new DefaultListModel();
		for (int x=0; x < settlement.getVehicleNum(); x++) vehicleListModel.addElement(settlement.getVehicle(x).getName());
		vehicleList = new JList(vehicleListModel);
		vehicleList.setVisibleRowCount(7);
		vehicleList.addMouseListener(this);
		vehiclePane.add(new JScrollPane(vehicleList), "Center");
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
