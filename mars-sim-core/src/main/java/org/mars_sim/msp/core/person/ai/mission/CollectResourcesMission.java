/*
 * Mars Simulation Project
 * CollectResourcesMission.java
 * @date 2021-11-30
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
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
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * The CollectResourcesMission class is a mission to travel in a rover to
 * several random locations around a settlement and collect resources of a given
 * type.
 */
public abstract class CollectResourcesMission extends RoverMission
	implements Serializable, SiteMission {


	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(CollectResourcesMission.class.getName());

	/** Mission phase. */
	public static final MissionPhase COLLECT_RESOURCES = new MissionPhase("Mission.phase.collectResources");

	private static final String PROPSPECTING_SITE = "Prospecting Site #";

	/** Estimated collection time multiplier for EVA. */
	private static final double EVA_COLLECTION_OVERHEAD = 50D;

	/** THe maximum number of sites under consideration. */
	private static final int MAX_NUM_PRIMARY_SITES = 30;
	/** THe maximum number of sites under consideration. */
	private static final int MAX_NUM_SECONDARY_SITES = 5;

	/** Minimum number of people to do mission. */
	private static final int MIN_PEOPLE = 2;

	/** Upper limit of mission to avoid airlock congestion */
	private static final int MAX_PEOPLE = 6;

	// Data members
	/** External flag for ending collection at the current site. */
	private boolean endCollectingSite;
	/** The number of containers needed for the mission. */
	private int containerNum;
	/** The type of resource to collect. */
	protected int resourceID;
	/** The total site score of this prospective resource collection mission. */
	private double totalSiteScore;
	/** The amount of resources (kg) collected at a collection site. */
	private double siteCollectedResources;
	/** The starting amount of resources in a rover at a collection site. */
	private double collectingStart;
	/** The goal amount of resources to collect at a site (kg). */
	private double siteResourceGoal;
	/** The resource collection rate for a person (kg/millisol). */
	protected double resourceCollectionRate;


	/** The total amount (kg) of resources collected. */
	private Map<Integer, Double> collected;
	/** The type of container needed for the mission or null if none. */
	private EquipmentType containerID;

	/**
	 * Constructor
	 *
	 * @param missionName            The name of the mission.
	 * @param startingPerson         The person starting the mission.
	 * @param resourceID           The type of resource.
	 * @param resourceCollectionRate The resource collection rate for a person
	 *                               (kg/millisol).
	 * @param containerID          The type of container needed for the mission or
	 *                               null if none.
	 * @param containerNum           The number of containers needed for the
	 *                               mission.
	 * @param numSites               The number of collection sites.
	 * @param minPeople              The mimimum number of people for the mission.
	 * @throws MissionException if problem constructing mission.
	 */
	protected CollectResourcesMission(String missionName, MissionType missionType, Person startingPerson, int resourceID,
			double resourceCollectionRate, EquipmentType containerID, int containerNum, int numSites) {

		// Use RoverMission constructor
		super(missionName, missionType, startingPerson);

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
			this.resourceCollectionRate = resourceCollectionRate;
			this.containerID = containerID;
			this.containerNum = containerNum;
			this.collected = new HashMap<>();

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
					logger.warning(getVehicle(), "Has zero range for mission " + missionName);
					endMission(MissionStatus.NO_AVAILABLE_VEHICLES);
					return;
				}

				if (terrainElevation == null)
					terrainElevation = surfaceFeatures.getTerrainElevation();

				// Find some sites
				List<Coordinates> unorderedSites = null;
				while (unorderedSites == null) {
					unorderedSites= determineCollectionSites(startingLocation,
							range,
							numSites);

					if (!isValidScore(totalSiteScore)) {
						totalSiteScore = 0;
						unorderedSites = null;
						logger.warning(startingPerson, missionName + " attempt another collection site find");
					}

					// Mission might be aborted at determine site step
					if (isDone()) {
						logger.warning(startingPerson, missionName + " site searched & mission aborted");
						return;
					}
				}

				// Reorder sites for shortest distance and load
				List<Coordinates> orderSites = Exploration.getMinimalPath(startingLocation, unorderedSites);
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
			// Set initial mission phase.
			setPhase(REVIEWING, null);
		}
	}


	/**
	 * Constructor with explicit data
	 *
	 * @param missionName            The name of the mission.
	 * @param members                collection of mission members
	 * @param resourceID           The type of resource.
	 * @param resourceCollectionRate The resource collection rate for a person
	 *                               (kg/millisol).
	 * @param containerID          The type of container needed for the mission or
	 *                               null if none.
	 * @param containerNum           The number of containers needed for the
	 *                               mission.
	 * @param minPeople              The mimimum number of people for the mission.
	 * @param rover                  the rover to use.
	 * @param collectionSites     the sites to collect ice.
	 */
	protected CollectResourcesMission(String missionName, MissionType missionType, Collection<MissionMember> members,
			Integer resourceID, double resourceCollectionRate, EquipmentType containerID,
			int containerNum, Rover rover, List<Coordinates> collectionSites) {

		// Use RoverMission constructor
		super(missionName, missionType, (MissionMember) members.toArray()[0], rover);

		this.resourceID = resourceID;

		double containerCap = ContainerUtil.getContainerCapacity(containerID);
		this.siteResourceGoal = 2 * containerCap * containerNum / collectionSites.size();
		this.resourceCollectionRate = resourceCollectionRate;
		this.containerID = containerID;
		this.containerNum = containerNum;

		// Set collection navpoints.
		addNavpoints(collectionSites, (i -> PROPSPECTING_SITE + (i+1)));

		// Add home navpoint.
		Settlement s = getStartingSettlement();
		addNavpoint(s);

		// Add mission members.
		addMembers(members, false);

		// Set initial mission phase.
		setPhase(EMBARKING, s.getName());

		// Check if vehicle can carry enough supplies for the mission.
		if (hasVehicle() && !isVehicleLoadable()) {
			endMission(MissionStatus.CANNOT_LOAD_RESOURCES);
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
	 * Determines a new phase for the mission when the current phase has ended.
	 *
	 * @throws MissionException if problem setting a new phase.
	 */
	@Override
	protected boolean determineNewPhase() {
		boolean handled = true;
		if (!super.determineNewPhase()) {
			if (TRAVELLING.equals(getPhase())) {

				if (getCurrentNavpoint() == null)
					// go back home
					returnHome();
				else if (getCurrentNavpoint().isSettlementAtNavpoint()) {
					startDisembarkingPhase();
				}

				else if (canStartEVA()) {
					setPhase(COLLECT_RESOURCES,
							getCurrentNavpoint().getDescription());
				}
			}
			else if (WAIT_SUNLIGHT.equals(getPhase())) {
				setPhase(COLLECT_RESOURCES,
						getCurrentNavpoint().getDescription());
			}
			else if (COLLECT_RESOURCES.equals(getPhase())) {
				// Update the resource collected
				updateResources(getVehicle());
				startTravellingPhase();
			}
			else {
				handled  = false;
			}
		}
		return handled;
	}

	@Override
	protected void performPhase(MissionMember member) {
		super.performPhase(member);
		if (COLLECT_RESOURCES.equals(getPhase())) {
			collectingPhase(member);
		}
	}

	/**
	 * If collecting phase then stop all EVA
	 */
	@Override
	public void abortPhase() {
		if (COLLECT_RESOURCES.equals(getPhase())) {

			endCollectingSite = true;

			endAllEVA();
		}
		else 
			super.abortPhase();
	}

	/**
	 * Updates the resources collected
	 *
	 * @param inv
	 * @return
	 */
	private double updateResources(ResourceHolder inv) {
		double resourceCollected = 0;
		double resourcesCapacity = 0;

		// Get capacity for all collectible resources. The collectible
		// resource at a site may be more than the single one specified.
		for(int resourceId : getCollectibleResources()) {
			double amount = inv.getAmountResourceStored(resourceId);
			resourceCollected += amount;
			resourcesCapacity += inv.getAmountResourceCapacity(resourceId);

			collected.put(resourceId, amount);
		}

		// Calculate resources collected at the site so far.
		siteCollectedResources = resourceCollected - collectingStart;

		// Check if rover capacity for resources is met, then end this phase.
		if (resourceCollected >= resourcesCapacity) {
			setPhaseEnded(true);
		}

		return resourcesCapacity;
	}

	/**
	 * what resources can be collected once on site. By default this is just
	 * the main resource but could be others.
	 * @return
	 */
	public abstract int [] getCollectibleResources();

	/**
	 * Performs the collecting phase of the mission.
	 *
	 * @param member the mission member currently performing the mission
	 */
	private void collectingPhase(MissionMember member) {

		Rover rover = getRover();
		double roverRemainingCap = rover.getCargoCapacity() - rover.getStoredMass();

		double weight = ((Person)member).getMass();

		if (roverRemainingCap < weight + 5) {
			endCollectingSite = false;
			setPhaseEnded(true);
		}

		double resourcesCapacity = updateResources(rover);

		// Check if end collecting flag is set.
		if (endCollectingSite) {
			endCollectingSite = false;
			setPhaseEnded(true);
		}

		// If collected resources are sufficient for this site, end the collecting
		// phase.
		if (siteCollectedResources >= siteResourceGoal) {
			setPhaseEnded(true);
		}

		if (isEveryoneInRover()) {

			// Determine if no one can start the collect resources task.
			boolean nobodyCollect = true;
			Iterator<MissionMember> j = getMembers().iterator();
			while (j.hasNext() && nobodyCollect) {
				MissionMember m = j.next();
				for (Integer type : getCollectibleResources()) {
					if (CollectResources.canCollectResources(m, getRover(), containerID, type)) {
						nobodyCollect = false;
					}
				}
			}

			if (nobodyCollect || !isEnoughSunlightForEVA()) {
				logger.info(member, "Too dark for " + getPhaseDescription() + " of " + getTypeID()
									+ ", moving to next site");
				setPhaseEnded(true);
			}

			// Anyone in the crew or a single person at the home settlement has a dangerous
			// illness, end phase.
			if (hasEmergency()) {
				setPhaseEnded(true);
			}

			// Check if enough resources for remaining trip. false = not using margin.
			if (!hasEnoughResourcesForRemainingMission(false)) {
				// If not, determine an emergency destination.
				determineEmergencyDestination(member);
				setPhaseEnded(true);
			}
		}

		if (!getPhaseEnded()
				&& (siteCollectedResources < siteResourceGoal)
				&& !endCollectingSite
				&& member instanceof Person) {

			Person person = (Person) member;

			double rate = calculateRate(person);

			// Randomize the rate of collection upon arrival
			rate = rate
					* (1 + RandomUtil.getRandomDouble(-.2, .2));

			// Note: Add how areologists and some scientific study may come up with better technique
			// to obtain better estimation of the collection rate. Go to a prospective site, rather
			// than going to a site coordinate in the blind.

			if (rate > 20)
				resourceCollectionRate = rate;

			// If person can collect resources, start him/her on that task.
			if (CollectResources.canCollectResources(person, getRover(), containerID, resourceID)) {
				EVAOperation collectResources = new CollectResources("Collecting Resources", person,
						getRover(), resourceID, resourceCollectionRate,
						siteResourceGoal - siteCollectedResources, rover.getAmountResourceStored(resourceID),
						containerID);
				assignTask(person, collectResources);
			}
		}

		else {
			// If the rover is full of resources, head home.
			if (siteCollectedResources >= resourcesCapacity) {
				setNextNavpointIndex(getNumberOfNavpoints() - 2);
				updateTravelDestination();
				siteCollectedResources = 0D;
			}
		}

		// This will update the siteCollectedResources and totalResourceCollected after the last on-site collection activity
		updateResources(rover);
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

		/////////////////////////////////////////

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

		/////////////////////////////////////////

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
	 * Gets the range of a trip based on its time limit and collection sites.
	 *
	 * @param tripTimeLimit time (millisols) limit of trip.
	 * @param numSites      the number of collection sites.
	 * @param useBuffer     Use time buffer in estimations if true.
	 * @return range (km) limit.
	 */
	protected double getTripTimeRange(double tripTimeLimit, int numSites, boolean useBuffer) {
		double timeAtSites = getEstimatedTimeAtCollectionSite(useBuffer) * numSites;
		double tripTimeTravellingLimit = tripTimeLimit - timeAtSites;
		double averageSpeed = getAverageVehicleSpeedForOperators();
		double averageSpeedMillisol = averageSpeed / MarsClock.MILLISOLS_PER_HOUR;
		return tripTimeTravellingLimit * averageSpeedMillisol;
	}

	/**
	 * Gets the settlement associated with the mission.
	 *
	 * @return settlement or null if none.
	 */
	@Override
	public Settlement getAssociatedSettlement() {
		return getStartingSettlement();
	}

	/**
	 * Gets the estimated time remaining for the mission.
	 *
	 * @param useBuffer use time buffer in estimations if true.
	 * @return time (millisols)
	 * @throws MissionException
	 */
	@Override
	protected double getEstimatedRemainingMissionTime(boolean useBuffer) {
		double result = super.getEstimatedRemainingMissionTime(useBuffer);

		result += getEstimatedRemainingCollectionSiteTime(useBuffer);

		return result;
	}

	/**
	 * Gets the estimated time remaining for collection sites in the mission.
	 *
	 * @param useBuffer use time buffer in estimations if true.
	 * @return time (millisols)
	 * @throws MissionException if error estimating time.
	 */
	private double getEstimatedRemainingCollectionSiteTime(boolean useBuffer) {
		double result = 0D;

		// Add estimated remaining collection time at current site if still there.
		if (COLLECT_RESOURCES.equals(getPhase())) {
			double remainingTime = getEstimatedTimeAtCollectionSite(useBuffer) - getPhaseDuration();
			if (remainingTime > 0D)
				result += remainingTime;
		}

		// Add estimated collection time at sites that haven't been visited yet.
		int remainingCollectionSites = getNumCollectionSites() - getNumCollectionSitesVisited();
		result += getEstimatedTimeAtCollectionSite(useBuffer) * remainingCollectionSites;

		return result;
	}

	@Override
	protected Map<Integer, Number> getResourcesNeededForRemainingMission(boolean useBuffer) {
		// Note: currently, it has methane resource only
		Map<Integer, Number> result = super.getResourcesNeededForRemainingMission(useBuffer);

		double collectionSitesTime = getEstimatedRemainingCollectionSiteTime(useBuffer);
		double timeSols = collectionSitesTime / 1000D;

		int crewNum = getPeopleNumber();

		// Determine life support supplies needed for collection.
		addLifeSupportResources(result, crewNum, timeSols, useBuffer);

		return result;
	}

	@Override
	protected Map<Integer, Number> getSparePartsForTrip(double distance) {
		// Load the standard parts from VehicleMission.
		Map<Integer, Number> result = super.getSparePartsForTrip(distance);

		// Determine repair parts for EVA Suits.
		double evaTime = getEstimatedRemainingCollectionSiteTime(false);
		double numberAccidents = evaTime * getPeopleNumber() * EVAOperation.BASE_ACCIDENT_CHANCE;

		// Assume the average number malfunctions per accident is 1.5.
		double numberMalfunctions = numberAccidents * VehicleMission.AVERAGE_EVA_MALFUNCTION;

		result.putAll(super.getEVASparePartsForTrip(numberMalfunctions));

		return result;
	}

	/**
	 * Gets the total number of collection sites for this mission.
	 *
	 * @return number of sites.
	 */
	public final int getNumCollectionSites() {
		return getNumberOfNavpoints() - 2;
	}

	/**
	 * Gets the number of collection sites that have been currently visited by the
	 * mission.
	 *
	 * @return number of sites.
	 */
	public final int getNumCollectionSitesVisited() {
		int result = getCurrentNavpointIndex();
		if (result == (getNumberOfNavpoints() - 1))
			result -= 1;
		return result;
	}

	/**
	 * Gets the estimated time spent at a collection site.
	 *
	 * @param useBuffer Use time buffer in estimation if true.
	 * @return time (millisols)
	 */
	protected double getEstimatedTimeAtCollectionSite(boolean useBuffer) {
		double timePerPerson = 2 * siteResourceGoal / resourceCollectionRate;
		if (useBuffer)
			timePerPerson *= EVA_COLLECTION_OVERHEAD;
		return timePerPerson / getPeopleNumber();
	}

	@Override
	protected Map<Integer, Integer> getEquipmentNeededForRemainingMission(boolean useBuffer) {
		Map<Integer, Integer> result = new HashMap<>();

		// Include required number of containers.
		result.put(EquipmentType.getResourceID(containerID), containerNum);
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
