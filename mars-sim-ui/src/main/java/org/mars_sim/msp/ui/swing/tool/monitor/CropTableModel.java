/*
 * Mars Simulation Project
 * CropTableModel.java
 * @date 2021-09-20
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.tool.monitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.SwingUtilities;

import org.apache.commons.lang3.StringUtils;
import org.mars_sim.msp.core.GameManager;
import org.mars_sim.msp.core.GameManager.GameMode;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitEvent;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.UnitManagerEvent;
import org.mars_sim.msp.core.UnitManagerEventType;
import org.mars_sim.msp.core.UnitManagerListener;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.farming.Crop;
import org.mars_sim.msp.core.structure.building.function.farming.CropCategoryType;
import org.mars_sim.msp.core.structure.building.function.farming.CropConfig;
import org.mars_sim.msp.core.structure.building.function.farming.Farming;

/**
 * The CropTableModel that maintains a list of crop related objects. It maps
 * food related info into Columns.
 */
@SuppressWarnings("serial")
public class CropTableModel extends UnitTableModel {

	/** default logger. */
//	private static final Logger logger = Logger.getLogger(CropTableModel.class.getName());

	private GameMode mode;

	// Column indexes
	private final static int SETTLEMENT_NAME = 0;
	private final static int GREENHOUSE_NAME = 1;
	private final static int CROPS = 2;

	private final static int FIRST_CROP_CAT = CROPS + 1;

	private static int numCropCat = CropCategoryType.values().length;// = 14
	/** The number of Columns. */
	private static int column_count = numCropCat + 3;

	/** Names of Columns. */
	private static String columnNames[];
	/** Types of columns. */
	private static Class<?> columnTypes[];

	static {
		columnNames = new String[column_count];
		columnTypes = new Class[column_count];
		columnNames[SETTLEMENT_NAME] = "Settlement";
		columnTypes[SETTLEMENT_NAME] = String.class;
		columnNames[GREENHOUSE_NAME] = "Name of Greenhouse";
		columnTypes[GREENHOUSE_NAME] = Integer.class;
		columnNames[CROPS] = "# Crops";
		columnTypes[CROPS] = Integer.class;

		for (CropCategoryType cat : CropCategoryType.values()) {
			int idx = FIRST_CROP_CAT + cat.ordinal();
			columnNames[idx] = StringUtils.capitalize(cat.getName());
			columnTypes[idx] = Integer.class;
		}
	};

	// Data members
	private UnitManagerListener unitManagerListener;

	private List<Settlement> paddedSettlements;
	private List<Building> buildings;
	private Map<Building, Integer> totalNumCropMap;
	private Map<Building, List<Integer>> cropCatMap;
	private Map<Integer, String> catMap;

	private Settlement commanderSettlement;

	private static UnitManager unitManager = Simulation.instance().getUnitManager();

	/*
	 * Constructs a FoodTableModel model that displays all Settlements in the
	 * simulation.
	 *
	 * @param unitManager Unit manager that holds settlements.
	 */
	public CropTableModel() throws Exception {
		super(Msg.getString("CropTableModel.tabName"), //$NON-NLS-1$
				"CropTableModel.countingCrops", //$NON-NLS-1$
				columnNames, columnTypes);

		totalNumCropMap = new ConcurrentHashMap<>();
		buildings = new ArrayList<>();
		paddedSettlements = new ArrayList<>();

		if (GameManager.getGameMode() == GameMode.COMMAND) {
			mode = GameMode.COMMAND;
			commanderSettlement = unitManager.getCommanderSettlement();
			addUnit(commanderSettlement);
		}
		else {
			setSource(unitManager.getSettlements());
		}

		updateMaps();

		unitManagerListener = new LocalUnitManagerListener();
		unitManager.addUnitManagerListener(unitManagerListener);

		if (catMap == null) {
			catMap = new ConcurrentHashMap<>();

			for (CropCategoryType type : CropCategoryType.values()) {
				int n = type.ordinal();
				String name = type.getName();
				catMap.put(n, name);
			}
		}
	}

	public void updateMaps() {
		paddedSettlements.clear();
		buildings.clear();

		List<Settlement> settlements = new ArrayList<Settlement>();

		if (mode == GameMode.COMMAND) {
			settlements.add(commanderSettlement);
		}
		else {
			settlements.addAll(unitManager.getSettlements());
			Collections.sort(settlements);
		}

		Iterator<Settlement> i = settlements.iterator();
		while (i.hasNext()) {
			Settlement s = i.next();
			List<Building> ghs = s.getBuildingManager().getBuildings(FunctionType.FARMING);
			Collections.sort(ghs);
			Iterator<Building> j = ghs.iterator();
			while (j.hasNext()) {
				Building b = j.next();
				paddedSettlements.add(s);
				buildings.add(b);
			}
		}
	}

	/**
	 * Give the position number for a particular crop group
	 *
	 * @param String cropCat
	 * @return a position number
	 */
	public int getCategoryNum(String cat) {
		return CropCategoryType.valueOf(cat.toUpperCase()).ordinal();
	}

	public String getCatName(int num) {

		return catMap.get(num);
	}

	/**
	 * Gets the total number of crop in a crop group from cropMap or cropCache
	 *
	 * @param return a number
	 */
	private Integer getValueAtCropCat(int rowIndex, int cropColumn) {
		int catNum = cropColumn - FIRST_CROP_CAT;
		List<Integer> cropCache = cropCatMap.get(buildings.get(rowIndex));
		Integer numCrop = cropCache.get(catNum);
		return numCrop;
	}

	/**
	 * Return the value of a Cell
	 *
	 * @param rowIndex    Row index of the cell.
	 * @param columnIndex Column index of the cell.
	 */
	public Object getValueAt(int rowIndex, int columnIndex) {
		Object result = null;

		int num = getRowCount();
		if (rowIndex < num) {

			try {
				switch (columnIndex) {

				case SETTLEMENT_NAME: {
					String i = paddedSettlements.get(rowIndex).getName();
					result = (Object) i;
				}
					break;

				case GREENHOUSE_NAME: {
					String name = buildings.get(rowIndex).getNickName();
					result = (Object) name;
				}
					break;

				case CROPS: {
					result = (Object) getTotalNumOfAllCrops(buildings.get(rowIndex));
				}
					break;

				default: {
					result = getValueAtCropCat(rowIndex, columnIndex);
				}
					break;

				}
			} catch (Exception e) {
			}
		}

		return result;
	}

	/**
	 * Gets the number of units in the model.
	 * Overload the super class (UnitTableModel)'s getUnitNumber()
	 * @return number of units.
	 */
	protected int getUnitNumber() {
		int result = 0;
		if (totalNumCropMap != null && !totalNumCropMap.isEmpty()) {
			for (Integer value : totalNumCropMap.values()) {
				result = result + (int) value;
			}
		} else {
			for (Building b : buildings) {
				result += getTotalNumOfAllCrops(b);
			}
		}

		return result;
	}

	/**
	 * Gets the model count string.
	 */
	public String getCountString() {
		return " " + Msg.getString("CropTableModel.countingCrops", Integer.toString(getUnitNumber())
		);
	}

	/**
	 * Get the number of rows in the model.
	 *
	 * @return the number of Units in the super class.
	 */
	public int getRowCount() {
		return buildings.size();
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
	 * Sets up a brand new local cropCache (a list of Integers) for a given
	 * settlement
	 *
	 * @param Unit newUnit
	 * @return an Integer List
	 */
	public List<Integer> setUpNewCropCache(Building b) {// Unit newUnit) {

		List<Integer> intList = new ArrayList<Integer>(numCropCat);
		// initialize the intList
		for (int i = 0; i < numCropCat; i++)
			intList.add(0);

//		CropConfig cropConfig = SimulationConfig.instance().getCropConfiguration();
		try {
			Farming farm = b.getFarming();
			List<Crop> cropsList = farm.getCrops();
//			int kk = 0;
			Iterator<Crop> k = cropsList.iterator();
			while (k.hasNext()) {
//				kk++;
				Crop crop = k.next();
				String catName = crop.getCropType().getCropCategoryType().getName();
				int num = getCategoryNum(catName);
				int val = intList.get(num) + 1;
				intList.set(num, val);
			}
		} catch (Exception e) {
		}
		return intList;
	}

	/**
	 * Catch unit update event.
	 *
	 * @param event the unit event.
	 */
	public void unitUpdate(UnitEvent event) {
		int unitIndex = -1;
		Unit unit = (Unit) event.getSource();
		UnitEventType eventType = event.getType();
		Object target = event.getTarget();


		if (mode == GameMode.COMMAND) {
			; // do nothing
		}
		else {
			unitIndex = getUnitIndex(unit);
		}

		int columnNum = -1;
		if (eventType == UnitEventType.NAME_EVENT)
			columnNum = SETTLEMENT_NAME; // = 0
		else if (eventType == UnitEventType.ADD_BUILDING_EVENT) {
			if (target instanceof Farming)
				columnNum = GREENHOUSE_NAME; // = 1
		}

		else if (eventType == UnitEventType.CROP_EVENT) {
			Crop crop = (Crop) target;
			String catName = crop.getCropType().getCropCategoryType().getName();

			try {
				int tempColumnNum = -1;

				tempColumnNum = getCategoryNum(catName);

				if (tempColumnNum > -1 && unitIndex > -1) {
					// Only update cell if value as int has changed.
					int currentValue = (Integer) getValueAt(unitIndex, tempColumnNum);
					int newValue = getNewValue(unit, catName);

					if (currentValue != newValue) {
						columnNum = tempColumnNum;

						List<Integer> cropCache = cropCatMap.get(unit);
						if (cropCache != null) {
							cropCache.set(tempColumnNum, newValue);
						}
					}
				}
			} catch (Exception e) {
			}
		}
		if (columnNum > -1) {
			SwingUtilities.invokeLater(new FoodTableCellUpdater(unitIndex, columnNum));

		}
	}

	/**
	 * Recompute the total number of cropType having a particular cropCategory
	 */
	public int getNewValue(Unit unit, String cropCat) {

		int result = 0;
		// Recompute only the total number of cropType having cropCategory = cropCat
		// Examine match the CropType within List<CropType> having having cropCategory
		Settlement settle = (Settlement) unit;
		BuildingManager bMgr = settle.getBuildingManager();
		List<Building> greenhouses = bMgr.getBuildings(FunctionType.FARMING);
		Iterator<Building> i = greenhouses.iterator();

		int total = 0;
		while (i.hasNext()) {
			try {
				Building greenhouse = i.next();
				Farming farm = greenhouse.getFarming();
				List<Crop> cropsList = farm.getCrops();

				Iterator<Crop> j = cropsList.iterator();
				while (j.hasNext()) {
					Crop crop = j.next();
					String catName = crop.getCropType().getCropCategoryType().getName();
					if (catName.equals(cropCat))
						total++;
				}
			} catch (Exception e) {
			}
		}
		result = total;
		return result;
	}

	/**
	 * Defines the source data from this table
	 */
	private void setSource(Collection<Settlement> source) {
		Iterator<Settlement> iter = source.iterator();
		while (iter.hasNext())
			addUnit(iter.next());
	}

	/**
	 * Add a unit (a settlement) to the model.
	 *
	 * @param newUnit Unit to add to the model.
	 */
	protected void addUnit(Unit newUnit) {
		if (cropCatMap == null)
			cropCatMap = new ConcurrentHashMap<>();
		// if cropCache does not a record of the settlement
		if (!paddedSettlements.contains(newUnit)) {
			try {// Setup a cropCache and cropMap in CropTableModel
				// All crops are to be newly added to the settlement
				updateMaps();

				for (Building b : buildings) {
					List<Integer> cropCache = setUpNewCropCache(b);
					cropCatMap.put(b, cropCache);
				}
			} catch (Exception e) {
			}
		}
		super.addUnit(newUnit);
	}

	/**
	 * Remove a unit from the model.
	 *
	 * @param oldUnit Unit to remove from the model.
	 */
	protected void removeUnit(Unit oldUnit) {
		if (paddedSettlements.contains(oldUnit)) {

			updateMaps();

			for (Building b : buildings) {
				if (b.getSettlement().equals(oldUnit))
					cropCatMap.remove(b);
			}

			buildings.remove(oldUnit);

		}

		super.removeUnit(oldUnit);
	}

	public String getToolTip(int row, int col) {
		StringBuilder tt = new StringBuilder();
		Building b = buildings.get(row);
		int catNum = cropCatMap.get(b).get(col);

		Farming f = b.getFarming();
		for (Crop c : f.getCrops()) {
				String catStr = c.getCropType().getCropCategoryType().toString();
			if (getCategoryNum(catStr) == catNum)
				tt.append(c.getCropName()).append(System.lineSeparator());
		}
		return tt.toString();
	}

	@Override
	public Unit getUnit(int row) {
		return (Unit) paddedSettlements.get(row);
	}

	/**
	 * Prepares the model for deletion.
	 */
	public void destroy() {
		super.destroy();

		unitManager.removeUnitManagerListener(unitManagerListener);
		unitManagerListener = null;

		cropCatMap = null;
		buildings = null;
		paddedSettlements = null;

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

	/**
	 * UnitManagerListener inner class.
	 */
	private class LocalUnitManagerListener implements UnitManagerListener {

		/**
		 * Catch unit manager update event.
		 *
		 * @param event the unit event.
		 */
		public void unitManagerUpdate(UnitManagerEvent event) {
			if (mode == GameMode.COMMAND) {
				; // do nothing
			}

			else {
				Unit unit = event.getUnit();
				UnitManagerEventType eventType = event.getEventType();

				if (unit.getUnitType() == UnitType.SETTLEMENT) {
					if (eventType == UnitManagerEventType.ADD_UNIT && !containsUnit(unit)) {
						addUnit(unit);
					} else if (eventType == UnitManagerEventType.REMOVE_UNIT && containsUnit(unit)) {
						removeUnit(unit);
					}
				}
			}
		}
	}
}
