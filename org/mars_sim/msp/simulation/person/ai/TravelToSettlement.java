/**
 * Mars Simulation Project
 * TravelToSettlement.java
 * @version 2.74 2002-02-19
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai;

import java.io.Serializable;
import java.util.*;
import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.structure.*;
import org.mars_sim.msp.simulation.vehicle.*;

/** The TravelToSettlement class is a mission to travel from one settlement 
 *  to another randomly selected one within range of an available rover.  
 *
 *  May also be constructed with predetermined destination. 
 */
class TravelToSettlement extends Mission implements Serializable {

    // Constant for phases
    private final static String DISEMBARK = "Disembarking";
    private final static String DRIVING = "Driving";
    private final static String EMBARK = "Embarking";
    
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
    ReserveRover reserveRover;

    /** Constructs a TravelToSettlement object with destination settlement
     *  randomly determined.
     *  @param missionManager the mission manager 
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
    }

    /** Gets the weighted probability that a given person would start this mission.
     *  @param person the given person
     *  @param mars the virtual Mars
     *  @return the weighted probability
     */
    public static double getNewMissionProbability(Person person, VirtualMars mars) {

        double result = 0D;

        if (person.getLocationSituation() == Person.INSETTLEMENT) {
            Settlement currentSettlement = person.getSettlement();
            boolean possible = true;
	    
            if (!mars.getSurfaceFeatures().inDarkPolarRegion(currentSettlement.getCoordinates())) {
                if (ReserveRover.availableRovers(currentSettlement)) result = 1D; 
            }
        }

        return result;
    }

    /** Gets the weighted probability that a given person join this mission.
     *  @param person the given person
     *  @return the weighted probability
     */
    public double getJoiningProbability(Person person) {

        double result = 0D;

        if (phase.equals(EMBARK) && !hasPerson(person)) { 
            if (person.getSettlement() == startingSettlement) {
                if (people.size() < missionCapacity) result = 50D;
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
            setMissionCapacity(getSettlementCapacity(destinationSettlement));
            name = "Travel To " + destinationSettlement.getName();
            if (mars.getSurfaceFeatures().inDarkPolarRegion(destinationSettlement.getCoordinates())) {
                endMission(); 
                return;
            }
        }

        // Reserve a rover.
        // If a rover cannot be reserved, end mission.
        if (rover == null) {
            if (reserveRover == null) {
                reserveRover = new ReserveRover(person, mars, destinationSettlement.getCoordinates());
                person.getMind().getTaskManager().addTask(reserveRover);
                return;
            }
            else { 
                if (reserveRover.isDone()) {
                    rover = reserveRover.getReservedRover();
                    if (rover == null) {
                        endMission(); 
                        return;
                    }
                    else {
                        if (rover.getMaxPassengers() < missionCapacity) 
                            setMissionCapacity(rover.getMaxPassengers());
                    }
                }
                else return;
            }
        }
                    
        // Load the rover with fuel and supplies.
        // If there isn't enough supplies available, end mission.
        if (isRoverLoaded()) roverLoaded = true;
        if (!roverLoaded) {
            LoadVehicle loadRover = new LoadVehicle(person, mars, rover);
            person.getMind().getTaskManager().addTask(loadRover);
            if (!LoadVehicle.hasEnoughSupplies(person.getSettlement(), rover)) endMission(); 
            return;
        }
        
        // Have person get in the rover 
        // When every person in mission is in rover, go to Driving phase.
        if (person.getLocationSituation() != Person.INVEHICLE) {
	    startingSettlement.getInventory().takeUnit(person, rover);
        }
        
        // If any people in mission haven't entered the rover, return.
	PersonIterator i = people.iterator();
	while (i.hasNext()) {
            Person tempPerson = i.next();
            if (tempPerson.getLocationSituation() != Person.INVEHICLE) return;
        }

        // Make final preperations on rover.
	startingSettlement.getInventory().dropUnitOutside(rover);
        rover.setDestinationSettlement(destinationSettlement);
        rover.setDestinationType("Settlement");

        // Transition phase to Driving.
        phase = DRIVING;
    }

    /** Performs the driving phase of the mission.
     *  @param person the person currently performing the mission
     */ 
    private void drivingPhase(Person person) {
       
        // Record starting time and distance to destination.
        if ((startingTime == null) || (startingDistance == 0D)) {
            startingTime = (MarsClock) mars.getMasterClock().getMarsClock().clone();
            startingDistance = rover.getCoordinates().getDistance(destinationSettlement.getCoordinates());
        }

        // If rover has reached destination, transition to Disembarking phase.
        if (person.getCoordinates().equals(destinationSettlement.getCoordinates())) {
            phase = DISEMBARK;
            return;
        }
 
        // If rover doesn't currently have a driver, start drive task for person.
        // Can't be immediate last driver and can't be at night time.
        if (mars.getSurfaceFeatures().getSurfaceSunlight(rover.getCoordinates()) > 0D) {
            if (person == lastDriver) {
                lastDriver = null;
            }
            else {
                if ((rover.getDriver() == null) && (rover.getStatus() == Rover.PARKED)) {
                    DriveGroundVehicle driveTask = new DriveGroundVehicle(person, mars, rover, 
				    destinationSettlement.getCoordinates(), startingTime, startingDistance); 
                    person.getMind().getTaskManager().addTask(driveTask);
                    lastDriver = person;
                }   
            }
        }     
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

        // Have person exit rover if necessary. 
        if (person.getLocationSituation() == Person.INVEHICLE) {
	    rover.getInventory().takeUnit(person, destinationSettlement);
        }

        // Unload rover if necessary.
        if (UnloadVehicle.isFullyUnloaded(rover)) roverUnloaded = true;
        if (!roverUnloaded) {
            person.getMind().getTaskManager().addTask(new UnloadVehicle(person, mars, rover));
            return;
        }

        // If everyone has disembarked and rover is unloaded, end mission.
        boolean allDisembarked = true;
	PersonIterator i = people.iterator();
	while (i.hasNext()) {
            Person tempPerson = i.next();
            if (tempPerson.getLocationSituation() == Person.INVEHICLE) allDisembarked = false;
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

        // Create collection of valid destination settlements.
        SettlementIterator iterator = settlements.iterator();
        while (iterator.hasNext()) {
            Settlement tempSettlement = iterator.next();
            if ((tempSettlement == startingSettlement) || (getSettlementCapacity(tempSettlement) < people.size())) 
                iterator.remove();
        }

        // Get settlements sorted by proximity to current settlement.
        SettlementCollection sortedSettlements = settlements.sortByProximity(startingSettlement.getCoordinates());

        // Randomly determine settlement with closer settlements being more likely. 
        result = sortedSettlements.getRandomRegressionSettlement();
    
        return result;
    }
 
    /** Determines settlement capacity.
     *  @param settlement the settlement 
     *  @return settlement capacity as int
     */
    private int getSettlementCapacity(Settlement settlement) {
        int result = 0;
     
        // Determine current capacity of settlement.
        result = settlement.getAvailablePopulationCapacity();
        
        // Subtract number of people currently traveling to settlement.
        VehicleIterator i = mars.getUnitManager().getVehicles().iterator();
        while (i.hasNext()) {
            Vehicle tempVehicle = i.next();
            Settlement tempSettlement = tempVehicle.getDestinationSettlement();
            if ((tempSettlement != null) && (tempSettlement == settlement))
                result -= tempVehicle.getPassengerNum();
        }

        return result;
    }

    /** Determine if a rover is fully loaded with fuel and supplies.
     *  @return true if rover is fully loaded.
     */
    private boolean isRoverLoaded() {
        boolean result = true;

	Inventory i = rover.getInventory();

        if (i.getResourceRemainingCapacity(Inventory.FUEL) > 0D) result = false;
        if (i.getResourceRemainingCapacity(Inventory.OXYGEN) > 0D) result = false;
        if (i.getResourceRemainingCapacity(Inventory.WATER) > 0D) result = false;
        if (i.getResourceRemainingCapacity(Inventory.FOOD) > 0D) result = false;

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

        done = true;
    }
}
