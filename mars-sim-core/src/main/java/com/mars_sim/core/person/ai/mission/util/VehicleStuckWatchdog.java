/*
 * Mars Simulation Project
 * VehicleStuckWatchdog.java
 * @date 2025-08-24
 * Author: GPT-5 Pro (utility class)
 *
 * A tiny, self-contained watchdog to detect "no progress" while travelling and
 * attempt staged recovery: (1) reroute, (2) skip micro-navpoint, (3) abort & RTB.
 *
 * Usage (example inside a travelling phase):
 *
 *   // As a mission field:
 *   private final VehicleStuckWatchdog.State stuck = new VehicleStuckWatchdog.State("Regolith Prospecting");
 *
 *   // Inside performPhase() when TRAVELLING:
 *   boolean recovered = VehicleStuckWatchdog.checkAndRecover(
 *       stuck,
 *       getVehicleCurrentCoordinates(),        // Coordinates of rover now
 *       getCurrentMissionLocation(),           // Coordinates of target/navpoint
 *       getPhaseDuration(),                    // phase-relative millisols "now"
 *       () -> recalculateRouteToCurrentNavpoint(), // Runnable: rebuild/refresh route
 *       () -> skipCurrentMicroNavpoint(),          // Runnable: advance to next navpoint
 *       () -> abortAndReturnHome(),                // Runnable: end or RTB
 *       getVehicle().getName()                     // tag for logging
 *   );
 *
 * You only need to add this file. Call it where appropriate.
 */

package com.mars_sim.core.person.ai.mission.util;

import java.io.Serializable;
import java.util.concurrent.ThreadLocalRandom;

import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.map.location.Coordinates;

public final class VehicleStuckWatchdog {

    // ---- Tuning knobs (units in comments) ----
    /** Minimum distance improvement between checks to consider "progress" (km). */
    public static final double PROGRESS_EPS_KM = 0.03D; // ~30 m
    /** Time between checks (millisols). */
    public static final double CHECK_INTERVAL_MSOL = 12D; // ~18 min
    /** Max consecutive "no progress" checks before escalating each stage. */
    public static final int NO_PROGRESS_FOR_REROUTE = 1; // first time: reroute
    public static final int NO_PROGRESS_FOR_SKIP    = 2; // second time: skip point
    public static final int NO_PROGRESS_FOR_ABORT   = 3; // third time: abort/RTB
    /** After any recovery action, wait at least this long before re-checking (msol). */
    public static final double COOLDOWN_AFTER_ACTION_MSOL = 20D;

    /** Add small random jitter (msol) to avoid synchronized retries across missions. */
    private static final double JITTER_MSOL = 2.5D;

    private static final SimLogger LOG = SimLogger.getLogger(VehicleStuckWatchdog.class.getName());

    private VehicleStuckWatchdog() {}

    /**
     * State kept per-mission (serializable so missions can be saved/loaded cleanly).
     */
    public static final class State implements Serializable {
        private static final long serialVersionUID = 1L;

        /** For log labeling (mission name, or type). */
        public final String label;

        // progress tracking
        private double lastCheckMillisol = Double.NaN;
        private double lastDistanceKm = Double.NaN;
        private int    noProgressStreak = 0;

        // recovery throttling
        private double nextAllowedCheckMillisol = 0D;
        private int    recoveryStage = 0; // 0=none, 1=rerouted, 2=skipped once

        public State(String label) {
            this.label = (label != null) ? label : "Mission";
        }

        @Override public String toString() {
            return "StuckState[" + label + " stage=" + recoveryStage
                   + " streak=" + noProgressStreak + "]";
        }

        // Accessors for debugging/telemetry if needed
        public int getNoProgressStreak() { return noProgressStreak; }
        public int getRecoveryStage()    { return recoveryStage; }
        public double getLastDistanceKm(){ return lastDistanceKm; }
        public double getLastCheckMillisol(){ return lastCheckMillisol; }
    }

    /**
     * Checks progress and triggers staged recovery actions if needed.
     *
     * @param s        per-mission state
     * @param rover    current rover coordinates (must be non-null)
     * @param target   current target/navpoint coordinates (must be non-null)
     * @param nowMsol  phase-relative time (millisols) or another monotonic clock in msold
     * @param tryReroute        runnable to rebuild path to the same target
     * @param skipMicroNavpoint runnable to skip the current navpoint and move to the next
     * @param abortAndReturn    runnable to abort current travel and return to base
     * @param roverTag          name/id for nicer log lines (vehicle name, etc.)
     * @return true if a recovery action was performed on this call
     */
    public static boolean checkAndRecover(
            State s,
            Coordinates rover,
            Coordinates target,
            double nowMsol,
            Runnable tryReroute,
            Runnable skipMicroNavpoint,
            Runnable abortAndReturn,
            String roverTag
    ) {
        if (s == null || rover == null || target == null) return false;

        // Respect cooldown after a recovery action
        if (nowMsol < s.nextAllowedCheckMillisol) return false;

        // Only sample at the requested cadence
        if (!Double.isNaN(s.lastCheckMillisol)) {
            double since = nowMsol - s.lastCheckMillisol;
            if (since < CHECK_INTERVAL_MSOL) return false;
        }

        // Compute latest distance to the goal
        double distKm = rover.getDistance(target);

        boolean progressed = true;
        if (!Double.isNaN(s.lastDistanceKm)) {
            progressed = (s.lastDistanceKm - distKm) >= PROGRESS_EPS_KM;
        }

        s.lastCheckMillisol = nowMsol;
        s.lastDistanceKm    = distKm;

        if (progressed) {
            if (s.noProgressStreak > 0) {
                LOG.fine(roverTag, "Progress restored (" + fmt(distKm) + " km to goal). Resetting streak.");
            }
            s.noProgressStreak = 0;
            return false;
        }

        // No progress this tick
        s.noProgressStreak++;
        LOG.info(roverTag, "No progress toward target (" + fmt(distKm) + " km). "
                + "Streak=" + s.noProgressStreak + " stage=" + s.recoveryStage
                + " [" + s.label + "]");

        // Stage 1: Reroute
        if (s.noProgressStreak >= NO_PROGRESS_FOR_REROUTE && s.recoveryStage < 1) {
            s.recoveryStage = 1;
            runSafely(tryReroute, roverTag, "reroute");
            armCooldown(s, nowMsol);
            return true;
        }

        // Stage 2: Skip the current micro navpoint (often a malformed or tiny hop)
        if (s.noProgressStreak >= NO_PROGRESS_FOR_SKIP && s.recoveryStage < 2) {
            s.recoveryStage = 2;
            runSafely(skipMicroNavpoint, roverTag, "skip-micro-navpoint");
            armCooldown(s, nowMsol);
            return true;
        }

        // Stage 3: Abort travel and return to base (fail-safe)
        if (s.noProgressStreak >= NO_PROGRESS_FOR_ABORT) {
            s.recoveryStage = 3;
            runSafely(abortAndReturn, roverTag, "abort-and-return");
            armCooldown(s, nowMsol);
            return true;
        }

        return false;
    }

    private static void armCooldown(State s, double nowMsol) {
        double jitter = ThreadLocalRandom.current().nextDouble() * JITTER_MSOL;
        s.nextAllowedCheckMillisol = nowMsol + COOLDOWN_AFTER_ACTION_MSOL + jitter;
    }

    private static void runSafely(Runnable r, String roverTag, String action) {
        if (r == null) return;
        try {
            r.run();
            LOG.info(roverTag, "Watchdog action executed: " + action);
        } catch (Throwable t) {
            LOG.severe(roverTag, 5_000, "Watchdog action '" + action + "' failed: " + t.getMessage());
        }
    }

    private static String fmt(double d) {
        return String.format(java.util.Locale.ROOT, "%.3f", d);
    }
}
