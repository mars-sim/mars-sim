/*
 * Mars Simulation Project
 * ResourceColumnHelper.java
 * @date 2026-06-27
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.utils.model;

import java.util.Set;

import com.mars_sim.core.EntityEvent;
import com.mars_sim.core.resource.AmountResource;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.ui.swing.components.ColumnSpec;
import com.mars_sim.ui.swing.utils.model.AbstractEntityModel.EntityColumnSpec;

/**
 * A utility class to help with the creation of resource columns for a table model based onthe resource ids.
 * It provides methods to create an array of EntityColumnSpec for the specified resources and columns,
 * and to convert an inbound event that references a change to an Inventory into a pseudo event that references the resource column.
 * 
 */
public class ResourceColumnHelper {
    private static final String RES_PREFIX = "res:";

    // Base value for resource columns. The actual column index is RESOURCE_VAL + resourceID
    static final int RESOURCE_VAL = 1000;

    private ResourceColumnHelper() {
        /* This utility class should not be instantiated */
    }

    /**
     * Create an array of EntityColumnSpec for the specified resources and columns.
     * Note this could be resued elsewhere.
     * @param resources Set of resource IDs to create columns for.
     * @param columns Array of named columns.
     * @return Array of EntityColumnSpec including both existing and resource columns.  
     */
    static EntityColumnSpec[] getColumns(Set<Integer> resources, EntityColumnSpec[] columns) {
        EntityColumnSpec[] resourceColumns = new EntityColumnSpec[resources.size() + columns.length];
    
        // Named columns first
        System.arraycopy(columns, 0, resourceColumns, 0, columns.length);
    
        // Then add the resource columns with the pseudo event type for each resource
        int idx = columns.length;
        for(var resourceID : resources) {
            var name =ResourceUtil.findAmountResourceName(resourceID);
            var resColumn = new EntityColumnSpec(new ColumnSpec(RESOURCE_VAL + resourceID, name, Double.class, ColumnSpec.STYLE_INTEGER),
                                Set.of(RES_PREFIX + resourceID));
            resourceColumns[idx++] = resColumn;
        }
        return resourceColumns;
    }

    /**
     * Take an inbound event that references a change to an Inventory and convert it to a pseudo event that references the resource column.
     * @param event The original event.
     * @param resources The set of monitored resource IDs.
     * @return A new event referencing the resource column, or null if the resource is not monitored.
     */
    static EntityEvent convertResourceToEvent(EntityEvent event, Set<Integer> resources) {
        // Resource change
        var target = event.getTarget();
        int resourceID = switch (target) {
            case AmountResource ar -> ar.getID();
            case Integer i -> i;
            default -> -1;
        };

        // Is the resource a monitored one
        var pseudoEventType = (resources.contains(resourceID)) ? RES_PREFIX + resourceID : null;
        if (pseudoEventType == null) {
            // Not a monitored resource
            return null;
        }
        return new EntityEvent(event.getSource(), pseudoEventType, event.getTarget());
    }
}
