/**
 * Mars Simulation Project
 * EarthClock.java
 * @version 3.1.0 2017-01-14
 * @author Scott Davis
 */

package org.mars_sim.msp.core.time;

import java.io.Serializable;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

/**
 * The EarthClock class keeps track of Earth Universal Time. It should be
 * synchronized with the Mars clock. TODO format date strings in an
 * internationalized fashion depending on user locale.
 */
public class EarthClock
//extends GregorianCalendar
		implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/*
	 * number of milliseconds since 1 January 1970 00:00:00 at the start of the sim
	 * (2043 Sep 30 00:00:00 UTC0)
	 */
	private static long millisAtStart;

	private GregorianCalendar gregCal;

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
		// To fully utilize Java 8's Date/Time API in java.time package, see
		// https://docs.oracle.com/javase/tutorial/datetime/TOC.html
		// dtFormatter_millis = DateTimeFormatter.ofPattern("yyyy-MMM-dd
		// HH:mm:ss");//.AAAA");//AAAA");

		// use ZonedDateTime utc = ZonedDateTime.now(ZoneOffset.UTC);
		// see
		// http://stackoverflow.com/questions/26142864/how-to-get-utc0-date-in-java-8

		// Use ZonedDate
		zonedDateTime = ZonedDateTime.now(ZoneOffset.UTC);

		// Convert to GregorianCalendar
		gregCal = GregorianCalendar.from(zonedDateTime);

		// Set GMT timezone for calendar
		zone = new SimpleTimeZone(0, "GMT");

		// see http://www.diffen.com/difference/GMT_vs_UTC

		gregCal.setTimeZone(zone);
		gregCal.clear();

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

		String iso8601 = null;// "2043-09-30T00:00:00.000Z"; //"2043-09-30T00:00:00";

		iso8601 = fullDateTimeString.replace(" ", "T") + "Z";

		dateOfFirstLanding = ZonedDateTime.parse(iso8601);
		// LocalDateTime ldt = zdt.toLocalDateTime();

		// DateTimeFormatter formatter =
		// DateTimeFormatter.ofPattern("yyyy-MM-ddTHH:m:ss.fff+zz:zz[Etc/UTC]");
		// //2043-09-30T00:0:00.000+00:00[Etc/UTC]
		// dateOfFirstLanding = ZonedDateTime.parse(fullDateTimeString);//, formatter);

//		LocalDateTime dateOfFirstLanding = LocalDateTime.of(2043, Month.SEPTEMBER, 30, 0, 0);
//		String formattedDateTime = dateOfFirstLanding.format(formatter); 

		try {
			gregCal.setTime(f2.parse(fullDateTimeString));
			computeMillisAtStart();
		} catch (Exception ex) {// ParseException ex) {
			ex.printStackTrace();
			// throw new IllegalStateException(ex);
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

	public Calendar getCalender() {
		return gregCal;
	}

	/**
	 * Returns the current date/time
	 * 
	 * @return date
	 */
	public Date getCurrentDateTime() {
		return gregCal.getTime();
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
		millisAtStart = gregCal.getTimeInMillis();
	}

	/**
	 * Returns the number of milliseconds since 1 January 1970 00:00:00
	 * 
	 * @return long
	 */
	public static long getMillis(EarthClock clock) {
		long millis = clock.getCalender().getTimeInMillis();

		// Instant instant =
		// Instant.ofEpochMilli(clock.getCalender().getTimeInMillis());
		// System.out.println("millis from cal is " +
		// clock.getCalender().getTimeInMillis());
		// System.out.println("instant is " + instant);

		// ZoneId zoneId = ZoneId.of("UTC-0");
		// LocalDateTime ldt = LocalDateTime.ofInstant(instant, zoneId);//
		// ZoneId.systemDefault());

		// convert formatter_millis to dtFormatter
		// String s = getCurrentDateTimeString(clock);
		// s = s.substring(0, 21) + "0" + s.substring(22, 24);

		// System.out.println("s mod is "+ s);

		// LocalDateTime ldt = LocalDateTime.parse(s, dtFormatter_millis);

		// ZoneOffset offset = ZoneOffset.of("+00:00");
		// ZonedDateTime dt = ZonedDateTime.now( ZoneOffset.UTC );
		// ZoneId zoneId = ZoneId.of("UTC-0");
		// LocalDateTime ldt = LocalDateTime.of(2043, 9, 30, 00, 00, 00);
		// ZonedDateTime zdt = ldt.atZone(ZoneId.of("UTC-0"));
		// ZonedDateTime zdt = ZonedDateTime.of(2043, 9, 30, 00, 00, 00, 0000, zoneId);
		// long millis = zdt.toInstant().toEpochMilli();
		// long sec = zdt.getEpochSecond();
		// long millis2 = zdt.get(ChronoField.MILLI_OF_SECOND);
		// System.out.println("millis2 is " + millis2);

		return millis;
	}

	/**
	 * Returns the date/time formatted in a string
	 * 
	 * @return date/time formatted in a string. eg "2055-May-06 03:37:22"
	 */
	public String getCurrentDateTimeString(EarthClock clock) {
		return f3.format(clock.getCalender().getTime());
	}

	/**
	 * Returns the date/time formatted in a string
	 * 
	 * @return date/time formatted in a string. ex "2055-05-06 03:37:22 (UT)"
	 */
	public String getTimeStampF0() {
		String result = f0.format(gregCal.getTime());
		if (result == null)
			result = "0";
		return result;
	}

	/**
	 * Returns the date/time formatted in a string
	 * 
	 * @return date/time formatted in a string. ex "2055-05-06 03:37 (UT)"
	 */
	public String getTimeStampF1() {
		String result = f1.format(gregCal.getTime());
		if (result == null)
			result = "0";
		return result;
	}

	/**
	 * Returns the date/time formatted in a string
	 * 
	 * @return date/time formatted in a string. ex "2055-May-06 03:37:22"
	 */
	public String getTimeStampF3() {
		String result = f3.format(gregCal.getTime());
		if (result == null)
			result = "0";
		return result;
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
	 * @param ms milliseconds added to the calendar
	 */
	public void addTime(double ms) {
		gregCal.add(Calendar.MILLISECOND, (int) (Math.round(ms)));
	}

	/**
	 * Displays the string version of the clock.
	 * 
	 * @return time stamp string.
	 * @deprecated
	 */
	public String toString() {
		return getTimeStampF0();
	}

	public int getDayOfMonth() {
		return gregCal.get(Calendar.DATE);
	}

	public int getMonth() {
		return gregCal.get(Calendar.MONTH);
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
		return gregCal.get(Calendar.YEAR);
	}

	public void setYear(int year) {
		gregCal.set(Calendar.YEAR, year);
	}

	public int getSecond() {
		return gregCal.get(Calendar.SECOND);
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
		return gregCal.get(Calendar.MINUTE);
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
		return gregCal.get(Calendar.HOUR);
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
		int w = gregCal.get(Calendar.DAY_OF_WEEK);
		StringBuilder s = new StringBuilder();
		if (w == Calendar.SUNDAY)
			s.append("Sunday");
		else if (w == Calendar.MONDAY)
			s.append("Monday");
		else if (w == Calendar.TUESDAY)
			s.append("Tuesday");
		else if (w == Calendar.WEDNESDAY)
			s.append("Wednesday");
		else if (w == Calendar.THURSDAY)
			s.append("Thursday");
		else if (w == Calendar.FRIDAY)
			s.append("Friday");
		else if (w == Calendar.SATURDAY)
			s.append("Saturaday");
		// else
		// s = "";
		return s.toString();
	}

	public ZonedDateTime convert2ZonedDT() {
		return zonedDateTime = ZonedDateTime.ofInstant(gregCal.toInstant(), ZoneId.of("UTC"));
		// return zonedDateTime = gregCal.toZonedDateTime();
	}

	public ZonedDateTime getZonedDateTime() {
		return zonedDateTime;
	}

	public LocalDate getLocalDate() {
		return convert2ZonedDT().toLocalDate();
	}

	public LocalTime getLocalTime() {
		return convert2ZonedDT().toLocalTime();
	}

	// public Date getDT() {
	// DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	// LocalDate localDate = zonedDateTime //LocalDate.parse(dateString, formatter);
	// Date.from(java.time.ZonedDateTime.now().toInstant());
	// }

	public void destroy() {
		gregCal = null;
		f0 = null;
		f2 = null;
		f1 = null;
		f3 = null;
		zone = null;
	}
}