package org.mars_sim.msp.ui.standard.tool.mission.create;

import java.util.ArrayList;
import java.util.List;

import org.mars_sim.msp.simulation.Coordinates;
import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.simulation.person.PersonCollection;
import org.mars_sim.msp.simulation.person.ai.mission.CollectIce;
import org.mars_sim.msp.simulation.person.ai.mission.Exploration;
import org.mars_sim.msp.simulation.person.ai.mission.Mission;
import org.mars_sim.msp.simulation.person.ai.mission.MissionException;
import org.mars_sim.msp.simulation.person.ai.mission.MissionManager;
import org.mars_sim.msp.simulation.person.ai.mission.RescueSalvageVehicle;
import org.mars_sim.msp.simulation.person.ai.mission.TravelToSettlement;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.vehicle.Rover;


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
	private Rover rover;
	private PersonCollection members;
	private Settlement destinationSettlement;
	private Rover rescueRover;
	private Coordinates iceCollectionSite;
	private Coordinates[] explorationSites;
	
	void createMission() {
		try {
			Mission mission = null;
			if (TRAVEL_MISSION.equals(type)) 
				mission = new TravelToSettlement(members, startingSettlement, destinationSettlement, rover, description);
			else if (RESCUE_MISSION.equals(type))
				mission = new RescueSalvageVehicle(members, startingSettlement, rescueRover, rover, description);
			else if (ICE_MISSION.equals(type)) {
				List collectionSites = new ArrayList(1);
				collectionSites.add(iceCollectionSite);
				mission = new CollectIce(members, startingSettlement, collectionSites, rover, description);
			}
			else if (EXPLORATION_MISSION.equals(type)) {
				List collectionSites = new ArrayList(explorationSites.length);
				for (int x = 0; x < explorationSites.length; x++) collectionSites.add(explorationSites[x]);
				mission = new Exploration(members, startingSettlement, collectionSites, rover, description);
			}
		
			MissionManager manager = Simulation.instance().getMissionManager();
			manager.addMission(mission);
		}
		catch (MissionException e) {
			e.printStackTrace(System.err);
		}
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
	
	Rover getRover() {
		return rover;
	}
	
	void setRover(Rover rover) {
		this.rover = rover;
	}
	
	PersonCollection getMembers() {
		return members;
	}
	
	void setMembers(PersonCollection members) {
		this.members = members;
	}
	
	Settlement getDestinationSettlement() {
		return destinationSettlement;
	}
	
	void setDestinationSettlement(Settlement destinationSettlement) {
		this.destinationSettlement = destinationSettlement;
	}
	
	Rover getRescueRover() {
		return rescueRover;
	}
	
	void setRescueRover(Rover rescueRover) {
		this.rescueRover = rescueRover;
	}
	
	Coordinates getIceCollectionSite() {
		return iceCollectionSite;
	}
	
	void setIceCollectionSite(Coordinates iceCollectionSite) {
		this.iceCollectionSite = iceCollectionSite;
	}
	
	Coordinates[] getExplorationSites() {
		return explorationSites;
	}
	
	void setExplorationSites(Coordinates[] explorationSites) {
		this.explorationSites = explorationSites;
	}
}