/**
 * Mars Simulation Project
 * EquipmentWindow.java
 * @version 2.75 2003-07-22
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.unit_window.equipment;

import org.mars_sim.msp.simulation.equipment.Equipment;
import org.mars_sim.msp.simulation.malfunction.Malfunctionable;
import org.mars_sim.msp.ui.standard.MainDesktopPane;
import org.mars_sim.msp.ui.standard.unit_window.InventoryTabPanel;
import org.mars_sim.msp.ui.standard.unit_window.LocationTabPanel;
import org.mars_sim.msp.ui.standard.unit_window.MaintenanceTabPanel;
import org.mars_sim.msp.ui.standard.unit_window.UnitWindow;


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
        if (equipment instanceof Malfunctionable) 
        	addTabPanel(new MaintenanceTabPanel(equipment, desktop));
    }
}
