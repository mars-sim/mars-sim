/*
 * Mars Simulation Project
 * TaskCache.java
 * @date 2022-09-18
 * @author Barry Evans
 */
package org.mars_sim.msp.core.person.ai.task.util;

import java.util.ArrayList;
import java.util.List;

import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * Class represents a set of TaskJob that can be used to select a new Task for a Work. 
 * They are weighted to the probability of being selected.
 */
public class TaskCache {
	private List<TaskJob> tasks = new ArrayList<>();
    private double totalProb = 0;
    private String context;

    public TaskCache(String context) {
        this.context = context;
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
     * Choose a Task to work at random.
    */
    TaskJob getRandomSelection() {			
        // Comes up with a random double based on probability
        double r = RandomUtil.getRandomDouble(totalProb);
        // Determine which task is selected.
        for (TaskJob entry: tasks) {
            double probWeight = entry.getScore();
            if (r <= probWeight) {
                // Select this task
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