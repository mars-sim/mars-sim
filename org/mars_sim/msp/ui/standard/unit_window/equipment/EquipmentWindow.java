/**
 * Mars Simulation Project
 * EquipmentWindow.java
 * @version 2.75 2003-07-08
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.unit_window.equipment;

import org.mars_sim.msp.simulation.equipment.*;
import org.mars_sim.msp.ui.standard.*;
import org.mars_sim.msp.ui.standard.unit_window.*;

/**
 * The EquipmentWindow is the window for displaying a piece of equipment.
 */
public class EquipmentWindow extends UnitWindow {
    
    /**
     * Constructor
     *
     * @param desktop the main desktop panel.
     * @param proxy the unit UI proxy for this window.
     */
    public EquipmentWindow(MainDesktopPane desktop, UnitUIProxy proxy) {
        // Use UnitWindow constructor
        super(desktop, proxy, true);
        
        Equipment equipment = (Equipment) proxy.getUnit();
        
        // Add tab panels
        addTabPanel(new LocationTabPanel(proxy, desktop));
        addTabPanel(new InventoryTabPanel(proxy, desktop));
        addTabPanel(new MaintenanceTabPanel(proxy, desktop));
    }
}
