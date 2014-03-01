package org.mars_sim.msp.ui.swing;

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

/**
 * used to get internationizations of strings
 * and other stuff from {@link ResourceBundle}-
 * properties-files, like date or decimal formats,
 * or image-icon-paths (think of red cross vs. crescent)
 * @author stpa
 */
public class Msg {

	/** location of the properties files in the project code base. */
	private static final String BUNDLE_NAME = "org.mars_sim.msp.ui.swing.messages"; //$NON-NLS-1$

	/**
	 * the default resource bundle.<br/>
	 * while translation is still ongoing, its safer to set a default locale,
	 * otherwise users with other locales will see partial translations.<br/>
	 * commas are in front to make it easier to comment those lines out.<br/>
	 * in order to test translations, change the desired locale, e.g. "de", "eo".
	 */
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(
		BUNDLE_NAME
		,new Locale("") //$NON-NLS-1$
		,new UTF8Control()
	);

	/** hidden constructor. */
	private Msg() {
	}

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}

	/**
	 * replaces all occurances of "%1" with the given parameter.
	 * @param key {@link String}
	 * @param param1 {@link String}
	 * @return {@link String}
	 */
	public static String getString(
		final String key,
		final String param1
	) {
		return getString(key).replace("%1",param1);
	}

	/**
	 * replaces all occurances of "%1" with the given parameter.
	 * replaces all occurances of "%2" with the given second parameter.
	 * @param key {@link String}
	 * @param param1 {@link String}
	 * @param param2 {@link String}
	 * @return {@link String}
	 */
	public static String getString(
		final String key,
		final String param1,
		final String param2
	) {
		return getString(key).replace("%1",param1).replace("%2",param2);
	}

	public static boolean getBool(String key) {
		try {
			return Boolean.parseBoolean(RESOURCE_BUNDLE.getString(key));
		} catch (MissingResourceException e) {
			handle(e);
			return false;
		}
	}

	public static int getInt(String key) {
		try {
			return Integer.parseInt(RESOURCE_BUNDLE.getString(key));
		} catch (MissingResourceException e) {
			handle(e);
			return 0;
		}
	}

	public static long getLong(String key) {
		try {
			return Long.parseLong(RESOURCE_BUNDLE.getString(key));
		} catch (MissingResourceException e) {
			handle(e);
			return 0;
		}
	}

	public static double getDouble(String key) {
		try {
			return Double.parseDouble(RESOURCE_BUNDLE.getString(key));
		} catch (MissingResourceException e) {
			handle(e);
			return 0;
		}
	}

	public static double[] getDoubleArray(String key) {
		double[] result = null;
		try {
			String rest = RESOURCE_BUNDLE.getString(key);
			if (
				(rest != null) && 
				(rest.startsWith("(") || rest.startsWith("{")) && 
				(rest.endsWith(")") || rest.endsWith("}"))
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
			handle(e);
		}
		return result;
	}

	/** prints an error message to the console. */
	public static final void handle(Exception e) {
		System.err.println(e.getMessage());
	}

	/**
	 * inner utility class to load properties files in unicode format.
	 * @author stpa
	 * @see http://stackoverflow.com/questions/4659929/how-to-use-utf-8-in-resource-properties-with-resourcebundle
	 */
	public static class UTF8Control extends Control {
		@Override
		public ResourceBundle newBundle
		(String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
				throws IllegalAccessException, InstantiationException, IOException
				{
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
