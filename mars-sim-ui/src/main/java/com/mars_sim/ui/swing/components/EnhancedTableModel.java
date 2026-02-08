/*
 * Mars Simulation Project
 * EnhancedTableModel.java
 * @date 2026-02-07
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.components;

import com.mars_sim.ui.swing.utils.ToolTipTableModel;

/**
 * This is an extension of the ToolTipTableModel to provide extra information on the column specification.
 * The main purpose of this is to allow the definition of column styles that can be used to apply renderers to the columns without the need for the model to return specific classes. This is useful for cases where the data is not easily represented by a specific class, such as when a column contains mixed types or when the data is derived from multiple sources.
 * The column styles are defined in the ColumnSpec and are used by teh ColumnSpecHelper.
 */
public interface EnhancedTableModel extends ToolTipTableModel {
	/**
	 * Get any defined style for this value.
	 * @param modelIndex the index of the column.
	 * @return see ColumnSpec for the style.
	 */
    public ColumnSpec getColumnSpec(int modelIndex);
}
