/**
 * Mars Simulation Project
 * BuildingWindow.java
  * @version 3.1.0 2017-09-15
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.unit_window.InventoryTabPanel;
import org.mars_sim.msp.ui.swing.unit_window.LocationTabPanel;
import org.mars_sim.msp.ui.swing.unit_window.MaintenanceTabPanel;
import org.mars_sim.msp.ui.swing.unit_window.UnitWindow;


/**
 * The BuildingWindow is the window for displaying a piece of building.
 */
public class BuildingWindow extends UnitWindow {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members
	//private boolean salvaged;

    /**
     * Constructor
     *
     * @param desktop the main desktop panel.
     * @param building the building this window is for.
     */
    public BuildingWindow(MainDesktopPane desktop, Building building) {
        // Use UnitWindow constructor
        super(desktop, building, false);

        // Add tab panels
        addTopPanel(new LocationTabPanel(building, desktop));
        addTabPanel(new InventoryTabPanel(building, desktop));
        if (building instanceof Malfunctionable)
        	addTabPanel(new MaintenanceTabPanel(building, desktop));

        //salvaged = building.isSalvaged();
        //if (salvaged) addTabPanel(new SalvageTabPanel(building, desktop));
    }

    /**
     * Updates this window.
     */
    public void update() {
        super.update();

        // Check if building has been salvaged.
        //Building building = (Building) getUnit();
        //if (!salvaged && building.isSalvaged()) {
        //    addTabPanel(new SalvageTabPanel(building, desktop));
        //    salvaged = true;
        //}
    }
}