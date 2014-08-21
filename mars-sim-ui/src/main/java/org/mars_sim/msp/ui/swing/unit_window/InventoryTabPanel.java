/**
 * Mars Simulation Project
 * InventoryTabPanel.java
 * @version 3.07 2014-08-21
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.unit_window;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ItemResource;
import org.mars_sim.msp.core.resource.Resource;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.NumberCellRenderer;

/** 
 * The InventoryTabPanel is a tab panel for displaying inventory information.
 */
public class InventoryTabPanel extends TabPanel implements ListSelectionListener {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

    private ResourceTableModel resourceTableModel;
    private EquipmentTableModel equipmentTableModel;
    private JTable equipmentTable;
    
    /**
     * Constructor
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
        equipmentTable.repaint();
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

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		private Inventory inventory;
		private Map<Resource, Number> resources;
		private Map<Resource, Number> capacity;
		private List<Resource> keys;
		private DecimalFormat decFormatter = new DecimalFormat("0.0");
        
        private ResourceTableModel(Inventory inventory) {
            this.inventory = inventory;
            keys = new ArrayList<Resource>();
            resources = new HashMap<Resource, Number>();
            capacity = new HashMap<Resource, Number>();
            
            keys.addAll(inventory.getAllAmountResourcesStored(false));
            Iterator<Resource> iAmount = keys.iterator();
            while (iAmount.hasNext()) {
                AmountResource resource = (AmountResource) iAmount.next();
                resources.put(resource, inventory.getAmountResourceStored(resource, true));
                capacity.put(resource, inventory.getAmountResourceCapacity(resource, true));
            }

            Set<ItemResource> itemResources = inventory.getAllItemResourcesStored();
            keys.addAll(itemResources);
            Iterator<ItemResource> iItem = itemResources.iterator();
            while (iItem.hasNext()) {
                ItemResource resource = iItem.next();
                resources.put(resource, inventory.getItemResourceNum(resource));
                capacity.put(resource, null);
            }

            // Sort resources alphabetically by name.
            Collections.sort(keys);
        }
        
        public int getRowCount() {
            return keys.size();
        }
        
        public int getColumnCount() {
            return 3;
        }
        
        public Class<?> getColumnClass(int columnIndex) {
            Class<?> dataType = super.getColumnClass(columnIndex);
            if (columnIndex == 1) dataType = Double.class;
            return dataType;
        }
        
        public String getColumnName(int columnIndex) {
            if (columnIndex == 0) return "resource";
            else if (columnIndex == 1) return "# or kg";
            else if (columnIndex == 2) return "capacity";
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
            else if (column == 2) {
            	Number number = capacity.get(keys.get(row));
            	return (number == null) ? "-" : decFormatter.format(number);
            }
            else return "unknown";
        }
  
        public void update() {
        	try {
        		List<Resource> newResourceKeys = new ArrayList<Resource>();
        		newResourceKeys.addAll(inventory.getAllAmountResourcesStored(false));
        		Map<Resource, Number> newResources = new HashMap<Resource, Number>();
        		Map<Resource, Number> newCapacity = new HashMap<Resource, Number>();
        		Iterator<Resource> i = newResourceKeys.iterator();
        		while (i.hasNext()) {
        			AmountResource resource = (AmountResource) i.next();
        			newResources.put(resource, inventory.getAmountResourceStored(resource, true));
        			newCapacity.put(resource, inventory.getAmountResourceCapacity(resource, true));
        		}
        		
        		Set<ItemResource> itemResources = inventory.getAllItemResourcesStored();
        		newResourceKeys.addAll(itemResources);
            	Iterator<ItemResource> iItem = itemResources.iterator();
            	while (iItem.hasNext()) {
            		ItemResource resource = iItem.next();
            		newResources.put(resource, inventory.getItemResourceNum(resource));
            		newCapacity.put(resource, null);
            	}

            	// Sort resources alphabetically by name.
                Collections.sort(newResourceKeys);
            
        		if (!resources.equals(newResources)) {
        			resources = newResources;
        			capacity = newCapacity;
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

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		private Inventory inventory;
		private List<Unit> list;
		private Map<String,String> equipment;

		/**
		 * hidden constructor.
		 * @param inventory {@link Inventory}
		 */
		private EquipmentTableModel(Inventory inventory) {
			this.inventory = inventory;
			// Sort equipment alphabetically by name.
			list = new ArrayList<Unit>();

			equipment = new HashMap<String,String>();
			for (Unit unit : inventory.findAllUnitsOfClass(Equipment.class)) {
				equipment.put(
					unit.getName(),
					yesNo(unit.getInventory().isEmpty(true))
				);
				list.add(unit);
			}
			Collections.sort(list);
		}

		private String yesNo(boolean bool) {
			return bool ? "yes" : "no";
		}

		public int getRowCount() {
			return equipment.size();
		}

		public int getColumnCount() {
			return 2;
		}

		public Class<?> getColumnClass(int columnIndex) {
			Class<?> dataType = super.getColumnClass(columnIndex);
			return dataType;
		}

		public String getColumnName(int columnIndex) {
			if (columnIndex == 0) return "equipment";
			else if (columnIndex == 1) return "empty";
			else return "unknown";
		}

		public Object getValueAt(int row, int column) {
			if ((row >= 0) && (row < equipment.size())) {
				if (column == 0) return list.get(row);
				else if (column == 1) return equipment.get(list.get(row).getName());
			}
			return "unknown";
		}

		public void update() {
			List<Unit> newList = new ArrayList<Unit>();
			Map<String,String> newMap = new HashMap<String,String>();
			for (Unit unit : inventory.findAllUnitsOfClass(Equipment.class)) {
				newMap.put(
					unit.getName(),
					yesNo(unit.getInventory().isEmpty(true))
				);
				newList.add(unit);
			};

			// Sort equipment alphabetically by name.
			Collections.sort(newList);
		
			if (!list.equals(newList)) {
				list = newList;
				equipment = newMap;
				fireTableDataChanged();
			}
		}
	}
}
