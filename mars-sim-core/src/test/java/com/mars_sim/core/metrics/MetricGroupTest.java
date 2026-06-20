package com.mars_sim.core.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.mars_sim.core.MockEntity;
import com.mars_sim.core.test.MarsSimUnitTest;


class MetricGroupTest extends MarsSimUnitTest {
    @Test
    void testGetSolBreakdown() {
        var entity = new MockEntity("id");

        var cat = new MetricCategory("Cat1");
        var group = new MetricGroup(cat);

        var measure1 = "Measure1";
        var measure2 = "Measure2";

        // Load up values in different sols
        group.recordValue(measure1, 1D, entity);
        group.recordValue(measure1, 1D, entity);
        group.recordValue(measure2, 5D, entity);

        var clock = getSim().getMasterClock();
        var nextSol = clock.getMarsTime().addTime(1000);
        clock.setMarsTime(nextSol);
        group.recordValue(measure1, 2D, entity);
        group.recordValue(measure2, 2D, entity);

        nextSol = nextSol.addTime(1000);
        clock.setMarsTime(nextSol);
        group.recordValue(measure1, 20D, entity);

        var histogram = group.getSolBreakdown();
        assertEquals(3, histogram.size(), "number of sols in breakdown");

        var sol1 = histogram.get(1);
        assertEquals(2, sol1.get(measure1), "sol 1 measure 1 value");
        assertEquals(5, sol1.get(measure2), "sol 1 measure 2 value");

        var sol2 = histogram.get(2);
        assertEquals(2, sol2.get(measure1), "sol 2 measure 1 value");
        assertEquals(2, sol2.get(measure2), "sol 2 measure 2 value");   

        var sol3 = histogram.get(3);
        assertEquals(20, sol3.get(measure1), "sol 3 measure 1 value");
    }
}
