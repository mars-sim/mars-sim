/**
 * Mars Simulation Project
 * Exploration.java
 * @version 3.2.0 2021-06-20
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
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.environment.ExploredLocation;
import org.mars_sim.msp.core.environment.MineralMap;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.job.JobType;
import org.mars_sim.msp.core.person.ai.task.EVAOperation;
import org.mars_sim.msp.core.person.ai.task.ExploreSite;
import org.mars_sim.msp.core.resource.ResourceUtil;
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

	/** Default description. */
	private static final String DEFAULT_DESCRIPTION = Msg.getString("Mission.description.exploration"); //$NON-NLS-1$

	/** Mission Type enum. */
	public static final MissionType MISSION_TYPE = MissionType.EXPLORATION;

	/** Mission phase. */
	private static final MissionPhase EXPLORE_SITE = new MissionPhase("Mission.phase.exploreSite");

	/** Exploration Site */
	private static final String EXPLORATION_SITE = "Exploration Site ";

	/** Number of specimen containers required for the mission. */
	public static final int REQUIRED_SPECIMEN_CONTAINERS = 20;

	/** Number of collection sites. */
	private static final int NUM_SITES = 5;

	/** Amount of time to explore a site. */
	public static final double EXPLORING_SITE_TIME = 500D;

	/** Maximum mineral concentration estimation diff from actual. */
	private static final double MINERAL_ESTIMATION_VARIANCE = 20D;

	/** Maximum mineral estimation */
	private static final double MINERAL_ESTIMATION_MAX = 100D;

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
	public Exploration(Person startingPerson) {

		// Use RoverMission constructor.
		super(DEFAULT_DESCRIPTION, MISSION_TYPE, startingPerson, null,
				EXPLORE_SITE);

		Settlement s = getStartingSettlement();

		if (s != null && !isDone()) {
			// Recruit additional members to mission.
			if (!recruitMembersForMission(startingPerson, MIN_GOING_MEMBERS))
			return;

			// Determine exploration sites
			if (!hasVehicle()) {
				return;
			}

			int skill = startingPerson.getSkillManager().getEffectiveSkillLevel(SkillType.AREOLOGY);
			List<Coordinates> explorationSites = determineExplorationSites(getVehicle().getRange(MISSION_TYPE),
					getTotalTripTimeLimit(getRover(), getPeopleNumber(), true),
					NUM_SITES, skill);

			if (explorationSites.isEmpty()) {
					endMission(MissionStatus.NO_EXPLORATION_SITES);
			}

			initSites(explorationSites);

			// Set initial mission phase.
			setPhase(REVIEWING, null);
		}

		logger.fine(startingPerson, "Just finished creating an Exploration mission.");
	}

	/**
	 * Constructor with explicit data.
	 *
	 * @param members            collection of mission members.
	 * @param explorationSites   the sites to explore.
	 * @param rover              the rover to use.
	 * @param description        the mission's description.
	 */
	public Exploration(Collection<MissionMember> members,
			List<Coordinates> explorationSites, Rover rover, String description) {

		// Use RoverMission constructor.
		super(description, MISSION_TYPE,(MissionMember) members.toArray()[0], rover,
				EXPLORE_SITE);
	

		initSites(explorationSites);

		// Add mission members.
		if (!isDone()) {
			addMembers(members, false);

			// Set initial mission phase.
			setPhase(EMBARKING, getStartingSettlement().getName());
		}
	}

	/**
	 * Setup the exploration sites
	 */
	private void initSites(List<Coordinates> explorationSites) {

		// Initialize explored sites.
		exploredSites = new ArrayList<>(NUM_SITES);
		explorationSiteCompletion = new HashMap<>(NUM_SITES);
		setEVAEquipment(EquipmentType.SPECIMEN_BOX, REQUIRED_SPECIMEN_CONTAINERS);

		// Configure the sites to be explored with mineral concentration during the stage of mission planning
		for(Coordinates c : explorationSites) {
			createAExploredSite(c);
		}

		// Set exploration navpoints.
		addNavpoints(explorationSites, (i -> EXPLORATION_SITE + (1+1)));

		// Add home navpoint.
		Settlement s = getStartingSettlement();
		addNavpoint(s);

		// Check if vehicle can carry enough supplies for the mission. Must have NavPoints loaded
		if (!isVehicleLoadable()) {
			endMission(MissionStatus.CANNOT_LOAD_RESOURCES);
		}
	}

	/**
	 * Checks if there are any mineral locations within rover/mission range.
	 *
	 * @param rover          the rover to use.
	 * @param homeSettlement the starting settlement.
	 * @return true if mineral locations.
	 * @throws Exception if error determining mineral locations.
	 */
	public static boolean hasNearbyMineralLocations(Rover rover, Settlement homeSettlement) {

		double roverRange = rover.getRange(MISSION_TYPE);
		double tripTimeLimit = getTotalTripTimeLimit(rover, rover.getCrewCapacity(), true);
		double tripRange = getTripTimeRange(tripTimeLimit, rover.getBaseSpeed() / 1.25D);
		double range = roverRange;
		if (tripRange < range)
			range = tripRange;

		MineralMap map = surfaceFeatures.getMineralMap();
		Coordinates mineralLocation = map.findRandomMineralLocation(homeSettlement.getCoordinates(), range / 2D);

		return (mineralLocation != null);
	}

	/**
	 * Checks if there are any mineral locations within rover/mission range.
	 *
	 * @param rover          the rover to use.
	 * @param homeSettlement the starting settlement.
	 * @return true if mineral locations.
	 * @throws Exception if error determining mineral locations.
	 */
	public static Map<String, Double> getNearbyMineral(Rover rover, Settlement homeSettlement) {
		Map<String, Double> minerals = new HashMap<>();

		double roverRange = rover.getRange(MISSION_TYPE);
		double tripTimeLimit = getTotalTripTimeLimit(rover, rover.getCrewCapacity(), true);
		double tripRange = getTripTimeRange(tripTimeLimit, rover.getBaseSpeed() / 1.25D);
		double range = roverRange;
		if (tripRange < range)
			range = tripRange;

		MineralMap map = surfaceFeatures.getMineralMap();
		Coordinates mineralLocation = map.findRandomMineralLocation(homeSettlement.getCoordinates(), range / 2D);

		if (mineralLocation != null)
			minerals = map.getAllMineralConcentrations(mineralLocation);

		return minerals;
	}

	/**
	 * Gets the range of a trip based on its time limit and exploration sites.
	 *
	 * @param tripTimeLimit time (millisols) limit of trip.
	 * @param averageSpeed  the average speed of the vehicle.
	 * @return range (km) limit.
	 */
	private static double getTripTimeRange(double tripTimeLimit, double averageSpeed) {
		double tripTimeTravellingLimit = tripTimeLimit - (NUM_SITES * EXPLORING_SITE_TIME);
		double millisolsInHour = MarsClock.convertSecondsToMillisols(60D * 60D);
		double averageSpeedMillisol = averageSpeed / millisolsInHour;
		return tripTimeTravellingLimit * averageSpeedMillisol;
	}

	/**
	 * Retrieves the current exploration site instance
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
	 * Update the explored site and start an ExploreSite Task
	 */
	@Override
	protected boolean performEVA(Person person) {

		// Update exploration site completion.
		double timeDiff = getPhaseDuration();
		double completion = timeDiff / EXPLORING_SITE_TIME;
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
	 * End the current EVA operations, i.e. get everyone to return to vehicle
	 */
	@Override
	protected void endEVATasks() {
		super.endEVATasks();

		// Speecifc to Exploration
		if (currentSite != null) {
			currentSite.setExplored(true);
		}
		currentSite = null;
	}

	/**
	 * Creates a brand new site at the current location and
	 * estimate its mineral concentrations
	 *
	 * @throws MissionException if error creating explored site.
	 * @return ExploredLocation
	 */
	private ExploredLocation createAExploredSite(Coordinates siteLocation) {
		MineralMap mineralMap = surfaceFeatures.getMineralMap();
		String[] mineralTypes = mineralMap.getMineralTypeNames();

		// Make sure site is not known already
		ExploredLocation el = surfaceFeatures.getExploredLocation(siteLocation);
		if (el == null) {
			// bUILD A NEW SITE
			Map<String, Double> initialMineralEstimations = new HashMap<>(mineralTypes.length);
			for (String mineralType : mineralTypes) {
				// Estimations are zero for initial site.
				double estimation = RandomUtil.getRandomDouble(MINERAL_ESTIMATION_VARIANCE);
				estimation += mineralMap.getMineralConcentration(mineralType, siteLocation);
				if (estimation < 0D)
					estimation = 0D - estimation;
				else if (estimation > MINERAL_ESTIMATION_MAX)
					estimation = MINERAL_ESTIMATION_MAX - estimation;
				initialMineralEstimations.put(mineralType, estimation);
			}

			el = surfaceFeatures.addExploredLocation(siteLocation,
					initialMineralEstimations, getStartingSettlement());
		}

		exploredSites.add(el);
		return el;
	}



	@Override
	protected int compareVehicles(Vehicle firstVehicle, Vehicle secondVehicle) {
		int result = super.compareVehicles(firstVehicle, secondVehicle);

		// Check of one rover has a research lab and the other one doesn't.
		if ((result == 0) && (isUsableVehicle(firstVehicle)) && (isUsableVehicle(secondVehicle))) {
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
		return EXPLORING_SITE_TIME * getNumEVASites();
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
		int confidence = 3 + (int)RandomUtil.getRandomDouble(marsClock.getMissionSol());

		List<Coordinates> unorderedSites = new ArrayList<>();

		// Determining the actual traveling range.
		double limit = 0;
		double range = roverRange;
		double timeRange = getTripTimeRange(tripTimeLimit, getAverageVehicleSpeedForOperators());
		if (timeRange < range) {
			range = timeRange;
		}


		// Determine the first exploration site.
		Coordinates startingLocation = getCurrentMissionLocation();
		Coordinates currentLocation = null;
		List<Coordinates> outstandingSites = findOutstandingSites(startingLocation);
		if (!outstandingSites.isEmpty()) {
			currentLocation = outstandingSites.remove(0);
		}
		else {
			currentLocation = determineFirstExplorationSite((range / 2D), areologySkill);
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
			siteDistance = RandomUtil.getRandomRegressionInteger(confidence, (int)limit);
			Coordinates newLocation = currentLocation.getNewLocation(direction, siteDistance);
			unorderedSites.add(newLocation);
			currentLocation = newLocation;
			remainingRange -= siteDistance;
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
	 * @param buffer Add a buffer allowance
	 * @return Estimated time per EVA site
	 */
	protected double getEstimatedTimeAtEVASite(boolean buffer) {
		return EXPLORING_SITE_TIME;
	}

	/**
	 * Get a list of explored location for this Settlement that needs further investigation
	 * @return
	 */
	private List<Coordinates> findOutstandingSites(Coordinates startingLoc) {

		Settlement home = getStartingSettlement();

		// Get any locations that belong to this home Settlement and need further
		// exploration before mining
		List<Coordinates> candiateLocations = surfaceFeatures.getExploredLocations().stream()
				.filter(e -> e.getNumEstimationImprovement() < Mining.MATURE_ESTIMATE_NUM)
				.filter(s -> s.getSettlement().equals(home))
				.map(ExploredLocation::getLocation)
				.collect(Collectors.toList());
		if (!candiateLocations.isEmpty()) {
			return getMinimalPath(startingLoc, candiateLocations);
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
			double distance = RandomUtil.getRandomDouble(10, range);
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
	public static double getTotalMineralValue(Settlement settlement, Map<String, Double> minerals) {

		double result = 0D;

		for (Map.Entry<String, Double> entry : minerals.entrySet()) {
		    String mineralType = entry.getKey();
		    double concentration = entry.getValue();
			int mineralResource = ResourceUtil.findIDbyAmountResourceName(mineralType);
			double mineralValue = settlement.getGoodsManager().getGoodValuePerItem(mineralResource);
			double mineralAmount = (concentration / 100D) * Mining.MINERAL_BASE_AMOUNT;
			result += mineralValue * mineralAmount;
		}

		return result;
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

	@Override
	protected Set<JobType> getPreferredPersonJobs() {
		return PREFERRED_JOBS;
	}

	@Override
	public void destroy() {
		super.destroy();

		if (explorationSiteCompletion != null)
			explorationSiteCompletion.clear();
		explorationSiteCompletion = null;
		currentSite = null;
		if (exploredSites != null)
			exploredSites.clear();
		exploredSites = null;
	}
}
