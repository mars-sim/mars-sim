/*
 * Mars Simulation Project
 * VehicleWindow.java
 * @date 2023-01-09
 * @author Scott Davis
 */

package com.mars_sim.ui.swing.unit_window.vehicle;

import com.mars_sim.core.vehicle.Crewable;
import com.mars_sim.core.vehicle.Rover;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.unit_window.InventoryTabPanel;
import com.mars_sim.ui.swing.unit_window.LocationTabPanel;
import com.mars_sim.ui.swing.unit_window.MaintenanceTabPanel;
import com.mars_sim.ui.swing.unit_window.MalfunctionTabPanel;
import com.mars_sim.ui.swing.unit_window.NotesTabPanel;
import com.mars_sim.ui.swing.unit_window.SalvageTabPanel;
import com.mars_sim.ui.swing.unit_window.UnitWindow;

/**
 * The VehicleWindow is the window for displaying a vehicle.
 */
@SuppressWarnings("serial")
public class VehicleUnitWindow extends UnitWindow {

	// Data members
	private boolean salvaged;

	private Vehicle vehicle;

	/**
	 * Constructor.
	 *
	 * @param desktop the main desktop panel.
	 * @param vehicle the vehicle for this window.
	 */
	public VehicleUnitWindow(MainDesktopPane desktop, Vehicle vehicle) {
		// Use UnitWindow constructor
		super(desktop, vehicle,  vehicle.getName() 
				+ " (" + vehicle.getVehicleType().getName() + ")"
				+ " of " + vehicle.getAssociatedSettlement()
				+ ((vehicle.getContainerUnit() != null) ? (" in " + vehicle.getContainerUnit()) :
					" in " + vehicle.getLocationTag().findSettlementVicinity() + " Vicinity"),
				true);
		this.vehicle = vehicle;
		
		if (vehicle instanceof Crewable crewableVehicle) {
			if (crewableVehicle.getCrewCapacity() > 0)
				addTabPanel(new TabPanelCrew(vehicle, desktop));
			else if (crewableVehicle.getRobotCrewCapacity() > 0)
				addTabPanel(new TabPanelBots(vehicle, desktop));
		}

		addTabPanel(new InventoryTabPanel(vehicle, desktop));

		addTabPanel(new LocationTabPanel(vehicle, desktop));

		if (vehicle instanceof Rover rover) {
			addTabPanel(new TabPanelEVA(rover, desktop));		
			if (rover.hasLab())
				addTabPanel(new LaboratoryTabPanel(rover, desktop));		
			// Future: Add sickbay tab panel.
		}

		addTabPanel(new TabPanelLog(vehicle, desktop));
		addTabPanel(new MaintenanceTabPanel(vehicle, desktop));
		addTabPanel(new MalfunctionTabPanel(vehicle, desktop));
		addTabPanel(new NavigationTabPanel(vehicle, desktop));
		addTabPanel(new TabPanelMission(vehicle, desktop));	
		addTabPanel(new NotesTabPanel(vehicle, desktop));
		addTabPanel(new TabPanelSpecs(vehicle, desktop));
				
		salvaged = vehicle.isSalvaged();
		if (salvaged)
			addTabPanel(new SalvageTabPanel(vehicle, desktop));

		addTabPanel(new TabPanelTow(vehicle, desktop));
		
		sortTabPanels();
		
		// Add as the first panel
		addFirstPanel(new TabPanelGeneralVehicle(vehicle, desktop));
		
		// Add to tab panels. 
		addTabIconPanels();
	}

	/**
	 * Updates this window.
	 */
	@Override
	public void update() {
		super.update();
		// Check if equipment has been salvaged.
		if (!salvaged && vehicle.isSalvaged()) {
			addTabPanel(new SalvageTabPanel(vehicle, desktop));
			salvaged = true;
		}
	}

	@Override
	public void destroy() {
		super.destroy();
		vehicle = null;
	}
}
