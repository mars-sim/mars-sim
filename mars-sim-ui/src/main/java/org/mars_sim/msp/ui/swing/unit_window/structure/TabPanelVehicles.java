/*
 * Mars Simulation Project
 * TabPanelVehicles.java
 * @date 2022-07-09
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure;


import java.awt.Dimension;
import java.util.Collection;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;
import org.mars_sim.msp.ui.swing.unit_window.UnitListPanel;

/** 
 * The TabPanelVehicles is a tab panel for parked vehicles and vehicles on mission.
 */
@SuppressWarnings("serial")
public class TabPanelVehicles extends TabPanel {
	
	private static final String SUV_ICON = Msg.getString("icon.suv"); //$NON-NLS-1$
	
	/** The Settlement instance. */
	private Settlement settlement;
	
	private UnitListPanel<Vehicle> parkedVehicles;
	private UnitListPanel<Vehicle> missionVehicles;
	
	/**
	 * Constructor.
	 * @param unit the unit to display
	 * @param desktop the main desktop.
	 */
	public TabPanelVehicles(Unit unit, MainDesktopPane desktop) { 
		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelVehicles.title"), //$NON-NLS-1$
			ImageLoader.getNewIcon(SUV_ICON),
			Msg.getString("TabPanelVehicles.title"), //$NON-NLS-1$
			unit, desktop
		);

		settlement = (Settlement) unit;
	}
	
	@Override
	protected void buildUI(JPanel content) {
		JPanel vehiclePanel = new JPanel();
		vehiclePanel.setLayout(new BoxLayout(vehiclePanel, BoxLayout.Y_AXIS));
		content.add(vehiclePanel);

		// Parked Vehicles
		MainDesktopPane desktop = getDesktop();
		parkedVehicles = new UnitListPanel<>(desktop, new Dimension(175, 200)) {
			@Override
			protected Collection<Vehicle> getData() {
				return settlement.getParkedVehicles();
			}
		};
		addBorder(parkedVehicles, Msg.getString("TabPanelVehicles.parked.vehicles"));
		vehiclePanel.add(parkedVehicles);

		// Mission vehicles
		missionVehicles = new UnitListPanel<>(desktop, new Dimension(175, 200)) {
			@Override
			protected Collection<Vehicle> getData() {
				return settlement.getMissionVehicles();
			}
		};
		addBorder(missionVehicles, Msg.getString("TabPanelVehicles.mission.vehicles"));
		vehiclePanel.add(missionVehicles);
	}

	/**
	 * Updates the info on this panel.
	 */
	@Override
	public void update() {
		// Update vehicle list
		parkedVehicles.update();
		missionVehicles.update();
	}
	
	/**
     * Prepare object for garbage collection.
     */
	@Override
    public void destroy() {
		super.destroy();
    	parkedVehicles = null;
    	missionVehicles = null;
    }
}