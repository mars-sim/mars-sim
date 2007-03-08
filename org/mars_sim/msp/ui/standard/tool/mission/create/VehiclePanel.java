package org.mars_sim.msp.ui.standard.tool.mission.create;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

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
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.vehicle.Rover;
import org.mars_sim.msp.simulation.vehicle.Vehicle;
import org.mars_sim.msp.simulation.vehicle.VehicleCollection;
import org.mars_sim.msp.simulation.vehicle.VehicleIterator;
import org.mars_sim.msp.ui.standard.MarsPanelBorder;

class VehiclePanel extends WizardPanel {

	private final static String NAME = "Rover";
	
	// Data members.
	private VehicleTableModel vehicleTableModel;
	private JTable vehicleTable;
	private JLabel errorMessageLabel;
	
	VehiclePanel(CreateMissionWizard wizard) {
		// User WizardPanel constructor.
		super(wizard);
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(new MarsPanelBorder());
		
		JLabel selectVehicleLabel = new JLabel("Select a rover for the mission.", JLabel.CENTER);
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
        						errorMessageLabel.setText("Rover cannot be used on the mission (see red cells).");
        						getWizard().setButtonEnabled(CreateMissionWizard.NEXT_BUTTON, false);
        					}
        					else {
        						errorMessageLabel.setText(" ");
        						getWizard().setButtonEnabled(CreateMissionWizard.NEXT_BUTTON, true);
        					}
        				}
        			}
        		}
        	});
        vehicleTable.setPreferredScrollableViewportSize(vehicleTable.getPreferredSize());
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
		getWizard().getMissionData().setRover(selectedVehicle);
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
    		columns.add("Type");
    		columns.add("Crew Cap.");
    		columns.add("Range");
    		columns.add("Lab");
    		columns.add("Sick Bay");
    		columns.add("Cargo Cap.");
    		columns.add("Status");
    		columns.add("Mission");
    	}
    	
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
            			result = vehicle.getStatus();
            		else if (column == 8) {
            			Mission mission = Simulation.instance().getMissionManager().getMissionForVehicle(vehicle);
            			if (mission != null) result = mission.getDescription();
            			else result = "None";
            		}
            	}
            	catch (Exception e) {}
            }
            
            return result;
        }
    	
    	void updateTable() {
    		units.clear();
    		Settlement startingSettlement = getWizard().getMissionData().getStartingSettlement();
    		VehicleCollection vehicles = startingSettlement.getParkedVehicles().sortByName();
    		VehicleIterator i = vehicles.iterator();
    		while (i.hasNext()) units.add(i.next());
    		fireTableDataChanged();
    	}
    	
    	boolean isFailureCell(int row, int column) {
    		boolean result = false;
    		Rover vehicle = (Rover) getUnit(row);
    		
    		if (column == 7) {
    			if (!vehicle.getStatus().equals(Vehicle.PARKED)) result = true;
    		}
    		else if (column == 8) {
    			Mission mission = Simulation.instance().getMissionManager().getMissionForVehicle(vehicle);
    			if (mission != null) result = true;
    		}
    		
    		return result;
    	}
    }
}