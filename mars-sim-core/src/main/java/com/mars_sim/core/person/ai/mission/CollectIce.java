/*
 * Mars Simulation Project
 * CollectIce.java
 * @date 2024-07-12
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.mission;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.ObjectiveType;
import com.mars_sim.core.vehicle.Rover;

/**
 * This class is a mission to travel in a rover to several random locations
 * around a settlement and collect ice.
 */
public class CollectIce extends CollectResourcesMission {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** Number of barrels required for the mission. */
	public static final int REQUIRED_BARRELS = 6;

	/** Number of collection sites. */
	private static final int NUM_SITES = 3;

	private static final MissionStatus NO_ICE_COLLECTION_SITES = new MissionStatus("Mission.status.noIceSites");

	private static final Set<ObjectiveType> OBJECTIVES = Set.of(ObjectiveType.CROP_FARM);

	private int searchCount = 0;

	/**
	 * Constructor
	 *
	 * @param startingPerson the person starting the mission.
	 * @param needsReview
	 * @throws MissionException if problem constructing mission.
	 */
	public CollectIce(Person startingPerson, boolean needsReview) {
		// Use CollectResourcesMission constructor.
		super(MissionType.COLLECT_ICE, startingPerson, ResourceUtil.ICE_ID,
				EquipmentType.BARREL, REQUIRED_BARRELS, NUM_SITES, needsReview);
	}

	/**
	 * Constructor with explicit data.
	 *
	 * @param members            collection of mission members.
	 * @param iceCollectionSites the sites to collect ice.
	 * @param rover              the rover to use.
	 */
	public CollectIce(Collection<Worker> members,
			List<Coordinates> iceCollectionSites, Rover rover) {

		// Use CollectResourcesMission constructor.
		super(MissionType.COLLECT_ICE, members, ResourceUtil.ICE_ID,
				EquipmentType.BARREL, REQUIRED_BARRELS,
				rover, iceCollectionSites);
	}

	@Override
	protected double scoreLocation(Coordinates newLocation) {
		if (terrainElevation == null) 
			terrainElevation = Simulation.instance().getSurfaceFeatures().getTerrainElevation();
		
		return terrainElevation.obtainIceCollectionRate(newLocation);
	}


	@Override
	protected boolean isValidScore(double score) {
		boolean accept = (score > 0);
		if (!accept && (searchCount++ >= 10)) {
			endMission(NO_ICE_COLLECTION_SITES);
		}

		return accept;
	}

	@Override
	protected double calculateRate(Worker worker) {
		return scoreLocation(worker.getCoordinates());
	}
	
	protected void pickType(Worker worker) {
		setResourceID(resourceID);
	}

	/**
	 * Gets the resources can be collected once on site. By default this is just
	 * the main resource but could be others.
	 * 
	 * @return
	 */
	@Override
	public int [] getCollectibleResources() {
		return new int [] {resourceID};
	}

	
	@Override
	public Set<ObjectiveType> getObjectiveSatisified() {
		return OBJECTIVES;
	}
}
