/**
 * Mars Simulation Project
 * InventoryTabPanel.java
 * @version 2.75 2003-07-06
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.unit_window;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.equipment.*;
import org.mars_sim.msp.ui.standard.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

/** 
 * The InventoryTabPanel is a tab panel for displaying inventory information.
 */
public class InventoryTabPanel extends TabPanel {
    
    private ResourceTableModel resourceTableModel;
    private EquipmentTableModel equipmentTableModel;
    
    /**
     * Constructor
     *
     * @param proxy the UI proxy for the unit.
     * @param desktop the main desktop.
     */
    public InventoryTabPanel(UnitUIProxy proxy, MainDesktopPane desktop) { 
        // Use the TabPanel constructor
        super("Inventory", null, "Inventory", proxy, desktop);
 
        Inventory inv = proxy.getUnit().getInventory();
 
        // Create inventory label panel.
        JPanel inventoryLabelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        topContentPanel.add(inventoryLabelPanel);
        
        // Create inventory label
        JLabel inventoryLabel = new JLabel("Inventory", JLabel.CENTER);
        inventoryLabelPanel.add(inventoryLabel);
        
        // Create inventory content panel
        JPanel inventoryContentPanel = new JPanel(new GridLayout(2, 1, 0, 0));
        centerContentPanel.add(inventoryContentPanel, BorderLayout.CENTER);
        
        // Create resources panel
        JScrollPane resourcesPanel = new JScrollPane();
        resourcesPanel.setBorder(new MarsPanelBorder());
        inventoryContentPanel.add(resourcesPanel);
        
        // Create resources table model
        resourceTableModel = new ResourceTableModel(proxy);
            
        // Create resources table
        JTable resourcesTable = new JTable(resourceTableModel);
        resourcesTable.setPreferredScrollableViewportSize(new Dimension(200, 75));
        resourcesTable.getColumnModel().getColumn(0).setPreferredWidth(120);
        resourcesTable.getColumnModel().getColumn(1).setPreferredWidth(50);
        resourcesTable.setCellSelectionEnabled(false);
        resourcesTable.setDefaultRenderer(Double.class, new NumberCellRenderer());
        resourcesPanel.setViewportView(resourcesTable);
        
        // Create equipment panel
        JScrollPane equipmentPanel = new JScrollPane();
        equipmentPanel.setBorder(new MarsPanelBorder());
        inventoryContentPanel.add(equipmentPanel);
        
        // Create equipment table model
        equipmentTableModel = new EquipmentTableModel(proxy);
        
        // Create equipment table
        JTable equipmentTable = new JTable(equipmentTableModel);
        equipmentTable.setPreferredScrollableViewportSize(new Dimension(200, 75));
        equipmentTable.getColumnModel().getColumn(0).setPreferredWidth(120);
        equipmentTable.getColumnModel().getColumn(1).setPreferredWidth(50);
        equipmentTable.setCellSelectionEnabled(false);
        equipmentTable.setDefaultRenderer(Integer.class, new NumberCellRenderer());
        equipmentPanel.setViewportView(equipmentTable);
    }
    
    /**
     * Updates the info on this panel.
     */
    public void update() {
        resourceTableModel.update();
        equipmentTableModel.update();
    }
    
    /** 
     * Internal class used as model for the resource table.
     */
    private class ResourceTableModel extends AbstractTableModel {
        
        UnitUIProxy proxy;
        java.util.Map resources;
        java.util.List keys;
        
        private ResourceTableModel(UnitUIProxy proxy) {
            this.proxy = proxy;
            resources = proxy.getUnit().getInventory().getAllResources();
            keys = new ArrayList();
            Iterator i = resources.keySet().iterator();
            while (i.hasNext()) {
                Object key = i.next();
                double mass = ((Double) resources.get(key)).doubleValue();
                if (mass > 0D) keys.add(key);
            }
        }
        
        public int getRowCount() {
            return keys.size();
        }
        
        public int getColumnCount() {
            return 2;
        }
        
        public Class getColumnClass(int columnIndex) {
            Class dataType = super.getColumnClass(columnIndex);
            if (columnIndex == 1) dataType = Double.class;
            return dataType;
        }
        
        public String getColumnName(int columnIndex) {
            if (columnIndex == 0) return "Resource";
            else if (columnIndex == 1) return "Mass (kg)";
            else return "unknown";
        }
        
        public Object getValueAt(int row, int column) {
            if (column == 0) return keys.get(row);
            else if (column == 1) return resources.get(keys.get(row));
            else return "unknown";
        }
  
        public void update() {
            java.util.Map newResources = proxy.getUnit().getInventory().getAllResources();
            if (!resources.equals(newResources)) {
                resources = newResources;
                keys = new ArrayList();
                Iterator i = resources.keySet().iterator();
                while (i.hasNext()) {
                    Object key = i.next();
                    double mass = ((Double) resources.get(key)).doubleValue();
                    if (mass > 0D) keys.add(key);
                }
            
                fireTableDataChanged();
            }
        }
    }
    
    /** 
     * Internal class used as model for the equipment table.
     */
    private class EquipmentTableModel extends AbstractTableModel {
        
        UnitUIProxy proxy;
        java.util.Map equipmentMap;
        java.util.Set keys;
        
        private EquipmentTableModel(UnitUIProxy proxy) {
            this.proxy = proxy;
            UnitCollection equipment = proxy.getUnit().getInventory().getUnitsOfClass(Equipment.class);

            equipmentMap = new HashMap();
            keys = equipmentMap.keySet();
            
            UnitIterator i = equipment.iterator();
            while (i.hasNext()) {
                String name = i.next().getName();
                if (keys.contains(name)) {
                    int num = ((Integer) equipmentMap.get(name)).intValue();
                    equipmentMap.put(name, new Integer(++num));
                }
                else equipmentMap.put(name, new Integer(1));
            }
        }
        
        public int getRowCount() {
            return keys.size();
        }
        
        public int getColumnCount() {
            return 2;
        }
        
        public Class getColumnClass(int columnIndex) {
            Class dataType = super.getColumnClass(columnIndex);
            if (columnIndex == 1) dataType = Integer.class;
            return dataType;
        }
        
        public String getColumnName(int columnIndex) {
            if (columnIndex == 0) return "Equipment";
            else if (columnIndex == 1) return "Num";
            else return "unknown";
        }
        
        public Object getValueAt(int row, int column) {
            if (row < keys.size()) {
                
                // Get key value.
                String keyValue = null;
                Iterator i = keys.iterator();
                int count = 0;
                while (i.hasNext()) {
                    String key = (String) i.next();
                    if (count == row) keyValue = key;
                    count ++;
                }
                
                if (column == 0) return keyValue;
                else if (column == 1) {
                    Integer num = (Integer) equipmentMap.get(keyValue);
                    return String.valueOf(num.intValue());
                }
                else return "unknown";
            }
            else return "unknown";
        }
  
        public void update() {
            
            UnitCollection equipment = proxy.getUnit().getInventory().getUnitsOfClass(Equipment.class);
            java.util.Map tempMap = new HashMap();
            java.util.Set tempKeys = tempMap.keySet();
            
            UnitIterator i = equipment.iterator();
            while (i.hasNext()) {
                String name = i.next().getName();
                if (tempKeys.contains(name)) {
                    int num = ((Integer) tempMap.get(name)).intValue();
                    tempMap.put(name, new Integer(++num));
                }
                else tempMap.put(name, new Integer(1));
            }
            
            // If the equipment map cache doesn't match what's in inventory, update it.
            if (!equipmentMap.equals(tempMap)) {
                equipmentMap = tempMap;
                keys = tempKeys;
                fireTableDataChanged();
            }
        }
    }
}
