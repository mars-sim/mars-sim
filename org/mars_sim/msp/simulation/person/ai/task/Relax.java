/**
 * Mars Simulation Project
 * Relax.java
 * @version 2.76 2004-05-04
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

/** The Relax class is a simple task that implements resting and doing nothing for a while.
 *  The duration of the task is by default chosen randomly, up to 100 millisols.
 *
 *  Note: Mental stress may be added later, which this task could be used to reduce.
 */
class Relax extends Task implements Serializable {

	// Static members
	private static final double STRESS_MODIFIER = -.5D; // The stress modified per millisol.

    // Data members
    private double duration; // The predetermined duration of task in millisols

    /** Constructs a Relax object
     *  @param person the person to perform the task
     *  @param mars the virtual Mars
     */
    public Relax(Person person, Mars mars) {
        super("Relaxing", person, false, false, STRESS_MODIFIER, mars);

        // If person is in a settlement, try to find a place to relax.
        if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
            BuildingManager buildingManager = person.getSettlement().getBuildingManager();
        	List recreationBuildings = buildingManager.getBuildings(Recreation.NAME);
			int rand = RandomUtil.getRandomInt(recreationBuildings.size() - 1);
			
        	try {
        		Building building = (Building) recreationBuildings.get(rand);
        		LifeSupport lifeSupport = (LifeSupport) building.getFunction(LifeSupport.NAME);
        		if (!lifeSupport.containsPerson(person)) {
        			lifeSupport.addPerson(person);
        			setStressModifier(STRESS_MODIFIER * 2D);
        		}
        	}
        	catch (Exception e) {
        		System.err.println("Relax.constructor(): " + e.getMessage());
        		endTask();
        	}
        }
        
        duration = RandomUtil.getRandomInt(100);
    }

    /** Returns the weighted probability that a person might perform this task.
     *  It should return a 0 if there is no chance to perform this task given the person and his/her situation.
     *  @param person the person to perform the task
     *  @param mars the virtual Mars
     *  @return the weighted probability that a person might perform this task
     */
    public static double getProbability(Person person, Mars mars) {
        return 10D + person.getPhysicalCondition().getStress();
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

        timeCompleted += time;
        if (timeCompleted > duration) {
            endTask();
            return timeCompleted - duration;
        }
        else return 0;
    }
}