/**
 * Mars Simulation Project
 * VehicleUIProxy.java
 * @version 2.75 2002-06-08
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard; 

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.vehicle.*;
import java.util.*;
 
/**
 * Abstract user interface proxy for a vehicle. 
 */
public abstract class VehicleUIProxy extends UnitUIProxy {

    // Data members
    private Vehicle vehicle;
    private ArrayList trail;
	
    /** Constructs a VehicleUIProxy object 
     *  @param vehicle the vehicle
     *  @param proxyManager the vehicle's proxy manager
     */
    public VehicleUIProxy(Vehicle vehicle, UIProxyManager proxyManager) {
        super(vehicle, proxyManager);

	// Initialize data members
	this.vehicle = vehicle;
	trail = new ArrayList();
    }

    /**
     * Gets a collection of locations in the vehicle's trail.
     * @return collection
     */
    public Collection getTrail() {
        return trail;
    }

    /**
     * Adds a location to the vehicle's trail if appropriate.
     * @param location location to be added.
     */
    public void addLocationToTrail(Coordinates location) {
        if (vehicle.getSettlement() != null) {
            if (trail.size() > 0) trail.clear();
	}
        else if (trail.size() > 0) {
            Coordinates lastLocation = (Coordinates) trail.get(trail.size() - 1);
	    if (!lastLocation.equals(location)) trail.add(new Coordinates(location));
	}
	else {
            trail.add(new Coordinates(location));
	}
    }
}
