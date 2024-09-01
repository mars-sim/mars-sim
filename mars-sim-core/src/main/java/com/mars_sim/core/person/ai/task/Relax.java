/*
 * Mars Simulation Project
 * Relax.java
 * @date 2022-08-10
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.task;

import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.PhysicalCondition;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.BuildingManager;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.tool.RandomUtil;
import com.mars_sim.core.vehicle.Rover;

/**
 * The Relax class is a simple task that implements resting and doing nothing for a while.
 * The duration of the task is by default chosen randomly, up to 100 millisols.
 */
public class Relax
extends Task {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.relax"); //$NON-NLS-1$

    /** Task phases. */
    private static final TaskPhase RELAXING = new TaskPhase(Msg.getString(
            "Task.phase.relaxing")); //$NON-NLS-1$

	// Static members
	/** The stress modified per millisol. */
	private static final double DURATION = 20D;
	
	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = -1D;

	/**
	 * Constructor.
	 * 
	 * @param person the person to perform the task
	 */
	public Relax(Person person) {
		super(NAME, person, false, false, STRESS_MODIFIER, DURATION);
		
		// If during person's work shift, only relax for short period.
        boolean isShiftHour = person.isOnDuty();
		if (isShiftHour) {
		    setDuration(RandomUtil.getRandomDouble(DURATION/6, DURATION/2));
		}
		
		// If person is in a settlement, try to find a place to relax.
		boolean walkSite = false;
		if (person.isInSettlement()) {
			Building rec = BuildingManager.getAvailableFunctionTypeBuilding(person, FunctionType.RECREATION);
			if (rec != null) {
				// Walk to recreation building.
				walkToTaskSpecificActivitySpotInBuilding(rec, FunctionType.RECREATION, true);
				walkSite = true;
			}
			else {
				// Go back to his bed
				if (person.hasBed()) {
					// Walk to the bed
					walkToBed(person, true);
				}
			}
		}

		if (!walkSite) {
            // If person is in rover, walk to passenger activity spot.
            if (person.getVehicle() instanceof Rover rover) {
                walkToPassengerActivitySpotInRover(rover, true);
            }
            else {
	            // Walk to random location.
	            walkToRandomLocation(true);
            }
		}

		// Initialize phase
		addPhase(RELAXING);
		setPhase(RELAXING);
	}

	@Override
	protected double performMappedPhase(double time) {
		if (getPhase() == null) {
			throw new IllegalArgumentException("Task phase is null");
		}
		else if (RELAXING.equals(getPhase())) {
			return relaxingPhase(time);
		}
		else {
			return time;
		}
	}

	/**
	 * Performs the relaxing phase of the task.
	 * 
	 * @param time the amount of time (millisol) to perform the phase.
	 * @return the amount of time (millisol) left after performing the phase.
	 */
	private double relaxingPhase(double time) {
	
		double remainingTime = 0;
		
		if (person != null) {
			
	        // Obtain the fractionOfRest to restore fatigue faster in high fatigue case.	   
			double fractionOfRest = time/1000;
		
			PhysicalCondition pc = person.getPhysicalCondition();
			double f =  pc.getFatigue();
			double perf = pc.getPerformanceFactor();		
			
	        // Reduce person's fatigue
	        pc.reduceFatigue(f * fractionOfRest);
	        
	        pc.relaxMuscle(time);
	        
	        if (perf < 1) {
	        	perf *= (1 + fractionOfRest);
	        	pc.setPerformanceFactor(perf);
	        }
		}
		
		return remainingTime;
	}
}
