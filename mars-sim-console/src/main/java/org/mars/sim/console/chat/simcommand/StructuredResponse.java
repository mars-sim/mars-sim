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
	
	/**
	 * Get the text output of this response
	 * @return
	 */
	public String getOutput() {
		return buffer.toString();
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
	 * Add a table row containg a digit value
	 * @param label
	 * @param value
	 */
	public void appendTableDigit(String label, int value) {
		buffer.append(String.format(tableDigitFormat, label, value));		
	}
	
	/**
	 * Add a table heading and prepares for table.
	 * @param heading1 1st column heading
	 * @param width Width of 1st column
	 * @param heading2 2nd column heading
	 */
	public void appendTableHeading(String heading1, int width, String heading2) {
		tableStringFormat = "%" + width + "s | %s%n";
		tableDigitFormat = "%" + width + "s | %d%n";
		
		appendTableString(heading1, heading2);
		appendSeperator();
	}

	/**
	 * Add a table row with a String value
	 * @param string1
	 * @param string2
	 */
	public void appendTableString(String string1, String string2) {
		buffer.append(String.format(tableStringFormat, string1, string2));		
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
	 * Seperator
	 */
	public void appendSeperator() {
		buffer.append(" --------------------------------------------\n");	
	}

	/**
	 * Adda free text"
	 * @param string
	 */
	public void append(String string) {
		buffer.append(string);
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
}
