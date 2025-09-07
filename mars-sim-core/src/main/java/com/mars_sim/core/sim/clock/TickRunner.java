package org.mars_sim.msp.core.sim.clock;

import java.util.function.LongConsumer;

/** Drives the simulation at a fixed number of ticks. */
public final class TickRunner {
  private final SimulationClock clock;

  public TickRunner(SimulationClock clock) { this.clock = clock; }

  /** Run for N ticks, calling step.accept(currentTick) each tick. */
  public void runForTicks(long ticks, LongConsumer step) {
    if (ticks < 0) throw new IllegalArgumentException("ticks must be >= 0");
    for (long i = 0; i < ticks; i++) {
      step.accept(clock.tick());
      clock.advance();
    }
  }
}
