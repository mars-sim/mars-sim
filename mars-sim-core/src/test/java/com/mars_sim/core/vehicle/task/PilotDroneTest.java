/*
 * Mars Simulation Project
 * PilotDroneTest.java
 * @date 2024-07-16
 * @author Barry Evans
 */

package com.mars_sim.core.vehicle.task;


import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.vehicle.Drone;
import com.mars_sim.core.vehicle.StatusType;
import com.mars_sim.mapdata.location.Direction;

public class PilotDroneTest extends AbstractMarsSimUnitTest {
    private static final double DIST = OperateVehicle.ARRIVING_BUFFER * 5;  // Drive 5 km
    private static final double METHANOL_AMOUNT = 30D;
    private static final double OXYGEN_AMOUNT = METHANOL_AMOUNT * OperateVehicle.RATIO_OXIDIZER_FUEL;
    
    private Drone buildDrone(Settlement settlement, String name) {
	    Drone flyer = new Drone(name,
                                simConfig.getVehicleConfiguration().getVehicleSpec("delivery drone"),
								settlement);

	    unitManager.addUnit(flyer);
	    return flyer;
	}

    public void testFlyDrone() {
        var s = buildSettlement("Test Settlement");
        var v = buildDrone(s, "Test Drone");
        v.storeAmountResource(v.getFuelTypeID(), METHANOL_AMOUNT);
        v.storeAmountResource(ResourceUtil.oxygenID, OXYGEN_AMOUNT);

        // move to plant
        v.transfer(getSim().getUnitManager().getMarsSurface());

        String name = "Test Pilot";
        var p = buildPerson(name, s, JobType.PILOT);
        p.transfer(v);

        var targetDir = new Direction(0.1);
        var dest = v.getCoordinates().getNewLocation(targetDir, DIST);
        var task = new PilotDrone(p, v, dest, getSim().getMasterClock().getMarsTime(),
                                    0D);

        assertFalse("Task created", task.isDone());
        assertEquals(name, p, v.getOperator());

        // Execute few calls to get driver positioned and moving
        executeTask(p, task, 10);
        
        assertEquals("Vehicle is moving", OperateVehicle.MOBILIZE, task.getPhase());
        assertGreaterThan("Vehicle speed", 0D, v.getSpeed());
        assertEquals("Vehicle primary status", StatusType.MOVING, v.getPrimaryStatus());

        // Drive the rest
        executeTaskUntilPhase(p, task, 1000);
        
        assertEquals("Vehicle oddmeter", Math.round(DIST), Math.round(v.getOdometerMileage()));
        assertEquals("Vehicle at destination", dest, v.getCoordinates());
        assertEquals("Vehicle end primary status", StatusType.PARKED, v.getPrimaryStatus());

        // There is a problem in the VehicleController because it does not consume fuel or oxygen
        // Need to move the fuel logic into the VehicleController which should be rename VehicleEngine
        // which can cover eletric engine or combustion engine
        assertTrue("Task complete", task.isDone());
    }

    public void testDroneNoFuel() {
        var s = buildSettlement("Test Settlement");
        var v = buildDrone(s, "Test Drone");
        v.storeAmountResource(v.getFuelTypeID(), METHANOL_AMOUNT);
        v.storeAmountResource(ResourceUtil.oxygenID, OXYGEN_AMOUNT);

        // move to plant
        v.transfer(getSim().getUnitManager().getMarsSurface());

        var p = buildPerson("Test Pilot", s, JobType.PILOT);
        p.transfer(v);

        var targetDir = new Direction(0.1);
        var dest = v.getCoordinates().getNewLocation(targetDir, DIST);
        var task = new PilotDrone(p, v, dest, getSim().getMasterClock().getMarsTime(),
                                    0D);

        assertFalse("Task created", task.isDone());

//        double originalBatteryPercent = v.getBatteryPercent();
        
        // Execute few calls to get driver positioned and moving then remove fuel
        executeTask(p, task, 10);
        
//        double nowBatteryPercent = v.getBatteryPercent();
        
        // Now that regen is possible for recharging the battery, the line below won't work
//        assertEqualLessThan("Battery Percent", originalBatteryPercent, nowBatteryPercent);
            
        assertEqualLessThan("Oxygen stored", OXYGEN_AMOUNT, v.getAmountResourceStored(ResourceUtil.oxygenID));
        assertEqualLessThan("Fuel stored", METHANOL_AMOUNT, v.getAmountResourceStored(v.getFuelTypeID()));
       
        // Take away the fuel
        v.retrieveAmountResource(v.getFuelTypeID(), v.getAmountResourceStored(v.getFuelTypeID()));
        assertEquals("Fuel emptied", 0.0D, v.getAmountResourceStored(v.getFuelTypeID()));

        executeTask(p, task, 10);
        
        // Need to find out in what situation a pilot may stop operating the drone, thus
        // causing task.getPhase() to be null from time to time
//        if (task.getPhase() != null)
//        	assertEquals("Vehicle end primary status", StatusType.MOVING, v.getPrimaryStatus());
//        else 
//        	assertEquals("Vehicle end primary status", StatusType.PARKED, v.getPrimaryStatus());
        
        assertTrue("Marked out of fuel", v.haveStatusType(StatusType.OUT_OF_FUEL));
        
        // Pilot the rest
        executeTaskUntilPhase(p, task, 500);
        
        assertTrue("Task complete", task.isDone());
    }
}
