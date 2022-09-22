/**
 * Mars Simulation Project
 * PlanMissionMeta.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.role.RoleType;
import org.mars_sim.msp.core.person.ai.task.PlanMission;
import org.mars_sim.msp.core.person.ai.task.util.MetaTask;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.Administration;

/**
 * The Meta task for the PlanMission task.
 */
public class PlanMissionMeta extends MetaTask {

    /** Task name */
    private static final String NAME = Msg.getString("Task.description.planMission"); //$NON-NLS-1$

    private static final int START_FACTOR = 100;
    
    public PlanMissionMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.WORK_HOUR);
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
            
            // This has been reduced
            result = START_FACTOR * (1/(fatigue + 1) + 1/(stress + 1) + 1/(hunger + 1));

            if (result > 0) {
            	 
            	RoleType roleType = person.getRole().getType();
            	
            	if (RoleType.MISSION_SPECIALIST == roleType)
            		result *= 1.125;
            	else if (RoleType.CHIEF_OF_MISSION_PLANNING == roleType)
            		result *= 2.25;
            	else if (RoleType.SUB_COMMANDER == roleType)
            		result *= 3.375;
            	else if (RoleType.COMMANDER == roleType)
            		result *= 4.5;
            	
                // Get an available office space.
                Building building = Administration.getAvailableOffice(person);
                if (building != null) {
                    result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, building);
                    result *= TaskProbabilityUtil.getRelationshipModifier(person, building);
                }

                result = applyPersonModifier(result, person);
            }
            
            if (result < 0) {
                result = 0;
            }
        }

//      if (result > 0) logger.info(person + " (" + person.getRole().getType() + ") was at PlanMissionMeta : " + Math.round(result*100.0)/100.0)

        return result;
    }
}
