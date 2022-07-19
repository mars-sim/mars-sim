/*
 * Mars Simulation Project
 * SettlementTableModel.java
 * @date 2022-07-01
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
	private final static int COMPUTING_UNIT = 4;
	private final static int POWER_GEN = 5;
	private final static int POWER_LOAD = 6;
	private final static int ENERGY_STORED = 7;
	
	private final static int MALFUNCTION = 8;

	private final static int OXYGEN_COL = 9;
	private final static int HYDROGEN_COL = 10;
	private final static int METHANE_COL = 11;
	private final static int METHANOL_COL = 12;
	
	private final static int WATER_COL = 13;
	private final static int ICE_COL = 14;

	private final static int CONCRETE_COL = 15;
	private final static int CEMENT_COL = 16;
	
	private final static int REGOLITHS_COL = 17;
	private final static int ROCKS_COL = 18;
	private final static int ORES_COL = 19;
	private final static int MINERALS_COL = 20;

	/** The number of Columns. */
	private final static int COLUMNCOUNT = 21;
	/** Names of Columns. */
	private final static String columnNames[];
	/** Types of columns. */
	private final static Class<?> columnTypes[];

	static {
		columnNames = new String[COLUMNCOUNT];
		columnTypes = new Class[COLUMNCOUNT];
		columnNames[NAME] = "Name";
		columnTypes[NAME] = String.class;
		columnNames[POPULATION] = "Pop";
		columnTypes[POPULATION] = Integer.class;
	
		columnNames[PARKED] = "Parked Veh";
		columnTypes[PARKED] = Integer.class;
		columnNames[MISSION] = "Mission Veh";
		columnTypes[MISSION] = Integer.class;
		
		columnNames[COMPUTING_UNIT] = "CU(s)";
		columnTypes[COMPUTING_UNIT] = Number.class;

		columnNames[POWER_GEN] = "kW Gen";
		columnTypes[POWER_GEN] = Number.class;
		columnNames[POWER_LOAD] = "kW Load";
		columnTypes[POWER_LOAD] = Number.class;
		columnNames[ENERGY_STORED] = "kWh Stored";
		columnTypes[ENERGY_STORED] = Number.class;
		
		
		columnNames[MALFUNCTION] = "Malfunction";
		columnTypes[MALFUNCTION] = String.class;
		
		columnNames[OXYGEN_COL] = "Oxygen";
		columnTypes[OXYGEN_COL] = Number.class;
		columnNames[HYDROGEN_COL] = "Hydrogen";
		columnTypes[HYDROGEN_COL] = Number.class;	
		columnNames[METHANE_COL] = "Methane";
		columnTypes[METHANE_COL] = Number.class;	
		columnNames[METHANOL_COL] = "Methanol";
		columnTypes[METHANOL_COL] = Number.class;
		
		columnNames[WATER_COL] = "Water";
		columnTypes[WATER_COL] = Number.class;
		columnNames[ICE_COL] = "Ice";
		columnTypes[ICE_COL] = Number.class;
		
		columnNames[CONCRETE_COL] = "Concrete";
		columnTypes[CONCRETE_COL] = Number.class;
		columnNames[CEMENT_COL] = "Cement";
		columnTypes[CEMENT_COL] = Number.class;
		
		columnNames[REGOLITHS_COL] = "Regoliths";
		columnTypes[REGOLITHS_COL] = Number.class;	
		columnNames[ROCKS_COL] = "Rocks";
		columnTypes[ROCKS_COL] = Number.class;	
		columnNames[ORES_COL] = "Ores";
		columnTypes[ORES_COL] = Number.class;	
		columnNames[MINERALS_COL] = "Minerals";
		columnTypes[MINERALS_COL] = Number.class;

	};

	private static UnitManager unitManager = Simulation.instance().getUnitManager();

	private static final DecimalFormat df = new DecimalFormat("#,###,##0");
	private static final DecimalFormat df3 = new DecimalFormat("#,###,##0.000");


	private static final int WATER_ID = ResourceUtil.waterID;
	private static final int ICE_ID = ResourceUtil.iceID;

	private static final int OXYGEN_ID = ResourceUtil.oxygenID;
	private static final int HYDROGEN_ID = ResourceUtil.hydrogenID;
	private static final int METHANE_ID = ResourceUtil.methaneID;
	private static final int METHANOL_ID = ResourceUtil.methanolID;

	private static final int[] REGOLITH_IDS = ResourceUtil.REGOLITH_TYPES;
	private static final int[] ROCK_IDS = ResourceUtil.rockIDs;
	private static final int[] MINERAL_IDS = ResourceUtil.mineralConcIDs;
	private static final int[] ORE_IDS = ResourceUtil.oreDepositIDs;

	private static final int CONCRETE_ID = ResourceUtil.concreteID;
	private static final int CEMENT_ID = ResourceUtil.cementID;
	
	static {
		df3.setMinimumFractionDigits(3);
		df.setMinimumIntegerDigits(1);	
	}

	// Data members
	private UnitManagerListener unitManagerListener;

	private Map<Unit, Map<Integer, Double>> resourceCache;

	/**
	 * Constructs a SettlementTableModel model that displays all Settlements in the
	 * simulation.
	 */
	public SettlementTableModel() throws Exception {
		super("Mars", "SettlementTableModel.countingSettlements",
				columnNames, columnTypes);

//		if (mode == GameMode.COMMAND)
//			addUnit(unitManager.getCommanderSettlement());
//		else
			setSource(unitManager.getSettlements());
			
		unitManagerListener = new LocalUnitManagerListener();
		unitManager.addUnitManagerListener(unitManagerListener);
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
					
				case PARKED: {
					result = settle.getParkedVehicleNum();
				}
					break;

				case MISSION: {
					result = settle.getMissionVehicleNum();
				}
					break;

				case COMPUTING_UNIT: {
					double computing = settle.getBuildingManager().getAllComputingResources();
					result = df3.format(computing);
				}
					break;
					
				case POWER_GEN: {
					double power = settle.getPowerGrid().getGeneratedPower();
					if (power < 0D || Double.isNaN(power) || Double.isInfinite(power))
						result = 0;
					else
						result = df.format(power);
				}
					break;

				case POWER_LOAD: {
					double power = settle.getPowerGrid().getRequiredPower();
					if (power < 0D || Double.isNaN(power) || Double.isInfinite(power))
						result = 0;
					else
						result = df.format(power);
				}
					break;

				case ENERGY_STORED: {
					double energy = settle.getPowerGrid().getStoredEnergy();
					if (energy < 0D || Double.isNaN(energy) || Double.isInfinite(energy))
						result = 0;
					else
						result = df.format(energy);
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

				case OXYGEN_COL: {
					result = df.format(resourceMap.get(OXYGEN_ID));
				}
					break;

				case HYDROGEN_COL: {
					result = df.format(resourceMap.get(HYDROGEN_ID));
				}
					break;
					
				case METHANE_COL: {
					result = df.format(resourceMap.get(METHANE_ID));
				}
					break;

				case METHANOL_COL: {
					result = df.format(resourceMap.get(METHANOL_ID));
				}
					break;
					
					
				case WATER_COL: {
					result = df.format(resourceMap.get(WATER_ID));
				}
					break;
					
				case ICE_COL: {
					result = df.format(resourceMap.get(ICE_ID));
				}
					break;
	
				case CONCRETE_COL: {
					result = df.format(resourceMap.get(CONCRETE_ID));
				}
					break;

				case CEMENT_COL: {
					result = df.format(resourceMap.get(CEMENT_ID));
				}
					break;
														
				case REGOLITHS_COL: {
					result = df.format(getTotalAmount(REGOLITH_IDS, resourceMap));
				}
					break;

				case ROCKS_COL: {
					result = df.format(getTotalAmount(ROCK_IDS, resourceMap));
				}
					break;
					
					
				case ORES_COL: {
					result = df.format(getTotalAmount(ORE_IDS, resourceMap));
				}
					break;
					
				case MINERALS_COL: {
					result = df.format(getTotalAmount(MINERAL_IDS, resourceMap));
				}
					break;
				
				default:
					break;
				}
			} catch (Exception e) {
				logger.severe("getValueAt is invalid: " + e.getMessage());
			}
		}

		return result;
	}

	/**
	 * Gets the sum of the amount of the same types of resources.
	 * 
	 * @param types
	 * @param resourceMap
	 * @return
	 */
	private double getTotalAmount(int [] types, Map<Integer, Double> resourceMap) {
		double result = 0;
		for (int i = 0; i < types.length; i++) {
			int id = types[i];
			result += resourceMap.get(id);
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
		else if (eventType == UnitEventType.CONSUMING_COMPUTING_EVENT) columnNum = COMPUTING_UNIT;
		else if (eventType == UnitEventType.GENERATED_POWER_EVENT) columnNum = POWER_GEN;
		else if (eventType == UnitEventType.REQUIRED_POWER_EVENT) columnNum = POWER_LOAD;
		else if (eventType == UnitEventType.STORED_POWER_EVENT) columnNum = ENERGY_STORED;		
		else if (eventType == UnitEventType.MALFUNCTION_EVENT) columnNum = MALFUNCTION;
		else if (eventType == UnitEventType.INVENTORY_RESOURCE_EVENT) {
			int target = -1;
			if (source instanceof AmountResource) {
				target = ((AmountResource)source).getID();
			}
			else if (source instanceof Integer) {
				// Note: most likely, the source is an integer id
				target = (Integer)source;
				if (target >= ResourceUtil.FIRST_ITEM_RESOURCE_ID)
					// if it's an item resource, quit
					return;
			}
			else
				return;
			
			try {
				int tempColumnNum = -1;
				double currentValue = 0.0;
				Map<Integer, Double> resourceMap = resourceCache.get(unit);

				if (target == OXYGEN_ID) {
					tempColumnNum = OXYGEN_COL;
					currentValue = resourceMap.get(OXYGEN_ID);
				}
				else if (target == HYDROGEN_ID) {
					tempColumnNum = HYDROGEN_COL;
					currentValue = resourceMap.get(HYDROGEN_ID);
				}
				else if (target == METHANOL_ID) {
					tempColumnNum = METHANOL_COL;
					currentValue = resourceMap.get(METHANOL_ID);
				}
				else if (target == METHANE_ID) {
					tempColumnNum = METHANE_COL;
					currentValue = resourceMap.get(METHANE_ID);
				}
				else if (target == WATER_ID) {
					tempColumnNum = WATER_COL;
					currentValue = resourceMap.get(WATER_ID);
				}
				else if (target == ICE_ID) {
					tempColumnNum = ICE_COL;
					currentValue = resourceMap.get(ICE_ID);
				}
				else if (target == CONCRETE_ID) {
					tempColumnNum = CONCRETE_COL;
					currentValue = resourceMap.get(CONCRETE_ID);
				}
				else if (target == CEMENT_ID) {
					tempColumnNum = CEMENT_COL;
					currentValue = resourceMap.get(CEMENT_ID);
				}
				else {
					boolean found = false;
					for (int i = 0; i < REGOLITH_IDS.length; i++) {
						if (!found && target == REGOLITH_IDS[i]) {
							tempColumnNum = REGOLITHS_COL;
							currentValue = resourceMap.get(target);
							found = true;
						}
					}
					if (!found) {
						for (int i = 0; i < ORE_IDS.length; i++) {
							if (!found && target == ORE_IDS[i]) {
								tempColumnNum = ORES_COL;
								currentValue = resourceMap.get(target);
								found = true;
							}
						}
					}
					if (!found) {
						for (int i = 0; i < MINERAL_IDS.length; i++) {
							if (!found && target == MINERAL_IDS[i]) {
								tempColumnNum = MINERALS_COL;
								currentValue = resourceMap.get(target);
								found = true;
							}
						}
					}
					if (!found) {
						for (int i = 0; i < ROCK_IDS.length; i++) {
							if (!found && target == ROCK_IDS[i]) {
								tempColumnNum = ROCKS_COL;
								currentValue = resourceMap.get(target);
								found = true;
							}
						}
					}
				}

				if (tempColumnNum > -1) {
					double newValue = getResourceStored((Settlement)unit, target);
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
	 * Defines the source data from this table.
	 * 
	 * @param source
	 */
	private void setSource(Collection<Settlement> source) {
		Iterator<Settlement> iter = source.iterator();
		while (iter.hasNext())
			addUnit(iter.next());
	}

	@Override
	protected void addUnit(Unit newUnit) {
		if (resourceCache == null)
			resourceCache = new HashMap<>();
		if (!resourceCache.containsKey(newUnit)) {
			try {
				Map<Integer, Double> resourceMap = new HashMap<>();
				Settlement settlement = (Settlement)newUnit;
				resourceMap.put(OXYGEN_ID, getResourceStored(settlement, OXYGEN_ID));
				resourceMap.put(HYDROGEN_ID, getResourceStored(settlement, HYDROGEN_ID));
				resourceMap.put(METHANE_ID, getResourceStored(settlement, METHANE_ID));
				resourceMap.put(METHANOL_ID, getResourceStored(settlement, METHANOL_ID));
				
				resourceMap.put(WATER_ID, getResourceStored(settlement, WATER_ID));
				resourceMap.put(ICE_ID, getResourceStored(settlement, ICE_ID));
				
				resourceMap.put(CONCRETE_ID, getResourceStored(settlement, CONCRETE_ID));
				resourceMap.put(CEMENT_ID, getResourceStored(settlement, CEMENT_ID));

				for (int i = 0; i < REGOLITH_IDS.length; i++) {
					resourceMap.put(REGOLITH_IDS[i], getResourceStored(settlement, REGOLITH_IDS[i]));
				}		
				for (int i = 0; i < ORE_IDS.length; i++) {
					resourceMap.put(ORE_IDS[i], getResourceStored(settlement, ORE_IDS[i]));
				}			
				for (int i = 0; i < MINERAL_IDS.length; i++) {
					resourceMap.put(MINERAL_IDS[i], getResourceStored(settlement, MINERAL_IDS[i]));
				}
				for (int i = 0; i < ROCK_IDS.length; i++) {
					resourceMap.put(ROCK_IDS[i], getResourceStored(settlement, ROCK_IDS[i]));
				}
	
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
		return Math.round(settlement.getAllAmountResourceOwned(resource) * 100.0) / 100.0;
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
