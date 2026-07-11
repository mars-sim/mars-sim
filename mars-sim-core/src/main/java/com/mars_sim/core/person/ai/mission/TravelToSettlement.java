/*
 * Mars Simulation Project
 * TravelToSettlement.java
 * @date 2024-07-14
 * @author Scott Davis
 */

package com.mars_sim.core.person.ai.mission;

import java.util.Set;

import com.mars_sim.core.mission.MetaMission;
import com.mars_sim.core.structure.ObjectiveType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.vehicle.Rover;

/**
 * The TravelToSettlement class is a mission to travel from one settlement to
 * another randomly selected one within range of an available rover. 
 */
public class TravelToSettlement extends RoverMission {
	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	// Travel to settlement mission event type
	public static final String DESTINATION_EVENT = "travelsettlement:destination";

	private static final Set<ObjectiveType> OBJECTIVES = Set.of(ObjectiveType.TRANSPORTATION_HUB, ObjectiveType.TOURISM);

	// Data members
	private Settlement destinationSettlement;


	/**
	 * Travels to a settlement.
	 * 
	 * @param members
	 * @param destinationSettlement
	 * @param rover
	 */
	public TravelToSettlement(MetaMission.Roster crew,  
			Settlement destinationSettlement, boolean needsReview) {
		// Use RoverMission constructor.
		super(MissionType.TRAVEL_TO_SETTLEMENT, crew.leader(), (Rover) crew.vehicle());

		// Set mission destination.
		setDestinationSettlement(destinationSettlement);
		addNavpoint(this.destinationSettlement);

		// Add mission members.
		addMembers(crew.members(), false);

		// Check if vehicle can carry enough supplies for the mission.
		if (hasVehicle() && !isVehicleLoadable()) {
			endMission(CANNOT_LOAD_RESOURCES);
			return;
		}

		setInitialPhase(needsReview);
	}

	/**
	 * Determines a new phase for the mission when the current phase has ended.
	 * 
	 * @throws MissionException if problem setting a new phase.
	 */
	@Override
	protected boolean determineNewPhase() {
		boolean handled = true;
	
		if (!super.determineNewPhase()) {
			if (TRAVELLING.equals(getPhase())) {
				if (isCurrentNavpointSettlement()) {
					startDisembarkingPhase();
				}
			} 
			else {
				handled = false;
			}
		}
		return handled;
	}

	/**
	 * Sets the destination settlement.
	 * 
	 * @param destinationSettlement the new destination settlement.
	 */
	private void setDestinationSettlement(Settlement destinationSettlement) {
		this.destinationSettlement = destinationSettlement;
		fireMissionUpdate(DESTINATION_EVENT);
	}

	/**
	 * Gets the destination settlement.
	 * 
	 * @return destination settlement
	 */
	public final Settlement getDestinationSettlement() {
		return destinationSettlement;
	}

	@Override
	public Set<ObjectiveType> getObjectiveSatisified() {
		return OBJECTIVES;
	}
}
