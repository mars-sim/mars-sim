/**
 * Mars Simulation Project
 * UnitEventType.java
 * @version 3.1.0 2017-11-06
 * @author stpa
 */

package org.mars_sim.msp.core;

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
	LOCATION_EVENT					("location"),
	CONTAINER_UNIT_EVENT			("container unit"),
	NOTES_EVENT						("notes"),
	
	// For Settlement
	ADD_ASSOCIATED_PERSON_EVENT		("add associated person"),
	REMOVE_ASSOCIATED_PERSON_EVENT	("remove associated person"),

	ADD_ASSOCIATED_ROBOT_EVENT		("add associated robot"),
	REMOVE_ASSOCIATED_ROBOT_EVENT	("remove associated robot"),
	
	ADD_ASSOCIATED_EQUIPMENT_EVENT		("add associated equipment"),
	REMOVE_ASSOCIATED_EQUIPMENT_EVENT	("remove associated equipment"),
	
//	ASSOCIATED_SETTLEMENT_EVENT		("associated settlement"),

	// For PhysicalCondition
	FATIGUE_EVENT					("fatigue event"),
	HUNGER_EVENT					("hunger event"),
	THIRST_EVENT					("thirst event"),
	STRESS_EVENT					("stress event"),
	EMOTION_EVENT					("emotion event"),
	PERFORMANCE_EVENT				("performance event"),
	ILLNESS_EVENT					("illness event"),
	DEATH_EVENT						("death event"),
	BURIAL_EVENT					("burial event"),
	RADIATION_EVENT					("radiation event"),


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
	//START_MANUAL_CONSTRUCTION_WIZARD_EVENT	("start construction event manually"),

	FINISH_CONSTRUCTION_BUILDING_EVENT	("finish building construction"),
	FINISH_CONSTRUCTION_SALVAGE_EVENT	("salvage building construction "),

	// For BuildingManager
	ADD_BUILDING_EVENT				("add building"),
	REMOVE_BUILDING_EVENT			("remove building"),

	START_BUILDING_PLACEMENT_EVENT	("start building placement"),
	START_TRANSPORT_WIZARD_EVENT	("start transport wizard"),
	END_TRANSPORT_WIZARD_EVENT		("end transport wizard"),
	FINISH_BUILDING_PLACEMENT_EVENT	("finish building placement"),

	// For Farming
	CROP_EVENT						("crop event"),
	ROLE_EVENT						("role event"),
	SHIFT_EVENT						("shift event"),

	// For Mind
	JOB_EVENT						("job event"),
	MISSION_EVENT					("mission event"),

	// For GoodsManager
	GOODS_VALUE_EVENT				("goods values"),

	// For Vehicle
	STATUS_EVENT					("vehicle status"),
	SPEED_EVENT						("vehicle speed"),
	OPERATOR_EVENT					("vehicle operator"),
	EMERGENCY_BEACON_EVENT			("vehicle emergency beacon event"),
	RESERVED_EVENT					("vehicle reserved event"),

	// For power grid
	POWER_MODE_EVENT				("power mode"),
	GENERATED_POWER_EVENT			("generated power"),
	STORED_POWER_EVENT				("stored power"),
	STORED_POWER_CAPACITY_EVENT		("stored power capacity"),
	REQUIRED_POWER_EVENT			("required power"),
	POWER_VALUE_EVENT				("power value"),

	// For thermal/heating system
	HEAT_MODE_EVENT					("heat mode"),
	GENERATED_HEAT_EVENT			("generated heat"),
	STORED_HEAT_EVENT				("stored heat"),
	STORED_HEAT_CAPACITY_EVENT		("stored heat capacity"),
	REQUIRED_HEAT_EVENT				("required heat"),
	HEAT_VALUE_EVENT				("heat value"),
	
	// For settlement sensor detection grid
	BASELINE_EVENT					("baseline radiation event"),
	SEP_EVENT						("SEP event"),	
	GCR_EVENT						("GCR event"),
	;
	
	private String name;

	private UnitEventType(String name) {
		this.name = name;
	}

	public String getName() {
		// TODO change all names to i18n-keys for accessing messages.properties
		return this.name;
	}

	public String toString() {
		// TODO change all names to i18n-keys for accessing messages.properties
		return this.name;
	}
	
}
