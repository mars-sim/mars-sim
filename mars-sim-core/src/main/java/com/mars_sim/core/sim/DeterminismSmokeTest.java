package com.mars_sim.core.sim;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.mars_sim.core.sim.clock.SimulationClock;
import com.mars_sim.core.sim.clock.TickRunner;
import com.mars_sim.core.sim.random.RandomService;

import java.util.random.RandomGenerator;

/**
 * Smoke test: given the same seed + number of ticks, results should be identical.
 * NOTE: This is a dummy-world test to validate the deterministic plumbing only.
 */
public class DeterminismSmokeTest {

  static final class DummyWorld {
    long accum = 0L;
    void step(long tick, RandomGenerator rng) {
      // deterministic contribution per tick for the smoke test
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
