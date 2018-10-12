package org.mars_sim.msp.core.sodium;

/**
 * An interface for event handlers.
 */
public interface Handler<A> {
    void run(A a);
}

