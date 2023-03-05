/*
 * Mars Simulation Project
 * AttributePanel.java
 * @date 2023-03-03
 * @author Barry Evans
 */
package org.mars_sim.msp.ui.swing.utils;

import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.mars_sim.msp.ui.swing.StyleManager;

/**
 * A simple panel implementation that supports adding JComponents accompanied by a JLabel.
 * These are added in a row layout.
 */
public class AttributePanel extends JPanel {

    public AttributePanel(int rows) {
        this(rows, 1);
    }

    public AttributePanel(int rows, int cols) {
        super(new GridLayout(rows, 2*cols, 5, 3));
    }

	/**
	 * Adds a text field and label to a Panel. The layout should be Spring layout.
	 * 
	 * @param label The fixed label
	 * @param content Initial content of the text field
	 * @param tooltip Optional tooltip
	 * @return The JLabel that can be updated.
	 */
	public JLabel addTextField(String label, String content, String tooltip) {
		JPanel wrapper3 = new JPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
		JLabel typeTF = new JLabel(content);
		if (tooltip != null) {
			typeTF.setToolTipText(tooltip);
		}
		wrapper3.add(typeTF);
		addLabelledItem(label, typeTF);
		return typeTF;
	}
	
	/**
	 * Add a labelled content to the TabPanel. This ensures the styling is common.
	 * @param label Label to add
	 * @param content Content showign the value
	 */
	public void addLabelledItem(String label, JComponent content) {
        if (!label.endsWith(":")) {
            label = label + " :";
        }
        JLabel title = new JLabel(label, SwingConstants.RIGHT);
        title.setFont(StyleManager.getLabelFont());
		add(title);
		add(content);
	}
}
