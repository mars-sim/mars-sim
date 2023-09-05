/*
 * Mars Simulation Project
 * ConnectOnlineMeta.java
 * @date 2023-08-31
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import org.mars.sim.tools.Msg;
import org.mars.sim.tools.util.RandomUtil;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.job.util.JobType;
import org.mars_sim.msp.core.person.ai.task.ConnectOnline;
import org.mars_sim.msp.core.person.ai.task.util.FactoryMetaTask;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskTrait;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * Meta task for the ConnectOnline task.
 */
public class ConnectOnlineMeta extends FactoryMetaTask {

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.connectOnline"); //$NON-NLS-1$

    /** Modifier if during person's work shift. */
    private static final double WORK_SHIFT_MODIFIER = .2D;

    public ConnectOnlineMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.ANY_HOUR);
		
		setTrait(TaskTrait.PEOPLE);
		setPreferredJob(JobType.POLITICIAN, JobType.REPORTER);
	}

    @Override
    public Task constructInstance(Person person) {
        return new ConnectOnline(person);
    }

    @Override
    public double getProbability(Person person) {

        double result = 0D;
 
        if (person.isInside()) {
        		
            // Probability affected by the person's stress and fatigue.
            PhysicalCondition condition = person.getPhysicalCondition();
            double fatigue = condition.getFatigue();
            double stress = condition.getStress();
            double hunger = condition.getHunger();
            
            if (fatigue > 1500 || hunger > 1500)
            	return 0;
             
            double pref = person.getPreference().getPreferenceScore(this);
            
            // Use preference modifier
         	result = (RandomUtil.getRandomDouble(10) + pref) * .5;
         	
            if (pref > 0) {
            	result *= Math.max(1, stress/20);
            }

            result -= fatigue/100 + hunger/100;
	        
	        if (person.isInSettlement()) {	
	            // Get an available office space.
	            Building building = BuildingManager.getAvailableFunctionTypeBuilding(person, FunctionType.COMMUNICATION);
	
	            if (building != null) {
	            	// A comm facility has terminal equipment that provides communication access with Earth
	            	// It is necessary
	                result *= getBuildingModifier(building, person);
	            }
	            
	            // Modify probability if during person's work shift.
		        if (person.isOnDuty()) {
	            	// Incur penalty if doing it on-duty
	                result *= WORK_SHIFT_MODIFIER;
	            }  
	        }
            
            else if (person.isInVehicle()) {	
    	        // Check if person is in a moving rover.
    	        if (Vehicle.inMovingRover(person)) {
    	        	result *= 1.5;
    	        }
            }
                
	        if (result < 0) result = 0;
        }
            
        return result;
    }
}
