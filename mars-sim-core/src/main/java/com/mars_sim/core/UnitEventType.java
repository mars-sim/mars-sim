/*
 * Mars Simulation Project
 * UnitEventType.java
 * @date 2024-07-03
 * @author stpa
 */

package com.mars_sim.core;

public enum UnitEventType {

	// For Crewable (but was never used)
	CREW_CAPACITY_EVENT				("crew capacity event"),

	// For Inventory
	INVENTORY_STORING_UNIT_EVENT	("inventory storing unit"),
	INVENTORY_RETRIEVING_UNIT_EVENT	("inventory retrieving unit"),
	INVENTORY_RESOURCE_EVENT		("inventory resource event"),

	// For Unit
	NAME_EVENT						("name"),
	DESCRIPTION_EVENT				("description"),
	MASS_EVENT						("mass"),
	COORDINATE_EVENT				("coordinate"),
	NOTES_EVENT						("notes"),
	LOCAL_POSITION_EVENT			("local position"),
	
	// For Settlement
	ADD_ASSOCIATED_PERSON_EVENT		("add associated person"),
	REMOVE_ASSOCIATED_PERSON_EVENT	("remove associated person"),

	ADD_ASSOCIATED_ROBOT_EVENT		("add associated robot"),
	REMOVE_ASSOCIATED_ROBOT_EVENT	("remove associated robot"),
	
	ADD_ASSOCIATED_EQUIPMENT_EVENT		("add associated equipment"),

	ADD_ASSOCIATED_BIN_EVENT			("add associated bin"),
	REMOVE_ASSOCIATED_BIN_EVENT			("remove associated bin"),
	
	BACKLOG_EVENT						("backlog event"),

	// For PhysicalCondition
	FATIGUE_EVENT					("fatigue event"),
	HUNGER_EVENT					("hunger event"),
	THIRST_EVENT					("thirst event"),
	STRESS_EVENT					("stress event"),
	EMOTION_EVENT					("emotion event"),
	PERFORMANCE_EVENT				("performance event"),
	
	// Others
	ILLNESS_EVENT					("illness event"),
	DEATH_EVENT						("death event"),
	BURIAL_EVENT					("burial event"),
	REVIVED_EVENT					("revived event"),
	RADIATION_EVENT					("radiation event"),
	METEORITE_EVENT					("meteorite event"),
	
	// For MalfunctionManager
	MALFUNCTION_EVENT				("malfunction"),
	
	// For TaskManager
	TASK_EVENT						("task"),

	// For Task
	TASK_NAME_EVENT					("task name"),
	TASK_DESCRIPTION_EVENT			("task description"),
	TASK_FUNCTION_EVENT				("task function"),
	TASK_PHASE_EVENT				("task phase"),
	TASK_ENDED_EVENT				("task ended"),
	TASK_SUBTASK_EVENT				("subtask"),

	// For ConstructionManager
	START_CONSTRUCTION_SITE_EVENT	("start construction site"),
	START_CONSTRUCTION_WIZARD_EVENT	("start construction wizard"),
	END_CONSTRUCTION_WIZARD_EVENT	("end construction wizard"),
	END_CONSTRUCTION_SITE_EVENT     ("end construction site"),
	ADD_CONSTRUCTION_STAGE_EVENT	("add construction stage"),
	REMOVE_CONSTRUCTION_STAGE_EVENT	("remove construction stage"),

	ADD_CONSTRUCTION_WORK_EVENT		("add construction work"),
	ADD_CONSTRUCTION_MATERIALS_EVENT("add construction materials"), 

	FINISH_CONSTRUCTION_BUILDING_EVENT	("finish construction"),

	// For BuildingManager
	ADD_BUILDING_EVENT				("add building"),
	REMOVE_BUILDING_EVENT			("remove building"),

	START_BUILDING_PLACEMENT_EVENT	("start building placement"),
	START_TRANSPORT_WIZARD_EVENT	("start transport wizard"),
	END_TRANSPORT_WIZARD_EVENT		("end transport wizard"),
	FINISH_BUILDING_PLACEMENT_EVENT	("finish building placement"),

	// For Cooking and PreparingDessert
	FOOD_EVENT						("food event"),
	
	// For Farming
	CROP_EVENT						("crop event"),
	
	// For Role change
	ROLE_EVENT						("role event"),
	
	// For work shift change
	SHIFT_EVENT						("shift event"),

	// For Mind
	JOB_EVENT						("job event"),
	MISSION_EVENT					("mission event"),

	// For Goods and GoodsManager
	MARKET_VALUE_EVENT				("market value of a good"),
	MARKET_DEMAND_EVENT				("market demand of a good"),
	MARKET_PRICE_EVENT				("market price of a good"),
	MARKET_COST_EVENT				("market cost of a good"),
	VALUE_EVENT						("value of a good"),
	DEMAND_EVENT					("demand of a good"),
	COST_EVENT						("cost of a good"),
	PRICE_EVENT						("price of a good"),
	
	SUPPLY_EVENT					("supply of a good"),
	
	// For Vehicle
	STATUS_EVENT					("vehicle status"),
	SPEED_EVENT						("vehicle speed"),
	OPERATOR_EVENT					("vehicle operator"),
	EMERGENCY_BEACON_EVENT			("vehicle emergency beacon event"),
	RESERVED_EVENT					("vehicle reserved event"),

	CONSUMING_COMPUTING_EVENT		("consuming computing units"),
	
	// For power grid
	POWER_MODE_EVENT				("power mode"),
	GENERATED_POWER_EVENT			("generated power"),
	STORED_ENERGY_EVENT				("stored power"),
	STORED_ENERGY_CAPACITY_EVENT	("stored power capacity"),
	REQUIRED_POWER_EVENT			("required power"),
	POWER_VALUE_EVENT				("power value"),
	BATTERY_EVENT					("battery event"),

	// For thermal/heating system
	HEAT_MODE_EVENT					("heat mode"),
	
	GENERATED_HEAT_EVENT			("generated heat"),
	NET_HEAT_0_EVENT				("net heat 0"),
	NET_HEAT_1_EVENT				("net heat 1"),
	HEAT_GAIN_EVENT					("heat gain"),
	HEAT_LOSS_EVENT					("heat loss"),
	EXCESS_HEAT_EVENT				("excess heat"),
	PASSIVE_VENT_EVENT				("passive vent"),
	ACTIVE_VENT_EVENT				("active vent"),
	HEAT_MATCH_EVENT				("heat match"),
	HEAT_SURPLUS_EVENT				("heat surplus"),
	
	ELECTRIC_HEAT_EVENT				("electric heat"),
	SOLAR_HEAT_EVENT				("solar heat"),
	NUCLEAR_HEAT_EVENT				("nuclear heat"),
	FUEL_HEAT_EVENT					("fuel heat"),

	REQUIRED_HEAT_EVENT				("required heat"),
	HEAT_VALUE_EVENT				("heat value"),
	
	TEMPERATURE_EVENT				("temperature"),
	DELTA_T_EVENT					("delta temperature"),
	DEV_T_EVENT						("dev temperature"),
	
	AIR_HEAT_SINK_EVENT				("air heat sink"),
	WATER_HEAT_SINK_EVENT			("water heat sink"),
	
	// For settlement sensor detection grid
	BASELINE_EVENT					("baseline radiation event"),
	SEP_EVENT						("SEP event"),	
	GCR_EVENT						("GCR event"),

	// Event for Manufacturing
	MANU_QUEUE_ADD			("add to manufacturing queue"),
	MANU_QUEUE_REMOVE		("remove from manufacturing queue"),
	MANE_QUEUE_REFRESH		("refresh manufacturing queue")
	;
	
	private String name;

	private UnitEventType(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	@Override
	public String toString() {
		return this.name;
	}
	
}
