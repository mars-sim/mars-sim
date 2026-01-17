/*
 * Mars Simulation Project
 * SettlementUnitWindow.java
 * @date 2023-06-04
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.unit_window.structure;

import java.util.Properties;

import com.mars_sim.core.structure.Settlement;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.entitywindow.EntityContentPanel;
import com.mars_sim.ui.swing.unit_window.InventoryTabPanel;
import com.mars_sim.ui.swing.unit_window.LocationTabPanel;
import com.mars_sim.ui.swing.unit_window.MalfunctionTabPanel;
import com.mars_sim.ui.swing.unit_window.NotesTabPanel;

/**
 * The SettlementUnitWindow is the window for displaying a settlement.
 */
@SuppressWarnings("serial")
public class SettlementUnitWindow extends EntityContentPanel<Settlement> {
	
	/**
	 * Constructor
	 *
	 * @param settlement The settlement to display.
	 * @param context The UI context.
	 * @param props The properties to apply to the window.
	 */
	public SettlementUnitWindow(Settlement settlement, UIContext context, Properties props) {
		super(settlement, context);
		
		addDefaultTabPanel(new TabPanelGeneral(settlement, context));

		addTabPanel(new TabPanelWeather(settlement, context));
		addTabPanel(new TabPanelAirComposition(settlement, context));
		addTabPanel(new TabPanelBots(settlement, context));
		addTabPanel(new TabPanelCitizen(settlement, context));
		addTabPanel(new TabPanelComputing(settlement, context));
		addTabPanel(new TabPanelCooking(settlement, context));
		addTabPanel(new TabPanelConstruction(settlement, context));
		addTabPanel(new TabPanelCredit(settlement, context));
		addTabPanel(new TabPanelFoodProduction(settlement, context));
		addTabPanel(new TabPanelGroupActivity(settlement, context));
		addTabPanel(new TabPanelGoods(settlement, context));
		addTabPanel(new InventoryTabPanel(settlement, context));
		addTabPanel(new TabPanelScience(settlement, context));
		addTabPanel(new LocationTabPanel(settlement, context));
		addTabPanel(new TabPanelMaintenance(settlement, context));
		addTabPanel(new MalfunctionTabPanel(settlement, context));
		addTabPanel(new TabPanelManufacture(settlement, context));
		addTabPanel(new TabPanelMissions(settlement, context));
		addTabPanel(new NotesTabPanel(settlement, context));
		addTabPanel(new TabPanelPreferences(settlement, context));
		addTabPanel(new TabPanelOrganization(settlement, context));
		addTabPanel(new TabPanelPowerGrid(settlement, context));
		addTabPanel(new TabPanelProcessHistory(settlement, context));
		addTabPanel(new TabPanelResourceProcesses(settlement, context));
		addTabPanel(new TabPanelThermal(settlement, context));
		addTabPanel(new TabPanelVehicles(settlement, context));
		addTabPanel(new TabPanelWasteProcesses(settlement, context));

		applyProps(props);
	}
}
