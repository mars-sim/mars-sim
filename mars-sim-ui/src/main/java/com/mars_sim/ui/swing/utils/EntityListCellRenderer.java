/*
 * Mars Simulation Project
 * EntityListCellRenderer.java
 * @date 2025-06-01
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.utils;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import com.mars_sim.core.Entity;

/**
 * This class renderers a Entity instance on a ListCell.
 * 
 * This can also be used in a ComboBox
 */
public class EntityListCellRenderer extends DefaultListCellRenderer {

	private String prompt;
	public EntityListCellRenderer() {
	}

    /**
     * The prompt will be displayed of the is no values selected.
     * @param prompt
     */
	public EntityListCellRenderer(String prompt) {
		this.prompt = prompt;
	}

	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
			boolean cellHasFocus) {
		Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		
		if ((value == null) && (prompt != null)) {
			setText(prompt);
		}
        else if (value instanceof Entity e) {
			setText(e.getName());
        }
		
        return c;
	}
}