/**
 * Mars Simulation Project
 * RelaxMeta.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.task.meta;

import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.task.Relax;
import com.mars_sim.core.person.ai.task.util.FactoryMetaTask;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskTrait;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.tools.Msg;

/**
 * Meta task for the Relax task.
 */
public class RelaxMeta extends FactoryMetaTask{

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.relax"); //$NON-NLS-1$

    /** Modifier if during person's work shift. */
    private static final double WORK_SHIFT_MODIFIER = .25D;

    public RelaxMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.ANY_HOUR);
		setTrait(TaskTrait.RELAXATION);
	}
   
    @Override
    public Task constructInstance(Person person) {
    	return new Relax(person);
    }

    @Override
    public double getProbability(Person person) {
        double result = 0D;

        // Crowding modifier
        if (person.isInside()) {

        	result = 0.5D;
        	         
            double pref = person.getPreference().getPreferenceScore(this);
            
          	result = result + result * pref/6D;
            if (result < 0) result = 0;
            
            Building recBuilding = Relax.getAvailableRecreationBuilding(person);
            result *= getBuildingModifier(recBuilding, person);

            // Modify probability if during person's work shift.
            boolean isShiftHour = person.isOnDuty();
            if (isShiftHour) {
                result*= WORK_SHIFT_MODIFIER;
            }
            
            if (result < 0) result = 0;
        }

        return result;
    }
}
