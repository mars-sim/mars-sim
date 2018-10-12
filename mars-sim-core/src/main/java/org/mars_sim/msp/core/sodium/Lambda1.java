package org.mars_sim.msp.core.sodium;

/**
 * An interface for 1-argument lambda functions.
 */
public interface Lambda1<A,B> {
    B apply(A a);
}

