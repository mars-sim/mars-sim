/**
 * Mars Simulation Project
 * CollectRockSamplesMission.java
 * @version 2.72 2001-08-12
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.task;

import java.util.*;
import org.mars_sim.msp.simulation.*;

/** The CollectRockSamplesMission class is a mission to travel to several 
 *  random locations around a settlement and collect rock samples. 
 */
class CollectRockSamplesMission extends Mission {

    // Data members
    private Settlement startingSettlement;
    private Coordinates destination;
    private Vehicle vehicle;
    private MarsClock startingTime;
    private double startingDistance;
    private Person lastDriver;
    private boolean vehicleLoaded;
    private boolean vehicleUnloaded;
    private Vector collectionSites;
    private int siteIndex;
    private double collectedSamples;

    // Tasks tracked
    ReserveGroundVehicle reserveVehicle;

    /** Constructs a CollectRockSamplesMission object. 
     *  @param missionManager the mission manager 
     */
    public CollectRockSamplesMission(MissionManager missionManager, Person startingPerson) {
        super("Collect Rock/Soil Samples", missionManager, startingPerson);

        // Initialize data members
        startingSettlement = startingPerson.getSettlement();
        destination = null;
        vehicle = null;
        startingTime = null;
        startingDistance = 0D;
        lastDriver = null;
        vehicleLoaded = false;
        vehicleUnloaded = false;
        collectionSites = new Vector();
        siteIndex = 0;
        collectedSamples = 0D;

        // Initialize tracked tasks to null;
        reserveVehicle = null;

        // Set initial phase
        phase = "Embarking";
        // System.out.println(name + " mission phase: Embarking");

        // System.out.println(name + " started");
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
                int vehicleNum = currentSettlement.getVehicleNum();
                if (vehicleNum > 0) {
                    for (int x=0; x < vehicleNum; x++) {
                        if (!currentSettlement.getVehicle(x).isReserved()) result = 10D;
                    }
                }
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

        // System.out.println(person.getName() + " performing mission: " + name);

        // If the mission has too many people, remove this person.
        if (people.size() > missionCapacity) {
            removePerson(person);
            return;
        }
        if (missionCapacity == 0) done = true;

        // If the mission is not yet completed, perform the mission phase.
        if (!done) {
            if (phase.equals("Embarking")) embarkingPhase(person);
            if (phase.startsWith("Driving")) drivingPhase(person);
            if (phase.startsWith("Collecting Rock and Soil Samples")) collectingPhase(person);
            if (phase.equals("Disembarking")) disembarkingPhase(person); 
        }
    }

    /** Performs the embarking phase of the mission.
     *  @param person the person currently performing the mission
     */ 
    private void embarkingPhase(Person person) {
       
        // Reserve a ground vehicle.
        // If a ground vehicle cannot be reserved, end mission.
        if (vehicle == null) {
            if (reserveVehicle == null) {
                reserveVehicle = new ReserveGroundVehicle(person, mars);
                person.getMind().getTaskManager().addTask(reserveVehicle);
                return;
            }
            else { 
                if (reserveVehicle.isDone()) {
                    vehicle = reserveVehicle.getReservedVehicle();
                    if (vehicle == null) {
                        done = true;
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
                   
        // Determine collection sites.
        if (collectionSites.size() == 0) {
            determineCollectionSites(vehicle.getRange());
            // System.out.println("Collection sites determined: " + collectionSites.size());
            if (done) return;
        }
 
        // Load the vehicle with fuel and supplies.
        // If there isn't enough supplies available, end mission.
        if (isVehicleLoaded()) vehicleLoaded = true;
        if (!vehicleLoaded) {
            LoadVehicle loadVehicle = new LoadVehicle(person, mars, vehicle);
            person.getMind().getTaskManager().addTask(loadVehicle);
            if (!loadVehicle.hasEnoughSupplies()) done = true;
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
        vehicle.setReserved(false);
        destination = (Coordinates) collectionSites.elementAt(0);
        vehicle.setDestination(destination);
        vehicle.setDestinationType("Coordinates");

        // Transition phase to Driving.
        phase = "Driving to Site 1";
        // System.out.println(name + " mission phase: " + phase);
    }

    /** Performs the driving phase of the mission.
     *  @param person the person currently performing the mission
     */ 
    private void drivingPhase(Person person) {
       
        // Record starting time and distance to destination.
        if ((startingTime == null) || (startingDistance == 0D)) {
            // System.out.println("Driving to " + destination.getFormattedLatitudeString() + " " + destination.getFormattedLongitudeString());
            startingTime = (MarsClock) mars.getMasterClock().getMarsClock().clone();
            // System.out.println(name + ": starting time: " + startingTime.getTimeStamp());
            startingDistance = vehicle.getCoordinates().getDistance(destination);
            // System.out.println(name + ": distance to destination: " + startingDistance);
        }

        // If vehicle has reached destination, transition to Collecting Rock Samples or Disembarking phase.
        if (person.getCoordinates().equals(destination)) {
            if (siteIndex == collectionSites.size()) { 
                phase = "Disembarking";
                // System.out.println(name + " mission phase: " + phase);
            }
            else {
                phase = "Collecting Rock and Soil Samples from Site " + (siteIndex + 1);
                // System.out.println(name + " mission phase: " + phase);
            }
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
                    DriveGroundVehicle driveTask = new DriveGroundVehicle(person, mars, (GroundVehicle) vehicle, destination, startingTime, startingDistance); 
                    person.getMind().getTaskManager().addTask(driveTask);
                    lastDriver = person;
                }   
            }
        }     
    }

    /** Performs the collecting phase of the mission.
     *  @param person the person currently performing the mission
     */
    private void collectingPhase(Person person) {

        if (collectedSamples < 1000D) {
            CollectRockSamples collectRocks = new CollectRockSamples(person, mars);
            collectedSamples += 100D;
            person.getMind().getTaskManager().addTask(collectRocks);
        }
        else {
            siteIndex++;
            if (siteIndex == collectionSites.size()) {
                phase = "Driving Home";
                destination = startingSettlement.getCoordinates();
                vehicle.setDestinationSettlement(startingSettlement);
                vehicle.setDestinationType("Settlement");
            }
            else {
                phase = "Driving to Site " + (siteIndex + 1);
                destination = (Coordinates) collectionSites.elementAt(siteIndex);
            }
            // System.out.println(getName() + " mission phase: " + phase);
            collectedSamples = 0D;
            startingTime = null;
            startingDistance = 0D;
        } 
    }

    /** Performs the disembarking phase of the mission.
     *  @param person the person currently performing the mission
     */ 
    private void disembarkingPhase(Person person) {
        
        // Make sure vehicle is parked at settlement.
        vehicle.setSettlement(startingSettlement);
        vehicle.setDestinationSettlement(null);
        vehicle.setDestinationType("None");
        vehicle.setStatus("Parked");
        vehicle.setETA(null);

        // Have person exit vehicle if necessary. 
        if (person.getLocationSituation().equals("In Vehicle")) {
            person.getMind().getTaskManager().addTask(new ExitVehicle(person, mars, vehicle, startingSettlement));
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
        if (allDisembarked && isVehicleUnloaded()) done = true;
    }

    /** Determine the locations of the sample collection sites.
     *  @param vehicleRange the vehicle's driving range
     */
    private void determineCollectionSites(double vehicleRange) {

        Vector tempVector = new Vector();
        int numSites = RandomUtil.getRandomInt(1, 5);
        Coordinates startingLocation = startingSettlement.getCoordinates();
        // System.out.println(vehicle.getName() + "'s range: " + vehicleRange);
        // System.out.println("Start: " + startingLocation.getFormattedLatitudeString() + " " + startingLocation.getFormattedLongitudeString());

        // Determine first site
        Direction direction = new Direction(RandomUtil.getRandomDouble(2 * Math.PI));
        double limit = vehicleRange / 4D;
        double siteDistance = RandomUtil.getRandomDouble(limit);
        // System.out.println("siteDistance 1: " + siteDistance);
        startingLocation = startingLocation.getNewLocation(direction, siteDistance);
        if (mars.getSurfaceFeatures().inDarkPolarRegion(startingLocation)) done = true;
        tempVector.addElement(startingLocation);
        // System.out.println("Site 1: " + startingLocation.getFormattedLatitudeString() + " " + startingLocation.getFormattedLongitudeString());

        // Determine remaining sites
        double remainingRange = (vehicleRange / 2D) - siteDistance;
        for (int x=1; x < numSites; x++) {
            double startDistanceToSettlement = startingLocation.getDistance(startingSettlement.getCoordinates()); 
            direction = new Direction(RandomUtil.getRandomDouble(2 * Math.PI));
            double tempLimit1 = (remainingRange * remainingRange) - (startDistanceToSettlement * startDistanceToSettlement);
            double tempLimit2 = (2D * remainingRange) - (2D * startDistanceToSettlement * direction.getCosDirection());
            limit = tempLimit1 / tempLimit2;
            siteDistance = RandomUtil.getRandomDouble(limit);
            startingLocation = startingLocation.getNewLocation(direction, siteDistance);
            if (mars.getSurfaceFeatures().inDarkPolarRegion(startingLocation)) done = true;
            tempVector.addElement(startingLocation);
            remainingRange -= siteDistance;
            // System.out.println("siteDistance " + (x + 1) + ": " + siteDistance);
            // System.out.println("Site " + (x + 1) + ": " + startingLocation.getFormattedLatitudeString() + " " + startingLocation.getFormattedLongitudeString());
        }
        // System.out.println("remainingRange: " + remainingRange);
      
        // System.out.println("Reordering sites"); 
        // Reorder sites for shortest distance.
        startingLocation = startingSettlement.getCoordinates();
        for (int x=0; x < numSites; x++) {
            Coordinates shortest = (Coordinates) tempVector.elementAt(0); 
            for (int y=1; y < tempVector.size(); y++) {
                Coordinates tempCoordinates = (Coordinates) tempVector.elementAt(y);
                if (startingLocation.getDistance(tempCoordinates) < startingLocation.getDistance(shortest))
                    shortest = tempCoordinates;
            }
            startingLocation = shortest;
            collectionSites.addElement(shortest);
            tempVector.removeElement(shortest);
            // System.out.println("Site " + (x + 1) + ": " + shortest.getFormattedLatitudeString() + " " + shortest.getFormattedLongitudeString());
        }           
        // System.out.println("Collection Sites: " + collectionSites.size()); 
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
}
