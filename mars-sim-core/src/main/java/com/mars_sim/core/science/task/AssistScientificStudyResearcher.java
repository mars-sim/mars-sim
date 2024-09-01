/*
 * Mars Simulation Project
 * AssistScientificStudyResearcher.java
 * @date 2023-08-11
 * @author Scott Davis
 */
package com.mars_sim.core.science.task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.social.RelationshipUtil;
import com.mars_sim.core.person.ai.task.util.ExperienceImpact;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.BuildingManager;
import com.mars_sim.core.structure.building.function.LifeSupport;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.tool.RandomUtil;
import com.mars_sim.core.vehicle.Crewable;
import com.mars_sim.core.vehicle.Rover;

/**
 * Task for assisting a scientific study researcher.
 */
public class AssistScientificStudyResearcher extends Task {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(AssistScientificStudyResearcher.class.getName());

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.assistScientificStudyResearcher"); //$NON-NLS-1$

	/** Task phases. */
	private static final TaskPhase ASSISTING = new TaskPhase(Msg.getString("Task.phase.assisting")); //$NON-NLS-1$

	/**
	 * The improvement in relationship opinion of the assistant from the researcher
	 * per millisol.
	 */
	private static final double BASE_RELATIONSHIP_MODIFIER = .2D;
	
	private ResearchScientificStudy researchTask;

	    
	/**
	 * Create a new Study anda Task to build the proposal for a Person
	 * @param p
	 * @return
	 */
	static AssistScientificStudyResearcher createTask(Person p) {
		var researcher = determineResearcher(p);
		if (researcher == null) {		
			logger.severe(p, "Cannot find researcher");
            return null;
		}

		// Found a suitable study
		var study = (ResearchScientificStudy) researcher.getMind().getTaskManager().getTask();
		if (study == null) {
			logger.severe(researcher, "Cannot find study");
            return null;
		}
		var impact = new ExperienceImpact(50D, NaturalAttributeType.ACADEMIC_APTITUDE,
		false, 0.2D, study.getResearchScience().getSkill());

		return new AssistScientificStudyResearcher(p, study, impact);
	}

	/**
	 * Constructor.
	 *
	 * @param person the person performing the task.
	 */
	private AssistScientificStudyResearcher(Person person, ResearchScientificStudy study, ExperienceImpact impact) {
		// Use Task constructor. Skill determined later based on study
		super(NAME, person, false, impact, -1);
		
		// Determine researcher
		this.researchTask = study;
		researchTask.setResearchAssistant(person);
		setDescription(
				Msg.getString("Task.description.assistScientificStudyResearcher.detail", study.getResearcher().getName())); // $NON-NLS-1$

		// If in settlement, move assistant to building researcher is in.
		if (person.isInSettlement()) {

			Building researcherBuilding = BuildingManager.getAvailableBuilding(study.getResearchScience(), person);
			if (researcherBuilding != null) {
				// Walk to researcher
				walkToResearchSpotInBuilding(researcherBuilding, false);
			}
		} else if (person.isInVehicle()) {
			// If person is in rover, walk to passenger activity spot.
			if (person.getVehicle() instanceof Rover) {
				walkToPassengerActivitySpotInRover((Rover) person.getVehicle(), false);
			}
		} else {
			// Walk to random location.
			walkToRandomLocation(true);
		}

		// Initialize phase
		setPhase(ASSISTING);
	}

	/**
	 * What is being assisted
	 * @return
	 */
	public ResearchScientificStudy getAssisted() {
		return researchTask;
	}

	/**
	 * Determines a researcher to assist.
	 *
	 * @return researcher or null if none found.
	 */
	private static Person determineResearcher(Person p) {
		return RandomUtil.getRandomElement(getBestResearchers(p));
	}

	/**
	 * Gets a list of the most preferred researchers to assist.
	 *
	 * @return collection of preferred researchers, empty of none available.
	 */
	public static List<Person> getBestResearchers(Person assistant) {
		// Get all available researchers.
		List<Person> researchers = getAvailableResearchers(assistant);

		// If assistant is in a settlement, best researchers are in least crowded
		// buildings.
		List<Person> leastCrowded = null;

		if (assistant.isInSettlement()) {
			// Find the least crowded buildings that researchers are in.
			int crowding = Integer.MAX_VALUE;
			Set<Building> empty = new HashSet<>();
			for(Person researcher : researchers) {
				Building building = BuildingManager.getBuilding(researcher);
				if (building != null) {
					LifeSupport lifeSupport = building.getLifeSupport();
					int buildingCrowding = lifeSupport.getOccupantNumber() - lifeSupport.getOccupantCapacity() + 1;
					buildingCrowding = Math.max(buildingCrowding, -1);
					if (buildingCrowding < crowding) {
						crowding = buildingCrowding;
						empty = new HashSet<>();
						empty.add(building);
					}
					else if (buildingCrowding == crowding) {
						empty.add(building);
					}
				}
			}

			// Add researchers in least crowded buildings to result.
			final Set<Building> target = empty;
			leastCrowded = researchers.stream()
					.filter(r -> target.contains(BuildingManager.getBuilding(r)))
					.toList();
		}
		else
			leastCrowded = researchers;

		if (leastCrowded.isEmpty()) {
			return leastCrowded;
		}

		// Find favorite opinion.
		double favorite = leastCrowded.stream()
				.mapToDouble(r -> RelationshipUtil.getOpinionOfPerson(assistant, r))
				.max().getAsDouble();

		// Get list of favorite researchers.
		final double fav = favorite;
		return leastCrowded.stream()
					.filter(r -> RelationshipUtil.getOpinionOfPerson(assistant, r) == fav)
					.toList();
	}

	/**
	 * Get a list of all available researchers to assist.
	 *
	 * @param assistant the research assistant.
	 * @return list of researchers.
	 */
	private static List<Person> getAvailableResearchers(Person assistant) {
		List<Person> result = new ArrayList<>();

		for(Person person : getLocalPeople(assistant)) {
			Task personsTask = person.getMind().getTaskManager().getTask();
			if (personsTask instanceof ResearchScientificStudy researchTask && !personsTask.isDone()
				&& !researchTask.hasResearchAssistant() && researchTask.getResearchScience() != null) {
					SkillType scienceSkill = researchTask.getResearchScience().getSkill();
					int personSkill = person.getSkillManager().getEffectiveSkillLevel(scienceSkill);
					int assistantSkill = assistant.getSkillManager().getEffectiveSkillLevel(scienceSkill);
					if (assistantSkill < personSkill)
						result.add(person);
			}
		}

		return result;
	}

	/**
	 * Gets a collection of people in a person's settlement or rover. The resulting
	 * collection doesn't include the given person.
	 *
	 * @param person the person checking
	 * @return collection of people
	 */
	private static Collection<Person> getLocalPeople(Person person) {
		Collection<Person> potentials = null;
		if (person.isInSettlement()) {
			potentials = person.getAssociatedSettlement().getIndoorPeople();
		}
		else if (person.isInVehicle()) {
			Crewable rover = (Crewable) person.getVehicle();
			potentials = rover.getCrew();
		}

		Collection<Person> people = null;
		if (potentials != null) {
			people = new ArrayList<>(potentials);
			people.remove(person);
		}
		else {
			Collections.emptyList();
		}
		return people;
	}

	@Override
	protected double performMappedPhase(double time) {
		if (getPhase() == null) {
			throw new IllegalArgumentException("Task phase is null");
		} else if (ASSISTING.equals(getPhase())) {
			return assistingPhase(time);
		} else {
			return time;
		}
	}

	/**
	 * Perform the assisting phase of the task.
	 *
	 * @param time the amount (millisols) of time to perform the phase.
	 * @return the amount (millisols) of time remaining after performing the phase.
	 * @throws Exception
	 */
	private double assistingPhase(double time) {
	
        // If person is incapacitated, end task.
        if (person.getPerformanceRating() <= .2) {
            endTask();
            return time;
        }

		if (person.getPhysicalCondition().computeFitnessLevel() < 2) {
			logger.log(person, Level.FINE, 10_000, "Ended assisting researcher. Not feeling well.");
			endTask();
			return time;
		}

	      // Check if task is finished.
        if (((Task) researchTask).isDone()) {
            endTask();
            return 0;
        }            

		// Add experience
		addExperience(time);

		// Add relationship modifier for opinion of assistant from the researcher.
		RelationshipUtil.changeOpinion(researchTask.getResearcher(), person, BASE_RELATIONSHIP_MODIFIER * time);

		return 0;
	}

	/**
	 * Remove the assistant
	 */
	@Override
	protected void clearDown() {
		researchTask.setResearchAssistant(null);

		super.clearDown();
	}
}