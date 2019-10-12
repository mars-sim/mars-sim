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
	
	/** Flag if always showing decimal places. */
	private boolean showDecimalPlace = false;
	
	/** The number of decimal places. */
	private int decimal = 0;
	
	/** The maximum number of decimal places. */
//	private int max = 0;
	
	private DecimalFormat dec0Formatter = new DecimalFormat("#,###,###");
	private DecimalFormat dec1Formatter = new DecimalFormat("#,###,##0.0");
	private DecimalFormat dec2Formatter = new DecimalFormat("#,###,##0.00");
	private DecimalFormat dec3Formatter = new DecimalFormat("#,###,##0.000");
	private DecimalFormat dec4Formatter = new DecimalFormat("#,###,##0.0000");
	private DecimalFormat dec5Formatter = new DecimalFormat("#,###,##0.00000");
	private DecimalFormat dec6Formatter = new DecimalFormat("#,###,##0.000000");
	private DecimalFormat dec7Formatter = new DecimalFormat("#,###,##0.0000000");
	private DecimalFormat dec8Formatter = new DecimalFormat("#,###,##0.00000000");
	
	private DecimalFormat formatter;
	private DecimalFormat formatter1;
	
	private DecimalFormat[] formatters = new DecimalFormat[] {  dec0Formatter, 
																dec1Formatter,
																dec2Formatter, 
																dec3Formatter,
																dec4Formatter,
																dec5Formatter,
																dec6Formatter,
																dec7Formatter,
																dec8Formatter}; 
	
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
		else if (digits == 5) formatter = dec5Formatter;
		else if (digits == 6) formatter = dec6Formatter;
		else if (digits == 7) formatter = dec7Formatter;
		else if (digits == 8) formatter = dec8Formatter;
		else formatter = dec1Formatter;

		// Set the horizontal alignment to right.
		setHorizontalAlignment(SwingConstants.RIGHT);
	}

	/**
	 * Constructor.
	 */
	public NumberCellRenderer(int decimal, boolean showDecimalPlace) {
		// Use DefaultTableCellRenderer constructor
		super();
		
		this.decimal = decimal;
		this.showDecimalPlace = showDecimalPlace;
		
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
			
			if (showDecimalPlace) {
				if (value instanceof Double) {
					double v = ((Double) value).doubleValue();
							
					int max = 0;
					
					if (v < 0.000_000_01) max = decimal + 8;
					else if (v < 0.000_000_1) max = decimal + 7;
					else if (v < 0.000_001) max = decimal + 6;
					else if (v < 0.000_01) max = decimal + 5;
					else if (v < 0.000_1) max = decimal + 4;	
					else if (v < 0.001) max = decimal + 3;
					else if (v < 0.01) max = decimal + 2;
					else if (v < 0.1) max = decimal + 1;
					else if (v < 1) max = decimal;	
					else max = decimal;	
					
					if (max > 0 && max <= 8)
						formatter1 = formatters[max];
					else 
						formatter1 = dec0Formatter;
					
					value = formatter1.format(v);
				}
				else if (value instanceof Float) {
					float v = ((Float) value).floatValue();
					int max = 0;
					
					if (v < 0.000_000_01) max = decimal + 8;
					else if (v < 0.000_000_1) max = decimal + 7;
					else if (v < 0.000_001) max = decimal + 6;
					else if (v < 0.000_01) max = decimal + 5;
					else if (v < 0.000_1) max = decimal + 4;	
					else if (v < 0.001) max = decimal + 3;
					else if (v < 0.01) max = decimal + 2;
					else if (v < 0.1) max = decimal + 1;
					else if (v < 1) max = decimal;	
					else max = decimal;	
					
					if (max > 0 && max <= 8)
						formatter1 = formatters[max];
					else 
						formatter1 = dec0Formatter;
					
					value = formatter1.format(v);
				}
				else if (value instanceof BigDecimal) {
					double v = ((BigDecimal) value).doubleValue();
					
					int max = 0;
					
					if (v < 0.000_000_01) max = decimal + 8;
					else if (v < 0.000_000_1) max = decimal + 7;
					else if (v < 0.000_001) max = decimal + 6;
					else if (v < 0.000_01) max = decimal + 5;
					else if (v < 0.000_1) max = decimal + 4;	
					else if (v < 0.001) max = decimal + 3;
					else if (v < 0.01) max = decimal + 2;
					else if (v < 0.1) max = decimal + 1;
					else if (v < 1) max = decimal;	
					else max = decimal;	
					
					if (max > 0 && max <= 8)
						formatter1 = formatters[max];
					else 
						formatter1 = dec0Formatter;
					
					value = formatter1.format(v);
				}
			}
			else {
				if (value instanceof Double) {
					value = formatter.format(((Double) value).doubleValue());
				}
				else if (value instanceof Float) {
					value = formatter.format(((Float) value).floatValue());
				}
				else if (value instanceof BigDecimal) {
					value = formatter.format(((BigDecimal) value).doubleValue());
				}
			}
			
		} 
		super.setValue(value);
	} 
}