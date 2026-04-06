/*
 * Mars Simulation Project
 * AttributePanel.java
 * @date 2023-08-27
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.utils;

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

	interface AttributePanelLayout {
		// Adds a labelled item to the panel.
		void addLabelledItem(JLabel titleLabel, JComponent content);

		// Adds a blank cell to the panel.
		void addBlankCell();
	}

	private AttributePanelLayout attributeLayout;
	private static boolean useDyanmic = false;

	/**
	 * Sets whether to use dynamic layout or fixed layout for all AttributePanels.
	 * @param dynamic True to use dynamic layout; false for fixed layout.
	 */
	public static void setUseDynamicLayout(boolean dynamic) {
		useDyanmic = dynamic;
	}

	/**
	 * Creates an Attribute panel that has a single column with fixed number of rows.
	 * 
	 * @param rows Number of rows; this is now redunandant
	 */
    public AttributePanel(int rows) {
        this();
    }

	/**
	 * Creates an Attribute panel that has a single column with a variable number pf rows.
	 */
	public AttributePanel() {
		super();
		buildLayout(1);
	}

	private void buildLayout(int cols) {
		attributeLayout = (useDyanmic ? new DynamicAttributeLayout(this, cols) :
										new FixedAttributeLayout(this, cols));
	}

	/**
	 * Creates an Attribute panel that has a fixed number of rows and columns.
	 * 
	 * @param rows Number of rows
	 * @param cols Number of cols
	 */
    public AttributePanel(int rows, int cols) {
        super();
		buildLayout(cols);
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
		addLabelledItem(titleLabel, contentLabel, tooltip);
		return contentLabel;
	}
	
	/**
	 * Adds a field and label to a Panel. The layout should be Spring layout.
	 * 
	 * @param titleLabel The fixed label
	 * @param content Initial content of the text field
	 * @param tooltip
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
		addLabelledItem(titleLabel, content, null);
	}

	/**
	 * Adds a labelled content to the Panel. This ensures the styling is common.
	 * 
	 * @param titleLabel Label to add
	 * @param content Content showing the value
	 * @param tooltip Optional tooltip
	 */
	public void addLabelledItem(String titleLabel, JComponent content, String tooltip) {
		var fullTitle = (titleLabel != null ? titleLabel + ": " : "");
		JLabel title = new JLabel(fullTitle, SwingConstants.RIGHT);
		title.setFont(StyleManager.getLabelFont());

		if (tooltip != null) {
			title.setToolTipText(tooltip);
			content.setToolTipText(tooltip);
		}
		attributeLayout.addLabelledItem(title, content);
	}

	/**
	 * Add a blank field to the panel to keep the layout correct.
	 * This may not have any action depending upon the layout used.
	 */
	public void addBlankField() {
		attributeLayout.addBlankCell();
	}
}
