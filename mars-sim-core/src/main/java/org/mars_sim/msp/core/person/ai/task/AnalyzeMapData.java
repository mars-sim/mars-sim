/*
 * Mars Simulation Project
 * AnalyzeMapData.java
 * @date 2023-06-30
 * @author Manny Kung
 */

package org.mars_sim.msp.core.person.ai.task;

import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.environment.ExploredLocation;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.mission.Mining;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskPhase;
import org.mars_sim.msp.core.structure.building.function.Computation;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * The AnalyzeMapData class is a task for analyzing and studying some map data set.
 */
public class AnalyzeMapData extends Task {

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
	private static final TaskPhase DISCOVERING = new TaskPhase(Msg.getString("Task.phase.discovering")); //$NON-NLS-1$
	
    // Data members.
    /** Computing Units needed per millisol. */		
	private double computingNeeded;
	/** The seed value. */
    private double seed;
	/** The total computing resources needed for this task. */
	private final double TOTAL_COMPUTING_NEEDED;
	/** The composite score for a multi-disciplinary of skills. */
	private double compositeSkill;
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
    			.getAllRegionOfInterestLocations().stream()
    			.filter(site -> site.isMinable())
    			.collect(Collectors.toList());

		int num = siteList0.size();
		if (num == 0) {
			endTask();
		} else if (num == 1) {
			site = siteList0.get(0);
		}
		else {
			List<ExploredLocation> siteList1 = siteList0.stream()
	    			.filter(site -> site.getNumEstimationImprovement() < 
	    					RandomUtil.getRandomDouble(0, Mining.MATURE_ESTIMATE_NUM * 10))
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
		
		double certainty = site.getAverageCertainty() / 100.0;
		
		// The higher the numImprovement, the more difficult the numerical solution, and 
		// the more the computing resources needed to do the refinement.		
		// The higher the composite skill, the less the computing resource.  
		double score = (1 + certainty) / compositeSkill;
		double rand1 = RandomUtil.getRandomDouble(score/60, score/30);
		seed = Math.min(MAX_SEED, rand1);
			
		// If a person is in a vehicle, either the vehicle has a computing core or 
		// it relies on some comm bandwith to connect with its settlement's computing core
		// to handle the computation
		
		TOTAL_COMPUTING_NEEDED = getDuration() * seed;
		computingNeeded = TOTAL_COMPUTING_NEEDED;
		
		int limit = (int)Math.round(Math.max(4, Mining.MATURE_ESTIMATE_NUM - certainty));
		
		int rand = RandomUtil.getRandomInt(0, limit);
		
//		logger.log(person, Level.INFO, 10_000, "Requested " 
//				+ Math.round(TOTAL_COMPUTING_NEEDED * 100.0)/100.0 
//				+ " CUs for "
//				+ NAME + ".");
//				+ ". rand: " + Math.round(rand * 1000.0)/1000.0);
// 		+ ". compositeSkill: " + Math.round(compositeSkill * 10.0)/10.0 
// 		+ ". certainty: " + Math.round(certainty * 1000.0)/1000.0 
// 		+ ". score: " + Math.round(score * 1000.0)/1000.0 
// 		+ ". rand: " + Math.round(rand1 * 1000.0)/1000.0 
// 		+ ". seed: " + Math.round(seed * 1000.0)/1000.0 
// 		+ ". # Candidate sites: " + num 
// 		+ ". Selected site: " + site.getLocation().getFormattedString() + ".");
		
		if (rand < 2) {
	       	// Add task phases
	    	addPhase(DISCOVERING);
	        setPhase(DISCOVERING);
		}
        else {
	       	// Add task phases
	    	addPhase(ANALYZING);
	        setPhase(ANALYZING);
        }
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
    	} else if (DISCOVERING.equals(getPhase())) {
			return discoveringPhase(time);
    	} else if (ANALYZING.equals(getPhase())) {
			return analyzingPhase(time);
		} else {
			return time;
		}
    }


	/**
     * Discovers a site.
     *
     * @param time time (millisol) to perform phase.
     * @return time (millisol) remaining after performing phase.
     * @throws Exception
     */
    private double discoveringPhase(double time) {
    	
       	if (isDone() || getTimeLeft() <= 0 || totalWork > getDuration()) {
        	// this task has ended
			endTask();
		}
    	
    	double workPerMillisol = consumeComputingResource(time);

    	totalWork += time * (1 + workPerMillisol);
        
        if (totalWork > getDuration() * .95) {
//        	logger.log(person, Level.CONFIG, 10_000, 
//        			"effort: " + Math.round(effort * 100.0)/100.0 
//        			+ "  workPerMillisol: " + Math.round(workPerMillisol * 1000.0)/1000.0
//        			+ "  getDuration(): " + Math.round(getDuration() * 100.0)/100.0
//        			);
   	
        	// Get a lowest range rover
     		Rover rover = person.getAssociatedSettlement().getVehicleWithMinimalRange();
     		
     		double rangeLimit = rover.getRange() / 100;

     		int skill = (int)Math.round(compositeSkill);
     		
     		// Look for the first site to be analyzed and explored
     		Coordinates aSite = person.getAssociatedSettlement().getAComfortableNearbyMineralLocation(rangeLimit, skill);
         				
         	// Creates an initial explored site in SurfaceFeatures
         	person.getAssociatedSettlement().createARegionOfInterest(aSite, skill);
         				
         	logger.info(person, 50_000L, "Zoned up a ROI at " +  aSite.getFormattedString() + ".");
         	
         	endTask();
        }

        // Add experience points
        addExperience(time);

        return 0;
    }
    
    private double consumeComputingResource(double time) {
    	int msol = getMarsTime().getMillisolInt();
    	boolean successful = false; 
    	double workPerMillisol = 0;
    	
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
	    		logger.warning(person, 30_000L, "No computing centers available for " + NAME + ".");
	      	
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
	    	logger.log(person, Level.FINE, 30_000L, NAME + " - " 
  				+ Math.round(TOTAL_COMPUTING_NEEDED * 100.0)/100.0 
  				+ " CUs Used.");
	    	endTask();
	    }
    	
    	return workPerMillisol;
    }
    
	/**
     * Analyzes the map data phase.
     *
     * @param time time (millisol) to perform phase.
     * @return time (millisol) remaining after performing phase.
     * @throws Exception
     */
    private double analyzingPhase(double time) {
  
//		if (person.getPhysicalCondition().computeFitnessLevel() < 2) {
//			logger.log(person, Level.INFO, 30_000, "Ended " + NAME + ". Not feeling well.");
//			endTask();
//			return time;
//		}
 
    	double workPerMillisol = consumeComputingResource(time);
        
        effort += time * (1 + workPerMillisol);
          
        if (effort > getDuration() / 3) {
//        	logger.log(person, Level.CONFIG, 10_000, 
//        			"effort: " + Math.round(effort * 100.0)/100.0 
//        			+ "  workPerMillisol: " + Math.round(workPerMillisol * 1000.0)/1000.0
//        			+ "  getDuration(): " + Math.round(getDuration() * 100.0)/100.0
//        			);
        	totalWork += effort;
        	// Limits # of improvement done at a site at most 2 times for each AnalyzeMapData
        	improveMineralConcentrationEstimates(time, effort);
        	// Reset effort back to zero
        	effort = 0;
        }
        	
		if (isDone() || getTimeLeft() <= 0 || totalWork / workPerMillisol > getDuration() ) {
        	// this task has ended
			endTask();
		}

        // Add experience points
        addExperience(time);

        return 0;
    }

    /**
	 * Improves the mineral concentration estimates of an explored site.
	 *
	 * @param time the amount of time available (millisols).
     * @param effort
     */
	private void improveMineralConcentrationEstimates(double time, double effort) {

		double probability = time * effort;
		if (probability > .75)
			probability = .75;
		
		int oldNum = site.getNumEstimationImprovement();
		
		if ((site.getNumEstimationImprovement() == 0) || (RandomUtil.getRandomDouble(1.0D) <= probability)) {
		
			// Improve the mineral concentration estimation
			ExploreSite.improveSiteEstimates(site, compositeSkill);
			
			int newNum = site.getNumEstimationImprovement();
			
			logger.log(person, Level.INFO, 10_000,
					"Improved " + site.getLocation().getFormattedString()
					+ ". # of estimation: " + oldNum 
					+ " -> " + newNum + ".");
		}
	}

    /**
     * Closes out this task.
     */
    @Override
    protected void clearDown() {

    }
    
	/**
	 * Prepares object for garbage collection.
	 */
	public void destroy() {
		site = null;
	}
}
