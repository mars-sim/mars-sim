/**
 * Mars Simulation Project
 * VehicleWindow.java
 * @version 2.75 2003-07-22
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
     * @param vehicle the vehicle for this window.
     */
    public VehicleWindow(MainDesktopPane desktop, Vehicle vehicle) {
        // Use UnitWindow constructor
        super(desktop, vehicle, true);
        
        // Add tab panels
        addTabPanel(new NavigationTabPanel(vehicle, desktop));
        if (vehicle instanceof Crewable) addTabPanel(new CrewTabPanel(vehicle, desktop));
        addTabPanel(new LocationTabPanel(vehicle, desktop));
        addTabPanel(new InventoryTabPanel(vehicle, desktop));
        addTabPanel(new MaintenanceTabPanel(vehicle, desktop));
        if (vehicle instanceof ExplorerRover) addTabPanel(new LaboratoryTabPanel(vehicle, desktop));
    }
}
