/**
 * Mars Simulation Project
 * CollectResourcesMission.java
 * @version 2.76 2004-06-12
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai.mission;

import java.io.Serializable;
import java.util.*;
import org.mars_sim.msp.simulation.Coordinates;
import org.mars_sim.msp.simulation.Direction;
import org.mars_sim.msp.simulation.Inventory;
import org.mars_sim.msp.simulation.RandomUtil;
import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.simulation.mars.Mars;
import org.mars_sim.msp.simulation.mars.SurfaceFeatures;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.person.ai.task.*;
import org.mars_sim.msp.simulation.person.medical.HealthProblem;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.structure.building.*;
import org.mars_sim.msp.simulation.structure.building.function.*;
import org.mars_sim.msp.simulation.time.MarsClock;
import org.mars_sim.msp.simulation.vehicle.*;

/** 
 * The CollectResourcesMission class is a mission to travel in a rover to several
 * random locations around a settlement and collect resources of a given type.
 */
abstract class CollectResourcesMission extends Mission implements Serializable {

	// Mission phases
	final protected static String EMBARK = "Embarking";
	final protected static String DRIVING = "Driving";
	final protected static String COLLECT_RESOURCES = "Collecting Resources";
	final protected static String DISEMBARK = "Disembarking";
	final protected static String DRIVESITE_1 = "Driving to Site 1";
	final protected static String DRIVE_HOME = "Driving Home";

	// Data members
	protected Settlement startingSettlement; // The settlement the mission starts at.
	private Rover rover; // The rover used in the mission.
	private List collectionSites; // The collection sites the mission will go to.
	private boolean roverLoaded; // True if the rover is fully loaded with supplies.
	private boolean roverUnloaded; // True if the rover is fully unloaded of supplies.
	private Coordinates destination; // The current destination of the mission.
	private String resourceType; // The type of resource to collect.
	private MarsClock startingTime; // The starting time of a driving phase.
	private double startingDistance; // The starting distance to destination of a driving phase.
	private int siteIndex; // The index of the current collection site.
	private double siteCollectedResources; // The amount of resources (kg) collected at a collection site.
	private double collectingStart; // The starting amount of resources in a rover at a collection site.
	private MarsClock startCollectingTime; // The time the resource collecting is started.
	private DriveGroundVehicle driveTask; // The current driving task.
	private Person lastDriver; // The last driver in a driving phase.
	private double siteResourceGoal; // The goal amount of resources to collect at a site (kg).
	private double resourceCollectionRate; // The resource collection rate for a person (kg/millisol). 
	ReserveRover reserveRoverTask;  // Mission task to reserve a rover.
	private int numSites; // Number of collection sites.
	private int minPeople; // Minimum number of people for the mission.

	/**
	 * Constructor
	 * @param missionName The name of the mission.
	 * @param missionManager The mission manager.
	 * @param startingPerson The person starting the mission.
	 * @param resourceType The type of resource (see org.mars_sim.msp.simulation.Resource).
	 * @param siteResourceGoal The goal amount of resources to collect at a site (kg).
	 * @param resourceCollectionRate The resource collection rate for a person (kg/millisol).
	 * @param numSites The number of collection sites.
	 * @param minPeople The mimimum number of people for the mission.
	 */
	CollectResourcesMission(String missionName, MissionManager missionManager, 
			Person startingPerson, String resourceType, double siteResourceGoal, 
			double resourceCollectionRate, int numSites, int minPeople) {
		
		// Use Mission parent constructor
		super(missionName, missionManager, startingPerson);
		
		// Initialize data members.
		startingSettlement = startingPerson.getSettlement();
		this.resourceType = resourceType;
		this.siteResourceGoal = siteResourceGoal;
		this.resourceCollectionRate = resourceCollectionRate;
		this.numSites = numSites;
		this.minPeople = minPeople;
		
		// Set initial mission phase.
		phase = EMBARK;
	}

	/**
	 * A person performs the mission.
	 * @param person the person performming the mission.
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
			if (phase.startsWith(COLLECT_RESOURCES)) collectingPhase(person);
			if (phase.equals(DISEMBARK)) disembarkingPhase(person);
		}
	}
	
	/** 
	 * Performs the embarking phase of the mission.
	 * @param person the person currently performing the mission
	 */
	private void embarkingPhase(Person person) {

		// Reserve a rover.
		// If a rover cannot be reserved, end mission.
		try {
			rover = getReservedRover(person);
			if (rover == null) return;
		}
		catch (Exception e) {
			// System.out.println("CollectResourcesMission.embarkingPhase(): " + e.getMessage());
			endMission();
			return;
		}
        
		// Make sure mission capacity is limited to the rover's crew capacity.
		if (rover.getCrewCapacity() < missionCapacity) setMissionCapacity(rover.getCrewCapacity());

		// Determine collection sites.
		if (collectionSites == null) {
			try {
				collectionSites = determineCollectionSites(rover.getRange());
			}
			catch (Exception e) {
				endMission();
				return;
			}
		}

		// Load the rover with fuel and supplies.
		// If there isn't enough supplies available, end mission.
		if (LoadVehicle.isFullyLoaded(rover)) roverLoaded = true;
		if (!roverLoaded) {
			assignTask(person, new LoadVehicle(person, rover));
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
		destination = (Coordinates) collectionSites.get(0);
		rover.setDestination(destination);
		rover.setDestinationType("Coordinates");

		if (getPeopleNumber() >= minPeople) {
			// Transition phase to Driving.
			phase = DRIVESITE_1;
		}
		else {
			// Transition phase to Disembarking.
			rover.setDestinationSettlement(startingSettlement);
			phase = DISEMBARK;
			// System.out.println("CollectResourcesMission does not have required " + minPeople + " people.");
		}
	}
	
	/** 
	 * Performs the driving phase of the mission.
	 * @param person the person currently performing the mission
	 */
	private void drivingPhase(Person person) {

		// If someone has a dangerous medical problem, drive home.
		if ((hasDangerousMedicalProblems() || hasDangerousMedicalProblemAtHome()) && !phase.equals(DRIVE_HOME)) {
			phase = DRIVE_HOME;
			destination = startingSettlement.getCoordinates();
			rover.setDestinationSettlement(startingSettlement);
			rover.setDestinationType("Settlement");
			startingTime = null;
			startingDistance = 0D;
			siteIndex = collectionSites.size();
		}	    
	    
		// Record starting time and distance to destination.
		if ((startingTime == null) || (startingDistance == 0D)) {
			startingTime = (MarsClock) Simulation.instance().getMasterClock().getMarsClock().clone();
			startingDistance = rover.getCoordinates().getDistance(destination);
		}

		// If rover has reached destination, transition to Collecting Resources or Disembarking phase.
		if (person.getCoordinates().equals(destination)) {
			if (siteIndex == collectionSites.size()) {
				phase = DISEMBARK;
			}
			else {
				phase = COLLECT_RESOURCES + " from Site " + (siteIndex + 1);
				siteCollectedResources = 0D;
				collectingStart = rover.getInventory().getResourceMass(resourceType);
				startCollectingTime = (MarsClock) Simulation.instance().getMasterClock().getMarsClock().clone();
			}
			return;
		}

		// If rover doesn't currently have a driver, start drive task for person.
		// Can't be immediate last driver.
		if (!rover.getMalfunctionManager().hasMalfunction()) {
			if (person == lastDriver) {
				lastDriver = null;
			}
			else {
				if ((rover.getDriver() == null) && (rover.getStatus().equals(Vehicle.PARKED))) {
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
	 * Performs the collecting phase of the mission.
	 * @param person the person currently performing the mission
	 */
	private void collectingPhase(Person person) {

		boolean endPhase = false;
	
		Inventory inv = rover.getInventory();
		double resourcesCollected = inv.getResourceMass(resourceType);
		double resourcesCapacity = inv.getResourceCapacity(resourceType);
	
		// Calculate resources collected at the site so far.
		siteCollectedResources = resourcesCollected - collectingStart;

		if (everyoneInRover()) {

			// Check if rover capacity for resources is met, then end this phase.
			if (resourcesCollected >= resourcesCapacity) endPhase = true;

			// If collected resources are sufficient for this site, end the collecting phase.
			if (siteCollectedResources >= siteResourceGoal) endPhase = true;

			// Determine if no one can start the collect resources task.
			boolean nobodyCollect = true;
			PersonIterator j = people.iterator();
			while (j.hasNext()) {
				if (CollectResources.canCollectResources(j.next(), rover)) nobodyCollect = false;
			}
	    
			// If no one can collect resources and this is not due to it just being
			// night time, end the collecting phase.
			Mars mars = Simulation.instance().getMars();
			boolean inDarkPolarRegion = mars.getSurfaceFeatures().inDarkPolarRegion(rover.getCoordinates());
			double sunlight = mars.getSurfaceFeatures().getSurfaceSunlight(rover.getCoordinates());
			if (nobodyCollect && ((sunlight > 0D) || inDarkPolarRegion)) endPhase = true;
			
			// Anyone in the crew or a single person at the home settlement has a dangerous illness, end phase.
			if (hasDangerousMedicalProblems() || hasDangerousMedicalProblemAtHome()) endPhase = true;
		}

		if (!endPhase) {
			if (siteCollectedResources < siteResourceGoal) {
				// If person can collect resources, start him/her on that task.
				if (CollectResources.canCollectResources(person, rover)) {
					CollectResources collectResources = new CollectResources("Collecting Resources", person, 
							rover, resourceType, resourceCollectionRate, 
							siteResourceGoal - siteCollectedResources, inv.getResourceMass(resourceType));
					assignTask(person, collectResources);
				}
			}
		}
		else {
			// End collecting phase.
			siteIndex++;
			boolean driveHome = false;
            
			// If any of the crew has a dangerous medical problem, head home.
			if (hasDangerousMedicalProblems()) driveHome = true;
			
			// If only one person is at the home settlement and has a serious medical problem, head home.
			if (hasDangerousMedicalProblemAtHome()) driveHome = true;
            
			// If the rover is full of resources, head home.
			if (siteCollectedResources >= resourcesCapacity) driveHome = true;
            
			if (driveHome || (siteIndex == collectionSites.size())) {
				phase = DRIVE_HOME;
				destination = startingSettlement.getCoordinates();
				rover.setDestinationSettlement(startingSettlement);
				rover.setDestinationType("Settlement");
			}
			else {
				phase = DRIVING + " to site " + (siteIndex + 1);
				destination = (Coordinates) collectionSites.get(siteIndex);
			}
			siteCollectedResources = 0D;
			startingTime = null;
			startingDistance = 0D;
		}
	}
	
	/** 
	 * Performs the disembarking phase of the mission.
	 * @param person the person currently performing the mission
	 */
	private void disembarkingPhase(Person person) {

		// Make sure rover is parked at settlement.
		startingSettlement.getInventory().addUnit(rover);
		rover.setDestinationSettlement(null);
		rover.setDestinationType("None");
		rover.setSpeed(0D);
		rover.setETA(null);

		// Add rover to a garage if possible.
		VehicleMaintenance garage = null;
		try {
			BuildingManager.addToRandomBuilding(rover, startingSettlement);
			Building garageBuilding = BuildingManager.getBuilding(rover);
			garage = (VehicleMaintenance) garageBuilding.getFunction(GroundVehicleMaintenance.NAME);
		}
		catch (Exception e) {}
        
		// Have person exit rover if necessary.
		if (person.getLocationSituation().equals(Person.INVEHICLE)) {
			rover.getInventory().takeUnit(person, startingSettlement);
			try {
				if ((garage != null) && garage.getBuilding().hasFunction(LifeSupport.NAME)) {
					LifeSupport lifeSupport = (LifeSupport) garage.getBuilding().getFunction(LifeSupport.NAME);
					lifeSupport.addPerson(person);
				}
				else BuildingManager.addToRandomBuilding(person, startingSettlement);
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
	
	/**
	 * Checks to see if the crew have any dangerous medical problems that require 
	 * treatment at a settlement. 
	 * Also any environmental problems, such as suffocation.
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
				if (prob.getIllness().getSeriousness() >= 50) result = true;
			}
		}
		return result;
	}
	
	/**
	 * Checks if there is only one person at the home settlement and has a serious medical problem.
	 * @return true if serious medical problem
	 */
	private boolean hasDangerousMedicalProblemAtHome() {
		boolean result = false;
		if (startingSettlement.getCurrentPopulationNum() == 1) {
			PersonIterator i = startingSettlement.getInhabitants().iterator();
			while (i.hasNext()) {
				Person person = i.next();
				Iterator meds = person.getPhysicalCondition().getProblems().iterator();
				while (meds.hasNext()) {
					HealthProblem prob = (HealthProblem) meds.next();
					if (prob.getIllness().getSeriousness() >= 50) result = true; 
				}
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
	
	/** 
	 * Determine the locations of the sample collection sites.
	 * @param roverRange the rover's driving range
	 * @return List of collection sites (as Coordinates objects)
	 * @throws Exception of collection sites can not be determined.
	 */
	private List determineCollectionSites(double roverRange) throws Exception {

		List result = new ArrayList();
		List unorderedSites = new ArrayList();
        
		// Get the current location.
		Coordinates startingLocation = startingSettlement.getCoordinates();
        
		// Get Mars surface features.
		SurfaceFeatures surfaceFeatures = Simulation.instance().getMars().getSurfaceFeatures();
        
		// Determine the first collection site.
		Direction direction = new Direction(RandomUtil.getRandomDouble(2 * Math.PI));
		double limit = roverRange / 4D;
		double siteDistance = RandomUtil.getRandomDouble(limit);
		Coordinates newLocation = startingLocation.getNewLocation(direction, siteDistance);
		unorderedSites.add(newLocation);
		Coordinates currentLocation = newLocation;
        
		// Determine remaining collection sites.
		double remainingRange = (roverRange / 2D) - siteDistance;
		for (int x=1; x < numSites; x++) {
			double currentDistanceToSettlement = currentLocation.getDistance(startingLocation);
			if (remainingRange > currentDistanceToSettlement) {
				direction = new Direction(RandomUtil.getRandomDouble(2D * Math.PI));
				double tempLimit1 = Math.pow(remainingRange, 2D) - Math.pow(currentDistanceToSettlement, 2D);
				double tempLimit2 = (2D * remainingRange) - (2D * currentDistanceToSettlement * direction.getCosDirection());
				limit = tempLimit1 / tempLimit2;
				siteDistance = RandomUtil.getRandomDouble(limit);
				newLocation = currentLocation.getNewLocation(direction, siteDistance);
				unorderedSites.add(newLocation);
				currentLocation = newLocation;
				remainingRange -= siteDistance;
			}
		}

		// Reorder sites for shortest distance.
		currentLocation = startingLocation;
		while (unorderedSites.size() > 0) {
			Coordinates shortest = (Coordinates) unorderedSites.get(0);
			Iterator i = unorderedSites.iterator();
			while (i.hasNext()) {
				Coordinates site = (Coordinates) i.next();
				if (currentLocation.getDistance(site) < currentLocation.getDistance(shortest)) 
					shortest = site;
			}
			result.add(shortest);
			unorderedSites.remove(shortest);
			currentLocation = shortest;
		}
        
		return result;
	}
	
	/** Finalizes the mission */
	protected void endMission() {

		if (rover != null) rover.setReserved(false);
		else if (reserveRoverTask != null) reserveRoverTask.unreserveRover();

		super.endMission();
	}	
	
	/**
	 * Gets the reserved explorer rover.
	 *
	 * @param person the person to reserve rover if needed.
	 * @return reserved rover or null if still in the process of reserving one.
	 * @throws Exception if a rover could not be reserved.
	 */
	private Rover getReservedRover(Person person) throws Exception {
        
		Rover result = null;
        
		// If reserve rover task is currently underway, check status.
		if (reserveRoverTask != null) {
			if (reserveRoverTask.isDone()) {
				result = reserveRoverTask.getReservedRover();
				if (result == null) throw new Exception("Explorer rover could not be reserved.");
			}
		}
		else {
			reserveRoverTask = new ReserveRover(resourceType, siteResourceGoal, person);
			assignTask(person, reserveRoverTask);
		}
        
		return result;
	}
	
	/**
	 * Gets the mission's rover.
	 * 
	 * @return rover or null if none.
	 */
	public Rover getRover() {
		return rover;
	}
	
	/**
	 * Gets the home settlement for the mission. 
	 * @return home settlement or null if none.
	 */
	public Settlement getHomeSettlement() {
		return startingSettlement;	
	}
	
	/**
	 * Gets a collection of the vehicles associated with this mission.
	 * @return collection of vehicles.
	 */
	public VehicleCollection getMissionVehicles() {
		VehicleCollection result = new VehicleCollection();
		if (getRover() != null) result.add(getRover());
		return result;
	}
}