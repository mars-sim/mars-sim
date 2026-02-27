// package org.mars_sim.msp.core.sim; // ← adjust to your project package

import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * Single-file simulation kernel:
 * - Deterministic tick-based SimClock with a priority queue
 * - Lightweight synchronous EventBus with priority + registration-ordered delivery
 * - Minimal time types (SolInstant/SolDuration) to avoid bare longs
 *
 * Keep everything here to minimize new files. Split later if desired.
 */
public final class SimClock {

    // ---------- Time types (kept here to avoid extra files) ----------
    /** Instant in simulation ticks (monotonic, integral, deterministic). */
    public record SolInstant(long tick) implements Comparable<SolInstant> {
        @Override public int compareTo(SolInstant o) { return Long.compare(this.tick, o.tick); }
        public SolInstant plus(long ticks) { return new SolInstant(Math.addExact(this.tick, ticks)); }
        public long until(SolInstant other) { return Math.subtractExact(other.tick, this.tick); }
    }

    /** Duration in simulation ticks (domain unit — not wall time). */
    public record SolDuration(long ticks) {
        public static SolDuration ofTicks(long t) { return new SolDuration(t); }
        public long toTicks() { return ticks; }
    }

    // ---------- Clock ----------
    private final long nanosPerTick;
    private final Duration tickDuration;
    private long tick = 0L;
    private final PriorityQueue<ScheduledTask> queue = new PriorityQueue<>();
    private final AtomicLong seq = new AtomicLong(0);
    private final SplittableRandom rng;

    /**
     * @param tickDuration fixed simulation tick duration (wall-time representation)
     * @param seed         RNG seed for deterministic pseudo-randomness
     */
    public SimClock(Duration tickDuration, long seed) {
        if (tickDuration == null || tickDuration.isZero() || tickDuration.isNegative()) {
            throw new IllegalArgumentException("tickDuration must be > 0");
        }
        this.tickDuration = tickDuration;
        this.nanosPerTick = tickDuration.toNanos();
        this.rng = new SplittableRandom(seed);
    }

    /** Advance exactly one tick; executes all callbacks scheduled for <= now. */
    public void tickOnce() {
        tick = Math.addExact(tick, 1);
        while (!queue.isEmpty() && queue.peek().tick <= tick) {
            final var s = queue.poll();
            if (s.cancelled) continue;
            try {
                s.r.run();
            } catch (Throwable t) {
                // Replace with your logger if desired
                System.err.printf("[SimClock] Uncaught task at tick %d (seq %d): %s%n", s.tick, s.seq, t);
                t.printStackTrace(System.err);
            }
        }
    }

    /** Run for N ticks. */
    public void runFor(long ticks) {
        if (ticks < 0) throw new IllegalArgumentException("ticks < 0");
        for (long i = 0; i < ticks; i++) tickOnce();
    }

    /** Run until the given target tick is reached. */
    public void runUntil(long targetTickInclusive) {
        while (tick < targetTickInclusive) tickOnce();
    }

    /** Now as an integral tick. */
    public long nowTick() { return tick; }

    /** Now as a domain instant. */
    public SolInstant now() { return new SolInstant(tick); }

    /** Fixed duration represented by one simulation tick. */
    public Duration tickDuration() { return tickDuration; }

    /** Convert a wall clock Duration to whole ticks (floor). */
    public long toTicks(Duration d) { return Math.max(0, d.toNanos() / nanosPerTick); }

    /** Deterministic RNG (do not reseed internally). */
    public SplittableRandom rng() { return rng; }

    /** Schedule runnable at an absolute target tick (>= now). */
    public ScheduledHandle scheduleAt(long targetTick, Runnable r) {
        Objects.requireNonNull(r, "runnable");
        if (targetTick < tick) throw new IllegalArgumentException("targetTick < now");
        final var s = new ScheduledTask(targetTick, seq.incrementAndGet(), r);
        queue.add(s);
        return () -> s.cancelled = true;
    }

    /** Schedule runnable after delta ticks (>= 0). */
    public ScheduledHandle scheduleIn(long deltaTicks, Runnable r) {
        if (deltaTicks < 0) throw new IllegalArgumentException("deltaTicks < 0");
        return scheduleAt(Math.addExact(tick, deltaTicks), r);
    }

    /**
     * Schedule a periodic task (repeat times total, including the first execution).
     * Deterministic and single-threaded relative to tick progression.
     */
    public ScheduledHandle schedulePeriodic(long startTick, long periodTicks, int repeat, Runnable r) {
        Objects.requireNonNull(r, "runnable");
        if (periodTicks <= 0 || repeat < 1) throw new IllegalArgumentException("periodTicks>0, repeat>=1");

        final class Periodic implements Runnable {
            int remaining = repeat;
            volatile boolean cancelled = false;

            @Override public void run() {
                if (cancelled) return;            // stop chaining if cancelled
                if (remaining-- <= 0) return;     // safety
                r.run();
                if (!cancelled && remaining > 0) {
                    scheduleAt(Math.addExact(nowTick(), Math.subtractExact(periodTicks, 0)), this);
                }
            }
        }
        final var p = new Periodic();
        final var first = scheduleAt(startTick, p);
        return () -> p.cancelled = true; // cancels future chain; already-scheduled next will no-op
    }

    // ---------- Event Bus (nested to keep footprint to one file) ----------
    public static final class EventBus {
        private final Map<Class<?>, List<Handler<?>>> handlers = new HashMap<>();
        private final AtomicLong regSeq = new AtomicLong(0);
        private final SimClock clock; // optional; enables postLater

        public EventBus() { this(null); }
        public EventBus(SimClock clock) { this.clock = clock; }

        /**
         * Subscribe to events of a given type (T) or its subtypes.
         * @return a subscription you can close() to unsubscribe.
         */
        public <T> Subscription subscribe(Class<T> eventType, Consumer<? super T> handler) {
            return subscribe(eventType, handler, 0);
        }

        /**
         * Subscribe with a priority. Higher priority is delivered first at each tick.
         * Tie-breaker is registration order for determinism.
         */
        public <T> Subscription subscribe(Class<T> eventType, Consumer<? super T> handler, int priority) {
            Objects.requireNonNull(eventType, "eventType");
            Objects.requireNonNull(handler, "handler");
            final var reg = new Handler<>(eventType, handler, priority, regSeq.incrementAndGet());

            synchronized (handlers) {
                handlers.computeIfAbsent(eventType, k -> new ArrayList<>()).add(reg);
                handlers.get(eventType).sort(null); // keep deterministic order within this key
            }

            return () -> {
                synchronized (handlers) {
                    final var list = handlers.get(eventType);
                    if (list != null) list.remove(reg);
                }
            };
        }

        /** Synchronously deliver to all matching handlers (type + supertypes/interfaces). */
        public void post(Object event) {
            Objects.requireNonNull(event, "event");
            final List<Handler<?>> callList = new ArrayList<>(8);
            final Class<?> evtClass = event.getClass();

            synchronized (handlers) {
                for (var e : handlers.entrySet()) {
                    if (e.getKey().isAssignableFrom(evtClass)) {
                        callList.addAll(e.getValue());
                    }
                }
            }
            // Handlers may have come from different keys; sort once for (priority desc, seq asc)
            Collections.sort(callList);

            for (Handler<?> h : callList) {
                @SuppressWarnings("unchecked")
                final Consumer<Object> c = (Consumer<Object>) h.consumer;
                try {
                    c.accept(event);
                } catch (Throwable t) {
                    // Replace with your logger if desired
                    System.err.printf(
                        "[EventBus] Handler error for %s (priority=%d, seq=%d): %s%n",
                        evtClass.getSimpleName(), h.priority, h.seq, t
                    );
                    t.printStackTrace(System.err);
                }
            }
        }

        /** Post in the future (by ticks). Requires a clock. */
        public SimClock.ScheduledHandle postLater(long ticksFromNow, Object event) {
            if (clock == null) throw new IllegalStateException("postLater requires an EventBus constructed with SimClock");
            return clock.scheduleIn(ticksFromNow, () -> post(event));
        }

        /** Post at an absolute tick. Requires a clock. */
        public SimClock.ScheduledHandle postAt(long targetTick, Object event) {
            if (clock == null) throw new IllegalStateException("postAt requires an EventBus constructed with SimClock");
            return clock.scheduleAt(targetTick, () -> post(event));
        }

        // ---- support types (kept private & local) ----
        public interface Subscription extends AutoCloseable { void close(); }

        private static final class Handler<T> implements Comparable<Handler<?>> {
            final Class<T> type;
            final Consumer<? super T> consumer;
            final int priority;
            final long seq;
            Handler(Class<T> type, Consumer<? super T> consumer, int priority, long seq) {
                this.type = type; this.consumer = consumer; this.priority = priority; this.seq = seq;
            }
            @Override public int compareTo(Handler<?> o) {
                int byPriority = Integer.compare(o.priority, this.priority); // higher first
                return (byPriority != 0) ? byPriority : Long.compare(this.seq, o.seq); // earlier first
            }
        }
    }

    // ---------- Scheduling support ----------
    @FunctionalInterface
    public interface ScheduledHandle extends AutoCloseable {
        /** Cancel future execution. */
        void close();
        default void cancel() { close(); }
    }

    private static final class ScheduledTask implements Comparable<ScheduledTask> {
        final long tick;
        final long seq;
        final Runnable r;
        boolean cancelled;
        ScheduledTask(long tick, long seq, Runnable r) { this.tick = tick; this.seq = seq; this.r = r; }
        @Override public int compareTo(ScheduledTask o) {
            int byTick = Long.compare(this.tick, o.tick);
            return (byTick != 0) ? byTick : Long.compare(this.seq, o.seq);
        }
    }

    // ---------- Minimal demo (remove or keep as a smoke test) ----------
    public static void main(String[] args) {
        var clock = new SimClock(Duration.ofMillis(50), 42L);
        var bus = new EventBus(clock);

        record ConstructionQueued(String templateId, int x, int y) {}

        // Handlers with priorities — deterministic order per tick
        var subA = bus.subscribe(ConstructionQueued.class,
                e -> System.out.println("[A] " + e.templateId()), 10);
        var subB = bus.subscribe(ConstructionQueued.class,
                e -> System.out.println("[B] at (" + e.x() + "," + e.y() + ")"), 0);

        // Schedule an event at tick 3
        bus.postAt(3, new ConstructionQueued("Hab-Dome", 12, 8));

        // Show periodic tick progression via a simple scheduled task
        clock.schedulePeriodic(1, 2, 3, () -> System.out.println("tick=" + clock.nowTick()));

        clock.runFor(6);

        // cleanup (no-op in this demo)
        subA.close(); subB.close();
    }
}
