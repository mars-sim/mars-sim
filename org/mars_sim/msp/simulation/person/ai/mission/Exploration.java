/**
 * Mars Simulation Project
 * Exploration.java
 * @version 2.84 2008-05-17
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai.mission;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
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
import org.mars_sim.msp.simulation.mars.ExploredLocation;
import org.mars_sim.msp.simulation.mars.Mars;
import org.mars_sim.msp.simulation.mars.MineralMap;
import org.mars_sim.msp.simulation.mars.SurfaceFeatures;
import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.person.PersonConfig;
import org.mars_sim.msp.simulation.person.PhysicalCondition;
import org.mars_sim.msp.simulation.person.ai.job.Job;
import org.mars_sim.msp.simulation.person.ai.task.ExploreSite;
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
		"org.mars_sim.msp.simulation.person.ai.mission.Exploration";
	
	private static Logger logger = Logger.getLogger(CLASS_NAME);
	
	// Default description.
	public static final String DEFAULT_DESCRIPTION = "Exploration";
	
	// Mission phases
	final public static String EXPLORE_SITE = "Exploring Site";
	
	// Number of specimen containers required for the mission. 
	public static final int REQUIRED_SPECIMEN_CONTAINERS = 20;
	
	//	Number of collection sites.
	private static final int NUM_SITES = 5;
	
	// Amount of time to explore a site.
	public final static double EXPLORING_SITE_TIME = 1000D;
	
	// Maximum mineral concentration estimation diff from actual.
	private final static double MINERAL_ESTIMATION_CEILING = 20D;
	
	// Data members
	private MarsClock explorationSiteStartTime; // The start time at the current exploration site.
	private ExploredLocation currentSite; // The current exploration site.
	private List<ExploredLocation> exploredSites; // List of sites explored by this mission.
	private boolean endExploringSite; // External flag for ending exploration at the current site.

	/**
	 * Constructor
	 * @param startingPerson the person starting the mission.
	 * @throws MissionException if problem constructing mission.
	 */
	public Exploration(Person startingPerson) throws MissionException {
		
		// Use RoverMission constructor.
		super(DEFAULT_DESCRIPTION, startingPerson, RoverMission.MIN_PEOPLE);
		
		if (!isDone()) {
			
        	// Set mission capacity.
        	if (hasVehicle()) setMissionCapacity(getRover().getCrewCapacity());
        	int availableSuitNum = VehicleMission.getNumberAvailableEVASuitsAtSettlement(startingPerson.getSettlement());
        	if (availableSuitNum < getMissionCapacity()) setMissionCapacity(availableSuitNum);
		
			// Initialize data members.
			setStartingSettlement(startingPerson.getSettlement());
			exploredSites = new ArrayList<ExploredLocation>(NUM_SITES);
			
			// Recruit additional people to mission.
        	recruitPeopleForMission(startingPerson);
			
			// Determine exploration sites
        	try {
        		if (hasVehicle()) determineExplorationSites(getVehicle().getRange(), getTotalTripTimeLimit(getRover(), 
        			getPeopleNumber(), true), NUM_SITES);
        	}
        	catch (Exception e) {
        		throw new MissionException(getPhase(), e);
        	}
			
			// Add home settlement
			addNavpoint(new NavPoint(getStartingSettlement().getCoordinates(), 
					getStartingSettlement(), getStartingSettlement().getName()));
			
        	// Check if vehicle can carry enough supplies for the mission.
        	if (hasVehicle() && !isVehicleLoadable()) 
        		endMission("Vehicle is not loadable. (Exploration)");
		}
		
		// Add exploring site phase.
		addPhase(EXPLORE_SITE);
		
		// Set initial mission phase.
		setPhase(VehicleMission.EMBARKING);
		setPhaseDescription("Embarking from " + getStartingSettlement().getName());
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
    		List<Coordinates> explorationSites, Rover rover, String description) throws MissionException {
    	
       	// Use RoverMission constructor.
    	super(description, (Person) members.toArray()[0], 1, rover);
    	
		setStartingSettlement(startingSettlement);
		
		// Set mission capacity.
		setMissionCapacity(getRover().getCrewCapacity());
		int availableSuitNum = VehicleMission.getNumberAvailableEVASuitsAtSettlement(startingSettlement);
    	if (availableSuitNum < getMissionCapacity()) setMissionCapacity(availableSuitNum);
    	
    	// Initialize explored sites.
    	exploredSites = new ArrayList<ExploredLocation>(NUM_SITES);
		
		// Set exploration navpoints.
		for (int x = 0; x < explorationSites.size(); x++) 
			addNavpoint(new NavPoint(explorationSites.get(x), "exploration site " + (x + 1)));
		
		// Add home navpoint.
		addNavpoint(new NavPoint(startingSettlement.getCoordinates(), startingSettlement, startingSettlement.getName()));
		
    	// Add mission members.
    	Iterator<Person> i = members.iterator();
    	while (i.hasNext()) i.next().getMind().setMission(this);
    	
		// Add exploring site phase.
		addPhase(EXPLORE_SITE);
		
		// Set initial mission phase.
		setPhase(VehicleMission.EMBARKING);
		setPhaseDescription("Embarking from " + getStartingSettlement().getName());

       	// Check if vehicle can carry enough supplies for the mission.
       	try {
       		if (hasVehicle() && !isVehicleLoadable()) endMission("Vehicle is not loadable. (Exploration)");
       	}
       	catch (Exception e) {
       		throw new MissionException(getPhase(), e);
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
			boolean reservableRover = RoverMission.areVehiclesAvailable(settlement);
			
			// Check if minimum number of people are available at the settlement.
			// Plus one to hold down the fort.
			boolean minNum = RoverMission.minAvailablePeopleAtSettlement(settlement, (MIN_PEOPLE + 1));
			
			// Check if there are enough specimen containers at the settlement for collecting rock samples.
			boolean enoughContainers = false;
			try {
				int numContainers = settlement.getInventory().findNumEmptyUnitsOfClass(SpecimenContainer.class);
				enoughContainers = (numContainers >= REQUIRED_SPECIMEN_CONTAINERS);
			}
			catch (InventoryException e) {
				logger.log(Level.SEVERE, "Error checking if enough collecting containers available.");
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
			if (VehicleMission.getNumberAvailableEVASuitsAtSettlement(person.getSettlement()) < MIN_PEOPLE) 
				result = 0D;
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
				setPhase(EXPLORE_SITE);
				setPhaseDescription("Exploring site at " + getCurrentNavpoint().getDescription());
			}
		}
		else if (EXPLORE_SITE.equals(getPhase())) {
			startTravelToNextNode();
			setPhase(VehicleMission.TRAVELLING);
			setPhaseDescription("Driving to " + getNextNavpoint().getDescription());
		}
		else if (DISEMBARKING.equals(getPhase())) endMission("Successfully disembarked.");
    }
    
    @Override
    protected void performPhase(Person person) throws MissionException {
    	super.performPhase(person);
    	if (EXPLORE_SITE.equals(getPhase())) exploringPhase(person);
    }
    
    /**
     * Ends the exploration at a site.
     */
    public void endExplorationAtSite() {
    	logger.info("Explore site phase ended due to external trigger.");
    	endExploringSite = true;
    	
    	// End each member's explore site task.
    	Iterator<Person> i = getPeople().iterator();
    	while (i.hasNext()) {
    		Task task = i.next().getMind().getTaskManager().getTask();
    		if (task instanceof ExploreSite) ((ExploreSite) task).endEVA();
    	}
    }
    
	/** 
	 * Performs the explore site phase of the mission.
	 * @param person the person currently performing the mission
	 * @throws MissionException if problem performing phase.
	 */
	private final void exploringPhase(Person person) throws MissionException {

		// Add new explored site if just starting exploring.
		if (currentSite == null) {
			createNewExploredSite();
			explorationSiteStartTime = (MarsClock) Simulation.instance().getMasterClock().getMarsClock().clone();
		}
		
		// Check if crew has been at site for more than one sol.
		boolean timeExpired = false;
		MarsClock currentTime = (MarsClock) Simulation.instance().getMasterClock().getMarsClock().clone();
		if (MarsClock.getTimeDiff(currentTime, explorationSiteStartTime) >= EXPLORING_SITE_TIME) 
			timeExpired = true;
		
		if (isEveryoneInRover()) {

			// Check if end exploring flag is set.
			if (endExploringSite) {
				endExploringSite = false;
				setPhaseEnded(true);
			}
			
			// Check if crew has been at site for more than one sol, then end this phase.
			if (timeExpired) setPhaseEnded(true);

			// Determine if no one can start the explore site task.
			boolean nobodyExplore = true;
			Iterator<Person> j = getPeople().iterator();
			while (j.hasNext()) {
				if (ExploreSite.canExploreSite(j.next(), getRover())) nobodyExplore = false;
			}
	    
			// If no one can explore the site and this is not due to it just being
			// night time, end the exploring phase.
			try {
				Mars mars = Simulation.instance().getMars();
				boolean inDarkPolarRegion = mars.getSurfaceFeatures().inDarkPolarRegion(getCurrentMissionLocation());
				double sunlight = mars.getSurfaceFeatures().getSurfaceSunlight(getCurrentMissionLocation());
				if (nobodyExplore && ((sunlight > 0D) || inDarkPolarRegion)) setPhaseEnded(true);
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
				throw new MissionException(getPhase(), e.getMessage());
			}
		}
		else {
			// If exploration time has expired for the site, have everyone end their exploration tasks.
			if (timeExpired) {
				Iterator<Person> i = getPeople().iterator();
				while (i.hasNext()) {
					Task task = i.next().getMind().getTaskManager().getTask();
					if ((task != null) && (task instanceof ExploreSite))
						((ExploreSite) task).endEVA();
				}
			}
		}

		if (!getPhaseEnded()) {
			
			if (!endExploringSite && !timeExpired) {
				// If person can explore the site, start that task.
				if (ExploreSite.canExploreSite(person, getRover())) {
					try {
						assignTask(person, new ExploreSite(person, currentSite, (Rover) getVehicle()));
					}
					catch(Exception e) {
						throw new MissionException(getPhase(), e);
					}
				}
			}
		}
		else {
			currentSite.setExplored(true);
			currentSite = null;
		}
	}
	
	/**
	 * Creates a new explored site at the current location, creates initial estimates for mineral
	 * concentrations, and adds it to the explored site list.
	 * @throws MissionException if error creating explored site.
	 */
	private void createNewExploredSite() throws MissionException {
		SurfaceFeatures surfaceFeatures = Simulation.instance().getMars().getSurfaceFeatures();
		MineralMap mineralMap = surfaceFeatures.getMineralMap();
		String[] mineralTypes = mineralMap.getMineralTypeNames();
		Map<String, Double> initialMineralEstimations = new HashMap<String, Double>(mineralTypes.length);
		for (int x = 0; x < mineralTypes.length; x++) {
			double estimation = RandomUtil.getRandomDouble(MINERAL_ESTIMATION_CEILING * 2D) - 
					MINERAL_ESTIMATION_CEILING;
			double actualConcentration = 
					mineralMap.getMineralConcentration(mineralTypes[x], getCurrentMissionLocation());
			estimation += actualConcentration;
			if (estimation < 0D) estimation = 0D - estimation;
			else if (estimation > 100D) estimation = 100D - estimation;
			initialMineralEstimations.put(mineralTypes[x], estimation);
		}
		currentSite = surfaceFeatures.addExploredLocation(
				new Coordinates(getCurrentMissionLocation()), initialMineralEstimations, 
				getAssociatedSettlement());
		exploredSites.add(currentSite);
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
    public double getEstimatedRemainingMissionTime(boolean useBuffer) throws MissionException {
    	double result = super.getEstimatedRemainingMissionTime(useBuffer);
    	result += getEstimatedRemainingExplorationSiteTime();
    	return result;
    }
	
    /**
     * Gets the estimated time remaining for exploration sites in the mission.
     * @return time (millisols)
     * @throws MissionException if error estimating time.
     */
    private final double getEstimatedRemainingExplorationSiteTime() throws MissionException {
    	double result = 0D;
    	
    	// Add estimated remaining exploration time at current site if still there.
    	if (EXPLORE_SITE.equals(getPhase())) {
    		MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();
    		double timeSpentAtExplorationSite = MarsClock.getTimeDiff(currentTime, explorationSiteStartTime);
    		double remainingTime = EXPLORING_SITE_TIME - timeSpentAtExplorationSite;
    		if (remainingTime > 0D) result += remainingTime;
    	}
    	
    	// Add estimated exploration time at sites that haven't been visited yet.
    	int remainingExplorationSites = getNumExplorationSites() - getNumExplorationSitesVisited();
    	result += EXPLORING_SITE_TIME * remainingExplorationSites;
    	
    	return result;
    }
    
    @Override
    public Map<Resource, Number> getResourcesNeededForRemainingMission(boolean useBuffer, 
    		boolean parts) throws MissionException {
    	Map<Resource, Number> result = super.getResourcesNeededForRemainingMission(useBuffer, parts);
    	
    	double explorationSitesTime = getEstimatedRemainingExplorationSiteTime();
    	double timeSols = explorationSitesTime / 1000D;
    	
    	int crewNum = getPeopleNumber();
    	
    	// Determine life support supplies needed for trip.
    	try {
    		AmountResource oxygen = AmountResource.findAmountResource("oxygen");
    		double oxygenAmount = PhysicalCondition.getOxygenConsumptionRate() * timeSols * crewNum;
    		if (result.containsKey(oxygen)) 
    			oxygenAmount += ((Double) result.get(oxygen)).doubleValue();
    		result.put(oxygen, new Double(oxygenAmount));
    		
    		AmountResource water = AmountResource.findAmountResource("water");
    		double waterAmount = PhysicalCondition.getWaterConsumptionRate() * timeSols * crewNum;
    		if (result.containsKey(water)) 
    			waterAmount += ((Double) result.get(water)).doubleValue();
    		result.put(water, new Double(waterAmount));
    		
    		AmountResource food = AmountResource.findAmountResource("food");
    		double foodAmount = PhysicalCondition.getFoodConsumptionRate() * timeSols * crewNum;
    		if (result.containsKey(food)) 
    			foodAmount += ((Double) result.get(food)).doubleValue();
    		result.put(food, new Double(foodAmount));
    	}
    	catch(Exception e) {
    		throw new MissionException(getPhase(), e);
    	}
    	
    	return result;
    }
	
	@Override
	public Settlement getAssociatedSettlement() {
		return getStartingSettlement();
	}
	
	@Override
	protected int compareVehicles(Vehicle firstVehicle, Vehicle secondVehicle) throws MissionException {
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
     * Gets the estimated time spent at all exploration sites.
     * @return time (millisols)
     */
    protected double getEstimatedTimeAtExplorationSites() {
    	return EXPLORING_SITE_TIME * getNumExplorationSites();
    }
    
    /**
     * Gets the total number of exploration sites for this mission.
     * @return number of sites.
     */
    public final int getNumExplorationSites() {
    	return getNumberOfNavpoints() - 2;
    }
    
    /**
     * Gets the number of exploration sites that have been currently visited by the mission.
     * @return number of sites.
     */
    public final int getNumExplorationSitesVisited() {
    	int result = getCurrentNavpointIndex();
    	if (result == (getNumberOfNavpoints() - 1)) result -= 1;
    	return result;
    }
    
    @Override
    public Map<Class, Integer> getEquipmentNeededForRemainingMission(boolean useBuffer) 
    		throws MissionException {
    	if (equipmentNeededCache != null) return equipmentNeededCache;
    	else {
    		Map<Class, Integer> result = new HashMap<Class, Integer>();
    	
        	// Include one EVA suit per person on mission.
        	result.put(EVASuit.class, new Integer(getPeopleNumber()));
    		
    		// Include required number of specimen containers.
    		result.put(SpecimenContainer.class, new Integer(REQUIRED_SPECIMEN_CONTAINERS));
    	
    		equipmentNeededCache = result;
    		return result;
    	}
    }
	
    /**
     * Gets the time limit of the trip based on life support capacity.
     * @param useBuffer use time buffer in estimation if true.
     * @return time (millisols) limit.
     * @throws MissionException if error determining time limit.
     */
    public static double getTotalTripTimeLimit(Rover rover, int memberNum, boolean useBuffer) 
    		throws MissionException {
    	
    	Inventory vInv = rover.getInventory();
    	
    	double timeLimit = Double.MAX_VALUE;
    	
    	PersonConfig config = SimulationConfig.instance().getPersonConfiguration();
		
    	try {
    		// Check food capacity as time limit.
    		AmountResource food = AmountResource.findAmountResource("food");
    		double foodConsumptionRate = config.getFoodConsumptionRate();
    		double foodCapacity = vInv.getAmountResourceCapacity(food);
    		double foodTimeLimit = foodCapacity / (foodConsumptionRate * memberNum);
    		if (foodTimeLimit < timeLimit) timeLimit = foodTimeLimit;
    		
    		// Check water capacity as time limit.
    		AmountResource water = AmountResource.findAmountResource("water");
    		double waterConsumptionRate = config.getWaterConsumptionRate();
    		double waterCapacity = vInv.getAmountResourceCapacity(water);
    		double waterTimeLimit = waterCapacity / (waterConsumptionRate * memberNum);
    		if (waterTimeLimit < timeLimit) timeLimit = waterTimeLimit;
    		
    		// Check oxygen capacity as time limit.
    		AmountResource oxygen = AmountResource.findAmountResource("oxygen");
    		double oxygenConsumptionRate = config.getOxygenConsumptionRate();
    		double oxygenCapacity = vInv.getAmountResourceCapacity(oxygen);
    		double oxygenTimeLimit = oxygenCapacity / (oxygenConsumptionRate * memberNum);
    		if (oxygenTimeLimit < timeLimit) timeLimit = oxygenTimeLimit;
    	}
    	catch (Exception e) {
    		throw new MissionException(null, e);
    	}
    	
    	// Convert timeLimit into millisols and use error margin.
    	timeLimit = (timeLimit * 1000D);
    	if (useBuffer) timeLimit /= Rover.LIFE_SUPPORT_RANGE_ERROR_MARGIN;
    	
    	return timeLimit;
    }
    
	/** 
	 * Determine the locations of the exploration sites.
	 * @param roverRange the rover's driving range
	 * @param numSites the number of exploration sites
	 * @throws MissionException if exploration sites can not be determined.
	 */
	private void determineExplorationSites(double roverRange, double tripTimeLimit, int numSites) 
			throws MissionException {

		List<Coordinates> unorderedSites = new ArrayList<Coordinates>();
		
		// Determining the actual traveling range.
		double range = roverRange;
		double timeRange = getTripTimeRange(tripTimeLimit);
    	if (timeRange < range) range = timeRange;
        
    	try {
    		// Get the current location.
    		Coordinates startingLocation = getCurrentMissionLocation();
        
    		// Determine the first exploration site.
    		Direction direction = new Direction(RandomUtil.getRandomDouble(2 * Math.PI));
    		double limit = range / 4D;
    		double siteDistance = RandomUtil.getRandomDouble(limit);
    		Coordinates newLocation = startingLocation.getNewLocation(direction, siteDistance);
    		unorderedSites.add(newLocation);
    		Coordinates currentLocation = newLocation;
        
    		// Determine remaining exploration sites.
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
    		int explorationSiteNum = 1;
    		currentLocation = startingLocation;
    		while (unorderedSites.size() > 0) {
    			Coordinates shortest = unorderedSites.get(0);
    			Iterator<Coordinates> i = unorderedSites.iterator();
    			while (i.hasNext()) {
    				Coordinates site = i.next();
    				if (currentLocation.getDistance(site) < currentLocation.getDistance(shortest)) 
    					shortest = site;
    			}
    			addNavpoint(new NavPoint(shortest, "exploration site " + explorationSiteNum));
    			unorderedSites.remove(shortest);
    			currentLocation = shortest;
    			explorationSiteNum++;
    		}
    	}
    	catch (Exception e) {
    		throw new MissionException(getPhase(), e);
    	}
	}
	
	/**
	 * Gets the range of a trip based on its time limit and exploration sites.
	 * @param tripTimeLimit time (millisols) limit of trip.
	 * @return range (km) limit.
	 */
	private double getTripTimeRange(double tripTimeLimit) {
		double timeAtSites = getEstimatedTimeAtExplorationSites();
		double tripTimeTravellingLimit = tripTimeLimit - timeAtSites;
    	double averageSpeed = getAverageVehicleSpeedForOperators();
    	double millisolsInHour = MarsClock.convertSecondsToMillisols(60D * 60D);
    	double averageSpeedMillisol = averageSpeed / millisolsInHour;
    	return tripTimeTravellingLimit * averageSpeedMillisol;
	}
	
	/**
	 * Gets a list of sites explored by the mission so far.
	 * @return list of explored sites.
	 */
	public List<ExploredLocation> getExploredSites() {
		return exploredSites;
	}
}