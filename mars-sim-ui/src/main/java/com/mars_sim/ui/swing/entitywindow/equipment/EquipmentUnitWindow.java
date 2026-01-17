/**
 * Mars Simulation Project
 * EquipmentWindow.java
 * @date 2023-06-07
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.entitywindow.equipment;

import java.util.Properties;

import com.mars_sim.core.equipment.Equipment;
import com.mars_sim.core.malfunction.Malfunctionable;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.entitywindow.EntityContentPanel;
import com.mars_sim.ui.swing.unit_window.InventoryTabPanel;
import com.mars_sim.ui.swing.unit_window.LocationTabPanel;
import com.mars_sim.ui.swing.unit_window.MaintenanceTabPanel;
import com.mars_sim.ui.swing.unit_window.MalfunctionTabPanel;
import com.mars_sim.ui.swing.unit_window.NotesTabPanel;
import com.mars_sim.ui.swing.unit_window.SalvageTabPanel;


/**
 * The EquipmentWindow is the window for displaying a piece of equipment.
 */
public class EquipmentUnitWindow extends EntityContentPanel<Equipment> {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members
	private boolean salvaged;
	
    /**
     * Constructor.
     *
     * @param context the UI context.
     * @param equipment the equipment this window is for.
     * @param props Any initial properties for the window.
     */
    public EquipmentUnitWindow(Equipment equipment, UIContext context, Properties props) {
        super(equipment, context);

        addDefaultTabPanel(new TabPanelGeneralEquipment(equipment, context));
        addTabPanel(new InventoryTabPanel(equipment, context));
        addTabPanel(new LocationTabPanel(equipment, context));

        if (equipment instanceof Malfunctionable m) {
        	addTabPanel(new MaintenanceTabPanel(m, context));
            addTabPanel(new MalfunctionTabPanel(m, context));
        }
        
		addTabPanel(new NotesTabPanel(equipment, context));

        salvaged = equipment.isSalvaged();
        if (salvaged)
        	addTabPanel(new SalvageTabPanel(equipment, context));

        applyProps(props);
    }

    /** 
     * Updates this window.
     */
    @Override
    public void clockUpdate(ClockPulse pulse) {
        super.clockUpdate(pulse);

        // Check if equipment has been salvaged.
        if (!salvaged && getEntity().isSalvaged()) {
            addTabPanel(new SalvageTabPanel(getEntity(), getContext()));
            salvaged = true;
        }
    }
}
