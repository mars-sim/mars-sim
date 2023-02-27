/*
 * Mars Simulation Project
 * InventoryTabPanel.java
 * @date 2021-12-20
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.equipment.Container;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.equipment.EquipmentOwner;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.equipment.ItemHolder;
import org.mars_sim.msp.core.equipment.ResourceHolder;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ItemResource;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.resource.Resource;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.tool.AlphanumComparator;
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

    private JTable resourceTable;
    private JTable itemTable;
    private JTable equipmentTable;
    
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

        // Create resources panel
        JScrollPane resourcesPanel = new JScrollPane();
        resourcesPanel.setBorder(new EmptyBorder(2, 2, 2, 2));
        inventoryContentPanel.add(resourcesPanel);

		NumberCellRenderer digit2Renderer = new NumberCellRenderer(2);

        // Create resources table model
        resourceTableModel = new ResourceTableModel(getUnit());

        // Create resources table
        resourceTable = new JTable(resourceTableModel);

        resourceTable.setPreferredScrollableViewportSize(new Dimension(200, 75));
		TableColumnModel resourceColumns = resourceTable.getColumnModel();
        resourceColumns.getColumn(0).setPreferredWidth(140);
        resourceColumns.getColumn(1).setPreferredWidth(30);
        resourceColumns.getColumn(2).setPreferredWidth(30);

        resourceTable.setRowSelectionAllowed(true);
        resourcesPanel.setViewportView(resourceTable);

		// Added sorting
        resourceTable.setAutoCreateRowSorter(true);


		// Align the preference score to the right of the cell
		DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
		rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
		resourceColumns.getColumn(0).setCellRenderer(rightRenderer);
		resourceColumns.getColumn(1).setCellRenderer(digit2Renderer);
		resourceColumns.getColumn(2).setCellRenderer(digit2Renderer);

        // Create item panel
        JScrollPane itemPanel = new JScrollPane();
        itemPanel.setBorder(new EmptyBorder(2, 2, 2, 2));
        inventoryContentPanel.add(itemPanel);

        // Create item table model
        itemTableModel = new ItemTableModel(getUnit());

        // Create item table
        itemTable = new JTable(itemTableModel);
        itemTable.setPreferredScrollableViewportSize(new Dimension(200, 75));
		TableColumnModel itemColumns = itemTable.getColumnModel();
        itemColumns.getColumn(0).setPreferredWidth(110);
        itemColumns.getColumn(1).setPreferredWidth(30);
        itemColumns.getColumn(2).setPreferredWidth(30);
        itemColumns.getColumn(3).setPreferredWidth(30);
        itemColumns.getColumn(4).setPreferredWidth(30);

        itemTable.setRowSelectionAllowed(true);
        itemPanel.setViewportView(itemTable);

		// Added sorting
        itemTable.setAutoCreateRowSorter(true);

		// Align the preference score to the right of the cell
		itemColumns.getColumn(0).setCellRenderer(rightRenderer);
		itemColumns.getColumn(1).setCellRenderer(new NumberCellRenderer(0));
		itemColumns.getColumn(2).setCellRenderer(digit2Renderer);
		itemColumns.getColumn(2).setCellRenderer(digit2Renderer);
		itemColumns.getColumn(4).setCellRenderer(digit2Renderer);

        // Create equipment panel
        JScrollPane equipmentPanel = new JScrollPane();
        equipmentPanel.setBorder(new EmptyBorder(2, 2, 2, 2));
        inventoryContentPanel.add(equipmentPanel);

        if (!(unit instanceof Container) && !(unit instanceof EVASuit)) {
	        // Create equipment table model
	        equipmentTableModel = new EquipmentTableModel(getUnit());
	
	        // Create equipment table
	        equipmentTable = new JTable(equipmentTableModel);
	        equipmentTable.setPreferredScrollableViewportSize(new Dimension(200, 75));
	        equipmentTable.setRowSelectionAllowed(true);
	        equipmentPanel.setViewportView(equipmentTable);
	
	        equipmentTable.setDefaultRenderer(Double.class, new NumberCellRenderer(2, true));
	
			TableColumnModel equipmentColumns = equipmentTable.getColumnModel();
	        equipmentColumns.getColumn(0).setPreferredWidth(60);
	        equipmentColumns.getColumn(1).setPreferredWidth(80);
	        equipmentColumns.getColumn(2).setPreferredWidth(30);
	        equipmentColumns.getColumn(3).setPreferredWidth(70);
	
			// Align the preference score to the center of the cell
			DefaultTableCellRenderer renderer2 = new DefaultTableCellRenderer();
			renderer2.setHorizontalAlignment(SwingConstants.RIGHT);
			equipmentColumns.getColumn(0).setCellRenderer(renderer2);
			equipmentColumns.getColumn(1).setCellRenderer(digit2Renderer);
			equipmentColumns.getColumn(2).setCellRenderer(renderer2);
			equipmentColumns.getColumn(3).setCellRenderer(renderer2);
	
			// Added sorting
	        equipmentTable.setAutoCreateRowSorter(true);
	
			// Add a mouse listener to hear for double-clicking a person (rather than single click using valueChanged()
	        equipmentTable.addMouseListener(new UnitTableLauncher(getDesktop()));
        }
    }

	
	
    /**
     * Updates the info on this panel.
     */
	@Override
    public void update() {

        resourceTableModel.update();
        itemTableModel.update();
        equipmentTableModel.update();
        resourceTable.repaint();
        itemTable.repaint();
        equipmentTable.repaint();
    }

	/**
	 * Internal class used as model for the resource table.
	 */
	private class ResourceTableModel extends AbstractTableModel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		private Map<Resource, Double> resources = new HashMap<>();
		private Map<Resource, Double> capacity = new HashMap<>();
		private List<Resource> keys = new ArrayList<>();

		private Unit unit;

        private ResourceTableModel(Unit unit) {
        	this.unit = unit;
        	loadResources(keys, resources, capacity);
        }

        private void loadResources(List<Resource> kys, Map<Resource, Double> res, Map<Resource, Double> cap) {
           	// Has equipment resources
        	if (unit instanceof EVASuit) {
        		EVASuit e = (EVASuit) unit;
        		Set<AmountResource> arItems = e.getAmountResourceIDs().stream()
  					  .map(ar -> ResourceUtil.findAmountResource(ar))
  					  .filter(Objects::nonNull)
  					  .collect(Collectors.toSet());

		  		kys.addAll(arItems);
		        for (AmountResource resource : arItems) {
		        	res.put(resource, e.getAmountResourceStored(resource.getID()));
		        	cap.put(resource, e.getAmountResourceCapacity(resource.getID()));
		        }
        	}

           	// Has equipment resources
        	else if (unit instanceof EquipmentOwner) {
        		EquipmentOwner eo = (EquipmentOwner) unit;
        		Set<AmountResource> arItems = eo.getAmountResourceIDs().stream()
    					  .map(ar -> ResourceUtil.findAmountResource(ar))
    					  .filter(Objects::nonNull)
    					  .collect(Collectors.toSet());

  		  		kys.addAll(arItems);
  		        for (AmountResource resource : arItems) {
  		        	res.put(resource, eo.getAmountResourceStored(resource.getID()));
  		        	cap.put(resource, eo.getAmountResourceCapacity(resource.getID()));
  		        }
        	}

        	// New approach based on interfaces
        	else if (unit instanceof ResourceHolder) {
        		ResourceHolder holder = (ResourceHolder) unit;
        		Set<AmountResource> arItems = holder.getAmountResourceIDs().stream()
        					  .map(ar -> ResourceUtil.findAmountResource(ar))
        					  .filter(Objects::nonNull)
        					  .collect(Collectors.toSet());

        		kys.addAll(arItems);
 	            for (AmountResource resource : arItems) {
 	                res.put(resource, holder.getAmountResourceStored(resource.getID()));
 	                cap.put(resource, holder.getAmountResourceCapacity(resource.getID()));
 	            }
        	}

            // Sort resources alphabetically by name.
            kys.stream().sorted(new AlphanumComparator()).collect(Collectors.toList());
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
            	return resources.get(keys.get(row));
            }
            else if (column == 2) {
            	return capacity.get(keys.get(row));
            }
            return 0 + "";
        }

        public void update() {
    		List<Resource> newResourceKeys = new ArrayList<>();
			Map<Resource, Double> newResources = new HashMap<>();
    		Map<Resource, Double> newCapacity = new HashMap<>();

    		loadResources(newResourceKeys, newResources, newCapacity);

    		if (!keys.equals(newResourceKeys)
    				|| !resources.equals(newResources)
    				|| !capacity.equals(newCapacity)) {
    			resources = newResources;
    			capacity = newCapacity;
    			keys = newResourceKeys;
    			fireTableDataChanged();

    		}

            keys.stream().sorted(new AlphanumComparator()).collect(Collectors.toList());
    	}
    }

	/**
	 * Internal class used as model for the item resource table.
	 */
	private class ItemTableModel extends AbstractTableModel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		private Map<Resource, Number> quantity = new HashMap<>();
		private Map<Resource, Double> mass = new HashMap<>();		
		private Map<Resource, Double> reliabilities = new HashMap<>();
		private Map<Resource, Double> mtbf = new HashMap<>();
		private List<Resource> keys = new ArrayList<>();

		private Unit unit;

        private ItemTableModel(Unit unit) {
        	this.unit = unit;
        	loadItems(keys, quantity, mass, reliabilities, mtbf);
        }

        private void loadItems(List<Resource> kys, Map<Resource, Number> quan, Map<Resource, Double> mass, Map<Resource, Double> rel, Map<Resource, Double> mtbf) {
           	// Has equipment resources
        	if (unit instanceof EVASuit) {
        		EVASuit e = (EVASuit) unit;
        		Set<Resource> irItems = e.getItemResourceIDs().stream()
            				.map(ir -> ItemResourceUtil.findItemResource(ir))
            				.filter(Objects::nonNull)
            		        .collect(Collectors.toSet());

            	kys.addAll(irItems);
 	            for(Resource resource : irItems) {
 	                quan.put(resource, e.getItemResourceStored(resource.getID()));
 	                mass.put(resource, ((ItemResource)resource).getMassPerItem());
 	                rel.put(resource, ((Part)resource).getReliability());
 	               mtbf.put(resource, ((Part)resource).getMTBF());
 	            }
        	}

           	// Has equipment resources
        	else if (unit instanceof EquipmentOwner) {
        		EquipmentOwner eo = (EquipmentOwner) unit;
        		Set<Resource> irItems = eo.getItemResourceIDs().stream()
            				.map(ir -> ItemResourceUtil.findItemResource(ir))
            				.filter(Objects::nonNull)
            		        .collect(Collectors.toSet());

            	kys.addAll(irItems);
 	            for (Resource resource : irItems) {
 	                quan.put(resource, eo.getItemResourceStored(resource.getID()));
 	                mass.put(resource, ((ItemResource)resource).getMassPerItem()); 	                
 	                rel.put(resource, ((Part)resource).getReliability());
 	               mtbf.put(resource, ((Part)resource).getMTBF());
 	            }
        	}

          	// Has Item resources
        	else if (unit instanceof ItemHolder) {
        		ItemHolder holder = (ItemHolder) unit;
        		Set<Resource> irItems = holder.getItemResourceIDs().stream()
            				.map(ir -> ItemResourceUtil.findItemResource(ir))
            				.filter(Objects::nonNull)
            		        .collect(Collectors.toSet());

            	kys.addAll(irItems);
 	            for(Resource resource : irItems) {
 	                quan.put(resource, holder.getItemResourceStored(resource.getID()));
 	                mass.put(resource, ((ItemResource)resource).getMassPerItem());	                
 	                rel.put(resource, ((Part)resource).getReliability());
 	               mtbf.put(resource, ((Part)resource).getMTBF());
 	            }
        	}

            // Sort resources alphabetically by name.
            kys.stream().sorted(new AlphanumComparator()).collect(Collectors.toList());
        }

        public int getRowCount() {
            return keys.size();
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
            if (column == 0) {
    			// Capitalize Resource Names
            	return keys.get(row).getName();
            }
            else if (column == 1) {
				return quantity.get(keys.get(row));
            }
            else if (column == 2) {
            	return mass.get(keys.get(row));
            }
            else if (column == 3) {
            	return reliabilities.get(keys.get(row));
            }
            else if (column == 4) {
            	return mtbf.get(keys.get(row));
            }
            return 0 + "";
        }

        public void update() {
        	List<Resource> newResourceKeys = new ArrayList<>();
        	Map<Resource, Number> newQ = new HashMap<>();
			Map<Resource, Double> newMass = new HashMap<>();
			Map<Resource, Double> newReliabilities = new HashMap<>();
			Map<Resource, Double> newMTBF = new HashMap<>();

    		loadItems(newResourceKeys, newQ, newMass, newReliabilities, newMTBF);

    		if (!keys.equals(newResourceKeys)
    				|| !quantity.equals(newQ)
    				|| !mass.equals(newQ)   				
    				|| !reliabilities.equals(newReliabilities)
    				|| !mtbf.equals(newMTBF)
    				) {
    			quantity = newQ;
    			mass = newMass;
    			reliabilities = newReliabilities;
    			mtbf = newMTBF;
    			keys = newResourceKeys;
    			fireTableDataChanged();
    		}

            keys.stream().sorted(new AlphanumComparator()).collect(Collectors.toList());
    	}
    }

	/**
	 * Internal class used as model for the equipment table.
	 */
	public class EquipmentTableModel extends AbstractTableModel
				implements UnitModel {

		private List<Equipment> equipmentList = new ArrayList<>();

		private Map<String, String> types = new HashMap<>();
		private Map<String, Double> mass = new HashMap<>();
		private Map<String, String> owner = new HashMap<>();
		private Map<String, String> content = new HashMap<>();

		private Unit unit;

		/**
		 * Constructor.
		 * 
		 * @param inventory {@link Inventory}
		 */
		public EquipmentTableModel(Unit unit) {
			this.unit = unit;
			equipmentList = new ArrayList<>(((EquipmentOwner)unit).getEquipmentSet());
			loadModel();
		}

  		private void loadModel() {
  			
            if (unit.getUnitType() == UnitType.PERSON
            		|| unit.getUnitType() == UnitType.ROBOT
            		|| unit.getUnitType() == UnitType.VEHICLE
            		|| unit.getUnitType() == UnitType.SETTLEMENT
            		) {
            	for (Equipment e : equipmentList) {
					String name = e.getName();
					types.put(name, e.getEquipmentType().getName());
					owner.put(name, getOwner(e));
					content.put(name, getContent(e));
					mass.put(name, e.getMass());
				}
            }

			// Sort equipment alphabetically by name.
			equipmentList.stream().sorted(new AlphanumComparator()).collect(Collectors.toList());
		}

		private String getOwner(Equipment e) {
			String s = "";
			if (e.getEquipmentType() == EquipmentType.EVA_SUIT) {
				Person p = e.getLastOwner();
				if (p != null)
					s = p.getName();
			}
			return s;
		}

		private String getContent(Equipment e) {
			String s = "";
			if (e instanceof Container) {
				int resource = ((Container)e).getResource();
				if (resource != -1) {
					s = ResourceUtil.findAmountResourceName(resource);
				}
			}

			return s;
		}

		public Equipment getEquipment(String name) {
			for (Equipment e: equipmentList) {
				if (e.getName().equalsIgnoreCase(name)) {
					return e;
				}
			}
			return null;
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
			if (equipmentList != null && row >= 0 && row < owner.size()) {
				if (column == 0) return equipmentList.get(row).getName();
				else if (column == 1) {
					String name = equipmentList.get(row).getName();
					if (name != null && mass.get(name) != null)
						return mass.get(name);
				}
				else if (column == 2) return owner.get(equipmentList.get(row).getName());
				else if (column == 3) return content.get(equipmentList.get(row).getName());
			}
			return "unknown";
		}

		public void update() {

			if (unit.getUnitType() == UnitType.PERSON
            		|| unit.getUnitType() == UnitType.ROBOT
            		|| unit.getUnitType() == UnitType.VEHICLE
            		|| unit.getUnitType() == UnitType.SETTLEMENT
            		) {
				
				List<Equipment> newEquipmentList = new ArrayList<>(((EquipmentOwner)unit).getEquipmentSet());
				
				// Sort equipment alphabetically by name.
				newEquipmentList.stream().sorted(new AlphanumComparator()).collect(Collectors.toList());
				
				if (equipmentList.size() != newEquipmentList.size()
    				|| !equipmentList.equals(newEquipmentList)) {
			
	            	for (Equipment e : newEquipmentList) {
						String name = e.getName();
						types.put(name, e.getEquipmentType().getName());
						owner.put(name, getOwner(e));
						content.put(name, getContent(e));
						mass.put(name, e.getMass());
					}
	            }
			}
		}

		@Override
		public Unit getAssociatedUnit(int row) {
			return equipmentList.get(row);
		}
	}

	/**
	 * Prepare object for garbage collection.
	 */
	@Override
	public void destroy() {
		super.destroy();
		
		// take care to avoid null exceptions
		resourceTableModel = null;
		equipmentTableModel = null;
		itemTableModel = null;
		equipmentTable = null;
		resourceTable = null;
		itemTable = null;
	}
}
