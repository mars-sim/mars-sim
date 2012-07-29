/**
 * Mars Simulation Project
 * InventoryTabPanel.java
 * @version 3.03 2012-07-19
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.unit_window;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ItemResource;
import org.mars_sim.msp.core.resource.Resource;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.NumberCellRenderer;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

/** 
 * The InventoryTabPanel is a tab panel for displaying inventory information.
 */
public class InventoryTabPanel extends TabPanel implements ListSelectionListener {
    
    private ResourceTableModel resourceTableModel;
    private EquipmentTableModel equipmentTableModel;
    private JTable equipmentTable;
    
    /**
     * Constructor
     *
     * @param unit the unit to display.
     * @param desktop the main desktop.
     */
    public InventoryTabPanel(Unit unit, MainDesktopPane desktop) { 
        // Use the TabPanel constructor
        super("Inventory", null, "Inventory", unit, desktop);
 
        Inventory inv = unit.getInventory();
 
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
        resourceTableModel = new ResourceTableModel(inv);
            
        // Create resources table
        JTable resourcesTable = new JTable(resourceTableModel);
        resourcesTable.setPreferredScrollableViewportSize(new Dimension(200, 75));
        resourcesTable.getColumnModel().getColumn(0).setPreferredWidth(120);
        resourcesTable.getColumnModel().getColumn(1).setPreferredWidth(50);
        resourcesTable.setCellSelectionEnabled(false);
        resourcesTable.setDefaultRenderer(Double.class, new NumberCellRenderer(1));
        resourcesPanel.setViewportView(resourcesTable);
        
        // Create equipment panel
        JScrollPane equipmentPanel = new JScrollPane();
        equipmentPanel.setBorder(new MarsPanelBorder());
        inventoryContentPanel.add(equipmentPanel);
        
        // Create equipment table model
        equipmentTableModel = new EquipmentTableModel(inv);
        
        // Create equipment table
        equipmentTable = new JTable(equipmentTableModel);
        equipmentTable.setPreferredScrollableViewportSize(new Dimension(200, 75));
        equipmentTable.setCellSelectionEnabled(true);
        equipmentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        equipmentTable.getSelectionModel().addListSelectionListener(this);
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
     * Called whenever the value of the selection changes.
     *
     * @param e the event that characterizes the change.
     */
    public void valueChanged(ListSelectionEvent e) {
        int index = equipmentTable.getSelectedRow();
        Object selectedEquipment = equipmentTable.getValueAt(index, 0);
        if ((selectedEquipment != null) && (selectedEquipment instanceof Equipment)) 
            desktop.openUnitWindow((Equipment) selectedEquipment, false);
    }
    
    /** 
     * Internal class used as model for the resource table.
     */
    private static class ResourceTableModel extends AbstractTableModel {
        
        private Inventory inventory;
        private Map<Resource, Number> resources;
        private List<Resource> keys;
        private DecimalFormat decFormatter = new DecimalFormat("0.0");
        
        private ResourceTableModel(Inventory inventory) {
            this.inventory = inventory;
            keys = new ArrayList<Resource>();
            resources = new HashMap<Resource, Number>();
            
            keys.addAll(inventory.getAllAmountResourcesStored(false));
            Iterator<Resource> iAmount = keys.iterator();
            while (iAmount.hasNext()) {
                AmountResource resource = (AmountResource) iAmount.next();
                resources.put(resource, inventory.getAmountResourceStored(resource, true));
            }

            Set<ItemResource> itemResources = inventory.getAllItemResourcesStored();
            keys.addAll(itemResources);
            Iterator<ItemResource> iItem = itemResources.iterator();
            while (iItem.hasNext()) {
                ItemResource resource = iItem.next();
                resources.put(resource, inventory.getItemResourceNum(resource));
            }

            // Sort resources alphabetically by name.
            Collections.sort(keys);
        }
        
        public int getRowCount() {
            return keys.size();
        }
        
        public int getColumnCount() {
            return 2;
        }
        
        public Class<?> getColumnClass(int columnIndex) {
            Class<?> dataType = super.getColumnClass(columnIndex);
            if (columnIndex == 1) dataType = Double.class;
            return dataType;
        }
        
        public String getColumnName(int columnIndex) {
            if (columnIndex == 0) return "Resource";
            else if (columnIndex == 1) return "# or Mass (kg)";
            else return "unknown";
        }
        
        public Object getValueAt(int row, int column) {
            if (column == 0) return keys.get(row);
            else if (column == 1) {
            	Resource resource = keys.get(row);
            	String result = resources.get(resource).toString();
            	if (resource instanceof AmountResource) {
            		double amount = (Double) resources.get(resource);
            		result = decFormatter.format(amount);
            	}
            	return result;
            }
            else return "unknown";
        }
  
        public void update() {
        	try {
        		List<Resource> newResourceKeys = new ArrayList<Resource>();
        		newResourceKeys.addAll(inventory.getAllAmountResourcesStored(true));
        		Map<Resource, Number> newResources = new HashMap<Resource, Number>();
        		Iterator<Resource> i = newResourceKeys.iterator();
        		while (i.hasNext()) {
        			AmountResource resource = (AmountResource) i.next();
        			newResources.put(resource, inventory.getAmountResourceStored(resource, true));
        		}
        		
        		Set<ItemResource> itemResources = inventory.getAllItemResourcesStored();
        		newResourceKeys.addAll(itemResources);
            	Iterator<ItemResource> iItem = itemResources.iterator();
            	while (iItem.hasNext()) {
            		ItemResource resource = iItem.next();
            		newResources.put(resource, inventory.getItemResourceNum(resource));
            	}
                
            	// Sort resources alphabetically by name.
                Collections.sort(newResourceKeys);
            
        		if (!resources.equals(newResources)) {
        			resources = newResources;
        			keys = newResourceKeys;
        			fireTableDataChanged();
        		}
        	}
        	catch(Exception e) {
        	    e.printStackTrace(System.err);   
            }
        }
    }
    
    /** 
     * Internal class used as model for the equipment table.
     */
    private static class EquipmentTableModel extends AbstractTableModel {
        
        Inventory inventory;
        List<Unit> equipment;
        
        private EquipmentTableModel(Inventory inventory) {
            this.inventory = inventory;
            Collection<Unit> equipmentCol = inventory.findAllUnitsOfClass(Equipment.class);
            equipment = new ArrayList<Unit>(equipmentCol);
            
            // Sort equipment alphabetically by name.
            Collections.sort(equipment);
        }
        
        public int getRowCount() {
            return equipment.size();
        }
        
        public int getColumnCount() {
            return 1;
        }
        
        public Class<?> getColumnClass(int columnIndex) {
            Class<?> dataType = super.getColumnClass(columnIndex);
            return dataType;
        }
        
        public String getColumnName(int columnIndex) {
            if (columnIndex == 0) return "Equipment";
            else return "unknown";
        }
        
        public Object getValueAt(int row, int column) {
            if ((row >= 0) && (row < equipment.size())) {
                return equipment.get(row);
            }   
            else return "unknown";
        }
  
        public void update() {
            List<Unit> newEquipment = new ArrayList<Unit>(
                    inventory.findAllUnitsOfClass(Equipment.class));
            
            // Sort equipment alphabetically by name.
            Collections.sort(newEquipment);
            
            if (!equipment.equals(newEquipment)) {
                equipment = newEquipment;
                fireTableDataChanged();
            }
        }
    } 
}
