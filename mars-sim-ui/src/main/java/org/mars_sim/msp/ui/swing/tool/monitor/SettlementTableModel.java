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
public class SettlementTableModel extends UnitTableModel {

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
		columnTypes[POWER] = String.class;
		columnNames[MALFUNCTION] = "Malfunction";
		columnTypes[MALFUNCTION] = String.class;
		columnNames[OXYGEN] = "Oxygen";
		columnTypes[OXYGEN] = String.class;
		columnNames[HYDROGEN] = "Hydrogen";
		columnTypes[HYDROGEN] = String.class;
		columnNames[CO2] = "CO2";
		columnTypes[CO2] = String.class;
		columnNames[WATER] = "Water";
		columnTypes[WATER] = String.class;
		columnNames[METHANE] = "Methane";
		columnTypes[METHANE] = String.class;
		columnNames[GREY_WATER] = "Grey Water";
		columnTypes[GREY_WATER] = String.class;
		columnNames[BLACK_WATER] = "Black Water";
		columnTypes[BLACK_WATER] = String.class;
		columnNames[ROCK_SAMPLES] = "Rock Samples";
		columnTypes[ROCK_SAMPLES] = String.class;
		columnNames[REGOLITH] = "Regolith";
		columnTypes[REGOLITH] = String.class;
		columnNames[ICE] = "Ice";
		columnTypes[ICE] = String.class;
	};

	private static UnitManager unitManager = Simulation.instance().getUnitManager();

	// Data members
	private UnitManagerListener unitManagerListener;

	private Map<Unit, Map<AmountResource, Double>> resourceCache;

	private DecimalFormat df = new DecimalFormat("#,###,##0.00");

	
	/**
	 * Constructs a SettlementTableModel model that displays all Settlements in the
	 * simulation.
	 *
	 * @param unitManager Unit manager that holds settlements.
	 */
	public SettlementTableModel() {
		super(Msg.getString("SettlementTableModel.tabName"), "SettlementTableModel.countingSettlements", //$NON-NLS-2$
				columnNames, columnTypes);

		setSource(unitManager.getSettlements());
		unitManagerListener = new LocalUnitManagerListener();
		unitManager.addUnitManagerListener(unitManagerListener);
		
		df.setMinimumFractionDigits(2);
		df.setMinimumIntegerDigits(1);
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
			Map<AmountResource, Double> resourceMap = resourceCache.get(settle);

			try {
				switch (columnIndex) {
				case NAME: {
					result = settle.getName();
				}
					break;

				case WATER: {
					result = df.format(resourceMap.get(ResourceUtil.waterAR));
				}
					break;

				case OXYGEN: {
					result = df.format(resourceMap.get(ResourceUtil.oxygenAR));
				}
					break;

				case METHANE: {
					result = df.format(resourceMap.get(ResourceUtil.methaneAR));
				}
					break;

				case HYDROGEN: {
					result = df.format(resourceMap.get(ResourceUtil.hydrogenAR));
				}
					break;

				case CO2: {
					result = df.format(resourceMap.get(ResourceUtil.carbonDioxideAR));
				}
					break;

				case ROCK_SAMPLES: {
					result = df.format(resourceMap.get(ResourceUtil.rockSamplesAR));
				}
					break;

				case REGOLITH: {
					result = df.format(resourceMap.get(ResourceUtil.regolithAR));
				}
					break;

				case PARKED: {
					result = settle.getParkedVehicleNum();
				}
					break;

				case POWER: {
					result = df.format(settle.getPowerGrid().getGeneratedPower());
				}
					break;

				case GREY_WATER: {
					result = df.format(resourceMap.get(ResourceUtil.greyWaterAR));
				}
					break;

				case BLACK_WATER: {
					result = df.format(resourceMap.get(ResourceUtil.blackWaterAR));
				}
					break;

				case ICE: {
					result = df.format(resourceMap.get(ResourceUtil.iceAR));
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
				double currentValue = 0.0;
				Map<AmountResource, Double> resourceMap = resourceCache.get(unit);
				
				if (target.equals(ResourceUtil.oxygenAR)) {
					tempColumnNum = OXYGEN;
					currentValue = resourceMap.get(ResourceUtil.oxygenAR);
				}
				else if (target.equals(ResourceUtil.hydrogenAR)) {
					tempColumnNum = HYDROGEN;
					currentValue = resourceMap.get(ResourceUtil.hydrogenAR);
				}	
				else if (target.equals(ResourceUtil.carbonDioxideAR)) {
					tempColumnNum = CO2;
					currentValue = resourceMap.get(ResourceUtil.carbonDioxideAR);
				}
				else if (target.equals(ResourceUtil.methaneAR)) {
					tempColumnNum = METHANE;		
					currentValue = resourceMap.get(ResourceUtil.methaneAR);
				}
				else if (target.equals(ResourceUtil.waterAR)) {
					tempColumnNum = WATER;
					currentValue = resourceMap.get(ResourceUtil.waterAR);
				}
				else if (target.equals(ResourceUtil.greyWaterAR)) {
					tempColumnNum = GREY_WATER;
					currentValue = resourceMap.get(ResourceUtil.greyWaterAR);
				}
				else if (target.equals(ResourceUtil.blackWaterAR)) {
					tempColumnNum = BLACK_WATER;
					currentValue = resourceMap.get(ResourceUtil.blackWaterAR);
				}
				else if (target.equals(ResourceUtil.rockSamplesAR)) {
					tempColumnNum = ROCK_SAMPLES;
					currentValue = resourceMap.get(ResourceUtil.rockSamplesAR);
				}
				else if (target.equals(ResourceUtil.regolithAR)) {
					tempColumnNum = REGOLITH;
					currentValue = resourceMap.get(ResourceUtil.regolithAR);
				}
				else if (target.equals(ResourceUtil.iceAR)) {
					tempColumnNum = ICE;
					currentValue = resourceMap.get(ResourceUtil.iceAR);
				}
				
				if (tempColumnNum > -1) {
//					double currentValue = Math.round((Double)getValueAt(unitIndex, tempColumnNum)*10.0)/10.0;
					double newValue = Math.round(getResourceStored(unit, (AmountResource) target)*100.0)/100.0;
					if (currentValue != newValue) {
						columnNum = tempColumnNum;
//						Map<AmountResource, Double> resourceMap = resourceCache.get(unit);
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
		while (iter.hasNext())
			addUnit(iter.next());
	}

	@Override
	protected void addUnit(Unit newUnit) {
		if (resourceCache == null)
			resourceCache = new HashMap<Unit, Map<AmountResource, Double>>();
		if (!resourceCache.containsKey(newUnit)) {
			try {
				Map<AmountResource, Double> resourceMap = new HashMap<AmountResource, Double>(9);

				resourceMap.put(ResourceUtil.oxygenAR, getResourceStored(newUnit, ResourceUtil.oxygenAR));

				resourceMap.put(ResourceUtil.waterAR, getResourceStored(newUnit, ResourceUtil.waterAR));

				resourceMap.put(ResourceUtil.hydrogenAR, getResourceStored(newUnit, ResourceUtil.hydrogenAR));

				resourceMap.put(ResourceUtil.methaneAR, getResourceStored(newUnit, ResourceUtil.methaneAR));

				resourceMap.put(ResourceUtil.rockSamplesAR, getResourceStored(newUnit, ResourceUtil.rockSamplesAR));

				resourceMap.put(ResourceUtil.regolithAR, getResourceStored(newUnit, ResourceUtil.regolithAR));

				resourceMap.put(ResourceUtil.greyWaterAR, getResourceStored(newUnit, ResourceUtil.greyWaterAR));

				resourceMap.put(ResourceUtil.blackWaterAR, getResourceStored(newUnit, ResourceUtil.blackWaterAR));

				resourceMap.put(ResourceUtil.iceAR, getResourceStored(newUnit, ResourceUtil.iceAR));

				resourceMap.put(ResourceUtil.carbonDioxideAR, getResourceStored(newUnit, ResourceUtil.carbonDioxideAR));

				resourceCache.put(newUnit, resourceMap);
			} catch (Exception e) {
			}
		}
		super.addUnit(newUnit);
	}

	@Override
	protected void removeUnit(Unit oldUnit) {
		if (resourceCache == null)
			resourceCache = new HashMap<Unit, Map<AmountResource, Double>>();
		if (resourceCache.containsKey(oldUnit)) {
			Map<AmountResource, Double> resourceMap = resourceCache.get(oldUnit);
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
	private double getResourceStored(Unit unit, AmountResource resource) {
		// This is the quickest way but it may or may not work if the object reference
		// of ARs have changed during (de)serialization.
		return Math.round(unit.getInventory().getAmountResourceStored(resource, true) * 100.0) / 100.0;
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