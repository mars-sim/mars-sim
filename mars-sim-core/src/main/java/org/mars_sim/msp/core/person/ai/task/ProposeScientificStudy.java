/**
 * Mars Simulation Project
 * ProposeScientificStudy.java
 * @version 3.1.0 2017-09-18
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * A task for proposing a new scientific study.
 */
public class ProposeScientificStudy extends Task implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(ProposeScientificStudy.class.getName());
	private static String loggerName = logger.getName();
	private static String sourceName = loggerName.substring(loggerName.lastIndexOf(".") + 1, loggerName.length());

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.proposeScientificStudy"); //$NON-NLS-1$

	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = .2D;

	/** Task phases. */
	private static final TaskPhase PROPOSAL_PHASE = new TaskPhase(Msg.getString("Task.phase.proposalPhase")); //$NON-NLS-1$

	/** The scientific study to propose. */
	private ScientificStudy study;

	/**
	 * Constructor.
	 * 
	 * @param person the person performing the task.
	 */
	public ProposeScientificStudy(Person person) {
		super(NAME, person, false, true, STRESS_MODIFIER, true, 10D + RandomUtil.getRandomDouble(50D));

		study = scientificStudyManager.getOngoingPrimaryStudy(person);
		if (study == null) {
			// Create new scientific study.
			Job job = person.getMind().getJob();
			ScienceType science = ScienceType.getJobScience(job);
			if (science != null) {
				SkillType skill = science.getSkill();
				int level = person.getSkillManager().getSkillLevel(skill);
				study = scientificStudyManager.createScientificStudy(person, science, level);
			} else {
				logger.severe(person.getName() + " is a " 
					+ job.getName(person.getGender()).toLowerCase() + "-- not a scientist");
				endTask();
			}
		}

		if (study != null) {
			setDescription(
					Msg.getString("Task.description.proposeScientificStudy.detail", study.getScience().getName())); // $NON-NLS-1$

			// If person is in a settlement, try to find a building.
			boolean walk = false;
			if (person.isInSettlement()) {
				Building b = getAvailableBuilding(study, person);
				if (b != null) {
					// Walk to administration building.
					walkToActivitySpotInBuilding(b, false);
					walk = true;
				}
			}

			if (!walk) {

				if (person.isInVehicle()) {
					// If person is in rover, walk to passenger activity spot.
					if (person.getVehicle() instanceof Rover) {
						walkToPassengerActivitySpotInRover((Rover) person.getVehicle(), false);
					}
				} else {
					// Walk to random location.
					walkToRandomLocation(true);
				}
			}
		} else {
			endTask();
		}

		// Initialize phase
		addPhase(PROPOSAL_PHASE);
		setPhase(PROPOSAL_PHASE);
	}

	/**
	 * Gets an available building that the person can use.
	 * 
	 * @param person the person
	 * @return available building or null if none.
	 */
	public static Building getAvailableBuilding(ScientificStudy study, Person person) {

		Building result = null;

		if (person.isInSettlement()) {
			List<Building> buildings = null;

			if (study != null ) {
				ScienceType science = study.getScience();
				
				if (science != null && science == ScienceType.ASTRONOMY)
					buildings = getBuildings(person, FunctionType.ASTRONOMICAL_OBSERVATIONS);
				else if (science == ScienceType.BOTANY)
					buildings = getBuildings(person, FunctionType.FARMING);
			}
			
			if (buildings == null || buildings.size() == 0) {
				buildings = getBuildings(person, FunctionType.RESEARCH);
			}
			if (buildings == null || buildings.size() == 0) {
				buildings = getBuildings(person, FunctionType.ADMINISTRATION);
			}
			if (buildings == null || buildings.size() == 0) {
				buildings = getBuildings(person, FunctionType.DINING);
			}
			if (buildings == null || buildings.size() == 0) {
				buildings = getBuildings(person, FunctionType.LIVING_ACCOMODATIONS);
			}
			if (buildings == null || buildings.size() > 0) {
				Map<Building, Double> possibleBuildings = BuildingManager.getBestRelationshipBuildings(person,
						buildings);
				result = RandomUtil.getWeightedRandomObject(possibleBuildings);
			}
		}

		return result;
	}

	public static List<Building> getBuildings(Person person, FunctionType functionType) {
		List<Building> buildings = person.getSettlement().getBuildingManager().getBuildings(functionType);
		buildings = BuildingManager.getNonMalfunctioningBuildings(buildings);
		return BuildingManager.getLeastCrowdedBuildings(buildings);
	}

	@Override
	public FunctionType getLivingFunction() {
		return FunctionType.RESEARCH;
	}

	/**
	 * Performs the writing study proposal phase.
	 * 
	 * @param time the amount of time (millisols) to perform the phase.
	 * @return the amount of time (millisols) left over after performing the phase.
	 */
	private double proposingPhase(double time) {

		if (!study.getPhase().equals(ScientificStudy.PROPOSAL_PHASE)) {
			endTask();
		}

		if (isDone()) {
			return time;
		}

		// Determine amount of effective work time based on science skill.
		double workTime = time;
		int scienceSkill = getEffectiveSkillLevel();
		if (scienceSkill == 0) {
			workTime /= 2;
		} else {
			workTime += workTime * (.2D * (double) scienceSkill);
		}

		study.addProposalWorkTime(workTime);

		checkDone();

		// Add experience
		addExperience(time);

		return 0D;
	}

	private void checkDone() {
		if (study.isProposalCompleted()) {
			LogConsolidated.log(Level.INFO, 0, sourceName,
					"[" + person.getLocationTag().getLocale() + "] " + person.getName()
							+ " just finished writing a study proposal in " + study.getScience().getName() + " in "
							+ person.getLocationTag().getImmediateLocation());

			// Update the person's preference
//   			person.updateBuildingPreference(getBuildingFunction());
			endTask();
		}
	}

	@Override
	protected void addExperience(double time) {
		// Add experience to relevant science skill
		// 1 base experience point per 25 millisols of proposal writing time.
		double newPoints = time / 25D;

		// Experience points adjusted by person's "Academic Aptitude" attribute.
		int academicAptitude = person.getNaturalAttributeManager().getAttribute(NaturalAttributeType.ACADEMIC_APTITUDE);
		newPoints += newPoints * ((double) academicAptitude - 50D) / 100D;
		newPoints *= getTeachingExperienceModifier();

		person.getSkillManager().addExperience(study.getScience().getSkill(), newPoints, time);
	}

	@Override
	public List<SkillType> getAssociatedSkills() {
		List<SkillType> skills = new ArrayList<SkillType>(1);
		if (study != null && study.getScience() != null)
			skills.add(study.getScience().getSkill());
		return skills;
	}

	@Override
	public int getEffectiveSkillLevel() {
		SkillManager manager = person.getSkillManager();
		return manager.getEffectiveSkillLevel(study.getScience().getSkill());
	}

	@Override
	protected double performMappedPhase(double time) {
		if (getPhase() == null) {
			throw new IllegalArgumentException("Task phase is null");
		} else if (PROPOSAL_PHASE.equals(getPhase())) {
			return proposingPhase(time);
		} else {
			return time;
		}
	}

//	/**
//	 * Reloads instances after loading from a saved sim
//	 * 
//	 * @param {{@link ScientificStudyManager}
//	 */
//	public static void initializeInstances(ScientificStudyManager s) {
//		scientificStudyManager = s;
//	}

	@Override
	public void destroy() {
		super.destroy();

		study = null;
	}
}