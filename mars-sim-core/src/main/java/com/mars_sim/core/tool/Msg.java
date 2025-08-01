/*
 * Mars Simulation Project
 * Msg.java
 * @date 2025-07-29
 * @author stpa
 */
package com.mars_sim.core.tool;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * For getting internationalization of strings
 * from {@link ResourceBundle}-
 * properties-files, like date or decimal formats,
 * or image-icon-paths (think of red cross vs. crescent).
 * 
 * @author stpa
 */
public class Msg {

	/** default logger. */
	private static final Logger logger = Logger.getLogger(Msg.class.getName());

	/** location of the properties files in the project code base. */
	private static final String BUNDLE_NAME = "messages"; //$NON-NLS-1$

	/**
	 * The default resource bundle.<br/>
	 * while translation is still ongoing, it is safer to set a default locale,
	 * otherwise users with other locales will see partial translations,
	 * which can be very annoying.<br/>
	 * commas are in front to make it easier to comment those lines out.<br/>
	 * in order to test translations, change the desired locale, e.g. "de", "eo".
	 */
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(
		BUNDLE_NAME
		, Locale.US
		,new UTF8Control()
	);

	// Snippets for constructing html-style tooltips
	public static final String BR = "<br>"; //$NON-NLS-1$
	public static final String NBSP = "&nbsp;"; //$NON-NLS-1$
	public static final String HTML_START = "<html>"; //$NON-NLS-1$
	public static final String HTML_STOP = "</html>"; //$NON-NLS-1$

	/** Hidden constructor. */
	private Msg() {
	}

	/**
	 * Gets the string.
	 * 
	 * @param key {@link String} should comply with the format
	 * <code>"ClassName.categoryIfAny.keyForThisText"</code>
	 * @return {@link String} translation for given key to the current locale.
	 */
	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return handle(e, key);
		}
	}

	/**
	 * Gets the string with fallback.
	 * 
	 * @param key {@link String} should comply with the format
	 * <code>"ClassName.categoryIfAny.keyForThisText"</code>
	 * @param fallback Fallback value if no key match
	 */
	public static String getStringWithFallback(String key, String fallback) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return fallback;
		}
	}

	
	/**
	 * Gets the string with a search for an entry <category>.<key> in the resource bundle. 
	 * If no entry is found then the key is formatted and returned.
	 * This is particularly useful to create readable labels for Enum.
	 * 
	 * @param category should comply with the format <code>"ClassName.categoryIfAny"</code>
	 * @param key should comply with the format <code>"keyForThisText"</code>
	 * @return translation for given key to the current locale or the automatically
	 * generated key if no translation is found.
	 */
	public static String getStringOptional(String category, String key) {
		var fullKey = category + "." + key.toLowerCase(); //$NON-NLS-1$
		try {
			return RESOURCE_BUNDLE.getString(fullKey);
		} catch (MissingResourceException e) {
			return Conversion.capitalize(key.replace("_", " "));
		}
	}

	/**
	 * Gets the string and replaces all occurrences of "{n}" (with n an integer).
	 * 
	 * @param key
	 * @param args
	 * @return
	 */
	public static String getString(final String key, final Object... args) {
		// Richer formatting rules using built in Message Format class
		return MessageFormat.format(getString(key), args);
	}

	public static boolean getBool(String key) {
		try {
			return Boolean.parseBoolean(RESOURCE_BUNDLE.getString(key));
		} catch (MissingResourceException e) {
			handle(e,key);
			return false;
		}
	}

	public static int getInt(String key) {
		try {
			return Integer.parseInt(RESOURCE_BUNDLE.getString(key));
		} catch (MissingResourceException e) {
			handle(e,key);
			return 0;
		}
	}

	public static long getLong(String key) {
		try {
			return Long.parseLong(RESOURCE_BUNDLE.getString(key));
		} catch (MissingResourceException e) {
			handle(e,key);
			return 0;
		}
	}

	public static double getDouble(String key) {
		try {
			return Double.parseDouble(RESOURCE_BUNDLE.getString(key));
		} catch (MissingResourceException e) {
			handle(e,key);
			return 0;
		}
	}

	public static double[] getDoubleArray(String key) {
		double[] result = null;
		try {
			String rest = RESOURCE_BUNDLE.getString(key);
			if (
				rest != null && (
					rest.startsWith("(") || rest.startsWith("{")
				) && (
					rest.endsWith(")") || rest.endsWith("}")
				)
			) {
				rest = rest.substring(1,rest.length() - 1);
				String[] parts = rest.split(",");
				List<String> list = new ArrayList<>();
				for (String part : parts) {
					if (part != null && !part.isEmpty()) {
						list.add(part);
					}
				}
				result = new double[list.size()];
				for (int i = 0; i < list.size(); i++) {
					result[i] = Double.parseDouble(list.get(i));
				}
			}
		} catch (MissingResourceException e) {
			handle(e,key);
		}
		return result;
	}

	/** prints an error message to the console. */
	private static final String handle(Exception e, String key) {
		var msg = new StringBuilder();
		msg.append("!! ") //$NON-NLS-1$
		.append(key)
		.append(" ??") //$NON-NLS-1$
		.toString();
		logger.log(Level.SEVERE, msg.toString());
		return msg.toString();
	}

	/**
	 * The inner utility class to load properties files in proper Unicode format.
	 * 
	 * @author stpa
	 * @see <a href="http://stackoverflow.com/questions/4659929/how-to-use-utf-8-in-resource-properties-with-resourcebundle">
	 * StackOverflow - How to Use UTF-8 in Resource Properties</a>
	 */
	public static class UTF8Control
	extends Control {

		/* Uses utf-8 instead of default encoding to read files. */
		@Override
		public ResourceBundle newBundle (
			String baseName, Locale locale,
			String format, ClassLoader loader,
			boolean reload
		) throws IllegalAccessException, InstantiationException, IOException {
			// The below is a copy of the default implementation.
			String bundleName = toBundleName(baseName, locale);
			String resourceName = toResourceName(bundleName, "properties");
			ResourceBundle bundle = null;
			InputStream stream = null;
			if (reload) {
				URL url = loader.getResource(resourceName);
				if (url != null) {
					URLConnection connection = url.openConnection();
					if (connection != null) {
						connection.setUseCaches(false);
						stream = connection.getInputStream();
					}
				}
			} else {
				stream = loader.getResourceAsStream(resourceName);
			}
			if (stream != null) {
				try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
					// Only this line is changed to make it to read properties files as UTF-8.
					bundle = new PropertyResourceBundle(reader);
				} finally {
					stream.close();
				}
			}
			return bundle;
		}
	}
}
