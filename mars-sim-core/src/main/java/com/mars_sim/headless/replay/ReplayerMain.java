package org.mars_sim.msp.headless.replay;

import org.mars_sim.msp.core.sim.clock.SimulationClock;
import org.mars_sim.msp.core.sim.clock.TickRunner;
import org.mars_sim.msp.core.sim.event.*;
import org.mars_sim.msp.core.sim.invariant.Invariants;
import org.mars_sim.msp.core.sim.random.RandomService;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * Headless journal/replay launcher.
 * NOTE: This uses a dummy world now. TODO(mars-sim): wire to real sim tick.
 */
public final class ReplayerMain {

  interface WorldFacade {
    void init(long seed);
    /** advance one tick and optionally consume external events of this tick */
    void step(long tick, List<SimEvent> eventsForTick);
    /** returns a snapshot object for invariants (can be a lightweight DTO) */
    Object snapshot();
  }

  // Minimal dummy world so the skeleton runs.
  static final class DummyWorld implements WorldFacade {
    long sum = 0L;
    @Override public void init(long seed) { sum = seed; }
    @Override public void step(long tick, List<SimEvent> eventsForTick) { sum += tick + eventsForTick.size(); }
    @Override public Object snapshot() { return sum; }
  }

  public static void main(String[] args) throws Exception {
    var params = parseArgs(args);
    if (params.help) { printHelp(); return; }

    long seed = params.seed;
    int tickMillis = params.tickMillis;
    long ticks = params.ticks;
    Path journal = params.journal;

    var clock = new SimulationClock(tickMillis);
    var runner = new TickRunner(clock);
    var rng = RandomService.ofSeed(seed);
    var invariants = new Invariants<Object>()
        .add(s -> null); // add real invariants later

    try (var jw = new JournalWriter(new FileOutputStream(journal.toFile()),
        new JournalHeader(seed, tickMillis))) {

      WorldFacade world = new DummyWorld(); // TODO: replace with real sim adapter
      world.init(seed);

      runner.runForTicks(ticks, t -> {
        try {
          // In a real run, UI/exogenous events would be pulled from a queue. Here: none.
          var events = List.<SimEvent>of();
          // Journal empty tick (ok)
          // jw.append(new UiCommand(t, "noop", Map.of()), new byte[0]); // optional
          world.step(t, (List<SimEvent>) events);
        } catch (Exception e) { throw new RuntimeException(e); }
      });

      var violations = invariants.violations(world.snapshot());
      if (!violations.isEmpty()) {
        throw new IllegalStateException("Invariant(s) failed: " + violations);
      }
    }

    System.out.println("Replay finished. seed=" + seed + " ticks=" + ticks + " journal=" + journal);
  }

  private static void printHelp() {
    System.out.println("""
      Usage: ReplayerMain --seed <long> --ticks <long> [--tickMillis <int>] --journal <file>
      """);
  }

  private record Params(long seed, long ticks, int tickMillis, java.nio.file.Path journal, boolean help) {}

  private static Params parseArgs(String[] args) {
    long seed = 0L; long ticks = 0L; int tickMillis = 50;
    Path journal = Path.of("target/run.msjor");
    boolean help = false;
    for (int i = 0; i < args.length; i++) {
      switch (args[i]) {
        case "-h": case "--help": help = true; break;
        case "--seed": seed = Long.parseLong(args[++i]); break;
        case "--ticks": ticks = Long.parseLong(args[++i]); break;
        case "--tickMillis": tickMillis = Integer.parseInt(args[++i]); break;
        case "--journal": journal = Path.of(args[++i]); break;
        default: throw new IllegalArgumentException("Unknown arg: " + args[i]);
      }
    }
    return new Params(seed, ticks, tickMillis, journal, help);
  }
}
