package com.mars_sim.core.metrics.memory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mars_sim.core.metrics.MetricKey;
import com.mars_sim.core.metrics.MetricManager;
import com.mars_sim.core.metrics.MetricManagerTest;

@DisplayName("MemoryMetricManager Tests")
class MemoryMetricManagerTest extends MetricManagerTest {
    private static final int MAX_SOL = 3;
    
    @Override
    protected MetricManager createMetricManager() {
        return new MemoryMetricManager(MAX_SOL);
    }

    @Test
    @DisplayName("newSol should remove old sols from metrics")
    void testNewSolRemovesOldSols() {
        var manager = (MemoryMetricManager) getManager();
        var s = buildSettlement("Test");
        
        // Add data points for sols 0 to MAX_SOL+1
        var clock = getSim().getMasterClock();
        var marsTime = clock.getMarsTime();
        for (int sol = 1; sol <= MAX_SOL+1; sol++) {
            marsTime = marsTime.addTime(1000D);
            clock.setMarsTime(marsTime);

            manager.addValue(s, TEMP_CAT, "Average", 1D);

        }
        
        var m = manager.getMetric(new MetricKey(s, TEMP_CAT, "Average"));
        var origRange = m.getSolRange().size();
        assertTrue(origRange > MAX_SOL, "Original sol range should be greater than MAX_SOL");
        assertEquals(MAX_SOL+1, m.getSize(), "Metric size original");

        manager.newSol(marsTime);
        var newRange = m.getSolRange();
        assertEquals(MAX_SOL, newRange.size(), "Sol range should equal MAX_SOL");
        assertEquals(MAX_SOL, m.getSize(), "Metric size after removing old sols");
        
    }
}
