/*
 * Mars Simulation Project
 * ReadMeta.java
 * @date 2022-07-16
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.fav.FavoriteType;
import org.mars_sim.msp.core.person.ai.task.Read;
import org.mars_sim.msp.core.person.ai.task.util.FactoryMetaTask;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskTrait;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * Meta task for the Read task.
 */
public class ReadMeta extends FactoryMetaTask {

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
        
        if (fatigue > 1000 || hunger > 750)
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
            
        	FavoriteType fav = person.getFavorite().getFavoriteActivity();
            // The 3 favorite activities drive the person to want to read
            if (fav == FavoriteType.RESEARCH) {
                result *= 2D;
            }
            else if (fav == FavoriteType.TINKERING) {
                result *= 0.8;
            }
            else if (fav == FavoriteType.LAB_EXPERIMENTATION) {
                result *= 1.2;
            }
          
            result -= fatigue/5;           
            
            double pref = person.getPreference().getPreferenceScore(this);
            
        	result += pref * 2.5;
        	
	        if (result < 0) result = 0;
	        
            if (pref > 0) {
            	
             	if (stress > 25D)
             		result *= 1.5;
             	else if (stress > 50D)
             		result *= 2D;
             	else if (stress > 75D)
             		result *= 3D;
            }
            
	        if (result < 0) result = 0;

        }

        return result;
    }
}
