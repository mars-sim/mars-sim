/*
 * Mars Simulation Project
 * SupplyTableModel.java
 * @date 2022-07-20
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.tool.resupply;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.building.BuildingTemplate;
import com.mars_sim.core.building.config.BuildingConfig;
import com.mars_sim.core.building.config.BuildingSpec;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.interplanetary.transport.resupply.Resupply;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.resource.AmountResource;
import com.mars_sim.core.resource.Part;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.vehicle.VehicleConfig;

@SuppressWarnings("serial")
public class SupplyTableModel
extends AbstractTableModel {

 	private static SimLogger logger = SimLogger.getLogger(SupplyTableModel.class.getName());

	// Supply categories.
	public static final String BUILDING = "Building";
	public static final String VEHICLE = "Vehicle";
	public static final String EQUIPMENT = "Equipment";
	public static final String RESOURCE = "Resource";
	public static final String PART = "Part";

	private static List<String> categoryList;
	private static Map<String, List<String>> categoryTypeMap;

	// Data members
	private List<SupplyItem> supplyList;
	
	private static BuildingConfig buildingConfig = SimulationConfig.instance().getBuildingConfiguration(); 
	private static VehicleConfig vehicleConfig = SimulationConfig.instance().getVehicleConfiguration();

	/**
	 * Constructor.
	 * 
	 * @param resupply the resupply mission or null if none.
	 */
	public SupplyTableModel(Resupply resupply) {

		// Initialize data members.
		supplyList = new ArrayList<>();

		if (resupply != null) {
			// Populate supply list from resupply mission.
			populateSupplies(resupply);
		}
	}

	/**
	 * Populates supply list from resupply mission.
	 * 
	 * @param resupply the resupply mission.
	 */
	private void populateSupplies(Resupply resupply) {

		// Populate buildings.
		List<String> buildingList = new ArrayList<>(resupply.getBuildings().size());
		Iterator<BuildingTemplate> l = resupply.getBuildings().iterator();
		while(l.hasNext()) {
			buildingList.add(l.next().getBuildingType());
		}
		
		// Create a supply type list 
		createSupplyTypeList(BUILDING, buildingList);

		// Populate vehicles.
		Map<String, Integer> newVehicles = resupply.getVehicles();
		List<String> sortVehicles = new ArrayList<>(newVehicles.keySet());
		Collections.sort(sortVehicles);
		for(String vechType : sortVehicles) {
			int num = newVehicles.get(vechType);
			SupplyItem supplyItem = new SupplyItem(VEHICLE, vechType, num);
			supplyList.add(supplyItem);
		}

		// Populate equipment.
		Map<String, Integer> newEquipment = resupply.getEquipment();
		List<String> sortEquipment = new ArrayList<>(newEquipment.keySet());
		Collections.sort(sortEquipment);
		for(String equipmentType : sortEquipment) {
			int num = newEquipment.get(equipmentType);
			SupplyItem supplyItem = new SupplyItem(EQUIPMENT, equipmentType, num);
			supplyList.add(supplyItem);
		}

		// Populate resources.
		Map<AmountResource, Double> newResources = resupply.getResources();
		List<AmountResource> sortResources = new ArrayList<>(newResources.keySet());
		Collections.sort(sortResources);
		for(AmountResource resource : sortResources) {
			double amount = newResources.get(resource);
			SupplyItem supplyItem = new SupplyItem(RESOURCE, resource.getName(), amount);
			supplyList.add(supplyItem);
		}

		// Populate parts.
		Map<Part, Integer> newParts = resupply.getParts();
		List<Part> sortParts = new ArrayList<>(newParts.keySet());
		Collections.sort(sortParts);
		for(Part part : sortParts) {
			int num = newParts.get(part);
			SupplyItem supplyItem = new SupplyItem(PART, part.getName(), num);
			supplyList.add(supplyItem);
		}
	}

	/**
	 * Populates supplies that are in a list of strings.
	 * 
	 * @param category the supply category.
	 * @param supplies the list of supplies as strings.
	 */
	private void createSupplyTypeList(String category, List<String> supplies) {

		// Create map of supplies and their numbers.
		Map<String, Integer> supplyMap = HashMap.newHashMap(supplies.size());
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
		List<String> sortKeys = new ArrayList<>(supplyMap.keySet());
		Collections.sort(sortKeys);
		Iterator<String> j = sortKeys.iterator();
		while (j.hasNext()) {
			String supplyType = j.next();
			int num = supplyMap.get(supplyType);
			SupplyItem supplyItem = new SupplyItem(category, supplyType, num);
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
		else if (column == 1) result = "Item";
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
			else if (colIndex == 1) result = item.type;
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

		if (row >= 0 && row < supplyList.size()) {
			SupplyItem item = supplyList.get(row);
			if (col == 0) {
				item.category = (String) value;
			}
			else if (col == 1) {
				item.type = (String) value;
			}
			else if (col == 2) {
				try {
					item.number = (Integer) value;
				}
				catch (NumberFormatException e) {
					logger.severe("SupplyTableModel Quantity invalid: " + e.getMessage());
				}
			}
			fireTableCellUpdated(row, col);
		}
	}

	/**
	 * Adds a new supply item with default values.
	 */
	 public void addNewSupplyItem() {
		String category = getCategoryList().get(0);
		String type = getCategoryTypeMap().get(category).get(0);
		int num = 1;
		supplyList.add(new SupplyItem(category, type, num));
		fireTableRowsInserted(supplyList.size() - 1, supplyList.size() - 1);
	 }

	 /**
	  * Removes items from the given rows.
	  * 
	  * @param rows an array of row indexes to remove.
	  */
	 public void removeSupplyItems(int[] rows) {

		 List<SupplyItem> items = new ArrayList<>(rows.length);
		 for (int x = 0; x < rows.length; x++) {
			 items.add(supplyList.get(rows[x]));
		 }

		 supplyList.removeAll(items);

		 fireTableDataChanged();
	 }

	 /**
	  * Gets the list of supply items.
	  * 
	  * @return list of supply items.
	  */
	 public List<SupplyItem> getSupplyItems() {
		 return new ArrayList<>(supplyList);
	 }

	 /**
	  * Gets a list of all categories.
	  * 
	  * @return list of category strings.
	  */
	 public static List<String> getCategoryList() {

		 if (categoryList == null) {
			 categoryList = new ArrayList<>();
			 categoryList.add(BUILDING);
			 categoryList.add(VEHICLE);
			 categoryList.add(EQUIPMENT);
			 categoryList.add(RESOURCE);
			 categoryList.add(PART);
		 }

		 return categoryList;
	 }

	 public static List<String> getSortedBuildingTypes() {
		return buildingConfig.getBuildingSpecs().stream()
					.map(BuildingSpec::getName)
					.sorted()
					.toList();
	 }

	 public static List<String> getSortedVehicleTypes() {
		 return vehicleConfig.getVehicleSpecs().stream()
		 							.map(v -> v.getName())
									.sorted()
									.toList();
	 }

	 /**
	  * Gets a map of categories and a list of their types.
	  * 
	  * @return map of categories to lists of types.
	  */
	 public static Map<String, List<String>> getCategoryTypeMap() {

		 if (categoryTypeMap == null) {
			 // Create and populate category type map.
			 categoryTypeMap = HashMap.newHashMap(5);

			 // Create building type list.
			 categoryTypeMap.put(BUILDING, getSortedBuildingTypes());

			 // Create vehicle type list.
			 categoryTypeMap.put(VEHICLE, getSortedVehicleTypes());

			 // Create equipment type list.
			 List<String> sortedEquipmentTypes = Arrays.stream(EquipmentType.values())
					 						.map(EquipmentType::name)
					 						.sorted()
					 						.toList();
			 categoryTypeMap.put(EQUIPMENT, sortedEquipmentTypes);

			 // Create resource type list.
			 var amountNames = ResourceUtil.getAmountResources().stream()
					 						.map(AmountResource::getName)
					 						.sorted()
					 						.toList();
			 categoryTypeMap.put(RESOURCE, amountNames);

			 // Create part type list.
			 var partNames = Part.getParts().stream()
					 						.map(Part::getName)
					 						.sorted()
					 						.toList();
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
		  * 
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
