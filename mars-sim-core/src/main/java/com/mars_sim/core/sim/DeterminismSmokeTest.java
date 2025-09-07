package org.mars_sim.msp.core.sim;

import org.junit.jupiter.api.Test;
import org.mars_sim.msp.core.sim.clock.SimulationClock;
import org.mars_sim.msp.core.sim.clock.TickRunner;
import org.mars_sim.msp.core.sim.random.RandomService;

import java.util.random.RandomGenerator;
import static org.junit.jupiter.api.Assertions.*;

/** Proves that given same seed/ticks, we get same result in a tiny dummy world. */
public class DeterminismSmokeTest {

  static final class DummyWorld {
    long accum = 0L;
    void step(long tick, RandomGenerator rng) {
      // deterministic per tick
      accum += (tick ^ rng.nextLong());
    }
  }

  private long run(long seed) {
    var clock = new SimulationClock(50);
    var runner = new TickRunner(clock);
    var rng = RandomService.ofSeed(seed).global();
    var world = new DummyWorld();
    runner.runForTicks(200, t -> world.step(t, rng));
    return world.accum;
  }

  @Test
  void deterministicWithSameSeed() {
    assertEquals(run(42L), run(42L));
  }

  @Test
  void differentSeedsDiverge() {
    assertNotEquals(run(1L), run(2L));
  }
}
