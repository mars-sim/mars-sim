/**
 * Mars Simulation Project
 * PersonalityTraitManager.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */
 
package com.mars_sim.core.person.ai;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.mars_sim.core.tool.RandomUtil;

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

	/** List of the person's big five personalities keyed by unique name. */
	private Map<PersonalityTraitType, Integer> personalityTraits;
	
	/**
	 * Constructor.
	 */
	public PersonalityTraitManager() {
		personalityTraits = new ConcurrentHashMap<PersonalityTraitType, Integer>();
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
	 public void syncUpExtraversionScore(int value) { 
		 personalityTraits.put(PersonalityTraitType.EXTRAVERSION, value + 50); 
	 }

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

	public Map<PersonalityTraitType, Integer> getPersonalityTraitMap() {
		return personalityTraits;
	}

	/**
	 * Creates the personality vector.
	 * 
	 * @return double array
	 */
	public double[] getPersonalityVector() {
		if (pVector == null) {
			pVector = new double[5];
			for (var t : personalityTraits.entrySet()) {
				var newValue = t.getValue()/100D;
				int idx = switch (t.getKey()) {
					case OPENNESS -> 0;
					case CONSCIENTIOUSNESS -> 1;
					case EXTRAVERSION -> 2;
					case AGREEABLENESS -> 3;
					case NEUROTICISM -> 4;
				};
				pVector[idx] = newValue;
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
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		personalityTraits.clear();
		personalityTraits = null;
		pVector = null;
	}
}
