/*
 * Mars Simulation Project
 * InventoryTabPanel.java
 * @date 2023-06-10
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;

import org.mars.sim.tools.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.equipment.Container;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.equipment.EquipmentOwner;
import org.mars_sim.msp.core.equipment.ItemHolder;
import org.mars_sim.msp.core.equipment.ResourceHolder;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.resource.Resource;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.NumberCellRenderer;
import org.mars_sim.msp.ui.swing.utils.UnitModel;
import org.mars_sim.msp.ui.swing.utils.UnitTableLauncher;


/**
 * The InventoryTabPanel is a tab panel for displaying inventory information.
 */
@SuppressWarnings("serial")
public class InventoryTabPanel extends TabPanel {

	private static final String INVENTORY_ICON = "inventory";

    private ResourceTableModel resourceTableModel;
    private ItemTableModel itemTableModel;
    private EquipmentTableModel equipmentTableModel;
    
    private Unit unit;

    /**
     * Constructor.
     * 
     * @param unit the unit to display.
     * @param desktop the main desktop.
     */
    public InventoryTabPanel(Unit unit, MainDesktopPane desktop) {
        // Use the TabPanel constructor
        super(null, ImageLoader.getIconByName(INVENTORY_ICON), "Inventory", unit, desktop);
        
        this.unit = unit;
	}

	@Override
	protected void buildUI(JPanel content) {

        // Create inventory content panel
        JPanel inventoryContentPanel = new JPanel(new GridLayout(3, 1, 0, 0));
        content.add(inventoryContentPanel, BorderLayout.CENTER);

		NumberCellRenderer digit2Renderer = new NumberCellRenderer(2);
		DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();

        // Create resources panel
		if (unit instanceof ResourceHolder rHolder) {
			JScrollPane resourcesPanel = new JScrollPane();
			resourcesPanel.setBorder(new EmptyBorder(2, 2, 2, 2));
			inventoryContentPanel.add(resourcesPanel);

			// Create resources table model
			resourceTableModel = new ResourceTableModel(unit);

			// Create resources table
			JTable resourceTable = new JTable(resourceTableModel);
			resourceTable.setPreferredScrollableViewportSize(new Dimension(200, 75));


			resourceTable.setRowSelectionAllowed(true);
			resourcesPanel.setViewportView(resourceTable);

			// Add sorting
			resourceTable.setAutoCreateRowSorter(true);

			// Align the preference score to the right of the cell
			rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
			
			TableColumnModel resourceColumns = resourceTable.getColumnModel();
			resourceColumns.getColumn(0).setPreferredWidth(140);
			resourceColumns.getColumn(1).setPreferredWidth(30);
			resourceColumns.getColumn(2).setPreferredWidth(30);
			resourceColumns.getColumn(0).setCellRenderer(rightRenderer);
			resourceColumns.getColumn(1).setCellRenderer(digit2Renderer);
			resourceColumns.getColumn(2).setCellRenderer(digit2Renderer);
		}

        // Create item panel
		if (unit instanceof ItemHolder iHolder) {
			JScrollPane itemPanel = new JScrollPane();
			itemPanel.setBorder(new EmptyBorder(2, 2, 2, 2));
			inventoryContentPanel.add(itemPanel);

			// Create item table model
			itemTableModel = new ItemTableModel(iHolder);

			// Create item table
			JTable itemTable = new JTable(itemTableModel);
			itemTable.setPreferredScrollableViewportSize(new Dimension(200, 75));

			itemTable.setRowSelectionAllowed(true);
			itemPanel.setViewportView(itemTable);

			// Add sorting
			itemTable.setAutoCreateRowSorter(true);

			TableColumnModel itemColumns = itemTable.getColumnModel();
			itemColumns.getColumn(0).setPreferredWidth(110);
			itemColumns.getColumn(1).setPreferredWidth(20);
			itemColumns.getColumn(2).setPreferredWidth(30);
			itemColumns.getColumn(3).setPreferredWidth(30);
			itemColumns.getColumn(4).setPreferredWidth(30);
			// Align the preference score to the right of the cell
			itemColumns.getColumn(0).setCellRenderer(rightRenderer);
			itemColumns.getColumn(1).setCellRenderer(new NumberCellRenderer(0));
			itemColumns.getColumn(2).setCellRenderer(digit2Renderer);
			itemColumns.getColumn(2).setCellRenderer(digit2Renderer);
			itemColumns.getColumn(4).setCellRenderer(digit2Renderer);
		}

        // Create equipment panel
        if (unit instanceof EquipmentOwner eo) {
            JScrollPane equipmentPanel = new JScrollPane();
            equipmentPanel.setBorder(new EmptyBorder(2, 2, 2, 2));
            inventoryContentPanel.add(equipmentPanel);
            
	        // Create equipment table model
	        equipmentTableModel = new EquipmentTableModel(eo);
	
	        // Create equipment table
	        JTable equipmentTable = new JTable(equipmentTableModel);
	        equipmentTable.setPreferredScrollableViewportSize(new Dimension(200, 75));
	        
	        equipmentTable.setRowSelectionAllowed(true);
	        equipmentPanel.setViewportView(equipmentTable);
	
			// Add sorting
	        equipmentTable.setAutoCreateRowSorter(true);
			
	        equipmentTable.setDefaultRenderer(Double.class, new NumberCellRenderer(2, true));
	
			TableColumnModel equipmentColumns = equipmentTable.getColumnModel();
	        equipmentColumns.getColumn(0).setPreferredWidth(80);
	        equipmentColumns.getColumn(1).setPreferredWidth(30);
	        equipmentColumns.getColumn(2).setPreferredWidth(50);
	        equipmentColumns.getColumn(3).setPreferredWidth(70);
	
			// Align the preference score to the center of the cell
			DefaultTableCellRenderer renderer2 = new DefaultTableCellRenderer();
			renderer2.setHorizontalAlignment(SwingConstants.RIGHT);
			equipmentColumns.getColumn(0).setCellRenderer(renderer2);
			equipmentColumns.getColumn(1).setCellRenderer(digit2Renderer);
			equipmentColumns.getColumn(2).setCellRenderer(renderer2);
			equipmentColumns.getColumn(3).setCellRenderer(renderer2);
	
	
			// Add a mouse listener to hear for double-clicking a person (rather than single click using valueChanged()
	        equipmentTable.addMouseListener(new UnitTableLauncher(getDesktop()));
        }
    }

	
	
    /**
     * Updates the info on this panel.
     */
	@Override
    public void update() {
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
	private class ResourceTableModel extends AbstractTableModel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		private Map<Resource, Double> stored = new HashMap<>();
		private Map<Resource, Double> capacity = new HashMap<>();
		private List<Resource> keys = new ArrayList<>();

		private Unit unit;
		private ResourceHolder holder;

        private ResourceTableModel(Unit unit) {
        	this.unit = unit;
        	this.holder = (ResourceHolder) unit;
        	loadResources(keys, stored, capacity);
        }

        private void loadResources(List<Resource> kys, Map<Resource, Double> stored, Map<Resource, Double> cap) {  
        	List<AmountResource> arItems = //new ArrayList<>();
        			holder.getAllAmountResourceIDs().stream()
					.map(ar -> ResourceUtil.findAmountResource(ar))
					.filter(Objects::nonNull)
					.toList();
 
//        	if (unit.getUnitType() == UnitType.PERSON) {
//
//        		List<AmountResource> ars = ((Person)unit).getEquipmentInventory()
//        				.getAllAmountResourceIDs().stream()
//    					.map(ar -> ResourceUtil.findAmountResource(ar))
//    					.filter(Objects::nonNull)
//    					.toList();
//
//        		kys.addAll(ars);	
//        		
//    			for (AmountResource resource : arItems) {
//    				stored.put(resource, ((Person)unit).getAllAmountResourceStored(resource.getID()));
//    				cap.put(resource, ((Person)unit).getAmountResourceCapacity(resource.getID()));
//    			}
//    			
//        	}
//        	else if (unit.getUnitType() == UnitType.ROBOT) {
//        		List<AmountResource> ars = ((Robot)unit).getEquipmentInventory()
//        				.getAllAmountResourceIDs().stream()
//    					.map(ar -> ResourceUtil.findAmountResource(ar))
//    					.filter(Objects::nonNull)
//    					.toList();
//
//        		kys.addAll(ars);	
//        		
//    			for (AmountResource resource : arItems) {
//    				stored.put(resource, ((Robot)unit).getAllAmountResourceStored(resource.getID()));
//    				cap.put(resource, ((Robot)unit).getAmountResourceCapacity(resource.getID()));
//    			}
//            }
//        	else {
        		arItems = holder.getAllAmountResourceIDs().stream()
				.map(ar -> ResourceUtil.findAmountResource(ar))
				.filter(Objects::nonNull)
				.toList();
//        	}

			kys.addAll(arItems);
			
			for (AmountResource resource : arItems) {
				stored.put(resource, holder.getAllAmountResourceStored(resource.getID()));
				cap.put(resource, holder.getAmountResourceCapacity(resource.getID()));
			}
        }

        public int getRowCount() {
            return keys.size();
        }

        public int getColumnCount() {
            return 3;
        }

        public Class<?> getColumnClass(int columnIndex) {
            Class<?> dataType = null;
            if (columnIndex == 0) dataType = String.class;
            else if (columnIndex == 1) dataType = Double.class;
            else if (columnIndex == 2) dataType = Double.class;
            return dataType;
        }

        public String getColumnName(int columnIndex) {
			// Internationalized and capitalized column headers
            if (columnIndex == 0) return Msg.getString("InventoryTabPanel.Resource.header.name");
            else if (columnIndex == 1) return Msg.getString("InventoryTabPanel.Resource.header.quantity");
            else if (columnIndex == 2) return Msg.getString("InventoryTabPanel.Resource.header.capacity");
            else return "unknown";
        }

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

        public void update() {
    		List<Resource> newResourceKeys = new ArrayList<>();
			Map<Resource, Double> newStored = new HashMap<>();
    		Map<Resource, Double> newCapacity = new HashMap<>();

    		loadResources(newResourceKeys, newStored, newCapacity);

    		if (!keys.equals(newResourceKeys)
    				|| !stored.equals(newStored)
    				|| !capacity.equals(newCapacity)) {
    			stored = newStored;
    			capacity = newCapacity;
    			keys = newResourceKeys;
    			fireTableDataChanged();
    		}
    	}
    }

	/**
	 * Internal class used as model for the item resource table.
	 */
	private class ItemTableModel extends AbstractTableModel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		private ItemHolder holder;

		private List<Part> items;

        private ItemTableModel(ItemHolder unit) {
        	this.holder = unit;
        	this.items = getItems();
        }

        private List<Part> getItems() {
			return holder.getItemResourceIDs().stream()
							.map(ir -> ItemResourceUtil.findItemResource(ir))
							.filter(Objects::nonNull)
							.toList();
		}

        public int getRowCount() {
            return items.size();
        }

        public int getColumnCount() {
            return 5;
        }

        public Class<?> getColumnClass(int columnIndex) {
            Class<?> dataType = super.getColumnClass(columnIndex);
            if (columnIndex == 0) dataType = String.class;
            else if (columnIndex == 1) dataType = Integer.class;
            else if (columnIndex == 2) dataType = Double.class;
            else if (columnIndex == 3) dataType = Double.class;
            else if (columnIndex == 4) dataType = Double.class;
            return dataType;
        }

        public String getColumnName(int columnIndex) {
			// Internationalized and capitalized column headers
            if (columnIndex == 0) return Msg.getString("InventoryTabPanel.item.header.name");
            else if (columnIndex == 1) return Msg.getString("InventoryTabPanel.item.header.quantity");
            else if (columnIndex == 2) return Msg.getString("InventoryTabPanel.item.header.mass");           
            else if (columnIndex == 3) return Msg.getString("InventoryTabPanel.item.header.reliability");
            else return Msg.getString("InventoryTabPanel.item.header.mtbf");
        }

        public Object getValueAt(int row, int column) {
			Part i = items.get(row);
			switch(column) {
				case 0: return i.getName();
				case 1: return holder.getItemResourceStored(i.getID());
				case 2: return i.getMassPerItem();
				case 3: return i.getReliability();
				case 4: return i.getMTBF();
			}
           
            return null;
        }

        public void update() {
        	List<Part> newItems = getItems();

    		if (!items.equals(newItems)) {
    			items = newItems;
    			fireTableDataChanged();
    		}
    	}
    }

	/**
	 * Internal class used as model for the equipment table.
	 */
	public class EquipmentTableModel extends AbstractTableModel
				implements UnitModel {

		private List<Equipment> equipmentList = new ArrayList<>();

		private EquipmentOwner unit;

		/**
		 * Constructor.
		 * 
		 * @param inventory {@link Inventory}
		 */
		public EquipmentTableModel(EquipmentOwner unit) {
			this.unit = unit;
			equipmentList = new ArrayList<>(unit.getEquipmentSet());
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
			return equipmentList.size();
		}

		public int getColumnCount() {
			return 4;
		}

		public Class<?> getColumnClass(int columnIndex) {
			Class<?> dataType = super.getColumnClass(columnIndex);
			if (columnIndex == 0) dataType = String.class;
			else if (columnIndex == 1) dataType = Double.class;
			else if (columnIndex == 2) dataType = String.class;
			else if (columnIndex == 3) dataType = String.class;
			return dataType;
		}

		public String getColumnName(int columnIndex) {
			if (columnIndex == 0) return Msg.getString("InventoryTabPanel.Equipment.header.type"); //$NON-NLS-1$
			else if (columnIndex == 1) return Msg.getString("InventoryTabPanel.Equipment.header.mass"); //$NON-NLS-1$
			else if (columnIndex == 2) return Msg.getString("InventoryTabPanel.Equipment.header.owner"); //$NON-NLS-1$			
			else if (columnIndex == 3) return Msg.getString("InventoryTabPanel.Equipment.header.content"); //$NON-NLS-1$
			else return "unknown";
		}

		public Object getValueAt(int row, int column) {
			if (equipmentList != null && row >= 0 && row < equipmentList.size()) {
				Equipment e = equipmentList.get(row);
				switch(column) {
					case 0: return e.getName();
					case 1: return e.getMass();
					case 2: {
						Person owner = e.getRegisteredOwner();
						return (owner != null ? owner.getName() : null);
					}
					case 3: return getContent(e);
				}
			}
			return "unknown";
		}

		public void update() {

			List<Equipment> newEquipmentList = new ArrayList<>(unit.getEquipmentSet());
			
			if (equipmentList.size() != newEquipmentList.size()
				|| !equipmentList.equals(newEquipmentList)) {
				equipmentList = newEquipmentList;

				fireTableDataChanged();
			}
		}

		@Override
		public Unit getAssociatedUnit(int row) {
			return equipmentList.get(row);
		}
	}
}
