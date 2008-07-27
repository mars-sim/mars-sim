/**
 * Mars Simulation Project
 * StandardPowerSource.java
 * @version 2.85 26.7.2008
 * @author Sebastien Venot
 */
package org.mars_sim.msp.simulation.structure.building.function;

import java.io.Serializable;

import org.mars_sim.msp.simulation.Inventory;
import org.mars_sim.msp.simulation.resource.AmountResource;
import org.mars_sim.msp.simulation.structure.building.Building;


public class FuelPowerSource extends PowerSource implements Serializable {

    private static final long serialVersionUID = 1L;
    private final static String TYPE = "Fuel Power Source";
    private boolean toggle = false;
    private double fuelCapacity; 
    
    //A fuelpower source works only with one kind of fuel
    //similar to cars
    private AmountResource resource;
    private double consumptionSpeed;
    private double currentFuelLevel;

    /**
     * @param type
     * @param maxPower
     */
    public FuelPowerSource(double _maxPower, double _capacity, 
	    AmountResource _resource, double _consumptionSpeed) {
	super(TYPE, _maxPower);
	fuelCapacity = _capacity;
	currentFuelLevel= _capacity;
	consumptionSpeed = _consumptionSpeed;
	resource = _resource;
    }

    /* 
     * 
     */
    @Override
    public double getCurrentPower(Building building) {
	if(isToggleON()) {
	    currentFuelLevel = currentFuelLevel- consumptionSpeed;
	    
	    if(currentFuelLevel > 0) {
		return getMaxPower();
	    } else {
		return 0;
	    }
	} else {
	    return 0;
	}
    }
    
    public void toggleON() {
	toggle = true;
    }
    
    public void toggleOFF() {
	toggle = false;
    }
    
    public boolean isToggleON() {
	return toggle;
    }
    
    public void addFuel(double amount) {
	double temp = currentFuelLevel + amount;
	
	if(temp > fuelCapacity) {
	    currentFuelLevel = fuelCapacity;
	} else {
	    currentFuelLevel = temp;
	}
    }
    
    public void consumeFuel(double time, Inventory inv) {
	
    }
}
