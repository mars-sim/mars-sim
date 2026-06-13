/*
 * Mars Simulation Project
 * AchievementTableModel.java
 * @date 2026-06-09
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.utils;

import java.util.function.Function;

import javax.swing.table.AbstractTableModel;

import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.components.ColumnSpec;
import com.mars_sim.ui.swing.components.EnhancedTableModel;

/**
 * Table model for Science achievement table.
 */
public class AchievementTableModel extends AbstractTableModel implements EnhancedTableModel {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
    private static final ColumnSpec SCIENCE = new ColumnSpec(0, Msg.getString("scientificstudy.science"), String.class);
    private static final ColumnSpec ACHIEVEMENT_CREDIT = new ColumnSpec(1, Msg.getString("TabPanelScience.column.achievementCredit"), Double.class);

	private ScienceType[] sciences;
    private Function<ScienceType, Double> resolver;

	/** hidden constructor. */
	public AchievementTableModel(Function<ScienceType, Double> resolver) {
        this.resolver = resolver;
		sciences = ScienceType.values();
	}

	/**
	 * Returns the number of columns in the model.
	 * @return the number of columns in the model.
	 */
	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public String getColumnName(int columnIndex) {
		return getColumnSpec(columnIndex).name();
	}

	/**
	 * Returns the most specific superclass for all the cell values in the column.
	 * @param columnIndex the index of the column.
	 * @return the common ancestor class of the object values in the model.
	 */
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return getColumnSpec(columnIndex).type();
	}

	/**
	 * Returns the number of rows in the model.
	 * @return the number of rows in the model.
	 */
	@Override
	public int getRowCount() {
		return sciences.length;
	}

	/**
	 * Returns the value for the cell at columnIndex and rowIndex.
	 * @param rowIndex the row whose value is to be queried.
	 * @param columnIndex the column whose value is to be queried.
	 * @return the value Object at the specified cell.
	 */
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Object result = null;
		if ((rowIndex >= 0) && (rowIndex < sciences.length)) {
			ScienceType science = sciences[rowIndex];
			if (columnIndex == 0) result = science.getName();
			else if (columnIndex == 1) {
				result = resolver.apply(science);
			}
		}
		return result;
	}

	/**
	 * Updates the table model.
	 */
	public void update() {
		for (int i = 0; i < sciences.length; i++) {
            fireTableCellUpdated(i, 1);
        }
	}

    @Override
    public String getToolTipAt(int row, int col) {
        return null;
    }

    @Override
    public ColumnSpec getColumnSpec(int modelIndex) {
        return switch (modelIndex) {
            case 0 -> SCIENCE;
            case 1 -> ACHIEVEMENT_CREDIT;
            default -> null;
        };
    }
}