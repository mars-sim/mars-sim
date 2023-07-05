/*
 * Mars Simulation Project
 * Exploration.java
 * @date 2023-06-30
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.mars.sim.mapdata.location.Coordinates;
import org.mars.sim.mapdata.location.Direction;
import org.mars.sim.tools.util.RandomUtil;
import org.mars_sim.msp.core.CollectionUtils;
import org.mars_sim.msp.core.environment.ExploredLocation;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.job.util.JobType;
import org.mars_sim.msp.core.person.ai.task.ExploreSite;
import org.mars_sim.msp.core.person.ai.task.util.Worker;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.ObjectiveType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * The Exploration class is a mission to travel in a rover to several random
 * locations around a settlement and collect rock samples.
 */
public class Exploration extends EVAMission
	implements SiteMission {

	private static final Set<JobType> PREFERRED_JOBS = Set.of(JobType.AREOLOGIST, JobType.ASTRONOMER, JobType.BIOLOGIST, JobType.BOTANIST, JobType.CHEMIST, JobType.METEOROLOGIST, JobType.PILOT);

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(Exploration.class.getName());

	/** Mission Type enum. */
	public static final MissionType MISSION_TYPE = MissionType.EXPLORATION;

	/** Mission phase. */
	private static final MissionPhase EXPLORE_SITE = new MissionPhase("Mission.phase.exploreSite");
	private static final MissionStatus NO_EXPLORATION_SITES = new MissionStatus("Mission.status.noExplorationSites");

	/** Exploration Site */
	private static final String EXPLORATION_SITE = "Exploration Site ";

	/** Number of specimen containers required for the mission. */
	public static final int REQUIRED_SPECIMEN_CONTAINERS = 20;
	/** Amount of time to explore a site. */
	private static final int STANDARD_TIME_PER_SITE = 250;
	
	/** Number of collection sites. */
	private int numSites;
	
	/**
	 * The cumulative and combined site time for all members who will explore the site
	 */
	private double siteTime;
	
	private static final Set<ObjectiveType> OBJECTIVES = Set.of(ObjectiveType.TOURISM, ObjectiveType.TRANSPORTATION_HUB);

	// Data members
	/** Map of exploration sites and their completion. */
	private Map<String, Double> explorationSiteCompletion;
	/** The current exploration site. */
	private ExploredLocation currentSite;
	/** The set of sites to be claimed by this mission. */
	private Set<ExploredLocation> claimedSites;

	/**
	 * Constructor.
	 *
	 * @param startingPerson the person starting the mission.
	 * @throws MissionException if problem constructing mission.
	 */
	public Exploration(Person startingPerson, boolean needsReview) {

		// Use RoverMission constructor.
		super(MISSION_TYPE, startingPerson, null,
				EXPLORE_SITE);
		setIgnoreSunlight(true);
		
		Settlement s = getStartingSettlement();

		if (s != null && !isDone()) {
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
			numSites = 2 + (int)(1.0 * sol / 20);
			
			List<Coordinates> sitesToClaim = determineExplorationSites(getVehicle().getRange(),
					getRover().getTotalTripTimeLimit(true),
					numSites, skill);

			if (sitesToClaim.isEmpty()) {
				endMission(NO_EXPLORATION_SITES);
				return;
			}

			// Update the number of determined sites
			numSites = sitesToClaim.size();
			
			initSites(sitesToClaim, skill);

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
				EXPLORE_SITE);
		
		setIgnoreSunlight(true);
		
		Person person = (Person)members.toArray()[0];
		
		int skill = person.getSkillManager().getEffectiveSkillLevel(SkillType.AREOLOGY);		
		
		initSites(explorationSites, skill);

		// Add mission members.
		if (!isDone()) {
			addMembers(members, false);

			// Set initial mission phase.
			setInitialPhase(false);
		}
	}

	/**
	 * Sets up the exploration sites.
	 */
	private void initSites(List<Coordinates> explorationSites, int skill) {

		numSites = explorationSites.size();
		
		// Initialize explored sites.
		if (claimedSites == null || claimedSites.isEmpty())
			claimedSites = new HashSet<>(numSites);
		
		explorationSiteCompletion = new HashMap<>(numSites);
		
		setEVAEquipment(EquipmentType.SPECIMEN_BOX, REQUIRED_SPECIMEN_CONTAINERS);

		// Configure the sites to be explored with mineral concentration during the stage of mission planning
		for (Coordinates c : explorationSites) {
			declareARegionOfInterest(c, skill);
		}

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
	 * @param siteTime
	 * @param tripTimeLimit time (millisols) limit of trip.
	 * @param averageSpeed  the average speed of the vehicle.
	 * @return range (km) limit.
	 */
	private double getTripTimeRange(int numSites, double tripTimeLimit, double averageSpeed) {
		double tripTimeTravellingLimit = tripTimeLimit - (numSites * STANDARD_TIME_PER_SITE);
		double millisolsInHour = MarsClock.convertSecondsToMillisols(60D * 60D);
		double averageSpeedMillisol = averageSpeed / millisolsInHour;
		return tripTimeTravellingLimit * averageSpeedMillisol;
	}

	/**
	 * Retrieves the current exploration site instance.
	 *
	 * @return
	 */
	private ExploredLocation retrieveASiteToClaim() {
		
		if (claimedSites != null && !claimedSites.isEmpty()) {
			Coordinates current = getCurrentMissionLocation();
			for (ExploredLocation e: claimedSites) {
				if (e.getLocation().equals(current))
					return e;
			}
		}
		
		logger.info(getStartingSettlement(), "No explored sites found. Looking for one.");
		
		// Creates an random site
		
		Coordinates location = determineFirstSiteCoordinate();
		if (location == null) {
			logger.info(getStartingSettlement(), "location is null.");
			return null;
		}
		
		return declareARegionOfInterest(location, 0);
	}

	/**
	 * Determine the coordinate of the first exploration site.
	 *
	 * @return
	 */
	private Coordinates determineFirstSiteCoordinate() {
		double range = getVehicle().getRange();
		return getStartingSettlement().getARandomNearbyMineralLocation(range);
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
		double completion = timeDiff / STANDARD_TIME_PER_SITE;
		if (completion > 1D) {
			completion = 1D;
		}
		else if (completion < 0D) {
			completion = 0D;
		}

		// Add new explored site if just starting exploring.
		if (currentSite == null) {
			currentSite = retrieveASiteToClaim();
			fireMissionUpdate(MissionEventType.SITE_EXPLORATION_EVENT, getCurrentNavpointDescription());
		}
		
		explorationSiteCompletion.put(getCurrentNavpointDescription(), completion);

		// If person can explore the site, start that task.
		if (ExploreSite.canExploreSite(person, getRover())) {
			assignTask(person, new ExploreSite(person, currentSite, (Rover) getVehicle()));
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
	private ExploredLocation declareARegionOfInterest(Coordinates siteLocation, int skill) {
		
		ExploredLocation el = getStartingSettlement().createARegionOfInterest(siteLocation, skill);
		
		if (claimedSites == null || claimedSites.isEmpty())
			claimedSites = new HashSet<>();
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
		ExploredLocation el = null;
		
		// Find mature sites to explore
		List<Coordinates> outstandingSites = findCandidateSitesToClaim(startingLocation);
		if (!outstandingSites.isEmpty()) {
			currentLocation = outstandingSites.remove(0);
		}
		
		else {
			limit = range / 2D;

			// Use the confidence score to limit the range
			double dist = RandomUtil.getRandomRegressionInteger(confidence, (int)limit);
			
			currentLocation = determineFirstSiteCoordinate(dist, areologySkill);
			
			// Creates an initial explored site in SurfaceFeatures
			el = declareARegionOfInterest(currentLocation, areologySkill);
		}

		if (currentLocation != null) {
			unorderedSites.add(currentLocation);
		}
		else if (el == null) {
			logger.info(CollectionUtils.findSettlement(startingLocation), 10_000L, "Unable to pinpoint a good site. Need to analyze map more.");
		}
		else
			throw new IllegalStateException(getPhase() + " : Could not determine first exploration site.");

		// Determine remaining exploration sites.
		double siteDistance = Coordinates.computeDistance(startingLocation, currentLocation);
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

			sites = unorderedSites;
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
	private Coordinates determineFirstSiteCoordinate(double limit, int areologySkill) {
		
		int skillDistance = RandomUtil.getRandomRegressionInteger(10, 500 * (1 + areologySkill));
	
		return getStartingSettlement().getAComfortableNearbyMineralLocation(limit, skillDistance);
	}
	
	/**
	 * Gets a list of candidate site coordinates for a settlement. Filter for those that needs estimation improvement.
	 * 
	 * @return
	 */
	private List<Coordinates> findCandidateSitesToClaim(Coordinates startingLoc) {

		Settlement home = getStartingSettlement();

		// Get any locations that belong to this home Settlement and need further
		// exploration before mining
		List<Coordinates> candidateLocs = surfaceFeatures.getAllRegionOfInterestLocations().stream()
				.filter(e -> e.getNumEstimationImprovement() < 
						RandomUtil.getRandomInt(0, Mining.MATURE_ESTIMATE_NUM * 10))
				.filter(s -> home.equals(s.getSettlement()))
				.map(ExploredLocation::getLocation)
				.collect(Collectors.toList());
		
		if (!candidateLocs.isEmpty()) {
			return getMinimalPath(startingLoc, candidateLocs);
		}
		
		return Collections.emptyList();
	}

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
	public Set<ExploredLocation> getExploredSites() {
		return claimedSites;
	}

	/**
	 * Gets a map of exploration site names and their level of completion.
	 *
	 * @return map of site names and completion level (0.0 - 1.0).
	 */
	public Map<String, Double> getExplorationSiteCompletion() {
		return new HashMap<>(explorationSiteCompletion);
	}

	/**
	 * Estimate the time needed at an EVA site.
	 * 
	 * @param buffer Add a buffer allowance
	 * @return Estimated time per EVA site
	 */
	protected double getEstimatedTimeAtEVASite(boolean buffer) {
		return STANDARD_TIME_PER_SITE;
	}

	
	/**
	 * Gets the estimated total mineral value of a mining site.
	 *
	 * @param site       the mining site.
	 * @param settlement the settlement valuing the minerals.
	 * @return estimated value of the minerals at the site (VP).
	 * @throws MissionException if error determining the value.
	 */
	public static int getTotalMineralValue(Settlement settlement, Map<String, Integer> minerals) {

		double result = 0D;

		for (Map.Entry<String, Integer> entry : minerals.entrySet()) {
		    String mineralType = entry.getKey();
		    double concentration = entry.getValue();
			int mineralResource = ResourceUtil.findIDbyAmountResourceName(mineralType);
			double mineralValue = settlement.getGoodsManager().getGoodValuePoint(mineralResource);
			double mineralAmount = (concentration / 100) * Mining.MINERAL_GOOD_VALUE_FACTOR;
			result += mineralValue * mineralAmount;
		}
		
		result = Math.round(result * 100.0)/100.0;
		
		logger.info(settlement, "A site has an Exploration Value of " + result + ".");
		return (int)result;
	}

	/**
	 * Return the average site score of all exploration sites
	 */
	@Override
	public double getTotalSiteScore(Settlement reviewerSettlement) {
		if (claimedSites.isEmpty()) {
			return 0D;
		}

		int count = 0;
		double siteValue = 0D;
		for (ExploredLocation el : claimedSites) {
			count++;
			siteValue += Mining.getMiningSiteValue(el, reviewerSettlement);
		}

		if (count == 0)
			return 0;

		return siteValue / count;
	}

	
	/** 
	 * Gets number of collection sites. 
	 */
	public int getNumSites() {
		return numSites;
	}

	
	/**
	 * Adds the site time.
	 * 
	 * @param time
	 */
	public void addSiteTime(double time) {
		siteTime += time;
	}
	
	/** 
	 * Gets amount of time to explore a site. 
	 */
	public double getSiteTime() {
		return siteTime;
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
		explorationSiteCompletion.clear();
		explorationSiteCompletion = null;
		currentSite = null;
		claimedSites.clear();
		claimedSites = null;
	}
}
