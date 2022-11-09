/*
 * Mars Simulation Project
 * TaskCache.java
 * @date 2022-09-18
 * @author Barry Evans
 */
package org.mars_sim.msp.core.person.ai.task.util;

import java.util.ArrayList;
import java.util.List;

import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * Class represents a set of TaskJob that can be used to select a new Task for a Work. 
 * They are weighted to the probability of being selected.
 */
public class TaskCache {
	private List<TaskJob> tasks = new ArrayList<>();
    private double totalProb = 0;
    private String context;
    private MarsClock createdOn;
    private TaskJob lastEntry;

    /**
     * Create a cache of Tasks. A cache can work in transient mode where selected entries are removed.
     * A static mode means entries are fixed and never removed.
     * 
     * @param context Descriptive context of the purpose
     * @param createdOn If this is non-null then the cache works in transient mode.
     */
    public TaskCache(String context, MarsClock createdOn) {
        this.context = context;
        if (createdOn != null) {
            this.createdOn = new MarsClock(createdOn);
        }
    }

    /**
     * Add a new potetential TaskJob to the cache.
     * @param job The new potential Task.
     */
	public void put(TaskJob job) {
		tasks.add(job);
		totalProb += job.getScore();
	}

    /**
     * This creates a basic Task Job delegated to a MetaType class with a default score.
     */
    public void putDefault(MetaTask metaTask) {
        TaskJob newJob = new BasicTaskJob(metaTask, 1D);
        put(newJob);
    }

    public void add(List<TaskJob> jobs) {
        for(TaskJob j : jobs) {
            tasks.add(j);
            totalProb += j.getScore(); 
        }
    }

    /**
     * When was this cache instance created.
     */
    public MarsClock getCreatedOn() {
        return createdOn;
    }

    /**
     * Get the total probability score for all tasks.
     * @return
     */
    public double getTotal() {
        return totalProb;
    }

    /**
     * Get the context where this cache was created.
     * @return
     */
    public String getContext() {
        return context;
    }

    /**
     * Get the jobs registered
     */
    public List<TaskJob> getTasks() {
        return tasks;
    }

    /**
     * What was the last entry selected and removed from this cache?
     */
    public TaskJob getLastSelected() {
        return lastEntry;
    }

    /** 
     * Choose a Task to work at random.
    */
    TaskJob getRandomSelection() {			
        // Comes up with a random double based on probability
        double r = RandomUtil.getRandomDouble(totalProb);
        // Determine which task is selected.
        for (TaskJob entry: tasks) {
            double probWeight = entry.getScore();
            if (r <= probWeight) {
                // THis is a transient cache so remove the selected entry
                if (createdOn != null) {
                    lastEntry = entry;
                    tasks.remove(entry);
                    totalProb -= entry.getScore();
                }
                return entry;
            }
            else {
                r -= probWeight;
            }
        }

        // Should never get here
        return null;
    }
}