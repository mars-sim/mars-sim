package com.mars_sim.core.vehicle.comparators;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mars_sim.core.test.MarsSimUnitTest;
import com.mars_sim.core.map.location.LocalPosition;

class ComparatorTest extends MarsSimUnitTest {
    @Test
    void testLab() {
        var s = buildSettlement("mock");

        var t = buildRover(s, "Transport", LocalPosition.DEFAULT_POSITION, TRANSPORT_ROVER);
        var c = buildRover(s, "Cargo", LocalPosition.DEFAULT_POSITION, CARGO_ROVER);
        var e = buildRover(s, "Explorer", LocalPosition.DEFAULT_POSITION, EXPLORER_ROVER);
        var l = buildRover(s, "Long Range Explorer", LocalPosition.DEFAULT_POSITION, LONG_RANGE_EXPLORER);
        
        var v = new ArrayList<>(List.of(t,c,e,l));

        Collections.sort(v, new LabRangeComparator());
        
        assertEquals(t, v.get(0), "Worst lab");
        assertEquals(l, v.get(3), "Best lab");

    }

    @Test
    void testRange() {
        var s = buildSettlement("mock");

        var t = buildRover(s, "Transport", LocalPosition.DEFAULT_POSITION, TRANSPORT_ROVER);
        var c = buildRover(s, "Cargo", LocalPosition.DEFAULT_POSITION, CARGO_ROVER);
        var e = buildRover(s, "Explorer", LocalPosition.DEFAULT_POSITION, EXPLORER_ROVER);
        var l = buildRover(s, "Long Range Explorer", LocalPosition.DEFAULT_POSITION, LONG_RANGE_EXPLORER);
        
        assertTrue(e.getRange() < c.getRange(), "Explorer has less range than Cargo");

        var v = new ArrayList<>(List.of(t,c,e,l));

        Collections.sort(v, new RangeComparator());
        
        assertEquals(e, v.get(0), "Shortest range");
        assertEquals(c, v.get(3), "Longest range");

    }

    @Test
    void testCargo() {
        var s = buildSettlement("mock");

        var t = buildRover(s, "Transport", LocalPosition.DEFAULT_POSITION, TRANSPORT_ROVER);
        var c = buildRover(s, "Cargo", LocalPosition.DEFAULT_POSITION, CARGO_ROVER);
        var e = buildRover(s, "Explorer", LocalPosition.DEFAULT_POSITION, EXPLORER_ROVER);
        var l = buildRover(s, "Long Range Explorer", LocalPosition.DEFAULT_POSITION, LONG_RANGE_EXPLORER);
        
        assertTrue(e.getEquipmentInventory().getCargoCapacity()
                    < c.getEquipmentInventory().getCargoCapacity(), "Explorer has less cargo capacity than Cargo");

        var v = new ArrayList<>(List.of(t,c,e,l));

        Collections.sort(v, new CargoRangeComparator());
        
        assertEquals(e, v.get(0), "Smallest cargo");
        assertEquals(c, v.get(3), "Largest cargo");
    }

    @Test
    void testCrew() {
        var s = buildSettlement("mock");

        var t = buildRover(s, "Transport", LocalPosition.DEFAULT_POSITION, TRANSPORT_ROVER);
        var c = buildRover(s, "Cargo", LocalPosition.DEFAULT_POSITION, CARGO_ROVER);
        var e = buildRover(s, "Explorer", LocalPosition.DEFAULT_POSITION, EXPLORER_ROVER);
        var l = buildRover(s, "Long Range Explorer", LocalPosition.DEFAULT_POSITION, LONG_RANGE_EXPLORER);

        assertTrue(c.getCrewCapacity() < t.getCrewCapacity(), "Cargo has less crew capacity than Transport");
        var v = new ArrayList<>(List.of(t,c,e,l));

        Collections.sort(v, new CrewRangeComparator());
        
        // Note: both the regular explorer or long range explorer has a size of 4. 
        boolean smallest = c == v.get(0) || c == v.get(1);
        
        assertTrue(smallest, "Smallest crew");
        assertEquals(t, v.get(3), "Largest crew");
    }
}
