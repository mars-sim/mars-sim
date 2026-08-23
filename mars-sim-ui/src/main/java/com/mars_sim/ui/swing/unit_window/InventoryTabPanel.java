/*
 * Mars Simulation Project
 * InventoryTabPanel.java
 * @date 2025-07-15
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.unit_window;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.swing.JPanel;

import com.mars_sim.core.Entity;
import com.mars_sim.core.Unit;
import com.mars_sim.core.equipment.Container;
import com.mars_sim.core.equipment.Equipment;
import com.mars_sim.core.equipment.EquipmentOwner;
import com.mars_sim.core.equipment.ItemHolder;
import com.mars_sim.core.equipment.ResourceHolder;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.resource.AmountResource;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.resource.Part;
import com.mars_sim.core.resource.Resource;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.TemporalComponent;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.components.AbstractEnhancedTableModel;
import com.mars_sim.ui.swing.components.ColumnSpec;
import com.mars_sim.ui.swing.entitywindow.EntityTabPanel;
import com.mars_sim.ui.swing.utils.EntityModel;
import com.mars_sim.ui.swing.utils.SwingHelper;


/**
 * The InventoryTabPanel is a tab panel for displaying inventory information.
 */
@SuppressWarnings("serial")
public class InventoryTabPanel extends EntityTabPanel<Unit> implements TemporalComponent{

	private static final String INVENTORY_ICON = "inventory";

	private static final String BR = "<br/>";
	
    private ResourceTableModel resourceTableModel;
    private ItemTableModel itemTableModel;
    private EquipmentTableModel equipmentTableModel;
    
    /**
     * Constructor.
     * 
     * @param unit the unit to display.
     * @param context the UI context.
     */
    public InventoryTabPanel(Unit unit, UIContext context) {
        // Use the TabPanel constructor
        super(null, ImageLoader.getIconByName(INVENTORY_ICON), "Inventory", context, unit);
        
	}

	@Override
	protected void buildUI(JPanel content) {

        // Create inventory content panel
        JPanel inventoryContentPanel = new JPanel(new GridLayout(3, 1, 0, 0));
        content.add(inventoryContentPanel, BorderLayout.CENTER);
		
        // Create resources panel
		var rh = ResourceHolder.getAttached(getEntity());
		if (rh != null) {
			// Create resources table model
			resourceTableModel = new ResourceTableModel(rh);

			var resourcesPanel = SwingHelper.createScrolledTable(resourceTableModel, getContext(), null,
												new Dimension(200, 75));
			inventoryContentPanel.add(resourcesPanel);
		}

        // Create item panel
		var ih = ItemHolder.getAttached(getEntity());
		if (ih != null) {
			// Create item table model
			itemTableModel = new ItemTableModel(ih);

			var itemPanel = SwingHelper.createScrolledTable(itemTableModel, getContext(), null,
												new Dimension(200, 75));
			inventoryContentPanel.add(itemPanel);
		}
		
        // Create equipment panel
		var eo = EquipmentOwner.getAttached(getEntity());
        if (eo != null) {     
	        // Create equipment table model
	        equipmentTableModel = new EquipmentTableModel(eo);

			var equipmentPanel = SwingHelper.createScrolledTable(equipmentTableModel, getContext(), null, new Dimension(200, 75));
			inventoryContentPanel.add(equipmentPanel);
        }
    }
	
	/**
     * Generates the tooltip based on the resource's description.
     * 
     * @param resource
     * @param building
     * @return
     */
    private static String generateToolTip(Resource resource) {
		var desc = resource.getDescription();
		if (desc == null || desc.isEmpty()) {
			return null;
		}

        // NOTE: internationalize the resource processes' dynamic tooltip.
        StringBuilder result = new StringBuilder("<html>");
        // Future: Use another tool tip manager to align text to improve tooltip readability			
        result.append(wrapText(desc, 80));
     
        result.append("</html>");   
        
        return result.toString();
    }
    
    /**
     * Adds a html line break to every line and wraps the text around.
     * 
     * @param text
     * @param wrapCharAt
     * @return
     */
    private static String wrapText(String text, int wrapCharAt) {
        int lastBreak = 0;
        int nextBreak = wrapCharAt;
        if (text.length() > wrapCharAt) {
            StringBuffer setString = new StringBuffer();
            do {
                while (text.charAt(nextBreak) != ' ' && nextBreak > lastBreak) {
                    nextBreak--;
                }
                if (nextBreak == lastBreak) {
                    nextBreak = lastBreak + wrapCharAt;
                }
                setString.append(text.substring(lastBreak, nextBreak).trim()).append(BR);
                lastBreak = nextBreak;
                nextBreak += wrapCharAt;

            } while (nextBreak < text.length());
            setString.append(text.substring(lastBreak).trim());
            return setString.toString();
        }
        else {
        
        	return text;
        }
    }
    
	/**
	 * Update the table panels.
	 */
	@Override
	public void clockUpdate(ClockPulse pulse) {
		if (resourceTableModel != null)
        	resourceTableModel.update();
		if (itemTableModel != null)
       		itemTableModel.update();
		if (equipmentTableModel != null)
        	equipmentTableModel.update();
    }

	/**
	 * Internal class used as model for the resource table.
	 */
	private static class ResourceTableModel extends AbstractEnhancedTableModel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		private static final ColumnSpec RESOURCE_NAME = new ColumnSpec(Msg.getString("InventoryTabPanel.Resource.header.name"), String.class);
		private static final ColumnSpec RESOURCE_STORED = new ColumnSpec(Msg.getString("InventoryTabPanel.Resource.header.quantity"), Double.class, ColumnSpec.STYLE_DIGIT2);
		private static final ColumnSpec RESOURCE_CAPACITY = new ColumnSpec(Msg.getString("InventoryTabPanel.Resource.header.capacity"), Double.class, ColumnSpec.STYLE_DIGIT2);

		private Map<Resource, Double> stored = new HashMap<>();
		private Map<Resource, Double> capacity = new HashMap<>();
		private List<Resource> keys = new ArrayList<>();

		private ResourceHolder holder;

        private ResourceTableModel(ResourceHolder unit) {
			super(RESOURCE_NAME, RESOURCE_STORED, RESOURCE_CAPACITY);
        	this.holder = unit;
        	loadResources(keys, stored, capacity);
        }

        private void loadResources(List<Resource> kys, Map<Resource, Double> stored, Map<Resource, Double> cap) {  
    		var arItems = holder.getAllAmountResourceStoredIDs().stream()
						.map(ResourceUtil::findAmountResource)
						.filter(Objects::nonNull)
						.toList();

			kys.addAll(arItems);
			
			for (AmountResource resource : arItems) {
				stored.put(resource, holder.getAllAmountResourceStored(resource.getID()));
				cap.put(resource, holder.getSpecificCapacity(resource.getID()));
			}
        }

        private Resource getResource(int row) {
        	return keys.get(row);
        }
        
		@Override
        public int getRowCount() {
            return keys.size();
        }
		
		@Override
        public Object getValueAt(int row, int column) {
            if (column == 0) {
    			// Capitalize Resource Names
            	return keys.get(row).getName();
            }
            else if (column == 1) {
            	return stored.get(keys.get(row));
            }
            else if (column == 2) {
            	return capacity.get(keys.get(row));
            }
            return 0 + "";
        }

		private void updateData() {
	    	int numRow = getRowCount();
	    	int numCol = getColumnCount();
	    	for (int i=0; i< numRow; i++) {	
	    		for (int j=1; j< numCol; j++) {	
		    		fireTableCellUpdated(i, j);
	    		}
	    	}
		}
        
        public void update() {
			
    		List<Resource> newResourceKeys = new ArrayList<>();
			Map<Resource, Double> newStored = new HashMap<>();
    		Map<Resource, Double> newCapacity = new HashMap<>();

    		loadResources(newResourceKeys, newStored, newCapacity);

    		var oldKeys = keys;
			keys = newResourceKeys;
			stored = newStored;
			capacity = newCapacity;

			if (!keys.equals(oldKeys)) {
				fireTableDataChanged();
			}
			else {			
				updateData();
			}
    	}

		@Override
		public String getToolTipAt(int row, int col) {
			var r = getResource(row);
			if (r != null) {
				return generateToolTip(r);
			}
			return null;
		}
    }

	/**
	 * Internal class used as model for the item resource table.
	 */
	private static class ItemTableModel extends AbstractEnhancedTableModel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;
		
		private static final ColumnSpec ITEM_NAME = new ColumnSpec(Msg.getString("InventoryTabPanel.item.header.name"), String.class);
		private static final ColumnSpec ITEM_QUANTITY = new ColumnSpec(Msg.getString("InventoryTabPanel.item.header.quantity"), Integer.class);
		private static final ColumnSpec ITEM_MASS = new ColumnSpec(Msg.getString("InventoryTabPanel.item.header.mass"), Double.class, ColumnSpec.STYLE_DIGIT2);
		private static final ColumnSpec ITEM_RELIABILITY = new ColumnSpec(Msg.getString("InventoryTabPanel.item.header.reliability"), Double.class, ColumnSpec.STYLE_DIGIT2);
		private static final ColumnSpec ITEM_MTBF = new ColumnSpec(Msg.getString("InventoryTabPanel.item.header.mtbf"), Double.class, ColumnSpec.STYLE_DIGIT2);

		private ItemHolder holder;

		private List<Part> items;

        private ItemTableModel(ItemHolder unit) {
			super(ITEM_NAME, ITEM_QUANTITY, ITEM_MASS, ITEM_RELIABILITY, ITEM_MTBF);
        	this.holder = unit;
        	this.items = getItems();
        }

        private List<Part> getItems() {
			return holder.getItemResourceIDs().stream()
							.map(ItemResourceUtil::findItemResource)
							.filter(Objects::nonNull)
							.toList();
		}

        private Part getPart(int row) {
        	return items.get(row);
        }
        
        @Override
        public int getRowCount() {
            return items.size();
        }

        public Object getValueAt(int row, int column) {
			Part i = items.get(row);
			return switch(column) {
				case 0 -> i.getName();
				case 1 -> holder.getItemResourceStored(i.getID());
				case 2 -> i.getMassPerItem();
				case 3 -> i.getReliability();
				case 4 -> i.getMTBF();
				default -> "";
			};
        }

		private void updateData() {
	    	int numRow = getRowCount();
	    	int numCol = getColumnCount();
	    	for (int i=0; i< numRow; i++) {	
	    		for (int j=1; j< numCol; j++) {	
		    		fireTableCellUpdated(i, j);
	    		}
	    	}
		}
		
        public void update() {
 
			List<Part> newList = getItems();
			
			if (!items.equals(newList)) {
				items = newList;
				fireTableDataChanged();
			}
			else {
				items = newList;
				updateData();
			}
    	}

		@Override
		public String getToolTipAt(int row, int col) {
			var r = getPart(row);
			if (r != null) {
				return generateToolTip(r);
			}
			return null;
		}
    }

	/**
	 * Internal class used as model for the equipment table.
	 */
	public class EquipmentTableModel extends AbstractEnhancedTableModel
				implements EntityModel {

		private List<Equipment> equipmentList = new ArrayList<>();

		private static final ColumnSpec EQUIPMENT_NAME = new ColumnSpec(Msg.getString("equipment.singular"), String.class);
		private static final ColumnSpec EQUIPMENT_MASS = new ColumnSpec(Msg.getString("InventoryTabPanel.Equipment.header.mass"), Double.class, ColumnSpec.STYLE_DIGIT2);
		private static final ColumnSpec EQUIPMENT_OWNER = new ColumnSpec(Msg.getString("InventoryTabPanel.Equipment.header.owner"), String.class);
		private static final ColumnSpec EQUIPMENT_CONTENT = new ColumnSpec(Msg.getString("InventoryTabPanel.Equipment.header.content"), String.class);

		private EquipmentOwner owner;

		/**
		 * Constructor.
		 * 
		 * @param inventory {@link Inventory}
		 */
		public EquipmentTableModel(EquipmentOwner owner) {
			super(EQUIPMENT_NAME, EQUIPMENT_MASS, EQUIPMENT_OWNER, EQUIPMENT_CONTENT);
			this.owner = owner;
			equipmentList = new ArrayList<>(owner.getEquipmentSet());
		}

		private Equipment getEquipment(int row) {
			if (equipmentList != null && !equipmentList.isEmpty())
				return equipmentList.get(row);
			return null;
		}
        
		private String getContent(Equipment e) {
			String s = "";
			if (e instanceof Container c) {
				int resource = c.getResource();
				if (resource != -1) {
					s = ResourceUtil.findAmountResourceName(resource);
				}
			}

			return s;
		}

		public int getRowCount() {
			if (equipmentList != null && !equipmentList.isEmpty())
				return equipmentList.size();
			return 0;
		}

		@Override
		public Object getValueAt(int row, int column) {
			if (equipmentList != null && row >= 0 && row < equipmentList.size()) {
				Equipment e = equipmentList.get(row);
				switch(column) {
					case 0: return e.getName();
					case 1: return e.getMass();
					case 2: {
						Person o = e.getRegisteredOwner();
						return (o != null ? o.getName() : null);
					}
					case 3: return getContent(e);
					default: return null;
				}
			}
			return "unknown";
		}

		private void updateData() {
	    	int numRow = getRowCount();
	    	int numCol = getColumnCount();
	    	for (int i=0; i< numRow; i++) {	
	    		for (int j=1; j< numCol; j++) {	
		    		fireTableCellUpdated(i, j);
	    		}
	    	}
		}
		
		public void update() {

			List<Equipment> newList = new ArrayList<>(owner.getEquipmentSet());
			
			if (!equipmentList.equals(newList)) {
				equipmentList = newList;
				fireTableDataChanged();
			}
			else {
				equipmentList = newList;
				updateData();
			}
		}
		
		@Override
		public Entity getAssociatedEntity(int row) {
			return getEquipment(row);
		}

		@Override
		public String getToolTipAt(int row, int col) {
			Equipment equipment = getEquipment(row);
			// NOTE: internationalize the resource processes' dynamic tooltip.
			StringBuilder result = new StringBuilder("<html>");
			if (equipment != null) {			
				result.append(wrapText(equipment.getDescription(), 80));
			}
			result.append("</html>");   
			
			return result.toString();
		}
	}
}
