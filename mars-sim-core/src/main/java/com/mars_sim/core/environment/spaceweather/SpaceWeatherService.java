package com.mars_sim.core.environment.spaceweather;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Lightweight stochastic generator for space-weather events (SEP/GCR) with alerting.
 * - SEP events temporarily halt EVA (by policy) and suggest shelter-in-place drills.
 * - GCR elevated periods are long/low-grade; gameplay may apply mild penalties only.
 *
 * Default rates are conservative and can be tuned later or driven by data.
 */
public final class SpaceWeatherService {

    private final Random rng;
    private final List<SpaceWeatherListener> listeners = new ArrayList<>();
    private SpaceWeatherEvent current = new SpaceWeatherEvent(
            SpaceWeatherEvent.Kind.QUIET, SpaceWeatherEvent.Severity.NONE,
            Instant.EPOCH, Instant.EPOCH, "Initialized");

    /** Mean sol-length; service is unit-agnostic but sol scale is intuitive in Mars-Sim. */
    private static final Duration SOL = Duration.ofSeconds(88775); // 24h 39m 35s

    /** Tunables (rough placeholders) */
    private double dailySepMinorChance = 0.002; // ~0.2% per sol
    private double dailySepMajorChance = 0.0003; // ~0.03% per sol
    private double dailyGcrElevatedChance = 0.02; // ~2% per sol

    public SpaceWeatherService() {
        this(new SecureRandom());
    }

    public SpaceWeatherService(Random rng) {
        this.rng = rng;
    }

    public void addListener(SpaceWeatherListener l) { if (l != null) listeners.add(l); }
    public SpaceWeatherEvent getCurrentEvent() { return current; }

    /**
     * Advance the generator by dt, possibly emitting a new event when the prior event ends.
     * Call this once per clock pulse with the absolute time and delta time.
     */
    public void tick(Instant now, Duration dt) {
        if (now.isBefore(current.getExpectedEnd())) return; // event still active

        // Event window ended → sample a new state for the next window (1 sol granularity)
        SpaceWeatherEvent next = sampleEvent(now);
        if (next.getKind() != current.getKind() || next.getSeverity() != current.getSeverity()) {
            current = next;
            for (SpaceWeatherListener l : listeners) l.onSpaceWeather(current);
        } else {
            current = next; // silent refresh
        }
    }

    private SpaceWeatherEvent sampleEvent(Instant now) {
        double p = rng.nextDouble();

        // Order: rarest first
        if (p < dailySepMajorChance) {
            return new SpaceWeatherEvent(
                    SpaceWeatherEvent.Kind.SEP_MAJOR,
                    SpaceWeatherEvent.Severity.EXTREME,
                    now, now.plus(SOL.multipliedBy(1 + rng.nextInt(2))),
                    "Major SEP: Suspend EVA; shelter occupants; protect electronics.");
        }
        p -= dailySepMajorChance;

        if (p < dailySepMinorChance) {
            return new SpaceWeatherEvent(
                    SpaceWeatherEvent.Kind.SEP_MINOR,
                    SpaceWeatherEvent.Severity.HIGH,
                    now, now.plus(SOL), "Minor SEP: Minimize EVA; consider drills.");
        }
        p -= dailySepMinorChance;

        if (p < dailyGcrElevatedChance) {
            return new SpaceWeatherEvent(
                    SpaceWeatherEvent.Kind.GCR_ELEVATED,
                    SpaceWeatherEvent.Severity.LOW,
                    now, now.plus(SOL.multipliedBy(5 + rng.nextInt(10))),
                    "GCR elevated: background dose slightly higher.");
        }

        return new SpaceWeatherEvent(
                SpaceWeatherEvent.Kind.QUIET, SpaceWeatherEvent.Severity.NONE,
                now, now.plus(SOL), "Quiet Sun");
    }

    // Optional tuners
    public void setDailySepMinorChance(double v) { dailySepMinorChance = clamp01(v); }
    public void setDailySepMajorChance(double v) { dailySepMajorChance = clamp01(v); }
    public void setDailyGcrElevatedChance(double v) { dailyGcrElevatedChance = clamp01(v); }

    private static double clamp01(double v) { return Math.max(0.0, Math.min(1.0, v)); }
}
