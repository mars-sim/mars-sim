/*
 * Mars Simulation Project
 * DriveGroundVehicleTest.java
 * @date 2024-07-16
 * @author Barry Evans
 */

package com.mars_sim.core.vehicle.task;


import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.vehicle.StatusType;
import com.mars_sim.mapdata.location.Coordinates;
import com.mars_sim.mapdata.location.Direction;
import com.mars_sim.mapdata.location.LocalPosition;

public class DriveGroundVehicleTest extends AbstractMarsSimUnitTest {
    private static final double DIST = OperateVehicle.DISTANCE_BUFFER_ARRIVING * 10;
    private static final double METHANOL_AMOUNT = 30D;
    private static final double OXYGEN_AMOUNT = METHANOL_AMOUNT * OperateVehicle.RATIO_OXIDIZER_FUEL;
    
    public void testDriveVehicle() {
        var s = buildSettlement("Test Settlement");
        var v = buildRover(s, "Test Rover", LocalPosition.DEFAULT_POSITION);
        v.storeAmountResource(v.getFuelTypeID(), METHANOL_AMOUNT);
        v.storeAmountResource(ResourceUtil.oxygenID, OXYGEN_AMOUNT);

        // move rover to outside
        v.transfer(getSim().getUnitManager().getMarsSurface());

        String name = "Test Driver";
        var p = buildPerson(name, s, JobType.PILOT);
        p.transfer(v);

        System.out.println(p + "'s location: " + v.getCoordinates().getFormattedString());
    
        var targetDir = new Direction(0.1);
        Coordinates dest = v.getCoordinates().getNewLocation(targetDir, DIST);
        var task = new DriveGroundVehicle(p, v, dest, getSim().getMasterClock().getMarsTime(),
                                    0D);

        System.out.println("DIST: " + DIST);
        System.out.println("dist0: " + Math.round(Coordinates.computeDistance(dest, v.getCoordinates())));
        
        assertFalse("Task created", task.isDone());
        assertEquals(name, p, v.getOperator());

        System.out.println("odo: " + Math.round(v.getOdometerMileage() * 10.0) / 10.0);
        System.out.println("dist: " + Math.round(task.getDistanceToDestination() * 10.0) / 10.0);
        System.out.println("speed: " + Math.round(v.getSpeed() * 10.0) / 10.0);
        System.out.println(p + "'s location: " + v.getCoordinates().getFormattedString());
        System.out.println(v + "'s location: " + v.getCoordinates().getFormattedString());
        
        // Execute few calls to get driver positioned and moving
        executeTask(p, task, 5);
        
        System.out.println("odo: " + Math.round(v.getOdometerMileage() * 10.0) / 10.0);
        System.out.println("dist: " + Math.round(task.getDistanceToDestination() * 10.0) / 10.0);
        System.out.println("speed: " + Math.round(v.getSpeed() * 10.0) / 10.0);
        System.out.println(p + "'s location: " + v.getCoordinates().getFormattedString());
        System.out.println(v + "'s location: " + v.getCoordinates().getFormattedString());
        
        // Execute few calls to get driver positioned and moving
        executeTask(p, task, 10);
        
        System.out.println("odo: " + Math.round(v.getOdometerMileage() * 10.0) / 10.0);
        System.out.println("dist: " + Math.round(task.getDistanceToDestination() * 10.0) / 10.0);
        System.out.println("speed: " + Math.round(v.getSpeed() * 10.0) / 10.0);
        System.out.println(p + "'s location: " + v.getCoordinates().getFormattedString());
        System.out.println(v + "'s location: " + v.getCoordinates().getFormattedString());
        
        assertEquals("Vehicle is moving", OperateVehicle.MOBILIZE, task.getPhase());
//        assertGreaterThan("Vehicle speed", 0D, v.getSpeed());
        assertEquals("Vehicle primary status", StatusType.MOVING, v.getPrimaryStatus());

        // Drive the rest
        executeTaskUntilPhase(p, task, 30);
        
        System.out.println("odo: " + Math.round(v.getOdometerMileage() * 10.0) / 10.0);
        System.out.println("dist: " + Math.round(task.getDistanceToDestination() * 10.0) / 10.0);
        System.out.println("speed: " + Math.round(v.getSpeed() * 10.0) / 10.0);
        System.out.println(p + "'s location: " + v.getCoordinates().getFormattedString());
        System.out.println(v + "'s location: " + v.getCoordinates().getFormattedString());
        
        assertEquals("Vehicle travelled distance", Math.round(DIST), Math.round(v.getOdometerMileage() + task.getDistanceToDestination()));

        // Drive the rest
        executeTaskUntilPhase(p, task, 100);
        
        assertEquals("Vehicle at destination", dest, v.getCoordinates());
        assertEquals("Vehicle end primary status", StatusType.PARKED, v.getPrimaryStatus());

        assertTrue("Task complete", task.isDone());
        
    }

    public void testDriveVehicleNoFuel() {
        var s = buildSettlement("Test Settlement");
        var v = buildRover(s, "Test Rover", LocalPosition.DEFAULT_POSITION);
        v.storeAmountResource(v.getFuelTypeID(), METHANOL_AMOUNT);
        v.storeAmountResource(ResourceUtil.oxygenID, OXYGEN_AMOUNT);

        // move to plant
        v.transfer(getSim().getUnitManager().getMarsSurface());

        var p = buildPerson("Test Driver", s, JobType.PILOT);
        p.transfer(v);

        var targetDir = new Direction(0.1);
        var dest = v.getCoordinates().getNewLocation(targetDir, DIST);
        var task = new DriveGroundVehicle(p, v, dest, getSim().getMasterClock().getMarsTime(),
                                    0D);

        assertFalse("Task created", task.isDone());
 
//        double originalBatteryPercent = v.getBatteryPercent();
        
        // Execute few calls to get driver positioned and moving then remove fuel
        executeTask(p, task, 10);
        
//        double nowBatteryPercent = v.getBatteryPercent();
        
        // Now that regen is possible for recharging the battery, the line below won't work
//        assertEqualLessThan("Battery Percent", originalBatteryPercent, nowBatteryPercent);
        
        // If Battery power is used, instead of fuel
        assertEqualLessThan("Oxygen stored", OXYGEN_AMOUNT, v.getAmountResourceStored(ResourceUtil.oxygenID));
        assertEqualLessThan("Fuel stored", METHANOL_AMOUNT, v.getAmountResourceStored(v.getFuelTypeID()));
        
        // Remove methanol
        v.retrieveAmountResource(v.getFuelTypeID(), v.getAmountResourceStored(v.getFuelTypeID()));
        assertEquals("Fuel emptied", 0.0D, v.getAmountResourceStored(v.getFuelTypeID()));

        executeTask(p, task, 10);
        
        // With battery, rover can still be moving
        // Need to find out in what situation a driver may stop operating the vehicle, thus
        // causing task.getPhase() to be null from time to time
        if (task.getPhase() != null)
        	assertEquals("Vehicle end primary status", StatusType.MOVING, v.getPrimaryStatus());
        else 
        	assertEquals("Vehicle end primary status", StatusType.PARKED, v.getPrimaryStatus());
        
//        assertFalse("Marked out of fuel", v.haveStatusType(StatusType.OUT_OF_FUEL));
        
        // Drive the rest
        executeTaskUntilPhase(p, task, 5000);
        
        assertTrue("Task complete", task.isDone());
    }
}
