/*
 * Mars Simulation Project
 * AbstractEntityModel.java
 * @date 2026-05-21
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.utils.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import com.mars_sim.core.Entity;
import com.mars_sim.core.EntityEvent;
import com.mars_sim.core.EntityListener;
import com.mars_sim.core.MonitorableEntity;
import com.mars_sim.ui.swing.components.ColumnSpec;
import com.mars_sim.ui.swing.components.EnhancedTableModel;
import com.mars_sim.ui.swing.utils.EntityModel;
import com.mars_sim.ui.swing.utils.StatefulComponent;

/**
 * A generic table model showing entities. It provides a number of predefined available columns.
 * The subclass defines which columns are to be rendered.
 * The model automatically monitors the entities for changes and updates the table as needed.
 */
abstract class AbstractEntityModel<T extends MonitorableEntity> extends AbstractTableModel
    implements EnhancedTableModel, EntityListener, EntityModel, StatefulComponent {

	private List<T> entities = Collections.emptyList();
    private ColumnSpec[] columns;

    protected AbstractEntityModel(ColumnSpec... columns) {
        this.columns = columns;
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
            SwingUtilities.invokeLater(() -> {
                cleanUp();
                entities = new ArrayList<>(newEntities);

                // reload the whole table
                fireTableDataChanged();

                entities.forEach(e -> e.addEntityListener(this));
            });
        }
    }

    /**
     * Unregister the listening for EntityEvents of the managed Entities.
     */
    @Override
    public void cleanUp() {
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

    @Override
    public void entityUpdate(EntityEvent event) {
        var source = event.getSource();
        if (entities.contains(source)) {
            // Need to check index when update is fired as rows may change
            SwingUtilities.invokeLater(() -> {
                var idx = entities.indexOf(source);
                if (idx >= 0) {
                    fireTableRowsUpdated(idx, idx);
                }
            });
        }
    }
}