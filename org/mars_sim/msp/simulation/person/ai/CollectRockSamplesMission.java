/**
 * Mars Simulation Project
 * CollectRockSamplesMission.java
 * @version 2.74 2002-02-07
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai;

import java.util.*;
import java.io.Serializable;
import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.structure.*;
import org.mars_sim.msp.simulation.vehicle.*;

/** The CollectRockSamplesMission class is a mission to travel to several
 *  random locations around a settlement and collect rock samples.
 */
class CollectRockSamplesMission extends Mission implements Serializable {

    // Phase constants
    final private static String EMBARK = "Embarking";
    final private static String DRIVING = "Driving";
    final private static String COLLECTSAMPLES =
                                        "Collecting Rock and Soil Samples";
    final private static String DISEMBARK = "Disembarking";
    final private static String DRIVESITE1 = "Driving to Site 1";
    final private static String DRIVEHOME = "Driving Home";

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
            if (!mars.getSurfaceFeatures().inDarkPolarRegion(currentSettlement.getCoordinates())) {
                if (ReserveGroundVehicle.availableVehicles(currentSettlement)) result = 2D;
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

        if ((phase == EMBARK) && !hasPerson(person)) {
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
            if (phase == EMBARK) embarkingPhase(person);
            if (phase.startsWith(DRIVING)) drivingPhase(person);
            if (phase.startsWith(COLLECTSAMPLES)) collectingPhase(person);
            if (phase == DISEMBARK) disembarkingPhase(person);
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

        // Determine collection sites.
        if (collectionSites.size() == 0) {
            determineCollectionSites(vehicle.getRange());
            if (done) {
                endMission();
                return;
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
        if (person.getLocationSituation() != Person.INVEHICLE) {
            person.getMind().getTaskManager().addTask(new EnterVehicle(person, mars, vehicle));
            return;
        }

        // If any people in mission haven't entered the vehicle, return.
        for (int x=0; x < people.size(); x++) {
            Person tempPerson = (Person) people.elementAt(x);
            if (tempPerson.getLocationSituation() != Person.INVEHICLE) return;
        }

        // Make final preperations on vehicle.
	startingSettlement.getInventory().dropUnit(vehicle);
        destination = (Coordinates) collectionSites.elementAt(0);
        vehicle.setDestination(destination);
        vehicle.setDestinationType("Coordinates");

        // Transition phase to Driving.
        phase = DRIVESITE1;
    }

    /** Performs the driving phase of the mission.
     *  @param person the person currently performing the mission
     */
    private void drivingPhase(Person person) {

        if (vehicle.getName().equals("Sandstorm")) {
	    System.out.println(person.getName() + " drivingPhase()");
	}
	    
        // Record starting time and distance to destination.
        if ((startingTime == null) || (startingDistance == 0D)) {
            startingTime = (MarsClock) mars.getMasterClock().getMarsClock().clone();
            startingDistance = vehicle.getCoordinates().getDistance(destination);
        }

        // If vehicle has reached destination, transition to Collecting Rock Samples or Disembarking phase.
        if (person.getCoordinates().equals(destination)) {
            if (siteIndex == collectionSites.size()) {
                phase = DISEMBARK;
            }
            else {
                phase = COLLECTSAMPLES + " from Site " + (siteIndex + 1);
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
		if (vehicle.getDriver() == null) {
		    System.out.println(vehicle.getName() + " driver is null");
		    System.out.println(vehicle.getName() + " status: " + vehicle.getStatus());
		}
                if ((vehicle.getDriver() == null) && (vehicle.getStatus() == Vehicle.PARKED)) {
		    System.out.println(vehicle.getName() + " new driver: " + person.getName());
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
                phase = DRIVEHOME;
                destination = startingSettlement.getCoordinates();
                vehicle.setDestinationSettlement(startingSettlement);
                vehicle.setDestinationType("Settlement");
            }
            else {
                phase = DRIVING + " to site " + (siteIndex + 1);
                destination = (Coordinates) collectionSites.elementAt(siteIndex);
            }
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
	startingSettlement.getInventory().addUnit(vehicle);
        vehicle.setDestinationSettlement(null);
        vehicle.setDestinationType("None");
	vehicle.setSpeed(0D);
        vehicle.setETA(null);

        // Have person exit vehicle if necessary.
        if (person.getLocationSituation() == Person.INVEHICLE) {
            person.getMind().getTaskManager().addTask(new ExitVehicle(person, mars, vehicle, startingSettlement));
            return;
        }

        // Unload vehicle if necessary.
        if (UnloadVehicle.isFullyUnloaded(vehicle)) vehicleUnloaded = true;
        if (!vehicleUnloaded) {
            person.getMind().getTaskManager().addTask(new UnloadVehicle(person, mars, vehicle));
            return;
        }

        // If everyone has disembarked and vehicle is unloaded, end mission.
        boolean allDisembarked = true;
        for (int x=0; x < people.size(); x++) {
            Person tempPerson = (Person) people.elementAt(x);
            if (tempPerson.getLocationSituation() == Person.INVEHICLE) allDisembarked = false;
        }
        if (allDisembarked && UnloadVehicle.isFullyUnloaded(vehicle)) endMission();
    }

    /** Determine the locations of the sample collection sites.
     *  @param vehicleRange the vehicle's driving range
     */
    private void determineCollectionSites(double vehicleRange) {

        Vector tempVector = new Vector();
        int numSites = RandomUtil.getRandomInt(1, 5);
        Coordinates startingLocation = startingSettlement.getCoordinates();

        // Determine first site
        Direction direction = new Direction(RandomUtil.getRandomDouble(2 * Math.PI));
        double limit = vehicleRange / 4D;
        double siteDistance = RandomUtil.getRandomDouble(limit);
        startingLocation = startingLocation.getNewLocation(direction, siteDistance);
        if (mars.getSurfaceFeatures().inDarkPolarRegion(startingLocation)) endMission();
        tempVector.addElement(startingLocation);

        // Determine remaining sites
        double remainingRange = (vehicleRange / 2D) - siteDistance;
        for (int x=1; x < numSites; x++) {

            double startDistanceToSettlement = startingLocation.getDistance(startingSettlement.getCoordinates());

	    // Don't add collection site if greater than remaining vehicle range.
	    if (remainingRange < startDistanceToSettlement) {
                numSites = x;
		break;
            }

            direction = new Direction(RandomUtil.getRandomDouble(2 * Math.PI));
            double tempLimit1 = (remainingRange * remainingRange) - (startDistanceToSettlement * startDistanceToSettlement);
            double tempLimit2 = (2D * remainingRange) - (2D * startDistanceToSettlement * direction.getCosDirection());
            limit = tempLimit1 / tempLimit2;
            siteDistance = RandomUtil.getRandomDouble(limit);
            startingLocation = startingLocation.getNewLocation(direction, siteDistance);
            if (mars.getSurfaceFeatures().inDarkPolarRegion(startingLocation)) endMission();
            tempVector.addElement(startingLocation);
            remainingRange -= siteDistance;
        }

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
        }
    }

    /** Determine if a vehicle is fully loaded with fuel and supplies.
     *  @return true if vehicle is fully loaded.
     */
    private boolean isVehicleLoaded() {
        boolean result = true;

        Inventory i = vehicle.getInventory();
	
	if (i.getResourceRemainingCapacity(Inventory.FUEL) > 0D) result = false;
	if (i.getResourceRemainingCapacity(Inventory.OXYGEN) > 0D) result = false;
	if (i.getResourceRemainingCapacity(Inventory.WATER) > 0D) result = false;
	if (i.getResourceRemainingCapacity(Inventory.FOOD) > 0D) result = false;
	
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
