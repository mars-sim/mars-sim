/***********************************************************************
*This program/file is free software: you can redistribute it and/or modify
*it under the terms of the GNU General Public License as published by
*the Free Software Foundation, either version 3 of the License, or
*(at your option) any later version.
*This program/file is distributed in the hope that it will be useful,
*but WITHOUT ANY WARRANTY; without even the implied warranty of
*MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*GNU General Public License for more details.
*You should have received a copy of the GNU General Public License
*along with this program/file.  If not, see <http://www.gnu.org/licenses/>.
* 
***********************************************************************/
package org.mars_sim.msp;

import java.text.DateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * @author Sebastien Venot
 * @version 1.0
 * @date 8.2.2008
 */
public class SimuLoggingFormatter extends Formatter {
    
    public final static String LINEFEED =
	System.getProperty("line.separator");
    
    private DateFormat df = DateFormat.getDateTimeInstance();
    private Date date = new Date();
    private StringBuffer sb = new StringBuffer();
    

    public String format(LogRecord record)
	{
		sb.delete(0,sb.length());
		date.setTime(record.getMillis());
			
		sb.append(df.format(date));
		sb.append(" ");
			
		// Get the level name and add it to the buffer
		sb.append(record.getLevel().getName());
		sb.append(" ");
			
		sb.append(record.getLoggerName());
		sb.append(" ");
			 
		// Get the formatted message (includes localization 
		// and substitution of paramters) and add it to the buffer
		sb.append(formatMessage(record));
		sb.append(LINEFEED);

		return sb.toString();
		
	}
}
