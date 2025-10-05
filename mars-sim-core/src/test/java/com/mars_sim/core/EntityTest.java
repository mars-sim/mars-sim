package com.mars_sim.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.test.MarsSimUnitTest;

public class EntityTest extends MarsSimUnitTest {
    
    @Test
    public void testSettlementContext() {
        var s = buildSettlement("Test");
        assertNull("Settlement context", s.getContext());
        assertEquals("Settlement name", "Test", s.getName());
    }

    @Test
    public void personInSettlementContext() {
        var s = buildSettlement("Test");
        var p = buildPerson("Fred", s);

        assertEquals("Person context", s.getName(), p.getContext());
        assertEquals("Person name", "Fred", p.getName());
    }

    @Test
    public void testVehicleAtSettlementContext() {
        var s = buildSettlement("Test");
        var v = buildRover(s, "Rover", LocalPosition.DEFAULT_POSITION, EXPLORER_ROVER);


        assertEquals("Vehicle context", s.getName(), v.getContext());
        assertEquals("Vehicle name", "Rover", v.getName());
    }

    @Test
    public void testPersonInVehicleContext() {
        var s = buildSettlement("Test");
        var v = buildRover(s, "Rover", LocalPosition.DEFAULT_POSITION, EXPLORER_ROVER);
        var p = buildPerson("Fred", s);

        p.transfer(v);

        assertEquals("Person context", s.getName() + Entity.ENTITY_SEPERATOR
                                         + v.getName(), p.getContext());
        assertEquals("Person name", "Fred", p.getName());
    }

    @Test
    public void testPersonOnSurfaceContext() {
        var s = buildSettlement("Test");
        var p = buildPerson("Fred", s);

        p.transfer(getContext().getSurface());

        assertEquals("Person context", s.getCoordinates().getFormattedString(), p.getContext());
        assertEquals("Person name", "Fred", p.getName());
    }

    @Test
    public void testVehicleOnSurfaceContext() {
        var s = buildSettlement("Test");
        var v = buildRover(s, "Rover", LocalPosition.DEFAULT_POSITION, EXPLORER_ROVER);

        v.transfer(getContext().getSurface());

        assertEquals("Vehicle context", s.getCoordinates().getFormattedString(), v.getContext());
        assertEquals("Vehicle name", "Rover", v.getName());
    }

    @Test
    public void testPersonInVehicleOnSurfaceContext() {
        var s = buildSettlement("Test");
        var v = buildRover(s, "Rover", LocalPosition.DEFAULT_POSITION, EXPLORER_ROVER);
        var p = buildPerson("Fred", s);

        v.transfer(getContext().getSurface());
        p.transfer(v);

        assertEquals("Person context", s.getCoordinates().getFormattedString()
                    + Entity.ENTITY_SEPERATOR + v.getName(), p.getContext());
        assertEquals("Person name", "Fred", p.getName());
    }
}
