/*
 * Mars Simulation Project
 * DynamicAttributeLayout.java
 * @date 2026-01-11
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.utils;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * A dynamic panel implementation that supports adding JComponents accompanied by a JLabel.
 * These are added in a row layout that dynamically adjusts the number of columns based on the
 * size of the panel.
 * It uses a GridBagLayout to manage the layout.
 */
class DynamicAttributeLayout implements AttributePanel.AttributePanelLayout {

    private static final int X_PAD = 4;
    private static final int Y_PAD = 4;
    private static final int COLUMN_PAD = 2;

    private int gridPerValue;
    private int gridPerLabel;
    private int gridPerColumn;
    private int currentColumns;
    private GridBagLayout gbl;
    private JPanel container;

    // The position of a cell in terms of the grid
    private record CellPosition(int col, int row) {}
    
    /**
     * Handles resizing of the parent panel
     */
    private class Resizer extends ComponentAdapter {
        @Override
        public void componentResized(ComponentEvent e) {
            adjustLayout();
        }
    }

    public DynamicAttributeLayout(JPanel container, int cols) {
        gbl = new GridBagLayout();
        container.setLayout(gbl);
        currentColumns = cols;
        this.container = container;
        gridPerLabel = 1; // Evenly balanced between label/value. Label is always 1 grid
        gridPerValue = 1;
        gridPerColumn = gridPerLabel + gridPerValue;
        
        container.addComponentListener(new Resizer());
    }

    /**
     * Adds a labelled content to the Panel. This ensures the styling is common. This will create a label/value pair.
     * @param label The Label
     * @param value The value component.
     */
    @Override
    public void addLabelledItem(JLabel label, JComponent value) {
        int numValues = container.getComponentCount()/2;
        var cell = getCell(numValues);

        // Label consumes 1 column
        container.add(label, createLabelConstraints(cell));
        container.add(value, createValueConstraints(cell));

        adjustLayout();
    }

    /**
     * For a specific cell number, get the column and row position.
     * @param cellId Cell number
     * @return Position in terms of Grid x & y
     */
    private CellPosition getCell(int cellId) {
        int col = (cellId%currentColumns) * gridPerColumn;
        int row = cellId/currentColumns;

        return new CellPosition(col, row);
    }

    /**
     * Creates the constraints for the label part of a Cell.
     * @param cell Cell position for the label part.
     */
    private GridBagConstraints createLabelConstraints(CellPosition cell) {
        var constraints = new GridBagConstraints();
        constraints.gridx = cell.col * gridPerColumn;
        constraints.gridy = cell.row;
        constraints.gridwidth = gridPerLabel;
        constraints.anchor = GridBagConstraints.LINE_END;
        constraints.ipadx = X_PAD;
        constraints.ipady = Y_PAD;

        return constraints;
    }

    /**
     * Creates the constraints for the value part of a Cell.
     * @param cell Cell position for the value part.
     */
    private GridBagConstraints createValueConstraints(CellPosition cell) {
        var constraints = new GridBagConstraints();
        constraints.gridx = (cell.col * gridPerColumn) + gridPerLabel;
        constraints.gridy = cell.row;
        constraints.gridwidth = gridPerValue;
        constraints.anchor = GridBagConstraints.LINE_START;
        constraints.ipadx = X_PAD + (cell.col != (currentColumns-1) ? COLUMN_PAD : 0);
        constraints.ipady = Y_PAD;
        return constraints;
    }
    
    /**
     * Adjusts the layout based on the current size of the panel. Attempts to maximise the horizontal
     * use of space by fitting multiples of label/value pairs in columns.
     */
    private void adjustLayout() {
        int maxLabel = 0;
        int maxValue = 0;
        for(int i = 0; i < container.getComponentCount(); i++) {
            int w = container.getComponent(i).getWidth();
            if ((i % 2) == 0) {
                maxLabel = Math.max(maxLabel, w);
            }
            else {
                maxValue = Math.max(maxValue, w);
            }
        }
        if (maxValue == 0 || maxLabel == 0) {
            // No components yet
            return;
        }

        // Add a 10% margin
        int maxColumnWidth = (int)((maxLabel + maxValue + (X_PAD * 2) + COLUMN_PAD) * 1.1);
        int potentialColumns = container.getWidth()/maxColumnWidth;
        if ((potentialColumns == 0) || (currentColumns == potentialColumns)) {
            return;
        }
        currentColumns = potentialColumns;

        if (maxValue < maxLabel) {
            // Wide labels
            gridPerValue = 1;
            gridPerLabel = (maxColumnWidth/maxValue) - 1;
        }
        else {
            // Values wider
            gridPerLabel = 1;
            gridPerValue = (maxColumnWidth/maxLabel)-1;
        }
        gridPerColumn = gridPerLabel + gridPerValue;

        int numValues = container.getComponentCount()/2;
        for(int i = 0; i < numValues; i++) {
            var cell = getCell(i);
            int offset = i * 2;
            var label = container.getComponent(offset);
            gbl.setConstraints(label, createLabelConstraints(cell));

            var value = container.getComponent(offset+1);
            gbl.setConstraints(value, createValueConstraints(cell));
        }
    }

	/**
	 * Adds a blank cell to the panel as an empty label and value
	 */
	@Override
	public void addBlankCell() {
		// Does nothing as the layout is dynamic
	}
}
