/*
 * Mars Simulation Project
 * PercentageCellRenderer.java
 * @date 2023-03-01
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.components;

import java.awt.Component;
import java.text.DecimalFormat;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

import com.mars_sim.ui.swing.StyleManager;

/**
 * Simple table cell renderer that styles the values as percentages according to the Stylemanager
 */
@SuppressWarnings("serial")
public class PercentageTableCellRenderer extends DefaultTableCellRenderer {

    /**
     * The default width for a cell using this renderer
     */
    public static final int DEFAULT_WIDTH = 50;

    private DecimalFormat format;

    /**
     * Render a double as a percentage value.
     * @param showDecimal Show decimal value
     */
    public PercentageTableCellRenderer(boolean showDecimal) {
        setHorizontalAlignment( SwingConstants.RIGHT );

        if (showDecimal) {
            format = StyleManager.DECIMAL2_PERC;
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
