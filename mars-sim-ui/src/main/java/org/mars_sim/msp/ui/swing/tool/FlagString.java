/*
 * Mars Simulation Project
 * FlagString.java
 * @date 2023-10-11
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.swing.tool;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class FlagString {
	
	private static final int flagOffset = 0x1F1E6;
	private static final int asciiOffset = 0x41;

	private static final Map<String, String> map = new HashMap<>();
	
	static {
		map.put("Austria", "AT");
		map.put("Belgium", "BE");
		map.put("Brazil", "BR");
		map.put("Canada", "CA");
		map.put("China", "CN");
		map.put("Czech Republic", "CZ");
		map.put("Denmark", "DK");
		map.put("Estonia", "EE");
		map.put("Finland", "FI");
		map.put("France", "FR");
		map.put("Germany", "DE");
		map.put("Greece", "GR");
		map.put("Hungary", "HU");
		map.put("India", "IN");
		map.put("Ireland", "IE");
		map.put("Italy", "IT");
		map.put("Japan", "JP");
		map.put("Luxembourg", "LU");
		map.put("Netherlands", "NL");
		map.put("Norway", "NO");	
		map.put("Poland", "PL");
		map.put("Portugal", "PT");
		map.put("Romania", "RO");
		map.put("Russia", "RU");
		map.put("Saudi Arabia", "SA");
		map.put("South Korea", "KR");
		map.put("Spain", "ES");
		map.put("Sweden", "SE");
		map.put("Switzerland", "CH");
		map.put("United Arab Emirates", "AE");
		map.put("United Kingdom", "GB");
		map.put("United States", "US");
	}
	
	public static String getCountryCode(String countryName) {
		if (map.containsKey(countryName))
			return map.get(countryName);
		else
			return "";
	}
	
	// For country codes, see https://en.m.wikipedia.org/wiki/Regional_indicator_symbol
	// Codes derived from 
	// 1. https://stackoverflow.com/questions/42234666/get-emoji-flag-by-country-code
	// 2. https://stackoverflow.com/questions/30494284/android-get-country-emoji-flag-using-locale/35849652#35849652
	
	/**
	 * Gets the flag string in unicode.
	 * 
	 * @param countryCode
	 * @return
	 */
	public static String getEmoji(String countryName) {
		String code = getCountryCode(countryName);
		if (!code.isBlank())
			return getEmoji2(code);
		else
			return "";
	}
	
	/**
	 * Gets the flag string in unicode.
	 * 
	 * @param countryCode
	 * @return
	 */
	public static String getEmoji2(String countryCode) {
		int firstChar = Character.codePointAt(countryCode, 0) - asciiOffset + flagOffset;
		int secondChar = Character.codePointAt(countryCode, 1) - asciiOffset + flagOffset;

		return new String(Character.toChars(firstChar))
		            + new String(Character.toChars(secondChar));
	}
	
	/**
	 * Gets the flag strings from locale.
	 * 
	 * @param locale
	 * @return
	 */
	public static String localeToEmoji(Locale locale) {
		String code = locale.getCountry();
		if (!code.isBlank())
			return getEmoji2(code);
		else
			return "";
	}
}
