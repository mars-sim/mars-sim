/**
 * Mars Simulation Project
 * MissionEventType.java
 * @version 3.1.0 2018-10-10
 * @author stpa
 */
package org.mars_sim.msp.core.person.ai.mission;

public enum MissionEventType {

	// from Mission.java
	DATE_EVENT					("date"),
	NAME_EVENT					("name"),
	TYPE_EVENT					("type"),
	DESCRIPTION_EVENT			("description"),
	DESIGNATION_EVENT			("designation"),
	PHASE_EVENT					("phase"),
	PHASE_DESCRIPTION_EVENT		("phase description"),
	MIN_MEMBERS_EVENT           ("minimum members"),
//	MIN_PEOPLE_EVENT			("minimum people"),
//	MIN_ROBOTS_EVENT			("minimum robots"),
	ASSOCIATED_SETTLEMENT_EVENT	("associated settlement"),
	CAPACITY_EVENT				("capacity"),
	ADD_MEMBER_EVENT			("add member"),
	REMOVE_MEMBER_EVENT			("remove member"),
	END_MISSION_EVENT			("end mission"),

	// from Exploration.java
	SITE_EXPLORATION_EVENT		("explore site"),

	// from Mining.java
	EXCAVATE_MINERALS_EVENT		("excavate minerals"),
	COLLECT_MINERALS_EVENT		("collect minerals"),

	// from TradeMission.java
	TRAVEL_STATUS_EVENT			("travel status"),
	NAVPOINTS_EVENT				("navpoints"),
	DISTANCE_EVENT				("distance"),

	// from VehicleMission.java
	VEHICLE_EVENT				("vehicle"),
	OPERATOR_EVENT				("operator"),

	// from RoverMission.java
	STARTING_SETTLEMENT_EVENT	("starting settlement"),

	// from Trade.java
	BUY_LOAD_EVENT				("buy load"),

	// from TravelToSettlement.java
    DESTINATION_SETTLEMENT		("destination settlement");

	private String name;

	private MissionEventType(String name) {
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
