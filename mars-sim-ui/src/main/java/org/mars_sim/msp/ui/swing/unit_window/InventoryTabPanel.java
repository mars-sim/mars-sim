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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

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
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.resource.Resource;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.tool.AlphanumComparator;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.NumberCellRenderer;
import org.mars_sim.msp.ui.swing.tool.TableStyle;
import org.mars_sim.msp.ui.swing.tool.ZebraJTable;

import com.alee.laf.panel.WebPanel;
import com.alee.laf.scroll.WebScrollPane;

/**
 * The InventoryTabPanel is a tab panel for displaying inventory information.
 */
@SuppressWarnings("serial")
public class InventoryTabPanel extends TabPanel implements ListSelectionListener {

	private static final String INVENTORY_ICON = Msg.getString("icon.inventory"); //$NON-NLS-1$

	private static final String WHITESPACE = "  ";

	private final DecimalFormat formatter0 = new DecimalFormat("#,###,###,###");
	private final DecimalFormat formatter2 = new DecimalFormat("#,###,###,###.##");

    private ResourceTableModel resourceTableModel;
    private ItemTableModel itemTableModel;
    private EquipmentTableModel equipmentTableModel;

    private JTable resourceTable;
    private JTable itemTable;
    private JTable equipmentTable;

    /**
     * Constructor.
     * 
     * @param unit the unit to display.
     * @param desktop the main desktop.
     */
    public InventoryTabPanel(Unit unit, MainDesktopPane desktop) {
        // Use the TabPanel constructor
        super(null, ImageLoader.getNewIcon(INVENTORY_ICON), "Inventory", unit, desktop);
	}

	@Override
	protected void buildUI(JPanel content) {

        // Create inventory content panel
        WebPanel inventoryContentPanel = new WebPanel(new GridLayout(3, 1, 0, 0));
        content.add(inventoryContentPanel, BorderLayout.CENTER);

        // Create resources panel
        WebScrollPane resourcesPanel = new WebScrollPane();
        resourcesPanel.setBorder(new EmptyBorder(2, 2, 2, 2));
        inventoryContentPanel.add(resourcesPanel);

        // Create resources table model
        resourceTableModel = new ResourceTableModel(getUnit());

        // Create resources table
        resourceTable = new ZebraJTable(resourceTableModel);
        resourceTable.setPreferredScrollableViewportSize(new Dimension(200, 75));
        resourceTable.getColumnModel().getColumn(0).setPreferredWidth(140);
        resourceTable.getColumnModel().getColumn(1).setPreferredWidth(30);
        resourceTable.getColumnModel().getColumn(2).setPreferredWidth(30);

        resourceTable.setRowSelectionAllowed(true);
        resourcesPanel.setViewportView(resourceTable);

		// Added sorting
        resourceTable.setAutoCreateRowSorter(true);

		// Override default cell renderer for formatting double values.
//        resourcesTable.setDefaultRenderer(Number.class, new NumberCellRenderer(2, true));

		// Align the preference score to the left of the cell
//		DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
//		leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);
//		resourcesTable.getColumnModel().getColumn(0).setCellRenderer(leftRenderer);

		// Align the preference score to the right of the cell
		DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
		rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
		resourceTable.getColumnModel().getColumn(0).setCellRenderer(rightRenderer);
		resourceTable.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);
		resourceTable.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);

		// Added setTableStyle()
		TableStyle.setTableStyle(resourceTable);

     	// Added resourcesSearchable
//     	TableSearchable searchable = SearchableUtils.installSearchable(resourcesTable);
//        searchable.setPopupTimeout(5000);
//     	searchable.setCaseSensitive(false);

        // Create item panel
        WebScrollPane itemPanel = new WebScrollPane();
        itemPanel.setBorder(new EmptyBorder(2, 2, 2, 2));
        inventoryContentPanel.add(itemPanel);

        // Create item table model
        itemTableModel = new ItemTableModel(getUnit());

        // Create item table
        itemTable = new ZebraJTable(itemTableModel);
        itemTable.setPreferredScrollableViewportSize(new Dimension(200, 75));
        itemTable.getColumnModel().getColumn(0).setPreferredWidth(110);
        itemTable.getColumnModel().getColumn(1).setPreferredWidth(30);
        itemTable.getColumnModel().getColumn(2).setPreferredWidth(30);
        itemTable.getColumnModel().getColumn(2).setPreferredWidth(30);

        itemTable.setRowSelectionAllowed(true);
        itemPanel.setViewportView(itemTable);

		// Added sorting
        itemTable.setAutoCreateRowSorter(true);

		// Align the preference score to the right of the cell
//		DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
//		rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
		itemTable.getColumnModel().getColumn(0).setCellRenderer(rightRenderer);
//		itemTable.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);
//		itemTable.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
//		itemTable.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);

		// Added setTableStyle()
		TableStyle.setTableStyle(itemTable);

        // Create equipment panel
        WebScrollPane equipmentPanel = new WebScrollPane();
        equipmentPanel.setBorder(new EmptyBorder(2, 2, 2, 2));
        inventoryContentPanel.add(equipmentPanel);

        // Create equipment table model
        equipmentTableModel = new EquipmentTableModel(getUnit());

        // Create equipment table
        equipmentTable = new ZebraJTable(equipmentTableModel);
        equipmentTable.setPreferredScrollableViewportSize(new Dimension(200, 75));
        equipmentTable.setRowSelectionAllowed(true);
        equipmentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        equipmentTable.getSelectionModel().addListSelectionListener(this);
        equipmentPanel.setViewportView(equipmentTable);

        equipmentTable.setDefaultRenderer(Double.class, new NumberCellRenderer(2, true));

        equipmentTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        equipmentTable.getColumnModel().getColumn(1).setPreferredWidth(80);
        equipmentTable.getColumnModel().getColumn(2).setPreferredWidth(30);
        equipmentTable.getColumnModel().getColumn(3).setPreferredWidth(70);

		// Align the preference score to the center of the cell
		DefaultTableCellRenderer renderer2 = new DefaultTableCellRenderer();
		renderer2.setHorizontalAlignment(SwingConstants.RIGHT);
		equipmentTable.getColumnModel().getColumn(0).setCellRenderer(renderer2);
		equipmentTable.getColumnModel().getColumn(1).setCellRenderer(renderer2);
		equipmentTable.getColumnModel().getColumn(2).setCellRenderer(renderer2);
		equipmentTable.getColumnModel().getColumn(3).setCellRenderer(renderer2);

		// Added sorting
        equipmentTable.setAutoCreateRowSorter(true);

		// Add a mouse listener to hear for double-clicking a person (rather than single click using valueChanged()
        equipmentTable.addMouseListener(new MouseAdapter() {
		    public void mousePressed(MouseEvent event) {

		    	// If double-click, open person window.
				if (event.getClickCount() >= 2) {
					Point p = event.getPoint();
					int row = equipmentTable.rowAtPoint(p);
					Equipment e = (Equipment)equipmentTable.getValueAt(row, 1);
					if (e != null) {
						getDesktop().openUnitWindow(e, false);
					}
				}
		    }
		});

		// Added setTableStyle()
		TableStyle.setTableStyle(equipmentTable);

    }

    /**
     * Updates the info on this panel.
     */
	@Override
    public void update() {

        resourceTableModel.update();
        itemTableModel.update();
        equipmentTableModel.update();
		TableStyle.setTableStyle(resourceTable);
		TableStyle.setTableStyle(itemTable);
		TableStyle.setTableStyle(equipmentTable);
        resourceTable.repaint();
        itemTable.repaint();
        equipmentTable.repaint();
    }

    /**
     * Called whenever the value of the selection changes.
     *
     * @param ev the event that characterizes the change.
     */
    public void valueChanged(ListSelectionEvent ev) {
//        int row = equipmentTable.getSelectedRow();
//        if (row > 0) {
////	        Object selectedEquipment = equipmentTable.getValueAt(index, 0);
////	        if ((selectedEquipment != null) && (selectedEquipment instanceof Equipment))
////	            desktop.openUnitWindow((Equipment) selectedEquipment, false);
//        	String name = ((Equipment)equipmentTable.getValueAt(row, 1)).getName();
////    		System.out.println("name : " + name + "   row : " + row);
//		    for (Equipment e : equipmentList) {
////	    		System.out.println("nickname : " + e.getNickName());
//		    	if (e.getNickName().equalsIgnoreCase(name)) {
////		    		System.out.println("name : " + name + "   nickname : " + e.getNickName());
//	    		    desktop.openUnitWindow(e, false);
//		    	}
//		    }
//        }
    }

	/**
	 * Internal class used as model for the resource table.
	 */
	private class ResourceTableModel extends AbstractTableModel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		private Map<Resource, Number> resources = new HashMap<>();
		private Map<Resource, Number> capacity = new HashMap<>();
		private List<Resource> keys = new ArrayList<>();

		private Unit unit;

        private ResourceTableModel(Unit unit) {
        	this.unit = unit;
        	loadResources(keys, resources, capacity);
        }

        private void loadResources(List<Resource> kys, Map<Resource, Number> res, Map<Resource, Number> cap) {
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
            Class<?> dataType = super.getColumnClass(columnIndex);
            if (columnIndex >= 0) dataType = String.class;
            else if (columnIndex >= 1) dataType = Number.class;
            else if (columnIndex >= 2) dataType = Number.class;
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
            	return formatter2.format(resources.get(keys.get(row)));
            }
            else if (column == 2) {
            	Number number = capacity.get(keys.get(row));
            	return (number == null) ? 0 + "": formatter0.format(number);
            }
            return 0 + "";
        }

        public void update() {
    		List<Resource> newResourceKeys = new ArrayList<>();
			Map<Resource, Number> newResources = new HashMap<>();
    		Map<Resource, Number> newCapacity = new HashMap<>();

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

		private Map<Resource, Number> resources = new HashMap<>();
		private Map<Resource, Double> reliabilities = new HashMap<>();
		private Map<Resource, Double> mtbf = new HashMap<>();
		private List<Resource> keys = new ArrayList<>();

		private Unit unit;

        private ItemTableModel(Unit unit) {
        	this.unit = unit;
        	loadItems(keys, resources, reliabilities, mtbf);
        }

        private void loadItems(List<Resource> kys, Map<Resource, Number> res, Map<Resource, Double> rel, Map<Resource, Double> mtbf) {
           	// Has equipment resources
        	if (unit instanceof EVASuit) {
        		EVASuit e = (EVASuit) unit;
        		Set<Resource> irItems = e.getItemResourceIDs().stream()
            				.map(ir -> ItemResourceUtil.findItemResource(ir))
            				.filter(Objects::nonNull)
            		        .collect(Collectors.toSet());

            	kys.addAll(irItems);
 	            for(Resource resource : irItems) {
 	                res.put(resource, e.getItemResourceStored(resource.getID()));
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
 	                res.put(resource, eo.getItemResourceStored(resource.getID()));
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
 	                res.put(resource, holder.getItemResourceStored(resource.getID()));
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
            return 4;
        }

        public Class<?> getColumnClass(int columnIndex) {
            Class<?> dataType = super.getColumnClass(columnIndex);
            if (columnIndex >= 0) dataType = String.class;
            else if (columnIndex >= 1) dataType = Integer.class;
            else if (columnIndex >= 2) dataType = Double.class;
            else if (columnIndex >= 3) dataType = Double.class;
            return dataType;
        }

        public String getColumnName(int columnIndex) {
			// Internationalized and capitalized column headers
            if (columnIndex == 0) return Msg.getString("InventoryTabPanel.item.header.name");
            else if (columnIndex == 1) return Msg.getString("InventoryTabPanel.item.header.quantity");
            else if (columnIndex == 2) return Msg.getString("InventoryTabPanel.item.header.reliability");
            else return Msg.getString("InventoryTabPanel.item.header.mtbf");
        }

        public Object getValueAt(int row, int column) {
            if (column == 0) {
    			// Capitalize Resource Names
            	return keys.get(row).getName();
            }
            else if (column == 1) {
				return formatter0.format(resources.get(keys.get(row)));
            }
            else if (column == 2) {
            	return formatter2.format(reliabilities.get(keys.get(row)));
            }
            else if (column == 3) {
            	return formatter2.format(mtbf.get(keys.get(row)));
            }
            return 0 + "";
        }

        public void update() {
        	List<Resource> newResourceKeys = new ArrayList<>();
        	Map<Resource, Number> newResources = new HashMap<>();
			Map<Resource, Double> newReliabilities = new HashMap<>();
			Map<Resource, Double> newMTBF = new HashMap<>();

    		loadItems(newResourceKeys, newResources, newReliabilities, newMTBF);

    		if (!keys.equals(newResourceKeys)
    				|| !resources.equals(newResources)
    				|| !reliabilities.equals(newReliabilities)
    				|| !mtbf.equals(newMTBF)
    				) {
    			resources = newResources;
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
	private class EquipmentTableModel extends AbstractTableModel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		private List<Equipment> equipmentList = new ArrayList<>();
		private Map<String, String> types = new HashMap<>();
		private Map<String, String> contentOwner = new HashMap<>();
		private Map<String, Double> mass = new HashMap<>();

		private Unit unit;


		/**
		 * hidden constructor.
		 * @param inventory {@link Inventory}
		 */
		public EquipmentTableModel(Unit unit) {
			this.unit = unit;
			loadModel(equipmentList, types, contentOwner, mass);
		}

  		private void loadModel(List<Equipment> equipmentList, Map<String, String> types, Map<String, String> contentOwner, Map<String, Double> mass) {

            if (unit.getUnitType() == UnitType.PERSON
            		|| unit.getUnitType() == UnitType.ROBOT
            		|| unit.getUnitType() == UnitType.VEHICLE
            		|| unit.getUnitType() == UnitType.SETTLEMENT
            		) {
            	for (Equipment e : ((EquipmentOwner)unit).getEquipmentSet()) {
					String name = e.getName();
					types.put(name, e.getEquipmentType().getName());
					contentOwner.put(name, getContentOwner(e));
					mass.put(name, e.getMass());
					equipmentList.add(e);
				}
            }

			// Sort equipment alphabetically by name.
			equipmentList.stream().sorted(new AlphanumComparator()).collect(Collectors.toList());
		}

		private String getContentOwner(Equipment e) {
			String s = "";
			if (e.getEquipmentType() == EquipmentType.EVA_SUIT) {
				Person p = e.getLastOwner();
				if (p != null)
					s = p.getName();
			}
//			else if (e instanceof Robot) {
//				Person p = e.getLastOwner();
//				if (p != null)
//					s = p.getName();
//			}
			else if (e instanceof Container) {
				int resource = ((Container)e).getResource();
				if (resource != -1) {
					s = ResourceUtil.findAmountResourceName(resource);
				}
			}

			return s;
		}

		public int getRowCount() {
			return contentOwner.size();
		}

		public int getColumnCount() {
			return 4;
		}

		public Class<?> getColumnClass(int columnIndex) {
			Class<?> dataType = super.getColumnClass(columnIndex);
			if (columnIndex == 0) dataType = Equipment.class;
			else if (columnIndex == 1) dataType = String.class;
			else if (columnIndex == 2) dataType = Double.class;
			else if (columnIndex == 3) dataType = String.class;
			return dataType;
		}

		public String getColumnName(int columnIndex) {
			if (columnIndex == 0) return Msg.getString("InventoryTabPanel.Equipment.header.type"); //$NON-NLS-1$
			else if (columnIndex == 1) return Msg.getString("InventoryTabPanel.Equipment.header.name"); //$NON-NLS-1$
			else if (columnIndex == 2) return Msg.getString("InventoryTabPanel.Equipment.header.mass"); //$NON-NLS-1$
			else if (columnIndex == 3) return Msg.getString("InventoryTabPanel.Equipment.header.content"); //$NON-NLS-1$
			else return "unknown";
		}

		public Object getValueAt(int row, int column) {
			if (equipmentList != null && row >= 0 && row < contentOwner.size()) {
				if (column == 0) return types.get(equipmentList.get(row).getName()) + WHITESPACE;
				else if (column == 1) return equipmentList.get(row);
				else if (column == 2) {
					String name = equipmentList.get(row).getName();
					if (name != null && mass.get(name) != null)
						return Math.round(mass.get(name)*100.0)/100.0;
				}
				else if (column == 3) return contentOwner.get(equipmentList.get(row).getName());
			}
			return "unknown";
		}

		public void update() {
			List<Equipment> newEquipment = new ArrayList<>();
			Map<String, String> newTypes = new HashMap<>();
			Map<String, String> newContentOwner = new HashMap<>();
			Map<String, Double> newMass = new HashMap<>();

			loadModel(newEquipment, newTypes, newContentOwner, newMass);

    		if (equipmentList.size() != newEquipment.size()
    				|| !newEquipment.equals(equipmentList)
    				|| !newContentOwner.equals(contentOwner)
    				|| !mass.equals(newMass)) {
				equipmentList = newEquipment;
				contentOwner = newContentOwner;
				types = newTypes;
				mass = newMass;
				fireTableDataChanged();
			}
		}
	}

	static class NameComparator implements Comparator<Equipment> {
	     public int compare(Equipment e0, Equipment e1) {
	    	 String[] names0 = e0.getName().split(" ");
	    	 String[] names1 = e1.getName().split(" ");
	    	 int size0 = names0.length;
	    	 int size1 = names1.length;

	    	 if (!names0[0].equals(names1[0]))
	    		 return names0[0].compareTo(names1[0]);

	    	 int num0 = Integer.parseInt(names0[size0-1]);
	    	 int num1 = Integer.parseInt(names1[size1-1]);

	    	 return num0 - num1;
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
