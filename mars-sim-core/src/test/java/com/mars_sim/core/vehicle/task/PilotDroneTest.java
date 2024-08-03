/*
 * Mars Simulation Project
 * PilotDroneTest.java
 * @date 2024-07-16
 * @author Barry Evans
 */

package com.mars_sim.core.vehicle.task;


import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.equipment.Battery;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.vehicle.Drone;
import com.mars_sim.core.vehicle.Flyer;
import com.mars_sim.core.vehicle.StatusType;
import com.mars_sim.core.vehicle.VehicleController;
import com.mars_sim.mapdata.location.Direction;

public class PilotDroneTest extends AbstractMarsSimUnitTest {
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
	double vPropeller = voltage * 60;	 

	double efficiencyMotor = 0.9;
	 
	double secs = 20;
	
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
        
        assertEquals("Vehicle is being mobilized", OperateVehicle.MOBILIZE, task.getPhase());
        
        // Execute few calls to get driver positioned and moving
        executeTask(p, task, 10);
           
        v.setPrimaryStatus(StatusType.PARKED);
        
        assertEquals("Vehicle primary status", StatusType.PARKED, v.getPrimaryStatus());
        
        // Execute few calls to get driver positioned and moving
        executeTask(p, task, 50);
             
        v.setPrimaryStatus(StatusType.MOVING);
        v.setSpeed(1.1);

        assertGreaterThan("Vehicle speed", 0D, v.getSpeed());
        assertEquals("Vehicle primary status", StatusType.MOVING, v.getPrimaryStatus());

//        assertEquals("Vehicle oddmeter", Math.round(DIST), Math.round(v.getOdometerMileage() * 10.0) / 10.0);
//        assertEquals("Vehicle at destination", dest, v.getCoordinates());
      
        // Drive the rest
        executeTaskUntilPhase(p, task, 100);
           
//        assertEquals("Vehicle end primary status", StatusType.PARKED, v.getPrimaryStatus());

        // There is a problem in the VehicleController because it does not consume fuel or oxygen
        // Need to move the fuel logic into the VehicleController which should be rename VehicleEngine
        // which can cover electric engine or combustion engine
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

        System.out.println("1. odo: " + Math.round(v.getOdometerMileage() * 10.0) / 10.0);
        System.out.println("dist: " + Math.round(task.getDistanceToDestination() * 10.0) / 10.0);
        System.out.println("speed: " + Math.round(v.getSpeed() * 10.0) / 10.0);
        System.out.println(v + "'s location: " + v.getCoordinates().getFormattedString());
        System.out.println("Batt %: " + Math.round(v.getBatteryPercent() * 10.0) / 10.0);
        
        // Execute few calls to get driver positioned and moving then remove fuel
        executeTask(p, task, 7);

        System.out.println("2. odo: " + Math.round(v.getOdometerMileage() * 10.0) / 10.0);
        System.out.println("dist: " + Math.round(task.getDistanceToDestination() * 10.0) / 10.0);
        System.out.println("speed: " + Math.round(v.getSpeed() * 10.0) / 10.0);
        System.out.println(v + "'s location: " + v.getCoordinates().getFormattedString());
        System.out.println("Batt %: " + Math.round(v.getBatteryPercent() * 10.0) / 10.0);
        
        // Now that regen is possible for recharging the battery, the line below won't work
//        assertEqualLessThan("Battery Percent", originalBatteryPercent, nowBatteryPercent);
            
        assertEqualLessThan("Oxygen stored", OXYGEN_AMOUNT, v.getAmountResourceStored(ResourceUtil.oxygenID));
        assertEqualLessThan("Fuel stored", METHANOL_AMOUNT, v.getAmountResourceStored(v.getFuelTypeID()));
       
        // Now it will rely on its battery to power the flight

        executeTask(p, task, 7);     

        System.out.println("3. odo: " + Math.round(v.getOdometerMileage() * 10.0) / 10.0);
        System.out.println("dist: " + Math.round(task.getDistanceToDestination() * 10.0) / 10.0);
        System.out.println("speed: " + Math.round(v.getSpeed() * 10.0) / 10.0);
        System.out.println(v + "'s location: " + v.getCoordinates().getFormattedString());
        System.out.println("Batt %: " + Math.round(v.getBatteryPercent() * 10.0) / 10.0);
        
        // Need to find out in what situation a pilot may stop operating the drone, thus
        // causing task.getPhase() to be null from time to time
//        if (task.getPhase() != null)
//        	assertEquals("Vehicle end primary status", StatusType.MOVING, v.getPrimaryStatus());
//        else 
//        	assertEquals("Vehicle end primary status", StatusType.PARKED, v.getPrimaryStatus());
        
        // Pilot
        executeTask(p, task, 7);  

        System.out.println("4. odo: " + Math.round(v.getOdometerMileage() * 10.0) / 10.0);
        System.out.println("dist: " + Math.round(task.getDistanceToDestination() * 10.0) / 10.0);
        System.out.println("speed: " + Math.round(v.getSpeed() * 10.0) / 10.0);
        System.out.println(v + "'s location: " + v.getCoordinates().getFormattedString());
        System.out.println("Batt %: " + Math.round(v.getBatteryPercent() * 10.0) / 10.0);
        

        // Take away the fuel
        v.retrieveAmountResource(v.getFuelTypeID(), v.getAmountResourceStored(v.getFuelTypeID()));
        assertEquals("Fuel emptied", 0.0D, v.getAmountResourceStored(v.getFuelTypeID()));
      
        // Pilot
        executeTask(p, task, 8);

        System.out.println("5. odo: " + Math.round(v.getOdometerMileage() * 10.0) / 10.0);
        System.out.println("dist: " + Math.round(task.getDistanceToDestination() * 10.0) / 10.0);
        System.out.println("speed: " + Math.round(v.getSpeed() * 10.0) / 10.0);
        System.out.println(v + "'s location: " + v.getCoordinates().getFormattedString());
        System.out.println("Batt %: " + Math.round(v.getBatteryPercent() * 10.0) / 10.0);
        
        assertTrue("Marked out of fuel", v.haveStatusType(StatusType.OUT_OF_FUEL));
       
        // Pilot the rest
        executeTaskUntilPhase(p, task, 10);
        
        System.out.println("6. odo: " + Math.round(v.getOdometerMileage() * 10.0) / 10.0);
        System.out.println("dist: " + Math.round(task.getDistanceToDestination() * 10.0) / 10.0);
        System.out.println("speed: " + Math.round(v.getSpeed() * 10.0) / 10.0);
        System.out.println(v + "'s location: " + v.getCoordinates().getFormattedString());
        System.out.println("Batt %: " + Math.round(v.getBatteryPercent() * 10.0) / 10.0);
        
        assertTrue("Task complete", task.isDone());
    }
    
    /**
     * Tests the thrust calculation for ascent flight condition.
     */
    public void testAscentThrust() {
    	
      	var s = buildSettlement("Test Settlement");
        var v = buildDrone(s, "Test Drone");
        v.storeAmountResource(v.getFuelTypeID(), METHANOL_AMOUNT);
        v.storeAmountResource(ResourceUtil.oxygenID, OXYGEN_AMOUNT);
        
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
		 
		System.out.println("a. Ascent - powerDrone: " + Math.round(powerDrone * 1000.0)/1000.0 + " W  "
				 + "thrust: " + Math.round(thrustForceTotal * 1000.0)/1000.0 + " N  "
				 + "radiusPropellerSquare: " + Math.round(radiusPropellerSquare * 1000.0)/1000.0 + " m2  "
				 + "ratio: " + Math.round(thrustToWeightRatio * 1000.0)/1000.0 + "  "
				 + "height: " + Math.round(currentHoveringHeight * 10.0)/10.0 + " km  "
				 + "PE: " + Math.round(potentialEnergyDrone * 1000.0)/1000.0 + " J  "
				 + "gainPE: " + Math.round(gainPotentialEnergy * 1000.0)/1000.0 + " J  "
//				 + "lostPE: " + Math.round(lostPotentialEnergy * 1000.0)/1000.0 + " J  "
				 + "ascentHeight: " + Math.round(ascentHeight * 1000.0)/1000.0 + " m  "
				 + "currentHoveringHeight: " + Math.round(currentHoveringHeight * 1000.0)/1000.0 + " km  ");
		
    }
    
    /**
     * Tests the thrust calculation for descent flight condition.
     */
    public void testDescentThrust() {
    	
      	var s = buildSettlement("Test Settlement");
        var v = buildDrone(s, "Test Drone");
        v.storeAmountResource(v.getFuelTypeID(), METHANOL_AMOUNT);
        v.storeAmountResource(ResourceUtil.oxygenID, OXYGEN_AMOUNT);
        
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
		 
		System.out.println("b. Descent - powerDrone: " + Math.round(powerDrone * 1000.0)/1000.0 + " W  "
				 + "thrust: " + Math.round(thrustForceTotal * 1000.0)/1000.0 + " N  "
				 + "radiusPropellerSquare: " + Math.round(radiusPropellerSquare * 1000.0)/1000.0 + " m2  "
				 + "ratio: " + Math.round(thrustToWeightRatio * 1000.0)/1000.0 + "  "
				 + "height: " + Math.round(currentHoveringHeight * 10.0)/10.0 + " km  "
				 + "PE: " + Math.round(potentialEnergyDrone * 1000.0)/1000.0 + " J  "
				 + "gainPE: " + Math.round(gainPotentialEnergy * 1000.0)/1000.0 + " J  "
//				 + "lostPE: " + Math.round(lostPotentialEnergy * 1000.0)/1000.0 + " J  "
				 + "ascentHeight: " + Math.round(ascentHeight * 1000.0)/1000.0 + " m  "
				 + "currentHoveringHeight: " + Math.round(currentHoveringHeight * 1000.0)/1000.0 + " km  ");
		
    }
    
    /**
     * Tests the thrust calculation for hovering. 
     */
    public void testHoverThrust() {
    	var s = buildSettlement("Test Settlement");
        var v = buildDrone(s, "Test Drone");
        v.storeAmountResource(v.getFuelTypeID(), METHANOL_AMOUNT);
        v.storeAmountResource(ResourceUtil.oxygenID, OXYGEN_AMOUNT);
        
    	double currentHoveringHeight = .1;
    	
		double ascentHeight = 0;
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
	
		System.out.println("c. Hovering - powerDrone: " + Math.round(powerDrone * 1000.0)/1000.0 + " W  "
				 + "thrust: " + Math.round(thrustForceTotal * 1000.0)/1000.0 + " N  "
				 + "radiusPropellerSquare: " + Math.round(radiusPropellerSquare * 1000.0)/1000.0 + " m2  "
				 + "ratio: " + Math.round(thrustToWeightRatio * 1000.0)/1000.0 + "  "
				 + "height: " + Math.round(currentHoveringHeight * 10.0)/10.0 + " km"
				 );
    	
    }
 
    /**
     * Tests the thrust calculation for tilted flight condition.
     */
    public void testTiltedThrust() {
    	
      	var s = buildSettlement("Test Settlement");
        var v = buildDrone(s, "Test Drone");
        v.storeAmountResource(v.getFuelTypeID(), METHANOL_AMOUNT);
        v.storeAmountResource(ResourceUtil.oxygenID, OXYGEN_AMOUNT);
        
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
		 
		System.out.println("d. Tilted - powerDrone: " + Math.round(powerDrone * 1000.0)/1000.0 + " W  "
				 + "thrust: " + Math.round(thrustForceTotal * 1000.0)/1000.0 + " N  "
				 + "radiusPropellerSquare: " + Math.round(radiusPropellerSquare * 1000.0)/1000.0 + " m2  "
				 + "ratio: " + Math.round(thrustToWeightRatio * 1000.0)/1000.0 + "  "
				 + "height: " + Math.round(currentHoveringHeight * 10.0)/10.0 + " km  "
				 + "PE: " + Math.round(potentialEnergyDrone * 1000.0)/1000.0 + " J  "
				 + "gainPE: " + Math.round(gainPotentialEnergy * 1000.0)/1000.0 + " J  "
				 + "vAirFlow: " + Math.round(vAirFlow * 1000.0)/1000.0 + " J  "
				 + "ascentHeight: " + Math.round(ascentHeight * 1000.0)/1000.0 + " m  "
				 + "currentHoveringHeight: " + Math.round(currentHoveringHeight * 1000.0)/1000.0 + " km  ");
		
    }
}
