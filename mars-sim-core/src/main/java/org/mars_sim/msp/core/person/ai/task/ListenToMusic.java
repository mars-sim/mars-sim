/**
 * Mars Simulation Project
 * ListenToMusic.java
 * @version 3.08 2015-11-02
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.BuildingFunction;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * This task lowers the stress and fatigue.
 * The duration of the task is by default chosen randomly, up to 100 millisols.
 */
public class ListenToMusic
extends Task
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(ListenToMusic.class.getName());

	/** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.listenToMusic"); //$NON-NLS-1$

    /** Task phases. */
    private static final TaskPhase LISTENING_TO_MUSIC = new TaskPhase(Msg.getString(
            "Task.phase.listeningToMusic")); //$NON-NLS-1$

    private static final TaskPhase FINDING_A_SONG = new TaskPhase(Msg.getString(
            "Task.phase.findingASong")); //$NON-NLS-1$

	// Static members
	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = -.3D;

	/**
	 * Constructor.
	 * @param person the person to perform the task
	 */
	public ListenToMusic(Person person) {
		super(NAME, person, false, false, STRESS_MODIFIER, true, 10D +
				RandomUtil.getRandomDouble(40D));

		// If during person's work shift, only relax for short period.
		int millisols = (int) Simulation.instance().getMasterClock().getMarsClock().getMillisol();
        boolean isShiftHour = person.getTaskSchedule().isShiftHour(millisols);
		if (isShiftHour) {
		    setDuration(5D);
		}

		// If person is in a settlement, try to find a place to relax.
		boolean walkSite = false;
		if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {

			try {

				Building recBuilding = getAvailableRecreationBuilding(person);
				if (recBuilding != null) {
					// Walk to recreation building.
					// 2016-01-10 Added BuildingFunction.RECREATION
				    walkToActivitySpotInBuilding(recBuilding, BuildingFunction.RECREATION, true);
				    walkSite = true;
				} else {
                	// 2016-01-10 if rec building is not available, go to a gym
                	Building gym = Workout.getAvailableGym(person);
                	if (gym != null) {
	                	walkToActivitySpotInBuilding(gym, BuildingFunction.EXERCISE, true);
	                	walkSite = true;
	                } else {
						// 2016-01-10 if gym is not available, go back to his quarters
		                Building quarters = person.getQuarters();    
		                if (quarters != null) {
		                	walkToActivitySpotInBuilding(quarters, BuildingFunction.LIVING_ACCOMODATIONS, true);
						    walkSite = true;
		                }
	                }
				}
			}
			catch (Exception e) {
				logger.log(Level.SEVERE,"ListenToMusic's constructor(): " + e.getMessage());
				endTask();
			}
		}

		if (!walkSite) {
		    if (person.getLocationSituation() == LocationSituation.IN_VEHICLE) {
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
		addPhase(FINDING_A_SONG);
		addPhase(LISTENING_TO_MUSIC);

		setPhase(FINDING_A_SONG);

	}

	public ListenToMusic(Robot robot) {
		super(NAME, robot, false, false, STRESS_MODIFIER, true, 10D +
				RandomUtil.getRandomDouble(20D));
	}

    @Override
    protected BuildingFunction getRelatedBuildingFunction() {
        return BuildingFunction.LIVING_ACCOMODATIONS;//.RECREATION;
    }

    //protected BuildingFunction getRelatedBuildingRoboticFunction() {
    //    return BuildingFunction.ROBOTIC_STATION;
    //}

	@Override
	protected double performMappedPhase(double time) {
		if (getPhase() == null) {
			throw new IllegalArgumentException("ListenToMusic. Task phase is null");
		}
		else if (FINDING_A_SONG.equals(getPhase())) {
			return findingPhase(time);
		}
		else if (LISTENING_TO_MUSIC.equals(getPhase())) {
			return listeningPhase(time);
		}
		else {
			return time;
		}
	}

	/**
	 * Performs the listening phase of the task.
	 * @param time the amount of time (millisol) to perform the phase.
	 * @return the amount of time (millisol) left after performing the phase.
	 */
	private double listeningPhase(double time) {
        // Reduce person's fatigue
        double newFatigue = person.getPhysicalCondition().getFatigue() - (2D * time);
        if (newFatigue < 0D) {
            newFatigue = 0D;
        }
        person.getPhysicalCondition().setFatigue(newFatigue);

		return 0D;
	}

	/**
	 * Performs the finding phase of the task.
	 * @param time the amount of time (millisol) to perform the phase.
	 * @return the amount of time (millisol) left after performing the phase.
	 */
	private double findingPhase(double time) {
		// TODO: add codes for selecting a particular type of music		
		setPhase(LISTENING_TO_MUSIC);
		return time*.9D;
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

		if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
			BuildingManager manager = person.getSettlement().getBuildingManager();
			List<Building> recreationBuildings = manager.getBuildings(BuildingFunction.RECREATION);
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