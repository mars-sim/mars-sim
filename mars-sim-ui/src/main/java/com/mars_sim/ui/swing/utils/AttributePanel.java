/*
 * Mars Simulation Project
 * AttributePanel.java
 * @date 2023-08-27
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.utils;

import java.awt.GridLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.mars_sim.ui.swing.StyleManager;

/**
 * A simple panel implementation that supports adding JComponents accompanied by a JLabel.
 * These are added in a row layout.
 */
@SuppressWarnings("serial")
public class AttributePanel extends JPanel {

	private static final int DEFAULT_GAP = 3;

	private boolean autoLayout = false;

	/**
	 * Create an Attribute panel that has a single column with fixed number of rows
	 * @param rows Number of rows
	 */
    public AttributePanel(int rows) {
        this(rows, 1);
    }

	/**
	 * Create an Attribute panel that has a fixed number of rows and columns
	 * @param rows Number of rows
	 * @param cols Number of cols
	 */
    public AttributePanel(int rows, int cols) {
        super(new GridLayout(rows, 2*cols, DEFAULT_GAP, DEFAULT_GAP));
    }

    public AttributePanel(int rows, int cols, int hgap, int vgap) {
        super(new GridLayout(rows, 2*cols, hgap, vgap));
    }
	
	/**
	 * Create an Attribute panel that has a single column with variable numnber pf rows
	 */
	public AttributePanel() {
		autoLayout = true;
	}

	/**
	 * Adds a text field and label to a Panel. The layout should be Spring layout.
	 * 
	 * @param titleLabel The fixed label
	 * @param content Initial content of the text field
	 * @param tooltip Optional tooltip
	 * @return The JLabel that can be updated.
	 */
	public JLabel addTextField(String titleLabel, String content, String tooltip) {
		JLabel contentLabel = new JLabel(content);
		if (tooltip != null) {
			contentLabel.setToolTipText(tooltip);
		}
		addLabelledItem(titleLabel, contentLabel);
		return contentLabel;
	}
	
	/**
	 * Adds a field and label to a Panel. The layout should be Spring layout.
	 * 
	 * @param titleLabel The fixed label
	 * @param content Initial content of the text field
	 * @tooltip
	 * @return The JLabel that can be updated.
	 */
	public JLabel addRow(String titleLabel, String content, String tooltip) {
		return addTextField(titleLabel, content, tooltip);
	}
	
	/**
	 * Adds a field and label to a Panel. The layout should be Spring layout.
	 * 
	 * @param titleLabel The fixed label
	 * @param content Initial content of the text field
	 * @return The JLabel that can be updated.
	 */
	public JLabel addRow(String titleLabel, String content) {
		return addTextField(titleLabel, content, null);
	}
	
	/**
	 * Adds a labelled content to the TabPanel. This ensures the styling is common.
	 * 
	 * @param titleLabel Label to add
	 * @param content Content showing the value
	 */
	public void addLabelledItem(String titleLabel, JComponent content) {
        if ((titleLabel != null) && !titleLabel.endsWith(": ")) {
            titleLabel = titleLabel + " :";
        }
        JLabel title = new JLabel(titleLabel, SwingConstants.RIGHT);
        title.setFont(StyleManager.getLabelFont());
		add(title);
		add(content);

		// Set the layout as a grid based on the number of rows added
		if (autoLayout) {
			int rows = getComponentCount()/2;
			setLayout(new GridLayout(rows, 2, DEFAULT_GAP, DEFAULT_GAP));
		}
	}
}
