package com.mars_sim.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.test.MarsSimUnitTest;

public class EntityTest extends MarsSimUnitTest {
    
    @Test
    void testSettlementContext() {
        var s = buildSettlement("Test");
        assertNotNull("Settlement context", s.getContext());
        assertEquals("Test", s.getName());
    }

    @Test
    public void personInSettlementContext() {
        var s = buildSettlement("Test");
        var p = buildPerson("Fred", s);

        assertEquals(s.getName(), p.getContext());
        assertEquals("Fred", p.getName());
    }

    @Test
    public void testVehicleAtSettlementContext() {
        var s = buildSettlement("Test");
        var v = buildRover(s, "Rover", LocalPosition.DEFAULT_POSITION, EXPLORER_ROVER);


        assertEquals(s.getName(), v.getContext());
        assertEquals("Rover", v.getName());
    }

    @Test
    public void testPersonInVehicleContext() {
        var s = buildSettlement("Test");
        var v = buildRover(s, "Rover", LocalPosition.DEFAULT_POSITION, EXPLORER_ROVER);
        var p = buildPerson("Fred", s);

        p.transfer(v);

        assertEquals(s.getName() + Entity.ENTITY_SEPERATOR
                                         + v.getName(), p.getContext());
        assertEquals("Fred", p.getName());
    }

    @Test
    public void testPersonOnSurfaceContext() {
        var s = buildSettlement("Test");
        var p = buildPerson("Fred", s);

        p.transfer(getContext().getSurface());

        assertEquals(s.getCoordinates().getFormattedString(), p.getContext());
        assertEquals("Fred", p.getName());
    }

    @Test
    public void testVehicleOnSurfaceContext() {
        var s = buildSettlement("Test");
        var v = buildRover(s, "Rover", LocalPosition.DEFAULT_POSITION, EXPLORER_ROVER);

        v.transfer(getContext().getSurface());

        assertEquals(s.getCoordinates().getFormattedString(), v.getContext());
        assertEquals("Rover", v.getName());
    }

    @Test
    public void testPersonInVehicleOnSurfaceContext() {
        var s = buildSettlement("Test");
        var v = buildRover(s, "Rover", LocalPosition.DEFAULT_POSITION, EXPLORER_ROVER);
        var p = buildPerson("Fred", s);

        v.transfer(getContext().getSurface());
        p.transfer(v);

        assertEquals(s.getCoordinates().getFormattedString()
                    + Entity.ENTITY_SEPERATOR + v.getName(), p.getContext());
        assertEquals("Fred", p.getName());
    }
}
