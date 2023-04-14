/*
 * Mars Simulation Project
 * VehicleController.java
 * @date 2023-04-13
 * @author Manny Kung
 */

package org.mars_sim.msp.core.vehicle;

import java.io.Serializable;
import java.util.logging.Level;

import org.mars_sim.msp.core.equipment.Battery;
import org.mars_sim.msp.core.logging.SimLogger;

/**
 * This class represents the modeling of a motor controller, which comprises power 
 * electronics and embedded micro-computing elements that directs the energy flow 
 * to the motor of an electric vehicle.
 */
public class VehicleController implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(VehicleController.class.getName());

	private static final String KG = " kg   ";
	private static final String N = " N   ";
	private static final String KM_KG = " km/kg   ";
	private static final String WH_KM = " Wh/km   ";
	private static final String KM = " km   ";
	private static final String KW = " kW   ";
	private static final String KPH = " kph   ";
    /** Mars surface gravity is 3.72 m/s2. */
    private static final double GRAVITY = 3.72;
	/** Conversion factor : 1 Wh = 3.6 kilo Joules */
    private static final double JOULES_PER_WH = 3_600.0;
	/** Conversion factor : 1 m/s = 3.6 km/h (or kph) */
	private static final double KPH_CONV = 3.6;
	
    // Data members
	/**  Cache the time in hr. */ 
	private double hrsTimeCache;
	/** Cache the distance traveled in km. */ 
	private double distanceCache;
	/** Cache the fuel used in kg. */ 
	private double fuelUsedCache;	
	
	/** The vehicle to operate. */ 
	private Vehicle vehicle;
	/** The battery of the vehicle. */ 
	private Battery battery;
	
    /**
     * Constructor.
     * 
     * @param vehicle The vehicle requiring a controller.
     * 
     */
    public VehicleController(Vehicle vehicle) {
    	this.vehicle = vehicle;
    	battery = new Battery(vehicle);
    }
    
	/**
	 * Calculates and returns the amount of fuel to be used.
	 *
	 * @param beginningSpeed
	 * @param finalSpeed
	 * @param distance
	 * @param hrsTime
	 * @param remainingFuel
	 * @return
	 */
    public double calculateFuelUsed(double beginningSpeed, double finalSpeed, double distance, double hrsTime, double remainingFuel) {
    	double d_km = distance; // [in km]   
    	double u_ms = beginningSpeed; // [in m/s]
    	double u_kph = u_ms * KPH_CONV; // [in kph]
    	double hr = hrsTime; // [in hrs]
    	double secs = 3600 * hr; // [in s]
    	// v_kph may be negative
    	double v_kph = finalSpeed; // [in km/hr]
    	double v_ms = v_kph / KPH_CONV; // [in m/s]
    	// Note: if a_ms is negative, it gives off free momentum to move forward.
    	double a_ms = (v_ms - u_ms) / secs; // [in m/s2]
    	
		
        double mass = vehicle.getMass(); // [in kg]
        
        double initFE = vehicle.getInitialFuelEconomy(); // [in km/kg]
        // Calculate force against Mars surface gravity
        double fGravity = 0; 
        
        // Calculate force on rolling resistance 
        double fRolling = 0;
        
        // Calculate force on rolling resistance 
        double fGradingResistance = 0;
        
        if (vehicle instanceof Drone) {
            // For drones, it needs energy to ascend into the air and hover in the air
            // Note: Refine this equation for drones
        	 fGravity = GRAVITY * mass;
        	 
        	 // FUTURE : Account for the use of fuel or battery's power to ascend and descend 
        }
        
        double angle = 0;
        
        if (vehicle instanceof Rover) {
           	// For Ground rover, it doesn't need as much
        	angle = vehicle.getTerrainGrade();
            // In general, road load (friction) force = road rolling resistance coeff *  mass * gravity * cos (slope angle)
        	fRolling = 0.11 * mass * GRAVITY * Math.cos(angle); 

        	fGradingResistance = mass * GRAVITY * Math.sin(angle); 
        }
    
        double vSQ = v_ms * v_ms; // [in (m/s)^2]
        
        double fInitialFriction = 5.0 / (0.5 + v_ms);  //[in N]
        
        // Note : Aerodynamic drag force = air drag coeff * air density * vehicle frontal area / 2 * vehicle speed 
        double fAeroDrag = 0.4 * 0.02 * 1.5 / 2.0 * vSQ;
    	// Note: if a_ms is negative, fAccel will be negative and provides free momentum.
        double fAccel = mass * a_ms;

		if (a_ms < 0) {
			
			// FUTURE : Will consider how to convert excess kinetic energy to 
			// potential energy to be stored in on-board battery via regenerative braking
			
			logger.log(vehicle, Level.INFO, 20_000, "Reducing speed from " 
					+  Math.round(u_kph * 1000.0)/1000.0 + " kph "
					+ " to " + Math.round(v_kph * 1000.0)/1000.0
					+ " kph. Deceleration: " + Math.round(a_ms * 1000.0)/1000.0 
					+ " m/s2.");
			
	        // Note: 1 m/s = 3.6 km/hr (or kph)
	        double iPower = fAccel * v_ms; // [in W]
	        
	        // Convert the potential energy from J to Wh
	        double potentialEnergy = - iPower * secs / JOULES_PER_WH ; // [in Wh]
	        
	        double energyforCharging = battery.provideEnergy(potentialEnergy / 1000, hrsTime) * 1000;
	      
	        logger.log(vehicle, Level.INFO, 10_000, 
        			"type: " 				+ vehicle.getVehicleTypeString() + "   "
        		 	+ "mass: " 				+ Math.round(mass * 100.0)/100.0 + KG
        		 	+ "odometer: " 			+ Math.round(vehicle.getOdometerMileage()* 1_000.0)/1_000.0 + KM
        		 	+ "d_km: " 				+ Math.round(d_km * 1_000.0)/1_000.0 + KM
        	        + "hr: "				+ Math.round(hr * 10_000.0)/10_000.0 + " hrs   "
        	        + "u_kph: "				+ Math.round(u_kph * 10_000.0)/10_000.0 + KPH
                	+ "v_kph: " 			+ Math.round(v_kph * 10_000.0)/10_000.0 + KPH
        	        + "a_ms: "				+ Math.round(a_ms * 10_000.0)/10_000.0 + " m/s2   "
                	+ "fAccel: " 			+ Math.round(fAccel * 10_000.0)/10_000.0 + N       	        
            	    + "iPower: " 			+ Math.round(iPower * 1_000.0)/1_000.0 + KW
    				+ "potentialEnergy: " 	+ Math.round(potentialEnergy * 1_000.0)/1_000.0 + " Wh   "
    				+ "energyforCharging: " + Math.round(energyforCharging * 1_000.0)/1_000.0 + " Wh   "
    				+ "Battery: " 			+ Math.round(battery.getcurrentEnergy() * 1_000.0)/1_000.0 + " kWh"
	        );
	        
	        // Determine new position
	        vehicle.setCoordinates(vehicle.getCoordinates().getNewLocation(vehicle.getDirection(), d_km));
	        
			// Cache the new value of hr
			hrsTimeCache = hr;
			// Cache the new value of d_km
			distanceCache = d_km;
			
	        return 0;
		}
    	
		else {
	        // Account for the use of onboard battery to supply the energy
			
	        double fTot = fInitialFriction + fGravity + fAeroDrag + fRolling + fGradingResistance + fAccel;
	        // Note: 1 m/s = 3.6 km/hr (or kph)
	        double iPower = fTot * v_ms; // [in W]
	        
	        // Convert the energy usage from J to Wh
	        double energyNeeded = iPower * secs / JOULES_PER_WH ; // [in Wh]
	        
	        double energyByBattery = battery.requestEnergy(energyNeeded / 1000, hrsTime) * 1000;
	        
	        double energyByFuel = energyNeeded - energyByBattery;
	        
	        double fuelNeeded = 0;
	        
	        // Case 3 : fuel needed is less than available (just used up the last drop of fuel). Update fuelUsed.
	     	if (energyByFuel > 0) {
	        
		        // Derive the mass of fuel needed kg = Wh / Wh/kg
		        fuelNeeded = energyByFuel / vehicle.getFuelConv();
		
		        // Case 3 : fuel needed is less than available (just used up the last drop of fuel). Update fuelUsed.
				if (fuelNeeded > remainingFuel) {
					// Limit the fuel to be used
					fuelNeeded = remainingFuel;
					
					// Recompute the new distance it could travel
					d_km = vehicle.getConservativeFuelEconomy() * fuelNeeded;
					
					logger.log(vehicle, Level.WARNING,  20_000L, "fuelUsed: " +  Math.round(fuelNeeded * 1000.0)/1000.0  + " kg" 
							+ "new d_km: " +  Math.round(d_km * 1000.0)/1000.0  + " km.");
					// Find the new speed Slow down the vehicle            
					v_kph = d_km / hr; // [in kph]
					
					v_ms = v_kph / KPH_CONV; // [in m/s^2]
					
					hr = d_km / v_kph;
					
					iPower = fTot * v_ms; // [in W]
			        
					a_ms = (v_ms - u_ms) / secs; // [in m/s^2]
					
			        // Convert the energy usage from J to Wh
					energyByFuel = iPower * secs / JOULES_PER_WH ; // [in Wh]
	
		     	}
	     	}
	
		   	if (v_kph < 0 || v_ms < 0) {
	    		logger.log(vehicle, Level.INFO, 20_000, "Final speed was negative (" 
	    				+  Math.round(v_kph * 1000.0)/1000.0 + " kph). Reset back to zero.");
	    		v_kph = 0;
	    	}
		   	
		   	if (u_kph < 0 || u_ms < 0) {
	    		logger.log(vehicle, Level.INFO, 20_000, "Initial speed was negative (" 
	    				+  Math.round(u_kph * 1000.0)/1000.0 + " kph). Reset back to zero.");
	    		u_kph = 0;
	    	}
	
			// Adjust the speed
			vehicle.setSpeed(v_kph);
			
	        // Add distance traveled to vehicle's odometer.
	        vehicle.addOdometerMileage(d_km, fuelNeeded);
	        
	        // Track maintenance due to distance traveled.
	        vehicle.addDistanceLastMaintenance(d_km);
	        
	        // Derive the instantaneous fuel economy [in km/kg]
	        double iFE = d_km / fuelNeeded;
	        
	        // Set the instantaneous fuel economy [in km/kg]
	        vehicle.setIFuelEconomy(iFE);
	              
	        // Derive the instantaneous fuel consumption [Wh/km]
	        double iFC = energyNeeded / d_km;
	        
	        // Set the instantaneous fuel consumption [Wh/km]
	        vehicle.setIFuelConsumption(iFC);
	        
	        /*
			 * May comment off the block of codes below. 
			 * 
			 * NOTE: DO NOT delete any of them. Needed for testing.
			 */
	        
	        double bFC = vehicle.getBaseFuelConsumption();      
	        
	        // Fuel Economy 
	        double bFE = vehicle.getBaseFuelEconomy();   
	        double estFE = vehicle.getEstimatedFuelEconomy();
	        	
	        // Calculate the average power for this time period [in kW]
	        double aveP = energyNeeded / 1000.0 / hr;
	        
	        logger.log(vehicle, Level.INFO, 10_000, 
	        			"type: " 				+ vehicle.getVehicleTypeString() + "   "
	        		 	+ "mass: " 				+ Math.round(mass * 100.0)/100.0 + KG
	        		 	+ "odometer: " 			+ Math.round(vehicle.getOdometerMileage()* 1_000.0)/1_000.0 + KM
	        		 	+ "d_km: " 				+ Math.round(d_km * 1_000.0)/1_000.0 + KM
	        	        + "hr: "				+ Math.round(hr * 10_000.0)/10_000.0 + " hrs   "
	        	        + "u_kph: "				+ Math.round(u_kph * 10_000.0)/10_000.0 + KPH
	                	+ "v_kph: " 			+ Math.round(v_kph * 10_000.0)/10_000.0 + KPH
	        	        + "a_ms: "				+ Math.round(a_ms * 10_000.0)/10_000.0 + " m/s2   "
	            	    + "avePower: " 			+ Math.round(aveP * 1_000.0)/1_000.0 + KW
	    				+ "energyUsed: " 		+ Math.round(energyNeeded * 1_000.0)/1_000.0 + " Wh   "
	    				+ "energyByFuel: " 		+ Math.round(energyByFuel * 1_000.0)/1_000.0 + " Wh   "
	    				+ "energyByBattery: " 	+ Math.round(energyByBattery * 1_000.0)/1_000.0 + " Wh   "
	    				+ "Battery: " 			+ Math.round(battery.getcurrentEnergy() * 1_000.0)/1_000.0 + " kWh   "	    				
	            		+ "fuelUsed: " 			+ Math.round(fuelNeeded * 100_000.0)/100_000.0 + KG 
	        	        + "fAccel: " 			+ Math.round(fAccel * 10_000.0)/10_000.0 + N
	        	        + "angle: "				+ Math.round(angle / Math.PI * 180.0 * 10.0)/10.0 + " deg   "
	        			+ "fInitialF: " 		+ Math.round(fInitialFriction * 10_000.0)/10_000.0 + N
	        			+ "fGravity: " 			+ Math.round(fGravity * 10_000.0)/10_000.0 + N
	        			+ "fAeroDrag: " 		+ Math.round(fAeroDrag * 10_000.0)/10_000.0 + N
	    	    		+ "fRolling: " 			+ Math.round(fRolling * 10_000.0)/10_000.0 + N
	    	    		+ "fGradingRes: "		+ Math.round(fGradingResistance * 10_000.0)/10_000.0 + N
	    	    		+ "fTot: " 				+ Math.round(fTot * 10_000.0)/10_000.0 + N
	    	    		+ "baseFE: " 			+ Math.round(bFE * 1_000.0)/1_000.0 + KM_KG  
	                   	+ "estFE: " 			+ Math.round(estFE * 1_000.0)/1_000.0 + KM_KG
	                   	+ "initFE: " 			+ Math.round(initFE * 1_000.0)/1_000.0 + KM_KG
	    	    		+ "instantFE: " 		+ Math.round(iFE * 1_000.0)/1_000.0 + KM_KG  
	       	    		+ "cumFE: " 			+ Math.round(vehicle.getCumFuelEconomy() * 1_000.0)/1_000.0 + KM_KG  
	    	    		+ "baseFC: " 			+ Math.round(bFC * 1_000.0)/1_000.0 + WH_KM 
	    	    		+ "instantFC: " 		+ Math.round(iFC * 1_000.0)/1_000.0 + WH_KM    
	 	      	   		+ "cumFC: " 			+ Math.round(vehicle.getCumFuelConsumption() * 1_000.0)/1_000.0 + WH_KM  
	    	);
	        
			
			// Cache the new value of fuelUsed	
	        if (fuelNeeded <= 0) {
	        	// No fuel is expended. 
	        	// Usually indicative of vehicle deceleration.
	        	// FUTURE : may engage regenerative braking to recharge the battery
	        	fuelUsedCache = 0;
	        }
	        
	        else
	        	fuelUsedCache = fuelNeeded;
	        
	        // Determine new position
	        vehicle.setCoordinates(vehicle.getCoordinates().getNewLocation(vehicle.getDirection(), d_km));
	        
			// Cache the new value of hr
			hrsTimeCache = hr;
			// Cache the new value of d_km
			distanceCache = d_km;
			
	        return fuelNeeded;
		}
    }

    
	/**
	 * Gets the HrsTime cache in hr.
	 * 
	 * @return
	 */
	public double getHrsTimeCache() {
		return hrsTimeCache;
	}
	
	/** 
	 * Gets the distance cache in km.
	 * 
	 * @return
	 */
	public double getDistanceCache() {
		return distanceCache;
	}
    
	/** 
	 * Gets the fuelUsed cache in kg.
	 * 
	 * @return
	 */
	public double getFuelUsedCache() {
		return fuelUsedCache;
	}
	
}
