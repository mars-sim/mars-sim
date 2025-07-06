/*
 * Mars Simulation Project
 * AnalyzeMapData.java
 * @date 2024-07-23
 * @author Manny Kung
 */

package com.mars_sim.core.person.ai.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.mars_sim.core.computing.ComputingJob;
import com.mars_sim.core.computing.ComputingLoadType;
import com.mars_sim.core.environment.MineralSite;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.mission.Mining;
import com.mars_sim.core.person.ai.task.util.ExperienceImpact;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.tool.RandomUtil;

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
														 SkillType.COMPUTING, SkillType.AREOLOGY);

    // Data members.
	/** The number of ROI sites. */	
	private int numROIs;
	/** The composite score for a multi-disciplinary of skills. */
	private double compositeSkill;
	/** The portion of analytical effort spent. */
	private double effort;
	/** The total amount of work done. */
	private double totalWork;
	
    /** Computing Units needed per millisol. */		
	private ComputingJob compute;
	
	/** The selected explored location for this session. */
	private MineralSite exploredLoc;
	

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
		
        int now = getMarsTime().getMillisolInt();
        
		var s = person.getAssociatedSettlement();
        this.compute = new ComputingJob(s, ComputingLoadType.HIGH, now, getDuration(), NAME);

        compute.pickMultipleNodes(0, now);
        
		var eMgr = s.getExplorations();
    	Set<Coordinates> mineralLocs = eMgr.getNearbyMineralLocations();  	
    	
    	int numSites = mineralLocs.size(); 	
    	if (numSites == 0) {
    		
	       	// Set task phase to discovering
	    	addPhase(DISCOVERING);
	        setPhase(DISCOVERING);
    	}
    	else {

    		Set<MineralSite> locROIs = eMgr.getDeclaredLocations();

    		numROIs = locROIs.size();
    		
    		if (numROIs == 0) {
				
    			// At the beginning, here is where it will most likely land on
    			
				// Set task phase to discovering
    	    	addPhase(DISCOVERING);
    	        setPhase(DISCOVERING);
    	        
    			return;
    		} 
    		
    		else if (numROIs == 1) {
    			exploredLoc = new ArrayList<>(locROIs).get(0);
    		}
    		
    		else {
    			double rand = RandomUtil.getRandomDouble(Mining.MATURE_ESTIMATE_NUM * 1.5);
    			List<MineralSite> sitesToimprove = locROIs.stream()
    	    			.filter(el -> el != null && el.getNumEstimationImprovement() < rand)
    	    			.toList();
    			
    			int num = sitesToimprove.size();
    			if (num == 0) {
    				
    				// Set task phase to analyzing
        	    	addPhase(ANALYZING);
        	        setPhase(ANALYZING);
        	        
    				return;
    			}
    			
    			else if (num > 0) {
    				exploredLoc = RandomUtil.getRandomElement(sitesToimprove);
    				
    				// Set task phase to analyzing
        	    	addPhase(ANALYZING);
        	        setPhase(ANALYZING);
        	        
    				return;
    			}

    		}
    		
    		if (exploredLoc == null) {
    			
    	       	// Set task phase to discovering
    	    	addPhase(DISCOVERING);
    	        setPhase(DISCOVERING);
    	        
    			return;
    		}

    		double certainty = exploredLoc.getAverageCertainty() / 100.0;
    		
    		// The higher the numImprovement, the more difficult the numerical solution, and 
    		// the more the computing resources needed to do the refinement.		
    		// The higher the composite skill, the less the computing resource.  
    			
    		// If a person is in a vehicle, either the vehicle has a computing core or 
    		// it relies on some comm bandwith to connect with its settlement's computing core
    		// to handle the computation
    		int limit = (int)Math.round(Math.max(4, Mining.MATURE_ESTIMATE_NUM - certainty));
    		
    		int rand = RandomUtil.getRandomInt(0, limit);
    		
    		int value = (int)Math.round(rand / 5.0);
    		
    		if (rand < value) {
    	       	// Add task phases
    	    	addPhase(DISCOVERING);
    	        setPhase(DISCOVERING);
    		}
            else if (!exploredLoc.isEmpty()) {
    	       	// Add task phases
    	    	addPhase(ANALYZING);
    	        setPhase(ANALYZING);
            }
            else {
            	endTask();
            }
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
			throw new IllegalArgumentException("The AnalyzeMapData phase is null");
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

    	consumeComputingResource();

    	totalWork += time;
        
        if (totalWork > getDuration() *.95) {

        	// Get a lowest range rover
        	double range = person.getAssociatedSettlement().getVehicleWithMinimalRange().getEstimatedRange() * (1 + RandomUtil.getRandomDouble(-.1, .1));

     		int skill = (int)Math.round(compositeSkill);
     		
			double limit =  300 + (range - 200) * Math.min(100, 2 * getMarsTime().getMissionSol()) / 200;
     		
     		Coordinates aSite = null;
			var eMgr = person.getAssociatedSettlement().getExplorations();
     		// If there's no ROI zoned up yet, get the first one right away.
     		
     		if (numROIs == 0) {
	     		// Look for the first site to be analyzed and explored
	     		aSite = eMgr.getNextClosestMineralLoc(limit);
	
	     		if (aSite != null) {
		         	// Creates an initial explored site in SurfaceFeatures
	     			MineralSite el = eMgr.createARegionOfInterest(aSite, skill);
		         			
	     			if (el != null) {
	     				logger.info(person, 20_000, "Zoned up the first new ROI at " +  aSite.getFormattedString() + ".");
	     			}
	     		}
	     		else {
	     			logger.info(person, 20_000, "Could not get the first ROI ready.");
	     		}
     		}
     		else {
     			int rand = RandomUtil.getRandomInt(3);
     			
     			if (rand == 0) {
     				// Pick one of the existing nearby mineral location that's not a ROI yet.
     				aSite = eMgr.getExistingNearbyMineralLocation();
     			}
     			
     			else {
         			// Or get a new nearby mineral location  				
     				aSite = eMgr.acquireNearbyMineralLocation(limit);         		
     			}
     			
         		if (aSite != null) {
    	         	// Creates an initial explored site in SurfaceFeatures
         			MineralSite loc = eMgr.createARegionOfInterest(aSite, skill);
    	         			
         			if (loc != null) {
         				logger.info(person, 20_000, "Zoned up a new ROI at " +  aSite.getFormattedString() + ".");
         			}
         			else {
             			logger.info(person, 20_000, "Could not zone up a ROI.");
         			}
         		}
         		else {
         			logger.info(person, 20_000, "Could not get a nearby site of interest.");
         		}
     		}
     		
            // Add experience points
            addExperience(time);
            
         	endTask();
        }

    	
       	if (isDone() || getTimeLeft() <= 0) {
        	// this task has ended
			endTask();
		}
    	
       	
        return 0;
    }
    
    /**
     * Consumes computing resources.
     * 
     * @param time
     */
    private void consumeComputingResource() {
    	
		if (compute.isCompleted()) {
        	// this task has ended
        	endTask();
        }

		compute.process(getTimeCompleted(), getMarsTime().getMillisolInt());
    }
    
	/**
     * Analyzes the map data phase.
     *
     * @param time time (millisol) to perform phase.
     * @return time (millisol) remaining after performing phase.
     * @throws Exception
     */
    private double analyzingPhase(double time) {
 
    	if (exploredLoc == null) {
    		endTask();
    	}
    	
    	consumeComputingResource();
        
    	double duration = getDuration();
    	
        effort += time;
          
        if (effort > duration * .95) {
        	totalWork += effort;
        	// Limits # of improvement done at a site at most 2 times for each AnalyzeMapData
        	improveMineralConcentrationEstimates(time, effort);
		
        	// Reset effort back to zero
        	effort = 0;
        }

        // Add experience points
        addExperience(time);
    	
		if (isDone() || getTimeLeft() <= 0 || totalWork > duration) {
	    	// this task has ended
			endTask();
		}
		
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
				
		if ((exploredLoc.getNumEstimationImprovement() == 0) || (RandomUtil.getRandomDouble(1.0D) <= probability)) {
		
			// Improve the mineral concentration estimation
			ExploreSite.improveSiteEstimates(exploredLoc, compositeSkill);
		}
	}

    
	/**
	 * Prepares object for garbage collection.
	 */
	@Override
	public void destroy() {
		exploredLoc = null;
		super.destroy();
	}
}
