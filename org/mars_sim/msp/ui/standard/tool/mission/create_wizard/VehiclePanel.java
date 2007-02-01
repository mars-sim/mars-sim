package org.mars_sim.msp.ui.standard.tool.mission.create_wizard;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.mars_sim.msp.simulation.Inventory;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.vehicle.Rover;
import org.mars_sim.msp.simulation.vehicle.Vehicle;
import org.mars_sim.msp.simulation.vehicle.VehicleCollection;

class VehiclePanel extends WizardPanel {

	private final static String NAME = "Rover";
	
	// Data members.
	private VehicleTableModel vehicleTableModel;
	private JTable vehicleTable;
	private JLabel errorMessageLabel;
	
	VehiclePanel(CreateMissionWizard wizard) {
		// User WizardPanel constructor.
		super(wizard);
		
		setLayout(new BorderLayout(0, 0));
		
		JLabel selectVehicleLabel = new JLabel("Select a rover for the mission.", JLabel.CENTER);
		add(selectVehicleLabel, BorderLayout.NORTH);
		
		JPanel vehiclePane = new JPanel(new BorderLayout(0, 0));
		// vehiclePane.setPreferredSize(new Dimension(500, 200));
		add(vehiclePane, BorderLayout.CENTER);
		
        // Create scroll panel for vehicle list.
        JScrollPane vehicleScrollPane = new JScrollPane();
        vehiclePane.add(vehicleScrollPane, BorderLayout.CENTER);
        
        vehicleTableModel = new VehicleTableModel();
        vehicleTable = new JTable(vehicleTableModel);
        vehicleTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        vehicleTable.setDefaultRenderer(Object.class, new LocalRenderer());
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
        						getWizard().nextButton.setEnabled(false);
        					}
        					else {
        						errorMessageLabel.setText(" ");
        						getWizard().nextButton.setEnabled(true);
        					}
        				}
        			}
        		}
        	});
        vehicleScrollPane.setViewportView(vehicleTable);
		
		errorMessageLabel = new JLabel(" ", JLabel.CENTER);
		errorMessageLabel.setForeground(Color.RED);
		add(errorMessageLabel, BorderLayout.SOUTH);
	}
	
	String getPanelName() {
		return NAME;
	}

	void commitChanges() {
		int selectedIndex = vehicleTable.getSelectedRow();
		Rover selectedVehicle = vehicleTableModel.getVehicle(selectedIndex);
		getWizard().missionBean.setVehicle(selectedVehicle);
	}

	void clearInfo() {
		vehicleTable.clearSelection();
		errorMessageLabel.setText(" ");
	}

	void updatePanel() {
		vehicleTableModel.updateVehicleList();
	}
	
    private class VehicleTableModel extends AbstractTableModel {
    	
    	VehicleCollection vehicles;
    	List columns;
    	
    	private VehicleTableModel() {
    		
    		vehicles = new VehicleCollection();
    		
    		columns = new ArrayList();
    		columns.add("Name");
    		columns.add("Type");
    		columns.add("Crew Cap.");
    		columns.add("Range");
    		columns.add("Lab");
    		columns.add("Sick Bay");
    		columns.add("Cargo Cap.");
    		columns.add("Status");
    	}
    	
    	public int getRowCount() {
            return vehicles.size();
        }
    	
    	public int getColumnCount() {
            return columns.size();
        }
    	
    	public String getColumnName(int columnIndex) {
    		return (String) columns.get(columnIndex);
        }
    	
    	public Object getValueAt(int row, int column) {
    		Object result = "unknown";
    		
            if (row < vehicles.size()) {
            	Rover vehicle = (Rover) vehicles.get(row);
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
            	}
            	catch (Exception e) {}
            }
            
            return result;
        }
    	
    	Rover getVehicle(int row) {
    		Rover result = null;
    		if ((row > -1) && (row < getRowCount())) 
    			result = (Rover) vehicles.get(row);
    		return result;
    	}
    	
    	void updateVehicleList() {
    		Settlement startingSettlement = getWizard().missionBean.getStartingSettlement();
    		vehicles = startingSettlement.getParkedVehicles().sortByName();
    		fireTableDataChanged();
    	}
    	
    	boolean isFailureCell(int row, int column) {
    		boolean result = false;
    		Rover vehicle = (Rover) vehicles.get(row);
    		
    		if (column == 7) if (!vehicle.getStatus().equals(Vehicle.PARKED)) result = true;
    		
    		return result;
    	}
    	
    	boolean isFailureRow(int row) {
    		boolean result = false;
    		for (int x = 0; x < getColumnCount(); x++) {
    			if (isFailureCell(row, x)) result = true;
    		}
    		return result;
    	}
    }
    
    private class LocalRenderer extends DefaultTableCellRenderer {
    	
    	public Component getTableCellRendererComponent(JTable table, Object value, 
    			boolean isSelected, boolean hasFocus, int row, int column) {
    		
    		Component result = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    		if (vehicleTableModel.isFailureCell(row, column)) setBackground(Color.RED);
    		else if (!isSelected) setBackground(Color.WHITE);
    		
    		return result;
    	}
    }
}