/**
 * Mars Simulation Project
 * PowerGridTabPanel.java
 * @version 2.85 2008-11-25
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.unit_window.structure;

import java.awt.*;
import java.text.DecimalFormat;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import org.mars_sim.msp.simulation.Unit;
import org.mars_sim.msp.simulation.structure.*;
import org.mars_sim.msp.simulation.structure.building.Building;
import org.mars_sim.msp.simulation.structure.building.function.PowerGeneration;
import org.mars_sim.msp.ui.standard.*;
import org.mars_sim.msp.ui.standard.unit_window.TabPanel;

/** 
 * The PowerGridTabPanel is a tab panel for a settlement's power grid information.
 */
public class PowerGridTabPanel extends TabPanel {
    
    // Data Members
    private JLabel powerGeneratedLabel; // The total power generated label.
    private JLabel powerUsedLabel; // The total power used label.
    private JLabel powerStorageCapacityLabel; // The total power storage capacity label.
    private JLabel powerStoredLabel; // The total power stored label.
    private PowerTableModel powerTableModel; // Table model for power info.
    private PowerGrid powerGrid; // The settlement's power grid.
    
    // Data cache
    private double powerGeneratedCache; // The total power generated cache.
    private double powerUsedCache; // The total power used cache.
    private double powerStorageCapacityCache; // The total power storage capacity cache.
    private double powerStoredCache; // The total power stored cache.
    
    private DecimalFormat formatter = new DecimalFormat("0.0");
    
    /**
     * Constructor
     *
     * @param unit the unit to display.
     * @param desktop the main desktop.
     */
    public PowerGridTabPanel(Unit unit, MainDesktopPane desktop) { 
        
        // Use the TabPanel constructor
        super("Power", null, "Power Grid", unit, desktop);
        
        Settlement settlement = (Settlement) unit;
        powerGrid = settlement.getPowerGrid();
        
        // Prepare power grid label panel.
        JPanel powerGridLabelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        topContentPanel.add(powerGridLabelPanel);
        
        // Prepare power grid label.
        JLabel powerGridLabel = new JLabel("Power Grid", JLabel.CENTER);
        powerGridLabelPanel.add(powerGridLabel);
        
        // Prepare power info panel.
        JPanel powerInfoPanel = new JPanel(new GridLayout(4, 1, 0, 0));
        powerInfoPanel.setBorder(new MarsPanelBorder());
        topContentPanel.add(powerInfoPanel);
        
        // Prepare power generated label.
        powerGeneratedCache = powerGrid.getGeneratedPower();
        powerGeneratedLabel = new JLabel("Total Power Generated: " + 
            formatter.format(powerGeneratedCache) + " kW.", JLabel.CENTER);
        powerInfoPanel.add(powerGeneratedLabel);
        
        // Prepare power used label.
        powerUsedCache = powerGrid.getRequiredPower();
        powerUsedLabel = new JLabel("Total Power Used: " + 
            formatter.format(powerUsedCache) + " kW.", JLabel.CENTER);
        powerInfoPanel.add(powerUsedLabel);
        
        // Prepare power storage capacity label.
        powerStorageCapacityCache = powerGrid.getStoredPowerCapacity();
        powerStorageCapacityLabel = new JLabel("Power Storage Capacity: " + 
                formatter.format(powerStorageCapacityCache) + " kW hr.", JLabel.CENTER);
        powerInfoPanel.add(powerStorageCapacityLabel);
        
        // Prepare power stored label.
        powerStoredCache = powerGrid.getStoredPower();
        powerStoredLabel = new JLabel("Total Power Stored: " +
                formatter.format(powerStoredCache) + " kW hr.", JLabel.CENTER);
        powerInfoPanel.add(powerStoredLabel);
        
		// Create scroll panel for the outer table panel.
		JScrollPane powerScrollPanel = new JScrollPane();
		powerScrollPanel.setPreferredSize(new Dimension(257, 230));
		topContentPanel.add(powerScrollPanel);         
        
        // Prepare outer table panel.
        JPanel outerTablePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        outerTablePanel.setBorder(new MarsPanelBorder());
        powerScrollPanel.setViewportView(outerTablePanel);   
        
        // Prepare power table panel.
        JPanel powerTablePanel = new JPanel(new BorderLayout(0, 0));
        outerTablePanel.add(powerTablePanel);
        // powerScrollPanel.setViewportView(powerTablePanel);
        
        // Prepare power table model.
        powerTableModel = new PowerTableModel(settlement);
        
        // Prepare power table.
        JTable powerTable = new JTable(powerTableModel);
        powerTable.setCellSelectionEnabled(false);
        powerTable.setDefaultRenderer(Double.class, new NumberCellRenderer());
        powerTable.getColumnModel().getColumn(0).setPreferredWidth(10);
        powerTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        powerTable.getColumnModel().getColumn(2).setPreferredWidth(40);
        powerTable.getColumnModel().getColumn(3).setPreferredWidth(40);
        powerTablePanel.add(powerTable.getTableHeader(), BorderLayout.NORTH);
        powerTablePanel.add(powerTable, BorderLayout.CENTER);
    }
    
    /**
     * Updates the info on this panel.
     */
    public void update() {
       
        // Update power generated label.
        if (powerGeneratedCache != powerGrid.getGeneratedPower()) {
            powerGeneratedCache = powerGrid.getGeneratedPower();
            powerGeneratedLabel.setText("Total Power Generated: " + 
                formatter.format(powerGeneratedCache) + " kW.");
        }
        
        // Update power used label.
        if (powerUsedCache != powerGrid.getRequiredPower()) {
            powerUsedCache = powerGrid.getRequiredPower();
            powerUsedLabel.setText("Total Power Used: " + 
                formatter.format(powerUsedCache) + " kW.");
        }
        
        // Update power storage capacity label.
        if (powerStorageCapacityCache != powerGrid.getStoredPowerCapacity()) {
            powerStorageCapacityCache = powerGrid.getStoredPowerCapacity();
            powerStorageCapacityLabel.setText("Power Storage Capacity: " + 
                formatter.format(powerStorageCapacityCache) + " kW hr.");
        }
        
        // Update poewr stored label.
        if (powerStoredCache != powerGrid.getStoredPower()) {
            powerStoredCache = powerGrid.getStoredPower();
            powerStoredLabel.setText("Total Power Stored: " +
                    formatter.format(powerStoredCache) + " kW hr.");
        }
        
        // Update power table.
        powerTableModel.update();
    }
    
    /** 
     * Internal class used as model for the power table.
     */
    private class PowerTableModel extends AbstractTableModel {
        
        Settlement settlement;
        java.util.List buildings;
        ImageIcon redDot;
        ImageIcon yellowDot;
        ImageIcon greenDot;
        
        private PowerTableModel(Settlement settlement) {
            this.settlement = settlement;
            buildings = settlement.getBuildingManager().getBuildings();
            redDot = new ImageIcon("images/RedDot.gif");
            yellowDot = new ImageIcon("images/YellowDot.gif");
            greenDot = new ImageIcon("images/GreenDot.gif");
        }
        
        public int getRowCount() {
            return buildings.size();
        }
        
        public int getColumnCount() {
            return 4;
        }
        
        public Class<?> getColumnClass(int columnIndex) {
            Class dataType = super.getColumnClass(columnIndex);
            if (columnIndex == 0) dataType = ImageIcon.class;
            else if (columnIndex == 1) dataType = String.class;
            else if (columnIndex == 2) dataType = Double.class;
            else if (columnIndex == 3) dataType = Double.class;
            return dataType;
        }
        
        public String getColumnName(int columnIndex) {
            if (columnIndex == 0) return "S";
            else if (columnIndex == 1) return "Building";
            else if (columnIndex == 2) return "Gen.";
            else if (columnIndex == 3) return "Used";
            else return "unknown";
        }
        
        public Object getValueAt(int row, int column) {
            
            Building building = (Building) buildings.get(row);
            String powerMode = building.getPowerMode();
            
            if (column == 0) {
                if (powerMode.equals(Building.FULL_POWER)) { 
                    return greenDot;
                }
                else if (powerMode.equals(Building.POWER_DOWN)) {
                    return yellowDot;
                }
                else if (powerMode.equals(Building.NO_POWER)) {
                    return redDot;
                }
                else return null;
            }
            else if (column == 1) return buildings.get(row);
            else if (column == 2) {
                double generated = 0D;
                if (building.hasFunction(PowerGeneration.NAME)) {
                	try {
                		PowerGeneration generator = (PowerGeneration) building.getFunction(PowerGeneration.NAME);
                    	generated = generator.getGeneratedPower();
                	}
                	catch (Exception e) {}
                }
                return new Double(generated);
            }
            else if (column == 3) {
                double used = 0D;
                if (powerMode.equals(Building.FULL_POWER)) 
                    used = building.getFullPowerRequired();
                else if (powerMode.equals(Building.POWER_DOWN))
                    used = building.getPoweredDownPowerRequired();
                return new Double(used);
            }
            else return "unknown";
        }
  
        public void update() {
            if (!buildings.equals(settlement.getBuildingManager().getBuildings())) 
                buildings = settlement.getBuildingManager().getBuildings();
                
            fireTableDataChanged();
        }
    }
}