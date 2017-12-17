/**
 * Mars Simulation Project
 * MeetTogetherMeta.java
 * @version 3.1.0 2017-10-22
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.io.Serializable;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.RoleType;
import org.mars_sim.msp.core.person.ai.task.MeetTogether;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.robot.Robot;


/**
 * Meta task for MeetTogether task.
 */
public class MeetTogetherMeta implements MetaTask, Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.meetTogether"); //$NON-NLS-1$
     
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Task constructInstance(Person person) {
        return new MeetTogether(person);
    }

    @Override
    public double getProbability(Person person) {
    	
        double result = 0D;

        if (person.isInSettlement()) {

	        RoleType roleType = person.getRole().getType();
	
	        if (roleType.equals(RoleType.PRESIDENT)
	                	|| roleType.equals(RoleType.MAYOR)
	            		|| roleType.equals(RoleType.COMMANDER))
	        	result += 50D;
	
	        else if (roleType.equals(RoleType.CHIEF_OF_AGRICULTURE)
	            	|| roleType.equals(RoleType.CHIEF_OF_ENGINEERING)
	            	|| roleType.equals(RoleType.CHIEF_OF_LOGISTICS_N_OPERATIONS)
	            	|| roleType.equals(RoleType.CHIEF_OF_MISSION_PLANNING)
	            	|| roleType.equals(RoleType.CHIEF_OF_SAFETY_N_HEALTH)
	            	|| roleType.equals(RoleType.CHIEF_OF_SCIENCE)
	            	|| roleType.equals(RoleType.CHIEF_OF_SUPPLY_N_RESOURCES))
	        	result += 30D;
	 
	
	        // TODO: Probability affected by the person's stress and fatigue.
	
	        // Effort-driven task modifier.
	        result *= person.getPerformanceRating();
	
	    	int now = Simulation.instance().getMasterClock().getMarsClock().getMsol0();
	        boolean isOnShiftNow = person.getTaskSchedule().isShiftHour(now);
	        
	        if (isOnShiftNow)
	        	result = result*1.5D;
	        
	        if (result > 0)
	        	result = result + result * person.getPreference().getPreferenceScore(this)/5D;
	
	        if (result < 0) 
	        	result = 0;
        }
        
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