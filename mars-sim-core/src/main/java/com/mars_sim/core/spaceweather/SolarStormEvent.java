/*
 * Mars Simulation Project - Space Weather
 * GPL-3.0-or-later
 */
package org.mars_sim.msp.core.spaceweather;

/** Immutable snapshot of an active solar storm. */
public final class SolarStormEvent {
    private final SpaceWeatherSeverity severity;
    private final double totalDurationMsol;
    private double remainingMsol;

    public SolarStormEvent(SpaceWeatherSeverity severity, double durationMsol) {
        this.severity = severity;
        this.totalDurationMsol = durationMsol;
        this.remainingMsol = durationMsol;
    }

    public SpaceWeatherSeverity getSeverity() { return severity; }
    public double getTotalDurationMsol() { return totalDurationMsol; }
    public double getRemainingMsol() { return remainingMsol; }
    public boolean isFinished() { return remainingMsol <= 0.0; }

    /** Advance time (millisols). */
    public void advance(double deltaMsol) {
        remainingMsol -= deltaMsol;
    }

    /** Multiplier to apply to solar array output. */
    public double solarPowerMultiplier() { return severity.solarPowerMultiplier; }

    /** True if comms blackout is in effect. */
    public boolean commsBlackout() { return severity.commsBlackout; }

    /** Scale factor to apply to ambient radiation dose rate calculations (optional hook). */
    public double radiationDoseScale() { return severity.radiationDoseScale; }

    @Override
    public String toString() {
        return "SolarStormEvent[" + severity + ", remainingMsol=" + Math.max(0.0, remainingMsol) + "]";
    }
}
