/*
 * Mars Simulation Project
 * MarsTimeCellRenderer.java
 * @date 2023-05-06
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.components;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

import com.mars_sim.core.time.MarsTime;

/**
 * Simple table cell renderer that styles the values as Mars Clock according to the Stylemanager
 */
@SuppressWarnings("serial")
public class MarsTimeTableCellRenderer extends DefaultTableCellRenderer {

    /**
     * Render a MarsTime value
     */
    public MarsTimeTableCellRenderer() {
        setHorizontalAlignment( SwingConstants.RIGHT );
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {
        JLabel cell = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                column);

        String contents = "";
        if (value instanceof MarsTime mc) {
            contents = mc.getTruncatedDateTimeStamp();
        }
        else if (value != null) {
            contents = value.toString();
        }
        cell.setText(contents);

        return cell;
    }

}
