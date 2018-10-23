/**
 * Mars Simulation Project
 * PersonalityType.java
 * @version 3.1.0 2016-10-31
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person.ai;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PersonConfig;
import org.mars_sim.msp.core.person.PersonalityTraitType;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * The Myers-Briggs Type Indicator (MBTI) personality type for the person.
 */
public class PersonalityType implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Jung's typology theories postulated a sequence of four cognitive functions
	// (thinking, feeling, sensation, and intuition),
	// each having one of two polar orientations (extraversion or introversion),
	// giving a total of eight dominant functions.
	// (A). Four main functions of consciousness:
	// - 2 perceiving functions: Intuition (N) vs. Sensation (S)
	// - 2 judging functions: Feeling (F) vs. Thinking (T)
	// (B). Two main attitude types: Extraversion (E) vs. Introversion (I)

	// The MBTI is based on these eight hypothetical functions, although with some
	// differences in expression from Jung's model
	// While the Jungian model offers empirical evidence for the first 3
	// dichotomies,
	// it is unclear whether the Briggs had evidence for the J-P preference.
	// (C). Judging (J) vs. Perceiving (P)

	// As a whole, they above gives rise to 16 outcomes

	// TODO Personality types should be enums
	public static final String ISTP = "ISTP";
	public static final String ISTJ = "ISTJ";
	public static final String ISFP = "ISFP";
	public static final String ISFJ = "ISFJ";
	public static final String INTP = "INTP";
	public static final String INTJ = "INTJ";
	public static final String INFP = "INFP";
	public static final String INFJ = "INFJ";
	public static final String ESTP = "ESTP";
	public static final String ESTJ = "ESTJ";
	public static final String ESFP = "ESFP";
	public static final String ESFJ = "ESFJ";
	public static final String ENTP = "ENTP";
	public static final String ENTJ = "ENTJ";
	public static final String ENFP = "ENFP";
	public static final String ENFJ = "ENFJ";

	// Add four MBTI scores
	public static final int INTROVERSION_EXTRAVERSION = 0;
	public static final int INTUITION_SENSATION = 1;
	public static final int FEELING_THINKING = 2;
	public static final int JUDGING_PERCEIVING = 3;

	// The solitude stress modifier per millisol.
	private static final double BASE_SOLITUDE_STRESS_MODIFIER = .1D;

	// The company stress modifier per millisol.
	private static final double BASE_COMPANY_STRESS_MODIFIER = .1D;

	// Domain members
	// % Breakdown of MBTI type of a general population, loading from people.xml
	private static Map<String, Double> personalityDistribution = null;

	// Add score map for each settler
	private Map<Integer, Integer> scores = null;

	// In case of Introversion vs. Extraversion pair, 0 is extremely Introvert, 100
	// is extremely extravert
	private String personalityType;
	private Person person;

	/**
	 * Constructor
	 */
	PersonalityType(Person person) {

		this.person = person;

		PersonConfig config = SimulationConfig.instance().getPersonConfiguration();

		// Load personality type map if necessary.
		if (personalityDistribution == null)
			personalityDistribution = config.loadPersonalityDistribution();

		// Determine personality type.
		double randValue = RandomUtil.getRandomDouble(100D);
		Iterator<String> i = personalityDistribution.keySet().iterator();
		while (i.hasNext()) {
			String type = i.next();
			double percentage = personalityDistribution.get(type);
			if (randValue <= percentage) {
				personalityType = type;
				break;
			} else {
				randValue -= percentage;
			}
		}

		// Add setScorePairs()
		setScorePairs();

		if (personalityType == null)
			throw new IllegalStateException("PersonalityType.constructor(): Unable to determine personality type.");
	}

	/*
	 * Sets the personality score pairs.
	 */
	public void setScorePairs() {

		// Add computing the scores
		scores = new HashMap<Integer, Integer>(4);

		for (int j = 0; j < 4; j++) {

			int score = 0;
			int rand = RandomUtil.getRandomInt(50);
			if (j == 0) {
				if (isIntrovert())
					score = rand;
				else
					score = rand + 50;
			} else if (j == 1) {
				if (isIntuitive())
					score = rand;
				else
					score = rand + 50;
			} else if (j == 2) {
				if (isFeeler())
					score = rand;
				else
					score = rand + 50;
			} else if (j == 3) {
				if (isJudger())
					score = rand;
				else
					score = rand + 50;
			}

			scores.put(j, score);
		}

	}

	/**
	 * Gets the personality type as a four letter code. Ex. "ISTJ"
	 * 
	 * @return personality type.
	 */
	public String getTypeString() {
		return personalityType;
	}

	/**
	 * Sets the personality type
	 * 
	 * @param newPersonalityType for letter MBTI code.
	 */
	public void setTypeString(String newPersonalityType) {
		if (personalityDistribution.containsKey(newPersonalityType)) {
			personalityType = newPersonalityType;
			setScorePairs();
		} else
			throw new IllegalArgumentException("Personality type: " + newPersonalityType + " invalid.");
	}

	/*
	 * Sync up with the I-E pair score in MBTI
	 */
	public void syncUpExtraversion() {
		int value = person.getMind().getTraitManager().getPersonalityTraitMap().get(PersonalityTraitType.EXTRAVERSION);
		scores.put(0, value);
	}

	/**
	 * Get this object as a string.
	 */
	public String toString() {
		return personalityType;
	}

	/**
	 * Get the numerical difference between two personality types (0 - 4)
	 * 
	 * @param otherPersonality the other MBTI personality to check.
	 * @return total difference in indicators.
	 */
	public int getPersonalityDifference(String otherPersonality) {
		int diff = 0;

		for (int x = 0; x < 4; x++)
			if (!personalityType.substring(x, (x + 1)).equals(otherPersonality.substring(x, (x + 1))))
				diff++;

		return diff;
	}

	/**
	 * Checks if the personality is introvert.
	 * 
	 * @return true if introvert
	 */
	public boolean isIntrovert() {
		return personalityType.substring(0, 1).equals("I");
	}

	/**
	 * Checks if the personality is extrovert.
	 * 
	 * @return true if extrovert
	 */
	public boolean isExtrovert() {
		return personalityType.substring(0, 1).equals("E");
	}

	/**
	 * Checks if the personality is sensor.
	 * 
	 * @return true if sensor
	 */
	public boolean isSensor() {
		return personalityType.substring(1, 2).equals("S");
	}

	/**
	 * Checks if the personality is intuitive.
	 * 
	 * @return true if intuitive
	 */
	public boolean isIntuitive() {
		return personalityType.substring(1, 2).equals("N");
	}

	/**
	 * Checks if the personality is thinker.
	 * 
	 * @return true if thinker
	 */
	public boolean isThinker() {
		return personalityType.substring(2, 3).equals("T");
	}

	/**
	 * Checks if the personality is feeler.
	 * 
	 * @return true if feeler
	 */
	public boolean isFeeler() {
		return personalityType.substring(2, 3).equals("F");
	}

	/**
	 * Checks if the personality is judger.
	 * 
	 * @return true if judger
	 */
	public boolean isJudger() {
		return personalityType.substring(3, 4).equals("J");
	}

	/**
	 * Checks if the personality is perceiver.
	 * 
	 * @return true if perceiver
	 */
	public boolean isPerceiver() {
		return personalityType.substring(3, 4).equals("P");
	}

	/**
	 * Updates a person's stress based on his/her personality.
	 * 
	 * @param time the time passing (millisols)
	 * @throws Exception if problem updating stress.
	 */
	public void updateStress(double time) {

		Collection<Person> localGroup = person.getLocalGroup();
		PhysicalCondition condition = person.getPhysicalCondition();

		// Introverts reduce stress when alone.
		if (isIntrovert() && (localGroup.size() == 0)) {
			double solitudeStressModifier = BASE_SOLITUDE_STRESS_MODIFIER * time;
			condition.setStress(condition.getStress() - solitudeStressModifier);
		}

		// Extroverts reduce stress when with company.
		if (isExtrovert() && (localGroup.size() > 0)) {
			double companyStressModifier = BASE_COMPANY_STRESS_MODIFIER * time;
			condition.setStress(condition.getStress() - companyStressModifier);
		}
	}

	public Map<Integer, Integer> getScores() {
		return scores;
	}

	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		if (personalityDistribution != null)
			personalityDistribution.clear();
		personalityDistribution = null;
		personalityType = null;
		person = null;
	}
}