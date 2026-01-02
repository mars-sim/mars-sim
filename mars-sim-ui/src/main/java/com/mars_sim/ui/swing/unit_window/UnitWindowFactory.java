/*
 * Mars Simulation Project
 * UnitWindowFactory.java
 * @date 2024-07-12
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.unit_window;

import com.mars_sim.core.Unit;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.construction.ConstructionSite;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.tool.construction.ConstructionSiteWindow;
import com.mars_sim.ui.swing.unit_window.structure.SettlementUnitWindow;
import com.mars_sim.ui.swing.unit_window.structure.building.BuildingUnitWindow;

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
        return switch (unit) {
            case Settlement s -> new SettlementUnitWindow(desktop, s);
            case Building b -> new BuildingUnitWindow(desktop, b);
            case ConstructionSite cs -> new ConstructionSiteWindow(desktop, cs);
            default -> null;
        };
    }
}
