/*
 * Mars Simulation Project
 * EntityContentFactory.java
 * @date 2025-12-01
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.entitywindow;

import java.util.Properties;

import com.mars_sim.core.Entity;
import com.mars_sim.core.Simulation;
import com.mars_sim.core.UnitType;
import com.mars_sim.core.authority.Authority;
import com.mars_sim.core.building.construction.ConstructionSite;
import com.mars_sim.core.equipment.Equipment;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.science.ScientificStudy;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.entitywindow.authority.AuthorityWindow;
import com.mars_sim.ui.swing.entitywindow.construction.ConstructionSiteWindow;
import com.mars_sim.ui.swing.entitywindow.equipment.EquipmentUnitWindow;
import com.mars_sim.ui.swing.entitywindow.science.ScientificStudyWindow;
import com.mars_sim.ui.swing.unit_window.person.PersonUnitWindow;
import com.mars_sim.ui.swing.unit_window.robot.RobotUnitWindow;
import com.mars_sim.ui.swing.unit_window.vehicle.VehicleUnitWindow;

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
     * @param props any initial properties for the panel.
     * @return entity content panel; maybe null if Entity not supported.
     */
    public static EntityContentPanel getEntityPanel(Entity ent, UIContext context, Properties props) {
        return switch (ent) {
            case Authority a -> new AuthorityWindow(a, context, props);
            case ConstructionSite cs -> new ConstructionSiteWindow(cs, context, props);
            case Equipment e -> new EquipmentUnitWindow(e, context, props);
            case Person p -> new PersonUnitWindow(p, context, props);
            case Robot r -> new RobotUnitWindow(r, context, props);
            case ScientificStudy s -> new ScientificStudyWindow(s, context, props);
            case Vehicle v -> new VehicleUnitWindow(v, context, props);
            default -> null;
        };
    }

    
	/**
	 * Finds a Entity from a previously generated UI Settings instance.
	 * 
	 * @see #getUIProps()
	 * @param sim
	 * @param settings
	 * @return
	 */
	public static Entity getEntity(Simulation sim, Properties settings) {
		String type = settings.getProperty(EntityContentPanel.UNIT_TYPE);
		String name = settings.getProperty(EntityContentPanel.UNIT_NAME);

		if ((type == null) || (name == null)) {
            return null;
        }

        if ("AUTHORITY".equals(type)) {
            return sim.getConfig().getReportingAuthorityFactory().getItem(name);
        }
        else if ("SCIENTIFICSTUDY".equals(type)) {
            // Find the study matching the given name
            return sim.getScientificStudyManager().getAllStudies().stream()
                .filter(study -> study.getName().equals(name))
                .findFirst()
                .orElse(null);
        }

        // Default to UnitManager lookup
		UnitType uType = UnitType.valueOf(type);
		return sim.getUnitManager().getUnitByName(uType, name);
	}
}
