/**
 * Mars Simulation Project
 * VehicleUIProxy.java
 * @version 2.71 2000-09-17
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard; 

import org.mars_sim.msp.simulation.*; 
 
/**
 * Abstract user interface proxy for a vehicle. 
 */
 
public abstract class VehicleUIProxy extends UnitUIProxy {

    public VehicleUIProxy(Vehicle vehicle, UIProxyManager proxyManager) {
        super(vehicle, proxyManager);
    }
}
