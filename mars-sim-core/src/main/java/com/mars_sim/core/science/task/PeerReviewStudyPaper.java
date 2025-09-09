/*
 * Mars Simulation Project
 * PeerReviewStudyPaper.java
 * @date 2022-07-26
 * @author Scott Davis
 */
package com.mars_sim.core.science.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.computing.ComputingJob;
import com.mars_sim.core.computing.ComputingLoadType;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.task.util.ExperienceImpact;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.science.ScientificStudy;
import com.mars_sim.core.science.StudyStatus;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.tool.RandomUtil;
import com.mars_sim.core.vehicle.Rover;

/**
 * A task for peer reviewing a compiled study's paper.
 */
public class PeerReviewStudyPaper extends Task {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(PeerReviewStudyPaper.class.getName());

	/** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.peerReviewStudyPaper"); //$NON-NLS-1$

	/** Task phases. */
    private static final TaskPhase REVIEW = new TaskPhase(Msg.getString(
            "Task.phase.review")); //$NON-NLS-1$
    
    private ComputingJob compute;

	/** The scientific study to review. */
	private ScientificStudy study;

    
	/**
	 * Create a Task to review a study. This will select the most appropirate Scientific Study for the Person
	 * and create an appropriate Task.
	 * @param person
	 * @return
	 */
	public static PeerReviewStudyPaper createTask(Person person) {
		var study = determineStudy(person);
		if (study != null) {
			// Found a suitable study
			var impact = new ExperienceImpact(25D, NaturalAttributeType.ACADEMIC_APTITUDE,
										false, 0.2D,
							            study.getScience().getSkill());
			return new PeerReviewStudyPaper(person, study, impact);
		}

		return null;
	}


	/**
	 * Constructor.
	 * 
	 * @param person the person performing the task.
	 */
	private PeerReviewStudyPaper(Person person, ScientificStudy study, ExperienceImpact impact) {
        // Use task constructor. Skill determined later by Study
        super(NAME, person, false, impact, 50D + RandomUtil.getRandomDouble(20D));
        this.study = study;

        int now = getMarsTime().getMillisolInt();
        
        this.compute = new ComputingJob(person.getAssociatedSettlement(), ComputingLoadType.LOW, now, getDuration(), NAME);

        compute.pickSingleNode(0, now);
		
        // Determine study to review.
        setDescription(Msg.getString("Task.description.peerReviewStudyPaper.detail",
                study.getName())); //$NON-NLS-1$

        // If person is in a settlement, try to find an administration building.
        boolean adminWalk = false;
        if (person.isInSettlement()) {
            Building b = BuildingManager.getAvailableBuilding(study.getScience(), person);
            if (b != null) {
                // Walk to that building.
                walkToResearchSpotInBuilding(b, false);
                adminWalk = true;
            }
            else {
                b = getAvailableAdministrationBuilding(person);
                if (b != null) {
                    // Walk to that building.
                    walkToResearchSpotInBuilding(b, false);
                    adminWalk = true;
                }
            }
        }

        if (!adminWalk) {

            if (person.isInVehicle()) {
                // If person is in rover, walk to passenger activity spot.
                if (person.getVehicle() instanceof Rover rover) {
                    walkToPassengerActivitySpotInRover(rover, false);
                }
            }
            else {
                // Walk to random location.
                walkToRandomLocation(true);
            }
        }
        
        // Initialize phase
        setPhase(REVIEW);
    }

    /**
     * Gets an available administration building that the person can use.
     * 
     * @param person the person
     * @return available administration building or null if none.
     */
    private static Building getAvailableAdministrationBuilding(Person person) {

        Building result = null;

        if (person.isInSettlement()) {
            BuildingManager manager = person.getSettlement().getBuildingManager();
            Set<Building> administrationBuildings = manager.getBuildingSet(FunctionType.ADMINISTRATION);
            administrationBuildings = BuildingManager.getLeastCrowdedBuildings(
            		BuildingManager.getNonMalfunctioningBuildings(administrationBuildings));

            if (!administrationBuildings.isEmpty()) {
                Map<Building, Double> administrationBuildingProbs = BuildingManager.getBestRelationshipBuildings(
                        person, administrationBuildings);
                result = RandomUtil.getWeightedRandomObject(administrationBuildingProbs);
            }
        }

        return result;
    }

    /**
     * Determines the scientific study that will be reviewed.
     * 
     * @return study or null if none available.
     */
    private static ScientificStudy determineStudy(Person person) {

        List<ScientificStudy> possibleStudies = new ArrayList<>();

        // Get all studies in the peer review phase.
        for(ScientificStudy study : scientificStudyManager.getAllStudies(false)) {
            // Check that person isn't a researcher in the study.
            if ((StudyStatus.PEER_REVIEW_PHASE == study.getPhase())
                    && !person.equals(study.getPrimaryResearcher())
                    && !study.getCollaborativeResearchers().contains(person)) {

                // Check if person's current job is related to study primary science.
                JobType job = person.getMind().getJobType();
                if (job != null) {
                    ScienceType jobScience = ScienceType.getJobScience(job);
                    if (study.getScience() == jobScience) {
                        possibleStudies.add(study);
                    }
                }
            }
        }

        // Randomly select study.
        return RandomUtil.getRandomElement(possibleStudies);
    }

    @Override
    protected double performMappedPhase(double time) {
        if (getPhase() == null) {
            throw new IllegalArgumentException("Task phase is null");
        }
        else if (REVIEW.equals(getPhase())) {
            return reviewingPhase(time);
        }
        else {
            return time;
        }
    }

    /**
     * Performs the study peer reviewing phase.
     * 
     * @param time the amount of time (millisols) to perform the phase.
     * @return the amount of time (millisols) left over after performing the phase.
     */
    private double reviewingPhase(double time) {

        // If person is incapacitated, end task.
        if (person.getPerformanceRating() < 0.1) {
            endTask();
        }

		if (person.getPhysicalCondition().computeFitnessLevel() < 2) {
			logger.log(person, Level.FINE, 10_000, "Ended peer reviewing study paper. Not feeling well.");
			endTask();
		}

        // Check if peer review phase in study is completed.
		if (study.isCompleted() || isDone() || getTimeCompleted() + time > getDuration() || compute.isCompleted()) {
			endTask();
			return time;
		}
		
		compute.process(getTimeCompleted(), getMarsTime().getMillisolInt());

        // Add experience
        addExperience(time);

        return 0D;
    }
}
