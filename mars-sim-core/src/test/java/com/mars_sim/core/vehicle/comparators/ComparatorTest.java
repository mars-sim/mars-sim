package com.mars_sim.core.vehicle.comparators;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mars_sim.core.test.MarsSimUnitTest;
import com.mars_sim.core.map.location.LocalPosition;

public class ComparatorTest extends MarsSimUnitTest {
    @Test
    public void testLab() {
        var s = buildSettlement("mock");

        var t = buildRover(s, "Transport", LocalPosition.DEFAULT_POSITION, TRANSPORT_ROVER);
        var c = buildRover(s, "Cargo", LocalPosition.DEFAULT_POSITION, CARGO_ROVER);
        var e = buildRover(s, "Explorer", LocalPosition.DEFAULT_POSITION, EXPLORER_ROVER);

        var v = new ArrayList<>(List.of(t,c,e));

        Collections.sort(v, new LabRangeComparator());
        
        assertEquals(t, v.get(0), "Worst lab");
        assertEquals(e, v.get(2), "Best lab");

    }

    @Test
    public void testRange() {
        var s = buildSettlement("mock");

        var t = buildRover(s, "Transport", LocalPosition.DEFAULT_POSITION, TRANSPORT_ROVER);
        var c = buildRover(s, "Cargo", LocalPosition.DEFAULT_POSITION, CARGO_ROVER);
        var e = buildRover(s, "Explorer", LocalPosition.DEFAULT_POSITION, EXPLORER_ROVER);

        var v = new ArrayList<>(List.of(t,c,e));

        Collections.sort(v, new RangeComparator());
        
        assertEquals(e, v.get(0), "Shortest range");
        assertEquals(c, v.get(2), "Longest range");

    }

    @Test
    public void testCargo() {
        var s = buildSettlement("mock");

        var t = buildRover(s, "Transport", LocalPosition.DEFAULT_POSITION, TRANSPORT_ROVER);
        var c = buildRover(s, "Cargo", LocalPosition.DEFAULT_POSITION, CARGO_ROVER);
        var e = buildRover(s, "Explorer", LocalPosition.DEFAULT_POSITION, EXPLORER_ROVER);

        var v = new ArrayList<>(List.of(t,c,e));

        Collections.sort(v, new CargoRangeComparator());
        
        assertEquals(e, v.get(0), "Smallest cargo");
        assertEquals(c, v.get(2), "Largest cargo");
    }

    @Test
    public void testCrew() {
        var s = buildSettlement("mock");

        var t = buildRover(s, "Transport", LocalPosition.DEFAULT_POSITION, TRANSPORT_ROVER);
        var c = buildRover(s, "Cargo", LocalPosition.DEFAULT_POSITION, CARGO_ROVER);
        var e = buildRover(s, "Explorer", LocalPosition.DEFAULT_POSITION, EXPLORER_ROVER);

        var v = new ArrayList<>(List.of(t,c,e));

        Collections.sort(v, new CrewRangeComparator());
        
        assertEquals(c, v.get(0), "Smallest crew");
        assertEquals(t, v.get(2), "Largest crew");
    }
}
