/**
 * Mars Simulation Project
 * VehicleDialog.java
 * @version 2.72 2001-08-13
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard;  
 
import org.mars_sim.msp.simulation.*; 
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * The VehicleDialog class is an abstract detail window for a vehicle.
 * It displays information about the vehicle as well as its current status.
 * It is abstract and an appropriate detail window needs to be derived for
 * a particular type of vehicle.
 */
public abstract class VehicleDialog extends UnitDialog implements MouseListener {

    // Data members
    protected Vehicle vehicle; // Vehicle detail window is about
    protected JTabbedPane tabPane; // Main tabbed pane
    protected JLabel statusLabel; // Status label
    protected JPanel locationLabelPane; // Location label pane
    protected JButton locationButton; // Location button
    protected JLabel latitudeLabel; // Latitude label
    protected JLabel longitudeLabel; // Longitude label
    protected JPanel destinationLabelPane; // Destination label pane
    protected JButton destinationButton; // Destination settlement button
    protected JLabel destinationLatitudeLabel; // Destination latitude label
    protected JLabel destinationLongitudeLabel; // Destination longitude label
    protected JLabel distanceDestinationLabel; // Distance to destination label
    protected JLabel etaDestinationLabel;  // ETA to destination label
    protected JLabel speedLabel; // Speed label
    protected JLabel fuelLabel; // Fuel label
    protected JPanel driverButtonPane; // Driver pane
    protected JButton driverButton; // Driver button
    protected JList crewList; // List of passengers
    protected JLabel damageLabel; // Vehicle damage label
    protected JPanel navigationInfoPane; // Navigation info pane
    protected JLabel odometerLabel; // Odometer Label
    protected JLabel lastMaintLabel; // Distance Since Last Maintenance Label
    protected JLabel failureDetailLabel; // Mechanical failure name label
    protected JProgressBar repairProgressBar; // Failure repair progress bar
    protected JProgressBar maintenanceProgressBar; // Maintenance progress bar
    protected JLabel foodValueLabel; // A label displaying the stores of food.
    protected JLabel oxygenValueLabel; // A label displaying the stores of oxygen.
    protected JLabel waterValueLabel; // A label displaying the stores of water.
    protected JLabel fuelValueLabel; // A label displaying the stores of fuel.

    // Cached data members
    protected String status; // Cached status of vehicle
    protected Coordinates location; // Cached location of vehicle
    protected Coordinates destination; // Cached destination of vehicle
    protected int distance; // Cached distance to destination
    protected float speed; // Cached speed of vehicle.
    protected Vector crewInfo; // Cached list of crewmembers.
    protected double distanceTraveled; // Cached total distance traveled by vehicle.
    protected double distanceMaint; // Cached distance traveled by vehicle since last maintenance.
    protected String failureName; // Cached mechanical failure name.
    protected int repairProgress; // Cached repair progress percentage.
    protected int maintenanceProgress; // Cached maintenance progress percentage;
    protected double fuel; // Cached fuel stores in vehicle
    protected double oxygen; // Cached oxygen stores in vehicle
    protected double water; // Cached water stores in vehicle
    protected double food; // Cached food stores in vehicle
    
    /** Constructs a VehicleDialog object 
     *  @param parentDesktop the desktop pane
     *  @param vehicleUIProxy the vehicle's UI proxy
     */
    public VehicleDialog(MainDesktopPane parentDesktop, VehicleUIProxy vehicleUIProxy) {

        // Use UnitDialog constructor
        super(parentDesktop, vehicleUIProxy);
    }

    /** Initialize cached data members */
    protected void initCachedData() {
        status = "Parked";
        location = new Coordinates(0D, 0D);
        destination = new Coordinates(0D, 0D);
        speed = 0F;
        crewInfo = new Vector();
    }

    /** Complete update (overridden) */
    protected void generalUpdate() {
        updateStatus();
        updateLocation();
        updateDestination();
        updateSpeed();
        updateCrew();
        updateOdometer();
        updateMechanicalFailure();
        updateMaintenance();
        updateStorage();
    }

    /** Implement MouseListener Methods */
    public void mouseClicked(MouseEvent event) {
        Object object = event.getSource();
        if (object == crewList) {
            if (event.getClickCount() >= 2) {
                if (crewList.locationToIndex(event.getPoint()) > -1) {
                    if ((crewList.getSelectedValue() != null) &&
                            !((String) crewList.getSelectedValue()).equals(" ")) {
                        UnitUIProxy personProxy = (UnitUIProxy) crewInfo.elementAt(
                                crewList.getSelectedIndex());
                        try { parentDesktop.openUnitWindow(personProxy); } 
                        catch (NullPointerException e) {}
                    }
                }
            }
        }
    }
    public void mousePressed(MouseEvent event) {}
    public void mouseReleased(MouseEvent event) {}
    public void mouseEntered(MouseEvent event) {}
    public void mouseExited(MouseEvent event) {}

    /** ActionListener method overriden */
    public void actionPerformed(ActionEvent event) {
        super.actionPerformed(event);
        
        Object button = event.getSource();

        // If location button, open window for selected unit
        if ((button == locationButton) && (vehicle.getStatus().equals("Parked") ||
                vehicle.getStatus().equals("Periodic Maintenance")))
            parentDesktop.openUnitWindow(proxyManager.getUnitUIProxy(vehicle.getSettlement()));
        if ((button == destinationButton) && !(vehicle.getStatus().equals("Parked") ||
                vehicle.getStatus().equals("Periodic Maintenance")))
            parentDesktop.openUnitWindow(proxyManager.getUnitUIProxy(vehicle.getDestinationSettlement()));
        if ((button == driverButton) && !(vehicle.getStatus().equals("Parked") ||
                vehicle.getStatus().equals("Periodic Maintenance")))
            parentDesktop.openUnitWindow(proxyManager.getUnitUIProxy(vehicle.getDriver()));
    }

    /** Prepare and add components to window */
    protected void setupComponents() {

        super.setupComponents();

        // Initialize vehicle
        vehicle = (Vehicle) parentUnit;

        // Prepare tab pane
        JTabbedPane tabPane = new JTabbedPane();
        tabPane.addTab("Navigation", setupNavigationPane());
        tabPane.addTab("Crew", setupCrewPane());
        tabPane.addTab("Damage", setupDamagePane());
        tabPane.addTab("Storage", setupStoragePane());
        mainPane.add(tabPane, "Center");
    }

    /** Set up navigation panel 
     *  @return navigation pane
     */
    protected JPanel setupNavigationPane() {

        // Prepare navigation pane
        JPanel navigationPane = new JPanel();
        navigationPane.setBorder(
                new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
        navigationPane.setLayout(new BoxLayout(navigationPane, BoxLayout.Y_AXIS));

        // Prepare status label
        statusLabel = new JLabel("Status: " + vehicle.getStatus(), JLabel.CENTER);
        statusLabel.setForeground(Color.black);
        JPanel statusLabelPanel = new JPanel();
        statusLabelPanel.add(statusLabel);
        navigationPane.add(statusLabelPanel);

        // Prepare location pane
        JPanel locationPane = new JPanel(new BorderLayout());
        locationPane.setBorder(
                new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
        navigationPane.add(locationPane);

        // Preparing location label pane
        locationLabelPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
        locationPane.add(locationLabelPane, "North");

        // Prepare center map button
        centerMapButton = new JButton(new ImageIcon("images/CenterMap.gif"));
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
        if ((vehicle.getStatus().equals("Parked") ||
                vehicle.getStatus().equals("Periodic Maintenance")) &&
                (vehicle.getSettlement() != null)) {
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
        destinationPane.setBorder(
                new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
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
        // if (!(vehicle.getStatus().equals("Parked") || vehicle.getStatus().equals("Periodic Maintenance"))) {
        if (vehicle.getDestination() != null) {
            if (vehicle.getDestinationType().equals("Settlement")) {
                destinationButton.setText(
                        vehicle.getDestinationSettlement().getName());
                destinationLabelPane.add(destinationButton);
            }
        }

        // Prepare destination coordinates pane
        JPanel destinationCoordsPane = new JPanel(new GridLayout(1, 2, 0, 0));
        destinationPane.add(destinationCoordsPane, "Center");

        // Prepare destination latitude label
        destinationLatitudeLabel = new JLabel("Latitude: ", JLabel.LEFT);
        // if (!(vehicle.getStatus().equals("Parked") || vehicle.getStatus().equals("Periodic Maintenance")))
        if (vehicle.getDestination() != null) destinationLatitudeLabel.setText("Latitude: ");
        destinationLatitudeLabel.setForeground(Color.black);
        destinationCoordsPane.add(destinationLatitudeLabel);

        // Prepare destination longitude label
        destinationLongitudeLabel = new JLabel("Longitude: ", JLabel.LEFT);
        // if (!(vehicle.getStatus().equals("Parked") || vehicle.getStatus().equals("Periodic Maintenance")))
        if (vehicle.getDestination() != null) destinationLongitudeLabel.setText("Longitude: ");
        destinationLongitudeLabel.setForeground(Color.black);
        destinationCoordsPane.add(destinationLongitudeLabel);
       
        // Prepare destination info pane
        JPanel destinationInfoPane = new JPanel(new GridLayout(2, 1, 0, 0));
        destinationPane.add(destinationInfoPane, "South");
 
        // Prepare ETA to destination label
        etaDestinationLabel = new JLabel("ETA: " + vehicle.getETA(), JLabel.LEFT);
        etaDestinationLabel.setForeground(Color.black);
        destinationInfoPane.add(etaDestinationLabel);

        // Prepare distance to destination label
        distanceDestinationLabel = new JLabel("Distance: ", JLabel.LEFT);
        // if (!(vehicle.getStatus().equals("Parked") || vehicle.getStatus().equals("Periodic Maintenance"))) {
        if (vehicle.getDestination() != null) {
            int tempDistance = (int) Math.round(vehicle.getDistanceToDestination());
            distanceDestinationLabel.setText("Distance: " + tempDistance + " km.");
        }
        distanceDestinationLabel.setForeground(Color.black);
        destinationInfoPane.add(distanceDestinationLabel);
        
        // Prepare navigation info pane
        navigationInfoPane = new JPanel();
        navigationInfoPane.setLayout(
                new BoxLayout(navigationInfoPane, BoxLayout.Y_AXIS));
        navigationInfoPane.setBorder(
                new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
        navigationPane.add(navigationInfoPane);

        // Prepare speed/fuel pane
        JPanel speedPane = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        navigationInfoPane.add(speedPane);

        // Prepare speed label
        int tempSpeed = (int) Math.round(vehicle.getSpeed());
        speedLabel = new JLabel("Speed: " + tempSpeed + " kph.", JLabel.LEFT);
        speedLabel.setForeground(Color.black);
        speedPane.add(speedLabel);
        
        // Return navigation pane
        return navigationPane;
    }

    /** Set up crew pane 
     *  @return crew pane
     */
    protected JPanel setupCrewPane() {
        
        // Prepare crew pane
        JPanel crewPane = new JPanel();
        crewPane.setBorder(
                new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
        crewPane.setLayout(new BoxLayout(crewPane, BoxLayout.Y_AXIS));

        // Prepare maximum crew capacity pane
        JPanel maxCrewPane = new JPanel(new BorderLayout());
        maxCrewPane.setBorder(
                new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
        crewPane.add(maxCrewPane);

        // Prepare maximum crew capacity label
        JLabel maxCrewLabel = new JLabel("Maximum Crew Capacity: " +
                vehicle.getMaxPassengers(), JLabel.CENTER);
        maxCrewLabel.setForeground(Color.black);
        maxCrewPane.add(maxCrewLabel, "Center");
        
        // Prepare driver pane
        JPanel driverPane = new JPanel(new BorderLayout());
        driverPane.setBorder(
                new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
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
       
        if (vehicle.getStatus().equals("Moving")) {
            driverButton.setText(vehicle.getDriver().getName());
            driverButtonPane.add(driverButton);
        }
        
        // Prepare crew list pane
        JPanel crewListPane = new JPanel(new BorderLayout());
        crewListPane.setBorder(
                new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
        crewPane.add(crewListPane);

        // Prepare crew label
        JLabel peopleLabel = new JLabel("Crew", JLabel.CENTER);
        peopleLabel.setForeground(Color.black);
        crewListPane.add(peopleLabel, "North");

        // Prepare crew list
        DefaultListModel crewListModel = new DefaultListModel();

        for (int x = 0; x < vehicle.getPassengerNum(); x++) {
            if (!(vehicle.getStatus().equals("Moving") && (vehicle.getPassenger(x) == vehicle.getDriver()))) {
                PersonUIProxy tempCrew = (PersonUIProxy) 
                        proxyManager.getUnitUIProxy(vehicle.getPassenger(x));
                crewInfo.addElement(tempCrew);
                crewListModel.addElement(tempCrew.getUnit().getName());
            }
        }
        
        // This prevents the list from sizing strange due to having no contents
        if (crewInfo.size() == 0) crewListModel.addElement(" ");

        crewList = new JList(crewListModel);
        crewList.setVisibleRowCount(7);
        crewList.addMouseListener(this);
        crewList.setPreferredSize(
                new Dimension(150, (int) crewList.getPreferredSize().getHeight()));
        JScrollPane crewScroll = new JScrollPane(crewList);
        JPanel crewScrollPane = new JPanel();
        crewScrollPane.add(crewScroll);
        crewListPane.add(crewScrollPane, "Center");
        
        // Return crew pane
        return crewPane;
    }

    /** Set up damage pane 
     *  @return damage pane
     */
    protected JPanel setupDamagePane() {

        // Prepare damage pane
        JPanel damagePane = new JPanel(new BorderLayout(0, 5));
        damagePane.setBorder(
                new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));

        // Prepare name label
        JLabel nameLabel = new JLabel("Vehicle Condition", JLabel.CENTER);
        nameLabel.setForeground(Color.black);
        damagePane.add(nameLabel, "North");

        // Prepare content pane
        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        contentPane.setBorder(
                new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
        damagePane.add(contentPane, "Center");

        // Prepare odometer pane
        JPanel odometerPane = new JPanel(new BorderLayout());
        odometerPane.setBorder(
                new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
        contentPane.add(odometerPane);

        // Prepare title pane
        JPanel titlePane = new JPanel(new GridLayout(2, 1));
        odometerPane.add(titlePane, "West");

        // Prepare odometer label
        JLabel odometerTitleLabel = new JLabel("Total Distance Traveled:");
        odometerTitleLabel.setForeground(Color.black);
        titlePane.add(odometerTitleLabel);

        // Prepare distance since last maintenance label
        JLabel lastMaintTitleLabel = new JLabel("Since Last Maintenance:");
        lastMaintTitleLabel.setForeground(Color.black);
        titlePane.add(lastMaintTitleLabel);

        // Prepare value pane
        JPanel valuePane = new JPanel(new GridLayout(2, 1));
        odometerPane.add(valuePane, "Center");

        // Prepare odometer value label
        odometerLabel = new JLabel((int) vehicle.getTotalDistanceTraveled() + " km.",
                JLabel.RIGHT);
        odometerLabel.setForeground(Color.black);
        valuePane.add(odometerLabel);

        // Prepare distance since last maintenance label
        lastMaintLabel =
                new JLabel((int) vehicle.getDistanceLastMaintenance() + " km.",
                JLabel.RIGHT);
        lastMaintLabel.setForeground(Color.black);
        valuePane.add(lastMaintLabel);

        // Prepare maintenance pane
        JPanel maintenancePane = new JPanel(new BorderLayout());
        maintenancePane.setBorder(
                new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
        contentPane.add(maintenancePane);

        // Prepare maintenance label
        JLabel maintenanceLabel = new JLabel("Periodic Maintenance:", JLabel.CENTER);
        maintenanceLabel.setForeground(Color.black);
        maintenancePane.add(maintenanceLabel, "North");

        // Prepare maintenance progress bar
        maintenanceProgressBar = new JProgressBar();
        maintenanceProgressBar.setStringPainted(true);
        maintenancePane.add(maintenanceProgressBar, "South");
        maintenanceProgress = 0;
        if (vehicle.getStatus().equals("Periodic Maintenance"))
            maintenanceProgress = (int)(100F *
                    ((float) vehicle.getCurrentMaintenanceWork() /
                    (float) vehicle.getTotalMaintenanceWork()));
        maintenanceProgressBar.setValue(maintenanceProgress);

        // Prepare failure pane
        JPanel failurePane = new JPanel(new GridLayout(4, 1));
        failurePane.setBorder(
                new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
        contentPane.add(failurePane);

        // Prepare failure label
        JLabel failureLabel = new JLabel("Mechanical Failure:", JLabel.CENTER);
        failureLabel.setForeground(Color.black);
        failurePane.add(failureLabel);

        // Prepare failure detail label
        failureDetailLabel = new JLabel("None", JLabel.CENTER);
        failureDetailLabel.setForeground(Color.black);
        failurePane.add(failureDetailLabel);
        MechanicalFailure failure = vehicle.getMechanicalFailure();
        if ((failure != null) && !failure.isFixed()) {
            failureDetailLabel.setText(failure.getName());
            failureName = failure.getName();
        } else
            failureName = "None";

        // Prepare repair label
        JLabel repairLabel = new JLabel("Repair Progress:", JLabel.CENTER);
        repairLabel.setForeground(Color.black);
        failurePane.add(repairLabel);

        // Prepare repair progress bar
        repairProgressBar = new JProgressBar();
        repairProgressBar.setStringPainted(true);
        failurePane.add(repairProgressBar);
        repairProgress = 0;
        if ((failure != null) && !failure.isFixed()) {
            double totalTime = failure.getTotalWorkTime();
            double remainingTime = failure.getRemainingWorkTime();
            repairProgress = (int)(100D * (totalTime - remainingTime) / totalTime);
        }
        repairProgressBar.setValue(repairProgress);

        // Create vertical glue
        contentPane.add(Box.createVerticalStrut(25));

        // Return damage pane
        return damagePane;
    }

    /** Set up storage pane 
     *  @return storage pane
     */
    protected JPanel setupStoragePane() {

        // Prepare damage pane
        JPanel storagePane = new JPanel(new BorderLayout(0, 5));
        storagePane.setBorder(
                new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));

        // Prepare name label
        JLabel nameLabel = new JLabel("Storage", JLabel.CENTER);
        nameLabel.setForeground(Color.black);
        storagePane.add(nameLabel, "North");

        // Prepare content pane
        JPanel contentPane = new JPanel();
        contentPane.setBorder(
                new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
        storagePane.add(contentPane, "Center");
        
        // Prepare label pane
	JPanel labelPane = new JPanel(new GridLayout(5, 2, 5, 3));
	contentPane.add(labelPane);
        
        // Prepare fuel label
	JLabel fuelLabel = new JLabel("Fuel:");
	fuelLabel.setForeground(Color.black);
	labelPane.add(fuelLabel);
		
	// Prepare fuel value label
	fuel = vehicle.getFuel();
	fuelValueLabel = new JLabel("" + roundOneDecimal(fuel), JLabel.RIGHT);
	fuelValueLabel.setForeground(Color.black);
	labelPane.add(fuelValueLabel);
        
        // Prepare oxygen label		
	JLabel oxygenLabel = new JLabel("Oxygen:");
	oxygenLabel.setForeground(Color.black);
	labelPane.add(oxygenLabel);
		
	// Prepare oxygen value label
	oxygen = vehicle.getOxygen();
	oxygenValueLabel = new JLabel("" + roundOneDecimal(oxygen), JLabel.RIGHT);
	oxygenValueLabel.setForeground(Color.black);
	labelPane.add(oxygenValueLabel);
        
        // Prepare water label
	JLabel waterLabel = new JLabel("Water:");
	waterLabel.setForeground(Color.black);
	labelPane.add(waterLabel);
		
	// Prepare water value label
	water = vehicle.getWater();
	waterValueLabel = new JLabel("" + roundOneDecimal(water), JLabel.RIGHT);
	waterValueLabel.setForeground(Color.black);
	labelPane.add(waterValueLabel);
        
        // Prepare food label
	JLabel foodLabel = new JLabel("Food:");
	foodLabel.setForeground(Color.black);
	labelPane.add(foodLabel);
		
	// Prepare food value label
	food = vehicle.getFood();
	foodValueLabel = new JLabel("" + roundOneDecimal(food), JLabel.RIGHT);
	foodValueLabel.setForeground(Color.black);
	labelPane.add(foodValueLabel);
        
        // Return storage pane
        return storagePane;
    }
    
    /** Update status info */
    protected void updateStatus() {

        // Update status label
        if (!status.equals(vehicle.getStatus())) {
            status = vehicle.getStatus();
            statusLabel.setText("Status: " + status);
        }
    }

    /** Update location info */
    protected void updateLocation() {

        if (!location.equals(vehicle.getCoordinates())) {
            location = new Coordinates(vehicle.getCoordinates());
            if ((vehicle.getStatus().equals("Parked") ||
                    vehicle.getStatus().equals("Periodic Maintenance")) &&
                    (vehicle.getSettlement() != null)) {
                if (!locationButton.getText().equals(
                        vehicle.getSettlement().getName()))
                    locationButton.setText(vehicle.getSettlement().getName());
                if (locationLabelPane.getComponentCount() == 2)
                    locationLabelPane.add(locationButton);
            } else if (locationLabelPane.getComponentCount() > 2)
                locationLabelPane.remove(locationButton);

            // Update latitude and longitude labels
            latitudeLabel.setText("Latitude: " +
                    vehicle.getCoordinates().getFormattedLatitudeString());
            longitudeLabel.setText("Longitude: " +
                    vehicle.getCoordinates().getFormattedLongitudeString());
        }
    }

    /** Update destination info */
    protected void updateDestination() {

        String destinationType = vehicle.getDestinationType();

        // Update destination button
        if (destinationType.equals("Settlement")) {
            if (!destinationButton.getText().equals(vehicle.getDestinationSettlement().getName()))
                destinationButton.setText(vehicle.getDestinationSettlement().getName());
            if (destinationLabelPane.getComponentCount() == 1) destinationLabelPane.add(destinationButton);
        } else {
            if (destinationLabelPane.getComponentCount() > 1) destinationLabelPane.remove(destinationButton);
        }

        // Update destination longitude and latitude labels
        if (destinationType.equals("None")) {
            if (!destinationLatitudeLabel.getText().equals("Latitude:")) {
                destinationLatitudeLabel.setText("Latitude:");
                destinationLongitudeLabel.setText("Longitude:");
            }
        } else {
            if (!destination.equals(vehicle.getDestination())) {
                destination = new Coordinates(vehicle.getDestination());
                destinationLatitudeLabel.setText("Latitude: " +
                        destination.getFormattedLatitudeString());
                destinationLongitudeLabel.setText("Longitude: " +
                        destination.getFormattedLongitudeString());
            }
        }

        // Update distance to destination label
        if (destinationType.equals("None")) {
            distanceDestinationLabel.setText("Distance:");
        } else {
            if (distance != (int) Math.round(vehicle.getDistanceToDestination())) {
                distance = (int) Math.round(vehicle.getDistanceToDestination());
                distanceDestinationLabel.setText("Distance: " + distance + " km.");
            }
        }
        
        etaDestinationLabel.setText("ETA: " + vehicle.getETA());
    }

    /** Update speed info */
    protected void updateSpeed() {

        // Update speed label
        if (speed != (float)((int) Math.round(vehicle.getSpeed() * 100D) / 100D)) {
            speed = (float)((int) Math.round(vehicle.getSpeed() * 100D) / 100D);
            speedLabel.setText("Speed: " + speed + " kph.");
        }
    }

    /** Update crew info */
    protected void updateCrew() {
        boolean vehicleMoving = vehicle.getStatus().equals("Moving");
      
        // Update driver button
        if (!vehicleMoving) {
            if (driverButtonPane.getComponentCount() > 0) driverButtonPane.remove(driverButton);
        } 
        else {
            if (!driverButton.getText().equals(vehicle.getDriver().getName()))
                driverButton.setText(vehicle.getDriver().getName());
            if (driverButtonPane.getComponentCount() == 0)
                driverButtonPane.add(driverButton);
        }

        // Update crew list
        DefaultListModel model = (DefaultListModel) crewList.getModel();
        boolean match = false;

        // Check if model matches passengers
        Vector tempPassengers = new Vector();
        for (int x=0; x < tempPassengers.size(); x++) {
            Person tempPerson = vehicle.getPassenger(x);
            if (tempPerson != vehicle.getDriver()) tempPassengers.addElement(vehicle.getPassenger(x));
        }

        if (model.getSize() == tempPassengers.size()) {
            match = true;
            for (int x=0; x < tempPassengers.size(); x++) {
                String tempName = (String) model.getElementAt(x);
                if (!tempName.equals(vehicle.getPassenger(x).getName())) match = false;
            }
        }

        // If no match, update crew list
        if (!match) {
            model.removeAllElements();
            crewInfo.removeAllElements();
            for (int x = 0; x < vehicle.getPassengerNum(); x++) {
                Person tempPassenger = vehicle.getPassenger(x);
                if (tempPassenger != null) {
                    if (!(tempPassenger == vehicle.getDriver())) { 
                        crewInfo.addElement(proxyManager.getUnitUIProxy(tempPassenger));
                        model.addElement(tempPassenger.getName());
                    }
                }
            }

            // This prevents the list from sizing strange due to having no contents
            if (crewInfo.size() == 0) model.addElement(" ");

            validate();
        }
    }

    /** Update odometer info */
    protected void updateOdometer() {

        // Update odometer label
        if (distanceTraveled != vehicle.getTotalDistanceTraveled()) {
            distanceTraveled = vehicle.getTotalDistanceTraveled();
            odometerLabel.setText((int) distanceTraveled + " km.");
        }

        // Update distance since last maintenance label
        if (distanceMaint != vehicle.getDistanceLastMaintenance()) {
            distanceMaint = vehicle.getDistanceLastMaintenance();
            lastMaintLabel.setText((int) distanceMaint + " km.");
        }
    }

    /** Update mechanical failure */
    protected void updateMechanicalFailure() {

        // Update failure detail label
        MechanicalFailure failure = vehicle.getMechanicalFailure();
        boolean change = false;

        if ((failure == null) || failure.isFixed()) {
            if (!failureName.equals("None")) {
                failureName = "None";
                change = true;
            }
        } else {
            if (failureName.equals("None")) {
                failureName = failure.getName();
                change = true;
            }
        }

        if (change)
            failureDetailLabel.setText(failureName);

        // Update repair progress bar
        int repairProgressTemp = 0;
        if ((failure != null) && !failure.isFixed()) {
            double totalTime = failure.getTotalWorkTime();
            double remainingTime = failure.getRemainingWorkTime();
            repairProgressTemp =
                    (int)(100F * (totalTime - remainingTime) / totalTime);
        }
        if (repairProgress != repairProgressTemp) {
            repairProgress = repairProgressTemp;
            repairProgressBar.setValue(repairProgress);
        }
    }

    /** Update maintenance progress */
    protected void updateMaintenance() {

        // Update maintenance progress bar
        int maintenanceProgressTemp = 0;
        if (vehicle.getStatus().equals("Periodic Maintenance"))
            maintenanceProgressTemp = (int)(100F *
                    ((float) vehicle.getCurrentMaintenanceWork() /
                    (float) vehicle.getTotalMaintenanceWork()));
        if (maintenanceProgress != maintenanceProgressTemp) {
            maintenanceProgress = maintenanceProgressTemp;
            maintenanceProgressBar.setValue(maintenanceProgress);
        }
    }
    
    /** Update storage */
    protected void updateStorage() {
        
        // Update fuel
        if (fuel != vehicle.getFuel()) {
			fuel = vehicle.getFuel();
			fuelValueLabel.setText("" + roundOneDecimal(fuel));
		}
        
        // Update oxygen
		if (oxygen != vehicle.getOxygen()) {
			oxygen = vehicle.getOxygen();
			oxygenValueLabel.setText("" + roundOneDecimal(oxygen));
		}
        
        // Update water
		if (water != vehicle.getWater()) {
			water = vehicle.getWater();
			waterValueLabel.setText("" + roundOneDecimal(water));
		}
        
        // Update food
        if (food != vehicle.getFood()) {
			food = vehicle.getFood();
			foodValueLabel.setText("" + roundOneDecimal(food));
		}
	}
}
