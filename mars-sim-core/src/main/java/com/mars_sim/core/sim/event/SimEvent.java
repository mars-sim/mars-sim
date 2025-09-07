package org.mars_sim.msp.core.sim.event;

import java.nio.charset.StandardCharsets;

/** Marker interface for inputs/exogenous events that affect sim evolution. */
public sealed interface SimEvent permits UiCommand, InjectedFault {
  long tick();
  String type();

  default byte[] typeBytes() { return type().getBytes(StandardCharsets.UTF_8); }
}
