/**
 * Mars Simulation Project
 * StructuredResponse.java
 * @version 3.1.2 2020-12-30
 * @author Barry Evans
 */

package org.mars.sim.console.chat.simcommand;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.google.common.primitives.Ints;

/**
 * A buffer holding structured output similar to a table.
 */
public class StructuredResponse {

	// Formats
	private static final String HEADING_FORMAT = " %s%n";
	private static final String ONE_COLUMN = "%30s : %s%n";
	private static final String ONE_DIGITCOLUMN = "%30s : %d%n";
	
	private static final String LIST = "  %2d - %s%n";
	
	private static final String LF = System.lineSeparator();
	
	private StringBuilder buffer = new StringBuilder();
	private int[] columnsWidth;
	
	/**
	 * Adda free text"
	 * @param string
	 */
	public void append(String string) {
		buffer.append(string);
	}

	/**
	 * Add a blank line to the structured output.
	 */
	public void appendBlankLine() {
		buffer.append(LF);
	}
	
	/**
	 * Add a subheading to the output. This will also add a seperator.
	 * @param heading
	 */
	public void appendHeading(String heading) {
		buffer.append(String.format(HEADING_FORMAT, heading));	
		appendSeperator();
	}

	/**
	 * Add a string value to the output with a label.
	 * @param label
	 * @param value
	 */
	public void appendLabeledString(String label, String value) {
		buffer.append(String.format(ONE_COLUMN, label, value));
	}
	
	/**
	 * Adda initeger value to the output with a label
	 * @param label
	 * @param value
	 */
	public void appendLabelledDigit(String label, int value) {
		buffer.append(String.format(ONE_DIGITCOLUMN, label, value));		
	}
	
	/**
	 * Output a numbered list of items
	 * @param heading An optional heading
	 * @param items
	 */
	public void appendNumberedList(String heading, List<String> items) {
		if (heading != null) {
			buffer.append(heading + ":" + LF);
		}
		
		if (!items.isEmpty()) {
			int i = 1;
			for (String string : items) {
				buffer.append(String.format(LIST, i++, string));
			}
		}
		else {
			buffer.append("None");
			buffer.append(LF);
		}
	}

	/**
	 * Seperator
	 */
	public void appendSeperator() {
		buffer.append(" --------------------------------------------");
		buffer.append(LF);	
	}

	/**
	 * Add a table heading and prepares for table. The number of heading strings defines how many
	 * columns the table contains. Table must have at least 2 columns including the 1st fixed one.
	 * The headings columns contains String to define the columns; if a String is followed by an integer
	 * then the int specifies the width of that column. 
	 * @param heading1 1st column heading
	 * @param width Width of 1st column
	 * @param headings A variab le list of heading defining the columns
	 */
	public void appendTableHeading(String heading1, int width, Object ... headings) {
		List<Integer> widths = new ArrayList<>();
		
		int firstWidth = Math.max(width, heading1.length()); 
		StringBuilder fmt  = new StringBuilder();
		fmt.append("%");
		fmt.append(firstWidth);
		fmt.append('s');
		buffer.append(String.format(fmt.toString(), heading1));
		widths.add(firstWidth);

		int tableWidth = firstWidth;
		for(int i = 0; i < headings.length; i++) {
			String column = (String) headings[i];
			int w = column.length();
			// If the next arg is an int then it's width
			if (((i + 1) < headings.length) && (headings[i+1] instanceof Integer)) {
				i++;
				int hWidth = (int) headings[i];
				
				// hWidth could be negative if left aligned but need absolute width
				if (w < Math.abs(hWidth)) {
					w = hWidth;
				}
			}
			
			// Add column
			fmt = new StringBuilder();
			fmt.append(" | %");
			fmt.append(w);
			fmt.append('s');
			buffer.append(String.format(fmt.toString(), column));
			widths.add(w);
			tableWidth += (Math.abs(w) + 3);
		}

		// Save widths
		columnsWidth = Ints.toArray(widths);
		buffer.append(System.lineSeparator());
		buffer.append(StringUtils.repeat('-', tableWidth));
		buffer.append(System.lineSeparator());

	}

	/**
	 * Add a table row with a list of values. The total numebr of values must equals the
	 * number of columns previously defined in {@link #appendTableHeading(String, int, Object...)}
	 * @param label Label for the entry
	 * @param values Other column values.
	 */
	public void appendTableRow(String label, Object ... values) {
		int valueCount = values.length + 1;
		if (columnsWidth == null) {
			throw new IllegalStateException("No table columns defined");
		}
		if (columnsWidth.length != valueCount) {
			throw new IllegalArgumentException("The number of values (" + valueCount
					+ ") does not match the defined columns " + columnsWidth.length);
		}
		
		StringBuilder headFmt = new StringBuilder("%").append(columnsWidth[0]).append("s");
		buffer.append(String.format(headFmt.toString(), label));
		
		for(int i = 0; i < values.length; i++) {
			StringBuilder fmt = new StringBuilder();
			fmt.append(" | %");
			int w = columnsWidth[i + 1];
			Object value = values[i];
			if (value instanceof String) {
				fmt.append(w);
				fmt.append('s');
			}
			else if (value instanceof Double) {
				fmt.append(w);
				fmt.append(".2f");				
			}
			else if (value instanceof Integer) {
				fmt.append(w);
				fmt.append('d');			
			}
			else if (value instanceof Boolean) {
				fmt.append(w);
				fmt.append('s');			
				value = ((Boolean)value).booleanValue() ? "Yes" : "No";
			}
			else if (value != null) {
				fmt.append(w);
				fmt.append('s');
				value = value.toString();
			}
			else {
				fmt.append(w);
				fmt.append('s');
				value = "";				
			}
			buffer.append(String.format(fmt.toString(), value));
		}
		buffer.append(LF);		
	}


	/**
	 * Write a line of text
	 * @param string
	 */
	public void appendText(String string) {
		buffer.append(string);
		buffer.append(LF);		
	}
	
	/**
	 * Get the text output of this response
	 * @return
	 */
	public String getOutput() {
		return buffer.toString();
	}
}
