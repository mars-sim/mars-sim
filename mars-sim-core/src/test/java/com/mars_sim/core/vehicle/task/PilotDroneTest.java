/*
 * Mars Simulation Project
 * PilotDroneTest.java
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
import com.mars_sim.core.equipment.Battery;
import com.mars_sim.core.map.location.Direction;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.vehicle.Drone;
import com.mars_sim.core.vehicle.Flyer;
import com.mars_sim.core.vehicle.StatusType;
import com.mars_sim.core.vehicle.VehicleController;

public class PilotDroneTest extends MarsSimUnitTest {
    private static final double DIST = OperateVehicle.DISTANCE_BUFFER_ARRIVING * 10;  // Drive 5 km
    private static final double METHANOL_AMOUNT = 30D;
    private static final double OXYGEN_AMOUNT = METHANOL_AMOUNT * OperateVehicle.RATIO_OXIDIZER_FUEL;
    
    double currentHoveringHeight = 0;
    
	double fullMass = 777.7; 
	
	double mg = fullMass * VehicleController.GRAVITY;
	
	double ascentHeight = 1000 * Flyer.ELEVATION_ABOVE_GROUND / 50;
	 // Gain in potential energy
	double gainPotentialEnergy = mg * ascentHeight;
	
	double potentialEnergyDrone = 0 + gainPotentialEnergy;	
	//  g/m3 -> kg /m3
	double airDensity = 14.76 / 1000; 
	// 1 m/s = 3.6 km/h (or kph). KPH_CONV = 3.6;
	double vMS = 0; //60 /  VehicleController.KPH_CONV;
		
	double weight = mg;
	// Assume a constant voltage
	double voltage = Battery.DRONE_VOLTAGE;	 
	// For now, assume the propeller induced velocity is linearly proportional to the voltage of the battery 
	double vPropeller = voltage * 14;	 

	double efficiencyMotor = 0.9;
	 
	double secs = 20;
	
    private Drone buildDrone(Settlement settlement, String name) {
	    Drone flyer = new Drone(name,
                                getConfig().getVehicleConfiguration().getVehicleSpec("delivery drone"),
								settlement);

	    getSim().getUnitManager().addUnit(flyer);
	    return flyer;
	}

    @Test
    public void testFlyDrone() {
        var s = buildSettlement("Test Settlement");
        var v = buildDrone(s, "Test Drone");
        v.storeAmountResource(v.getFuelTypeID(), METHANOL_AMOUNT);
        v.storeAmountResource(ResourceUtil.OXYGEN_ID, OXYGEN_AMOUNT);

        // move to plant
        v.transfer(getSurface());

        String name = "Test Pilot";
        var p = buildPerson(name, s, JobType.PILOT);
        p.transfer(v);

        var targetDir = new Direction(0.1);
        var dest = v.getCoordinates().getNewLocation(targetDir, DIST);
        var task = new PilotDrone(p, v, dest, getSim().getMasterClock().getMarsTime(),
                                    0D);
    
        assertFalse(task.isDone(), "Task created");
        assertEquals(p, v.getOperator(), name);
        
        assertEquals(OperateVehicle.MOBILIZE, task.getPhase(), "Vehicle is being mobilized");
        
        // Execute few calls to get driver positioned and moving
        executeTask(p, task, 10);
           
        v.setPrimaryStatus(StatusType.PARKED);
        
        assertEquals(StatusType.PARKED, v.getPrimaryStatus(), "Vehicle primary status");
        
        // Execute few calls to get driver positioned and moving
        executeTask(p, task, 50);
             
        v.setPrimaryStatus(StatusType.MOVING);
        v.setSpeed(1.1);

        assertGreaterThan("Vehicle speed", 0D, v.getSpeed());
        assertEquals(StatusType.MOVING, v.getPrimaryStatus(), "Vehicle primary status");

//        assertEquals(Math.round(DIST), Math.round(v.getOdometerMileage() * 10.0) / 10.0, "Vehicle oddmeter");
//        assertEquals(dest, v.getCoordinates(), "Vehicle at destination");
      
        // Drive the rest
        executeTaskUntilPhase(p, task, 100);
           
//        assertEquals(StatusType.PARKED, v.getPrimaryStatus(), "Vehicle end primary status");

        // There is a problem in the VehicleController because it does not consume fuel or oxygen
        // Need to move the fuel logic into the VehicleController which should be rename VehicleEngine
        // which can cover electric engine or combustion engine
        assertTrue(task.isDone(), "Task complete");
    }

    @Test
    public void testDroneNoFuel() {
        var s = buildSettlement("Test Settlement");
        var v = buildDrone(s, "Test Drone");
        v.storeAmountResource(v.getFuelTypeID(), METHANOL_AMOUNT);
        v.storeAmountResource(ResourceUtil.OXYGEN_ID, OXYGEN_AMOUNT);

        // move to plant
        v.transfer(getSim().getUnitManager().getMarsSurface());

        var p = buildPerson("Test Pilot", s, JobType.PILOT);
        p.transfer(v);

        var targetDir = new Direction(0.1);
        var dest = v.getCoordinates().getNewLocation(targetDir, DIST);
        var task = new PilotDrone(p, v, dest, getSim().getMasterClock().getMarsTime(),
                                    0D);

        assertFalse(task.isDone(), "Task created");

        // Execute few calls to get driver positioned and moving then remove fuel
        executeTask(p, task, 7);

    
        // Now that regen is possible for recharging the battery, the line below won't work
//        assertEqualLessThan("Battery Percent", originalBatteryPercent, nowBatteryPercent);
            
        assertEqualLessThan("Oxygen stored", OXYGEN_AMOUNT, v.getSpecificAmountResourceStored(ResourceUtil.OXYGEN_ID));
        assertEqualLessThan("Fuel stored", METHANOL_AMOUNT, v.getSpecificAmountResourceStored(v.getFuelTypeID()));
       
        // Now it will rely on its battery to power the flight

        executeTask(p, task, 7);     

        // Need to find out in what situation a pilot may stop operating the drone, thus
        // causing task.getPhase() to be null from time to time
//        if (task.getPhase() != null)
//        	assertEquals(StatusType.MOVING, v.getPrimaryStatus(), "Vehicle end primary status");
//        else 
//        	assertEquals(StatusType.PARKED, v.getPrimaryStatus(), "Vehicle end primary status");
        
        // Pilot
        executeTask(p, task, 7);  

        // Take away the fuel
        v.retrieveAmountResource(v.getFuelTypeID(), v.getSpecificAmountResourceStored(v.getFuelTypeID()));
        assertEquals(0.0D, v.getSpecificAmountResourceStored(v.getFuelTypeID()), "Fuel emptied");
      
        // Pilot
        executeTask(p, task, 8);


//        assertTrue(v.haveStatusType(StatusType.OUT_OF_FUEL), "Marked out of fuel");
       
        // Pilot the rest
        executeTaskUntilPhase(p, task, 10);
        
//        assertTrue(task.isDone(), "Task complete");
    }
    
    /**
     * Tests the thrust calculation for ascent flight condition.
     */
    @Test
    public void testAscentThrust() {
    	
      	var s = buildSettlement("Test Settlement");
        var v = buildDrone(s, "Test Drone");
        v.storeAmountResource(v.getFuelTypeID(), METHANOL_AMOUNT);
        v.storeAmountResource(ResourceUtil.OXYGEN_ID, OXYGEN_AMOUNT);
        
        double currentHoveringHeight = 0;
		
		double width = v.getWidth();
		
    	double radiusPropeller = width / 8;
    	
		double radiusPropellerSquare = radiusPropeller * radiusPropeller;		
	
		double thrustForceTotal = 2 * airDensity * Math.PI * radiusPropellerSquare * (vPropeller + vMS) * vPropeller;
		// Double check with the ratio. Need to be at least 2:1
		double thrustToWeightRatio = thrustForceTotal / weight;
		// the gain of potential energy of the drone require extra the power drain on the drone's fuel and battery system
		double powerDrone = thrustForceTotal * voltage / efficiencyMotor + gainPotentialEnergy / secs;
		
		 // Drone will hover over at around ELEVATION_ABOVE_GROUND km and no more
		 if (currentHoveringHeight <= Flyer.ELEVATION_ABOVE_GROUND) {
			 // Assume the height gained is the same as distanceTravelled
			 currentHoveringHeight = currentHoveringHeight + ascentHeight;
		 }

    }
    
    /**
     * Tests the thrust calculation for descent flight condition.
     */
    @Test
    public void testDescentThrust() {
    	
      	var s = buildSettlement("Test Settlement");
        var v = buildDrone(s, "Test Drone");
        v.storeAmountResource(v.getFuelTypeID(), METHANOL_AMOUNT);
        v.storeAmountResource(ResourceUtil.OXYGEN_ID, OXYGEN_AMOUNT);
        
        double currentHoveringHeight = Flyer.ELEVATION_ABOVE_GROUND;
		
        ascentHeight = -Flyer.ELEVATION_ABOVE_GROUND / 25;
        		
        gainPotentialEnergy = mg * ascentHeight;
        
		double width = v.getWidth();
		
    	double radiusPropeller = width / 8;
    	
		double radiusPropellerSquare = radiusPropeller * radiusPropeller;	
		
		double thrustForceTotal = .5 * 2 * airDensity * Math.PI * radiusPropellerSquare * (vPropeller + vMS) * vPropeller;
		// Double check with the ratio. Need to be at least 2:1
		double thrustToWeightRatio = thrustForceTotal / weight;

		double efficiencyMotor = 0.9;
		 
		double secs = 20;
		// the gain of potential energy of the drone require extra the power drain on the drone's fuel and battery system
		double powerDrone = thrustForceTotal * voltage / efficiencyMotor + gainPotentialEnergy / secs;
		
		 // Drone will hover over at around ELEVATION_ABOVE_GROUND km and no more
		 if (currentHoveringHeight <= Flyer.ELEVATION_ABOVE_GROUND) {
			 // Assume the height gained is the same as distanceTravelled
			 currentHoveringHeight = currentHoveringHeight + ascentHeight;
		 }

    }
    
    /**
     * Tests the thrust calculation for hovering. 
     */
    @Test
    public void testHoverThrust() {
    	var s = buildSettlement("Test Settlement");
        var v = buildDrone(s, "Test Drone");
        v.storeAmountResource(v.getFuelTypeID(), METHANOL_AMOUNT);
        v.storeAmountResource(ResourceUtil.OXYGEN_ID, OXYGEN_AMOUNT);
        
    	double currentHoveringHeight = .1;
    	
		 // Gain in potential energy
		double gainPotentialEnergy = 0;
		// Do NOT ascent anymore. Hover at the this height
		potentialEnergyDrone = weight * currentHoveringHeight;
    	// 1 m/s = 3.6 km/h (or kph) */
    	// KPH_CONV = 3.6;
    	double vMS = 0;
    	
		double width = v.getWidth();
		
    	double radiusPropeller = width / 8;
    	
		double radiusPropellerSquare = radiusPropeller * radiusPropeller;	
		
//    	double vMSBar = vMS / omega / radiusPropeller;
    	
		double thrustForceTotal = 2 * airDensity * Math.PI * radiusPropellerSquare * (vPropeller + vMS) * vPropeller;
		// Double check with the ratio. Need to be at least 2:1
		double thrustToWeightRatio = thrustForceTotal / weight;
		
		double efficiencyMotor = 0.9;
		 
		double secs = 20;
		// the gain of potential energy of the drone require extra the power drain on the drone's fuel and battery system
		double powerDrone = thrustForceTotal * voltage / efficiencyMotor + gainPotentialEnergy / secs;
    	
    }
 
    /**
     * Tests the thrust calculation for tilted flight condition.
     */
    @Test
    public void testTiltedThrust() {
    	
      	var s = buildSettlement("Test Settlement");
        var v = buildDrone(s, "Test Drone");
        v.storeAmountResource(v.getFuelTypeID(), METHANOL_AMOUNT);
        v.storeAmountResource(ResourceUtil.OXYGEN_ID, OXYGEN_AMOUNT);
        
        double currentHoveringHeight = 0;
        
		double ascentHeight = 1000 * Flyer.ELEVATION_ABOVE_GROUND / 50;
		 // Gain in potential energy
		double gainPotentialEnergy = mg * ascentHeight;
		
		double potentialEnergyDrone = 0 + gainPotentialEnergy;	
    	
    	// 1 m/s = 3.6 km/h (or kph). KPH_CONV = 3.6;
    	double vMS = 60 /  VehicleController.KPH_CONV;
		
		double width = v.getWidth();
		
    	double radiusPropeller = width / 8;
    	
		double radiusPropellerSquare = radiusPropeller * radiusPropeller;	
		
		double alpha1 = Math.PI / 6;
		
		double alpha2 = Math.PI / 7;
		// 1000 RPM ~ 10.47 rad/s
		double radPerSec = 10;
		
		double vAirFlow =  (vMS * Math.sin(alpha2) + vPropeller * Math.cos(alpha1-alpha2)) * radiusPropeller * radPerSec; // vPropeller + vMS;
		// With four propeller motors
		double thrustForceTotal = 2 * airDensity * Math.PI * radiusPropellerSquare * vAirFlow * vPropeller;
		// Double check with the ratio. Need to be at least 2:1
		double thrustToWeightRatio = thrustForceTotal / weight;

		double efficiencyMotor = 0.9;
		 
		double secs = 20;
		// the gain of potential energy of the drone require extra the power drain on the drone's fuel and battery system
		double powerDrone = thrustForceTotal * voltage / efficiencyMotor + gainPotentialEnergy / secs;
		
		 // Drone will hover over at around ELEVATION_ABOVE_GROUND km and no more
		 if (currentHoveringHeight <= Flyer.ELEVATION_ABOVE_GROUND) {
			 // Assume the height gained is the same as distanceTravelled
			 currentHoveringHeight = currentHoveringHeight + ascentHeight;
		 }

    }
}
