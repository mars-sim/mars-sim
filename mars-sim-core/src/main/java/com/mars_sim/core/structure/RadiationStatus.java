/*
 * Mars Simulation Project
 * RadiationStatus.java
 * @date 2023-11-05
 * @author Barry Evans
 */
package com.mars_sim.core.structure;

import java.io.Serializable;

import com.mars_sim.core.person.health.RadiationExposure;
import com.mars_sim.tools.util.RandomUtil;

/**
 * Represents the current status of pending Radiation events.
 */
public class RadiationStatus implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/** default logger. */
//	May add back private static final SimLogger logger = SimLogger.getLogger(RadiationStatus.class.getName())

    private boolean baselineEvent;
    private boolean gcrEvent;
    private boolean sepEvent;

    public RadiationStatus(boolean baselineEvent, boolean gcrEvent, boolean sepEvent) {
        this.baselineEvent = baselineEvent;
        this.gcrEvent = gcrEvent;
        this.sepEvent = sepEvent;
    }

    public boolean isBaselineEvent() {
        return baselineEvent;
    }

    public boolean isGCREvent() {
        return gcrEvent;
    }

    public boolean isSEPEvent() {
        return sepEvent;
    }

    /**
     * Calculates the current status of pending radiation events for a period of time.
     * 
     * @param time
     * @return
     */
    public static RadiationStatus calculateChance(double time) {
    	if (time == 0)
    		return new RadiationStatus(false, false, false);
    	
        double ratio = time * RadiationExposure.RADIATION_CHECK_FREQ;
		double var1 = 1 + RandomUtil.getRandomDouble(-RadiationExposure.GCR_CHANCE_SWING, RadiationExposure.GCR_CHANCE_SWING);
		if (var1 < 0)
			var1 = 0;
		double var2 = 1 + RandomUtil.getRandomDouble(-RadiationExposure.SEP_CHANCE_SWING, RadiationExposure.SEP_CHANCE_SWING);
		if (var2 < 0)
			var2 = 0;

		// Galactic cosmic rays (GCRs) event // average 1.22% per 1000 millisols
		double chance1 = Math.min(ratio * 1.22/1000 * time, RadiationExposure.GCR_PERCENT * var1);
//		logger.info("chance1: " + chance1);
		// Solar energetic particles (SEPs) event // average 0.122 % per 1000 millisols
		double chance2 = Math.min(ratio * 0.122/1000 * time, RadiationExposure.SEP_PERCENT * var2); 
//		logger.info("chance2: " + chance2);
		// Baseline radiation event // average 3.53% per 1000 millisols
		double chance0 = Math.max(0, (ratio * 3.53/1000 * time - chance1 - chance2)); 
//		if (chance0 < 0) chance0 = 0;
//		logger.info("chance0: " + chance0);
		
		// Note that RadiationExposure.BASELINE_PERCENT * ratio * (variation1 + variation2);
        boolean baseline = RandomUtil.lessThanRandPercent(chance0);
		
		// Galactic cosmic rays (GCRs) event
		boolean gcr = RandomUtil.lessThanRandPercent(chance1);
		
		// ~300 milli Sieverts for a 500-day mission
		// Solar energetic particles (SEPs) event
		boolean sep = RandomUtil.lessThanRandPercent(chance2);

        return new RadiationStatus(baseline, gcr, sep);
    }
}
