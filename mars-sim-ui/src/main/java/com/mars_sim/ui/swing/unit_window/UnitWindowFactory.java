/*
 * Mars Simulation Project
 * UnitWindowFactory.java
 * @date 2024-07-12
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.unit_window;

import java.util.Properties;

import com.mars_sim.core.Entity;
import com.mars_sim.core.Unit;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.construction.ConstructionSite;
import com.mars_sim.core.equipment.Equipment;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.UIConfig.WindowSpec;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.tool.construction.ConstructionSiteWindow;
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
     * Gets a new entity content panel for a given entity.
     *
     * @param ent the entity the panel is for.
     * @param context the UI context.
     * @param initProps any initial properties for the window.
     * @return entity content panel; maybe null if Entity not supported.
     */
    public static EntityContentPanel getEntityPanel(Entity ent, UIContext context, WindowSpec initProps) {
        // Find the initial properties
        Properties props;
        if (initProps != null) {
            props = initProps.props();
        }
        else {
            props = new Properties();
        }

        return switch (ent) {
            case Equipment e -> new EquipmentUnitWindow(context, e, props);
            default -> null;
        };
    }

    /**
     * Gets a new unit window for a given unit.
     *
     * @param unit the unit the window is for.
     * @param desktop the main desktop.
     * @return unit window
     */
    public static UnitWindow getUnitWindow(Unit unit, MainDesktopPane desktop) {
        return switch (unit) {
            case Person p -> new PersonUnitWindow(desktop, p);
            case Robot r -> new RobotUnitWindow(desktop, r);
            case Vehicle v -> new VehicleUnitWindow(desktop, v);
            case Settlement s -> new SettlementUnitWindow(desktop, s);
            case Building b -> new BuildingUnitWindow(desktop, b);
            case ConstructionSite cs -> new ConstructionSiteWindow(desktop, cs);
            default -> null;
        };
    }
}
