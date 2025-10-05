/*
 * Mars Simulation Project
 * UserConfigurableListRenderer.java
 * @date 2025-09-23
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.components;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import com.mars_sim.core.configuration.UserConfigurable;

/**
 * A generic List renderer that will show UserConfigurables
 */
@SuppressWarnings("serial")
public class UserConfigurableListRenderer extends JLabel implements
        ListCellRenderer<UserConfigurable> {

    public UserConfigurableListRenderer() {

        setOpaque(true);
        setVerticalAlignment(CENTER);
    }

    @Override
    public Component getListCellRendererComponent(
            JList<? extends UserConfigurable> list,
            UserConfigurable value, int index, boolean isSelected,
            boolean cellHasFocus) {

        this.setFont(list.getFont());

        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }
        if (value != null) {
            this.setText(value.getName());
        }
        return this;
    }
}
