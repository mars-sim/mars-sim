/*
 * Mars Simulation Project
 * MarsTimeCellRenderer.java
 * @date 2023-05-06
 * @author Barry Evans
 */
package org.mars_sim.msp.ui.swing.utils;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.MarsClockFormat;

/**
 * Simple table cell renderer that styles the values as Mars Clock according to the Stylemanager
 */
public class MarsTimeCellRenderer extends DefaultTableCellRenderer {

    /**
     * Render a MarsClock value
     */
    public MarsTimeCellRenderer() {
        setHorizontalAlignment( JLabel.RIGHT );
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {
        JLabel cell = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                column);

        String contents = "";
        if (value instanceof MarsClock mc) {
            contents = MarsClockFormat.getTruncatedDateTimeStamp(mc);
        }
        else if (value != null) {
            contents = value.toString();
        }
        cell.setText(contents);

        return cell;
    }

}
