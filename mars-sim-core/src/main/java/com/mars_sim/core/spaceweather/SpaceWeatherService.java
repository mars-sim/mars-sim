/*
 * Mars Simulation Project - Space Weather
 * GPL-3.0-or-later
 */
package org.mars_sim.msp.core.spaceweather;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Objects;
import java.util.Random;

/**
 * Models heliophysical "space weather" events that periodically strike the colony.
 * <p>
 * This service is deterministic when constructed with a fixed RNG seed, which makes
 * save/load straightforward. Call {@link #advance(double)} exactly once per
 * simulation pulse (with the elapsed millisols) and query its public getters for
 * effects such as the current solar power multiplier and comms blackout status.
 * </p>
 *
 * <pre>
 * // Wire from Simulation.clockPulse(...):
 * double deltaMsol = ...;    // elapsed millisols this pulse
 * spaceWeather.advance(deltaMsol);
 * </pre>
 */
public final class SpaceWeatherService implements Serializable {

    private static final long serialVersionUID = 1L;

    /** One sol equals 1000 msol in mars-sim notation. */
    public static final double MSOL_PER_SOL = 1000.0;

    // --- Tuning constants (kept as named constants to satisfy style checks) ---

    /** Default per-sol chance that a storm begins. */
    private static final double DEFAULT_DAILY_STORM_PROBABILITY = 0.08;

    /** Default severity weighting for new storms. */
    private static final double DEFAULT_WEIGHT_MILD = 0.60;
    private static final double DEFAULT_WEIGHT_MODERATE = 0.30;
    private static final double DEFAULT_WEIGHT_SEVERE = 0.10;

    /** Randomized base duration range, in sols: base in [BASE_DURATION_MIN, BASE_DURATION_MIN + BASE_DURATION_RANGE]. */
    private static final double BASE_DURATION_MIN = 0.6;     // sols
    private static final double BASE_DURATION_RANGE = 0.6;   // sols, so 0.6-1.2 total

    /** Per-severity scaling of duration. */
    private static final double MILD_DURATION_SCALE = 0.7;
    private static final double MODERATE_DURATION_SCALE = 1.0;
    private static final double SEVERE_DURATION_SCALE = 1.3;

    /** RNG is transient; we persist and restore it manually for deterministic replay. */
    private transient Random rng;

    /** Active storm is transient; we persist its minimal state manually. */
    private transient SolarStormEvent active;

    private double solarMultiplier = 1.0;
    private boolean commsBlackout = false;

    /** Accumulator for detecting sol boundaries (rolls for storm starts). */
    private double sinceLastCheckMsol = 0.0;

    /** Baseline per-sol chance for a storm to begin (tuned for gameplay). */
    private double baseDailyStormProbability = DEFAULT_DAILY_STORM_PROBABILITY;

    /** Severity weights (mild, moderate, severe); normalized in {@link #setSeverityWeights(double, double, double)}. */
    private double pMild = DEFAULT_WEIGHT_MILD;
    private double pModerate = DEFAULT_WEIGHT_MODERATE;
    private double pSevere = DEFAULT_WEIGHT_SEVERE;

    /**
     * Constructs the service with a deterministic RNG.
     *
     * @param seed random seed used to generate events deterministically
     */
    public SpaceWeatherService(long seed) {
        this.rng = new Random(seed);
        recomputeMultipliers();
    }

    /**
     * Advances the space weather model by the given elapsed time in millisols.
     * <p>Call once per simulation pulse.</p>
     *
     * @param deltaMsol elapsed millisols for this pulse (1000 msol == 1 sol)
     */
    public void advance(double deltaMsol) {
        sinceLastCheckMsol += deltaMsol;

        if (active != null && !active.isFinished()) {
            active.advance(deltaMsol);
            if (active.isFinished()) {
                active = null;
                recomputeMultipliers();
            }
            else {
                // Keep current multipliers and return early if storm still active.
                return;
            }
        }

        // No active event: roll the dice once per sol in aggregate (cheap and predictable).
        if (sinceLastCheckMsol >= MSOL_PER_SOL) {
            sinceLastCheckMsol -= MSOL_PER_SOL;
            maybeStartNewStorm();
        }
    }

    /**
     * Gets the current solar power multiplier to apply to solar array output.
     *
     * @return multiplier in [0, 1], where 1 means no curtailment
     */
    public double getSolarPowerMultiplier() {
        return solarMultiplier;
    }

    /**
     * Indicates whether a long-range communications blackout is in effect.
     *
     * @return {@code true} if comms are blacked out, otherwise {@code false}
     */
    public boolean isCommsBlackout() {
        return commsBlackout;
    }

    /**
     * Returns the currently active storm event, if any.
     *
     * @return the active {@link SolarStormEvent} or {@code null} if clear skies
     */
    public SolarStormEvent getActiveEvent() {
        return active;
    }

    /**
     * Sets the baseline per-sol probability that a new storm begins.
     *
     * @param p probability in [0, 1]
     */
    public void setBaseDailyStormProbability(double p) {
        this.baseDailyStormProbability = Math.max(0.0, Math.min(1.0, p));
    }

    /**
     * Sets the relative weights for sampling storm severity. Values are normalized.
     *
     * @param mild     weight for {@link SpaceWeatherSeverity#MILD}
     * @param moderate weight for {@link SpaceWeatherSeverity#MODERATE}
     * @param severe   weight for {@link SpaceWeatherSeverity#SEVERE}
     */
    public void setSeverityWeights(double mild, double moderate, double severe) {
        double sum = mild + moderate + severe;
        if (sum <= 0.0) {
            return;
        }
        this.pMild = mild / sum;
        this.pModerate = moderate / sum;
        this.pSevere = severe / sum;
    }

    private void maybeStartNewStorm() {
        if (rng.nextDouble() < baseDailyStormProbability) {
            SpaceWeatherSeverity s = sampleSeverity();
            // Duration 0.6-1.2 sols, severity-weighted scaling.
            double base = BASE_DURATION_MIN + rng.nextDouble() * BASE_DURATION_RANGE;
            double durationSol;
            switch (s) {
                case MILD:
                    durationSol = base * MILD_DURATION_SCALE;
                    break;
                case MODERATE:
                    durationSol = base * MODERATE_DURATION_SCALE;
                    break;
                case SEVERE:
                    durationSol = base * SEVERE_DURATION_SCALE;
                    break;
                default:
                    durationSol = base;
                    break;
            }
            active = new SolarStormEvent(s, durationSol * MSOL_PER_SOL);
            recomputeMultipliers();
        }
    }

    private SpaceWeatherSeverity sampleSeverity() {
        double u = rng.nextDouble();
        if (u < pSevere) {
            return SpaceWeatherSeverity.SEVERE;
        }
        if (u < pSevere + pModerate) {
            return SpaceWeatherSeverity.MODERATE;
        }
        return SpaceWeatherSeverity.MILD;
    }

    private void recomputeMultipliers() {
        if (active == null) {
            solarMultiplier = 1.0;
            commsBlackout = false;
        }
        else {
            solarMultiplier = active.solarPowerMultiplier();
            commsBlackout = active.commsBlackout();
        }
    }

    // ---------------------------------------------------------------------
    // Save/load helpers (custom Java serialization to avoid leaking internals)
    // ---------------------------------------------------------------------

    /**
     * Creates a snapshot DTO of the current service state.
     *
     * @return immutable state snapshot suitable for serialization
     */
    public SpaceWeatherState snapshot() {
        return new SpaceWeatherState(
            active == null ? null : active.getSeverity().name(),
            active == null ? 0.0 : active.getRemainingMsol(),
            baseDailyStormProbability,
            pMild,
            pModerate,
            pSevere,
            sinceLastCheckMsol
        );
    }

    /**
     * Restores the service state from a snapshot DTO.
     *
     * @param state previously captured state; must not be null
     * @throws NullPointerException if {@code state} is null
     */
    public void restore(SpaceWeatherState state) {
        Objects.requireNonNull(state, "state");
        if (state.getActiveSeverity() != null) {
            SpaceWeatherSeverity s = SpaceWeatherSeverity.valueOf(state.getActiveSeverity());
            active = new SolarStormEvent(s, state.getRemainingMsol());
        }
        else {
            active = null;
        }
        baseDailyStormProbability = state.getBaseDailyStormProbability();
        pMild = state.getPMild();
        pModerate = state.getPModerate();
        pSevere = state.getPSevere();
        sinceLastCheckMsol = state.getSinceLastCheckMsol();
        recomputeMultipliers();
    }

    /**
     * Custom serialization: write a compact snapshot and the RNG state.
     * We avoid default serialization of transient fields and non-serializable internals.
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        // Write non-transient fields
        out.defaultWriteObject();
        // Write snapshot (covers active storm, weights, probability, accumulator)
        out.writeObject(snapshot());
        // Persist RNG for deterministic replay across save/load
        out.writeObject(rng);
    }

    /**
     * Custom deserialization: read snapshot and RNG, then reconstruct transient fields.
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        Object snapshotObj = in.readObject();
        Object rngObj = in.readObject();

        SpaceWeatherState st = (SpaceWeatherState) snapshotObj;
        rng = (rngObj instanceof Random) ? (Random) rngObj : new Random(0L);

        // Rebuild active event and derived fields
        restore(st);
    }

    /**
     * Immutable DTO for persisting {@link SpaceWeatherService} state.
     * <p>Replace with the project's serialization mechanism as needed.</p>
     */
    public static final class SpaceWeatherState implements Serializable {

        private static final long serialVersionUID = 1L;

        private final String activeSeverity;
        private final double remainingMsol;
        private final double baseDailyStormProbability;
        private final double pMild;
        private final double pModerate;
        private final double pSevere;
        private final double sinceLastCheckMsol;

        /**
         * Creates a new state snapshot.
         *
         * @param activeSeverity             {@code null} if no active storm, otherwise enum name
         * @param remainingMsol              remaining duration of the active storm in millisols
         * @param baseDailyStormProbability  per-sol probability that a storm begins
         * @param pMild                      normalized weight for mild storms
         * @param pModerate                  normalized weight for moderate storms
         * @param pSevere                    normalized weight for severe storms
         * @param sinceLastCheckMsol         accumulator toward next per-sol roll (msol)
         */
        public SpaceWeatherState(
                String activeSeverity,
                double remainingMsol,
                double baseDailyStormProbability,
                double pMild,
                double pModerate,
                double pSevere,
                double sinceLastCheckMsol) {
            this.activeSeverity = activeSeverity;
            this.remainingMsol = remainingMsol;
            this.baseDailyStormProbability = baseDailyStormProbability;
            this.pMild = pMild;
            this.pModerate = pModerate;
            this.pSevere = pSevere;
            this.sinceLastCheckMsol = sinceLastCheckMsol;
        }

        /** @return enum name of the active severity or {@code null} if none */
        public String getActiveSeverity() {
            return activeSeverity;
        }

        /** @return remaining duration in millisols for the active storm */
        public double getRemainingMsol() {
            return remainingMsol;
        }

        /** @return per-sol probability that a storm begins */
        public double getBaseDailyStormProbability() {
            return baseDailyStormProbability;
        }

        /** @return normalized weight for mild storms */
        public double getPMild() {
            return pMild;
        }

        /** @return normalized weight for moderate storms */
        public double getPModerate() {
            return pModerate;
        }

        /** @return normalized weight for severe storms */
        public double getPSevere() {
            return pSevere;
        }

        /** @return accumulator toward next per-sol roll (msol) */
        public double getSinceLastCheckMsol() {
            return sinceLastCheckMsol;
        }
    }
}
