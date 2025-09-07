package com.mars_sim.core.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.Test;

import com.mars_sim.core.map.location.BoundedObject;
import com.mars_sim.core.map.location.LocalBoundedObject;

class RelativePositionTest {
    @Test
    void testIsWithin() {
        RelativePosition pos = new RelativePosition(1, 1);

        assertTrue("Within bounds", pos.isWithin(2, 2));
        assertFalse("Too wide bounds", pos.isWithin(1, 0.5));

        assertFalse("Too long bounds", pos.isWithin(0.5, 1));
    }

    @Test
    void testToPositionWithNoRotate() {
        RelativePosition pos = new RelativePosition(2, 1);

        LocalBoundedObject context = new BoundedObject(0, 0, 10, 5, 0);
        var result = pos.toPosition(context);
        assertEquals("X", pos.x(), result.getX(), 0);
        assertEquals("Y", pos.y(), result.getY(), 0);
    }

    @Test
    void testToPositionWithOffset() {
        RelativePosition pos = new RelativePosition(2, 1);

        var context = new BoundedObject(2, 1, 10, 5, 0);
        var result = pos.toPosition(context);
        assertEquals("X", pos.x()+context.getXLocation(), result.getX(), 0);
        assertEquals("Y", pos.y()+context.getYLocation(), result.getY(), 0);
    }

    @Test
    void testToPositionWithRotate() {
        RelativePosition pos = new RelativePosition(2, 1);

        // Rotate 90 degress clockwise
        // context base is the bottom left of region
        LocalBoundedObject context = new BoundedObject(0, 0, 10, 5, 90);
        var result = pos.toPosition(context);
        assertEquals("X", -pos.y(), result.getX(), 0.1);
        assertEquals("Y", pos.x(), result.getY(), 0.1);

        // Rotate 180, 
        context = new BoundedObject(0, 0, 10, 5, 180);
        result = pos.toPosition(context);
        assertEquals("X", -pos.x(), result.getX(), 0.1);
        assertEquals("Y", -pos.y(), result.getY(), 0.1);

        // Rotate 270, 
        context = new BoundedObject(0, 0, 10, 5, 270);
        result = pos.toPosition(context);
        assertEquals("X", pos.y(), result.getX(), 0.1);
        assertEquals("Y", -pos.x(), result.getY(), 0.1);
    }

    @Test
    void testToPositionWithRotateOffset() {
        RelativePosition pos = new RelativePosition(2, 1);

        // Rotate 90 degress clockwise
        // context base is the bottom left of region
        LocalBoundedObject context = new BoundedObject(2, 1, 10, 5, 90);
        var result = pos.toPosition(context);
        assertEquals("X", -pos.y() + context.getXLocation(), result.getX() , 0.1);
        assertEquals("Y", pos.x() + context.getYLocation(), result.getY(), 0.1);

        // Rotate 180, 
        context = new BoundedObject(2, 1, 10, 5, 180);
        result = pos.toPosition(context);
        assertEquals("X", -pos.x() + context.getXLocation(), result.getX(), 0.1);
        assertEquals("Y", -pos.y() + context.getYLocation(), result.getY(), 0.1);

        // Rotate 270, 
        context = new BoundedObject(2, 1, 10, 5, 270);
        result = pos.toPosition(context);
        assertEquals("X", pos.y() + context.getXLocation(), result.getX(), 0.1);
        assertEquals("Y", -pos.x() + context.getYLocation(), result.getY(), 0.1);
    }
}
