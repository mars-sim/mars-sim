/*
 * Mars Simulation Project
 * RendezvousVehiclePanel.java
 * @date 2021-10-21
 * @author Scott Davis
 */

package com.mars_sim.ui.swing.tool.mission.create;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import com.mars_sim.core.person.ai.mission.RescueSalvageVehicle;
import com.mars_sim.core.resource.AmountResource;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.vehicle.Rover;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.ui.swing.MarsPanelBorder;

/**
 * A wizard panel for selecting a mission rendezvous vehicle.
 */
@SuppressWarnings("serial")
class RendezvousVehiclePanel extends WizardPanel {

	/** Wizard panel name. */
	private static final String NAME = "Rendezvous Vehicle";
	
	// Data members.
	private VehicleTableModel vehicleTableModel;
	private JTable vehicleTable;
	private JLabel errorMessageLabel;

	/**
	 * Constructor.
	 * @param wizard the create mission wizard.
	 */
	public RendezvousVehiclePanel(CreateMissionWizard wizard) {
		// Use WizardPanel constructor.
		super(wizard);
				 
		// Set the layout.
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		// Set the border.
		setBorder(new MarsPanelBorder());
		
		// Create the select vehicle label.
		JLabel selectVehicleLabel = createTitleLabel("Select a rover to rescue/salvage.");
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
        	e -> {
				if (e.getValueIsAdjusting()) {
					int index = vehicleTable.getSelectedRow();
					if (index > -1) {
						// Check if selected row has a failed cell.
						if (vehicleTableModel.isFailureRow(index)) {
							// Set error message and disable final button.
							errorMessageLabel.setText("Rover cannot be rescued/salvaged (see red cells).");
							getWizard().setButtons(false);
						}
						else {
							// Clear error message and enable final button.
							errorMessageLabel.setText(" ");
							getWizard().setButtons(true);
						}
					}
				}
			});
        vehicleScrollPane.setViewportView(vehicleTable);
		
        // Create the error message label.
		errorMessageLabel = createErrorLabel();
		add(errorMessageLabel);
		
		// Add vertical glue.
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
	 * 
	 * @param isTesting true if it's only testing conditions
	 * @return true if changes can be committed.
	 */
	@Override
	boolean commitChanges(boolean isTesting) {
		int selectedIndex = vehicleTable.getSelectedRow();
		Rover selectedVehicle = (Rover) vehicleTableModel.getUnit(selectedIndex);
		getWizard().getMissionData().setRescueVehicle(selectedVehicle);
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
    	
        /** default serial id. */
    	private static final long serialVersionUID = 1L;

    	/**
    	 * Constructor.
    	 */
    	private VehicleTableModel() {
    		// Use UnitTableModel constructor.
    		super();
    		
    		// Add columns.
    		columns.add("Name");
    		columns.add("Distance");
    		columns.add("Crew");
    		columns.add("Oxygen");
    		columns.add("Water");
    		columns.add("Food");
    		columns.add("Dessert");
    		columns.add("Rescuing Rover");
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
            	Rover vehicle = (Rover) getUnit(row);
   	
            	try {
            		if (column == 0) 
            			result = vehicle.getName();
            		else if (column == 1) {
                		Settlement startingSettlement = getWizard().getMissionData().getStartingSettlement();
                		double distance = startingSettlement.getCoordinates().getDistance(vehicle.getCoordinates());
                		return (int) distance;
            		}
            		else if (column == 2) 
            			result = vehicle.getCrewNum();
            		else if (column == 3) {
            			result = (int) vehicle.getSpecificAmountResourceStored(ResourceUtil.OXYGEN_ID);
            		}
                	else if (column == 4) {
                		result = (int) vehicle.getSpecificAmountResourceStored(ResourceUtil.WATER_ID);
                	}
                	else if (column == 5) { 
                		result = (int) vehicle.getSpecificAmountResourceStored(ResourceUtil.FOOD_ID);
                	}
                	else if (column == 6) { 
                		AmountResource dessert = ResourceUtil.findAmountResource("Soymilk");
                		result = (int) vehicle.getSpecificAmountResourceStored(dessert.getID());
                	}
                	else if (column == 7) {
                		Vehicle rescueVehicle = getRescueVehicle(vehicle);
                		if (rescueVehicle != null) return rescueVehicle.getName();
                		else return "None";
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
    		Collection<Vehicle> emergencyVehicles = getEmergencyBeaconVehicles();
    		
    		// Sort by distance from starting settlement.
    		while (emergencyVehicles.size() > 0) {
    			Vehicle closestVehicle = null;
    			double closestDistance = Double.MAX_VALUE;
    			Iterator<Vehicle> i = emergencyVehicles.iterator();
    			while (i.hasNext()) {
    				Vehicle vehicle = i.next();
    				double distance = startingSettlement.getCoordinates().getDistance(vehicle.getCoordinates());
    				if (distance < closestDistance) {
    					closestDistance = distance;
    					closestVehicle = vehicle;
    				}
    			}
    			units.add(closestVehicle);
    			emergencyVehicles.remove(closestVehicle);
    		}
    		
    		fireTableDataChanged();
    	}
    	
    	/**
    	 * Gets a collection of all the vehicles with emergency beacons on.
    	 * @return collection of vehicles.
    	 */
    	private Collection<Vehicle> getEmergencyBeaconVehicles() {
    		Collection<Vehicle> result = new ConcurrentLinkedQueue<>();
        	Iterator<Vehicle> i = unitManager.getVehicles().iterator();
        	while (i.hasNext()) {
        		Vehicle vehicle = i.next();
        		if (vehicle.isBeaconOn()) result.add(vehicle);
        	}
        	return result;
    	}
    	
    	/**
    	 * Gets the vehicle currently rescuing a given emergency vehicle.
    	 * @param emergencyVehicle the vehicle in emergency.
    	 * @return rescuing vehicle or null if none.
    	 */
    	private Vehicle getRescueVehicle(Vehicle emergencyVehicle) {
			return RescueSalvageVehicle.getRescueingVehicle(emergencyVehicle);
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
    		
    		if (column == 1) {
    			try {
    				Vehicle missionVehicle = getWizard().getMissionData().getRover();
    				Settlement startingSettlement = getWizard().getMissionData().getStartingSettlement();
    				double distance = startingSettlement.getCoordinates().getDistance(vehicle.getCoordinates()) * 2D;
    				if (distance > missionVehicle.getRange()) result = true;
    			}
    			catch (Exception e) {}
    		}
    		else if (column == 6) {
    			if (getRescueVehicle(vehicle) != null) result = true;
    		}
    		
    		return result;
    	}
    }
}
