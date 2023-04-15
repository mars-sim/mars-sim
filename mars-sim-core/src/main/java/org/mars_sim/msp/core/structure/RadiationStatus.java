/*
 * Mars Simulation Project
 * RadiationStatus.java
 * @date 2022-11-09
 * @author Barry Evans
 */
package org.mars_sim.msp.core.structure;

import java.io.Serializable;

import org.mars_sim.msp.core.person.health.RadiationExposure;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * Represents the current status of pending Radiation events.
 */
public class RadiationStatus implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
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
     * Calculate the current status of pending radiation events for a period of time.
     */
    public static RadiationStatus calculateCurrent(double time) {
        double ratio = time / RadiationExposure.RADIATION_CHECK_FREQ;
		double magVariation1 = 1 + RandomUtil.getRandomDouble(-RadiationExposure.GCR_CHANCE_SWING, RadiationExposure.GCR_CHANCE_SWING);
		if (magVariation1 < 0)
			magVariation1 = 0;
		double magVariation2 = 1 + RandomUtil.getRandomDouble(- RadiationExposure.SEP_CHANCE_SWING, RadiationExposure.SEP_CHANCE_SWING);
		if (magVariation2 < 0)
			magVariation2 = 0;

		// Galactic cosmic rays (GCRs) event // average 1.22% per 1000 millisols
		double chance1 = (1.22/1000 + RadiationExposure.GCR_PERCENT * ratio * magVariation1) / 2.0; 
		// Solar energetic particles (SEPs) event // average 0.122 % per 1000 millisols
		double chance2 = (0.122/1000 + RadiationExposure.SEP_PERCENT * ratio * magVariation2) / 2.0; 
		// Baseline radiation event
		double chance0 = (3.53/1000 + .1 - chance1 - chance2) / 2.0; // average 3.53% per 1000 millisols

		// Note that RadiationExposure.BASELINE_PERCENT * ratio * (variation1 + variation2);
        boolean baseLine = (chance0 > 0) && RandomUtil.lessThanRandPercent(chance0);
		
		// Galactic cosmic rays (GCRs) event
		// double rand2 = Math.round(RandomUtil.getRandomDouble(100) * 100.0)/100.0;
		boolean sep = RandomUtil.lessThanRandPercent(chance1);

		// ~ 300 milli Sieverts for a 500-day mission
		// Solar energetic particles (SEPs) event
		boolean gcr = RandomUtil.lessThanRandPercent(chance2);

        return new RadiationStatus(baseLine, gcr, sep);
    }
}
