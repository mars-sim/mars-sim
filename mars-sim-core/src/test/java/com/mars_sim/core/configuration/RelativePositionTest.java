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

        assertTrue(pos.isWithin(2, 2), "Within bounds");
        assertFalse(pos.isWithin(1, 0.5), "Too wide bounds");

        assertFalse(pos.isWithin(0.5, 1), "Too long bounds");
    }

    @Test
    void testToPositionWithNoRotate() {
        RelativePosition pos = new RelativePosition(2, 1);

        LocalBoundedObject context = new BoundedObject(0, 0, 10, 5, 0);
        var result = pos.toPosition(context);
        assertEquals(pos.x(), result.getX(), 0, "X");
        assertEquals(pos.y(), result.getY(), 0, "Y");
    }

    @Test
    void testToPositionWithOffset() {
        RelativePosition pos = new RelativePosition(2, 1);

        var context = new BoundedObject(2, 1, 10, 5, 0);
        var result = pos.toPosition(context);
        assertEquals(pos.x()+context.getXLocation(), result.getX(), 0, "X");
        assertEquals(pos.y()+context.getYLocation(), result.getY(), 0, "Y");
    }

    @Test
    void testToPositionWithRotate() {
        RelativePosition pos = new RelativePosition(2, 1);

        // Rotate 90 degress clockwise
        // context base is the bottom left of region
        LocalBoundedObject context = new BoundedObject(0, 0, 10, 5, 90);
        var result = pos.toPosition(context);
        assertEquals(-pos.y(), result.getX(), 0.1, "X");
        assertEquals(pos.x(), result.getY(), 0.1, "Y");

        // Rotate 180, 
        context = new BoundedObject(0, 0, 10, 5, 180);
        result = pos.toPosition(context);
        assertEquals(-pos.x(), result.getX(), 0.1, "X");
        assertEquals(-pos.y(), result.getY(), 0.1, "Y");

        // Rotate 270, 
        context = new BoundedObject(0, 0, 10, 5, 270);
        result = pos.toPosition(context);
        assertEquals(pos.y(), result.getX(), 0.1, "X");
        assertEquals(-pos.x(), result.getY(), 0.1, "Y");
    }

    @Test
    void testToPositionWithRotateOffset() {
        RelativePosition pos = new RelativePosition(2, 1);

        // Rotate 90 degress clockwise
        // context base is the bottom left of region
        LocalBoundedObject context = new BoundedObject(2, 1, 10, 5, 90);
        var result = pos.toPosition(context);
        assertEquals(-pos.y() + context.getXLocation(), result.getX() , 0.1, "X");
        assertEquals(pos.x() + context.getYLocation(), result.getY(), 0.1, "Y");

        // Rotate 180, 
        context = new BoundedObject(2, 1, 10, 5, 180);
        result = pos.toPosition(context);
        assertEquals(-pos.x() + context.getXLocation(), result.getX(), 0.1, "X");
        assertEquals(-pos.y() + context.getYLocation(), result.getY(), 0.1, "Y");

        // Rotate 270, 
        context = new BoundedObject(2, 1, 10, 5, 270);
        result = pos.toPosition(context);
        assertEquals(pos.y() + context.getXLocation(), result.getX(), 0.1, "X");
        assertEquals(-pos.x() + context.getYLocation(), result.getY(), 0.1, "Y");
    }
}
