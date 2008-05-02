/**
 * Mars Simulation Project
 * Mining.java
 * @version 2.84 2008-04-01
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai.mission;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.simulation.Coordinates;
import org.mars_sim.msp.simulation.Inventory;
import org.mars_sim.msp.simulation.InventoryException;
import org.mars_sim.msp.simulation.RandomUtil;
import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.simulation.SimulationConfig;
import org.mars_sim.msp.simulation.equipment.Bag;
import org.mars_sim.msp.simulation.equipment.EVASuit;
import org.mars_sim.msp.simulation.mars.ExploredLocation;
import org.mars_sim.msp.simulation.mars.Mars;
import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.person.PersonConfig;
import org.mars_sim.msp.simulation.person.PhysicalCondition;
import org.mars_sim.msp.simulation.person.ai.job.Job;
import org.mars_sim.msp.simulation.person.ai.task.CollectMinedMinerals;
import org.mars_sim.msp.simulation.person.ai.task.MineSite;
import org.mars_sim.msp.simulation.person.ai.task.Task;
import org.mars_sim.msp.simulation.resource.AmountResource;
import org.mars_sim.msp.simulation.resource.Resource;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.structure.goods.Good;
import org.mars_sim.msp.simulation.structure.goods.GoodsUtil;
import org.mars_sim.msp.simulation.time.MarsClock;
import org.mars_sim.msp.simulation.vehicle.Rover;

/**
 * Mission for mining mineral concentrations at an explored site.
 */
public class Mining extends RoverMission {

	private static String CLASS_NAME = 
		"org.mars_sim.msp.simulation.person.ai.mission.Mining";
	private static Logger logger = Logger.getLogger(CLASS_NAME);
	
	// Default description.
	public static final String DEFAULT_DESCRIPTION = "Mining";
	
	// Mission phases
	final public static String MINING_SITE = "Mining Site";
	
	// Number of bags needed for mission.
	private static final int NUMBER_OF_BAGS = 20;
	
	private static final double MINERAL_BASE_AMOUNT = 1000D;
	
	private static final double MINING_SITE_TIME = 3000D;
	
	private static final double MINIMUM_COLLECT_AMOUNT = 10D;
	
	// Data members
	private ExploredLocation miningSite;
	private MarsClock miningSiteStartTime;
	private boolean endMiningSite;
	private Map<AmountResource, Double> excavatedMinerals;
	
	/**
	 * Constructor
	 * @param startingPerson the person starting the mission.
	 * @throws MissionException if error creating mission.
	 */
	public Mining(Person startingPerson) throws MissionException {
		
		// Use RoverMission constructor.
		super(DEFAULT_DESCRIPTION, startingPerson, RoverMission.MIN_PEOPLE);
		
		if (!isDone()) {
        	// Set mission capacity.
        	if (hasVehicle()) setMissionCapacity(getRover().getCrewCapacity());
        	int availableSuitNum = VehicleMission.getNumberAvailableEVASuitsAtSettlement(startingPerson.getSettlement());
        	if (availableSuitNum < getMissionCapacity()) setMissionCapacity(availableSuitNum);
        	
			// Initialize data members.
			setStartingSettlement(startingPerson.getSettlement());
			
			// Recruit additional people to mission.
        	recruitPeopleForMission(startingPerson);
        	
        	// Determine mining site.
        	try {
        		if (hasVehicle()) {
        			miningSite = determineBestMiningSite(getRover(), getStartingSettlement());
        			miningSite.setMined(true);
        			addNavpoint(new NavPoint(miningSite.getLocation(), "mining site"));
        		}
        	}
        	catch (Exception e) {
        		throw new MissionException(getPhase(), e);
        	}
        	
			// Add home settlement
			addNavpoint(new NavPoint(getStartingSettlement().getCoordinates(), 
					getStartingSettlement(), getStartingSettlement().getName()));
			
        	// Check if vehicle can carry enough supplies for the mission.
        	if (hasVehicle() && !isVehicleLoadable()) 
        		endMission("Vehicle is not loadable. (Mining)");
		}
		
		// Add mining site phase.
		addPhase(MINING_SITE);
		
		// Set initial mission phase.
		setPhase(VehicleMission.EMBARKING);
		setPhaseDescription("Embarking from " + getStartingSettlement().getName());
	}
	
	/**
	 * Constructor with explicit data.
	 * @param members collection of mission members.
	 * @param startingSettlement the starting settlement.
	 * @param miningSite the site to mine.
	 * @param rover the rover to use.
	 * @param description the mission's description.
	 * @throws MissionException if error constructing mission.
	 */
	public Mining(Collection<Person> members, Settlement startingSettlement, 
    		ExploredLocation miningSite, Rover rover, String description) throws MissionException {
		
       	// Use RoverMission constructor.
    	super(description, (Person) members.toArray()[0], 1, rover);
		
    	// Initialize data members.
		setStartingSettlement(startingSettlement);
		this.miningSite = miningSite;
		miningSite.setMined(true);
		
		// Set mission capacity.
		setMissionCapacity(getRover().getCrewCapacity());
		int availableSuitNum = VehicleMission.getNumberAvailableEVASuitsAtSettlement(startingSettlement);
    	if (availableSuitNum < getMissionCapacity()) setMissionCapacity(availableSuitNum);
    	
    	// Add mining site nav point.
    	addNavpoint(new NavPoint(miningSite.getLocation(), "mining site"));
    	
		// Add home settlement
		addNavpoint(new NavPoint(getStartingSettlement().getCoordinates(), 
				getStartingSettlement(), getStartingSettlement().getName()));
		
    	// Check if vehicle can carry enough supplies for the mission.
    	if (hasVehicle() && !isVehicleLoadable()) 
    		endMission("Vehicle is not loadable. (Mining)");
    	
		// Add mining site phase.
		addPhase(MINING_SITE);
		
		// Set initial mission phase.
		setPhase(VehicleMission.EMBARKING);
		setPhaseDescription("Embarking from " + getStartingSettlement().getName());
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
			
			// Check if there are enough bags at the settlement for collecting minerals.
			boolean enoughBags = false;
			try {
				int numBags = settlement.getInventory().findNumEmptyUnitsOfClass(Bag.class);
				enoughBags = (numBags >= NUMBER_OF_BAGS);
			}
			catch (InventoryException e) {
				logger.log(Level.SEVERE, "Error checking if enough bags available.");
			}
			
			// Check for embarking missions.
			boolean embarkingMissions = VehicleMission.hasEmbarkingMissions(settlement);
	    
			// TODO: Check for available light utility vehicles.
			
			if (reservableRover && minNum && enoughBags && !embarkingMissions) {
				
				try {
					// Get available rover.
					Rover rover = (Rover) getVehicleWithGreatestRange(settlement);
					if (rover != null) {
				
						// Find best mining site.
						ExploredLocation miningSite = determineBestMiningSite(rover, settlement);
						if (miningSite != null) 
							result = getMiningSiteValue(miningSite, settlement);
					}
				}
				catch (Exception e) {
			    	logger.log(Level.SEVERE, "Error getting mining site.", e);
				}
			}
			
			// Crowding modifier
			int crowding = settlement.getCurrentPopulationNum() - settlement.getPopulationCapacity();
			if (crowding > 0) result *= (crowding + 1);		
			
			// Job modifier.
			Job job = person.getMind().getJob();
			if (job != null) result *= job.getStartMissionProbabilityModifier(Mining.class);	
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
				setPhase(MINING_SITE);
				setPhaseDescription("Mining at " + getCurrentNavpoint().getDescription());
			}
		}
		else if (MINING_SITE.equals(getPhase())) {
			startTravelToNextNode();
			setPhase(VehicleMission.TRAVELLING);
			setPhaseDescription("Driving to " + getNextNavpoint().getDescription());
		}
		else if (DISEMBARKING.equals(getPhase())) endMission("Successfully disembarked.");
    }
    
    @Override
    protected void performPhase(Person person) throws MissionException {
    	super.performPhase(person);
    	if (MINING_SITE.equals(getPhase())) miningPhase(person);
    }
    
    /**
     * Perform the mining phase.
     * @param person the person performing the mining phase.
     * @throws MissionException if error performing the mining phase.
     */
    private final void miningPhase(Person person) throws MissionException {
    	
    	// Set the mining site start time if necessary.
    	if (miningSiteStartTime == null) 
    		miningSiteStartTime = (MarsClock) Simulation.instance().getMasterClock().getMarsClock().clone();
    	
    	// Initialize the excavated minerals if necessary.
    	if (excavatedMinerals == null)
    		excavatedMinerals = new HashMap<AmountResource, Double>(1);
    	
    	// TODO detach towed light utility vehicle if necessary.
    	
		// Check if crew has been at site for more than three sols.
		boolean timeExpired = false;
		MarsClock currentTime = (MarsClock) Simulation.instance().getMasterClock().getMarsClock().clone();
		if (MarsClock.getTimeDiff(currentTime, miningSiteStartTime) >= MINING_SITE_TIME) 
			timeExpired = true;
		
		if (isEveryoneInRover()) {
			
			// Check if end mining flag is set.
			if (endMiningSite) {
				endMiningSite = false;
				setPhaseEnded(true);
			}
			
			// Check if crew has been at site for more than three sols, then end this phase.
			if (timeExpired) setPhaseEnded(true);

			// Determine if no one can start the mine site or collect resources tasks.
			boolean nobodyMineOrCollect = true;
			Iterator<Person> i = getPeople().iterator();
			while (i.hasNext()) {
				Person personTemp = i.next();
				if (MineSite.canMineSite(personTemp, getRover())) nobodyMineOrCollect = false;
				if (canCollectExcavatedMinerals(personTemp)) nobodyMineOrCollect = false;
			}
	    
			// If no one can mine or collect minerals at the site and this is not due to it just being
			// night time, end the mining phase.
			try {
				Mars mars = Simulation.instance().getMars();
				boolean inDarkPolarRegion = mars.getSurfaceFeatures().inDarkPolarRegion(getCurrentMissionLocation());
				double sunlight = mars.getSurfaceFeatures().getSurfaceSunlight(getCurrentMissionLocation());
				if (nobodyMineOrCollect && ((sunlight > 0D) || inDarkPolarRegion)) setPhaseEnded(true);
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
			// If mining time has expired for the site, have everyone end their 
			// mining and collection tasks.
			if (timeExpired) {
				Iterator<Person> i = getPeople().iterator();
				while (i.hasNext()) {
					Task task = i.next().getMind().getTaskManager().getTask();
		    		if (task instanceof MineSite) ((MineSite) task).endEVA();
		    		if (task instanceof CollectMinedMinerals) ((CollectMinedMinerals) task).endEVA();
				}
			}
		}

		if (!getPhaseEnded()) {
			
			// 75% chance of assigning task, otherwise allow break.
			if (RandomUtil.lessThanRandPercent(75D)) {
				// If mining is still needed at site, assign tasks.
				if (!endMiningSite && !timeExpired) {
					try {
						// If person can collect minerals the site, start that task.
						if (canCollectExcavatedMinerals(person)) {
							AmountResource mineralToCollect = getMineralToCollect(person);
							assignTask(person, new CollectMinedMinerals(person, getRover(), 
									excavatedMinerals, mineralToCollect));
						}
						// Otherwise start the mining task if it can be done.
						else if (MineSite.canMineSite(person, getRover())) {
							assignTask(person, new MineSite(person, miningSite.getLocation(), 
									(Rover) getVehicle(), excavatedMinerals));
						}
					}
					catch(Exception e) {
						throw new MissionException(getPhase(), e);
					}
				}
			}
		}
		else {
			// TODO attach light utility vehicle for towing.
		}
    }
    
    private boolean canCollectExcavatedMinerals(Person person) {
    	boolean result = false;
    	
    	Iterator<AmountResource> i = excavatedMinerals.keySet().iterator();
    	while (i.hasNext()) {
    		AmountResource resource = i.next();
    		if ((excavatedMinerals.get(resource) >= MINIMUM_COLLECT_AMOUNT) && 
    				CollectMinedMinerals.canCollectMinerals(person, getRover(), resource))
    			result = true;
    	}
    	
    	return result;
	}
    
    private AmountResource getMineralToCollect(Person person) {
    	AmountResource result = null;
    	double largestAmount = 0D;
    	
    	Iterator<AmountResource> i = excavatedMinerals.keySet().iterator();
    	while (i.hasNext()) {
    		AmountResource resource = i.next();
    		if ((excavatedMinerals.get(resource) >= MINIMUM_COLLECT_AMOUNT) && 
    				CollectMinedMinerals.canCollectMinerals(person, getRover(), resource)) {
    			double amount = excavatedMinerals.get(resource);
    			if (amount > largestAmount) {
    				result = resource;
    				largestAmount = amount;
    			}
    		}
    	}
    	
    	return result;
    }

	/**
     * Ends mining at a site.
     */
    public void endMiningAtSite() {
    	logger.info("Mining site phase ended due to external trigger.");
    	endMiningSite = true;
    	
    	// End each member's mining site task.
    	Iterator<Person> i = getPeople().iterator();
    	while (i.hasNext()) {
    		Task task = i.next().getMind().getTaskManager().getTask();
    		if (task instanceof MineSite) ((MineSite) task).endEVA();
    		if (task instanceof CollectMinedMinerals) ((CollectMinedMinerals) task).endEVA();
    	}
    }
    
	/**
	 * Determines the best available mining site.
	 * @param roverRange the range of the mission rover (km).
	 * @param homeSettlement the mission home settlement.
	 * @return best explored location for mining, or null if none found.
	 * @throws MissionException if error determining mining site.
	 */
	private static ExploredLocation determineBestMiningSite(Rover rover, Settlement homeSettlement) 
			throws MissionException {
		
		ExploredLocation result = null;
		double bestValue = 0D;
		
		try {
			double roverRange = rover.getRange();
			double tripTimeLimit = getTotalTripTimeLimit(rover, rover.getCrewCapacity(), true);
			double tripRange = getTripTimeRange(tripTimeLimit, rover.getBaseSpeed() / 2D);
			double range = roverRange;
			if (tripRange < range) range = tripRange;
		
			Iterator<ExploredLocation> i = 
				Simulation.instance().getMars().getSurfaceFeatures().getExploredLocations().iterator();
			while (i.hasNext()) {
				ExploredLocation site = i.next();
				if (!site.isMined()) {
					Coordinates siteLocation = site.getLocation();
					Coordinates homeLocation = homeSettlement.getCoordinates();
					if (homeLocation.getDistance(siteLocation) <= (range / 2D)) {
						double value = getMiningSiteValue(site, homeSettlement);
						if (value > bestValue) {
							result = site;
							bestValue = value;
						}
					}
				}
			}
		}
		catch (Exception e) {
			logger.log(Level.SEVERE, "Error determining best mining site.");
		}
		
		return result;
	}
	
	/**
	 * Gets the estimated mineral value of a mining site.
	 * @param site the mining site.
	 * @param settlement the settlement valuing the minerals.
	 * @return estimated value of the minerals at the site (VP).
	 * @throws MissionException if error determining the value.
	 */
	private static double getMiningSiteValue(ExploredLocation site, Settlement settlement) 
			throws MissionException {
		
		double result = 0D;
		
		Map<String, Double> concentrations = site.getEstimatedMineralConcentrations();
		Iterator<String> i = concentrations.keySet().iterator();
		while (i.hasNext()) {
			String mineralType = i.next();
			try {
				AmountResource mineralResource = AmountResource.findAmountResource(mineralType);
				Good mineralGood = GoodsUtil.getResourceGood(mineralResource);
				double mineralValue = settlement.getGoodsManager().getGoodValuePerMass(mineralGood);
				double concentration = concentrations.get(mineralType);
				double mineralAmount = (concentration / 100D) * MINERAL_BASE_AMOUNT;
				result += mineralValue * mineralAmount;
			}
			catch (Exception e) {
				throw new MissionException(null, e);
			}
		}
		
		return result;
	}
	
    /**
     * Gets the time limit of the trip based on life support capacity.
     * @param useBuffer use time buffer in estimation if true.
     * @return time (millisols) limit.
     * @throws MissionException if error determining time limit.
     */
    private static double getTotalTripTimeLimit(Rover rover, int memberNum, boolean useBuffer) 
    		throws MissionException {
    	
    	Inventory vInv = rover.getInventory();
    	
    	double timeLimit = Double.MAX_VALUE;
    	
    	PersonConfig config = SimulationConfig.instance().getPersonConfiguration();
		
    	try {
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
    	}
    	catch (Exception e) {
    		throw new MissionException(null, e);
    	}
    	
    	// Convert timeLimit into millisols and use error margin.
    	timeLimit = (timeLimit * 1000D);
    	if (useBuffer) timeLimit /= Rover.LIFE_SUPPORT_RANGE_ERROR_MARGIN;
    	
    	return timeLimit;
    }
	
	@Override
	public Map<Class, Integer> getEquipmentNeededForRemainingMission(
			boolean useBuffer) throws MissionException {
    	if (equipmentNeededCache != null) return equipmentNeededCache;
    	else {
    		Map<Class, Integer> result = new HashMap<Class, Integer>();
    	
        	// Include one EVA suit per person on mission.
        	result.put(EVASuit.class, new Integer(getPeopleNumber()));
    		
    		// Include required number of bags.
    		result.put(Bag.class, new Integer(NUMBER_OF_BAGS));
    	
    		equipmentNeededCache = result;
    		return result;
    	}
	}

	@Override
	public Settlement getAssociatedSettlement() {
		return getStartingSettlement();
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
    	result += getEstimatedRemainingMiningSiteTime();
    	return result;
    }
	
    /**
     * Gets the estimated time remaining at mining site in the mission.
     * @return time (millisols)
     */
	private double getEstimatedRemainingMiningSiteTime() {
		double result = 0D;
		
    	// Use estimated remaining mining time at site if still there.
    	if (MINING_SITE.equals(getPhase())) {
    		MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();
    		double timeSpentAtMiningSite = MarsClock.getTimeDiff(currentTime, miningSiteStartTime);
    		double remainingTime = MINING_SITE_TIME - timeSpentAtMiningSite;
    		if (remainingTime > 0D) result = remainingTime;
    	}
    	else {
    		// If mission hasn't reached mining site yet, use estimated mining site time.
    		if (miningSiteStartTime == null) result = MINING_SITE_TIME;
    	}
		
		return result;
	}
	
    @Override
    public Map<Resource, Number> getResourcesNeededForRemainingMission(boolean useBuffer, 
    		boolean parts) throws MissionException {
    	Map<Resource, Number> result = super.getResourcesNeededForRemainingMission(useBuffer, parts);
    	
    	double miningSiteTime = getEstimatedRemainingMiningSiteTime();
    	double timeSols = miningSiteTime / 1000D;
    	
    	int crewNum = getPeopleNumber();
    	
    	// Determine life support supplies needed for trip.
    	try {
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
    	}
    	catch(Exception e) {
    		throw new MissionException(getPhase(), e);
    	}
    	
    	return result;
    }
    
	/**
	 * Gets the range of a trip based on its time limit and mining site.
	 * @param tripTimeLimit time (millisols) limit of trip.
	 * @param averageSpeed the average speed of the vehicle.
	 * @return range (km) limit.
	 */
	private static double getTripTimeRange(double tripTimeLimit, double averageSpeed) {
		double tripTimeTravellingLimit = tripTimeLimit - MINING_SITE_TIME;
    	double millisolsInHour = MarsClock.convertSecondsToMillisols(60D * 60D);
    	double averageSpeedMillisol = averageSpeed / millisolsInHour;
    	return tripTimeTravellingLimit * averageSpeedMillisol;
	}
}