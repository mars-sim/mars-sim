/*
 * Mars Simulation Project
 * CollectResourcesMission.java
 * @date 2024-07-12
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.mission;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.environment.TerrainElevation;
import com.mars_sim.core.equipment.ContainerUtil;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.map.location.Direction;
import com.mars_sim.core.mission.objectives.CollectResourceObjective;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.task.CollectResources;
import com.mars_sim.core.person.ai.task.EVAOperation;
import com.mars_sim.core.person.ai.task.Sleep;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.RandomUtil;
import com.mars_sim.core.vehicle.Rover;

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
	private static final int NUM_EVA_PER_SITE = 6;
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
	private int resourceID;
	/** The total site score of this prospective resource collection mission. */
	private double totalSiteScore;

	private CollectResourceObjective objective;

	/** The type of container needed for the mission or null if none. */
	private EquipmentType containerID;

	private static TerrainElevation terrainElevation;
	
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
		
		this.resourceID = resourceID;
		this.containerID = containerID;
		
		
		// Recruit additional members to mission.
		if (!recruitMembersForMission(startingPerson, MIN_PEOPLE)) {
			logger.warning(getVehicle(), "Not enough members recruited for mission " 
					+ getName() + ".");
			endMission(NOT_ENOUGH_MEMBERS);
			return;
		}
		
		// Check vehicle
		if (!hasVehicle()) {
			return;
		}
			
		// Get the current location.
		Coordinates startingLocation = s.getCoordinates();
		double range = getVehicle().getEstimatedRange();
		double timeLimit = getRover().getTotalTripTimeLimit(true);

		// Determining the actual traveling range.
		double timeRange = getTripTimeRange(timeLimit, numSites, true);
		if (timeRange < range)
			range = timeRange;
		if (range <= 0D) {
			logger.warning(getVehicle(), "Zero range for mission " 
					+ getName() + ".");
			endMission(NO_AVAILABLE_VEHICLE);
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

		// Add home settlement for return
		addNavpoint(s);
		setObjectives(containerNum, orderSites.size());

		// Check if vehicle can carry enough supplies for the mission.
		if (hasVehicle() && !isVehicleLoadable()) {
			endMission(CANNOT_LOAD_RESOURCES);
		}

		if (!isDone()) {
			setInitialPhase(needsReview);
		}
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
		this.containerID = containerID;
		
		// Set collection navpoints.
		addNavpoints(collectionSites, (i -> PROPSPECTING_SITE + (i+1)));

		// Add home navpoint.
		addNavpoint(getStartingSettlement());
		
		// Add mission members.
		addMembers(members, false);

		// Setup objectives
		setObjectives(containerNum, collectionSites.size());

		// Check if vehicle can carry enough supplies for the mission.
		if (hasVehicle() && !isVehicleLoadable()) {
			endMission(CANNOT_LOAD_RESOURCES);
		}
		else {
			setInitialPhase(false);
		}
	}

 	private void setObjectives(int containerNum, double numberSites) {
		int numMembers = (getMissionCapacity() + getMembers().size()) / 2;
		int buffer = (int)(numMembers * 1.5);
		int newContainerNum = Math.min(buffer, containerNum);
	
		setEVAEquipment(containerID, newContainerNum);

		double containerCap = ContainerUtil.getContainerCapacity(containerID);
		var siteResourceGoal = NUM_EVA_PER_SITE * containerCap * containerNum / numberSites;

		objective = new CollectResourceObjective(siteResourceGoal);
		addObjective(objective);
	}

	private static TerrainElevation getTerrainElevation() {
		if (terrainElevation == null) {
			terrainElevation = Simulation.instance().getSurfaceFeatures().getTerrainElevation();
		}
		return terrainElevation;
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
	 * Gets the amount collected at the current site.
	 * 
	 * @return
	 */
	public double getCollectedAtCurrentSite() {
		int siteIndex = getCurrentNavpointIndex(); 
		return objective.getCollectedAtSites().getOrDefault(siteIndex, 0D);
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
		objective.recordResourceCollected(siteIndex, resourceType, samplesCollected);
	}

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
			logger.warning(getRover(), "No enough capacity to fit " + person.getName() + "(" + weight + " kg).");
			addMissionLog("Rover capacity full");
			return false;
		}
		
		// Compute the collected resources on this person and the rover
		double amountCollectedAtSiteSoFar0 = getCollectedAtCurrentSite();

		
		// If collected resources are sufficient for this site, end the collecting
		// phase.
		if (amountCollectedAtSiteSoFar0 >= objective.getSiteResourceGoal()) {
			addMissionLog("Full resources collected");
			return false;
		}

		// Note: Add how areologists and some scientific study may come up with better technique
		// to obtain better estimation of the collection rate. Go to a prospective site, rather
		// than going to a site coordinate in the blind.
	
		Task currentTask = person.getMind().getTaskManager().getTask();

		if (currentTask != null && currentTask.getName().equals(Sleep.NAME)) {
			// Leave him alone and do NOT assign him any tasks
			logger.info(person, 4_000,
        			"Asleep in " + person.getContainerUnit() + " and not to be disturbed.");
		}
		
		else if (!person.isEVAFit()) {
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
		else if (CollectResources.canCollectResources(person, getRover(), containerID, resourceID)) {		
			// Set the type of resource
			resourceID = pickType(person);
			
			// Randomize the rate of collection upon arrival
			double rate = scoreLocation(getTerrainElevation(), person.getCoordinates())
					* (1 + RandomUtil.getRandomDouble(-.2, .2));
			
			EVAOperation collectResources = new CollectResources(person,
					getRover(), resourceID, rate,
					objective.getSiteResourceGoal() - amountCollectedAtSiteSoFar0, 
					rover.getAmountResourceStored(resourceID),
					containerID, this);
			
			assignTask(person, collectResources);
		}

		return true;
	}

	/**
	 * Picks the type of resource.
	 * 
	 * @param worker
	 * @return
	 */
	protected abstract int pickType(Worker worker);
	
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

			double score = scoreLocation(getTerrainElevation(), newLocation);

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
			double currentDistanceToSettlement = currentLocation.getDistance(startingLocation);
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

					double score = scoreLocation(getTerrainElevation(), newLocation);

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

	protected abstract double scoreLocation(TerrainElevation terrain, Coordinates newLocation);

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
