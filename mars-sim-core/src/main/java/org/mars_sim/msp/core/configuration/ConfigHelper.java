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
	 */
	public static int getOptionalAttributeInt(Element sourceElement, String attrName, int defaultInt) {
		int result = defaultInt;
		String txt = sourceElement.getAttributeValue(attrName);
		if (txt != null) {
			result = Integer.parseInt(txt);
		}
		return result;
	}
}
