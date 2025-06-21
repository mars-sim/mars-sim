/*
 * Mars Simulation Project
 * CollectRegolith.java
 * @date 2022-07-16
 * @author Sebastien Venot
 */

package com.mars_sim.core.person.ai.mission;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.mars_sim.core.environment.TerrainElevation;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.ObjectiveType;
import com.mars_sim.core.tool.RandomUtil;
import com.mars_sim.core.vehicle.Rover;

/**
 * This class is a mission to travel in a rover to several random locations
 * around a settlement and collect regolith-b, regolith-c, and/or regolith-d).
 */
public class CollectRegolith extends CollectResourcesMission {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** Number of large bags required for the mission. */
	public static final int REQUIRED_LARGE_BAGS = 6;

	/** Number of collection sites. */
	private static final int NUM_SITES = 3;

	private static final Set<ObjectiveType> OBJECTIVES = Set.of(ObjectiveType.BUILDERS_HAVEN, ObjectiveType.MANUFACTURING_DEPOT);

	/**
	 * Constructor.
	 *
	 * @param startingPerson the person starting the mission.
	 * @param needsReview Needs to be reviewed
	 * @throws MissionException if problem constructing mission.
	 */
	public CollectRegolith(Person startingPerson, boolean needsReview) {
		// Use CollectResourcesMission constructor.
		super(MissionType.COLLECT_REGOLITH, startingPerson, ResourceUtil.REGOLITH_ID,
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
		super(MissionType.COLLECT_REGOLITH, members, ResourceUtil.REGOLITH_ID,
				EquipmentType.LARGE_BAG, REQUIRED_LARGE_BAGS,
				rover, regolithCollectionSites);
	}

	@Override
	protected double scoreLocation(TerrainElevation terrain, Coordinates newLocation) {
		return terrain.obtainRegolithCollectionRate(newLocation);
	}

	@Override
	protected int pickType(Worker worker) {
		if (RandomUtil.getRandomInt(3) == 0) {
			// Pick the one that has the highest vp
			double highest = 0;
			int bestType = 0;
			var goodsMgr = worker.getAssociatedSettlement().getGoodsManager();
			for (int type: ResourceUtil.REGOLITH_TYPES) {
				double vp = goodsMgr.getGoodValuePoint(type);
				if (highest < vp) {
					highest = vp;
					bestType = type;
				}
			}
			
			return bestType;
		}
		// Randomly pick one of the 4 regolith types
		int rand = RandomUtil.getRandomInt(ResourceUtil.REGOLITH_TYPES.length - 1);

		return ResourceUtil.REGOLITH_TYPES[rand];
	}
	
	@Override
	public Set<ObjectiveType> getObjectiveSatisified() {
		return OBJECTIVES;
	}
}
