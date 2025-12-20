/**
 * Mars Simulation Project
 * EntityMonitorModel.java
 * @date 2021-12-07
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.tool.monitor;

import com.mars_sim.core.Entity;
import com.mars_sim.core.EntityListener;
import com.mars_sim.core.MonitorableEntity;
import com.mars_sim.ui.swing.utils.ColumnSpec;
import com.mars_sim.ui.swing.utils.EntityModel;

/**
 * The table model of Entities objects. It provides the ability to view and monitor Entities in the MonitorTab.
 * Any entities added will be automatically monitored for changes via the EntityListener interface.
 */
@SuppressWarnings("serial")
public abstract class EntityMonitorModel<T extends MonitorableEntity> extends CachingTableModel<T>
		implements EntityListener, EntityModel {

	private boolean monitorEntities = false;

	/**
	 * Constructor define an explicit name and counting definitions.
	 * @param name
	 * @param countingMsgKey
	 * @param columns Defines the columns in the table
	 */
	protected EntityMonitorModel(String name, String countingMsgKey, ColumnSpec[] columns) {
		super(name, columns);

		setCountingMsgKey(countingMsgKey);
	}

	/**
	 * Constructor 
	 * @param name Name of the table commonly is the plural of the entity
	 * @param columns Defines the columns in the table
	 */
	protected EntityMonitorModel(String name, ColumnSpec[] columns) {
		super(name, columns);
	}

	/**
	 * Sets whether the changes to the Entities should be monitor for change. Sets up the 
	 * EntityListeners for the Entities in the table.
	 * 
	 * @param activate Start watching entities or disable watching.
	 */
	@Override
    public void setMonitorEntities(boolean activate) {
		if (activate != monitorEntities) {
			if (activate) {
				getItems().forEach(e -> e.addEntityListener(this));
			}
			else {
				getItems().forEach(e -> e.removeEntityListener(this));
			}
			monitorEntities = activate;
		}
	}

	/**
	 * Adds a unit to the model. Attaches a listener to the Unit.
	 *
	 * @param newUnit Unit to add to the model.
	 */
	@Override
	protected boolean addItem(T newUnit) {
		boolean added = super.addItem(newUnit);
		if (added && monitorEntities) {
			newUnit.addEntityListener(this);
		}
		return added;
	}

	/**
	 * Removes a unit from the model.
	 *
	 * @param oldUnit Unit to remove from the model.
	 */
	@Override
	protected void removeItem(T oldUnit) {
		if (monitorEntities) {
			oldUnit.removeEntityListener(this);
		}
		super.removeItem(oldUnit);
	}

    /**
     * Gets the associated Entity at a row index.
     * 
     * @param row Index
     * @return Entity associated 
     */
	@Override
    public Entity getAssociatedEntity(int row) {
		return getItem(row);
	}

	/**
	 * Prepares the model for deletion.
	 */
	@Override
	public void destroy() {
		getItems().forEach(unit -> unit.removeEntityListener(this));
		super.destroy();
	}
}