/**
 * Mars Simulation Project
 * CollectRockSamples.java
 * @version 2.74 2002-03-11
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.vehicle.*;
import org.mars_sim.msp.simulation.equipment.*;
import java.io.Serializable;

/** 
 * The CollectRockSamples class is a task for collecting rock and soil samples at a site.
 */
class CollectRockSamples extends Task implements Serializable {

    // Phase names
    private static final String EXIT_ROVER = "Exit Rover";
    private static final String COLLECT_ROCKS = "Collect Rock Samples";
    private static final String ENTER_ROVER = "Enter Rover";

    // Samples (kg) collected per millisol (base rate). 
    private static final double COLLECTION_RATE = .1D;
    
    // Data members
    private Rover rover;
    private double collectedSamples;
    private double requiredSamples;
    private double startingVehicleRockCargo;
	
    /** 
     * Constructs a CollectRockSamples object.
     *  @param person the person to perform the task
     *  @param mars the virtual Mars
     */
    public CollectRockSamples(Person person, Rover rover, Mars mars, 
		    double requiredSamples, double startingVehicleRockCargo) {
        super("Collecting rock samples", person, true, mars);

	this.rover = rover;
	this.requiredSamples = requiredSamples;
	this.startingVehicleRockCargo = startingVehicleRockCargo;
        this.collectedSamples = 0D;
	
        phase = EXIT_ROVER;
	
        // System.out.println(person.getName() + " has started collecting rock samples task.");
    }

    /** 
     * Performs this task for a given period of time
     *  @param time amount of time to perform task (in millisols)
     */
    double performTask(double time) {
        double timeLeft = super.performTask(time);
        if (subTask != null) return timeLeft;
	
	while ((timeLeft > 0D) && !done) {
            if (phase.equals(EXIT_ROVER)) timeLeft = exitRover(timeLeft);
	    else if (phase.equals(COLLECT_ROCKS)) timeLeft = collectRocks(timeLeft);
	    else if (phase.equals(ENTER_ROVER)) timeLeft = enterRover(timeLeft);
	}
	
        return timeLeft;
    }

    /**
     * Perform the exit rover phase of the task.
     * @param time the time to perform this phase (in millisols)
     * @return the time remaining after performing this phase (in millisols)
     */
    private double exitRover(double time) {

	if (person.getLocationSituation().equals(Person.INVEHICLE)) {
	    if (ExitRoverEVA.canExitRover(person, rover)) {
	        addSubTask(new ExitRoverEVA(person, mars, rover));
	        return 0D;
	    }
	    else {
		// System.out.println(person.getName() + " unable to exit " + rover.getName());
	        done = true;
		return time;
	    }
	}
	else {
	    // System.out.println(person.getName() + " collecting rock samples.");
	    phase = COLLECT_ROCKS;
	    return time;
        }
    }

    /**
     * Perform the collect rocks phase of the task.
     * @param time the time to perform this phase (in millisols)
     * @return the time remaining after performing this phase (in millisols)
     */
    private double collectRocks(double time) {

        // Check if there is reason to cut the collection phase short and return
	// to the rover.
	if (shouldEndCollectionPhase()) {
	    phase = ENTER_ROVER;
	    return time;
	}
	    
        double remainingPersonCapacity = person.getInventory()
	        .getResourceRemainingCapacity(Inventory.ROCK_SAMPLES);
	
        double currentSamplesCollected = rover.getInventory()
	        .getResourceMass(Inventory.ROCK_SAMPLES) - startingVehicleRockCargo; 

	double remainingSamplesNeeded = requiredSamples - currentSamplesCollected;

	double sampleLimit = remainingPersonCapacity;
	if (remainingSamplesNeeded < remainingPersonCapacity)
	    sampleLimit = remainingSamplesNeeded;

	double samplesCollected = time * COLLECTION_RATE;

        // Modify collection rate by "Areology" skill.
	int areologySkill = person.getSkillManager().getEffectiveSkillLevel("Areology");
	if (areologySkill == 0) samplesCollected /= 2D;
	if (areologySkill > 1) samplesCollected += samplesCollected * (.2D * areologySkill);

	// Add experience to "Areology" skill.
	// 1 base experience point per 10 millisols of collection time spent.
	// Experience points adjusted by person's "Experience Aptitude" attribute.
        double experience = time / 100D;
	experience += experience *
                (((double) person.getNaturalAttributeManager().getAttribute("Experience Aptitude") - 50D) / 100D);
        person.getSkillManager().addExperience("Areology", experience);
	
	// Collect rock samples.
	if (samplesCollected <= sampleLimit) {
	    person.getInventory().addResource(Inventory.ROCK_SAMPLES, samplesCollected);
	    return 0D;
	}
	else {
	    if (sampleLimit >= 0D) 
                person.getInventory().addResource(Inventory.ROCK_SAMPLES, sampleLimit);
	    phase = ENTER_ROVER;
	    return time - (sampleLimit / COLLECTION_RATE);
	}
    }

    /**
     * Perform the enter rover phase of the task.
     * @param time the time to perform this phase (in millisols)
     * @return the time remaining after performing this phase (in millisols)
     */
    private double enterRover(double time) {

        if (person.getLocationSituation().equals(Person.OUTSIDE)) {
	    addSubTask(new EnterRoverEVA(person, mars, rover));
	    return 0D;
	}
	else {
	    double rockSamples = person.getInventory().getResourceMass(Inventory.ROCK_SAMPLES);

	    // Load rock samples into rover.
	    if (rockSamples > 0D) {
	        rockSamples = person.getInventory().removeResource(Inventory.ROCK_SAMPLES, rockSamples);
	        // System.out.println(person.getName() + " unloading " + rockSamples + " kg. of rock samples into " + rover.getName());
	        rover.getInventory().addResource(Inventory.ROCK_SAMPLES, rockSamples);
	        return 0D;
	    }
	    else {
	        // System.out.println(person.getName() + " ending collect rock samples task.");
		done = true;
		return time;
	    }
	}
    }

    /**
     * Checks if a person can perform an CollectRockSamples task.
     * @param person the person to perform the task
     * @param rover the rover the person will EVA from
     * @param mars the virtual mars instance
     * @return true if person can perform the task.
     */
    public static boolean canCollectRockSamples(Person person, Rover rover, Mars mars) {
        boolean result = true;

	// Check if person can exit the rover.
	if (!ExitRoverEVA.canExitRover(person, rover)) result = false;

	// Check if it is night time outside.
	if (mars.getSurfaceFeatures().getSurfaceSunlight(rover.getCoordinates()) == 0) result = false;

	// Check if person's medical condition will not allow task.
        if (person.getPerformanceRating() < .5D) result = false;
	
        return result;
    }

    /**
     * Checks if situation requires that the collection phase should end prematurely
     * and the person should return to the rover.
     * @return true if collection phase should end
     */
    private boolean shouldEndCollectionPhase() {

	boolean result = false;
        
	// Check if it is night time. 
	if (mars.getSurfaceFeatures().getSurfaceSunlight(rover.getCoordinates()) == 0) {
	    // System.out.println(person.getName() + " should end collection phase: night time.");
	    result = true;
	}

        EVASuit suit = (EVASuit) person.getInventory().findUnit(EVASuit.class);
	if (suit == null) {
	    System.out.println("**************************************************************");
	    System.out.println(person.getName() + " doesn't have an EVA suit!");
	    System.out.println("**************************************************************");
	    return true;
	}
        Inventory suitInv = suit.getInventory();
	
	// Check if EVA suit is at 10% of its oxygen capacity.
	double oxygenCap = suitInv.getResourceCapacity(Inventory.OXYGEN);
	double oxygen = suitInv.getResourceMass(Inventory.OXYGEN);
	if (oxygen <= (oxygenCap * .1D)) {
	    // System.out.println(person.getName() + " should end collection phase: EVA suit oxygen level less than 10%");	
	    result = true;
	}

	// Check if EVA suit is at 10% of its water capacity.
	double waterCap = suitInv.getResourceCapacity(Inventory.WATER);
	double water = suitInv.getResourceMass(Inventory.WATER);
	if (water <= (waterCap * .1D)) {
	    // System.out.println(person.getName() + " should end collection phase: EVA suit water level less than 10%");	
            result = true;
	}

	// Check if life support system in suit is working properly.
	if (!suit.lifeSupportCheck()) {
	    // System.out.println(person.getName() + " should end collection phase: EVA suit failed life support check.");	
	    result = true;
	}

	// Check if person's medical condition is sufficient to continue phase.
        if (person.getPerformanceRating() < .5D) {
	    // System.out.println(person.getName() + " should end collection phase: medical problems.");	
	    result = true;
	}
	
	return result;
    }
}
