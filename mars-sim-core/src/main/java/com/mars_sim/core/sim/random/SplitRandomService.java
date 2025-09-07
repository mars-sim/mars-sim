package org.mars_sim.msp.core.sim.random;

import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

/** JDK21 RandomGenerator-based service. */
final class SplitRandomService implements RandomService {
  private final long runSeed;
  private final RandomGenerator global;

  SplitRandomService(long runSeed) {
    this.runSeed = runSeed;
    // L64X128MixRandom is high-quality and in JDK >= 17
    this.global = RandomGeneratorFactory.of("L64X128MixRandom").create(runSeed);
  }

  @Override public RandomGenerator global() { return global; }

  @Override public RandomGenerator forEntity(long entityId) {
    long s = mix(runSeed ^ (entityId * 0x9E3779B97F4A7C15L));
    return RandomGeneratorFactory.of("L64X128MixRandom").create(s);
  }

  private static long mix(long z) {
    z ^= (z >>> 33); z *= 0xff51afd7ed558ccdL;
    z ^= (z >>> 33); z *= 0xc4ceb9fe1a85ec53L;
    z ^= (z >>> 33);
    return z;
  }
}
