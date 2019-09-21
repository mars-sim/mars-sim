/**
 * Mars Simulation Project
 * SupplyTableModel.java
 * @version 3.1.0 2017-11-21
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.resupply;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.table.AbstractTableModel;

import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.equipment.EquipmentFactory;
import org.mars_sim.msp.core.interplanetary.transport.resupply.Resupply;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.BuildingTemplate;
import org.mars_sim.msp.ui.swing.tool.Conversion;

/** TODO externalize strings */
@SuppressWarnings("serial")
public class SupplyTableModel
extends AbstractTableModel {

	// Supply categories.
	public final static String BUILDING = "Building";
	public final static String VEHICLE = "Vehicle";
	public final static String EQUIPMENT = "Equipment";
	public final static String RESOURCE = "Resource";
	public final static String PART = "Part";

	private static List<String> categoryList;
	private static Map<String, List<String>> categoryTypeMap;

	// Data members
	private List<SupplyItem> supplyList;

	/**
	 * Constructor.
	 * @param resupply the resupply mission or null if none.
	 */
	public SupplyTableModel(Resupply resupply) {

		// Initialize data members.
		supplyList = new ArrayList<SupplyItem>();

		if (resupply != null) {
			// Populate supply list from resupply mission.
			populateSupplies(resupply);
		}
	}

	/**
	 * Populate supply list from resupply mission.
	 * @param resupply the resupply mission.
	 */
	private void populateSupplies(Resupply resupply) {

		// Populate buildings.
		List<String> buildingList = new ArrayList<String>(resupply.getNewBuildings().size());
		Iterator<BuildingTemplate> l = resupply.getNewBuildings().iterator();
		while(l.hasNext()) {
			buildingList.add(l.next().getBuildingType());
		}
		populateSupplyTypeList(BUILDING, buildingList);

		// Populate vehicles.
		populateSupplyTypeList(VEHICLE, resupply.getNewVehicles());

		// Populate equipment.
		List<String> sortEquipment =
				new ArrayList<String>(resupply.getNewEquipment().keySet());
		Collections.sort(sortEquipment);
		Iterator<String> i = sortEquipment.iterator();
		while (i.hasNext()) {
			String equipmentType = i.next();
			int num = resupply.getNewEquipment().get(equipmentType);
			SupplyItem supplyItem = new SupplyItem(EQUIPMENT, equipmentType, num);
			supplyList.add(supplyItem);
		}

		// Populate resources.
		List<AmountResource> sortResources =
				new ArrayList<AmountResource>(resupply.getNewResources().keySet());
		Collections.sort(sortResources);
		Iterator<AmountResource> j = sortResources.iterator();
		while (j.hasNext()) {
			AmountResource resource = j.next();
			double amount = resupply.getNewResources().get(resource);
			// 2014-12-01 Added Conversion.capitalize()
			SupplyItem supplyItem = new SupplyItem(RESOURCE, Conversion.capitalize(resource.getName()), amount);
			supplyList.add(supplyItem);
		}

		// Populate parts.
		List<Part> sortParts =
		new ArrayList<Part>(resupply.getNewParts().keySet());
		Collections.sort(sortParts);
		Iterator<Part> k = sortParts.iterator();
		while (k.hasNext()) {
			Part part = k.next();
			int num = resupply.getNewParts().get(part);
			// 2014-12-01 Added WordUtils.capitalize()
			SupplyItem supplyItem = new SupplyItem(PART, Conversion.capitalize(part.getName()), num);
			supplyList.add(supplyItem);
		}
	}

	/**
	 * Populate supplies that are in a list of strings.
	 * @param category the supply category.
	 * @param supplies the list of supplies as strings.
	 */
	private void populateSupplyTypeList(String category, List<String> supplies) {

		// Create map of supplies and their numbers.
		Map<String, Integer> supplyMap =
				new HashMap<String, Integer>(supplies.size());
		Iterator<String> i = supplies.iterator();
		while (i.hasNext()) {
			String supplyType = i.next();
			if (supplyMap.keySet().contains(supplyType)) {
				int num = supplyMap.get(supplyType) + 1;
				supplyMap.put(supplyType, num);
			}
			else {
				supplyMap.put(supplyType, 1);
			}
		}

		// Create and add supply item for each supply.
		List<String> sortKeys = new ArrayList<String>(supplyMap.keySet());
		Collections.sort(sortKeys);
		Iterator<String> j = sortKeys.iterator();
		while (j.hasNext()) {
			String supplyType = j.next();
			int num = supplyMap.get(supplyType);
			// 2014-12-01 Added Conversion.capitalize()
			SupplyItem supplyItem = new SupplyItem(category, Conversion.capitalize(supplyType), num);
			supplyList.add(supplyItem);
		}
	}

	@Override
	public int getColumnCount() {
		return 3;
	}

	@Override
	public Class<?> getColumnClass(int column) {
		Class<?> result = null;
		if ((column == 0) || (column == 1)) result = String.class;
		else if (column == 2) result = Integer.class;
		return result;
	}

	@Override
	public String getColumnName(int column) {
		String result = null;
		if (column == 0) result = "Category";
		else if (column == 1) result = "Supply Type";
		else if (column == 2) result = "Quantity (# or kg)";
		return result;
	}

	@Override
	public int getRowCount() {
		return supplyList.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int colIndex) {
		Object result = null;
		if (rowIndex < supplyList.size()) {
			SupplyItem item = supplyList.get(rowIndex);
			if (colIndex == 0) result = item.category;
			// 2014-12-01 Added Conversion.capitalize()
			else if (colIndex == 1) result = Conversion.capitalize(item.type);
			else if (colIndex == 2) result = item.number.intValue();
		}
		return result;
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		return true;
	}

	@Override
	public void setValueAt(Object value, int row, int col) {

		if (row < supplyList.size()) {
			SupplyItem item = supplyList.get(row);
			if (col == 0) {
				item.category = (String) value;
			}
			else if (col == 1) {
				// 2014-12-01 Added Conversion.capitalize()
				item.type = Conversion.capitalize((String) value);
			}
			else if (col == 2) {
				try {
					item.number = (Integer) value;
				}
				catch (NumberFormatException e) {
					e.printStackTrace(System.err);
				}
			}
			fireTableCellUpdated(row, col);
		}
	}

	/**
	 * Add a new supply item with default values.
	 */
	 public void addNewSupplyItem() {
		String category = getCategoryList().get(0);
		String type = getCategoryTypeMap().get(category).get(0);
		int num = 1;
		supplyList.add(new SupplyItem(category, type, num));
		fireTableRowsInserted(supplyList.size() - 1, supplyList.size() - 1);
	 }

	 /**
	  * Remove items from the given rows.
	  * @param rows an array of row indexes to remove.
	  */
	 public void removeSupplyItems(int[] rows) {

		 List<SupplyItem> items = new ArrayList<SupplyItem>(rows.length);
		 for (int x = 0; x < rows.length; x++) {
			 items.add(supplyList.get(rows[x]));
		 }

		 supplyList.removeAll(items);

		 fireTableDataChanged();
	 }

	 /**
	  * Gets the list of supply items.
	  * @return list of supply items.
	  */
	 public List<SupplyItem> getSupplyItems() {
		 return new ArrayList<SupplyItem>(supplyList);
	 }

	 /**
	  * Gets a list of all categories.
	  * @return list of category strings.
	  */
	 public static List<String> getCategoryList() {

		 if (categoryList == null) {
			 categoryList = new ArrayList<String>(5);
			 categoryList.add(BUILDING);
			 categoryList.add(VEHICLE);
			 categoryList.add(EQUIPMENT);
			 categoryList.add(RESOURCE);
			 categoryList.add(PART);
		 }

		 return categoryList;
	 }

	 public static List<String> getSortedBuildingTypes() {
		 Set<String> buildingTypes = SimulationConfig.instance().getBuildingConfiguration().getBuildingTypes();
		 List<String> sortedBuildingTypes = new ArrayList<String>(buildingTypes);
		 Collections.sort(sortedBuildingTypes);
		 return sortedBuildingTypes;
	 }

	 public static List<String> getSortedVehicleTypes() {
		 Set<String> vehicleTypes = SimulationConfig.instance().getVehicleConfiguration().getVehicleTypes();
		 List<String> sortedVehicleTypes = new ArrayList<String>(vehicleTypes);
		 Collections.sort(sortedVehicleTypes);
		 return sortedVehicleTypes;
	 }

	 /**
	  * Gets a map of categories and a list of their types.
	  * @return map of categories to lists of types.
	  */
	 public static Map<String, List<String>> getCategoryTypeMap() {

		 if (categoryTypeMap == null) {
			 // Create and populate category type map.
			 categoryTypeMap = new HashMap<String, List<String>>(5);

			 // Create building type list.
			 categoryTypeMap.put(BUILDING, getSortedBuildingTypes());

			 // Create vehicle type list.
			 categoryTypeMap.put(VEHICLE, getSortedVehicleTypes());

			 // Create equipment type list.
			 Set<String> equipmentTypes = EquipmentFactory.getEquipmentNames();
			 List<String> sortedEquipmentTypes = new ArrayList<String>(equipmentTypes);
			 Collections.sort(sortedEquipmentTypes);
			 categoryTypeMap.put(EQUIPMENT, sortedEquipmentTypes);

			 // Create resource type list.
			 categoryTypeMap.put(RESOURCE, ResourceUtil.getAmountResourceStringSortedList());

			 // Create part type list.
			 List<String> partNames = new ArrayList<String>();
			 Iterator<Part> j = Part.getParts().iterator();
			 while (j.hasNext()) {
				 partNames.add(j.next().getName());
			 }
			 Collections.sort(partNames);
			 categoryTypeMap.put(PART, partNames);
		 }

		 return categoryTypeMap;
	 }

	 /**
	  * Inner class to represent a supply table item.
	  */
	 public class SupplyItem {

		 // Data members
		 public String category;
		 public String type;
		 public Number number;

		 /**
		  * Constructor.
		  * @param category the supply category.
		  * @param type the supply type.
		  * @param number the supply number.
		  */
		 public SupplyItem(String category, String type, Number number) {
			 this.category = category;
			 this.type = type;
			 this.number = number;
		 }
	 }
}