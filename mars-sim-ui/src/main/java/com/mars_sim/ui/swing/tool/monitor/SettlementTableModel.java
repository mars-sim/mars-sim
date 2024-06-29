/*
 * Mars Simulation Project
 * SettlementTableModel.java
 * @date 2022-07-01
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.tool.monitor;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.mars_sim.core.Unit;
import com.mars_sim.core.UnitEvent;
import com.mars_sim.core.UnitEventType;
import com.mars_sim.core.UnitType;
import com.mars_sim.core.equipment.ResourceHolder;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.resource.AmountResource;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.BuildingManager;
import com.mars_sim.core.structure.building.function.Computation;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.ui.swing.utils.ColumnSpec;

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
	
	private static final int ORES_COL = 17;
	private static final int MINERALS_COL = 18;
	
	private static final int CONCRETE_COL = 19;
	private static final int CEMENT_COL = 20;
	private static final int LIME_COL = 21;
	private static final int ROCKS_COL = 22;
	
	private static final int COLUMNCOUNT = 23;
	private static final ColumnSpec[] COLUMNS;
	private static final Map<Integer,Integer> RESOURCE_TO_COL;
	private static final int[] COL_TO_RESOURCE;

	// Pseudo resource ids to cover composites
	private static final int REGOLITH_ID = -1;
	private static final int ROCK_ID = -2;
	private static final int MINERAL_ID = -3;
	private static final int ORE_ID = -4;

	static {
		COLUMNS = new ColumnSpec[COLUMNCOUNT];
		COL_TO_RESOURCE = new int[COLUMNCOUNT];
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
		COL_TO_RESOURCE[OXYGEN_COL] = ResourceUtil.oxygenID;
		COLUMNS[HYDROGEN_COL] = new ColumnSpec("Hydrogen", Double.class);	
		COL_TO_RESOURCE[HYDROGEN_COL] = ResourceUtil.hydrogenID;
		COLUMNS[METHANE_COL] = new ColumnSpec("Methane", Double.class);
		COL_TO_RESOURCE[METHANE_COL] = ResourceUtil.methaneID;	
		COLUMNS[METHANOL_COL] = new ColumnSpec("Methanol", Double.class);
		COL_TO_RESOURCE[METHANOL_COL] = ResourceUtil.methanolID;	
		COLUMNS[WATER_COL] = new ColumnSpec("Water", Double.class);
		COL_TO_RESOURCE[WATER_COL] = ResourceUtil.waterID;	
		COLUMNS[ICE_COL] = new ColumnSpec("Ice", Double.class);
		COL_TO_RESOURCE[ICE_COL] = ResourceUtil.iceID;			
		COLUMNS[REGOLITHS_COL] = new ColumnSpec("Regoliths", Double.class);
		COL_TO_RESOURCE[REGOLITHS_COL] = REGOLITH_ID;	
		COLUMNS[SAND_COL] = new ColumnSpec("Sand", Double.class);	
		COL_TO_RESOURCE[SAND_COL] = ResourceUtil.sandID;	
		COLUMNS[ROCKS_COL] = new ColumnSpec("Rocks", Double.class);	
		COL_TO_RESOURCE[ROCKS_COL] = ROCK_ID;	
		COLUMNS[ORES_COL] = new ColumnSpec("Ores", Double.class);
		COL_TO_RESOURCE[ORES_COL] = ORE_ID;	
		COLUMNS[MINERALS_COL] = new ColumnSpec("Minerals", Double.class);
		COL_TO_RESOURCE[MINERALS_COL] = MINERAL_ID;	
		COLUMNS[CONCRETE_COL] = new ColumnSpec("Concrete", Double.class);
		COL_TO_RESOURCE[CONCRETE_COL] = ResourceUtil.concreteID;	
		COLUMNS[CEMENT_COL] = new ColumnSpec("Cement", Double.class);
		COL_TO_RESOURCE[CEMENT_COL] = ResourceUtil.cementID;	
		COLUMNS[LIME_COL] = new ColumnSpec("Lime", Double.class);
		COL_TO_RESOURCE[LIME_COL] = ResourceUtil.limeID;
		
		
		// Mapping from resource to the column
		RESOURCE_TO_COL = new HashMap<>();
		RESOURCE_TO_COL.put(ResourceUtil.oxygenID, OXYGEN_COL);
		RESOURCE_TO_COL.put(ResourceUtil.hydrogenID, HYDROGEN_COL);
		RESOURCE_TO_COL.put(ResourceUtil.methanolID, METHANOL_COL);
		RESOURCE_TO_COL.put(ResourceUtil.methaneID, METHANE_COL);
		RESOURCE_TO_COL.put(ResourceUtil.waterID, WATER_COL);
		RESOURCE_TO_COL.put(ResourceUtil.oxygenID, OXYGEN_COL);
		RESOURCE_TO_COL.put(ResourceUtil.iceID, ICE_COL);
		RESOURCE_TO_COL.put(ResourceUtil.sandID, SAND_COL);
		RESOURCE_TO_COL.put(ResourceUtil.concreteID, CONCRETE_COL);
		RESOURCE_TO_COL.put(ResourceUtil.cementID, CEMENT_COL);
		RESOURCE_TO_COL.put(ResourceUtil.limeID, LIME_COL);
		
		for (int i : ResourceUtil.REGOLITH_TYPES) {
			RESOURCE_TO_COL.put(i, REGOLITHS_COL);
		}
		for (int i : ResourceUtil.oreDepositIDs) {
			RESOURCE_TO_COL.put(i, ORES_COL);
		}
		for (int i : ResourceUtil.mineralConcIDs) {
			RESOURCE_TO_COL.put(i, MINERALS_COL);
		}
		for (int i : ResourceUtil.rockIDs) {
			RESOURCE_TO_COL.put(i, ROCKS_COL);
		}
	}

	private boolean allSettlements;

	/**
	 * Constructs a SettlementTableModel model that displays all Settlements in the
	 * simulation.
	 */
	public SettlementTableModel(boolean allSettlements) {
		super(UnitType.SETTLEMENT, (allSettlements ? "Mars" : "Settlement"), "SettlementTableModel.countingSettlements",
				COLUMNS);
		this.allSettlements = allSettlements;

		setupCaches();
		if (allSettlements) {
			resetEntities(unitManager.getSettlements());
			listenForUnits();
		}
	}

	private void setupCaches() {
		setCachedColumns(OXYGEN_COL, MINERALS_COL);
	}

	/**
	 * Sets the settlement filter for the Robot table.
	 * 
	 * @param filter
	 */
	@Override
	public boolean setSettlementFilter(Set<Settlement> filter) {

		if (!allSettlements) {
			resetEntities(filter);
		}
		return !allSettlements;
	}

	/**
	 * Returns the value of a Cell.
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
				result = displayComputingResources(settle.getBuildingManager());
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
					var found = settle.getBuildingManager().getBuildingSet().stream()
										.map(b -> b.getMalfunctionManager().getMostSeriousMalfunction())
										.filter(Objects::nonNull)
										.max((a, b) -> a.getSeverity() - b.getSeverity());

					if (found.isPresent()) {
						result = found.get().getName();
					}
				}
				break;

			default: {
					// must be a resource column
					int resourceId = COL_TO_RESOURCE[columnIndex];
					result = switch(resourceId) {
						case REGOLITH_ID -> getTotalAmount(ResourceUtil.REGOLITH_TYPES, settle);
						case ORE_ID -> getTotalAmount(ResourceUtil.oreDepositIDs, settle);
						case MINERAL_ID -> getTotalAmount(ResourceUtil.mineralConcIDs, settle);
						case ROCK_ID -> getTotalAmount(ResourceUtil.rockIDs, settle);
						default -> settle.getAllAmountResourceOwned(resourceId);
					};
				}
				break;						
		}

		return result;
	}

	/**
	 * Gets the sum of all computing resources in a settlement and displays 
	 * the total CUs and the percent.
	 * 
	 * @return
	 */
	private static String displayComputingResources(BuildingManager bm) {
		double max = 0;
		double units = 0;
		Set<Building> nodeBldgs = bm.getBuildingSet(FunctionType.COMPUTATION);
		for (Building b: nodeBldgs) {
			Computation node = b.getComputation();
			units += node.getCurrentCU();
			max += node.getPeakCU();
		}
		
		if (max == 0) {
			return "";
		}
		double percent = units / max * 100;
		
		StringBuilder sb = new StringBuilder();
		sb.append(Math.round(units *10.0)/10.0)
		.append(" (")
		.append(Math.round(percent *10.0)/10.0)
		.append(" %)");
		
		return sb.toString();
	}

	/**
	 * Gets the sum of the amount of the same types of resources.
	 * 
	 * @param types
	 * @param holder
	 * @return
	 */
	static double getTotalAmount(int [] types, ResourceHolder holder) {
		double result = 0;
		for (int id : types) {
			result += holder.getAmountResourceStored(id);
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
		switch (eventType) {
			case NAME_EVENT: columnNum = NAME; break;
			case INVENTORY_STORING_UNIT_EVENT:
			case INVENTORY_RETRIEVING_UNIT_EVENT: {
				if (target instanceof Person) columnNum = POPULATION;
				else if (target instanceof Vehicle) columnNum = PARKED;
			} break;
			case CONSUMING_COMPUTING_EVENT: columnNum = COMPUTING_UNIT; break;
			case GENERATED_POWER_EVENT: columnNum = POWER_GEN; break;
			case REQUIRED_POWER_EVENT: columnNum = POWER_LOAD; break;
			case STORED_ENERGY_EVENT: columnNum = ENERGY_STORED; break;		
			case MALFUNCTION_EVENT: columnNum = MALFUNCTION; break;
			case INVENTORY_RESOURCE_EVENT: {
				// Resource change
				int resourceID = -1;
				if (target instanceof AmountResource ar) {
					resourceID = ar.getID();
				}
				else if (target instanceof Integer i) {
					// Note: most likely, the source is an integer id
					resourceID = i;
				}
				else {
					return;
				}

				if (RESOURCE_TO_COL.containsKey(resourceID)) 
					columnNum = RESOURCE_TO_COL.get(resourceID);
			} break;

			default:
		}

		if (columnNum > -1) {
			entityValueUpdated((Settlement)unit, columnNum, columnNum);
		}
	}
}
