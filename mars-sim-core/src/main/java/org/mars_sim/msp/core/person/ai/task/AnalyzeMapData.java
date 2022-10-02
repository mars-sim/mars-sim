/*
 * Mars Simulation Project
 * AnalyzeMapData.java
 * @date 2022-10-01
 * @author Manny Kung
 */

package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.environment.ExploredLocation;
import org.mars_sim.msp.core.environment.MineralMap;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.mission.Exploration;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskPhase;
import org.mars_sim.msp.core.structure.building.function.Computation;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * The AnalyzeMapData class is a task for analyzing and studying some map data set.
 */
public class AnalyzeMapData extends Task implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(AnalyzeMapData.class.getName());

	// Static members
    /** The maximum allowable amount for seed. */
	private static final double MAX_SEED = 0.5;
	/** Task name */
	private static final String NAME = Msg.getString("Task.description.analyzeMapData"); //$NON-NLS-1$

	/** Task phases. */
	private static final TaskPhase ANALYZING = new TaskPhase(Msg.getString("Task.phase.analyzing")); //$NON-NLS-1$

    // Data members.
	/** The number of estimation improvement made for a site. */	
	private int numImprovement;
    /** Computing Units needed per millisol. */		
	private double computingNeeded;
	/** The seed value. */
    private double seed;
	/** The total computing resources needed for this task. */
	private final double TOTAL_COMPUTING_NEEDED;
	/** The composite score for a multi-disciplinary of skills. */
	private double compositeSkill;		
	/** The adjusted computing work per millisols. */
	private double workPerMillisol;
	/** The portion of effort spent. */
	private double effort;
	/** The total amount of work done. */
	private double totalWork;
	/** The selected explored location for this session. */
	private ExploredLocation site;
	
	/**
	 * Constructor.
	 * 
	 * @param person the person performing the task.
	 */
	public AnalyzeMapData(Person person) {
        // Use Task constructor
     	super(NAME, person, false, false, 0.01, SkillType.COMPUTING, 
     			25D, 30 + RandomUtil.getRandomDouble(-5, 5));
     	
		int prospectingSkill = person.getSkillManager().getEffectiveSkillLevel(SkillType.PROSPECTING);		
		int computingSkill = person.getSkillManager().getEffectiveSkillLevel(SkillType.COMPUTING);		
		int areologySkill = person.getSkillManager().getEffectiveSkillLevel(SkillType.AREOLOGY);
				
		// Modify by "Areology" skill.
		if (areologySkill >= 1) {
			compositeSkill = .75 * areologySkill * (1 + computingSkill + prospectingSkill/2.0);  
		}
		else {
			compositeSkill = .5 * (1 + computingSkill + prospectingSkill/2.0);
		}
	
		List<ExploredLocation> siteList0 = surfaceFeatures
    			.getExploredLocations().stream()
    			.filter(site -> !site.isMined())
    			.collect(Collectors.toList());

		int num = siteList0.size();
		if (num == 0) {
			endTask();
		} else if (num == 1) {
			site = siteList0.get(0);
		}
		else {
			List<ExploredLocation> siteList1 = siteList0.stream()
	    			.filter(site -> site.getNumEstimationImprovement() < 30)
	    			.collect(Collectors.toList());
			
			num = siteList1.size();
			if (num == 0) {
				int rand = RandomUtil.getRandomInt(num - 1);
				site = siteList0.get(rand);
			}
			else if (num == 1) {
				site = siteList1.get(0);
			}
			else {
				int rand = RandomUtil.getRandomInt(num - 1);
				site = siteList1.get(rand);
			}
		}
		
		numImprovement = site.getNumEstimationImprovement();
		
		// The higher the numImprovement, the more difficult the numerical solution, and 
		// the more the computing resources needed to do the refinement.		
		// The higher the composite skill, the less the computing resource.  
		double score = (1 + numImprovement)/compositeSkill;
		double rand1 = RandomUtil.getRandomDouble(score/20.0, score/10.0);
		seed = Math.min(MAX_SEED, rand1);
			
		TOTAL_COMPUTING_NEEDED = getDuration() * seed;
		computingNeeded = TOTAL_COMPUTING_NEEDED;
		
		logger.info(person, 10_000, "Total computing needs: " 
				+ Math.round(TOTAL_COMPUTING_NEEDED * 1000.0)/1000.0 
				+ " CUs. score: " 
				+ Math.round(score * 1000.0)/1000.0 + ". rand: "
				+ Math.round(rand1 * 1000.0)/1000.0 + ". seed: "
				+ Math.round(seed * 1000.0)/1000.0 + ". "
				+ num + " candidate sites identified. Final site selected: " 
				+ site.getLocation().getCoordinateString() + ".");
		
       	// Add task phases
    	addPhase(ANALYZING);
        setPhase(ANALYZING);
    }
	
	
    /**
     * Performs the method mapped to the task's current phase.
     * 
     * @param time the amount of time the phase is to be performed.
     * @return the remaining time after the phase has been performed.
     */
    protected double performMappedPhase(double time) {
    	if (getPhase() == null) {
			throw new IllegalArgumentException("The analyzing task phase is null");
		} else if (ANALYZING.equals(getPhase())) {
			return analyzingPhase(time);
		} else {
			return time;
		}
    }


	/**
     * Analyzes the map data phase.
     *
     * @param time time (millisol) to perform phase.
     * @return time (millisol) remaining after performing phase.
     * @throws Exception
     */
    private double analyzingPhase(double time) {
  
		if (person.getPhysicalCondition().computeFitnessLevel() < 2) {
			logger.log(person, Level.INFO, 30_000, "Ended " + NAME + ". Not feeling well.");
			endTask();
			return time;
		}
 
        int msol = marsClock.getMillisolInt();
        boolean successful = false; 
        
        if (computingNeeded > 0) {
 
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
//    		logger.log(person, Level.INFO, 30_000L, NAME + " - " 
//    				+ Math.round(TOTAL_COMPUTING_NEEDED * 100.0)/100.0 
//    				+ " CUs Used.");
//        	endTask();
        }
        
        effort += time * workPerMillisol;
          
        if (effort > getDuration() / 2D) {
        	totalWork += effort;
        	// Limits # of improvement done at a site at most 2 times for each AnalyzeMapData
        	improveMineralConcentrationEstimates(time, effort);
        	effort = 0;
        }
        	
		if (isDone() || getTimeLeft() <= 0 || totalWork / workPerMillisol > getDuration() ) {
        	// this task has ended
    		logger.info(person, 30_000L, "Done '" + NAME + "' - " 
    				+ Math.round(TOTAL_COMPUTING_NEEDED * 100.0)/100.0 
    				+ " CUs Used.");
			endTask();
			return 0;
		}

        // Add experience points
        addExperience(time);

        return 0;
    }

    /**
	 * Improves the mineral concentration estimates of an explored site.
	 *
	 * @param time the amount of time available (millisols).
     * @param improvement
     */
	private void improveMineralConcentrationEstimates(double time, double improvement) {
		double probability = (time * Exploration.EXPLORING_SITE_TIME / 1000.0) * improvement;
		if (probability > .9)
			probability = .9;
		if ((site.getNumEstimationImprovement() == 0) || (RandomUtil.getRandomDouble(1.0D) <= probability)) {
			ExploreSite.improveSiteEstimates(site, (int)Math.round(compositeSkill));

			logger.log(person, Level.INFO, 10_000,
					NAME + " for " + site.getLocation().getFormattedString()
					+ ". # of estimation done: "
					+ site.getNumEstimationImprovement() + ".");
		}
	}

    /**
     * Closes out this task. If person is inside then transfer the resource from the bag to the Settlement.
     */
    @Override
    protected void clearDown() {

    }
}
