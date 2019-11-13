/**
 * Mars Simulation Project
 * PlanMissionMeta.java
 * @version 3.1.0 2019-10-23
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.io.Serializable;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.FavoriteType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.task.PlanMission;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.Administration;

/**
 * The Meta task for the PlanMission task.
 */
public class PlanMissionMeta implements MetaTask, Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

	private static transient Logger logger = Logger.getLogger(PlanMissionMeta.class.getName());
	private static String loggerName = logger.getName();
	private static String sourceName = loggerName.substring(loggerName.lastIndexOf(".") + 1, loggerName.length());
	
    /** Task name */
    private static final String NAME = Msg.getString("Task.description.planMission"); //$NON-NLS-1$

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Task constructInstance(Person person) {
        return new PlanMission(person);
    }

    @Override
    public double getProbability(Person person) {

        double result = 0D;

        if (person.isInSettlement()) {

    		boolean canDo = person.getMind().canStartNewMission();
    		if (!canDo)
    			return 0;
    		
            // Probability affected by the person's stress and fatigue.
            PhysicalCondition condition = person.getPhysicalCondition();
            double fatigue = condition.getFatigue();
            double stress = condition.getStress();
            double hunger = condition.getHunger();
            
            if (fatigue > 1000 || stress > 75 || hunger > 750)
            	return 0;
            
            result = 100.0 * (1/(fatigue + 1) + 1/(stress + 1) + 1/(hunger + 1));

            if (result > 0) {
            	 
                // Get an available office space.
                Building building = Administration.getAvailableOffice(person);
                if (building != null) {
                    result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, building);
                    result *= TaskProbabilityUtil.getRelationshipModifier(person, building);
                }

                // Modify if operation is the person's favorite activity.
                if (person.getFavorite().getFavoriteActivity() == FavoriteType.OPERATION) {
                    result *= 1.5D;
                }

                if (result > 0)
                	result += result * person.getPreference().getPreferenceScore(this)/5D;

                // Effort-driven task modifier.
                result *= person.getPerformanceRating();
            }
            
            if (result < 0) {
                result = 0;
            }
        }

//      if (result > 0) 
//  			logger.info(person + " (" + person.getRole().getType() + ") was at PlanMissionMeta : " + Math.round(result*100.0)/100.0);

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