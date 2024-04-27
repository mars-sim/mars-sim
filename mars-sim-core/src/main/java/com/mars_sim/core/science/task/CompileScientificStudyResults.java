/*
 * Mars Simulation Project
 * CompileScientificStudyResults.java
 * @date 2022-07-11
 * @author Scott Davis
 */
package com.mars_sim.core.science.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.task.util.ExperienceImpact;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.science.ScientificStudy;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.BuildingManager;
import com.mars_sim.core.vehicle.Rover;
import com.mars_sim.tools.Msg;
import com.mars_sim.tools.util.RandomUtil;

/**
 * A task for compiling research data for a scientific study.
 */
public class CompileScientificStudyResults
extends Task {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(CompileScientificStudyResults.class.getName());

	/** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.compileScientificStudyResults"); //$NON-NLS-1$

    /** Task phases. */
    private static final TaskPhase COMPILING_PHASE = new TaskPhase(Msg.getString(
            "Task.phase.compilingPhase")); //$NON-NLS-1$

    // Data members
    /** Computing Units needed per millisol. */		
	private double computingNeeded;
	/** The seed value. */
    private double seed = RandomUtil.getRandomDouble(.05, 0.15);

	private final double TOTAL_COMPUTING_NEEDED;
	
    /** The scientific study to compile. */
    private ScientificStudy study;
    
    
	/**
	 * Create a new Study anda Task to build the proposal for a Person
	 * @param p
	 * @return
	 */
	static Task createTask(Person p) {
		var study = determineStudy(p);
		if (study == null) {		
            logger.severe(p, "This task is no longer needed.");
            return null;
		}

		// Found a suitable study
		var impact = new ExperienceImpact(25D, NaturalAttributeType.ACADEMIC_APTITUDE,
		false, 0.2D, Set.of(study.getScience().getSkill()));

		return new CompileScientificStudyResults(p, study, impact);
	}

    /**
     * Constructor.
     * 
     * @param person the person performing the task.
     * @throws Exception if error constructing the class.
     */
    private CompileScientificStudyResults(Person person, ScientificStudy study, ExperienceImpact impact) {
        // Use task constructor. Skill determined by Study
        super(NAME, person, false, impact, RandomUtil.getRandomDouble(20, 50));
        
		TOTAL_COMPUTING_NEEDED = getDuration() * seed;
		computingNeeded = TOTAL_COMPUTING_NEEDED;
		
		logger.log(person, Level.INFO, 10_000, "Requested computing resources: " 
		 		+ Math.round(TOTAL_COMPUTING_NEEDED * 100.0)/100.0 + " CUs for "
		 		+ NAME + ".");
		 		

        // Determine study.
        this.study = study;
        setDescription(Msg.getString("Task.description.compileScientificStudyResults.detail",
                study.getName())); //$NON-NLS-1$

        // If person is in a settlement, try to find an administration building.
        boolean adminWalk = false;
        if (person.isInSettlement()) {
            Building b = BuildingManager.getAvailableBuilding(study.getScience(), person);
            if (b != null) {
                // Walk to that building.
                walkToResearchSpotInBuilding(b, true);
                adminWalk = true;
            }
        }

        if (!adminWalk) {

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
        addPhase(COMPILING_PHASE);
        setPhase(COMPILING_PHASE);
    }

    /**
     * Determines the scientific study that will be compiled.
     * 
     * @return study or null if none available.
     */
    private static ScientificStudy determineStudy(Person person) {
        List<ScientificStudy> possibleStudies = new ArrayList<>();

        // Add primary study if in paper phase.
        ScientificStudy primaryStudy = person.getStudy();
        if ((primaryStudy != null) && ScientificStudy.PAPER_PHASE.equals(primaryStudy.getPhase()) &&
                    !primaryStudy.isPrimaryPaperCompleted()) {
            // Primary study added twice to double chance of random selection.
            possibleStudies.add(primaryStudy);
            possibleStudies.add(primaryStudy);
        }

        // Add all collaborative studies in research phase.
        for(ScientificStudy collabStudy : person.getCollabStudies()) {
            if (ScientificStudy.PAPER_PHASE.equals(collabStudy.getPhase()) &&
                    !collabStudy.isCollaborativePaperCompleted(person))
                possibleStudies.add(collabStudy);
        }

        // Randomly select study.
        return RandomUtil.getRandomElement(possibleStudies);
    }

    /**
     * Gets the field of science that the researcher is involved with in a study.
     * 
     * @return the field of science or null if researcher is not involved with study.
     */
    public ScienceType getScience() {
        if (study == null)
        	return null;

        return study.getContribution(person);
    }


    /**
     * Gets the effective compilation time based on the person's science skill.
     * 
     * @param time the real amount of time (millisol) for result data compilation.
     * @return the effective amount of time (millisol) for result data compilation.
     */
    public double getEffectiveCompilationTime(double time) {
        // Determine effective compilation time based on the science skill.
        double compilationTime = time;
        int scienceSkill = getEffectiveSkillLevel();
        if (scienceSkill == 0) {
            compilationTime /= 2D;
        }
        if (scienceSkill > 1) {
            compilationTime += compilationTime * (.2D * scienceSkill);
        }

        return compilationTime;
    }

    @Override
    public double performMappedPhase(double time) {
        if (getPhase() == null) {
            throw new IllegalArgumentException("Task phase is null");
        }
        else if (COMPILING_PHASE.equals(getPhase())) {
            return compilingPhase(time);
        }
        else {
            return time;
        }
    }

    /**
     * Performs the data results compilation phase.
     * 
     * @param time the amount of time (millisols) to perform the phase.
     * @return the amount of time (millisols) left over after performing the phase.
     * @throws Exception if error performing the phase.
     */
    public double compilingPhase(double time) {
		double remainingTime = 0;
		
        // If person is incapacitated, end task.
        if (person.getPerformanceRating() <= .2) {
            endTask();
            return time;
        }

		if (person.getPhysicalCondition().computeFitnessLevel() < 2) {
			logger.log(person, Level.FINE, 10_000, "Ended compiling scientific results. Not feeling well.");
			endTask();
			return time;
		}

		if (isDone() || getTimeCompleted() + time > getDuration() || computingNeeded <= 0) {
        	// this task has ended
    		logger.info(person, 30_000L, NAME + " - " 
    				+ Math.round((TOTAL_COMPUTING_NEEDED - computingNeeded) * 100.0)/100.0 
    				+ " CUs Used.");
        	endTask();
            return time;
        }

        int msol = getMarsTime().getMillisolInt();
       
        computingNeeded = person.getAssociatedSettlement().getBuildingManager().
            	accessNode(person, computingNeeded, time, seed, 
            			msol, getDuration(), NAME);

        // Add paper work time to study.
        double compilingTime = getEffectiveCompilationTime(time);
        if (study.getPrimaryResearcher().equals(person)) {
            study.addPrimaryPaperWorkTime(compilingTime);
            if (study.isPrimaryPaperCompleted() && computingNeeded <= 0) {
    			logger.log(worker, Level.INFO, 0, "Spent "
    					+ Math.round(study.getPrimaryPaperWorkTimeCompleted() *10.0)/10.0
    					+ " millisols in compiling data for primary research study "
    					+ study.getName() + ".");
            	endTask();
            }
        }
        else {
            study.addCollaborativePaperWorkTime(person, compilingTime);
            if (study.isCollaborativePaperCompleted(person) && computingNeeded <= 0) {
    			logger.log(worker, Level.INFO, 0, "Spent "
    					+ Math.round(study.getCollaborativePaperWorkTimeCompleted(person) *10.0)/10.0
    					+ " millisols in performing lab experiments for collaborative research study "
    					+ study.getName() + ".");
            	endTask();
            }
        }

        // Add experience
        addExperience(time);

        return remainingTime;
    }
}