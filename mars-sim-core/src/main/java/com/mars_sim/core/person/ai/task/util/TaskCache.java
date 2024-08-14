/*
 * Mars Simulation Project
 * TaskCache.java
 * @date 2022-09-18
 * @author Barry Evans
 */
package com.mars_sim.core.person.ai.task.util;

import java.util.ArrayList;
import java.util.List;

import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.tool.RandomUtil;

/**
 * Class represents a set of TaskJob that can be used to select a new Task for a Work. 
 * They are weighted to the probability of being selected.
 */
public class TaskCache {
	private List<TaskJob> tasks = new ArrayList<>();
    private double totalProb = 0;
    private String context;
    private MarsTime createdOn;
    private TaskJob lastSelected;

    /**
     * Creates a cache of Tasks. A cache can work in transient mode where selected entries are removed.
     * A static mode means entries are fixed and never removed.
     * 
     * @param context Descriptive context of the purpose
     * @param createdOn If this is non-null then the cache works in transient mode.
     */
    public TaskCache(String context, MarsTime createdOn) {
        this.context = context;
        if (createdOn != null) {
            this.createdOn = createdOn;
        }
    }

    /**
     * Adds a new potential TaskJob to the cache.
     * 
     * @param job The new potential Task.
     */
	public void put(TaskJob job) {
		tasks.add(job);
		totalProb += job.getScore().getScore();
	}

    /**
     * Adds a list of Jobs to the cache. Only select those that have a +ve score.
     * 
     * @param jobs
     */
    public void add(List<TaskJob> jobs) {
        for(TaskJob j : jobs) {
            if (j.getScore().getScore() > 0) {
                tasks.add(j);
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
     * Gets the total probability score for all tasks.
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
    public List<TaskJob> getTasks() {
        return tasks;
    }

    /**
     * Gets the last entry selected and removed from this cache.
     */
    public TaskJob getLastSelected() {
        return lastSelected;
    }

    /** 
     * Chooses a task to work at random.
    */
    TaskJob getRandomSelection() {		
        TaskJob lastEntry = null;

        // Comes up with a random double based on probability
        double r = RandomUtil.getRandomDouble(totalProb);
        // Determine which task is selected.
        for (TaskJob entry: tasks) {
            double probWeight = entry.getScore().getScore();
            if (r <= probWeight) {
                // This is a transient cache so remove the selected entry
                if (createdOn != null) {
                    lastSelected = entry;
                    tasks.remove(entry);
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
		tasks.clear();
		tasks = null;
	    createdOn = null;
	    lastSelected = null;
	}
}