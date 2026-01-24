/*
 * Mars Simulation Project
 * FixedAttributeLayout.java
 * @date 2023-08-27
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.utils;

import java.awt.GridLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * A simple panel implementation that supports adding JComponents accompanied by a JLabel.
 * These are added in a with row loayout.
 */
@SuppressWarnings("serial")
class FixedAttributeLayout implements AttributePanel.AttributePanelLayout {

	private static final int DEFAULT_GAP = 2;

	private int gridCols;
	private JPanel container;
	private GridLayout autoLayout = null;

    public FixedAttributeLayout(JPanel container, int cols) {
		this.gridCols = cols * 2;
        autoLayout = new GridLayout(1, gridCols, DEFAULT_GAP, DEFAULT_GAP);
		container.setLayout(autoLayout);
		this.container = container;
    }

	/**
	 * Adds a labelled content to the TabPanel. This ensures the styling is common.
	 * 
	 * @param title Label to add
	 * @param content Content showing the value
	 */
	@Override
	public void addLabelledItem(JLabel title, JComponent content) {
		container.add(title);
		container.add(content);

		// Set the layout as a grid based on the number of rows added
		int components = container.getComponentCount();
		int rows = Math.ceilDiv(components, gridCols);
		autoLayout.setRows(rows);
	}

	/**
	 * Adds a blank cell to the panel as an empty label and value
	 */
	@Override
	public void addBlankCell() {
		// Add two blanks to represent a label/value pair
		addLabelledItem(new JLabel(), new JLabel());
	}
}