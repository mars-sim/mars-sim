/*
 * Mars Simulation Project
 * ExplorationObjective.java
 * @date 2025-06-15
 * @author Barry Evans
 */
package com.mars_sim.core.mission.objectives;

import java.util.HashMap;
import java.util.Map;

import com.mars_sim.core.mission.MissionObjective;

/**
 *  This class represents an objective for exploring a site during a mission.
 *  It tracks the amount of resources collected cumulatively and assessment of sites
 */
public class ExplorationObjective implements MissionObjective {


	/** Completed exploration per site */
	private Map<String, Double> completedBySite = new HashMap<>();

	/** The cumulative amount (kg) of resources collected across all sites. */
	private Map<Integer, Double> cumulativeCollectedByID = new HashMap<>();

    @Override
    public String getName() {
        return "Site Exploration";
    }

    /**
     * Get the cummulative amount of resources collected so far.
     * @return
     */
    public Map<Integer, Double> getResourcesCollected() {
        return cumulativeCollectedByID;
    }

    /**
     * Record the amount of resources collected at a specific site. This updates the perSite and perResource totals.
     * @param resourceId  ID of the resource collected.
     * @param collected  Amount of resources collected (in kg).
     */
    public void recordResourceCollected(int resourceId, double collected) {
		cumulativeCollectedByID.merge(resourceId, collected, Double::sum);
    }

    /**
     * Get the completd exploratino by site name
     * @return
     */
    public Map<String, Double> getCompletion() {
        return completedBySite;
    }


    /**
     * UPdate how much is completed on a site
     * @param siteName
     * @param completion
     */
    public void updateSiteCompletion(String siteName, double completion) {
        completedBySite.put(siteName, completion);
    }
}
