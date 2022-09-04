/*
 * Mars Simulation Project
 * MeetTogetherMeta.java
 * @date 2022-09-02
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.JobType;
import org.mars_sim.msp.core.person.ai.role.RoleType;
import org.mars_sim.msp.core.person.ai.task.MeetTogether;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.Task;


/**
 * Meta task for MeetTogether task.
 */
public class MeetTogetherMeta extends MetaTask {

    /** default logger. */
//    private static final SimLogger logger = SimLogger.getLogger(MeetTogetherMeta.class.getName());
	
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.meetTogether"); //$NON-NLS-1$
    
	private static final int CAP = 1_000;
	
    public MeetTogetherMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.WORK_HOUR);
		
		setPreferredJob(JobType.POLITICIAN, JobType.REPORTER);
	}

    @Override
    public Task constructInstance(Person person) {
        return new MeetTogether(person);
    }

    @Override
    public double getProbability(Person person) {
    	
        double result = 0D;
        
        RoleType roleType = person.getRole().getType();
        
        if (person.isInSettlement() && roleType != null) {
	
	        if (roleType.isCouncil())
	        	result += 50D;
	
	        else if (roleType.isChief())
	        	result += 30D;
	 
	        // TODO: Probability affected by the person's stress and fatigue.
	
	    	int now = marsClock.getMillisolInt();
	        boolean isOnShiftNow = person.getTaskSchedule().isShiftHour(now);
	        
	        int size = person.getAssociatedSettlement().getIndoorPeopleCount();
	        result /= 2 * Math.sqrt(size/8.0);
	        
	        if (isOnShiftNow)
	        	result = result * 10;
	        
	        if (result > 0)
	        	result = result + result * person.getPreference().getPreferenceScore(this)/5D;
	
	        // Probability affected by the person's stress and fatigue.
	        double fatigue = person.getPhysicalCondition().getFatigue();
	        
	        result -= fatigue/50;
	         
	        if (result < 0) 
	        	result = 0;
	        
	        // Effort-driven task modifier.
	        result *= person.getPerformanceRating();
	       
        }
        
        if (result > CAP)
        	result = CAP;
        
        return result;
    }
}
