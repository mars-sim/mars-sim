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

/**
 * The EarthClock class keeps track of Earth Universal Time. It should be
 * synchronized with the Mars clock. TODO format date strings in an
 * internationalized fashion depending on user locale.
 */
public class EarthClock implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/*
	 * number of milliseconds since 1 January 1970 00:00:00 at the start of the sim
	 * (2043 Sep 30 00:00:00 UTC0)
	 */
	private static long millisAtStart;

//	private double leftoverCache;
	
//	private GregorianCalendar gregCal;

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
//		gregCal = GregorianCalendar.from(zonedDateTime);
	
		// Set GMT timezone for calendar
		zone = new SimpleTimeZone(0, "GMT");

		// see http://www.diffen.com/difference/GMT_vs_UTC

//		gregCal.setTimeZone(zone);
//		gregCal.clear();

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
//			gregCal.setTime(f2.parse(fullDateTimeString));
//			zonedDateTime = gregCal.toZonedDateTime();
			zonedDateTime = dateOfFirstLanding;

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
		
//		System.out.println(getTimeStampF3());
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

//	public Calendar getCalender() {
//		if (gregCal != null)
//			return gregCal;
//		return null;
//	}

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
//		return gregCal.getTime();
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
		millisAtStart = getInstant().toEpochMilli();//gregCal.getTimeInMillis();
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
	public void addTime(int milliseconds) {
//		if (gregCal != null) gregCal.add(Calendar.MILLISECOND, milliseconds);
//		System.out.println(milliseconds*1_000_000);
		zonedDateTime = zonedDateTime.plusNanos(milliseconds*1_000_000);
//		System.out.println(zonedDateTime);//getTimeStampF3());
//		LocalDateTime hourLater = LocalDateTime.now().plusHours(1);
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
		return zonedDateTime.getDayOfMonth();//gregCal.get(Calendar.DATE);
	}

	public int getMonth() {
		return zonedDateTime.getMonthValue();//gregCal.get(Calendar.MONTH);
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
		return zonedDateTime.getYear();//gregCal.get(Calendar.YEAR);
	}

//	public void setYear(int year) {
//		zonedDateTime.plu //gregCal.set(Calendar.YEAR, year);
//	}

	public int getSecond() {
		return zonedDateTime.getSecond();//gregCal.get(Calendar.SECOND);
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
		return zonedDateTime.getMinute();//gregCal.get(Calendar.MINUTE);
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
		return zonedDateTime.getHour();//gregCal.get(Calendar.HOUR);
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

//	public ZonedDateTime convert2ZonedDT() {
//		return zonedDateTime; //= ZonedDateTime.ofInstant(gregCal.toInstant(), ZoneId.of("UTC"));
//		// return zonedDateTime = gregCal.toZonedDateTime();
//	}

	public ZonedDateTime getZonedDateTime() {
		return zonedDateTime;
	}

	public LocalDate getLocalDate() {
		return zonedDateTime.toLocalDate();
	}

	public LocalTime getLocalTime() {
		return zonedDateTime.toLocalTime();
	}

	// public Date getDT() {
	// DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	// LocalDate localDate = zonedDateTime //LocalDate.parse(dateString, formatter);
	// Date.from(java.time.ZonedDateTime.now().toInstant());
	// }

//    /**
//     * Save the state of this object to a stream (i.e., serialize it).
//     *
//     * @serialData The value returned by {@code getTime()}
//     *             is emitted (long).  This represents the offset from
//     *             January 1, 1970, 00:00:00 GMT in milliseconds.
//     */
//    private void writeObject(ObjectOutputStream s) {
////        s.defaultWriteObject();
////        s.writeLong(getTimeImpl());
//    }
//
//    /**
//     * Reconstitute this object from a stream (i.e., deserialize it).
//     */
//    private void readObject(ObjectInputStream s)
//         throws IOException, ClassNotFoundException  {
////        s.defaultReadObject();
////        fastTime = s.readLong();
//    }
    
	public void destroy() {
//		gregCal = null;
		f0 = null;
		f2 = null;
		f1 = null;
		f3 = null;
		zone = null;
		zonedDateTime = null;
		dateOfFirstLanding = null;
	}
}