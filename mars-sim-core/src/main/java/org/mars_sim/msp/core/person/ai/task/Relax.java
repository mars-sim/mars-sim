/*
 * Mars Simulation Project
 * Relax.java
 * @date 2022-08-10
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars.sim.tools.Msg;
import org.mars.sim.tools.util.RandomUtil;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskPhase;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * The Relax class is a simple task that implements resting and doing nothing for a while.
 * The duration of the task is by default chosen randomly, up to 100 millisols.
 */
public class Relax
extends Task {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static final Logger logger = Logger.getLogger(Relax.class.getName());

	/** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.relax"); //$NON-NLS-1$

    /** Task phases. */
    private static final TaskPhase RELAXING = new TaskPhase(Msg.getString(
            "Task.phase.relaxing")); //$NON-NLS-1$

	// Static members
	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = -1D;

	/**
	 * Constructor.
	 * @param person the person to perform the task
	 */
	public Relax(Person person) {
		super(NAME, person, false, false, STRESS_MODIFIER, 10D);
		
		// If during person's work shift, only relax for short period.
        boolean isShiftHour = person.isOnDuty();
		if (isShiftHour) {
		    setDuration(10D);
		}
		
		// If person is in a settlement, try to find a place to relax.
		boolean walkSite = false;
		if (person.isInSettlement()) {
			try {
				Building rec = getAvailableRecreationBuilding(person);
				if (rec != null) {
					// Walk to recreation building.
				    walkToTaskSpecificActivitySpotInBuilding(rec, FunctionType.RECREATION, true);
				    walkSite = true;
				}
				else {
					// Go back to his quarters
					Building quarters = person.getQuarters();
					if (quarters != null) {
						walkToBed(quarters, person, true);
					}
				}
			}
			catch (Exception e) {
				logger.log(Level.SEVERE,"Relax.constructor(): " + e.getMessage());
				endTask();
			}
		}

		if (!walkSite) {
		    if (person.isInVehicle()) {
                // If person is in rover, walk to passenger activity spot.
                if (person.getVehicle() instanceof Rover) {
                    walkToPassengerActivitySpotInRover((Rover) person.getVehicle(), true);
                }
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
					
	        // Reduce person's fatigue
	        pc.reduceFatigue(f * fractionOfRest);
	        
	        pc.relaxMuscle(time);
		}
		
		return remainingTime;
	}


	/**
	 * Gets an available recreation building that the person can use.
	 * Returns null if no recreation building is currently available.
	 * @param person the person
	 * @return available recreation building
	 */
	public static Building getAvailableRecreationBuilding(Person person) {

		Building result = null;

		if (person.isInSettlement()) {
			BuildingManager manager = person.getSettlement().getBuildingManager();
			Set<Building> recreationBuildings = manager.getBuildingSet(FunctionType.RECREATION);
			recreationBuildings = BuildingManager.getNonMalfunctioningBuildings(recreationBuildings);
			recreationBuildings = BuildingManager.getLeastCrowdedBuildings(recreationBuildings);

			if (recreationBuildings.size() > 0) {
				Map<Building, Double> recreationBuildingProbs = BuildingManager.getBestRelationshipBuildings(
						person, recreationBuildings);
				result = RandomUtil.getWeightedRandomObject(recreationBuildingProbs);
			}
		}

		return result;
	}
}
