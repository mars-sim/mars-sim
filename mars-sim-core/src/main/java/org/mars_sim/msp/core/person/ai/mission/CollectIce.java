/**
 * Mars Simulation Project
 * CollectIce.java
 * @version 3.1.0 2017-10-14
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission;

import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.equipment.Bag;
import org.mars_sim.msp.core.equipment.Barrel;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.mars.TerrainElevation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * This class is a mission to travel in a rover to several random locations
 * around a settlement and collect ice. TODO externalize strings
 */
public class CollectIce extends CollectResourcesMission {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

  private static Logger logger = Logger.getLogger(CollectIce.class.getName());

//	private static String sourceName = logger.getName().substring(logger.getName().lastIndexOf(".") + 1,
//			 logger.getName().length());

	/** Default description. */
	public static final String DEFAULT_DESCRIPTION = Msg.getString("Mission.description.collectIce"); //$NON-NLS-1$

	/** Amount of ice to be gathered at a given site (kg). */
	private static final double SITE_GOAL = 1000D;

	/** Number of bags required for the mission. */
	public static final int REQUIRED_BAGS = 20;

	/** Collection rate of ice during EVA (kg/millisol). */
//	private static final double COLLECTION_RATE = 1D;
	protected static final double collectionRate = 1;
	
	/** Number of collection sites. */
	private static final int NUM_SITES = 1;

	/** Minimum number of people to do mission. */
	private final static int MIN_PEOPLE = 2;

	/**
	 * Constructor
	 * 
	 * @param startingPerson the person starting the mission.
	 * @throws MissionException if problem constructing mission.
	 */
	public CollectIce(Person startingPerson) {
		// Use CollectResourcesMission constructor.
		super(DEFAULT_DESCRIPTION, startingPerson, ResourceUtil.iceID, SITE_GOAL, collectionRate,
				EquipmentType.convertName2ID(Bag.TYPE), REQUIRED_BAGS, NUM_SITES, MIN_PEOPLE);
	}

	/**
	 * Constructor with explicit data.
	 * 
	 * @param members            collection of mission members.
	 * @param startingSettlement the starting settlement.
	 * @param iceCollectionSites the sites to collect ice.
	 * @param rover              the rover to use.
	 * @param description        the mission's description.
	 * @throws MissionException if error constructing mission.
	 */
	public CollectIce(Collection<MissionMember> members, Settlement startingSettlement,
			List<Coordinates> iceCollectionSites, Rover rover, String description) {

		// Use CollectResourcesMission constructor.
		super(description, members, startingSettlement, ResourceUtil.iceID, SITE_GOAL, 
				computeAverageCollectionRate(iceCollectionSites),
				EquipmentType.convertName2ID(Barrel.TYPE), REQUIRED_BAGS, iceCollectionSites.size(),
				RoverMission.MIN_GOING_MEMBERS, rover, iceCollectionSites);
	}

	public static double computeAverageCollectionRate(Collection<Coordinates> locations) {
		double totalRate = 0;
		int size = locations.size();
		
		for (Coordinates location : locations) {
			totalRate += computeCollectionRate(location);
		}
	
		return totalRate / size;
	}
	
	public static double computeCollectionRate(Coordinates location) {
		// Get the elevation and terrain gradient factor
		double[] terrainProfile = TerrainElevation.getTerrainProfile(location);
				
		double elevation = terrainProfile[0];
		double gradient = terrainProfile[1];		
		
		double iceCollectionRate = (- 0.639 * elevation + 14.2492) / 2D  + gradient / 250;
		
		if (iceCollectionRate < 0)
			iceCollectionRate = 0;
		
//		String coord = location.getFormattedString();
//		logger.info(coord + " elevation : " + Math.round(elevation*1000.0)/1000.0);
//		logger.info(coord + " gradient : " + Math.round(gradient*10.0)/10.0);
//		logger.info(coord + " ice collection rate : " + Math.round(iceCollectionRate*100.0)/100.0);	
		
		return iceCollectionRate;
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