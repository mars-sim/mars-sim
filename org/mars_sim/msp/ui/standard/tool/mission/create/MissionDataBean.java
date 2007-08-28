/**
 * Mars Simulation Project
 * MissionDataBean.java
 * @version 2.81 2007-08-27
 * @author Scott Davis
 */

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

/**
 * Mission data holder bean.
 */
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
	
	/**
	 * Creates a mission from the mission data.
	 */
	void createMission() {
		try {
			Mission mission = null;
			if (TRAVEL_MISSION.equals(type)) 
				mission = new TravelToSettlement(members, startingSettlement, destinationSettlement, rover, description);
			else if (RESCUE_MISSION.equals(type))
				mission = new RescueSalvageVehicle(members, startingSettlement, rescueRover, rover, description);
			else if (ICE_MISSION.equals(type)) {
				List<Coordinates> collectionSites = new ArrayList<Coordinates>(1);
				collectionSites.add(iceCollectionSite);
				mission = new CollectIce(members, startingSettlement, collectionSites, rover, description);
			}
			else if (EXPLORATION_MISSION.equals(type)) {
				List<Coordinates> collectionSites = new ArrayList<Coordinates>(explorationSites.length);
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
	
	/**
	 * Gets mission types.
	 * @return array of mission type strings.
	 */
	static final String[] getMissionTypes() {
		String[] result = { TRAVEL_MISSION, EXPLORATION_MISSION, ICE_MISSION, RESCUE_MISSION };
		return result;
	}
	
	/**
	 * Gets mission description based on a mission type.
	 * @param missionType the mission type.
	 * @return the mission description.
	 */
	static final String getMissionDescription(String missionType) {
		String result = "";
		if (missionType.equals(TRAVEL_MISSION)) result = TravelToSettlement.DEFAULT_DESCRIPTION;
		else if (missionType.equals(EXPLORATION_MISSION)) result = Exploration.DEFAULT_DESCRIPTION;
		else if (missionType.equals(ICE_MISSION)) result = CollectIce.DEFAULT_DESCRIPTION;
		else if (missionType.equals(RESCUE_MISSION)) result = RescueSalvageVehicle.DEFAULT_DESCRIPTION;
		return result;
	}
	
	/**
	 * Gets the mission type.
	 * @return type.
	 */
	String getType() {
		return type;
	}
	
	/**
	 * Sets the mission type.
	 * @param type the mission type.
	 */
	void setType(String type) {
		this.type = type;
	}
	
	/**
	 * Gets the mission description.
	 * @return description.
	 */
	String getDescription() {
		return description;
	}
	
	/**
	 * Sets the mission description.
	 * @param description the mission description.
	 */
	void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * Gets the starting settlement.
	 * @return settlement.
	 */
	Settlement getStartingSettlement() {
		return startingSettlement;
	}
	
	/**
	 * Sets the starting settlement.
	 * @param startingSettlement starting settlement.
	 */
	void setStartingSettlement(Settlement startingSettlement) {
		this.startingSettlement = startingSettlement;
	}
	
	/**
	 * Gets the rover.
	 * @return rover.
	 */
	Rover getRover() {
		return rover;
	}
	
	/**
	 * Sets the rover.
	 * @param rover the rover.
	 */
	void setRover(Rover rover) {
		this.rover = rover;
	}
	
	/**
	 * Gets the mission members.
	 * @return the members.
	 */
	PersonCollection getMembers() {
		return members;
	}
	
	/**
	 * Sets the mission members.
	 * @param members the members.
	 */
	void setMembers(PersonCollection members) {
		this.members = members;
	}
	
	/**
	 * Gets the destination settlement.
	 * @return destination settlement.
	 */
	Settlement getDestinationSettlement() {
		return destinationSettlement;
	}
	
	/**
	 * Sets the destination settlement.
	 * @param destinationSettlement the destination settlement.
	 */
	void setDestinationSettlement(Settlement destinationSettlement) {
		this.destinationSettlement = destinationSettlement;
	}
	
	/**
	 * Gets the rescue rover.
	 * @return the rescue rover.
	 */
	Rover getRescueRover() {
		return rescueRover;
	}
	
	/**
	 * Sets the rescue rover.
	 * @param rescueRover the rescue rover.
	 */
	void setRescueRover(Rover rescueRover) {
		this.rescueRover = rescueRover;
	}
	
	/**
	 * Gets the ice collection site.
	 * @return ice collection site.
	 */
	Coordinates getIceCollectionSite() {
		return iceCollectionSite;
	}
	
	/**
	 * Sets the ice collection site.
	 * @param iceCollectionSite the ice collection site.
	 */
	void setIceCollectionSite(Coordinates iceCollectionSite) {
		this.iceCollectionSite = iceCollectionSite;
	}

	/**
	 * Gets the exploration sites.
	 * @return exploration sites.
	 */
	Coordinates[] getExplorationSites() {
		return explorationSites;
	}

	/**
	 * Sets the exploration sites.
	 * @param explorationSites the exploration sites.
	 */
	void setExplorationSites(Coordinates[] explorationSites) {
		this.explorationSites = explorationSites;
	}
}