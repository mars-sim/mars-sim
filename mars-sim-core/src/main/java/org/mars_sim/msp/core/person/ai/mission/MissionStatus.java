/**
 * Mars Simulation Project
 * MissionStatus.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */

package org.mars_sim.msp.core.person.ai.mission;

public enum MissionStatus {
	
	MISSION_ACCOMPLISHED 							("Mission Accomplished"),
	MISSION_ABORTED									("Mission aborted"),
	MISSION_NOT_APPROVED 							("Mission not approved"),
	
	CURRENT_MISSION_PHASE_IS_NULL 					("Current mission phase is null"),	
	ABORTED_MISSION 							("Mission aborted by user"),
	
	UNREPAIRABLE_MALFUNCTION  						("Unrepairable malfunction"),
	
	NOT_ENOUGH_RESOURCES 							("Not enough resources"),
	NOT_ENOUGH_MEMBERS 								("Not enough members recruited"),
	
	NO_MEMBERS_AVAILABLE 							("No members available for mission"),
	NO_METHANE										("No more methane"),
	NO_OXYGEN 										("No more oxygen"),
	NO_EMERGENCY_SETTLEMENT_DESTINATION_FOUND 		("No emergency settlement destination found"),
	NO_TRADING_SETTLEMENT 							("No trading settlement found"),
	NO_ONGOING_SCIENTIFIC_STUDY 					("No on-going scientific study being conducted in this subject"),
	NO_EXPLORATION_SITES 							("Exploration sites could not be determined"),
	NO_ICE_COLLECTION_SITES 						("No ice collection sites found"),
	NO_GOOD_EVA_SUIT 								("No good EVA suit"),
	
	MEDICAL_EMERGENCY 								("A member has a medical emergency"),
	
	EVA_SUIT_CANNOT_BE_LOADED						("EVA suit cannot be loaded"),

	REQUEST_RESCUE 									("Requesting rescue"),

	// Construction
	CONSTRUCTION_ENDED 								("Construction ended."),
	CONSTRUCTION_SITE_NOT_FOUND_OR_CREATED			("Construction site NOT found or created"),
	NEW_CONSTRUCTION_STAGE_NOT_DETERMINED			("New construction stage could not be determined"),
	CONSTRUCTION_STAGE_NOT_CREATED					("Construction stage could not be created"),	
	SALVAGE_CONSTRUCTION_STAGE_NOT_FOUND			("Salvage construction stage NOT found"),
	SALVAGE_CONSTRUCTION_SITE_NOT_FOUND_OR_CREATED	("Salvage construction site NOT found or created"),
	BUILDING_SALVAGE_SUCCESSFULLY_ENDED				("Building salvage successfully ended"),
	
	// Vehicle related
	CONSTRUCTION_VEHICLE_NOT_RETRIEVED				("Construction vehicle NOT retrieved"),
	CONSTRUCTION_ATTACHMENT_PART_NOT_RETRIEVED		("Construction attachment part NOT retrieved"),
	LUV_NOT_RETRIEVED								("LUV NOT retrieved"),
	LUV_NOT_AVAILABLE 								("LUV NOT available"),
	LUV_ATTACHMENT_PARTS_NOT_LOADABLE 				("LUV and/or its attachment parts could not be loaded"),
	NO_RESERVABLE_VEHICLES 							("No reservable vehicles"),
	NO_AVAILABLE_VEHICLES 							("No available vehicles"),
	VEHICLE_UNDER_MAINTENANCE						("Vehicle under maintenance"),
	CANNOT_LOAD_RESOURCES 							("Cannot load resources into the rover"),
	TARGET_VEHICLE_NOT_FOUND 						("Target vehicle not found"),
	SELLING_VEHICLE_NOT_AVAILABLE_FOR_TRADE			("Selling vehicle NOT available for trade"),
	CANNOT_ENTER_ROVER								("Cannot enter rover"),
	VEHICLE_BEACON_ACTIVE							("Vehicle beacon active"),

	// Trade related
	COULD_NOT_ESTIMATE_TRADE_PROFIT					("Could not estimate trade profit"),
	
	// Settlement related
	NO_INHABITABLE_BUILDING							("No inhabitable buildings"),
	DESTINATION_IS_NULL								("Destination is null"),
	NO_SETTLEMENT_FOUND_TO_DELIVER_EMERGENCY_SUPPLIES ("No settlement found to deliver emergency supplies"),
	COULD_NOT_EXIT_SETTLEMENT						("Could not exit settlement"),
	
	// Site related
	MINING_SITE_NOT_BE_DETERMINED					("Mining site could not be determined"),
	;
	

	private String name;

	private MissionStatus(String name) {
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
