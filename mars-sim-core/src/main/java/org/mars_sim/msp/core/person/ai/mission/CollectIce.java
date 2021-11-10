/*
 * Mars Simulation Project
 * CollectIce.java
 * @date 2021-08-15
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission;

import java.util.Collection;
import java.util.List;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.utils.Worker;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * This class is a mission to travel in a rover to several random locations
 * around a settlement and collect ice.
 */
public class CollectIce extends CollectResourcesMission {


	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** Default description. */
	private static final String DEFAULT_DESCRIPTION = Msg.getString("Mission.description.collectIce"); //$NON-NLS-1$

	/** Mission Type enum. */
	public static final MissionType missionType = MissionType.COLLECT_ICE;

	/** Number of barrels required for the mission. */
	public static final int REQUIRED_BARRELS = 10;

	/** Collection rate of ice during EVA (kg/millisol). */
	private static final double BASE_COLLECTION_RATE = 5D;
	
	/** Number of collection sites. */
	private static final int NUM_SITES = 2;

	private int searchCount = 0;

	/**
	 * Constructor
	 * 
	 * @param startingPerson the person starting the mission.
	 * @throws MissionException if problem constructing mission.
	 */
	public CollectIce(Person startingPerson) {
		// Use CollectResourcesMission constructor.
		super(DEFAULT_DESCRIPTION, missionType, startingPerson, ResourceUtil.iceID, BASE_COLLECTION_RATE,
				EquipmentType.BARREL, REQUIRED_BARRELS, NUM_SITES);
	}

	/**
	 * Constructor with explicit data.
	 * 
	 * @param members            collection of mission members.
	 * @param iceCollectionSites the sites to collect ice.
	 * @param rover              the rover to use.
	 * @param description        the mission's description.
	 */
	public CollectIce(Collection<MissionMember> members,
			List<Coordinates> iceCollectionSites, Rover rover, String description) {

		// Use CollectResourcesMission constructor.
		super(description, missionType, members, ResourceUtil.iceID,
				computeAverageCollectionRate(iceCollectionSites),
				EquipmentType.BARREL, REQUIRED_BARRELS,
				rover, iceCollectionSites);
	}

	public static double computeAverageCollectionRate(Collection<Coordinates> locations) {
		double totalRate = 0;
		int size = locations.size();
		
		for (Coordinates location : locations) {
			totalRate += terrainElevation.getIceCollectionRate(location);
		}
	
		return totalRate / size;
	}

	@Override
	protected double scoreLocation(Coordinates newLocation) {
		return terrainElevation.getIceCollectionRate(newLocation);
	}
	

	@Override
	protected boolean isValidScore(double score) {
		boolean accept = (score > 0);
		if (!accept && (searchCount++ >= 10)) {
			endMission(MissionStatus.NO_ICE_COLLECTION_SITES);
		}
	
		return accept;
	}

	@Override
	protected double calculateRate(Worker worker) {
		return terrainElevation.getIceCollectionRate(worker.getCoordinates());
	}
}
