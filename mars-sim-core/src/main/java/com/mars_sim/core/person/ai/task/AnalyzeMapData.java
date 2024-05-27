/*
 * Mars Simulation Project
 * AnalyzeMapData.java
 * @date 2023-07-04
 * @author Manny Kung
 */

package com.mars_sim.core.person.ai.task;

import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import com.mars_sim.core.environment.ExploredLocation;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.mission.Mining;
import com.mars_sim.core.person.ai.task.util.ExperienceImpact;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.structure.building.function.ComputingJob;
import com.mars_sim.core.vehicle.Rover;
import com.mars_sim.mapdata.location.Coordinates;
import com.mars_sim.tools.Msg;
import com.mars_sim.tools.util.RandomUtil;

/**
 * The AnalyzeMapData class is a task for analyzing and studying some map data set.
 */
public class AnalyzeMapData extends Task {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(AnalyzeMapData.class.getName());

	// Static members
	/** Task name */
	private static final String NAME = Msg.getString("Task.description.analyzeMapData"); //$NON-NLS-1$

	/** Task phases. */
	private static final TaskPhase ANALYZING = new TaskPhase(Msg.getString("Task.phase.analyzing")); //$NON-NLS-1$
	private static final TaskPhase DISCOVERING = new TaskPhase(Msg.getString("Task.phase.discovering")); //$NON-NLS-1$
	
	private static final ExperienceImpact IMPACT = new ExperienceImpact(.25D,
														NaturalAttributeType.EXPERIENCE_APTITUDE,
														 false, 0.01,
														 SkillType.COMPUTING);

    // Data members.
    /** Computing Units needed per millisol. */		
	private ComputingJob compute;
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
     	super(NAME, person, false, IMPACT, 30 + RandomUtil.getRandomDouble(-5, 5));
     	
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
		
    	Set<Coordinates> coords = person.getAssociatedSettlement()
    			.getNearbyMineralLocations();  	
    	
		List<ExploredLocation> siteList0 = surfaceFeatures
    			.getAllRegionOfInterestLocations().stream()
    			.filter(s -> s.isMinable()
    					&& coords.contains(s.getLocation()))
    			.toList();

		int num = siteList0.size();
		if (num == 0) {
			endTask();
			return;
		} 
		else if (num == 1) {
			site = siteList0.get(0);
		}
		else {
			List<ExploredLocation> siteList1 = siteList0.stream()
	    			.filter(s -> s.getNumEstimationImprovement() < 
	    					RandomUtil.getRandomDouble(Mining.MATURE_ESTIMATE_NUM / 1.5, 
	    							Mining.MATURE_ESTIMATE_NUM * 1.5))
	    			.toList();
			
			num = siteList1.size();
			if (num == 0) {
				endTask();
				return;
			}
			site = RandomUtil.getRandomElement(siteList1);
		}
		
		if (site == null) {
			endTask();
			return;
		}
		compute = new ComputingJob(person.getAssociatedSettlement(), getDuration(), NAME);

		double certainty = site.getAverageCertainty() / 100.0;
		
		// The higher the numImprovement, the more difficult the numerical solution, and 
		// the more the computing resources needed to do the refinement.		
		// The higher the composite skill, the less the computing resource.  
		double score = (1 + certainty) / compositeSkill;
		double rand1 = RandomUtil.getRandomDouble(score/60, score/30);
			
		// If a person is in a vehicle, either the vehicle has a computing core or 
		// it relies on some comm bandwith to connect with its settlement's computing core
		// to handle the computation
		int limit = (int)Math.round(Math.max(4, Mining.MATURE_ESTIMATE_NUM - certainty));
		
		int rand = RandomUtil.getRandomInt(0, limit);
		
		logger.log(person, Level.FINE, 10_000, "Requested " 
				+ Math.round(compute.getNeeded() * 100.0)/100.0 
				+ " CUs for "
				+ NAME + "."
				+ ". rand: " + Math.round(rand * 1000.0)/1000.0
 		+ ". compositeSkill: " + Math.round(compositeSkill * 10.0)/10.0 
 		+ ". certainty: " + Math.round(certainty * 1000.0)/1000.0 
 		+ ". score: " + Math.round(score * 1000.0)/1000.0 
 		+ ". rand: " + Math.round(rand1 * 1000.0)/1000.0 
 		+ ". # Candidate sites: " + num 
 		+ ". Selected site: " + site.getLocation().getFormattedString() + ".");
		
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
    	
    	consumeComputingResource(time);

    	totalWork += time;
        
        if (totalWork > getDuration() * .95) {

   	
        	// Get a lowest range rover
     		Rover rover = person.getAssociatedSettlement().getVehicleWithMinimalRange();
     		
     		double rangeLimit = rover.getRange() / 100;

     		int skill = (int)Math.round(compositeSkill);
     		
     		// Look for the first site to be analyzed and explored
     		Coordinates aSite = person.getAssociatedSettlement().getAComfortableNearbyMineralLocation(rangeLimit, skill);
         				
         	// Creates an initial explored site in SurfaceFeatures
         	person.getAssociatedSettlement().createARegionOfInterest(aSite, skill);
         				
         	logger.info(person, "Zoned up a ROI at " +  aSite.getFormattedString() + ".");
         	
         	endTask();
        }

        // Add experience points
        addExperience(time);

        return 0;
    }
    
    /**
     * Consumes computing resources.
     * 
     * @param time
     */
    private void consumeComputingResource(double time) {
    	
		if (isDone() || getTimeCompleted() + time > getDuration() || compute.isCompleted()) {
        	// this task has ended
        	endTask();
        }

        compute.consumeProcessing(time, getMarsTime());
    }
    
	/**
     * Analyzes the map data phase.
     *
     * @param time time (millisol) to perform phase.
     * @return time (millisol) remaining after performing phase.
     * @throws Exception
     */
    private double analyzingPhase(double time) {
 
    	consumeComputingResource(time);
        
        effort += time;
          
        if (effort > getDuration() / 3) {
        	logger.log(person, Level.FINER, 10_000, 
        			"effort: " + Math.round(effort * 100.0)/100.0 
        			+ "  time: " + Math.round(time * 1000.0)/1000.0
        			+ "  getDuration(): " + Math.round(getDuration() * 100.0)/100.0
        			);
        	totalWork += effort;
        	// Limits # of improvement done at a site at most 2 times for each AnalyzeMapData
        	improveMineralConcentrationEstimates(time, effort);
        	// Reset effort back to zero
        	effort = 0;
        }
        	
		if (isDone() || getTimeLeft() <= 0 || totalWork / time > getDuration() ) {
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
			
			logger.log(person, Level.FINE, 0,
					"Improved " + site.getLocation().getFormattedString()
					+ ". # of estimation: " + oldNum 
					+ " -> " + newNum + ".");
		}
	}

    
	/**
	 * Prepares object for garbage collection.
	 */
	@Override
	public void destroy() {
		site = null;
		super.destroy();
	}
}
