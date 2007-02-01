package org.mars_sim.msp.ui.standard.tool.mission.create_wizard;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
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
import org.mars_sim.msp.simulation.InventoryException;
import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.simulation.UnitManager;
import org.mars_sim.msp.simulation.equipment.Bag;
import org.mars_sim.msp.simulation.equipment.EVASuit;
import org.mars_sim.msp.simulation.equipment.SpecimenContainer;
import org.mars_sim.msp.simulation.person.ai.mission.CollectIce;
import org.mars_sim.msp.simulation.person.ai.mission.Exploration;
import org.mars_sim.msp.simulation.resource.AmountResource;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.structure.SettlementCollection;

class StartingSettlementPanel extends WizardPanel {

	private final static String NAME = "Starting Settlement";
	
	// Data members.
	private SettlementTableModel settlementTableModel;
	private JTable settlementTable;
	private JLabel errorMessageLabel;
	
	StartingSettlementPanel(CreateMissionWizard wizard) {
		// Use WizardPanel constructor.
		super(wizard);
		
		setLayout(new BorderLayout(0, 0));
		
		JLabel selectSettlementLabel = new JLabel("Select a starting settlement.", JLabel.CENTER);
		add(selectSettlementLabel, BorderLayout.NORTH);
		
		JPanel settlementPane = new JPanel(new BorderLayout(0, 0));
		settlementPane.setPreferredSize(new Dimension(500, 200));
		add(settlementPane, BorderLayout.CENTER);
		
        // Create scroll panel for settlement list.
        JScrollPane settlementScrollPane = new JScrollPane();
        settlementPane.add(settlementScrollPane, BorderLayout.CENTER);
        
        settlementTableModel = new SettlementTableModel();
        settlementTable = new JTable(settlementTableModel);
        settlementTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        settlementTable.setDefaultRenderer(Object.class, new LocalRenderer());
        settlementTable.setRowSelectionAllowed(true);
        settlementTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        settlementTable.getSelectionModel().addListSelectionListener(
        	new ListSelectionListener() {
        		public void valueChanged(ListSelectionEvent e) {
        			if (e.getValueIsAdjusting()) {
        				int index = settlementTable.getSelectedRow();
        				if (index > -1) {
        					if (settlementTableModel.isFailureRow(index)) {
        						errorMessageLabel.setText("Settlement cannot start the mission (see red cells).");
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
        settlementScrollPane.setViewportView(settlementTable);
		
		errorMessageLabel = new JLabel(" ", JLabel.CENTER);
		errorMessageLabel.setForeground(Color.RED);
		add(errorMessageLabel, BorderLayout.SOUTH);
	}
	
	String getPanelName() {
		return NAME;
	}

	void commitChanges() {
		int selectedIndex = settlementTable.getSelectedRow();
		Settlement selectedSettlement = settlementTableModel.getSettlement(selectedIndex);
		getWizard().missionBean.setStartingSettlement(selectedSettlement);
	}

	void clearInfo() {
		settlementTable.clearSelection();
		errorMessageLabel.setText(" ");
	}
	
	void updatePanel() {
		settlementTableModel.updateTableColumns();
	}
	
    private class SettlementTableModel extends AbstractTableModel {
    	
    	SettlementCollection settlements;
    	List columns;
    	
    	private SettlementTableModel() {
    		
    		UnitManager manager = Simulation.instance().getUnitManager();
    		settlements = manager.getSettlements().sortByName();
    		
    		columns = new ArrayList();
    		columns.add("Name");
    		columns.add("Pop.");
    		columns.add("Rovers");
    		columns.add("Oxygen");
    		columns.add("Water");
    		columns.add("Food");
    		columns.add("Methane");
    		columns.add("EVA Suits");
    	}
    	
    	public int getRowCount() {
            return settlements.size();
        }
    	
    	public int getColumnCount() {
            return columns.size();
        }
    	
    	public String getColumnName(int columnIndex) {
    		return (String) columns.get(columnIndex);
        }
    	
    	public Object getValueAt(int row, int column) {
    		Object result = "unknown";
    		
            if (row < settlements.size()) {
            	Settlement settlement = (Settlement) settlements.get(row);
            	Inventory inv = settlement.getInventory();
            	if (column == 0) 
            		result = settlement.getName();
            	else if (column == 1) 
            		result = new Integer(settlement.getCurrentPopulationNum());
            	else if (column == 2) 
            		result = new Integer(settlement.getParkedVehicleNum());
            	else if (column > 2) {
            		try {
            			if (column == 3) 
            				result = new Integer((int) inv.getAmountResourceStored(AmountResource.OXYGEN));
            			else if (column == 4) 
            				result = new Integer((int) inv.getAmountResourceStored(AmountResource.WATER));
            			else if (column == 5) 
            				result = new Integer((int) inv.getAmountResourceStored(AmountResource.FOOD));
            			else if (column == 6) 
            				result = new Integer((int) inv.getAmountResourceStored(AmountResource.METHANE));
            			else if (column == 7) 
            				result = new Integer(inv.findNumUnitsOfClass(EVASuit.class));
            			else if (column == 8) {
            				String type = getWizard().missionBean.getType();
            				if (type.equals(MissionDataBean.EXPLORATION_MISSION))
            					result = new Integer(inv.findNumUnitsOfClass(SpecimenContainer.class));
            				else if (type.equals(MissionDataBean.ICE_MISSION))
            					result = new Integer(inv.findNumUnitsOfClass(Bag.class));
            			}
            		}
            		catch (InventoryException e) {}
            	}
            }
            
            return result;
        }
    	
    	Settlement getSettlement(int row) {
    		Settlement result = null;
    		if ((row > -1) && (row < getRowCount())) 
    			result = (Settlement) settlements.get(row);
    		return result;
    	}
    	
    	void updateTableColumns() {
    		if (columns.size() == 9) columns.remove(8);
    		String type = getWizard().missionBean.getType();
    		if (type.equals(MissionDataBean.EXPLORATION_MISSION)) columns.add("Specimen Containers");
    		else if (type.equals(MissionDataBean.ICE_MISSION)) columns.add("Bags");
    		fireTableStructureChanged();
    	}
    	
    	boolean isFailureCell(int row, int column) {
    		boolean result = false;
    		Settlement settlement = (Settlement) settlements.get(row);
    		Inventory inv = settlement.getInventory();
    		
    		try {
    			if (column == 1) {
    				if (settlement.getCurrentPopulationNum() == 0) result = true;
    			}
    			else if (column == 2) {
    				if (settlement.getParkedVehicleNum() == 0) result = true;
    			}
    			else if (column == 3) {
    				if (inv.getAmountResourceStored(AmountResource.OXYGEN) < 100D) result = true;
    			}
    			else if (column == 4) {
    				if (inv.getAmountResourceStored(AmountResource.WATER) < 100D) result = true;
    			}
    			else if (column == 5) {
    				if (inv.getAmountResourceStored(AmountResource.FOOD) < 100D) result = true;
    			}
    			else if (column == 6) {
    				if (inv.getAmountResourceStored(AmountResource.METHANE) < 100D) result = true;
    			}
    			else if (column == 7) {
    				if (inv.findNumUnitsOfClass(EVASuit.class) == 0) result = true;
    			}
    			else if (column == 8) {
    				String type = getWizard().missionBean.getType();
    				if (type.equals(MissionDataBean.EXPLORATION_MISSION)) {
    					if (inv.findNumUnitsOfClass(SpecimenContainer.class) < Exploration.REQUIRED_SPECIMEN_CONTAINERS) result = true;
    				}
    				else if (type.equals(MissionDataBean.ICE_MISSION)) {
    					if (inv.findNumUnitsOfClass(Bag.class) < CollectIce.REQUIRED_BAGS) result = true;
    				}
    			}
    		}
    		catch (InventoryException e) {}
    		
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
    		if (settlementTableModel.isFailureCell(row, column)) setBackground(Color.RED);
    		else if (!isSelected) setBackground(Color.WHITE);
    		
    		return result;
    	}
    }
}