/**
 * Mars Simulation Project
 * SettlementTableModel.java
 * @version 3.07 2015-02-27
 * @author Barry Evans
 */
package org.mars_sim.msp.ui.swing.tool.monitor;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.SwingUtilities;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LifeSupportType;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitEvent;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.UnitManagerEvent;
import org.mars_sim.msp.core.UnitManagerEventType;
import org.mars_sim.msp.core.UnitManagerListener;
import org.mars_sim.msp.core.malfunction.Malfunction;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * The SettlementTableModel that maintains a list of Settlement objects.
 * It maps key attributes of the Settlement into Columns.
 */
// 2014-10-14 mkung: moved all food related columns to FoodTableModel.java
public class SettlementTableModel
extends UnitTableModel {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	//private DecimalFormat decFormatter = new DecimalFormat("#,###,###.#");

	// Column indexes
	private final static int NAME = 0;
	private final static int POPULATION = 1;
	private final static int PARKED = 2;
	private final static int POWER = 3;

	private final static int MALFUNCTION = 4;
	private final static int OXYGEN = 5;
	private final static int HYDROGEN = 6;
	private final static int CO2 = 7;
	private final static int METHANE = 8;

	private final static int WATER = 9;
	private final static int GREY_WATER = 10;
	private final static int BLACK_WATER = 11;
	private final static int ROCK_SAMPLES = 12;
	private final static int ICE = 13;
	/** The number of Columns. */
	private final static int COLUMNCOUNT = 14;
	/** Names of Columns. */
	private static String columnNames[];
	/** Types of columns. */
	private static Class<?> columnTypes[];

	static {
		columnNames = new String[COLUMNCOUNT];
		columnTypes = new Class[COLUMNCOUNT];
		columnNames[NAME] = "Name";
		columnTypes[NAME] = String.class;
		columnNames[POPULATION] = "Total Population";
		columnTypes[POPULATION] = Integer.class;
		columnNames[PARKED] = "Parked Vehicles";
		columnTypes[PARKED] = Integer.class;
		columnNames[POWER] = "Power (kW)";
		columnTypes[POWER] = Integer.class;
		columnNames[MALFUNCTION] = "Malfunction";
		columnTypes[MALFUNCTION] = String.class;
		columnNames[OXYGEN] = "Oxygen";
		columnTypes[OXYGEN] = Integer.class;
		columnNames[WATER] = "Water";
		columnTypes[WATER] = Integer.class;
		columnNames[METHANE] = "Methane";
		columnTypes[METHANE] = Integer.class;
		columnNames[ROCK_SAMPLES] = "Rock Samples";
		columnTypes[ROCK_SAMPLES] = Integer.class;
		columnNames[HYDROGEN] = "Hydrogen";
		columnTypes[HYDROGEN] = Integer.class;
		columnNames[GREY_WATER] = "Grey Water";
		columnTypes[GREY_WATER] = Integer.class;
		columnNames[BLACK_WATER] = "Black Water";
		columnTypes[BLACK_WATER] = Integer.class;
		columnNames[CO2] = "CO2";
		columnTypes[CO2] = Integer.class;
		columnNames[ICE] = "Ice";
		columnTypes[ICE] = Integer.class;
	};

	// Data members
	private UnitManagerListener unitManagerListener;
	private Map<Unit, Map<AmountResource, Integer>> resourceCache;

	/**
	 * Constructs a SettlementTableModel model that displays all Settlements
	 * in the simulation.
	 *
	 * @param unitManager Unit manager that holds settlements.
	 */
	public SettlementTableModel(UnitManager unitManager) {
		super(
			Msg.getString("SettlementTableModel.tabName"),
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

				case WATER : {
					result = resourceMap.get(
							AmountResource.findAmountResource(LifeSupportType.WATER));
				} break;


				case OXYGEN : {
					result = resourceMap.get(
							AmountResource.findAmountResource(LifeSupportType.OXYGEN));
				} break;

				case METHANE : {
					result = resourceMap.get(
							AmountResource.findAmountResource("methane"));
				} break;

				case ROCK_SAMPLES : {
					result = resourceMap.get(
						AmountResource.findAmountResource("rock samples"));
				} break;

				case MALFUNCTION: {
					int severity = 0;
					Malfunction malfunction = null;
					Iterator<Building> i = settle.getBuildingManager().getACopyOfBuildings().iterator();
					while (i.hasNext()) {
						Building building = i.next();
						Malfunction tempMalfunction = building.getMalfunctionManager().getMostSeriousMalfunction();
						if ((tempMalfunction != null) && (tempMalfunction.getSeverity() > severity)) {
							malfunction = tempMalfunction;
							severity = tempMalfunction.getSeverity();
						}
					}
					if (malfunction != null) result = malfunction.getName();
				} break;

				case POPULATION : {
					result = settle.getAllAssociatedPeople().size();
				} break;

				case PARKED : {
					result = settle.getParkedVehicleNum();
				} break;

				case POWER : {
					result = (int) settle.getPowerGrid().getGeneratedPower();
				} break;

				case HYDROGEN : {
					result = resourceMap.get(
							AmountResource.findAmountResource("hydrogen"));
				} break;

				case GREY_WATER : {
					result = resourceMap.get(
							AmountResource.findAmountResource("grey water"));
				} break;

				case BLACK_WATER : {
					result = resourceMap.get(
							AmountResource.findAmountResource("black water"));
				} break;

				case CO2 : {
					result = resourceMap.get(
							AmountResource.findAmountResource("carbon dioxide"));
				} break;

				case ICE : {
					result = resourceMap.get(
							AmountResource.findAmountResource("ice"));
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
		else if (eventType == UnitEventType.INVENTORY_STORING_UNIT_EVENT ||
				eventType == UnitEventType.INVENTORY_RETRIEVING_UNIT_EVENT) {
			if (target instanceof Person) columnNum = POPULATION;
			else if (target instanceof Vehicle) columnNum = PARKED;
		}
		else if (eventType == UnitEventType.GENERATED_POWER_EVENT) columnNum = POWER;
		else if (eventType == UnitEventType.MALFUNCTION_EVENT) columnNum = MALFUNCTION;
		else if (eventType == UnitEventType.INVENTORY_RESOURCE_EVENT) {
			try {
				int tempColumnNum = -1;

				if (target.equals(AmountResource.findAmountResource(LifeSupportType.OXYGEN)))
					tempColumnNum = OXYGEN;
				else if (target.equals(AmountResource.findAmountResource("hydrogen")))
					tempColumnNum = HYDROGEN;
				else if (target.equals(AmountResource.findAmountResource("carbon dioxide")))
					tempColumnNum = CO2;
				else if (target.equals(AmountResource.findAmountResource("methane")))
					tempColumnNum = METHANE;
				else if (target.equals(AmountResource.findAmountResource(LifeSupportType.WATER)))
					tempColumnNum = WATER;
				else if (target.equals(AmountResource.findAmountResource("grey water")))
					tempColumnNum = GREY_WATER;
				else if (target.equals(AmountResource.findAmountResource("black water")))
					tempColumnNum = BLACK_WATER;
				else if (target.equals(AmountResource.findAmountResource("rock samples")))
					tempColumnNum = ROCK_SAMPLES;
				else if (target.equals(AmountResource.findAmountResource("ice")))
					tempColumnNum = ICE;

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
			SwingUtilities.invokeLater(new SettlementTableCellUpdater(unitIndex, columnNum));
		}
	}

	/**
	 * Defines the source data from this table
	 */
	private void setSource(Collection<Settlement> source) {
		Iterator<Settlement> iter = source.iterator();
		while(iter.hasNext()) addUnit(iter.next());
	}

	@Override
	protected void addUnit(Unit newUnit) {
		if (resourceCache == null) resourceCache = new HashMap<Unit, Map<AmountResource, Integer>>();
		if (!resourceCache.containsKey(newUnit)) {
			try {
				Map<AmountResource, Integer> resourceMap = new HashMap<AmountResource, Integer>(9);
				AmountResource oxygen = AmountResource.findAmountResource(LifeSupportType.OXYGEN);
				resourceMap.put(oxygen, getResourceStored(newUnit, oxygen));
				AmountResource water = AmountResource.findAmountResource(LifeSupportType.WATER);
				resourceMap.put(water, getResourceStored(newUnit, water));
				AmountResource hydrogen = AmountResource.findAmountResource("hydrogen");
				resourceMap.put(hydrogen, getResourceStored(newUnit, hydrogen));
				AmountResource methane = AmountResource.findAmountResource("methane");
				resourceMap.put(methane, getResourceStored(newUnit, methane));
				AmountResource rockSamples = AmountResource.findAmountResource("rock samples");
				resourceMap.put(rockSamples, getResourceStored(newUnit, rockSamples));
				AmountResource greyWater = AmountResource.findAmountResource("grey water");
				resourceMap.put(greyWater, getResourceStored(newUnit, greyWater));
				AmountResource blackWater = AmountResource.findAmountResource("black water");
				resourceMap.put(blackWater, getResourceStored(newUnit, blackWater));
				AmountResource ice = AmountResource.findAmountResource("ice");
				resourceMap.put(ice, getResourceStored(newUnit, ice));
				AmountResource carbonDioxide = AmountResource.findAmountResource("carbon dioxide");
				resourceMap.put(carbonDioxide, getResourceStored(newUnit, carbonDioxide));
				resourceCache.put(newUnit, resourceMap);
			}
			catch (Exception e) {}
		}
		super.addUnit(newUnit);
	}

	@Override
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

	private class SettlementTableCellUpdater implements Runnable {

		private int row;
		private int column;

		private SettlementTableCellUpdater(int row, int column) {
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