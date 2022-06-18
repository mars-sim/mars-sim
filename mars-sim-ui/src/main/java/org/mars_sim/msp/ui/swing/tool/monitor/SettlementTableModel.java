/*
 * Mars Simulation Project
 * SettlementTableModel.java
 * @date 2021-12-24
 * @author Barry Evans
 */
package org.mars_sim.msp.ui.swing.tool.monitor;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitEvent;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.UnitManagerEvent;
import org.mars_sim.msp.core.UnitManagerEventType;
import org.mars_sim.msp.core.UnitManagerListener;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.malfunction.Malfunction;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * The SettlementTableModel that maintains a list of Settlement objects. It maps
 * key attributes of the Settlement into Columns.
 */
@SuppressWarnings("serial")
public class SettlementTableModel extends UnitTableModel {

	private final static Logger logger = Logger.getLogger(SettlementTableModel.class.getName());

	// Column indexes
	private final static int NAME = 0;
	private final static int POPULATION = 1;
	private final static int PARKED = 2;
	private final static int MISSION = 3;
	private final static int POWER = 4;

	private final static int MALFUNCTION = 5;

	private final static int OXYGEN = 6;
	private final static int HYDROGEN = 7;
	private final static int CO2 = 8;
	private final static int METHANE = 9;

	private final static int WATER = 10;
	private final static int ICE = 11;

	private final static int ROCK_SAMPLES = 12;
	private final static int REGOLITH = 13;
	private final static int CONCRETE = 14;
	private final static int CEMENT = 15;

	private int type = -1;
	
	/** The number of Columns. */
	private final static int COLUMNCOUNT = 16;
	/** Names of Columns. */
	private final static String columnNames[];
	/** Types of columns. */
	private final static Class<?> columnTypes[];

	static {
		columnNames = new String[COLUMNCOUNT];
		columnTypes = new Class[COLUMNCOUNT];
		columnNames[NAME] = "Name";
		columnTypes[NAME] = String.class;
		columnNames[POPULATION] = "Population";
		columnTypes[POPULATION] = Integer.class;
		columnNames[PARKED] = "Parked Vehicles";
		columnTypes[PARKED] = Integer.class;
		columnNames[MISSION] = "Mission Vehicles";
		columnTypes[MISSION] = Integer.class;
		columnNames[POWER] = "Power (kW)";
		columnTypes[POWER] = Number.class;
		columnNames[MALFUNCTION] = "Malfunction";
		columnTypes[MALFUNCTION] = String.class;
		columnNames[OXYGEN] = "Oxygen";
		columnTypes[OXYGEN] = Number.class;
		columnNames[HYDROGEN] = "Hydrogen";
		columnTypes[HYDROGEN] = Number.class;
		columnNames[CO2] = "CO2";
		columnTypes[CO2] = Number.class;
		columnNames[WATER] = "Water";
		columnTypes[WATER] = Number.class;
		columnNames[METHANE] = "Methane";
		columnTypes[METHANE] = Number.class;
		columnNames[CONCRETE] = "Concrete";
		columnTypes[CONCRETE] = Number.class;
		columnNames[CEMENT] = "Cement";
		columnTypes[CEMENT] = Number.class;
		columnNames[ROCK_SAMPLES] = "Rock Samples";
		columnTypes[ROCK_SAMPLES] = Number.class;
		columnNames[REGOLITH] = "Regolith";
		columnTypes[REGOLITH] = Number.class;
		columnNames[ICE] = "Ice";
		columnTypes[ICE] = Number.class;
	};

	private static UnitManager unitManager = Simulation.instance().getUnitManager();

	// Data members
	private UnitManagerListener unitManagerListener;

	private Map<Unit, Map<Integer, Double>> resourceCache;

	private static final DecimalFormat df = new DecimalFormat("#,###,##0.00");

	private static final int WATER_ID = ResourceUtil.waterID;
	private static final int ICE_ID = ResourceUtil.iceID;

	private static final int OXYGEN_ID = ResourceUtil.oxygenID;
	private static final int CO2_ID = ResourceUtil.co2ID;
	private static final int HYDROGEN_ID = ResourceUtil.hydrogenID;
	private static final int METHANE_ID = ResourceUtil.methaneID;

	private static final int REGOLITH_ID = ResourceUtil.regolithID;
	private static final int CONCRETE_ID = ResourceUtil.concreteID;
	private static final int CEMENT_ID = ResourceUtil.cementID;
	private static final int ROCK_SAMPLES_ID = ResourceUtil.rockSamplesID;

	static {
		df.setMinimumFractionDigits(2);
		df.setMinimumIntegerDigits(1);
	}

	/**
	 * Constructs a SettlementTableModel model that displays all Settlements in the
	 * simulation.
	 */
	public SettlementTableModel() throws Exception {
		super("Mars", "SettlementTableModel.countingSettlements",
				columnNames, columnTypes);

//		if (GameManager.mode == GameMode.COMMAND)
//			addUnit(unitManager.getCommanderSettlement());
//		else
			setSource(unitManager.getSettlements());

		unitManagerListener = new LocalUnitManagerListener();
		unitManager.addUnitManagerListener(unitManagerListener);

		type = 0;
	}

	/**
	 * Constructs a SettlementTableModel model that displays a specific settlement in the
	 * simulation.
	 *
	 * @param settlement
	 */
	public SettlementTableModel(Settlement settlement) throws Exception {
		super(Msg.getString("SettlementTableModel.tabName"), //$NON-NLS-2$
				"SettlementTableModel.countingSettlements", 
				columnNames, columnTypes);

		addUnit(settlement);

		unitManagerListener = new LocalUnitManagerListener();
		unitManager.addUnitManagerListener(unitManagerListener);
		
		type = 1;
	}

	/**
	 * Return the value of a Cell
	 *
	 * @param rowIndex    Row index of the cell.
	 * @param columnIndex Column index of the cell.
	 */
	public Object getValueAt(int rowIndex, int columnIndex) {
		Object result = null;

		if (rowIndex < getUnitNumber()) {
			Settlement settle = (Settlement) getUnit(rowIndex);
			Map<Integer, Double> resourceMap = resourceCache.get(settle);

			try {
				switch (columnIndex) {
				case NAME: {
					result = settle.getName();
				}
					break;

				case WATER: {
					result = df.format(resourceMap.get(WATER_ID));
				}
					break;

				case OXYGEN: {
					result = df.format(resourceMap.get(OXYGEN_ID));
				}
					break;

				case METHANE: {
					result = df.format(resourceMap.get(METHANE_ID));
				}
					break;

				case HYDROGEN: {
					result = df.format(resourceMap.get(HYDROGEN_ID));
				}
					break;

				case CO2: {
					result = df.format(resourceMap.get(CO2_ID));
				}
					break;

				case ROCK_SAMPLES: {
					result = df.format(resourceMap.get(ROCK_SAMPLES_ID));
				}
					break;

				case REGOLITH: {
					result = df.format(resourceMap.get(REGOLITH_ID));
				}
					break;

				case PARKED: {
					result = settle.getParkedVehicleNum();
				}
					break;

				case MISSION: {
					result = settle.getMissionVehicleNum();
				}
					break;

				case POWER: {
					double power = settle.getPowerGrid().getGeneratedPower();
					if (power < 0D || Double.isNaN(power) || Double.isInfinite(power))
						result = 0;
					else
						result = df.format(power);
				}
					break;

//				case GREY_WATER: {
//					result = df.format(resourceMap.get(greyWaterID));
//				}
//					break;
//
//				case BLACK_WATER: {
//					result = df.format(resourceMap.get(blackWaterID));
//				}
//					break;

				case CONCRETE: {
					result = df.format(resourceMap.get(CONCRETE_ID));
				}
					break;

				case CEMENT: {
					result = df.format(resourceMap.get(CEMENT_ID));
				}
					break;

				case ICE: {
					result = df.format(resourceMap.get(ICE_ID));
				}
					break;

				case POPULATION: {
					result = settle.getNumCitizens();
				}
					break;

				case MALFUNCTION: {
					int severity = 0;
					Malfunction malfunction = null;
					Iterator<Building> i = settle.getBuildingManager().getBuildings().iterator();
					while (i.hasNext()) {
						Building building = i.next();
						Malfunction tempMalfunction = building.getMalfunctionManager().getMostSeriousMalfunction();
						if ((tempMalfunction != null) && (tempMalfunction.getSeverity() > severity)) {
							malfunction = tempMalfunction;
							severity = tempMalfunction.getSeverity();
						}
					}
					if (malfunction != null)
						result = malfunction.getName();
					else
						result = "";
				}
					break;

				}
			} catch (Exception e) {
			}
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
		Object source = event.getTarget();
		UnitEventType eventType = event.getType();

		int columnNum = -1;
		if (eventType == UnitEventType.NAME_EVENT) columnNum = NAME;
		else if (eventType == UnitEventType.INVENTORY_STORING_UNIT_EVENT ||
				eventType == UnitEventType.INVENTORY_RETRIEVING_UNIT_EVENT) {
			if (source instanceof Person) columnNum = POPULATION;
			else if (source instanceof Vehicle) columnNum = PARKED;
		}
		else if (eventType == UnitEventType.GENERATED_POWER_EVENT) columnNum = POWER;
		else if (eventType == UnitEventType.MALFUNCTION_EVENT) columnNum = MALFUNCTION;
		else if (eventType == UnitEventType.INVENTORY_RESOURCE_EVENT) {
			int target = -1;
			if (source instanceof AmountResource) {
				target = ((AmountResource)source).getID();
			}

			else if (source instanceof Integer) {
				target = (Integer)source;
				if (target >= ResourceUtil.FIRST_ITEM_RESOURCE_ID)
					// if it's an item resource, quit
					return;
			}

			try {
				int tempColumnNum = -1;
				double currentValue = 0.0;
				Map<Integer, Double> resourceMap = resourceCache.get(unit);

				if (target == OXYGEN_ID) {
					tempColumnNum = OXYGEN;
					currentValue = resourceMap.get(OXYGEN_ID);
				}
				else if (target == HYDROGEN_ID) {
					tempColumnNum = HYDROGEN;
					currentValue = resourceMap.get(HYDROGEN_ID);
				}
				else if (target == CO2_ID) {
					tempColumnNum = CO2;
					currentValue = resourceMap.get(CO2_ID);
				}
				else if (target == METHANE_ID) {
					tempColumnNum = METHANE;
					currentValue = resourceMap.get(METHANE_ID);
				}
				else if (target == WATER_ID) {
					tempColumnNum = WATER;
					currentValue = resourceMap.get(WATER_ID);
				}
				else if (target == CONCRETE_ID) {
					tempColumnNum = CONCRETE;
					currentValue = resourceMap.get(CONCRETE_ID);
				}
				else if (target == CEMENT_ID) {
					tempColumnNum = CEMENT;
					currentValue = resourceMap.get(CEMENT_ID);
				}
				else if (target == ROCK_SAMPLES_ID) {
					tempColumnNum = ROCK_SAMPLES;
					currentValue = resourceMap.get(ROCK_SAMPLES_ID);
				}
				else if (target == REGOLITH_ID) {
					tempColumnNum = REGOLITH;
					currentValue = resourceMap.get(REGOLITH_ID);
				}
				else if (target == ICE_ID) {
					tempColumnNum = ICE;
					currentValue = resourceMap.get(ICE_ID);
				}

				if (tempColumnNum > -1) {
					currentValue = Math.round (currentValue * 10.0 ) / 10.0;
					double newValue = Math.round (getResourceStored((Settlement)unit, target) * 10.0 ) / 10.0;
					if (currentValue != newValue) {
						columnNum = tempColumnNum;
						resourceMap.put(target, newValue);
					}
				}
			}
			catch (Exception e) {
				logger.log(Level.SEVERE, "Issues with unitUpdate()", e);
			}
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
		while (iter.hasNext())
			addUnit(iter.next());
	}

	@Override
	protected void addUnit(Unit newUnit) {
		if (resourceCache == null)
			resourceCache = new HashMap<Unit, Map<Integer, Double>>();
		if (!resourceCache.containsKey(newUnit)) {
			try {
				Map<Integer, Double> resourceMap = new HashMap<>();
				Settlement settlement = (Settlement)newUnit;
				resourceMap.put(OXYGEN_ID, getResourceStored(settlement, OXYGEN_ID));
				resourceMap.put(WATER_ID, getResourceStored(settlement, WATER_ID));
				resourceMap.put(HYDROGEN_ID, getResourceStored(settlement, HYDROGEN_ID));
				resourceMap.put(METHANE_ID, getResourceStored(settlement, METHANE_ID));
				resourceMap.put(ROCK_SAMPLES_ID, getResourceStored(settlement, ROCK_SAMPLES_ID));
				resourceMap.put(REGOLITH_ID, getResourceStored(settlement, REGOLITH_ID));
//				resourceMap.put(greyWaterID, getResourceStored(settlement, greyWaterID));
//				resourceMap.put(blackWaterID, getResourceStored(settlement, blackWaterID));
				resourceMap.put(CONCRETE_ID, getResourceStored(settlement, CONCRETE_ID));
				resourceMap.put(CEMENT_ID, getResourceStored(settlement, CEMENT_ID));
				resourceMap.put(ICE_ID, getResourceStored(settlement, ICE_ID));
				resourceMap.put(CO2_ID, getResourceStored(settlement, CO2_ID));

				resourceCache.put(newUnit, resourceMap);
			} catch (Exception e) {
			}
		}
		super.addUnit(newUnit);
	}

	@Override
	protected void removeUnit(Unit oldUnit) {
		if (resourceCache == null)
			resourceCache = new HashMap<>();
		if (resourceCache.containsKey(oldUnit)) {
			Map<Integer, Double> resourceMap = resourceCache.get(oldUnit);
			resourceMap.clear();
			resourceCache.remove(oldUnit);
		}
		super.removeUnit(oldUnit);
	}

	/**
	 * Gets the amount of resources stored in a unit.
	 *
	 * @param unit     the unit to check.
	 * @param resource the resource to check.
	 * @return integer amount of resource.
	 */
	private double getResourceStored(Settlement settlement, int resource) {
		// This is the quickest way but it may or may not work if the object reference
		// of ARs have changed during (de)serialization.
		return Math.round(settlement.getAmountResourceStored(resource) * 100.0) / 100.0;
	}

	public int getType() {
		return type;
	}
	
	/**
	 * Prepares the model for deletion.
	 */
	public void destroy() {
		super.destroy();
		unitManager.removeUnitManagerListener(unitManagerListener);
		unitManagerListener = null;
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
		 *
		 * @param event the unit event.
		 */
		public void unitManagerUpdate(UnitManagerEvent event) {
			Unit unit = event.getUnit();
			UnitManagerEventType eventType = event.getEventType();
			if (unit.getUnitType() == UnitType.SETTLEMENT) {
				if (eventType == UnitManagerEventType.ADD_UNIT
					&& !containsUnit(unit)) {
						addUnit(unit);
				} else if (eventType == UnitManagerEventType.REMOVE_UNIT
					&& containsUnit(unit))
						removeUnit(unit);
			}
		}
	}
}
