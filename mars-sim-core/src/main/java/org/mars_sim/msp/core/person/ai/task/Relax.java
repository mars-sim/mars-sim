/**
 * Mars Simulation Project
 * Relax.java
 * @version 3.1.0 2017-09-13
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * The Relax class is a simple task that implements resting and doing nothing for a while.
 * The duration of the task is by default chosen randomly, up to 100 millisols.
 */
public class Relax
extends Task
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(Relax.class.getName());

	/** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.relax"); //$NON-NLS-1$

    /** Task phases. */
    private static final TaskPhase RELAXING = new TaskPhase(Msg.getString(
            "Task.phase.relaxing")); //$NON-NLS-1$

	// Static members
	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = -.45D;

	/**
	 * Constructor.
	 * @param person the person to perform the task
	 */
	public Relax(Person person) {
		super(NAME, person, false, false, STRESS_MODIFIER, true, 10D);
		
		// If during person's work shift, only relax for short period.
		int msols = marsClock.getMillisolInt();
        boolean isShiftHour = person.getTaskSchedule().isShiftHour(msols);
		if (isShiftHour) {
		    setDuration(10D);
		}
		
		// If person is in a settlement, try to find a place to relax.
		boolean walkSite = false;
		if (person.isInSettlement()) {
			try {
				Building recBuilding = getAvailableRecreationBuilding(person);
				if (recBuilding != null) {
					// Walk to recreation building.
				    walkToActivitySpotInBuilding(recBuilding, true);
				    walkSite = true;
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

	public Relax(Robot robot) {
		super(NAME, robot, false, false, STRESS_MODIFIER, true, 10D);// + RandomUtil.getRandomDouble(10D));
		
		// If robot is in a settlement, try to find a place to relax.
//		boolean walkSite = false;
//
//		if (robot.isInSettlement()) {
//			try {
//				Building recBuilding = Sleep.getAvailableRoboticStationBuilding(robot);
//				if (recBuilding != null) {
//					// Walk to recreation building.
//				    walkToActivitySpotInBuilding(recBuilding, true);
//				    walkSite = true;
//				}
//			}
//			catch (Exception e) {
//				logger.log(Level.SEVERE,"Relax.constructor(): " + e.getMessage());
//				endTask();
//			}
//		}
//		
//		if (!walkSite) {
//		    if (robot.isInVehicle()) {
//                // If robot is in rover, walk to passenger activity spot.
//                if (robot.getVehicle() instanceof Rover) {
//                    walkToPassengerActivitySpotInRover((Rover) robot.getVehicle(), true);
//                }
//            }
//		    else {
//                // Walk to random location.
//                walkToRandomLocation(true);
//            }
//		}
//
//		// Initialize phase
//		addPhase(RELAXING);
//		setPhase(RELAXING);

	}

    @Override
    public FunctionType getLivingFunction() {
        return FunctionType.RECREATION;
    }

    public FunctionType getRoboticFunction() {
        return FunctionType.ROBOTIC_STATION;
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
		
		if (person != null) {
			
	        // Obtain the fractionOfRest to restore fatigue faster in high fatigue case.	   
			double fractionOfRest = time/1000;
		
			double f = person.getFatigue();
					
	        // Reduce person's fatigue
	        double newFatigue = f - f * fractionOfRest;

	        if (newFatigue < 0D) {
	            newFatigue = 0D;
	        }
	        person.getPhysicalCondition().setFatigue(newFatigue);

		}
		
		return 0D;
	}

	@Override
	protected void addExperience(double time) {
		// This task adds no experience.
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
			List<Building> recreationBuildings = manager.getBuildings(FunctionType.RECREATION);
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

	@Override
	public int getEffectiveSkillLevel() {
		return 0;
	}

	@Override
	public List<SkillType> getAssociatedSkills() {
		List<SkillType> results = new ArrayList<SkillType>(0);
		return results;
	}
	
}