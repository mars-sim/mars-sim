/**
 * Mars Simulation Project
 * SettlementWindow.java
 * @version 2.75 2003-05-08
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.unit_window.structure;

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
     * @param proxy the unit UI proxy for this window.
     */
    public SettlementWindow(MainDesktopPane desktop, UnitUIProxy proxy) {
        // Use UnitWindow constructor
        super(desktop, proxy);
        
        // Add tab panels
        SettlementUIProxy settlementProxy = (SettlementUIProxy) proxy;
        addTabPanel(new LocationTabPanel(proxy, desktop));
        addTabPanel(new PopulationTabPanel(proxy, desktop));
        addTabPanel(new VehicleTabPanel(proxy, desktop));
        addTabPanel(new InventoryTabPanel(proxy, desktop));
    }
}
