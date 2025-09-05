/*
 * Mars Simulation Project
 * LabTask.java
 * @date 2024-04-13
 * @author Barry Evans
 */
package com.mars_sim.core.science.task;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingException;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.building.function.LifeSupport;
import com.mars_sim.core.building.function.Research;
import com.mars_sim.core.computing.ComputingJob;
import com.mars_sim.core.computing.ComputingLoadType;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.malfunction.Malfunctionable;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.SkillManager;
import com.mars_sim.core.person.ai.social.RelationshipUtil;
import com.mars_sim.core.person.ai.task.util.ExperienceImpact;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.science.ScientificStudy;
import com.mars_sim.core.science.StudyStatus;
import com.mars_sim.core.structure.Lab;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.tool.RandomUtil;
import com.mars_sim.core.vehicle.Rover;
import com.mars_sim.core.vehicle.Vehicle;

/**
 * This class represents a Task that involves working in a Lab for the purpose of a Scientific Study.
 * It handles finding a lab and adding and removing researchers.
 */
public abstract class LabTask extends Task implements ResearchScientificStudy {

    private static final long serialVersionUID = 1L;

	private static SimLogger logger = SimLogger.getLogger(LabTask.class.getName());

    private Lab lab;
	private ScientificStudy study;
	private Malfunctionable malfunctions;
	private Person researchAssistant;

    private ComputingJob compute;

    private TaskPhase researchPhase;


    protected LabTask(String name, Person person, ScientificStudy study, ExperienceImpact impact, double duration,
                    TaskPhase researchPhase, String descriptionKey) {
        super(name, person, false, impact, duration);
		
		if (person.isOutside()) {
			logger.warning(person, "Still Outside.");
			endTask();
			return;
		}

		this.researchAssistant = null;
        this.researchPhase = researchPhase;
        this.study = study;
	
		// Determine study.
		ScienceType science = study.getContribution(person);
		if (science != null) {
			setDescription(Msg.getString(descriptionKey, science.getName())); // $NON-NLS-1$
			lab = getLocalLab(person, science);
			if (lab != null) {
				addPersonToLab(person);

				// Initialize phase
				setPhase(researchPhase);
			} else {
				logger.severe(person, "lab could not be determined.");
				endTask();
				return;
			}
		} else {
			logger.severe(person, "science could not be determined");
			endTask();
			return;
		}

		// Check if person is in a moving rover.
		if (Vehicle.inMovingRover(person)) {
			endTask();
			return;
		}

        int now = getMarsTime().getMillisolInt();
        
        this.compute = new ComputingJob(person.getAssociatedSettlement(), ComputingLoadType.MID, now, getDuration(), name);

        compute.pickMultipleNodes(0, now);
    }

	/**
	 * Determines the scientific study that will be researched.
	 * 
	 * @return study or null if none available.
	 */
	protected static ScientificStudy determineStudy(Person person, Set<ScienceType> target) {
		List<ScientificStudy> possibleStudies = new ArrayList<>();

		// Add primary study if appropriate science and in research phase.
		ScientificStudy primaryStudy = person.getResearchStudy().getStudy();
		if ((primaryStudy != null) && StudyStatus.RESEARCH_PHASE == primaryStudy.getPhase()
					&& !primaryStudy.isPrimaryResearchCompleted()
					&& target.contains(primaryStudy.getScience())) {
			// Primary study added twice to double chance of random selection.
			possibleStudies.add(primaryStudy);
			possibleStudies.add(primaryStudy);
		}

		// Add all collaborative studies with appropriate sciences and in research
		// phase.
		for (ScientificStudy collabStudy : person.getResearchStudy().getCollabStudies()) {
			if (StudyStatus.RESEARCH_PHASE == collabStudy.getPhase()
					&& !collabStudy.isCollaborativeResearchCompleted(person)) {
				ScienceType collabScience = collabStudy.getContribution(person);
				if (target.contains(collabScience)) {
					possibleStudies.add(collabStudy);
				}
			}
		}

		// Randomly select study.
		return RandomUtil.getRandomElement(possibleStudies);
	}

	/**
	 * Adds a person to a lab.
	 * 
	 * @param person 
	 */
	private void addPersonToLab(Person person) {

		if (person.isInSettlement()) {
			Building labBuilding = ((Research) lab).getBuilding();

			// Walk to lab building.
			walkToResearchSpotInBuilding(labBuilding, false);

			malfunctions = labBuilding;
		} else if (person.isInVehicle()) {

			// Walk to lab internal location in rover.
			walkToLabActivitySpotInRover((Rover) person.getVehicle(), false);

			malfunctions = person.getVehicle();
		}

		// Task can close if Walk fails
		if (!isDone()) {
			lab.addResearcher();
		}
		else {
			logger.warning(person, "Could not allocate lab research " + getName() + ", completed early.");
		}
	}


    /**
     * Gets a local lab for scientific research.
	 * 
	 * @param person  the person checking for the lab.
	 * @param science the science to research.
	 * @return laboratory found or null if none.
	 * @throws Exception if error getting a lab.
	 */
	public static Lab getLocalLab(Person person, ScienceType science) {
		Lab result = null;

		if (person.isInSettlement()) {
			result = getSettlementLab(person, science);
		} else if (person.isInVehicle()) {
			result = getVehicleLab(person.getVehicle(), science);
		}

		return result;
	}

    
	/**
	 * Gets a settlement lab to research a particular science.
	 * 
	 * @param person  the person looking for a lab.
	 * @param science the science to research.
	 * @return a valid research lab.
	 */
	private static Lab getSettlementLab(Person person, ScienceType science) {
		Lab result = null;

		BuildingManager manager = person.getSettlement().getBuildingManager();
		Set<Building> labs0 = manager.getBuildingSet(FunctionType.RESEARCH);
		if (labs0.isEmpty()) {
			return result;
		}

		Set<Building> labs1 = getSettlementLabsWithSpecialty(science, labs0);
		if (labs1.isEmpty()) {
			return pickALab(person, labs0);
		}
		
		Set<Building> labs2 = BuildingManager.getNonMalfunctioningBuildings(labs1);
		if (labs2.isEmpty()) {
			return pickALab(person, labs1);
		}
		
		Set<Building> labs3 = getSettlementLabsWithAvailableSpace(labs2);
		if (labs3.isEmpty()) {
			return pickALab(person, labs2);
		}
		
		Set<Building> labs4 = BuildingManager.getLeastCrowdedBuildings(labs3);
		if (labs4.isEmpty()) {
			return pickALab(person, labs3);
		}
		else
			return pickALab(person, labs4);
	}

	/**
	 * Pick a lab by weighted probability.
	 * 
	 * @param person
	 * @param labs
	 * @return
	 */
	private static Lab pickALab(Person person, Set<Building> labs) {
		Map<Building, Double> labBuildingProbs = BuildingManager.getBestRelationshipBuildings(person, labs);
		return RandomUtil.getWeightedRandomObject(labBuildingProbs).getResearch();
	}
	
	/**
	 * Gets a list of research buildings with available research space from a list
	 * of buildings with the research function.
	 * 
	 * @param buildingList list of buildings with research function.
	 * @return research buildings with available lab space.
	 */
	private static Set<Building> getSettlementLabsWithAvailableSpace(Set<Building> buildingList) {
		Set<Building> result = new HashSet<>();

		for(Building building : buildingList) {
			Research lab = building.getResearch();
			if (lab.getResearcherNum() < lab.getLaboratorySize()) {
				result.add(building);
			}
		}

		return result;
	}

	/**
	 * Gets a list of research buildings with a given science specialty from a list
	 * of buildings with the research function.
	 * 
	 * @param science      the science specialty.
	 * @param buildingList list of buildings with research function.
	 * @return research buildings with science specialty.
	 */
	private static Set<Building> getSettlementLabsWithSpecialty(ScienceType science, Set<Building> buildingList) {
		return buildingList.stream()
							.filter(b -> b.getResearch().hasSpecialty(science))
							.collect(Collectors.toSet());
	}

    /**
	 * Gets an available lab in a vehicle. Returns null if no lab is currently
	 * available.
	 * 
	 * @param vehicle the vehicle
	 * @param science the science to research.
	 * @return available lab
	 */
	private static Lab getVehicleLab(Vehicle vehicle, ScienceType science) {

		if ((vehicle instanceof Rover rover) && rover.hasLab()) {
            Lab lab = rover.getLab();
            boolean availableSpace = (lab.getResearcherNum() < lab.getLaboratorySize());
            boolean specialty = lab.hasSpecialty(science);
            boolean malfunction = (rover.getMalfunctionManager().hasMalfunction());
            if (availableSpace && specialty && !malfunction) {
                return lab;
            }
		}

		return null;
	}
    
	/**
	 * Gets the crowding modifier for a researcher to use a given laboratory
	 * building.
	 * 
	 * @param researcher the researcher.
	 * @param lab        the laboratory.
	 * @return crowding modifier.
	 */
	public static double getLabCrowdingModifier(Person researcher, Lab lab) {
		double result = 1D;
		if (researcher.isInSettlement()) {
			Building labBuilding = ((Research) lab).getBuilding();
			if (labBuilding != null) {
				result *= getCrowdingProbabilityModifier(researcher, labBuilding);
				result *= getRelationshipModifier(researcher, labBuilding);
			}
		}
		return result;
	}

	
	/**
	 * Gets the probability modifier for a person performing a task based on his/her
	 * relationships with the people in the room the task is to be performed in.
	 * 
	 * @param person   the person to check for.
	 * @param building the building the person will need to be in for the task.
	 * @return probability modifier
	 */
	private static double getRelationshipModifier(Person person, Building building) {
		double result = 1D;

        LifeSupport lifeSupport = building.getFunction(FunctionType.LIFE_SUPPORT);
		if (lifeSupport != null) {
			double totalOpinion = 0D;
			for (Person occupant : lifeSupport.getOccupants()) {
				if (person != occupant) {
					totalOpinion += ((RelationshipUtil.getOpinionOfPerson(person, occupant) - 50D) / 50D);
				}
			}

			if (totalOpinion >= 0D) {
				result *= (1D + totalOpinion);
			} else {
				result /= (1D - totalOpinion);
			}
		}

		return result;
	}
	
	/**
	 * Gets the probability modifier for a task if person needs to go to a new
	 * building.
	 * 
	 * @param person      the person to perform the task.
	 * @param newBuilding the building the person is to go to.
	 * @return probability modifier
	 * @throws BuildingException if current or new building doesn't have life
	 *                           support function.
	 */
	private static double getCrowdingProbabilityModifier(Person person, Building newBuilding) {
		double modifier = 1D;

		Building currentBuilding = BuildingManager.getBuilding(person);

		if ((currentBuilding != null) && (newBuilding != null) && (currentBuilding != newBuilding)) {

			// Increase probability if current building is overcrowded.
			LifeSupport currentLS = currentBuilding.getLifeSupport();
			int currentOverCrowding = currentLS.getOccupantNumber() - currentLS.getOccupantCapacity();
			if (currentOverCrowding > 0) {
				modifier *= ((double) currentOverCrowding + 2);
			}

			// Decrease probability if new building is overcrowded.
			LifeSupport newLS = newBuilding.getLifeSupport();
			int newOverCrowding = newLS.getOccupantNumber() - newLS.getOccupantCapacity();
			if (newOverCrowding > 0) {
				modifier /= ((double) newOverCrowding + 2);
			}
		}

		return modifier;
	}

	/**
	 * Gets the effective research time based on the person's science skill.
	 * 
	 * @param time the real amount of time (millisol) for research.
	 * @return the effective amount of time (millisol) for research.
	 */
	private double getEffectiveResearchTime(double time) {
		// Determine effective research time based on the science skill.
		double researchTime = time;
		int scienceSkill = getEffectiveSkillLevel();
		if (scienceSkill == 0) {
			researchTime /= 2D;
		} else if (scienceSkill > 1) {
			researchTime += researchTime * (.2D * scienceSkill);
		}

		// Modify by tech level of laboratory.
		int techLevel = lab.getTechnologyLevel();
		if (techLevel > 0) {
			researchTime *= techLevel;
		}

		// If research assistant, modify by assistant's effective skill.
		if (hasResearchAssistant()) {
			SkillManager manager = researchAssistant.getSkillManager();
			int assistantSkill = manager.getEffectiveSkillLevel(study.getScience().getSkill());
			if (scienceSkill > 0) {
				researchTime *= 1D + ((double) assistantSkill / (double) scienceSkill);
			}
		}

		return researchTime;
	}

    /**
     * Gets the study of this research.
     * 
     * @return
     */
	public ScientificStudy getStudy() {
		return study;
	}

    /**
     * Gets the lab is the research taking place.
     * 
     * @return
     */
	public Lab getLaboratory() {
        return lab;
	}

	@Override
	protected double performMappedPhase(double time) {
		if (getPhase() == null) {
			throw new IllegalArgumentException("Task phase is null");
		} else if (researchPhase.equals(getPhase())) {
			return labPhase(time);
		} else {
			return time;
		}
	}

	/**
	 * Performs the researching phase.
	 * 
	 * @param time the amount of time (millisols) to perform the phase.
	 * @return the amount of time (millisols) left over after performing the phase.
	 */
	private double labPhase(double time) {
		double remainingTime = 0;
		
		// If person is incapacitated, end task.
		if (person.getPerformanceRating() < .1) {
			endTask();
            return time;
		}
		
		if (person.getPhysicalCondition().computeFitnessLevel() < 2) {
			logger.fine(person, 10_000, "Ended performing lab research. Not feeling well.");
			endTask();
            return time;
		}

		// Check for laboratory malfunction.
		if (malfunctions.getMalfunctionManager().hasMalfunction()) {
			endTask();
            return time;
		}

		if (isDone() || getTimeCompleted() + time > getDuration() || compute.isCompleted()) {
			endTask();
			return time;
		}
		
		compute.process(getTimeCompleted(), getMarsTime().getMillisolInt());
		
		// Check if person is in a moving rover.
		if (Vehicle.inMovingRover(person)) {
			endTask();
            return time;
		}

		// Do any special research activity
		executeResearchActivity(time);
		if (isDone()) {
			return 0D;
		}

		// Add research work time to study.
		double researchTime = getEffectiveResearchTime(time);
		boolean isPrimary = study.getPrimaryResearcher().equals(person);
		if (isPrimary) {
			study.addPrimaryResearchWorkTime(researchTime);
			if (study.isPrimaryResearchCompleted()) {
				endTask();
			}
		} else {
			study.addCollaborativeResearchWorkTime(person, researchTime);
			if (study.isCollaborativeResearchCompleted(person)) {   
				endTask();
			}
		} 

		// Add experience
		addExperience(time);

		// Check for lab accident.
		checkForAccident(malfunctions, time, 0.001);

		return remainingTime;
	}

	/**
	 * Do any special research activity in the lab ? 
	 * Note: Nothing by default.
	 * Should be overridden for subtasks if needed.
	 * 
	 * @param time Time spent doing the lab research
	 */
	protected void executeResearchActivity(double time) {
		// By default there is nothing to do
	}

	@Override
	public ScienceType getResearchScience() {
		return study.getScience();
	}

	@Override
	public Person getResearcher() {
		return person;
	}

	@Override
	public boolean hasResearchAssistant() {
		return (researchAssistant != null);
	}

	@Override
	public Person getResearchAssistant() {
		return researchAssistant;
	}

	@Override
	public void setResearchAssistant(Person researchAssistant) {
		this.researchAssistant = researchAssistant;
	}

    /**
	 * Releases the lab space.
	 */
	@Override
	protected void clearDown() {
		// Remove person from lab so others can use it.
		if (lab != null) {
			lab.removeResearcher();
			lab = null;
		}

		super.clearDown();
	}
}
