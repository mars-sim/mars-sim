package com.mars_sim.core.map.location;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.Test;


/**
 * Unit test suite for the Coordinates class.
 */
class TestCoordinates {
	/* default logger. */
	
    private static final double ERROR_MARGIN_KM = .000000001D;
    private static final double ERROR_MARGIN_RAD = .00001D;
	
    /**
     * Finds the distance resolution for one degree at the equator.
     */
    @Test
    void testResolution() {
    	
        // 59.21 km resolution
    	
    	Coordinates c0 = new Coordinates("0", "0");
        Coordinates c1 = new Coordinates("1", "0");
    	
        double dist = c0.getDistance(c1);
                
        double expected = 59.219_021_520;
        double error = Math.abs(dist - expected);
        assertTrue(error < ERROR_MARGIN_KM);

        // 5.92 m resolution
        
        Coordinates c2 = new Coordinates("0.0000", "0");
        Coordinates c3 = new Coordinates("0.0001", "0");
    	
        double dist4Dec = c2.getDistance(c3) * 1000 ;
                
        double expected1 = 5.921_902_152;
        
        double error1 = Math.abs(dist4Dec - expected1);
        
        assertTrue(error1 < ERROR_MARGIN_KM);
        
        // 4.18 m resolution
        
        Coordinates c4 = new Coordinates("45", "0.0000");
        Coordinates c5 = new Coordinates("45", "0.0001");
    	
        double dist45 = c4.getDistance(c5) * 1000 ;
        
        
        double expected2 = 4.187_417_169;
        
        double error2 = Math.abs(dist45 - expected2);
        
        assertTrue(error2 < ERROR_MARGIN_KM);
        
    }
    
    @Test
    void testGetAngle() {
        var locInit = new Coordinates(Math.PI / 2D, 0D);

        double inc = 0.1D;
        var horzRight = new Coordinates(locInit.getPhi(), locInit.getTheta() + inc);
        var angle = locInit.getAngle(horzRight);
        assertEquals("Horizontal right arc", inc, angle, 0.01);

        var horzLeft = new Coordinates(locInit.getPhi(), locInit.getTheta() - inc);
        angle = locInit.getAngle(horzLeft);
        assertEquals("Horizontal left arc", inc, angle, 0.01);

        var vertTop = new Coordinates(locInit.getPhi() -inc, locInit.getTheta());
        angle = locInit.getAngle(vertTop);
        assertEquals("Vertical top arc", inc, angle, 0.01);

        var vertBottom = new Coordinates(locInit.getPhi() + inc, locInit.getTheta());
        angle = locInit.getAngle(vertBottom);
        assertEquals("Vertical bottom arc", inc, angle, 0.01);

        double diagInc = Math.sqrt((inc * inc)/2); 
        var northEast = new Coordinates(locInit.getPhi() - diagInc, locInit.getTheta() + diagInc);
        angle = locInit.getAngle(northEast);
        assertEquals("North East arc", inc, angle, 0.01);

        var southWest = new Coordinates(locInit.getPhi() + diagInc, locInit.getTheta() - diagInc);
        angle = locInit.getAngle(southWest);
        assertEquals("SOuth West arc", inc, angle, 0.01);
    }

    /**
     * Test the getDistance method.
     */
    @Test
    void testGetDistance() {
        
        Coordinates locInit = new Coordinates(Math.PI / 2D, 0D);
        
        // 1m east at equator.
        Coordinates loc1 = new Coordinates(Math.PI / 2D, Math.PI / 
                Coordinates.MARS_CIRCUMFERENCE / 500D);
        double dist1 = locInit.getDistance(loc1);
        double expected1 = .001D;
        double error1 = Math.abs(dist1 - expected1);
        assertTrue(error1 < ERROR_MARGIN_KM);
        
        // 10cm east at equator.
        Coordinates loc2 = new Coordinates(Math.PI / 2D, Math.PI / 
                Coordinates.MARS_CIRCUMFERENCE / 5000D);
        double dist2 = locInit.getDistance(loc2);
        double expected2 = .0001D;
        double error2 = Math.abs(dist2 - expected2);
        assertTrue(error2 < ERROR_MARGIN_KM);
        
        // 1cm east at equator.
        Coordinates loc3 = new Coordinates(Math.PI / 2D, Math.PI / 
                Coordinates.MARS_CIRCUMFERENCE / 50000D);
        double dist3 = locInit.getDistance(loc3);
        double expected3 = .00001D;
        double error3 = Math.abs(dist3 - expected3);
        assertTrue(error3 < ERROR_MARGIN_KM);
        
        // 1mm east at equator.
        Coordinates loc4 = new Coordinates(Math.PI / 2D, Math.PI / 
                Coordinates.MARS_CIRCUMFERENCE / 500000D);
        double dist4 = locInit.getDistance(loc4);
        double expected4 = .000001D;
        double error4 = Math.abs(dist4 - expected4);
        assertTrue(error4 < ERROR_MARGIN_KM);
        
        // Antipode (180 degrees) position at equator.
        Coordinates loc5 = new Coordinates(Math.PI / 2D, Math.PI);
        double dist5 = locInit.getDistance(loc5);
        double expected5 = Coordinates.MARS_CIRCUMFERENCE / 2D;
        double error5 = Math.abs(dist5 - expected5);
        assertTrue(error5 < ERROR_MARGIN_KM);
        
        // 90 degrees east at equator.
        Coordinates loc6 = new Coordinates(Math.PI / 2D, Math.PI / 2D);
        double dist6 = locInit.getDistance(loc6);
        double expected6 = Coordinates.MARS_CIRCUMFERENCE / 4D;
        double error6 = Math.abs(dist6 - expected6);
        assertTrue(error6 < ERROR_MARGIN_KM);
        
        // 90 degrees west at equator.
        Coordinates loc7 = new Coordinates(Math.PI / 2D, 3D * Math.PI / 2D);
        double dist7 = locInit.getDistance(loc7);
        double expected7 = Coordinates.MARS_CIRCUMFERENCE / 4D;
        double error7 = Math.abs(dist7 - expected7);
        assertTrue(error7 < ERROR_MARGIN_KM);
        
        // 1mm west at equator.
        Coordinates loc8 = new Coordinates(Math.PI / 2D, (2D * Math.PI) - 
                (Math.PI / Coordinates.MARS_CIRCUMFERENCE / 500000D));
        double dist8 = locInit.getDistance(loc8);
        double expected8 = .000001D;
        double error8 = Math.abs(dist8 - expected8);
        assertTrue(error8 < ERROR_MARGIN_KM);
        
        // 1mm north at equator.
        Coordinates loc9 = new Coordinates((Math.PI / 2D) - (Math.PI / 
                Coordinates.MARS_CIRCUMFERENCE / 500000D), 0D);
        double dist9 = locInit.getDistance(loc9);
        double expected9 = .000001D;
        double error9 = Math.abs(dist9 - expected9);
        assertTrue(error9 < ERROR_MARGIN_KM);
        
        // 1mm south at equator.
        Coordinates loc10 = new Coordinates((Math.PI / 2D) + (Math.PI / 
                Coordinates.MARS_CIRCUMFERENCE / 500000D), 0D);
        double dist10 = locInit.getDistance(loc10);
        double expected10 = .000001D;
        double error10 = Math.abs(dist10 - expected10);
        assertTrue(error10 < ERROR_MARGIN_KM);
        
        // 1mm south at north pole.
        Coordinates loc11Init = new Coordinates(0D, 0D);
        Coordinates loc11 = new Coordinates(Math.PI / 
                Coordinates.MARS_CIRCUMFERENCE / 500000D, 0D);
        double dist11 = loc11Init.getDistance(loc11);
        double expected11 = .000001D;
        double error11 = Math.abs(dist11 - expected11);
        assertTrue(error11 < ERROR_MARGIN_KM);
        
        // Same location at equator.
        Coordinates loc12 = new Coordinates(Math.PI / 2D, 0D);
        double dist12 = locInit.getDistance(loc12);
        double expected12 = 0D;
        double error12 = Math.abs(dist12 - expected12);
        assertTrue(error12 < ERROR_MARGIN_KM);
        
        // Same location at north pole.
        Coordinates loc13Init = new Coordinates(0D, 0D);
        Coordinates loc13 = new Coordinates(0D, 0D);
        double dist13 = loc13Init.getDistance(loc13);
        double expected13 = 0D;
        double error13 = Math.abs(dist13 - expected13);
        assertTrue(error13 < ERROR_MARGIN_KM);
        
        // Same location at south pole.
        Coordinates loc14Init = new Coordinates(Math.PI, 0D);
        Coordinates loc14 = new Coordinates(Math.PI, 0D);
        double dist14 = loc14Init.getDistance(loc14);
        double expected14 = 0D;
        double error14 = Math.abs(dist14 - expected14);
        assertTrue(error14 < ERROR_MARGIN_KM);
        
        // Antipode (180 degrees) position at north/south poles.
        Coordinates loc15Init = new Coordinates(0D, 0D);
        Coordinates loc15 = new Coordinates(Math.PI, 0D);
        double dist15 = loc15Init.getDistance(loc15);
        double expected15 = Coordinates.MARS_CIRCUMFERENCE / 2D;
        double error15 = Math.abs(dist15 - expected15);
        assertTrue(error15 < ERROR_MARGIN_KM);
    }
    
    /**
     * Test the getDirectionToPoint method.
     */
    @Test
    void testGetDirectionToPoint() {
        
        Coordinates locInit = new Coordinates(Math.PI / 2D, 0D);
        
        // Position north of initial.
        Coordinates loc1 = new Coordinates(Math.PI / 4D, 0D);
        Direction direction1 = locInit.getDirectionToPoint(loc1);
        assertEquals("Direction North", 0D, direction1.getDirection(), 0D);
        
        // Position east of initial.
        Coordinates loc2 = new Coordinates(Math.PI / 2D, Math.PI / 4D);
        Direction direction2 = locInit.getDirectionToPoint(loc2);
        assertEquals("Direction East", Math.PI / 2D, direction2.getDirection(), 0D);
        
        // Position south of initial.
        Coordinates loc3 = new Coordinates(3D * Math.PI / 4D, 0D);
        Direction direction3 = locInit.getDirectionToPoint(loc3);
        assertEquals("Direction South", Math.PI, direction3.getDirection(), 0D);
        
        // Position west of initial.
        Coordinates loc4 = new Coordinates(Math.PI / 2D, 3D * Math.PI / 2D);
        Direction direction4 = locInit.getDirectionToPoint(loc4);
        assertEquals("Direction west", 3D * Math.PI / 2D, direction4.getDirection(), 0D);
        
        double offset = Math.PI / Coordinates.MARS_CIRCUMFERENCE / 500D;
        
        // Position north-east of initial.
        Coordinates loc5 = new Coordinates((Math.PI / 2D) - offset, offset);
        Direction direction5 = locInit.getDirectionToPoint(loc5);
        double error5 = Math.abs(direction5.getDirection() - (Math.PI / 4D));
        assertTrue(error5 < ERROR_MARGIN_RAD);
        
        // Position south-east of initial.
        Coordinates loc6 = new Coordinates((Math.PI / 2D) + offset, offset);
        Direction direction6 = locInit.getDirectionToPoint(loc6);
        double error6 = Math.abs(direction6.getDirection() - (3D * Math.PI / 4D));
        assertTrue(error6 < ERROR_MARGIN_RAD);
        
        // Position south-west of initial.
        Coordinates loc7 = new Coordinates((Math.PI / 2D) + offset, 0D - offset);
        Direction direction7 = locInit.getDirectionToPoint(loc7);
        double error7 = Math.abs(direction7.getDirection() - (5D * Math.PI / 4D));
        assertTrue(error7 < ERROR_MARGIN_RAD);
        
        // Position north-west of initial.
        Coordinates loc8 = new Coordinates((Math.PI / 2D) - offset, 0D - offset);
        Direction direction8 = locInit.getDirectionToPoint(loc8);
        double error8 = Math.abs(direction8.getDirection() - (7D * Math.PI / 4D));
        assertTrue(error8 < ERROR_MARGIN_RAD);
        
        // Position antipodal of initial at equator.
        Coordinates loc9 = new Coordinates(Math.PI / 2D, Math.PI);
        Direction direction9 = locInit.getDirectionToPoint(loc9);
        double error9 = Math.abs(direction9.getDirection() - (Math.PI / 2D));
        assertTrue(error9 < ERROR_MARGIN_RAD);
        
        // Position antipodal from north pole to south pole.
        Coordinates locInit10 = new Coordinates(0D, 0D);
        Coordinates loc10 = new Coordinates(Math.PI, 0D);
        Direction direction10 = locInit10.getDirectionToPoint(loc10);
        double error10 = Math.abs(direction10.getDirection() - Math.PI);
        assertTrue(error10 < ERROR_MARGIN_RAD);
    }
    
    /**
     * Test the getNewLocation method.
     */
    public void testGetNewLocation() {
        
        Coordinates locInit = new Coordinates(Math.PI / 2D, 0D);
        double distance = 25D;
        double angleDistance = distance / Coordinates.MARS_CIRCUMFERENCE * Math.PI * 2D;
        double angleDistanceDiagonal = angleDistance * Math.sin(Math.PI / 4D);
        
        // Direction north.
        Direction direction1 = new Direction(0D);
        Coordinates loc1 = locInit.getNewLocation(direction1, distance);
        double phiError1 = Math.abs(loc1.getPhi() - ((Math.PI / 2D) - angleDistance));
        double thetaError1 = Math.abs(loc1.getTheta() - 0D);
        assertTrue(phiError1 < ERROR_MARGIN_RAD);
        assertTrue(thetaError1 < ERROR_MARGIN_RAD);

    
        // Direction north-east.
        Direction direction2 = new Direction(Math.PI / 4D);
        Coordinates loc2 = locInit.getNewLocation(direction2, distance);
        double phiError2 = Math.abs(loc2.getPhi() - ((Math.PI / 2D) - angleDistanceDiagonal));
        double thetaError2 = Math.abs(loc2.getTheta() - angleDistanceDiagonal);
        assertTrue(phiError2 < ERROR_MARGIN_RAD);
        assertTrue(thetaError2 < ERROR_MARGIN_RAD);
        
        // Direction east.
        Direction direction3 = new Direction(Math.PI / 2D);
        Coordinates loc3 = locInit.getNewLocation(direction3, distance);
        double phiError3 = Math.abs(loc3.getPhi() - (Math.PI / 2D));
        double thetaError3 = Math.abs(loc3.getTheta() - angleDistance);
        assertTrue(phiError3 < ERROR_MARGIN_RAD);
        assertTrue(thetaError3 < ERROR_MARGIN_RAD);
        
        // Direction south-east.
        Direction direction4 = new Direction(Math.PI * 3D / 4D);
        Coordinates loc4 = locInit.getNewLocation(direction4, distance);
        double phiError4 = Math.abs(loc4.getPhi() - ((Math.PI / 2D) + angleDistanceDiagonal));
        double thetaError4 = Math.abs(loc4.getTheta() - angleDistanceDiagonal);
        assertTrue(phiError4 < ERROR_MARGIN_RAD);
        assertTrue(thetaError4 < ERROR_MARGIN_RAD);
        
        // Direction south.
        Direction direction5 = new Direction(Math.PI);
        Coordinates loc5 = locInit.getNewLocation(direction5, distance);
        double phiError5 = Math.abs(loc5.getPhi() - ((Math.PI / 2D) + angleDistance));
        double thetaError5 = Math.abs(loc5.getTheta() - 0D);
        assertTrue(phiError5 < ERROR_MARGIN_RAD);
        assertTrue(thetaError5 < ERROR_MARGIN_RAD);
        
        // Direction south-west.
        Direction direction6 = new Direction(Math.PI * 5D / 4D);
        Coordinates loc6 = locInit.getNewLocation(direction6, distance);
        double phiError6 = Math.abs(loc6.getPhi() - ((Math.PI / 2D) + angleDistanceDiagonal));
        double thetaError6 = Math.abs(loc6.getTheta() - ((2D * Math.PI) - angleDistanceDiagonal));
        assertTrue(phiError6 < ERROR_MARGIN_RAD);
        assertTrue(thetaError6 < ERROR_MARGIN_RAD);
        
        // Direction west.
        Direction direction7 = new Direction(Math.PI * 3D / 2D);
        Coordinates loc7 = locInit.getNewLocation(direction7, distance);
        double phiError7 = Math.abs(loc7.getPhi() - (Math.PI / 2D));
        double thetaError7 = Math.abs(loc7.getTheta() - ((2D * Math.PI) - angleDistance));
        assertTrue(phiError7 < ERROR_MARGIN_RAD);
        assertTrue(thetaError7 < ERROR_MARGIN_RAD);
        
        // Direction north-west.
        Direction direction8 = new Direction(Math.PI * 7D / 4D);
        Coordinates loc8 = locInit.getNewLocation(direction8, distance);
        double phiError8 = Math.abs(loc8.getPhi() - ((Math.PI / 2D) - angleDistanceDiagonal));
        double thetaError8 = Math.abs(loc8.getTheta() - ((2D * Math.PI) - angleDistanceDiagonal));
        assertTrue(phiError8 < ERROR_MARGIN_RAD);
        assertTrue(thetaError8 < ERROR_MARGIN_RAD);
    }
}