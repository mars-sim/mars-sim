/**
 * Mars Simulation Project
 * SystemDateTime.java
 * @version 3.1.0 2017-03-24
 * @author Manny Kung
 */

package org.mars_sim.msp.core.time;

import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;

/*
 * The SystemDateTime class provides date and time formatting for constructing autosave filename
 */
public class SystemDateTime {

       protected SimpleDateFormat dateFormat =
               new SimpleDateFormat("MM-dd-yyyy");

       protected SimpleDateFormat timeFormat =
               new SimpleDateFormat("hhmmssaa");

//       private String dateStr;
//       private String timeStr;
//       private static String dateTimeStr;

       public SystemDateTime() {
       }

       /*
        * Constructs the portion of the autosave filename string based on local date and time
        */
       public String getDateTimeStr() {

//    	   Calendar currentCalendar = Calendar.getInstance();
//    	   Date currentTime = currentCalendar.getTime();
//    	   dateStr = dateFormat.format(currentTime);
//    	   timeStr = timeFormat.format(currentTime);
//    	   dateTimeStr = dateStr + "_" + timeStr;

    	   // Use ISO-8601-like calendar system.
    	   //e.g. 2007-12-03T10:15:30+01:00 Europe/Paris.
    	   //e.g. 2014-04-01T01:48:41.750-04:00[America/Montreal]

    	   ZonedDateTime cal = ZonedDateTime.now();
    	   String s = cal.toString();
    	   String dateTimeStr = (s.substring(0, s.indexOf("."))).replace(":", ".");

    	   //System.out.println("dateTimeStr : " + dateTimeStr);
    	   return dateTimeStr;
       }
   }
