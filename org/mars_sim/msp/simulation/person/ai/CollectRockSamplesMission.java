/**
 * Mars Simulation Project
 * CollectRockSamplesMission.java
 * @version 2.74 2002-05-13
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai;

import java.util.*;
import java.io.Serializable;
import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.person.medical.*;
import org.mars_sim.msp.simulation.structure.*;
import org.mars_sim.msp.simulation.vehicle.*;

/** The CollectRockSamplesMission class is a mission to travel to several
 *  random locations around a settlement and collect rock samples.
 */
class CollectRockSamplesMission extends Mission implements Serializable {

    // Phase constants
    final private static String EMBARK = "Embarking";
    final private static String DRIVING = "Driving";
    final private static String COLLECTSAMPLES = "Collecting Rock Samples";
    final private static String DISEMBARK = "Disembarking";
    final private static String DRIVESITE1 = "Driving to Site 1";
    final private static String DRIVEHOME = "Driving Home";

    // Amount of rock samples to be gathered at a given site. (in kg.) 
    final private static double SITE_SAMPLE_AMOUNT = 100D;
    
    // Data members
    private Settlement startingSettlement; // The settlement the mission starts at.
    private Coordinates destination; // The current destination of the mission.
    private ExplorerRover rover; // The rover used in the mission.
    private MarsClock startingTime; // The starting time of a driving phase.
    private double startingDistance; // The starting distance to destination of a driving phase.
    private Person lastDriver; // The last driver in a driving phase.
    private boolean roverLoaded; // True if the rover is fully loaded with supplies.
    private boolean roverUnloaded; // True if the rover is fully unloaded of supplies.
    private Vector collectionSites; // The collection sites the mission will go to.
    private int siteIndex; // The index of the current collection site.
    private double collectedSamples; // The amount of samples (kg) collected in a collection phase.
    private double collectingStart; // The starting amount of samples in a rover during a collection phase.
    private MarsClock startCollectingTime; // The time the rock sample collecting is started.
    
    // Tasks tracked
    ReserveRover reserveRover;

    /** Constructs a CollectRockSamplesMission object.
     *  @param missionManager the mission manager
     */
    public CollectRockSamplesMission(MissionManager missionManager, Person startingPerson) {
        super("Collect Rock Samples", missionManager, startingPerson);

        // Initialize data members
        startingSettlement = startingPerson.getSettlement();
        destination = null;
        rover = null;
        startingTime = null;
        startingDistance = 0D;
        lastDriver = null;
        roverLoaded = false;
        roverUnloaded = false;
        collectionSites = new Vector();
        siteIndex = 0;
        collectedSamples = 0D;

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
	    
            if (mars.getSurfaceFeatures().inDarkPolarRegion(currentSettlement.getCoordinates())) 
	        possible = false;
	    
            if (!ReserveRover.availableRovers(ReserveRover.EXPLORER_ROVER, currentSettlement)) possible = false;

	    double rocks = currentSettlement.getInventory().getResourceMass(Inventory.ROCK_SAMPLES);
	    if (rocks >= 300D) possible = false;

            if (currentSettlement.getCurrentPopulationNum() <= 1) possible = false;
	    
	    if (possible) result = 5D;
        }

        return result;
    }

    /** Gets the weighted probability that a given person join this mission.
     *  @param person the given person
     *  @return the weighted probability
     */
    public double getJoiningProbability(Person person) {

        double result = 0D;

        if ((phase.equals(EMBARK)) && !hasPerson(person)) {
            if (person.getSettlement() == startingSettlement) {
                if (people.size() < missionCapacity) {
		    if (people.size() < person.getSettlement().getCurrentPopulationNum()) 	
			result = 50D;
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
            if (phase.startsWith(DRIVING)) drivingPhase(person);
            if (phase.startsWith(COLLECTSAMPLES)) collectingPhase(person);
            if (phase.equals(DISEMBARK)) disembarkingPhase(person);
        }
    }

    /** Performs the embarking phase of the mission.
     *  @param person the person currently performing the mission
     */
    private void embarkingPhase(Person person) {

        // Reserve a rover.
        // If a rover cannot be reserved, end mission.
        if (rover == null) {
            if (reserveRover == null) {
                reserveRover = new ReserveRover(ReserveRover.EXPLORER_ROVER, person, mars, 
	                startingSettlement.getCoordinates());
		assignTask(person, reserveRover);
                return;
            }
            else {
                if (reserveRover.isDone()) {
                    rover = (ExplorerRover) reserveRover.getReservedRover();
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

        // Determine collection sites.
        if (collectionSites.size() == 0) {
            determineCollectionSites(rover.getRange());
            if (done) {
                endMission();
                return;
            }
        }

        // Load the rover with fuel and supplies.
        // If there isn't enough supplies available, end mission.
        if (LoadVehicle.isFullyLoaded(rover)) roverLoaded = true;
        if (!roverLoaded) {
	    assignTask(person, new LoadVehicle(person, mars, rover));
            if (!LoadVehicle.hasEnoughSupplies(person.getSettlement(), rover)) endMission();
            return;
        }

        // Have person get in the rover 
        // When every person in mission is in rover, go to Driving phase.
        if (!person.getLocationSituation().equals(Person.INVEHICLE)) 
	    person.getSettlement().getInventory().takeUnit(person, rover);

        // If any people in mission haven't entered the rover, return.
	PersonIterator i = people.iterator();
	while (i.hasNext()) {
            Person tempPerson = i.next();
            if (!tempPerson.getLocationSituation().equals(Person.INVEHICLE)) return;
        }

        // Make final preperations on rover.
	startingSettlement.getInventory().dropUnit(rover);
        destination = (Coordinates) collectionSites.elementAt(0);
        rover.setDestination(destination);
        rover.setDestinationType("Coordinates");

        // Transition phase to Driving.
        phase = DRIVESITE1;
    }

    /** Performs the driving phase of the mission.
     *  @param person the person currently performing the mission
     */
    private void drivingPhase(Person person) {

        // Record starting time and distance to destination.
        if ((startingTime == null) || (startingDistance == 0D)) {
            startingTime = (MarsClock) mars.getMasterClock().getMarsClock().clone();
            startingDistance = rover.getCoordinates().getDistance(destination);
        }

        // If rover has reached destination, transition to Collecting Rock Samples or Disembarking phase.
        if (person.getCoordinates().equals(destination)) {
            if (siteIndex == collectionSites.size()) {
                phase = DISEMBARK;
            }
            else {
                phase = COLLECTSAMPLES + " from Site " + (siteIndex + 1);
		collectedSamples = 0D;
		collectingStart = rover.getInventory().getResourceMass(Inventory.ROCK_SAMPLES);
		startCollectingTime = (MarsClock) mars.getMasterClock().getMarsClock().clone();
            }
            return;
        }

        // If rover doesn't currently have a driver, start drive task for person.
        // Can't be immediate last driver and can't be at night time.
        if (mars.getSurfaceFeatures().getSurfaceSunlight(rover.getCoordinates()) > 0D) {
            if (!rover.getMalfunctionManager().hasMalfunction()) {
	        if (person == lastDriver) {
                    lastDriver = null;
                }
                else {
                    if ((rover.getDriver() == null) && (rover.getStatus().equals(Vehicle.PARKED))) {
                        DriveGroundVehicle driveTask = new DriveGroundVehicle(person, mars, rover, 
			        destination, startingTime, startingDistance);
		        assignTask(person, driveTask);
                        lastDriver = person;
		    }
                }
            }
        }
    }

    /** Performs the collecting phase of the mission.
     *  @param person the person currently performing the mission
     */
    private void collectingPhase(Person person) {

        boolean endPhase = false;
	
        // Calculate samples collected in phase so far.
	collectedSamples = rover.getInventory().getResourceMass(Inventory.ROCK_SAMPLES) - collectingStart;

	if (everyoneInRover()) {

	    // If collected samples are sufficient for this site, end the collecting phase.
	    if (collectedSamples >= SITE_SAMPLE_AMOUNT) {
		// System.out.println("CollectRockSamplesMission: collectedSamples: " + collectedSamples);
		// MarsClock currentTime = mars.getMasterClock().getMarsClock();
		// double collectionTime = MarsClock.getTimeDiff(currentTime, startCollectingTime);
		// System.out.println("CollectRockSamplesMission: collecting phase time: " + collectionTime);
		endPhase = true;
	    }

	    // Determine if no one can start the collect rock samples task.
	    boolean nobodyCollect = true;
	    PersonIterator j = people.iterator();
	    while (j.hasNext()) {
	        if (CollectRockSamples.canCollectRockSamples(j.next(), rover, mars)) nobodyCollect = false;
	    }
	    
	    // If no one can collect rocks and this is not due to it just being
	    // night time, end the collecting phase.
	    int sunlight = mars.getSurfaceFeatures().getSurfaceSunlight(rover.getCoordinates());
	    if (nobodyCollect && (sunlight > 0)) {
		// System.out.println("CollectRockSamplesMission: nobody can collect and not nighttime.");
		// MarsClock currentTime = mars.getMasterClock().getMarsClock();
		// double collectionTime = MarsClock.getTimeDiff(currentTime, startCollectingTime);
		// System.out.println("CollectRockSamplesMission: collecting phase time: " + collectionTime);
		endPhase = true;
	    }
	}

	if (!endPhase) {
	    if (collectedSamples < SITE_SAMPLE_AMOUNT) {
	        // If person can collect rock samples, start him/her on that task.
	        if (CollectRockSamples.canCollectRockSamples(person, rover, mars)) {
                    CollectRockSamples collectRocks = new CollectRockSamples(person, rover, mars, 
                            SITE_SAMPLE_AMOUNT - collectedSamples, 
                            rover.getInventory().getResourceMass(Inventory.ROCK_SAMPLES));
		    assignTask(person, collectRocks);
                }
            }
	}
	else {
            // End collecting phase.
            siteIndex++;
	    if (hasDangerousMedicalProblems()) siteIndex = collectionSites.size();
            if (siteIndex == collectionSites.size()) {
                phase = DRIVEHOME;
                destination = startingSettlement.getCoordinates();
                rover.setDestinationSettlement(startingSettlement);
                rover.setDestinationType("Settlement");
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

    /**
     * Checks to see if the crew have any dangerous medical problems that require treatment
     * at a settlment.
     * @return true if dangerous medical problems
     */
    private boolean hasDangerousMedicalProblems() {
        boolean result = false;
	PersonIterator i = people.iterator();
	while (i.hasNext()) {
	    Person person = i.next();
	    Iterator meds = person.getPhysicalCondition().getProblems().iterator();
	    while (meds.hasNext()) {
                HealthProblem prob = (HealthProblem) meds.next();
		if (prob.getIllness().getRecoveryTreatment() != null) result = true;
	    }
	}
	return result;
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
	startingSettlement.getInventory().addUnit(rover);
        rover.setDestinationSettlement(null);
        rover.setDestinationType("None");
	rover.setSpeed(0D);
        rover.setETA(null);

        // Have person exit rover if necessary.
        if (person.getLocationSituation().equals(Person.INVEHICLE)) {
	    rover.getInventory().takeUnit(person, startingSettlement);
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

    /** Determine the locations of the sample collection sites.
     *  @param roverRange the rover's driving range
     */
    private void determineCollectionSites(double roverRange) {

        Vector tempVector = new Vector();
        int numSites = RandomUtil.getRandomInt(1, 5);
        Coordinates startingLocation = startingSettlement.getCoordinates();

        // Determine first site
        Direction direction = new Direction(RandomUtil.getRandomDouble(2 * Math.PI));
        double limit = roverRange / 4D;
        double siteDistance = RandomUtil.getRandomDouble(limit);
        startingLocation = startingLocation.getNewLocation(direction, siteDistance);
        if (mars.getSurfaceFeatures().inDarkPolarRegion(startingLocation)) endMission();
        tempVector.addElement(startingLocation);

        // Determine remaining sites
        double remainingRange = (roverRange / 2D) - siteDistance;
        for (int x=1; x < numSites; x++) {

            double startDistanceToSettlement = startingLocation.getDistance(startingSettlement.getCoordinates());

	    // Don't add collection site if greater than remaining rover range.
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

    /** Finalizes the mission */
    protected void endMission() {

        if (rover != null) rover.setReserved(false);
        else {
            if ((reserveRover != null) && reserveRover.isDone()) {
                rover = (ExplorerRover) reserveRover.getReservedRover();
                if (rover != null) rover.setReserved(false);
            }
        }

	super.endMission();
    }
}
