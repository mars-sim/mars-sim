/**
 * Mars Simulation Project
 * MissionStatus.java
 * @version 3.1.0 2019-10-12
 * @author Manny Kung
 */

package org.mars_sim.msp.core.person.ai.mission;

public enum MissionStatus {
	
	MISSION_ACCOMPLISHED 							("Mission Accomplished"),
	
	MISSION_NOT_APPROVED 							("Mission not approved"),
	
	CURRENT_MISSION_PHASE_IS_NULL 					("Current mission phase is null"),	

	USER_ABORTED_MISSION 							("Mission aborted by user"),
	
	MISSION_ABORTED									("Mission aborted"),
	
	UNREPAIRABLE_MALFUNCTION  						("Unrepairable malfunction"),
	
	NOT_ENOUGH_RESOURCES 							("Not enough resources"),
	
	NO_METHANE										("No more methane"),
	
	NO_EMERGENCY_SETTLEMENT_DESTINATION_FOUND 		("No emergency settlement destination found"),
	MEDICAL_EMERGENCY 								("A member has a medical emergency"),
	NO_TRADING_SETTLEMENT 							("No trading settlement found"),
	
	EVA_SUIT_CANNOT_BE_LOADED						("EVA suit cannot be loaded"),
	NO_GOOD_EVA_SUIT 								("No good EVA suit"),
	REQUEST_RESCUE 									("Requesting rescue"),
	NO_ONGOING_SCIENTIFIC_STUDY 					("No on-going scientific study being conducted in this subject"),

	NO_EXPLORATION_SITES 							("Exploration sites could not be determined"),
	
	NO_ICE_COLLECTION_SITES 						("No ice collection sites found"),
	
	NOT_ENOUGH_MEMBERS 								("Not enough members recruited"),
	NO_MEMBERS_ON_MISSION 							("No members available for mission"),
	
	CONSTRUCTION_ENDED 								("Construction ended."),
	CONSTRUCTION_SITE_NOT_FOUND_OR_CREATED			("Construction site NOT found or created"),
	NEW_CONSTRUCTION_STAGE_NOT_DETERMINED			("New construction stage could not be determined"),
	CONSTRUCTION_STAGE_NOT_CREATED					("Construction stage could not be created"),
	
	SALVAGE_CONSTRUCTION_STAGE_NOT_FOUND			("Salvage construction stage NOT found"),
	SALVAGE_CONSTRUCTION_SITE_NOT_FOUND_OR_CREATED	("Salvage construction site NOT found or created"),
	BUILDING_SALVAGE_SUCCESSFULLY_ENDED				("Building salvage successfully ended"),
	
	CONSTRUCTION_ATTACHMENT_PART_NOT_RETRIEVED		("Construction attachment part NOT retrieved"),

	CONSTRUCTION_VEHICLE_NOT_RETRIEVED				("Construction vehicle NOT retrieved"),
	LUV_NOT_RETRIEVED								("LUV NOT retrieved"),
	NO_RESERVABLE_VEHICLES 							("No reservable vehicles"),
	NO_AVAILABLE_VEHICLES 							("No available vehicles"),
	VEHICLE_NOT_LOADABLE 							("Cannot load resources into the rover"),
	TARGET_VEHICLE_NOT_FOUND 						("Target vehicle not found"),
	LUV_NOT_AVAILABLE 								("LUV NOT available"),
	LUV_ATTACHMENT_PARTS_NOT_LOADABLE 				("LUV and/or its attachment parts could not be loaded"),
	
	COULD_NOT_ESTIMATE_TRADE_PROFIT					("Could not estimate trade profit"),
	NO_INHABITABLE_BUILDING							("No inhabitable buildings"),
	SELLING_VEHICLE_NOT_AVAILABLE_FOR_TRADE			("Selling vehicle NOT available for trade"),
	CANNOT_ENTER_ROVER								("Cannot enter rover"),

	DESTINATION_IS_NULL								("Destination is null"),
	NO_SETTLEMENT_FOUND_TO_DELIVER_EMERGENCY_SUPPLIES ("No settlement found to deliver emergency supplies"),
	
	MINING_SITE_NOT_BE_DETERMINED					("Mining site could not be determined"),
	;
	

	private String name;

	private MissionStatus(String name) {
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
