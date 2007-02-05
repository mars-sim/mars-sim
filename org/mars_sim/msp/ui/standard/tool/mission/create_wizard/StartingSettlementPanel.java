package org.mars_sim.msp.ui.standard.tool.mission.create_wizard;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

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
import org.mars_sim.msp.simulation.structure.SettlementIterator;

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
        settlementTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        settlementTable.setDefaultRenderer(Object.class, new UnitTableCellRenderer(settlementTableModel));
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
		Settlement selectedSettlement = (Settlement) settlementTableModel.getUnit(selectedIndex);
		getWizard().getMissionData().setStartingSettlement(selectedSettlement);
	}

	void clearInfo() {
		settlementTable.clearSelection();
		errorMessageLabel.setText(" ");
	}
	
	void updatePanel() {
		settlementTableModel.updateTable();
	}
	
    private class SettlementTableModel extends UnitTableModel {
    	
    	private SettlementTableModel() {
    		// Use UnitTableModel constructor.
    		super();
    		
    		UnitManager manager = Simulation.instance().getUnitManager();
    		SettlementCollection settlements = manager.getSettlements().sortByName();
    		SettlementIterator i = settlements.iterator();
    		while (i.hasNext()) units.add(i.next());
    		
    		columns.add("Name");
    		columns.add("Pop.");
    		columns.add("Rovers");
    		columns.add("Oxygen");
    		columns.add("Water");
    		columns.add("Food");
    		columns.add("Methane");
    		columns.add("EVA Suits");
    	}
    	
    	public Object getValueAt(int row, int column) {
    		Object result = "unknown";
    		
            if (row < units.size()) {
            	Settlement settlement = (Settlement) getUnit(row);
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
            				String type = getWizard().getMissionData().getType();
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
    	
    	void updateTable() {
    		if (columns.size() == 9) columns.remove(8);
    		String type = getWizard().getMissionData().getType();
    		if (type.equals(MissionDataBean.EXPLORATION_MISSION)) columns.add("Specimen Containers");
    		else if (type.equals(MissionDataBean.ICE_MISSION)) columns.add("Bags");
    		fireTableStructureChanged();
    	}
    	
    	boolean isFailureCell(int row, int column) {
    		boolean result = false;
    		Settlement settlement = (Settlement) getUnit(row);
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
    				String type = getWizard().getMissionData().getType();
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
    }
}