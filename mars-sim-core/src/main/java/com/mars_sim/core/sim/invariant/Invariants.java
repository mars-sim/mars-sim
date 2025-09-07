package org.mars_sim.msp.core.sim.invariant;

import java.util.ArrayList;
import java.util.List;

public final class Invariants<T> {
  private final List<Invariant<T>> checks = new ArrayList<>();
  public Invariants<T> add(Invariant<T> i) { checks.add(i); return this; }
  public List<String> violations(T worldSnapshot) {
    return checks.stream().map(i -> i.check(worldSnapshot)).filter(msg -> msg != null).toList();
  }
}
