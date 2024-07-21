/*
 * Mars Simulation Project
 * CollectResourcesMission.java
 * @date 2024-07-12
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.mission;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import com.mars_sim.core.environment.TerrainElevation;
import com.mars_sim.core.equipment.ContainerUtil;
import com.mars_sim.core.equipment.Equipment;
import com.mars_sim.core.equipment.EquipmentOwner;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.task.CollectResources;
import com.mars_sim.core.person.ai.task.EVAOperation;
import com.mars_sim.core.person.ai.task.Sleep;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.vehicle.Rover;
import com.mars_sim.mapdata.location.Coordinates;
import com.mars_sim.mapdata.location.Direction;
import com.mars_sim.tools.util.RandomUtil;

/**
 * The CollectResourcesMission class is a mission to travel in a rover to
 * several random locations around a settlement and collect resources of a given
 * type.
 */
public abstract class CollectResourcesMission extends EVAMission
	implements SiteMission {


	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(CollectResourcesMission.class.getName());

	/** Mission phase. */
	public static final MissionPhase COLLECT_RESOURCES = new MissionPhase("Mission.phase.collectResources");

	private static final String PROPSPECTING_SITE = "Prospecting Site #";

	/** The estimated number of trips made for collecting resources. */
	private static final int NUM_TRIPS = 6;
	/** The maximum number of sites under consideration. */
	private static final int MAX_NUM_PRIMARY_SITES = 30;
	/** The maximum number of sites under consideration. */
	private static final int MAX_NUM_SECONDARY_SITES = 5;
	/** Minimum number of people to do mission. */
	private static final int MIN_PEOPLE = 2;
	/** Upper limit of mission to avoid airlock congestion */
	private static final int MAX_PEOPLE = 6;

	// Data members
	/** The type of resource to collect. */
	protected int resourceID;
	/** The total site score of this prospective resource collection mission. */
	private double totalSiteScore;
	/** The goal amount of resources to collect at one site (kg). */
	private double siteResourceGoal;

	/** The cumulative amount (kg) of resources collected across multiple sites. */
	private Map<Integer, Double> amountCollectedBySite;
	/** The cumulative amount (kg) of resources collected across multiple sites. */
	private Map<Integer, Double> cumulativeCollectedByID;
	/** The type of container needed for the mission or null if none. */
	private EquipmentType containerID;

	protected static TerrainElevation terrainElevation;
	
	/**
	 * Constructor.
	 *
	 * @param startingPerson         The person starting the mission.
	 * @param resourceID           The type of resource.
	 * @param containerID          The type of container needed for the mission or
	 *                               null if none.
	 * @param containerNum           The number of containers needed for the
	 *                               mission.
	 * @param numSites               The number of collection sites.
	 * @param needsReview
	 * @param minPeople              The minimum number of people for the mission.
	 * @throws MissionException if problem constructing mission.
	 */
	protected CollectResourcesMission(MissionType missionType, Person startingPerson, int resourceID,
			EquipmentType containerID, int containerNum, int numSites, boolean needsReview) {

		// Use RoverMission constructor
		super(missionType, startingPerson, null, COLLECT_RESOURCES, CollectResources.LIGHT_LEVEL);

		// Problem starting mission
		if (isDone()) {
			return;
		}
		
		// Too many members creates a congestion at the airlock during EVA
		if (getMissionCapacity() > MAX_PEOPLE) {
			setMissionCapacity(MAX_PEOPLE);
		}
		
		Settlement s = startingPerson.getSettlement();

		if (s == null || isDone()) {
			return;
		}
		
		setResourceID(resourceID);
		
		this.cumulativeCollectedByID = new HashMap<>();
		this.containerID = containerID;
		
		
		// Recruit additional members to mission.
		if (!recruitMembersForMission(startingPerson, MIN_PEOPLE)) {
			logger.warning(getVehicle(), "Not enough members recruited for mission " 
					+ getName() + ".");
			endMission(NOT_ENOUGH_MEMBERS);
			return;
		}

		int numMembers = (getMissionCapacity() + getMembers().size()) / 2;
		int buffer = (int)(numMembers * 1.5);
		int newContainerNum = Math.max(buffer, containerNum);
		
		setEVAEquipment(containerID, newContainerNum);
		
		// Check vehicle
		if (!hasVehicle()) {
			return;
		}
			
		// Get the current location.
		Coordinates startingLocation = s.getCoordinates();
		double range = getVehicle().getRange();
		double timeLimit = getRover().getTotalTripTimeLimit(true);

		// Determining the actual traveling range.
		double timeRange = getTripTimeRange(timeLimit, numSites, true);
		if (timeRange < range)
			range = timeRange;
		if (range <= 0D) {
			logger.warning(getVehicle(), "Zero range for mission " 
					+ getName() + ".");
			endMission(NO_AVAILABLE_VEHICLES);
			return;
		}

		// Find some sites
		List<Coordinates> unorderedSites = null;
		while (unorderedSites == null) {
			unorderedSites = determineCollectionSites(startingLocation, range, numSites);

			if (!isValidScore(totalSiteScore)) {
				totalSiteScore = 0;
				unorderedSites = null;
				logger.warning(startingPerson, getName() + " attempt another collection site find");
			}

			// Mission might be aborted at determine site step
			if (isDone()) {
				logger.warning(startingPerson, getName() + " site searched & mission aborted");
				return;
			}
		}

		// Reorder sites for shortest distance and load
		List<Coordinates> orderSites = getMinimalPath(startingLocation, unorderedSites);
		addNavpoints(orderSites, (i -> PROPSPECTING_SITE + (i+1)));

		double containerCap = ContainerUtil.getContainerCapacity(containerID);
		this.siteResourceGoal = NUM_TRIPS * containerCap * containerNum / orderSites.size();
		logger.info(getVehicle(), "Estimating amount of "
				+ ResourceUtil.findAmountResourceName(resourceID)
				+ " per site: "
				+ (int)siteResourceGoal + " kg.");

		// Add home settlement for return
		addNavpoint(s);

		// Check if vehicle can carry enough supplies for the mission.
		if (hasVehicle() && !isVehicleLoadable()) {
			endMission(CANNOT_LOAD_RESOURCES);
		}

		if (!isDone()) {
			setInitialPhase(needsReview);
		}
		
		// Initialize the map for tallying amount collected at each site
		amountCollectedBySite = new HashMap<>();
	}

	/**
	 * Constructor with explicit data.
	 *
	 * @param members                collection of mission members
	 * @param resourceID           The type of resource.
	 * @param containerID          The type of container needed for the mission or
	 *                               null if none.
	 * @param containerNum           The number of containers needed for the
	 *                               mission.
	 * @param minPeople              The minimum number of people for the mission.
	 * @param rover                  the rover to use.
	 * @param collectionSites     the sites to collect ice.
	 */
	protected CollectResourcesMission(MissionType missionType, Collection<Worker> members,
			Integer resourceID, EquipmentType containerID,
			int containerNum, Rover rover, List<Coordinates> collectionSites) {

		// Use RoverMission constructor
		super(missionType, (Worker) members.toArray()[0], rover, COLLECT_RESOURCES, CollectResources.LIGHT_LEVEL);

		this.resourceID = resourceID;
		double containerCap = ContainerUtil.getContainerCapacity(containerID);
		this.siteResourceGoal = NUM_TRIPS * containerCap * containerNum / collectionSites.size();
		
		this.cumulativeCollectedByID = new HashMap<>();
		this.containerID = containerID;
		
		int numMembers = (getMissionCapacity() + getMembers().size()) / 2;
		int buffer = (int)(numMembers * 1.5);
		int newContainerNum = Math.min(buffer, containerNum);
		
		setEVAEquipment(containerID, newContainerNum);
		
		// Set collection navpoints.
		addNavpoints(collectionSites, (i -> PROPSPECTING_SITE + (i+1)));

		// Add home navpoint.
		addNavpoint(getStartingSettlement());
		
		// Add mission members.
		addMembers(members, false);

		// Check if vehicle can carry enough supplies for the mission.
		if (hasVehicle() && !isVehicleLoadable()) {
			endMission(CANNOT_LOAD_RESOURCES);
		}
		else {
			setInitialPhase(false);
		}
		
		// Initialize the map for tallying amount collected at each site
		amountCollectedBySite = new HashMap<>();
	}

	/**
	 * By default score is always accepted.
	 * 
	 * @param score This score may be use for further computation in overriding classes
	 * @return
	 */
	protected boolean isValidScore(double score) {
		return true;
	}

	/**
	 * Sets the resource.
	 * 
	 * @param newResource
	 */
	protected void setResourceID(int newResource) {
		this.resourceID = newResource;
	}

	/**
	 * Gets the total amount of resources collected so far in the mission.
	 *
	 * @return resource amount (kg).
	 */
	public Map<Integer, Double> getCumulativeCollectedByID() {
		return cumulativeCollectedByID;
	}


	/**
	 * Computes the total resources collected at the current site.
	 * This includes the resources on this person and the rover.
	 *
	 * @param inv a person as the EquipmentOwner
	 * @return the total resources collected at this particular site
	 */
	private double computeTotalResources(EquipmentOwner inv) {
		double resourceStored = 0;
		
		////// NOTE: this method needs to be fixed to be non-negative
		
		// Get capacity for all collectible resources. The collectible
		// resource at a site may be more than the single one specified.
		for (int resourceId : getCollectibleResources()) {
			
			// From this person carrying cargo
			resourceStored += inv.getAmountResourceStored(resourceId);
			// From this person's equipment set
			for (Equipment e : inv.getContainerSet()) {
				resourceStored += e.getAmountResourceStored(resourceId);
			}

			// From mission rover
			EquipmentOwner invRover = (EquipmentOwner)getRover();
			// From this rover's carrying cargo
			resourceStored += invRover.getAmountResourceStored(resourceId);
			// From this rover's equipment set
			for (Equipment e : invRover.getContainerSet()) {
				resourceStored += e.getAmountResourceStored(resourceId);
			}
		}

		// The difference is the resource collected at current site
		return  getTotalCollectedAllSites() - resourceStored;
	}

	/**
	 * Gets the total amount of collected resources at all sites by far.
	 *  
	 * @return
	 */
	public double getTotalCollectedAllSites() {
		// Find out how much collected at this site by far
		int siteIndex = getCurrentNavpointIndex(); 
		double collectedSoFar = 0;
		for (int i=0; i<siteIndex; i++) {
			if (amountCollectedBySite.containsKey(siteIndex)) {
				double amount = amountCollectedBySite.get(siteIndex);
				collectedSoFar += amount;
			}
		}	
		return collectedSoFar;
	}
	
	/**
	 * Gets the amount collected at the current site.
	 * 
	 * @return
	 */
	public double getCollectedAtCurrentSite() {
		int siteIndex = getCurrentNavpointIndex(); 
		double collectedSoFar = 0;
		if (amountCollectedBySite.containsKey(siteIndex)) {
			collectedSoFar = amountCollectedBySite.get(siteIndex);
		}
		return collectedSoFar;
	}
	
	/**
	 * Records the amount of resources collected.
	 * 
	 * @param resourceType
	 * @param samplesCollected
	 */
	public void recordResourceCollected(int resourceType, double samplesCollected) {
		// Update amountCollectedBySite
		int siteIndex = getCurrentNavpointIndex(); 
		if (amountCollectedBySite.containsKey(siteIndex)) {
			double oldAmount0 = amountCollectedBySite.get(siteIndex);
			double newAmount0 = oldAmount0 + samplesCollected;
			amountCollectedBySite.put(siteIndex, newAmount0);
		}
		else {
			amountCollectedBySite.put(siteIndex, samplesCollected);
		}
		
		// Update cumulativeCollectedByID
		if (cumulativeCollectedByID.containsKey(resourceType)) {
			double oldAmount1 = cumulativeCollectedByID.get(resourceType);
			double newAmount1 = oldAmount1 + samplesCollected;
			cumulativeCollectedByID.put(resourceType, newAmount1);
		}
		else {
			cumulativeCollectedByID.put(resourceType, samplesCollected);
		}
	}
	
	/**
	 * Gets the resources can be collected once on site. By default this is just
	 * the main resource but could be others.
	 * 
	 * @return
	 */
	public abstract int [] getCollectibleResources();

	@Override
	protected boolean performEVA(Person person) {

		Rover rover = getRover();
		double roverRemainingCap = rover.getCargoCapacity() - rover.getStoredMass();

		if (roverRemainingCap <= 0) {
			logger.info(getRover(), "No more room in " + rover.getName());
			addMissionLog("No remaining rover capacity");
			return false;
		}

		double weight = person.getMass();
		if (roverRemainingCap < weight) {
			logger.info(getRover(), "No enough capacity to fit " + person.getName() + "(" + weight + " kg).");
			addMissionLog("Rover capacity full");
			return false;
		}
		
		// Compute the collected resources on this person and the rover
		double amountCollectedAtSiteSoFar0 = getCollectedAtCurrentSite();
//		Note: computeTotalResources(person) does not work
		
//		logger.info(getRover(), 20_000, "amountCollectedAtSiteSoFar0: " +
//				Math.round(amountCollectedAtSiteSoFar0 * 100.0)/100.0);
		
		// If collected resources are sufficient for this site, end the collecting
		// phase.
		if (amountCollectedAtSiteSoFar0 >= siteResourceGoal) {
			logger.info(getRover(), "Full resources collected at site.");
			addMissionLog("Full resources collected");
			return false;
		}

		// Set the type of resource
		pickType(person);
		
		// Do the EVA task
		double rate = calculateRate(person);

		// Randomize the rate of collection upon arrival
		rate = rate
				* (1 + RandomUtil.getRandomDouble(-.2, .2));

		// Note: Add how areologists and some scientific study may come up with better technique
		// to obtain better estimation of the collection rate. Go to a prospective site, rather
		// than going to a site coordinate in the blind.

		if (!person.isEVAFit()) {
			logger.info(person, 4_000, "Not EVA fit to exit " + getRover() +  ".");
			// Note: How to take care of the person if he does not have high fatigue but other health issues ?
			boolean canSleep = assignTask(person, new Sleep(person));
        	if (canSleep) {
        		logger.log(person, Level.INFO, 4_000,
            			"Instructed to sleep in " + getVehicle() + ".");
        	}
        	// Do NOT return false or else it will end EVAMission for everyone
//			return false;
		}
		
		// If person can collect resources, start him/her on that task.
		if (CollectResources.canCollectResources(person, getRover(), containerID, resourceID)) {
			EVAOperation collectResources = new CollectResources(person,
					getRover(), resourceID, rate,
					siteResourceGoal - amountCollectedAtSiteSoFar0, 
					rover.getAmountResourceStored(resourceID),
					containerID, this);
			assignTask(person, collectResources);
		}

		return true;
	}

	/**
	 * EVA ended so update the mission resources.
	 */
	@Override
	protected void phaseEVAEnded() {
//		updateResources(getRover());
	}

	/**
	 * Signals the start of an EVA phase to do any housekeeping.
	 */
	@Override
	protected void phaseEVAStarted() {
		super.phaseEVAStarted();
	}

	/**
	 * Calculates the collection for for a worker.
	 * 
	 * @param worker
	 * @return
	 */
	protected abstract double calculateRate(Worker worker);

	/**
	 * Picks the type of resource.
	 * 
	 * @param worker
	 * @return
	 */
	protected abstract void pickType(Worker worker);
	
	/**
	 * Determines the locations of the sample collection sites.
	 *
	 * @param startingLocation Where to start from
	 * @param range the rover's driving range.
	 * @param numSites   the number of collection sites.
	 * @return List of fund sites to visit
	 * @throws MissionException of collection sites can not be determined.
	 */
	private List<Coordinates> determineCollectionSites(Coordinates startingLocation,
		double range, int numSites) {

		int confidence = 3 + (int)RandomUtil.getRandomDouble(getMarsTime().getMissionSol());

		List<Coordinates> unorderedSites = new ArrayList<>();

		double limit = 0;
		Direction direction = null;
		Coordinates newLocation = null;
		Coordinates currentLocation = null;
		int siteDistance = 0;

		// Determine the first collection site.
		double bestScore = 0;
		Coordinates bestLocation = null;
		int count = 0;
		while (count++ <= MAX_NUM_PRIMARY_SITES || bestScore == 0) {
			direction = new Direction(RandomUtil.getRandomDouble(2 * Math.PI));
			limit = range / 4D;
			siteDistance = RandomUtil.getRandomRegressionInteger(confidence, (int)limit);
			newLocation = startingLocation.getNewLocation(direction, siteDistance);

			double score = scoreLocation(newLocation);

			if (score > bestScore) {
				bestScore = score;
				bestLocation = newLocation;
			}
		}
		if (bestLocation.equals(startingLocation)) {
			throw new IllegalStateException("First site is at starting location");
		}
		totalSiteScore += bestScore;

		unorderedSites.add(bestLocation);
		currentLocation = bestLocation;

		// Determine remaining collection sites.
		double remainingRange = RandomUtil.getRandomDouble(range/2 - siteDistance);

		for (int x = 1; x < numSites; x++) {
			double currentDistanceToSettlement = Coordinates.computeDistance(currentLocation, startingLocation);
			if (remainingRange > currentDistanceToSettlement) {
				bestScore = 0;
				bestLocation = null;
				count = 0;
				while (count++ <= MAX_NUM_SECONDARY_SITES || bestScore == 0) {
					direction = new Direction(RandomUtil.getRandomDouble(2D * Math.PI));

					double tempLimit1 = Math.pow(remainingRange, 2D) - Math.pow(currentDistanceToSettlement, 2D);
					double tempLimit2 = (2D * remainingRange)
							- (2D * currentDistanceToSettlement * direction.getCosDirection());
					limit = tempLimit1 / tempLimit2;

					siteDistance = RandomUtil.getRandomRegressionInteger(confidence, (int)limit);
					newLocation = currentLocation.getNewLocation(direction, siteDistance);

					double score = scoreLocation(newLocation);

					if (score > bestScore) {
						bestScore = score;
						bestLocation = newLocation;
					}
				}

				totalSiteScore += bestScore;
				logger.log(Level.INFO, "Analyzing sites for " + getName() 
						+ ".  Total Score: " + Math.round(totalSiteScore * 100.0)/100.0
						+ ".  Best Score: " + Math.round(bestScore * 100.0)/100.0);
				unorderedSites.add(bestLocation);
				currentLocation = bestLocation;

				remainingRange -= siteDistance;
			}
		}
		return unorderedSites;
	}

	protected abstract double scoreLocation(Coordinates newLocation);

	/**
	 * Gets the estimated time spent at a collection site.
	 *
	 * @param useBuffer Use time buffer in estimation if true.
	 * @return time (millisols)
	 */
	@Override
	protected double getEstimatedTimeAtEVASite(boolean useBuffer) {
		double result = 500D;
		if (useBuffer) {
			result += MAX_WAIT_SUBLIGHT;
		}
 		return result;
	}

	/**
	 * Gets the computed site score of this prospective resource collection mission.
	 * 
	 * @param reviewSettlement
	 * @return
	 */
	@Override
	public double getTotalSiteScore(Settlement reviewSettlement) {
		return totalSiteScore;
	}
}
