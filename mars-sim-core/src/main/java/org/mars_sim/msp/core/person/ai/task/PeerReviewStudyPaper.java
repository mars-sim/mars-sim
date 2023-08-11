/*
 * Mars Simulation Project
 * PeerReviewStudyPaper.java
 * @date 2022-07-26
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.mars.sim.tools.Msg;
import org.mars.sim.tools.util.RandomUtil;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeManager;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.job.util.JobType;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskPhase;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.vehicle.Rover;

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

	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = .2D;

	/** Task phases. */
    private static final TaskPhase REVIEW = new TaskPhase(Msg.getString(
            "Task.phase.review")); //$NON-NLS-1$
    
	// Data members.
    /** Computing Units needed per millisol. */		
	private double computingNeeded;
	/** The seed value. */
    private double seed = RandomUtil.getRandomDouble(.005, 0.025);
	/** The total computing resources needed for this task. */
	private final double TOTAL_COMPUTING_NEEDED;
	
	/** The scientific study to review. */
	private ScientificStudy study;

	/**
	 * Constructor.
	 * 
	 * @param person the person performing the task.
	 */
	public PeerReviewStudyPaper(Person person) {
        // Use task constructor. Skill determined later by Study
        super(NAME, person, true, false, STRESS_MODIFIER, null, 25D,
                50D + RandomUtil.getRandomDouble(20D));
        
		TOTAL_COMPUTING_NEEDED = getDuration() * seed;
		computingNeeded = TOTAL_COMPUTING_NEEDED;
		
        setExperienceAttribute(NaturalAttributeType.ACADEMIC_APTITUDE);

        // Determine study to review.
        study = determineStudy(person);
        if (study != null) {
        	addAdditionSkill(study.getScience().getSkill());
            setDescription(Msg.getString("Task.description.peerReviewStudyPaper.detail",
                    study.toString())); //$NON-NLS-1$

            // If person is in a settlement, try to find an administration building.
            boolean adminWalk = false;
            if (person.isInSettlement()) {
                Building b = BuildingManager.getAvailableBuilding(study, person);
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
                    if (person.getVehicle() instanceof Rover) {
                        walkToPassengerActivitySpotInRover((Rover) person.getVehicle(), false);
                    }
                }
                else {
                    // Walk to random location.
                    walkToRandomLocation(true);
                }
            }
        }
        else {
            logger.severe(person, "Study could not be determined");
            endTask();
        }

        // Initialize phase
        addPhase(REVIEW);
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
    private ScientificStudy determineStudy(Person person) {
        ScientificStudy result = null;

        List<ScientificStudy> possibleStudies = new ArrayList<>();

        // Get all studies in the peer review phase.
        Iterator<ScientificStudy> i = scientificStudyManager.getOngoingStudies().iterator();
        while (i.hasNext()) {
            ScientificStudy study = i.next();
            if (ScientificStudy.PEER_REVIEW_PHASE.equals(study.getPhase())) {

                // Check that person isn't a researcher in the study.
                if (!person.equals(study.getPrimaryResearcher()) &&
                        !study.getCollaborativeResearchers().contains(person)) {

                    // Check if person's current job is related to study primary science.
                    JobType job = person.getMind().getJob();
                    if (job != null) {
						ScienceType jobScience = ScienceType.getJobScience(job);
						if (study.getScience() == jobScience) {
						    possibleStudies.add(study);
                        }
                    }
                }
            }
        }

        // Randomly select study.
        if (possibleStudies.size() > 0) {
            int selected = RandomUtil.getRandomInt(possibleStudies.size() - 1);
            result = possibleStudies.get(selected);
        }

        return result;
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
		if (study.isCompleted() || isDone() || getTimeCompleted() + time > getDuration() || computingNeeded <= 0) {
			logger.log(worker, Level.INFO, 0, "Just spent "
					+ (int)study.getPeerReviewTimeCompleted()
					+ " msols to finish peer reviewing a paper "
					+ " for " + study.getName() + ".");
			// this task has ended
	  		logger.info(person, 0, NAME + " - " 
    				+ Math.round((TOTAL_COMPUTING_NEEDED - computingNeeded) * 100.0)/100.0 
    				+ " CUs Used.");
			endTask();
			return time;
		}
		
		int msol = getMarsTime().getMillisolInt(); 
              
        computingNeeded = person.getAssociatedSettlement().getBuildingManager().
            	accessNode(person, computingNeeded, time, seed, 
            			msol, getDuration(), NAME);

        // Add experience
        addExperience(time);

        return 0D;
    }
    
	@Override
	protected void addExperience(double time) {
        // Experience points adjusted by person's "Experience Aptitude" attribute.
        NaturalAttributeManager nManager = person.getNaturalAttributeManager();
        int aptitude = nManager.getAttribute(NaturalAttributeType.ACADEMIC_APTITUDE);

		double learned = time * (aptitude / 100D) * RandomUtil.getRandomDouble(1);

		person.getSkillManager().addExperience(study.getScience().getSkill(), learned, time);

		super.addExperience(time);
	}
}
