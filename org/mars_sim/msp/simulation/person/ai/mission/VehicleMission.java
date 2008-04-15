/**
 * Mars Simulation Project
 * VehicleMission.java
 * @version 2.84 2008-04-14
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai.mission;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

import org.mars_sim.msp.simulation.Coordinates;
import org.mars_sim.msp.simulation.Inventory;
import org.mars_sim.msp.simulation.InventoryException;
import org.mars_sim.msp.simulation.RandomUtil;
import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.simulation.Unit;
import org.mars_sim.msp.simulation.UnitEvent;
import org.mars_sim.msp.simulation.UnitListener;
import org.mars_sim.msp.simulation.equipment.EVASuit;
import org.mars_sim.msp.simulation.events.HistoricalEvent;
import org.mars_sim.msp.simulation.malfunction.Malfunction;
import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.person.ai.task.LoadVehicle;
import org.mars_sim.msp.simulation.person.ai.task.OperateVehicle;
import org.mars_sim.msp.simulation.resource.AmountResource;
import org.mars_sim.msp.simulation.resource.ItemResource;
import org.mars_sim.msp.simulation.resource.Part;
import org.mars_sim.msp.simulation.resource.Resource;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.time.MarsClock;
import org.mars_sim.msp.simulation.vehicle.Vehicle;
import org.mars_sim.msp.simulation.vehicle.VehicleOperator;

/**
 * A mission that involves driving a vehicle along a series of navpoints.
 */
public abstract class VehicleMission extends TravelMission implements UnitListener {
    
	private static String CLASS_NAME = 
	    "org.mars_sim.msp.simulation.person.ai.mission.VehicleMission";
	
	private static Logger logger = Logger.getLogger(CLASS_NAME);
	
	// Mission event types
	public static final String VEHICLE_EVENT = "vehicle";
	public static final String OPERATOR_EVENT = "operator";
	
	// Mission phases
	public static final String EMBARKING = "Embarking";
	public static final String TRAVELLING = "Travelling";
	public static final String DISEMBARKING = "Disembarking";
	
	// Static members
	
	// Buffer distance for determining fuel requirements.
	private static final double BUFFER_DISTANCE = 50D; 
	
	// Modifier for number of parts needed for a trip.
	private static final double PARTS_NUMBER_MODIFIER = 2D;
	
	// Data members
	private Vehicle vehicle;
	private VehicleOperator lastOperator; // The last operator of this vehicle in the mission.
	protected boolean loadedFlag = false; // True if vehicle has been loaded.
	private double startingTravelledDistance; // Vehicle travelled distance at start of mission.
	
    // Mission tasks tracked
    private OperateVehicle operateVehicleTask; // The current operate vehicle task.
    
    // Caches
	protected Map<Class, Integer> equipmentNeededCache;

    /**
     * Constructor
     * @param name the name of the mission.
     * @param startingPerson the person starting the mission
     * @param minPeople the minimum number of mission members allowed
     * @throws MissionException if error constructing mission.
     */
	protected VehicleMission(String name, Person startingPerson, int minPeople) throws MissionException {
		// Use TravelMission constructor.
		super(name, startingPerson, minPeople);
		
		// Add mission phases.
		addPhase(EMBARKING);
		addPhase(TRAVELLING);
		addPhase(DISEMBARKING);
		
		// Reserve a vehicle.
		if (!reserveVehicle(startingPerson)) endMission("No reservable vehicles.");
	}
	
    /**
     * Constructor with vehicle.
     * @param name the name of the mission.
     * @param startingPerson the person starting the mission
     * @param minPeople the minimum number of mission members allowed
     * @param vehicle the vehicle to use on the mission.
     * @throws MissionException if error constructing mission.
     */
	protected VehicleMission(String name, Person startingPerson, int minPeople, 
			Vehicle vehicle) throws MissionException {
		// Use TravelMission constructor.
		super(name, startingPerson, minPeople);
		
		// Add mission phases.
		addPhase(EMBARKING);
		addPhase(TRAVELLING);
		addPhase(DISEMBARKING);
		
		// Set the vehicle.
		setVehicle(vehicle);
	}
	
	/**
	 * Gets the mission's vehicle if there is one.
	 * @return vehicle or null if none.
	 */
	public final Vehicle getVehicle() {
		return vehicle;
	}
	
	/**
	 * Sets the vehicle for this mission.
	 * @param newVehicle the vehicle to use.
	 * @throws MissionException if vehicle cannot be used.
	 */
	protected final void setVehicle(Vehicle newVehicle) throws MissionException {
		if (newVehicle != null) {
			try {
				if (isUsableVehicle(newVehicle)) {
					vehicle = newVehicle;
					startingTravelledDistance = vehicle.getTotalDistanceTraveled();
					newVehicle.setReservedForMission(true);
					vehicle.addUnitListener(this);
					fireMissionUpdate(VEHICLE_EVENT);
				}
				else throw new MissionException(getPhase(), "newVehicle is not usable for this mission.");
			}
			catch (Exception e) {
				throw new MissionException(getPhase(), "Problem determining if vehicle is usable.");
			}
		}
		else throw new IllegalArgumentException("newVehicle is null.");
	}
	
	/**
	 * Checks if the mission has a vehicle.
	 * @return true if vehicle.
	 */
	public final boolean hasVehicle() {
		return (vehicle != null);
	}
	
	/**
	 * Leaves the mission's vehicle and unreserves it.
	 */
	protected final void leaveVehicle() {
		if (hasVehicle()) {
			vehicle.setReservedForMission(false);
			vehicle.removeUnitListener(this);
			vehicle = null;
			fireMissionUpdate(VEHICLE_EVENT);
		}
	}
	
	/**
	 * Checks if vehicle is usable for this mission.
	 * (This method should be added to by children)
	 * @param newVehicle the vehicle to check
	 * @return true if vehicle is usable.
	 * @throws IllegalArgumentException if newVehicle is null.
	 * @throws MissionException if problem checking vehicle is loadable.
	 */
	protected boolean isUsableVehicle(Vehicle newVehicle) throws MissionException {
		if (newVehicle != null) {
			boolean usable = true;
			if (newVehicle.isReserved()) usable = false;
			if (!newVehicle.getStatus().equals(Vehicle.PARKED)) usable = false;
			try {
				if (newVehicle.getInventory().getTotalInventoryMass() > 0D) usable = false;
			}
			catch (InventoryException e) {
				throw new MissionException(getPhase(), e);
			}
			return usable;
		}
		else throw new IllegalArgumentException("isUsableVehicle: newVehicle is null.");
	}
	
	/**
	 * Compares the quality of two vehicles for use in this mission.
	 * (This method should be added to by children)
	 * @param firstVehicle the first vehicle to compare
	 * @param secondVehicle the second vehicle to compare
	 * @return -1 if the second vehicle is better than the first vehicle, 
	 * 0 if vehicle are equal in quality,
	 * and 1 if the first vehicle is better than the second vehicle.
	 * @throws MissionException if error determining vehicle range.
	 */
	protected int compareVehicles(Vehicle firstVehicle, Vehicle secondVehicle) throws MissionException {
		if (isUsableVehicle(firstVehicle)) {
			if (isUsableVehicle(secondVehicle)) return 0;
			else return 1;
		}
		else {
			if (isUsableVehicle(secondVehicle)) return -1;
			else return 0;
		}
	}
	
	/**
	 * Reserves a vehicle for the mission if possible.
	 * @param person the person reserving the vehicle.
	 * @return true if vehicle is reserved, false if unable to.
	 * @throws MissionException if error reserving vehicle.
	 */
	protected final boolean reserveVehicle(Person person) throws MissionException {
		
		Collection<Vehicle> bestVehicles = new ConcurrentLinkedQueue<Vehicle>();
		
		// Create list of best unreserved vehicles for the mission.
		Iterator<Vehicle> i = getAvailableVehicles(person.getSettlement()).iterator();
		while (i.hasNext()) {
			Vehicle availableVehicle = i.next();
			if (bestVehicles.size() > 0) {
				int comparison = compareVehicles(availableVehicle, (Vehicle) bestVehicles.toArray()[0]);
				if (comparison == 0) bestVehicles.add(availableVehicle);
				else if (comparison == 1) {
					 bestVehicles.clear();
					 bestVehicles.add(availableVehicle);
				}
			}
			else bestVehicles.add(availableVehicle);
		}
		
		// Randomly select from the best vehicles.
		if (bestVehicles.size() > 0) {
			int bestVehicleIndex = RandomUtil.getRandomInt(bestVehicles.size() - 1);
			try {
				setVehicle((Vehicle) bestVehicles.toArray()[bestVehicleIndex]);
			}
			catch (Exception e) {}
		}
		
		return hasVehicle();
	}	
	
	/**
	 * Gets a collection of available vehicles at a settlement that are usable for this mission.
	 * @param settlement the settlement to find vehicles.
	 * @return list of available vehicles.
	 * @throws MissionException if problem determining if vehicles are usable.
	 */
	private final Collection<Vehicle> getAvailableVehicles(Settlement settlement) throws MissionException {
		Collection<Vehicle> result = new ConcurrentLinkedQueue<Vehicle>();
		
		Iterator<Vehicle> i = settlement.getParkedVehicles().iterator();
		while (i.hasNext()) {
			Vehicle vehicle = i.next();
			if (isUsableVehicle(vehicle)) result.add(vehicle);
		}
		
		return result;
	}	
	
	/** 
	 * Finalizes the mission 
	 * @param reason the reason of ending the mission.
	 */
	public void endMission(String reason) {
		
		// Set emergency beacon if vehicle is not at settlement.
		if (hasVehicle()) {
			if (getVehicle().getSettlement() == null) setEmergencyBeacon(null, getVehicle(), true);
		}
		
		leaveVehicle();
		super.endMission(reason);
	}	
	
    /** 
     * Determine if a vehicle is sufficiently loaded with fuel and supplies.
     * @return true if rover is loaded.
     * @throws MissionException if error checking vehicle.
     */
    public final boolean isVehicleLoaded() throws MissionException {
    	if (getVehicle() == null) throw new MissionException(getPhase(), "vehicle is null");
    	try {
    		return LoadVehicle.isFullyLoaded(getResourcesToLoad(), getEquipmentToLoad(), getVehicle());
    	}
    	catch (Exception e) {
    		throw new MissionException(getPhase(), e);
    	}
    }
    
    /**
     * Checks if a vehicle can load the supplies needed by the mission.
     * @return true if vehicle is loadable.
     * @throws Exception if error checking vehicle.
     */
    public final boolean isVehicleLoadable() throws MissionException {
    	
    	Map<Resource, Number> resources = getResourcesToLoad();
    	Map<Class, Integer> equipment = getEquipmentToLoad();
    	Vehicle vehicle = getVehicle();
    	Settlement settlement = vehicle.getSettlement();
    	double tripTime = getEstimatedRemainingMissionTime(true);
    	
    	try {
    		boolean vehicleCapacity = LoadVehicle.enoughCapacityForSupplies(resources, equipment, 
    				vehicle, settlement);
    		boolean settlementSupplies = LoadVehicle.hasEnoughSupplies(settlement, vehicle, 
    				resources, equipment, getPeopleNumber(), tripTime);
    		if (!vehicleCapacity) logger.info("Vehicle doesn't have capacity.");
        	if (!settlementSupplies) logger.info("Settlement doesn't have supplies.");
        	
        	return vehicleCapacity && settlementSupplies;
    	}
    	catch (Exception e) {
    		throw new MissionException(getPhase(), e);
    	}
    }
    
    /**
     * Gets the amount of fuel (kg) needed for a trip of a given distance (km).
     * @param tripDistance the distance (km) of the trip.
     * @param fuelEfficiency the vehicle's fuel efficiency (km/kg).
     * @param useBuffer use time buffers in estimation if true.
     * @return amount of fuel needed for trip (kg)
     */
    public static double getFuelNeededForTrip(double tripDistance, double fuelEfficiency, boolean useBuffer) {
    	double result = tripDistance / fuelEfficiency;
    	if (useBuffer) {
    		result *= Vehicle.RANGE_ERROR_MARGIN;
    		result += BUFFER_DISTANCE / fuelEfficiency;
    	}
    	
    	return result;
    }
    
    /**
     * Determines a new phase for the mission when the current phase has ended.
     * @throws MissionException if problem setting a new phase.
     */
    protected void determineNewPhase() throws MissionException {
    	if (EMBARKING.equals(getPhase())) {
    		startTravelToNextNode();
    		setPhase(VehicleMission.TRAVELLING);
    		setPhaseDescription("Travelling to " + getNextNavpoint().getDescription());
    	}
		else if (TRAVELLING.equals(getPhase())) {
			if (getCurrentNavpoint().isSettlementAtNavpoint()) {
				setPhase(VehicleMission.DISEMBARKING);
				setPhaseDescription("Disembarking at " + getCurrentNavpoint().getDescription());
			}
		}
		else if (DISEMBARKING.equals(getPhase())) endMission("Successfully disembarked.");
    }
    
    /**
     * The person performs the current phase of the mission.
     * @param person the person performing the phase.
     * @throws MissionException if problem performing the phase.
     */
    protected void performPhase(Person person) throws MissionException {
    	if (EMBARKING.equals(getPhase())) performEmbarkFromSettlementPhase(person);
		else if (TRAVELLING.equals(getPhase())) performTravelPhase(person);
		else if (DISEMBARKING.equals(getPhase())) performDisembarkToSettlementPhase(person, 
				getCurrentNavpoint().getSettlement());
    }
    
    /**
     * Performs the travel phase of the mission.
     * @param person the person currently performing the mission.
     * @throws MissionException if error performing phase.
     */
    protected final void performTravelPhase(Person person) throws MissionException {
    	
    	NavPoint destination = getNextNavpoint();
    	
    	// If vehicle has not reached destination and isn't broken down, travel to destination.
    	boolean reachedDestination = getVehicle().getCoordinates().equals(destination.getLocation());
    	boolean malfunction = getVehicle().getMalfunctionManager().hasMalfunction();
    	if (!reachedDestination && !malfunction) {
    		// Don't operate vehicle if person was the last operator.
    		if (person != lastOperator) {
    			// If vehicle doesn't currently have an operator, set this person as the operator.
    			if (getVehicle().getOperator() == null) {
    				try {
    					if (operateVehicleTask != null) 
    						operateVehicleTask = getOperateVehicleTask(person, operateVehicleTask.getTopPhase());
    					else operateVehicleTask = getOperateVehicleTask(person, null); 
    					assignTask(person, operateVehicleTask);
    					lastOperator = person;
    				}
    				catch (Exception e) {
    					throw new MissionException(getPhase(), e);
    				}
    			}
    			else {
    				// If emergency, make sure current operate vehicle task is pointed home.
    				if (!operateVehicleTask.getDestination().equals(destination.getLocation())) {
    					operateVehicleTask.setDestination(destination.getLocation());
    					setPhaseDescription("Driving to " + getNextNavpoint().getDescription());
    				}
    			}
    		}
    		else lastOperator = null;
    	}
    	
    	// If the destination has been reached, end the phase.
    	if (reachedDestination) {
    		try {
    			reachedNextNode();
    			setPhaseEnded(true);
    		}
    		catch (Exception e) {
    			throw new MissionException(getPhase(), e);
    		}
    	}
    	
    	try {
    		// Check if enough resources for remaining trip 
    		// or if there is an emergency medical problem.
    		if (!hasEnoughResourcesForRemainingMission(false) || hasEmergency()) {
    			// If not, determine an emergency destination.
    			determineEmergencyDestination(person);
    		}
    		
    		// If vehicle has unrepairable malfunction, end mission.
    		if (hasUnrepairableMalfunction()) endMission("unrepairable malfunction");
    	}
    	catch (Exception e) {
    		throw new MissionException(getPhase(), e.getMessage());
    	}
    }
    
    /**
     * Gets a new instance of an OperateVehicle task for the person.
     * @param person the person operating the vehicle.
     * @return an OperateVehicle task for the person.
     * @throws MissionException if error creating OperateVehicle task.
     */
    protected abstract OperateVehicle getOperateVehicleTask(Person person, 
    		String lastOperateVehicleTaskPhase) throws MissionException;
	
    /** 
     * Performs the embark from settlement phase of the mission.
     * @param person the person currently performing the mission.
     * @throws MissionException if error performing phase.
     */ 
    protected abstract void performEmbarkFromSettlementPhase(Person person) throws MissionException;
    
    /**
     * Performs the disembark to settlement phase of the mission.
     * @param person the person currently performing the mission.
     * @param disembarkSettlement the settlement to be disembarked to.
     * @throws MissionException if error performing phase.
     */
    protected abstract void performDisembarkToSettlementPhase(Person person, 
    		Settlement disembarkSettlement) throws MissionException;
    
    /**
     * Gets the estimated time of arrival (ETA) for the current leg of the mission.
     * @return time (MarsClock) or null if not applicable.
     */
    public final MarsClock getLegETA() {
    	if (TRAVELLING.equals(getPhase())) return operateVehicleTask.getETA();
    	else return null;
    }
    
    /**
     * Gets the estimated time for a trip.
     * @param useBuffer use time buffers in estimation if true.
     * @param distance the distance of the trip.
     * @return time (millisols)
     * @throws MissionException
     */
    public final double getEstimatedTripTime(boolean useBuffer, double distance) throws MissionException {
    	
    	// Determine average driving speed for all mission members.
    	double averageSpeed = getAverageVehicleSpeedForOperators();
    	double millisolsInHour = MarsClock.convertSecondsToMillisols(60D * 60D);
    	double averageSpeedMillisol = averageSpeed / millisolsInHour;
    	
    	double result = distance / averageSpeedMillisol;
    	
    	// If buffer, add one sol.
    	if (useBuffer) result += 1000D;
    	
    	return result;
    }
    
    /**
     * Gets the estimated time remaining for the mission.
     * @param useBuffer Use time buffer in estimations if true.
     * @return time (millisols)
     * @throws MissionException
     */
    public double getEstimatedRemainingMissionTime(boolean useBuffer) throws MissionException {
    	return getEstimatedTripTime(useBuffer, getTotalRemainingDistance());
    }
    
    /**
     * Gets the average operating speed of the mission vehicle for all of the mission members. 
     * @return average operating speed (km/h)
     */
    protected final double getAverageVehicleSpeedForOperators() {
    	
    	double totalSpeed = 0D;
    	Iterator<Person> i = getPeople().iterator();
    	while (i.hasNext()) totalSpeed += getAverageVehicleSpeedForOperator(i.next());
    	
    	return totalSpeed / getPeopleNumber();
    }
    
    /**
     * Gets the average speed of a vehicle with a given person operating it.
     * @param person the vehicle operator.
     * @return average speed (km/h)
     */
    private final double getAverageVehicleSpeedForOperator(Person person) {
    	return OperateVehicle.getAverageVehicleSpeed(vehicle, person);
    }
    
	/**
	 * Gets the number and amounts of resources needed for the mission.
	 * @param useBuffer use time buffers in estimation if true.
	 * @param parts include parts.
	 * @return map of amount and item resources and their Double amount or Integer number.
	 * @throws MissionException if error determining needed resources.
	 */
    public Map<Resource, Number> getResourcesNeededForRemainingMission(boolean useBuffer, 
    		boolean parts) throws MissionException {
    	return getResourcesNeededForTrip(useBuffer, parts, getTotalRemainingDistance());
    }
    
    /**
     * Gets the number and amounts of resources needed for a trip.
     * @param useBuffer use time buffers in estimation if true.
     * @param parts include parts.
     * @param distance the distance (km) of the trip.
     * @return map of amount and item resources and their Double amount or Integer number.
     * @throws MissionException if error determining needed resources.
     */
    public Map<Resource, Number> getResourcesNeededForTrip(boolean useBuffer, boolean parts, 
    		double distance) throws MissionException {
    	Map<Resource, Number> result = new HashMap<Resource, Number>();
    	if (vehicle != null) {
    		result.put(vehicle.getFuelType(), new Double(getFuelNeededForTrip(distance, 
    				vehicle.getFuelEfficiency(), useBuffer)));
    		if (parts) result.putAll(getPartsNeededForTrip(distance));
    	}
    	return result;
    }
    
    /**
     * Gets the parts needed for the trip.
     * @param distance the distance of the trip.
     * @return map of part resources and their number.
     * @throws MissionException if error determining parts.
     */
    protected Map<Resource, Number> getPartsNeededForTrip(double distance) throws MissionException {
    	Map<Resource, Number> result = new HashMap<Resource, Number>();
    	
    	// Determine vehicle parts.
    	if (vehicle != null) {
    		double drivingTime = getEstimatedTripTime(false, distance);
    		double numberAccidents = drivingTime * OperateVehicle.BASE_ACCIDENT_CHANCE;
    		// Average number malfunctions per accident is two.
    		double numberMalfunctions = numberAccidents * 2D; 
    		
    		try {
    			Map<Part, Double> parts = vehicle.getMalfunctionManager().getRepairPartProbabilities();
    			Iterator<Part> i = parts.keySet().iterator();
    			while (i.hasNext()) {
    				Part part = i.next();
    				int number = (int) Math.round(parts.get(part) * numberMalfunctions * PARTS_NUMBER_MODIFIER);
    				if (number > 0) result.put(part, number);
    			}
    		}
    		catch (Exception e) {
    			throw new MissionException(getPhase(), e);
    		}
    	}

    	return result;
    }
    
    /**
     * Checks if there are enough resources available in the vehicle for the remaining mission.
     * @param useBuffers use time buffers for estimation if true.
     * @return true if enough resources.
     * @throws MissionException if error checking resources.
     */
    protected final boolean hasEnoughResourcesForRemainingMission(boolean useBuffers) throws MissionException {
    	return hasEnoughResources(getResourcesNeededForRemainingMission(useBuffers, false));
    }
    
    /**
     * Checks if there are enough resources available in the vehicle.
     * @param neededResources map of amount and item resources and their Double amount or Integer number.
     * @return true if enough resources.
     * @throws MissionException if error checking resources.
     */
    private final boolean hasEnoughResources(Map neededResources) throws MissionException {
    	boolean result = true;
    	
        Inventory inv = vehicle.getInventory();

        Iterator iR = neededResources.keySet().iterator();
        while (iR.hasNext() && result) {
        	Resource resource = (Resource) iR.next();
        	try {
        		if (resource instanceof AmountResource) {
        			double amount = ((Double) neededResources.get(resource)).doubleValue();
        			double amountStored = inv.getAmountResourceStored((AmountResource) resource);
        			if (amountStored < amount) result = false;
        		}
        		else if (resource instanceof ItemResource) {
        			int num = ((Integer) neededResources.get(resource)).intValue();
        			if (inv.getItemResourceNum((ItemResource) resource) < num) result = false;
        		}
        		else throw new MissionException(getPhase(), "Unknown resource type: " + resource);
        	}
        	catch (InventoryException e) {
        		throw new MissionException(getPhase(), e);
        	}
        }
        
        return result;
    }
    
    /**
     * Determines the emergency destination settlement for the mission if one is reachable, 
     * otherwise sets the emergency beacon and ends the mission.
     * @param person the person performing the mission.
     * @throws MissionException if error determining an emergency destination.
     */
    protected final void determineEmergencyDestination(Person person) throws MissionException {
    	
    	// Determine closest settlement.
    	Settlement newDestination = findClosestSettlement();
    	if (newDestination != null) {
    		
    		// Check if enough resources to get to settlement.
    		double distance = getCurrentMissionLocation().getDistance(newDestination.getCoordinates());
    		if (hasEnoughResources(getResourcesNeededForTrip(false, false, distance)) && !hasEmergencyAllCrew()) {
    				
    			// Check if closest settlement is already the next navpoint.
    	    	boolean sameDestination = false;
    	    	NavPoint nextNav = getNextNavpoint();
    	    	if ((nextNav != null) && (newDestination == nextNav.getSettlement())) 
    	    		sameDestination = true; 
    			
    	    	if (!sameDestination) {
    				logger.info(vehicle.getName() + " setting emergency destination to " + newDestination.getName() + ".");
    		
    				// Creating emergency destination mission event.
    				HistoricalEvent newEvent = new MissionHistoricalEvent(person, this, MissionHistoricalEvent.EMERGENCY_DESTINATION);
    				Simulation.instance().getEventManager().registerNewEvent(newEvent);
    		
    				// Set the new destination as the travel mission's next and final navpoint.
    				clearRemainingNavpoints();
    				addNavpoint(new NavPoint(newDestination.getCoordinates(), newDestination, 
    						"emergency destination: " + newDestination.getName()));
    				associateAllMembersWithSettlement(newDestination);
    			}
    		}
    		else endMission("Not enough resources to continue.");
    	}
    	else endMission("No emergency settlement destination found.");
    }
    
    /**
     * Sets the vehicle's emergency beacon on or off.
     * @param person the person performing the mission.
     * @param vehicle the vehicle on the mission.
     * @param beaconOn true if beacon is on, false if not.
     */
    public void setEmergencyBeacon(Person person, Vehicle vehicle, boolean beaconOn) {
		// Creating mission emergency beacon event.
        HistoricalEvent newEvent = new MissionHistoricalEvent(person, this, MissionHistoricalEvent.EMERGENCY_BEACON);
		Simulation.instance().getEventManager().registerNewEvent(newEvent);
		logger.info("Emergency beacon activated on " + vehicle.getName());
		
		vehicle.setEmergencyBeacon(beaconOn);
    }
    
    /**
     * Update mission to the next navpoint destination.
     */
    public void updateTravelDestination() {
    	if (operateVehicleTask != null) operateVehicleTask.setDestination(getNextNavpoint().getLocation());
		setPhaseDescription("Driving to " + getNextNavpoint().getDescription());
    }
    
    /**
     * Finds the closest settlement to the mission.
     * @return settlement
     * @throws MissionException if error finding closest settlement.
     */
    public final Settlement findClosestSettlement() throws MissionException {
    	Settlement result = null;
    	Coordinates location = getCurrentMissionLocation();
    	double closestDistance = Double.MAX_VALUE;
    	
    	Iterator<Settlement> i = Simulation.instance().getUnitManager().getSettlements().iterator();
    	while (i.hasNext()) {
    		Settlement settlement = i.next();
    		double distance = settlement.getCoordinates().getDistance(location);
    		if (distance < closestDistance) {
    			result = settlement;
    			closestDistance = distance;
    		}
    	}
    	
    	return result;
    }
    
	/**
	 * Gets the total distance travelled during the mission so far.
	 * @return distance (km)
	 */
	public final double getTotalDistanceTravelled() {
		if (vehicle != null) return vehicle.getTotalDistanceTraveled() - startingTravelledDistance;
		else return 0D;
	}
	
    /** 
     * Time passing for mission.
     * @param time the amount of time passing (in millisols)
     * @throws Exception if error during time passing.
     */
    public void timePassing(double time) throws Exception {
    	// Add this mission as a vehicle listener (does nothing if already listening to vehicle).
    	// Note this is needed so that mission will reattach itself as a vehicle listener after deserialization
    	// since listener collection is transient. - Scott
    	if (hasVehicle()) getVehicle().addUnitListener(this);
    }
    
	/**
	 * Catch unit update event.
	 * @param event the unit event.
	 */
	public void unitUpdate(UnitEvent event) {
		String type = event.getType();
		if (type.equals(Unit.LOCATION_EVENT)) fireMissionUpdate(DISTANCE_EVENT);
		else if (type.equals(Unit.NAME_EVENT)) fireMissionUpdate(VEHICLE_EVENT);
	}
	
	/**
	 * Gets the number of available EVA suits for a mission at a settlement.
	 * @param settlement the settlement to check.
	 * @return number of available suits.
	 */
	static int getNumberAvailableEVASuitsAtSettlement(Settlement settlement) {
		int result = 0;
		
		result = settlement.getInventory().findNumUnitsOfClass(EVASuit.class);
		
		// Leave one suit for settlement use.
		if (result > 0) result--;
		
		return result;
	}
	
	/**
	 * Gets the resources needed for loading the vehicle.
	 * @return resources and their number.
	 * @throws MissionException if error determining resources.
	 */
	public Map<Resource, Number> getResourcesToLoad() throws MissionException {
		return getResourcesNeededForRemainingMission(true, true);
	}
	
	/**
	 * Gets the equipment needed for loading the vehicle.
	 * @return equipment and their number.
	 * @throws MissionException if error determining equipment.
	 */
	public Map<Class, Integer> getEquipmentToLoad() throws MissionException {
		return getEquipmentNeededForRemainingMission(true);
	}
	
	/**
	 * Checks if the vehicle has a malfunction that cannot be repaired.
	 * @return true if unrepairable malfunction.
	 * @throws MissionException if error checking for malfunction.
	 */
	private boolean hasUnrepairableMalfunction() throws MissionException {
		boolean result = false;
		
		if (vehicle != null) {
			vehicle.getMalfunctionManager();
			Iterator<Malfunction> i = vehicle.getMalfunctionManager().getMalfunctions().iterator();
			while (i.hasNext()) {
				Malfunction malfunction = i.next();
				Map<Part, Integer> parts = malfunction.getRepairParts();
				Iterator<Part> j = parts.keySet().iterator();
				while (j.hasNext()) {
					Part part = j.next();
					int number = parts.get(part);
					try {
						if (vehicle.getInventory().getItemResourceNum(part) < number) result = true;
					}
					catch (InventoryException e) {
						throw new MissionException(getPhase(), e);
					}
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Checks to see if there are any currently embarking missions at the settlement.
	 * @param settlement the settlement.
	 * @return true if embarking missions.
	 */
	protected static boolean hasEmbarkingMissions(Settlement settlement) {
		boolean result = false;
		
		MissionManager manager = Simulation.instance().getMissionManager();
		Iterator<Mission> i = manager.getMissionsForSettlement(settlement).iterator();
		while (i.hasNext()) {
			if (EMBARKING.equals(i.next().getPhase())) result = true;
		}
		
		return result;
	}
}