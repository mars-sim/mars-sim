/**
 * Mars Simulation Project
 * SettlementWindow.java
 * @version 2.84 2008-05-29
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.unit_window.structure;

import org.mars_sim.msp.simulation.Unit;
import org.mars_sim.msp.simulation.structure.Settlement;
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
        
        Settlement settlement = (Settlement) unit;
        
        // Add tab panels
        addTabPanel(new LocationTabPanel(settlement, desktop));
        addTabPanel(new PopulationTabPanel(settlement, desktop));
        addTabPanel(new AssociatedPeopleTabPanel(settlement, desktop));
        addTabPanel(new VehicleTabPanel(settlement, desktop));
        addTabPanel(new InventoryTabPanel(settlement, desktop));
        addTabPanel(new PowerGridTabPanel(settlement, desktop));
        addTabPanel(new BuildingsTabPanel(settlement, desktop));
        addTabPanel(new GoodsTabPanel(settlement, desktop));
        addTabPanel(new CreditTabPanel(settlement, desktop));
        addTabPanel(new ResourceProcessesTabPanel(settlement, desktop));
        addTabPanel(new MaintenanceTabPanel(settlement, desktop));
        addTabPanel(new ManufactureTabPanel(settlement, desktop));
        addTabPanel(new MissionTabPanel(settlement, desktop));
    }
}
