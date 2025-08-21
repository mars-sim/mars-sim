/*
 * Mars Simulation Project
 * Relax.java
 * @date 2025-08-18
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.task;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.PhysicalCondition;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
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
	
	private static final double TIME_FACTOR = 0.9; // NOTE: should vary this factor by person
	
	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = -1D;

	private static final FunctionType[] LOCATIONS_WIDE = {FunctionType.DINING,
			FunctionType.RECREATION,
			FunctionType.FARMING,
			FunctionType.FISHERY,
			FunctionType.EXERCISE, 
			FunctionType.LIVING_ACCOMMODATION};
	private static final FunctionType[] LOCATIONS_SMALL = {FunctionType.RECREATION,
			FunctionType.LIVING_ACCOMMODATION};
	private static final FunctionType[] LOCATIONS_EMPTY = {};

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
		
		if (person.isInSettlement()) {
			boolean walkDone = false;
			int rand = RandomUtil.getRandomInt(15);
			FunctionType [] locations = switch(rand) {
				case 0,1 -> LOCATIONS_SMALL;
				case 2,3,4,5 -> LOCATIONS_WIDE;
				default -> LOCATIONS_EMPTY;
				// Note: in future, save the preferred venue for each person
			};
			
			boolean anyZone = false;
			int zoneRand = RandomUtil.getRandomInt(50);
			if (zoneRand == 50) {
				// 98% same zone; 2% any zones (including other zones)
				anyZone = true;
			}
			
			// Choose a building in order
			for (var ft : locations) {
				Building b = BuildingManager.getAvailableFunctionBuilding(person, ft, anyZone);
				if (b != null) {
					walkDone = walkToActivitySpotInBuilding(b, ft, true);
					if (walkDone) {
						break;
					}
				}
			}
			
			// Note: if locations is LOCATIONS_EMPTY, 
			//       then stay at the same activity spot for this task
			
			if (!walkDone && locations.length != 0) {
				// Case: if no suitable building is found
			
				// Go back to his bed
				walkToBed(person, true);
				
				// Note: in future, save and use a preference location for each person
			}
		}

		else if (person.isInVehicle()) {
			// If person is in rover, walk to passenger activity spot.
			if (person.getVehicle() instanceof Rover rover) {
				walkToPassengerActivitySpotInRover(rover, true);
			}
		}

		// Initialize phase
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
			
			double fractionOfRest = time * TIME_FACTOR;
			
			PhysicalCondition pc = person.getPhysicalCondition();
			double perf = pc.getPerformanceFactor();		

	        // Reduce person's fatigue
	        pc.reduceFatigue(fractionOfRest);
	        
	        pc.reduceMuscleSoreness(time/2);
	        // Assume practicing relaxation techniques and cognitive rehearsal
	        // to increase pain tolerance
	        pc.increasePainTolerance(time);
	        
	        pc.reduceStress(time/2);   
	        
	        if (perf < 1) {
	        	perf *= (1 + fractionOfRest);
	        	pc.setPerformanceFactor(perf);
	        }
		}
		
		return remainingTime;
	}
}
