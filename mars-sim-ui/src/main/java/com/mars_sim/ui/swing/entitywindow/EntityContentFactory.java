/*
 * Mars Simulation Project
 * EntityContentFactory.java
 * @date 2025-12-01
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.entitywindow;

import java.util.Properties;

import com.mars_sim.core.Entity;
import com.mars_sim.core.equipment.Equipment;
import com.mars_sim.ui.swing.UIConfig.WindowSpec;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.entitywindow.equipment.EquipmentUnitWindow;

/**
 * This factory classes creates EntityContentPanel instances for various Entity types.
 */
public class EntityContentFactory {
    private EntityContentFactory() {
        // Static factory class
    }

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
            //case Authority a -> new AuthorityWindow(a, context, props);
            case Equipment e -> new EquipmentUnitWindow(context, e, props);
            default -> null;
        };
    }
}
