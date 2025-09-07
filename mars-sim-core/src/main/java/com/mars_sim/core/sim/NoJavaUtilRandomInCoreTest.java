package com.mars_sim.core.sim;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.*;
import java.util.stream.Stream;

/**
 * Opt-in guard rail to discourage `new Random(` in core sources.
 * Enable with: -Denforce.no.random=true
 */
@EnabledIfSystemProperty(named = "enforce.no.random", matches = "true")
public class NoJavaUtilRandomInCoreTest {

  @Test
  void forbidNewRandomInCoreSources() throws IOException {
    // Run from module base dir; scan main sources only
    Path src = Paths.get("src/main/java");
    try (Stream<Path> files = Files.walk(src)) {
      var offenders = files
          .filter(p -> p.toString().endsWith(".java"))
          // allow under our deterministic RNG wrapper
          .filter(p -> !p.toString().contains("/sim/random/"))
          .filter(p -> {
            try {
              String s = Files.readString(p);
              return s.contains("new Random(");
            } catch (IOException e) {
              return false;
            }
          })
          .toList();

      if (!offenders.isEmpty()) {
        fail("Found 'new Random(' in core sources: " + offenders);
      }
    }
  }
}
