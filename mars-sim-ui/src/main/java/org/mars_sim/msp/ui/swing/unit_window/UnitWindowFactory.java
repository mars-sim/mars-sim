/**
 * Mars Simulation Project
 * UnitWindowFactory.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.unit_window;

import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.unit_window.equipment.EquipmentWindow;
import org.mars_sim.msp.ui.swing.unit_window.person.PersonWindow;
import org.mars_sim.msp.ui.swing.unit_window.person.RobotWindow;
import org.mars_sim.msp.ui.swing.unit_window.structure.SettlementUnitWindow;
import org.mars_sim.msp.ui.swing.unit_window.structure.building.BuildingWindow;
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

        if (unit.getUnitType() == UnitType.PERSON) return new PersonWindow(desktop, (Person) unit);
        else if (unit.getUnitType() == UnitType.ROBOT) return new RobotWindow(desktop, (Robot) unit);
        else if (unit.getUnitType() == UnitType.VEHICLE) return new VehicleWindow(desktop, (Vehicle) unit);
        else if (unit.getUnitType() == UnitType.SETTLEMENT) return new SettlementUnitWindow(desktop, unit);
        else if (unit.getUnitType() == UnitType.BUILDING) return new BuildingWindow(desktop, (Building) unit);
        else if (unit.getUnitType() == UnitType.EVA_SUIT) return new EquipmentWindow(desktop, (EVASuit) unit);
        else if (unit.getUnitType() == UnitType.EQUIPMENT) return new EquipmentWindow(desktop, (Equipment) unit);
        else return null;
    }
}
