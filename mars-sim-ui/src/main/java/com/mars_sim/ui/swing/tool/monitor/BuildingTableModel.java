/*
 * Mars Simulation Project
 * BuildingTableModel.java
 * @date 2024-07-03
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.tool.monitor;

import java.util.Set;

import com.mars_sim.core.UnitEvent;
import com.mars_sim.core.UnitEventType;
import com.mars_sim.core.UnitType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.tools.Msg;
import com.mars_sim.ui.swing.utils.ColumnSpec;

/**
 * The BuildingTableModel maintains a list of Building objects. By defaults the source
 * of the list is the Unit Manager. 
 */
@SuppressWarnings("serial")
public class BuildingTableModel extends UnitTableModel<Building> {

	// Column indexes
	private static final int NAME = 0;
	private static final int SETTLEMENT = NAME + 1;
	private static final int TYPE = SETTLEMENT + 1;
	private static final int CATEGORY = TYPE + 1;
	private static final int POWER_MODE = CATEGORY + 1;
	private static final int POWER_REQ = POWER_MODE + 1;
	private static final int POWER_GEN = POWER_REQ + 1;
	
	private static final int TEMPERATURE = POWER_GEN + 1;
	private static final int HEAT_GEN = TEMPERATURE + 1;
	private static final int HEAT_LOAD = HEAT_GEN + 1;
	private static final int HEAT_GAIN = HEAT_LOAD + 1;
	private static final int HEAT_MATCH = HEAT_GAIN + 1;
	private static final int HEAT_VENT = HEAT_MATCH + 1;
	private static final int HEAT_DEV = HEAT_VENT + 1;
	private static final int EXCESS_HEAT = HEAT_DEV + 1;

	
	private static final int SOLAR = EXCESS_HEAT + 1;
	private static final int ELECTRIC = SOLAR + 1;
	private static final int NUCLEAR = ELECTRIC + 1;
	private static final int FUEL = NUCLEAR + 1;
	
	private static final int COLUMNCOUNT = FUEL + 1;

	/** Names of Columns. */
	private static final ColumnSpec[] COLUMNS;

	/**
	 * The static initializer creates the name & type arrays.
	 */
	static {
		COLUMNS = new ColumnSpec[COLUMNCOUNT];
		COLUMNS[NAME] = new ColumnSpec(Msg.getString("BuildingTableModel.column.name"), String.class);
		COLUMNS[SETTLEMENT] = new ColumnSpec("Settlement", String.class);
		COLUMNS[TYPE] = new ColumnSpec(Msg.getString("BuildingTableModel.column.type"), String.class);
		COLUMNS[CATEGORY] = new ColumnSpec(Msg.getString("BuildingTableModel.column.category"), String.class);	
		COLUMNS[POWER_MODE] = new ColumnSpec(Msg.getString("BuildingTableModel.column.power.mode"), String.class);		
		COLUMNS[POWER_REQ]  = new ColumnSpec(Msg.getString("BuildingTableModel.column.power.req"), Double.class);
		COLUMNS[POWER_GEN]  = new ColumnSpec(Msg.getString("BuildingTableModel.column.power.gen"), Double.class);
		
		COLUMNS[HEAT_GEN]  = new ColumnSpec(Msg.getString("BuildingTableModel.column.heat.gen"), Double.class);
		COLUMNS[HEAT_MATCH]  = new ColumnSpec(Msg.getString("BuildingTableModel.column.heat.match"), Double.class);
		COLUMNS[HEAT_LOAD] = new ColumnSpec(Msg.getString("BuildingTableModel.column.heat.load"), Double.class);
		COLUMNS[HEAT_VENT] = new ColumnSpec(Msg.getString("BuildingTableModel.column.heat.vent"), Double.class);
		COLUMNS[TEMPERATURE] = new ColumnSpec(Msg.getString("BuildingTableModel.column.temperature"),Double.class);
		COLUMNS[EXCESS_HEAT] = new ColumnSpec(Msg.getString("BuildingTableModel.column.heat.excess"), Double.class);
		COLUMNS[HEAT_GAIN] = new ColumnSpec(Msg.getString("BuildingTableModel.column.heat.gain"), Double.class);
		COLUMNS[HEAT_DEV] = new ColumnSpec(Msg.getString("BuildingTableModel.column.heat.dev"), Double.class);
		
		COLUMNS[SOLAR] = new ColumnSpec(Msg.getString("BuildingTableModel.column.heat.solar"), Double.class);
		COLUMNS[ELECTRIC] = new ColumnSpec(Msg.getString("BuildingTableModel.column.heat.electric"),Double.class);
		COLUMNS[NUCLEAR]  = new ColumnSpec(Msg.getString("BuildingTableModel.column.heat.nuclear"), Double.class);
		COLUMNS[FUEL]  = new ColumnSpec(Msg.getString("BuildingTableModel.column.heat.fuel"), Double.class);

	}

	/**
	 * Constructor.
	 * 
	 * @param settlement
	 * @throws Exception
	 */
	public BuildingTableModel() {
		super(UnitType.BUILDING, Msg.getString("BuildingTableModel.nameBuildings", ""),
				"BuildingTableModel.countingBuilding", //$NON-NLS-1$
				COLUMNS);	
		
		setSettlementColumn(SETTLEMENT);
	}

	@Override
	public boolean setSettlementFilter(Set<Settlement> filter) {
		getEntities().forEach(s -> s.removeUnitListener(this));

		var newBuildings = filter.stream().flatMap(s -> s.getBuildingManager().getBuildingSet().stream()).toList();
		resetEntities(newBuildings);

		newBuildings.forEach(s -> s.addUnitListener(this));

		return true;
	}

	/**
	 * Returns the value of a Cell.
	 *
	 * @param rowIndex    Row index of the cell.
	 * @param columnIndex Column index of the cell.
	 */
	@Override
	protected Object getEntityValue(Building building, int columnIndex) {
		Object result = null;

		switch (columnIndex) {

		case NAME: 
			result = building.getName();
			break;
		case SETTLEMENT: 
			result = building.getSettlement().getName();
			break;

		case TYPE: 
			result = building.getBuildingType();
			break;
		
		case CATEGORY:
			result = building.getCategory().getName();
			break;

		case POWER_MODE:
			result = building.getPowerMode().getName();
			break;
						
		case POWER_REQ:
			result =  building.getFullPowerRequired();
			break;
			
		case POWER_GEN:
			if (building.getPowerGeneration() != null)
				result =  building.getPowerGeneration().getGeneratedPower();
			break;
			
		case HEAT_MATCH:
			result = building.getHeatMatch();
			break;

		case HEAT_VENT:
			result = building.getHeatVent();
			break;
			
		case HEAT_DEV:
			result = building.getHeatDev();
			break;
			
		case HEAT_GEN:
			result = building.getHeatGenerated();
			break;
			
		case HEAT_LOAD:
			result = building.getHeatRequired();
			break;
			
		case HEAT_GAIN:
			result = building.getHeatGain();
			break;
			
		case EXCESS_HEAT:
			result = building.getExcessHeat();
			break;
			
		case TEMPERATURE:
			result = building.getCurrentTemperature();
			break;
			
		case SOLAR:
			result = building.getSolarPowerGen();
			break;
			
		case ELECTRIC:
			result = building.getElectricPowerGen();
			break;
			
		case NUCLEAR:
			result = building.getNuclearPowerGen();
			break;
			
		case FUEL:
			result = building.getFuelPowerGen();
			break;
			
		default:
			break;
		}

		return result;
	}
	
	@Override
	public void destroy() {
		getEntities().forEach(s -> s.removeUnitListener(this));
		super.destroy();
	}
	
	/**
	 * Catches unit update event.
	 *
	 * @param event the unit event.
	 */
	@Override
	public void unitUpdate(UnitEvent event) {
		if (event.getSource() instanceof Building building) {
			UnitEventType eventType = event.getType();

			int columnIndex = switch(eventType) {
				case POWER_MODE_EVENT -> POWER_MODE;
				case GENERATED_POWER_EVENT -> POWER_GEN;
				case REQUIRED_POWER_EVENT -> POWER_REQ;
				
				case REQUIRED_HEAT_EVENT -> HEAT_LOAD;
				case GENERATED_HEAT_EVENT -> HEAT_GEN;
				case TOTAL_HEAT_GAIN_EVENT -> HEAT_GAIN;
				case TEMPERATURE_EVENT -> TEMPERATURE;
				case EXCESS_HEAT_EVENT -> EXCESS_HEAT;
				case HEAT_VENT_EVENT -> HEAT_VENT;
				case HEAT_MATCH_EVENT -> HEAT_MATCH;
				case HEAT_DEV_EVENT -> HEAT_DEV;
				
				case SOLAR_HEAT_EVENT -> SOLAR;
				case ELECTRIC_HEAT_EVENT -> ELECTRIC;
				case NUCLEAR_HEAT_EVENT -> NUCLEAR;
				case FUEL_HEAT_EVENT -> FUEL;
				default -> -1;
			};

			if (columnIndex >= 0) {
				entityValueUpdated(building, columnIndex, columnIndex);
			}
		}
	}
}
