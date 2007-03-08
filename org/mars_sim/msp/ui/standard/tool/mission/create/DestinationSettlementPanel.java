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

import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.structure.SettlementCollection;
import org.mars_sim.msp.simulation.structure.SettlementIterator;
import org.mars_sim.msp.ui.standard.MarsPanelBorder;


class DestinationSettlementPanel extends WizardPanel {

	private final static String NAME = "Destination Settlement";
	
	// Data members.
	private SettlementTableModel settlementTableModel;
	private JTable settlementTable;
	private JLabel errorMessageLabel;
	
	DestinationSettlementPanel(CreateMissionWizard wizard) {
		// Use WizardPanel constructor.
		super(wizard);
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(new MarsPanelBorder());
		
		JLabel selectSettlementLabel = new JLabel("Select a destination settlement.", JLabel.CENTER);
		selectSettlementLabel.setFont(selectSettlementLabel.getFont().deriveFont(Font.BOLD));
		selectSettlementLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(selectSettlementLabel);
		
		JPanel settlementPane = new JPanel(new BorderLayout(0, 0));
		settlementPane.setMaximumSize(new Dimension(Short.MAX_VALUE, 100));
		settlementPane.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(settlementPane);
		
        // Create scroll panel for settlement list.
        JScrollPane settlementScrollPane = new JScrollPane();
        settlementPane.add(settlementScrollPane, BorderLayout.CENTER);
        
        settlementTableModel = new SettlementTableModel();
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
        						errorMessageLabel.setText("Selected destination settlement is not within rover range.");
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
        settlementTable.setPreferredScrollableViewportSize(settlementTable.getPreferredSize());
        settlementScrollPane.setViewportView(settlementTable);
		
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
		int selectedIndex = settlementTable.getSelectedRow();
		Settlement selectedSettlement = (Settlement) settlementTableModel.getUnit(selectedIndex);
		getWizard().getMissionData().setDestinationSettlement(selectedSettlement);
		getWizard().getMissionData().createMission();
	}

	void clearInfo() {
		settlementTable.clearSelection();
		errorMessageLabel.setText(" ");
	}

	void updatePanel() {
		settlementTableModel.updateTable();
		settlementTable.setPreferredScrollableViewportSize(settlementTable.getPreferredSize());
	}
	
    private class SettlementTableModel extends UnitTableModel {
    	
    	private SettlementTableModel() {
    		// Use UnitTableModel constructor.
    		super();
    		
    		columns.add("Name");
    		columns.add("Distance");
    		columns.add("Inhabitants");
    		columns.add("Pop. Capacity");
    	}
    	
    	public Object getValueAt(int row, int column) {
    		Object result = "unknown";
    		
            if (row < units.size()) {
            	Settlement settlement = (Settlement) getUnit(row);
            
            	if (column == 0) 
            		result = settlement.getName();
            	else if (column == 1) {
            		Settlement startingSettlement = getWizard().getMissionData().getStartingSettlement();
            		double distance = startingSettlement.getCoordinates().getDistance(settlement.getCoordinates());
            		return new Integer((int) distance);
            	}
            	else if (column == 2) 
            		result = new Integer(settlement.getCurrentPopulationNum());
            	else if (column == 3) {
            		result = new Integer(settlement.getPopulationCapacity());
            	}
            }
            
            return result;
        }
    	
    	void updateTable() {
    		units.clear();
    		Settlement startingSettlement = getWizard().getMissionData().getStartingSettlement();    		
    		SettlementCollection settlements = Simulation.instance().getUnitManager().getSettlements();
    		settlements.remove(startingSettlement);
    		
    		while (settlements.size() > 0) {
    			double smallestDistance = Double.MAX_VALUE;
    			Settlement smallestDistanceSettlement = null;
    			SettlementIterator i = settlements.iterator();
    			while (i.hasNext()) {
    				Settlement settlement = i.next();
    				double distance = startingSettlement.getCoordinates().getDistance(settlement.getCoordinates());
    				if (distance < smallestDistance) {
    					smallestDistance = distance;
    					smallestDistanceSettlement = settlement;
    				}
    			}
    			settlements.remove(smallestDistanceSettlement);
    			units.add(smallestDistanceSettlement);
    		}

    		fireTableDataChanged();
    	}
    	
    	boolean isFailureCell(int row, int column) {
    		boolean result = false;
    		Settlement settlement = (Settlement) getUnit(row);
    		
    		if (column == 1) {
    			try {
    				Settlement startingSettlement = getWizard().getMissionData().getStartingSettlement();
    				double distance = startingSettlement.getCoordinates().getDistance(settlement.getCoordinates());
    				double roverRange = getWizard().getMissionData().getRover().getRange();
    				if (roverRange < distance) result = true;
    			}
    			catch (Exception e) {}
    		}
    		
    		return result;
    	}
    }
}