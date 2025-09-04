/*
 *  Mars Simulation Project – Solar panel dust model
 *  Copyright (C) 2025
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, version 3.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 */
package com.mars_sim.core.building.utility.power;

import java.io.Serializable;
import java.util.Objects;

/**
 * Tracks dust deposition on a building's exposed solar panels and provides an efficiency factor (0..1).
 * Drive it each tick with current optical depth (tau), wind speed (m/s), and dt (millisols or seconds).
 *
 * Assumptions (tunable):
 *  - Deposition scales with atmospheric optical depth (tau) during daylight.
 *  - Strong winds provide partial self-cleaning (gust threshold).
 *  - Manual cleaning reduces dust at a fixed rate per m^2 cleaned.
 */
public class SolarPanelDustComponent implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 0 (clean) .. 1 (opaque) */
    private double dust; 

    /** exposed area of PV panels in m^2 (configuration/Building-dependent) */
    private final double panelAreaM2;

    /** how hard it is to remove dust (per minute), tuned by field tests */
    private double manualCleaningRate = 0.015; // fraction/min at 1 m^2/min throughput

    /** wind speed (m/s) above which dust begins to self-clean */
    private double gustThreshold = 15.0;

    /** self-cleaning strength at high wind (fraction/min at 25 m/s) */
    private double selfCleanMax = 0.003;

    /** Max optical-depth-driven deposition per minute at severe storm */
    private double depMaxPerMin = 0.01;

    public SolarPanelDustComponent(double panelAreaM2) {
        this.panelAreaM2 = Math.max(1.0, panelAreaM2);
    }

    /** 
     * Update dust with current conditions.
     * @param opticalDepth dimensionless tau (e.g., 0.2 clear .. 3.0 storm)
     * @param windSpeedMS  surface wind (m/s)
     * @param solarIrradiance  W/m^2 (0 at night -> zero deposition)
     * @param minutes elapsed minutes
     */
    public void update(double opticalDepth, double windSpeedMS, double solarIrradiance, double minutes) {
        if (minutes <= 0) return;

        // Deposition (daylight only): scales with tau, saturates near depMaxPerMin.
        double daylight = solarIrradiance > 5 ? 1 : 0;
        double depRate = daylight * Math.min(depMaxPerMin, 0.003 + 0.004 * Math.max(0, opticalDepth - 0.2));
        double deposit = depRate * minutes;

        // Self-cleaning for strong winds (gusting)
        double gust = Math.max(0, windSpeedMS - gustThreshold) / 10.0; // 0..~1
        double cleanRate = Math.min(selfCleanMax, gust * selfCleanMax);
        double selfClean = cleanRate * minutes;

        dust = clamp01(dust + deposit - selfClean);
    }

    /**
     * Efficiency multiplier: 1 means clean; 0.2 means 80% power loss.
     * Use a convex curve (dust hurts more at higher levels).
     */
    public double efficiency() {
        // Efficiency = (1 - dust)^k, k>1 makes the drop nonlinear
        double k = 1.6;
        return Math.pow(1.0 - dust, k);
    }

    /** Called by a cleaning task. @param minutesOnBrush minutes spent actively cleaning */
    public void manualClean(double minutesOnBrush, double brushThroughputM2PerMin) {
        double rate = manualCleaningRate * brushThroughputM2PerMin; // fraction/min
        double cleaned = rate * minutesOnBrush;
        dust = clamp01(dust - cleaned);
    }

    public double getDust() { return dust; }
    public void setDust(double dust) { this.dust = clamp01(dust); }
    public double getPanelAreaM2() { return panelAreaM2; }

    private static double clamp01(double x) { return x < 0 ? 0 : (x > 1 ? 1 : x); }

    @Override public String toString() {
        return "SolarPanelDustComponent{dust=" + String.format("%.3f", dust) +
               ", eff=" + String.format("%.3f", efficiency()) +
               ", area=" + panelAreaM2 + "m^2}";
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SolarPanelDustComponent that)) return false;
        return Double.compare(that.dust, dust) == 0 &&
               Double.compare(that.panelAreaM2, panelAreaM2) == 0;
    }
    @Override public int hashCode() { return Objects.hash(dust, panelAreaM2); }
}
