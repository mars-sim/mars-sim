package org.mars_sim.msp.ui.standard.tool.mission.create;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
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

import org.mars_sim.msp.simulation.Inventory;
import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.simulation.person.ai.mission.Mission;
import org.mars_sim.msp.simulation.person.ai.mission.MissionManager;
import org.mars_sim.msp.simulation.person.ai.mission.RescueSalvageVehicle;
import org.mars_sim.msp.simulation.person.ai.mission.VehicleMission;
import org.mars_sim.msp.simulation.resource.AmountResource;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.vehicle.Rover;
import org.mars_sim.msp.simulation.vehicle.Vehicle;
import org.mars_sim.msp.simulation.vehicle.VehicleCollection;
import org.mars_sim.msp.simulation.vehicle.VehicleIterator;
import org.mars_sim.msp.ui.standard.MarsPanelBorder;

class RendezvousVehiclePanel extends WizardPanel {

	private final static String NAME = "Rendezvous Vehicle";
	
	// Data members.
	private VehicleTableModel vehicleTableModel;
	private JTable vehicleTable;
	private JLabel errorMessageLabel;
	
	RendezvousVehiclePanel(CreateMissionWizard wizard) {
		// Use WizardPanel constructor.
		super(wizard);
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(new MarsPanelBorder());
		
		JLabel selectVehicleLabel = new JLabel("Select a rover to rescue/salvage.", JLabel.CENTER);
		selectVehicleLabel.setFont(selectVehicleLabel.getFont().deriveFont(Font.BOLD));
		selectVehicleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(selectVehicleLabel);
		
		JPanel vehiclePane = new JPanel(new BorderLayout(0, 0));
		vehiclePane.setMaximumSize(new Dimension(Short.MAX_VALUE, 100));
		vehiclePane.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(vehiclePane);
		
        // Create scroll panel for vehicle list.
        JScrollPane vehicleScrollPane = new JScrollPane();
        vehiclePane.add(vehicleScrollPane, BorderLayout.CENTER);
        
        vehicleTableModel = new VehicleTableModel();
        vehicleTable = new JTable(vehicleTableModel);
        vehicleTable.setDefaultRenderer(Object.class, new UnitTableCellRenderer(vehicleTableModel));
        vehicleTable.setRowSelectionAllowed(true);
        vehicleTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        vehicleTable.getSelectionModel().addListSelectionListener(
        	new ListSelectionListener() {
        		public void valueChanged(ListSelectionEvent e) {
        			if (e.getValueIsAdjusting()) {
        				int index = vehicleTable.getSelectedRow();
        				if (index > -1) {
        					if (vehicleTableModel.isFailureRow(index)) {
        						errorMessageLabel.setText("Rover cannot be rescued/salvaged (see red cells).");
        						getWizard().setButtonEnabled(CreateMissionWizard.FINAL_BUTTON, false);
        					}
        					else {
        						errorMessageLabel.setText(" ");
        						getWizard().setButtonEnabled(CreateMissionWizard.FINAL_BUTTON, true);
        					}
        				}
        			}
        		}
        	});
        vehicleScrollPane.setViewportView(vehicleTable);
		
		errorMessageLabel = new JLabel(" ", JLabel.CENTER);
		errorMessageLabel.setFont(errorMessageLabel.getFont().deriveFont(Font.BOLD));
		errorMessageLabel.setForeground(Color.RED);
		errorMessageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(errorMessageLabel);
		
		add(Box.createVerticalGlue());
	}
	
	String getPanelName() {
		return NAME;
	}

	void commitChanges() {
		int selectedIndex = vehicleTable.getSelectedRow();
		Rover selectedVehicle = (Rover) vehicleTableModel.getUnit(selectedIndex);
		getWizard().getMissionData().setRescueRover(selectedVehicle);
		getWizard().getMissionData().createMission();
	}

	void clearInfo() {
		vehicleTable.clearSelection();
		errorMessageLabel.setText(" ");
	}

	void updatePanel() {
		vehicleTableModel.updateTable();
		vehicleTable.setPreferredScrollableViewportSize(vehicleTable.getPreferredSize());
	}
	
    private class VehicleTableModel extends UnitTableModel {
    	
    	private VehicleTableModel() {
    		// Use UnitTableModel constructor.
    		super();
    		
    		columns.add("Name");
    		columns.add("Distance");
    		columns.add("Crew");
    		columns.add("Oxygen");
    		columns.add("Water");
    		columns.add("Food");
    		columns.add("Rescuing Rover");
    	}
    	
    	public Object getValueAt(int row, int column) {
    		Object result = "unknown";
    		
            if (row < units.size()) {
            	Rover vehicle = (Rover) getUnit(row);
            	Inventory inv = vehicle.getInventory();
            	
            	try {
            		if (column == 0) 
            			result = vehicle.getName();
            		else if (column == 1) {
                		Settlement startingSettlement = getWizard().getMissionData().getStartingSettlement();
                		double distance = startingSettlement.getCoordinates().getDistance(vehicle.getCoordinates());
                		return new Integer((int) distance);
            		}
            		else if (column == 2) 
            			result = new Integer(vehicle.getCrewNum());
            		else if (column == 3) 
            			result = new Integer((int) inv.getAmountResourceStored(AmountResource.OXYGEN));
                	else if (column == 4) 
                		result = new Integer((int) inv.getAmountResourceStored(AmountResource.WATER));
                	else if (column == 5) 
                		result = new Integer((int) inv.getAmountResourceStored(AmountResource.FOOD));
                	else if (column == 6) {
                		Vehicle rescueVehicle = getRescueVehicle(vehicle);
                		if (rescueVehicle != null) return rescueVehicle.getName();
                		else return "None";
                	}
    			}
            	catch (Exception e) {}
            }
            
            return result;
        }
    	
    	void updateTable() {
    		units.clear();
    		
    		Settlement startingSettlement = getWizard().getMissionData().getStartingSettlement();
    		VehicleCollection emergencyVehicles = getEmergencyBeaconVehicles();
    		
    		// Sort by distance from starting settlement.
    		while (emergencyVehicles.size() > 0) {
    			Vehicle closestVehicle = null;
    			double closestDistance = Double.MAX_VALUE;
    			VehicleIterator i = emergencyVehicles.iterator();
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
    	
    	private VehicleCollection getEmergencyBeaconVehicles() {
    		VehicleCollection result = new VehicleCollection();
        	VehicleIterator i = Simulation.instance().getUnitManager().getVehicles().iterator();
        	while (i.hasNext()) {
        		Vehicle vehicle = i.next();
        		if (vehicle.isEmergencyBeacon()) result.add(vehicle);
        	}
        	return result;
    	}
    	
    	private Vehicle getRescueVehicle(Vehicle emergencyVehicle) {
    		Vehicle result = null;
    		
    	   	MissionManager manager = Simulation.instance().getMissionManager();
        	Iterator i = manager.getMissions().iterator();
        	while (i.hasNext()) {
        		Mission mission = (Mission) i.next();
        		if (mission instanceof RescueSalvageVehicle) {
        			Vehicle vehicleTarget = ((RescueSalvageVehicle) mission).getVehicleTarget();
        			if (emergencyVehicle == vehicleTarget) result = ((VehicleMission) mission).getVehicle();
        		}
        	}
    		
    		return result;
    	}
    	
    	boolean isFailureCell(int row, int column) {
    		boolean result = false;
    		Rover vehicle = (Rover) getUnit(row);
    		
    		if (column == 1) {
    			try {
    				Vehicle missionVehicle = getWizard().getMissionData().getRover();
    				Settlement startingSettlement = getWizard().getMissionData().getStartingSettlement();
    				double distance = startingSettlement.getCoordinates().getDistance(vehicle.getCoordinates());
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