/**
 * Mars Simulation Project
 * StartingSettlementPanel.java
 * @version 2.84 2008-05-17
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
import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.simulation.UnitManager;
import org.mars_sim.msp.simulation.equipment.Bag;
import org.mars_sim.msp.simulation.equipment.EVASuit;
import org.mars_sim.msp.simulation.equipment.SpecimenContainer;
import org.mars_sim.msp.simulation.person.ai.mission.CollectIce;
import org.mars_sim.msp.simulation.person.ai.mission.Exploration;
import org.mars_sim.msp.simulation.resource.AmountResource;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.ui.standard.MarsPanelBorder;

/**
 * A wizard panel for selecting the mission's starting settlement.
 */
class StartingSettlementPanel extends WizardPanel {

	// The wizard panel name.
	private final static String NAME = "Starting Settlement";
	
	// Data members.
	private SettlementTableModel settlementTableModel;
	private JTable settlementTable;
	private JLabel errorMessageLabel;
	
	/**
	 * Constructor
	 * @param wizard the create mission wizard.
	 */
	StartingSettlementPanel(CreateMissionWizard wizard) {
		// Use WizardPanel constructor.
		super(wizard);
		
		// Set the layout.
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		// Set the border.
		setBorder(new MarsPanelBorder());
		
		// Create the select settlement label.
		JLabel selectSettlementLabel = new JLabel("Select a starting settlement.", JLabel.CENTER);
		selectSettlementLabel.setFont(selectSettlementLabel.getFont().deriveFont(Font.BOLD));
		selectSettlementLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(selectSettlementLabel);
		
		// Create the settlement panel.
		JPanel settlementPane = new JPanel(new BorderLayout(0, 0));
		settlementPane.setMaximumSize(new Dimension(Short.MAX_VALUE, 100));
		settlementPane.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(settlementPane);
		
        // Create scroll panel for settlement list.
        JScrollPane settlementScrollPane = new JScrollPane();
        settlementPane.add(settlementScrollPane, BorderLayout.CENTER);
        
        // Create the settlement table model.
        settlementTableModel = new SettlementTableModel();
        
        // Create the settlement table.
        settlementTable = new JTable(settlementTableModel);
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
        						getWizard().setButtons(false);
        					}
        					else {
        						errorMessageLabel.setText(" ");
        						getWizard().setButtons(true);
        					}
        				}
        			}
        		}
        	});
        settlementTable.setPreferredScrollableViewportSize(settlementTable.getPreferredSize());
        settlementScrollPane.setViewportView(settlementTable);
		
        // Create the error message label.
		errorMessageLabel = new JLabel(" ", JLabel.CENTER);
		errorMessageLabel.setForeground(Color.RED);
		errorMessageLabel.setFont(errorMessageLabel.getFont().deriveFont(Font.BOLD));
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
		int selectedIndex = settlementTable.getSelectedRow();
		Settlement selectedSettlement = (Settlement) settlementTableModel.getUnit(selectedIndex);
		getWizard().getMissionData().setStartingSettlement(selectedSettlement);
		return true;
	}

	/**
	 * Clear information on the wizard panel.
	 */
	void clearInfo() {
		settlementTable.clearSelection();
		errorMessageLabel.setText(" ");
	}
	
	/**
	 * Updates the wizard panel information.
	 */
	void updatePanel() {
		settlementTableModel.updateTable();
		settlementTable.setPreferredScrollableViewportSize(settlementTable.getPreferredSize());
	}
	
	/**
	 * A table model for settlements.
	 */
    private class SettlementTableModel extends UnitTableModel {
    	
    	/**
    	 * Constructor
    	 */
    	private SettlementTableModel() {
    		// Use UnitTableModel constructor.
    		super();
    		
    		// Add all settlements to table sorted by name.
    		UnitManager manager = Simulation.instance().getUnitManager();
    		Collection<Settlement> settlements = CollectionUtils.sortByName(manager.getSettlements());
    		Iterator<Settlement> i = settlements.iterator();
    		while (i.hasNext()) units.add(i.next());
    		
    		// Add columns.
    		columns.add("Name");
    		columns.add("Pop.");
    		columns.add("Rovers");
    		columns.add("Oxygen");
    		columns.add("Water");
    		columns.add("Food");
    		columns.add("Methane");
    		columns.add("EVA Suits");
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
            			if (column == 3) {
            				AmountResource oxygen = AmountResource.findAmountResource("oxygen");
            				result = new Integer((int) inv.getAmountResourceStored(oxygen));
            			}
            			else if (column == 4) {
            				AmountResource water = AmountResource.findAmountResource("water");
            				result = new Integer((int) inv.getAmountResourceStored(water));
            			}
            			else if (column == 5) {
            				AmountResource food = AmountResource.findAmountResource("food");
            				result = new Integer((int) inv.getAmountResourceStored(food));
            			}
            			else if (column == 6) {
            				AmountResource methane = AmountResource.findAmountResource("methane");
            				result = new Integer((int) inv.getAmountResourceStored(methane));
            			}
            			else if (column == 7) 
            				result = new Integer(inv.findNumUnitsOfClass(EVASuit.class));
            			else if (column == 8) {
            				String type = getWizard().getMissionData().getType();
            				if (type.equals(MissionDataBean.EXPLORATION_MISSION))
            					result = new Integer(inv.findNumEmptyUnitsOfClass(SpecimenContainer.class));
            				else if (type.equals(MissionDataBean.ICE_MISSION) || 
            						type.equals(MissionDataBean.REGOLITH_MISSION))
            					result = new Integer(inv.findNumEmptyUnitsOfClass(Bag.class));
            			}
            		}
            		catch (Exception e) {}
            	}
            }
            
            return result;
        }
    	
    	/**
    	 * Updates the table data.
    	 */
    	void updateTable() {
    		if (columns.size() == 9) columns.remove(8);
    		String type = getWizard().getMissionData().getType();
    		if (type.equals(MissionDataBean.EXPLORATION_MISSION)) columns.add("Specimen Containers");
    		else if (type.equals(MissionDataBean.ICE_MISSION) || 
    				type.equals(MissionDataBean.REGOLITH_MISSION)) columns.add("Bags");
    		fireTableStructureChanged();
    	}
    	
    	/**
    	 * Checks if a table cell is a failure cell.
    	 * @param row the table row.
    	 * @param column the table column.
    	 * @return true if cell is a failure cell.
    	 */
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
    				AmountResource oxygen = AmountResource.findAmountResource("oxygen");
    				if (inv.getAmountResourceStored(oxygen) < 100D) result = true;
    			}
    			else if (column == 4) {
    				AmountResource water = AmountResource.findAmountResource("water");
    				if (inv.getAmountResourceStored(water) < 100D) result = true;
    			}
    			else if (column == 5) {
    				AmountResource food = AmountResource.findAmountResource("food");
    				if (inv.getAmountResourceStored(food) < 100D) result = true;
    			}
    			else if (column == 6) {
    				AmountResource methane = AmountResource.findAmountResource("methane");
    				if (inv.getAmountResourceStored(methane) < 100D) result = true;
    			}
    			else if (column == 7) {
    				if (inv.findNumUnitsOfClass(EVASuit.class) == 0) result = true;
    			}
    			else if (column == 8) {
    				String type = getWizard().getMissionData().getType();
    				if (type.equals(MissionDataBean.EXPLORATION_MISSION)) {
    					if (inv.findNumEmptyUnitsOfClass(SpecimenContainer.class) < Exploration.REQUIRED_SPECIMEN_CONTAINERS) result = true;
    				}
    				else if (type.equals(MissionDataBean.ICE_MISSION) || 
    						type.equals(MissionDataBean.REGOLITH_MISSION)) {
    					if (inv.findNumEmptyUnitsOfClass(Bag.class) < CollectIce.REQUIRED_BAGS) result = true;
    				}
    			}
    		}
    		catch (Exception e) {}
    		
    		return result;
    	}
    }
}