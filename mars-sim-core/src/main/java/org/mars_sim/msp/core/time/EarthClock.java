/**
 * Mars Simulation Project
 * EarthClock.java
 * @version 3.1.0 2017-01-14
 * @author Scott Davis
 */

package org.mars_sim.msp.core.time;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * The EarthClock class keeps track of Earth Universal Time. It should be
 * synchronized with the running MarsClock.
 */
public class EarthClock implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** Initialized logger. */
	private static Logger logger = Logger.getLogger(EarthClock.class.getName());
	private static String loggerName = logger.getName();
	private static String sourceName = loggerName.substring(loggerName.lastIndexOf(".") + 1, loggerName.length());
	
	/**
	 * Tracks the number of milliseconds since 1 January 1970 00:00:00 at the start of the sim
	 */
	private static long millisAtStart;

	private SimpleDateFormat f0;
	private SimpleDateFormat f1;
	private SimpleDateFormat f2;
	private SimpleDateFormat f3;

	private SimpleTimeZone zone;

	private ZonedDateTime zonedDateTime;

	private static ZonedDateTime dateOfFirstLanding;

	/**
	 * Constructor.
	 * 
	 * @param fullDateTimeString the UT date string
	 * @throws Exception if date string is invalid.
	 */
	public EarthClock(String fullDateTimeString) {
		// Java 8's Date/Time API in java.time package, see
		// https://docs.oracle.com/javase/tutorial/datetime/TOC.html
		
		// dtFormatter_millis = DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss");//.AAAA");//AAAA");

		// see http://stackoverflow.com/questions/26142864/how-to-get-utc0-date-in-java-8

		// Use ZonedDate
		zonedDateTime = ZonedDateTime.now(ZoneOffset.UTC);
		
		// Set Greenwich Mean Time (GMT) timezone for calendar
		zone = new SimpleTimeZone(0, "GMT");
		// see http://www.diffen.com/difference/GMT_vs_UTC

		// Set Earth clock to Martian Zero-orbit date-time.
		// This date may need to be adjusted if it is inaccurate.

		// Set it to Locale.US
		f0 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss '(UT)'", Locale.US);
		f0.setTimeZone(zone);

		// Set it to Locale.US
		f1 = new SimpleDateFormat("yyyy-MM-dd HH:mm '(UT)'", Locale.US);
		f1.setTimeZone(zone);

		// Note: By default, java set locale to user's machine system locale via
		// Locale.getDefault(Locale.Category.FORMAT));
		// i.e. f2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
		// Locale.getDefault(Locale.Category.FORMAT));
		// 2017-03-27 set it to Locale.US

		f2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
		f2.setTimeZone(zone);

		String iso8601 = null;

		// The default value for fullDateTimeString from simulations.xml is "2028-08-17 15:23:13.740"
		iso8601 = fullDateTimeString.replace(" ", "T") + "Z";

		dateOfFirstLanding = ZonedDateTime.parse(iso8601);
		// LocalDateTime ldt = zdt.toLocalDateTime();

		try {
			zonedDateTime = dateOfFirstLanding;
			computeMillisAtStart();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		// Initialize a second formatter
		// Set it to Locale.US
		f3 = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);// .SSS"); // :SSS
		TimeZone gmt = TimeZone.getTimeZone("GMT");
		f3.setTimeZone(gmt);
		f3.setLenient(false);

	}

	public static String getMonthForInt(int m) {
		String month = "invalid";
		DateFormatSymbols dfs = new DateFormatSymbols();
		String[] months = dfs.getMonths();
		if (m >= 0 && m <= 11) {
			month = months[m];
		}
		return month;
	}

	public static int getCurrentYear(EarthClock clock) {
		return clock.getYear();
	}

	public Instant getInstant() {
		return zonedDateTime.toInstant();
	}
	
	/**
	 * Returns the current date/time
	 * 
	 * @return date
	 */
	public Date getCurrentDateTime() {
		return Date.from(getInstant());
	}

	public static ZonedDateTime getDateOfFirstLanding() {
		return dateOfFirstLanding;
	}

	/**
	 * Returns the number of milliseconds since 1 January 1970 00:00:00
	 * 
	 * @return long
	 */
	public static long getMillisAtStart() {
		return millisAtStart;
	}

	/**
	 * Returns the number of milliseconds since 1 January 1970 00:00:00
	 * 
	 * @return long
	 */
	public void computeMillisAtStart() {
		millisAtStart = getInstant().toEpochMilli();
	}

	/**
	 * Returns the number of milliseconds since 1 January 1970 00:00:00
	 * 
	 * @return long
	 */
	public static long getMillis(EarthClock clock) {
		return clock.getZonedDateTime().toInstant().toEpochMilli();
	}

	/**
	 * Returns the date/time formatted in a string
	 * 
	 * @return date/time formatted in a string. eg "2055-May-06 03:37:22"
	 */
	public String getCurrentDateTimeString(EarthClock clock) {
		return f3.format(Timestamp.from(clock.getZonedDateTime().toInstant()));
	}

	/**
	 * Returns the date/time formatted in a string
	 * 
	 * @return date/time formatted in a string. ex "2055-05-06 03:37:22 (UT)"
	 */
	public String getTimeStampF0() {		
		return f0.format(Timestamp.from(zonedDateTime.toInstant()));
	}

	/**
	 * Returns the date/time formatted in a string
	 * 
	 * @return date/time formatted in a string. ex "2055-05-06 03:37 (UT)"
	 */
	public String getTimeStampF1() {
		return f1.format(Timestamp.from(zonedDateTime.toInstant()));
	}

	/**
	 * Returns the date/time formatted in a string
	 * 
	 * @return date/time formatted in a string. ex "2055-May-06 03:37:22"
	 */
	public String getTimeStampF3() {
		return f3.format(Timestamp.from(zonedDateTime.toInstant()));
	}

	/**
	 * Returns the date formatted in a string
	 * 
	 * @return date formatted in a string. ex "2055-05-06"
	 */
	public String getDateStringF0() {
		String d = getTimeStampF0();
		return d.substring(0, d.indexOf(" "));
	}

	/**
	 * Returns the date formatted in a string
	 * 
	 * @return date formatted in a string. ex "2055-May-06"
	 */
	public String getDateStringF3() {
		String d = getTimeStampF3();
		return d.substring(0, d.indexOf(" "));
	}

	/**
	 * Returns the time formatted in a string
	 * 
	 * @return date formatted in a string. ex "03:37:22"
	 */
	public String getTimeStringF0() {
		String d = getTimeStampF0();
		return d.substring(d.indexOf(" ") + 1, d.length());
	}

	/**
	 * Adds time to the calendar
	 * 
	 * @param milliseconds the time to be added to the calendar
	 */
	public void addTime(long milliseconds) {
		zonedDateTime = zonedDateTime.plusNanos(milliseconds*1_000_000);
//		logger.config("zonedDateTime : " + zonedDateTime);
	}

	/**
	 * Displays the string version of the clock.
	 * 
	 * @return time stamp string.
	 */
	public String toString() {
		return getTimeStampF0();
	}

	public int getDayOfMonth() {
		return zonedDateTime.getDayOfMonth();
	}

	public int getMonth() {
		return zonedDateTime.getMonthValue();
	}

	// Used by scheduleSecondTask() in EarthMinimalClock only
	public String getMonthString() {
		int w = getMonth();
		StringBuilder s = new StringBuilder();
		if (w == Calendar.JANUARY)
			s.append("Jan");
		else if (w == Calendar.FEBRUARY)
			s.append("Feb");
		else if (w == Calendar.MARCH)
			s.append("Mar");
		else if (w == Calendar.APRIL)
			s.append("Apr");
		else if (w == Calendar.MAY)
			s.append("May");
		else if (w == Calendar.JUNE)
			s.append("Jun");
		else if (w == Calendar.JULY)
			s.append("Jul");
		else if (w == Calendar.AUGUST)
			s.append("Aug");
		else if (w == Calendar.SEPTEMBER)
			s.append("Sep");
		else if (w == Calendar.OCTOBER)
			s.append("Oct");
		else if (w == Calendar.NOVEMBER)
			s.append("Nov");
		else if (w == Calendar.DECEMBER)
			s.append("Dec");
		return s.toString();
	}

	public int getYear() {
		return zonedDateTime.getYear();
	}

	public int getSecond() {
		return zonedDateTime.getSecond();
	}

	public String getSecondString() {
		StringBuilder s = new StringBuilder();
		int ss = getSecond();
		if (ss > 10)
			s.append(ss);
		else
			s.append("0").append(ss);
		return s.toString();
	}

	public int getMinute() {
		return zonedDateTime.getMinute();
	}

	public String getMinuteString() {
		StringBuilder s = new StringBuilder();
		int m = getMinute();
		if (m > 10)
			s.append(m);
		else
			s.append("0").append(m);
		return s.toString();
	}

	public int getHour() {
		return zonedDateTime.getHour();
	}

	public String getHourString() {
		StringBuilder s = new StringBuilder();
		int h = getHour();
		if (h > 10)
			s.append(h);
		else
			s.append("0").append(h);
		return s.toString();
	}

	public String getDayOfWeekString() {
		return zonedDateTime.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);//gregCal.get(Calendar.DAY_OF_WEEK);
		
//		StringBuilder s = new StringBuilder();
//		if (w == Calendar.SUNDAY)
//			s.append("Sunday");
//		else if (w == Calendar.MONDAY)
//			s.append("Monday");
//		else if (w == Calendar.TUESDAY)
//			s.append("Tuesday");
//		else if (w == Calendar.WEDNESDAY)
//			s.append("Wednesday");
//		else if (w == Calendar.THURSDAY)
//			s.append("Thursday");
//		else if (w == Calendar.FRIDAY)
//			s.append("Friday");
//		else if (w == Calendar.SATURDAY)
//			s.append("Saturaday");
//		// else
//		// s = "";
//		return s.toString();
	}

	public ZonedDateTime getZonedDateTime() {
		return zonedDateTime;
	}

	public LocalDate getLocalDate() {
		return zonedDateTime.toLocalDate();
	}

	public LocalTime getLocalTime() {
		return zonedDateTime.toLocalTime();
	}

//	public Date getDT() {
//		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//		LocalDate localDate = zonedDateTime //LocalDate.parse(dateString, formatter);
//		Date.from(java.time.ZonedDateTime.now().toInstant());
//	}
    
	public void destroy() {
		f0 = null;
		f2 = null;
		f1 = null;
		f3 = null;
		zone = null;
		zonedDateTime = null;
		dateOfFirstLanding = null;
	}
}