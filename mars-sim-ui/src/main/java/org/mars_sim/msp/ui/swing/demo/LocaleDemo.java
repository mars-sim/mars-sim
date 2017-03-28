package org.mars_sim.msp.ui.swing.demo;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

public class LocaleDemo {

	double d1 = 123456.78099;
	double d2 = 567890;

	String fullDateTimeString = "2043-Sep-30 00:00:00";//"2043-Sep-30 00:00:00";

	public LocaleDemo() {
		Locale locale =  Locale.US;//new Locale("en");//"Russian");//Locale.getDefault(Category.FORMAT);
		//System.out.println("My locale is " + locale);
		System.out.printf("My locale is " + "%5s - %s, %s \n\n" , locale.toString(),
				locale.getDisplayName(), locale.getDisplayCountry());
		// "My locale is en_US"
		// e.g. en_US, ru_RU,
		//System.out.println("US English : " + d1);

		NumberFormat formatter = NumberFormat.getInstance(locale);
		formatter.setMaximumFractionDigits(2);
		formatter.setMinimumFractionDigits(2);
		System.out.printf("%12s --> %5s\n", d1, formatter.format(d1));
		System.out.printf("%12s --> %5s\n\n", d2, formatter.format(d2));
		//System.out.println(locale.getDisplayName() + " " + locale.getDisplayCountry() + " " + locale.toLanguageTag());
		//System.out.printf("%10s - %s, %s \n" , locale.toString(), locale.getDisplayName(), locale.getDisplayCountry());

		// e.g.     ru_RU - Russian (Russia), Russia

		ZonedDateTime zonedDateTime = ZonedDateTime.now();
		// Convert to GregorianCalendar
		GregorianCalendar cal = GregorianCalendar.from(zonedDateTime);

		//cal.setTimeZone(zone);
		//cal.clear();
		TimeZone tz = cal.getTimeZone();
		int rawOffset = tz.getRawOffset();
		int dstOffset = tz.getDSTSavings();
		int dst = dstOffset/1000/3600;
		int diff = rawOffset/1000/3600;
		System.out.printf("%20s %1d\n", "Timezone offset is", diff);
		System.out.printf("%20s %1d\n", "DST offset is",  dst);
		System.out.printf("%16s %1d:00\n", "Local time is UTC", (dst + diff));
		SimpleTimeZone zone = new SimpleTimeZone(0, "GMT");//0, "GMT");

		SimpleDateFormat f2 = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss", Locale.US);//Locale.getDefault(Category.FORMAT));//Locale.US);
		f2.setTimeZone(zone);
		try {
			cal.setTime(f2.parse(fullDateTimeString));
		} catch (Exception ex) {//ParseException ex) {
			ex.printStackTrace();
			//throw new IllegalStateException(ex);
		}

		System.out.println("\nStart date/time for mars-sim");
		System.out.println("          Default : " + fullDateTimeString);
		System.out.println("              Now : " + cal.getTime());
		// e.g. Date/Time is Tue Sep 29 17:00:00 PDT 2043
		// since 0:00 AM (0:00) GMT = 5:00 PM (17:00) Previous Day Los Angeles Time

/*
		//returns array of all locales
        Locale locales[] = SimpleDateFormat.getAvailableLocales();

		//iterate through each locale and print
		// locale code, display name and country
        for (int i = 0; i < locales.length; i++) {

            System.out.printf("%10s - %s, %s \n" , locales[i].toString(),
				locales[i].getDisplayName(), locales[i].getDisplayCountry());

		}
*/
	}


    public static void main(String[] args) {
    	new LocaleDemo();
    }

}
