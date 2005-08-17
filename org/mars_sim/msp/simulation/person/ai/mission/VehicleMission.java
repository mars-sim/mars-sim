/**
 * Mars Simulation Project
 * VehicleMission.java
 * @version 2.78 2005-08-02
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai.mission;

import org.mars_sim.msp.simulation.RandomUtil;
import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.person.ai.task.OperateVehicle;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.vehicle.Vehicle;
import org.mars_sim.msp.simulation.vehicle.VehicleCollection;
import org.mars_sim.msp.simulation.vehicle.VehicleOperator;
import org.mars_sim.msp.simulation.vehicle.VehicleIterator;
import org.mars_sim.msp.simulation.time.MarsClock;

/**
 * A mission that involves driving a vehicle along a series of navpoints.
 */
public abstract class VehicleMission extends TravelMission {
	
	// Mission phases
	protected static final String EMBARKING = "Embarking";
	protected static final String TRAVELLING = "Travelling";
	protected static final String DISEMBARKING = "Disembarking";
	
	// Data members
	private Vehicle vehicle;
	private VehicleOperator lastOperator; // The last operator of this vehicle in the mission.
	private boolean vehicleLoaded;
	private boolean vehicleUnloaded;
	
    // Mission tasks tracked
    private OperateVehicle operateVehicleTask; // The current operate vehicle task.

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
		if (!reserveVehicle(startingPerson)) endMission();
	}
	
	/**
	 * Gets the mission's vehicle if there is one.
	 * @return vehicle or null if none.
	 */
	public Vehicle getVehicle() {
		return vehicle;
	}
	
	/**
	 * Sets the vehicle for this mission.
	 * @param newVehicle the vehicle to use.
	 * @throws MissionException if vehicle cannot be used.
	 */
	protected void setVehicle(Vehicle newVehicle) throws MissionException {
		if (newVehicle != null) {
			if (isUsableVehicle(newVehicle)) {
				vehicle = newVehicle;
				newVehicle.setReservedForMission(true);
			}
			throw new MissionException(getPhase(), "newVehicle is not usable for this mission.");
		}
		else throw new IllegalArgumentException("newVehicle is null.");
	}
	
	/**
	 * Checks if the mission has a vehicle.
	 * @return true if vehicle.
	 */
	public boolean hasVehicle() {
		return (vehicle != null);
	}
	
	/**
	 * Leaves the mission's vehicle and unreserves it.
	 */
	protected void leaveVehicle() {
		if (hasVehicle()) {
			vehicle.setReservedForMission(false);
			vehicle = null;
		}
	}
	
	/**
	 * Checks if vehicle is usable for this mission.
	 * (This method should be added to by children)
	 * @param newVehicle the vehicle to check
	 * @return true if vehicle is usable.
	 * @throws IllegalArgumentException if newVehicle is null.
	 */
	protected static boolean isUsableVehicle(Vehicle newVehicle) {
		if (newVehicle != null) {
			boolean usable = true;
			if (newVehicle.isReserved()) usable = false;
			if (!newVehicle.getStatus().equals(Vehicle.PARKED)) usable = false;
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
	 * @throws IllegalArgumentException if firstVehicle or secondVehicle is null.
	 */
	protected static int compareVehicles(Vehicle firstVehicle, Vehicle secondVehicle) {
		if (isUsableVehicle(firstVehicle)) {
			if (isUsableVehicle(secondVehicle)) {
				// Vehicle with superior range should be ranked higher.
				if (firstVehicle.getRange() > secondVehicle.getRange()) return 1;
				else if (firstVehicle.getRange() < secondVehicle.getRange()) return -1;
				else return 0;
			}
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
	 */
	protected boolean reserveVehicle(Person person) {
		
		VehicleCollection bestVehicles = new VehicleCollection();
		
		// Create list of best unreserved vehicles for the mission.
		VehicleIterator i = getAvailableVehicles(person.getSettlement()).iterator();
		while (i.hasNext()) {
			Vehicle availableVehicle = i.next();
			if (bestVehicles.size() > 0) {
				int comparison = compareVehicles(availableVehicle, (Vehicle) bestVehicles.get(0));
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
				setVehicle((Vehicle) bestVehicles.get(bestVehicleIndex));
			}
			catch (Exception e) {}
		}
		
		return hasVehicle();
	}	
	
	/**
	 * Gets a collection of available vehicles at a settlement that are usable for this mission.
	 * @param settlement the settlement to find vehicles.
	 * @return list of available vehicles.
	 */
	private static VehicleCollection getAvailableVehicles(Settlement settlement) {
		VehicleCollection result = new VehicleCollection();
		
		VehicleIterator i = settlement.getParkedVehicles().iterator();
		while (i.hasNext()) {
			Vehicle vehicle = i.next();
			if (isUsableVehicle(vehicle)) result.add(vehicle);    
		}
		
		return result;
	}	
	
	/**
	 * Checks to see if any vehicles are available at a settlement.
	 * @param settlement the settlement to check.
	 * @return true if vehicles are available.
	 */
	protected static boolean areVehiclesAvailable(Settlement settlement) {
		return (getAvailableVehicles(settlement).size() > 0);
	}
	
	/** 
	 * Finalizes the mission 
	 */
	protected void endMission() {
		leaveVehicle();
		super.endMission();
	}	
	
    /** 
     * Determine if a vehicle is sufficiently loaded with fuel and supplies.
     * @param distance the distance (km) the vehicle is to travel.
     * @return true if rover is loaded.
     */
    protected boolean isVehicleLoaded(double distance) {
    	if (distance > vehicle.getRange()) 
    		throw new IllegalArgumentException("Distance out of vehicle range.");
    	if (distance < 0D) throw new IllegalArgumentException("Distance is negative.");
    	
        return vehicle.isLoaded(distance / vehicle.getRange());
    }
    
    /**
     * Determines a new phase for the mission when the current phase has ended.
     * @throws MissionException if problem setting a new phase.
     */
    protected void determineNewPhase() throws MissionException {
    	if (EMBARKING.equals(getPhase())) setPhase(VehicleMission.TRAVELLING);
		else if (TRAVELLING.equals(getPhase())) {
			if (getCurrentNavpoint().isSettlementAtNavpoint()) setPhase(VehicleMission.DISEMBARKING);
		}
		else if (DISEMBARKING.equals(getPhase())) endMission();
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
    protected void performTravelPhase(Person person) throws MissionException {
    	
    	// Initialize travel phase if it's not.
    	if (!TravelMission.TRAVEL_TO_NAVPOINT.equals(getTravelStatus())) startTravelToNextNode(person);
    	
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
    					if (operateVehicleTask != null) {
    						operateVehicleTask = getOperateVehicleTask(person, operateVehicleTask.getPhase());
    					}
    					else operateVehicleTask = getOperateVehicleTask(person, null); 
    					assignTask(person, operateVehicleTask);
    					lastOperator = person;
    				}
    				catch (Exception e) {
    					throw new MissionException(TRAVELLING, e);
    				}
    			}
    			else {
    				// If emergency, make sure current operate vehicle task is pointed home.
    				if (!operateVehicleTask.getDestination().equals(destination.getLocation())) 
    					operateVehicleTask.setDestination(destination.getLocation());
    			}
    		}
    		else lastOperator = null;
    	}
    	
    	// If the destination has been reached, end the phase.
    	if (reachedDestination) {
    		reachedNextNode(person);
    		setPhaseEnded(true);
    	}
    }
    
    /**
     * Gets a new instance of an OperateVehicle task for the person.
     * @param person the person operating the vehicle.
     * @return an OperateVehicle task for the person.
     * @throws Exception if error creating OperateVehicle task.
     */
    protected abstract OperateVehicle getOperateVehicleTask(Person person, 
    		String lastOperateVehicleTaskPhase) throws Exception;
	
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
    public MarsClock getLegETA() {
    	if (TRAVELLING.equals(getPhase())) 
    		return operateVehicleTask.getETA();
    	else return null;
    }
    
	/**
	 * Gets the remaining distance for the current leg of the mission.
	 * @return distance (km) or 0 if not in the travelling phase.
	 */
	public double getCurrentLegRemainingDistance() {
		if (getTravelStatus().equals(TravelMission.TRAVEL_TO_NAVPOINT))
			return vehicle.getCoordinates().getDistance(getNextNavpoint().getLocation());
		else return 0D;
	}
}