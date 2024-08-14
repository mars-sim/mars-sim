/**
 * Mars Simulation Project
 * EquipmentWindow.java
 * @date 2023-06-07
 * @author Scott Davis
 */

package com.mars_sim.ui.swing.unit_window.equipment;

import com.mars_sim.core.equipment.EVASuit;
import com.mars_sim.core.equipment.Equipment;
import com.mars_sim.core.malfunction.Malfunctionable;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.unit_window.InventoryTabPanel;
import com.mars_sim.ui.swing.unit_window.LocationTabPanel;
import com.mars_sim.ui.swing.unit_window.MaintenanceTabPanel;
import com.mars_sim.ui.swing.unit_window.MalfunctionTabPanel;
import com.mars_sim.ui.swing.unit_window.NotesTabPanel;
import com.mars_sim.ui.swing.unit_window.SalvageTabPanel;
import com.mars_sim.ui.swing.unit_window.UnitWindow;


/**
 * The EquipmentWindow is the window for displaying a piece of equipment.
 */
public class EquipmentUnitWindow extends UnitWindow {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members
	private boolean salvaged;

	private Equipment equipment;
	
    /**
     * Constructor.
     *
     * @param desktop the main desktop panel.
     * @param equipment the equipment this window is for.
     */
    public EquipmentUnitWindow(MainDesktopPane desktop, Equipment equipment) {
        // Use UnitWindow constructor
        super(desktop, equipment, equipment.getName()
				+ " of " + ((equipment.getAssociatedSettlement() != null) ? 
						equipment.getAssociatedSettlement() : "")
				+ ((equipment.getContainerUnit() != null) ? (" in " + equipment.getContainerUnit()) : ""),
				true);
        this.equipment = equipment;

        if (equipment instanceof EVASuit) 
        	addTabPanel(new TabPanelGeneralEquipment(equipment, desktop));
        
        addTabPanel(new InventoryTabPanel(equipment, desktop));

        addTabPanel(new LocationTabPanel(equipment, desktop));

        if (equipment instanceof Malfunctionable) {
            Malfunctionable m = (Malfunctionable) equipment;

        	addTabPanel(new MaintenanceTabPanel(m, desktop));
            addTabPanel(new MalfunctionTabPanel(m, desktop));
        }
        
		addTabPanel(new NotesTabPanel(equipment, desktop));

        salvaged = equipment.isSalvaged();
        if (salvaged)
        	addTabPanel(new SalvageTabPanel(equipment, desktop));

    	sortTabPanels();

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
        if (!salvaged && equipment.isSalvaged()) {
            addTabPanel(new SalvageTabPanel(equipment, desktop));
            salvaged = true;
        }
    }
}
