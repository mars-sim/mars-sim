/*
 * Mars Simulation Project
 * MiningObjective.java
 * @date 2025-06-15
 * @author Barry Evans
 */
package com.mars_sim.core.mission.objectives;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.mars_sim.core.environment.MineralSite;
import com.mars_sim.core.mission.MissionObjective;
import com.mars_sim.core.vehicle.LightUtilityVehicle;

/**
 *  This class represents an objective performing a mining activity at a site.
 *  It tracks the amount of resources mined cumulatively.
 */
public class MiningObjective implements MissionObjective {

    public static class MineralStats implements Serializable {
        double detected;
        double extracted;
        double collected;

        MineralStats(double detected) {
            this.detected = detected;
        }

        public double getDetected() {
            return detected;
        }
        public double getExtracted() {
            return extracted;
        }
        public double getCollected() {
            return collected;
        }

        public double getAvailable() {
            return extracted - collected;
        }
        
    }

	/** The cumulative amount (kg) of resources collected across all sites. */
	private Map<Integer, MineralStats> minerals;

    private LightUtilityVehicle luv;
    private MineralSite site;


    /**
     * Constructor.
     * @param detectedMinerals 
     */
    public MiningObjective(LightUtilityVehicle luv, MineralSite miningSite) {
        this.luv = luv;
        this.site = miningSite;
        this.minerals = new HashMap<>();

        // Add an entry for eachg detected mineral
        miningSite.getEstimatedMineralAmounts().entrySet().forEach(e -> minerals.put(e.getKey(), new MineralStats(e.getValue()))); 
    }

    @Override
    public String getName() {
        return "Mining";
    }

    /**
     * Get the information on mineral mined so far
     * @return
     */
    public Map<Integer, MineralStats> getMineralStats() {
        return minerals;
    }

    /**
     * Record the amount of resources mined at a specific site. This updates the perResource totals.
     * @param resourceId  ID of the resource collected.
     * @param collected  Amount of resources collected (in kg).
     */
    public void recordResourceCollected(int resourceId, double collected) {
        var found = minerals.get(resourceId);
        found.collected += collected;
    }

    public LightUtilityVehicle getLUV() {
       return luv;
    }

    /**
     * Where is the mining focussed.
     * @return
     */
    public MineralSite getSite() {
        return site;
    }

    /**
     * An amount of mineral has been extracted from the site.
     * Reduce the mass buried at the site,
     * @param mineral
     * @param amount 
     */
    public void extractedMineral(int mineralId, double amount) {
        var found = minerals.get(mineralId);
        found.extracted += amount;

        site.excavateMass(amount);
    }
}
