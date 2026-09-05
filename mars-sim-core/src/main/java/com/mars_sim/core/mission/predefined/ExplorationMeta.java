/*
 * Mars Simulation Project
 * ExplorationMeta.java
 * @date 2025-07-06
 * @author Scott Davis
 */
package com.mars_sim.core.mission.predefined;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.environment.MineralSite;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.goods.GoodsManager.CommerceType;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.mission.AbstractMetaMission;
import com.mars_sim.core.mission.MetaMission;
import com.mars_sim.core.mission.MissionCreationException;
import com.mars_sim.core.mission.MissionStep;
import com.mars_sim.core.mission.MissionVehicleProject;
import com.mars_sim.core.mission.objectives.ExplorationObjective;
import com.mars_sim.core.mission.steps.MissionTravelStep;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.mission.Mining;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.MissionType;
import com.mars_sim.core.person.ai.mission.NavPoint;
import com.mars_sim.core.person.ai.role.RoleType;
import com.mars_sim.core.structure.ObjectiveType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.tool.RandomUtil;
import com.mars_sim.core.vehicle.Rover;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.core.vehicle.VehicleType;
import com.mars_sim.core.vehicle.comparators.LabRangeComparator;
import com.mars_sim.core.vehicle.task.OperateVehicle;

/**
 * A meta mission for the Exploration mission.
 */
public class ExplorationMeta extends AbstractMetaMission {

	private static final int MAX = 50;

	private static final Set<JobType> PREFERRED_WORKER_JOBS = Set.of(JobType.AREOLOGIST, JobType.ASTRONOMER, JobType.ASTROBIOLOGIST, 
			JobType.BOTANIST, JobType.CHEMIST, JobType.METEOROLOGIST, JobType.PILOT);	

	/** Starting sol for this mission to commence. */
	private static final int MIN_STARTING_SOL = 2;
	private static final double STANDARD_TIME_PER_SITE = 1000.0;


	private static final double VALUE = 20D;

	/** Number of specimen containers required for the mission. */
	public static final int REQUIRED_SPECIMEN_CONTAINERS = 8;

	// Exploration mission event type
	public static final String SITE_EXPLORATION_EVENT = "explore site";

	public ExplorationMeta() {
		super(MissionType.EXPLORATION, 4,
					Set.of(JobType.AREOLOGIST, JobType.ASTRONOMER, JobType.METEOROLOGIST),
					PREFERRED_WORKER_JOBS);
		
		setPreferredVehicle(VehicleType.ROVER_TYPES);
		setPopulationRatio(5);
		setSolThreshold(MIN_STARTING_SOL);

		setObjectives(Set.of(ObjectiveType.RESEARCH_CAMPUS, ObjectiveType.TRANSPORTATION_HUB));
	}

	/**
	 * Gets the Vehicle comparator that is based on largest cargo.
	 */
	@Override
	protected Comparator<Vehicle> getVehicleComparator() {
		return new LabRangeComparator();
	}

	/**
	 * Constructs a new instance of the Exploration mission with the given crew and review status.
	 * @param crew the roster of crew members for the mission.
	 * @param needsReview whether the mission requires review before execution.
	 * @return a new instance of the Exploration mission.
	 */
	@Override
	public Mission constructInstance(Roster crew, boolean needsReview) throws MissionCreationException{

		var numSites = getExpectedSites(crew.leader().getAssociatedSettlement());
		List<MineralSite> sites = determineExplorationSites(crew, numSites);

		if (sites.isEmpty()) {
			throw new MissionCreationException("mission.exploration.nosites");
		}
			
		return constructInstance(crew, needsReview, sites);
	}

	/**
	 * Gets a list of candidate MineralSites known to a settlement.
	 * Filter for those that needs estimation improvement.
	 * 
	 * @return
	 */
	private List<MineralSite> findClaimedCandidateSites(Settlement settlement) {

		var home = settlement.getReportingAuthority();

		// Get any locations that belong to this home Settlement and need further
		// exploration before mining
		return settlement.getExplorations().getDeclaredROIs()
				.stream()
				.filter(e -> e.getNumEstimationImprovement() < 
						RandomUtil.getRandomInt(0, Mining.MATURE_ESTIMATE_NUM * 10))
				.filter(s -> home.equals(s.getOwner()))
				.toList();
	}

	/**
	 * Determines the locations of the exploration sites.
	 *
	 * @param crew		  the roster of crew members for the mission
	 * @param numSites      the number of exploration sites
	 */
	private List<MineralSite> determineExplorationSites(MetaMission.Roster crew, int numSites) {

		Rover rover = (Rover)crew.vehicle();	
		double theorticalMaxRange = rover.getEstimatedRange();
		double theoreticalMaxTripTime = rover.getTotalTripTimeLimit(true);

		// Determining the actual traveling range.
		double possibleRange = getTripTimeRange(numSites, theoreticalMaxTripTime, getAverageVehicleSpeed(crew));
		double range = Math.min(theorticalMaxRange, possibleRange);

		// Determine the first exploration site.
		var starting = crew.leader().getAssociatedSettlement();
		
		// Find mature sites to explore
		List<MineralSite> knownSites = findClaimedCandidateSites(starting);

		// Determine remaining exploration sites.
		Coordinates homeLocation = starting.getCoordinates();
		Coordinates currentLocation = homeLocation;
		double remainingRange = range;
		double returnDist = 0D;
		List<MineralSite> claimedSites = new ArrayList<>();

		// Add in some existing ones first
		int knownId = 0;
		while ((claimedSites.size() < numSites)
				&& (remainingRange > returnDist)
				&& (knownId < knownSites.size())) {
			// Take the next one off the front
			var site = knownSites.get(knownId++);
			claimedSites.add(site);

			// Calc what distance is left
			Coordinates nextLocation = site.getCoordinates();
			remainingRange -= nextLocation.getDistance(currentLocation);
			currentLocation = nextLocation;
			returnDist = currentLocation.getDistance(homeLocation);
		}

		// Pick some new ones if still space but limit the attempts
		if (claimedSites.size() < numSites) {
			var explorationMgr = starting.getExplorations();
			var claimedLocns = new HashSet<>(claimedSites.stream().map(MineralSite::getLocation).toList());
			int unplannedAttempts = 10;
			int areologySkill = crew.leader().getSkillManager().getEffectiveSkillLevel(SkillType.AREOLOGY);

			while ((claimedSites.size() < numSites)
					&& (remainingRange > returnDist)
					&& (unplannedAttempts-- > 0)) {
				// Find minerals near base
				var unplannedLimit = (remainingRange - returnDist) / 2D;
				var newLocn = explorationMgr.getUnexploredLocalSites(false, unplannedLimit);

				// Check not in the current list
				if ((newLocn == null) || claimedLocns.contains(newLocn)) {
					continue;
				}

				// Is it good enough to create MineralSite
				var el = explorationMgr.createROI(newLocn, areologySkill);
				if (el != null) {
					claimedSites.add(el);

					// Add to the list
					claimedLocns.add(newLocn);
					remainingRange -= newLocn.getDistance(currentLocation);
					currentLocation = newLocn;
					returnDist = currentLocation.getDistance(homeLocation);
				}
			}
		}
		
		// Original used route optimisation
		// getMinimalPath(startingLocation, selectedLocns)

		return claimedSites;
	}

	/**
	 * What is the travelling speed for this crew. This may be sharable?
	 * @param crew Crew travelling
	 * @return
	 */
	private static double getAverageVehicleSpeed(Roster crew) {

		var v = crew.vehicle();
		double totalSpeed = OperateVehicle.getAverageVehicleSpeed(v, crew.leader());

		totalSpeed += crew.members().stream()
				.mapToDouble(member -> OperateVehicle.getAverageVehicleSpeed(v, member))
				.sum();
	
		return totalSpeed / (crew.members().size() + 1);
	}

	/**
	 * Gets the range of a trip based on its time limit and exploration sites.
	 *
	 * @param numSites
	 * @param currentSiteTime
	 * @param tripTimeLimit time (millisols) limit of trip.
	 * @param averageSpeed  the average speed of the vehicle.
	 * @return range (km) limit.
	 */
	private double getTripTimeRange(int numSites, double tripTimeLimit, double averageSpeed) {
		double tripTimeTravellingLimit = tripTimeLimit - (numSites * STANDARD_TIME_PER_SITE);
		double millisolsInHour = MarsTime.convertSecondsToMillisols(60D * 60D);
		double averageSpeedMillisol = averageSpeed / millisolsInHour;
		return tripTimeTravellingLimit * averageSpeedMillisol;
	}

	/**
	 * Constructs a new instance of the Exploration mission with the given crew and exploration sites.
	 * @param crew the roster of crew members for the mission.
	 * @param needsReview whether the mission requires review before execution.
	 * @param sites the list of mineral sites to be explored during the mission.
	 * @return a new instance of the Exploration mission.
	 */
	public Mission constructInstance(Roster crew, boolean needsReview, List<MineralSite> sites) {
        Settlement base = crew.leader().getAssociatedSettlement();
        Coordinates lastLocation = base.getCoordinates();

		var mission = new MissionVehicleProject(null, MissionType.EXPLORATION, 10, crew);
		var objectives = new ExplorationObjective();

        List<MissionStep> plan = new ArrayList<>();
		for (MineralSite site : sites) {
			var siteLocn = site.getCoordinates();
        	plan.add(new MissionTravelStep(mission, new NavPoint(siteLocn, site.getName(),
                                                            lastLocation)));
        	plan.add(new ExploreSiteStep(mission, objectives, site));

        	lastLocation = siteLocn;
		}

		// Return home
		plan.add(new MissionTravelStep(mission, new NavPoint(base, lastLocation)));
        mission.setSteps(plan);  

        return mission;
	}

	@Override
	public RatingScore getProbability(Person person) {

    	RatingScore missionProbability = RatingScore.ZERO_RATING;
		Settlement settlement = person.getAssociatedSettlement();

        RoleType roleType = person.getRole().getType();

		if (roleType.isCouncil()
				|| RoleType.CHIEF_OF_MISSION_PLANNING == roleType
				|| RoleType.CHIEF_OF_SUPPLY_RESOURCE == roleType
	 			|| RoleType.MISSION_SPECIALIST == roleType
				|| RoleType.RESOURCE_SPECIALIST == roleType
				) {

			// 1. Check if there are enough specimen containers at the settlement for
			// collecting rock samples.
        	int stored = settlement.getEquipmentInventory().findNumContainersOfType(EquipmentType.SPECIMEN_BOX);
            int needed = REQUIRED_SPECIMEN_CONTAINERS;
	        if (stored < needed) {
				BuildingManager.injectEquipmentDemand(EquipmentType.SPECIMEN_BOX, settlement, stored, needed);
				
				return RatingScore.ZERO_RATING;
			}

			// Get available rover.
			var rover = (Rover)selectVehicle(settlement);
			if (rover == null) {
				return RatingScore.ZERO_RATING;
			}
			
			missionProbability = new RatingScore(1);
							

			// Check if any mineral locations within rover range and obtain their concentration
			missionProbability.addModifier(DEMAND_PROBABILITY, Math.min(MAX,
								settlement.getExplorations().getTotalMineralValue(rover)) / VALUE);

			// Job modifier.
			missionProbability.addModifier(LEADER, getLeaderSuitability(person));
			missionProbability = applyCommerceAverage(missionProbability, settlement, CommerceType.TOURISM,
												CommerceType.RESEARCH);

			// if introvert, score  0 to  50 --> -2 to 0
			// if extrovert, score 50 to 100 -->  0 to 2
			// Increase probability if extrovert
			int extrovert = person.getExtrovertmodifier();
			missionProbability.addModifier(PERSON_EXTROVERT, (1 + extrovert/2.0));

			missionProbability.applyRange(0, LIMIT);
		}

		return missionProbability;
	}

	/**
	 * How many sites should be explored for this Settlement.
	 * Default returns 2
	 * @param s Settlement leading exploration
	 * @return
	 */
	public int getExpectedSites(Settlement s) {
		return 2;
	}
}