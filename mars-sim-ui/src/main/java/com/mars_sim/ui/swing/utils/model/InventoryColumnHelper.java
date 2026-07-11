/*
 * Mars Simulation Project
 * InventoryColumnHelper.java
 * @date 2026-06-27
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.utils.model;

import java.util.List;
import java.util.Set;

import com.mars_sim.core.EntityEvent;
import com.mars_sim.core.equipment.EquipmentOwner;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.resource.AmountResource;
import com.mars_sim.core.resource.ItemResource;
import com.mars_sim.core.resource.ResourceType;
import com.mars_sim.ui.swing.components.ColumnSpec;
import com.mars_sim.ui.swing.utils.model.AbstractEntityModel.EntityColumnSpec;

/**
 * A utility class to help with the creation of columns for a table model based on the resource ids.
 * It supports AmountResource, ItemResource e.g. parts, Containers & EquipmentType.
 * It provides methods to create an array of EntityColumnSpec for the specified resources and columns,
 * and to convert an inbound event that references a change to an Inventory into a pseudo event that references the resource column.
 * 
 */
public class InventoryColumnHelper {
    private static final String RES_PREFIX = "res:";

    // Base value for resource columns. The actual column index is RESOURCE_VAL + resourceID
    static final int AMOUNT_VAL = 1000;
    private static final Set<Integer> SUPPORTED_TYPES = Set.of(ResourceType.AMOUNT_RESOURCE, ResourceType.ITEM_RESOURCE,
                                                                ResourceType.EQUIPMENT_RESOURCE);

    private InventoryColumnHelper() {
        /* This utility class should not be instantiated */
    }

    /**
     * Create an array of EntityColumnSpec for the specified Resources.
     * @param resources Set of resource IDs to create columns for.
     * @return Array of EntityColumnSpec covering the resource columns.  
     */
    static EntityColumnSpec[] getResourceColumn(List<Integer> resources) {
        EntityColumnSpec[] resourceColumns = new EntityColumnSpec[resources.size()];
    
        // Then add the resource columns with the pseudo event type for each resource
        int idx = 0;
        for(var resourceID : resources) {
            // Check supported resource type
            if (!SUPPORTED_TYPES.contains(ResourceType.getType(resourceID))) {
                throw new IllegalArgumentException("Unsupported resource type for resource ID: " + resourceID);
            }

            var name = ResourceType.getName(resourceID);
            var resColumn = new EntityColumnSpec(new ColumnSpec(AMOUNT_VAL + resourceID, name, Double.class, ColumnSpec.STYLE_INTEGER),
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
    static EntityEvent convertResourceToEvent(EntityEvent event, List<Integer> resources) {
        // Resource change
        var target = event.getTarget();
        int resourceID = switch (target) {
            case AmountResource ar -> ar.getID();
            case ItemResource ir -> ir.getID();
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


    public static Object getValue(EquipmentOwner owner, int columnIndex) {
        if (columnIndex >= AMOUNT_VAL) {
            int resourceID = columnIndex - AMOUNT_VAL;
            return switch (ResourceType.getType(resourceID)) {
                case ResourceType.AMOUNT_RESOURCE -> owner.getSpecificAmountResourceStored(resourceID);
                case ResourceType.ITEM_RESOURCE -> owner.getItemResourceStored(resourceID);
                case ResourceType.EQUIPMENT_RESOURCE -> owner.findNumContainersOfType(EquipmentType.convertID2Type(resourceID));
                default -> null;
            };
        }
        return null;
    }
}
