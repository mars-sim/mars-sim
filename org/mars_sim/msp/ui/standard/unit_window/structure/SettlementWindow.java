/**
 * Mars Simulation Project
 * SettlementWindow.java
 * @version 2.83 2008-02-10
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.unit_window.structure;

import org.mars_sim.msp.simulation.Unit;
import org.mars_sim.msp.ui.standard.MainDesktopPane;
import org.mars_sim.msp.ui.standard.unit_window.InventoryTabPanel;
import org.mars_sim.msp.ui.standard.unit_window.LocationTabPanel;
import org.mars_sim.msp.ui.standard.unit_window.UnitWindow;

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
        addTabPanel(new AssociatedPeopleTabPanel(unit, desktop));
        addTabPanel(new VehicleTabPanel(unit, desktop));
        addTabPanel(new InventoryTabPanel(unit, desktop));
        addTabPanel(new PowerGridTabPanel(unit, desktop));
        addTabPanel(new BuildingsTabPanel(unit, desktop));
        addTabPanel(new GoodsTabPanel(unit, desktop));
        addTabPanel(new CreditTabPanel(unit, desktop));
        addTabPanel(new ResourceProcessesTabPanel(unit, desktop));
        addTabPanel(new MaintenanceTabPanel(unit, desktop));
        addTabPanel(new ManufactureTabPanel(unit, desktop));
    }
}
