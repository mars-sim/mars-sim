/**
 * Mars Simulation Project
 * NaturalAttributeManager.java
 * @version 3.1.0 2017-10-03
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * The NaturalAttributeManager class manages a person's natural attributes.
 * There is only natural attribute manager for each person.
 */
public class NaturalAttributeManager implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** List of the person's natural attributes keyed by unique name. */
	private Hashtable<NaturalAttributeType, Integer> attributeTable;
	private List<Map<String, NaturalAttributeType>> n_attributes;

//	private Map<String, Integer> attributeMap;
	private List<String> attributeList;
	
	/**
	 * Constructor.
	 * 
	 * @param person the person with the attributes.
	 */
	NaturalAttributeManager(Person person) {

		attributeTable = new Hashtable<NaturalAttributeType, Integer>();

		attributeList = new ArrayList<>();
//		attributeMap = new HashMap<>();
		
		// Create natural attributes using random values (averaged for bell curve around
		// 50%).
		// Note: this may change later.
		for (NaturalAttributeType attributeKey : NaturalAttributeType.values()) {
			int attributeValue = 0;
			int numberOfIterations = 3;
			for (int y = 0; y < numberOfIterations; y++)
				attributeValue += RandomUtil.getRandomInt(100);
			attributeValue /= numberOfIterations;
			attributeTable.put(attributeKey, attributeValue);
		}

		// Modify the attributes reflective of Martian settlers.
		addAttributeModifier(NaturalAttributeType.ACADEMIC_APTITUDE, 30);
		addAttributeModifier(NaturalAttributeType.AGILITY, 30);
		//addAttributeModifier(NaturalAttributeType.ARTISTRY, 20);
		addAttributeModifier(NaturalAttributeType.COURAGE, 40);
		addAttributeModifier(NaturalAttributeType.ATTRACTIVENESS, 20);
		addAttributeModifier(NaturalAttributeType.EMOTIONAL_STABILITY, 30);
		addAttributeModifier(NaturalAttributeType.ENDURANCE, 5);
		addAttributeModifier(NaturalAttributeType.EXPERIENCE_APTITUDE, 10);
		addAttributeModifier(NaturalAttributeType.LEADERSHIP, 20);
		//addAttributeModifier(NaturalAttributeType.SPIRITUALITY, 60);
		addAttributeModifier(NaturalAttributeType.STRENGTH, 20);
		addAttributeModifier(NaturalAttributeType.STRESS_RESILIENCE, 40);
		//addAttributeModifier(NaturalAttributeType.TEACHING, 20);

		// Adjust certain attributes reflective of differences between the genders.
		// TODO: Do more research on this and cite references if possible.
		if (person.getGender() == GenderType.MALE) {
			addAttributeModifier(NaturalAttributeType.STRENGTH, 20);
		} else if (person.getGender() == GenderType.FEMALE) {
			addAttributeModifier(NaturalAttributeType.STRENGTH, -20);
			addAttributeModifier(NaturalAttributeType.ENDURANCE, 20);
		}
		
		n_attributes = new ArrayList<Map<String, NaturalAttributeType>>();
		for (NaturalAttributeType type : NaturalAttributeType.values()) {
			Map<String,NaturalAttributeType> map = new TreeMap<String,NaturalAttributeType>();
			map.put(type.getName(),type);
			attributeList.add(type.getName());
//			attributeMap.put(value.getName(), getAttribute(value));
			n_attributes.add(map);
		}
		Collections.sort(
			n_attributes,
			new Comparator<Map<String, NaturalAttributeType>>() {
				@Override
				public int compare(Map<String, NaturalAttributeType> o1,Map<String, NaturalAttributeType> o2) {
					return o1.keySet().iterator().next().compareTo(o2.keySet().iterator().next());
				}
			}
		);
	}


	/**
	 * Adds a random modifier to an attribute.
	 * 
	 * @param attributeName the name of the attribute
	 * @param modifier      the random ceiling of the modifier
	 */
	private void addAttributeModifier(NaturalAttributeType attributeName, int modifier) {
		int random = RandomUtil.getRandomInt(Math.abs(modifier));
		if (modifier < 0)
			random *= -1;
		setAttribute(attributeName, getAttribute(attributeName) + random);
	}

	/**
	 * Returns the number of natural attributes.
	 * 
	 * @return the number of natural attributes
	 */
	public int getAttributeNum() {
		return attributeTable.size();
	}

	/**
	 * Gets the integer value of a named natural attribute if it exists. Returns 0
	 * otherwise.
	 * 
	 * @param attribute {@link NaturalAttributeType} the attribute
	 * @return the value of the attribute
	 */
	public int getAttribute(NaturalAttributeType attribute) {
		int result = 0;
		if (attributeTable.containsKey(attribute))
			result = attributeTable.get(attribute);
		return result;
	}

	/**
	 * Sets an attribute's level.
	 * 
	 * @param attrib {@link NaturalAttributeType} the attribute
	 * @param level  the level the attribute is to be set
	 */
	public void setAttribute(NaturalAttributeType attrib, int level) {
		if (level > 100)
			level = 100;
		if (level < 0)
			level = 0;
		attributeTable.put(attrib, level);
	}

	public List<Map<String, NaturalAttributeType>> getAttributes() {
		return n_attributes;
	}
	
	public Hashtable<NaturalAttributeType, Integer> getAttributeTable() {
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