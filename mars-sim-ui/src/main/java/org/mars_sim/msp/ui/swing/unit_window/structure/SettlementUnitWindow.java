/**
 * Mars Simulation Project
 * SettlementUnitWindow.java
 * @version 3.08 2015-04-28
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.unit_window.structure;

import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.unit_window.InventoryTabPanel;
import org.mars_sim.msp.ui.swing.unit_window.LocationTabPanel;
import org.mars_sim.msp.ui.swing.unit_window.UnitWindow;
import org.mars_sim.msp.ui.swing.unit_window.structure.building.food.TabPanelCooking;
import org.mars_sim.msp.ui.swing.unit_window.structure.building.food.TabPanelFoodProduction;

/**
 * The SettlementUnitWindow is the window for displaying a settlement.
 */
public class SettlementUnitWindow extends UnitWindow {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/**
     * Constructor
     *
     * @param desktop the main desktop panel.
     * @param unit the unit to display.
     */
    public SettlementUnitWindow(MainDesktopPane desktop, Unit unit) {
        // Use UnitWindow constructor
        super(desktop, unit, false);

        Settlement settlement = (Settlement) unit;

        //setSize(600,600);

        // Add tab panels
        // 2015-12-29 Added TabPanelAirComposition
        addTabPanel(new TabPanelAirComposition(settlement, desktop));

        addTabPanel(new TabPanelAssociatedPeople(settlement, desktop));
        //2015-01-21 Added TabPanelBot
        addTabPanel(new TabPanelBots(settlement, desktop));

        addTabPanel(new TabPanelBuildings(settlement, desktop));
        //2014-12-02 Added TabPanelCooking
        addTabPanel(new TabPanelCooking(settlement, desktop));

        addTabPanel(new TabPanelConstruction(settlement, desktop));

        addTabPanel(new TabPanelCredit(settlement, desktop));
        //2014-11-23 Added TabPanelFoodProduction
        addTabPanel(new TabPanelFoodProduction(settlement, desktop));

        addTabPanel(new TabPanelGoods(settlement, desktop));

        addTabPanel(new InventoryTabPanel(settlement, desktop));

        addTopPanel(new LocationTabPanel(settlement, desktop));

        addTabPanel(new TabPanelMaintenance(settlement, desktop));

        addTabPanel(new TabPanelManufacture(settlement, desktop));

        addTabPanel(new TabPanelMissions(settlement, desktop));
        //2015-04-28 Added TabPanelOrganization
        addTabPanel(new TabPanelOrganization(settlement, desktop));

        addTabPanel(new TabPanelPopulation(settlement, desktop));

        addTabPanel(new TabPanelPowerGrid(settlement, desktop));

        addTabPanel(new TabPanelResourceProcesses(settlement, desktop));

        addTabPanel(new TabPanelScience(settlement, desktop));
    	//2014-10-17 Added TabPanelHeatingSystem
        addTabPanel(new TabPanelThermalSystem(settlement, desktop));

        addTabPanel(new TabPanelVehicles(settlement, desktop));
        //2015-05-01 Added TabPanelWeather
        addTabPanel(new TabPanelWeather(settlement, desktop));

        // 2015-06-20 Added tab sorting
		sortTabPanels();
    }
}
