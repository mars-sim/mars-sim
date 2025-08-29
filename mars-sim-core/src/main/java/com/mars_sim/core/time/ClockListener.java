/*
  * Mars Simulation Project
  * ClockListener.java
  */
 package com.mars_sim.core.time;
 
 /**
- * Receives simulated time pulses and pause changes from the MasterClock.
+ * Receives simulated time pulses and pause changes from the MasterClock.
+ * <p>
+ * Implementations are called from the MasterClock's listener executor.
+ * A listener should do only quick work here, or offload to its own worker.
  */
 public interface ClockListener {
 
-    void clockPulse(double time);
-    void uiPulse(double time);
-    void pauseChange(boolean isPaused, boolean showPane);
+    /**
+     * Called each simulation pulse.
+     *
+     * @param elapsedMillisols elapsed simulated time in millisols for this delivery
+     */
+    default void clockPulse(double elapsedMillisols) {}
+
+    /**
+     * Optional UI pulse (unused by core clock, reserved for UI integrations).
+     */
+    default void uiPulse(double time) {}
+
+    /**
+     * Called when the simulation pause state changes.
+     */
+    default void pauseChange(boolean isPaused, boolean showPane) {}
 }
