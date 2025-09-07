package org.mars_sim.msp.core.sim;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.io.IOException;
import java.nio.file.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Opt-in guard rail to discourage `new Random(` in core.
 * Enable with: -Denforce.no.random=true
 */
@EnabledIfSystemProperty(named = "enforce.no.random", matches = "true")
public class NoJavaUtilRandomInCoreTest {

  @Test
  void forbidNewRandomInCoreSources() throws IOException {
    Path src = Paths.get("src/main/java");
    try (Stream<Path> files = Files.walk(src)) {
      var bad = files.filter(p -> p.toString().endsWith(".java"))
          .filter(p -> !p.toString().contains("/sim/random/"))
          .filter(p -> {
            try {
              String s = Files.readString(p);
              return s.contains("new Random(");
            } catch (IOException e) { return false; }
          }).toList();
      if (!bad.isEmpty()) {
        fail("Found 'new Random(' in core sources: " + bad);
      }
    }
  }
}
