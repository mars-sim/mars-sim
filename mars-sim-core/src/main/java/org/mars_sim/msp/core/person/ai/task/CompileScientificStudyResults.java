/*
 * Mars Simulation Project
 * CompileScientificStudyResults.java
 * @date 2022-07-11
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.Computation;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * A task for compiling research data for a scientific study.
 */
public class CompileScientificStudyResults
extends Task
implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(CompileScientificStudyResults.class.getName());

	/** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.compileScientificStudyResults"); //$NON-NLS-1$

    /** The stress modified per millisol. */
    private static final double STRESS_MODIFIER = 0D;

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
     * Constructor.
     * @param person the person performing the task.
     * @throws Exception if error constructing the class.
     */
    public CompileScientificStudyResults(Person person) {
        // Use task constructor. Skill determined by Study
        super(NAME, person, true, false,
                STRESS_MODIFIER, null, 25D, RandomUtil.getRandomDouble(50D));
        
		TOTAL_COMPUTING_NEEDED = getDuration() * seed;
		computingNeeded = TOTAL_COMPUTING_NEEDED;
		
        setExperienceAttribute(NaturalAttributeType.ACADEMIC_APTITUDE);

        // Determine study.
        study = determineStudy();
        if (study != null) {
        	addAdditionSkill(study.getScience().getSkill());

            setDescription(Msg.getString("Task.description.compileScientificStudyResults.detail",
                    study.toString())); //$NON-NLS-1$

            // If person is in a settlement, try to find an administration building.
            boolean adminWalk = false;
            if (person.isInSettlement()) {
                Building b = BuildingManager.getAvailableBuilding(study, person);
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
        }
        else {
            logger.severe(person, "This task is no longer needed.");
            endTask();
        }

        // Initialize phase
        addPhase(COMPILING_PHASE);
        setPhase(COMPILING_PHASE);
    }

    /**
     * Determines the scientific study that will be compiled.
     * @return study or null if none available.
     */
    public ScientificStudy determineStudy() {
        ScientificStudy result = null;

        List<ScientificStudy> possibleStudies = new ArrayList<ScientificStudy>();

        // Add primary study if in paper phase.
        ScientificStudy primaryStudy = person.getStudy();
        if (primaryStudy != null) {
            if (ScientificStudy.PAPER_PHASE.equals(primaryStudy.getPhase()) &&
                    !primaryStudy.isPrimaryPaperCompleted()) {
                // Primary study added twice to double chance of random selection.
                possibleStudies.add(primaryStudy);
                possibleStudies.add(primaryStudy);
            }
        }

        // Add all collaborative studies in research phase.
        Iterator<ScientificStudy> i = person.getCollabStudies().iterator();
        while (i.hasNext()) {
            ScientificStudy collabStudy = i.next();
            if (ScientificStudy.PAPER_PHASE.equals(collabStudy.getPhase()) &&
                    !collabStudy.isCollaborativePaperCompleted(person))
                possibleStudies.add(collabStudy);
        }

        // Randomly select study.
        if (possibleStudies.size() > 0) {
            int selected = RandomUtil.getRandomInt(possibleStudies.size() - 1);
            result = possibleStudies.get(selected);
        }

        return result;
    }

    /**
     * Gets the field of science that the researcher is involved with in a study.
     * @return the field of science or null if researcher is not involved with study.
     */
    public ScienceType getScience() {
        if (study == null)
        	return null;

        return study.getContribution(person);
    }


    /**
     * Gets the effective compilation time based on the person's science skill.
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
     * @param time the amount of time (millisols) to perform the phase.
     * @return the amount of time (millisols) left over after performing the phase.
     * @throws Exception if error performing the phase.
     */
    public double compilingPhase(double time) {

        // If person is incapacitated, end task.
        if (person.getPerformanceRating() <= .2) {
            endTask();
        }

		if (person.getPhysicalCondition().computeFitnessLevel() < 2) {
			logger.log(person, Level.FINE, 10_000, "Ended compiling scientific results. Not feeling well.");
			endTask();
		}

        if (isDone()) {
        	// this task has ended
    		logger.info(person, 30_000L, NAME + " - " 
    				+ Math.round((TOTAL_COMPUTING_NEEDED - computingNeeded) * 100.0)/100.0 
    				+ " CUs Used.");
        	endTask();
            return time;
        }

        int msol = marsClock.getMillisolInt();
        
        boolean successful = false; 
        
        if (computingNeeded > 0) {
        	double workPerMillisol = 0; 
 
        	if (computingNeeded <= seed) {
        		workPerMillisol = time * computingNeeded;
        	}
        	else {
        		workPerMillisol = time * seed * RandomUtil.getRandomDouble(.9, 1.1);
        	}

        	// Submit request for computing resources
        	Computation center = person.getAssociatedSettlement().getBuildingManager()
        			.getMostFreeComputingNode(workPerMillisol, msol + 1, (int)(msol + getDuration()));
        	if (center != null) {
        		if (computingNeeded <= seed)
        			successful = center.scheduleTask(workPerMillisol, msol + 1, msol + 2);
        		else
        			successful = center.scheduleTask(workPerMillisol, msol + 1, (int)(msol + getDuration()));
        	}
	    	else
	    		logger.info(person, 30_000L, "No computing centers available for " + NAME + ".");
        	
        	if (successful) {
        		if (computingNeeded <= seed)
        			computingNeeded = computingNeeded - workPerMillisol;
        		else
        			computingNeeded = computingNeeded - workPerMillisol * getDuration();
        		if (computingNeeded < 0) {
        			computingNeeded = 0; 
        		}
          	}
	    	else {
	    		logger.info(person, 30_000L, "No computing resources for " + NAME + ".");
	    	}
        }
        else if (computingNeeded <= 0) {
        	// this task has ended
    		logger.log(person, Level.INFO, 30_000L, NAME + " - " 
    				+ Math.round(TOTAL_COMPUTING_NEEDED * 100.0)/100.0 
    				+ " CUs Used.");
        	endTask();
        }   
        	
        // Check if data results compilation in study is completed.
        boolean isPrimary = study.getPrimaryResearcher().equals(person);

        // Add paper work time to study.
        double compilingTime = getEffectiveCompilationTime(time);
        if (isPrimary) {
            study.addPrimaryPaperWorkTime(compilingTime);
        }
        else {
            study.addCollaborativePaperWorkTime(person, compilingTime);
        }

        if (isPrimary) {
            if (study.isPrimaryPaperCompleted() && computingNeeded <= 0) {
    			logger.log(worker, Level.INFO, 0, "Spent "
    					+ Math.round(study.getPrimaryPaperWorkTimeCompleted() *10.0)/10.0
    					+ " millisols in compiling data for primary research study "
    					+ study.getName() + ".");
            	endTask();
            }
        }
        else {
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

        return 0D;
    }
}
