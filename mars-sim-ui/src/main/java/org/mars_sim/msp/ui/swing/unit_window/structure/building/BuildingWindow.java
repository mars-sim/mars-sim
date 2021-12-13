/**
 * Mars Simulation Project
 * BuildingWindow.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import javax.swing.event.ChangeEvent;

import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.unit_window.InventoryTabPanel;
import org.mars_sim.msp.ui.swing.unit_window.LocationTabPanel;
import org.mars_sim.msp.ui.swing.unit_window.MaintenanceTabPanel;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;
import org.mars_sim.msp.ui.swing.unit_window.UnitWindow;
import org.mars_sim.msp.ui.swing.unit_window.person.TabPanelActivity;
import org.mars_sim.msp.ui.swing.unit_window.person.TabPanelAttribute;
import org.mars_sim.msp.ui.swing.unit_window.structure.building.food.BuildingPanelCooking;


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
        addTopPanel(new BuildingPanelGeneral(building, desktop));
        
        // Add tabs for each supported Function. A bit messy but it is type safe
        if (building.hasFunction(FunctionType.LIVING_ACCOMMODATIONS)) {
        	addTabPanel(new BuildingPanelLiving(building.getLivingAccommodations(), desktop));
        }
        if (building.hasFunction(FunctionType.COOKING)) {
        	addTabPanel(new BuildingPanelCooking(building.getCooking(), desktop));        	
        }
        if (building.hasFunction(FunctionType.FARMING)) {
        	addTabPanel(new BuildingPanelFarming(building.getFarming(), desktop));        	
        }
        
    	sortTabPanels();
    }

    /**
     * Updates this window.
     */
    public void update() {
        super.update();
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
