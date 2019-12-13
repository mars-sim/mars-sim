/**
 * Mars Simulation Project
 * SleepMeta.java
 * @version 3.1.0 2017-01-19
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.io.Serializable;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.CircadianClock;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ShiftType;
import org.mars_sim.msp.core.person.ai.job.Astronomer;
import org.mars_sim.msp.core.person.ai.task.Sleep;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * Meta task for the Sleep task.
 */
public class SleepMeta implements MetaTask, Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    /** default logger. */
    private static Logger logger = Logger.getLogger(SleepMeta.class.getName());
	private static String loggerName = logger.getName();
	private static String sourceName = loggerName.substring(loggerName.lastIndexOf(".") + 1, loggerName.length());

    /** Task name */
    private static final String NAME = Msg.getString("Task.description.sleep"); //$NON-NLS-1$

    private static final int MAX_SUPPRESSION = 100;

    private CircadianClock circadian;
    
    private PhysicalCondition pc;
		
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Task constructInstance(Person person) {
    	return new Sleep(person);
    }

    @Override
    public double getProbability(Person person) {

   		circadian = person.getCircadianClock();
   		pc = person.getPhysicalCondition();
   		
        double result = 0;
	
       	boolean proceed = false;

    	// Note : each millisol generates 1 fatigue point
    	// 500 millisols is ~12 hours

       // boolean isOnCall = ts.getShiftType() == ShiftType.ON_CALL;
        
        double fatigue = pc.getFatigue();
    	double stress = pc.getStress();
    	double ghrelin = circadian.getSurplusGhrelin();
    	double leptin = circadian.getSurplusLeptin();
    	double hunger = pc.getHunger();
    	double energy = pc.getEnergy();
    	
    	// When we are sleep deprived. The 2 hormones (Leptin and Ghrelin) are "out of sync" and that is 
    	// when we eat more than we should without even realizing.
    	
    	// People who don't sleep enough end up with too much ghrelin in their system, so the body thinks 
    	// it's hungry and it needs more calories, and it stops burning those calories because it thinks 
    	// there's a shortage.
    	
    	// 1000 millisols is 24 hours, if a person hasn't slept for 24 hours,
        // he is supposed to want to sleep right away.
    	if (fatigue > 333 || stress > 50 || ghrelin-leptin > 500) {
    		proceed = true;
    	}
    	
    	int rand = RandomUtil.getRandomInt(1);
    	if (rand == 1 && (hunger > 667 || energy < 1000))
    		proceed = false;
    	
        if (proceed) {
        	// the desire to go to bed increase linearly after 12 hours of wake time
            result = result + (fatigue - 333) * 10 + stress * 10 
            		+ (ghrelin-leptin - 500)/2.5D
            		- hunger/20;
            
            double pref = person.getPreference().getPreferenceScore(this);
            
         	result = result + result * pref/8D;                            	
        	
    	    if (result < 0) {
    	    	// Reduce the probability if it's not the right time to sleep
//    	    	refreshSleepHabit(person); 
    	    	return 0;
    	    }
    	    	
            // Check if person is an astronomer.
            boolean isAstronomer = (person.getMind().getJob() instanceof Astronomer);

            // Dark outside modifier.
            boolean isDark = (surface.getSolarIrradiance(person.getCoordinates()) == 0);
            
            if (isDark && !isAstronomer) {
                // Non-astronomers more likely to sleep when it's dark out.
                result *= 2D;
            }
            else if (!isDark && isAstronomer) {
                // Astronomers more likely to sleep when it's not dark out.
                result *= 2D;
            }

            boolean isOnShift = person.getTaskSchedule().isShiftHour(marsClock.getMillisolInt());
            if (isOnShift && person.getShiftType() != ShiftType.ON_CALL) {
         	   // Reduce the probability if it's not the right time to sleep
         	   refreshSleepHabit(person);
         	   // probability of sleep reduces to one fifth of its value
               result = result / 10D;
            }
            else
            	result = result * 10D;
            
        	Building quarters = null;
        	Settlement s1 = person.getSettlement();
        	Settlement s0 = person.getAssociatedSettlement();

        	// Note: !s1.equals(s2) is troublesome if s1 or s2 is null

			// check to see if a person is a trader or on a trading mission
			if (s1 != null && !s1.equals(s0)) {
        	//if (person.getMind().getJob() instanceof Trader) {
        		// yes he is a trader/guest
            	logger.fine("SleepMeta : " + person + " is a trader or a guest of a trade mission and will need to "
            			+ "use an unoccupied bed randomly if being too tired.");
            	// Get a quarters that has an "unoccupied bed" (even if that bed has been designated to someone else)
            	quarters = Sleep.getBestAvailableQuarters(person, false);

                if (quarters != null) {
            		result = modifyProbability(result, person, quarters);
                }
//	                else {
                   	//logger.fine("SleepMeta : " + person + " couldn't find an empty bed at all. Falling asleep at any spot if being too tired.");
                	// TODO: should allow him/her to go sleep in gym or medical station.
//		            }
			}

			else {
		        // Add checking if a person has a designated bed
                quarters = person.getQuarters();
                
                if (quarters != null) {
	            	// if this person has already been assigned a quarter and a bed, not a shared/guest bed
                	// he should be "more" inclined to fall asleep this way
                	result = 1.2D * modifyProbability(result, person, quarters);
                }
                else {
	            	// if this person has never been assigned a quarter and a bed so far
	            	logger.fine("SleepMeta : " + person + " has never been designated a bed");

       				quarters = Sleep.getBestAvailableQuarters(person, true);

		            if (quarters != null) {
	            		logger.fine("SleepMeta : " + person + " will be designated a bed in " 
	            				+ quarters.getNickName());
	                    // set it as his quarters
                		result = modifyProbability(result, person, quarters);
		            }
		            else {
	              		// There are no undesignated beds left in any quarters
	                	logger.info("SleepMeta : " + person + " cannot find any empty, undesignated beds in any "
	                			+ "quarters. Will use an unoccupied bed randomly.");
	                	// Get a quarters that has an "unoccupied bed" (even if that bed has been designated to someone else)
	                	quarters = Sleep.getBestAvailableQuarters(person, false);
	                	if (quarters != null) {
	                		result = modifyProbability(result, person, quarters);
	                	}
	                    else {
	                    	logger.info("Sleep : " + person + " couldn't find an empty bed. Falling asleep at "
	                    			+ "right where he/she is.");
	                    	// TODO: should allow him/her to sleep in gym or anywhere.
    	                	// he should be "less" inclined to fall asleep this way
	                    	result /= 1.2D;
	                    }
	                }
                }
			}

        	int maxNumSleep = 0;

        	if (person.getTaskSchedule().getShiftType() == ShiftType.ON_CALL)
            	maxNumSleep = 8;
            else
            	maxNumSleep = 4;

        	int sol = marsClock.getMissionSol();
        	
        	// Skip the first sol since the sleep time pattern has not been established
            if (sol != 1 && person.getCircadianClock().getNumSleep() <= maxNumSleep) {
            	result = modifiedBySleepHabit(person, result);
            }
            
    	    if (result < 0)
    	    	result = 0;

        }
        
        else {
        	// if process is false
        	
	        // Reduce the probability if it's not the right time to sleep
//	    	refreshSleepHabit(person);  
       }

       // No sleeping outside.
       if (person.isOutside()) {
    	   // Reduce the probability if it's not the right time to sleep
//    	   refreshSleepHabit(person);  
    	   result = 0D;
       }
       
//        if (result > 0)
//        	LogConsolidated.log(logger, Level.INFO, 0, sourceName,
//        		person + " " + Math.round(result*100.0)/100.0, null);
        
        return result;
    }
    
    public double modifyProbability(double value, Person person, Building quarters) {
    	return value * TaskProbabilityUtil.getCrowdingProbabilityModifier(person, quarters) 
    			* TaskProbabilityUtil.getRelationshipModifier(person, quarters);
    }
    
    
	@Override
	public Task constructInstance(Robot robot) {
		return new Sleep(robot); 	  	
	}

	@Override
	public double getProbability(Robot robot) {

        double result = 1D;

        // No sleeping outside.
        if (robot.isOutside())
            return 0;

        else if (robot.isInVehicle())
            return result;
        
        // Crowding modifier.
        else if (robot.isInSettlement()) {
        	result += 1D;
        	// TODO: stay at the work location
        	// TODO: go to that building to recharge if battery is low
            Building building = Sleep.getAvailableRoboticStationBuilding(robot);
            if (building != null) {
            	result += 1D;

            }
        }


        return result;
	}
	
	public double modifiedBySleepHabit(Person person, double result) {
        	// Checks the current time against the sleep habit heat map
	    	int bestSleepTime[] = person.getPreferredSleepHours();
	    	// is now falling two of the best sleep time ?
	    	for (int time : bestSleepTime) {
	        	int now = marsClock.getMillisolInt();
		    	int diff = time - now;
		    	if (diff < 30 || diff > -30) {
		    		result = result*5;
		    		return result;
		    	}
		    	else {
		    		// Reduce the probability by a factor of 10
		    		result = result/5;
		    		return result;
		    	}
	    	}
	    	return result;
	}
	
	/**
     * Refreshes a person's sleep habit based on his/her latest work shift 
     * 
     * @param person
     */
    public void refreshSleepHabit(Person person) {
    	int now = marsClock.getMillisolInt();

        // if a person is NOT on-call
        if (person.getTaskSchedule().getShiftType() != ShiftType.ON_CALL) {
	        // if a person is on shift right now
           	if (person.getTaskSchedule().isShiftHour(now)) {

           		int habit = circadian.getSuppressHabit();
           		int spaceOut = circadian.getSpaceOut();
	           	// limit adjustment to 10 times and space it out to at least 50 millisols apart
           		if (spaceOut < now && habit < MAX_SUPPRESSION) {
	           		// Discourage the person from forming the sleep habit at this time
		  	  		person.updateSleepCycle(now, false);
			    	//System.out.println("spaceOut : " + spaceOut + "   now : " + now + "  suppressHabit : " + habit);

		  	  		int rand = RandomUtil.getRandomInt(2);
		  	  		if (rand == 2) {
				    	circadian.setSuppressHabit(habit+1);
				    	spaceOut = now + 20;
				    	if (spaceOut > 1000) {
				    		spaceOut = spaceOut - 1000;
				    	}
				    	circadian.setSpaceOut(spaceOut);
		  	  		}
           		}
		    }

//           	else {
//           		int future = now;
//                // Check if person's work shift will begin within the next 50 millisols.
//           		future += 50;
//	            if (future > 1000)
//	            	future = future - 1000;
//
//	            boolean willBeShiftHour = person.getTaskSchedule().isShiftHour(future);
//	            if (willBeShiftHour) {
//	            	//if work shift is slated to begin in the next 50 millisols, probability of sleep reduces to one tenth of its value
//	                result = result / 10D;
//	            }
//	            //else
//	            	//result = result * 2D;
//           	}
	    }
        
//        else {
//        	// if he's on-call
//        	result = result * 1.1D;
//        }

    }
    
    public void destroy() {
    }
}