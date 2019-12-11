/**
 * Mars Simulation Project
 * BuildingWindow.java
  * @version 3.1.0 2017-09-15
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import javax.swing.event.ChangeEvent;

import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.unit_window.InventoryTabPanel;
import org.mars_sim.msp.ui.swing.unit_window.LocationTabPanel;
import org.mars_sim.msp.ui.swing.unit_window.MaintenanceTabPanel;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;
import org.mars_sim.msp.ui.swing.unit_window.UnitWindow;
import org.mars_sim.msp.ui.swing.unit_window.person.TabPanelActivity;
import org.mars_sim.msp.ui.swing.unit_window.person.TabPanelAttribute;


/**
 * The BuildingWindow is the window for displaying a piece of building.
 */
@SuppressWarnings("serial")
public class BuildingWindow extends UnitWindow {

	// Data members
	/** The cache for the currently selected TabPanel. */
	private TabPanel oldTab;

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

    @Override
	public void stateChanged(ChangeEvent e) {
		// SwingUtilities.updateComponentTreeUI(this);
		TabPanel newTab = getSelected();

		if (newTab != oldTab) {

			if (newTab instanceof TabPanelActivity) {
//				if (tabPanelActivity.isUIDone());
//				 	tabPanelActivity.initializeUI();
			} else if (newTab instanceof TabPanelAttribute) {
				
			}
		}
	}
}