/*
 * Mars Simulation Project
 * BuildingTableModel.java
 * @date 2024-07-03
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.tool.monitor;

import java.util.Set;

import com.mars_sim.core.EntityEvent;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.utility.heating.HeatMode;
import com.mars_sim.core.building.utility.heating.HeatSource;
import com.mars_sim.core.building.utility.heating.HeatSourceType;
import com.mars_sim.core.building.utility.heating.Heating;
import com.mars_sim.core.building.utility.heating.ThermalGeneration;
import com.mars_sim.core.building.utility.power.PowerGrid;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.utils.ColumnSpec;

/**
 * The BuildingTableModel maintains a list of Building objects. By defaults the source
 * of the list is the Unit Manager. 
 */
@SuppressWarnings("serial")
class BuildingTableModel extends EntityMonitorModel<Building> {

	// Column indexes
	private static final int NAME = 0;
	private static final int SETTLEMENT = NAME + 1;
	private static final int TYPE = SETTLEMENT + 1;
	private static final int CATEGORY = TYPE + 1;
	
	private static final int POWER_MODE = CATEGORY + 1;
	private static final int POWER_REQ = POWER_MODE + 1;
	private static final int POWER_GEN = POWER_REQ + 1;
	
	private static final int SOLAR = POWER_GEN + 1;
	private static final int ELECTRIC = SOLAR + 1;
	private static final int FUEL = ELECTRIC + 1;
	private static final int NUCLEAR = FUEL + 1;
	
	private static final int TEMPERATURE = NUCLEAR + 1;
	private static final int DELTA_TEMP = TEMPERATURE + 1;
	private static final int DEV_TEMP = DELTA_TEMP + 1;
	
	private static final int HEAT_GEN = DEV_TEMP + 1;
	private static final int HEAT_REQ = HEAT_GEN + 1;
	private static final int HEAT_SURPLUS = HEAT_REQ + 1;
	
	private static final int HEAT_GAIN = HEAT_SURPLUS + 1;
	private static final int HEAT_LOSS = HEAT_GAIN + 1;
	private static final int PRE_NET_HEAT = HEAT_LOSS + 1;
	private static final int POST_NET_HEAT = PRE_NET_HEAT + 1;
	
	private static final int PASSIVE_VENT = POST_NET_HEAT + 1;
	private static final int ACTIVE_VENT = PASSIVE_VENT + 1;
	
	private static final int AIR_HEAT_SINK = ACTIVE_VENT + 1;
	private static final int WATER_HEAT_SINK = AIR_HEAT_SINK + 1;

	private static final int EXCESS_HEAT = WATER_HEAT_SINK + 1;

	
	private static final int COLUMNCOUNT = EXCESS_HEAT + 1;

	private static final String KW_OPEN_PARA = " kW - ";
	private static final String PERCENT_CLOSE_PARA = " %";
	
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
		
		COLUMNS[POWER_MODE] = new ColumnSpec(Msg.getString("BuildingTableModel.column.power.mode"), Object.class);		
		COLUMNS[POWER_REQ]  = new ColumnSpec(Msg.getString("BuildingTableModel.column.power.req"), Double.class);
		COLUMNS[POWER_GEN]  = new ColumnSpec(Msg.getString("BuildingTableModel.column.power.gen"), Double.class);

		COLUMNS[TEMPERATURE] = new ColumnSpec(Msg.getString("BuildingTableModel.column.heat.T"), Double.class);
		COLUMNS[DELTA_TEMP]  = new ColumnSpec(Msg.getString("BuildingTableModel.column.heat.deltaT"), Double.class);
		COLUMNS[DEV_TEMP]  = new ColumnSpec(Msg.getString("BuildingTableModel.column.heat.devT"), Double.class);

		COLUMNS[HEAT_GEN]  = new ColumnSpec(Msg.getString("BuildingTableModel.column.heat.gen"), Double.class);
		COLUMNS[HEAT_REQ] = new ColumnSpec(Msg.getString("BuildingTableModel.column.heat.req"), Double.class);
		COLUMNS[HEAT_SURPLUS] = new ColumnSpec(Msg.getString("BuildingTableModel.column.heat.surplus"), Double.class);
		
		COLUMNS[HEAT_GAIN]  = new ColumnSpec(Msg.getString("BuildingTableModel.column.heat.gain"), Double.class);
		COLUMNS[HEAT_LOSS] = new ColumnSpec(Msg.getString("BuildingTableModel.column.heat.loss"), Double.class);
		
		COLUMNS[PASSIVE_VENT] = new ColumnSpec(Msg.getString("BuildingTableModel.column.heat.vent.passive"), Double.class);
		COLUMNS[ACTIVE_VENT] = new ColumnSpec(Msg.getString("BuildingTableModel.column.heat.vent.active"), Double.class);
		
		COLUMNS[AIR_HEAT_SINK] = new ColumnSpec(Msg.getString("BuildingTableModel.column.heat.air.sink"), Double.class);
		COLUMNS[WATER_HEAT_SINK] = new ColumnSpec(Msg.getString("BuildingTableModel.column.heat.water.sink"), Double.class);
		
		COLUMNS[EXCESS_HEAT] = new ColumnSpec(Msg.getString("BuildingTableModel.column.heat.excess"), Double.class);
		
		COLUMNS[PRE_NET_HEAT] = new ColumnSpec(Msg.getString("BuildingTableModel.column.heat.net.pre"), Double.class);
		COLUMNS[POST_NET_HEAT] = new ColumnSpec(Msg.getString("BuildingTableModel.column.heat.net.post"), Double.class);
	
		COLUMNS[SOLAR] = new ColumnSpec(Msg.getString("BuildingTableModel.column.heat.solar"), Object.class);
		COLUMNS[ELECTRIC] = new ColumnSpec(Msg.getString("BuildingTableModel.column.heat.electric"), Object.class);
		COLUMNS[NUCLEAR]  = new ColumnSpec(Msg.getString("BuildingTableModel.column.heat.nuclear"), Object.class);
		COLUMNS[FUEL]  = new ColumnSpec(Msg.getString("BuildingTableModel.column.heat.fuel"), Object.class);

	}

	/**
	 * Constructor.
	 * 
	 * @param settlement
	 * @throws Exception
	 */
	public BuildingTableModel() {
		super(Msg.getString("BuildingTableModel.nameBuildings", ""),
				"BuildingTableModel.countingBuilding", //$NON-NLS-1$
				COLUMNS);	
		
		setSettlementColumn(SETTLEMENT);
	}

	@Override
	public boolean setSettlementFilter(Set<Settlement> filter) {

		var newBuildings = filter.stream()
				.flatMap(s -> s.getBuildingManager().getBuildingSet().stream())
				.toList();
	
		resetItems(newBuildings);

		return true;
	}

	/**
	 * Returns the value of a Cell.
	 *
	 * @param rowIndex    Row index of the cell.
	 * @param columnIndex Column index of the cell.
	 */
	@Override
	protected Object getItemValue(Building building, int columnIndex) {
		ThermalGeneration furnace = building.getThermalGeneration();
	
		return switch (columnIndex) {
			case NAME -> building.getName();
			case SETTLEMENT -> building.getSettlement().getName();
			case TYPE -> building.getBuildingType();
			case CATEGORY -> building.getCategory().getName();
			case POWER_MODE -> building.getPowerMode() != null ? building.getPowerMode().getName() : null;
			case POWER_REQ -> building.getFullPowerRequired();
			case POWER_GEN -> building.getGeneratedPower();
			case DELTA_TEMP -> furnace != null ? building.getDeltaTemp() : null;
			case DEV_TEMP -> furnace != null ? building.getDevTemp() : null;
			case PASSIVE_VENT -> furnace != null ? building.getPassiveVentHeat() : null;
			case ACTIVE_VENT -> furnace != null ? building.getActiveVentHeat() : null;
			case HEAT_SURPLUS -> furnace != null ? building.getHeatSurplus() : null;
			case HEAT_GEN -> furnace != null ? building.getHeatGenerated() : null;
			case HEAT_REQ -> furnace != null ? building.getHeatRequired() : null;
			case HEAT_GAIN -> furnace != null ? building.getHeatGain() : null;
			case HEAT_LOSS -> furnace != null ? building.getHeatLoss() : null;
			case PRE_NET_HEAT -> furnace != null ? building.getPreNetHeat() : null;
			case POST_NET_HEAT -> furnace != null ? building.getPostNetHeat() : null;
			case AIR_HEAT_SINK -> furnace != null ? building.getAirHeatSink() : null;
			case WATER_HEAT_SINK -> furnace != null ? building.getWaterHeatSink() : null;
			case EXCESS_HEAT -> furnace != null ? building.getExcessHeat() : null;
			case TEMPERATURE -> furnace != null ? building.getCurrentTemperature() : null;
			case SOLAR -> furnace != null ? getHeatSourceGen(HeatSourceType.SOLAR_HEATING, furnace) : null;
			case ELECTRIC -> furnace != null ? getHeatSourceGen(HeatSourceType.ELECTRIC_HEATING, furnace) : null;
			case NUCLEAR -> furnace != null ? getHeatSourceGen(HeatSourceType.THERMAL_NUCLEAR, furnace) : null;
			case FUEL -> furnace != null ? getHeatSourceGen(HeatSourceType.FUEL_HEATING, furnace) : null;
			default -> null;
		};
	}
	
	/**
	 * Gets the string of a heat source to generate heat.
	 * 
	 * @param heatSource
	 * @param furnace
	 * @param heatGen
	 * @return
	 */
	public Object getHeatSourceGen(HeatSourceType heatSourceType, ThermalGeneration furnace) {
		HeatSource heatSource = null;
		HeatMode heatMode = null;
		
		double heatGen = 0;
		double percent = 0;
				
		if (heatSourceType == HeatSourceType.SOLAR_HEATING) {
			
			heatSource = furnace.getSolarHeatSource();
			if (heatSource != null)
				heatMode = heatSource.getHeatMode();
			else
				return null;
		}	
		else if (heatSourceType == HeatSourceType.THERMAL_NUCLEAR) {
	
			heatSource = furnace.getNuclearHeatSource();
			if (heatSource != null)
				heatMode = heatSource.getHeatMode();
			else
				return null;

		}
		else if (heatSourceType == HeatSourceType.ELECTRIC_HEATING) {
			
			heatSource = furnace.getElectricHeatSource();
			if (heatSource != null)
				heatMode = heatSource.getHeatMode();
			else
				return null;

		}
		else if (heatSourceType == HeatSourceType.FUEL_HEATING) {
			
			heatSource = furnace.getFuelHeatSource();
			if (heatSource != null)
				heatMode = heatSource.getHeatMode();
			else
				return null;
		}
		
		if (heatSource != null && heatMode != null) {
			heatGen = Math.round(heatSource.getCurrentHeat() * 100.0)/100.0;
			percent = Math.round(heatMode.getPercentage() * 10.0)/10.0;
		}
		
		if (heatGen == 0 || percent == 0) {
			return null;
		}
		
		return heatGen + KW_OPEN_PARA +  percent + PERCENT_CLOSE_PARA;
	}
	
	
	@Override
	public void destroy() {
		getItems().forEach(s -> s.removeEntityListener(this));
		super.destroy();
	}
	
	/**
	 * Catches unit update event.
	 *
	 * @param event the unit event.
	 */
	@Override
	public void entityUpdate(EntityEvent event) {
		if (event.getSource() instanceof Building building) {
			String eventType = event.getType();

			int columnIndex = getColumnIndexForEventType(eventType);

			if (columnIndex >= 0) {
				entityValueUpdated(building, columnIndex, columnIndex);
			}
		}
	}
	
	/**
	 * Maps event type strings to column indices.
	 * 
	 * @param eventType the event type string
	 * @return the column index, or -1 if not mapped
	 */
	private int getColumnIndexForEventType(String eventType) {
		return switch (eventType) {
			case PowerGrid.POWER_MODE_EVENT -> POWER_MODE;
			case PowerGrid.GENERATED_POWER_EVENT -> POWER_GEN;
			case PowerGrid.REQUIRED_POWER_EVENT -> POWER_REQ;
			case Heating.REQUIRED_HEAT_EVENT -> HEAT_REQ;
			case Heating.GENERATED_HEAT_EVENT -> HEAT_GEN;
			case ThermalGeneration.HEAT_SURPLUS_EVENT -> HEAT_SURPLUS;
			case Heating.NET_HEAT_0_EVENT -> PRE_NET_HEAT;
			case Heating.NET_HEAT_1_EVENT -> POST_NET_HEAT;
			case Heating.TEMPERATURE_EVENT -> TEMPERATURE;
			case Heating.DELTA_T_EVENT -> DELTA_TEMP;
			case Heating.DEV_T_EVENT -> DEV_TEMP;
			case Heating.PASSIVE_VENT_EVENT -> PASSIVE_VENT;
			case Heating.ACTIVE_VENT_EVENT -> ACTIVE_VENT;
			case Heating.HEAT_GAIN_EVENT -> HEAT_GAIN;
			case Heating.HEAT_LOSS_EVENT -> HEAT_LOSS;
			case Heating.AIR_HEAT_SINK_EVENT -> AIR_HEAT_SINK;
			case Heating.WATER_HEAT_SINK_EVENT -> WATER_HEAT_SINK;
			case Heating.EXCESS_HEAT_EVENT -> EXCESS_HEAT;
			case ThermalGeneration.SOLAR_HEAT_EVENT -> SOLAR;
			case ThermalGeneration.ELECTRIC_HEAT_EVENT -> ELECTRIC;
			case ThermalGeneration.NUCLEAR_HEAT_EVENT -> NUCLEAR;
			case ThermalGeneration.FUEL_HEAT_EVENT -> FUEL;
			default -> -1;
		};
	}
}
