/**
 * Mars Simulation Project
 * VehicleWindow.java
 * @version 2.75 2003-06-24
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.unit_window.vehicle;

import org.mars_sim.msp.simulation.vehicle.*;
import org.mars_sim.msp.ui.standard.*;
import org.mars_sim.msp.ui.standard.unit_window.*;

/**
 * The VehicleWindow is the window for displaying a vehicle.
 */
public class VehicleWindow extends UnitWindow {
    
    /**
     * Constructor
     *
     * @param desktop the main desktop panel.
     * @param proxy the unit UI proxy for this window.
     */
    public VehicleWindow(MainDesktopPane desktop, UnitUIProxy proxy) {
        // Use UnitWindow constructor
        super(desktop, proxy);
        
        Vehicle vehicle = (Vehicle) proxy.getUnit();
        
        // Add tab panels
        addTabPanel(new LocationTabPanel(proxy, desktop));
        addTabPanel(new InventoryTabPanel(proxy, desktop));
    }
}
