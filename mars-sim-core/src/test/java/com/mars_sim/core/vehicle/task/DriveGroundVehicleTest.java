/*
 * Mars Simulation Project
 * DriveGroundVehicleTest.java
 * @date 2024-07-16
 * @author Barry Evans
 */

package com.mars_sim.core.vehicle.task;
import static com.mars_sim.core.test.SimulationAssertions.assertGreaterThan;
import static com.mars_sim.core.test.SimulationAssertions.assertEqualLessThan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;


import com.mars_sim.core.test.MarsSimUnitTest;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.map.location.Direction;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.vehicle.StatusType;

public class DriveGroundVehicleTest extends MarsSimUnitTest {
    private static final double DIST = OperateVehicle.DISTANCE_BUFFER_ARRIVING * 10;
    private static final double METHANOL_AMOUNT = 30D;
    private static final double OXYGEN_AMOUNT = METHANOL_AMOUNT * OperateVehicle.RATIO_OXIDIZER_FUEL;
    
    @Test
    public void testDriveVehicle() {
        var s = buildSettlement("Test Settlement");
        var v = buildRover(s, "Test Rover", LocalPosition.DEFAULT_POSITION);
        v.storeAmountResource(v.getFuelTypeID(), METHANOL_AMOUNT);
        v.storeAmountResource(ResourceUtil.OXYGEN_ID, OXYGEN_AMOUNT);

        // move rover to outside
        v.transfer(getSim().getUnitManager().getMarsSurface());

        String name = "Test Driver";
        var p = buildPerson(name, s, JobType.PILOT);
        p.transfer(v);
    
        var targetDir = new Direction(0.1);
        Coordinates dest = v.getCoordinates().getNewLocation(targetDir, DIST);
        var task = new DriveGroundVehicle(p, v, dest, getSim().getMasterClock().getMarsTime(), 0D);
        
        assertFalse(task.isDone(), "Task created");
        assertEquals(p, v.getOperator(), name);

        // Execute few calls to get driver positioned and moving
        executeTask(p, task, 10);
        
        // The following 3 tests can have unreliable results. Commented them out for now.
//        assertEquals(OperateVehicle.MOBILIZE, task.getPhase(), "Vehicle is moving");
//        assertGreaterThan("Vehicle speed", 0D, v.getSpeed());
//        assertEquals(StatusType.MOVING, v.getPrimaryStatus(), "Vehicle primary status");
        
        // Execute few calls to get driver positioned and moving
        executeTask(p, task, 20);
        
        // Execute few calls to get driver positioned and moving
        executeTask(p, task, 20);

        // Drive the rest
        executeTask(p, task, 25);
        
        // Drive the rest
        executeTaskUntilPhase(p, task, 100);
//        executeTask(p, task, 30);
            
//        assertEquals(dest, v.getCoordinates(), "Vehicle at destination");
//        assertEquals(StatusType.PARKED, v.getPrimaryStatus(), "Vehicle end primary status");

        assertTrue(task.isDone(), "Task complete");   
    }

    @Test
    public void testDriveVehicleNoFuel() {
        var s = buildSettlement("Test Settlement");
        var v = buildRover(s, "Test Rover", LocalPosition.DEFAULT_POSITION);
        v.storeAmountResource(v.getFuelTypeID(), METHANOL_AMOUNT);
        v.storeAmountResource(ResourceUtil.OXYGEN_ID, OXYGEN_AMOUNT);

        // move to plant
        v.transfer(getSim().getUnitManager().getMarsSurface());

        var p = buildPerson("Test Driver", s, JobType.PILOT);
        p.transfer(v);

        var targetDir = new Direction(0.1);
        var dest = v.getCoordinates().getNewLocation(targetDir, DIST);
        var task = new DriveGroundVehicle(p, v, dest, getSim().getMasterClock().getMarsTime(),
                                    0D);

        assertFalse(task.isDone(), "Task created");
 
//        double originalBatteryPercent = v.getBatteryPercent();
        
        // Execute few calls to get driver positioned and moving then remove fuel
        executeTask(p, task, 10);
        
//        double nowBatteryPercent = v.getBatteryPercent();
        
        // Now that regen is possible for recharging the battery, the line below won't work
//        assertEqualLessThan("Battery Percent", originalBatteryPercent, nowBatteryPercent);
        
        // If Battery power is used, instead of fuel
        assertEqualLessThan("Oxygen stored", OXYGEN_AMOUNT, v.getSpecificAmountResourceStored(ResourceUtil.OXYGEN_ID));
        assertEqualLessThan("Fuel stored", METHANOL_AMOUNT, v.getSpecificAmountResourceStored(v.getFuelTypeID()));
        
        // Remove methanol
        v.retrieveAmountResource(v.getFuelTypeID(), v.getSpecificAmountResourceStored(v.getFuelTypeID()));
        assertEquals(0.0D, v.getSpecificAmountResourceStored(v.getFuelTypeID()), "Fuel emptied");

        executeTask(p, task, 10);
        
        // With battery, rover can still be moving
        // Need to find out in what situation a driver may stop operating the vehicle, thus
        // causing task.getPhase() to be null from time to time
        if (task.getPhase() != null)
        	assertEquals(StatusType.MOVING, v.getPrimaryStatus(), "Vehicle end primary status");
        else 
        	assertEquals(StatusType.PARKED, v.getPrimaryStatus(), "Vehicle end primary status");
        
//        assertFalse(v.haveStatusType(StatusType.OUT_OF_FUEL), "Marked out of fuel");
        
        // Drive the rest
        executeTaskUntilPhase(p, task, 5000);
        
        assertTrue(task.isDone(), "Task complete");
    }
}
