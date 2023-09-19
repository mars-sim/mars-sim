/*
 * Mars Simulation Project
 * SettlementTableModel.java
 * @date 2022-07-01
 * @author Barry Evans
 */
package org.mars_sim.msp.ui.swing.tool.monitor;

import java.util.ArrayList;
import java.util.List;

import org.mars.sim.tools.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitEvent;
import org.mars_sim.msp.core.UnitEventType;
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
public class SettlementTableModel extends UnitTableModel<Settlement> {

	// Column indexes
	private static final int NAME = 0;
	private static final int POPULATION = 1;
	private static final int PARKED = 2;
	private static final int MISSION = 3;
	private static final int COMPUTING_UNIT = 4;
	private static final int POWER_GEN = 5;
	private static final int POWER_LOAD = 6;
	private static final int ENERGY_STORED = 7;
	
	private static final int MALFUNCTION = 8;

	private static final int OXYGEN_COL = 9;
	private static final int HYDROGEN_COL = 10;
	private static final int METHANE_COL = 11;
	private static final int METHANOL_COL = 12;
	
	private static final int WATER_COL = 13;
	private static final int ICE_COL = 14;
	private static final int REGOLITHS_COL = 15;
	private static final int SAND_COL = 16;
	
	private static final int ROCKS_COL = 17;
	private static final int ORES_COL = 18;
	private static final int MINERALS_COL = 19;
	
	private static final int CONCRETE_COL = 20;
	private static final int CEMENT_COL = 21;
	

	/** The number of Columns. */
	private static final int COLUMNCOUNT = 22;
	/** Names of Columns. */
	private static final ColumnSpec[] COLUMNS;

	static {
		COLUMNS = new ColumnSpec[COLUMNCOUNT];
		COLUMNS[NAME] = new ColumnSpec("Name", String.class);
		COLUMNS[POPULATION] = new ColumnSpec("Pop", Integer.class);
		COLUMNS[PARKED] = new ColumnSpec("Parked Veh", Integer.class);
		COLUMNS[MISSION] = new ColumnSpec("Mission Veh", Integer.class);
		COLUMNS[COMPUTING_UNIT] = new ColumnSpec("CU(s)", String.class);
		COLUMNS[POWER_GEN] = new ColumnSpec("kW Gen", Double.class);
		COLUMNS[POWER_LOAD] = new ColumnSpec("kW Load", Double.class);
		COLUMNS[ENERGY_STORED] = new ColumnSpec("kWh Stored", String.class);
		COLUMNS[MALFUNCTION] = new ColumnSpec("Malfunction", String.class);		
		COLUMNS[OXYGEN_COL] = new ColumnSpec("Oxygen", Double.class);
		COLUMNS[HYDROGEN_COL] = new ColumnSpec("Hydrogen", Double.class);	
		COLUMNS[METHANE_COL] = new ColumnSpec("Methane", Double.class);	
		COLUMNS[METHANOL_COL] = new ColumnSpec("Methanol", Double.class);		
		COLUMNS[WATER_COL] = new ColumnSpec("Water", Double.class);
		COLUMNS[ICE_COL] = new ColumnSpec("Ice", Double.class);		
		COLUMNS[REGOLITHS_COL] = new ColumnSpec("Regoliths", Double.class);
		COLUMNS[SAND_COL] = new ColumnSpec("Sand", Double.class);	
		COLUMNS[ROCKS_COL] = new ColumnSpec("Rocks", Double.class);	
		COLUMNS[ORES_COL] = new ColumnSpec("Ores", Double.class);	
		COLUMNS[MINERALS_COL] = new ColumnSpec("Minerals", Double.class);	
		COLUMNS[CONCRETE_COL] = new ColumnSpec("Concrete", Double.class);
		COLUMNS[CEMENT_COL] = new ColumnSpec("Cement", Double.class);	
	};

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

	private static final int SAND_ID = ResourceUtil.sandID;
	
	private static final int CONCRETE_ID = ResourceUtil.concreteID;
	private static final int CEMENT_ID = ResourceUtil.cementID;

	private boolean singleSettlement;

	/**
	 * Constructs a SettlementTableModel model that displays all Settlements in the
	 * simulation.
	 */
	public SettlementTableModel() {
		super(UnitType.SETTLEMENT, "Mars", "SettlementTableModel.countingSettlements",
				COLUMNS);
		singleSettlement = false;

		setupCaches();
		resetEntities(unitManager.getSettlements());
			
		listenForUnits();
	}

	private void setupCaches() {
		setCachedColumns(OXYGEN_COL, MINERALS_COL);
	}

	/**
	 * Constructs a SettlementTableModel model that displays a specific settlement in the
	 * simulation.
	 *
	 * @param settlement
	 */
	public SettlementTableModel(Settlement settlement) {
		super(UnitType.SETTLEMENT, Msg.getString("SettlementTableModel.tabName"), //$NON-NLS-2$
				"SettlementTableModel.countingSettlements", 
				COLUMNS);
		singleSettlement = true;
		setupCaches();

		setSettlementFilter(settlement);
	}

	/**
	 * Set the settlement filter for the Robot table
	 * @param filter
	 */
	@Override
	public boolean setSettlementFilter(Settlement filter) {

		if (singleSettlement) {
			List<Settlement> sList = new ArrayList<>();
			sList.add(filter);
			resetEntities(sList);
		}
		return singleSettlement;
	}

	/**
	 * Return the value of a Cell
	 *
	 * @param rowIndex    Row index of the cell.
	 * @param columnIndex Column index of the cell.
	 */
	@Override
	protected Object getEntityValue(Settlement settle, int columnIndex) {
		Object result = null;

		switch (columnIndex) {
			case NAME: 
				result = settle.getName();
				break;
				
			case PARKED: 
				result = settle.getNumParkedVehicles();
				break;

			case MISSION: 
				result = settle.getMissionVehicleNum();
				break;

			case COMPUTING_UNIT: 
				result = settle.getBuildingManager().displayComputingResources();
				break;
				
			case POWER_GEN: 
				double genPower = settle.getPowerGrid().getGeneratedPower();
				if (genPower < 0D || Double.isNaN(genPower) || Double.isInfinite(genPower))
					genPower = 0;
				result = genPower;
				break;

			case POWER_LOAD: 
				double reqPower = settle.getPowerGrid().getRequiredPower();
				if (reqPower < 0D || Double.isNaN(reqPower) || Double.isInfinite(reqPower))
					reqPower = 0;
				result = reqPower;
				break;

			case ENERGY_STORED: 
				result = settle.getPowerGrid().getDisplayStoredEnergy();
				break;
				
			case POPULATION: 
				result = settle.getNumCitizens();
				break;

			case MALFUNCTION: {
				int severity = 0;
				Malfunction malfunction = null;
				for(Building building : settle.getBuildingManager().getBuildingSet()) {
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

			case OXYGEN_COL: 
				result = settle.getAllAmountResourceOwned(OXYGEN_ID);
				break;

			case HYDROGEN_COL: 
				result = settle.getAllAmountResourceOwned(HYDROGEN_ID);
				break;
				
			case METHANE_COL: 
				result = settle.getAllAmountResourceOwned(METHANE_ID);
				break;

			case METHANOL_COL: 
				result = settle.getAllAmountResourceOwned(METHANOL_ID);
				break;
				
			case WATER_COL: 
				result = settle.getAllAmountResourceOwned(WATER_ID);
				break;
				
			case ICE_COL: 
				result = settle.getAllAmountResourceOwned(ICE_ID);
				break;
													
			case REGOLITHS_COL:
				result = getTotalAmount(REGOLITH_IDS, settle);
				break;

			case SAND_COL:
				result = settle.getAllAmountResourceOwned(SAND_ID);
				break;
				
			case ROCKS_COL:
				result = getTotalAmount(ROCK_IDS, settle);
				break;
				
			case ORES_COL:
				result = getTotalAmount(ORE_IDS, settle);
				break;
				
			case MINERALS_COL:
				result = getTotalAmount(MINERAL_IDS, settle);
				break;
			
			case CONCRETE_COL: 
				result = settle.getAllAmountResourceOwned(CONCRETE_ID);
				break;

			case CEMENT_COL: 
				result = settle.getAllAmountResourceOwned(CEMENT_ID);
				break;
				
			default:
				break;
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
	private static double getTotalAmount(int [] types, Settlement settle) {
		double result = 0;
		for (int i = 0; i < types.length; i++) {
			int id = types[i];
			result += settle.getAmountResourceStored(id);
		}
		return result;
	}
	
	/**
	 * Catches unit update event.
	 * 
	 * @param event the unit event.
	 */
	@Override
	public void unitUpdate(UnitEvent event) {
		Unit unit = (Unit) event.getSource();
		Object target = event.getTarget();
		UnitEventType eventType = event.getType();

		int columnNum = -1;
		if (eventType == UnitEventType.NAME_EVENT) columnNum = NAME;
		else if (eventType == UnitEventType.INVENTORY_STORING_UNIT_EVENT ||
				eventType == UnitEventType.INVENTORY_RETRIEVING_UNIT_EVENT) {
			if (target instanceof Person) columnNum = POPULATION;
			else if (target instanceof Vehicle) columnNum = PARKED;
		}
		else if (eventType == UnitEventType.CONSUMING_COMPUTING_EVENT) columnNum = COMPUTING_UNIT;
		else if (eventType == UnitEventType.GENERATED_POWER_EVENT) columnNum = POWER_GEN;
		else if (eventType == UnitEventType.REQUIRED_POWER_EVENT) columnNum = POWER_LOAD;
		else if (eventType == UnitEventType.STORED_ENERGY_EVENT) columnNum = ENERGY_STORED;		
		else if (eventType == UnitEventType.MALFUNCTION_EVENT) columnNum = MALFUNCTION;
		else if (eventType == UnitEventType.INVENTORY_RESOURCE_EVENT)
		{
			// Resource change
			int resourceID = -1;
			if (target instanceof AmountResource) {
				resourceID = ((AmountResource)target).getID();
			}
			else if (target instanceof Integer) {
				// Note: most likely, the source is an integer id
				resourceID = (Integer)target;
				if (resourceID >= ResourceUtil.FIRST_ITEM_RESOURCE_ID)
					// if it's an item resource, quit
					return;
			}
			else {
				return;
			}

			if (resourceID == OXYGEN_ID) {
				columnNum = OXYGEN_COL;
			}
			else if (resourceID == HYDROGEN_ID) {
				columnNum = HYDROGEN_COL;
			}
			else if (resourceID == METHANOL_ID) {
				columnNum = METHANOL_COL;
			}
			else if (resourceID == METHANE_ID) {
				columnNum = METHANE_COL;
			}
			else if (resourceID == WATER_ID) {
				columnNum = WATER_COL;
			}
			else if (resourceID == ICE_ID) {
				columnNum = ICE_COL;
			}
			else if (resourceID == SAND_ID) {
				columnNum = SAND_COL;
			}
			else if (resourceID == CONCRETE_ID) {
				columnNum = CONCRETE_COL;
			}
			else if (resourceID == CEMENT_ID) {
				columnNum = CEMENT_COL;
			}
			else {
				boolean found = false;
				for (int i = 0; i < REGOLITH_IDS.length; i++) {
					if (!found && resourceID == REGOLITH_IDS[i]) {
						columnNum = REGOLITHS_COL;
						found = true;
					}
				}
				if (!found) {
					for (int i = 0; i < ORE_IDS.length; i++) {
						if (!found && resourceID == ORE_IDS[i]) {
							columnNum = ORES_COL;
							found = true;
						}
					}
				}
				if (!found) {
					for (int i = 0; i < MINERAL_IDS.length; i++) {
						if (!found && resourceID == MINERAL_IDS[i]) {
							columnNum = MINERALS_COL;
							found = true;
						}
					}
				}
				if (!found) {
					for (int i = 0; i < ROCK_IDS.length; i++) {
						if (!found && resourceID == ROCK_IDS[i]) {
							columnNum = ROCKS_COL;
							found = true;
						}
					}
				}
			}
		}

		if (columnNum > -1) {
			entityValueUpdated((Settlement)unit, columnNum, columnNum);
		}
	}
}
