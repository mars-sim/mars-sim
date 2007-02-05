package org.mars_sim.msp.ui.standard.tool.mission.create_wizard;

import org.mars_sim.msp.simulation.person.PersonCollection;
import org.mars_sim.msp.simulation.person.ai.mission.CollectIce;
import org.mars_sim.msp.simulation.person.ai.mission.Exploration;
import org.mars_sim.msp.simulation.person.ai.mission.MissionException;
import org.mars_sim.msp.simulation.person.ai.mission.RescueSalvageVehicle;
import org.mars_sim.msp.simulation.person.ai.mission.TravelToSettlement;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.vehicle.Vehicle;

class MissionDataBean {

	// Mission type strings.
	final static String TRAVEL_MISSION = "Travel to Settlement";
	final static String EXPLORATION_MISSION = "Exploration";
	final static String ICE_MISSION = "Ice Prospecting";
	final static String RESCUE_MISSION = "Rescue/Salvage Vehicle";

	// Data members.
	private String type;
	private String description;
	private Settlement startingSettlement;
	private Vehicle vehicle;
	private PersonCollection members;
	
	void createMission() throws MissionException {
		
	}
	
	static final String[] getMissionTypes() {
		String[] result = { TRAVEL_MISSION, EXPLORATION_MISSION, ICE_MISSION, RESCUE_MISSION };
		return result;
	}
	
	static final String getMissionDescription(String missionType) {
		String result = "";
		if (missionType.equals(TRAVEL_MISSION)) result = TravelToSettlement.DEFAULT_DESCRIPTION;
		else if (missionType.equals(EXPLORATION_MISSION)) result = Exploration.DEFAULT_DESCRIPTION;
		else if (missionType.equals(ICE_MISSION)) result = CollectIce.DEFAULT_DESCRIPTION;
		else if (missionType.equals(RESCUE_MISSION)) result = RescueSalvageVehicle.DEFAULT_DESCRIPTION;
		return result;
	}
	
	String getType() {
		return type;
	}
	
	void setType(String type) {
		this.type = type;
	}
	
	String getDescription() {
		return description;
	}
	
	void setDescription(String description) {
		this.description = description;
	}
	
	Settlement getStartingSettlement() {
		return startingSettlement;
	}
	
	void setStartingSettlement(Settlement startingSettlement) {
		this.startingSettlement = startingSettlement;
	}
	
	Vehicle getVehicle() {
		return vehicle;
	}
	
	void setVehicle(Vehicle vehicle) {
		this.vehicle = vehicle;
	}
	
	PersonCollection getMembers() {
		return members;
	}
	
	void setMembers(PersonCollection members) {
		this.members = members;
	}
}