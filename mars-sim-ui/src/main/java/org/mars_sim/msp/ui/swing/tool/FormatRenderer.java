/**
 * Mars Simulation Project
 * FormatRenderer.java
 * @version 3.1.2 2021-05-22
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.tool;

import java.text.Format;
import java.text.DateFormat;
import javax.swing.table.DefaultTableCellRenderer;

//Source : https://tips4java.wordpress.com/2008/10/11/table-format-renderers/

/*
 *	Use a formatter to format the cell Object
 */
public class FormatRenderer extends DefaultTableCellRenderer
{
	private Format formatter;

	/*
	 *   Use the specified formatter to format the Object
	 */
	public FormatRenderer(Format formatter)
	{
		this.formatter = formatter;
	}

	public void setValue(Object value)
	{
		//  Format the Object before setting its value in the renderer

		try
		{
			if (value != null)
				value = formatter.format(value);
		}
		catch(IllegalArgumentException e) {}

		super.setValue(value);
	}

	/*
	 *  Use the default date/time formatter for the default locale
	 */
	public static FormatRenderer getDateTimeRenderer()
	{
		return new FormatRenderer( DateFormat.getDateTimeInstance() );
	}

	/*
	 *  Use the default time formatter for the default locale
	 */
	public static FormatRenderer getTimeRenderer()
	{
		return new FormatRenderer( DateFormat.getTimeInstance() );
	}
}
