/*
 * Mars Simulation Project
 * TableModelUpdater.java
 * @date 2026-01-24
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.components;

import javax.swing.table.AbstractTableModel;

/**
 * A TableModelUpdater is a helper class to update table models in the Swing thread.
 * It can update either a single cell or the entire table.
 */
public final class TableModelUpdater  implements Runnable {

    private int row;
    private int column;
    private boolean entireData;
    private AbstractTableModel model;

    public TableModelUpdater(AbstractTableModel model, int row, int column) {
        this.model = model;
        this.row = row;
        this.column = column;
        entireData = false;
    }

    public TableModelUpdater(AbstractTableModel model) {
        this.model = model;
        entireData = true;
    }

    @Override
    public void run() {
        if (entireData) {
            model.fireTableDataChanged();
        } else {
            if (row >= 0 && row < model.getRowCount()) {
                model.fireTableCellUpdated(row, column);
            }
        }
    }
}