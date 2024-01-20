package com.mars_sim.core;

import com.mars_sim.mapdata.location.LocalPosition;

public class EntityTest extends AbstractMarsSimUnitTest {
    
    public void testSettlementContext() {
        var s = buildSettlement("Test");
        assertNull("Settlement context", s.getContext());
        assertEquals("Settlement name", "Test", s.getName());
    }

    public void testPersonInSettlementContext() {
        var s = buildSettlement("Test");
        var p = buildPerson("Fred", s);

        assertEquals("Person context", s.getName(), p.getContext());
        assertEquals("Person name", "Fred", p.getName());
    }

    public void testVehicleAtSettlementContext() {
        var s = buildSettlement("Test");
        var v = buildRover(s, "Rover", LocalPosition.DEFAULT_POSITION);


        assertEquals("Vehicle context", s.getName(), v.getContext());
        assertEquals("Vehicle name", "Rover", v.getName());
    }

    public void testPersonInVehicleContext() {
        var s = buildSettlement("Test");
        var v = buildRover(s, "Rover", LocalPosition.DEFAULT_POSITION);
        var p = buildPerson("Fred", s);

        p.transfer(v);

        assertEquals("Person context", s.getName() + Entity.ENTITY_SEPERATOR
                                         + v.getName(), p.getContext());
        assertEquals("Person name", "Fred", p.getName());
    }

    public void testPersonOnSurfaceContext() {
        var s = buildSettlement("Test");
        var p = buildPerson("Fred", s);

        var ms = unitManager.getMarsSurface();
        p.transfer(ms);

        assertEquals("Person context", s.getCoordinates().getFormattedString(), p.getContext());
        assertEquals("Person name", "Fred", p.getName());
    }

    public void testVehicleOnSurfaceContext() {
        var s = buildSettlement("Test");
        var v = buildRover(s, "Rover", LocalPosition.DEFAULT_POSITION);

        var ms = unitManager.getMarsSurface();
        v.transfer(ms);

        assertEquals("Vehicle context", s.getCoordinates().getFormattedString(), v.getContext());
        assertEquals("Vehicle name", "Rover", v.getName());
    }

    
    public void testPersonInVehicleOnSurfaceContext() {
        var s = buildSettlement("Test");
        var v = buildRover(s, "Rover", LocalPosition.DEFAULT_POSITION);
        var p = buildPerson("Fred", s);

        var ms = unitManager.getMarsSurface();
        v.transfer(ms);
        p.transfer(v);

        assertEquals("Person context", s.getCoordinates().getFormattedString()
                    + Entity.ENTITY_SEPERATOR + v.getName(), p.getContext());
        assertEquals("Person name", "Fred", p.getName());
    }
}
