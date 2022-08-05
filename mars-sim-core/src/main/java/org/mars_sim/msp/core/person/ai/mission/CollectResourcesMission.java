/*
 * Mars Simulation Project
 * CollectResourcesMission.java
 * @date 2022-07-16
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Direction;
import org.mars_sim.msp.core.equipment.ContainerUtil;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.equipment.ResourceHolder;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.CollectResources;
import org.mars_sim.msp.core.person.ai.task.EVAOperation;
import org.mars_sim.msp.core.person.ai.task.utils.Worker;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * The CollectResourcesMission class is a mission to travel in a rover to
 * several random locations around a settlement and collect resources of a given
 * type.
 */
public abstract class CollectResourcesMission extends EVAMission
	implements  SiteMission {


	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(CollectResourcesMission.class.getName());

	/** Mission phase. */
	public static final MissionPhase COLLECT_RESOURCES = new MissionPhase("Mission.phase.collectResources");

	private static final String PROPSPECTING_SITE = "Prospecting Site #";

	/** THe maximum number of sites under consideration. */
	private static final int MAX_NUM_PRIMARY_SITES = 30;
	/** THe maximum number of sites under consideration. */
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
	/** The starting amount of resources in a rover at a collection site. */
	private double collectingStart;
	/** The goal amount of resources to collect at a site (kg). */
	private double siteResourceGoal;

	/** The total amount (kg) of resources collected. */
	private Map<Integer, Double> collected;
	/** The type of container needed for the mission or null if none. */
	private EquipmentType containerID;

	/**
	 * Constructor
	 *
	 * @param startingPerson         The person starting the mission.
	 * @param resourceID           The type of resource.
	 * @param containerID          The type of container needed for the mission or
	 *                               null if none.
	 * @param containerNum           The number of containers needed for the
	 *                               mission.
	 * @param numSites               The number of collection sites.
	 * @param needsReview
	 * @param minPeople              The mimimum number of people for the mission.
	 * @throws MissionException if problem constructing mission.
	 */
	protected CollectResourcesMission(MissionType missionType, Person startingPerson, int resourceID,
			EquipmentType containerID, int containerNum, int numSites, boolean needsReview) {

		// Use RoverMission constructor
		super(missionType, startingPerson, null, COLLECT_RESOURCES);

		// Problem starting mission
		if (isDone()) {
			return;
		}
		
		// Too many members creates a congestion at the airlock during EVA
		if (getMissionCapacity() > MAX_PEOPLE) {
			setMissionCapacity(MAX_PEOPLE);
		}
		
		Settlement s = startingPerson.getSettlement();

		if (s != null) {
			setResourceID(resourceID);
			this.collected = new HashMap<>();
			this.containerID = containerID;
			setEVAEquipment(containerID, containerNum);

			// Recruit additional members to mission.
			if (!recruitMembersForMission(startingPerson, MIN_PEOPLE))
				return;

			// Determine collection sites
			if (hasVehicle()) {
				// Get the current location.
				Coordinates startingLocation = s.getCoordinates();
				double range = getVehicle().getRange(missionType);
				double timeLimit = getTotalTripTimeLimit(getRover(), getPeopleNumber(), true);

				// Determining the actual traveling range.
				double timeRange = getTripTimeRange(timeLimit, numSites, true);
				if (timeRange < range)
					range = timeRange;
				if (range <= 0D) {
					logger.warning(getVehicle(), "Has zero range for mission " + getName());
					endMission(MissionStatus.NO_AVAILABLE_VEHICLES);
					return;
				}

				// Find some sites
				List<Coordinates> unorderedSites = null;
				while (unorderedSites == null) {
					unorderedSites= determineCollectionSites(startingLocation,
							range,
							numSites);

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
				this.siteResourceGoal = 2 *containerCap * containerNum / orderSites.size();
				logger.info(getVehicle(), "Target amount of resource per site: "
						+ (int)siteResourceGoal + " kg of " + ResourceUtil.findAmountResourceName(resourceID)
						+ ".");
			}

			// Add home settlement for return
			addNavpoint(s);

			// Check if vehicle can carry enough supplies for the mission.
			if (hasVehicle() && !isVehicleLoadable()) {
				endMission(MissionStatus.CANNOT_LOAD_RESOURCES);
			}
		}

		if (!isDone()) {
			setInitialPhase(needsReview);
		}
	}

	/**
	 * Constructor with explicit data
	 *
	 * @param members                collection of mission members
	 * @param resourceID           The type of resource.
	 * @param containerID          The type of container needed for the mission or
	 *                               null if none.
	 * @param containerNum           The number of containers needed for the
	 *                               mission.
	 * @param minPeople              The mimimum number of people for the mission.
	 * @param rover                  the rover to use.
	 * @param collectionSites     the sites to collect ice.
	 */
	protected CollectResourcesMission(MissionType missionType, Collection<MissionMember> members,
			Integer resourceID, EquipmentType containerID,
			int containerNum, Rover rover, List<Coordinates> collectionSites) {

		// Use RoverMission constructor
		super(missionType, (MissionMember) members.toArray()[0], rover, COLLECT_RESOURCES);

		this.resourceID = resourceID;
		double containerCap = ContainerUtil.getContainerCapacity(containerID);
		this.siteResourceGoal = 2 * containerCap * containerNum / collectionSites.size();
		this.collected = new HashMap<>();
		this.containerID = containerID;
		setEVAEquipment(containerID, containerNum);

		// Set collection navpoints.
		addNavpoints(collectionSites, (i -> PROPSPECTING_SITE + (i+1)));

		// Add home navpoint.
		Settlement s = getStartingSettlement();
		addNavpoint(s);

		// Add mission members.
		addMembers(members, false);

		// Check if vehicle can carry enough supplies for the mission.
		if (hasVehicle() && !isVehicleLoadable()) {
			endMission(MissionStatus.CANNOT_LOAD_RESOURCES);
		}
		else {
			setInitialPhase(false);
		}
	}

	/**
	 * By default score is always accepted
	 * @param score This score may be use for further computation in overriding classes
	 * @return
	 */
	protected boolean isValidScore(double score) {
		return true;
	}

	protected void setResourceID(int newResource) {
		this.resourceID = newResource;
	}

	/**
	 * Gets the total amount of resources collected so far in the mission.
	 *
	 * @return resource amount (kg).
	 */
	public Map<Integer,Double> getResourcesCollected() {
		return collected;
	}


	/**
	 * Updates the resources collected
	 *
	 * @param inv
	 * @return
	 */
	private double updateResources(ResourceHolder inv) {
		double resourceCollected = 0;

		// Get capacity for all collectible resources. The collectible
		// resource at a site may be more than the single one specified.
		for(int resourceId : getCollectibleResources()) {
			double amount = inv.getAmountResourceStored(resourceId);
			resourceCollected += amount;
			collected.put(resourceId, amount);
		}

		// Calculate resources collected at the site so far.
		return resourceCollected - collectingStart;
	}

	/**
	 * what resources can be collected once on site. By default this is just
	 * the main resource but could be others.
	 * @return
	 */
	public abstract int [] getCollectibleResources();

	@Override
	protected boolean performEVA(Person person) {

		Rover rover = getRover();
		double roverRemainingCap = rover.getCargoCapacity() - rover.getStoredMass();

		double weight = person.getMass();
		if (roverRemainingCap < weight + 5) {
			addMissionLog("Rover capacity full");
			return false;
		}

		// This will update the siteCollectedResources and totalResourceCollected after the last on-site collection activity
		double siteCollectedSoFar = updateResources(rover);

		// If collected resources are sufficient for this site, end the collecting
		// phase.
		if (siteCollectedSoFar >= siteResourceGoal) {
			logger.info(getRover(), "Full resources collected at site");
			return false;
		}

		// Do the EVA task
		double rate = calculateRate(person);

		// Randomize the rate of collection upon arrival
		rate = rate
				* (1 + RandomUtil.getRandomDouble(-.2, .2));

		// Note: Add how areologists and some scientific study may come up with better technique
		// to obtain better estimation of the collection rate. Go to a prospective site, rather
		// than going to a site coordinate in the blind.

		// If person can collect resources, start him/her on that task.
		if (CollectResources.canCollectResources(person, getRover(), containerID, resourceID)) {
			EVAOperation collectResources = new CollectResources("Collecting Resources", person,
					getRover(), resourceID, rate,
					siteResourceGoal - siteCollectedSoFar, rover.getAmountResourceStored(resourceID),
					containerID);
			assignTask(person, collectResources);
		}

		return true;
	}

	/**
	 * EVA ended so update the mission resources.
	 */
	@Override
	protected void phaseEVAEnded() {
		updateResources(getRover());
	}

	/**
	 * Signak the start of an EVA phase to do any housekeeping
	 */
	@Override
	protected void phaseEVAStarted() {
		super.phaseEVAStarted();
		collectingStart = 0D;
	}

	/**
	 * Calculate the collection for for a worker.
	 * @param worker
	 * @return
	 */
	protected abstract double calculateRate(Worker worker);

	/**
	 * Determine the locations of the sample collection sites.
	 *
	 * @parma startingLocation Where to start from
	 * @param range the rover's driving range.
	 * @param numSites   the number of collection sites.
	 * @return List of fund sites to visit
	 * @throws MissionException of collection sites can not be determined.
	 */
	private List<Coordinates> determineCollectionSites(Coordinates startingLocation,
		double range, int numSites) {

		int confidence = 3 + (int)RandomUtil.getRandomDouble(marsClock.getMissionSol());

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
				logger.log(Level.INFO, getMissionType().getName() + " totalSiteScore: " + Math.round(totalSiteScore*1000.0)/1000.0
						+ "   bestScore: " + Math.round(bestScore*1000.0)/1000.0);
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
	 * @return
	 */
	@Override
	public double getTotalSiteScore(Settlement reviewSettlement) {
		return totalSiteScore;
	}
}
