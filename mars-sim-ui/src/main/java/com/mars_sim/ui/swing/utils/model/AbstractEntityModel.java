/*
 * Mars Simulation Project
 * AbstractEntityModel.java
 * @date 2026-05-21
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.utils.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.table.AbstractTableModel;

import com.mars_sim.core.Entity;
import com.mars_sim.core.EntityEvent;
import com.mars_sim.core.EntityListener;
import com.mars_sim.core.MonitorableEntity;
import com.mars_sim.ui.swing.components.ColumnSpec;
import com.mars_sim.ui.swing.components.EnhancedTableModel;
import com.mars_sim.ui.swing.utils.EntityModel;
import com.mars_sim.ui.swing.utils.StatefulComponent;
import com.mars_sim.ui.swing.utils.SwingHelper;

/**
 * A generic table model showing entities. It provides a number of predefined available columns.
 * The subclass defines which columns are to be rendered.
 * The model automatically monitors the entities for changes and updates the table as needed.
 */
public abstract class AbstractEntityModel<T extends MonitorableEntity> extends AbstractTableModel
    implements EnhancedTableModel, EntityListener, EntityModel, StatefulComponent {

	// Used to associate column index with column spec and event types to listen for
    public record EntityColumnSpec(ColumnSpec column, Set<String> eventTypes) {}

	private List<T> entities = new ArrayList<>();
    private ColumnSpec[] columns;
    private Map<String, List<Integer>> monitoredEvents = new HashMap<>();

    /**
     * Create a generic entity model with the specified columns.
     * @param columns Columns to render
     */
    protected AbstractEntityModel(EntityColumnSpec[] columns) {
        Set<Integer> valIds = new HashSet<>();

        // Create the event map by extracting the event types against the index of the EntityColumnSpec
        int idx = 0;
        for (EntityColumnSpec ecs : columns) {
            if (ecs.eventTypes() != null) {
                for (String eventType : ecs.eventTypes()) {
                    monitoredEvents.computeIfAbsent(eventType, k -> new ArrayList<>()).add(idx);
                }
            }
            idx++;
            
            valIds.add(ecs.column().id());
        }

        // Check all columns have unique Ids
        if (valIds.size() != columns.length) {
            throw new IllegalArgumentException("Column Ids must be unique");
        }

        this.columns = Arrays.stream(columns).map(EntityColumnSpec::column).toArray(ColumnSpec[]::new);
    }

    /**
     * Updates the entities to be shown.
     * The model will automatically monitor the new entities for changes and update the table as needed.
     * @param newEntities New entities to display.
     * @return true if the entities were updated, false if the new entities are the same as the current entities.
     */
    public boolean setEntities(Collection<? extends T> newEntities) {
        // Cannot use straight equals because parameter is not a list
        if (newEntities.size() != entities.size() || !entities.containsAll(newEntities)) {
            release();
            entities = new ArrayList<>(newEntities);
            
            // If there are no monitored events, then no need to register as listener
            if (!monitoredEvents.isEmpty()) {
                entities.forEach(e -> enableListener(e, true));
            }
            // Update in swing thread as table has sorting
            SwingHelper.runInEDT(this::fireTableDataChanged);
            return true;
        }

        return false;
    }

    /**
     * Add an entity to the model. This check the entity is not already present.
     * @param entity Entity to add.
     */
    public void addEntity(T entity) {
        if (!entities.contains(entity)) {
            entities.add(entity);
            int index = entities.size() - 1;
            if (!monitoredEvents.isEmpty()) {
                enableListener(entity, true);
            }

            SwingHelper.runInEDT(() -> fireTableRowsInserted(index, index));
        }
    }

    /**
     * Remove an entity from the model.
     * @param entity Entity to remove.
     */
    public void removeEntity(T entity) {
        int index = entities.indexOf(entity);
        if (index >= 0) {
            entities.remove(index);
            enableListener(entity, false);
            SwingHelper.runInEDT(() -> fireTableRowsDeleted(index, index));
        }
    }

    /**
     * When releasing the model, the listeners for each entity are deactivated via #enableListeners(false).
     * Subclasses may override to release any additional resources listerners but should call super.release() to ensure the entity listeners are removed.
     */
    @Override
    public void release() {
       enableListeners(false);
    }

    /**
     * Enable or disable the entity listeners for the model.
     * The default just handles the entities in the model, but subclasses may have additional entities to monitor.
     * @param activate Activate the listeners if true, disable if false.
     */
    public void enableListeners(boolean activate) {
        entities.forEach(e -> enableListener(e, activate));
    }

    /**
     * Enable the listeners required for the entity. The default implementation adds a EntityListener to the entity.
     * Subclasses may override to add additional listeners.
     * @param entity Source of events
     * @param activate Activiate listeners
     */
    protected void enableListener(T entity, boolean activate) {
        if (activate) {
            entity.addEntityListener(this);
        }
        else {
            entity.removeEntityListener(this);
        }
    }

    @Override
    public int getRowCount() {
        return entities.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columns[columnIndex].type();    }

    @Override
    public String getColumnName(int columnIndex) {
        return columns[columnIndex].name();
    }

    @Override
    public Entity getAssociatedEntity(int row) {
        return entities.get(row);
    }

    @Override
    public ColumnSpec getColumnSpec(int modelIndex) {
        return columns[modelIndex];
    }

    /**
     * A tooltip is needed for a specific cell in the model. The implementation resolves the relevant column spec and entity
     * and delegates to getEntityDescription to get the tooltip text.
     * @param rowIndex Row index of the cell.
     * @param columnIndex Column index of the cell.
     * @return Tooltip text for the cell, or null if no tooltip is provided.
     */
    @Override
    public String getToolTipAt(int rowIndex, int columnIndex) {
        var spec = getColumnSpec(columnIndex);
        if (rowIndex < 0 || rowIndex >= entities.size()) {
            return null;
        }
        var entity = entities.get(rowIndex);
        return getEntityDescription(entity, spec.id());
    }
    
    /**
     * Get a cell description for the associated Entity. The description is a longer version of the value commonly used for tooltip.
     * Column index maps to the associated ColumnSpec where the id is used to determine the value to return.
     * Default implementatino return null, override to provide descriptions.
     * @param entity Source of value.
     * @param valueIndex Index of the value.
     * @return String description
     */
    protected String getEntityDescription(T entity, int valueIndex) {
        return null;
    }

    /**
     * Get a cell value for the associated Entity. Column index maps to the associated ColumnSpec where the id
     * is used to determine the value to return.
     * @param entity Source of value.
     * @param valueIndex Index of the value.
     * @return Object to render
     */
    protected abstract Object getEntityValue(T entity, int valueIndex);
    
    /**
     * Get a cell value for the associated Entity. Column index maps to the associated ColumnSpec where the id
     * is used to determine the value to return.
     * @param rowIndex Row index
     * @param columnIndex Column index. 
     * @return Associated value.
     */
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        var spec = getColumnSpec(columnIndex);
        if (rowIndex < 0 || rowIndex >= entities.size()) {
            return null;
        }
        var entity = entities.get(rowIndex);
        return getEntityValue(entity, spec.id());
    }

    /**
     * Handle entity updates. If there is a column impacted by this event type, the relevant cell will be updated.
     * @param event The entity event.
     */
    @Override
    public void entityUpdate(EntityEvent event) {
        var source = event.getSource();
        if (entities.contains(source)) {
            // Need to check index when update is fired as rows may change
            var impactedCols = monitoredEvents.get(event.getType());
            if (impactedCols != null) {
                // Fire event for each individual column column
                SwingHelper.runInEDT(() -> {
                    // Check row is still valid
                    var row = entities.indexOf(source);
                    if (row >= 0) {
                        impactedCols.forEach(col -> fireTableCellUpdated(row, col));
                    }
                });
            }
        }
    }
}