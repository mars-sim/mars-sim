//************************** Vehicle Detail Window **************************
// Last Modified: 3/1/00

// The VehicleDialog class is an abstract detail window for a vehicle.
// It displays information about the vehicle as well as its current status.
// It is abstract and an appropriate detail window needs to be derived for 
// a particular type of vehicle.

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

public abstract class VehicleDialog extends UnitDialog implements MouseListener {

	// Data members

	protected Vehicle vehicle;	            // Vehicle detail window is about
	protected JTabbedPane tabPane;              // Main tabbed pane
	protected JLabel statusLabel;               // Status label
	protected JPanel locationLabelPane;         // Location label pane
	protected JButton locationButton;           // Location button
	protected JLabel latitudeLabel;             // Latitude label
	protected JLabel longitudeLabel;            // Longitude label
	protected JPanel destinationLabelPane;      // Destination label pane	
	protected JButton destinationButton;        // Destination settlement button
	protected JLabel destinationLatitudeLabel;  // Destination latitude label
	protected JLabel destinationLongitudeLabel; // Destination longitude label
	protected JLabel distanceDestinationLabel;  // Distance to destination label
	protected JLabel speedLabel;                // Speed label
	protected JPanel driverButtonPane;          // Driver pane
	protected JButton driverButton;             // Driver button
	protected JList crewList;                   // List of passengers
	protected JLabel damageLabel;               // Vehicle damage label	
	protected JPanel navigationInfoPane;        // Navigation info pane

	// Cached data members
	
	protected String status;                    // Cached status of vehicle
	protected Coordinates location;             // Cached location of vehicle
	protected Coordinates destination;          // Cached destination of vehicle
	protected int distance;                     // Cached distance to destination
	protected float speed;                      // Cached speed of vehicle.
	protected Vector crewInfo;                  // Cached list of crewmembers.

	// Constructor

	public VehicleDialog(MainDesktopPane parentDesktop, Vehicle vehicle) {

		// Use UnitDialog constructor

		super(parentDesktop, vehicle);
	}
	
	// Initialize cached data members
	
	protected void initCachedData() {
		status = "Parked";
		location = new Coordinates(0D, 0D);
		destination = new Coordinates(0D, 0D);
		speed = 0F;
		crewInfo = new Vector();
	}
	
	// Complete update (overridden)

	protected void generalUpdate() {
			updateStatus();
			updateLocation();
			updateDestination();
			updateSpeed();
			updateCrew();
	}
	
	// Implement MouseListener Methods
	
	public void mouseClicked(MouseEvent event) {
		Object object = event.getSource();
		if (object == crewList) {
			if (event.getClickCount() >= 2) {
	 			if (crewList.locationToIndex(event.getPoint()) > -1) {
	 				if ((crewList.getSelectedValue() != null) && !((String) crewList.getSelectedValue()).equals(" ")) {
	 					UnitInfo personInfo = (UnitInfo) crewInfo.elementAt(crewList.getSelectedIndex());
						try { parentDesktop.openUnitWindow(personInfo.getID()); }
						catch(NullPointerException e) {}
					}
				}
	 		}
		}
	}
	public void mousePressed(MouseEvent event) {}
	public void mouseReleased(MouseEvent event) {}
	public void mouseEntered(MouseEvent event) {}
	public void mouseExited(MouseEvent event) {}
	
	// ActionListener method overriden
	
	public void actionPerformed(ActionEvent event) {
		super.actionPerformed(event);
		
		Object button = event.getSource();
			
		// If location button, open window for selected unit
			
		if ((button == locationButton) && vehicle.getStatus().equals("Parked")) parentDesktop.openUnitWindow(vehicle.getSettlement().getID());
		if ((button == destinationButton) && !vehicle.getStatus().equals("Parked")) parentDesktop.openUnitWindow(vehicle.getDestinationSettlement().getID());
		if ((button == driverButton) && !vehicle.getStatus().equals("Parked")) parentDesktop.openUnitWindow(vehicle.getDriver().getID());
	}

	// Prepare and add components to window

	protected void setupComponents() {

		super.setupComponents();
		
		// Initialize vehicle

		vehicle = (Vehicle) parentUnit;
		
		// Prepare tab pane
		
		JTabbedPane tabPane = new JTabbedPane();
		tabPane.addTab("Navigation", setupNavigationPane());
		tabPane.addTab("Crew", setupCrewPane());
		tabPane.addTab("Damage", setupDamagePane());
		mainPane.add(tabPane);
	}

	// Set up navigation panel

	protected JPanel setupNavigationPane() {
		
		// Prepare navigation pane

		JPanel navigationPane = new JPanel();
		navigationPane.setLayout(new BoxLayout(navigationPane, BoxLayout.Y_AXIS));

		// Prepare status label

		statusLabel = new JLabel("Status: " + vehicle.getStatus(), JLabel.CENTER);
		statusLabel.setForeground(Color.black);
		JPanel statusLabelPanel = new JPanel();
		statusLabelPanel.add(statusLabel);
		navigationPane.add(statusLabelPanel);

		// Prepare location pane
		
		JPanel locationPane = new JPanel(new BorderLayout());
		locationPane.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
		navigationPane.add(locationPane);

		// Preparing location label pane

		locationLabelPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
		locationPane.add(locationLabelPane, "North");

		// Prepare center map button
		
		centerMapButton = new JButton(new ImageIcon("CenterMap.gif"));
		centerMapButton.setMargin(new Insets(1, 1, 1, 1));
		centerMapButton.addActionListener(this);
		locationLabelPane.add(centerMapButton);
		
		// Prepare location label

		JLabel locationLabel = new JLabel("Location: ", JLabel.CENTER);
		locationLabel.setForeground(Color.black);
		locationLabelPane.add(locationLabel);

		// Prepare location button
		
		locationButton = new JButton();
		locationButton.setMargin(new Insets(1, 1, 1, 1));
		locationButton.addActionListener(this);
		if (vehicle.getStatus().equals("Parked")) {
			locationButton.setText(vehicle.getSettlement().getName());
			locationLabelPane.add(locationButton);
		}

		// Prepare location coordinates pane
		
		JPanel locationCoordsPane = new JPanel(new GridLayout(1, 2, 0, 0));
		locationPane.add(locationCoordsPane, "Center");

		// Prepare latitude label

		latitudeLabel = new JLabel("Latitude: ", JLabel.LEFT);
		latitudeLabel.setForeground(Color.black);
		locationCoordsPane.add(latitudeLabel);

		// Prepare longitude label

		longitudeLabel = new JLabel("Longitude: ", JLabel.LEFT);
		longitudeLabel.setForeground(Color.black);
		locationCoordsPane.add(longitudeLabel);

		// Prepare destination pane
		
		JPanel destinationPane = new JPanel(new BorderLayout());
		destinationPane.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
		navigationPane.add(destinationPane);

		destinationLabelPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
		destinationPane.add(destinationLabelPane, "North");

		// Prepare destination label

		JLabel destinationLabel = new JLabel("Destination: ", JLabel.LEFT);
		destinationLabel.setForeground(Color.black);
		destinationLabelPane.add(destinationLabel);
		
		// Prepare destination button
		
		destinationButton = new JButton();
		destinationButton.setMargin(new Insets(1, 1, 1, 1));
		destinationButton.addActionListener(this);
		if (!vehicle.getStatus().equals("Parked")) {
			if (vehicle.getDestinationType().equals("Settlement")) {
				destinationButton.setText(vehicle.getDestinationSettlement().getName());
				destinationLabelPane.add(destinationButton);
			}
		}

		// Prepare destination coordinates pane
		
		JPanel destinationCoordsPane = new JPanel(new GridLayout(1, 2, 0, 0));
		destinationPane.add(destinationCoordsPane, "Center");

		// Prepare destination latitude label

		destinationLatitudeLabel = new JLabel("Latitude: ", JLabel.LEFT);
		if (!vehicle.getStatus().equals("Parked")) destinationLatitudeLabel.setText("Latitude: ");
		destinationLatitudeLabel.setForeground(Color.black);
		destinationCoordsPane.add(destinationLatitudeLabel);

		// Prepare destination longitude label

		destinationLongitudeLabel = new JLabel("Longitude: ", JLabel.LEFT);
		if (!vehicle.getStatus().equals("Parked")) destinationLongitudeLabel.setText("Longitude: ");
		destinationLongitudeLabel.setForeground(Color.black);
		destinationCoordsPane.add(destinationLongitudeLabel);

		// Prepare distance to destination label

		distanceDestinationLabel = new JLabel("Distance: ", JLabel.LEFT);
		if (!vehicle.getStatus().equals("Parked")) {
			int tempDistance = (int) Math.round(vehicle.getDistanceToDestination());
			distanceDestinationLabel.setText("Distance: " + tempDistance + " km.");
		}
		distanceDestinationLabel.setForeground(Color.black);
		destinationPane.add(distanceDestinationLabel, "South");

		// Prepare navigation info pane
		
		navigationInfoPane = new JPanel();
		navigationInfoPane.setLayout(new BoxLayout(navigationInfoPane, BoxLayout.Y_AXIS));
		navigationInfoPane.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
		navigationPane.add(navigationInfoPane);

		// Prepare speed label

		int tempSpeed = (int) Math.round(vehicle.getSpeed());
		speedLabel = new JLabel("Speed: " + tempSpeed + " kph.", JLabel.LEFT);
		speedLabel.setForeground(Color.black);
		JPanel speedLabelPane = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		speedLabelPane.add(speedLabel);
		navigationInfoPane.add(speedLabelPane);

		// Return navigation pane

		return navigationPane;
	}

	// Set up crew pane

	protected JPanel setupCrewPane() {

		// Prepare crew pane
		
		JPanel crewPane = new JPanel();
		crewPane.setLayout(new BoxLayout(crewPane, BoxLayout.Y_AXIS));

		// Prepare driver pane

		JPanel driverPane = new JPanel(new BorderLayout());
		driverPane.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
		crewPane.add(driverPane);

		// Prepare driver label
		
		JLabel driverLabel = new JLabel("Driver", JLabel.CENTER);
		driverLabel.setForeground(Color.black);
		driverPane.add(driverLabel, "North");

		// Prepare driver button pane
		
		driverButtonPane = new JPanel();
		driverPane.add(driverButtonPane, "Center");

		// Prepare driver button
		
		driverButton = new JButton();
		driverButton.setMargin(new Insets(1, 1, 1, 1));
		driverButton.addActionListener(this);
		if (!vehicle.getStatus().equals("Parked")) {
			driverButton.setText(vehicle.getDriver().getName());
			driverButtonPane.add(driverButton);
		}

		// Prepare crew list pane
		
		JPanel crewListPane = new JPanel(new BorderLayout());
		crewListPane.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
		crewPane.add(crewListPane);

		// Prepare crew label
		
		JLabel peopleLabel = new JLabel("Crew", JLabel.CENTER);
		peopleLabel.setForeground(Color.black);
		crewListPane.add(peopleLabel, "North");

		// Prepare crew list
		
		DefaultListModel crewListModel = new DefaultListModel();
		
		for (int x=0; x < vehicle.getPassengerNum(); x++) {
			if (vehicle.getPassenger(x) != vehicle.getDriver()) {
				UnitInfo tempCrew = vehicle.getPassenger(x).getUnitInfo();
				crewInfo.addElement(tempCrew);
				crewListModel.addElement(tempCrew.getName());
			}
		}
		
		// This prevents the list from sizing strange due to having no contents
		
		if (vehicle.getPassengerNum() <= 1) crewListModel.addElement(" ");
		
		crewList = new JList(crewListModel);
		crewList.setVisibleRowCount(7);
		crewList.addMouseListener(this);
		crewList.setPreferredSize(new Dimension(150, (int) crewList.getPreferredSize().getHeight()));
		JScrollPane crewScroll = new JScrollPane(crewList);
		JPanel crewScrollPane = new JPanel();
		crewScrollPane.add(crewScroll);
		crewListPane.add(crewScrollPane, "Center");
		
		// Return crew pane
		
		return crewPane;
	}

	// Set up damage pane

	protected JPanel setupDamagePane() {

		// Prepare damage pane

		JPanel damagePane = new JPanel();
		damagePane.setLayout(new BoxLayout(damagePane, BoxLayout.Y_AXIS));

		// Prepare damage label

		JLabel damageLabel = new JLabel("Damage: None", JLabel.CENTER);
		damageLabel.setForeground(Color.black);
		JPanel damageLabelPane = new JPanel();
		damageLabelPane.add(damageLabel);
		damagePane.add(damageLabelPane);

		// Return damage pane

		return damagePane;
	}

	// Update status info

	protected void updateStatus() {
		
		// Update status label
		
		if (!status.equals(vehicle.getStatus())) {
			status = vehicle.getStatus();
			statusLabel.setText("Status: " + status);
		}
	}

	// Update location info

	protected void updateLocation() {
		
		if (!location.equals(vehicle.getCoordinates())) {
			location = new Coordinates(vehicle.getCoordinates());
			if (vehicle.getStatus().equals("Parked") && (vehicle.getSettlement() != null)) {
				if (!locationButton.getText().equals(vehicle.getSettlement().getName())) locationButton.setText(vehicle.getSettlement().getName());
				if (locationLabelPane.getComponentCount() == 2) locationLabelPane.add(locationButton);
			}
			else if (locationLabelPane.getComponentCount() > 2) locationLabelPane.remove(locationButton);
		
			// Update latitude and longitude labels
		
			latitudeLabel.setText("Latitude: " + vehicle.getCoordinates().getFormattedLatitudeString());
			longitudeLabel.setText("Longitude: " + vehicle.getCoordinates().getFormattedLongitudeString());
		}
	}

	// Update destination info

	protected void updateDestination() {
		
		String destinationType = vehicle.getDestinationType();
		
		// Update destination button
		
		if (destinationType.equals("Settlement")) {
			if (!destinationButton.getText().equals(vehicle.getDestinationSettlement().getName())) destinationButton.setText(vehicle.getDestinationSettlement().getName());
			if (destinationLabelPane.getComponentCount() == 1) destinationLabelPane.add(destinationButton);
		}
		else {
			if (destinationLabelPane.getComponentCount() > 1) destinationLabelPane.remove(destinationButton);
		}
		
		// Update destination longitude and latitude labels
		
		if (destinationType.equals("None")) {
			if (!destinationLatitudeLabel.getText().equals("Latitude:")) {
				destinationLatitudeLabel.setText("Latitude:");
				destinationLongitudeLabel.setText("Longitude:");
			}
		}
		else {
			if (!destination.equals(vehicle.getDestination())) {
				destination = new Coordinates(vehicle.getDestination());
				destinationLatitudeLabel.setText("Latitude: " + destination.getFormattedLatitudeString());
				destinationLongitudeLabel.setText("Longitude: " + destination.getFormattedLongitudeString());
			}
		}
		
		// Update distance to destination label
		
		if (destinationType.equals("None")) {
			distanceDestinationLabel.setText("Distance:");
		}
		else {
			if (distance != (int) Math.round(vehicle.getDistanceToDestination())) {
				distance = (int) Math.round(vehicle.getDistanceToDestination());
				distanceDestinationLabel.setText("Distance: " + distance + " km.");
			}
		}
	} 

	// Update speed info

	protected void updateSpeed() {
		
		// Update speed label
		
		if (speed != (float) ((int) Math.round(vehicle.getSpeed() * 100D) / 100D)) {
			speed = (float) ((int) Math.round(vehicle.getSpeed() * 100D) / 100D);
			speedLabel.setText("Speed: " + speed + " kph.");
		}
	}

	// Update crew info

	protected void updateCrew() {
		
		// Update driver button
		
		if (vehicle.getStatus().equals("Parked")) {
			if (driverButtonPane.getComponentCount() > 0) driverButtonPane.remove(driverButton);
		}
		else {
			if (!driverButton.getText().equals(vehicle.getDriver().getName())) driverButton.setText(vehicle.getDriver().getName());
			if (driverButtonPane.getComponentCount() == 0) driverButtonPane.add(driverButton);
		}
		
		// Update crew list
		
		DefaultListModel model = (DefaultListModel) crewList.getModel();
		boolean match = false;
		
		// Check if crew list matches vehicle's crew
		
		if ((model.getSize() + 1) == vehicle.getPassengerNum()) {
			match = true;
			int passengerCount = 0;
			for (int x=0; x < vehicle.getPassengerNum(); x++) {
				if (vehicle.getPassenger(x) != vehicle.getDriver()) {
					if (!((String) model.getElementAt(passengerCount)).equals(vehicle.getPassenger(x).getName())) match = false;
					passengerCount++;
				}
			}
		}
			
		// If no match, update crew list
		
		if (!match) {
			model.removeAllElements();
			crewInfo.removeAllElements();
			for (int x=0; x < vehicle.getPassengerNum(); x++) {
				Person tempPassenger = vehicle.getPassenger(x);
				if ((tempPassenger != null) && (tempPassenger != vehicle.getDriver())) {
					crewInfo.addElement(tempPassenger.getUnitInfo());
					model.addElement(tempPassenger.getName()); 
				}
			}
			
			// This prevents the list from sizing strange due to having no contents
			
			if (vehicle.getPassengerNum() <= 1) model.addElement(" ");
			
			validate();
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