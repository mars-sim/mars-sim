/**
 * Mars Simulation Project
 * UnitWindowFactory.java
 * @version 2.75 2003-07-22
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.unit_window;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.equipment.Equipment;
import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.vehicle.Vehicle;
import org.mars_sim.msp.ui.standard.MainDesktopPane;
import org.mars_sim.msp.ui.standard.unit_window.equipment.EquipmentWindow;
import org.mars_sim.msp.ui.standard.unit_window.person.PersonWindow;
import org.mars_sim.msp.ui.standard.unit_window.structure.SettlementWindow;
import org.mars_sim.msp.ui.standard.unit_window.vehicle.VehicleWindow;

/**
 * The UnitWindowFactory is a factory for creating unit windows for units.
 */
public abstract class UnitWindowFactory {
    
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
        else if (unit instanceof Settlement) return new SettlementWindow(desktop, (Settlement) unit);
        else if (unit instanceof Equipment) return new EquipmentWindow(desktop, (Equipment) unit);
        else return null;
    }
}
