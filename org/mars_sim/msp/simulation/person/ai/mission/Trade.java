/**
 * Mars Simulation Project
 * TravelToSettlement.java
 * @version 2.81 2007-08-12
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai.mission;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.mars_sim.msp.simulation.InventoryException;
import org.mars_sim.msp.simulation.RandomUtil;
import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.simulation.equipment.EVASuit;
import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.person.PersonIterator;
import org.mars_sim.msp.simulation.person.ai.Skill;
import org.mars_sim.msp.simulation.person.ai.job.Job;
import org.mars_sim.msp.simulation.person.ai.task.LoadVehicle;
import org.mars_sim.msp.simulation.person.ai.task.NegotiateTrade;
import org.mars_sim.msp.simulation.person.ai.task.UnloadVehicle;
import org.mars_sim.msp.simulation.resource.AmountResource;
import org.mars_sim.msp.simulation.resource.ItemResource;
import org.mars_sim.msp.simulation.resource.Resource;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.structure.building.Building;
import org.mars_sim.msp.simulation.structure.building.BuildingException;
import org.mars_sim.msp.simulation.structure.building.BuildingManager;
import org.mars_sim.msp.simulation.structure.building.function.GroundVehicleMaintenance;
import org.mars_sim.msp.simulation.structure.building.function.LifeSupport;
import org.mars_sim.msp.simulation.structure.building.function.VehicleMaintenance;
import org.mars_sim.msp.simulation.structure.goods.Good;
import org.mars_sim.msp.simulation.time.MarsClock;
import org.mars_sim.msp.simulation.vehicle.GroundVehicle;
import org.mars_sim.msp.simulation.vehicle.Rover;
import org.mars_sim.msp.simulation.vehicle.Vehicle;
import org.mars_sim.msp.simulation.vehicle.VehicleIterator;

/**
 * A mission for trading between two settlements.
 */
public class Trade extends RoverMission implements Serializable {

	// Default description.
	public static final String DEFAULT_DESCRIPTION = "Trade with Settlement";
	
	// Mission phases.
	public static final String TRADE_DISEMBARKING = "Trade Disembarking";
	public static final String TRADE_NEGOTIATING = "Trade Negotiating";
	public static final String UNLOAD_GOODS = "Unload Goods";
	public static final String LOAD_GOODS = "Load Goods";
	public static final String TRADE_EMBARKING = "Trade Embarking";
	
	// Static members
	static final int MAX_MEMBERS = 2;
	
	// Data members.
	private Settlement tradingSettlement;
	private Map<Good, Integer> sellLoad;
	private Map<Good, Integer> buyLoad;
	private boolean outbound;
	private MarsClock startNegotiationTime;
	private NegotiateTrade negotiationTask;
	
	/**
	 * Constructor.
	 * @param startingPerson the person starting the settlement.
	 * @throws MissionException if error constructing mission.
	 */
	public Trade(Person startingPerson) throws MissionException {
		// Use RoverMission constructor.
		super(DEFAULT_DESCRIPTION, startingPerson);
		
		setMissionCapacity(MAX_MEMBERS);
		outbound = true;
		
		if (!isDone()) {
		
        	// Initialize data members
        	setStartingSettlement(startingPerson.getSettlement());
        	
        	// Get trading settlement
			tradingSettlement = TradeUtil.bestTradeSettlementCache;
			addNavpoint(new NavPoint(tradingSettlement.getCoordinates(), tradingSettlement, 
					tradingSettlement.getName()));
			setDescription("Trade with " + tradingSettlement.getName());
        	
			try {
				// Get sell load
				sellLoad = TradeUtil.determineBestSellLoad(getStartingSettlement(), getRover(), tradingSettlement);
			}
			catch (Exception e) {
				e.printStackTrace(System.err);
				endMission("Could not determine sell load.");
			}
        	
        	// Recruit additional people to mission.
        	if (!isDone()) recruitPeopleForMission(startingPerson);
		}
		
		// Add trade mission phases.
		addPhase(TRADE_DISEMBARKING);
		addPhase(TRADE_NEGOTIATING);
		addPhase(UNLOAD_GOODS);
		addPhase(LOAD_GOODS);
		addPhase(TRADE_EMBARKING);
		
        // Set initial phase
        setPhase(VehicleMission.EMBARKING);
        setPhaseDescription("Embarking from " + getStartingSettlement().getName());
        
        // System.out.println(startingPerson.getName() + " starting Trade mission on " + getRover().getName());
	}
	
    /** 
     * Gets the weighted probability that a given person would start this mission.
     * @param person the given person
     * @return the weighted probability
     */
    public static double getNewMissionProbability(Person person) {

    	double missionProbability = 0D;
    	
    	// Determine job modifier.
    	Job job = person.getMind().getJob();
    	double jobModifier = 0D;
    	if (job != null) jobModifier = job.getStartMissionProbabilityModifier(Trade.class);	
    	
    	// Check if person is in a settlement.
    	boolean inSettlement = person.getLocationSituation().equals(Person.INSETTLEMENT);
    	
        if (inSettlement && (jobModifier > 0D)) {
        	
        	// Check if mission is possible for person based on their circumstance.
        	boolean missionPossible = true;
            Settlement settlement = person.getSettlement();
	    
	    	// Check if available rover.
	    	if (!areVehiclesAvailable(settlement)) missionPossible = false;
            
			// Check if minimum number of people are available at the settlement.
			// Plus one to hold down the fort.
			if (!minAvailablePeopleAtSettlement(settlement, MIN_PEOPLE)) missionPossible = false;
	    	
	    	// Check for the best trade settlement within range.
			double tradeProfit = 0D;
	    	try {
	    		Rover rover = (Rover) getVehicleWithGreatestRange(settlement);
	    		if (rover != null) {
	    			// double startTime = System.currentTimeMillis();
	    			tradeProfit = TradeUtil.getBestTradeProfit(settlement, rover);
	    			// double endTime = System.currentTimeMillis();
	    			// System.out.println(person.getName() + " getBestTradeProfit: " + (endTime - startTime) + " - TP: " + tradeProfit);	    			
	    		}
	    	}
	    	catch (Exception e) {
	    		System.err.println("Error finding vehicles at settlement.");
	    		e.printStackTrace(System.err);
	    	}
	    	
	    	// Determine mission probability.
	        if (missionPossible) {
	        	
	        	// Trade value modifier.
	        	missionProbability = tradeProfit;
	            
	            // Crowding modifier.
	            int crowding = settlement.getCurrentPopulationNum() - settlement.getPopulationCapacity();
	            if (crowding > 0) missionProbability *= (crowding + 1);
	        	
	    		// Job modifier.
	        	missionProbability *= jobModifier;	
	        }
        }

        return missionProbability;
    }
    
    /**
     * Determines a new phase for the mission when the current phase has ended.
     * @throws MissionException if problem setting a new phase.
     */
    protected void determineNewPhase() throws MissionException {
    	if (EMBARKING.equals(getPhase())) {
    		startTravelToNextNode();
    		setPhase(VehicleMission.TRAVELLING);
    		setPhaseDescription("Driving to " + getNextNavpoint().getDescription());
    	}
		else if (TRAVELLING.equals(getPhase())) {
			if (getCurrentNavpoint().isSettlementAtNavpoint()) {
				if (outbound) {
					setPhase(TRADE_DISEMBARKING);
					setPhaseDescription("Disembarking at " + tradingSettlement);
				}
				else {
					setPhase(VehicleMission.DISEMBARKING);
					setPhaseDescription("Disembarking at " + getCurrentNavpoint().getDescription());
				}
			}
		}
		else if (TRADE_DISEMBARKING.equals(getPhase())) {
			setPhase(TRADE_NEGOTIATING);
			setPhaseDescription("Negotiating trade at " + tradingSettlement);
		}
		else if (TRADE_NEGOTIATING.equals(getPhase())) {
			setPhase(UNLOAD_GOODS);
			setPhaseDescription("Unloading sell goods at " + tradingSettlement);
		}
		else if (UNLOAD_GOODS.equals(getPhase())) {
			setPhase(LOAD_GOODS);
			setPhaseDescription("Loading buy goods at " + tradingSettlement);
		}
		else if (LOAD_GOODS.equals(getPhase())) {
			setPhase(TRADE_EMBARKING);
			setPhaseDescription("Embarking at " + tradingSettlement);
		}
		else if (TRADE_EMBARKING.equals(getPhase())) {
			startTravelToNextNode();
			setPhase(VehicleMission.TRAVELLING);
			setPhaseDescription("Driving to " + getNextNavpoint().getDescription());
		}
		else if (DISEMBARKING.equals(getPhase())) endMission("Successfully disembarked.");
    }
    
    /**
     * The person performs the current phase of the mission.
     * @param person the person performing the phase.
     * @throws MissionException if problem performing the phase.
     */
    protected void performPhase(Person person) throws MissionException {
    	super.performPhase(person);
    	if (TRADE_DISEMBARKING.equals(getPhase())) performTradeDisembarkingPhase(person);
    	else if (TRADE_NEGOTIATING.equals(getPhase())) performTradeNegotiatingPhase(person);
    	else if (UNLOAD_GOODS.equals(getPhase())) performUnloadGoodsPhase(person);
    	else if (LOAD_GOODS.equals(getPhase())) performLoadGoodsPhase(person);
    	else if (TRADE_EMBARKING.equals(getPhase())) performTradeEmbarkingPhase(person);
    }
    
    /**
     * Performs the trade disembarking phase.
     * @param person the person performing the mission.
     * @throws MissionException if error performing the phase.
     */
    private void performTradeDisembarkingPhase(Person person) throws MissionException {
    	
        Building garageBuilding = null;
    	
    	// If rover is not parked at settlement, park it.
        if ((getVehicle() != null) && (getVehicle().getSettlement() == null)) {
    		try {
    			tradingSettlement.getInventory().storeUnit(getVehicle());
    		}
    		catch (InventoryException e) {
    			throw new MissionException(VehicleMission.DISEMBARKING, e);
    		}
    		
    		// Add vehicle to a garage if available.
    		try {
    			BuildingManager.addToRandomBuilding((GroundVehicle) getVehicle(), tradingSettlement);
                garageBuilding = BuildingManager.getBuilding(getVehicle());
            }
            catch (BuildingException e) {}
    	}
    	
        // Have person exit rover if necessary.
        if (person.getLocationSituation().equals(Person.INVEHICLE)) {
        	try {
        		getVehicle().getInventory().retrieveUnit(person);
        		tradingSettlement.getInventory().storeUnit(person);
        	}
        	catch (InventoryException e) {
        		throw new MissionException(VehicleMission.DISEMBARKING, e);
        	}
            
            // Add the person to the rover's garage if it's in one.
            // Otherwise add person to another building in the settlement.
            try {
            	garageBuilding = BuildingManager.getBuilding(getVehicle());
                if (isRoverInAGarage() && garageBuilding.hasFunction(LifeSupport.NAME)) {
                	LifeSupport lifeSupport = (LifeSupport) garageBuilding.getFunction(LifeSupport.NAME);
                	lifeSupport.addPerson(person);
                }
                else BuildingManager.addToRandomBuilding(person, tradingSettlement);
            }
            catch (BuildingException e) {
            	throw new MissionException(VehicleMission.DISEMBARKING, e);
            } 
        }
        
        // End the phase when everyone is out of the rover.
        if (isNoOneInRover()) setPhaseEnded(true);
    }
    
    /**
     * Perform the trade negotiating phase.
     * @param person the person performing the phase.
     * @throws MissionException if error performing the phase.
     */
    private void performTradeNegotiatingPhase(Person person) throws MissionException {
    	if (person == getMissionTrader()) {
    		if (negotiationTask != null) {
    			if (negotiationTask.isDone()) {
    				buyLoad = negotiationTask.getBuyLoad();
    				setPhaseEnded(true);
    			}
    		}
    		else {
    			if (startNegotiationTime == null) 
    				startNegotiationTime = (MarsClock) Simulation.instance().getMasterClock().getMarsClock().clone();
    			Person settlementTrader = getSettlementTrader();
    			if (settlementTrader != null) {
    				try {
    					negotiationTask = new NegotiateTrade(tradingSettlement, getStartingSettlement(), getRover(), sellLoad, person, settlementTrader);
    					assignTask(person, negotiationTask);
    				}
    				catch (Exception e) {
    					throw new MissionException(TRADE_NEGOTIATING, e);
    				}
    			}
    			else {
    				MarsClock currentTime = (MarsClock) Simulation.instance().getMasterClock().getMarsClock().clone();
    				double timeDiff = MarsClock.getTimeDiff(startNegotiationTime, currentTime);
    				if (timeDiff > 1000D) {
    					buyLoad = new HashMap<Good, Integer>(0);
    					setPhaseEnded(true);
    				}
    			}
    		}
    	}
    	
    	if (getPhaseEnded()) {
    		outbound = false;
    		addNavpoint(new NavPoint(getStartingSettlement().getCoordinates(), getStartingSettlement(), 
					getStartingSettlement().getName()));
    	}
    }
    
    /**
     * Perform the unload goods phase.
     * @param person the person performing the phase.
     * @throws MissionException if errors performing the phase.
     */
    private void performUnloadGoodsPhase(Person person) throws MissionException {
    	
    	//	Unload rover if necessary.
    	try {
    		// Unload towed vehicle (if necessary).
    		unloadTowedVehicle();
    		
    		if (!UnloadVehicle.isFullyUnloaded(getRover())) {
    			// Random chance of having person unload (this allows person to do other things sometimes)
    			if (RandomUtil.lessThanRandPercent(50)) {
    				assignTask(person, new UnloadVehicle(person, getRover()));
    				return;
    			}
    		}
    		else setPhaseEnded(true);
    	}
    	catch (Exception e) {
    		throw new MissionException(UNLOAD_GOODS, e);
    	}
    }
    
    /**
     * Performs the load goods phase.
     * @param person the person performing the phase.
     * @throws MissionException if error performing the phase.
     */
    private void performLoadGoodsPhase(Person person) throws MissionException {
    	
    	try {
    		// Load towed vehicle (if necessary).
    		loadTowedVehicle();
    		
    		if (!isVehicleLoaded()) {
    			// Check if vehicle can hold enough supplies for mission.
    			if (isVehicleLoadable()) {
    				// Random chance of having person load (this allows person to do other things sometimes)
    				if (RandomUtil.lessThanRandPercent(50)) { 
    					assignTask(person, new LoadVehicle(person, getVehicle(), getResourcesNeededForRemainingMission(true), 
							getEquipmentNeededForRemainingMission(true)));
    				}
    			}
    			else endMission("Vehicle is not loadable (RoverMission).");
    		}
    		else setPhaseEnded(true);
    	}
    	catch (Exception e) {
    		throw new MissionException(VehicleMission.EMBARKING, e);
    	}
    }
    
    /**
     * Unload any towed vehicles.
     */
    private void unloadTowedVehicle() {
    	Vehicle towed = getRover().getTowedVehicle();
    	if (towed != null) {
    		towed.setReservedForMission(false);
    		getRover().setTowedVehicle(null);
    		towed.setTowingVehicle(null);
    	}
    }
    
    /**
     * Load the towed vehicle is not already loaded.
     */
    private void loadTowedVehicle() {
    	if (!isDone() && (getRover().getTowedVehicle() == null)) {
    		String vehicleType = getLoadVehicleType(true);
    		if (vehicleType != null) {
    			Vehicle buyVehicle = getInitialLoadVehicle(vehicleType, true);
    			if (buyVehicle != null) {
    				buyVehicle.setReservedForMission(true);
    				getRover().setTowedVehicle(buyVehicle);
    				buyVehicle.setTowingVehicle(getRover());
    			}	
    			else endMission("Selling vehicle (" + vehicleType + ") is not available (Trade).");
    		}
    	}
    }
    
    /**
     * Performs the trade embarking phase.
     * @param person the person performing the phase.
     * @throws MissionException if error performing the phase.
     */
    private void performTradeEmbarkingPhase(Person person) throws MissionException {
    	
    	try {
    		// If person is not aboard the rover, board rover.
    		if (!person.getLocationSituation().equals(Person.INVEHICLE) && !person.getLocationSituation().equals(Person.BURIED)) {
    			if (person.getLocationSituation().equals(Person.INSETTLEMENT))
    				tradingSettlement.getInventory().retrieveUnit(person);
    			getVehicle().getInventory().storeUnit(person);
    		}
		
    		// If rover is loaded and everyone is aboard, embark from settlement.
    		if (isEveryoneInRover()) {
			
    			// Remove from garage if in garage.
    			Building garageBuilding = BuildingManager.getBuilding(getVehicle());
    			if (garageBuilding != null) {
    				VehicleMaintenance garage = (VehicleMaintenance) garageBuilding.getFunction(GroundVehicleMaintenance.NAME);
    				garage.removeVehicle(getVehicle());
    			}
			
    			// Embark from settlement
    			tradingSettlement.getInventory().retrieveUnit(getVehicle());
    			setPhaseEnded(true);
    		}
		}
    	catch (Exception e) {
    		throw new MissionException(TRADE_EMBARKING, e);
    	}
    }
    
    /** 
     * Performs the embark from settlement phase of the mission.
     * @param person the person currently performing the mission
     * @throws MissionException if error performing phase.
     */ 
    protected void performEmbarkFromSettlementPhase(Person person) throws MissionException {
    	super.performEmbarkFromSettlementPhase(person);
    	
    	if (!isDone() && (getRover().getTowedVehicle() == null)) {
    		String vehicleType = getLoadVehicleType(false);		
    		if (vehicleType != null) {
    			Vehicle sellVehicle = getInitialLoadVehicle(vehicleType, false);
    			if (sellVehicle != null) {
    				sellVehicle.setReservedForMission(true);
    				getRover().setTowedVehicle(sellVehicle);
    				sellVehicle.setTowingVehicle(getRover());
    			}	
    			else endMission("Selling vehicle (" + vehicleType + ") is not available (Trade).");
    		}
    	}
    }
    
    /** 
     * Gets the type of vehicle in a load.
     * @param buy true if buy load, false if sell load.
     * @return vehicle type or null if none.
     */
    private String getLoadVehicleType(boolean buy) {
    	String result = null;
    	
    	Map<Good, Integer> load = null;
    	if (buy) load = buyLoad;
    	else load = sellLoad;
    	
    	Iterator<Good> i = load.keySet().iterator();
    	while (i.hasNext()) {
    		Good good = i.next();
    		if (good.getCategory().equals(Good.VEHICLE)) result = good.getName();
    	}
    	
    	return result;
    }
    
    /**
     * Gets the initial load vehicle.
     * @param vehicleType the vehicle type string.
     * @param buy true if buying load, false if selling load.
     * @return load vehicle.
     */
    private Vehicle getInitialLoadVehicle(String vehicleType, boolean buy) {
    	Vehicle result = null;
		
		if (vehicleType != null) {
			Settlement settlement = null;
			if (buy) settlement = tradingSettlement;
			else settlement = getStartingSettlement();
			
			VehicleIterator j = settlement.getParkedVehicles().iterator();
			while (j.hasNext()) {
				Vehicle vehicle = j.next();
				if (vehicleType.equalsIgnoreCase(vehicle.getDescription())) {
					if ((vehicle != getVehicle()) && !vehicle.isReserved()) result = vehicle;
				}
			}
		}
		
		return result;
    }

    /**
     * Gets the number and types of equipment needed for the mission.
     * @param useBuffer use time buffers in estimation if true.
     * @return map of equipment class and Integer number.
     * @throws Exception if error determining needed equipment.
     */
	public Map<Class, Integer> getEquipmentNeededForRemainingMission(boolean useBuffer) throws Exception {
    	if (equipmentNeededCache != null) return equipmentNeededCache;
    	else {
    		Map<Class, Integer> result = new HashMap<Class, Integer>();
    		
    		// Include two EVA suits.
        	result.put(EVASuit.class, new Integer(2));
    		
        	// Add buy/sell load.
        	Map<Good, Integer> load = null;
        	if (outbound) load = sellLoad;
        	else load = buyLoad;
        	
        	Iterator<Good> i = load.keySet().iterator();
        	while (i.hasNext()) {
        		Good good = i.next();
        		if (good.getCategory().equals(Good.EQUIPMENT)) {
        			Class equipmentClass = good.getClassType();
        			int num = load.get(good).intValue();
        			if (result.containsKey(equipmentClass)) 
        				num += ((Integer) result.get(equipmentClass)).intValue();
        			result.put(equipmentClass, new Integer(num));
        		}
        	}
        	
    		equipmentNeededCache = result;
    		return result;
    	}
	}
	
	/**
	 * Gets a map of all resources needed for the trip.
	 * @param useBuffer should a buffer be used when determining resources?
	 * @param distance the distance of the trip.
	 */
    public Map<Resource, Number> getResourcesNeededForTrip(boolean useBuffer, double distance) throws Exception {
    	Map<Resource, Number> result = super.getResourcesNeededForTrip(useBuffer, distance);
    	
    	// Add buy/sell load.
    	Map<Good, Integer> load = null;
    	if (outbound) load = sellLoad;
    	else load = buyLoad;
    	
    	Iterator<Good> i = load.keySet().iterator();
    	while (i.hasNext()) {
    		Good good = i.next();
    		if (good.getCategory().equals(Good.AMOUNT_RESOURCE)) {
    			AmountResource resource = (AmountResource) good.getObject();
    			double amount = load.get(good).doubleValue();
    			if (result.containsKey(resource)) amount += ((Double) result.get(resource)).doubleValue();
    			result.put(resource, new Double(amount));
    		}
    		else if (good.getCategory().equals(Good.ITEM_RESOURCE)) {
    			ItemResource resource = (ItemResource) good.getObject();
    			int num = load.get(good).intValue();
    			if (result.containsKey(resource)) num += ((Integer) result.get(resource)).intValue();
    			result.put(resource, new Integer(num));
    		}
    	}
    	
    	return result;
    }

	/**
	 * Gets the settlement associated with the mission.
	 * @return settlement or null if none.
	 */
	public Settlement getAssociatedSettlement() {
		return getStartingSettlement();
	}
	
	/**
	 * Checks to see if a person is capable of joining a mission.
	 * @param person the person to check.
	 * @return true if person could join mission.
	 */
	protected boolean isCapableOfMission(Person person) {
		if (super.isCapableOfMission(person)) {
			if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
				if (person.getSettlement() == getStartingSettlement()) return true;
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
		if (!atLeastOnePersonRemainingAtSettlement(getStartingSettlement(), startingPerson)) {
			// Remove last person added to the mission.
			Person lastPerson = (Person) getPeople().get(getPeopleNumber() - 1);
			if (lastPerson != null) {
				lastPerson.getMind().setMission(null);
				if (getPeopleNumber() < getMinPeople()) endMission("Not enough members.");
			}
		}
	}
	
	/**
	 * Compares the quality of two vehicles for use in this mission.
	 * @param firstVehicle the first vehicle to compare
	 * @param secondVehicle the second vehicle to compare
	 * @return -1 if the second vehicle is better than the first vehicle, 
	 * 0 if vehicle are equal in quality,
	 * and 1 if the first vehicle is better than the second vehicle.
	 * @throws IllegalArgumentException if firstVehicle or secondVehicle is null.
	 * @throws Exception if error comparing vehicles.
	 */
	protected int compareVehicles(Vehicle firstVehicle, Vehicle secondVehicle) throws Exception {
		int result = super.compareVehicles(firstVehicle, secondVehicle);
		
		if ((result == 0) && isUsableVehicle(firstVehicle) && isUsableVehicle(secondVehicle)) {
			// Check if one has more general cargo capacity than the other.
			double firstCapacity = firstVehicle.getInventory().getGeneralCapacity();
			double secondCapacity = secondVehicle.getInventory().getGeneralCapacity();
			if (firstCapacity > secondCapacity) result = 1;
			else if (secondCapacity > firstCapacity) result = -1;
				
			// Vehicle with superior range should be ranked higher.
			if (result == 0) {
				if (firstVehicle.getRange() > secondVehicle.getRange()) result = 1;
				else if (firstVehicle.getRange() < secondVehicle.getRange()) result = -1;
			}
		}
		
		return result;
	}
	
	/**
	 * Gets the trader for the mission.
	 * @return the trader.
	 */
	private Person getMissionTrader() {
		Person bestTrader = null;
		int bestTradeSkill = -1;
		
		PersonIterator i = getPeople().iterator();
		while (i.hasNext()) {
			Person person = i.next();
			int tradeSkill = person.getMind().getSkillManager().getEffectiveSkillLevel(Skill.TRADING);
			if (tradeSkill > bestTradeSkill) {
				bestTradeSkill = tradeSkill;
				bestTrader = person;
			}
		}
		
		return bestTrader;
	}
	
	/**
	 * Gets the trader and the destination settlement for the mission.
	 * @return the trader.
	 */
	private Person getSettlementTrader() {
		Person bestTrader = null;
		int bestTradeSkill = -1;
		
		PersonIterator i = tradingSettlement.getInhabitants().iterator();
		while (i.hasNext()) {
			Person person = i.next();
			if (!getPeople().contains(person)) {
				int tradeSkill = person.getMind().getSkillManager().getEffectiveSkillLevel(Skill.TRADING);
				if (tradeSkill > bestTradeSkill) {
					bestTradeSkill = tradeSkill;
					bestTrader = person;
				}
			}
		}
		
		return bestTrader;
	}
}