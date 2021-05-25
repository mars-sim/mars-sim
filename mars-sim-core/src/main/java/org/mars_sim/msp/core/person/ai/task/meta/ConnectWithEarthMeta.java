/**
 * Mars Simulation Project
 * ConnectWithEarthMeta.java
 * @version 3.1.2 2020-09-02
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ShiftType;
import org.mars_sim.msp.core.person.ai.task.ConnectWithEarth;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskTrait;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * Meta task for the ConnectWithEarth task.
 */
public class ConnectWithEarthMeta extends MetaTask {

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.connectWithEarth"); //$NON-NLS-1$

    /** Modifier if during person's work shift. */
    private static final double WORK_SHIFT_MODIFIER = .2D;

    public ConnectWithEarthMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.NONWORK_HOUR);
		
		addTrait(TaskTrait.PEOPLE);
	}

    @Override
    public Task constructInstance(Person person) {
        return new ConnectWithEarth(person);
    }

    @Override
    public double getProbability(Person person) {

        double result = 0D;

        // Check if a person has done this once today
//      if (person.getPreference().isTaskDue(this))
//      	return 0;	
        
        if (person.isInside()) {
        		
            // Probability affected by the person's stress and fatigue.
            PhysicalCondition condition = person.getPhysicalCondition();
            double fatigue = condition.getFatigue();
            double stress = condition.getStress();
            double hunger = condition.getHunger();
            
            if (fatigue > 1000 || hunger > 500)
            	return 0;
            
            result -= fatigue/15;
            
            double pref = person.getPreference().getPreferenceScore(this);
            
            // Use preference modifier
         	result += pref * .1D;
         	
            if (pref > 0) {
             	if (stress < 25)
             		result*=1.5;
             	else if (stress < 50)
             		result*=2D;
             	else if (stress > 75)
             		result*=3D;
             	else if (stress < 90)
             		result*=4D;
            }

            // Get an available office space.
            Building building = BuildingManager.getAvailableCommBuilding(person);

            if (building != null) {
            	result += 5;
            	// A comm facility has terminal equipment that provides communication access with Earth
            	// It is necessary
                result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, building);
                result *= TaskProbabilityUtil.getRelationshipModifier(person, building);
            }
            
            else if (person.isInVehicle()) {	
    	        // Check if person is in a moving rover.
    	        if (Vehicle.inMovingRover(person)) {
    	        	result += 20D;
    	        }
            }
        
            // Modify probability if during person's work shift.
            int millisols = marsClock.getMillisolInt();
            boolean isShiftHour = person.getTaskSchedule().isShiftHour(millisols);
            if (isShiftHour && person.getShiftType() != ShiftType.ON_CALL) {
                result*= WORK_SHIFT_MODIFIER;
            }
            
	        if (result < 0) result = 0;

        }
            
        return result;
    }
}
