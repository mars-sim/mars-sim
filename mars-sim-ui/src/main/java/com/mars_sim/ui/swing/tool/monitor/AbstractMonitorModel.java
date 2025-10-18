/*
 * Mars Simulation Project
 * AbstractMonitorModel.java
 * @date 2023-09-18
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.tool.monitor;

import javax.swing.table.AbstractTableModel;

import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.utils.ColumnSpec;

/**
 * Default implementation of the MonitorModel
 */
@SuppressWarnings("serial")
public abstract class AbstractMonitorModel extends AbstractTableModel
        implements MonitorModel {

    private String name;
    private String countingMsgKey;
    private ColumnSpec[] columns;
	private int settlementColumn = -1;

    protected AbstractMonitorModel(String name, String countingMsgKey, ColumnSpec[] columns) {
        this.name = name;
        this.countingMsgKey = countingMsgKey;
        this.columns = columns;
    }

	/**
	 * This model as a Settlement column. This is a special column that can be visible/hidden according
	 * to the selection.
	 * 
	 * @param settlementColumn
	 */
	protected void setSettlementColumn(int settlementColumn) {
		this.settlementColumn = settlementColumn;
	}

	/**
	 * Gets the index of the Settlement column if defined. This is a special column that can be visible/hidden according
	 * to the selection.
	 * 
	 * @return
	 */
	@Override
	public int getSettlementColumn() {
		return settlementColumn;
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
			return columns[columnIndex].type();
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
			return columns[columnIndex].name();
		}
		return "Unknown";
	}

	/**
	 * Get any defined style for this value.
	 * @param columnIndex the index of the column.
	 * @return see ColumnSpec for the style.
	 */
	@Override
    public int getColumnStyle(int columnIndex) {
		if ((columnIndex >= 0) && (columnIndex < columns.length)) {
			return columns[columnIndex].style();
		}
		return ColumnSpec.STYLE_DEFAULT;
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
