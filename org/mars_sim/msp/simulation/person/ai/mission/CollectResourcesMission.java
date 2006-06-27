/**
 * Mars Simulation Project
 * CollectResourcesMission.java
 * @version 2.79 2006-06-01
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai.mission;

import java.io.Serializable;
import java.util.*;

import org.mars_sim.msp.simulation.Coordinates;
import org.mars_sim.msp.simulation.Direction;
import org.mars_sim.msp.simulation.Inventory;
import org.mars_sim.msp.simulation.RandomUtil;
import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.simulation.Unit;
import org.mars_sim.msp.simulation.UnitIterator;
import org.mars_sim.msp.simulation.mars.Mars;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.person.ai.task.*;
import org.mars_sim.msp.simulation.resource.AmountResource;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.time.MarsClock;
import org.mars_sim.msp.simulation.vehicle.Rover;

/** 
 * The CollectResourcesMission class is a mission to travel in a rover to several
 * random locations around a settlement and collect resources of a given type.
 */
abstract class CollectResourcesMission extends RoverMission implements Serializable {

	// Mission phases
	final protected static String COLLECT_RESOURCES = "Collecting Resources";
	
	// Estimated collection time multiplyer for EVA.
	final private static double EVA_COLLECTION_OVERHEAD = 20D;

	// Data members
	protected Settlement startingSettlement; // The settlement the mission starts at.
	private AmountResource resourceType; // The type of resource to collect.
	// private int numCollectionSites; // The number of collection sites for the mission.
	private double siteCollectedResources; // The amount of resources (kg) collected at a collection site.
	private double collectingStart; // The starting amount of resources in a rover at a collection site.
	private double siteResourceGoal; // The goal amount of resources to collect at a site (kg).
	private double resourceCollectionRate; // The resource collection rate for a person (kg/millisol).
	private Class containerType; // The type of container needed for the mission or null if none.
	private int containerNum; // The number of containers needed for the mission.
	private MarsClock collectionSiteStartTime; // The start time at the current collection site.

	/**
	 * Constructor
	 * @param missionName The name of the mission.
	 * @param startingPerson The person starting the mission.
	 * @param resourceType The type of resource.
	 * @param siteResourceGoal The goal amount of resources to collect at a site (kg) (or 0 if none).
	 * @param resourceCollectionRate The resource collection rate for a person (kg/millisol).
	 * @param containerType The type of container needed for the mission or null if none.
	 * @param containerNum The number of containers needed for the mission.
	 * @param numSites The number of collection sites.
	 * @param minPeople The mimimum number of people for the mission.
	 * @throws MissionException if problem constructing mission.
	 */
	CollectResourcesMission(String missionName, Person startingPerson, AmountResource resourceType, 
			double siteResourceGoal, double resourceCollectionRate, Class containerType, 
			int containerNum, int numSites, int minPeople) throws MissionException {
		
		// Use RoverMission constructor
		super(missionName, startingPerson, minPeople);
		
		if (!isDone()) {
			
        	// Set mission capacity.
        	if (hasVehicle()) setMissionCapacity(getRover().getCrewCapacity());
		
			// Initialize data members.
			startingSettlement = startingPerson.getSettlement();
			this.resourceType = resourceType;
			this.siteResourceGoal = siteResourceGoal;
			this.resourceCollectionRate = resourceCollectionRate;
			this.containerType = containerType;
			this.containerNum = containerNum;
			
			// Recruit additional people to mission.
        	recruitPeopleForMission(startingPerson);
			
			// Determine collection sites
			try {
				if (hasVehicle()) determineCollectionSites(getVehicle().getRange(), getTotalTripTimeLimit(true), numSites);
			}
			catch (Exception e) {
				throw new MissionException(null, e);
			}
			
			// Add home settlement
			addNavpoint(new NavPoint(getAssociatedSettlement().getCoordinates(), 
					getAssociatedSettlement()));
        	
        	// Check if vehicle can carry enough supplies for the mission.
        	try {
        		if (hasVehicle() && !isVehicleLoadable()) endMission();
        	}
        	catch (Exception e) {
        		throw new MissionException(null, e);
        	}
		}
		
		// Add collecting phase.
		addPhase(COLLECT_RESOURCES);
		
		// Set initial mission phase.
		setPhase(VehicleMission.EMBARKING);
	}
	
    /**
     * Determines a new phase for the mission when the current phase has ended.
     * @throws MissionException if problem setting a new phase.
     */
    protected void determineNewPhase() throws MissionException {
    	if (EMBARKING.equals(getPhase())) setPhase(VehicleMission.TRAVELLING);
		else if (TRAVELLING.equals(getPhase())) {
			if (getCurrentNavpoint().isSettlementAtNavpoint()) setPhase(VehicleMission.DISEMBARKING);
			else {
				setPhase(COLLECT_RESOURCES);
				collectionSiteStartTime = (MarsClock) Simulation.instance().getMasterClock().getMarsClock().clone();
			}
		}
		else if (COLLECT_RESOURCES.equals(getPhase())) setPhase(VehicleMission.TRAVELLING);
		else if (DISEMBARKING.equals(getPhase())) endMission();
    }
	
    /**
     * The person performs the current phase of the mission.
     * @param person the person performing the phase.
     * @throws MissionException if problem performing the phase.
     */
    protected void performPhase(Person person) throws MissionException {
    	super.performPhase(person);
    	if (COLLECT_RESOURCES.equals(getPhase())) collectingPhase(person);
    }
	
	/** 
	 * Performs the collecting phase of the mission.
	 * @param person the person currently performing the mission
	 * @throws MissionException if problem performing collecting phase.
	 */
	private void collectingPhase(Person person) throws MissionException {
	
		Inventory inv = getRover().getInventory();
		double resourcesCollected = inv.getAmountResourceStored(resourceType);
		double resourcesCapacity = inv.getAmountResourceCapacity(resourceType);
	
		// Calculate resources collected at the site so far.
		siteCollectedResources = resourcesCollected - collectingStart;

		if (isEveryoneInRover()) {

			// Check if rover capacity for resources is met, then end this phase.
			if (resourcesCollected >= resourcesCapacity) setPhaseEnded(true);

			// If collected resources are sufficient for this site, end the collecting phase.
			if (siteCollectedResources >= siteResourceGoal) setPhaseEnded(true);

			// Determine if no one can start the collect resources task.
			boolean nobodyCollect = true;
			PersonIterator j = getPeople().iterator();
			while (j.hasNext()) {
				if (CollectResources.canCollectResources(j.next(), getRover())) nobodyCollect = false;
			}
	    
			// If no one can collect resources and this is not due to it just being
			// night time, end the collecting phase.
			Mars mars = Simulation.instance().getMars();
			boolean inDarkPolarRegion = mars.getSurfaceFeatures().inDarkPolarRegion(getRover().getCoordinates());
			double sunlight = mars.getSurfaceFeatures().getSurfaceSunlight(getRover().getCoordinates());
			if (nobodyCollect && ((sunlight > 0D) || inDarkPolarRegion)) setPhaseEnded(true);
			
			// Anyone in the crew or a single person at the home settlement has a dangerous illness, end phase.
			if (hasEmergency()) setPhaseEnded(true);
			
			try {
				// Check if enough resources for remaining trip.
				if (!hasEnoughResourcesForRemainingTrip(false)) {
					// If not, determine an emergency destination.
					determineEmergencyDestination();
					setPhaseEnded(true);
				}
			}
			catch (Exception e) {
				throw new MissionException(e.getMessage(), getPhase());
			}
		}

		if (!getPhaseEnded()) {
			if (siteCollectedResources < siteResourceGoal) {
				// If person can collect resources, start him/her on that task.
				if (CollectResources.canCollectResources(person, getRover())) {
					try {
						CollectResources collectResources = new CollectResources("Collecting Resources", person, 
							getRover(), resourceType, resourceCollectionRate, 
							siteResourceGoal - siteCollectedResources, inv.getAmountResourceStored(resourceType), containerType);
						assignTask(person, collectResources);
					}
					catch (Exception e) {
						throw new MissionException(COLLECT_RESOURCES, e);
					}
				}
			}
		}
		else {
			// If the rover is full of resources, head home.
			if (siteCollectedResources >= resourcesCapacity) {
				setNextNavpointIndex(getNumberOfNavpoints() - 2);
				siteCollectedResources = 0D;
				setPhaseEnded(true);
			}
		}
	}
	
	/** 
	 * Determine the locations of the sample collection sites.
	 * @param roverRange the rover's driving range
	 * @param numSites the number of collection sites
	 * @throws MissionException of collection sites can not be determined.
	 */
	private void determineCollectionSites(double roverRange, double tripTimeLimit, int numSites) throws MissionException {

		List unorderedSites = new ArrayList();
		
		// Determining the actual travelling range.
		double range = roverRange;
		double timeRange = getTripTimeRange(tripTimeLimit, numSites, true);
    	if (timeRange < range) range = timeRange;
        
		// Get the current location.
		Coordinates startingLocation = startingSettlement.getCoordinates();
        
		// Determine the first collection site.
		Direction direction = new Direction(RandomUtil.getRandomDouble(2 * Math.PI));
		double limit = roverRange / 4D;
		double siteDistance = RandomUtil.getRandomDouble(limit);
		Coordinates newLocation = startingLocation.getNewLocation(direction, siteDistance);
		unorderedSites.add(newLocation);
		Coordinates currentLocation = newLocation;
        
		// Determine remaining collection sites.
		double remainingRange = (roverRange / 2D) - siteDistance;
		for (int x=1; x < numSites; x++) {
			double currentDistanceToSettlement = currentLocation.getDistance(startingLocation);
			if (remainingRange > currentDistanceToSettlement) {
				direction = new Direction(RandomUtil.getRandomDouble(2D * Math.PI));
				double tempLimit1 = Math.pow(remainingRange, 2D) - Math.pow(currentDistanceToSettlement, 2D);
				double tempLimit2 = (2D * remainingRange) - (2D * currentDistanceToSettlement * direction.getCosDirection());
				limit = tempLimit1 / tempLimit2;
				siteDistance = RandomUtil.getRandomDouble(limit);
				newLocation = currentLocation.getNewLocation(direction, siteDistance);
				unorderedSites.add(newLocation);
				currentLocation = newLocation;
				remainingRange -= siteDistance;
			}
		}

		// Reorder sites for shortest distance.
		currentLocation = startingLocation;
		while (unorderedSites.size() > 0) {
			Coordinates shortest = (Coordinates) unorderedSites.get(0);
			Iterator i = unorderedSites.iterator();
			while (i.hasNext()) {
				Coordinates site = (Coordinates) i.next();
				if (currentLocation.getDistance(site) < currentLocation.getDistance(shortest)) 
					shortest = site;
			}
			addNavpoint(new NavPoint(shortest));
			unorderedSites.remove(shortest);
			currentLocation = shortest;
		}		
	}
	
	/**
	 * Gets the range of a trip based on its time limit and collection sites.
	 * @param tripTimeLimit time (millisols) limit of trip.
	 * @param numSites the number of collection sites.
	 * @param useBuffer Use time buffer in estimations if true.
	 * @return range (km) limit.
	 */
	private double getTripTimeRange(double tripTimeLimit, int numSites, boolean useBuffer) {
		double timeAtSites = getEstimatedTimeAtCollectionSite(useBuffer) * numSites;
		double tripTimeTravellingLimit = tripTimeLimit - timeAtSites;
    	double averageSpeed = getAverageVehicleSpeedForOperators();
    	double millisolsInHour = MarsClock.convertSecondsToMillisols(60D * 60D);
    	double averageSpeedMillisol = averageSpeed / millisolsInHour;
    	return tripTimeTravellingLimit * averageSpeedMillisol;
	}
	
	/**
	 * Gets the settlement associated with the mission.
	 * @return settlement or null if none.
	 */
	public Settlement getAssociatedSettlement() {
		return startingSettlement;
	}
	
	/**
	 * Checks to see if a person is capable of joining a mission.
	 * @param person the person to check.
	 * @return true if person could join mission.
	 */
	protected boolean isCapableOfMission(Person person) {
		if (super.isCapableOfMission(person)) {
			if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
				if (person.getSettlement() == startingSettlement) return true;
			}
		}
		return false;
	}
	
	/**
	 * Recruits new people into the mission.
	 * @param startingPerson the person starting the mission.
	 */
	protected void recruitPeopleForMission(Person startingPerson) {
		super.recruitPeopleForMission(startingPerson);
		
		// Make sure there is at least one person left at the starting settlement.
		if (!atLeastOnePersonRemainingAtSettlement(startingSettlement)) {
			// Remove last person added to the mission.
			Person lastPerson = (Person) getPeople().get(getPeopleNumber() - 1);
			if (lastPerson != null) {
				lastPerson.getMind().setMission(null);
				if (getPeopleNumber() < getMinPeople()) endMission();
			}
		}
	}
	
	/**
	 * Gets the number of empty containers of given type at the settlement.
	 * @param settlement the settlement
	 * @param containerType the type of container
	 * @return number of empty containers.
	 */
	protected static int numCollectingContainersAvailable(Settlement settlement, Class containerType) {
		Inventory inv = settlement.getInventory();
		int availableContainersNum = 0;
		UnitIterator i = inv.findAllUnitsOfClass(containerType).iterator();
		while (i.hasNext()) {
			Unit container = (Unit) i.next();
			if (container.getInventory().getTotalInventoryMass() == 0D) availableContainersNum ++;
		}
		
		return availableContainersNum; 
	}
	
    /**
     * Gets the estimated time remaining on the trip.
     * @param useBuffer Use time buffer in estimations if true.
     * @return time (millisols)
     * @throws Exception
     */
    public double getEstimatedRemainingTripTime(boolean useBuffer) throws Exception {
    	double result = super.getEstimatedRemainingTripTime(useBuffer);
    	
    	// Add estimated remaining collection time at current site if still there.
    	if (COLLECT_RESOURCES.equals(getPhase())) {
    		MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();
    		double timeSpentAtCollectionSite = MarsClock.getTimeDiff(currentTime, collectionSiteStartTime);
    		double remainingTime = getEstimatedTimeAtCollectionSite(useBuffer) - timeSpentAtCollectionSite;
    		if (remainingTime > 0D) result += remainingTime;
    	}
    	
    	// Add estimated collection time at sites that haven't been visited yet.
    	int remainingCollectionSites = getNumCollectionSites() - getNumCollectionSitesVisited();
    	result += getEstimatedTimeAtCollectionSite(useBuffer) * remainingCollectionSites;
    	
    	return result;
    }
    
    /**
     * Gets the total number of collection sites for this mission.
     * @return number of sites.
     */
    public int getNumCollectionSites() {
    	return getNumberOfNavpoints() - 2;
    }
    
    /**
     * Gets the number of collection sites that have been currently visited by the mission.
     * @return number of sites.
     */
    public int getNumCollectionSitesVisited() {
    	int result = getCurrentNavpointIndex();
    	if (result == (getNumberOfNavpoints() - 1)) result -= 1;
    	return result;
    }
    
    /**
     * Gets the estimated time spent at a collection site.
     * @param useBuffer Use time buffer in estimation if true.
     * @return time (millisols)
     */
    protected double getEstimatedTimeAtCollectionSite(boolean useBuffer) {
    	double timePerPerson =  siteResourceGoal / resourceCollectionRate;
    	if (useBuffer) timePerPerson *= EVA_COLLECTION_OVERHEAD;
    	double result =  timePerPerson / getPeopleNumber();
    	return result;
    }
    
    /**
     * Gets the time limit of the trip based on life support capacity.
     * @param useBuffer use time buffer in estimation if true.
     * @return time (millisols) limit.
     * @throws Exception if error determining time limit.
     */
    public double getTotalTripTimeLimit(boolean useBuffer) throws Exception {
    	
    	int crewNum = getPeopleNumber();
    	Inventory vInv = getVehicle().getInventory();
    	
    	double timeLimit = Double.MAX_VALUE;
    	
    	PersonConfig config = Simulation.instance().getSimConfig().getPersonConfiguration();
		
    	// Check food capacity as time limit.
    	double foodConsumptionRate = config.getFoodConsumptionRate();
    	double foodCapacity = vInv.getAmountResourceCapacity(AmountResource.FOOD);
    	double foodTimeLimit = foodCapacity / (foodConsumptionRate * crewNum);
    	if (foodTimeLimit < timeLimit) timeLimit = foodTimeLimit;
    		
    	// Check water capacity as time limit.
    	double waterConsumptionRate = config.getWaterConsumptionRate();
    	double waterCapacity = vInv.getAmountResourceCapacity(AmountResource.WATER);
    	double waterTimeLimit = waterCapacity / (waterConsumptionRate * crewNum);
    	if (waterTimeLimit < timeLimit) timeLimit = waterTimeLimit;
    		
    	// Check oxygen capacity as time limit.
    	double oxygenConsumptionRate = config.getOxygenConsumptionRate();
    	double oxygenCapacity = vInv.getAmountResourceCapacity(AmountResource.OXYGEN);
    	double oxygenTimeLimit = oxygenCapacity / (oxygenConsumptionRate * crewNum);
    	if (oxygenTimeLimit < timeLimit) timeLimit = oxygenTimeLimit;
    	
    	// Convert timeLimit into millisols and use error margin.
    	timeLimit = (timeLimit * 1000D);
    	if (useBuffer) timeLimit /= Rover.LIFE_SUPPORT_RANGE_ERROR_MARGIN;
    	
    	return timeLimit;
    }
    
    /**
     * Gets the number and types of equipment needed for the mission.
     * @param useBuffer use time buffer in estimation if true.
     * @return map of equipment class and Integer number.
     * @throws Exception if error determining needed equipment.
     */
    public Map getEquipmentNeededForRemainingMission(boolean useBuffer) throws Exception {
    	if (equipmentNeededCache != null) return equipmentNeededCache;
    	else {
    		Map result = super.getEquipmentNeededForRemainingMission(useBuffer);
    	
    		// Include required number of containers.
    		result.put(containerType, new Integer(containerNum));
    	
    		equipmentNeededCache = result;
    		return result;
    	}
    }
}