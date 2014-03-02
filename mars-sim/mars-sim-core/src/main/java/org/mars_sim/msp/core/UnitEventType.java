package org.mars_sim.msp.core;

/**
 * @author stpa
 */
public enum UnitEventType {

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

	// from PhysicalCondition.java
	FATIGUE_EVENT					("fatigue event"),
	HUNGER_EVENT					("hunger event"),
	STRESS_EVENT					("stress event"),
	PERFORMANCE_EVENT				("performance event"),
	ILLNESS_EVENT					("illness event"),
	DEATH_EVENT						("death event"),

	// from Person.java
	ASSOCIATED_SETTLEMENT_EVENT		("associated settlement"),

	// from MalfunctionManager.java
	MALFUNCTION_EVENT				("malfunction"),

	// from Task.java
	TASK_NAME_EVENT					("task name"),
	TASK_DESC_EVENT					("task name"),
	TASK_PHASE_EVENT				("task name"),
	TASK_ENDED_EVENT				("task ended"),
	TASK_SUBTASK_EVENT				("subtask"),

	// from ConstructionManager.java
	START_CONSTRUCTION_SITE_EVENT	("start construction site"),
	FINISH_BUILDING_EVENT			("finish building"),
	FINISH_SALVAGE_EVENT			("salvage building"),

	// from BuildingManager.java
	ADD_BUILDING_EVENT				("add building"),
	REMOVE_BUILDING_EVENT			("remove building"),

	// from Farming.java
	CROP_EVENT						("crop event"),

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
	POWER_VALUE_EVENT				("power value");

	private String name;

	private UnitEventType(String name) {
		this.name = name;
	}

	public String getName() {
		// TODO change all names to i18n-keys for accessing messages.properties
		return this.name;
	}
}
