/**
 * Mars Simulation Project
 * Sleep.java
 * @version 2.76 2004-05-02
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai.task;

import java.io.Serializable;
import java.util.*;
import org.mars_sim.msp.simulation.Mars;
import org.mars_sim.msp.simulation.RandomUtil;
import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.structure.building.*;
import org.mars_sim.msp.simulation.structure.building.function.*;

/** The Sleep class is a task for sleeping.
 *  The duration of the task is by default chosen randomly, between 250 - 350 millisols.
 *  Note: Sleeping reduces fatigue.
 */
class Sleep extends Task implements Serializable {

	// Static members
	private static final double STRESS_MODIFIER = -.3D; // The stress modified per millisol.

    // Data members
    private double duration; // The duration of task in millisols

    /** Constructs a Sleep object
     *  @param person the person to perform the task
     *  @param mars the virtual Mars
     */
    public Sleep(Person person, Mars mars) {
        super("Sleeping", person, false, false, STRESS_MODIFIER, mars);

        // If person is in a settlement, try to find a living accommodations building.
        if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
            BuildingManager buildingManager = person.getSettlement().getBuildingManager();
            Building sleepingBuilding = null;
        
            // Try to find an available living accommodations building.
            List accommodations = buildingManager.getBuildings(LivingAccommodations.NAME);
			int rand = RandomUtil.getRandomInt(accommodations.size() - 1);
			
			try {
				Building building = (Building) accommodations.get(rand);
				LifeSupport lifeSupport = (LifeSupport) building.getFunction(LifeSupport.NAME);
				if (!lifeSupport.containsPerson(person)) {
					if (lifeSupport.getAvailableOccupancy() > 0) lifeSupport.addPerson(person);
					else endTask();
				}
			}
			catch (Exception e) {
				System.err.println("Relax.constructor(): " + e.getMessage());
				endTask();
			}
        }
        
        duration = 250D + RandomUtil.getRandomInt(100);
    }

    /** Returns the weighted probability that a person might perform this task.
     *  Returns 25 if person's fatigue is over 750, more if fatigue is much higher.
     *  Returns an additional 50 if it is night time.
     *  @param person the person to perform the task
     *  @param mars the virtual Mars
     *  @return the weighted probability that a person might perform this task
     */
    public static double getProbability(Person person, Mars mars) {
        double result = 0D;

		double fatigue = person.getPhysicalCondition().getFatigue();

        if (fatigue > 750D) {
            result = 25D;
            if (mars.getSurfaceFeatures().getSurfaceSunlight(person.getCoordinates()) == 0)
                result += 50D;
        }
        
        // If fatigue is higher than 1000, add 1 to result for every 5 points over 1000.
        if (fatigue > 1000D) result += (fatigue - 1000D) / 5D;

        return result;
    }

    /** 
     * This task allows the person to sleep for the duration.
     * @param time the amount of time to perform this task (in millisols)
     * @return amount of time remaining after finishing with task (in millisols)
     * @throws Exception if error performing task.
     */
    double performTask(double time) throws Exception {
        double timeLeft = super.performTask(time);
        if (subTask != null) return timeLeft;

		double newFatigue = person.getPhysicalCondition().getFatigue() - (time * 10D);
		if (newFatigue < 0D) newFatigue = 0D;
        person.getPhysicalCondition().setFatigue(newFatigue);
        timeCompleted += time;
        if (timeCompleted > duration) {
            endTask();
            return timeCompleted - duration;
        }
        else return 0;
    }
}