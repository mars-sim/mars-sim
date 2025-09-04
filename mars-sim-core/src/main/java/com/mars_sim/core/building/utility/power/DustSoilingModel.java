/*
 * Mars Simulation Project
 * DustSoilingModel.java
 * A simple, pluggable dust attenuation model for solar power generation on Mars.
 *
 * Combines two effects into a single multiplicative factor in [0,1]:
 *   1) Atmospheric dust opacity (tau) -> Beer–Lambert attenuation ~ exp(-tau / mu)
 *   2) Panel soiling coverage fraction (0..1) -> linear transmittance (1 - soiling)
 *
 * Defaults align with published mission data trends (e.g., ~0.2%/sol soiling w/o cleaning events),
 * and allow wind “cleaning events” to partially restore panels. Use setters to tailor per site.
 *
 * Usage:
 *   DustSoilingModel model = new DustSoilingModel();
 *   model.setAtmosphericTau(currentTau);  // from weather if available
 *   model.advanceBySols(elapsedSols, isStorming, 5.0 /* storm multiplier */);
 *   powerGeneration.setDustEfficiencySupplier(model); // model implements DoubleSupplier
 */
package com.mars_sim.core.power;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.DoubleSupplier;

public final class DustSoilingModel implements DoubleSupplier {

    /** Fraction of panel area covered by dust [0,1]. 0 = clean; 1 = fully obscured. */
    private double panelSoiling = 0.0;

    /** Atmospheric dust optical depth (tau >= 0). */
    private double atmosphericTau = 0.0;

    /** Cosine of mean solar zenith used in Beer–Lambert; 0.3–0.7 reasonable. Default 0.5. */
    private double meanCosZenith = 0.5;

    /** Base daily deposition rate (fraction of panel area per sol). Default ~0.2%/sol. */
    private double baseDailyDeposit = 0.002;

    /** Maximum practical soiling cap before model stops accumulating (e.g., 60%). */
    private double maxSoiling = 0.60;

    /** Typical fraction removed during a “cleaning event” (wind/dust‑devil); 0.0..1.0. */
    private double cleaningEventMean = 0.20;

    /** Randomness of cleaning events (stddev in fraction removed). */
    private double cleaningEventStd = 0.05;

    /** Probability of a cleaning event per sol (background, non‑storm season). */
    private double cleaningEventProbPerSol = 0.01;

    /** During active storms, raise deposition and reduce cleaning probability. */
    private double stormCleaningProbPerSol = 0.002;

    public DustSoilingModel() {}

    /** Returns the current multiplicative efficiency factor in [0,1]. */
    @Override
    public double getAsDouble() {
        // Atmospheric transmission via Beer–Lambert with an effective airmass (1/mu)
        double mu = clamp(meanCosZenith, 0.1, 1.0);
        double atmTrans = Math.exp(-clamp(atmosphericTau, 0.0, 10.0) / mu);
        // Panel transmittance reduced linearly by soiling coverage
        double panelTrans = 1.0 - clamp(panelSoiling, 0.0, 1.0);
        double eff = atmTrans * panelTrans;
        return clamp(eff, 0.0, 1.0);
    }

    /** Advance the soiling state by {@code elapsedSols}. Optionally scale deposition during storms. */
    public void advanceBySols(double elapsedSols, boolean stormActive, double stormDepositMultiplier) {
        if (elapsedSols <= 0) return;

        // 1) Accumulate soiling
        double depositRate = baseDailyDeposit * (stormActive ? Math.max(1.0, stormDepositMultiplier) : 1.0);
        panelSoiling = clamp(panelSoiling + depositRate * elapsedSols, 0.0, maxSoiling);

        // 2) Random chance of a cleaning event (wind/dust devils)
        double p = (stormActive ? stormCleaningProbPerSol : cleaningEventProbPerSol) * elapsedSols;
        if (ThreadLocalRandom.current().nextDouble() < clamp(p, 0.0, 1.0)) {
            double removed = gaussian(cleaningEventMean, cleaningEventStd);
            cleanFraction(removed);
        }
    }

    /** Apply a cleaning event that removes {@code fraction} of current dust (0..1). */
    public void cleanFraction(double fraction) {
        double f = clamp(fraction, 0.0, 1.0);
        panelSoiling = clamp(panelSoiling * (1.0 - f), 0.0, maxSoiling);
    }

    // ----------------------- setters for tuning/site-specific wiring -----------------------

    public DustSoilingModel setAtmosphericTau(double tau) {
        this.atmosphericTau = Math.max(0.0, tau);
        return this;
    }

    public DustSoilingModel setMeanCosZenith(double mu) {
        this.meanCosZenith = clamp(mu, 0.1, 1.0);
        return this;
    }

    public DustSoilingModel setBaseDailyDeposit(double fractionPerSol) {
        this.baseDailyDeposit = clamp(fractionPerSol, 0.0, 1.0);
        return this;
    }

    public DustSoilingModel setMaxSoiling(double fraction) {
        this.maxSoiling = clamp(fraction, 0.0, 1.0);
        return this;
    }

    public DustSoilingModel setCleaningEventMean(double fraction) {
        this.cleaningEventMean = clamp(fraction, 0.0, 1.0);
        return this;
    }

    public DustSoilingModel setCleaningEventStd(double fraction) {
        this.cleaningEventStd = clamp(fraction, 0.0, 1.0);
        return this;
    }

    public DustSoilingModel setCleaningEventProbPerSol(double prob) {
        this.cleaningEventProbPerSol = clamp(prob, 0.0, 1.0);
        return this;
    }

    public DustSoilingModel setStormCleaningProbPerSol(double prob) {
        this.stormCleaningProbPerSol = clamp(prob, 0.0, 1.0);
        return this;
    }

    public double getPanelSoiling() { return panelSoiling; }
    public double getAtmosphericTau() { return atmosphericTau; }

    // ----------------------- helpers -----------------------
    private static double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    private static double gaussian(double mean, double std) {
        // Box–Muller
        double u1 = 1.0 - ThreadLocalRandom.current().nextDouble();
        double u2 = 1.0 - ThreadLocalRandom.current().nextDouble();
        double z = Math.sqrt(-2.0 * Math.log(u1)) * Math.cos(2.0 * Math.PI * u2);
        return Math.max(0.0, mean + std * z);
    }
}
