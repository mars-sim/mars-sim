/**
 * Mars Simulation Project
 * SettlementDialog.java
 * @version 2.71 2000-09-22
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard;  
 
import org.mars_sim.msp.simulation.*;  
import java.awt.*;
import java.awt.event.*;
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
    }

    /** Update vehicle list */
    protected void updateVehicles() {

        DefaultListModel model = (DefaultListModel) vehicleList.getModel();
        boolean match = false;
        int numVehicles = settlement.getVehicleNum();

        // Check if vehicle list matches settlement's parked vehicles
        if (model.getSize() == numVehicles) {
            match = true;
            for (int x = 0; x < numVehicles; x++) {
                if (!((String) model.getElementAt(x)).equals(
                        settlement.getVehicle(x).getName())) {
                    match = false;
                    break;
                }
            }
        }

        // If no match, update vehicle list
        if (!match) {
            model.removeAllElements();
            for (int x = 0; x < numVehicles; x++) {
                Vehicle tempVehicle = settlement.getVehicle(x);
                if (tempVehicle != null) {
                    model.addElement(tempVehicle.getName());
                }
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
                    parentDesktop.openUnitWindow(
                            proxyManager.getUnitUIProxy(settlement.getVehicle(index)));
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
        GreenhouseFacility greenhouse = (GreenhouseFacility) facilityManager.getFacility("Greenhouse");
        GreenhouseFacilityPanel greenhousePane = new GreenhouseFacilityPanel(greenhouse, parentDesktop);
        tabPane.add(greenhousePane, greenhousePane.getTabName());
        facilityPanes[0] = greenhousePane;
        
        // Prepare laboratory pane
        LaboratoryFacility laboratory = (LaboratoryFacility) facilityManager.getFacility("Research Laboratories");
        LaboratoryFacilityPanel laboratoryPane = new LaboratoryFacilityPanel(laboratory, parentDesktop);
        tabPane.add(laboratoryPane, laboratoryPane.getTabName());
        facilityPanes[1] = laboratoryPane;
        
        // Prepare living quarters pane
        LivingQuartersFacility livingQuarters = (LivingQuartersFacility) facilityManager.getFacility("Living Quarters");
        LivingQuartersFacilityPanel livingQuartersPane = new LivingQuartersFacilityPanel(livingQuarters, parentDesktop);
        tabPane.add(livingQuartersPane, livingQuartersPane.getTabName());
        facilityPanes[2] = livingQuartersPane;
        
        // Prepare maintenance garage pane
        MaintenanceGarageFacility garage = (MaintenanceGarageFacility) facilityManager.getFacility("Maintenance Garage");
        MaintenanceGarageFacilityPanel garagePane = new MaintenanceGarageFacilityPanel(garage, parentDesktop);
        tabPane.add(garagePane, garagePane.getTabName());
        facilityPanes[3] = garagePane;
        
        // Prepare storeroom pane
        StoreroomFacility storeroom = (StoreroomFacility) facilityManager.getFacility("Storerooms");
        StoreroomFacilityPanel storeroomPane = new StoreroomFacilityPanel(storeroom, parentDesktop);
        tabPane.add(storeroomPane, storeroomPane.getTabName());
        facilityPanes[4] = storeroomPane;
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
        for (int x = 0; x < settlement.getVehicleNum(); x++) {
            vehicleListModel.addElement(settlement.getVehicle(x).getName());
        }
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
        JLabel latitudeLabel = new JLabel("Latitude: " +
                settlement.getCoordinates().getFormattedLatitudeString(),
                JLabel.LEFT);
        latitudeLabel.setForeground(Color.black);
        locationCoordsPane.add(latitudeLabel);

        // Prepare longitude label

        JLabel longitudeLabel = new JLabel("Longitude: " +
                settlement.getCoordinates().getFormattedLongitudeString(),
                JLabel.LEFT);
        longitudeLabel.setForeground(Color.black);
        locationCoordsPane.add(longitudeLabel);

        // Return location pane
        return mainLocationPane;
    }
}
