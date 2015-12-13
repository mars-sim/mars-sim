/**
 * Mars Simulation Project
 * PersonalityManager.java
 * @version 3.08 2015-12-12
 * @author Manny Kung
 */

package org.mars_sim.msp.core.person;

import java.io.Serializable;
import java.util.Hashtable;

import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.robot.Robot;

/**
 * The PersonalityManager class manages a person's big five personalities.
 * There is one personality manager for each person.
 */
public class PersonalityManager
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	private static final int RANGE = 100;

	/** List of the person's big five personalities keyed by unique name. */
	private Hashtable<PersonalityTrait, Integer> personalityTraitList;

	/**
	 * Constructor.
	 * @param person.
	 */
	PersonalityManager(Person person) {

		personalityTraitList = new Hashtable<PersonalityTrait, Integer>();

		// Create big five personality traits using random values
		for (PersonalityTrait personalityTraitKey : PersonalityTrait.values()) {
			int personalityValue = 0;
			int numberOfIterations = 3;
			for (int y = 0; y < numberOfIterations; y++) personalityValue += RandomUtil.getRandomInt(RANGE);
			personalityValue /= numberOfIterations;
			personalityTraitList.put(personalityTraitKey, personalityValue);
		}

	}
	
	/**
	 * Adds a random modifier to an personality trait.
	 * @param personalityTraitName the name of the personality trait
	 * @param modifier the random ceiling of the modifier
	 */
	private void addPersonalityTraitModifier(PersonalityTrait personalityTraitName, int modifier) {
		int random = RandomUtil.getRandomInt(Math.abs(modifier));
		if (modifier < 0) random *= -1;
		setPersonalityTrait(personalityTraitName, getPersonalityTrait(personalityTraitName) + random);
	}

	/**
	 * Returns the number of big five personalities.
	 * @return the number of big five personalities
	 */
	public int getPersonalityTraitNum() {
		return personalityTraitList.size();
	}

	/**
	 * Gets the integer value of a named personality trait if it exists.
	 * Returns 0 otherwise.
	 * @param personalityTrait {@link PersonalityTrait} the personalityTrait
	 * @return the value of the personalityTrait
	 */
	public int getPersonalityTrait(PersonalityTrait personalityTrait) {
		int result = 0;
		if (personalityTraitList.containsKey(personalityTrait)) 
			result = personalityTraitList.get(personalityTrait);
		return result;
	}

	/**
	 * Sets an personalityTrait's level.
	 * @param attrib {@link PersonalityTrait} the personalityTrait
	 * @param level the level the personalityTrait is to be set
	 */
	public void setPersonalityTrait(PersonalityTrait personalityTrait, int level) {
		if (level > 100) level = 100;
		if (level < 0) level = 0;
		personalityTraitList.put(personalityTrait, level);
	}

	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		personalityTraitList.clear();
		personalityTraitList = null;
	}
}