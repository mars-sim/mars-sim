// package org.mars_sim.msp.core.construction;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Minimal Construction MVP manager:
 * - FIFO queue of construction orders
 * - Tile reservation to prevent double-booking
 * - Deterministic IDs and event emission
 * - No threads; intended to be called from the simulation tick thread
 *
 * Integrates with SimClock + EventBus (from your earlier skeleton).
 */
public final class ConstructionManager {

    /** Validate that a template (blueprint) is known/allowed. Replace with your real registry later. */
    @FunctionalInterface
    public interface BlueprintValidator { boolean isValidTemplateId(String templateId); }

    // --- Dependencies ---
    private final SimClock clock;
    private final SimClock.EventBus bus;
    private final BlueprintValidator validator;

    // --- State (single-threaded; sim tick owns it) ---
    private final AtomicLong seq = new AtomicLong(0);
    private final ArrayDeque<Order> queue = new ArrayDeque<>();
    private final Map<ConstructionId, Order> inProgress = new HashMap<>();
    private final Set<GridKey> reserved = new HashSet<>();

    public ConstructionManager(SimClock clock, SimClock.EventBus bus, BlueprintValidator validator) {
        this.clock = Objects.requireNonNull(clock, "clock");
        this.bus   = Objects.requireNonNull(bus, "bus");
        this.validator = Objects.requireNonNull(validator, "validator");
    }

    // --- Public API types (kept nested to minimize new files) ---
    /** Stable identity for a construction. */
    public record ConstructionId(long value) { @Override public String toString() { return "C" + value; } }
    /** Integer grid coordinates; adapt to your world coords if needed. */
    public record GridKey(int x, int y) {}
    /** Immutable order descriptor. */
    public record Order(ConstructionId id, String templateId, GridKey position, long enqueuedTick, String requestedBy) {}

    // --- Events (simple POJOs for the EventBus) ---
    public record ConstructionQueued(Order order) {}
    public record ConstructionStarted(Order order) {}
    public record ConstructionCompleted(ConstructionId id) {}
    public record ConstructionCancelled(ConstructionId id, String reason) {}

    /**
     * Enqueue a new construction order.
     * - Validates templateId via the provided validator.
     * - Reserves the target tile immediately to prevent duplicates.
     * - Emits ConstructionQueued.
     */
    public Order enqueue(String templateId, int x, int y, String requestedBy) {
        Objects.requireNonNull(templateId, "templateId");
        if (!validator.isValidTemplateId(templateId)) {
            throw new IllegalArgumentException("Unknown blueprint/templateId: " + templateId);
        }
        final GridKey key = new GridKey(x, y);
        if (reserved.contains(key)) {
            throw new IllegalStateException("Position already reserved by another construction: " + key);
        }

        final ConstructionId id = new ConstructionId(seq.incrementAndGet());
        final Order order = new Order(id, templateId, key, clock.nowTick(), requestedBy);
        queue.addLast(order);
        reserved.add(key);
        bus.post(new ConstructionQueued(order));
        return order;
    }

    /**
     * Pops the next order (FIFO) and marks it in-progress.
     * Keeps the tile reserved; call complete(id) to release it when done.
     */
    public Optional<Order> pullNextToStart() {
        final Order o = queue.pollFirst();
        if (o == null) return Optional.empty();
        inProgress.put(o.id(), o);
        bus.post(new ConstructionStarted(o));
        return Optional.of(o);
    }

    /** Marks a construction as completed and releases its reservation; emits ConstructionCompleted. */
    public boolean complete(ConstructionId id) {
        final Order o = inProgress.remove(Objects.requireNonNull(id, "id"));
        if (o == null) return false;
        reserved.remove(o.position());
        bus.post(new ConstructionCompleted(id));
        return true;
    }

    /**
     * Cancels a queued or in-progress order; releases the reservation and emits ConstructionCancelled.
     * Returns true if something was cancelled.
     */
    public boolean cancel(ConstructionId id, String reason) {
        Objects.requireNonNull(id, "id");
        // Try to remove from queue (cheap)
        for (var it = queue.iterator(); it.hasNext(); ) {
            final Order o = it.next();
            if (o.id().equals(id)) {
                it.remove();
                reserved.remove(o.position());
                bus.post(new ConstructionCancelled(id, reason == null ? "cancelled" : reason));
                return true;
            }
        }
        // Or in-progress
        final Order o = inProgress.remove(id);
        if (o != null) {
            reserved.remove(o.position());
            bus.post(new ConstructionCancelled(id, reason == null ? "cancelled" : reason));
            return true;
        }
        return false;
    }

    /** Snapshot the FIFO queue for UI/debug. */
    public List<Order> listQueue() { return List.copyOf(queue); }

    /** Quick check for UI: is a grid cell currently blocked by construction? */
    public boolean isReserved(int x, int y) { return reserved.contains(new GridKey(x, y)); }

    /** Wipes all state (tests). */
    public void clearAll() { queue.clear(); inProgress.clear(); reserved.clear(); }
}
