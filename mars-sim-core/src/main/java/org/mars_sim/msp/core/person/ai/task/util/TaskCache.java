/*
 * Mars Simulation Project
 * TaskCache.java
 * @date 2022-09-18
 * @author Barry Evans
 */
package org.mars_sim.msp.core.person.ai.task.util;

import java.util.HashMap;
import java.util.Map;

import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * Class represents a set of MetaTask that can be used to select a new Task for a Work. 
 * They are weighted to the probability of being selected.
 */
public class TaskCache {
	private Map<MetaTask, Double> tasks = new HashMap<>();
    private double totalProb = 0;
    private String context;

    public TaskCache(String context) {
        this.context = context;
    }

    /**
     * Add a new MetaTask to the cache with a probility score
     * @param mt Meta task
     * @param probability The probability score.
     */
	public void put(MetaTask mt, double probability) {
		tasks.put(mt, probability);
		totalProb += probability;
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
     * Get the MetaTasks registered
     */
    public Map<MetaTask, Double> getTasks() {
        return tasks;
    }

    /** 
     * Choose a MetaTask at random.
    */
    MetaTask getRandomSelection() {			
        // Comes up with a random double based on probability
        double r = RandomUtil.getRandomDouble(totalProb);
        // Determine which task is selected.
        for (Map.Entry<MetaTask, Double> entry: tasks.entrySet()) {
            MetaTask mt = entry.getKey();
            double probWeight = entry.getValue();
            if (r <= probWeight) {
                // Select this task
                return mt;
            }
            else {
                r -= probWeight;
            }
        }

        // Should never get here
        return null;
    }
}