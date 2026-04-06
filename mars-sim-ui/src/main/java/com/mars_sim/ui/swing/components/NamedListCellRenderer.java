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
@SuppressWarnings("serial")
public class NamedListCellRenderer extends DefaultListCellRenderer {

	private String prompt;
	private int horizontalAlignment = CENTER;

	/**
	 * Default settings
	 */
	public NamedListCellRenderer() {
		this(null, CENTER);
	}

    /**
     * The prompt will be displayed of the is no values selected.
     * @param prompt Text to display when no object to renderer
     */
	public NamedListCellRenderer(String prompt) {
		this(prompt, CENTER);
	}

	/**
	 * Renderer with specified horizontal alignment
	 * @param horizontalAlignment The horizontal alignment of text	
	 */
	public NamedListCellRenderer(int horizontalAlignment) {
		this(null, horizontalAlignment);
	}

	/**
	 * Fully defined constructor
	 * @param prompt Text to display when no object to renderer
	 * @param horizontalAlignment The horizontal alignment of text
	 */
	public NamedListCellRenderer(String prompt, int horizontalAlignment) {
		this.prompt = prompt;
		this.horizontalAlignment = horizontalAlignment;
	}

	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
			boolean cellHasFocus) {
		Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		// Center horizontally
		setHorizontalAlignment(horizontalAlignment); 
		
		if ((value == null) && (prompt != null)) {
			setText(prompt);
		}
        else if (value instanceof Named e) {
			setText(e.getName());
        }
		
        return c;
	}
}