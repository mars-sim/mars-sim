/**
 * Mars Simulation Project
 * CropTableModel.java
 * @version 3.1.0 2017-03-12
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

import org.mars_sim.msp.core.GameManager;
import org.mars_sim.msp.core.GameManager.GameMode;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitEvent;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.UnitManagerEvent;
import org.mars_sim.msp.core.UnitManagerEventType;
import org.mars_sim.msp.core.UnitManagerListener;
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
public class CropTableModel extends UnitTableModel {

	/** default logger. */
//	private static Logger logger = Logger.getLogger(CropTableModel.class.getName());

	private GameMode mode;
	
	// Column indexes
	private final static int SETTLEMENT_NAME = 0;
	private final static int GREENHOUSE_NAME = 1;
	private final static int CROPS = 2;

	private final static int BULBS = 3;
	private final static int CORMS = 4;
	private final static int FLOWERS = 5;
	private final static int FRUITS = 6;

	private final static int FUNGI = 7;
	private final static int GRAINS = 8;
	private final static int GRASSES = 9;
	private final static int LEAVES = 10;

	private final static int LEGUMES = 11;
	private final static int ROOTS = 12;
	private final static int SEEDS = 13;
//	 private final static int SPICES = 14;

	private final static int STEMS = 14;
	private final static int TUBERS = 15;

//	enum CropsEnum {
//		BULBS,CORMS,FLOWERS,FRUITS,
//		FUNGI,GRAINS,GRASSES,LEAVES,
//		LEGUMES,ROOTS,SEEDS,SPICES,
//		STEMS,TUBERS;
//
//	    //public static CropsEnum c(int ord) {
//	    //   return CropsEnum.values()[ord]; // less safe
//	    //}
//	}

	private String name = null;
	private int numHouse = 0;

	// private List<CropCategoryType> cropCategoryTypes = new
	// ArrayList<CropCategoryType>(Arrays.asList(CropCategoryType.values()));

	// 2014-11-25 Added NUMCROPTYPE
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

		columnNames[BULBS] = "Bulbs";
		columnTypes[BULBS] = Integer.class;
		columnNames[CORMS] = "Corms";
		columnTypes[CORMS] = Integer.class;
		columnNames[FLOWERS] = "Flowers";
		columnTypes[FLOWERS] = Integer.class;
		columnNames[FRUITS] = "Fruits";
		columnTypes[FRUITS] = Integer.class;

		columnNames[FUNGI] = "Fungi";
		columnTypes[FUNGI] = Integer.class;
		columnNames[GRAINS] = "Grains";
		columnTypes[GRAINS] = Integer.class;
		columnNames[GRASSES] = "Grasses";
		columnTypes[GRASSES] = Integer.class;
		columnNames[LEAVES] = "Leaves";
		columnTypes[LEAVES] = Integer.class;

		columnNames[LEGUMES] = "Legumes";
		columnTypes[LEGUMES] = Integer.class;
		columnNames[ROOTS] = "Roots";
		columnTypes[ROOTS] = Integer.class;
		columnNames[SEEDS] = "Seeds";
		columnTypes[SEEDS] = Integer.class;
//		 columnNames[SPICES] = "Spices";
//		 columnTypes[SPICES] = Integer.class;

		columnNames[STEMS] = "Stems";
		columnTypes[STEMS] = Integer.class;
		columnNames[TUBERS] = "Tubers";
		columnTypes[TUBERS] = Integer.class;

//		 * columnTypes[CropsEnum.BULBS.ordinal()+3] = Integer.class;
//		 * columnNames[CropsEnum.BULBS.ordinal()+3] = "Bulbs";
//		 * columnTypes[CropsEnum.BULBS.ordinal()+3] = Integer.class;
//		 * columnNames[CropsEnum.CORMS.ordinal()+3] = "Corms";
//		 * columnTypes[CropsEnum.CORMS.ordinal()+3] = Integer.class;
//		 * columnNames[CropsEnum.FLOWERS.ordinal()+3] = "Flowers";
//		 * columnTypes[CropsEnum.FLOWERS.ordinal()+3] = Integer.class;
//		 * columnNames[CropsEnum.FRUITS.ordinal()+3] = "Fruits";
//		 * columnTypes[CropsEnum.FRUITS.ordinal()+3] = Integer.class;
//		 * 
//		 * columnNames[CropsEnum.FUNGI.ordinal()+3] = "Fungi";
//		 * columnTypes[CropsEnum.FUNGI.ordinal()+3] = Integer.class;
//		 * columnNames[CropsEnum.GRAINS.ordinal()+3] = "Grains";
//		 * columnTypes[CropsEnum.GRAINS.ordinal()+3] = Integer.class;
//		 * columnNames[CropsEnum.GRASSES.ordinal()+3] = "Grasses";
//		 * columnTypes[CropsEnum.GRASSES.ordinal()+3] = Integer.class;
//		 * columnNames[CropsEnum.LEAVES.ordinal()+3] = "Legumes";
//		 * columnTypes[CropsEnum.LEAVES.ordinal()+3] = Integer.class;
//		 * 
//		 * columnNames[CropsEnum.LEGUMES.ordinal()+3] = "Legumes";
//		 * columnTypes[CropsEnum.LEGUMES.ordinal()+3] = Integer.class;
//		 * columnNames[CropsEnum.ROOTS.ordinal()+3] = "Roots";
//		 * columnTypes[CropsEnum.ROOTS.ordinal()+3] = Integer.class;
//		 * columnNames[CropsEnum.SEEDS.ordinal()+3] = "Seeds";
//		 * columnTypes[CropsEnum.SEEDS.ordinal()+3] = Integer.class;
//		 * columnNames[CropsEnum.SPICES.ordinal()+3] = "Spices";
//		 * columnTypes[CropsEnum.SPICES.ordinal()+3] = Integer.class;
//		 * 
//		 * columnNames[CropsEnum.STEMS.ordinal()+3] = "Stems";
//		 * columnTypes[CropsEnum.STEMS.ordinal()+3] = Integer.class;
//		 * columnNames[CropsEnum.TUBERS.ordinal()+3] = "Tubers";
//		 * columnTypes[CropsEnum.TUBERS.ordinal()+3] = Integer.class;

	};

	// Data members
	private UnitManagerListener unitManagerListener;
	// private Map<Unit, List<Integer>> unitCache;

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
	public CropTableModel() {
		super(Msg.getString("CropTableModel.tabName"), //$NON-NLS-1$
				"CropTableModel.countingCrops", //$NON-NLS-1$
				columnNames, columnTypes);

		totalNumCropMap = new ConcurrentHashMap<>();
		buildings = new ArrayList<Building>();
		paddedSettlements = new ArrayList<Settlement>();

		if (GameManager.mode == GameMode.COMMAND) {
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
			catMap = new ConcurrentHashMap<Integer, String>();

			for (CropCategoryType type : CropCategoryType.values()) {
				int n = type.ordinal();
				String name = type.getName();
				catMap.put(n, name);
			}
		}

		// updateBuildings();
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
	// Called by getTotalNumforCropGroup() which in terms was called by getValueAt()
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
	// Called by getValueAt()
	public Integer getValueAtColumn(int rowIndex, String cropCat) {
		// logger.info("getValueAtColumn() : entering");
		// Settlement settle = (Settlement)getUnit(rowIndex);
		int catNum = getCategoryNum(cropCat);
		// logger.info("getValueAtColumn() : groupNumber : "+groupNumber);
		List<Integer> cropCache = cropCatMap.get(buildings.get(rowIndex));
		Integer numCrop = cropCache.get(catNum);
		// logger.info("numCrop is " + numCrop);
		return numCrop;
	}

	/**
	 * Return the value of a Cell
	 * 
	 * @param rowIndex    Row index of the cell.
	 * @param columnIndex Column index of the cell.
	 */
	public Object getValueAt(int rowIndex, int columnIndex) {
		// logger.info("getValueAt() : Just Entered ");
		// logger.info("rowIndex : " + rowIndex );
		// logger.info("columnIndex : " + columnIndex);

		Object result = null;

		int num = getRowCount();
		if (rowIndex < num) {

			// Settlement settle = (Settlement)getUnit(rowIndex);
			// BuildingManager bMgr = settle.getBuildingManager();

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

				case BULBS: {
					result = getValueAtColumn(rowIndex, "Bulbs");
				}
					break;

				case CORMS: {
					result = getValueAtColumn(rowIndex, "Corms");
				}
					break;

				case FLOWERS: {
					result = getValueAtColumn(rowIndex, "Flowers");
				}
					break;

				case FRUITS: {
					result = getValueAtColumn(rowIndex, "Fruits");
				}
					break;

				case FUNGI: {
					result = getValueAtColumn(rowIndex, "Fungi");
				}
					break;

				case GRAINS: {
					result = getValueAtColumn(rowIndex, "Grains");
				}
					break;

				case GRASSES: {
					result = getValueAtColumn(rowIndex, "Grasses");
				}
					break;

				case LEAVES: {
					result = getValueAtColumn(rowIndex, "Leaves");
				}
					break;

				case LEGUMES: {
					result = getValueAtColumn(rowIndex, "Legumes");
				}
					break;

				case ROOTS: {
					result = getValueAtColumn(rowIndex, "Roots");
				}
					break;

				case SEEDS: {
					result = getValueAtColumn(rowIndex, "Seeds");
				}
					break;

				// case SPICES : {
				// result = getValueAtColumn(rowIndex, "Spices");
				// } break;

				case STEMS: {
					result = getValueAtColumn(rowIndex, "Stems");
				}
					break;

				case TUBERS: {
					result = getValueAtColumn(rowIndex, "Tubers");
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
	 * 
	 * @return number of units.
	 */
	// Overload the super class (UnitTableModel)'s getUnitNumber()
	protected int getUnitNumber() {
		int result = 0;
		if (totalNumCropMap != null && !totalNumCropMap.isEmpty()) {

//			Iterator<Map.Entry<Settlement, Integer>> i = cropMap.entrySet().iterator();
//			if(i.hasNext()){
//				Map.Entry<Settlement, Integer> entry = i.next();
//				int value = entry.getValue();
//				result = result + value;
//			}

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
//				getUnitNumber()
		);
	}

	/**
	 * Get the number of rows in the model.
	 * 
	 * @return the number of Units in the super class.
	 */
	public int getRowCount() {
		// return super.getUnitNumber();
		// int sum = 0;
		// for (Settlement s : unitManager.getSettlements()) {
		// List<Building> ghs =
		// s.getBuildingManager().getBuildings(BuildingFunction.FARMING);
		// sum += ghs.size();
		// }
		// for (Building b : buildings) {
		// Farming f = (Farming) b.getFunction(BuildingFunction.FARMING);
		// sum += f.getCrops().size();
		// }
		return buildings.size();
		// return sum;
	}

//	/**
//	 * Gets the number of units in the model.
//	 * @return number of units.
//
//	// Overload UnitTableModel's getUnitNumber()
//	protected int getNumSettlement() {
//		int size = 0;
//		if(getRefreshSize()){
//			size = getUnits() == null ? 0 : getSize();
//			setRefreshSize(false);
//		}
//		//        if (units != null) return units.size();
//		//    	else return 0;
//		setSize(size);
//		return size;
//	}

	/**
	 * Gets the total numbers of all crops in a greenhouse building
	 * 
	 * @param b Building
	 * @return total num of crops
	 */
	// Called by getValueAt()
	public int getTotalNumOfAllCrops(Building b) {
		int num = 0;

		Farming farm = b.getFarming();// (Farming) b.getFunction(FunctionType.FARMING);
		num += farm.getCrops().size();

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
	// Called by addUnit()
	public List<Integer> setUpNewCropCache(Building b) {// Unit newUnit) {

		List<Integer> intList = new ArrayList<Integer>(numCropCat);
		// initialize the intList
		for (int i = 0; i < numCropCat; i++)
			intList.add(0);

		// Settlement settle = (Settlement) newUnit;
		// BuildingManager bMgr = settle.getBuildingManager();
		// List<Building> greenhouses = bMgr.getBuildings(BuildingFunction.FARMING);
		// Iterator<Building> i = greenhouses.iterator();

		// while (i.hasNext()) {
		try {
			// Building greenhouse = i.next();
			Farming farm = (Farming) b.getFunction(FunctionType.FARMING);
			List<Crop> cropsList = farm.getCrops();
			int kk = 0;
			// logger.info("setUpNewCropCache() : cropsList.size is " + cropsList.size() ) ;
			Iterator<Crop> k = cropsList.iterator();
			while (k.hasNext()) {
				kk++;
				// logger.info("setUpNewCropCache() : kk is " + kk ) ;
				Crop crop = k.next();
				int id = crop.getCropTypeID();
				String catName = CropConfig.getCropCategoryType(id).getName();
				// logger.info("setUpNewCropCache() : testCat is " + testCat ) ;
				int num = getCategoryNum(catName);
				// logger.info("setUpNewCropCache() : num is " + num ) ;
				int val = intList.get(num) + 1;
				// logger.info("setUpNewCropCache() : val is " + val ) ;
				intList.set(num, val);
				// logger.info("setUpNewCropCache() : intList.get(" + num + ") : " +
				// intList.get(num));
			}
		} catch (Exception e) {
		}
		// }

		// logger.info("setUpNewCropCache() : intList.toString() : " +
		// intList.toString());

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
			// logger.info("unitUpdate() : CROP_EVENT");
			// logger.info("unitUpdate() : eventType : " + eventType.toString());
			// TODO: check with total Crops get updated
			// columnNum = CROPS; // = 2
			Crop crop = (Crop) target;
			int id = crop.getCropTypeID();
			String catName = CropConfig.getCropCategoryType(id).getName();
			// logger.info("unitUpdate() : cropCat is " + cropCat);

			try {
				int tempColumnNum = -1;

				tempColumnNum = getCategoryNum(catName);
				// logger.info(" tempColumnNum : " + tempColumnNum);

				if (tempColumnNum > -1 && unitIndex > -1) {
					// Only update cell if value as int has changed.
					int currentValue = (Integer) getValueAt(unitIndex, tempColumnNum);
					int newValue = getNewValue(unit, catName);

					if (currentValue != newValue) {
						columnNum = tempColumnNum;

						List<Integer> cropCache = cropCatMap.get(unit);
						cropCache.set(tempColumnNum, newValue);
					}
				}
			} catch (Exception e) {
			}
		} // end of else if (eventType == UnitEventType.CROP_EVENT) {
		if (columnNum > -1) {
			SwingUtilities.invokeLater(new FoodTableCellUpdater(unitIndex, columnNum));
			// Exception in thread "pool-5-thread-2" java.lang.NoSuchMethodError:
			// org.mars_sim.msp.ui.swing.tool.monitor.CropTableModel$FoodTableCellUpdater.<init>(Lorg/mars_sim/msp/ui/swing/tool/monitor/CropTableModel;IILorg/mars_sim/msp/ui/swing/tool/monitor/CropTableModel$1;)V

		}
	}

	/**
	 * Recompute the total number of cropType having a particular cropCategory
	 */
	public int getNewValue(Unit unit, String cropCat) {

		int result = 0;
		// recompute only the total number of cropType having cropCategory = cropCat
		// examine match the CropType within List<CropType> having having cropCategory
		Settlement settle = (Settlement) unit;
		BuildingManager bMgr = settle.getBuildingManager();
		List<Building> greenhouses = bMgr.getBuildings(FunctionType.FARMING);
		Iterator<Building> i = greenhouses.iterator();

		int total = 0;
		while (i.hasNext()) {
			try {
				Building greenhouse = i.next();
				Farming farm = greenhouse.getFarming();// (Farming) greenhouse.getFunction(FunctionType.FARMING);
				List<Crop> cropsList = farm.getCrops();

				Iterator<Crop> j = cropsList.iterator();
				while (j.hasNext()) {
					Crop crop = j.next();
					int id = crop.getCropTypeID();
					String catName = CropConfig.getCropCategoryType(id).getName();
					// System.out.println("type is " + type);
					if (catName.equals(cropCat))
						total++;
				}
			} catch (Exception e) {
			}
		}
		result = total;
		// logger.info("getNewNumCropAtSameCat() : cropCat : " + cropCat + ", total : "
		// + total);
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
		// logger.info("addUnit() : just entered in " + newUnit);
		if (cropCatMap == null)
			cropCatMap = new ConcurrentHashMap<Building, List<Integer>>();
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
		// logger.info("addUnit() : leaving " );
	}

	/**
	 * Remove a unit from the model.
	 * 
	 * @param oldUnit Unit to remove from the model.
	 */
	protected void removeUnit(Unit oldUnit) {
		if (paddedSettlements.contains(oldUnit)) {
			// List<Integer> cropCache = unitCache.get(oldUnit);
			// TODO: need to check if this way can remove the unit and cropCache
			// cropCache.clear();
			// cropCache.remove(oldUnit);
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
				int id = c.getCropTypeID();
				String catStr = CropConfig.getCropCategoryType(id).toString();
			if (getCategoryNum(catStr) == catNum)
				tt.append(c.getCropName()).append(System.lineSeparator());
		}
		System.out.println(tt);
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

		// UnitManager unitManager = Simulation.instance().getUnitManager();
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
				
				if (unit instanceof Settlement) {
					if (eventType == UnitManagerEventType.ADD_UNIT && !containsUnit(unit)) {
						addUnit(unit);
						// updateBuildings();
						// logger.info(unit + " has just entered");
					} else if (eventType == UnitManagerEventType.REMOVE_UNIT && containsUnit(unit)) {
						removeUnit(unit);
						// updateBuildings();
						// logger.info(unit + " has just entered");
					}
				}
			}
		}
	}
}