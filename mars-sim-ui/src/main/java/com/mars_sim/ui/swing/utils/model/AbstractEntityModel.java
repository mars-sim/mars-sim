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
import java.util.Collections;
import java.util.HashMap;
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
abstract class AbstractEntityModel<T extends MonitorableEntity> extends AbstractTableModel
    implements EnhancedTableModel, EntityListener, EntityModel, StatefulComponent {

    // Used to associate column index with column spec and event types to listen for
    public record EntityColumnSpec(ColumnSpec column, Set<String> eventTypes) {}

    
	private List<T> entities = Collections.emptyList();
    private ColumnSpec[] columns;
    private Map<String, List<Integer>> monitoredEvents = new HashMap<>();


    protected AbstractEntityModel(EntityColumnSpec... columns) {
        // Create the event map by extracting the event types against the index of the EntityColumnSpec
        int idx = 0;
        for (EntityColumnSpec ecs : columns) {
            if (ecs.eventTypes() != null) {
                for (String eventType : ecs.eventTypes()) {
                    monitoredEvents.computeIfAbsent(eventType, k -> new ArrayList<>()).add(idx);
                }
            }
            idx++;
        }
        this.columns = Arrays.stream(columns).map(EntityColumnSpec::column).toArray(ColumnSpec[]::new);
    }

    /**
     * Updates the entities to be shown.
     * The model will automatically monitor the new entities for changes and update the table as needed.
     * @param newEntities New entities to display.
     */
    public void setEntities(Collection<T> newEntities) {
        // Cannot use straight equals because parameter is not a list
        if (newEntities.size() != entities.size() || !entities.containsAll(newEntities)) {
            // Update in swing thread as table has sorting
            SwingHelper.runInEDT(() -> {
                release();
                entities = new ArrayList<>(newEntities);

                // reload the whole table
                fireTableDataChanged();

                // If there are no monitored events, then no need to register as listener
                if (!monitoredEvents.isEmpty()) {
                    entities.forEach(e -> e.addEntityListener(this));
                }
            });
        }
    }

    /**
     * Unregister the listening for EntityEvents of the managed Entities.
     */
    @Override
    public void release() {
       entities.forEach(e -> e.removeEntityListener(this));
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

    @Override
    public String getToolTipAt(int row, int col) {
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