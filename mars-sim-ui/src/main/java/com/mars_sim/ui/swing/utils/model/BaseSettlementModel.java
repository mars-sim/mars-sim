/*
 * Mars Simulation Project
 * BaseSettlementModel.java
 * @date 2026-06-08
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.utils.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.mars_sim.core.EntityEvent;
import com.mars_sim.core.EntityEventType;
import com.mars_sim.core.building.utility.power.PowerGrid;
import com.mars_sim.core.mission.MissionControl;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.components.ColumnSpec;
import com.mars_sim.ui.swing.components.TrendValue;

/**
 * A generic table model showing Settlements. It provides a number of predefined available columns.
 * The subclass defines which columns are to be rendered.
 * The model automatically monitors the Settlement for changes and updates the table as needed.
 */
@SuppressWarnings("serial")
public abstract class BaseSettlementModel extends AbstractEntityModel<Settlement> {

    private static final int NAME_VAL = 0;
	private static final int POP_VAL = 1;
	private static final int PARKED_VAL = 2;
	private static final int MISSION_VAL = 3;
	private static final int POWER_GEN_VAL = 5;
	private static final int POWER_LOAD_VAL = 6;
	private static final int ENERGY_STORED_VAL = 7;

    // Basic fixed properties of a Settlement
    protected static final EntityColumnSpec NAME = new EntityColumnSpec(new ColumnSpec(NAME_VAL, Msg.getString("entity.name"), String.class),
                                null);
    protected static final EntityColumnSpec POPULATION = new EntityColumnSpec(new ColumnSpec(POP_VAL, Msg.getString("settlement.population"), Integer.class),
                                Set.of(EntityEventType.INVENTORY_RETRIEVING_UNIT_EVENT, EntityEventType.INVENTORY_STORING_UNIT_EVENT));
    protected static final EntityColumnSpec PARKED = new EntityColumnSpec(new ColumnSpec(PARKED_VAL, Msg.getString("vehicle.plural"), Integer.class),
                                Set.of(EntityEventType.INVENTORY_RETRIEVING_UNIT_EVENT, EntityEventType.INVENTORY_STORING_UNIT_EVENT));
    protected static final EntityColumnSpec MISSION = new EntityColumnSpec(new ColumnSpec(MISSION_VAL, Msg.getString("mission.plural"), Integer.class),                            
                                Set.of(MissionControl.MISSION_ADD, MissionControl.MISSION_REMOVED));
    protected static final EntityColumnSpec POWER_GEN = new EntityColumnSpec(new ColumnSpec(POWER_GEN_VAL, "kW Gen", Double.class),
                                Set.of(PowerGrid.GENERATED_POWER_EVENT));
    protected static final EntityColumnSpec POWER_LOAD = new EntityColumnSpec(new ColumnSpec(POWER_LOAD_VAL, "kW Load", Double.class),
                                Set.of(PowerGrid.POWER_LOAD_EVENT));
    protected static final EntityColumnSpec ENERGY_STORED = new EntityColumnSpec(new ColumnSpec(ENERGY_STORED_VAL,"kWh Stored", String.class),
                                Set.of(PowerGrid.STORED_ENERGY_EVENT));

    // Resource columns
    private List<Integer> resources = new ArrayList<>();

    // Tracks the direction of change of resources; null if trends are not shown
    private ResourceTrendTracker<Settlement> trends;

    /**
     * Creates a generic building model with the specified columns.
     * 
     * @param columns Columns to show.
     */
    protected BaseSettlementModel(EntityColumnSpec... columns) {
        super(columns);
    }

    /**
     * Add resource columns to the model. The resource columns are created for the specified list of resource IDs.
     * @param resources Resource IDs to add
     */
    protected void addResourceColumns(List<Integer> resources) {
        addResourceColumns(resources, false);
    }

    /**
     * Add resource columns to the model. The resource columns are created for the specified list of resource IDs.
     * @param resources Resource IDs to add
     * @param showTrend If true the columns also show whether each amount is increasing or decreasing
     */
    protected void addResourceColumns(List<Integer> resources, boolean showTrend) {
        addColumns(InventoryColumnHelper.getResourceColumn(resources, showTrend));
        this.resources.addAll(resources);
        if (showTrend && trends == null) {
            trends = new ResourceTrendTracker<>();
        }
    }

    /**
     * Releases the model and forgets any tracked trends.
     */
    @Override
    public void release() {
        super.release();
        if (trends != null) {
            trends.clear();
        }
    }

    /**
     * Removes a Settlement and forgets its tracked trends.
     *
     * @param entity Settlement to remove
     */
    @Override
    public void removeEntity(Settlement entity) {
        super.removeEntity(entity);
        if (trends != null) {
            trends.remove(entity);
        }
    }

    /**
     * Filters the inventory events when resources change.
     * 
     * @param entity Receiving entity
     * @param eventType Event type
     */
    @Override
    public void entityUpdate(EntityEvent event) {
        if (event.getType().equals(EntityEventType.INVENTORY_RESOURCE_EVENT)) {
            event = InventoryColumnHelper.convertResourceToEvent(event, resources);
            if (event == null) {
                // Not a monitored resource
                return;
            }

            // Recalculate the trend only when the amount actually changes, not on every repaint
            if (trends != null && event.getSource() instanceof Settlement s) {
                int resourceID = InventoryColumnHelper.getResourceID(event.getTarget());
                var amount = InventoryColumnHelper.getValue(s.getEquipmentInventory(),
                                        InventoryColumnHelper.AMOUNT_VAL + resourceID);
                if (amount instanceof Number n) {
                    trends.update(s, resourceID, n.doubleValue());
                }
            }
        }

        super.entityUpdate(event);
    }

    /**
     * Gets a cell value for the associated Building. Column index maps to the associated ColumnSpec where the id
     * is used to determine the value to return.
     * 
     * @param entity The Building entity.
     * @param valueIndex Column index.
     * @return Associated value.
     */
    @Override
    protected Object getEntityValue(Settlement entity, int valueIndex) {
        return switch (valueIndex) {
            case NAME_VAL -> entity.getName();
            case POP_VAL -> entity.getNumCitizens();
            case PARKED_VAL -> entity.getNumParkedNGaragedVehicles();
            case MISSION_VAL -> entity.getMissionControl().getActiveMissions().size();
            case POWER_GEN_VAL -> entity.getPowerGrid().getGeneratedPower();
            case POWER_LOAD_VAL -> entity.getPowerGrid().getPowerLoad();
            case ENERGY_STORED_VAL -> entity.getPowerGrid().displayStoredEnergy();
            default -> getResourceValue(entity, valueIndex);
        };
    }

    /**
     * Gets the value of a resource column. If trends are shown the amount is wrapped with its last trend.
     *
     * @param entity The Settlement.
     * @param valueIndex Column value index.
     * @return Resource amount, or null if not a resource column.
     */
    private Object getResourceValue(Settlement entity, int valueIndex) {
        var amount = InventoryColumnHelper.getValue(entity.getEquipmentInventory(), valueIndex);
        if (trends != null && amount instanceof Number n) {
            int resourceID = valueIndex - InventoryColumnHelper.AMOUNT_VAL;
            return new TrendValue(n.doubleValue(), trends.getTrend(entity, resourceID));
        }
        return amount;
    }
}