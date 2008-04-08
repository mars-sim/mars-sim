/**
 * Mars Simulation Project
 * Exploration.java
 * @version 2.84 2008-04-08
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai.mission;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.mars_sim.msp.simulation.Coordinates;
import org.mars_sim.msp.simulation.Direction;
import org.mars_sim.msp.simulation.Inventory;
import org.mars_sim.msp.simulation.InventoryException;
import org.mars_sim.msp.simulation.RandomUtil;
import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.simulation.SimulationConfig;
import org.mars_sim.msp.simulation.equipment.EVASuit;
import org.mars_sim.msp.simulation.equipment.SpecimenContainer;
import org.mars_sim.msp.simulation.mars.Mars;
import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.person.PersonConfig;
import org.mars_sim.msp.simulation.person.PhysicalCondition;
import org.mars_sim.msp.simulation.person.ai.job.Job;
import org.mars_sim.msp.simulation.person.ai.task.CollectResources;
import org.mars_sim.msp.simulation.person.ai.task.Task;
import org.mars_sim.msp.simulation.resource.AmountResource;
import org.mars_sim.msp.simulation.resource.Resource;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.time.MarsClock;
import org.mars_sim.msp.simulation.vehicle.Rover;
import org.mars_sim.msp.simulation.vehicle.Vehicle;


/** 
 * The Exploration class is a mission to travel in a rover to several
 * random locations around a settlement and collect rock samples.
 */
public class Exploration extends RoverMission {

	private static String CLASS_NAME = 
		"org.mars_sim.msp.simulation.person.ai.mission.CollectResourcesMission";
	
	private static Logger logger = Logger.getLogger(CLASS_NAME);
	
	// Default description.
	public static final String DEFAULT_DESCRIPTION = "Exploration";
	
	// Mission phases
	final public static String COLLECT_RESOURCES = "Collecting Resources";
	
	// Amount of rock samples to be gathered at a given site (kg). 
	public static final double SITE_GOAL = 40D;
	
	// Collection rate of rock samples during EVA (kg/millisol).
	public static final double COLLECTION_RATE = .1D;
	
	// Number of specimen containers required for the mission. 
	public static final int REQUIRED_SPECIMEN_CONTAINERS = 20;
	
	//	Number of collection sites.
	private static final int NUM_SITES = 5;
	
	// Minimum number of people to do mission.
	private final static int MIN_PEOPLE = 2;
	
	// Data members
	private AmountResource resourceType; // The type of resource to collect.
	private double siteCollectedResources; // The amount of resources (kg) collected at a collection site.
	private double collectingStart; // The starting amount of resources in a rover at a collection site.
	private double siteResourceGoal; // The goal amount of resources to collect at a site (kg).
	private double resourceCollectionRate; // The resource collection rate for a person (kg/millisol).
	private Class containerType; // The type of container needed for the mission or null if none.
	private int containerNum; // The number of containers needed for the mission.
	private MarsClock collectionSiteStartTime; // The start time at the current collection site.
	private boolean endCollectingSite; // External flag for ending collection at the current site.

	/**
	 * Constructor
	 * @param startingPerson the person starting the mission.
	 * @throws MissionException if problem constructing mission.
	 */
	public Exploration(Person startingPerson) throws MissionException {
		
		// Use RoverMission constructor.
		super(DEFAULT_DESCRIPTION, startingPerson, MIN_PEOPLE);
		
		if (!isDone()) {
			
        	// Set mission capacity.
        	if (hasVehicle()) setMissionCapacity(getRover().getCrewCapacity());
        	int availableSuitNum = VehicleMission.getNumberAvailableEVASuitsAtSettlement(startingPerson.getSettlement());
        	if (availableSuitNum < getMissionCapacity()) setMissionCapacity(availableSuitNum);
		
			// Initialize data members.
			setStartingSettlement(startingPerson.getSettlement());
			this.resourceType = AmountResource.ROCK_SAMPLES;
			this.siteResourceGoal = SITE_GOAL;
			this.resourceCollectionRate = COLLECTION_RATE;
			this.containerType = SpecimenContainer.class;
			this.containerNum = REQUIRED_SPECIMEN_CONTAINERS;
			
			// Recruit additional people to mission.
        	recruitPeopleForMission(startingPerson);
			
			// Determine collection sites
			try {
				if (hasVehicle()) determineCollectionSites(getVehicle().getRange(), getTotalTripTimeLimit(getRover(), 
						getPeopleNumber(), true), NUM_SITES);
			}
			catch (Exception e) {
				throw new MissionException(null, e);
			}
			
			// Add home settlement
			addNavpoint(new NavPoint(getStartingSettlement().getCoordinates(), 
					getStartingSettlement(), getStartingSettlement().getName()));
			
        	// Check if vehicle can carry enough supplies for the mission.
        	try {
        		if (hasVehicle() && !isVehicleLoadable()) endMission("Vehicle is not loadable. (CollectingResourcesMission)");
        	}
        	catch (Exception e) {
        		throw new MissionException(null, e);
        	}
		}
		
		// Add collecting phase.
		addPhase(COLLECT_RESOURCES);
		
		// Set initial mission phase.
		setPhase(VehicleMission.EMBARKING);
		setPhaseDescription("Embarking from " + getStartingSettlement().getName());
		
		// int emptyContainers = numCollectingContainersAvailable(getStartingSettlement(), containerType);
		// logger.info("Starting " + getName() + " with " + emptyContainers + " " + containerType);
	}
	
    /**
     * Constructor with explicit data.
     * @param members collection of mission members.
     * @param startingSettlement the starting settlement.
     * @param explorationSites the sites to explore.
     * @param rover the rover to use.
     * @param description the mission's description.
     * @throws MissionException if error constructing mission.
     */
    public Exploration(Collection<Person> members, Settlement startingSettlement, 
    		List explorationSites, Rover rover, String description) throws MissionException {
    	
       	// Use RoverMission constructor.
    	super(description, (Person) members.toArray()[0], 1, rover);
    	
		setStartingSettlement(startingSettlement);
		
		// Set mission capacity.
		setMissionCapacity(getRover().getCrewCapacity());
		int availableSuitNum = VehicleMission.getNumberAvailableEVASuitsAtSettlement(startingSettlement);
    	if (availableSuitNum < getMissionCapacity()) setMissionCapacity(availableSuitNum);
    	
		this.resourceType = AmountResource.ROCK_SAMPLES;
		this.siteResourceGoal = SITE_GOAL;
		this.resourceCollectionRate = COLLECTION_RATE;
		this.containerType = SpecimenContainer.class;
		this.containerNum = REQUIRED_SPECIMEN_CONTAINERS;
		
		// Set collection navpoints.
		for (int x = 0; x < explorationSites.size(); x++) 
			addNavpoint(new NavPoint((Coordinates) explorationSites.get(x), getCollectionSiteDescription(x + 1)));
		
		// Add home navpoint.
		addNavpoint(new NavPoint(startingSettlement.getCoordinates(), startingSettlement, startingSettlement.getName()));
		
    	// Add mission members.
    	Iterator<Person> i = members.iterator();
    	while (i.hasNext()) i.next().getMind().setMission(this);
    	
		// Add collecting phase.
		addPhase(COLLECT_RESOURCES);
		
		// Set initial mission phase.
		setPhase(VehicleMission.EMBARKING);
		setPhaseDescription("Embarking from " + getStartingSettlement().getName());

       	// Check if vehicle can carry enough supplies for the mission.
       	try {
       		if (hasVehicle() && !isVehicleLoadable()) endMission("Vehicle is not loadable. (CollectingResourcesMission)");
       	}
       	catch (Exception e) {
       		throw new MissionException(null, e);
       	}
    }

	/** 
	 * Gets the weighted probability that a given person would start this mission.
	 * @param person the given person
	 * @return the weighted probability
	 */
	public static double getNewMissionProbability(Person person) {
		
		double result = 0D;
		
		if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
			Settlement settlement = person.getSettlement();
	    
			// Check if a mission-capable rover is available.
			boolean reservableRover = areVehiclesAvailable(settlement);
			
			// Check if minimum number of people are available at the settlement.
			// Plus one to hold down the fort.
			boolean minNum = minAvailablePeopleAtSettlement(settlement, (MIN_PEOPLE + 1));
			
			// Check if there are enough specimen containers at the settlement for collecting rock samples.
			boolean enoughContainers = false;
			try {
				enoughContainers = (numCollectingContainersAvailable(settlement, SpecimenContainer.class) >= REQUIRED_SPECIMEN_CONTAINERS);
			}
			catch (Exception e) {
				e.printStackTrace(System.err);
			}
			
			// Check for embarking missions.
			boolean embarkingMissions = VehicleMission.hasEmbarkingMissions(settlement);
	    
			if (reservableRover && minNum && enoughContainers && !embarkingMissions) result = 5D;
			
			// Crowding modifier
			int crowding = settlement.getCurrentPopulationNum() - settlement.getPopulationCapacity();
			if (crowding > 0) result *= (crowding + 1);		
			
			// Job modifier.
			Job job = person.getMind().getJob();
			if (job != null) result *= job.getStartMissionProbabilityModifier(Exploration.class);	
		}
		
		if (result > 0D) {
			// Check if min number of EVA suits at settlement.
			if (VehicleMission.getNumberAvailableEVASuitsAtSettlement(person.getSettlement()) < MIN_PEOPLE) result = 0D;
		}
		
		return result;
	}
	
    @Override
    protected void determineNewPhase() throws MissionException {
    	if (EMBARKING.equals(getPhase())) {
    		startTravelToNextNode();
    		setPhase(VehicleMission.TRAVELLING);
    		setPhaseDescription("Driving to " + getNextNavpoint().getDescription());
    	}
		else if (TRAVELLING.equals(getPhase())) {
			if (getCurrentNavpoint().isSettlementAtNavpoint()) {
				setPhase(VehicleMission.DISEMBARKING);
				setPhaseDescription("Disembarking at " + getCurrentNavpoint().getSettlement().getName());
			}
			else {
				setPhase(COLLECT_RESOURCES);
				setPhaseDescription("Collecting resources at " + getCurrentNavpoint().getDescription());
				collectionSiteStartTime = (MarsClock) Simulation.instance().getMasterClock().getMarsClock().clone();
			}
		}
		else if (COLLECT_RESOURCES.equals(getPhase())) {
			startTravelToNextNode();
			setPhase(VehicleMission.TRAVELLING);
			setPhaseDescription("Driving to " + getNextNavpoint().getDescription());
		}
		else if (DISEMBARKING.equals(getPhase())) endMission("Successfully disembarked.");
    }
    
    @Override
    protected void performPhase(Person person) throws MissionException {
    	super.performPhase(person);
    	if (COLLECT_RESOURCES.equals(getPhase())) collectingPhase(person);
    }
    
    public void endCollectingAtSite() {
    	logger.info("Collecting phase ended due to external trigger.");
    	endCollectingSite = true;
    	
    	// End each member's collection task.
    	Iterator<Person> i = getPeople().iterator();
    	while (i.hasNext()) {
    		Task task = i.next().getMind().getTaskManager().getTask();
    		if (task instanceof CollectResources) 
    			((CollectResources) task).endEVA();
    	}
    }
    
	/** 
	 * Performs the collecting phase of the mission.
	 * @param person the person currently performing the mission
	 * @throws MissionException if problem performing collecting phase.
	 */
	private final void collectingPhase(Person person) throws MissionException {
		Inventory inv = getRover().getInventory();
		double resourcesCollected = 0D;
		double resourcesCapacity = 0D;
		try {
			resourcesCollected = inv.getAmountResourceStored(resourceType);
			resourcesCapacity = inv.getAmountResourceCapacity(resourceType);
		}
		catch (InventoryException e) {
			throw new MissionException(getPhase(), e);
		}
	
		// Calculate resources collected at the site so far.
		siteCollectedResources = resourcesCollected - collectingStart;

		if (isEveryoneInRover()) {

			// Check if end collecting flag is set.
			if (endCollectingSite) {
				endCollectingSite = false;
				setPhaseEnded(true);
			}
			
			// Check if rover capacity for resources is met, then end this phase.
			if (resourcesCollected >= resourcesCapacity) setPhaseEnded(true);

			// If collected resources are sufficient for this site, end the collecting phase.
			if (siteCollectedResources >= siteResourceGoal) setPhaseEnded(true);

			// Determine if no one can start the collect resources task.
			boolean nobodyCollect = true;
			Iterator<Person> j = getPeople().iterator();
			while (j.hasNext()) {
				if (CollectResources.canCollectResources(j.next(), getRover(), containerType, resourceType)) 
					nobodyCollect = false;
			}
	    
			// If no one can collect resources and this is not due to it just being
			// night time, end the collecting phase.
			try {
				Mars mars = Simulation.instance().getMars();
				boolean inDarkPolarRegion = mars.getSurfaceFeatures().inDarkPolarRegion(getCurrentMissionLocation());
				double sunlight = mars.getSurfaceFeatures().getSurfaceSunlight(getCurrentMissionLocation());
				if (nobodyCollect && ((sunlight > 0D) || inDarkPolarRegion)) setPhaseEnded(true);
			} 
			catch (Exception e) {
				throw new MissionException(getPhase(), e);
			}
			
			// Anyone in the crew or a single person at the home settlement has a dangerous illness, end phase.
			if (hasEmergency()) setPhaseEnded(true);
			
			try {
				// Check if enough resources for remaining trip.
				if (!hasEnoughResourcesForRemainingMission(false)) {
					// If not, determine an emergency destination.
					determineEmergencyDestination(person);
					setPhaseEnded(true);
				}
			}
			catch (Exception e) {
				throw new MissionException(e.getMessage(), getPhase());
			}
		}

		if (!getPhaseEnded()) {
			if ((siteCollectedResources < siteResourceGoal) && !endCollectingSite) {
				// If person can collect resources, start him/her on that task.
				if (CollectResources.canCollectResources(person, getRover(), containerType, resourceType)) {
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
				updateTravelDestination();
				siteCollectedResources = 0D;
			}
		}
	}
	
	@Override
	protected boolean isCapableOfMission(Person person) {
		if (super.isCapableOfMission(person)) {
			if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
				if (person.getSettlement() == getStartingSettlement()) return true;
			}
		}
		return false;
	}
	
	@Override
	protected void recruitPeopleForMission(Person startingPerson) {
		super.recruitPeopleForMission(startingPerson);
		
		// Make sure there is at least one person left at the starting settlement.
		if (!atLeastOnePersonRemainingAtSettlement(getStartingSettlement(), startingPerson)) {
			// Remove last person added to the mission.
			Person lastPerson = (Person) getPeople().toArray()[getPeopleNumber() - 1];
			if (lastPerson != null) {
				lastPerson.getMind().setMission(null);
				if (getPeopleNumber() < getMinPeople()) endMission("Not enough members.");
			}
		}
	}
	
	@Override
    public double getEstimatedRemainingMissionTime(boolean useBuffer) throws Exception {
    	double result = super.getEstimatedRemainingMissionTime(useBuffer);
    	
    	result += getEstimatedRemainingCollectionSiteTime(useBuffer);
    	
    	return result;
    }
	
    /**
     * Gets the estimated time remaining for collection sites in the mission.
     * @param useBuffer use time buffer in estimations if true.
     * @return time (millisols)
     * @throws Exception if error estimating time.
     */
    private final double getEstimatedRemainingCollectionSiteTime(boolean useBuffer) throws Exception {
    	double result = 0D;
    	
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
    
    @Override
    public Map<Resource, Number> getResourcesNeededForRemainingMission(boolean useBuffer, boolean parts) throws Exception {
    	Map<Resource, Number> result = super.getResourcesNeededForRemainingMission(useBuffer, parts);
    	
    	double collectionSitesTime = getEstimatedRemainingCollectionSiteTime(useBuffer);
    	double timeSols = collectionSitesTime / 1000D;
    	
    	int crewNum = getPeopleNumber();
    	
    	// Determine life support supplies needed for trip.
    	double oxygenAmount = PhysicalCondition.getOxygenConsumptionRate() * timeSols * crewNum;
    	if (result.containsKey(AmountResource.OXYGEN)) 
    		oxygenAmount += ((Double) result.get(AmountResource.OXYGEN)).doubleValue();
    	result.put(AmountResource.OXYGEN, new Double(oxygenAmount));
    		
    	double waterAmount = PhysicalCondition.getWaterConsumptionRate() * timeSols * crewNum;
    	if (result.containsKey(AmountResource.WATER)) 
    		waterAmount += ((Double) result.get(AmountResource.WATER)).doubleValue();
    	result.put(AmountResource.WATER, new Double(waterAmount));
    		
    	double foodAmount = PhysicalCondition.getFoodConsumptionRate() * timeSols * crewNum;
    	if (result.containsKey(AmountResource.FOOD)) 
    		foodAmount += ((Double) result.get(AmountResource.FOOD)).doubleValue();
    	result.put(AmountResource.FOOD, new Double(foodAmount));
    	
    	return result;
    }
	
	@Override
	public Settlement getAssociatedSettlement() {
		return getStartingSettlement();
	}
	
	@Override
	protected int compareVehicles(Vehicle firstVehicle, Vehicle secondVehicle) throws Exception {
		int result = super.compareVehicles(firstVehicle, secondVehicle);
		
		// Check of one rover has a research lab and the other one doesn't.
		if ((result == 0) && (isUsableVehicle(firstVehicle)) && (isUsableVehicle(secondVehicle))) {
			boolean firstLab = ((Rover) firstVehicle).hasLab();
			boolean secondLab = ((Rover) secondVehicle).hasLab();
			if (firstLab && !secondLab) result = 1;
			else if (!firstLab && secondLab) result = -1;
		}
		
		return result;
	}
    
    /**
     * Gets the estimated time spent at a collection site.
     * @param useBuffer use time buffers in estimation if true.
     * @return time (millisols)
     */
    protected double getEstimatedTimeAtCollectionSite(boolean useBuffer) {
    	double result = 0D;
    	
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
    public final int getNumCollectionSites() {
    	return getNumberOfNavpoints() - 2;
    }
    
    /**
     * Gets the number of collection sites that have been currently visited by the mission.
     * @return number of sites.
     */
    public final int getNumCollectionSitesVisited() {
    	int result = getCurrentNavpointIndex();
    	if (result == (getNumberOfNavpoints() - 1)) result -= 1;
    	return result;
    }
    
    /**
     * Gets the description of a collection site.
     * @param siteNum the number of the site.
     * @return description
     */
    protected String getCollectionSiteDescription(int siteNum) {
    	return "exploration site " + siteNum;
    }
    
    @Override
    public Map<Class, Integer> getEquipmentNeededForRemainingMission(boolean useBuffer) throws Exception {
    	if (equipmentNeededCache != null) return equipmentNeededCache;
    	else {
    		Map<Class, Integer> result = new HashMap<Class, Integer>();
    	
        	// Include one EVA suit per person on mission.
        	result.put(EVASuit.class, new Integer(getPeopleNumber()));
    		
    		// Include required number of containers.
    		result.put(containerType, new Integer(containerNum));
    	
    		equipmentNeededCache = result;
    		return result;
    	}
    }
    
	/**
	 * Gets the number of empty containers of given type at the settlement.
	 * @param settlement the settlement
	 * @param containerType the type of container
	 * @return number of empty containers.
	 */
	protected static int numCollectingContainersAvailable(Settlement settlement, 
			Class containerType) throws Exception {
		return settlement.getInventory().findNumEmptyUnitsOfClass(containerType); 
	}
	
    /**
     * Gets the time limit of the trip based on life support capacity.
     * @param useBuffer use time buffer in estimation if true.
     * @return time (millisols) limit.
     * @throws Exception if error determining time limit.
     */
    public static double getTotalTripTimeLimit(Rover rover, int memberNum, boolean useBuffer) throws Exception {
    	
    	Inventory vInv = rover.getInventory();
    	
    	double timeLimit = Double.MAX_VALUE;
    	
    	PersonConfig config = SimulationConfig.instance().getPersonConfiguration();
		
    	// Check food capacity as time limit.
    	double foodConsumptionRate = config.getFoodConsumptionRate();
    	double foodCapacity = vInv.getAmountResourceCapacity(AmountResource.FOOD);
    	double foodTimeLimit = foodCapacity / (foodConsumptionRate * memberNum);
    	if (foodTimeLimit < timeLimit) timeLimit = foodTimeLimit;
    		
    	// Check water capacity as time limit.
    	double waterConsumptionRate = config.getWaterConsumptionRate();
    	double waterCapacity = vInv.getAmountResourceCapacity(AmountResource.WATER);
    	double waterTimeLimit = waterCapacity / (waterConsumptionRate * memberNum);
    	if (waterTimeLimit < timeLimit) timeLimit = waterTimeLimit;
    		
    	// Check oxygen capacity as time limit.
    	double oxygenConsumptionRate = config.getOxygenConsumptionRate();
    	double oxygenCapacity = vInv.getAmountResourceCapacity(AmountResource.OXYGEN);
    	double oxygenTimeLimit = oxygenCapacity / (oxygenConsumptionRate * memberNum);
    	if (oxygenTimeLimit < timeLimit) timeLimit = oxygenTimeLimit;
    	
    	// Convert timeLimit into millisols and use error margin.
    	timeLimit = (timeLimit * 1000D);
    	if (useBuffer) timeLimit /= Rover.LIFE_SUPPORT_RANGE_ERROR_MARGIN;
    	
    	return timeLimit;
    }
    
	/** 
	 * Determine the locations of the sample collection sites.
	 * @param roverRange the rover's driving range
	 * @param numSites the number of collection sites
	 * @throws MissionException of collection sites can not be determined.
	 */
	private void determineCollectionSites(double roverRange, double tripTimeLimit, int numSites) throws MissionException {

		List<Coordinates> unorderedSites = new ArrayList<Coordinates>();
		
		// Determining the actual travelling range.
		double range = roverRange;
		double timeRange = getTripTimeRange(tripTimeLimit, numSites, true);
    	if (timeRange < range) range = timeRange;
        
    	try {
    		// Get the current location.
    		Coordinates startingLocation = getCurrentMissionLocation();
        
    		// Determine the first collection site.
    		Direction direction = new Direction(RandomUtil.getRandomDouble(2 * Math.PI));
    		double limit = range / 4D;
    		double siteDistance = RandomUtil.getRandomDouble(limit);
    		Coordinates newLocation = startingLocation.getNewLocation(direction, siteDistance);
    		unorderedSites.add(newLocation);
    		Coordinates currentLocation = newLocation;
        
    		// Determine remaining collection sites.
    		double remainingRange = (range / 2D) - siteDistance;
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
    		int collectionSiteNum = 1;
    		currentLocation = startingLocation;
    		while (unorderedSites.size() > 0) {
    			Coordinates shortest = unorderedSites.get(0);
    			Iterator<Coordinates> i = unorderedSites.iterator();
    			while (i.hasNext()) {
    				Coordinates site = i.next();
    				if (currentLocation.getDistance(site) < currentLocation.getDistance(shortest)) 
    					shortest = site;
    			}
    			addNavpoint(new NavPoint(shortest, getCollectionSiteDescription(collectionSiteNum)));
    			unorderedSites.remove(shortest);
    			currentLocation = shortest;
    			collectionSiteNum++;
    		}
    	}
    	catch (Exception e) {
    		throw new MissionException(getPhase(), e);
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
}