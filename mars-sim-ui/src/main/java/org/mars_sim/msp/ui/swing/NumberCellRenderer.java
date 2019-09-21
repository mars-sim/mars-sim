/**
 * Mars Simulation Project
 * NumberCellRenderer.java
 * @version 3.1.0 2019-09-20
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * The NumberCellRenderer class renders table cells containing Number objects.
 */
public class NumberCellRenderer
extends DefaultTableCellRenderer {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private DecimalFormat dec0Formatter = new DecimalFormat("#,###,###");
	private DecimalFormat dec1Formatter = new DecimalFormat("#,###,##0.0");
	private DecimalFormat dec2Formatter = new DecimalFormat("#,###,##0.00");
	private DecimalFormat dec3Formatter = new DecimalFormat("#,###,##0.000");
	private DecimalFormat dec4Formatter = new DecimalFormat("#,###,##0.0000");
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
	public NumberCellRenderer(int digits) {
		// Use DefaultTableCellRenderer constructor
		super();
		if (digits == 0) formatter = dec0Formatter;
		else if (digits == 1) formatter = dec1Formatter;
		else if (digits == 2) formatter = dec2Formatter;
		else if (digits == 3) formatter = dec3Formatter;
		else if (digits == 4) formatter = dec4Formatter;
		else formatter = dec1Formatter;

		// Set the horizontal alignment to right.
		setHorizontalAlignment(SwingConstants.RIGHT);
	}

	/**
	 * Sets the String object for the cell being rendered to value.
	 * @param value the string value for this cell; if value is 
	 * null it sets the text value to an empty string.
	 */
	@Override
	public void setValue(Object value) {
		if (value != null) {
			if (value instanceof Double) 
				value = formatter.format(((Double) value).doubleValue());
			else if (value instanceof Float)
				value = formatter.format(((Float) value).floatValue());
			else if (value instanceof BigDecimal)
				value = formatter.format(((BigDecimal) value).doubleValue());
		} 
		super.setValue(value);
	} 
}