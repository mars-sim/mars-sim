/**
 * Mars Simulation Project
 * SettlementTableModel.java
 * @version 3.1.0 2017-04-10
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
	private final static int REGOLITH = 13;
	private final static int ICE = 14;
	/** The number of Columns. */
	private final static int COLUMNCOUNT = 15;
	/** Names of Columns. */
	private final static String columnNames[];
	/** Types of columns. */
	private final static Class<?> columnTypes[];

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
		columnNames[GREY_WATER] = "Grey Water";
		columnTypes[GREY_WATER] = Number.class;
		columnNames[BLACK_WATER] = "Black Water";
		columnTypes[BLACK_WATER] = Number.class;
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

	private final static DecimalFormat df = new DecimalFormat("#,###,##0.00");

	private final static int regolithID = ResourceUtil.regolithID;
	private final static int oxygenID = ResourceUtil.oxygenID;
	private final static int waterID = ResourceUtil.waterID;
	private final static int methaneID = ResourceUtil.methaneID;
	private final static int rockSamplesID = ResourceUtil.rockSamplesID;
	private final static int iceID = ResourceUtil.iceID;
	
	private final static int greyWaterID = ResourceUtil.greyWaterID;
	private final static int blackWaterID = ResourceUtil.blackWaterID;
	private final static int co2ID = ResourceUtil.co2ID;
	private final static int hydrogenID = ResourceUtil.hydrogenID;
	
	static {
		df.setMinimumFractionDigits(2);
		df.setMinimumIntegerDigits(1);
	}
	
	/**
	 * Constructs a SettlementTableModel model that displays all Settlements in the
	 * simulation.
	 */
	public SettlementTableModel() {
		super(Msg.getString("SettlementTableModel.tabName"), "SettlementTableModel.countingSettlements", //$NON-NLS-2$
				columnNames, columnTypes);

		if (GameManager.mode == GameMode.COMMAND)
			addUnit(unitManager.getCommanderSettlement());
		else
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
	public SettlementTableModel(Settlement settlement) {
		super(Msg.getString("SettlementTableModel.tabName"), "SettlementTableModel.countingSettlements", //$NON-NLS-2$
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
			// BuildingManager bMgr = settle.getBuildingManager();
			Map<Integer, Double> resourceMap = resourceCache.get(settle);

			try {
				switch (columnIndex) {
				case NAME: {
					result = settle.getName();
				}
					break;

				case WATER: {
					result = df.format(resourceMap.get(waterID));
				}
					break;

				case OXYGEN: {
					result = df.format(resourceMap.get(oxygenID));
				}
					break;

				case METHANE: {
					result = df.format(resourceMap.get(methaneID));
				}
					break;

				case HYDROGEN: {
					result = df.format(resourceMap.get(hydrogenID));
				}
					break;

				case CO2: {
					result = df.format(resourceMap.get(co2ID));
				}
					break;

				case ROCK_SAMPLES: {
					result = df.format(resourceMap.get(rockSamplesID));
				}
					break;

				case REGOLITH: {
					result = df.format(resourceMap.get(regolithID));
				}
					break;

				case PARKED: {
					result = settle.getParkedVehicleNum();
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

				case GREY_WATER: {
					result = df.format(resourceMap.get(greyWaterID));
				}
					break;

				case BLACK_WATER: {
					result = df.format(resourceMap.get(blackWaterID));
				}
					break;

				case ICE: {
					result = df.format(resourceMap.get(iceID));
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
				
				if (target == oxygenID) {
					tempColumnNum = OXYGEN;
					currentValue = resourceMap.get(oxygenID);
				}
				else if (target == hydrogenID) {
					tempColumnNum = HYDROGEN;
					currentValue = resourceMap.get(hydrogenID);
				}	
				else if (target == co2ID) {
					tempColumnNum = CO2;
					currentValue = resourceMap.get(co2ID);
				}
				else if (target == methaneID) {
					tempColumnNum = METHANE;		
					currentValue = resourceMap.get(methaneID);
				}
				else if (target == waterID) {
					tempColumnNum = WATER;
					currentValue = resourceMap.get(waterID);
				}
				else if (target == greyWaterID) {
					tempColumnNum = GREY_WATER;
					currentValue = resourceMap.get(greyWaterID);
				}
				else if (target == blackWaterID) {
					tempColumnNum = BLACK_WATER;
					currentValue = resourceMap.get(blackWaterID);
				}
				else if (target == rockSamplesID) {
					tempColumnNum = ROCK_SAMPLES;
					currentValue = resourceMap.get(rockSamplesID);
				}
				else if (target == regolithID) {
					tempColumnNum = REGOLITH;
					currentValue = resourceMap.get(regolithID);
				}
				else if (target == iceID) {
					tempColumnNum = ICE;
					currentValue = resourceMap.get(iceID);
				}
				
				if (tempColumnNum > -1) {
					currentValue = Math.round (currentValue * 10.0 ) / 10.0;
					double newValue = Math.round (getResourceStored(unit, target) * 10.0 ) / 10.0;
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

				resourceMap.put(oxygenID, getResourceStored(newUnit, oxygenID));
				resourceMap.put(waterID, getResourceStored(newUnit, waterID));
				resourceMap.put(hydrogenID, getResourceStored(newUnit, hydrogenID));
				resourceMap.put(methaneID, getResourceStored(newUnit, methaneID));
				resourceMap.put(rockSamplesID, getResourceStored(newUnit, rockSamplesID));
				resourceMap.put(regolithID, getResourceStored(newUnit, regolithID));
				resourceMap.put(greyWaterID, getResourceStored(newUnit, greyWaterID));
				resourceMap.put(blackWaterID, getResourceStored(newUnit, blackWaterID));
				resourceMap.put(iceID, getResourceStored(newUnit, iceID));
				resourceMap.put(co2ID, getResourceStored(newUnit, co2ID));

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
	private double getResourceStored(Unit unit, int resource) {
		// This is the quickest way but it may or may not work if the object reference
		// of ARs have changed during (de)serialization.
		return Math.round(unit.getInventory().getAmountResourceStored(resource, false) * 100.0) / 100.0;
	}

	/**
	 * Prepares the model for deletion.
	 */
	public void destroy() {
		super.destroy();

		UnitManager unitManager = Simulation.instance().getUnitManager();
		unitManager.removeUnitManagerListener(unitManagerListener);
		unitManagerListener = null;

		// if (resourceCache != null) {
		// resourceCache.clear();
		// }
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
			if (unit instanceof Settlement) {
				if (eventType == UnitManagerEventType.ADD_UNIT) {
					if (!containsUnit(unit))
						addUnit(unit);
				} else if (eventType == UnitManagerEventType.REMOVE_UNIT) {
					if (containsUnit(unit))
						removeUnit(unit);
				}
			}
		}
	}
}