/*
 * Mars Simulation Project
 * Exploration.java
 * @date 2024-07-23
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.mission;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.mars_sim.core.environment.MineralSite;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.map.location.Direction;
import com.mars_sim.core.mission.objectives.ExplorationObjective;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.task.ExploreSite;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.structure.ExplorationManager;
import com.mars_sim.core.structure.ObjectiveType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.tool.RandomUtil;
import com.mars_sim.core.vehicle.Rover;
import com.mars_sim.core.vehicle.Vehicle;

/**
 * The Exploration class is a mission to travel in a rover to several random
 * locations around a settlement and collect rock samples.
 */
public class Exploration extends EVAMission
	implements SiteMission {

	private static final Set<JobType> PREFERRED_JOBS = Set.of(JobType.AREOLOGIST, JobType.ASTRONOMER, JobType.BIOLOGIST, 
			JobType.BOTANIST, JobType.CHEMIST, JobType.METEOROLOGIST, JobType.PILOT);

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(Exploration.class.getName());

	/** Number of specimen containers required for the mission. */
	public static final int REQUIRED_SPECIMEN_CONTAINERS = 8;
	/** Amount of time to explore a site. */
	private static final double STANDARD_TIME_PER_SITE = 500.0;
	
	/** Exploration Site */
	private static final String EXPLORATION_SITE = "Exploration Site ";

	
	/** Mission Type enum. */
	public static final MissionType MISSION_TYPE = MissionType.EXPLORATION;

	/** Mission phase. */
	private static final MissionPhase EXPLORE_SITE = new MissionPhase("Mission.phase.exploreSite");
	private static final MissionStatus NO_EXPLORATION_SITES = new MissionStatus("Mission.status.noExplorationSites");

	private static final Set<ObjectiveType> OBJECTIVES = Set.of(ObjectiveType.TOURISM, ObjectiveType.TRANSPORTATION_HUB);

	private double currentSiteTime;
	private MineralSite currentSite;

	private ExplorationObjective objective;

	/** Manager of the explorations at the home Settlement */
	private ExplorationManager explorationMgr;
	
	/** The set of sites to be claimed by this mission. */
	private Set<MineralSite> claimedSites = new HashSet<>();
	

	/**
	 * Constructor.
	 *
	 * @param startingPerson the person starting the mission.
	 * @throws MissionException if problem constructing mission.
	 */
	public Exploration(Person startingPerson, boolean needsReview) {

		// Use RoverMission constructor.
		super(MISSION_TYPE, startingPerson, null,
				EXPLORE_SITE, ExploreSite.LIGHT_LEVEL);
		
		this.objective = new ExplorationObjective();
		addObjective(objective);
		
		Settlement s = getStartingSettlement();

		if (s != null && !isDone()) {
			explorationMgr = s.getExplorations();

			// Recruit additional members to mission.
			if (!recruitMembersForMission(startingPerson, MIN_GOING_MEMBERS)) {
				logger.warning(getVehicle(), "Not enough members recruited for mission " 
						+ getName() + ".");
				endMission(NOT_ENOUGH_MEMBERS);
				return;
			}

			// Determine exploration sites
			if (!hasVehicle()) {
				return;
			}

			int skill = startingPerson.getSkillManager().getEffectiveSkillLevel(SkillType.AREOLOGY);

			int sol = getMarsTime().getMissionSol();
			var numSites = 2 + (int)(1.0 * sol / 20);
			
			List<Coordinates> sitesToClaim = determineExplorationSites(getVehicle().getEstimatedRange(),
					getRover().getTotalTripTimeLimit(true),
					numSites, skill);

			if (sitesToClaim.isEmpty()) {
				endMission(NO_EXPLORATION_SITES);
				return;
			}

			// Update the number of determined sites
			numSites = sitesToClaim.size();
			
			initializeExplorationSites(sitesToClaim);

			// Set initial mission phase.
			setInitialPhase(needsReview);
		}
	}

	/**
	 * Constructor with explicit data.
	 *
	 * @param members            collection of mission members.
	 * @param explorationSites   the sites to explore.
	 * @param rover              the rover to use.
	 */
	public Exploration(Collection<Worker> members,
			List<Coordinates> explorationSites, Rover rover) {

		// Use RoverMission constructor.
		super(MISSION_TYPE,(Worker) members.toArray()[0], rover,
				EXPLORE_SITE, ExploreSite.LIGHT_LEVEL);
		
		this.objective = new ExplorationObjective();
		addObjective(objective);

		explorationMgr = getStartingSettlement().getExplorations();
				
		initializeExplorationSites(explorationSites);

		// Add mission members.
		if (!isDone()) {
			addMembers(members, false);
			// Set initial mission phase.
			setInitialPhase(false);
		}
	}

	/**
	 * Sets up the exploration sites.
	 *  
	 * @param explorationSites
	 * @param skill
	 */
	private void initializeExplorationSites(List<Coordinates> explorationSites) {
		
		// Initialize explored sites.
		int numMembers = (getMissionCapacity() + getMembers().size()) / 2;
		int buffer = (int)(numMembers * 1.5);
		int newContainerNum = Math.max(buffer, REQUIRED_SPECIMEN_CONTAINERS);
		
		setEVAEquipment(EquipmentType.SPECIMEN_BOX, newContainerNum);
	
		// Set exploration navpoints.
		addNavpoints(explorationSites, (i -> EXPLORATION_SITE + (i+1)));

		// Add home navpoint.
		Settlement s = getStartingSettlement();
		addNavpoint(s);

		// Check if vehicle can carry enough supplies for the mission. Must have NavPoints loaded
		if (!isVehicleLoadable()) {
			endMission(CANNOT_LOAD_RESOURCES);
		}
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
	 * Retrieves the current exploration site instance.
	 *
	 * @return
	 */
	private MineralSite retrieveASiteToClaim() {
		
		Coordinates current = getCurrentMissionLocation();
		for (MineralSite e: claimedSites) {
			if (e.getLocation().equals(current))
				return e;
		}
		
		logger.info(getStartingSettlement(), "No explored sites found. Looking for one.");
		
	
		return declareARegionOfInterest(current, 2);
	}

	/**
	 * Updates the explored site and start an ExploreSite Task.
	 * 
	 * @param person
	 */
	@Override
	protected boolean performEVA(Person person) {

		// Update exploration site completion.
		double timeDiff = getPhaseDuration();
		double completion = timeDiff / STANDARD_TIME_PER_SITE * 2;
		if (completion > 1D) {
			completion = 1D;
		}
		else if (completion < 0D) {
			completion = 0D;
		}

		// Add new explored site if just starting exploring.
		if (currentSite == null) {
			currentSite = retrieveASiteToClaim();
		}
		fireMissionUpdate(MissionEventType.SITE_EXPLORATION_EVENT, getCurrentNavpointDescription());

		objective.updateSiteCompletion(getCurrentNavpointDescription(), completion);

		// If person can explore the site, start that task.
		if (ExploreSite.canExploreSite(person, getRover())) {
			assignTask(person, new ExploreSite(person, currentSite, getRover(), this));
		}

		return true;
	}

	/**
	 * Ends the current EVA operations, i.e. getting everyone back to vehicle.
	 */
	@Override
	protected void endEVATasks() {
		super.endEVATasks();
		
		// Set the site to have been explored
		if (currentSite != null) {
			currentSite.setExplored(true);
		}
		
		currentSiteTime = 0D;
		currentSite = null;
	}

	/**
	 * Creates a brand new site at a given location and
	 * estimate its mineral concentrations.
	 * 
	 * @param siteLocation
	 * @param skill
	 * @return ExploredLocation
	 */
	private MineralSite declareARegionOfInterest(Coordinates siteLocation, int skill) {
		
		MineralSite el = explorationMgr.createARegionOfInterest(siteLocation, skill);
		
		if (el != null)
			claimedSites.add(el);
		
		return el;
	}

	@Override
	protected int compareVehicles(Vehicle firstVehicle, Vehicle secondVehicle) {
		int result = super.compareVehicles(firstVehicle, secondVehicle);

		// Check of one rover has a research lab and the other one doesn't.
		if (result == 0) {
			boolean firstLab = ((Rover) firstVehicle).hasLab();
			boolean secondLab = ((Rover) secondVehicle).hasLab();
			if (firstLab && !secondLab)
				result = 1;
			else if (!firstLab && secondLab)
				result = -1;
		}

		return result;
	}

	/**
	 * Gets the estimated time spent at all exploration sites.
	 *
	 * @return time (millisols)
	 */
	protected double getEstimatedTimeOfAllEVAs() {
		return STANDARD_TIME_PER_SITE * getNumEVASites();
	}

	/**
	 * Determine the locations of the exploration sites.
	 *
	 * @param roverRange    the rover's driving range
	 * @param numSites      the number of exploration sites
	 * @param areologySkill the skill level in areology for the areologist starting
	 *                      the mission.
	 * @throws MissionException if exploration sites can not be determined.
	 */
	private List<Coordinates> determineExplorationSites(double roverRange, double tripTimeLimit, int numSites, int areologySkill) {
		// Calculate the confidence score for determining the distance
		// The longer it stays on Mars, the higher the confidence
		int confidence = 2 * (1 + (int)RandomUtil.getRandomDouble(getMarsTime().getMissionSol()));

		List<Coordinates> unorderedSites = new ArrayList<>();

		// Determining the actual traveling range.
		double limit = 0;
		double range = roverRange;
		double timeRange = getTripTimeRange(numSites, tripTimeLimit, getAverageVehicleSpeedForOperators());
		if (timeRange < range) {
			range = timeRange;
		}

		// Determine the first exploration site.
		Coordinates startingLocation = getCurrentMissionLocation();
		Coordinates currentLocation = null;
		MineralSite el = null;
		
		// Find mature sites to explore
		List<Coordinates> outstandingSites = findClaimedCandidateSites(startingLocation);
		if (!outstandingSites.isEmpty()) {
			currentLocation = outstandingSites.remove(0);
		}
		
		else {
			limit = range / 2D;

			// Use the confidence score to limit the range
			double dist = RandomUtil.getRandomRegressionInteger(confidence, (int)limit);
			
			currentLocation = determineFirstSiteCoordinate(dist);
			
			if (currentLocation != null) {
				// Creates an initial explored site in SurfaceFeatures
				el = declareARegionOfInterest(currentLocation, areologySkill);
			}
		}
	
		if (currentLocation != null) {
			unorderedSites.add(currentLocation);
		}
		else {
			if (el == null) {
				logger.info(getStartingSettlement(), 10_000L, "Unable to pinpoint a good site. Need to further analyze maps.");
			}
			else {
				logger.info(getStartingSettlement(), 10_000L, "Could not determine first exploration site.");
			}
			return unorderedSites;
		}

		// Determine remaining exploration sites.
		double siteDistance = startingLocation.getDistance(currentLocation);
		double remainingRange = (range / 2D) - siteDistance;

		// Add in some existing ones first
		while ((unorderedSites.size() < numSites)  && (remainingRange > 1D)
				&& !outstandingSites.isEmpty()) {
			// Take the next one off the front
			Coordinates nextLocation = outstandingSites.remove(0);
			unorderedSites.add(nextLocation);
			remainingRange -= nextLocation.getDistance(currentLocation);
			currentLocation = nextLocation;
		}

		// Pick some new ones
		while ((unorderedSites.size() < numSites) && (remainingRange > 1D)) {
			Direction direction = new Direction(RandomUtil.getRandomDouble(2D * Math.PI));
			
			limit = range / 4D;
			
			double distance = RandomUtil.getRandomDouble(confidence, (int)limit);
			
			Coordinates newLocation = currentLocation.getNewLocation(direction, distance);
			unorderedSites.add(newLocation);
			
			currentLocation = newLocation;
			remainingRange -= distance;
		}

		List<Coordinates> sites = null;

		if (unorderedSites.size() > 1) {
			double unorderedSitesTotalDistance = getTotalDistance(startingLocation, unorderedSites);

			// Try to reorder sites for shortest distance.
			List<Coordinates> orderedSites = getMinimalPath(startingLocation, unorderedSites);

			double orderedSitesTotalDistance = getTotalDistance(startingLocation, orderedSites);

			if (orderedSitesTotalDistance < unorderedSitesTotalDistance) {
				sites = orderedSites;
			} else {
				sites = unorderedSites;
			}
		} else {
			sites = unorderedSites;
		}

		return sites;
	}

	/**
	 * Determine the first exploration site.
	 *
	 * @return first exploration site or null if none.
	 */
	private Coordinates determineFirstSiteCoordinate(double limit) {
		// Get a random site that is one of the closest
		return explorationMgr.getUnexploredDeclaredSite(true, limit);
	}
	
	/**
	 * Gets a list of candidate site coordinates for a settlement. Filter for those that needs estimation improvement.
	 * 
	 * @return
	 */
	private List<Coordinates> findClaimedCandidateSites(Coordinates startingLoc) {

		Settlement home = getStartingSettlement();

		// Get any locations that belong to this home Settlement and need further
		// exploration before mining
		List<Coordinates> candidateLocs = explorationMgr.getDeclaredLocations()
				//surfaceFeatures.getAllPossibleRegionOfInterestLocations()
				.stream()
				.filter(e -> e.getNumEstimationImprovement() < 
						RandomUtil.getRandomInt(0, Mining.MATURE_ESTIMATE_NUM * 10))
				.filter(s -> home.equals(s.getSettlement()))
				.map(MineralSite::getLocation)
				.toList();
		
		if (!candidateLocs.isEmpty()) {
			return getMinimalPath(startingLoc, candidateLocs);
		}
		
		return Collections.emptyList();
	}

	/**
	 * Gets the total distances of going to all sites.
	 * 
	 * @param startingLoc
	 * @param sites
	 * @return
	 */
	private static double getTotalDistance(Coordinates startingLoc, List<Coordinates> sites) {
		double result = 0D;

		Coordinates currentLoc = startingLoc;
		Iterator<Coordinates> i = sites.iterator();
		while (i.hasNext()) {
			Coordinates site = i.next();
			result += currentLoc.getDistance(site);
			currentLoc = site;
		}

		// Add return trip to starting loc.
		result += currentLoc.getDistance(startingLoc);

		return result;
	}
	

	/**
	 * Gets a list of sites explored by the mission so far.
	 *
	 * @return list of explored sites.
	 */
	public Set<MineralSite> getExploredSites() {
		return claimedSites;
	}

	/**
	 * Estimates the time needed at an EVA site.
	 * 
	 * @param buffer Add a buffer allowance
	 * @return Estimated time per EVA site
	 */
	protected double getEstimatedTimeAtEVASite(boolean buffer) {
		return STANDARD_TIME_PER_SITE;
	}

	/**
	 * Returns the average site score of all exploration sites.
	 */
	@Override
	public double getTotalSiteScore(Settlement reviewerSettlement) {
		if (claimedSites.isEmpty()) {
			return 0D;
		}

		int count = 0;
		double siteValue = 0D;
		for (MineralSite el : claimedSites) {
			count++;
			siteValue += Mining.getMiningSiteValue(el, reviewerSettlement);
		}

		if (count == 0)
			return 0;

		return siteValue / count;
	}

	/**
	 * Records the amount of resources collected.
	 * 
	 * @param resourceType
	 * @param samplesCollected
	 */
	public void recordResourceCollected(int resourceType, double samplesCollected) {
		objective.recordResourceCollected(resourceType, samplesCollected);
	}
	
	/**
	 * Adds the site time.
	 * 
	 * @param time
	 */
	public void addSiteTime(double time) {
		currentSiteTime += time;
	}
	
	/** 
	 * Gets amount of time to explore a site. 
	 */
	public double getCurrentSiteTime() {
		return currentSiteTime;
	}
	
	@Override
	protected Set<JobType> getPreferredPersonJobs() {
		return PREFERRED_JOBS;
	}

	@Override
	public Set<ObjectiveType> getObjectiveSatisified() {
		return OBJECTIVES;
	}
	
	/**
	 * Prepares object for garbage collection.
	 */
	public void destroy() {
		claimedSites.clear();
		claimedSites = null;
	}
}
