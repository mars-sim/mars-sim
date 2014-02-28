package org.mars_sim.msp.ui.swing;

import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Msg {

	private static final String BUNDLE_NAME = "org.mars_sim.msp.ui.swing.messages"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

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
}
