package org.mars_sim.msp.core.person;

/**
 * @author stpa
 * 2014-03-21
 */
public enum EventType {

	MALFUNCTION_FIXED	("Malfunction fixed"),
	MALFUNCTION_UNFIXED	("Malfunction occurred"),

	MEDICAL_CURED		("Illness Cured"),
	MEDICAL_STARTS		("Illness Starts"),
	MEDICAL_DEGRADES	("Illness Degrades"),
	MEDICAL_RECOVERY	("Illness Recovering"),
	MEDICAL_TREATED		("Illness Treated"),
	MEDICAL_DEATH		("Person Dies"),

	MISSION_START					("Mission Started"),
	MISSION_JOINING					("Mission Joined"),
	MISSION_FINISH					("Mission Finished"),
	MISSION_DEVELOPMENT				("Mission Development"),
	MISSION_EMERGENCY_DESTINATION	("Changing To Emergency Destination"),
	MISSION_EMERGENCY_BEACON		("Emergency Beacon Turned On"),
	MISSION_RENDEZVOUS				("Rescue/Salvage Mission Rendezvous with Target Vehicle"),
	MISSION_SALVAGE_VEHICLE			("Salvage Vehicle"),
	MISSION_RESCUE_PERSON			("Rescue Person"),

	TASK_START			("Task Starting"),
	TASK_FINISH			("Task Finished"),
	TASK_DEVELOPMENT	("Task Development"),

	TRANSPORT_ITEM_CREATED		("Transport Item Created"),
	TRANSPORT_ITEM_CANCELLED	("Transport Item Canceled"),
	TRANSPORT_ITEM_LAUNCHED		("Transport Item Launched"),
	TRANSPORT_ITEM_ARRIVED		("Transport Item Arrived"),
	TRANSPORT_ITEM_MODIFIED		("Transport Item Modified"),
	;

	private String name;

	/** hidden constructor. */
	private EventType(String name) {
		this.name = name;
	}

	public final String getName() {
		return this.name;
	}

	@Override
	public final String toString() {
		return getName();
	}
}
