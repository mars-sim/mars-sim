/*
 * Mars Simulation Project
 * ColumnSpecHelper.java
 * @date 2026-02-07
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.components;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import com.mars_sim.core.time.MarsTime;

/**
 * This is a helper class that utilises the ColumnSpec in the EnhancedTableModel.
 */
public final class ColumnSpecHelper {

    private ColumnSpecHelper() {
        // Private constructor to prevent instantiation
    }

    /**
     * Applies the appropriate renderers to the columns of the target JTable based on the column styles defined in the EnhancedTableModel.
     * The method checks for the column style first, and if not defined, it falls back to checking the column class to determine the appropriate renderer.
     * Individual renderers are created as renderers are not thread safe.
     * @param target Table to apply renderers.
     */
    public static void applyRenderers(JTable target) {
        EnhancedTableModel enhancedModel = null;
        if (target.getModel() instanceof EnhancedTableModel em) {
            enhancedModel = em;
        }
        var colModel = target.getColumnModel();
	    for(int colId = 0; colId < colModel.getColumnCount(); colId++) {
            TableCellRenderer renderer = null;
            var col = colModel.getColumn(colId);

			// Use the enhanced model index to handle the table hiding or reordering columns
            if (enhancedModel != null) {
                var style = enhancedModel.getColumnStyle(col.getModelIndex());
                renderer = switch (style) {
                    case ColumnSpec.STYLE_CURRENCY -> new NumberCellRenderer(2, "$");
                    case ColumnSpec.STYLE_INTEGER -> new NumberCellRenderer(0);
                    case ColumnSpec.STYLE_DIGIT1 -> new NumberCellRenderer(1);
                    case ColumnSpec.STYLE_DIGIT2 -> new NumberCellRenderer(2);
                    case ColumnSpec.STYLE_DIGIT3 -> new NumberCellRenderer(3);
                    case ColumnSpec.STYLE_PERCENTAGE -> new PercentageTableCellRenderer(false);
                    default -> null;
                };
            }

            // No style renderer defined, so use return type
            if (renderer == null) {
                // Switch wouldbe nice but desnpt work with Class<?> types
                Class<?> colClass = target.getModel().getColumnClass(colId);
                if (Integer.class.isAssignableFrom(colClass)) {
                    renderer = new NumberCellRenderer(0);
                } else if (Double.class.isAssignableFrom(colClass)) {
                    renderer = new NumberCellRenderer(1);
                } else if (Number.class.isAssignableFrom(colClass)) {
                    renderer = new NumberCellRenderer(2);
                } else if (MarsTime.class.isAssignableFrom(colClass)) {
                    renderer = new MarsTimeTableCellRenderer();
                }
            }
           
            if (renderer != null) {
                col.setCellRenderer(renderer);
            }
        }
    }
}
