*** /dev/null
--- b/mars-sim-core/src/main/java/com/mars_sim/core/unit/TemporalExecutorService.java
@@
+/*
+ * Mars Simulation Project
+ * TemporalExecutorService.java
+ * @date 2025-08-31 (barrier parallel fan-out + shielding)
+ */
+package com.mars_sim.core.unit;
+
+import java.util.ArrayList;
+import java.util.List;
+import java.util.concurrent.Callable;
+import java.util.concurrent.ExecutorService;
+import java.util.concurrent.Executors;
+
+import com.google.common.util.concurrent.ThreadFactoryBuilder;
+import com.mars_sim.core.SimulationRuntime;
+import com.mars_sim.core.logging.SimLogger;
+import com.mars_sim.core.time.ClockPulse;
+import com.mars_sim.core.time.Temporal;
+
+/**
+ * Parallel temporal fan-out with invokeAll barrier semantics.
+ */
+public class TemporalExecutorService extends TemporalExecutor {
+
+    private static final SimLogger logger = SimLogger.getLogger(TemporalExecutorService.class.getName());
+
+    private final ExecutorService pool;
+
+    public TemporalExecutorService(String threadPrefix) {
+        int cores = Math.max(1, SimulationRuntime.NUM_CORES);
+        this.pool = Executors.newFixedThreadPool(
+                cores,
+                new ThreadFactoryBuilder().setNameFormat((threadPrefix == null ? "temporal-" : threadPrefix) + "%d").build()
+        );
+    }
+
+    @Override
+    public void applyPulse(ClockPulse pulse) {
+        List<Temporal> snapshot = snapshotTargets();
+        if (snapshot.isEmpty()) return;
+
+        // Build shielded batch
+        List<Callable<Void>> batch = new ArrayList<>(snapshot.size());
+        for (Temporal t : snapshot) {
+            batch.add(() -> {
+                try {
+                    t.timePassing(pulse);
+                }
+                catch (Throwable ex) {
+                    logger.severe("Temporal target threw during pulse: " + t + " ", ex);
+                }
+                return null;
+            });
+        }
+
+        try {
+            // Barrier: wait for all to finish this pulse
+            pool.invokeAll(batch);
+        }
+        catch (InterruptedException ie) {
+            Thread.currentThread().interrupt();
+            logger.severe("Interrupted while delivering temporal pulse.", ie);
+        }
+    }
+
+    @Override
+    public void stop() {
+        pool.shutdownNow();
+    }
+}
