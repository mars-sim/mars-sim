/*
 * Mars Simulation Project
 * PercentageCellRenderer.java
 * @date 2023-03-01
 * @author Barry Evans
 */
package org.mars_sim.msp.ui.swing.utils;

import java.awt.Component;
import java.text.DecimalFormat;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.mars_sim.msp.ui.swing.StyleManager;

/**
 * Simple table cell renderer that styles the values as percentages according to the Stylemanager
 */
public class PercentageCellRenderer extends DefaultTableCellRenderer {

    /**
     * The default width for a cell using this renderer
     */
    public static int DEFAULT_WIDTH = 40;

    private DecimalFormat format;

    /**
     * Render a double as a percentage value.
     * @param showDecimal Show decimal value
     */
    public PercentageCellRenderer(boolean showDecimal) {
        setHorizontalAlignment( JLabel.RIGHT );

        if (showDecimal) {
            format = StyleManager.DECIMAL_PERC2;
        }
        else {
            format = StyleManager.DECIMAL_PERC;
        }
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {
        JLabel cell = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                column);
        
        cell.setText(format.format(value));

        return cell;
    }

}
