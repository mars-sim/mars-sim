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
import org.mars_sim.msp.core.Simulation;

import org.mars_sim.msp.core.person.CircadianClock;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ShiftType;
import org.mars_sim.msp.core.person.TaskSchedule;
import org.mars_sim.msp.core.person.ai.job.Astronomer;
import org.mars_sim.msp.core.person.ai.task.Sleep;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;

import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.MasterClock;

/**
 * Meta task for the Sleep task.
 */
public class SleepMeta implements MetaTask, Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    /** default logger. */
    private static Logger logger = Logger.getLogger(SleepMeta.class.getName());

    private static String sourceName = logger.getName();
    
    /** Task name */
    private static final String NAME = Msg.getString("Task.description.sleep"); //$NON-NLS-1$

    private static final int MAX_SUPPRESSION = 10;

    private static Simulation sim = Simulation.instance();
	private static MasterClock masterClock;// = sim.getMasterClock();
	private static MarsClock marsClock;// = masterClock.getMarsClock();
	
	private TaskSchedule ts;// = person.getTaskSchedule();
	private PhysicalCondition pc;// = person.getPhysicalCondition();
	private CircadianClock circadian;// = person.getCircadianClock();
	
	//private int solCache = 0;

	public SleepMeta() {
		
        sourceName = sourceName.substring(sourceName.lastIndexOf(".") + 1, sourceName.length());

        masterClock = sim.getMasterClock();
        if (masterClock != null) { // to avoid NullPointerException during maven test
	        marsClock = masterClock.getMarsClock();
        }

		//if (marsClock == null)
		//	if (masterClock != null)
		//		marsClock = masterClock.getMarsClock();

	}

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

        double result = 0;

        LocationSituation ls = person.getLocationSituation();
        if (ls == LocationSituation.IN_SETTLEMENT || ls == LocationSituation.IN_VEHICLE) {

           	boolean proceed = false;

        	// each millisol generates 1 fatigue point
        	// 500 millisols is 12 hours

           	ts = person.getTaskSchedule();
        	pc = person.getPhysicalCondition();
        	circadian = person.getCircadianClock();

        	int now = (int) marsClock.getMillisol();
            boolean isOnCall = ts.getShiftType() == ShiftType.ON_CALL;
            
            double fatigue = pc.getFatigue();
        	double stress = pc.getStress();
        	double ghrelin = circadian.getSurplusGhrelin();
        	double leptin = circadian.getSurplusLeptin();
        	//str = person + " GS " + Math.round(ghrelin*10.0)/10.0  + " LS " + Math.round(leptin*10.0)/10.0  + " "; 
        			
        	// When we are sleep deprived. The 2 hormones (Leptin and Ghrelin) are "out of sync" and that is 
        	// when we eat more than we should without even realizing.
        	
        	// People who don't sleep enough end up with too much ghrelin in their system, so the body thinks 
        	// it's hungry and it needs more calories, and it stops burning those calories because it thinks 
        	// there's a shortage.
            
        	// 1000 millisols is 24 hours, if a person hasn't slept for 24 hours,
            // he is supposed to want to sleep right away.
        	if (fatigue > 500D || stress > 50D || ghrelin-leptin > 300) {
        		proceed = true;
        	}

        	else {

	        	int maxNumSleep = 0;

	        	if (isOnCall)
	            	maxNumSleep = 7;
	            else
	            	maxNumSleep = 3;

	            if (circadian.getNumSleep() <= maxNumSleep) {
	            	// 2015-12-05 checks the current time against the sleep habit heat map
	    	    	int bestSleepTime[] = person.getBestKeySleepCycle();
	    	    	// is now falling two of the best sleep time ?
	    	    	for (int time : bestSleepTime) {
	    		    	int diff = time - now;
	    		    	if (diff < 20 || diff > -20) {
	    		    		proceed = true;
	    		    		break;
	    		    	}
	    	    	}
	            }
        	}

            if (proceed) {
    	
	        	// the desire to go to bed increase linearly after 12 hours of wake time
	            result += (fatigue - 250) / 5D + stress * 5D + (ghrelin-leptin - 300)/10D;

            	result += refreshSleepHabit(person);
                         	
	    	    if (result < 0)
	    	    	return 0;
   	    	            
	            //str += Math.round(result*10.0)/10.0 + " -> ";
	            
	            // Check if person is an astronomer.
	            boolean isAstronomer = (person.getMind().getJob() instanceof Astronomer);

	            // Dark outside modifier.
	            boolean isDark = (sim.getMars().getSurfaceFeatures().getSolarIrradiance(person.getCoordinates()) == 0);
	            
	            if (isDark && !isAstronomer) {
	                // Non-astronomers more likely to sleep when it's dark out.
	                result *= 2D;
	            }
	            else if (!isDark && isAstronomer) {
	                // Astronomers more likely to sleep when it's not dark out.
	                result *= 2D;
	            }

	        	Building quarters = null;
	        	Settlement s1 = person.getSettlement();
	        	Settlement s2 = person.getAssociatedSettlement();

	        	// Note: !s1.equals(s2) is troublesome if s1 or s2 is null

				// check to see if a person is a trader or on a trading mission
				if (s1 != s2) {
	        	//if (person.getMind().getJob() instanceof Trader) {
	        		// yes he is a trader/guest
	            	logger.fine("SleepMeta : " + person + " is a trader or a guest of a trade mission and will need to "
	            			+ "use an unoccupied bed randomly if being too tired.");
	            	// Get a quarters that has an "unoccupied bed" (even if that bed has been designated to someone else)
	            	quarters = Sleep.getBestAvailableQuarters(person, false);

	                if (quarters != null) {
	                	result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, quarters);
		                result *= TaskProbabilityUtil.getRelationshipModifier(person, quarters);
	                }
	                else {
	                   	//logger.fine("SleepMeta : " + person + " couldn't find an empty bed at all. Falling asleep at any spot if being too tired.");
	                	// TODO: should allow him/her to go sleep in gym or medical station.
		            }
				}

				else {

			        // 2016-01-10 Added checking if a person has a designated bed
	                quarters = person.getQuarters();
	                if (quarters != null) {
		            	// if this person has already been assigned a quarter and a bed, not a shared/guest bed
	                	// he should be "more" inclined to fall asleep this way
	                	result *= 1.2D;
		                result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, quarters);
		                result *= TaskProbabilityUtil.getRelationshipModifier(person, quarters);
	                }
	                else {
		            	// if this person has never been assigned a quarter and a bed so far
		            	logger.fine("SleepMeta : " + person + " has never been designated a bed");

	       				quarters = Sleep.getBestAvailableQuarters(person, true);

			            if (quarters != null) {
		            		logger.fine("SleepMeta : " + person + " will be designated a bed in " 
		            				+ quarters.getNickName());
		                    // set it as his quarters
			                result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, quarters);
			                result *= TaskProbabilityUtil.getRelationshipModifier(person, quarters);
			            }
			            else {
		              		// There are no undesignated beds left in any quarters
		                	logger.fine("SleepMeta : " + person + " cannot find any empty, undesignated beds in any "
		                			+ "quarters. Will use an unoccupied bed randomly.");
		                	// Get a quarters that has an "unoccupied bed" (even if that bed has been designated to someone else)
		                	quarters = Sleep.getBestAvailableQuarters(person, false);
		                	if (quarters != null) {
	      		                result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, quarters);
	    		                result *= TaskProbabilityUtil.getRelationshipModifier(person, quarters);
		                	}
		                    else {
		                    	logger.fine("Sleep : " + person + " couldn't find an empty bed. Falling asleep at "
		                    			+ "right where he/she is.");
		                    	// TODO: should allow him/her to sleep in gym or anywhere.
	    	                	// he should be "less" inclined to fall asleep this way
		                    	result /= 1.2D;
		                    }
		                }
	                }
				}

	        	if (result > 0)
	        		result = result + result * person.getPreference().getPreferenceScore(this)/5D;


	    	    if (result < 0)
	    	    	result = 0;

            }
        }


        // No sleeping outside.
        //else if (person.getLocationSituation() == LocationSituation.OUTSIDE) {
        //   result = 0D;
        //}

        //if (result > 0)
        //	LogConsolidated.log(logger, Level.INFO, 2000, sourceName,
        //		str + Math.round(result*100.0)/100.0, null);
        
        return result;
    }

    /***
     * Refreshes a person's sleep habit based on his/her latest work shift 
     * @param person
     * @return
     */
    public double refreshSleepHabit(Person person) {
        double result = 0;

    	int now = (int) marsClock.getMillisol();
  	  	boolean isOnShiftNow = ts.isShiftHour(now);
        boolean isOnCall = ts.getShiftType() == ShiftType.ON_CALL;

        // if a person is NOT on-call
        if (!isOnCall) {
	        // if a person is on shift right now
           	if (isOnShiftNow){

           		int habit = circadian.getSuppressHabit();
           		int spaceOut = circadian.getSpaceOut();
	           	// limit adjustment to 10 times and space it out to at least 50 millisols apart
           		if (spaceOut < now && habit < MAX_SUPPRESSION) {
	           		// Discourage the person from forming the sleep habit at this time
		  	  		person.updateValueSleepCycle(now, false);
		        	// shouldn't be zero since it's possible a person did not have enough sleep at other time and now fall asleep
			    	result = result / 10D;

			    	//System.out.println("spaceOut : " + spaceOut + "   now : " + now + "  suppressHabit : " + habit);

			    	circadian.setSuppressHabit(habit+1);
			    	spaceOut = now + 20;
			    	if (spaceOut > 1000) {
			    		spaceOut = spaceOut - 1000;
			    	}
			    	circadian.setSpaceOut(spaceOut);
           		}
		    }

           	else {
           		int future = now;
                // Check if person's work shift will begin within the next 50 millisols.
           		future += 50;
	            if (future > 1000)
	            	future -= 1000;

	            boolean willBeShiftHour = ts.isShiftHour(future);
	            if (willBeShiftHour) {
	            	//if work shift is slated to begin in the next 50 millisols, probability of sleep reduces to one quarter of its value
	                result = result / 20D;
	            }
	            //else
	            	//result = result * 2D;
           	}
	    }
        else {
        	// if he's on-call
        	result = result * 1.1D;
        }
        
        return result;
    }
    
    
	@Override
	public Task constructInstance(Robot robot) {
        return new Sleep(robot);
	}

	@Override
	public double getProbability(Robot robot) {

        double result = 1D;

        // No sleeping outside.
        if (robot.getLocationSituation() == LocationSituation.OUTSIDE)
            result = 0;


        // TODO: in what case should a bot "relax" or slow down its pace?
        // result += robot.getPhysicalCondition().getStress();

        /*
        // Fatigue modifier.
        double fatigue = robot.getPhysicalCondition().getFatigue();
        if (fatigue > 500D) {
            result = (fatigue - 500D) / 4D;
        }
        */
        // Dark outside modifier.
        //SurfaceFeatures surface = Simulation.instance().getMars().getSurfaceFeatures();
        //if (surface.getSurfaceSunlight(robot.getCoordinates()) == 0) {
        //    result *= 2D;
        //}

        // Crowding modifier.
        else if (robot.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
        	result += 2D;


        	// TODO: stay at the work location
        	// TODO: go to that building to recharge if battery is low

            Building building = Sleep.getAvailableRoboticStationBuilding(robot);
            if (building != null) {
            	result += 2D;

                //result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(robot, building);
                //result *= TaskProbabilityUtil.getRelationshipModifier(robot, building);
            }
        }


        return result;
	}
}