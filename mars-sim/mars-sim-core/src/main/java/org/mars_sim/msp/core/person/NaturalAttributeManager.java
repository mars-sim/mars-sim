/**
 * Mars Simulation Project
 * NaturalAttributeManager.java
 * @version 3.02 2011-11-26
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person;

import org.mars_sim.msp.core.RandomUtil;

import java.io.Serializable;
import java.util.Hashtable;

/** 
 * The NaturalAttributeManager class manages a person's natural attributes.
 * There is only natural attribute manager for each person.
 */
public class NaturalAttributeManager implements Serializable {

	// Natural attributes
	public static final String STRENGTH = "Strength";
	public static final String ENDURANCE = "Endurance";
	public static final String AGILITY = "Agility";
	public static final String TEACHING = "Teaching";
	public static final String ACADEMIC_APTITUDE = "Academic Aptitude";
	public static final String EXPERIENCE_APTITUDE = "Experience Aptitude";
	public static final String STRESS_RESILIENCE = "Stress Resilience";
	public static final String ATTRACTIVENESS = "Attractiveness";
	public static final String LEADERSHIP = "Leadership";
	public static final String CONVERSATION = "Conversation";

    // List of the person's natural attributes by name.
    static private String[] attributeKeys = {STRENGTH, ENDURANCE, AGILITY, TEACHING, ACADEMIC_APTITUDE, 
        EXPERIENCE_APTITUDE, STRESS_RESILIENCE, ATTRACTIVENESS, LEADERSHIP, CONVERSATION}; 

    // List of the person's natural attributes keyed by unique name.
    private Hashtable<String, Integer> attributeList; 

    /**
     * Constructor
     * @param person the person with the attributes.
     */
    NaturalAttributeManager(Person person) {

        attributeList = new Hashtable<String, Integer>();

        // Create natural attributes using random values (averaged for bell curve around 50%).
        // Note: this may change later.
        for (String attributeKey : attributeKeys) {
            int attributeValue = 0;
            int numberOfIterations = 3;
            for (int y = 0; y < numberOfIterations; y++) attributeValue += RandomUtil.getRandomInt(100);
            attributeValue /= numberOfIterations;
            attributeList.put(attributeKey, attributeValue);
        }

        // Adjust certain attributes reflective of Martian settlers.
		addAttributeModifier(STRENGTH, 40);
		addAttributeModifier(ENDURANCE, 40);
		addAttributeModifier(AGILITY, 20);
		addAttributeModifier(STRESS_RESILIENCE, 80);
		addAttributeModifier(TEACHING, 40);
		addAttributeModifier(ACADEMIC_APTITUDE, 80);
		addAttributeModifier(EXPERIENCE_APTITUDE, 60);
        
        // Adjust certain attributes reflective of differences between the genders.
        // TODO: Do more research on this and cite references if possible.
        if (person.getGender().equals(Person.MALE)) {
			addAttributeModifier(STRENGTH, 40);
        }
        else if (person.getGender().equals(Person.FEMALE)) {
			addAttributeModifier(STRENGTH, -40);
			addAttributeModifier(ENDURANCE, 20);
        }
    }

	/**
	 * Adds a random modifier to an attribute.
	 * @param attributeName the name of the attribute
	 * @param modifier the random ceiling of the modifier
	 */
	private void addAttributeModifier(String attributeName, int modifier) {
		int random = RandomUtil.getRandomInt(Math.abs(modifier));
		if (modifier < 0) random *= -1;
		setAttribute(attributeName, getAttribute(attributeName) + random);
	}

    /** Returns the number of natural attributes. 
     *  @return the number of natural attributes
     */
    public int getAttributeNum() {
        return attributeKeys.length;
    }

    /** Returns an array of the natural attribute names as strings. 
     *  @return an array of the natural attribute names
     */
    public static String[] getKeys() {
        String[] result = new String[attributeKeys.length];
        System.arraycopy(attributeKeys, 0, result, 0, result.length);
        return result;
    }

    /** 
     * Gets the integer value of a named natural attribute if it exists.
     * Returns 0 otherwise.
     * @param name the name of the attribute
     * @return the value of the attribute
     */
    public int getAttribute(String name) {
        int result = 0;
        if (attributeList.containsKey(name)) result = attributeList.get(name);
        return result;
    }

    /** 
     * Sets an attribute's level.
     * @param name the name of the attribute
     * @param level the level the attribute is to be set
     */
    public void setAttribute(String name, int level) {

        if (level > 100) level = 100;
        if (level < 0) level = 0;

        boolean found = false;
        for (String attributeKey : attributeKeys) {
            if (name.equals(attributeKey)) {
                attributeList.put(name, level);
                found = true;
            }
        }
        if (!found) throw new IllegalArgumentException("Attribute: " + name + " is not a valid attribute.");
    }

    /**
     * Prepare object for garbage collection.
     */
    public void destroy() {
        attributeList.clear();
        attributeList = null;
    }
}