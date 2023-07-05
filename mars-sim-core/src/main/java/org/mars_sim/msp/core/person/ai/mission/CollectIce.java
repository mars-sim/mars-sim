/*
 * Mars Simulation Project
 * CollectIce.java
 * @date 2022-07-16
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.mars.sim.mapdata.location.Coordinates;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.util.Worker;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.ObjectiveType;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * This class is a mission to travel in a rover to several random locations
 * around a settlement and collect ice.
 */
public class CollectIce extends CollectResourcesMission {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** Number of barrels required for the mission. */
	public static final int REQUIRED_BARRELS = 20;

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
		super(MissionType.COLLECT_ICE, startingPerson, ResourceUtil.iceID,
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
		super(MissionType.COLLECT_ICE, members, ResourceUtil.iceID,
				EquipmentType.BARREL, REQUIRED_BARRELS,
				rover, iceCollectionSites);
	}

//	public static double computeAverageCollectionRate(Collection<Coordinates> locations) {
//		double totalRate = 0;
//		int size = locations.size();
//
//		for (Coordinates location : locations) {
//			totalRate += TerrainElevation.obtainIceCollectionRate(location);
//		}
//
//		return totalRate / size;
//	}

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

	/**
	 * what resources can be collected once on site. By default this is just
	 * the main resource but could be others.
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
