/*
 * Mars Simulation Project
 * CollectRegolith.java
 * @date 2021-08-20
 * @author Sebastien Venot
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
 * around a settlement and collect regolith-b, regolith-c, and/or regolith-d).
 */
public class CollectRegolith extends CollectResourcesMission {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** Default description. */
	private static final String DEFAULT_DESCRIPTION = Msg.getString("Mission.description.collectRegolith"); //$NON-NLS-1$

	/** Mission Type enum. */
	public static final MissionType missionType = MissionType.COLLECT_REGOLITH;

	/** Number of large bags required for the mission. */
	public static final int REQUIRED_LARGE_BAGS = 10;

	/** Collection rate of regolith during EVA (kg/millisol). */
	private static final double BASE_COLLECTION_RATE = 10D;

	/** Number of collection sites. */
	private static final int NUM_SITES = 1;

	/** Minimum number of people to do mission. */
	public static final int MIN_PEOPLE = 2;


	/**
	 * Constructor.
	 * 
	 * @param startingPerson the person starting the mission.
	 * @throws MissionException if problem constructing mission.
	 */
	public CollectRegolith(Person startingPerson) {
		// Use CollectResourcesMission constructor.
		super(DEFAULT_DESCRIPTION, missionType, startingPerson, ResourceUtil.regolithID, BASE_COLLECTION_RATE,
				EquipmentType.LARGE_BAG, REQUIRED_LARGE_BAGS, NUM_SITES, MIN_PEOPLE);
	}

	/**
	 * Constructor with explicit data.
	 * 
	 * @param members                 collection of mission members.
	 * @param regolithCollectionSites the sites to collect regolith.
	 * @param rover                   the rover to use.
	 * @param description             the mission's description.
	 * @throws MissionException if error constructing mission.
	 */
	public CollectRegolith(Collection<MissionMember> members, 
			List<Coordinates> regolithCollectionSites, Rover rover, String description) {

		// Use CollectResourcesMission constructor.
		super(description, missionType, members, ResourceUtil.regolithID, BASE_COLLECTION_RATE,
				EquipmentType.LARGE_BAG, REQUIRED_LARGE_BAGS,
				RoverMission.MIN_GOING_MEMBERS, rover, regolithCollectionSites);
	}

	/**
	 * Gets the description of a collection site.
	 * 
	 * @param siteNum the number of the site.
	 * @return description
	 */
	protected String getCollectionSiteDescription(int siteNum) {
		return "prospecting site " + siteNum;
	}

	@Override
	protected double scoreLocation(Coordinates newLocation) {
		return terrainElevation.getRegolithCollectionRate(null, newLocation);
	}

	/**
	 * The main resource is regolith but on site it can cover numerous
	 * sub-resources.
	 * @return
	 */
	@Override
	protected int [] getCollectibleResources() {
		return ResourceUtil.REGOLITH_TYPES;
	}

	/**
	 * THis implementatino can refine the resource being collected according to what is at the site.
	 */
	@Override
	protected double calculateRate(Worker worker) {
		
		// Look for the regolith type that has the highest vp
		double highest = 0;
		for (int type: ResourceUtil.REGOLITH_TYPES) {
			double vp = worker.getAssociatedSettlement().getGoodsManager().getGoodValuePerItem(type);
			if (highest < vp) {
				highest = vp;
				setResourceID(type);
			}
		}
			
		return terrainElevation.getRegolithCollectionRate(null, worker.getCoordinates());
	}
}
