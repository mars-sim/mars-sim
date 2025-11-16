/*
 * Mars Simulation Project
 * EntityEventType.java
 * @date 2025-11-16
 * @author stpa
 */

package com.mars_sim.core;

public class EntityEventType {

	// For Crewable (but was never used)
	public static final String CREW_CAPACITY_EVENT = "crew capacity event";

	// For Inventory
	public static final String INVENTORY_STORING_UNIT_EVENT = "inventory storing unit";
	public static final String INVENTORY_RETRIEVING_UNIT_EVENT = "inventory retrieving unit";
	public static final String INVENTORY_RESOURCE_EVENT = "inventory resource event";

	// For Unit
	public static final String NAME_EVENT = "name";
	public static final String DESCRIPTION_EVENT = "description";
	public static final String MASS_EVENT = "mass";
	public static final String COORDINATE_EVENT = "coordinate";
	public static final String NOTES_EVENT = "notes";
	public static final String LOCAL_POSITION_EVENT = "local position";
	
	// For Settlement
	public static final String ADD_ASSOCIATED_PERSON_EVENT = "add associated person";
	public static final String REMOVE_ASSOCIATED_PERSON_EVENT = "remove associated person";

	public static final String ADD_ASSOCIATED_ROBOT_EVENT = "add associated robot";
	public static final String REMOVE_ASSOCIATED_ROBOT_EVENT = "remove associated robot";
	
	public static final String ADD_ASSOCIATED_EQUIPMENT_EVENT = "add associated equipment";

	public static final String ADD_ASSOCIATED_BIN_EVENT = "add associated bin";
	public static final String REMOVE_ASSOCIATED_BIN_EVENT = "remove associated bin";
	
	public static final String BACKLOG_EVENT = "backlog event";

	// For PhysicalCondition
	public static final String FATIGUE_EVENT = "fatigue event";
	public static final String HUNGER_EVENT = "hunger event";
	public static final String THIRST_EVENT = "thirst event";
	public static final String STRESS_EVENT = "stress event";
	public static final String EMOTION_EVENT = "emotion event";
	public static final String PERFORMANCE_EVENT = "performance event";
	
	// Others
	public static final String ILLNESS_EVENT = "illness event";
	public static final String DEATH_EVENT = "death event";
	public static final String BURIAL_EVENT = "burial event";
	public static final String REVIVED_EVENT = "revived event";
	public static final String RADIATION_EVENT = "radiation event";
	public static final String METEORITE_EVENT = "meteorite event";
	
	// For MalfunctionManager
	public static final String MALFUNCTION_EVENT = "malfunction";
	
	// For TaskManager
	public static final String TASK_EVENT = "task";

	// For Task
	public static final String TASK_NAME_EVENT = "task name";
	public static final String TASK_DESCRIPTION_EVENT = "task description";
	public static final String TASK_FUNCTION_EVENT = "task function";
	public static final String TASK_PHASE_EVENT = "task phase";
	public static final String TASK_ENDED_EVENT = "task ended";
	public static final String TASK_SUBTASK_EVENT = "subtask";

	// For ConstructionManager
	public static final String START_CONSTRUCTION_SITE_EVENT = "start construction site";
	public static final String START_CONSTRUCTION_WIZARD_EVENT = "start construction wizard";
	public static final String END_CONSTRUCTION_WIZARD_EVENT = "end construction wizard";
	public static final String END_CONSTRUCTION_SITE_EVENT = "end construction site";
	public static final String ADD_CONSTRUCTION_STAGE_EVENT = "add construction stage";
	public static final String REMOVE_CONSTRUCTION_STAGE_EVENT = "remove construction stage";

	public static final String ADD_CONSTRUCTION_WORK_EVENT = "add construction work";
	public static final String ADD_CONSTRUCTION_MATERIALS_EVENT = "add construction materials"; 

	public static final String FINISH_CONSTRUCTION_BUILDING_EVENT = "finish construction";

	// For BuildingManager
	public static final String ADD_BUILDING_EVENT = "add building";
	public static final String REMOVE_BUILDING_EVENT = "remove building";

	public static final String START_BUILDING_PLACEMENT_EVENT = "start building placement";
	public static final String START_TRANSPORT_WIZARD_EVENT = "start transport wizard";
	public static final String END_TRANSPORT_WIZARD_EVENT = "end transport wizard";
	public static final String FINISH_BUILDING_PLACEMENT_EVENT = "finish building placement";

	// For Cooking and PreparingDessert
	public static final String FOOD_EVENT = "food event";
	
	// For Farming
	public static final String CROP_EVENT = "crop event";
	
	// For Role change
	public static final String ROLE_EVENT = "role event";
	
	// For work shift change
	public static final String SHIFT_EVENT = "shift event";

	// For Mind
	public static final String JOB_EVENT = "job event";
	public static final String MISSION_EVENT = "mission event";

	// For Goods and GoodsManager
	public static final String PROJECTED_DEMAND_EVENT = "projected demand of a good";
	public static final String TRADE_DEMAND_EVENT = "trade demand of a good";
	public static final String REPAIR_DEMAND_EVENT = "repair demand of a good";
	public static final String MARKET_VALUE_EVENT = "market value of a good";
	public static final String MARKET_DEMAND_EVENT = "market demand of a good";
	public static final String MARKET_PRICE_EVENT = "market price of a good";
	public static final String MARKET_COST_EVENT = "market cost of a good";
	public static final String VALUE_EVENT = "value of a good";
	public static final String DEMAND_EVENT = "demand of a good";
	public static final String COST_EVENT = "cost of a good";
	public static final String PRICE_EVENT = "price of a good";
	
	public static final String SUPPLY_EVENT = "supply of a good";
	
	// For Vehicle
	public static final String STATUS_EVENT = "vehicle status";
	public static final String SPEED_EVENT = "vehicle speed";
	public static final String OPERATOR_EVENT = "vehicle operator";
	public static final String EMERGENCY_BEACON_EVENT = "vehicle emergency beacon event";
	public static final String RESERVED_EVENT = "vehicle reserved event";

	public static final String CONSUMING_COMPUTING_EVENT = "consuming computing units";
	
	// For power grid
	public static final String POWER_MODE_EVENT = "power mode";
	public static final String GENERATED_POWER_EVENT = "generated power";
	public static final String STORED_ENERGY_EVENT = "stored power";
	public static final String STORED_ENERGY_CAPACITY_EVENT = "stored power capacity";
	public static final String REQUIRED_POWER_EVENT = "required power";
	public static final String POWER_VALUE_EVENT = "power value";
	public static final String BATTERY_EVENT = "battery event";

	// For thermal/heating system
	public static final String HEAT_MODE_EVENT = "heat mode";
	
	public static final String GENERATED_HEAT_EVENT = "generated heat";
	public static final String NET_HEAT_0_EVENT = "net heat 0";
	public static final String NET_HEAT_1_EVENT = "net heat 1";
	public static final String HEAT_GAIN_EVENT = "heat gain";
	public static final String HEAT_LOSS_EVENT = "heat loss";
	public static final String EXCESS_HEAT_EVENT = "excess heat";
	public static final String PASSIVE_VENT_EVENT = "passive vent";
	public static final String ACTIVE_VENT_EVENT = "active vent";
	public static final String HEAT_MATCH_EVENT = "heat match";
	public static final String HEAT_SURPLUS_EVENT = "heat surplus";
	
	public static final String ELECTRIC_HEAT_EVENT = "electric heat";
	public static final String SOLAR_HEAT_EVENT = "solar heat";
	public static final String NUCLEAR_HEAT_EVENT = "nuclear heat";
	public static final String FUEL_HEAT_EVENT = "fuel heat";

	public static final String REQUIRED_HEAT_EVENT = "required heat";
	public static final String HEAT_VALUE_EVENT = "heat value";
	
	public static final String TEMPERATURE_EVENT = "temperature";
	public static final String DELTA_T_EVENT = "delta temperature";
	public static final String DEV_T_EVENT = "dev temperature";
	
	public static final String AIR_HEAT_SINK_EVENT = "air heat sink";
	public static final String WATER_HEAT_SINK_EVENT = "water heat sink";
	
	// For settlement sensor detection grid
	public static final String BASELINE_EVENT = "baseline radiation event";
	public static final String SEP_EVENT = "SEP event";
	public static final String GCR_EVENT = "GCR event";

	// Event for Manufacturing
	public static final String MANU_QUEUE_ADD = "add to manufacturing queue";
	public static final String MANU_QUEUE_REMOVE = "remove from manufacturing queue";
	public static final String MANE_QUEUE_REFRESH = "refresh manufacturing queue";
	
	// Private constructor to prevent instantiation
	private EntityEventType() {
		throw new UnsupportedOperationException("Utility class");
	}
}
