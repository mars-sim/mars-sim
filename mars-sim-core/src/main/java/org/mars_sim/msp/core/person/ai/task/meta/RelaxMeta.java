/**
 * Mars Simulation Project
 * RelaxMeta.java
 * @version 3.1.0 2017-01-19
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.task.Relax;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.building.Building;

/**
 * Meta task for the Relax task.
 */
public class RelaxMeta implements MetaTask, Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.relax"); //$NON-NLS-1$

    /** Modifier if during person's work shift. */
    private static final double WORK_SHIFT_MODIFIER = .5D;

    /** default logger. */
    private static Logger logger = Logger.getLogger(RelaxMeta.class.getName());

    @Override
    public String getName() {
        return NAME;
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

        	result = 0.1D;
        	
            // Probability affected by the person's stress and fatigue.
            PhysicalCondition condition = person.getPhysicalCondition();
            double fatigue = condition.getFatigue();
            double stress = condition.getStress();
            double hunger = condition.getHunger();
              
            if (fatigue > 1000 || stress > 75 || hunger > 667)
            	return 0;
            else
            	result += fatigue / 1000 + stress / 100 + hunger / 1000;
            
            double pref = person.getPreference().getPreferenceScore(this);
            
          	result = result + result * pref/6D;
            if (result < 0) result = 0;
            
            try {
                Building recBuilding = Relax.getAvailableRecreationBuilding(person);
                if (recBuilding != null) {
                    result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, recBuilding);
                    result *= TaskProbabilityUtil.getRelationshipModifier(person, recBuilding);
                }
            }
            catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage());
            }
            

            // Modify probability if during person's work shift.
            int millisols = marsClock.getMillisolInt();
            boolean isShiftHour = person.getTaskSchedule().isShiftHour(millisols);
            if (isShiftHour) {
                result*= WORK_SHIFT_MODIFIER;
            }
            
            if (result < 0) result = 0;
        }

        return result;
    }

	@Override
	public Task constructInstance(Robot robot) {
    	return null;
	}

	@Override
	public double getProbability(Robot robot) {
        return 0;
	}
	
    public void destroy() {
    }
}
