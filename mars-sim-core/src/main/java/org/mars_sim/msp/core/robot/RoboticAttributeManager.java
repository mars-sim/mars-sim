/**
 * Mars Simulation Project
 * RoboticAttributeManager.java
 * @version 3.08 2016-05-04
 * @author Manny Kung
 */

package org.mars_sim.msp.core.robot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * The RoboticAttributeManager class manages a person's natural attributes.
 * There is only natural attribute manager for each person.
 */
public class RoboticAttributeManager
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** List of the person's natural attributes keyed by unique name. */
	private Hashtable<RoboticAttributeType, Integer> attributeTable;
	private List<Map<String, RoboticAttributeType>> r_attributes;
	
	private List<String> attributeList;
	/**
	 * Constructor.
	 * @param robot the robot with the attributes.
	 */
	public RoboticAttributeManager(Robot robot) {

		attributeTable = new Hashtable<RoboticAttributeType, Integer>();

		attributeList = new ArrayList<>();
		
		// Create natural attributes using random values (averaged for bell curve around 50%).
		// Note: this may change later.
		for (RoboticAttributeType type : RoboticAttributeType.values()) {
			int attributeValue = 0;
			int numberOfIterations = 3;
			for (int y = 0; y < numberOfIterations; y++) attributeValue += RandomUtil.getRandomInt(100);
			attributeValue /= numberOfIterations;
			attributeList.add(type.getName());
			attributeTable.put(type, attributeValue);
		}

		// TODO: need to overhaul and define the use of attributes for robots.
		addAttributeModifier(RoboticAttributeType.STRENGTH, 50);
		addAttributeModifier(RoboticAttributeType.ENDURANCE, 50);
		addAttributeModifier(RoboticAttributeType.AGILITY, 50);
		addAttributeModifier(RoboticAttributeType.TEACHING, 5);
		addAttributeModifier(RoboticAttributeType.EXPERIENCE_APTITUDE, 50);
		addAttributeModifier(RoboticAttributeType.CONVERSATION, 5);

 		r_attributes = new ArrayList<Map<String, RoboticAttributeType>>();
			for (RoboticAttributeType value : RoboticAttributeType.values()) {
				Map<String, RoboticAttributeType> map = new TreeMap<String, RoboticAttributeType>();
				map.put(value.getName(),value);
				r_attributes.add(map);
			}
			Collections.sort(
				r_attributes,
				new Comparator<Map<String,RoboticAttributeType>>() {
					@Override
					public int compare(Map<String,RoboticAttributeType> o1,Map<String,RoboticAttributeType> o2) {
						return o1.keySet().iterator().next().compareTo(o2.keySet().iterator().next());
					}
				}
			);
	}

	/**
	 * Adds a random modifier to an attribute.
	 * @param attributeName the name of the attribute
	 * @param modifier the random ceiling of the modifier
	 */
	private void addAttributeModifier(RoboticAttributeType attributeName, int modifier) {
		int random = RandomUtil.getRandomInt(Math.abs(modifier));
		if (modifier < 0) random *= -1;
		setAttribute(attributeName, getAttribute(attributeName) + random);
	}

	/**
	 * Returns the number of natural attributes.
	 * @return the number of natural attributes
	 */
	public int getAttributeNum() {
		return attributeTable.size();
	}

	/**
	 * Gets the integer value of a named natural attribute if it exists.
	 * Returns 0 otherwise.
	 * @param attribute {@link RoboticAttributeType} the attribute
	 * @return the value of the attribute
	 */
	public int getAttribute(RoboticAttributeType attribute) {
		int result = 0;
		if (attributeTable.containsKey(attribute)) result = attributeTable.get(attribute);
		return result;
	}

	/**
	 * Sets an attribute's level.
	 * @param attrib {@link RoboticAttributeType} the attribute
	 * @param level the level the attribute is to be set
	 */
	public void setAttribute(RoboticAttributeType attrib, int level) {
		if (level > 100) level = 100;
		if (level < 0) level = 0;
		attributeTable.put(attrib, level);
	}

	public List<Map<String, RoboticAttributeType>> getAttributes() {
		return r_attributes;
	}
	
	public Hashtable<RoboticAttributeType, Integer> getAttributeTable() {
		return attributeTable;
	}
	
	public List<String> getAttributeList() {
		return attributeList;
	}
	
	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		attributeTable.clear();
		attributeTable = null;
	}
}