*** /dev/null
--- b/mars-sim-core/src/main/java/com/mars_sim/core/unit/TemporalThreadExecutor.java
@@
+/*
+ * Mars Simulation Project
+ * TemporalThreadExecutor.java
+ * @date 2025-08-31 (sequential, CME-safe base)
+ */
+package com.mars_sim.core.unit;
+
+import com.mars_sim.core.time.ClockPulse;
+
+/**
+ * Sequential executor; inherits CME-safe iteration from base.
+ */
+public class TemporalThreadExecutor extends TemporalExecutor {
+
+    public TemporalThreadExecutor() {}
+
+    @Override
+    public void applyPulse(ClockPulse pulse) {
+        // Use base (sequential + shielding).
+        super.applyPulse(pulse);
+    }
+}
