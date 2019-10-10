/**
 * Mars Simulation Project
 * PersonalityTraitManager.java
 * @version 3.1.0 2016-11-05
 * @author Manny Kung
 */
 
package org.mars_sim.msp.core.person.ai;

import java.io.Serializable;
import java.util.Hashtable;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * The PersonalityTraitManager class manages a person's big five personalities.
 * There is one personality trait manager for each person.
 */
public class PersonalityTraitManager implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/** The person's personality score upper limit. */
	private static final int RANGE = 100;
	/** The person's personality vector. */
	private double[] pVector = null;
	/** The person's ID. */
	private Integer personID;
	/** List of the person's big five personalities keyed by unique name. */
	private Hashtable<PersonalityTraitType, Integer> personalityTraits;
	/** The unit manager instance. */
	private static UnitManager unitManager = Simulation.instance().getUnitManager();
	
	/**
	 * Constructor.
	 * 
	 * @param person.
	 */
	public PersonalityTraitManager(Person person) {
		personID = person.getIdentifier();
		personalityTraits = new Hashtable<PersonalityTraitType, Integer>();
	}
	
	/**
	 * Create a random profile of big five personality scores
	 */
	public void setRandomBigFive() {
		int numberOfIterations = 2;

		// Create big five personality traits using random values
		for (PersonalityTraitType type : PersonalityTraitType.values()) {
			int value = 0;
			for (int y = 0; y < numberOfIterations; y++)
				value += RandomUtil.getRandomInt(RANGE);
			value /= numberOfIterations;
			personalityTraits.put(type, value);
		}

		// Create the personality vector
		getPersonalityVector();
	}

	/**
	 * Gets the I-E pair score
	 * 
	 * @return the score
	 */
	public int getIntrovertExtrovertScore() {
		return getPersonalityTraitMap().get(PersonalityTraitType.EXTRAVERSION);
	}
	
	/**
	 * Sync up with the I-E pair score in MBTI 
	 */
	 public void syncUpExtraversion(int value) { 
//		 int value = getPerson().getMind().getMBTI().getScores().get(0); 
		 personalityTraits.put(PersonalityTraitType.EXTRAVERSION, value); 
	 }


//	/**
//	 * Adds a random modifier to an personality trait.
//	 * @param type the name of the personality trait
//	 * @param modifier the random ceiling of the modifier
//
//	private void addPersonalityTraitModifier(PersonalityTraitType type, int modifier) {
//		int random = RandomUtil.getRandomInt(Math.abs(modifier));
//		if (modifier < 0) random *= -1;
//		setPersonalityTrait(type, getPersonalityTrait(type) + random);
//	}
//	 */

	/**
	 * Returns the number of big five personalities.
	 * 
	 * @return the number of big five personalities
	 */
	public int getPersonalityTraitNum() {
		return personalityTraits.size();
	}

	/**
	 * Gets the integer value of a named personality trait if it exists. Returns 0
	 * otherwise.
	 * 
	 * @param type {@link PersonalityTraitType} the personalityTrait
	 * @return the value of the personalityTrait
	 */
	public int getPersonalityTrait(PersonalityTraitType type) {
		int result = 0;
		if (personalityTraits.containsKey(type))
			result = personalityTraits.get(type);
		return result;
	}

	public Hashtable<PersonalityTraitType, Integer> getPersonalityTraitMap() {
		return personalityTraits;
	}

	/**
	 * Creates the personality vector
	 * 
	 * @return double array
	 */
	public double[] getPersonalityVector() {
		if (pVector == null) {
			pVector = new double[5];
			for (PersonalityTraitType t : personalityTraits.keySet()) {
				if (t == PersonalityTraitType.OPENNESS)
					pVector[0] = personalityTraits.get(t)/100D;
				else if (t == PersonalityTraitType.CONSCIENTIOUSNESS)
					pVector[1] = personalityTraits.get(t)/100D;
				else if (t == PersonalityTraitType.EXTRAVERSION)
					pVector[2] = personalityTraits.get(t)/100D;
				else if (t == PersonalityTraitType.AGREEABLENESS)
					pVector[3] = personalityTraits.get(t)/100D;
				else if (t == PersonalityTraitType.NEUROTICISM)
					pVector[4] = personalityTraits.get(t)/100D;
			}
			return pVector;
		}
		else {
			return pVector;
		}
	}

	
	/**
	 * Sets an personality trait's value.
	 * 
	 * @param attrib {@link PersonalityTraitType} the personality trait
	 * @param value  the value the personality trait is to be set
	 */
	public void setPersonalityTrait(PersonalityTraitType type, int value) {
		if (value > 100)
			value = 100;
		if (value < 0)
			value = 0;
		personalityTraits.put(type, value);
	}

	/**
	 * Gets the person's reference.
	 * 
	 * @return {@link Person}
	 */
	public Person getPerson() {
		return unitManager.getPersonByID(personID);
	}
	
	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		personalityTraits.clear();
		personalityTraits = null;
		pVector = null;
		unitManager = null;
	}
}