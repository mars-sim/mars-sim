/*
 * Mars Simulation Project
 * CollectResourceObjective.java
 * @date 2025-06-15
 * @author Barry Evans
 */
package com.mars_sim.core.mission.objectives;

import java.util.HashMap;
import java.util.Map;

import com.mars_sim.core.mission.MissionObjective;

/**
 *  This class represents an objective for collecting resources during a mission.
 *  It tracks the amount of resources collected at various sites and cumulatively.
 */
@SuppressWarnings("serial")
public class CollectResourceObjective implements MissionObjective {
	
    private String name;
    private double siteGoal = 0.0;

	/** The cumulative amount (kg) of resources collected per site */
	private Map<Integer, Double> amountCollectedBySite = new HashMap<>();

	/** The cumulative amount (kg) of resources collected across all sites. */
	private Map<Integer, Double> cumulativeCollectedByID = new HashMap<>();

    /**
     * Constructor.
     * 
     * @param siteResourceGoal 
     */
    public CollectResourceObjective(double siteResourceGoal) {
        this.name = "Collect Resources";
        this.siteGoal = siteResourceGoal;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Gets the cumulative amount of resources collected so far.
     * 
     * @return
     */
    public Map<Integer, Double> getResourcesCollected() {
        return cumulativeCollectedByID;
    }

    /**
     * Gets the cumulative amount of resources collected at each site.
     * 
     * @return
     */
    public Map<Integer,Double> getCollectedAtSites() {
        return amountCollectedBySite;
    }

    /**
     * Records the amount of resources collected at a specific site. This updates the perSite and perResource totals.
     * 
     * @param siteIndex Index of the site where the resources were collected.
     * @param resourceId  ID of the resource collected.
     * @param collected  Amount of resources collected (in kg).
     */
    public void recordResourceCollected(int siteIndex, int resourceId, double collected) {
        amountCollectedBySite.merge(siteIndex, collected, Double::sum);

		cumulativeCollectedByID.merge(resourceId, collected, Double::sum);

    }

    /**
     * Gets the collection goal (resources in [kg]) at each site.
     * 
     * @return
     */
    public double getSiteResourceGoal() {
        return siteGoal;
    }
}
