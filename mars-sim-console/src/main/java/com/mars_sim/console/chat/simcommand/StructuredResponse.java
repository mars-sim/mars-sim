/*
 * Mars Simulation Project
 * StructuredResponse.java
 * @date 2024-08-10
 * @author Barry Evans
 */

package com.mars_sim.console.chat.simcommand;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.google.common.primitives.Ints;

/**
 * A buffer holding structured output similar to a table.
 */
public class StructuredResponse {

	private static record SplitString(String left, String right) {}

	private static final Pattern SPLIT_CHARS = Pattern.compile("[ ,-]");


	// Formats
	private static final String HEADING_FORMAT = " %s%n";
	private static final String ONE_COLUMN = "%30s : %s%n";
	private static final String ONE_DIGITCOLUMN = "%30s : %d%n";
	
	private static final String LIST = "  %2d - %s%n";
	
	private static final String LF = System.lineSeparator();
	
	private StringBuilder buffer = new StringBuilder();
	private int[] columnsWidth;
	private boolean wrapTableCells;
	
	/**
	 * Adds free text.
	 * 
	 * @param string
	 */
	public void append(String string) {
		buffer.append(string);
	}

	/**
	 * Adds a blank line to the structured output.
	 */
	public void appendBlankLine() {
		buffer.append(LF);
	}
	
	/**
	 * Adds a subheading to the output. This will also add a separator.
	 * 
	 * @param heading
	 */
	public void appendHeading(String heading) {
		buffer.append(String.format(HEADING_FORMAT, heading));	
		appendSeperator();
	}

	/**
	 * Adds a string value to the output with a label one column away from the left margin.
	 * 
	 * @param label
	 * @param value
	 */
	public void appendLabeledString(String label, String value) {
		buffer.append(String.format(ONE_COLUMN, label, value));
	}
	
	/**
	 * Adds integer value to the output with a label.
	 * 
	 * @param label
	 * @param value
	 */
	public void appendLabelledDigit(String label, int value) {
		buffer.append(String.format(ONE_DIGITCOLUMN, label, value));		
	}
	
	/**
	 * Outputs a numbered list of items.
	 * 
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
	 * Appends a separator.
	 */
	public void appendSeperator() {
		buffer.append(" --------------------------------------------");
		buffer.append(LF);	
	}

	/**
	 * Adds a table heading and prepares for table. The number of heading strings defines how many
	 * columns the table contains. Table must have at least 2 columns including the 1st fixed one.
	 * The headings columns contains String to define the columns; if a String is followed by an integer
	 * then the int specifies the width of that column. 
	 * This has no cell wrapping.
	 * 
	 * @param heading1 1st column heading
	 * @param width Width of 1st column
	 * @param headings A variab le list of heading defining the columns
	 */
	public void appendTableHeading(String heading1, int width, Object ... headings) {
		appendTableHeading(false, heading1, width, headings);
	}

	/**
	 * Adds a table heading and prepares for table. The number of heading strings defines how many
	 * columns the table contains. Table must have at least 2 columns including the 1st fixed one.
	 * The headings columns contains String to define the columns; 
	 * if a String is followed by an integer, then the int specifies the width of that column.
	 *  
	 * @param wrapContents Wrap the contents of the cells to fit in the column width
	 * @param heading1 1st column heading
	 * @param width Width of 1st column
	 * @param headings A variable list of heading defining the columns
	 */
	public void appendTableHeading(boolean wrapContents, String heading1, int width, Object ... headings) {
		List<Integer> widths = new ArrayList<>();
		wrapTableCells = wrapContents;
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
			if (((i + 1) < headings.length) && (headings[i+1] instanceof Integer hWidth)) {
				i++;
				
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
		buffer.append(' ' + StringUtils.repeat('-', tableWidth));
		buffer.append(System.lineSeparator());

	}

	/**
	 * Adds a table row with a list of values. The total number of values must equals the
	 * number of columns previously defined in {@link #appendTableHeading(String, int, Object...)}
	 * 
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
		
		outputTableRow(buffer, label, values);
	}

	private void outputTableRow(StringBuilder tableBody, String label, Object [] values) {
		String nextLabel = "";
		Object [] nextValues = new Object[values.length];
		boolean nextRow = false;

		int labelWidth = columnsWidth[0];
		StringBuilder headFmt = new StringBuilder("%").append(labelWidth).append("s");
		var labSplit = splitText(label, labelWidth);
		if (labSplit != null) {
			nextRow = true;
			nextLabel = labSplit.right();
			label = labSplit.left();
		}
		tableBody.append(String.format(headFmt.toString(), label));
		
		for(int i = 0; i < values.length; i++) {
			StringBuilder fmt = new StringBuilder();
			fmt.append(" | %");
			int w = columnsWidth[i + 1];
			Object value = values[i];
			if (value instanceof String str) {
				fmt.append(w);
				fmt.append('s');

				// Can wrap String cells
				var split = splitText(str, w);
				if (split != null) {
					nextRow = true;
					nextValues[i] = split.right();
					value = split.left();
				}
			}
			else if (value instanceof Double) {
				fmt.append(w);
				fmt.append(".2f");				
			}
			else if (value instanceof Integer) {
				fmt.append(w);
				fmt.append('d');			
			}
			else if (value instanceof Boolean b) {
				fmt.append(w);
				fmt.append('s');			
				value = b.booleanValue() ? "Yes" : "No";
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
			tableBody.append(String.format(fmt.toString(), value));
		}
		tableBody.append(LF);	
		
		// Output another row?
		if (nextRow) {
			outputTableRow(tableBody, nextLabel, nextValues);
		}
	}

	/**
	 * Splits a string into 2 parts where the first is no bigger than the width.
	 * If the source string is less than the width then a null is returned as no
	 * split is needed.
	 * 
	 * @param source String to be split
	 * @param width Maximum size of lefthand string
	 * @return
	 */
	private SplitString splitText(String source, int width) {
		SplitString result = null;
		int w = Math.abs(width);

		if (wrapTableCells && source.length() > w) {
			int splitIdx = w;

			var match = SPLIT_CHARS.matcher(source);
			while(match.find()) {
				if (match.end() < w) {
					splitIdx = match.end();
				}
				else {
					// Gone too far
					break;
				}
			}
			result = new SplitString(source.substring(0, splitIdx),
									 source.substring(splitIdx).trim());
		}
		return result;
	}

	/**
	 * Writes a line of text.
	 * 
	 * @param string
	 */
	public void appendText(String string) {
		buffer.append(string);
		buffer.append(LF);		
	}
	
	/**
	 * Gets the text output of this response.
	 * 
	 * @return
	 */
	public String getOutput() {
		return buffer.toString();
	}
}
