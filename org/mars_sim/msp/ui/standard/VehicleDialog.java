/**
 * Mars Simulation Project
 * VehicleDialog.java
 * @version 2.74 2002-03-17
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.vehicle.*;
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
public abstract class VehicleDialog extends UnitDialog {

    // Data members
    protected Vehicle vehicle; // Vehicle detail window is about
    protected JTabbedPane tabPane; // Main tabbed pane
    protected JLabel statusLabel; // Status label
    protected JPanel locationLabelPane; // Location label pane
    protected JButton locationButton; // Location button
    protected JLabel latitudeLabel; // Latitude label
    protected JLabel longitudeLabel; // Longitude label
    protected JPanel destinationLabelPane; // Destination label pane
    protected JButton destinationCenterMapButton; // Destination center map button
    protected JLabel destinationLabel; // Destination label
    protected JButton destinationButton; // Destination settlement button
    protected JLabel destinationLatitudeLabel; // Destination latitude label
    protected JLabel destinationLongitudeLabel; // Destination longitude label
    protected JLabel distanceDestinationLabel; // Distance to destination label
    protected JLabel etaDestinationLabel;  // ETA to destination label
    protected JLabel speedLabel; // Speed label
    protected JLabel fuelLabel; // Fuel label
    protected JPanel driverButtonPane; // Driver pane
    protected JButton driverButton; // Driver button
    protected JLabel damageLabel; // Vehicle damage label
    protected JPanel navigationInfoPane; // Navigation info pane
    protected JLabel odometerLabel; // Odometer Label
    protected JLabel lastMaintLabel; // Distance Since Last Maintenance Label
    protected JLabel failureDetailLabel; // Mechanical failure name label
    protected JProgressBar repairProgressBar; // Failure repair progress bar
    protected JProgressBar maintenanceProgressBar; // Maintenance progress bar
    protected InventoryPanel inventoryPane; // The inventory panel.

    // Cached data members
    protected String status; // Cached status of vehicle
    protected Coordinates location; // Cached location of vehicle
    protected Coordinates destination; // Cached destination of vehicle
    protected int distance; // Cached distance to destination
    protected float speed; // Cached speed of vehicle.
    protected double distanceTraveled; // Cached total distance traveled by vehicle.
    protected double distanceMaint; // Cached distance traveled by vehicle since last maintenance.
    protected String failureName; // Cached mechanical failure name.
    protected int repairProgress; // Cached repair progress percentage.
    protected int maintenanceProgress; // Cached maintenance progress percentage;

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
    }

    /** Complete update (overridden) */
    protected void generalUpdate() {
        updateStatus();
        updateLocation();
        updateDestination();
        updateSpeed();
        updateDriver();
        updateOdometer();
        updateMechanicalFailure();
        updateMaintenance();
	inventoryPane.updateInfo();
    }

    /** ActionListener method overriden */
    public void actionPerformed(ActionEvent event) {
        super.actionPerformed(event);

        Object button = event.getSource();

        // If destination center map button, center map on destination
	if (button == destinationCenterMapButton)
            parentDesktop.centerMapGlobe(vehicle.getDestination());

        // If location button, open window for settlement.
        if ((button == locationButton) && (vehicle.getSettlement() != null)) {
            parentDesktop.openUnitWindow(proxyManager.getUnitUIProxy(vehicle.getSettlement()));
	}

	// If destination button, open window for destination settlement.
        if ((button == destinationButton) && (vehicle.getDestinationSettlement() != null)) {
            parentDesktop.openUnitWindow(proxyManager.getUnitUIProxy(vehicle.getDestinationSettlement()));
	}

	// If driver button, open window for driver.
        if ((button == driverButton) && (vehicle.getDriver() != null)) {
            parentDesktop.openUnitWindow(proxyManager.getUnitUIProxy(vehicle.getDriver()));
	}
    }

    /** Prepare and add components to window */
    protected void setupComponents() {

        super.setupComponents();

        // Initialize vehicle
        vehicle = (Vehicle) parentUnit;

        // Prepare inner pane
	JPanel innerPane = new JPanel(new BorderLayout());
	mainPane.add(innerPane, "Center");

	// Prepare description label
	JLabel descriptionLabel = new JLabel(vehicle.getDescription(), JLabel.CENTER);
	innerPane.add(descriptionLabel, "North");
	
        // Prepare tab pane
        tabPane = new JTabbedPane();
        tabPane.addTab("Navigation", setupNavigationPane());
	tabPane.addTab("Driver", setupDriverPane());
        tabPane.addTab("Damage", setupDamagePane());
	inventoryPane = new InventoryPanel(vehicle.getInventory());
	tabPane.addTab("Inventory", inventoryPane);
        innerPane.add(tabPane, "Center");
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
        locationLabelPane.add(locationLabel);

        // Prepare location button
        locationButton = new JButton();
        locationButton.setMargin(new Insets(1, 1, 1, 1));
        locationButton.addActionListener(this);
	if (vehicle.getSettlement() != null) {
            locationButton.setText(vehicle.getSettlement().getName());
            locationLabelPane.add(locationButton);
        }

        // Prepare location coordinates pane
        JPanel locationCoordsPane = new JPanel(new GridLayout(1, 2, 0, 0));
        locationPane.add(locationCoordsPane, "Center");

        // Prepare latitude label
        latitudeLabel = new JLabel("Latitude: ", JLabel.LEFT);
        locationCoordsPane.add(latitudeLabel);

        // Prepare longitude label
        longitudeLabel = new JLabel("Longitude: ", JLabel.LEFT);
        locationCoordsPane.add(longitudeLabel);

        // Prepare destination pane
        JPanel destinationPane = new JPanel(new BorderLayout());
        destinationPane.setBorder(
                new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
        navigationPane.add(destinationPane);

        destinationLabelPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
        destinationPane.add(destinationLabelPane, "North");

        // Prepare destination center map button
	destinationCenterMapButton = new JButton(new ImageIcon("images/CenterMap.gif"));
	destinationCenterMapButton.setMargin(new Insets(1, 1, 1, 1));
	destinationCenterMapButton.addActionListener(this);
	destinationLabelPane.add(destinationCenterMapButton);

        // Prepare destination label
        destinationLabel = new JLabel("Destination: ", JLabel.LEFT);
        if (vehicle.getDestinationType().equals("Coordinates")) destinationLabel.setText("Destination: Coordinates");
        destinationLabelPane.add(destinationLabel);

        // Prepare destination button
        destinationButton = new JButton();
        destinationButton.setMargin(new Insets(1, 1, 1, 1));
        destinationButton.addActionListener(this);
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
        if (vehicle.getDestination() != null) destinationLatitudeLabel.setText("Latitude: ");
        destinationCoordsPane.add(destinationLatitudeLabel);

        // Prepare destination longitude label
        destinationLongitudeLabel = new JLabel("Longitude: ", JLabel.LEFT);
        if (vehicle.getDestination() != null) destinationLongitudeLabel.setText("Longitude: ");
        destinationCoordsPane.add(destinationLongitudeLabel);

        // Prepare destination info pane
        JPanel destinationInfoPane = new JPanel(new GridLayout(2, 1, 0, 0));
        destinationPane.add(destinationInfoPane, "South");

        // Prepare ETA to destination label
        etaDestinationLabel = new JLabel("ETA: " + vehicle.getETA(), JLabel.LEFT);
        destinationInfoPane.add(etaDestinationLabel);

        // Prepare distance to destination label
        distanceDestinationLabel = new JLabel("Distance: ", JLabel.LEFT);
        if (vehicle.getDestination() != null) {
            int tempDistance = (int) Math.round(vehicle.getDistanceToDestination());
            distanceDestinationLabel.setText("Distance: " + tempDistance + " km.");
        }
        destinationInfoPane.add(distanceDestinationLabel);

        // Prepare navigation info pane
        navigationInfoPane = new JPanel();
        navigationInfoPane.setLayout(
                new BoxLayout(navigationInfoPane, BoxLayout.Y_AXIS));
        navigationInfoPane.setBorder(
                new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
        navigationPane.add(navigationInfoPane);

        // Prepare speed pane
        JPanel speedPane = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        navigationInfoPane.add(speedPane);

        // Prepare speed label
        int tempSpeed = (int) Math.round(vehicle.getSpeed());
        speedLabel = new JLabel("Speed: " + tempSpeed + " kph.", JLabel.LEFT);
        speedPane.add(speedLabel);

        // Return navigation pane
        return navigationPane;
    }

    /** Set up driver pane
     *  @return driver pane
     */
    protected JPanel setupDriverPane() {

        // Prepare driver pane
        JPanel driverPane = new JPanel();
        driverPane.setBorder(
                new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
        driverPane.setLayout(new BoxLayout(driverPane, BoxLayout.Y_AXIS));

        // Prepare inner driver pane
        JPanel innerDriverPane = new JPanel(new BorderLayout());
        innerDriverPane.setBorder(
                new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
        driverPane.add(innerDriverPane);

        // Prepare driver label
        JLabel driverLabel = new JLabel("Driver", JLabel.CENTER);
        innerDriverPane.add(driverLabel, "North");

        // Prepare driver button pane
        driverButtonPane = new JPanel();
        innerDriverPane.add(driverButtonPane, "Center");

        // Prepare driver button
        driverButton = new JButton();
        driverButton.setMargin(new Insets(1, 1, 1, 1));
        driverButton.addActionListener(this);

        if (vehicle.getSpeed() != 0D) {
            driverButton.setText(vehicle.getDriver().getName());
            driverButtonPane.add(driverButton);
        }

        // Return driver pane
        return driverPane;
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
        titlePane.add(odometerTitleLabel);

        // Prepare distance since last maintenance label
        JLabel lastMaintTitleLabel = new JLabel("Since Last Maintenance:");
        titlePane.add(lastMaintTitleLabel);

        // Prepare value pane
        JPanel valuePane = new JPanel(new GridLayout(2, 1));
        odometerPane.add(valuePane, "Center");

        // Prepare odometer value label
        odometerLabel = new JLabel((int) vehicle.getTotalDistanceTraveled() + " km.",
                JLabel.RIGHT);
        valuePane.add(odometerLabel);

        // Prepare distance since last maintenance label
        lastMaintLabel =
                new JLabel((int) vehicle.getDistanceLastMaintenance() + " km.",
                JLabel.RIGHT);
        valuePane.add(lastMaintLabel);

        // Prepare maintenance pane
        JPanel maintenancePane = new JPanel(new BorderLayout());
        maintenancePane.setBorder(
                new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
        contentPane.add(maintenancePane);

        // Prepare maintenance label
        JLabel maintenanceLabel = new JLabel("Periodic Maintenance:", JLabel.CENTER);
        maintenancePane.add(maintenanceLabel, "North");

        // Prepare maintenance progress bar
        maintenanceProgressBar = new JProgressBar();
        maintenanceProgressBar.setStringPainted(true);
        maintenancePane.add(maintenanceProgressBar, "South");
        maintenanceProgress = 0;
        if (vehicle.getStatus() == Vehicle.MAINTENANCE)
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
        failurePane.add(failureLabel);

        // Prepare failure detail label
        failureDetailLabel = new JLabel("None", JLabel.CENTER);
        failurePane.add(failureDetailLabel);
        MechanicalFailure failure = vehicle.getMechanicalFailure();
        if ((failure != null) && !failure.isFixed()) {
            failureDetailLabel.setText(failure.getName());
            failureName = failure.getName();
        } else
            failureName = "None";

        // Prepare repair label
        JLabel repairLabel = new JLabel("Repair Progress:", JLabel.CENTER);
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

    /** Update status info */
    protected void updateStatus() {

        // Update status label
        if (status != vehicle.getStatus()) {
            status = vehicle.getStatus();
            statusLabel.setText("Status: " + status);
        }
    }

    /** Update location info */
    protected void updateLocation() {

        if (!location.equals(vehicle.getCoordinates())) {
            location = new Coordinates(vehicle.getCoordinates());
            if (vehicle.getSettlement() != null) {
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

        Coordinates destinationCoords = vehicle.getDestination();

	// Update destination center map button
	if (destinationCoords == null) destinationCenterMapButton.setVisible(false);
	else destinationCenterMapButton.setVisible(true);

        String destinationType = vehicle.getDestinationType();

        // Update destination button
        if (destinationType.equals("Settlement")) {
            if (!destinationButton.getText().equals(vehicle.getDestinationSettlement().getName()))
                destinationButton.setText(vehicle.getDestinationSettlement().getName());
            if (destinationLabelPane.getComponentCount() == 2) {
                destinationLabel.setText("Destination: ");
                destinationLabelPane.add(destinationButton);
            }
        } else {
            if (destinationLabelPane.getComponentCount() > 2)
                destinationLabelPane.remove(destinationButton);
            if (destinationType.equals("Coordinates"))
                destinationLabel.setText("Destination: Coordinates");
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

    /** Update driver info */
    protected void updateDriver() {
        boolean vehicleMoving = (vehicle.getStatus() == Vehicle.MOVING);

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
        if (vehicle.getStatus() == Vehicle.MAINTENANCE)
            maintenanceProgressTemp = (int)(100F *
                    ((float) vehicle.getCurrentMaintenanceWork() /
                    (float) vehicle.getTotalMaintenanceWork()));
        if (maintenanceProgress != maintenanceProgressTemp) {
            maintenanceProgress = maintenanceProgressTemp;
            maintenanceProgressBar.setValue(maintenanceProgress);
        }
    }
}
