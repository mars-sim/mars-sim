/*
 * Mars Simulation Project
 * ConfigHelper.java
 * @date 2021-11-25
 * @author Barry Evans
 */
package org.mars_sim.msp.core.configuration;

import org.jdom2.Element;
import org.mars_sim.msp.core.BoundedObject;
import org.mars_sim.msp.core.LocalPosition;

/**
 * Provides methods applicable to all Config classes.
 */
public class ConfigHelper {

	private static final String X_LOCATION = "xloc";
	private static final String Y_LOCATION = "yloc";
	private static final String WIDTH = "width";
	private static final String LENGTH = "length";
	private static final String FACING = "facing";
	
		
	/**
	 * Convert a text label into a format that is suitable to bne used for an Enum.valueof method.
	 * This involves changing to upper case and repalicing ' ' & '-' with a '_'
	 * @param text
	 * @return
	 */
	public static String convertToEnumName(String text) {
		return text.replaceAll("[ -/]", "_").toUpperCase().trim();
	}

	/**
	 * Parse an element that conforms to the Bounded Object pattern of x,y,w,h,f
	 * @param element
	 * @return
	 */
	public static BoundedObject parseBoundedObject(Element element) {		
		double width = -1D;
		if (element.getAttribute(WIDTH) != null) {
			width = Double.parseDouble(element.getAttributeValue(WIDTH));
		}
	
		// Determine optional length attribute value. "-1" if it doesn't exist.
		double length = -1D;
		if (element.getAttribute(LENGTH) != null) {
			length = Double.parseDouble(element.getAttributeValue(LENGTH));
		}
	
		LocalPosition loc = ConfigHelper.parseLocalPosition(element);
		double facing = Double.parseDouble(element.getAttributeValue(FACING));
		
		return new BoundedObject(loc, width, length, facing);
	}

	/**
	 * Parse an element that conforms to the LocalPosition style.
	 * @param element
	 * @return
	 */
	public static LocalPosition parseLocalPosition(Element element) {
		double x = Double.parseDouble(element.getAttributeValue(X_LOCATION));
		double y = Double.parseDouble(element.getAttributeValue(Y_LOCATION));
		
		return new LocalPosition(x, y);
	}
	
	/**
	 * A generic extract to get an optional Attribute as int value
	 * @param sourceElement The XML Element to extract Attribute from
	 * @param attrname Attribute name to look for
	 * @param defaultInt Default int value if the attribute is not present
	 * @return The Attribute value converted to an int OR the default
	 */
	public static int getOptionalAttributeInt(Element sourceElement, String attrName, int defaultInt) {
		int result = defaultInt;
		String txt = sourceElement.getAttributeValue(attrName);
		if (txt != null) {
			result = Integer.parseInt(txt);
		}
		return result;
	}

		
	/**
	 * A generic extract to get an optional Attribute as double value
	 * @param sourceElement The XML Element to extract Attribute from
	 * @param attrname Attribute name to look for
	 * @param defaultDouble Default int value if the attribute is not present
	 * @return The Attribute value converted to an int OR the default
	 */
	public static double getOptionalAttributeDouble(Element sourceElement, String attrName, double defaultDouble) {
		double result = defaultDouble;
		String txt = sourceElement.getAttributeValue(attrName);
		if (txt != null) {
			result = Double.parseDouble(txt);
		}
		return result;
	}

	/**
	 * A generic extract to get an optional Attribute as bool value
	 * @param sourceElement The XML Element to extract Attribute from
	 * @param attrname Attribute name to look for
	 * @param defaultBool Default boolean value if the attribute is not present
	 * @return The Attribute value converted to an int OR the default
	 */
    public static boolean getOptionalAttributeBool(Element sourceElement, String attrName, boolean defaultBool) {
		boolean result = defaultBool;
		String txt = sourceElement.getAttributeValue(attrName);
		if (txt != null) {
			result = Boolean.parseBoolean(txt);
		}
		return result;
    }
}
