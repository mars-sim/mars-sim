package org.mars.sim.console.chat.simcommand;

import java.util.List;

/**
 * A buffer holding structured output similar to a table.
 */
public class StructuredResponse {

	// Formats
	private static final String HEADING_FORMAT = " %s%n";
	private static final String ONE_COLUMN = "%27s : %s%n";
	private static final String ONE_DIGITCOLUMN = "%27s : %d%n";
	
	private static final String LIST = "  %2d - %s%n";
	
	private StringBuffer buffer = new StringBuffer();
	private String tableStringFormat = null;
	private String tableDigitFormat = null;
	private String tableDoubleFormat = null;
	
	/**
	 * Adda free text"
	 * @param string
	 */
	public void append(String string) {
		buffer.append(string);
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
	 * Output anumbers list of items
	 * @param names
	 */
	public void appendNumberedList(List<String> names) {
		int i = 1;
		for (String string : names) {
			buffer.append(String.format(LIST, i++, string));
		}
	}

	/**
	 * Seperator
	 */
	public void appendSeperator() {
		buffer.append(" --------------------------------------------\n");	
	}
	
	/**
	 * Add a table row containg a digit value
	 * @param label
	 * @param value
	 */
	public void appendTableDigit(String label, int value) {
		buffer.append(String.format(tableDigitFormat, label, value));		
	}
	
	/**
	 * Add a table row with a double value
	 * @param label Label for the entry
	 * @param value Value.
	 */
	public void appendTableDouble(String label, double value) {
		buffer.append(String.format(tableDoubleFormat , label, value));		
	}
	
	/**
	 * Add a table heading and prepares for table.
	 * @param heading1 1st column heading
	 * @param width Width of 1st column
	 * @param heading2 2nd column heading
	 */
	public void appendTableHeading(String heading1, int width, String heading2) {
		tableStringFormat = "%" + width + "s | %s%n";
		tableDigitFormat = "%" + width + "s | %7d%n";
		tableDoubleFormat = "%" + width + "s | %7.2f%n";

		appendTableString(heading1, heading2);
		appendSeperator();
	}

	/**
	 * Add a table row with a String value
	 * @param label Label for the entry
	 * @param value Value.
	 */
	public void appendTableString(String label, String value) {
		buffer.append(String.format(tableStringFormat, label, value));		
	}

	/**
	 * Get the text output of this response
	 * @return
	 */
	public String getOutput() {
		return buffer.toString();
	}


}
