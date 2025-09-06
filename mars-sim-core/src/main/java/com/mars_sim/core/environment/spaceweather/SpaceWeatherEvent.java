package com.mars_sim.core.environment.spaceweather;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A coarse-grained space weather event focused on crew/gameplay impacts.
 * See: mars-sim wiki "Radiation Exposure" (SEP vs GCR). 
 */
public final class SpaceWeatherEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Simplified taxonomy aligned with gameplay. */
    public enum Kind { QUIET, GCR_ELEVATED, SEP_MINOR, SEP_MAJOR }

    /** Qualitative severity for UI filters and thresholds. */
    public enum Severity { NONE, LOW, MODERATE, HIGH, EXTREME }

    private final Kind kind;
    private final Severity severity;
    private final Instant start;
    private final Instant expectedEnd;
    private final String message;

    public SpaceWeatherEvent(Kind kind, Severity severity, Instant start, Instant expectedEnd, String message) {
        this.kind = Objects.requireNonNull(kind);
        this.severity = Objects.requireNonNull(severity);
        this.start = Objects.requireNonNull(start);
        this.expectedEnd = Objects.requireNonNull(expectedEnd);
        this.message = message == null ? "" : message;
    }

    public Kind getKind() { return kind; }
    public Severity getSeverity() { return severity; }
    public Instant getStart() { return start; }
    public Instant getExpectedEnd() { return expectedEnd; }
    public String getMessage() { return message; }

    @Override public String toString() {
        return "SpaceWeatherEvent[" + kind + "," + severity + "," + start + "→" + expectedEnd + "] " + message;
    }
}
