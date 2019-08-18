/**
 * Mars Simulation Project
 * Msg.java
 * @version 3.1.0 2017-11-06
 * @author stpa
 */
package org.mars_sim.msp.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
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
 * used to get internationizations of strings
 * and other stuff from {@link ResourceBundle}-
 * properties-files, like date or decimal formats,
 * or image-icon-paths (think of red cross vs. crescent)
 * @author stpa
 */
public class Msg {

	/** default logger. */
	private static Logger logger = Logger.getLogger(Msg.class.getName());

	/** location of the properties files in the project code base. */
	private static final String BUNDLE_NAME = "messages"; //$NON-NLS-1$

	/**
	 * the default resource bundle.<br/>
	 * while translation is still ongoing, it is safer to set a default locale,
	 * otherwise users with other locales will see partial translations,
	 * which can be very annoying.<br/>
	 * commas are in front to make it easier to comment those lines out.<br/>
	 * in order to test translations, change the desired locale, e.g. "de", "eo".
	 */
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(
		BUNDLE_NAME
		,new Locale("") //$NON-NLS-1$
		,new UTF8Control()
	);

	// snippets for constructing html-style tooltips
	public static final String BR = "<br>"; //$NON-NLS-1$
	public static final String NBSP = "&nbsp;"; //$NON-NLS-1$
	public static final String HTML_START = "<html>"; //$NON-NLS-1$
	public static final String HTML_STOP = "</html>"; //$NON-NLS-1$

	/** hidden constructor. */
	private Msg() {
	}

	/**
	 * @param key {@link String} should comply with the format
	 * <code>"ClassName.categoryIfAny.keyForThisText"</code>
	 * @return {@link String} translation for given key to the current locale.
	 */
	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
//			Log.warn(e.getStackTrace()[1].getClassName());
			return handle(e,key);
		}
		

		
	}

	/*
	 * replaces all occurrences of "{0}" with the given parameter.
	 * @param key {@link String}
	 * @param param1 {@link String}
	 * @return {@link String}
	 *
	 */
//	public static String getString(
//		final String key,
//		final String param1
//	) {
//		return getString(key)
//		.replace("{0}",param1);
//	}

	/*
	 * replaces all occurrences of "{0}" with the given parameter.
	 * @param key {@link String}
	 * @param param1 {@link Integer}
	 * @return {@link String}
	 *
     */
//	public static String getString(
//		final String key,
//		final int param1
//	) {
//		return getString(key)
//		.replace("{0}",Integer.toString(param1));
//	}

	/**
	 * replaces all occurrences of "{n}" (with n an integer)
	 * with the
	 * @param key
	 * @param args
	 * @return
	 */
	public static String getString(final String key, final Object... args) {
		String s = getString(key);
		int i = 0;
		for (Object arg : args) {
			s = s.replace(
				"{" + i + "}",
				arg.toString()
			);
			i++;
		}
		return s;
	}
	
	/*
	 * replaces all occurrences of "{0}" with the given parameter.
	 * @param key {@link String}
	 * @param param1 {@link Double}
	 * @return {@link String}
	 *
	 */
//	public static String getString(
//		final String key,
//		final double param1
//	) {
//		return getString(key)
//		.replace("{0}",Double.toString(param1));
//	}

	/*
	 * replaces all occurrences of "{0}" with the given parameter.
	 * replaces all occurrences of "{1}" with the given second parameter.
	 * @param key {@link String}
	 * @param param1 {@link String}
	 * @param param2 {@link String}
	 * @return {@link String}
	 *
	 */
//	public static String getString(
//		final String key,
//		final String param1,
//		final String param2
//	) {
//		return getString(key)
//		.replace("{0}",param1)
//		.replace("{1}",param2);
//	}

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
				List<String> list = new ArrayList<String>();
				for (String part : parts) {
					if (part != null && part.length() > 0) {
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
	public static final String handle(Exception e,String key) {
		// Note : StringBuffer is thread safe and synchronized whereas StringBuilder is not, 
		// thats why StringBuilder is more faster than StringBuffer.
		StringBuffer msg = new StringBuffer();
		msg.append("!!") //$NON-NLS-1$
		.append(key)
		.append("??") //$NON-NLS-1$
		.toString();
		logger.log(Level.WARNING, msg.toString());
		return msg.toString();
	}

	/**
	 * inner utility class to load properties files in proper unicode format.
	 * @author stpa
	 * @see <a href="http://stackoverflow.com/questions/4659929/how-to-use-utf-8-in-resource-properties-with-resourcebundle">
	 * Stackoverflow - How to Use UTF-8 in Resource Properties</a>
	 */
	public static class UTF8Control
	extends Control {

		/* use utf-8 instead of default encoding to read files. */
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
				try {
					// Only this line is changed to make it to read properties files as UTF-8.
					bundle = new PropertyResourceBundle(new InputStreamReader(stream, "UTF-8"));
				} finally {
					stream.close();
				}
			}
			return bundle;
		}
	}
}
