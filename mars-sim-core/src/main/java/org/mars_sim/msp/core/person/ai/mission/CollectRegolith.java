/*
 * Mars Simulation Project
 * CollectRegolith.java
 * @date 2022-07-16
 * @author Sebastien Venot
 */

package org.mars_sim.msp.core.person.ai.mission;

import java.util.Collection;
import java.util.List;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.environment.TerrainElevation;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.util.Worker;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * This class is a mission to travel in a rover to several random locations
 * around a settlement and collect regolith-b, regolith-c, and/or regolith-d).
 */
public class CollectRegolith extends CollectResourcesMission {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** Number of large bags required for the mission. */
	public static final int REQUIRED_LARGE_BAGS = 20;

	/** Number of collection sites. */
	private static final int NUM_SITES = 3;


	/**
	 * Constructor.
	 *
	 * @param startingPerson the person starting the mission.
	 * @param needsReview Needs to be reviewed
	 * @throws MissionException if problem constructing mission.
	 */
	public CollectRegolith(Person startingPerson, boolean needsReview) {
		// Use CollectResourcesMission constructor.
		super(MissionType.COLLECT_REGOLITH, startingPerson, ResourceUtil.regolithID,
				EquipmentType.LARGE_BAG, REQUIRED_LARGE_BAGS, NUM_SITES, needsReview);
	}

	/**
	 * Constructor with explicit data.
	 *
	 * @param members                 collection of mission members.
	 * @param regolithCollectionSites the sites to collect regolith.
	 * @param rover                   the rover to use.
	 * @throws MissionException if error constructing mission.
	 */
	public CollectRegolith(Collection<Worker> members,
			List<Coordinates> regolithCollectionSites, Rover rover) {

		// Use CollectResourcesMission constructor.
		super(MissionType.COLLECT_REGOLITH, members, ResourceUtil.regolithID,
				EquipmentType.LARGE_BAG, REQUIRED_LARGE_BAGS,
				rover, regolithCollectionSites);
	}

	@Override
	protected double scoreLocation(Coordinates newLocation) {
		return TerrainElevation.obtainRegolithCollectionRate(newLocation);
	}

	/**
	 * The main resource is regolith but on site it can cover numerous
	 * sub-resources.
	 * 
	 * @return
	 */
	@Override
	public int [] getCollectibleResources() {
		return ResourceUtil.REGOLITH_TYPES;
	}

	/**
	 * This implementation can refine the resource being collected according to what is at the site.
	 */
	@Override
	protected double calculateRate(Worker worker) {

		// Randomly pick one of the 4 regolith types
		int rand = RandomUtil.getRandomInt(4);
		if (rand == 4) {
			// Pick the one that has the highest vp
			double highest = 0;
			for (int type: getCollectibleResources()) {
				double vp = worker.getAssociatedSettlement().getGoodsManager().getGoodValuePoint(type);
				if (highest < vp) {
					highest = vp;
					setResourceID(type);
				}
			}
		}
		else
			setResourceID(getCollectibleResources()[rand]);

		return scoreLocation(worker.getCoordinates());
	}
}
