/**
 * Mars Simulation Project
 * StudyRockSamples.java
 * @version 2.74 2002-04-23
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.structure.*;
import org.mars_sim.msp.simulation.vehicle.*;
import java.io.Serializable;
import java.util.*;

/** 
 * The StudyRockSamples class is a task for scientific research on 
 * collected rock samples. 
 */
public class StudyRockSamples extends Task implements Serializable {

    // Rate of rock sample research (kg / millisol)
    private static final double RESEARCH_RATE = .01D;
	
    // Data members
    private Inventory inv;   // The inventory containing the rock samples. 
    private Lab lab;         // The laboratory the person is working in.
    private double duration; // The duration (in millisols) the person will perform this task.

    /** 
     * Constructor for StudyRockSamples task.  
     * This is an effort driven task.
     * @param person the person to perform the task
     * @param mars the virtual Mars
     */
    public StudyRockSamples(Person person, Mars mars) {
        super("Studying Rock Samples", person, true, mars);

	if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
	    Settlement settlement = person.getSettlement();
            inv = settlement.getInventory();
            lab = (Laboratory) settlement.getFacilityManager().getFacility(Laboratory.NAME);
	    lab.addResearcher();
	}
	else if (person.getLocationSituation().equals(Person.INVEHICLE)) {
	    if (person.getVehicle() instanceof ExplorerRover) {
	        ExplorerRover rover = (ExplorerRover) person.getVehicle();
		inv = rover.getInventory();
		lab = rover.getLab();
		lab.addResearcher();
	    }
	}

	// Randomly determine duration from 0 - 500 millisols.
	duration = RandomUtil.getRandomDouble(500D);
    }

    /** Returns the weighted probability that a person might perform this task.
     *  @param person the person to perform the task
     *  @param mars the virtual Mars
     *  @return the weighted probability that a person might perform this task
     */
    public static double getProbability(Person person, Mars mars) {
        double result = 0D;

        if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
            Settlement settlement = person.getSettlement();
	    Inventory inv = settlement.getInventory();
	    Lab lab = (Laboratory) settlement.getFacilityManager().getFacility(Laboratory.NAME);
            if (inv.getResourceMass(Inventory.ROCK_SAMPLES) > 0D) {
		if (lab.getResearcherNum() < lab.getLaboratorySize()) result = 25D;
	    }
	}
	else if (person.getLocationSituation().equals(Person.INVEHICLE)) {
	    if (person.getVehicle() instanceof ExplorerRover) {
	        ExplorerRover rover = (ExplorerRover) person.getVehicle();
		Inventory inv = rover.getInventory();
		Lab lab = rover.getLab();
                if (inv.getResourceMass(Inventory.ROCK_SAMPLES) > 0D) {
		    if (lab.getResearcherNum() < lab.getLaboratorySize()) result = 25D;
	        }
	    }
	}
	    
	// Effort-driven task modifier.
	result *= person.getPerformanceRating();

        return result;
    }

    /** Performs the mechanic task for a given amount of time.
     *  @param time amount of time to perform the task (in millisols)
     *  @return amount of time remaining after finishing with task (in millisols)
     */
    double performTask(double time) {
        double timeLeft = super.performTask(time);
        if (subTask != null) return timeLeft;

        // If person is incompacitated, end task.
	if (person.getPerformanceRating() == 0D) {
	    done = true;
	    lab.removeResearcher();
	}
	
        // Determine effective research time based on "Areology" skill.
        double researchTime = timeLeft;
        int areologySkill = person.getSkillManager().getEffectiveSkillLevel("Areology");
        if (areologySkill == 0) researchTime /= 2D;
        if (areologySkill > 1) researchTime += researchTime * (.2D * areologySkill);

        // Remove studied rock samples.
	double studied = RESEARCH_RATE * researchTime;
	inv.removeResource(Inventory.ROCK_SAMPLES, studied);
	
        // Add experience to "Areology" skill.
        // (1 base experience point per 100 millisols of research time spent)
        // Experience points adjusted by person's "Academic Aptitude" attribute.
        double experience = timeLeft / 100D;
        experience += experience *
                (((double) person.getNaturalAttributeManager().getAttribute("Academic Aptitude") - 50D) / 100D);
        person.getSkillManager().addExperience("Areology", experience);

        // Keep track of the duration of this task.
	timeCompleted += time;
	if (timeCompleted >= duration) {
            done = true;
	    lab.removeResearcher();
	}

        // Check for lab accident.
	checkForAccident(time);
	
        return 0D;
    }

    /**
     * Check for accident in laboratory.
     * @param time the amount of time researching (in millisols)
     */
    private void checkForAccident(double time) {

        double chance = .001D;

	// Areology skill modification.
	int skill = person.getSkillManager().getEffectiveSkillLevel("Areology");
        if (skill <= 3) chance *= (4 - skill);
        else chance /= (skill - 2);

        if (RandomUtil.lessThanRandPercent(chance * time)) {
            System.out.println(person.getName() + " has a lab accident while researching rock samples.");
	    
            if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
                Settlement settlement = person.getSettlement();
	        Facility facility = settlement.getFacilityManager().getFacility(Laboratory.NAME);
		facility.getMalfunctionManager().accident();
	    }
	    else if (person.getLocationSituation().equals(Person.INVEHICLE)) {
	        person.getVehicle().getMalfunctionManager().accident(); 
	    }
        }
    }
}
