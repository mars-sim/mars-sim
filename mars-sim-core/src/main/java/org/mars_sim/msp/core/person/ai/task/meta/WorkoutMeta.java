/**
 * Mars Simulation Project
 * WorkoutMeta.java
 * @version 3.1.0 2017-09-03
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.io.Serializable;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.FavoriteType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.task.Workout;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * Meta task for the Workout task.
 */
public class WorkoutMeta implements MetaTask, Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;
    
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.workout"); //$NON-NLS-1$

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Task constructInstance(Person person) {
        return new Workout(person);
    }

    @Override
    public double getProbability(Person person) {

        double result = 0D;
               
        if (person.isInside()) {

            // Probability affected by the person's stress and fatigue.
            PhysicalCondition condition = person.getPhysicalCondition();

            double stress = condition.getStress();
            double fatigue = condition.getFatigue();
            double kJ = condition.getEnergy();
            double hunger = condition.getHunger();
            double[] muscle = condition.getMusculoskeletal();

            if (kJ < 500 || fatigue > 1000 || hunger > 750)
            	return 0;
 
            result = stress - (muscle[2] - muscle[0])/5D - fatigue/100D ;
            if (result < 0) 
            	return 0;
            
            double pref = person.getPreference().getPreferenceScore(this);
            
         	result += pref * 5D;
            
            if (pref > 0) {
             	if (stress > 45D)
             		result*=1.5;
             	else if (stress > 65D)
             		result*=2D;
             	else if (stress > 85D)
             		result*=3D;
             	else
             		result*=4D;
            }

            if (result <= 0) result = 0;
            
            // Get an available gym.
            Building building = Workout.getAvailableGym(person);
            
            if (building != null) {
                result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, building);
                result *= TaskProbabilityUtil.getRelationshipModifier(person, building);
            } 

            else {
                // a person can still have workout on his own without a gym in MDP Phase 1-3
            	return 0;
            }
                 
            if (person.isInVehicle()) {	
    	        // Check if person is in a moving rover.
    	        if (Vehicle.inMovingRover(person)) {
    		        // the bonus inside a vehicle
    	        	result += 30;
    	        } 	       
    	        else
    	        	// the penalty inside a vehicle
    	        	result += -30;
            }
            
            // Modify if working out is the person's favorite activity.
            if (person.getFavorite().getFavoriteActivity() == FavoriteType.SPORT) {
                result += RandomUtil.getRandomInt(1, 20);
            }

            if (result < 0) result = 0;

        }
    
        return result;
    }

	@Override
	public Task constructInstance(Robot robot) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getProbability(Robot robot) {
		// TODO Auto-generated method stub
		return 0;
	}
}