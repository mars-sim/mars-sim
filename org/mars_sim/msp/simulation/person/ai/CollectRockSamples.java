/**
 * Mars Simulation Project
 * CollectRockSamples.java
 * @version 2.74 2002-05-05
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
class CollectRockSamples extends EVAOperation implements Serializable {

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
        super("Collecting rock samples", person, mars);

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

        // Add experience to "EVA Operations" skill.
        // (1 base experience point per 100 millisols of time spent)
	// Experience points adjusted by person's "Experience Aptitude" attribute.
        double experience = timeLeft / 100D;
	NaturalAttributeManager nManager = person.getNaturalAttributeManager();
        experience += experience * (((double) nManager.getAttribute("Experience Aptitude") - 50D) / 100D);
        person.getSkillManager().addExperience("EVA Operations", experience);
	
        return timeLeft;
    }

    /**
     * Perform the exit rover phase of the task.
     * @param time the time to perform this phase (in millisols)
     * @return the time remaining after performing this phase (in millisols)
     */
    private double exitRover(double time) {
        time = exitAirlock(time, rover);
	if (exitedAirlock) phase = COLLECT_ROCKS;
	return time;
    }

    /**
     * Perform the collect rocks phase of the task.
     * @param time the time to perform this phase (in millisols)
     * @return the time remaining after performing this phase (in millisols)
     */
    private double collectRocks(double time) {

	// Check for an accident during the EVA operation.
        checkForAccident(time);
	    
        // Check if there is reason to cut the collection phase short and return
	// to the rover.
	if (shouldEndEVAOperation()) {
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

        time = enterAirlock(time, rover);

	if (!enteredAirlock) {
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
	return 0D;
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
}
