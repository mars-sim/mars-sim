/**
 * Mars Simulation Project
 * UnitWindowFactory.java
 * @version 3.07 2014-12-06

 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.unit_window;

import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.unit_window.equipment.EquipmentWindow;
import org.mars_sim.msp.ui.swing.unit_window.person.PersonWindow;
import org.mars_sim.msp.ui.swing.unit_window.structure.TabPanelUnitWindow;
import org.mars_sim.msp.ui.swing.unit_window.vehicle.VehicleWindow;

/**
 * The UnitWindowFactory is a factory for creating unit windows for units.
 */
public class UnitWindowFactory {
    
    /**
     * Private constructor
     */
    private UnitWindowFactory() {}
    
    /**
     * Gets a new unit window for a given unit.
     *
     * @param unit the unit the window is for.
     * @param desktop the main desktop.
     * @return unit window
     */
    public static UnitWindow getUnitWindow(Unit unit, MainDesktopPane desktop) {
        
        if (unit instanceof Person) return new PersonWindow(desktop, (Person) unit);
        else if (unit instanceof Vehicle) return new VehicleWindow(desktop, (Vehicle) unit);
        else if (unit instanceof Settlement) return new TabPanelUnitWindow(desktop, unit);
        else if (unit instanceof Equipment) return new EquipmentWindow(desktop, (Equipment) unit);
        else return null;
    }
}
