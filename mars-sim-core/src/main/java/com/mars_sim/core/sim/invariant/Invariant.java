package org.mars_sim.msp.core.sim.invariant;

/** An invariant over sim state; return null if OK, or a message if violated. */
@FunctionalInterface
public interface Invariant<T> {
  String check(T worldSnapshot);
}
