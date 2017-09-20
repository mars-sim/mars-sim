package org.mars_sim.msp.core;

/**
 * Mars Simulation Project
 * UnitEventType.java
 * @version 3.07 2014-12-19
 * @author stpa
 *
 */
public enum UnitEventType {

	// from Crewable.java but was never used
	CREW_CAPACITY_EVENT				("crew capacity event"),

	// from Inventory.java
	INVENTORY_STORING_UNIT_EVENT	("inventory storing unit"),
	INVENTORY_RETRIEVING_UNIT_EVENT	("inventory retrieving unit"),
	INVENTORY_RESOURCE_EVENT		("inventory resource event"),

	// from Unit.java
	NAME_EVENT						("name"),
	DESCRIPTION_EVENT				("description"),
	MASS_EVENT						("mass"),
	LOCATION_EVENT					("location"),
	CONTAINER_UNIT_EVENT			("container unit"),

	// from Settlement.java
	ADD_ASSOCIATED_PERSON_EVENT		("add associated person"),
	REMOVE_ASSOCIATED_PERSON_EVENT	("remove associated person"),

	ADD_ASSOCIATED_ROBOT_EVENT		("add associated robot"),
	REMOVE_ASSOCIATED_ROBOT_EVENT	("remove associated robot"),


	// from PhysicalCondition.java
	FATIGUE_EVENT					("fatigue event"),
	HUNGER_EVENT					("hunger event"),
	STRESS_EVENT					("stress event"),
	PERFORMANCE_EVENT				("performance event"),
	ILLNESS_EVENT					("illness event"),
	DEATH_EVENT						("death event"),
	BURIAL_EVENT					("burial event"),
	RADIATION_EVENT					("radiation event"),

	// from Person.java
	ASSOCIATED_SETTLEMENT_EVENT		("associated settlement"),

	// from MalfunctionManager.java
	MALFUNCTION_EVENT				("malfunction"),

	// from Task.java
	TASK_NAME_EVENT					("task name"),
	TASK_DESCRIPTION_EVENT			("task description"),
	TASK_FUNCTION_EVENT				("task function"),
	TASK_PHASE_EVENT				("task phase"),
	TASK_ENDED_EVENT				("task ended"),
	TASK_SUBTASK_EVENT				("subtask"),

	// from ConstructionManager.java
	START_CONSTRUCTION_SITE_EVENT	("start construction site"),
	START_CONSTRUCTION_WIZARD_EVENT	("start construction wizard"),
	END_CONSTRUCTION_WIZARD_EVENT	("end construction wizard"),
	//START_MANUAL_CONSTRUCTION_WIZARD_EVENT	("start construction event manually"),


	FINISH_CONSTRUCTION_BUILDING_EVENT	("finish building construction"),
	FINISH_CONSTRUCTION_SALVAGE_EVENT	("salvage building construction "),

	// from BuildingManager.java
	ADD_BUILDING_EVENT				("add building"),
	REMOVE_BUILDING_EVENT			("remove building"),


	START_BUILDING_PLACEMENT_EVENT	("start building placement"),
	START_TRANSPORT_WIZARD_EVENT	("start transport wizard"),
	END_TRANSPORT_WIZARD_EVENT		("end transport wizard"),

	FINISH_BUILDING_PLACEMENT_EVENT	("finish building placement"),

	// from Farming.java
	CROP_EVENT						("crop event"),

	ROLE_EVENT						("role event"),

	SHIFT_EVENT						("shift event"),

	// from Mind.java
	JOB_EVENT						("job event"),
	MISSION_EVENT					("mission event"),

	// from GoodsManager.java
	GOODS_VALUE_EVENT				("goods values"),

	// from Vehicle.java
	STATUS_EVENT					("vehicle status"),
	SPEED_EVENT						("vehicle speed"),
	OPERATOR_EVENT					("vehicle operator"),
	EMERGENCY_BEACON_EVENT			("vehicle emergency beacon event"),
	RESERVED_EVENT					("vehicle reserved event"),

	// from TaskManager.java
	TASK_EVENT						("task"),

	// from PowerGrid.java
	POWER_MODE_EVENT				("power mode"),
	GENERATED_POWER_EVENT			("generated power"),
	STORED_POWER_EVENT				("stored power"),
	STORED_POWER_CAPACITY_EVENT		("stored power capacity"),
	REQUIRED_POWER_EVENT			("required power"),
	POWER_VALUE_EVENT				("power value"),

	//2014-10-17 mkung: Added HeatingSystem.java
	HEAT_MODE_EVENT					("heat mode"),
	GENERATED_HEAT_EVENT			("generated heat"),
	STORED_HEAT_EVENT				("stored heat"),
	STORED_HEAT_CAPACITY_EVENT		("stored heat capacity"),
	REQUIRED_HEAT_EVENT				("required heat"),
	HEAT_VALUE_EVENT				("heat value"),
	
	// 2016-10-03 for settlement sensor detection grid
	LOW_DOSE_EVENT					("radiation event"),
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
}
