/*
 * Mars Simulation Project
 * UnitWindowFactory.java
 * @date 2023-06-07
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.unit_window;

import com.mars_sim.core.Unit;
import com.mars_sim.core.equipment.EVASuit;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.unit_window.equipment.EquipmentUnitWindow;
import com.mars_sim.ui.swing.unit_window.person.PersonUnitWindow;
import com.mars_sim.ui.swing.unit_window.robot.RobotUnitWindow;
import com.mars_sim.ui.swing.unit_window.structure.SettlementUnitWindow;
import com.mars_sim.ui.swing.unit_window.structure.building.BuildingUnitWindow;
import com.mars_sim.ui.swing.unit_window.vehicle.VehicleUnitWindow;

/**
 * The UnitWindowFactory is a factory for creating unit windows for units.
 */
public class UnitWindowFactory {

    /**
     * Private constructor.
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
