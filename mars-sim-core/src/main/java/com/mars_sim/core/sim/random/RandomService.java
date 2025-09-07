package org.mars_sim.msp.core.sim.random;

import java.util.random.RandomGenerator;

/** Deterministic RNG facade; per-run seed and per-entity substreams. */
public interface RandomService {
  RandomGenerator global();
  RandomGenerator forEntity(long entityId);

  static RandomService ofSeed(long runSeed) {
    return new SplitRandomService(runSeed);
  }
}
