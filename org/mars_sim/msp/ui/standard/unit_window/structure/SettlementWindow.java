/**
 * Mars Simulation Project
 * SettlementWindow.java
 * @version 2.75 2003-07-16
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.unit_window.structure;

import org.mars_sim.msp.simulation.Unit;
import org.mars_sim.msp.ui.standard.*;
import org.mars_sim.msp.ui.standard.unit_window.*;

/**
 * The SettlementWindow is the window for displaying a settlement.
 */
public class SettlementWindow extends UnitWindow {
    
    /**
     * Constructor
     *
     * @param desktop the main desktop panel.
     * @param unit the unit to display.
     */
    public SettlementWindow(MainDesktopPane desktop, Unit unit) {
        // Use UnitWindow constructor
        super(desktop, unit, false);
        
        // Add tab panels
        addTabPanel(new LocationTabPanel(unit, desktop));
        addTabPanel(new PopulationTabPanel(unit, desktop));
        addTabPanel(new VehicleTabPanel(unit, desktop));
        addTabPanel(new InventoryTabPanel(unit, desktop));
        addTabPanel(new PowerGridTabPanel(unit, desktop));
        addTabPanel(new BuildingsTabPanel(unit, desktop));
    }
}
