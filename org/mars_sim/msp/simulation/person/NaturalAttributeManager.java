/**
 * Mars Simulation Project
 * NaturalAttributeManager.java
 * @version 2.74 2002-01-13
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person;

import org.mars_sim.msp.simulation.*;
import java.io.Serializable;
import java.util.*;

/** The NaturalAttributeManager class manages a person's natural attributes.
 *  There is only natural attribute manager for each person.
 */
public class NaturalAttributeManager implements Serializable {

    // List of the person's natural attributes by name.
    static private String[] attributeKeys = {"Strength", "Endurance", "Agility", "Academic Aptitude", 
        "Experience Aptitude", "Attractiveness", "Presence", "Leadership", "Conversation"}; 

    // Data members
    private Hashtable attributeList; // List of the person's natural attributes keyed by unique name.

    /** Constructs a NaturalAttributeManager object **/
    NaturalAttributeManager() {

        attributeList = new Hashtable();

        // Create natural attributes using random values,
        // Note: this may change later.
        for (int x = 0; x < attributeKeys.length; x++) {
            int attributeValue = 0;
            for (int y = 0; y < 10; y++)
                attributeValue += RandomUtil.getRandomInt(10);
            attributeList.put(attributeKeys[x], new Integer(attributeValue));
        }

        // Adjust certain attributes reflective of Martian settlers.
        addSettlerBonus("Strength", 20);
        addSettlerBonus("Endurance", 20);
        addSettlerBonus("Agility", 10);
        addSettlerBonus("Academic Aptitude", 40);
        addSettlerBonus("Experience Aptitude", 30);
    }

    /** Adds a random bonus for Martian settlers in a given attribute. 
     *  @param attributeName the name of the attribute
     *  @param bonus the settler bonus to be added to the attribute
     */
    private void addSettlerBonus(String attributeName, int bonus) {
        int newValue = getAttribute(attributeName) + RandomUtil.getRandomInt(bonus);
        if (newValue > 100)
            newValue = 100;
        if (newValue < 0)
            newValue = 0;
        attributeList.put(attributeName, new Integer(newValue));
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
    public String[] getKeys() {
        String[] result = new String[attributeKeys.length];

        for (int x = 0; x < result.length; x++)
            result[x] = attributeKeys[x];

        return result;
    }

    /** Returns the integer value of a named natural attribute if it exists.
     *  Returns 0 otherwise.
     *  @param name the name of the attribute
     *  @return the value of the attribute
     */
    public int getAttribute(String name) {
        int result = 0;
        if (attributeList.containsKey(name))
            result = ((Integer) attributeList.get(name)).intValue();

        return result;
    }

    /** Sets an attribute's level.
     *  @param name the name of the attribute
     *  @param level the level the attribute is to be set
     */
    public void setAttribute(String name, int level) {

        if (level > 100) level = 100;
        if (level < 0) level = 0;

        for (int x=0; x < attributeKeys.length; x++) {
            if (name.equals(attributeKeys[x])) attributeList.put(name, new Integer(level));
        }
    }
}
