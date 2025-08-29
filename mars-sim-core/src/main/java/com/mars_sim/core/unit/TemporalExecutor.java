package com.mars_sim.core.unit;

import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.Temporal;

/**
 * Coordinates fan‑out of time pulses to registered Temporal targets.
 */
public interface TemporalExecutor {
    void addTarget(Temporal target);
    void removeTarget(Temporal target);
    void applyPulse(ClockPulse pulse);
    void stop();
}
