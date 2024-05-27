package com.mars_sim.core.vehicle.task;


import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.vehicle.StatusType;
import com.mars_sim.mapdata.location.Direction;
import com.mars_sim.mapdata.location.LocalPosition;

public class DriveGroundVehicleTest extends AbstractMarsSimUnitTest {
    private static final int DIST = 1;  // Drive 1 KM
    private static final double RESOURCE_AMOUNT = 100D;

    public void testDriveVehicle() {
        var s = buildSettlement("Start");
        var v = buildRover(s, "Test", LocalPosition.DEFAULT_POSITION);
        v.storeAmountResource(v.getFuelTypeID(), RESOURCE_AMOUNT);
        v.storeAmountResource(ResourceUtil.oxygenID, RESOURCE_AMOUNT);

        // move to plant
        v.transfer(getSim().getUnitManager().getMarsSurface());

        var p = buildPerson("Driver", s, JobType.PILOT);
        p.transfer(v);

        var targetDir = new Direction(0.1);
        var dest = v.getCoordinates().getNewLocation(targetDir, DIST);
        var task = new DriveGroundVehicle(p, v, dest, getSim().getMasterClock().getMarsTime(),
                                    0D);

        assertFalse("Task created", task.isDone());
        assertEquals("Driver", p, v.getOperator());

        // Execute few calls to get driver positioned and moving
        executeTask(p, task, 10);
        assertEquals("Vehicle is moving", OperateVehicle.MOBILIZE, task.getPhase());
        assertGreaterThan("Vehicle speed", 0D, v.getSpeed());
        assertEquals("Vehicle primary status", StatusType.MOVING, v.getPrimaryStatus());

        // Drive the rest
        executeTaskUntilPhase(p, task, 1000);
        assertEquals("Vehicle oddmeter", DIST, Math.round(v.getOdometerMileage()));
        assertEquals("Vehicle at destination", dest, v.getCoordinates());
        assertEquals("Vehicle end primary status", StatusType.PARKED, v.getPrimaryStatus());
        assertLessThan("Oxygen stored", RESOURCE_AMOUNT, v.getAmountResourceStored(ResourceUtil.oxygenID));
        assertLessThan("Fuel stored", RESOURCE_AMOUNT, v.getAmountResourceStored(v.getFuelTypeID()));

        assertTrue("Task complete", task.isDone());
    }

    public void testDriveVehicleNoFuel() {
        var s = buildSettlement("Start");
        var v = buildRover(s, "Test", LocalPosition.DEFAULT_POSITION);
        v.storeAmountResource(v.getFuelTypeID(), 100);
        v.storeAmountResource(ResourceUtil.oxygenID, 100);

        // move to plant
        v.transfer(getSim().getUnitManager().getMarsSurface());

        var p = buildPerson("Driver", s, JobType.PILOT);
        p.transfer(v);

        var targetDir = new Direction(0.1);
        var dest = v.getCoordinates().getNewLocation(targetDir, DIST);
        var task = new DriveGroundVehicle(p, v, dest, getSim().getMasterClock().getMarsTime(),
                                    0D);

        assertFalse("Task created", task.isDone());

        // Execute few calls to get driver positioned and moving then remove fuel
        executeTask(p, task, 10);
        v.retrieveAmountResource(v.getFuelTypeID(), v.getAmountResourceStored(v.getFuelTypeID()));
        assertEquals("Fuel emptied", 0.0D, v.getAmountResourceStored(v.getFuelTypeID()));

        executeTask(p, task, 10);
        assertEquals("Vehicle end primary status", StatusType.PARKED, v.getPrimaryStatus());
        assertTrue("Marked out of fuel", v.haveStatusType(StatusType.OUT_OF_FUEL));
        assertTrue("Task complete", task.isDone());
    }
}
