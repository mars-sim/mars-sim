/*
 * Mars Simulation Project
 * TaskCache.java
 * @date 2022-09-18
 * @author Barry Evans
 */
package org.mars_sim.msp.core.person.ai.task.utils;

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

    public TaskCache() {
        // Nothing to do
    }


	public void put(MetaTask mt, double probability) {
		tasks.put(mt, probability);
		totalProb += probability;
	}

    public double getTotal() {
        return totalProb;
    }

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