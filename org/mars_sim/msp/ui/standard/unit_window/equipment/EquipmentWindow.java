/**
 * Mars Simulation Project
 * EquipmentWindow.java
 * @version 2.75 2003-07-22
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
     * @param equipment the equipment this window is for.
     */
    public EquipmentWindow(MainDesktopPane desktop, Equipment equipment) {
        // Use UnitWindow constructor
        super(desktop, equipment, false);
        
        // Add tab panels
        addTabPanel(new LocationTabPanel(equipment, desktop));
        addTabPanel(new InventoryTabPanel(equipment, desktop));
        addTabPanel(new MaintenanceTabPanel(equipment, desktop));
    }
}
