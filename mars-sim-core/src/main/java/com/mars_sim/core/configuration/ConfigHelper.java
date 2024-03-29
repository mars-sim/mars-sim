/*
 * Mars Simulation Project
 * ConfigHelper.java
 * @date 2021-11-25
 * @author Barry Evans
 */
package com.mars_sim.core.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jdom2.Element;

import com.mars_sim.core.person.PopulationCharacteristics;
import com.mars_sim.core.process.ProcessItem;
import com.mars_sim.core.process.ProcessItemFactory;
import com.mars_sim.core.resource.ItemType;
import com.mars_sim.mapdata.location.BoundedObject;
import com.mars_sim.mapdata.location.LocalPosition;

/**
 * Provides methods applicable to all Config classes.
 */
public class ConfigHelper {

	private static final String MALE_HEIGHT = "average-male-height";
	private static final String FEMALE_HEIGHT = "average-female-height";
	private static final String MALE_WEIGHT = "average-male-weight";
	private static final String FEMALE_WEIGHT = "average-female-weight";
	private static final String X_LOCATION = "xloc";
	private static final String Y_LOCATION = "yloc";
	private static final String WIDTH = "width";
	private static final String LENGTH = "length";
	private static final String FACING = "facing";
	private static final String NAME = "name";
	private static final String NUMBER = "number";	
	private static final String AMOUNT = "amount";
	private static final String ALTERNATIVE = "alternative";

	private ConfigHelper() {
		// static utility class
	}	
	
	/**
	 * Converts a text label into a format that is suitable to be used for an Enum.valueof method.
	 * This involves changing to upper case and replacing ' ' & '-' with a '_'.
	 * 
	 * @param text
	 * @return
	 */
	public static String convertToEnumName(String text) {
		return text.replaceAll("[ -/]", "_").toUpperCase().trim();
	}

	/**
	 * Parses an element that conforms to the Bounded Object pattern of x,y,w,h,f.
	 * 
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
	 * Parses an element that conforms to the LocalPosition style.
	 * 
	 * @param element
	 * @return
	 */
	public static LocalPosition parseLocalPosition(Element element) {
		double x = -1;
		double y = -1;
		
		String xx = element.getAttributeValue(X_LOCATION);
		if (xx != null) {
			x = Double.parseDouble(xx);
		}
		
		String yy = element.getAttributeValue(Y_LOCATION);
		if (yy != null) {
			y = Double.parseDouble(yy);
		}
		
		return new LocalPosition(x, y);
	}
	
	/**
	 * A generic extract to get an optional Attribute as int value.
	 * 
	 * @param sourceElement The XML Element to extract Attribute from
	 * @param attrName Attribute name to look for
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
	 * A generic extract to get an Attribute as int value.
	 * 
	 * @param sourceElement The XML Element to extract Attribute from
	 * @param attrName Attribute name to look for
	 * @return The Attribute value converted to an int OR the default
	 */
    public static int getAttributeInt(Element sourceElement, String attrName) {
		return Integer.parseInt(sourceElement.getAttributeValue(attrName));
    }

	/**
	 * A generic extract to get an optional Attribute as double value.
	 * 
	 * @param sourceElement The XML Element to extract Attribute from
	 * @param attrName Attribute name to look for
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
	 * A generic extract to get an Attribute as double value.
	 * 
	 * @param sourceElement The XML Element to extract Attribute from
	 * @param attrName Attribute name to look for
	 * @return The Attribute value converted to an int OR the default
	 */
    public static double getAttributeDouble(Element sourceElement, String attrName) {
		var str = sourceElement.getAttributeValue(attrName);
		if (str == null) {
			throw new IllegalArgumentException("Attribute " + attrName + " not found in " + sourceElement.getName());
		}
		return Double.parseDouble(str);
    }

	/**
	 * A generic extract to get an optional Attribute as bool value.
	 * 
	 * @param sourceElement The XML Element to extract Attribute from
	 * @param attrName Attribute name to look for
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

	/**
	 * Parses an Element that represents a Population Char. entity. The values are Attributes.
	 * 
	 * @param el
	 * @return
	 */
    public static PopulationCharacteristics parsePopulation(Element el) {
		return new PopulationCharacteristics(
								Double.parseDouble(el.getAttributeValue(MALE_HEIGHT)),
								Double.parseDouble(el.getAttributeValue(FEMALE_HEIGHT)),
								Double.parseDouble(el.getAttributeValue(MALE_WEIGHT)),
								Double.parseDouble(el.getAttributeValue(FEMALE_WEIGHT)));
    }

		/**
	 * Parses the input amount resource elements in a node list.
	 * 
	 * @param resourceNodes the node list.
	 * @param alternateResourceMap the map that stores the resource to be swapped out with an alternate resource
	 * @throws Exception if error parsing resources.
	 */
	public static List<ProcessItem> parseInputResources(List<Element> resourceNodes, 
			Map<ProcessItem, String> alternateResourceMap) {
		List<ProcessItem> list = new ArrayList<>();

		for (Element resourceElement : resourceNodes) {
			ProcessItem primaryItem = parseProcessItem(resourceElement, ItemType.AMOUNT_RESOURCE);
			list.add(primaryItem);

			var alternatives = resourceElement.getChildren(ALTERNATIVE);
			if (!alternatives.isEmpty()) {
				var altItems = parseProcessItems(ItemType.AMOUNT_RESOURCE, alternatives);
				altItems.forEach(i -> alternateResourceMap.put(i, primaryItem.getName()));
			}		
		}

		return list;
	}

	
	/**
	 * Parse a XML node that follows the ProcessItem convention
	 * @param resourceElement
	 * @param type
	 * @return
	 */
	public static ProcessItem parseProcessItem(Element resourceElement, ItemType type) {
		String name = resourceElement.getAttributeValue(NAME);
		String sizeAttr = (type == ItemType.AMOUNT_RESOURCE ? AMOUNT : NUMBER);
		double amount = ConfigHelper.getAttributeDouble(resourceElement, sizeAttr);
		return ProcessItemFactory.createByName(name, type, amount);
	}

	/**
	 * Parses the output amount resource elements in a node list.
	 * 
	 * @param resourceNodes the node list.
	 * @return 
	 * @throws Exception if error parsing resources.
	 */
	public static List<ProcessItem> parseProcessItems(ItemType type, List<Element> resourceNodes) {
		return resourceNodes.stream()
					.map(i -> parseProcessItem(i, type))
					.toList();
	}

	/**
	 * Creates a set of alternative input lists for a process by replacing one resource on the original
	 * list with an alternative.
	 * @param alternateResourceMap
	 * @param inputList
	 * @return
	 */
	public static List<List<ProcessItem>> getAlternateInputsList(Map<ProcessItem, String> alternateResourceMap,
												List<ProcessItem> inputList) {
		List<List<ProcessItem>> results = new ArrayList<>();

		// Create a list for the original resources from alternateResourceMap
		for(var entry : alternateResourceMap.entrySet()) {
			String originalResource = entry.getValue();

			// Create a brand new list
			List<ProcessItem> newInputItems = new ArrayList<>();
			for (var item: inputList) {
				String resName = item.getName();														
				if (resName.equalsIgnoreCase(originalResource)) {
					item = entry.getKey();
				}
				newInputItems.add(item);	
			}
			results.add(newInputItems);
		}

		return results;
	}
}
