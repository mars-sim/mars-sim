/*
 * Mars Simulation Project
 * Exploration.java
 * @date 2022-07-14
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Direction;
import org.mars_sim.msp.core.environment.ExploredLocation;
import org.mars_sim.msp.core.environment.MineralMap;
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
import org.mars_sim.msp.core.tool.RandomUtil;
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
	
	private static final Set<ObjectiveType> OBJECTIVES = Set.of(ObjectiveType.TOURISM, ObjectiveType.TRANSPORTATION_HUB);

	// Data members
	/** Map of exploration sites and their completion. */
	private Map<String, Double> explorationSiteCompletion;
	/** The current exploration site. */
	private ExploredLocation currentSite;
	/** List of sites explored by this mission. */
	private List<ExploredLocation> exploredSites;

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

			int sol = marsClock.getMissionSol();
			numSites = 2 + (int)(1.0 * sol / 20);
			
			List<Coordinates> explorationSites = determineExplorationSites(getVehicle().getRange(),
					getRover().getTotalTripTimeLimit(true),
					numSites, skill);

			if (explorationSites.isEmpty()) {
				endMission(NO_EXPLORATION_SITES);
				return;
			}

			// Update the number of determined sites
			numSites = explorationSites.size();
			
			initSites(explorationSites);

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
		
		initSites(explorationSites);

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
	private void initSites(List<Coordinates> explorationSites) {

		numSites = explorationSites.size();
		
		// Initialize explored sites.
		exploredSites = new ArrayList<>(numSites);
		explorationSiteCompletion = new HashMap<>(numSites);
		setEVAEquipment(EquipmentType.SPECIMEN_BOX, REQUIRED_SPECIMEN_CONTAINERS);

		// Configure the sites to be explored with mineral concentration during the stage of mission planning
		for(Coordinates c : explorationSites) {
			createAExploredSite(c);
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
	private ExploredLocation retrieveCurrentSite() {
		Coordinates current = getCurrentMissionLocation();
		for (ExploredLocation e: exploredSites) {
			if (e.getLocation().equals(current))
				return e;
		}

		// Should never get here
		return createAExploredSite(current);
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
			currentSite = retrieveCurrentSite();
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
	 * Creates a brand new site at the current location and
	 * estimate its mineral concentrations.
	 *
	 * @throws MissionException if error creating explored site.
	 * @return ExploredLocation
	 */
	private ExploredLocation createAExploredSite(Coordinates siteLocation) {

		// Make sure site is not known already
		ExploredLocation el = surfaceFeatures.getExploredLocation(siteLocation);
		if (el == null) {
			el = surfaceFeatures.addExploredLocation(siteLocation,
					0, getStartingSettlement());
		}

		exploredSites.add(el);
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
		int confidence = 2 * (1 + (int)RandomUtil.getRandomDouble(marsClock.getMissionSol()));

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
		
		List<Coordinates> outstandingSites = findCandidateSites(startingLocation);
		if (!outstandingSites.isEmpty()) {
			currentLocation = outstandingSites.remove(0);
		}
		else {
			limit = range / 2D;
	
			// Use the confidence score to limit the range
			double distance = RandomUtil.getRandomRegressionInteger(confidence, (int)limit);
			
			currentLocation = determineFirstExplorationSite(distance, areologySkill);
		}
		
		if (currentLocation != null) {
			unorderedSites.add(currentLocation);
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
	 * Estimate the time needed at an EVA site.
	 * 
	 * @param buffer Add a buffer allowance
	 * @return Estimated time per EVA site
	 */
	protected double getEstimatedTimeAtEVASite(boolean buffer) {
		return STANDARD_TIME_PER_SITE;
	}

	/**
	 * Gets a list of candidate explored location for this Settlement that needs estimation improvement.
	 * 
	 * @return
	 */
	private List<Coordinates> findCandidateSites(Coordinates startingLoc) {

		Settlement home = getStartingSettlement();

		int rand = RandomUtil.getRandomRegressionInteger(100);
		// Get any locations that belong to this home Settlement and need further
		// exploration before mining
		List<Coordinates> candidateLocs = surfaceFeatures.getExploredLocations().stream()
				.filter(e -> e.getNumEstimationImprovement() < rand)
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
	 * Determine the first exploration site.
	 *
	 * @param range         the range (km) for site.
	 * @param areologySkill the skill level in areology of the areologist starting
	 *                      the mission.
	 * @return first exploration site or null if none.
	 * @throws MissionException if error determining site.
	 */
	private Coordinates determineFirstExplorationSite(double range, int areologySkill) {
		Coordinates result = null;

		Coordinates startingLocation = getCurrentMissionLocation();
		MineralMap map = surfaceFeatures.getMineralMap();
		Coordinates randomLocation = map.findRandomMineralLocation(startingLocation, range);
		if (randomLocation != null) {
			Direction direction = new Direction(RandomUtil.getRandomDouble(2D * Math.PI));
			if (areologySkill <= 0) {
				areologySkill = 1;
			}
			double distance = RandomUtil.getRandomDouble(10, 500D / areologySkill);
			result = randomLocation.getNewLocation(direction, distance);
			double distanceFromStart = Coordinates.computeDistance(startingLocation, result);
			if (distanceFromStart > range) {
				Direction direction2 = startingLocation.getDirectionToPoint(result);
				result = startingLocation.getNewLocation(direction2, range);
			}
		} else {
			// Use random direction and distance for first location
			// if no minerals found within range.
			Direction direction = new Direction(RandomUtil.getRandomDouble(2D * Math.PI));
			double distance = RandomUtil.getRandomDouble(1, range);
			result = startingLocation.getNewLocation(direction, distance);
		}

		return result;
	}

	/**
	 * Gets a list of sites explored by the mission so far.
	 *
	 * @return list of explored sites.
	 */
	public List<ExploredLocation> getExploredSites() {
		return exploredSites;
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
			double mineralAmount = (concentration / 100) * 50_000 * Mining.MINERAL_BASE_AMOUNT;
			result += mineralValue * mineralAmount;
		}

		return (int)(Math.round(result));
	}

	/**
	 * Return the average site score of all exploration sites
	 */
	@Override
	public double getTotalSiteScore(Settlement reviewerSettlement) {
		if (exploredSites.isEmpty()) {
			return 0D;
		}

		int count = 0;
		double siteValue = 0D;
		for (ExploredLocation e : exploredSites) {
			count++;
			siteValue += Mining.getMiningSiteValue(e, reviewerSettlement);
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
	 * Gets amount of time to explore a site. 
	 */
	public double getSiteTime() {
		return STANDARD_TIME_PER_SITE;
	}
	
	@Override
	protected Set<JobType> getPreferredPersonJobs() {
		return PREFERRED_JOBS;
	}

	@Override
	public Set<ObjectiveType> getObjectiveSatisified() {
		return OBJECTIVES;
	}
}
