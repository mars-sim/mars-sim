/**
 * Mars Simulation Project
 * TravelToSettlement.java
 * @version 2.76 2004-06-08
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai.mission;

import java.io.Serializable;
import org.mars_sim.msp.simulation.Coordinates;
import org.mars_sim.msp.simulation.Inventory;
import org.mars_sim.msp.simulation.Resource;
import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.simulation.UnitManager;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.person.ai.task.*;
import org.mars_sim.msp.simulation.structure.*;
import org.mars_sim.msp.simulation.structure.building.*;
import org.mars_sim.msp.simulation.structure.building.function.*;
import org.mars_sim.msp.simulation.time.MarsClock;
import org.mars_sim.msp.simulation.vehicle.*;

/** The TravelToSettlement class is a mission to travel from one settlement 
 *  to another randomly selected one within range of an available rover.  
 *
 *  May also be constructed with predetermined destination. 
 */
public class TravelToSettlement extends Mission implements Serializable {

    // Constant for phases
    private final static String DISEMBARK = "Disembarking";
    private final static String DRIVING = "Driving";
    private final static String EMBARK = "Embarking";
    
    // Minimum number of people to do mission.
    private final static int MIN_PEOPLE = 2;
    
    // Data members
    private Settlement startingSettlement;
    private Settlement destinationSettlement;
    private Rover rover;
    private MarsClock startingTime;
    private double startingDistance;
    private Person lastDriver;
    private boolean roverLoaded;
    private boolean roverUnloaded;

    // Tasks tracked
    private DriveGroundVehicle driveTask; // The current driving task.
    ReserveRover reserveRover;

    /** 
     * Constructs a TravelToSettlement object with destination settlement
     * randomly determined.
     * @param missionManager the mission manager 
     */
    public TravelToSettlement(MissionManager missionManager, Person startingPerson) {
        super("Travel To Settlement", missionManager, startingPerson);
   
        // Initialize data members
        startingSettlement = startingPerson.getSettlement();
        destinationSettlement = null;
        rover = null;
        startingTime = null;
        startingDistance = 0D;
        lastDriver = null;
        roverLoaded = false;
        roverUnloaded = false;

        // Initialize tracked tasks to null;
        reserveRover = null;

        // Set initial phase
        phase = EMBARK;
        
        // System.out.println("Travel to Settlement mission");
    }

    /** Gets the weighted probability that a given person would start this mission.
     *  @param person the given person
     *  @return the weighted probability
     */
    public static double getNewMissionProbability(Person person) {

        double result = 0D;

        if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
            Settlement settlement = person.getSettlement();
	    
	    	// Check if available rover.
	    	boolean availableRovers = ReserveRover.availableRovers(null, 0D, settlement);
            
			// At least one person left to hold down the fort.
			boolean remainingInhabitant = false;
			PersonIterator i = settlement.getInhabitants().iterator();
			while (i.hasNext()) {
				Person inhabitant = i.next();
				if (!inhabitant.getMind().hasActiveMission() && (inhabitant != person)) 
					remainingInhabitant = true;
			}
            
            if (availableRovers && remainingInhabitant) result = 1D;
            
            // Crowding modifier.
            int crowding = settlement.getCurrentPopulationNum() - settlement.getPopulationCapacity();
            if (crowding > 0) result *= (crowding + 1);
            
			// Job modifier.
			result *= person.getMind().getJob().getStartMissionProbabilityModifier(TravelToSettlement.class);	            
        }

        return result;
    }

	/** 
	 * Gets the weighted probability that a given person join this mission.
	 * @param person the given person
	 * @return the weighted probability
	 */
	public double getJoiningProbability(Person person) {

		double result = 0D;

		if ((phase.equals(EMBARK)) && !hasPerson(person)) {
			if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
				
				Settlement settlement = person.getSettlement();
				
				// Person is at mission starting settlement.
				boolean inStartingSettlement = (person.getSettlement() == startingSettlement);
				
				// Mission still has room for another person.
				boolean withinMissionCapacity = (people.size() < missionCapacity);
				
				// At least one person left to hold down the fort.
				boolean remainingInhabitant = false;
				PersonIterator i = settlement.getInhabitants().iterator();
				while (i.hasNext()) {
					Person inhabitant = i.next();
					if (!inhabitant.getMind().hasActiveMission() && (inhabitant != person)) 
						remainingInhabitant = true;
				}
				
				if (inStartingSettlement && withinMissionCapacity && remainingInhabitant) 
					result = 50D;
				
				// Crowding modifier.
				int crowding = settlement.getCurrentPopulationNum() - settlement.getPopulationCapacity();
				if (crowding > 0) result *= (crowding + 1);
				
				// Job modifier.
				result *= person.getMind().getJob().getJoinMissionProbabilityModifier(TravelToSettlement.class);					
			}
		}

		return result;
	}	

    /** Performs the mission.
     *  Mission may determine a new task for a person in the mission. 
     *  @param person the person performing the mission 
     */
    public void performMission(Person person) {

        // If the mission has too many people, remove this person.
        if (people.size() > missionCapacity) {
            removePerson(person);
            if (people.size() == 0) endMission(); 
            return;
        }

        // If the mission is not yet completed, perform the mission phase.
        if (!done) {
            if (phase.equals(EMBARK)) embarkingPhase(person);
            if (phase.equals(DRIVING)) drivingPhase(person);
            if (phase.equals(DISEMBARK)) disembarkingPhase(person); 
        }
    }

    /** Performs the embarking phase of the mission.
     *  @param person the person currently performing the mission
     */ 
    private void embarkingPhase(Person person) {

        // Determine the destination settlement.
        if (destinationSettlement == null) { 
            destinationSettlement = getRandomDestinationSettlement(startingSettlement);
            if (destinationSettlement == null) {
                endMission();
                return;
            } 
            description = "Travel To " + destinationSettlement.getName();
        }

        // Reserve a rover.
        // If a rover cannot be reserved, end mission.
        if (rover == null) {
            if (reserveRover == null) {
                reserveRover = new ReserveRover(null, 0D, person, destinationSettlement.getCoordinates());
                assignTask(person, reserveRover);
                return;
            }
            else { 
                if (reserveRover.isDone()) {
                    rover = reserveRover.getReservedRover();
                    if (rover == null) {
                        endMission(); 
                        return;
                    }
                    else setMissionCapacity(rover.getCrewCapacity());
                }
                else return;
            }
        }
                    
        // Load the rover with fuel and supplies.
        // If there isn't enough supplies available, end mission.
        if (isRoverLoaded()) roverLoaded = true;
        if (!roverLoaded) {
            assignTask(person, new LoadVehicle(person, rover));
            if (!LoadVehicle.hasEnoughSupplies(person.getSettlement(), rover)) endMission(); 
            return;
        }
        
        // Have person get in the rover 
        // When every person in mission is in rover, go to Driving phase.
        if (!person.getLocationSituation().equals(Person.INVEHICLE)) {
            startingSettlement.getInventory().takeUnit(person, rover);
        }
        
        // If any people in mission haven't entered the rover, return.
        PersonIterator i = people.iterator();
        while (i.hasNext()) {
            Person tempPerson = i.next();
            if (!tempPerson.getLocationSituation().equals(Person.INVEHICLE)) return;
        }

        // Make final preperations on rover.
        startingSettlement.getInventory().dropUnitOutside(rover);
        rover.setDestinationSettlement(destinationSettlement);
        rover.setDestinationType("Settlement");

		if (getPeopleNumber() >= MIN_PEOPLE) {
        	// Transition phase to Driving.
        	phase = DRIVING;
		}
		else {
			// Transition phase to Disembarking.
			rover.setDestinationSettlement(startingSettlement);
			phase = DISEMBARK;
			// System.out.println("TravelToSettlementMission does not have required " + MIN_PEOPLE + " people.");
		}
    }

    /** Performs the driving phase of the mission.
     *  @param person the person currently performing the mission
     */ 
    private void drivingPhase(Person person) {

        // Record starting time and distance to destination.
        if ((startingTime == null) || (startingDistance == 0D)) {
            startingTime = (MarsClock) Simulation.instance().getMasterClock().getMarsClock().clone();
            startingDistance = rover.getCoordinates().getDistance(destinationSettlement.getCoordinates());
        }

        // If rover has reached destination, transition to Disembarking phase.
        if (person.getCoordinates().equals(destinationSettlement.getCoordinates())) {
            phase = DISEMBARK;
            return;
        }
 
        // If rover doesn't currently have a driver, start drive task for person.
        // Can't be immediate last driver.
        if (!rover.getMalfunctionManager().hasMalfunction() && everyoneInRover()) {	    
            if (person == lastDriver) {
                lastDriver = null;
            }
            else {
                if ((rover.getDriver() == null) && (rover.getStatus().equals(Rover.PARKED))) {
                    Coordinates destination = destinationSettlement.getCoordinates();
                    if (driveTask != null) driveTask = new DriveGroundVehicle(person, rover, 
                        destination, startingTime, startingDistance, driveTask.getPhase()); 
                    else driveTask = new DriveGroundVehicle(person, rover, destination, 
                        startingTime, startingDistance); 
                    assignTask(person, driveTask);
                    lastDriver = person;
                }
            }   
        }     
    }

    /**
     * Checks that everyone in the mission is aboard the rover.
     * @return true if everyone is aboard
     */
    private boolean everyoneInRover() {
        boolean result = true;
        PersonIterator i = people.iterator();
        while (i.hasNext()) {
            if (!i.next().getLocationSituation().equals(Person.INVEHICLE)) result = false;
        }
        return result;
    }

    /** Performs the disembarking phase of the mission.
     *  @param person the person currently performing the mission
     */ 
    private void disembarkingPhase(Person person) {
        
        // Make sure rover is parked at settlement.
        destinationSettlement.getInventory().addUnit(rover);
        rover.setDestinationSettlement(null);
        rover.setDestinationType("None");
        rover.setETA(null);

        // Add rover to a garage if possible.
        VehicleMaintenance garage = null;
        Building garageBuilding = null;
        try {
            BuildingManager.addToRandomBuilding(rover, destinationSettlement);
            garageBuilding = BuildingManager.getBuilding(rover);
            if (garageBuilding != null) 
            	garage = (VehicleMaintenance) garageBuilding.getFunction(GroundVehicleMaintenance.NAME);
        }
        catch (Exception e) {}
        
        // Have person exit rover if necessary.
        if (person.getLocationSituation().equals(Person.INVEHICLE)) {
            rover.getInventory().takeUnit(person, destinationSettlement);
            try {
                if ((garage != null) && garageBuilding.hasFunction(LifeSupport.NAME)) {
                	LifeSupport lifeSupport = (LifeSupport) garageBuilding.getFunction(LifeSupport.NAME);
                	lifeSupport.addPerson(person);
                }
                else BuildingManager.addToRandomBuilding(rover, destinationSettlement);
            }
            catch (BuildingException e) {}
        }

        // Unload rover if necessary.
        if (UnloadVehicle.isFullyUnloaded(rover)) roverUnloaded = true;
        if (!roverUnloaded) {
            assignTask(person, new UnloadVehicle(person, rover));
            return;
        }

        // If everyone has disembarked and rover is unloaded, end mission.
        boolean allDisembarked = true;
        PersonIterator i = people.iterator();
        while (i.hasNext()) {
            Person tempPerson = i.next();
            if (tempPerson.getLocationSituation().equals(Person.INVEHICLE)) allDisembarked = false;
        }
        if (allDisembarked && UnloadVehicle.isFullyUnloaded(rover)) endMission(); 
    }

    /** Determines a random destination settlement other than current one.
     *  @param startingSettlement the settlement the mission is starting at
     *  @return randomly determined settlement
     */
    private Settlement getRandomDestinationSettlement(Settlement startingSettlement) {
        UnitManager unitManager = startingSettlement.getUnitManager();
        Settlement result = null;

        SettlementCollection settlements = unitManager.getSettlements();

        // Create collection of valid destination settlements that have available population capacity.
        SettlementIterator iterator = settlements.iterator();
        while (iterator.hasNext()) {
            Settlement tempSettlement = iterator.next();
            if (tempSettlement == startingSettlement) iterator.remove();
            else if (tempSettlement.getAvailablePopulationCapacity() <= 0) iterator.remove();
        } 

        // Get settlements sorted by proximity to current settlement.
        SettlementCollection sortedSettlements = settlements.sortByProximity(startingSettlement.getCoordinates());

        // Randomly determine settlement with closer settlements being more likely. 
        result = sortedSettlements.getRandomRegressionSettlement();
    
        return result;
    }
 

    /** Determine if a rover is fully loaded with fuel and supplies.
     *  @return true if rover is fully loaded.
     */
    private boolean isRoverLoaded() {
        boolean result = true;

        Inventory i = rover.getInventory();

        if (i.getResourceRemainingCapacity(Resource.METHANE) > 0D) result = false;
        if (i.getResourceRemainingCapacity(Resource.OXYGEN) > 0D) result = false;
        if (i.getResourceRemainingCapacity(Resource.WATER) > 0D) result = false;
        if (i.getResourceRemainingCapacity(Resource.FOOD) > 0D) result = false;

        return result;
    }


    /** Finalizes the mission */
    protected void endMission() {

        if (rover != null) rover.setReserved(false);
        else {
            if ((reserveRover != null) && reserveRover.isDone()) {
                rover = reserveRover.getReservedRover();
                if (rover != null) rover.setReserved(false); 
            }
        }

        super.endMission();
    }
}