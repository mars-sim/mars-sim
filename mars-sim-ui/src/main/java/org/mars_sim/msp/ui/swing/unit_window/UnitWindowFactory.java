/*
 * Mars Simulation Project
 * UnitWindowFactory.java
 * @date 2021-12-20
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window;

import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.robot.Robot;
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

        switch (unit.getUnitType()) {
        case PERSON:
            return new PersonWindow(desktop, (Person) unit);
        case ROBOT:
            return new RobotWindow(desktop, (Robot) unit);
        case VEHICLE:
            return new VehicleWindow(desktop, (Vehicle) unit);
        case SETTLEMENT:
            return new SettlementUnitWindow(desktop, unit);
        case BUILDING:
            return new BuildingWindow(desktop, (Building) unit);
        case EVA_SUIT:
            return new EquipmentWindow(desktop, (EVASuit) unit);
        default:
            return null;
        }
    }
}
