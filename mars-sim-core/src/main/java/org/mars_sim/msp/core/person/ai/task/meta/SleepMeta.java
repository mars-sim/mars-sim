/**
 * Mars Simulation Project
 * SleepMeta.java
 * @version 3.08 2015-06-15
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.io.Serializable;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ShiftType;
import org.mars_sim.msp.core.person.TaskSchedule;
import org.mars_sim.msp.core.person.ai.job.Astronomer;
import org.mars_sim.msp.core.person.ai.task.Sleep;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.time.MarsClock;

/**
 * Meta task for the Sleep task.
 */
public class SleepMeta implements MetaTask, Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    /** Task name */
    private static final String NAME = Msg.getString("Task.description.sleep"); //$NON-NLS-1$

    private static final int MAX_SUPPRESSION = 10;

	private MarsClock clock = Simulation.instance().getMasterClock().getMarsClock();

	//private int solCache = 0;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Task constructInstance(Person person) {
    	// the person will execute Sleep, increment numSleep;
    	PhysicalCondition pc = person.getPhysicalCondition();
    	pc.setNumSleep(pc.getNumSleep()+1);
    	pc.updateValueSleepHabit((int) clock.getMillisol(), true);
        return new Sleep(person);
    }

    @Override
    public double getProbability(Person person) {

        double result = 0;
    	boolean proceed = false;
    	double fatigue = 0;

    	PhysicalCondition pc = person.getPhysicalCondition();
/*
    	// 2015-12-05 check for the passing of each day
		int solElapsed = MarsClock.getSolOfYear(clock);
		if (solCache != solElapsed) {
			// 2015-12-05 reset numSleep back to zero at the beginning of each sol
			pc.setNumSleep(0);
			pc.setSuppressHabit(0);
			solCache = solElapsed;
		}
*/

    	int now = (int) clock.getMillisol();
  	  	boolean isOnShiftNow = person.getTaskSchedule().isShiftHour(now);

        // Fatigue modifier.
        fatigue = person.getFatigue();
        // 1000 millisols is 24 hours, if a person hasn't slept for 24 hours,
        // he is supposed to want to sleep right away.
    	if (fatigue > 1000D)
    		proceed = true;

    	int maxNumSleep = 0;
        boolean isOnCall = person.getTaskSchedule().getShiftType().equals(ShiftType.ON_CALL);
        if (isOnCall)
        	maxNumSleep = 7;
        else
        	maxNumSleep = 3;

        if (!proceed && pc.getNumSleep() <= maxNumSleep) {
        	// 2015-12-05 checks the current time against the sleep habit heat map
	    	int bestSleepTime[] = person.getBestKeySleepHabit();
	    	// check the two sleep time
	    	for (int time : bestSleepTime) {
		    	int diff = time - now;
		    	if (diff < 10 || diff > -10) {
		    		proceed = true;
		    		break;
		    	}
	    	}
        }


        if (!proceed) {
        	int stress = (int) person.getStress();
        	if (stress > 50D)
        		proceed = true;
        }

    	// each millisol generates 1 fatigue point
    	// 500 millisols is 12 hours
        if (proceed) {
        	// the desire to go to bed increase linearly after 12 hours of wake time
            result = (fatigue - 500D) / 3D;

            if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT
        		|| person.getLocationSituation() == LocationSituation.IN_VEHICLE) {

	            // Check if person is an astronomer.
	            boolean isAstronomer = (person.getMind().getJob() instanceof Astronomer);

	            // Dark outside modifier.
	            SurfaceFeatures surface = Simulation.instance().getMars().getSurfaceFeatures();
	            boolean isDark = (surface.getSolarIrradiance(person.getCoordinates()) == 0);
	            if (isDark && !isAstronomer) {
	                // Non-astronomers more likely to sleep when it's dark out.
	                result *= 2D;
	            }
	            else if (!isDark && isAstronomer) {
	                // Astronomers more likely to sleep when it's not dark out.
	                result *= 2D;
	            }

		        // if a person is NOT on-call
		        if (!isOnCall) {
			        // if a person is on shift right now
		           	if (isOnShiftNow){

		           		int habit = pc.getSuppressHabit();
		           		int spaceOut = pc.getSpaceOut();
			           	// limit adjustment to 10 times and space it out to at least 50 millisols apart
		           		if (spaceOut < now && habit < MAX_SUPPRESSION) {
			           		// Discourage the person from forming the sleep habit at this time
				  	  		person.updateValueSleepHabit(now, false);
				        	// shouldn't be zero since it's possible a person did not have enough sleep at other time and now fall asleep
					    	result = result / 5D;

					    	//System.out.println("spaceOut : " + spaceOut + "   now : " + now + "  suppressHabit : " + habit);

					    	pc.setSuppressHabit(habit+1);
					    	spaceOut = now + 20;
					    	if (spaceOut > 1000) {
					    		spaceOut = spaceOut - 1000;
					    	}
					    	pc.setSpaceOut(spaceOut);
		           		}
				    }

		           	else {
		           		int future = now;
		                // Check if person's work shift will begin within the next 50 millisols.
		           		future += 50;
			            if (future > 1000)
			            	future -= 1000;

			            boolean willBeShiftHour = person.getTaskSchedule().isShiftHour(future);
			            if (willBeShiftHour) {
			            	//if work shift is slated to begin in the next 50 millisols, probability of sleep reduces to one quarter of its value
			                result = result / 5D;
			            }
			            //else
			            	//result = result * 2D;
		           	}
			    }

	            // 2015-06-07 Added Preference modifier
		        if (result > 0)
		        	result += person.getPreference().getPreferenceScore(this);

		        // 2016-01-10 Added checking if a person has a designated bed
                Building quarters = person.getQuarters();    
                if (quarters == null) {
                	// he doesn't have his own bed and use shared/guest bed only, 
                	// he should be less inclined to fall asleep
                	result /= 1.5D;
                	
                	quarters = Sleep.getAvailableLivingQuartersBuilding(person, true);

		            if (quarters != null) {
		                result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, quarters);
		                result *= TaskProbabilityUtil.getRelationshipModifier(person, quarters);
		            }
                }
                
			    if (result < 0) 
			    	result = 0;
	        }
        }

        // No sleeping outside.
        //else if (person.getLocationSituation() == LocationSituation.OUTSIDE) {
        //   result = 0D;
        //}

        //System.out.println("sleep's result is " + result);
        return result;
    }

	@Override
	public Task constructInstance(Robot robot) {
        return new Sleep(robot);
	}

	@Override
	public double getProbability(Robot robot) {

        double result = 0D;

        // No sleeping outside.
        if (robot.getLocationSituation() == LocationSituation.OUTSIDE)
            result = 0D;


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
        if (robot.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
        	result += 2;


        	// TODO: stay at the work location
        	// TODO: go to that building to recharge if battery is low

            Building building = Sleep.getAvailableRoboticStationBuilding(robot);
            if (building != null) {
            	result += 2;

                //result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(robot, building);
                //result *= TaskProbabilityUtil.getRelationshipModifier(robot, building);
            }
        }


        return result;
	}
}