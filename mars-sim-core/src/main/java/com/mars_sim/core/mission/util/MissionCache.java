/*
 * Mars Simulation Project
 * MissionCache.java
 * @date 2024-08-04
 * @author Manny Kung
 */
package com.mars_sim.core.mission.util;

import java.util.ArrayList;
import java.util.List;

import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.tool.RandomUtil;

/**
 * Class represents a set of MissionJob that can be used to select a new mission. 
 * They are weighted to the probability of being selected.
 */
public class MissionCache {
	private List<MissionJob> missions = new ArrayList<>();
    private double totalProb = 0;
    private String context;
    private MarsTime createdOn;
    private MissionJob lastSelected;

    /**
     * Creates a cache of missions. A cache can work in transient mode where selected entries are removed.
     * A static mode means entries are fixed and never removed.
     * 
     * @param context Descriptive context of the purpose
     * @param createdOn If this is non-null then the cache works in transient mode.
     */
    public MissionCache(String context, MarsTime createdOn) {
        this.context = context;
        if (createdOn != null) {
            this.createdOn = createdOn;
        }
    }

    /**
     * Adds a new potential MissionJob to the cache.
     * 
     * @param job The new potential Task.
     */
	public void put(MissionJob job) {
		missions.add(job);
		totalProb += job.getScore().getScore();
	}

    /**
     * Adds a list of jobs to the cache. Only select those that have a +ve score.
     * 
     * @param jobs
     */
    public void add(List<MissionJob> jobs) {
        for (MissionJob j : jobs) {
            if (j.getScore().getScore() > 0) {
                missions.add(j);
                totalProb += j.getScore().getScore();
            }
        }
    }

    /**
     * Gets when this cache instance was created.
     */
    public MarsTime getCreatedOn() {
        return createdOn;
    }

    /**
     * Gets the total probability score for all missions.
     * 
     * @return
     */
    public double getTotal() {
        return totalProb;
    }

    /**
     * Gets the context where this cache was created.
     * 
     * @return
     */
    public String getContext() {
        return context;
    }

    /**
     * Gets the jobs registered.
     */
    public List<MissionJob> getMissions() {
        return missions;
    }

    /**
     * Gets the last entry selected and removed from this cache.
     */
    public MissionJob getLastSelected() {
        return lastSelected;
    }

    /** 
     * Chooses a mission to work at random.
    */
    MissionJob getRandomSelection() {		
        MissionJob lastEntry = null;

        // Comes up with a random double based on probability
        double r = RandomUtil.getRandomDouble(totalProb);
        // Determine which task is selected.
        for (MissionJob entry: missions) {
            double probWeight = entry.getScore().getScore();
            if (r <= probWeight) {
                // THis is a transient cache so remove the selected entry
                if (createdOn != null) {
                    lastSelected = entry;
                    missions.remove(entry);
                    totalProb -= probWeight;
                }
                return entry;
            }
            
            r -= probWeight;
            lastEntry = entry;
        }

        // Should never get here but return the last one
        return lastEntry;
    }
    
	/**
	 * Prepares object for garbage collection.
	 */
	public void destroy() {
		missions.clear();
		missions = null;
	    createdOn = null;
	    lastSelected = null;
	}
}