/**
 * Mars Simulation Project
 * TendGreenhouse.java
 * @version 2.75 2003-20-20
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai;

import java.io.Serializable;
import java.util.Iterator;
import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.structure.building.function.Farming;

/** The TendGreenhouse class is a task for tending the greenhouse in a settlement.
 *  It has the phases, "Planting", "Tending" and "Harvesting".
 *  This is an effort driven task.
 */
public class TendGreenhouse extends Task implements Serializable {

    // Data members
    private Farming greenhouse; // The greenhouse the person is tending.
    private Settlement settlement; // The settlement the greenhouse is in.
    private double duration; // The duration (in millisols) the person will perform the task.

    public TendGreenhouse(Person person, Mars mars) {
        // Use Task constructor
        super("Tending Greenhouse", person, true, mars);

        // Initialize data members
        description = "Tending Greenhouse at " + person.getSettlement().getName();
        this.settlement = person.getSettlement();
        Iterator i = settlement.getBuildingManager().getBuildings(Farming.class).iterator();
        while (i.hasNext()) {
            Farming farm = (Farming) i.next();
            if (farm.requiresWork()) greenhouse = farm;
        }

        // Randomly determine duration, from 0 - 500 millisols
        duration = RandomUtil.getRandomDouble(500D);
    }

    /** Returns the weighted probability that a person might perform this task.
     *  Returns a 25 probability if person is at a settlement.
     *  Returns a 0 if not.
     */
    public static double getProbability(Person person, Mars mars) {
        double result = 0D;
	    
        boolean workableFarm = false;
        if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
            Iterator i = person.getSettlement().getBuildingManager().getBuildings(Farming.class).iterator();
            while (i.hasNext()) {
                Farming farm = (Farming) i.next();
                if (farm.requiresWork()) workableFarm = true;
                // Add later
                // if (farm.getMalfunctionManager().hasMalfunction()) workableFarm = false;
            }
        }

        if (workableFarm) result = 25D;
        
        // Effort-driven task modifier.
        result *= person.getPerformanceRating();

        return result;
    }

    /** Performs the tending greenhouse task for a given amount of time.
     *  @param time amount of time to perform the task (in millisols)
     *  @return amount of time remaining after finishing with task (in millisols)
     */
    double performTask(double time) {
        double timeLeft = super.performTask(time);
        if (subTask != null) return timeLeft;

        // If person is incompacitated, end task.
        if (person.getPerformanceRating() == 0D) {
            done = true;
            return timeLeft;
        }

        // Add later
        // Check if greenhouse has malfunction.
        // if (greenhouse.getMalfunctionManager().hasMalfunction()) {
        //    done = true;
        //    return timeLeft;
        // }

        // Determine amount of effective work time based on "Greenhouse Farming" skill.
        double workTime = timeLeft;
        int greenhouseSkill = person.getSkillManager().getEffectiveSkillLevel("Greenhouse Farming");
        if (greenhouseSkill == 0) workTime /= 2;
        else workTime += workTime * (.2D * (double) greenhouseSkill);

        // Add this work to the greenhouse.
        greenhouse.addWork(workTime);

        // Keep track of the duration of the task.
        timeCompleted += time;
        if (timeCompleted >= duration) done = true;

        // Add experience to "Greenhouse Farming" skill
        // (1 base experience point per 100 millisols of work)
        // Experience points adjusted by person's "Experience Aptitude" attribute.
        double experience = timeLeft / 100D;
        double experienceAptitude = (double) person.getNaturalAttributeManager().getAttribute("Experience Aptitude");
        experience += experience * ((experienceAptitude - 50D) / 100D);
        person.getSkillManager().addExperience("Greenhouse Farming", experience);

        // Check for accident in greenhouse.
        checkForAccident(time);
	
        return 0D;
    }

    /**
     * Check for accident in greenhouse.
     * @param time the amount of time working (in millisols)
     */
    private void checkForAccident(double time) {

        double chance = .001D;

        // Greenhouse farming skill modification.
        int skill = person.getSkillManager().getEffectiveSkillLevel("Greenhouse Farming");
        if (skill <= 3) chance *= (4 - skill);
        else chance /= (skill - 2);

        if (RandomUtil.lessThanRandPercent(chance * time)) {
            // System.out.println(person.getName() + " has accident while tending the greenhouse.");
            // Add later
            // greenhouse.getMalfunctionManager().accident();
        }
    }
}
