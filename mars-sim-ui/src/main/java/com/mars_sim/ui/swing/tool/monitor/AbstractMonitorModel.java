/*
 * Mars Simulation Project
 * AbstractMonitorModel.java
 * @date 2023-09-18
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.tool.monitor;

import java.io.Serializable;

import javax.swing.table.AbstractTableModel;

import com.mars_sim.tools.Msg;

/**
 * Default implementation of the MonitorModel
 */
public abstract class AbstractMonitorModel extends AbstractTableModel
        implements MonitorModel {

    /**
     * Helper class to define the specification of a column.
     */
    protected record ColumnSpec (String name, Class<?> type) implements Serializable {};

    private String name;
    private String countingMsgKey;
    private ColumnSpec[] columns;

    protected AbstractMonitorModel(String name, String countingMsgKey, ColumnSpec[] columns) {
        this.name = name;
        this.countingMsgKey = countingMsgKey;
        this.columns = columns;
    }


    /**
	 * Returns the number of columns.
	 *
	 * @return column count.
	 */
	@Override
	public int getColumnCount() {
		return columns.length;
	}

	/**
	 * Returns the type of the column requested.
	 *
	 * @param columnIndex Index of column.
	 * @return Class of specified column.
	 */
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		if ((columnIndex >= 0) && (columnIndex < columns.length)) {
			return columns[columnIndex].type;
		}
		return Object.class;
	}

	/**
	 * Returns the name of the column requested.
	 *
	 * @param columnIndex Index of column.
	 * @return name of specified column.
	 */
	@Override
	public String getColumnName(int columnIndex) {
		if ((columnIndex >= 0) && (columnIndex < columns.length)) {
			return columns[columnIndex].name;
		}
		return "Unknown";
	}

	/**
	 * Gets the name of the model.
	 *
	 * @return model name.
	 */
	@Override
	public String getName() {
		return name;
	}

    /**
	 * Gets the model count string.
	 */
	@Override
	public String getCountString() {
		return "  " + Msg.getString(countingMsgKey, getRowCount());
	}

    /**
     * Default implementation return null as no tooltips are supported by default.
     * 
     * @param rowIndex Row index of cell
     * @param columnIndex Column index of cell
     * @return Return null by default
     */
    @Override
    public String getToolTipAt(int rowIndex, int columnIndex) {
        return null;
    }

    /**
     * Tidies up any listeners or external dependencies.
     */
    @Override
	public void destroy() {
        // Nothing to do for this base class
    }
}
