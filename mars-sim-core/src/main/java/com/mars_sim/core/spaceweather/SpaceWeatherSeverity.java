/*
 * Mars Simulation Project - Space Weather
 * GPL-3.0-or-later
 */
package org.mars_sim.msp.core.spaceweather;

/** Severity tiers for solar storms. */
public enum SpaceWeatherSeverity {
    MILD(0.80, 1.5, false),
    MODERATE(0.50, 2.5, true),
    SEVERE(0.20, 4.0, true);

    /** Multiplier applied to solar power output during the event. */
    public final double solarPowerMultiplier;
    /** Scale factor applied to environmental radiation dose rate (conceptual). */
    public final double radiationDoseScale;
    /** Whether long-range comms are blacked out. */
    public final boolean commsBlackout;

    SpaceWeatherSeverity(double solarPowerMultiplier, double radiationDoseScale, boolean commsBlackout) {
        this.solarPowerMultiplier = solarPowerMultiplier;
        this.radiationDoseScale = radiationDoseScale;
        this.commsBlackout = commsBlackout;
    }
}
