/**
 * Mars Simulation Project
 * MBTIPersonality.java
 * @date 2021-12-15
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.tool.RandomUtil;

/*
 * Jung's typology theories postulated a sequence of four cognitive functions
 * (thinking, feeling, sensation, and intuition), each having one of two polar
 * orientations (extraversion or introversion), giving a total of eight dominant
 * functions. 
 * Four main functions of consciousness: 
 *      - 2 perceiving functions: Intuition (N) vs. Sensation (S) 
 *      - 2 judging functions: Feeling (F)  vs. Thinking (T) (B) 
 *      - 2 main attitude types: Extraversion (E) vs. Introversion (I)
 * 
 * The MBTI is based on these eight hypothetical functions, although with some
 * differences in expression from Jung's model
 * 
 * While the Jungian model offers empirical evidence for the first 3
 * dichotomies, it is unclear whether the Briggs had evidence for the J-P
 * preference, namely, Judging (J) vs. Perceiving (P)
 * 
 * As a whole, Brigg's expanded model gives rise to 16 outcomes
 * 
 * In terms of score, in case of the introversion-extraversion pair, 
 * 0 being extremely Introvert, 100 being extremely extravert
 */

/**
 * The Myers-Briggs Type Indicator (MBTI) personality type for the person.
 */
public class MBTIPersonality implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** The person's MBTI */
	private MBTIType mbtiType;
	/** The person's score map */
	private final int[] scores = new int[4];

	// The solitude stress modifier per millisol.
	private static final double BASE_SOLITUDE_STRESS_MODIFIER = .1D;
	// The company stress modifier per millisol.
	private static final double BASE_COMPANY_STRESS_MODIFIER = .1D;
	
	/**
	 * Constructor
	 */
	MBTIPersonality() {
	}

	/**
	 * Obtains a random MBTI type
	 */
	public void setRandomMBTI() {
		// Determine personality type.
		double randValue = RandomUtil.getRandomDouble(100D);
		
		var personalityDistribution = SimulationConfig.instance().getPersonConfig().loadPersonalityDistribution();
		List<MBTIType> distribution = new ArrayList<>(personalityDistribution.keySet());
		Collections.shuffle(distribution);
		
		MBTIType selected = null;		
		for(var type : distribution) {
			double percentage = personalityDistribution.get(type);
			if (randValue <= percentage) {
				selected = type;
				break;
			}
			else {
				randValue -= percentage;
			}
		}

		// Set the MBTI string
		setType(selected);
	}

	/*
	 * Sets the personality score pairs.
	 */
	public void setScorePairs() {

		for (int j = 0; j < 4; j++) {
			int score = -RandomUtil.getRandomInt(1, 50);
			int factor = switch(j) {
				case 0 -> mbtiType.isIntrovert() ? 0 : 50;
				case 1 -> mbtiType.isIntuitive() ? 0 : 50;
				case 2 -> mbtiType.isFeeler() ? 0 : 50;
				case 3 -> mbtiType.isJudger() ? 0 : 50;
				default -> throw new IllegalArgumentException("Invalid score index: " + j);
			};

			scores[j] = score + factor;
		}
	}

	/**
	 * Gets the one word descriptor of this person's MBTI
	 * 
	 * @return
	 */
	public MBTIType getType() {
		return mbtiType;
	}

	/**
	 * Sets the personality type
	 * 
	 * @param newPersonalityType for letter MBTI code.
	 */
	public void setType(MBTIType newPersonalityType) {
		mbtiType = newPersonalityType;
		setScorePairs();
	}

	/**
	 * Gets the I-E pair score
	 * 
	 * @return the score
	 */
	public int getIntrovertExtrovertScore() {
		return scores[0];
	}
	
	/*
	 * Sync up with the I-E pair score in MBTI
	 */
	public void syncUpIntrovertExtravertScore(int value) {
		scores[0] = value - 50;
	}

	/**
	 * Get this object as a string.
	 */
	@Override
	public String toString() {
		return mbtiType.name();
	}

	/**
	 * Get the numerical difference between two personality types (0 - 4)
	 * 
	 * @param otherPersonality the other MBTI personality to check.
	 * @return total difference in indicators.
	 */
	public int getPersonalityDifference(MBTIType otherPersonality) {
		int diff = 0;

		for (int x = 0; x < 4; x++) {
			var s = mbtiType.name().charAt(x);
			var s2 = otherPersonality.name().charAt(x);

			if (s != s2)
				diff++;
		}
		return diff;
	}

	/**
	 * Updates a person's stress based on his/her personality.
	 * 
	 * @param person the person whose stress is being updated
	 * @param time the time passing (millisols)
	 * @throws Exception if problem updating stress.
	 */
	public void updateStress(Person person, double time) {
		
		Collection<Person> localGroup = person.getLocalGroup();
		int groupSize = localGroup.size();

		// Introverts reduce stress when alone (just me in group).
		if (mbtiType.isIntrovert() && (groupSize == 1)) {
			double solitudeStressModifier = BASE_SOLITUDE_STRESS_MODIFIER * time;
			person.getPhysicalCondition().reduceStress(solitudeStressModifier);
		}

		// Extroverts reduce stress when with company, i.e. more than me
		if (mbtiType.isExtrovert() && (groupSize > 1)) {
			double companyStressModifier = BASE_COMPANY_STRESS_MODIFIER * time;
			person.getPhysicalCondition().reduceStress(companyStressModifier);
		}

	}

	/**
	 * Obtains the scores map
	 * 
	 * @return
	 */
	public int [] getScores() {
		return scores;
	}
}
