package org.mars_sim.msp.core.sim.event;

public record InjectedFault(long tick, String fault, String id) implements SimEvent {
  @Override public String type() { return "fault"; }
}
