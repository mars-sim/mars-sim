/**
 * Mars Simulation Project
 * VehicleUIProxy.java
 * @version 2.75 2003-07-10
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard; 

import org.mars_sim.msp.simulation.vehicle.*;
import org.mars_sim.msp.ui.standard.unit_window.UnitWindow;
import org.mars_sim.msp.ui.standard.unit_window.vehicle.VehicleWindow; 
 
/**
 * Abstract user interface proxy for a vehicle. 
 */
public abstract class VehicleUIProxy extends UnitUIProxy {

    private UnitWindow unitWindow;
    
    /** Constructs a VehicleUIProxy object 
     *  @param vehicle the vehicle
     *  @param proxyManager the vehicle's proxy manager
     */
    public VehicleUIProxy(Vehicle vehicle, UIProxyManager proxyManager) {
        super(vehicle, proxyManager);
    }
    
    /** 
     * Gets a window for unit. 
     * 
     * @param desktop the desktop pane
     * @return unit window
     */
    public UnitWindow getUnitWindow(MainDesktopPane desktop) {
        if (unitWindow == null) unitWindow = new VehicleWindow(desktop, this);
        return unitWindow;
    }
}
