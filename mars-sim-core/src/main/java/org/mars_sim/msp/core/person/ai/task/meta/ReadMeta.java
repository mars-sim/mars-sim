/**
 * Mars Simulation Project
 * ReadMeta.java
 * @version 3.1.2 2020-09-02
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.FavoriteType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.task.Read;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskTrait;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * Meta task for the Read task.
 */
public class ReadMeta extends MetaTask {

    private static final double VALUE = 2.5D;
    
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.read"); //$NON-NLS-1$
    
    public ReadMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.NONWORK_HOUR);
		
		setTrait(TaskTrait.TEACHING);
	}
    
    @Override
    public Task constructInstance(Person person) {
        return new Read(person);
    }

    @Override
    public double getProbability(Person person) {

        double result = 0D;

        // Probability affected by the person's stress and fatigue.
        PhysicalCondition condition = person.getPhysicalCondition();
        double fatigue = condition.getFatigue();
        double stress = condition.getStress();
        double hunger = condition.getHunger();
        
        if (fatigue > 1000 || stress > 75 || hunger > 750)
        	return 0;
        
        if (person.isInside()) {
        	result += VALUE;

            if (person.isInVehicle()) {	
    	        // Check if person is in a moving rover.
    	        if (Vehicle.inMovingRover(person)) {
    		        // the penalty inside a vehicle
    	        	result += -20;
    	        } 	       
    	        else
    		        // the bonus inside a vehicle, 
    	        	// rather than having nothing to do if a person is not driving
    	        	result += 20;
            }
            
	        // Effort-driven task modifier.
	        //result *= person.getPerformanceRating();

        	FavoriteType fav = person.getFavorite().getFavoriteActivity();
            // The 3 favorite activities drive the person to want to read
            if (fav == FavoriteType.RESEARCH) {
                result *= 1.2D;
            }
            else if (fav == FavoriteType.TINKERING) {
                result *= 0.8D;
            }
            else if (fav == FavoriteType.LAB_EXPERIMENTATION) {
                result *= 0.9D;
            }
          
//         	if (fatigue > 750D)
//         		result/=1.5;
//         	else if (fatigue > 1500D)
//         		result/=2D;
//         	else if (fatigue > 2000D)
//         		result/=3D;
//         	else
//         		result/=4D;
         	
            result -= fatigue/5;           
            
            double pref = person.getPreference().getPreferenceScore(this);
            
        	result = pref * 2.5D;
	        if (result < 0) result = 0;
	        
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
            
	        if (result < 0) result = 0;

        }

        return result;
    }
}
