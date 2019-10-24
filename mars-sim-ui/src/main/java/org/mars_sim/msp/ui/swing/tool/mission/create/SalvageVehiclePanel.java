/**
 * Mars Simulation Project
 * SalvageVehiclePanel.java
 * @version 3.1.0 2017-09-20
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.tool.mission.create;

import org.mars_sim.msp.core.CollectionUtils;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.construction.ConstructionSite;
import org.mars_sim.msp.core.structure.construction.ConstructionStageInfo;
import org.mars_sim.msp.core.structure.construction.ConstructionUtil;
import org.mars_sim.msp.core.vehicle.GroundVehicle;
import org.mars_sim.msp.core.vehicle.LightUtilityVehicle;
import org.mars_sim.msp.core.vehicle.StatusType;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.TableStyle;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * A wizard panel for selecting the salvage vehicles for a mission.
 */
@SuppressWarnings("serial")
public class SalvageVehiclePanel extends WizardPanel {

    // The wizard panel name.
    private final static String NAME = "Salvage Vehicles";
    
    // Data members.
    private VehicleTableModel vehicleTableModel;
    private JTable vehicleTable;
    private JLabel requiredLabel;
    private JLabel selectedLabel;
    private JLabel errorMessageLabel;
    
	private static MissionManager missionManager;
	
    /**
     * Constructor
     * @param wizard the create mission wizard.
     */
    SalvageVehiclePanel(CreateMissionWizard wizard) {
        // User WizardPanel constructor.
        super(wizard);
        
    	missionManager = Simulation.instance().getMissionManager();
    	
        // Set the layout.
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        // Set the border.
        setBorder(new MarsPanelBorder());
        
        // Create the select vehicle label.
        JLabel selectVehicleLabel = new JLabel("Select the light utility vehicles for the mission.", 
                JLabel.CENTER);
        selectVehicleLabel.setFont(selectVehicleLabel.getFont().deriveFont(Font.BOLD));
        selectVehicleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(selectVehicleLabel);
        
        requiredLabel = new JLabel("Required vehicles: ");
        requiredLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(requiredLabel);
        
        selectedLabel = new JLabel("Number selected: ");
        selectedLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(selectedLabel);
        
        // Create the vehicle panel.
        JPanel vehiclePane = new JPanel(new BorderLayout(0, 0));
        vehiclePane.setMaximumSize(new Dimension(Short.MAX_VALUE, 100));
        vehiclePane.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(vehiclePane);
        
        // Create scroll panel for vehicle list.
        JScrollPane vehicleScrollPane = new JScrollPane();
        vehicleScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        vehiclePane.add(vehicleScrollPane, BorderLayout.CENTER);
        
        // Create the vehicle table model.
        vehicleTableModel = new VehicleTableModel();
        
        // Create the vehicle table.
        vehicleTable = new JTable(vehicleTableModel);
		TableStyle.setTableStyle(vehicleTable);
        vehicleTable.setDefaultRenderer(Object.class, new UnitTableCellRenderer(vehicleTableModel));
        vehicleTable.setRowSelectionAllowed(true);
        vehicleTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        vehicleTable.getSelectionModel().addListSelectionListener(
            new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    if (e.getValueIsAdjusting()) {
                        boolean goodVehicles = false;
                        errorMessageLabel.setText(" ");
                        
                        int requiredVehicles = getRequiredVehiclesNum();
                        
                        // Get the selected vehicle index.
                        int[] indexes = vehicleTable.getSelectedRows();
                        selectedLabel.setText("Number selected: " + indexes.length);
                        if (indexes.length >= requiredVehicles) {
                            goodVehicles = true;
                            for (int indexe : indexes) {
                                if (vehicleTableModel.isFailureRow(indexe)) {
                                    // Set the error message and disable the next button.
                                    errorMessageLabel.setText("Light utility vehicle cannot be " +
                                            "used on the mission (see red cells).");
                                    goodVehicles = false;
                                }
                            }
                        }
                        getWizard().setButtons(goodVehicles);
                    }
                }
            }
        );
        
        vehicleTable.setPreferredScrollableViewportSize(vehicleTable.getPreferredSize());
        vehicleScrollPane.setViewportView(vehicleTable);
        
        // Create the error message label.
        errorMessageLabel = new JLabel(" ", JLabel.CENTER);
        errorMessageLabel.setFont(errorMessageLabel.getFont().deriveFont(Font.BOLD));
        errorMessageLabel.setForeground(Color.RED);
        errorMessageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(errorMessageLabel);
        
        // Add a vertical glue.
        add(Box.createVerticalGlue());
    }
    
    @Override
    void clearInfo() {
        vehicleTable.clearSelection();
        errorMessageLabel.setText(" ");
        requiredLabel.setText("Required vehicles: ");
        selectedLabel.setText("Number selected: ");
    }

    @Override
    boolean commitChanges() {
        List<GroundVehicle> salvageVehicles = new ArrayList<GroundVehicle>();
        int[] selectedIndexs = vehicleTable.getSelectedRows();
        for (int x = 0; x < selectedIndexs.length; x++) {
            LightUtilityVehicle selectedVehicle = 
                (LightUtilityVehicle) vehicleTableModel.getUnit(selectedIndexs[x]);
            
            salvageVehicles.add(selectedVehicle);
        }
        
        getWizard().getMissionData().setSalvageVehicles(salvageVehicles);
        return true;
    }

    @Override
    String getPanelName() {
        return NAME;
    }

    @Override
    void updatePanel() {
        vehicleTableModel.updateTable();
        vehicleTable.setPreferredScrollableViewportSize(vehicleTable.getPreferredSize());
        
        requiredLabel.setText("Required vehicles: " + getRequiredVehiclesNum());
        
        if (getRequiredVehiclesNum() == 0) getWizard().setButtons(true);
    }
    
    /**
     * Gets the number of required salvage vehicles.
     * @return number of vehicles.
     */
    private int getRequiredVehiclesNum() {
        int result = 0;
        
        ConstructionStageInfo salvageInfo = null;
        Building salvageBuilding = getWizard().getMissionData().getSalvageBuilding();
        if (salvageBuilding != null) {
            try {
                salvageInfo = ConstructionUtil.getConstructionStageInfo(salvageBuilding.getBuildingType());
            }
            catch (Exception e) {
                e.printStackTrace(System.err);
            }
        }
        else {
            ConstructionSite salvageSite = getWizard().getMissionData().getSalvageSite();
            salvageInfo = salvageSite.getCurrentConstructionStage().getInfo();
        }
        
        if (salvageInfo != null) result = salvageInfo.getVehicles().size();
        
        return result;
    }
    
    /**
     * A table model for vehicles.
     */
    private class VehicleTableModel extends UnitTableModel {
        
        /**
         * Constructor
         */
        private VehicleTableModel() {
            // Use UnitTableModel constructor.
            super();
            
            // Add columns.
            columns.add("Name");
            columns.add("Status");
            columns.add("Mission");
        }
        
        /**
         * Returns the value for the cell at columnIndex and rowIndex.
         * @param row the row whose value is to be queried
         * @param column the column whose value is to be queried
         * @return the value Object at the specified cell
         */
        public Object getValueAt(int row, int column) {
            Object result = null;
            
            if (row < units.size()) {
                LightUtilityVehicle vehicle = (LightUtilityVehicle) getUnit(row);
                
                try {
                    if (column == 0) 
                        result = vehicle.getName();
                    else if (column == 1) 
                        result = vehicle.printStatusTypes();
                    else if (column == 2) {
                        Mission mission = missionManager.getMissionForVehicle(vehicle);
                        if (mission != null) result = mission.getDescription();
                        else result = "None";
                    }
                }
                catch (Exception e) {}
            }
            
            return result;
        }
        
        /**
         * Updates the table data.
         */
        void updateTable() {
            units.clear();
            Settlement settlement = getWizard().getMissionData().getSalvageSettlement();
            Collection<Vehicle> vehicles = CollectionUtils.sortByName(
                    settlement.getParkedVehicles());
            Iterator<Vehicle> i = vehicles.iterator();
            while (i.hasNext()) {
                Vehicle vehicle = i.next();
                if (vehicle instanceof LightUtilityVehicle) units.add(vehicle);
            }
            fireTableDataChanged();
        }
        
        /**
         * Checks if a table cell is a failure cell.
         * @param row the table row.
         * @param column the table column.
         * @return true if cell is a failure cell.
         */
        boolean isFailureCell(int row, int column) {
            boolean result = false;
            LightUtilityVehicle vehicle = (LightUtilityVehicle) getUnit(row);
            
            if (column == 1) {
				if (!vehicle.haveStatusType(StatusType.PARKED) && !vehicle.haveStatusType(StatusType.GARAGED))
                	result = true;
            }
            else if (column == 2) {
                Mission mission = missionManager.getMissionForVehicle(vehicle);
                if (mission != null) result = true;
            }
            
            return result;
        }
    }
}