/**
 * Mars Simulation Project
 * VehiclePanel.java
 * @version 2.85 2009-01-20
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.tool.mission.create;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.mars_sim.msp.simulation.CollectionUtils;
import org.mars_sim.msp.simulation.Inventory;
import org.mars_sim.msp.simulation.InventoryException;
import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.simulation.person.ai.mission.Mission;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.vehicle.Rover;
import org.mars_sim.msp.simulation.vehicle.Vehicle;
import org.mars_sim.msp.ui.standard.MarsPanelBorder;

/**
 * A wizard panel for selecting the mission vehicle.
 */
class VehiclePanel extends WizardPanel {

	// The wizard panel name.
	private final static String NAME = "Rover";
	
	// Data members.
	private VehicleTableModel vehicleTableModel;
	private JTable vehicleTable;
	private JLabel errorMessageLabel;
	
	/**
	 * Constructor
	 * @param wizard the create mission wizard.
	 */
	VehiclePanel(CreateMissionWizard wizard) {
		// User WizardPanel constructor.
		super(wizard);
		
		// Set the layout.
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		// Set the border.
		setBorder(new MarsPanelBorder());
		
		// Create the select vehicle label.
		JLabel selectVehicleLabel = new JLabel("Select a rover for the mission.", JLabel.CENTER);
		selectVehicleLabel.setFont(selectVehicleLabel.getFont().deriveFont(Font.BOLD));
		selectVehicleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(selectVehicleLabel);
		
		// Create the vehicle panel.
		JPanel vehiclePane = new JPanel(new BorderLayout(0, 0));
		vehiclePane.setMaximumSize(new Dimension(Short.MAX_VALUE, 100));
		vehiclePane.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(vehiclePane);
		
        // Create scroll panel for vehicle list.
        JScrollPane vehicleScrollPane = new JScrollPane();
        vehiclePane.add(vehicleScrollPane, BorderLayout.CENTER);
        
        // Create the vehicle table model.
        vehicleTableModel = new VehicleTableModel();
        
        // Create the vehicle table.
        vehicleTable = new JTable(vehicleTableModel);
        vehicleTable.setDefaultRenderer(Object.class, new UnitTableCellRenderer(vehicleTableModel));
        vehicleTable.setRowSelectionAllowed(true);
        vehicleTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        vehicleTable.getSelectionModel().addListSelectionListener(
        	new ListSelectionListener() {
        		public void valueChanged(ListSelectionEvent e) {
        			if (e.getValueIsAdjusting()) {
        				// Get the selected vehicle index.
        				int index = vehicleTable.getSelectedRow();
        				if (index > -1) {
        					// Check if selected row has a failure cell.
        					if (vehicleTableModel.isFailureRow(index)) {
        						// Set the error message and disable the next button.
        						errorMessageLabel.setText("Rover cannot be used on the mission (see red cells).");
        						getWizard().setButtons(false);
        					}
        					else {
        						// Clear the error message and enable the next button.
        						errorMessageLabel.setText(" ");
        						getWizard().setButtons(true);
        					}
        				}
        			}
        		}
        	});
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
	
	/**
	 * Gets the wizard panel name.
	 * @return panel name.
	 */
	String getPanelName() {
		return NAME;
	}

	/**
	 * Commits changes from this wizard panel.
	 * @retun true if changes can be committed.
	 */
	boolean commitChanges() {
		int selectedIndex = vehicleTable.getSelectedRow();
		Rover selectedVehicle = (Rover) vehicleTableModel.getUnit(selectedIndex);
		getWizard().getMissionData().setRover(selectedVehicle);
		return true;
	}

	/**
	 * Clear information on the wizard panel.
	 */
	void clearInfo() {
		vehicleTable.clearSelection();
		errorMessageLabel.setText(" ");
	}

	/**
	 * Updates the wizard panel information.
	 */
	void updatePanel() {
		vehicleTableModel.updateTable();
		vehicleTable.setPreferredScrollableViewportSize(vehicleTable.getPreferredSize());
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
    		columns.add("Type");
    		columns.add("Crew Cap.");
    		columns.add("Range");
    		columns.add("Lab");
    		columns.add("Sick Bay");
    		columns.add("Cargo Cap.");
            columns.add("Current Cargo");
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
    		Object result = "unknown";
    		
            if (row < units.size()) {
            	Rover vehicle = (Rover) getUnit(row);
            	Inventory inv = vehicle.getInventory();
            	
            	try {
            		if (column == 0) 
            			result = vehicle.getName();
            		else if (column == 1) 
            			result = vehicle.getDescription();
            		else if (column == 2) 
            			result = new Integer(vehicle.getCrewCapacity());
            		else if (column == 3) 
            			result = new Integer((int) vehicle.getRange());
            		else if (column == 4)
            			result = new Boolean(vehicle.hasLab());
            		else if (column == 5)
            			result = new Boolean(vehicle.hasSickBay());
            		else if (column == 6)
            			result = new Integer((int) inv.getGeneralCapacity());
                    else if (column == 7)
                        result = new Integer((int) inv.getTotalInventoryMass());
            		else if (column == 8)
            			result = vehicle.getStatus();
            		else if (column == 9) {
            			Mission mission = Simulation.instance().getMissionManager().getMissionForVehicle(vehicle);
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
    		Settlement startingSettlement = getWizard().getMissionData().getStartingSettlement();
    		Collection<Vehicle> vehicles = CollectionUtils.sortByName(startingSettlement.getParkedVehicles());
    		Iterator<Vehicle> i = vehicles.iterator();
    		while (i.hasNext()) {
    			Vehicle vehicle = i.next();
    			if (vehicle instanceof Rover) units.add(vehicle);
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
    		Rover vehicle = (Rover) getUnit(row);
    		
            if (column == 7) {
                try {
                    if (vehicle.getInventory().getTotalInventoryMass() > 0D) result = true;
                }
                catch (InventoryException e) {
                    e.printStackTrace(System.err);
                }
            }
            else if (column == 8) {
    			if (!vehicle.getStatus().equals(Vehicle.PARKED)) result = true;
                
                // Allow rescue/salvage mission to use vehicle undergoing maintenance.
                String type = getWizard().getMissionData().getType();
                if (MissionDataBean.RESCUE_MISSION.equals(type)) {
                    if (vehicle.getStatus().equals(Vehicle.MAINTENANCE)) result = false;
                }
    		}
    		else if (column == 9) {
    			Mission mission = Simulation.instance().getMissionManager().getMissionForVehicle(vehicle);
    			if (mission != null) result = true;
    		}
    		
    		return result;
    	}
    }
}