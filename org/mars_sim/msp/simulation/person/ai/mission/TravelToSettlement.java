/**
 * Mars Simulation Project
 * TravelToSettlement.java
 * @version 2.75 2003-11-27
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai.mission;

import java.io.Serializable;

import org.mars_sim.msp.simulation.Coordinates;
import org.mars_sim.msp.simulation.Inventory;
import org.mars_sim.msp.simulation.Mars;
import org.mars_sim.msp.simulation.MarsClock;
import org.mars_sim.msp.simulation.Resource;
import org.mars_sim.msp.simulation.UnitManager;
import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.person.PersonIterator;
import org.mars_sim.msp.simulation.person.ai.task.*;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.structure.SettlementCollection;
import org.mars_sim.msp.simulation.structure.SettlementIterator;
import org.mars_sim.msp.simulation.structure.building.BuildingException;
import org.mars_sim.msp.simulation.structure.building.BuildingManager;
import org.mars_sim.msp.simulation.structure.building.InhabitableBuilding;
import org
	.mars_sim
	.msp
	.simulation
	.structure
	.building
	.function
	.VehicleMaintenance;
import org.mars_sim.msp.simulation.vehicle.Crewable;
import org.mars_sim.msp.simulation.vehicle.Rover;
import org.mars_sim.msp.simulation.vehicle.TransportRover;
import org.mars_sim.msp.simulation.vehicle.Vehicle;
import org.mars_sim.msp.simulation.vehicle.VehicleIterator;

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
    private TransportRover rover;
    private MarsClock startingTime;
    private double startingDistance;
    private Person lastDriver;
    private boolean roverLoaded;
    private boolean roverUnloaded;

    // Tasks tracked
    private DriveGroundVehicle driveTask; // The current driving task.
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
    public static double getNewMissionProbability(Person person, Mars mars) {

        double result = 0D;

        if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
            Settlement currentSettlement = person.getSettlement();
            boolean possible = true;
	    
            if (ReserveRover.availableRovers(TransportRover.class, currentSettlement)) {
		        if (currentSettlement.getCurrentPopulationNum() > 1) result = 1D;
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
                if (people.size() < missionCapacity) {
                    if (people.size() < startingSettlement.getCurrentPopulationNum()) result = 50D;
                }
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
            description = "Travel To " + destinationSettlement.getName();
            if (mars.getSurfaceFeatures().inDarkPolarRegion(destinationSettlement.getCoordinates())) {
                endMission(); 
                return;
            }
        }

        // Reserve a rover.
        // If a rover cannot be reserved, end mission.
        if (rover == null) {
            if (reserveRover == null) {
                reserveRover = new ReserveRover(TransportRover.class, person, mars, 
		        destinationSettlement.getCoordinates());
                assignTask(person, reserveRover);
                return;
            }
            else { 
                if (reserveRover.isDone()) {
                    rover = (TransportRover) reserveRover.getReservedRover();
                    if (rover == null) {
                        endMission(); 
                        return;
                    }
                    else {
                        if (rover.getCrewCapacity() < missionCapacity) 
                            setMissionCapacity(rover.getCrewCapacity());
                    }
                }
                else return;
            }
        }
                    
        // Load the rover with fuel and supplies.
        // If there isn't enough supplies available, end mission.
        if (isRoverLoaded()) roverLoaded = true;
        if (!roverLoaded) {
            assignTask(person, new LoadVehicle(person, mars, rover));
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
        // Can't be immediate last driver.
        if (!rover.getMalfunctionManager().hasMalfunction() && everyoneInRover()) {	    
            if (person == lastDriver) {
                lastDriver = null;
            }
            else {
                if ((rover.getDriver() == null) && (rover.getStatus().equals(Rover.PARKED))) {
                    Coordinates destination = destinationSettlement.getCoordinates();
                    if (driveTask != null) driveTask = new DriveGroundVehicle(person, mars, rover, 
                        destination, startingTime, startingDistance, driveTask.getPhase()); 
                    else driveTask = new DriveGroundVehicle(person, mars, rover, destination, 
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
        try {
            BuildingManager.addToRandomBuilding(rover, destinationSettlement);
            garage = BuildingManager.getBuilding(rover);
        }
        catch (Exception e) {}
        
        // Have person exit rover if necessary.
        if (person.getLocationSituation().equals(Person.INVEHICLE)) {
            rover.getInventory().takeUnit(person, destinationSettlement);
            try {
                if ((garage != null) && (garage instanceof InhabitableBuilding))
                    ((InhabitableBuilding) garage).addPerson(person);
                else BuildingManager.addToRandomBuilding(rover, destinationSettlement);
            }
            catch (BuildingException e) { 
                System.out.println("CollectRockSamplesMission.disembarkingPhase(): " + e.getMessage()); 
            }
        }

        // Unload rover if necessary.
        if (UnloadVehicle.isFullyUnloaded(rover)) roverUnloaded = true;
        if (!roverUnloaded) {
            assignTask(person, new UnloadVehicle(person, mars, rover));
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
            if ((tempSettlement != null) && (tempSettlement == settlement)) {
                if (tempVehicle instanceof Crewable)
                result -= ((Crewable) tempVehicle).getCrewCapacity();
            }
        }

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
                rover = (TransportRover) reserveRover.getReservedRover();
                if (rover != null) rover.setReserved(false); 
            }
        }

        super.endMission();
    }
}
