/**
 * Mars Simulation Project
 * TravelToSettlement.java
 * @version 2.73 2001-11-22
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.task;

import java.io.Serializable;
import java.util.*;
import org.mars_sim.msp.simulation.*;

/** The TravelToSettlement class is a mission to travel from one settlement 
 *  to another randomly selected one within range of an available vehicle.  
 *
 *  May also be constructed with predetermined destination. 
 */
class TravelToSettlement extends Mission implements Serializable {

    // Data members
    private Settlement startingSettlement;
    private Settlement destinationSettlement;
    private Vehicle vehicle;
    private MarsClock startingTime;
    private double startingDistance;
    private Person lastDriver;
    private boolean vehicleLoaded;
    private boolean vehicleUnloaded;

    // Tasks tracked
    ReserveGroundVehicle reserveVehicle;

    /** Constructs a TravelToSettlement object with destination settlement
     *  randomly determined.
     *  @param missionManager the mission manager 
     */
    public TravelToSettlement(MissionManager missionManager, Person startingPerson) {
        super("Travel To Settlement", missionManager, startingPerson);
   
        // Initialize data members
        startingSettlement = startingPerson.getSettlement();
        destinationSettlement = null;
        vehicle = null;
        startingTime = null;
        startingDistance = 0D;
        lastDriver = null;
        vehicleLoaded = false;
        vehicleUnloaded = false;

        // Initialize tracked tasks to null;
        reserveVehicle = null;

        // Set initial phase
        phase = "Embarking";
    }

    /** Gets the weighted probability that a given person would start this mission.
     *  @param person the given person
     *  @param mars the virtual Mars
     *  @return the weighted probability
     */
    public static double getNewMissionProbability(Person person, VirtualMars mars) {

        double result = 0D;

        if (person.getLocationSituation().equals("In Settlement")) {
            Settlement currentSettlement = person.getSettlement();
            if (!mars.getSurfaceFeatures().inDarkPolarRegion(currentSettlement.getCoordinates())) {
                if (ReserveGroundVehicle.availableVehicles(currentSettlement)) result = 5D; 
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

        if (phase.equals("Embarking") && !hasPerson(person)) { 
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
            if (phase.equals("Embarking")) embarkingPhase(person);
            if (phase.equals("Driving")) drivingPhase(person);
            if (phase.equals("Disembarking")) disembarkingPhase(person); 
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

        // Reserve a ground vehicle.
        // If a ground vehicle cannot be reserved, end mission.
        if (vehicle == null) {
            if (reserveVehicle == null) {
                reserveVehicle = new ReserveGroundVehicle(person, mars, destinationSettlement.getCoordinates());
                person.getMind().getTaskManager().addTask(reserveVehicle);
                return;
            }
            else { 
                if (reserveVehicle.isDone()) {
                    vehicle = reserveVehicle.getReservedVehicle();
                    if (vehicle == null) {
                        endMission(); 
                        return;
                    }
                    else {
                        if (vehicle.getMaxPassengers() < missionCapacity) 
                            setMissionCapacity(vehicle.getMaxPassengers());
                    }
                }
                else return;
            }
        }
                    
        // Load the vehicle with fuel and supplies.
        // If there isn't enough supplies available, end mission.
        if (isVehicleLoaded()) vehicleLoaded = true;
        if (!vehicleLoaded) {
            LoadVehicle loadVehicle = new LoadVehicle(person, mars, vehicle);
            person.getMind().getTaskManager().addTask(loadVehicle);
            if (!LoadVehicle.hasEnoughSupplies(person.getSettlement(), vehicle)) endMission(); 
            return;
        }
        
        // Have person get in the vehicle
        // When every person in mission is in vehicle, go to Driving phase.
        if (!person.getLocationSituation().equals("In Vehicle")) {
            person.getMind().getTaskManager().addTask(new EnterVehicle(person, mars, vehicle));
            return;
        }
        
        // If any people in mission haven't entered the vehicle, return.
        for (int x=0; x < people.size(); x++) {
            Person tempPerson = (Person) people.elementAt(x);
            if (!tempPerson.getLocationSituation().equals("In Vehicle")) return;
        }

        // Make final preperations on vehicle.
        startingSettlement.vehicleLeave(vehicle);
        vehicle.setSettlement(null);
        vehicle.setDestinationSettlement(destinationSettlement);
        vehicle.setDestinationType("Settlement");

        // Transition phase to Driving.
        phase = "Driving";
    }

    /** Performs the driving phase of the mission.
     *  @param person the person currently performing the mission
     */ 
    private void drivingPhase(Person person) {
       
        // Record starting time and distance to destination.
        if ((startingTime == null) || (startingDistance == 0D)) {
            startingTime = (MarsClock) mars.getMasterClock().getMarsClock().clone();
            startingDistance = vehicle.getCoordinates().getDistance(destinationSettlement.getCoordinates());
        }

        // If vehicle has reached destination, transition to Disembarking phase.
        if (person.getCoordinates().equals(destinationSettlement.getCoordinates())) {
            phase = "Disembarking";
            return;
        }
 
        // If vehicle doesn't currently have a driver, start drive task for person.
        // Can't be immediate last driver and can't be at night time.
        if (mars.getSurfaceFeatures().getSurfaceSunlight(vehicle.getCoordinates()) > 0D) {
            if (person == lastDriver) {
                lastDriver = null;
            }
            else {
                if ((vehicle.getDriver() == null) && vehicle.getStatus().equals("Parked")) {
                    DriveGroundVehicle driveTask = new DriveGroundVehicle(person, mars, (GroundVehicle) vehicle, destinationSettlement.getCoordinates(), startingTime, startingDistance); 
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
        
        // Make sure vehicle is parked at settlement.
        vehicle.setSettlement(destinationSettlement);
        vehicle.setDestinationSettlement(null);
        vehicle.setDestinationType("None");
        vehicle.setStatus("Parked");
        vehicle.setETA(null);

        // Have person exit vehicle if necessary. 
        if (person.getLocationSituation().equals("In Vehicle")) {
            person.getMind().getTaskManager().addTask(new ExitVehicle(person, mars, vehicle, destinationSettlement));
            return;
        }

        // Unload vehicle if necessary.
        if (isVehicleUnloaded()) vehicleUnloaded = true;
        if (!vehicleUnloaded) {
            person.getMind().getTaskManager().addTask(new UnloadVehicle(person, mars, vehicle));
            return;
        }

        // If everyone has disembarked and vehicle is unloaded, end mission.
        boolean allDisembarked = true;
        for (int x=0; x < people.size(); x++) {
            Person tempPerson = (Person) people.elementAt(x);
            if (tempPerson.getLocationSituation().equals("In Vehicle")) allDisembarked = false;
        }
        if (allDisembarked && isVehicleUnloaded()) endMission(); 
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
        result = settlement.getPopulationCapacity() - settlement.getCurrentPopulation();
        
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

    /** Determine if a vehicle is fully loaded with fuel and supplies.
     *  @return true if vehicle is fully loaded.
     */
    private boolean isVehicleLoaded() {
        boolean result = true;

        if (vehicle.getFuel() < vehicle.getFuelCapacity()) result = false;
        if (vehicle.getOxygen() < vehicle.getOxygenCapacity()) result = false;
        if (vehicle.getWater() < vehicle.getWaterCapacity()) result = false;
        if (vehicle.getFood() < vehicle.getFoodCapacity()) result = false;

        return result;
    }

    /** Determine if a vehicle is fully unloaded.
     *  @return true if vehicle is fully unloaded.
     */
    private boolean isVehicleUnloaded() {
        boolean result = true;

        if (vehicle.getFuel() != 0D) result = false;
        if (vehicle.getOxygen() != 0D) result = false;
        if (vehicle.getWater() != 0D) result = false;
        if (vehicle.getFood() != 0D) result = false;

        return result;
    }

    /** Finalizes the mission */
    protected void endMission() {

        if (vehicle != null) vehicle.setReserved(false);
        else {
            if ((reserveVehicle != null) && reserveVehicle.isDone()) {
                vehicle = reserveVehicle.getReservedVehicle();
                if (vehicle != null) vehicle.setReserved(false); 
            }
        }

        done = true;
    }
}
