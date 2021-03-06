/**
 * Mars Simulation Project
 * InventoryTabPanel.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.unit_window;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ItemResource;
import org.mars_sim.msp.core.resource.Resource;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.tool.AlphanumComparator;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.NumberCellRenderer;
import org.mars_sim.msp.ui.swing.tool.Conversion;
import org.mars_sim.msp.ui.swing.tool.TableStyle;
import org.mars_sim.msp.ui.swing.tool.ZebraJTable;

import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.scroll.WebScrollPane;

/**
 * The InventoryTabPanel is a tab panel for displaying inventory information.
 */
@SuppressWarnings("serial")
public class InventoryTabPanel extends TabPanel implements ListSelectionListener {

	/** default logger. */
	private static final Logger logger = Logger.getLogger(InventoryTabPanel.class.getName());
	private static final String WHITESPACE = "  ";
	
	private final DecimalFormat formatter0 = new DecimalFormat("#,###,###,###"); 
	private final DecimalFormat formatter2 = new DecimalFormat("#,###,###,###.##");

	/** Is UI constructed. */
	private boolean uiDone = false;
	
	/** The Inventory instance. */
	private Inventory inv; 
	
    private ResourceTableModel resourceTableModel;
    private EquipmentTableModel equipmentTableModel;
    
    private JTable equipmentTable;
    private JTable resourcesTable;
    
	private List<Equipment> equipmentList = new ArrayList<>();

	
    /**
     * Constructor
     * @param unit the unit to display.
     * @param desktop the main desktop.
     */
    public InventoryTabPanel(Unit unit, MainDesktopPane desktop) {
        // Use the TabPanel constructor
        super("Inventory", null, "Inventory", unit, desktop);

        inv = unit.getInventory();
	}
	
	public boolean isUIDone() {
		return uiDone;
	}
	
	public void initializeUI() {
		uiDone = true;
		
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
        resourcesPanel.setBorder(new EmptyBorder(2, 2, 2, 2));
        inventoryContentPanel.add(resourcesPanel);

        // Create resources table model
        resourceTableModel = new ResourceTableModel(inv);

        // Create resources table
        resourcesTable = new ZebraJTable(resourceTableModel);
        resourcesTable.setPreferredScrollableViewportSize(new Dimension(200, 75));
        resourcesTable.getColumnModel().getColumn(0).setPreferredWidth(140);
        resourcesTable.getColumnModel().getColumn(1).setPreferredWidth(30);
        resourcesTable.getColumnModel().getColumn(2).setPreferredWidth(30);
        
        resourcesTable.setRowSelectionAllowed(true);//setCellSelectionEnabled(true);
        resourcesPanel.setViewportView(resourcesTable);

		// Added sorting
        resourcesTable.setAutoCreateRowSorter(true);
        
		// Override default cell renderer for formatting double values.
//        resourcesTable.setDefaultRenderer(Number.class, new NumberCellRenderer(2, true));
        
		// Align the preference score to the left of the cell
//		DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
//		leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);
//		resourcesTable.getColumnModel().getColumn(0).setCellRenderer(leftRenderer);

		// Align the preference score to the right of the cell
		DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
		rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
		resourcesTable.getColumnModel().getColumn(0).setCellRenderer(rightRenderer);
//		resourcesTable.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);
//		resourcesTable.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
		
		// Added setTableStyle()
		TableStyle.setTableStyle(resourcesTable);

     	// Added resourcesSearchable
//     	TableSearchable searchable = SearchableUtils.installSearchable(resourcesTable);
//        searchable.setPopupTimeout(5000);
//     	searchable.setCaseSensitive(false);

        // Create equipment panel
        WebScrollPane equipmentPanel = new WebScrollPane();
        equipmentPanel.setBorder(new EmptyBorder(2, 2, 2, 2));
        inventoryContentPanel.add(equipmentPanel);

        // Create equipment table model
        equipmentTableModel = new EquipmentTableModel(inv);

        // Create equipment table
        equipmentTable = new ZebraJTable(equipmentTableModel);
        equipmentTable.setPreferredScrollableViewportSize(new Dimension(200, 75));
        equipmentTable.setRowSelectionAllowed(true);//.setCellSelectionEnabled(true);
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
		    public void mousePressed(MouseEvent me) {
		    	JTable table =(JTable) me.getSource();
		        Point p = me.getPoint();
		        int row = table.rowAtPoint(p);
		        int col = table.columnAtPoint(p);
		        if (me.getClickCount() == 2) {
		            if (row > 0 && col > 0) {
		    		    String name = ((Equipment)equipmentTable.getValueAt(row, 1)).getName();
//    		    		System.out.println("name : " + name + "   row : " + row);
		    		    for (Equipment e : equipmentList) {
//	    		    		System.out.println("nickname : " + e.getName());
		    		    	if (e.getName().equalsIgnoreCase(name)) {
//		    		    		System.out.println("name : " + name + "   nickname : " + e.getName());
				    		    desktop.openUnitWindow(e, false);
		    		    	}
		    		    } 	    			
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
    public void update() {
		if (!uiDone)
			initializeUI();
		
        resourceTableModel.update();
        equipmentTableModel.update();
		TableStyle.setTableStyle(resourcesTable);
		TableStyle.setTableStyle(equipmentTable);
        equipmentTable.repaint();
        resourcesTable.repaint();
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

		private int counts = 0;
		
		private Inventory inventory;
		
		private Map<Resource, Number> resources;
		private Map<Resource, Number> capacity;
		private List<Resource> keys;
		
		private DecimalFormat decFormatter = new DecimalFormat("#,###,##0");

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

            Set<ItemResource> itemResources = inventory.getAllItemRsStored();
            keys.addAll(itemResources);
            Iterator<ItemResource> iItem = itemResources.iterator();
            while (iItem.hasNext()) {
                ItemResource resource = iItem.next();
                resources.put(resource, inventory.getItemResourceNum(resource));
                capacity.put(resource, null);
            }

            // Sort resources alphabetically by name.
            keys.stream().sorted(new AlphanumComparator()).collect(Collectors.toList());
//            Collections.sort(keys);
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
            	return Conversion.capitalize(keys.get(row).toString() + "  ");
            }
            else if (column == 1) {
            	Resource resource = keys.get(row);
            	if (resource instanceof AmountResource) {
            		return formatter2.format(resources.get(resource));//.doubleValue());
//            		result = decFormatter.format(amount);
            	}
            	else {
					return formatter0.format(resources.get(resource));//.intValue());
				}
            }
            else if (column == 2) {
            	Number number = capacity.get(keys.get(row));
            	return (number == null) ? 0 + "": formatter0.format(number);//.intValue());
            	
            }
            return 0 + "";
        }

//        public void updateRow() {
//            int rowCount = getRowCount();
//            String[] row = new String[rowCount];
//            for (int index = 0; index < rowCount; index++) {
//                row[index] = rowCount + "x" + index;
//            }
//            fireTableRowsInserted(rowCount, rowCount);
//        }      
//        
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

        		Set<ItemResource> itemResources = inventory.getAllItemRsStored();
        		newResourceKeys.addAll(itemResources);
            	//Iterator<ItemResource> iItem = itemResources.iterator();
            	//while (iItem.hasNext()) {
            	for (ItemResource resource : itemResources) {//= iItem.next();
            		newResources.put(resource, inventory.getItemResourceNum(resource));
            		newCapacity.put(resource, null);
            	}

            	// Sort resources alphabetically by name.
//                Collections.sort(newResourceKeys);

                counts++;
        		if (counts % 10 == 10 || !resources.equals(newResources)) {
//            		if (getRowCount() == newResources.size()) {
                   		counts = 0;
            			resources = newResources;
            			capacity = newCapacity;
            			keys = newResourceKeys;
            			fireTableDataChanged();
//                			((AbstractTableModel)this).fireTableCellUpdated(counts, counts);
//            		}
//            		else {
//               			counts = 0;
//            			resources = newResources;
//            			capacity = newCapacity;
//            			keys = newResourceKeys;
//            			fireTableDataChanged();
//            		}
        		}
        		
                keys.stream().sorted(new AlphanumComparator()).collect(Collectors.toList());
//              Collections.sort(keys);
        	}
        	catch(Exception e) {
        	    e.printStackTrace(System.err);
            }
        }
    }

	/**
	 * Internal class used as model for the equipment table.
	 */
	private class EquipmentTableModel extends AbstractTableModel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		private Inventory inventory;
		private Map<String, String> types;
		private Map<String, String> contentOwner;
		private Map<String, Double> mass;
		
		/**
		 * hidden constructor.
		 * @param inventory {@link Inventory}
		 */
		private EquipmentTableModel(Inventory inventory) {
			this.inventory = inventory;
			// Sort equipment alphabetically by name.
			types = new HashMap<>();
			contentOwner = new HashMap<>();
			mass = new HashMap<>();
			
			for (Equipment e : inventory.findAllEquipment()) {
				String name = e.getName();
				types.put(name, e.getType());
				contentOwner.put(name, showOwner(e));
				mass.put(name, e.getMass());
				equipmentList.add(e);
			}
			equipmentList.stream().sorted(new AlphanumComparator()).collect(Collectors.toList());
//			Collections.sort(equipmentList);
		}

		private String showOwner(Equipment e) {
			String s = "";
//			String item = e.getName().toLowerCase();
			if (e instanceof EVASuit) {//item.contains("eva suit")) {
				Person p = (Person) e.getLastOwner();
				if (p != null)
					s = p.getName();
				//else 
				//	s = unit.getContainerUnit().getName();		
			}
			else if (e instanceof Robot) {
				;// TODO: assign the ownership of each bot
			}
//			else if (item.contains("box") || item.contains("canister") 
//					|| item.contains("barrel") || item.contains("bag")) {
				// shower the owner of this Container instance
//				Person p = (Person) ((Equipment) unit).getLastOwner();	
//			}
			else {		
				List<AmountResource> ars = new ArrayList<>(e.getInventory().getAllAmountResourcesStored(false));
				if (ars.size() > 1) logger.info(e.getName() + " has a total of " + ars.size() + " different resources.");
				for (AmountResource ar : ars) {
					s = Conversion.capitalize(ar.getName() + "  ");
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
			if (columnIndex == 0) dataType = String.class;
			else if (columnIndex == 1) dataType = Equipment.class;
			else if (columnIndex == 2) dataType = Double.class;
			else if (columnIndex == 3) dataType = String.class;
			return dataType;
		}
		
		public String getColumnName(int columnIndex) {
			if (columnIndex == 0) return Msg.getString("InventoryTabPanel.Equipment.header.type"); //$NON-NLS-1$
			else if (columnIndex == 1) return Msg.getString("InventoryTabPanel.Equipment.header.name"); //$NON-NLS-1$
			else if (columnIndex == 2) return Msg.getString("InventoryTabPanel.Equipment.header.mass"); //$NON-NLS-1$
			else if (columnIndex == 3) return Msg.getString("InventoryTabPanel.Equipment.header.status"); //$NON-NLS-1$
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
				else if (column == 3) return contentOwner.get(equipmentList.get(row).getName()) + WHITESPACE;
			}
			return "unknown";
		}

		public boolean isMassDifferent(List<Double> oldMass, List<Double> newMass) {
			int size = oldMass.size();
			for (int i=0; i< size; i++) {
				if (oldMass.get(i) != newMass.get(i)) {
					return false;
				}
			}
			return true;
		}
		
		public void update() {
			List<Equipment> newNames = new ArrayList<>();
			Map<String, String> newTypes = new HashMap<>();
			Map<String, String> newContentOwner = new HashMap<>();
			Map<String, Double> newMass = new HashMap<>();
			for (Equipment e : inventory.findAllEquipment()) {
				newTypes.put(e.getName(), e.getType());
				newContentOwner.put(e.getName(), showOwner(e));
				newMass.put(e.getName(), e.getMass());
				newNames.add(e);
			}

			newNames.stream().sorted(new AlphanumComparator()).collect(Collectors.toList());
			Collections.sort(newNames);//, new NameComparator());
			
			if (equipmentList.size() != newNames.size() 
					|| !equipmentList.equals(newNames)) {
				
				List<Double> oldMassList = new ArrayList<>(mass.values());
				Collections.sort(oldMassList);
				List<Double> newMassList = new ArrayList<>(newMass.values());
				Collections.sort(newMassList);
				
				if (isMassDifferent(oldMassList, newMassList)) {
					equipmentList = newNames;
					contentOwner = newContentOwner;
					types = newTypes;
					mass = newMass;
					fireTableDataChanged();
				}
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
	public void destroy() {
		// take care to avoid null exceptions
		resourceTableModel = null;
		equipmentTableModel = null;
		equipmentTable = null;
		resourcesTable = null;
		equipmentList.clear();
		equipmentList = null;
		    
	}
	
}
