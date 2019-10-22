/**
 * Mars Simulation Project
 * RendezvousVehiclePanel.java
 * @version 3.1.0 2017-09-20
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.tool.mission.create;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LifeSupportInterface;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
import org.mars_sim.msp.core.person.ai.mission.RescueSalvageVehicle;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.TableStyle;

/**
 * A wizard panel for selecting a mission rendezvous vehicle.
 */
class RendezvousVehiclePanel extends WizardPanel {

	/** Wizard panel name. */
	private final static String NAME = "Rendezvous Vehicle";
	
	// Data members.
	private VehicleTableModel vehicleTableModel;
	private JTable vehicleTable;
	private JLabel errorMessageLabel;
	
	private static MissionManager missionManager;
	private static UnitManager unitManager = Simulation.instance().getUnitManager();
	
	/**
	 * Constructor.
	 * @param wizard the create mission wizard.
	 */
	public RendezvousVehiclePanel(CreateMissionWizard wizard) {
		// Use WizardPanel constructor.
		super(wizard);
		
		missionManager = Simulation.instance().getMissionManager();
		 
		// Set the layout.
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		// Set the border.
		setBorder(new MarsPanelBorder());
		
		// Create the select vehicle label.
		JLabel selectVehicleLabel = new JLabel("Select a rover to rescue/salvage.", JLabel.CENTER);
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
		TableStyle.setTableStyle(vehicleTable);
        vehicleTable.setDefaultRenderer(Object.class, new UnitTableCellRenderer(vehicleTableModel));
        vehicleTable.setRowSelectionAllowed(true);
        vehicleTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        vehicleTable.getSelectionModel().addListSelectionListener(
        	new ListSelectionListener() {
        		public void valueChanged(ListSelectionEvent e) {
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
        		}
        	});
        vehicleScrollPane.setViewportView(vehicleTable);
		
        // Create the error message label.
		errorMessageLabel = new JLabel(" ", JLabel.CENTER);
		errorMessageLabel.setFont(errorMessageLabel.getFont().deriveFont(Font.BOLD));
		errorMessageLabel.setForeground(Color.RED);
		errorMessageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
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
	 * @retun true if changes can be committed.
	 */
	boolean commitChanges() {
		int selectedIndex = vehicleTable.getSelectedRow();
		Rover selectedVehicle = (Rover) vehicleTableModel.getUnit(selectedIndex);
		getWizard().getMissionData().setRescueRover(selectedVehicle);
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
            	Inventory inv = vehicle.getInventory();
            	
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
            			AmountResource oxygen = ResourceUtil.findAmountResource(LifeSupportInterface.OXYGEN);
            			result = (int) inv.getAmountResourceStored(oxygen, false);
            		}
                	else if (column == 4) {
                		AmountResource water = ResourceUtil.findAmountResource(LifeSupportInterface.WATER);
                		result = (int) inv.getAmountResourceStored(water, false);
                	}
                	else if (column == 5) { 
                		AmountResource food = ResourceUtil.findAmountResource(LifeSupportInterface.FOOD);
                		result = (int) inv.getAmountResourceStored(food, false);
                	}
                	else if (column == 6) { 
                		AmountResource dessert = ResourceUtil.findAmountResource("Soymilk");
                		result = (int) inv.getAmountResourceStored(dessert, false);
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
    		Collection<Vehicle> result = new ConcurrentLinkedQueue<Vehicle>();
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
    		Vehicle result = null;
    		
    	   	//MissionManager manager = Simulation.instance().getMissionManager();
        	Iterator<?> i = missionManager.getMissions().iterator();
        	while (i.hasNext()) {
        		Mission mission = (Mission) i.next();
        		if (mission instanceof RescueSalvageVehicle) {
        			Vehicle vehicleTarget = ((RescueSalvageVehicle) mission).getVehicleTarget();
        			if (emergencyVehicle == vehicleTarget) result = ((VehicleMission) mission).getVehicle();
        		}
        	}
    		
    		return result;
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
    				if (distance > missionVehicle.getRange(wizard.getMissionBean().getMissionType())) result = true;
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