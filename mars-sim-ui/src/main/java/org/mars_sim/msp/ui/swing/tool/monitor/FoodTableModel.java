/**
 * Mars Simulation Project
 * FoodTableModel.java
 * @version 3.07 2014-10-15
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.tool.monitor;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitEvent;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.UnitManagerEvent;
import org.mars_sim.msp.core.UnitManagerEventType;
import org.mars_sim.msp.core.UnitManagerListener;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.BuildingFunction;
import org.mars_sim.msp.core.structure.building.function.Farming;


/**
 * The FoodTableModel that maintains a list of Food related objects.
 * It maps food related info into Columns.
 */
// 2014-10-14 mkung 
// Relocated all food related objects from SettlementTableModel Class to here
// Incorporated five major food groups into MSP
public class FoodTableModel
extends UnitTableModel {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Column indexes
	private final static int NAME = 0;
	private final static int GREENHOUSES = 1;
	private final static int CROPS = 2;
	
	private final static int FOOD = 3;
	private final static int FRUITS = 4;
	private final static int GRAINS = 5;
	private final static int LEGUMES = 6;
	private final static int SPICES = 7;
	private final static int VEGETABLES = 8;

	
	/** The number of Columns. */
	private final static int COLUMNCOUNT = 9;
	/** Names of Columns. */
	private static String columnNames[];
	/** Types of columns. */
	private static Class<?> columnTypes[];

	static {
		columnNames = new String[COLUMNCOUNT];
		columnTypes = new Class[COLUMNCOUNT];
		columnNames[NAME] = "Name";
		columnTypes[NAME] = String.class;
		columnNames[GREENHOUSES] = "Greenhouses";
		columnTypes[GREENHOUSES] = Integer.class;
		columnNames[CROPS] = "Crops";
		columnTypes[CROPS] = Integer.class;
		columnNames[FOOD] = "Food";
		columnTypes[FOOD] = Integer.class;
		columnNames[FRUITS] = "Fruits";
		columnTypes[FRUITS] = Integer.class;
		columnNames[GRAINS] = "Grains";
		columnTypes[GRAINS] = Integer.class;
		columnNames[VEGETABLES] = "Vegetables";
		columnTypes[VEGETABLES] = Integer.class;
		columnNames[LEGUMES] = "Legumes";
		columnTypes[LEGUMES] = Integer.class;
		columnNames[SPICES] = "Spices";
		columnTypes[SPICES] = Integer.class;
	};

	// Data members
	private UnitManagerListener unitManagerListener;
	private Map<Unit, Map<AmountResource, Integer>> resourceCache;

	/**
	 * Constructs a FoodTableModel model that displays all Settlements
	 * in the simulation.
	 *
	 * @param unitManager Unit manager that holds settlements.
	 */
	public FoodTableModel(UnitManager unitManager) {
		super(
			"Food Production ",
			"SettlementTableModel.countingSettlements", //$NON-NLS-1$
			columnNames,
			columnTypes
		);

		setSource(unitManager.getSettlements());
		unitManagerListener = new LocalUnitManagerListener();
		unitManager.addUnitManagerListener(unitManagerListener);
	}

	/**
	 * Return the value of a Cell
	 * @param rowIndex Row index of the cell.
	 * @param columnIndex Column index of the cell.
	 */
	public Object getValueAt(int rowIndex, int columnIndex) {
		Object result = null;

		if (rowIndex < getUnitNumber()) {
			Settlement settle = (Settlement)getUnit(rowIndex);
			BuildingManager bMgr = settle.getBuildingManager();
			Map<AmountResource, Integer> resourceMap = resourceCache.get(settle);

			try {
				// Invoke the appropriate method, switch is the best solution
				// althought disliked by some
				switch (columnIndex) {
				
				case NAME : {
					result = settle.getName();
				} break;


				case GREENHOUSES : {
					int greenhouses = bMgr.getBuildings(BuildingFunction.FARMING).size();
					result = greenhouses;
				} break;
				
				case FOOD : {
					result = resourceMap.get(
							AmountResource.findAmountResource("food"));
				} break;

				case FRUITS : {
					result = resourceMap.get(
							AmountResource.findAmountResource("Fruit Group"));
				} break;

				case GRAINS : {
					result = resourceMap.get(
							AmountResource.findAmountResource("Grain Group"));
				} break;

				case VEGETABLES : {
					result = resourceMap.get(
							AmountResource.findAmountResource("Vegetable Group"));
				} break;

				case LEGUMES: {
					result = resourceMap.get(
							AmountResource.findAmountResource("Legume Group"));
				} break;

				case SPICES : {
					result = resourceMap.get(
							AmountResource.findAmountResource("Spice Group"));
				} break;
				
				case CROPS : {
					int crops = 0;
					List<Building> greenhouses = bMgr.getBuildings(BuildingFunction.FARMING);
					Iterator<Building> i = greenhouses.iterator();
					while (i.hasNext()) {
						try {
							Building greenhouse = i.next();
							Farming farm = (Farming) greenhouse.getFunction(BuildingFunction.FARMING);
							crops += farm.getCrops().size();
						}
						catch (Exception e) {}
					}

					result = crops;
				} break;

				}
			}
			catch (Exception e) {}
		}

		return result;
	}

	/**
	 * Catch unit update event.
	 * @param event the unit event.
	 */
	public void unitUpdate(UnitEvent event) {
		Unit unit = (Unit) event.getSource();
		int unitIndex = getUnitIndex(unit);
		Object target = event.getTarget();
		UnitEventType eventType = event.getType();

		int columnNum = -1;
		if (eventType == UnitEventType.NAME_EVENT) columnNum = NAME;
		else if (eventType == UnitEventType.ADD_BUILDING_EVENT) {
			if (target instanceof Farming) columnNum = GREENHOUSES;
		}
		else if (eventType == UnitEventType.CROP_EVENT) columnNum = CROPS;

		else if (eventType == UnitEventType.INVENTORY_RESOURCE_EVENT) {
			try {
				int tempColumnNum = -1;			
				if (target.equals(AmountResource.findAmountResource("food"))) 
					tempColumnNum = FOOD;
				else if (target.equals(AmountResource.findAmountResource("Fruit Group"))) 
					tempColumnNum = FRUITS;
				else if (target.equals(AmountResource.findAmountResource("Grain Group"))) 
					tempColumnNum = GRAINS;
				else if (target.equals(AmountResource.findAmountResource("Vegetable Group"))) 
					tempColumnNum = VEGETABLES;
				else if (target.equals(AmountResource.findAmountResource("Grain Group"))) 
					tempColumnNum = GRAINS;
				else if (target.equals(AmountResource.findAmountResource("Legume Group"))) 
					tempColumnNum = LEGUMES;
				else if (target.equals(AmountResource.findAmountResource("Spice Group"))) 
					tempColumnNum = SPICES;
				
				if (tempColumnNum > -1) {
					// Only update cell if value as int has changed.
					int currentValue = (Integer) getValueAt(unitIndex, tempColumnNum);
					int newValue = getResourceStored(unit, (AmountResource) target);
					if (currentValue != newValue) {
						columnNum = tempColumnNum;
						Map<AmountResource, Integer> resourceMap = resourceCache.get(unit);
						resourceMap.put((AmountResource) target, newValue);
					}
				}
			}
			catch (Exception e) {}
		}

		if (columnNum > -1) {
			SwingUtilities.invokeLater(new FoodTableCellUpdater(unitIndex, columnNum));
		}
	}

	/**
	 * Defines the source data from this table
	 */
	private void setSource(Collection<Settlement> source) {
		Iterator<Settlement> iter = source.iterator();
		while(iter.hasNext()) addUnit(iter.next());
	}

	/**
	 * Add a unit to the model.
	 * @param newUnit Unit to add to the model.
	 */
	protected void addUnit(Unit newUnit) {
		if (resourceCache == null) resourceCache = new HashMap<Unit, Map<AmountResource, Integer>>();
		if (!resourceCache.containsKey(newUnit)) {
			try {
				Map<AmountResource, Integer> resourceMap = new HashMap<AmountResource, Integer>(9);
				AmountResource food = AmountResource.findAmountResource("food");
				resourceMap.put(food, getResourceStored(newUnit, food));
				AmountResource fruits = AmountResource.findAmountResource("Fruit Group");
				resourceMap.put(fruits, getResourceStored(newUnit, fruits));
				AmountResource grains = AmountResource.findAmountResource("Grain Group");
				resourceMap.put(grains, getResourceStored(newUnit, grains));
				AmountResource vegetables = AmountResource.findAmountResource("Vegetable Group");
				resourceMap.put(vegetables, getResourceStored(newUnit, vegetables));
				AmountResource legumes = AmountResource.findAmountResource("Legume Group");
				resourceMap.put(legumes, getResourceStored(newUnit, legumes));
				AmountResource spices = AmountResource.findAmountResource("Spice Group");
				resourceMap.put(spices, getResourceStored(newUnit, spices));
				
				resourceCache.put(newUnit, resourceMap);
			}
			catch (Exception e) {}
		}
		super.addUnit(newUnit);
	}

	/**
	 * Remove a unit from the model.
	 * @param oldUnit Unit to remove from the model.
	 */
	protected void removeUnit(Unit oldUnit) {
		if (resourceCache == null) resourceCache = new HashMap<Unit, Map<AmountResource, Integer>>();
		if (resourceCache.containsKey(oldUnit)) {
			Map<AmountResource, Integer> resourceMap = resourceCache.get(oldUnit);
			resourceMap.clear();
			resourceCache.remove(oldUnit);
		}
		super.removeUnit(oldUnit);
	}

	/**
	 * Gets the integer amount of resources stored in a unit.
	 * @param unit the unit to check.
	 * @param resource the resource to check.
	 * @return integer amount of resource.
	 */
	private Integer getResourceStored(Unit unit, AmountResource resource) {
		Integer result = null;	
		Inventory inv = unit.getInventory();
		result = (int) inv.getAmountResourceStored(resource, true);
		return result;
	}

	/**
	 * Prepares the model for deletion.
	 */
	public void destroy() {
		super.destroy();

		UnitManager unitManager = Simulation.instance().getUnitManager();
		unitManager.removeUnitManagerListener(unitManagerListener);
		unitManagerListener = null;

		if (resourceCache != null) {
			resourceCache.clear();
		}
		resourceCache = null;
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
		  * @param event the unit event.
		  */
		 public void unitManagerUpdate(UnitManagerEvent event) {
			 Unit unit = event.getUnit();
			 UnitManagerEventType eventType = event.getEventType();
			 if (unit instanceof Settlement) {
				 if (eventType == UnitManagerEventType.ADD_UNIT) {
					 if (!containsUnit(unit)) addUnit(unit);
				 }
				 else if (eventType == UnitManagerEventType.REMOVE_UNIT) {
					 if (containsUnit(unit)) removeUnit(unit);
				 }
			 }
		 }
	 }
}