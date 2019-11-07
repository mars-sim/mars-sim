/**
 * Mars Simulation Project
 * PeerReviewStudyPaper.java
 * @version 3.1.0 2017-09-13
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
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
 * A task for peer reviewing a compiled study's paper.
 */
public class PeerReviewStudyPaper
extends Task
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(PeerReviewStudyPaper.class.getName());

	private static String sourceName = logger.getName().substring(logger.getName().lastIndexOf(".") + 1,
			 logger.getName().length());

	/** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.peerReviewStudyPaper"); //$NON-NLS-1$

	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = .2D;

	/** Task phases. */
    private static final TaskPhase REVIEW = new TaskPhase(Msg.getString(
            "Task.phase.review")); //$NON-NLS-1$

	/** The scientific study to review. */
	private ScientificStudy study;

	/**
	 * Constructor.
	 * @param person the person performing the task.
	 */
	public PeerReviewStudyPaper(Person person) {
        // Use task constructor.
        super(NAME, person, true, false, STRESS_MODIFIER, true,
                10D + RandomUtil.getRandomDouble(300D));

        // Determine study to review.
        study = determineStudy();
        if (study != null) {
            setDescription(Msg.getString("Task.description.peerReviewStudyPaper.detail",
                    study.toString())); //$NON-NLS-1$

            // If person is in a settlement, try to find an administration building.
            boolean adminWalk = false;
            if (person.isInSettlement()) {
                Building adminBuilding = getAvailableAdministrationBuilding(person);
                if (adminBuilding != null) {
                    // Walk to administration building.
                    walkToActivitySpotInBuilding(adminBuilding, false);
                    adminWalk = true;
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
            logger.info("study could not be determined");
            endTask();
        }

        // Initialize phase
        addPhase(REVIEW);
        setPhase(REVIEW);
    }

    /**
     * Gets an available administration building that the person can use.
     * @param person the person
     * @return available administration building or null if none.
     */
    private static Building getAvailableAdministrationBuilding(Person person) {

        Building result = null;

        if (person.isInSettlement()) {
            BuildingManager manager = person.getSettlement().getBuildingManager();
            List<Building> administrationBuildings = manager.getBuildings(FunctionType.ADMINISTRATION);
            administrationBuildings = BuildingManager.getNonMalfunctioningBuildings(administrationBuildings);
            administrationBuildings = BuildingManager.getLeastCrowdedBuildings(administrationBuildings);

            if (administrationBuildings.size() > 0) {
                Map<Building, Double> administrationBuildingProbs = BuildingManager.getBestRelationshipBuildings(
                        person, administrationBuildings);
                result = RandomUtil.getWeightedRandomObject(administrationBuildingProbs);
            }
        }

        return result;
    }

    @Override
    public FunctionType getLivingFunction() {
        return FunctionType.ADMINISTRATION;
    }

    /**
     * Determines the scientific study that will be reviewed.
     * @return study or null if none available.
     */
    private ScientificStudy determineStudy() {
        ScientificStudy result = null;

        List<ScientificStudy> possibleStudies = new ArrayList<ScientificStudy>();

        // Get all studies in the peer review phase.
//        ScientificStudyManager studyManager = Simulation.instance().getScientificStudyManager();
        Iterator<ScientificStudy> i = scientificStudyManager.getOngoingStudies().iterator();
        while (i.hasNext()) {
            ScientificStudy study = i.next();
            if (ScientificStudy.PEER_REVIEW_PHASE.equals(study.getPhase())) {

                // Check that person isn't a researcher in the study.
                if (!person.equals(study.getPrimaryResearcher()) &&
                        !study.getCollaborativeResearchers().keySet().contains(person.getIdentifier())) {

                    // Check if person's current job is related to study primary science.
                    Job job = person.getMind().getJob();
                    if (job != null) {
						ScienceType jobScience = ScienceType.getJobScience(job);
						if (study.getScience().equals(jobScience)) {
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
    protected void addExperience(double time) {
        // Add experience to relevant science skill
        // (1 base experience point per 25 millisols of research time)
        // Experience points adjusted by person's "Academic Aptitude" attribute.
        double newPoints = time / 25D;
        int academicAptitude = person.getNaturalAttributeManager().getAttribute(NaturalAttributeType.ACADEMIC_APTITUDE);
        newPoints += newPoints * ((double) academicAptitude - 50D) / 100D;
        newPoints *= getTeachingExperienceModifier();
		person.getSkillManager().addExperience(study.getScience().getSkill(), newPoints, time);
    }

    @Override
	public List<SkillType> getAssociatedSkills() {
		List<SkillType> results = new ArrayList<SkillType>(1);
		if (study != null) results.add(study.getScience().getSkill());
        return results;
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
     * @param time the amount of time (millisols) to perform the phase.
     * @return the amount of time (millisols) left over after performing the phase.
     */
    private double reviewingPhase(double time) {

        // If person is incapacitated, end task.
        if (person.getPerformanceRating() == 0D) {
            endTask();
        }

        // Check if peer review phase in study is completed.
        if (study.isCompleted()) {
			LogConsolidated.log(Level.INFO, 0, sourceName, "[" + person.getLocationTag().getLocale() + "] "
					+ person.getName() + " just spent " 
					+ Math.round(study.getPeerReviewTimeCompleted() *10.0)/10.0
					+ " millisols to finish peer reviewing a paper "
					+ " in " + study.getScience().getName() 
					+ " in " + person.getLocationTag().getImmediateLocation());	
            endTask();
        }

        if (isDone()) {
            return time;
        }

        // Peer review study. (No operation required for this)

        // Add experience
        addExperience(time);

        return 0D;
    }

    @Override
    public void destroy() {
        super.destroy();

        study = null;
    }
}