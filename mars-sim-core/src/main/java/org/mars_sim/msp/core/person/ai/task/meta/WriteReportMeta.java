/**
 * Mars Simulation Project
 * WriteReportMeta.java
 * @version 3.1.0 2018-06-09
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.io.Serializable;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.FavoriteType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.role.RoleType;
import org.mars_sim.msp.core.person.ai.task.WriteReport;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * Meta task for the WriteReport task.
 */
public class WriteReportMeta implements MetaTask, Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.writeReport"); //$NON-NLS-1$

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Task constructInstance(Person person) {
        return new WriteReport(person);
    }

    @Override
    public double getProbability(Person person) {

        double result = 0D;

    	// check if he has this meta task done
//    	if (!person.getPreference().isTaskDue(this)) {
    		
            if (person.isInside()) {
	        
                // Probability affected by the person's stress and fatigue.
                PhysicalCondition condition = person.getPhysicalCondition();
                double fatigue = condition.getFatigue();
                double stress = condition.getStress();
                double hunger = condition.getHunger();
                
                if (fatigue > 1000 || stress > 50 || hunger > 500)
                	return 0;
            
	            if (result > 0) {
	                RoleType roleType = person.getRole().getType();

	                if (roleType.equals(RoleType.PRESIDENT))
	                	result += 50D;
	                
	            	else if (roleType.equals(RoleType.MAYOR))
	                	result -= 40D;
	            			
	            	else if (roleType.equals(RoleType.COMMANDER))
	                    result += 30D;
	            	
	            	else if (roleType.equals(RoleType.SUB_COMMANDER))
	            		result += 20D;
	                
	                else if (roleType.equals(RoleType.CHIEF_OF_AGRICULTURE)
	                	|| roleType.equals(RoleType.CHIEF_OF_ENGINEERING)
	                	|| roleType.equals(RoleType.CHIEF_OF_LOGISTICS_N_OPERATIONS)
	                	|| roleType.equals(RoleType.CHIEF_OF_MISSION_PLANNING)
	                	|| roleType.equals(RoleType.CHIEF_OF_SAFETY_N_HEALTH)
	                	|| roleType.equals(RoleType.CHIEF_OF_SCIENCE)
	                	|| roleType.equals(RoleType.CHIEF_OF_SUPPLY_N_RESOURCES)){
	                
	                	result += 15D;
	                }
	                
		            else
		            	result += 10D;
	            }
	            
	            if (result <= 0) result = 0;
	            
	            // Get an available office space.
	            Building building = WriteReport.getAvailableOffice(person);

	            // Note: if an office space is not available such as in a vehicle, one can still write reports!
	            if (building != null) {
	                result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, building);
	                result *= TaskProbabilityUtil.getRelationshipModifier(person, building);
	            }

	            // Effort-driven task modifier.
	            result *= person.getPerformanceRating();

                // Modify if operation is the person's favorite activity.
                if (person.getFavorite().getFavoriteActivity() == FavoriteType.OPERATION) {
                    result += RandomUtil.getRandomInt(1, 20);
                }

	            
		        // 2015-06-07 Added Preference modifier
		        if (result > 0)
		         	result = result + result * person.getPreference().getPreferenceScore(this)/5D;

		        if (result < 0) result = 0;
        	}
//    	}
        
        //System.out.println("result : " + result);
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