/*
 * Mars Simulation Project
 * ListenToMusicMeta.java
 * @date 2022-08-31
 * @author Manny Kung
 */
package com.mars_sim.core.person.ai.task.meta;

import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.PhysicalCondition;
import com.mars_sim.core.person.ai.task.ListenToMusic;
import com.mars_sim.core.person.ai.task.util.FactoryMetaTask;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskTrait;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.BuildingManager;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.tools.Msg;
import com.mars_sim.tools.util.RandomUtil;

/**
 * Meta task for the ListenToMusic task.
 */
public class ListenToMusicMeta extends FactoryMetaTask {

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.listenToMusic"); //$NON-NLS-1$

    private static final double CAP = 500D;
    
    /** Modifier if during person's work shift. */
    private static final double WORK_SHIFT_MODIFIER = .2D;

    public ListenToMusicMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.ANY_HOUR);
		setTrait(TaskTrait.RELAXATION);

	}

    @Override
    public Task constructInstance(Person person) {
        return new ListenToMusic(person);
    }

    @Override
    public double getProbability(Person person) {
        double result = 0D;

        if (person.isInside()) {
	        double pref = person.getPreference().getPreferenceScore(this);

	     	result = (RandomUtil.getRandomDouble(10) + pref) * .25;

	        // Probability affected by the person's stress and fatigue.
	        PhysicalCondition condition = person.getPhysicalCondition();
	        double stress = condition.getStress();

            if (pref > 0) {
            	result *= Math.max(1, stress/20);
            }
            
	        if (person.isInSettlement()) {
				// Check if a person has a designated bed
				Building recBuilding = BuildingManager.getAvailableFunctionTypeBuilding(person, FunctionType.RECREATION);
				result *= getBuildingModifier(recBuilding, person);

	            // Modify probability if during person's work shift.
	            if (person.isOnDuty()) {
	            	// Incur penalty if doing it on-duty
	                result *= WORK_SHIFT_MODIFIER;
	            }
	        }

	        else if (person.isInVehicle()) {
		        // Check if person is in a moving rover.
		        if (Vehicle.inMovingRover(person)) {
		        	result *= 2;
		        }
	        }
        }

        if (result > CAP)
        	result = CAP;
        
        if (result < 0) result = 0;

        return result;
    }
}
