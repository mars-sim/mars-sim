/*
 * Mars Simulation Project
 * CropTableModel.java
 * @date 2022-06-28
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.tool.monitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import org.apache.commons.lang3.StringUtils;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitEvent;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.farming.Crop;
import org.mars_sim.msp.core.structure.building.function.farming.CropCategory;
import org.mars_sim.msp.core.structure.building.function.farming.Farming;

/**
 * The CropTableModel keeps track of the quantity of the growing crops in each greenhouse by categories.
 */
@SuppressWarnings("serial")
public class CropTableModel extends UnitTableModel {

	/** default logger. */
	private static final Logger logger = Logger.getLogger(CropTableModel.class.getName());

	// Column indexes
	private static final int GREENHOUSE_NAME = 0;
	private static final int SETTLEMENT_NAME = 1;
	private static final int INITIAL_COLS = 2;

	private static final int FIRST_CROP_CAT = INITIAL_COLS + 1;
	
	/** The total number of available crop category. */
	private static int numCropCat = CropCategory.values().length;// = 14
	
	/** The number of Columns. */
	private static int column_count = numCropCat + 3;

	/** Names of Columns. */
	private static String[] columnNames;
	/** Types of columns. */
	private static Class<?>[] columnTypes;

	static {
		columnNames = new String[column_count];
		columnTypes = new Class[column_count];
		columnNames[GREENHOUSE_NAME] = "Name of Greenhouse";
		columnTypes[GREENHOUSE_NAME] = String.class;
		columnNames[SETTLEMENT_NAME] = "Settlement";
		columnTypes[SETTLEMENT_NAME] = String.class;
		columnNames[INITIAL_COLS] = "# Crops";
		columnTypes[INITIAL_COLS] = Integer.class;

		for (CropCategory cat : CropCategory.values()) {
			int idx = FIRST_CROP_CAT + cat.ordinal();
			columnNames[idx] = StringUtils.capitalize(cat.getName());
			columnTypes[idx] = Integer.class;
		}
	};

	// Data members
	/**
	 * A list of crop categories.
	 */
	private List<CropCategory> cropCategoryList;
	/**
	 * A list of greenhouse buildings.
	 */
	private List<Building> buildings;
	/**
	 * A map of greenhouse buildings with # of growing crops.
	 */
	private Map<Building, Integer> totalNumCropMap;
	/**
	 * A map of greenhouse buildings having a list of crop category and having a number of growing crops.
	 */
	private Map<Building, Map<CropCategory, Integer>> cropCatMap;

	private Settlement selectedSettlement;

	public CropTableModel(Settlement settlement) {
		super (UnitType.BUILDING, Msg.getString("CropTableModel.tabName"), //$NON-NLS-1$
				"CropTableModel.countingCrops", //$NON-NLS-1$
				columnNames, columnTypes);
		cropCategoryList = new ArrayList<>(List.of(CropCategory.values()));
	
		setSettlementFilter(settlement);

		listenForUnits();
	}

	/**
	 * Filter the Greenhouses according to a Settlement
	 */
	@Override
	public void setSettlementFilter(Settlement filter) {
		selectedSettlement = filter;
		
		totalNumCropMap = new ConcurrentHashMap<>();
		cropCatMap = new ConcurrentHashMap<>();

		buildings = new ArrayList<>();
		
		createBuildingCropCatMap();
		
		updateCropCatMap();

		resetUnits(buildings);

	}

	/**
	 * Creates the building crop category map.
	 */
	private void createBuildingCropCatMap() {

		List<Building> ghs = selectedSettlement.getBuildingManager().getBuildings(FunctionType.FARMING);
		Collections.sort(ghs);
		Iterator<Building> j = ghs.iterator();
		while (j.hasNext()) {
			Building b = j.next();
			if (!buildings.contains(b)) {
				buildings.add(b);
				Map<CropCategory, Integer> map = new ConcurrentHashMap<>();
				for (CropCategory type : CropCategory.values()) {
					map.put(type, 0);
				}
				cropCatMap.put(b, map);
			}
		}
	}
	
	/**
	 * Updates the list of crops according to the category for each building.
	 */
	private void updateCropCatMap() {		
		try {
			for (Building b: buildings) {
				Iterator<Crop> k = b.getFarming().getCrops().iterator();
				while (k.hasNext()) {
					CropCategory cat = k.next().getCropSpec().getCropCategory();
					Map<CropCategory, Integer> innerCatMap = null;
					if (cropCatMap.containsKey(b)) {
						innerCatMap = cropCatMap.get(b);
						if (innerCatMap.containsKey(cat)) {
							int num1 = innerCatMap.get(cat);
							num1++;
							innerCatMap.put(cat, num1);
						}
						else {
							innerCatMap = new ConcurrentHashMap<>();
							innerCatMap.put(cat, 1);
						}
					}
				}

			}
		} catch (Exception e) {
			logger.severe("updateCropCatMap not working: " + e.getMessage());
		}
	}
	
	/**
	 * Gives the position number for a particular crop group.
	 *
	 * @param String cropCat
	 * @return a position number
	 */
	public int getCategoryNum(String cat) {
		return CropCategory.valueOf(cat.toUpperCase()).ordinal();
	}

	/**
	 * Gets the total number of crop in a crop group from cropMap or cropCache.
	 *
	 * @param return a number
	 */
	private Object getValueAtCropCat(int rowIndex, int cropColumn) {
		int catNum = cropColumn - FIRST_CROP_CAT;
		return cropCatMap.get(buildings.get(rowIndex)).get(cropCategoryList.get(catNum));
	}

	/**
	 * Return the value of a Cell.
	 *
	 * @param rowIndex    Row index of the cell.
	 * @param columnIndex Column index of the cell.
	 */
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Object result = null;

		int num = getRowCount();
		if (rowIndex < num) {

			try {
				switch (columnIndex) {

				case GREENHOUSE_NAME: {
					String name = buildings.get(rowIndex).getNickName();
					result = (Object) name;
				}
					break;

				case SETTLEMENT_NAME: {
					String i = buildings.get(rowIndex).getSettlement().getName();
					result = (Object) i;
				}
					break;
					
				case INITIAL_COLS: {
					result = (Object) getTotalNumOfAllCrops(buildings.get(rowIndex));
				}
					break;

				default: {
					result = getValueAtCropCat(rowIndex, columnIndex);
				}
					break;

				}
			} catch (Exception e) {
				logger.severe("getValueAt not working: " + e.getMessage());
			}
		}

		return result;
	}

	/**
	 * Gets the model count string.
	 */
	@Override
	public String getCountString() {
		return " " + Msg.getString("CropTableModel.countingCrops", //$NON-NLS-1$
				Integer.toString(getRowCount()));
	}

	/**
	 * Gets the total numbers of all crops in a greenhouse building
	 *
	 * @param b Building
	 * @return total num of crops
	 */
	public int getTotalNumOfAllCrops(Building b) {
		int num = 0;

		num += b.getFarming().getCrops().size();

		totalNumCropMap.put(b, num);
		return num;
	}

	/**
	 * Catch unit update event.
	 *
	 * @param event the unit event.
	 */
	@Override
	public void unitUpdate(UnitEvent event) {
		Unit unit = (Unit) event.getSource();
		int unitIndex = getIndex(unit);
		UnitEventType eventType = event.getType();
		Object target = event.getTarget();

		int columnNum = -1;
		if (eventType == UnitEventType.NAME_EVENT)
			columnNum = SETTLEMENT_NAME; // = 0
		else if (eventType == UnitEventType.ADD_BUILDING_EVENT) {
			if (target instanceof Farming)
				columnNum = GREENHOUSE_NAME; // = 1
		}

		else if (eventType == UnitEventType.CROP_EVENT) {
			Crop crop = (Crop) target;
			CropCategory cat = crop.getCropSpec().getCropCategory();

			try {
				int tempColumnNum = -1;

				tempColumnNum = getCategoryNum(cat.getName());

				if (tempColumnNum > -1 && unitIndex > -1) {
					// Only update cell if value as int has changed.
					int currentValue = (Integer) getValueAt(unitIndex, tempColumnNum);
					int newValue = getNewValue(unit, cat);

					if (currentValue != newValue) {
						columnNum = tempColumnNum;

						Map<CropCategory, Integer> cropCache = cropCatMap.get(unit);
						if (cropCache != null) {
							cropCache.put(cat, newValue);
						}
					}
				}
			} catch (Exception e) {
				logger.severe("unitUpdate not working: " + e.getMessage());
			}
		}
		if (columnNum > -1) {
			SwingUtilities.invokeLater(new FoodTableCellUpdater(unitIndex, columnNum));
		}
	}

	/**
	 * Recomputes the total number of cropType having a particular cropCategory.
	 * 
	 * @param unit
	 * @param cropCat
	 * @return
	 */
	public int getNewValue(Unit unit, CropCategory cropCat) {
		int result = 0;
		
		if (unit.getUnitType() == UnitType.SETTLEMENT) {
			List<Building> greenhouses = ((Settlement) unit).getBuildingManager().getBuildings(FunctionType.FARMING);
			Iterator<Building> i = greenhouses.iterator();
	
			while (i.hasNext()) {
				try {
					Farming farm = i.next().getFarming();
					Iterator<Crop> j = farm.getCrops().iterator();
					while (j.hasNext()) {
						Crop crop = j.next();
						CropCategory cat = crop.getCropSpec().getCropCategory();
						// Match the crop name within the current list of crops having the same cropCategory
						if (cat == cropCat) {
							result++;
							// Do not break here since other greenhouses may also have this crop category name
						}
					}
				} catch (Exception e) {
					logger.severe("getNewValue not working: " + e.getMessage());
				}
			}
		}
		else if (unit.getUnitType() == UnitType.BUILDING) {
			Farming farm = ((Building)unit).getFarming();
			Iterator<Crop> j = farm.getCrops().iterator();
			while (j.hasNext()) {
				Crop crop = j.next();
				CropCategory cat = crop.getCropSpec().getCropCategory();
				// Match the crop name within the current list of crops having the same cropCategory
				if (cat == cropCat) {
					result++;
					// Do not break here since other greenhouses may also have this crop category name
				}
			}
		}
		
		return result;
	}

	// Need to find out how to call this method and how to associate it with TableTab.
	public String getToolTip(int row, int col) {
		StringBuilder tt = new StringBuilder();
		Building b = buildings.get(row);
		CropCategory cat = cropCategoryList.get(col);

		Farming f = b.getFarming();
		for (Crop c : f.getCrops()) {
			CropCategory cat1 = c.getCropSpec().getCropCategory();
			if (cat1 == cat)
				tt.append(c.getCropName()).append(System.lineSeparator());
		}
		
		
		return tt.toString();
	}

	/**
	 * Prepares the model for deletion.
	 */
	@Override
	public void destroy() {
		super.destroy();

		cropCatMap = null;
		buildings = null;
	}

	private class FoodTableCellUpdater implements Runnable {
		private int row;
		private int column;

		private FoodTableCellUpdater(int row, int column) {
			this.row = row;
			this.column = column;
		}

		public void run() {
			fireTableCellUpdated(row, column);
		}
	}
}
