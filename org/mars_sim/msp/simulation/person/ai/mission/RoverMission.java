/**
 * Mars Simulation Project
 * RoverMission.java
 * @version 2.79 2006-05-15
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai.mission;

import java.util.HashMap;
import java.util.Map;

import org.mars_sim.msp.simulation.InventoryException;
import org.mars_sim.msp.simulation.RandomUtil;
import org.mars_sim.msp.simulation.equipment.EVASuit;
import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.person.PersonIterator;
import org.mars_sim.msp.simulation.person.PhysicalCondition;
import org.mars_sim.msp.simulation.person.ai.task.DriveGroundVehicle;
import org.mars_sim.msp.simulation.person.ai.task.LoadVehicle;
import org.mars_sim.msp.simulation.person.ai.task.OperateVehicle;
import org.mars_sim.msp.simulation.person.ai.task.UnloadVehicle;
import org.mars_sim.msp.simulation.resource.AmountResource;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.structure.building.Building;
import org.mars_sim.msp.simulation.structure.building.BuildingException;
import org.mars_sim.msp.simulation.structure.building.BuildingManager;
import org.mars_sim.msp.simulation.structure.building.function.GroundVehicleMaintenance;
import org.mars_sim.msp.simulation.structure.building.function.LifeSupport;
import org.mars_sim.msp.simulation.structure.building.function.VehicleMaintenance;
import org.mars_sim.msp.simulation.vehicle.*;

/**
 * A mission that involves driving a rover vehicle along a series of navpoints.
 */
public abstract class RoverMission extends VehicleMission {

	// Static members
	protected static final int MIN_PEOPLE = 2;

	/**
	 * Constructor
	 * @param name the name of the mission.
	 * @param startingPerson the person starting the mission.
	 * @throws MissionException if error constructing mission.
	 */
	protected RoverMission(String name, Person startingPerson) throws MissionException {
		// Use VehicleMission constructor.
		super(name, startingPerson, MIN_PEOPLE);
	}
	
	/**
	 * Constructor
	 * @param name the name of the mission.
	 * @param startingPerson the person starting the mission.
	 * @param minPeople the minimum number of people required for mission.
	 * @throws MissionException if error constructing mission.
	 */
	protected RoverMission(String name, Person startingPerson, int minPeople) throws MissionException { 
		// Use VehicleMission constructor.
		super(name, startingPerson, minPeople);
	}
	
	/**
	 * Gets the mission's rover if there is one.
	 * @return vehicle or null if none.
	 */
	public Rover getRover() {
		return (Rover) getVehicle();
	}
	
    /**
     * The person performs the current phase of the mission.
     * @param person the person performing the phase.
     * @throws MissionException if problem performing the phase.
     */
    protected void performPhase(Person person) throws MissionException {
    	if (hasEmergency()) setEmergencyTravelHome(true);
    	super.performPhase(person);
    }
	
	/**
	 * Checks if vehicle is usable for this mission.
	 * (This method should be overridden by children)
	 * @param newVehicle the vehicle to check
	 * @return true if vehicle is usable.
	 * @throws Exception if problem determining if vehicle is usable.
	 */
	protected boolean isUsableVehicle(Vehicle newVehicle) throws Exception {
		boolean usable = super.isUsableVehicle(newVehicle);
		if (!(newVehicle instanceof Rover)) usable = false;
		return usable;
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
	 * @throws Exception if error comparing vehicles.
	 */
	protected int compareVehicles(Vehicle firstVehicle, Vehicle secondVehicle) throws Exception {
		int result = super.compareVehicles(firstVehicle, secondVehicle);
		
		// Check if one can hold more crew than the other.
		if ((result == 0) && (isUsableVehicle(firstVehicle)) && (isUsableVehicle(secondVehicle))) {
			if (((Rover) firstVehicle).getCrewCapacity() > ((Rover) secondVehicle).getCrewCapacity()) result = 1;
			else if (((Rover) firstVehicle).getCrewCapacity() < ((Rover) secondVehicle).getCrewCapacity()) result = -1;
		}
		
		return result;
	}
	
    /**
     * Checks that everyone in the mission is aboard the rover.
     * @return true if everyone is aboard
     */
    protected boolean isEveryoneInRover() {
        boolean result = true;
        PersonIterator i = getPeople().iterator();
        while (i.hasNext()) {
            if (!i.next().getLocationSituation().equals(Person.INVEHICLE)) result = false;
        }
        return result;
    }
    
    /**
     * Checks that no one in the mission is aboard the rover.
     * @return true if no one is aboard
     */
    protected boolean isNoOneInRover() {
    	boolean result = true;
        PersonIterator i = getPeople().iterator();
        while (i.hasNext()) {
            if (i.next().getLocationSituation().equals(Person.INVEHICLE)) result = false;
        }
        return result;
    }
    
    /**
     * Checks if the rover is currently in a garage or not.
     * @return true if rover is in a garage.
     */
    private boolean isRoverInAGarage() {
    	return (BuildingManager.getBuilding(getVehicle()) != null);
    }
    
    /** 
     * Performs the embark from settlement phase of the mission.
     * @param person the person currently performing the mission
     * @throws MissionException if error performing phase.
     */ 
    protected void performEmbarkFromSettlementPhase(Person person) throws MissionException {
    	
    	try {
    		Settlement settlement = getVehicle().getSettlement();
    		if (settlement == null) 
    			throw new MissionException(VehicleMission.EMBARKING, "Vehicle is not at a settlement.");
    	
    		// Add the rover to a garage if possible.
    		if (BuildingManager.getBuilding(getVehicle()) != null) {
    			try {
    				BuildingManager.addToRandomBuilding((Rover) getVehicle(), getVehicle().getSettlement());
    			}
    			catch (BuildingException e) {}
    		}
    	
    		// Load vehicle if not fully loaded.
    		if (!loadedFlag) {
    			if (isVehicleLoaded()) loadedFlag = true;
    			else {
    				// Check if vehicle can hold enough supplies for mission.
    				if (isVehicleLoadable() && LoadVehicle.hasEnoughSupplies(settlement, getResourcesNeededForMission(), 
							getEquipmentNeededForMission(), getPeopleNumber(), getEstimatedRemainingTripTime())) {
    					// Load rover
    					// Random chance of having person load (this allows person to do other things sometimes)
    					if (RandomUtil.lessThanRandPercent(50)) { 
    						assignTask(person, new LoadVehicle(person, getVehicle(), getResourcesNeededForMission(), 
    								getEquipmentNeededForMission()));
    					}
        			}
    				else endMission();
    			}
    		}
    		else {
    			// If person is not aboard the rover, board rover.
    			if (!person.getLocationSituation().equals(Person.INVEHICLE)) {
            		settlement.getInventory().retrieveUnit(person);
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
        			settlement.getInventory().retrieveUnit(getVehicle());
        			setPhaseEnded(true);
        		}
    		}
    	}
    	catch (Exception e) {
    		throw new MissionException(VehicleMission.EMBARKING, e);
    	}
    }
    
    /**
     * Performs the disembark to settlement phase of the mission.
     * @param person the person currently performing the mission.
     * @param disembarkSettlement the settlement to be disembarked to.
     * @throws MissionException if error performing phase.
     */
    protected void performDisembarkToSettlementPhase(Person person, Settlement disembarkSettlement) 
    		throws MissionException {
    	
        Building garageBuilding = null;
        VehicleMaintenance garage = null;
    	
    	// If rover is not parked at settlement, park it.
    	if (getVehicle().getSettlement() == null) {
    		try {
    			disembarkSettlement.getInventory().storeUnit(getVehicle());
    		}
    		catch (InventoryException e) {
    			throw new MissionException(VehicleMission.DISEMBARKING, e);
    		}
    		
    		// Add vehicle to a garage if available.
    		try {
    			BuildingManager.addToRandomBuilding((GroundVehicle) getVehicle(), disembarkSettlement);
                garageBuilding = BuildingManager.getBuilding(getVehicle());
                garage = (VehicleMaintenance) garageBuilding.getFunction(GroundVehicleMaintenance.NAME);
            }
            catch (BuildingException e) {}
    	}
    	
        // Have person exit rover if necessary.
        if (person.getLocationSituation().equals(Person.INVEHICLE)) {
        	try {
        		getVehicle().getInventory().retrieveUnit(person);
        		disembarkSettlement.getInventory().storeUnit(person);
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
                else BuildingManager.addToRandomBuilding(person, disembarkSettlement);
            }
            catch (BuildingException e) {
            	throw new MissionException(VehicleMission.DISEMBARKING, e);
            } 
        }
        
        // If any people are aboard the rover who aren't mission members, carry them into the settlement.
        Rover rover = (Rover) getVehicle();
        if (isNoOneInRover() && (rover.getCrewNum() > 0)) {
        	PersonIterator i = rover.getCrew().iterator();
        	while (i.hasNext()) {
        		Person crewmember = i.next();
        		try {
        			rover.getInventory().retrieveUnit(crewmember);
        			disembarkSettlement.getInventory().storeUnit(crewmember);
        			BuildingManager.addToRandomBuilding(crewmember, disembarkSettlement);
        		}
        		catch (Exception e) {
        			throw new MissionException(VehicleMission.DISEMBARKING, e);
        		}
        	}
        }
        
        // Unload rover if necessary.
        boolean roverUnloaded = UnloadVehicle.isFullyUnloaded(rover);
        if (!roverUnloaded) {
			// Random chance of having person unload (this allows person to do otherthings sometimes)
			if (RandomUtil.lessThanRandPercent(50)) {
				try {
					assignTask(person, new UnloadVehicle(person, rover));
					return;
				}
				catch (Exception e) {
					throw new MissionException(VehicleMission.DISEMBARKING, e);
				}
			}
			else return;
        }
        
        // If everyone has left the rover, end the phase.
        if (isNoOneInRover()) {
        	
        	// If the rover is in a garage, put the rover outside.
        	if (isRoverInAGarage()) {
        		try {
        			garageBuilding = BuildingManager.getBuilding(getVehicle());
                    garage = (VehicleMaintenance) garageBuilding.getFunction(GroundVehicleMaintenance.NAME);
        			garage.removeVehicle(getVehicle());
        		}
        		catch (BuildingException e) {}
        	}
        	
        	// Leave the vehicle.
        	leaveVehicle();
        	
        	setPhaseEnded(true);
        }
    }
    
    /**
     * Gets a new instance of an OperateVehicle task for the person.
     * @param person the person operating the vehicle.
     * @return an OperateVehicle task for the person.
     * @throws Exception if error creating OperateVehicle task.
     */
    protected OperateVehicle getOperateVehicleTask(Person person, String lastOperateVehicleTaskPhase) 
    		throws Exception {
    	OperateVehicle result = null;
    	if (lastOperateVehicleTaskPhase != null) {
    		result = new DriveGroundVehicle(person, getRover(), getNextNavpoint().getLocation(), 
    				getCurrentLegStartingTime(), getCurrentLegDistance(), lastOperateVehicleTaskPhase);
    	}
    	else {
    		result = new DriveGroundVehicle(person, getRover(), getNextNavpoint().getLocation(),
    				getCurrentLegStartingTime(), getCurrentLegDistance());
    	}
    	
    	return result;
    }
    
	/**
	 * Checks to see if at least one inhabitant a settlement is remaining there.
	 * @return true if at least one person left at settlement.
	 */
	protected static boolean atLeastOnePersonRemainingAtSettlement(Settlement settlement) {
		boolean result = false;
		
		if (settlement != null) {
			PersonIterator i = settlement.getInhabitants().iterator();
			while (i.hasNext()) {
				Person inhabitant = i.next();
				if (!inhabitant.getMind().hasActiveMission()) result = true;
			}
		}
		
		return result;
	}
	
	/**
	 * Checks if there is only one person at the associated settlement and he/she has a serious medical problem.
	 * @return true if serious medical problem
	 */
	protected boolean hasDangerousMedicalProblemAtAssociatedSettlement() {
		boolean result = false;
		if (getAssociatedSettlement() != null) {
			if (getAssociatedSettlement().getCurrentPopulationNum() == 1) {
				Person person = (Person) getAssociatedSettlement().getInhabitants().get(0);
				if (person.getPhysicalCondition().hasSeriousMedicalProblems()) result = true;
			}
		}
		return result;
	}
	
	/**
	 * Checks if the mission has an emergency situation.
	 * @return true if emergency.
	 */
	protected boolean hasEmergency() {
		boolean result = super.hasEmergency();
		if (hasDangerousMedicalProblemAtAssociatedSettlement()) result = true;
		return result;
	}
	
	/**
	 * Gets the number and amounts of resources needed for the mission.
	 * @return map of amount and item resources and their Double amount or Integer number.
	 * @throws Exception if error determining needed resources.
	 */
    public Map getResourcesNeededForMission() throws Exception {
    	Map result = super.getResourcesNeededForMission();
    	
    	// Determine estimate time for trip.
    	double time = getEstimatedRemainingTripTime();
    	double timeSols = time / 1000D;
    	
    	int crewNum = getPeopleNumber();
    	
    	// Determine life support supplies needed for trip.
    	result.put(AmountResource.OXYGEN, new Double(PhysicalCondition.getOxygenConsumptionRate() 
    			* timeSols * crewNum * Rover.LIFE_SUPPORT_RANGE_ERROR_MARGIN));
    	result.put(AmountResource.WATER, new Double(PhysicalCondition.getWaterConsumptionRate() 
    			* timeSols * crewNum * Rover.LIFE_SUPPORT_RANGE_ERROR_MARGIN));
    	result.put(AmountResource.FOOD, new Double(PhysicalCondition.getFoodConsumptionRate() 
    			* timeSols * crewNum* Rover.LIFE_SUPPORT_RANGE_ERROR_MARGIN));
    	
    	return result;
    }
    
    /**
     * Gets the number and types of equipment needed for the mission.
     * @return map of equipment class and Integer number.
     * @throws Exception if error determining needed equipment.
     */
    public Map getEquipmentNeededForMission() throws Exception {
    	Map result = new HashMap();
    	
    	// Include one EVA suit per person on mission.
    	result.put(EVASuit.class, new Integer(getPeopleNumber()));
    	
    	return result;
    }
}