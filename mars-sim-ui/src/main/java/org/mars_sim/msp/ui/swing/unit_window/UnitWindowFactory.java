/*
 * Mars Simulation Project
 * UnitWindowFactory.java
 * @date 2023-06-07
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window;

import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.construction.ConstructionSite;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.tool.construction.ConstructionSiteWindow;
import org.mars_sim.msp.ui.swing.unit_window.equipment.EquipmentUnitWindow;
import org.mars_sim.msp.ui.swing.unit_window.person.PersonUnitWindow;
import org.mars_sim.msp.ui.swing.unit_window.robot.RobotUnitWindow;
import org.mars_sim.msp.ui.swing.unit_window.structure.SettlementUnitWindow;
import org.mars_sim.msp.ui.swing.unit_window.structure.building.BuildingUnitWindow;
import org.mars_sim.msp.ui.swing.unit_window.vehicle.VehicleUnitWindow;

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
            return new PersonUnitWindow(desktop, (Person) unit);
        case ROBOT:
            return new RobotUnitWindow(desktop, (Robot) unit);
        case VEHICLE:
            return new VehicleUnitWindow(desktop, (Vehicle) unit);
        case SETTLEMENT:
            return new SettlementUnitWindow(desktop, unit);
        case BUILDING:
            return new BuildingUnitWindow(desktop, (Building) unit);
        case EVA_SUIT:
            return new EquipmentUnitWindow(desktop, (EVASuit) unit);
//        case CONSTRUCTION:
//            return new ConstructionSiteWindow(desktop, (ConstructionSite) unit);  
        default:
            return null;
        }
    }
}
