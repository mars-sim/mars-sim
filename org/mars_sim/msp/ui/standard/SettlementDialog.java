/**
 * Mars Simulation Project
 * SettlementDialog.java
 * @version 2.74 2002-03-17
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

/** The SettlementDialog class is a detail window for a settlement.
 *  It displays information about the settlement and its status.
 */
public class SettlementDialog extends UnitDialog implements MouseListener {

    // Data members
    private Settlement settlement; // Settlement which the dialog window is about.
    private JList vehicleList; // List of parked vehicles
    private FacilityPanel[] facilityPanes; // Panes for each of the settlement's facilities.
    private InventoryPanel inventoryPane; // The inventory pane.

    /** Constructs a SettlementDialog object 
     *  @param parentDesktop the desktop pane
     *  @param settlementUIProxy the settlement's UI proxy
     */
    public SettlementDialog(MainDesktopPane parentDesktop, 
            SettlementUIProxy settlementUIProxy) {
        
        // Use UnitDialog constructor
        super(parentDesktop, settlementUIProxy);
    }

    /** Complete update (overridden) */
    protected void generalUpdate() {
        updateVehicles();
        for (int x = 0; x < facilityPanes.length; x++) {
            facilityPanes[x].updateInfo();
        }

	inventoryPane.updateInfo();
    }

    /** Update vehicle list */
    protected void updateVehicles() {

        DefaultListModel model = (DefaultListModel) vehicleList.getModel();
        boolean match = false;
        int numVehicles = settlement.getParkedVehicleNum();

        // Check if vehicle list matches settlement's parked vehicles
        if (model.getSize() == numVehicles) {
            match = true;
	    int count = 0;
	    VehicleIterator vehicleIt = settlement.getParkedVehicles().iterator();
	    while (vehicleIt.hasNext()) {
                if (!((String) model.elementAt(count)).equals(vehicleIt.next().getName())) {
                    match = false;
                    break;
                }
	        count++;
            }
        }

        // If no match, update vehicle list
        if (!match) {
            model.removeAllElements();
	    VehicleIterator vehicleIt = settlement.getParkedVehicles().iterator();
	    while (vehicleIt.hasNext()) {
	        model.addElement(vehicleIt.next().getName());
	    }
            validate();
        }
    }

    /** Implement MouseListener Methods */
    public void mouseClicked(MouseEvent event) {
        if (event.getClickCount() >= 2) {
            int index = vehicleList.locationToIndex(event.getPoint());
            if (index > -1) {
                try {
		    VehicleIterator i = settlement.getParkedVehicles().iterator();
		    int count = 0;
		    while (i.hasNext()) {
			Vehicle vehicle = i.next();
		        if (index == count) {
                            parentDesktop.openUnitWindow(proxyManager.getUnitUIProxy(vehicle));
			}
		    }
                } catch (NullPointerException e) {}
            }
        }
    }

    public void mousePressed(MouseEvent event) {}
    public void mouseReleased(MouseEvent event) {}
    public void mouseEntered(MouseEvent event) {}
    public void mouseExited(MouseEvent event) {}

    /** Set window size 
     *  @return the window's size
     */
    protected Dimension setWindowSize() {
        return new Dimension(300, 410);
    }

    /** Prepare new components */
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
        FacilityManager facilityManager = settlement.getFacilityManager();
        
        facilityPanes = new FacilityPanel[5];
        
        // Prepare greenhouse pane
        Greenhouse greenhouse = (Greenhouse) facilityManager.getFacility("Greenhouse");
        GreenhousePanel greenhousePane = new GreenhousePanel(greenhouse, parentDesktop);
        tabPane.add(greenhousePane, greenhousePane.getTabName());
        facilityPanes[0] = greenhousePane;
        
        // Prepare laboratory pane
        Laboratory laboratory = (Laboratory) facilityManager.getFacility("Research Laboratories");
        LaboratoryPanel laboratoryPane = new LaboratoryPanel(laboratory, parentDesktop);
        tabPane.add(laboratoryPane, laboratoryPane.getTabName());
        facilityPanes[1] = laboratoryPane;
        
        // Prepare living quarters pane
        LivingQuartersPanel livingQuartersPane = new LivingQuartersPanel(settlement, parentDesktop);
        tabPane.add(livingQuartersPane, livingQuartersPane.getTabName());
        facilityPanes[2] = livingQuartersPane;
        
        // Prepare maintenance garage pane
        MaintenanceGarage garage = (MaintenanceGarage) facilityManager.getFacility("Maintenance Garage");
        MaintenanceGaragePanel garagePane = new MaintenanceGaragePanel(garage, parentDesktop);
        tabPane.add(garagePane, garagePane.getTabName());
        facilityPanes[3] = garagePane;
        
        // Prepare INSITU resoruce processor pane
        InsituResourceProcessor processor = (InsituResourceProcessor) facilityManager.getFacility("INSITU Resource Processor");
        InsituResourceProcessorPanel processorPane = new InsituResourceProcessorPanel(processor, parentDesktop);
        tabPane.add(processorPane, processorPane.getTabName());
        facilityPanes[4] = processorPane;

	// Prepare inventory pane.
	inventoryPane = new InventoryPanel(settlement.getInventory());
	tabPane.add(inventoryPane, "Inventory");
    }

    /** Prepare vehicles pane 
     *  @return the vehicle pane
     */
    protected JPanel setupVehiclesPane() {

        // Preapre primary pane
        JPanel vehiclesPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
        vehiclesPane.setBorder(
                new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));

        // Prepare inner vehicles pane
        JPanel innerVehiclesPane = new JPanel(new BorderLayout());
        vehiclesPane.add(innerVehiclesPane);

        // Prepare vehicle label
        JLabel vehicleLabel = new JLabel("Parked Vehicles:", JLabel.CENTER);
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
	VehicleIterator i = settlement.getParkedVehicles().iterator();
	while (i.hasNext()) vehicleListModel.addElement(i.next().getName());
        vehicleList = new JList(vehicleListModel);
        vehicleList.setVisibleRowCount(6);
        vehicleList.addMouseListener(this);
        innerVehicleListPane.add(new JScrollPane(vehicleList), "Center");

        // Return primary pane
        return vehiclesPane;
    }

    /** Prepare location pane 
     *  @return the location pane
     */
    protected JPanel setupLocationPane() {

        // Prepare main location pane
        JPanel mainLocationPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
        mainLocationPane.setBorder(
                new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));

        // Prepare location pane
        JPanel locationPane = new JPanel(new BorderLayout());
        mainLocationPane.add(locationPane);

        // Preparing location label pane
        JPanel locationLabelPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
        locationPane.add(locationLabelPane, "North");

        // Prepare center map button
        centerMapButton = new JButton(new ImageIcon("images/CenterMap.gif"));
        centerMapButton.setMargin(new Insets(1, 1, 1, 1));
        centerMapButton.addActionListener(this);
        locationLabelPane.add(centerMapButton);

        // Prepare location label
        JLabel locationLabel = new JLabel("Location:", JLabel.CENTER);
        locationLabelPane.add(locationLabel);

        // Prepare location coordinates pane
        JPanel locationCoordsPane = new JPanel(new GridLayout(1, 2, 0, 0));
        locationPane.add(locationCoordsPane, "Center");

        // Prepare latitude label
        JLabel latitudeLabel = new JLabel("Latitude: " +
                settlement.getCoordinates().getFormattedLatitudeString(),
                JLabel.LEFT);
        locationCoordsPane.add(latitudeLabel);

        // Prepare longitude label

        JLabel longitudeLabel = new JLabel("Longitude: " +
                settlement.getCoordinates().getFormattedLongitudeString(),
                JLabel.LEFT);
        locationCoordsPane.add(longitudeLabel);

        // Return location pane
        return mainLocationPane;
    }
}
