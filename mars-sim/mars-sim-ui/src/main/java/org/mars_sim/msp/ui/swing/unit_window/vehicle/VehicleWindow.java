/**
 * Mars Simulation Project
 * VehicleWindow.java
 * @version 3.08 2015-03-24
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.unit_window.vehicle;

import org.mars_sim.msp.core.vehicle.Crewable;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.unit_window.InventoryTabPanel;
import org.mars_sim.msp.ui.swing.unit_window.LocationTabPanel;
import org.mars_sim.msp.ui.swing.unit_window.MaintenanceTabPanel;
import org.mars_sim.msp.ui.swing.unit_window.SalvageTabPanel;
import org.mars_sim.msp.ui.swing.unit_window.UnitWindow;

/**
 * The VehicleWindow is the window for displaying a vehicle.
 */
public class VehicleWindow extends UnitWindow {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members
    private boolean salvaged;
    
    /**
     * Constructor
     *
     * @param desktop the main desktop panel.
     * @param vehicle the vehicle for this window.
     */
    public VehicleWindow(MainDesktopPane desktop, Vehicle vehicle) {
        // Use UnitWindow constructor
        super(desktop, vehicle, true);
        
        // Add tab panels
        addTabPanel(new NavigationTabPanel(vehicle, desktop));
        if (vehicle instanceof Crewable) {
        	Crewable crewableVehicle = (Crewable) vehicle;
            if (crewableVehicle.getCrewNum() > 0) 
            	addTabPanel(new TabPanelCrew(vehicle, desktop));
            else if (crewableVehicle.getRobotCrewNum() > 0)
            	addTabPanel(new TabPanelBots(vehicle, desktop));            	       
        }
        addTopPanel(new LocationTabPanel(vehicle, desktop));
        addTabPanel(new InventoryTabPanel(vehicle, desktop));
        addTabPanel(new MaintenanceTabPanel(vehicle, desktop));
        if (vehicle instanceof Rover) {
        	Rover rover = (Rover) vehicle;
        	if (rover.hasLab()) addTabPanel(new LaboratoryTabPanel(rover, desktop));
        	// TODO: Add sickbay tab panel.
        }
        addTabPanel(new TabPanelMission(vehicle, desktop));
        addTabPanel(new TabPanelTow(vehicle, desktop));
        
        salvaged = vehicle.isSalvaged();
        if (salvaged) addTabPanel(new SalvageTabPanel(vehicle, desktop));
    }
    
    /**
     * Updates this window.
     */
    public void update() {
        super.update();
        
        // Check if equipment has been salvaged.
        Vehicle vehicle = (Vehicle) getUnit();
        if (!salvaged && vehicle.isSalvaged()) {
            addTabPanel(new SalvageTabPanel(vehicle, desktop));
            salvaged = true;
        }
    }
}