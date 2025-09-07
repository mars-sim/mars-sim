package org.mars_sim.msp.core.sim.event;

import java.util.Map;

public record UiCommand(long tick, String action, Map<String, String> args) implements SimEvent {
  @Override public String type() { return "ui"; }
}
