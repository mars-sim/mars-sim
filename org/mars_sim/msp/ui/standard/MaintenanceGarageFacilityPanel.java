/**
 * Mars Simulation Project
 * MaintenanceGarageFacilityPanel.java
 * @version 2.74 2002-02-28
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard;  
 
import org.mars_sim.msp.simulation.*; 
import org.mars_sim.msp.simulation.structure.*;
import org.mars_sim.msp.simulation.vehicle.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * The MaintenanceGarageFacilityPanel class displays information about a 
 * settlement's maintenance garage facility in the user interface.
 */
public class MaintenanceGarageFacilityPanel extends FacilityPanel implements ActionListener {

    // Data members	
    private MaintenanceGarageFacility maintenanceGarage;  // The maintenance garage facility this panel displays.
    private Vector parkedVehicles;       // A vector of vehicles currently in the garage.
    private Vector vehicleButtons;       // A vector of vehicle buttons.
    private Vector vehicleProgressBars;  // A vector of vehicle progress bars.
    private JPanel vehicleGridPane;      // A UI panel that contains the vehicle buttons and progress bars.
    private JLabel massTotalLabel;       // A label showing the current total of the vehicles mass in the garage.
	
    // Cached garage data
    private double currentTotalMass;   // The current mass of vehicles in the garage.
	
    /** Constructs a MaintenanceGarageFacilityPanel 
     *  @param maintenanceGarage the maintenance garage facility
     *  @param desktop the desktop pane
     */
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
	JPanel labelPane = new JPanel(new GridLayout(0, 1, 0, 5));
	labelPane.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
	infoPane.add(labelPane, "North");
		
	// Prepare vehicle capacity label
	int vehicleCapacity = (int) maintenanceGarage.getVehicleCapacity();
	JLabel vehicleCapacityLabel = new JLabel("Vehicle Mass Capacity: " + vehicleCapacity + " kg.", JLabel.CENTER);
	vehicleCapacityLabel.setForeground(Color.black);
	labelPane.add(vehicleCapacityLabel);
		
	// Prepare current total mass label
	currentTotalMass = (int) maintenanceGarage.getCurrentVehicleMass();
	massTotalLabel = new JLabel("Current Vehicle Mass: " + currentTotalMass + " kg.", JLabel.CENTER);
	massTotalLabel.setForeground(Color.black);
	labelPane.add(massTotalLabel);
		
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
			
	    JLabel vehicleSize = new JLabel(" Mass: " + (int) vehicle.getMass(), JLabel.LEFT);
	    vehicleSize.setForeground(Color.black);
	    vehiclePane.add(vehicleSize, "Center");

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
	
    /** ActionListener method overriden */
    public void actionPerformed(ActionEvent event) {
		
	Object button = event.getSource();
			
	// Check to see if button is a vehicle button.
	for (int x=0; x < parkedVehicles.size(); x++) {
	    if (button == vehicleButtons.elementAt(x)) {
                
                // Open vehicle window
		Vehicle vehicle = (Vehicle) parkedVehicles.elementAt(x);
                UnitUIProxy proxy = desktop.getProxyManager().getUnitUIProxy(vehicle);
                desktop.openUnitWindow(proxy); 
	    }
	}
    }
	
    /** Updates the facility panel's information */
    public void updateInfo() { 
		
	// Change vehicle size total currently if necessary.
	if (currentTotalMass != maintenanceGarage.getCurrentVehicleMass()) {
	    currentTotalMass = maintenanceGarage.getCurrentVehicleMass();
	    massTotalLabel.setText("Current Total Vehicle Size: " + (int) currentTotalMass);
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
