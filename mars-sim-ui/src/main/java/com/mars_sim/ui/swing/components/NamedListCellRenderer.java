/*
 * Mars Simulation Project
 * NamedListCellRenderer.java
 * @date 2025-06-14
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.components;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import com.mars_sim.core.Named;

/**
 * This class renderers a Names instance on a ListCell.
 * 
 * This can also be used in a ComboBox
 */
public class NamedListCellRenderer extends DefaultListCellRenderer {

	private String prompt;
	public NamedListCellRenderer() {
	}

    /**
     * The prompt will be displayed of the is no values selected.
     * @param prompt
     */
	public NamedListCellRenderer(String prompt) {
		this.prompt = prompt;
	}

	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
			boolean cellHasFocus) {
		Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		
		if ((value == null) && (prompt != null)) {
			setText(prompt);
		}
        else if (value instanceof Named e) {
			setText(e.getName());
        }
		
        return c;
	}
}