/*
 * Mars Simulation Project
 * SleepMeta.java
 * @date 2025-08-18
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.task.meta;

import com.mars_sim.core.person.CircadianClock;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.PhysicalCondition;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.shift.ShiftSlot.WorkStatus;
import com.mars_sim.core.person.ai.task.Sleep;
import com.mars_sim.core.person.ai.task.util.FactoryMetaTask;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.tool.RandomUtil;

/**
 * Meta task for the Sleep task.
 */
public class SleepMeta extends FactoryMetaTask {

//	private static SimLogger logger = SimLogger.getLogger(SleepMeta.class.getName());

    /** Task name */
    private static final String NAME = Msg.getString("Task.description.sleep"); //$NON-NLS-1$
		
	private static final int CAP = 6_000;
    
	private static int MAX_NUM_SLEEP = 4;
	
    public SleepMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.ANY_HOUR);
	}
    
    @Override
    public Task constructInstance(Person person) {
    	return new Sleep(person);
    }

    @Override
    public double getProbability(Person person) {
  		
        double result = 0;
	
        // No sleeping outside.
    	// Should allow a person to walk back in to find a place to sleep
    	if (person.isOutside())
    		return 0;
    	
   		CircadianClock circadian = person.getCircadianClock();
   		PhysicalCondition pc = person.getPhysicalCondition();
 
       	boolean proceed = false;

    	// Note : each millisol generates 1 fatigue point
    	// 500 millisols is ~12 hours

        double fatigue = pc.getFatigue();
    	double stress = pc.getStress();
    	double ghrelinS = circadian.getSurplusGhrelin();
    	double leptinS = circadian.getSurplusLeptin();
    	double hunger = pc.getHunger();
    	double thirst = pc.getThirst();
    	double energy = pc.getEnergy();
    	
    	// When we are sleep deprived. The 2 hormones (Leptin and Ghrelin) are "out of sync" and that is 
    	// when we eat more than we should without even realizing.
    	
    	// People who don't sleep enough end up with too much ghrelin in their system, so the body thinks 
    	// it's hungry and it needs more calories, and it stops burning those calories because it thinks 
    	// there's a shortage.
    	
    	// 1000 millisols is 24 hours, if a person hasn't slept for 24 hours,
        // he is supposed to want to sleep right away.
    	// Note: sleep deprivation increases ghrelin levels, while at the same time 
    	// lowers leptin levels in the blood.
    	// Take a break from sleep if it's too hungry and too low energy
    	if (fatigue > PhysicalCondition.FATIGUE_MIN || energy < PhysicalCondition.ENERGY_THRESHOLD
    			&& (stress > 50 || ghrelinS > 0 || leptinS == 0)) {
    		
        	int rand = RandomUtil.getRandomInt(1);
            proceed = rand != 1 ;
    	}
    	
        if (proceed) {
			double ghrelin = circadian.getGhrelin();
			double leptin = circadian.getLeptin();
		    double sleepMillisols = circadian.getTodaySleepTime();
            double soreness = pc.getMuscleSoreness();
            
        	// the desire to go to bed increase linearly after 6 hours of wake time
            result += Math.max((fatigue - 250), 0) * 10 + stress * 10 
            		+ (ghrelin - leptin)
            		// High hunger/thirst makes it harder to fall asleep
            		// Therefore, limit the contribution to a max of 300
            		- Math.min(hunger/2, 300)
            		- Math.min(thirst/2, 300)
            		// Note: muscle condition affects the desire to sleep
            		+ soreness/2.5 
            		- sleepMillisols / 2;
        	
    	    if (result <= 0) {
    	    	return 0;
    	    }
    	    
            double pref = person.getPreference().getPreferenceScore(this);
            
         	result += result * pref/12D;                            	
	    	
            // Check if person is an astronomer.
            boolean isAstronomer = (person.getMind().getJob() == JobType.ASTRONOMER);

            // Dark outside modifier.
            boolean isDark = (surfaceFeatures.getSolarIrradiance(person.getCoordinates()) < 5);
            
            if (isDark && !isAstronomer) {
                // Non-astronomers more likely to sleep when it's dark out.
                result *= 2D;
            }
            else if (!isDark && isAstronomer) {
                // Astronomers more likely to sleep when it's not dark out.
                result *= 2D;
            }

			WorkStatus workStatus = person.getShiftSlot().getStatus();
            if (workStatus == WorkStatus.ON_CALL) {
            	// Note: For on-call personnel, there is no longer a definite sleep 
            	// pattern, recommend resting as much as possible to get ready 
            	// when duties call.
            	result = result * 100;
				MAX_NUM_SLEEP = 8;
			}
            else if (workStatus == WorkStatus.ON_DUTY) {
         	   // Reduce the probability of sleep
               result = result / 100D;
            }

            if (result < 5)
    	    	return 5;
            
        	int sol = getMarsTime().getMissionSol();
        	
        	// Skip the first sol since the sleep time pattern has not been established
            if (sol > 1 && circadian.getNumSleep() <= MAX_NUM_SLEEP) {
	        	int now = getMarsTime().getMillisolInt();
            	result = circadian.desireToSleep(person, result, now);
            }
            
    	    if (result < 0)
    	    	result = 0;
        }
 
        if (result > CAP)
        	result = CAP;

        return result;
    }
}
