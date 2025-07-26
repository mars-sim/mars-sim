package com.mars_sim.core.vehicle.comparators;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.map.location.LocalPosition;

public class ComparatorTest extends AbstractMarsSimUnitTest {
    public void testLab() {
        var s = buildSettlement();

        var t = buildRover(s, "Transport", LocalPosition.DEFAULT_POSITION, TRANSPORT_ROVER);
        var c = buildRover(s, "Cargo", LocalPosition.DEFAULT_POSITION, CARGO_ROVER);
        var e = buildRover(s, "Explorer", LocalPosition.DEFAULT_POSITION, EXPLORER_ROVER);

        var v = new ArrayList<>(List.of(t,c,e));

        Collections.sort(v, new LabRangeComparator());
        
        assertEquals("Worst lab", t, v.get(0));
        assertEquals("Best lab", e, v.get(2));

    }

    public void testRange() {
        var s = buildSettlement();

        var t = buildRover(s, "Transport", LocalPosition.DEFAULT_POSITION, TRANSPORT_ROVER);
        var c = buildRover(s, "Cargo", LocalPosition.DEFAULT_POSITION, CARGO_ROVER);
        var e = buildRover(s, "Explorer", LocalPosition.DEFAULT_POSITION, EXPLORER_ROVER);

        var v = new ArrayList<>(List.of(t,c,e));

        Collections.sort(v, new RangeComparator());
        
        assertEquals("Shortest range", e, v.get(0));
        assertEquals("Longest range", c, v.get(2));

    }

    public void testCargo() {
        var s = buildSettlement();

        var t = buildRover(s, "Transport", LocalPosition.DEFAULT_POSITION, TRANSPORT_ROVER);
        var c = buildRover(s, "Cargo", LocalPosition.DEFAULT_POSITION, CARGO_ROVER);
        var e = buildRover(s, "Explorer", LocalPosition.DEFAULT_POSITION, EXPLORER_ROVER);

        var v = new ArrayList<>(List.of(t,c,e));

        Collections.sort(v, new CargoRangeComparator());
        
        assertEquals("Smallest cargo", e, v.get(0));
        assertEquals("Largest cargo", c, v.get(2));
    }

    public void testCrew() {
        var s = buildSettlement();

        var t = buildRover(s, "Transport", LocalPosition.DEFAULT_POSITION, TRANSPORT_ROVER);
        var c = buildRover(s, "Cargo", LocalPosition.DEFAULT_POSITION, CARGO_ROVER);
        var e = buildRover(s, "Explorer", LocalPosition.DEFAULT_POSITION, EXPLORER_ROVER);

        var v = new ArrayList<>(List.of(t,c,e));

        Collections.sort(v, new CrewRangeComparator());
        
        assertEquals("Smallest crew", c, v.get(0));
        assertEquals("Largest crew", t, v.get(2));
    }
}
