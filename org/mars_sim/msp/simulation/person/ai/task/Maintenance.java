/**
 * Mars Simulation Project
 * Maintenance.java
 * @version 2.76 2004-05-02
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai.task;

import java.io.Serializable;
import java.util.Iterator;
import org.mars_sim.msp.simulation.Mars;
import org.mars_sim.msp.simulation.RandomUtil;
import org.mars_sim.msp.simulation.malfunction.*;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.structure.building.*;
import org.mars_sim.msp.simulation.structure.building.function.LifeSupport;
import org.mars_sim.msp.simulation.vehicle.Vehicle;

/** The Maintenance class is a task for performing
 *  preventive maintenance on vehicles, settlements and equipment.
 */
public class Maintenance extends Task implements Serializable {

	// Static members
	private static final double STRESS_MODIFIER = .1D; // The stress modified per millisol.

    // Data members
    private Malfunctionable entity; // Entity to be maintained.
    private double duration; // Duration (in millisols) the person with perform this task.

    /** Constructs a Maintenance object
     *  @param person the person to perform the task
     *  @param mars the virtual Mars
     */
    public Maintenance(Person person, Mars mars) {
        super("Performing Maintenance", person, true, false, STRESS_MODIFIER, mars);

        // Randomly determine duration, from 0 - 500 millisols
        duration = RandomUtil.getRandomDouble(500D);

        entity = null;
	
        // Determine entity to maintain.
        double totalProbabilityWeight = 0D;

        // Total probabilities for all malfunctionable entities in person's local.
        Iterator i = MalfunctionFactory.getMalfunctionables(person).iterator();
        while (i.hasNext()) {
            Malfunctionable e = (Malfunctionable) i.next();
            if (!(e instanceof Vehicle)) {
                MalfunctionManager manager = e.getMalfunctionManager();
                double entityWeight = manager.getEffectiveTimeSinceLastMaintenance();
                totalProbabilityWeight += entityWeight;
            }
        }
	
        // Randomly determine a malfunctionable entity.
        double chance = RandomUtil.getRandomDouble(totalProbabilityWeight);

        // Get the malfunctionable entity chosen.
        i = MalfunctionFactory.getMalfunctionables(person).iterator();
        while (i.hasNext()) {
            Malfunctionable malfunctionable = (Malfunctionable) i.next();
            MalfunctionManager manager = malfunctionable.getMalfunctionManager();
            double entityWeight = manager.getEffectiveTimeSinceLastMaintenance();
            if ((chance < entityWeight) && !(malfunctionable instanceof Vehicle)) {
                entity = malfunctionable;
                description = "Performing maintenance on " + entity.getName();
                if (entity instanceof Building) {
                	Building building = (Building) entity;
                	if (building.hasFunction(LifeSupport.NAME)) {
                    	try { 
                    		LifeSupport lifeSupport = (LifeSupport) building.getFunction(LifeSupport.NAME);
                    		if (!lifeSupport.containsPerson(person)) {
                    			if (lifeSupport.getAvailableOccupancy() > 0) lifeSupport.addPerson(person);
                    			else endTask();
                    		}
                    	}
                    	catch (Exception e) {
                    		System.err.println("Maintenance.constructor: " + e.getMessage());
                    	}
                	}
                }
                // System.out.println(person.getName() + " " + description + " - " + lastMaint);
                break;
            }
            else chance -= entityWeight;
        }
	
        if (entity == null) endTask();
    }

    /** Returns the weighted probability that a person might perform this task.
     *  It should return a 0 if there is no chance to perform this task given the person and his/her situation.
     *  @param person the person to perform the task
     *  @param mars the virtual Mars
     *  @return the weighted probability that a person might perform this task
     */
    public static double getProbability(Person person, Mars mars) {
        double result = 0D;

        // Total probabilities for all malfunctionable entities in person's local.
        Iterator i = MalfunctionFactory.getMalfunctionables(person).iterator();
        while (i.hasNext()) {
            Malfunctionable entity = (Malfunctionable) i.next();
            MalfunctionManager manager = entity.getMalfunctionManager();
            if (!manager.hasMalfunction() && !(entity instanceof Vehicle)) {
                double entityProb = manager.getEffectiveTimeSinceLastMaintenance() / 1000D;
                if (entityProb > 50D) entityProb = 50D;
                result += entityProb;
            }   
        }
	
        // Effort-driven task modifier.
        result *= person.getPerformanceRating();
	
        return result;
    }

    /** 
     * This task simply waits until the set duration of the task is complete, then ends the task.
     * @param time the amount of time to perform this task (in millisols)
     * @return amount of time remaining after finishing with task (in millisols)
     * @throws Exception if error performing task.
     */
    double performTask(double time) throws Exception {
        double timeLeft = super.performTask(time);
        if (subTask != null) return timeLeft;

        MalfunctionManager manager = entity.getMalfunctionManager();
	
        // If person is incompacitated, end task.
        if (person.getPerformanceRating() == 0D) endTask();

        // Check if maintenance has already been completed.
        if (manager.getEffectiveTimeSinceLastMaintenance() == 0D) endTask();

        // If equipment has malfunction, end task.
        if (manager.hasMalfunction()) endTask();

        if (isDone()) return timeLeft;
	
        // Determine effective work time based on "Mechanic" skill.
        double workTime = timeLeft;
        int mechanicSkill = person.getSkillManager().getEffectiveSkillLevel("Mechanic");
        if (mechanicSkill == 0) workTime /= 2;
        if (mechanicSkill > 1) workTime += workTime * (.2D * mechanicSkill);

        // Add work to the maintenance
        manager.addMaintenanceWorkTime(workTime);

        // Add experience to "Mechanic" skill.
        // (1 base experience point per 100 millisols of time spent)
        // Experience points adjusted by person's "Experience Aptitude" attribute.
        double experience = timeLeft / 100D;
        NaturalAttributeManager nManager = person.getNaturalAttributeManager();
        experience += experience * (((double) nManager.getAttribute("Experience Aptitude") - 50D) / 100D);
        person.getSkillManager().addExperience("Mechanic", experience);

        // If maintenance is complete, task is done.
        if (manager.getEffectiveTimeSinceLastMaintenance() == 0D) endTask();

        // Keep track of the duration of the task.
        timeCompleted += time;
        if (timeCompleted >= duration) endTask();

        // Check if an accident happens during maintenance.
        checkForAccident(timeLeft);
	
        return 0D;
    }

    /**
     * Check for accident with entity during maintenance phase.
     * @param time the amount of time (in millisols)
     */
    private void checkForAccident(double time) {

        double chance = .001D;

        // Mechanic skill modification.
        int skill = person.getSkillManager().getEffectiveSkillLevel("Mechanic");
        if (skill <= 3) chance *= (4 - skill);
        else chance /= (skill - 2);

        if (RandomUtil.lessThanRandPercent(chance * time)) {
            // System.out.println(person.getName() + " has accident while performing maintenance on " + entity.getName() + ".");
            entity.getMalfunctionManager().accident();
        }
    }

    /** 
     * Gets the entity the person is maintaining.
     * Returns null if none.
     * @return entity
     */
    public Malfunctionable getEntity() {
        return entity;
    }
}
