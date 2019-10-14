/**
 * Mars Simulation Project
 * ConstructionSettlementPanel.java
 * @version 3.1.0 2017-09-20
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.tool.mission.create;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
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

import org.mars_sim.msp.core.CollectionUtils;
import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.LightUtilityVehicle;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.TableStyle;

/**
 * A wizard panel for selecting the mission's construction settlement.
 */
class ConstructionSettlementPanel extends WizardPanel {

	/** The wizard panel name. */
    private final static String NAME = "Construction Settlement";
    
    // Data members.
    private SettlementTableModel settlementTableModel;
    private JTable settlementTable;
    private JLabel errorMessageLabel;
    
    /**
     * Constructor.
     * @param wizard the create mission wizard.
     */
    public ConstructionSettlementPanel(final CreateMissionWizard wizard) {
        // Use WizardPanel constructor.
        super(wizard);
        
        // Set the layout.
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        // Set the border.
        setBorder(new MarsPanelBorder());
        
        // Create the select settlement label.
        JLabel selectSettlementLabel = new JLabel("Select a settlement to construct a building.", 
                JLabel.CENTER);
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
		TableStyle.setTableStyle(settlementTable);
		settlementTable.setAutoCreateRowSorter(true);        
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
                                errorMessageLabel.setText("Settlement cannot be used in the mission (see red cells).");
                                getWizard().setButtons(false);
                            }
                            else {
                                errorMessageLabel.setText(" ");
                                getWizard().setButtons(true);
                            }
                        }
                    }
                }
            }
        );
		// call it a click to next button when user double clicks the table
		settlementTable.addMouseListener(
			new MouseListener() {
				public void mouseReleased(MouseEvent e) {}
				public void mousePressed(MouseEvent e) {}
				public void mouseExited(MouseEvent e) {}
				public void mouseEntered(MouseEvent e) {}
				public void mouseClicked(MouseEvent e) {
					if (e.getClickCount() == 2 && !e.isConsumed()) {
						wizard.buttonClickedNext();
					}
				}
			}
		);
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
    
    @Override
    void clearInfo() {
        settlementTable.clearSelection();
        errorMessageLabel.setText(" ");
    }

    @Override
    boolean commitChanges() {
        int selectedIndex = settlementTable.getSelectedRow();
        Settlement selectedSettlement = (Settlement) settlementTableModel.getUnit(selectedIndex);
        getWizard().getMissionData().setConstructionSettlement(selectedSettlement);
        return true;
    }

    @Override
    String getPanelName() {
        return NAME;
    }

    @Override
    void updatePanel() {
        settlementTableModel.updateTable();
        settlementTable.setPreferredScrollableViewportSize(settlementTable.getPreferredSize());
    }

    /**
     * A table model for settlements.
     */
    private static class SettlementTableModel extends UnitTableModel {
        
        /** default serial id. */
		private static final long serialVersionUID = 1L;

		/**
         * hidden constructor.
         */
        private SettlementTableModel() {
            // Use UnitTableModel constructor.
            super();
            
            // Add all settlements to table sorted by name.
//            UnitManager manager = Simulation.instance().getUnitManager();
            Collection<Settlement> settlements = CollectionUtils.sortByName(unitManager.getSettlements());
            Iterator<Settlement> i = settlements.iterator();
            while (i.hasNext()) units.add(i.next());
            
            // Add columns.
            columns.add("Name");
            columns.add("Population");
            columns.add("Construction Sites");
            columns.add("Light Utility Vehicles");
            columns.add("EVA Suits");
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
                try {
                    Settlement settlement = (Settlement) getUnit(row);
                    Inventory inv = settlement.getInventory();
                    if (column == 0) 
                        result = settlement.getName();
                    else if (column == 1) 
                        result = settlement.getIndoorPeopleCount();
                    else if (column == 2) {
                        int numSites = settlement.getConstructionManager().getConstructionSites().size();
                        result = numSites;
                    }
                    else if (column == 3) 
                        result = inv.findNumUnitsOfClass(LightUtilityVehicle.class);
                    else if (column == 4) 
                        result = inv.findNumUnitsOfClass(EVASuit.class);
                }
                catch (Exception e) {}
            }
            
            return result;
        }
        
        /**
         * Updates the table data.
         */
        void updateTable() {
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
            
            try {
                if (column == 1) {
                    if (settlement.getIndoorPeopleCount() == 0) result = true;
                }
            }
            catch (Exception e) {}
            
            return result;
        }
    }
}