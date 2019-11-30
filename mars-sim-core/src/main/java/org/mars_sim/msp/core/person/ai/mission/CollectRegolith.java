/**
 * Mars Simulation Project
 * CollectRegolith.java
 * @version 3.1.0 2017-10-14
 * @author Sebastien Venot
 */
package org.mars_sim.msp.core.person.ai.mission;

import java.util.Collection;
import java.util.List;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.equipment.LargeBag;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * This class is a mission to travel in a rover to several random locations
 * around a settlement and collect Regolith. TODO externalize strings
 */
public class CollectRegolith extends CollectResourcesMission {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

//  private static Logger logger = Logger.getLogger(CollectRegolith.class.getName());

//	private static String sourceName = logger.getName().substring(logger.getName().lastIndexOf(".") + 1,
//			 logger.getName().length());

	/** Default description. */
	public static final String DEFAULT_DESCRIPTION = Msg.getString("Mission.description.collectRegolith"); //$NON-NLS-1$

	/** Mission Type enum. */
	public static final MissionType missionType = MissionType.COLLECT_REGOLITH;
	
	/** Amount of regolith to be gathered at a given site (kg). */
	private static final double SITE_GOAL = 2000D;

	/** Number of large bags required for the mission. */
	public static final int REQUIRED_LARGE_BAGS = 15;

	/** Collection rate of regolith during EVA (kg/millisol). */
	private static final double BASE_COLLECTION_RATE = 10D;

	/** Number of collection sites. */
	private static final int NUM_SITES = 1;

	/** Minimum number of people to do mission. */
	public final static int MIN_PEOPLE = 2;

	/**
	 * Constructor.
	 * 
	 * @param startingPerson the person starting the mission.
	 * @throws MissionException if problem constructing mission.
	 */
	public CollectRegolith(Person startingPerson) {
		// Use CollectResourcesMission constructor.
		super(DEFAULT_DESCRIPTION, missionType, startingPerson, ResourceUtil.regolithID, SITE_GOAL, BASE_COLLECTION_RATE,
				EquipmentType.convertName2ID(LargeBag.TYPE), REQUIRED_LARGE_BAGS, NUM_SITES, MIN_PEOPLE);
	}

	/**
	 * Constructor with explicit data.
	 * 
	 * @param members                 collection of mission members.
	 * @param startingSettlement      the starting settlement.
	 * @param regolithCollectionSites the sites to collect regolith.
	 * @param rover                   the rover to use.
	 * @param description             the mission's description.
	 * @throws MissionException if error constructing mission.
	 */
	public CollectRegolith(Collection<MissionMember> members, Settlement startingSettlement,
			List<Coordinates> regolithCollectionSites, Rover rover, String description) {

		// Use CollectResourcesMission constructor.
		super(description, missionType, members, startingSettlement, ResourceUtil.regolithID, SITE_GOAL, BASE_COLLECTION_RATE,
				EquipmentType.convertName2ID(LargeBag.TYPE), REQUIRED_LARGE_BAGS, regolithCollectionSites.size(),
				RoverMission.MIN_GOING_MEMBERS, rover, regolithCollectionSites);
	}

	/**
	 * Gets the description of a collection site.
	 * 
	 * @param siteNum the number of the site.
	 * @return description
	 */
	protected String getCollectionSiteDescription(int siteNum) {
		return "prospecting site";
	}

}