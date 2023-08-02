/*
 * Mars Simulation Project
 * ConnectWithEarthMeta.java
 * @date 2022-07-25
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import org.mars.sim.tools.Msg;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.job.util.JobType;
import org.mars_sim.msp.core.person.ai.task.ConnectWithEarth;
import org.mars_sim.msp.core.person.ai.task.util.FactoryMetaTask;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskTrait;
import org.mars_sim.msp.core.structure.ShiftSlot.WorkStatus;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * Meta task for the ConnectWithEarth task.
 */
public class ConnectWithEarthMeta extends FactoryMetaTask {

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.connectWithEarth"); //$NON-NLS-1$

    /** Modifier if during person's work shift. */
    private static final double WORK_SHIFT_MODIFIER = .2D;

    public ConnectWithEarthMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.NONWORK_HOUR);
		
		setTrait(TaskTrait.PEOPLE);
		setPreferredJob(JobType.POLITICIAN, JobType.REPORTER);
	}

    @Override
    public Task constructInstance(Person person) {
        return new ConnectWithEarth(person);
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
         	result += pref * .1D;
         	
            if (pref > 0) {
            	result *= Math.max(1, stress/20);
            }

            result -= fatigue/100 + hunger/100;
   
	        if (result < 0) result = 0;
	        
	        if (person.isInSettlement()) {	
	            // Get an available office space.
	            Building building = BuildingManager.getAvailableCommBuilding(person);
	
	            if (building != null) {
	            	result += 5;
	            	// A comm facility has terminal equipment that provides communication access with Earth
	            	// It is necessary
	                result *= getBuildingModifier(building, person);
	            }
	        }
            
            else if (person.isInVehicle()) {	
    	        // Check if person is in a moving rover.
    	        if (Vehicle.inMovingRover(person)) {
    	        	result += 10;
    	        }
            }
        
            // Modify probability if during person's work shift.
            WorkStatus status = person.getShiftSlot().getStatus();
            if (status == WorkStatus.ON_DUTY) {
                result*= WORK_SHIFT_MODIFIER;
            }
            
	        if (result < 0) result = 0;

        }
            
        return result;
    }
}
