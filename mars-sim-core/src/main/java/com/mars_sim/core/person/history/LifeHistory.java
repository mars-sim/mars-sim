/*
 * Mars Simulation Project
 * LifeHistory.java
 * @date 2025-08-25
 * Implements a unified, time-stamped history of "life events" for a Person
 * (job changes, role changes, training, custom events), with optional bridging
 * into the existing History class via reflection.
 *
 * Related issue: https://github.com/mars-sim/mars-sim/issues/1629
 * The initial requirement is to capture events as strings with timestamps,
 * consolidating separate job/role/training histories into one log. See issue #1629. 
 *
 * Package note: choose a package close to Person. Adjust if your layout differs.
 */
package com.mars_sim.core.person.history;

import java.io.Serial;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.time.MarsTime;

/**
 * LifeHistory is a single source of truth for a Person's life events.
 * <p>
 * It stores events locally and can optionally mirror them into the project's
 * existing {@code History} class using reflection, so we don't need to hard-code
 * the import right now (keeps this a one-file, low-risk change).
 * <p>
 * Events are initially captured as timestamped strings (as requested), grouped
 * by a simple {@link Category}. This can be extended later to a richer model.
 */
public final class LifeHistory implements Serializable {

    @Serial private static final long serialVersionUID = 1L;

    private static final SimLogger log = SimLogger.getLogger(LifeHistory.class.getName());

    /** To avoid unbounded growth; tune as needed. */
    private static final int MAX_EVENTS = 4_096;

    /** Optional sink for mirroring into the project's History class (via reflection). */
    private transient Object historySink; // e.g., com.mars_sim.core.[...].History
    private transient Method historyAddMethod; // expected signature: add(MarsTime, String)

    /** Ring buffer of events (most recent last). */
    private final Deque<Entry> events = new ArrayDeque<>(256);

    /** A short label (e.g., person's name or ID) to prefix messages (optional). */
    private final String ownerLabel;

    /** Types of life events we care about (expand later without breaking API). */
    public enum Category {
        JOB, ROLE, TRAINING, STATUS, LOCATION, HEALTH, CUSTOM
    }

    /** Immutable entry object returned to callers. */
    public static final class Entry implements Serializable {
        @Serial private static final long serialVersionUID = 1L;
        private final MarsTime when;
        private final Category category;
        private final String message;

        public Entry(MarsTime when, Category category, String message) {
            this.when = when;
            this.category = category;
            this.message = message;
        }
        public MarsTime getWhen() { return when; }
        public Category getCategory() { return category; }
        public String getMessage() { return message; }

        @Override public String toString() {
            return "[" + when + "] " + category + " - " + message;
        }
    }

    // --------------------------------------------------------------------------------------------
    // Construction & optional History-bridge (reflection)
    // --------------------------------------------------------------------------------------------

    /**
     * Create a LifeHistory with an optional label to prefix messages (can be null).
     * Example label: the person's display name.
     */
    public LifeHistory(String ownerLabel) {
        this.ownerLabel = ownerLabel;
    }

    /** No-label convenience constructor. */
    public LifeHistory() {
        this(null);
    }

    /**
     * Attach an existing History instance to mirror entries into it.
     * <p>
     * We use reflection to find a method {@code add(MarsTime, String)} on the given
     * object. If found, all subsequent events are also forwarded to that method.
     * <p>
     * This lets us satisfy issue #1629's "use the existing History" goal without
     * taking a hard compile-time dependency on the History class/package today.
     *
     * @param history An instance of the project's History class
     * @return true if a suitable add(MarsTime, String) method was discovered
     */
    public boolean attachHistorySink(Object history) {
        if (history == null) {
            this.historySink = null;
            this.historyAddMethod = null;
            return false;
        }
        try {
            Method m = history.getClass().getMethod("add", MarsTime.class, String.class);
            this.historySink = history;
            this.historyAddMethod = m;
            return true;
        } catch (NoSuchMethodException e) {
            log.warning("LifeHistory: provided history sink has no method add(MarsTime, String). Not attaching.");
            this.historySink = null;
            this.historyAddMethod = null;
            return false;
        } catch (Throwable t) {
            log.warning("LifeHistory: failed to attach history sink: " + t);
            this.historySink = null;
            this.historyAddMethod = null;
            return false;
        }
    }

    // --------------------------------------------------------------------------------------------
    // Public API to add events (string-based payloads, as requested in #1629)
    // --------------------------------------------------------------------------------------------

    public void addJobChange(MarsTime when, String fromJob, String toJob) {
        String msg = fmtOwner() + "Job changed: " + nullToDash(fromJob) + " → " + nullToDash(toJob);
        add(when, Category.JOB, msg);
    }

    public void addRoleChange(MarsTime when, String fromRole, String toRole) {
        String msg = fmtOwner() + "Role changed: " + nullToDash(fromRole) + " → " + nullToDash(toRole);
        add(when, Category.ROLE, msg);
    }

    public void addTraining(MarsTime when, String trainingName) {
        String msg = fmtOwner() + "Training: " + nullToDash(trainingName);
        add(when, Category.TRAINING, msg);
    }

    public void addStatus(MarsTime when, String status) {
        String msg = fmtOwner() + "Status: " + nullToDash(status);
        add(when, Category.STATUS, msg);
    }

    public void addLocation(MarsTime when, String locationSummary) {
        String msg = fmtOwner() + "Location: " + nullToDash(locationSummary);
        add(when, Category.LOCATION, msg);
    }

    public void addHealth(MarsTime when, String healthSummary) {
        String msg = fmtOwner() + "Health: " + nullToDash(healthSummary);
        add(when, Category.HEALTH, msg);
    }

    /**
     * Generic custom event.
     */
    public void addCustom(MarsTime when, String message) {
        String msg = fmtOwner() + nullToDash(message);
        add(when, Category.CUSTOM, msg);
    }

    // --------------------------------------------------------------------------------------------
    // Query API
    // --------------------------------------------------------------------------------------------

    /** All events, oldest → newest (defensive copy). */
    public List<Entry> getAll() {
        return Collections.unmodifiableList(new ArrayList<>(events));
    }

    /** Most recent N events (0 → empty). */
    public List<Entry> getRecent(int n) {
        if (n <= 0) return List.of();
        int size = events.size();
        int from = Math.max(0, size - n);
        List<Entry> all = new ArrayList<>(events);
        return Collections.unmodifiableList(all.subList(from, size));
    }

    /** Filter by category, oldest → newest. */
    public List<Entry> getByCategory(Category c) {
        if (c == null) return List.of();
        List<Entry> out = new ArrayList<>();
        for (Entry e : events) if (e.category == c) out.add(e);
        return Collections.unmodifiableList(out);
    }

    // --------------------------------------------------------------------------------------------
    // Internals
    // --------------------------------------------------------------------------------------------

    private void add(MarsTime when, Category category, String message) {
        Objects.requireNonNull(when, "when");
        Objects.requireNonNull(category, "category");
        Objects.requireNonNull(message, "message");

        Entry e = new Entry(when, category, message);
        // enforce ring buffer limit
        while (events.size() >= MAX_EVENTS) {
            events.pollFirst();
        }
        events.addLast(e);

        // Mirror into the project's History class if attached
        if (historySink != null && historyAddMethod != null) {
            try {
                historyAddMethod.invoke(historySink, when, e.toString());
            } catch (Throwable t) {
                // Don't fail the sim if the sink misbehaves.
                log.fine("LifeHistory: failed to forward to History sink: " + t);
            }
        }
    }

    private String fmtOwner() {
        return (ownerLabel == null || ownerLabel.isBlank()) ? "" : ("[" + ownerLabel + "] ");
    }

    private static String nullToDash(String s) {
        return (s == null || s.isBlank()) ? "-" : s;
    }
}
