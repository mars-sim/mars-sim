/**
 * Mars Simulation Project
 * InventoryTabPanel.java
 * @version 3.1.0 2017-09-08
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.unit_window;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ItemResource;
import org.mars_sim.msp.core.resource.Resource;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.NumberCellRenderer;
import org.mars_sim.msp.ui.swing.tool.Conversion;
import org.mars_sim.msp.ui.swing.tool.TableStyle;
import org.mars_sim.msp.ui.swing.tool.ZebraJTable;

import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.scroll.WebScrollPane;
import com.alee.laf.table.WebTable;
import com.jidesoft.swing.SearchableUtils;
import com.jidesoft.swing.TableSearchable;

/**
 * The InventoryTabPanel is a tab panel for displaying inventory information.
 */
public class InventoryTabPanel extends TabPanel implements ListSelectionListener {

	/** default logger. */
	private static Logger logger = Logger.getLogger(InventoryTabPanel.class.getName());

    private ResourceTableModel resourceTableModel;
    private EquipmentTableModel equipmentTableModel;
    private WebTable equipmentTable;
    private WebTable resourcesTable;
    

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
        WebPanel inventoryLabelPanel = new WebPanel(new FlowLayout(FlowLayout.CENTER));
        topContentPanel.add(inventoryLabelPanel);

        // Create inventory label
        WebLabel titleLabel = new WebLabel("Inventory", WebLabel.CENTER);
		titleLabel.setFont(new Font("Serif", Font.BOLD, 16));
		//titleLabel.setForeground(new Color(102, 51, 0)); // dark brown
        inventoryLabelPanel.add(titleLabel);

        // Create inventory content panel
        WebPanel inventoryContentPanel = new WebPanel(new GridLayout(2, 1, 0, 0));
        centerContentPanel.add(inventoryContentPanel, BorderLayout.CENTER);

        // Create resources panel
        WebScrollPane resourcesPanel = new WebScrollPane();
        resourcesPanel.setBorder(new MarsPanelBorder());
        inventoryContentPanel.add(resourcesPanel);

        // Create resources table model
        resourceTableModel = new ResourceTableModel(inv);

        // Create resources table
        resourcesTable = new ZebraJTable(resourceTableModel);
        resourcesTable.setPreferredScrollableViewportSize(new Dimension(200, 75));
        resourcesTable.setDefaultRenderer(Double.class, new NumberCellRenderer(2));
        resourcesTable.getColumnModel().getColumn(0).setPreferredWidth(120);
        resourcesTable.getColumnModel().getColumn(1).setPreferredWidth(50);
        resourcesTable.getColumnModel().getColumn(2).setPreferredWidth(50);
        resourcesTable.setCellSelectionEnabled(false);
        resourcesPanel.setViewportView(resourcesTable);

		// Added sorting
        resourcesTable.setAutoCreateRowSorter(true);

		// Align the preference score to the center of the cell
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setHorizontalAlignment(SwingConstants.CENTER);
		resourcesTable.getColumnModel().getColumn(0).setCellRenderer(renderer);
		resourcesTable.getColumnModel().getColumn(1).setCellRenderer(renderer);
		resourcesTable.getColumnModel().getColumn(2).setCellRenderer(renderer);

		// Added setTableStyle()
		TableStyle.setTableStyle(resourcesTable);

     	// Added resourcesSearchable
     	TableSearchable searchable = SearchableUtils.installSearchable(resourcesTable);
        searchable.setPopupTimeout(5000);
     	searchable.setCaseSensitive(false);

        // Create equipment panel
        WebScrollPane equipmentPanel = new WebScrollPane();
        equipmentPanel.setBorder(new MarsPanelBorder());
        inventoryContentPanel.add(equipmentPanel);

        // Create equipment table model
        equipmentTableModel = new EquipmentTableModel(inv);

        // Create equipment table
        equipmentTable = new ZebraJTable(equipmentTableModel);
        equipmentTable.setPreferredScrollableViewportSize(new Dimension(200, 75));
        equipmentTable.setCellSelectionEnabled(true);
        equipmentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        equipmentTable.getSelectionModel().addListSelectionListener(this);
        equipmentPanel.setViewportView(equipmentTable);


		// Align the preference score to the center of the cell
		DefaultTableCellRenderer renderer2 = new DefaultTableCellRenderer();
		renderer2.setHorizontalAlignment(SwingConstants.CENTER);
		equipmentTable.getColumnModel().getColumn(0).setCellRenderer(renderer2);
		equipmentTable.getColumnModel().getColumn(1).setCellRenderer(renderer2);

		// Added sorting
        equipmentTable.setAutoCreateRowSorter(true);

		// Added setTableStyle()
		TableStyle.setTableStyle(equipmentTable);

    }

    /**
     * Updates the info on this panel.
     */
    public void update() {
        resourceTableModel.update();
        equipmentTableModel.update();
		TableStyle.setTableStyle(resourcesTable);
		TableStyle.setTableStyle(equipmentTable);
        equipmentTable.repaint();
    }

    /**
     * Called whenever the value of the selection changes.
     *
     * @param e the event that characterizes the change.
     */
    public void valueChanged(ListSelectionEvent e) {
        int index = equipmentTable.getSelectedRow();
        if (index > 0) {
	        Object selectedEquipment = equipmentTable.getValueAt(index, 0);
	        if ((selectedEquipment != null) && (selectedEquipment instanceof Equipment))
	            desktop.openUnitWindow((Equipment) selectedEquipment, false);
        }
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
		private DecimalFormat decFormatter = new DecimalFormat("#,###,##0.0");

        private ResourceTableModel(Inventory inventory) {
            this.inventory = inventory;
            keys = new ArrayList<Resource>();
            resources = new HashMap<Resource, Number>();
            capacity = new HashMap<Resource, Number>();

            keys.addAll(inventory.getAllAmountResourcesStored(false));
            Iterator<Resource> iAmount = keys.iterator();
            while (iAmount.hasNext()) {
                AmountResource resource = (AmountResource) iAmount.next();
                resources.put(resource, inventory.getAmountResourceStored(resource, false));
                capacity.put(resource, inventory.getAmountResourceCapacity(resource, false));
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
			// 2014-11-17 Internationalized and capitalized column headers
            if (columnIndex == 0) return Msg.getString("InventoryTabPanel.Resource.header.name");
            else if (columnIndex == 1) return Msg.getString("InventoryTabPanel.Resource.header.quantity");
            else if (columnIndex == 2) return Msg.getString("InventoryTabPanel.Resource.header.capacity");
            else return "unknown";
        }

        public Object getValueAt(int row, int column) {
            if (column == 0) {
    			// 2014-11-17 Capitalize Resource Names
            	Object result = keys.get(row);
            	return Conversion.capitalize(result.toString());
            }
            else if (column == 1) {
            	Resource resource = keys.get(row);
            	String result = resources.get(resource).toString();
            	if (resource instanceof AmountResource) {
            		double amount = (Double) resources.get(resource);
            		result = decFormatter.format(amount);
            		//result = amount + "";
            	}
            	return result;
            }
            else if (column == 2) {
            	Number number = capacity.get(keys.get(row));
            	return (number == null) ? "-" : decFormatter.format(number);
            	//return (number == null) ? "-" : number;
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
        			newResources.put(resource, inventory.getAmountResourceStored(resource, false));
        			newCapacity.put(resource, inventory.getAmountResourceCapacity(resource, false));
        		}

        		Set<ItemResource> itemResources = inventory.getAllItemResourcesStored();
        		newResourceKeys.addAll(itemResources);
            	//Iterator<ItemResource> iItem = itemResources.iterator();
            	//while (iItem.hasNext()) {
            	for (ItemResource resource : itemResources) {//= iItem.next();
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
					showOwner(unit)//.getInventory())//.isEmpty(true))
				);
				list.add(unit);
			}
			Collections.sort(list);
		}

		private String showOwner(Unit unit) {
			String s = "";
			String item = unit.getName().toLowerCase();
			if (item.contains("eva")) {
				Person p = (Person) ((Equipment) unit).getLastOwner();
				if (p != null)
					s = p.getName();
				//else 
				//	s = unit.getContainerUnit().getName();		
			}
			else if (unit instanceof Robot) {
				;// TODO: determine ownership of a bot
			}
			//if (item.contains("box") || item.contains("canister") 
			//		|| item.contains("barrel") || item.contains("bag")) {
			//	Person p = (Person) ((Equipment) unit).getLastOwner();	
			//}
			else {		
				List<AmountResource> ars = new ArrayList<>(unit.getInventory().getAllAmountResourcesStored(false));
				if (ars.size() > 1) logger.info(unit.getName() + " has " + ars.size() + " resources.");
				//for (AmountResource ar : ars) {
				//	s = Conversion.capitalize(ar.getName());
                //}
				if (ars.size() > 0)
					s = Conversion.capitalize(ars.get(0).getName());
			}
			return s;
			//return bool ? "yes" : "no";
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
			// 2014-11-17 Internationalize and Capitalize names
			if (columnIndex == 0) return Msg.getString("InventoryTabPanel.Equipment.header.name"); //$NON-NLS-1$
			else if (columnIndex == 1) return Msg.getString("InventoryTabPanel.Equipment.header.status"); //$NON-NLS-1$
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
/*
				newMap.put(
					unit.getName(),
					yesNo(unit.getInventory().isEmpty(true))
				);
*/				
				newMap.put(
						unit.getName(),
						showOwner(unit)//.getInventory())//.isEmpty(true))
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
	
	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		// take care to avoid null exceptions
		resourceTableModel = null;
		equipmentTableModel = null;
		equipmentTable = null;
		resourcesTable = null;
		    
	}
	
}
