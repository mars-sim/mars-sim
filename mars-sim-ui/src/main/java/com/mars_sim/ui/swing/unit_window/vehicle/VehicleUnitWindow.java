/*
 * Mars Simulation Project
 * VehicleWindow.java
 * @date 2023-01-09
 * @author Scott Davis
 */

package com.mars_sim.ui.swing.unit_window.vehicle;

import java.util.Properties;

import com.mars_sim.core.EntityEvent;
import com.mars_sim.core.vehicle.Crewable;
import com.mars_sim.core.vehicle.Rover;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.entitywindow.EntityContentPanel;
import com.mars_sim.ui.swing.unit_window.InventoryTabPanel;
import com.mars_sim.ui.swing.unit_window.LocationTabPanel;
import com.mars_sim.ui.swing.unit_window.MaintenanceTabPanel;
import com.mars_sim.ui.swing.unit_window.MalfunctionTabPanel;
import com.mars_sim.ui.swing.unit_window.NotesTabPanel;
import com.mars_sim.ui.swing.unit_window.SalvageTabPanel;

/**
 * The VehicleWindow is the window for displaying a vehicle.
 */
@SuppressWarnings("serial")
public class VehicleUnitWindow extends EntityContentPanel<Vehicle> {

	// Data members
	private boolean salvaged;

	/**
	 * Constructor.
	 *
	 * @param desktop the main desktop panel.
	 * @param vehicle the vehicle for this window.
	 */
	public VehicleUnitWindow(Vehicle vehicle, UIContext context, Properties props) {
		super(vehicle, context);

		// Add as the first panel
		addTabPanel(new TabPanelGeneralVehicle(vehicle, context));

		if (vehicle instanceof Crewable crewableVehicle) {
			if (crewableVehicle.getCrewCapacity() > 0)
				addTabPanel(new TabPanelCrew(vehicle, context));
			if (crewableVehicle.getRobotCrewCapacity() > 0)
				addTabPanel(new TabPanelBots(vehicle, context));
		}

		addTabPanel(new InventoryTabPanel(vehicle, context));

		addTabPanel(new LocationTabPanel(vehicle, context));

		if (vehicle instanceof Rover rover) {
			addTabPanel(new TabPanelEVA(rover, context));		
			if (rover.hasLab())
				addTabPanel(new LaboratoryTabPanel(rover, context));		
			// Future: Add sickbay tab panel.
		}

		addTabPanel(new TabPanelLog(vehicle, context));
		addTabPanel(new MaintenanceTabPanel(vehicle, context));
		addTabPanel(new MalfunctionTabPanel(vehicle, context));
		addTabPanel(new NavigationTabPanel(vehicle, context));
		addTabPanel(new TabPanelMission(vehicle, context));	
		addTabPanel(new NotesTabPanel(vehicle, context));
		addTabPanel(new TabPanelSpecs(vehicle, context));
				
		salvaged = vehicle.isSalvaged();
		if (salvaged)
			addTabPanel(new SalvageTabPanel(vehicle, context));
		addTabPanel(new TabPanelTow(vehicle, context));

		applyProps(props);
	}

	/**
     * Listens for the vehicle being salvaged.
     * @param event Incoming entity event.
     */
    @Override
    public void entityUpdate(EntityEvent event) {
		super.entityUpdate(event);

		// Check for salvage event
		if (event.getType().equals(Vehicle.SALVAGE_EVENT) && !salvaged) {
			addTabPanel(new SalvageTabPanel(getEntity(), getContext()));
			salvaged = true;
		}
	}
}
