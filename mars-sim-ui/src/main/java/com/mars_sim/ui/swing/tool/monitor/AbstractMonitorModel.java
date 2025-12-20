/*
 * Mars Simulation Project
 * AbstractMonitorModel.java
 * @date 2023-09-18
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.tool.monitor;

import java.util.Collections;
import java.util.Set;

import javax.swing.table.AbstractTableModel;

import com.mars_sim.core.structure.Settlement;
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
	private Set<Settlement> selected = Collections.emptySet();

    protected AbstractMonitorModel(String name, ColumnSpec[] columns) {
        this.name = name;

        this.columns = columns;
    }

	/**
	 * Allows the table model to define a custom counting message key.
	 * @param countingMsgKey
	 */
	protected void setCountingMsgKey(String countingMsgKey) {
		this.countingMsgKey = countingMsgKey;
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
		if (countingMsgKey == null) {
			return name + "  " + getRowCount();
		}
		return "  " + Msg.getString(countingMsgKey, getRowCount());
	}

	/**
	 * Apply the Settlement as a filter. Call the internal method
	 */
	@Override
	public boolean setSettlementFilter(Set<Settlement> selectedSettlements) {
		var result = applySettlementFilter(selectedSettlements);

		// Do not update the state until the filter has been applied
		this.selected = selectedSettlements;
		return result;
	}

	/**
	 * Get the currently selected settlements.
	 */
	protected Set<Settlement> getSelectedSettlements() {
		return selected;
	}

	/**
	 * Reapplies the current settlement filter.
	 */
	protected void reapplyFilter() {
		applySettlementFilter(selected);
	}

	/**
	 * Applies the settlement filter to the model. This should be overridden by subclasses.
	 * 
	 * @param selectedSettlement Settlements to filter by.
	 * @return true if the filter was applied.
	 */
	protected boolean applySettlementFilter(Set<Settlement> selectedSettlement) {
		return true;
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
