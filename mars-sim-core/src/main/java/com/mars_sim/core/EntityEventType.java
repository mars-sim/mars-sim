/*
 * Mars Simulation Project
 * EntityEventType.java
 * @date 2025-11-16
 * @author stpa
 */

package com.mars_sim.core;

public class EntityEventType {

	// For Inventory
	public static final String INVENTORY_STORING_UNIT_EVENT = "inventory storing unit";
	public static final String INVENTORY_RETRIEVING_UNIT_EVENT = "inventory retrieving unit";
	public static final String INVENTORY_RESOURCE_EVENT = "inventory resource event";

	// For Unit
	public static final String NAME_EVENT = "name";
	public static final String DESCRIPTION_EVENT = "description";
	public static final String MASS_EVENT = "mass";
	public static final String COORDINATE_EVENT = "coordinate";
	public static final String LOCAL_POSITION_EVENT = "local position";
	
	// For Settlement
	public static final String ADD_ASSOCIATED_PERSON_EVENT = "add associated person";
	public static final String REMOVE_ASSOCIATED_PERSON_EVENT = "remove associated person";

	public static final String ADD_ASSOCIATED_ROBOT_EVENT = "add associated robot";
	public static final String REMOVE_ASSOCIATED_ROBOT_EVENT = "remove associated robot";
	
	public static final String ADD_ASSOCIATED_EQUIPMENT_EVENT = "add associated equipment";

	public static final String ADD_ASSOCIATED_BIN_EVENT = "add associated bin";
	public static final String REMOVE_ASSOCIATED_BIN_EVENT = "remove associated bin";
	
	public static final String EMOTION_EVENT = "emotion event";
	public static final String PERFORMANCE_EVENT = "performance event";
	
	// Others
	public static final String ILLNESS_EVENT = "illness event";
	public static final String DEATH_EVENT = "death event";
	public static final String BURIAL_EVENT = "burial event";
	public static final String REVIVED_EVENT = "revived event";
	public static final String RADIATION_EVENT = "radiation event";
	public static final String METEORITE_EVENT = "meteorite event";
	
	// For Task
	public static final String TASK_NAME_EVENT = "task name";
	public static final String TASK_DESCRIPTION_EVENT = "task description";
	public static final String TASK_FUNCTION_EVENT = "task function";
	public static final String TASK_PHASE_EVENT = "task phase";
	public static final String TASK_ENDED_EVENT = "task ended";
	public static final String TASK_SUBTASK_EVENT = "subtask";

	// For BuildingManager
	public static final String ADD_BUILDING_EVENT = "add building";
	public static final String REMOVE_BUILDING_EVENT = "remove building";

	public static final String START_BUILDING_PLACEMENT_EVENT = "start building placement";

	// For Cooking and PreparingDessert
	public static final String FOOD_EVENT = "food event";
	
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

	public static final String BATTERY_EVENT = "battery event";

	// For thermal/heating system
	public static final String HEAT_MODE_EVENT = "heat mode";
	
	public static final String HEAT_VALUE_EVENT = "heat value";
	
	// For settlement sensor detection grid
	public static final String BASELINE_EVENT = "baseline radiation event";
	public static final String SEP_EVENT = "SEP event";
	public static final String GCR_EVENT = "GCR event";

	// Private constructor to prevent instantiation
	private EntityEventType() {
		throw new UnsupportedOperationException("Utility class");
	}
}
