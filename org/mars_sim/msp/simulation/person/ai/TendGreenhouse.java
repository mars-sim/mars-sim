/**
 * Mars Simulation Project
 * TendGreenhouse.java
 * @version 2.74 2002-01-13
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai;

import java.io.Serializable;
import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.structure.*;

/** The TendGreenhouse class is a task for tending the greenhouse in a settlement.
 *  It has the phases, "Planting", "Tending" and "Harvesting".
 *  This is an effort driven task.
 */
class TendGreenhouse extends Task implements Serializable {

    private GreenhouseFacility greenhouse; // The greenhouse the person is tending.
    private Settlement settlement; // The settlement the greenhouse is in.
    private double duration; // The duration (in millisols) the person will perform the task.

    public TendGreenhouse(Person person, VirtualMars mars) {
        // Use Task constructor
        super("Tending Greenhouse", person, true, mars);

        // Initialize data members
        this.settlement = person.getSettlement();
        this.greenhouse = (GreenhouseFacility) settlement.getFacilityManager().getFacility("Greenhouse");

        // Randomly determine duration, from 0 - 250 millisols
        duration = RandomUtil.getRandomDouble(250D);
    }

    /** Returns the weighted probability that a person might perform this task.
     *  Returns a 25 probability if person is at a settlement.
     *  Returns a 0 if not.
     */
    public static double getProbability(Person person, VirtualMars mars) {
        if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
            GreenhouseFacility greenhouse =
                   (GreenhouseFacility) person.getSettlement().getFacilityManager().getFacility("Greenhouse");
            if ((greenhouse.getPhase().equals("Growing")) &&
                    (greenhouse.getGrowingWork() >= greenhouse.getWorkLoad()))
                return 0D;
            else
                return 25D;
        } else
            return 0D;
    }

    /** Performs the tending greenhouse task for a given amount of time.
     *  @param time amount of time to perform the task (in millisols)
     *  @return amount of time remaining after finishing with task (in millisols)
     */
    double performTask(double time) {
        double timeLeft = super.performTask(time);
        if (subTask != null) return timeLeft;

        // Get the phase from the greenhouse's phase of operation.
        phase = greenhouse.getPhase();

        // Determine amount of effective work time based on "Greenhouse Farming" skill.
        double workTime = timeLeft;
        int greenhouseSkill = person.getSkillManager().getEffectiveSkillLevel("Greenhouse Farming");
        if (greenhouseSkill == 0) workTime /= 2;
        else workTime += workTime * (.2D * (double) greenhouseSkill);

        // Add this work to the greenhouse.
        greenhouse.addWorkToGrowthCycle(workTime);

        // Keep track of the duration of the task.
        timeCompleted += timeLeft;
        if (timeCompleted >= duration) done = true;

        // Add experience to "Greenhouse Farming" skill
        // (1 base experience point per 100 millisols of work)
        // Experience points adjusted by person's "Experience Aptitude" attribute.
        double experience = timeLeft / 100D;
        experience += experience *
                (((double) person.getNaturalAttributeManager().getAttribute("Experience Aptitude") -
                50D) / 100D);
        person.getSkillManager().addExperience("Greenhouse Farming", experience);

        return 0D;
    }
}

