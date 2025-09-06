package org.mars_sim.msp.core.sim.clock;

/** Fixed-timestep simulation clock. */
public final class SimulationClock {
  private final int tickMillis;  // e.g., 50
  private long tick;

  public SimulationClock(int tickMillis) {
    if (tickMillis <= 0) throw new IllegalArgumentException("tickMillis must be > 0");
    this.tickMillis = tickMillis;
    this.tick = 0L;
  }

  public long tick() { return tick; }
  public int tickMillis() { return tickMillis; }
  public long toMillis(long t) { return t * tickMillis; }
  public long fromMillis(long millis) { return millis / tickMillis; }

  /** Advance by exactly one tick. */
  public void advance() { tick++; }
}
