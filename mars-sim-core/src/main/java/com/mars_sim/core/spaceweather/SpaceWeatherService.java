/*
 * Mars Simulation Project - Space Weather
 * GPL-3.0-or-later
 */
package org.mars_sim.msp.core.spaceweather;

import java.util.Objects;
import java.util.Random;

/**
 * Models heliophysical "space weather" events that periodically strike the colony.
 * It is intentionally small and deterministic (seeded RNG) so it can be saved/loaded easily.
 *
 * Wire it from Simulation.clockPulse(...) once per pulse:
 *     spaceWeather.advance(deltaMsol);
 */
public final class SpaceWeatherService {

    /** One sol == 1000 msol in Mars-sim notation. */
    public static final double MSOL_PER_SOL = 1000.0;

    private final Random rng;
    private SolarStormEvent active;
    private double solarMultiplier = 1.0;
    private boolean commsBlackout = false;

    /** Accumulator for detecting sol boundaries (optional future use). */
    private double sinceLastCheckMsol = 0.0;

    /** Baseline per-sol chance for a storm to begin (tuned for gameplay). */
    private double baseDailyStormProbability = 0.08; // 8% per sol by default
    /** Weighting of moderate vs severe vs mild events. */
    private double pMild = 0.60, pModerate = 0.30, pSevere = 0.10;

    /** Construct with a seed for deterministic runs (serialize the seed for saves). */
    public SpaceWeatherService(long seed) {
        this.rng = new Random(seed);
        recomputeMultipliers();
    }

    /** Advance the model by delta msol. Call exactly once per clock pulse. */
    public void advance(double deltaMsol) {
        sinceLastCheckMsol += deltaMsol;

        if (active != null && !active.isFinished()) {
            active.advance(deltaMsol);
            if (active.isFinished()) {
                active = null;
                recomputeMultipliers();
            } else {
                // Keep current multipliers.
                return;
            }
        }

        // No active event: roll the dice once per sol in aggregate (cheap and predictable)
        if (sinceLastCheckMsol >= MSOL_PER_SOL) {
            sinceLastCheckMsol -= MSOL_PER_SOL;
            maybeStartNewStorm();
        }
    }

    /** Exposes the current solar power multiplier for arrays. */
    public double getSolarPowerMultiplier() { return solarMultiplier; }

    /** True if long-range communications are blacked out. */
    public boolean isCommsBlackout() { return commsBlackout; }

    /** Returns the active event, or null if clear skies. */
    public SolarStormEvent getActiveEvent() { return active; }

    /** Optional: tweak baseline storm probability at runtime (0..1). */
    public void setBaseDailyStormProbability(double p) {
        this.baseDailyStormProbability = Math.max(0, Math.min(1, p));
    }

    /** Optional: tweak severity distribution. Values are normalized automatically. */
    public void setSeverityWeights(double mild, double moderate, double severe) {
        double sum = mild + moderate + severe;
        if (sum <= 0) {
            return;
        }
        this.pMild = mild / sum;
        this.pModerate = moderate / sum;
        this.pSevere = severe / sum;
    }

    private void maybeStartNewStorm() {
        if (rng.nextDouble() < baseDailyStormProbability) {
            SpaceWeatherSeverity s = sampleSeverity();
            // Duration 0.4–1.6 sols, severity-weighted
            double base = 0.6 + rng.nextDouble() * 0.6; // 0.6-1.2 sols
            double durationSol = switch (s) {
                case MILD -> base * 0.7;
                case MODERATE -> base * 1.0;
                case SEVERE -> base * 1.3;
            };
            active = new SolarStormEvent(s, durationSol * MSOL_PER_SOL);
            recomputeMultipliers();
        }
    }

    private SpaceWeatherSeverity sampleSeverity() {
        double u = rng.nextDouble();
        if (u < pSevere) return SpaceWeatherSeverity.SEVERE;
        if (u < pSevere + pModerate) return SpaceWeatherSeverity.MODERATE;
        return SpaceWeatherSeverity.MILD;
    }

    private void recomputeMultipliers() {
        if (active == null) {
            solarMultiplier = 1.0;
            commsBlackout = false;
        } else {
            solarMultiplier = active.solarPowerMultiplier();
            commsBlackout = active.commsBlackout();
        }
    }

    // --- Save/load helpers (optional) ---
    /** Minimal serialization DTO; implement with the project’s existing save system. */
    public SpaceWeatherState snapshot() {
        return new SpaceWeatherState(
            active == null ? null : active.getSeverity().name(),
            active == null ? 0 : active.getRemainingMsol(),
            baseDailyStormProbability,
            pMild,
            pModerate,
            pSevere
        );
    }

    public void restore(SpaceWeatherState state) {
        Objects.requireNonNull(state, "state");
        if (state.activeSeverity != null) {
            SpaceWeatherSeverity s = SpaceWeatherSeverity.valueOf(state.activeSeverity);
            active = new SolarStormEvent(s, state.remainingMsol);
        } else {
            active = null;
        }
        baseDailyStormProbability = state.baseDailyStormProbability;
        pMild = state.pMild;
        pModerate = state.pModerate;
        pSevere = state.pSevere;
        recomputeMultipliers();
    }

    /** Tiny DTO. Replace with project’s serialization mechanism as needed. */
    public static final class SpaceWeatherState {
        public final String activeSeverity;
        public final double remainingMsol;
        public final double baseDailyStormProbability, pMild, pModerate, pSevere;

        public SpaceWeatherState(
                String activeSeverity,
                double remainingMsol,
                double baseDailyStormProbability,
                double pMild,
                double pModerate,
                double pSevere) {
            this.activeSeverity = activeSeverity;
            this.remainingMsol = remainingMsol;
            this.baseDailyStormProbability = baseDailyStormProbability;
            this.pMild = pMild;
            this.pModerate = pModerate;
            this.pSevere = pSevere;
        }
    }
}
