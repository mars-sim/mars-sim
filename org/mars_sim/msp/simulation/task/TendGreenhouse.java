/**
 * Mars Simulation Project
 * TendGreenhouse.java
 * @version 2.72 2001-07-28
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.task;

import org.mars_sim.msp.simulation.*;

/** The TendGreenhouse class is a task for tending the greenhouse in a settlement.
 *  It has the phases, "Planting", "Tending" and "Harvesting".
 */
class TendGreenhouse extends Task {

    private GreenhouseFacility greenhouse; // The greenhouse the person is tending.
    private Settlement settlement; // The settlement the greenhouse is in.
    private double duration; // The duration (in millisols) the person will perform the task.

    public TendGreenhouse(Person person, VirtualMars mars) {
        // Use Task constructor
        super("Tending Greenhouse", person, mars);

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
        if (person.getLocationSituation().equals("In Settlement")) {
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
    double doTask(double time) {
        double timeLeft = super.doTask(time);
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
        if (timeCompleted >= duration) isDone = true;

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

