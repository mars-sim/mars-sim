/**
 * Mars Simulation Project
 * NumberCellRenderer.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing;

import java.awt.Component;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * The NumberCellRenderer class renders table cells containing Number objects.
 */
public class NumberCellRenderer extends DefaultTableCellRenderer {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static final String BASE = "#,###,##0";
	private static final Map<Integer,DecimalFormat> FORMATS = new HashMap<>();
	
	private DecimalFormat formatter;
	
	/**
	 * Constructor.
	 */
	public NumberCellRenderer() {
		this(1);
	}

	/**
	 * Constructor.
	 */
	public NumberCellRenderer(int decimal, boolean showDecimalPlace) {
		this(decimal);
	}

	/**
	 * Constructor.
	 */
	public NumberCellRenderer(int digits) {
		// Use DefaultTableCellRenderer constructor
		super();
		formatter = FORMATS.get(digits);
		if (formatter == null) {
			// Build the format strng by adding zero's to the BASE
			StringBuilder format = new StringBuilder(BASE);
			if (digits > 0) {
				format.append('.');
				for(int i = 0; i < digits; i++) {
					format.append('0');
				}
			}
			formatter = new DecimalFormat(format.toString());
			FORMATS.put(digits, formatter);
		}

		// Set the horizontal alignment to right.
		setHorizontalAlignment(SwingConstants.RIGHT);
	}


    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {
        JLabel cell = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                column);

        String text = "";
		if (value != null) {
			text = formatter.format(value);
		}
		cell.setText(text);
        return cell;
    }
}
