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
import org.mars_sim.msp.simulation.person.medical.SickBay;
import org.mars_sim.msp.simulation.malfunction.*;
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
    private JLabel settlementMaintTimeLabel; // The settlement maintenance time label.
    private JProgressBar settlementMaintProgressBar; // The settlement maintenance progress bar.
    private Collection facilityMaintTimeLabels; // The facility maintenance time labels.
    private Collection facilityMaintProgressBars; // The facility maintenance progress bars.
    private JPanel malfunctionListPane; // The malfunction list panel.
    private Collection malfunctions; // The malfunction panels for the settlement.
    
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
	updateMaintenance();
	updateMalfunction();
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

    /** Update maintenance */
    protected void updateMaintenance() {

        MalfunctionManager manager = settlement.getMalfunctionManager();
	    
        // Update settlement maint time label.
        int settlementMaintTime = (int) manager.getTimeSinceLastMaintenance();
        settlementMaintTimeLabel.setText("Settlement last: " + settlementMaintTime + " millisols");

	// Update settlement maint progress bar.
        double maintCompleted = manager.getMaintenanceWorkTimeCompleted();
	double maintRequired = manager.getMaintenanceWorkTime();
	settlementMaintProgressBar.setValue((int) (100D * maintCompleted / maintRequired));

	Iterator i = settlement.getFacilityManager().getFacilities();
	Iterator j = facilityMaintTimeLabels.iterator();
	Iterator k = facilityMaintProgressBars.iterator();

	// Update each facility.
	while (i.hasNext()) {
	    Facility facility = (Facility) i.next();
	    manager = facility.getMalfunctionManager();

	    // Update facility maint time label.
	    int facilityMaintTime = (int) manager.getTimeSinceLastMaintenance();
	    ((JLabel) j.next()).setText(facility.getName() + " last: " + facilityMaintTime + " millisols");

	    // Update facility maint progress bar.
            maintCompleted = manager.getMaintenanceWorkTimeCompleted();
	    maintRequired = manager.getMaintenanceWorkTime();
	    ((JProgressBar) k.next()).setValue((int) (100D * maintCompleted / maintRequired));
	}
    }

    /** Update malfunctions */
    protected void updateMalfunction() {

        // Add any new malfunctions from settlement.
	Iterator i = settlement.getMalfunctionManager().getMalfunctions();
	while (i.hasNext()) {
	    Malfunction malfunction = (Malfunction) i.next();
	    boolean match = false;
	    Iterator j = malfunctions.iterator();
	    while (j.hasNext()) {
	        if (((MalfunctionPanel) j.next()).getMalfunction() == malfunction) match = true;
	    }

	    if (!match) {
                MalfunctionPanel mPane = new MalfunctionPanel("Settlement: ", malfunction);
                malfunctions.add(mPane);
                malfunctionListPane.add(mPane);
            }
        }

	// Add any new malfunctions from facilities.
	Iterator k = settlement.getFacilityManager().getFacilities();
	while (k.hasNext()) {
            Facility facility = (Facility) k.next();
	    i = facility.getMalfunctionManager().getMalfunctions();
	    while (i.hasNext()) {
	        Malfunction malfunction = (Malfunction) i.next();
	        boolean match = false;
	        Iterator j = malfunctions.iterator();
	        while (j.hasNext()) {
	            if (((MalfunctionPanel) j.next()).getMalfunction() == malfunction) match = true;
	        }

	        if (!match) {
                    MalfunctionPanel mPane = new MalfunctionPanel(facility.getName() + ": ", malfunction);
                    malfunctions.add(mPane);
                    malfunctionListPane.add(mPane);
                }
            }
	}

	// Remove any fixed malfunctions.
	i = malfunctions.iterator();
	while (i.hasNext()) {
	    MalfunctionPanel mPane = (MalfunctionPanel) i.next();
	    Malfunction malfunction = mPane.getMalfunction();
	    boolean goodMalfunction = false;

	    // Check if settlement has malfunction.
	    if (settlement.getMalfunctionManager().hasMalfunction(malfunction)) goodMalfunction = true;

	    // Check if facilities have malfunction.
	    k = settlement.getFacilityManager().getFacilities();
	    while (k.hasNext()) {
                Facility facility = (Facility) k.next();
		if (facility.getMalfunctionManager().hasMalfunction(malfunction)) goodMalfunction = true;
            }

	    if (!goodMalfunction) {
	        malfunctionListPane.remove(mPane);
		i.remove();
	    }
	}

	// Update malfunction panes.
        i = malfunctions.iterator();
        while (i.hasNext()) ((MalfunctionPanel) i.next()).updateInfo();
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
        return new Dimension(300, 455);
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

        facilityPanes = new FacilityPanel[6];

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

        // Prepare SickBay pane
        SickBay sickbay = ((Infirmary)facilityManager.getFacility("Infirmary")).getSickBay();
        SickBayPanel sickbayPane = new SickBayPanel(sickbay, parentDesktop);
        tabPane.add(sickbayPane, sickbayPane.getTabName());
        facilityPanes[5] = sickbayPane;

	// Prepare inventory pane.
	inventoryPane = new InventoryPanel(settlement.getInventory());
	tabPane.add(inventoryPane, "Inventory");

	// Prepare maintenance pane.
	tabPane.add(setupMaintenancePane(), "Maint");

	// Prepare malfunction pane.
	tabPane.add(setupMalfunctionPane(), "Malfunction");
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
        centerMapButton = new JButton(ImageLoader.getIcon("CenterMap"));
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

    /**
     * Prepare maintenance pane
     * @return the maintenance pane
     */
    protected JPanel setupMaintenancePane() {

        // Prepare maintenance pane
	JPanel maintenancePane = new JPanel(new BorderLayout(0, 5));
        maintenancePane.setBorder(
                new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));

        // Prepare name label
        JLabel nameLabel = new JLabel("Maintenance", JLabel.CENTER);
        maintenancePane.add(nameLabel, "North");

        // Prepare content pane
        JPanel contentPane = new JPanel(new BorderLayout());
	contentPane.setBorder(
	        new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
        maintenancePane.add(contentPane, "Center");

        // Prepare maintenance list pane
	JPanel maintenanceListPane = new JPanel(new GridLayout(0, 1));
	contentPane.add(maintenanceListPane, "North");

        Font smallFont = new Font("SansSerif", Font.PLAIN, 9);
	
        // Prepare settlement maintenance pane
	JPanel settlementMaint = new JPanel(new GridLayout(2, 1));
	settlementMaint.setBorder(
                new CompoundBorder(new EtchedBorder(), new EmptyBorder(3, 3, 3, 3)));
	maintenanceListPane.add(settlementMaint);
	
        // Prepare settlement maintenance time label
        int maintTime = (int) settlement.getMalfunctionManager().getTimeSinceLastMaintenance();
        settlementMaintTimeLabel = new JLabel("Settlement last: " + maintTime + " millisols");
	settlementMaintTimeLabel.setFont(smallFont);
        settlementMaint.add(settlementMaintTimeLabel);

        // Prepare settlement maintenance progress bar
        settlementMaintProgressBar = new JProgressBar();
        settlementMaintProgressBar.setStringPainted(true);
	settlementMaintProgressBar.setFont(smallFont);
        int settlementMaintProgress = 0;
        MalfunctionManager m = settlement.getMalfunctionManager();
	double maintWorkTimeCompleted = m.getMaintenanceWorkTimeCompleted();
	double maintWorkTimeRequired = m.getMaintenanceWorkTime();
        settlementMaintProgress = (int)(100D * maintWorkTimeCompleted / maintWorkTimeRequired);
        settlementMaintProgressBar.setValue(settlementMaintProgress);
        settlementMaint.add(settlementMaintProgressBar);
      
	// Prepare facility maintenance panes
        facilityMaintTimeLabels = new ArrayList();	
	facilityMaintProgressBars = new ArrayList();

	Iterator i = settlement.getFacilityManager().getFacilities();
	while (i.hasNext()) {
	    Facility facility = (Facility) i.next();

            // Prepare facility maintenance pane
	    JPanel facilityMaint = new JPanel(new GridLayout(2, 1));
	    facilityMaint.setBorder(
                    new CompoundBorder(new EtchedBorder(), new EmptyBorder(3, 3, 3, 3)));
	    maintenanceListPane.add(facilityMaint);
	
            // Prepare facility maintenance time label
            int facilityMaintTime = (int) facility.getMalfunctionManager().getTimeSinceLastMaintenance();
            JLabel facilityMaintTimeLabel = new JLabel(facility.getName() + 
                    " last: " + facilityMaintTime + " millisols");
	    facilityMaintTimeLabel.setFont(smallFont);
	    facilityMaintTimeLabels.add(facilityMaintTimeLabel);
            facilityMaint.add(facilityMaintTimeLabel);

            // Prepare facility maintenance progress bar
            JProgressBar facilityMaintProgressBar = new JProgressBar();
            facilityMaintProgressBar.setStringPainted(true);
	    facilityMaintProgressBar.setFont(smallFont);
            int facilityMaintProgress = 0;
            MalfunctionManager malfunct = facility.getMalfunctionManager();
	    double facilityMaintCompleted = malfunct.getMaintenanceWorkTimeCompleted();
	    double facilityMaintRequired = malfunct.getMaintenanceWorkTime();
            facilityMaintProgress = (int)(100D * facilityMaintCompleted / facilityMaintRequired);
            facilityMaintProgressBar.setValue(facilityMaintProgress);
	    facilityMaintProgressBars.add(facilityMaintProgressBar);
            facilityMaint.add(facilityMaintProgressBar);
	}
	
        return maintenancePane;
    }

    /**
     * Prepare malfunction pane
     * @return the malfunction pane
     */
    protected JPanel setupMalfunctionPane() {

        // Prepare malfunction pane
	JPanel malfunctionPane = new JPanel(new BorderLayout(0, 5));
        malfunctionPane.setBorder(
                new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));

        // Prepare name label
        JLabel nameLabel = new JLabel("Malfunction", JLabel.CENTER);
        malfunctionPane.add(nameLabel, "North");

        // Prepare content pane
        JPanel contentPane = new JPanel(new BorderLayout());
	contentPane.setBorder(
	        new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
        malfunctionPane.add(contentPane, "Center");

        // Prepare malfunction list pane
	malfunctionListPane = new JPanel(new GridLayout(0, 1));
	contentPane.add(malfunctionListPane, "North");

        // Prepare malfunctions
	malfunctions = new ArrayList();
        Iterator i = settlement.getMalfunctionManager().getMalfunctions();
	while (i.hasNext()) {
	    MalfunctionPanel m = new MalfunctionPanel("Settlement: ", (Malfunction) i.next());
	    malfunctions.add(m);
	    malfunctionListPane.add(m);
	}

	Iterator j = settlement.getFacilityManager().getFacilities();
	while (j.hasNext()) {
	    Facility facility = (Facility) j.next();
	    Iterator k = facility.getMalfunctionManager().getMalfunctions();
	    while (k.hasNext()) {
                MalfunctionPanel m = new MalfunctionPanel(facility.getName() + ": ", (Malfunction) k.next());
		malfunctions.add(m);
		malfunctionListPane.add(m);
	    }
	}

	return malfunctionPane;
    }
}
