/**
 * Mars Simulation Project
 * EquipmentWindow.java
 * @version 3.1.0 2017-03-19
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.unit_window.equipment;

import javax.swing.event.ChangeEvent;

import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.unit_window.InventoryTabPanel;
import org.mars_sim.msp.ui.swing.unit_window.LocationTabPanel;
import org.mars_sim.msp.ui.swing.unit_window.MaintenanceTabPanel;
import org.mars_sim.msp.ui.swing.unit_window.NotesTabPanel;
import org.mars_sim.msp.ui.swing.unit_window.SalvageTabPanel;
import org.mars_sim.msp.ui.swing.unit_window.UnitWindow;


/**
 * The EquipmentWindow is the window for displaying a piece of equipment.
 */
public class EquipmentWindow extends UnitWindow {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members
	private boolean salvaged;
	
	private Equipment equipment;
    /**
     * Constructor
     *
     * @param desktop the main desktop panel.
     * @param equipment the equipment this window is for.
     */
    public EquipmentWindow(MainDesktopPane desktop, Equipment equipment) {
        // Use UnitWindow constructor
        super(desktop, equipment, false);
        this.equipment = equipment;

        // Add tab panels
        addTabPanel(new LocationTabPanel(equipment, desktop));

        addTabPanel(new InventoryTabPanel(equipment, desktop));

        if (equipment instanceof Malfunctionable)
        	addTabPanel(new MaintenanceTabPanel(equipment, desktop));

		addTabPanel(new NotesTabPanel(equipment, desktop));

        salvaged = equipment.isSalvaged();
        if (salvaged)
        	addTabPanel(new SalvageTabPanel(equipment, desktop));

    	sortTabPanels();

    }

    /**
     * Updates this window.
     */
	@Override
    public void update() {
        super.update();
        // Check if equipment has been salvaged.
        if (!salvaged && equipment.isSalvaged()) {
            addTabPanel(new SalvageTabPanel(equipment, desktop));
            salvaged = true;
        }
    }

	@Override
	public void stateChanged(ChangeEvent e) {
//		TabPanel newTab = getSelected();
//
//		if (oldTab == null || newTab != oldTab) {
//			oldTab = newTab;
//			
//			if (!newTab.isUIDone());
//				newTab.initializeUI();
//		}
	}
}