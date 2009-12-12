/**
 * Mars Simulation Project
 * PersonalityType.java
 * @version 2.81 2007-08-26
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person.ai;

import java.io.Serializable;
import java.util.*;

import org.mars_sim.msp.core.*;
import org.mars_sim.msp.core.person.*;

/**
 * The MBTI (Myers-Briggs Type Indicator) personality type for the person.
 */
public class PersonalityType implements Serializable {

	// Personality types
	private static final String ISTP = "ISTP";
	private static final String ISTJ = "ISTJ";
	private static final String ISFP = "ISFP";
	private static final String ISFJ = "ISFJ";
	private static final String INTP = "INTP";
	private static final String INTJ = "INTJ";
	private static final String INFP = "INFP";
	private static final String INFJ = "INFJ";
	private static final String ESTP = "ESTP";
	private static final String ESTJ = "ESTJ";
	private static final String ESFP = "ESFP";
	private static final String ESFJ = "ESFJ";
	private static final String ENTP = "ENTP";
	private static final String ENTJ = "ENTJ";
	private static final String ENFP = "ENFP";
	private static final String ENFJ = "ENFJ";
	
	// The solitude stress modifier per millisol.
	private static final double BASE_SOLITUDE_STRESS_MODIFIER = .1D;

	// The company stress modifier per millisol.
	private static final double BASE_COMPANY_STRESS_MODIFIER = .1D;

	// Domain members
	private static Map<String, Double> personalityTypes = null;
	private String personalityType;
	private Person person;

	/**
	 * Constructor
	 */
	PersonalityType(Person person) throws Exception {
		
		this.person = person;
		
		// Load personality type map if necessary.
		if (personalityTypes == null) loadPersonalityTypes();
		
		// Determine personality type.
		double randValue = RandomUtil.getRandomDouble(100D);
		Iterator<String> i = personalityTypes.keySet().iterator();
		while (i.hasNext()) {
			String type = i.next();
			double percentage = personalityTypes.get(type);
			if (randValue <= percentage) {
				personalityType = type;
				break;
			}
			else {
				randValue-= percentage;
			}
		}
		
		if (personalityType == null) 
			throw new Exception("PersonalityType.constructor(): Unable to determine personality type.");
	}
	
	/**
	 * Gets the personality type as a four letter code.
	 * Ex. "ISTJ"
	 * @return personality type.
	 */
	public String getTypeString() {
		return personalityType;
	}
	
	/**
	 * Sets the personality type
	 * @param newPersonalityType for letter MBTI code.
	 */
	public void setTypeString(String newPersonalityType) {
		if (personalityTypes.containsKey(newPersonalityType)) personalityType = newPersonalityType;
		else throw new IllegalArgumentException("Personality type: " + newPersonalityType + " invalid.");
	}
	
	/**
	 * Get this object as a string.
	 */
	public String toString() {
		return getTypeString();
	}
	
	/**
	 * Get the numerical difference between two personality types (0 - 4)
	 * @param otherPersonality the other MBTI personality to check.
	 * @return total difference in indicators.
	 */
	public int getPersonalityDifference(String otherPersonality) {
		int diff = 0;
		
		for (int x=0; x < 4; x++) 
			if (!personalityType.substring(x, (x + 1)).equals(otherPersonality.substring(x, (x + 1)))) diff++;
		
		return diff;
	}
	
	/**
	 * Checks if the personality is introvert.
	 * @return true if introvert
	 */
	public boolean isIntrovert() {
		if (personalityType.substring(0, 1).equals("I")) return true;
		else return false;
	}
	
	/**
	 * Checks if the personality is extrovert.
	 * @return true if extrovert
	 */
	public boolean isExtrovert() {
		if (personalityType.substring(0, 1).equals("E")) return true;
		else return false;
	}
	
	/**
	 * Checks if the personality is sensor.
	 * @return true if sensor
	 */
	public boolean isSensor() {
		if (personalityType.substring(1, 2).equals("S")) return true;
		else return false;
	}
	
	/**
	 * Checks if the personality is intuitive.
	 * @return true if intuitive
	 */
	public boolean isIntuitive() {
		if (personalityType.substring(1, 2).equals("N")) return true;
		else return false;
	}
	
	/**
	 * Checks if the personality is thinker.
	 * @return true if thinker
	 */
	public boolean isThinker() {
		if (personalityType.substring(2, 3).equals("T")) return true;
		else return false;
	}
	
	/**
	 * Checks if the personality is feeler.
	 * @return true if feeler
	 */
	public boolean isFeeler() {
		if (personalityType.substring(2, 3).equals("F")) return true;
		else return false;
	}
	
	/**
	 * Checks if the personality is judger.
	 * @return true if judger
	 */
	public boolean isJudger() {
		if (personalityType.substring(3, 4).equals("J")) return true;
		else return false;
	}
	
	/**
	 * Checks if the personality is perceiver.
	 * @return true if perceiver
	 */
	public boolean isPerceiver() {
		if (personalityType.substring(3, 4).equals("P")) return true;
		else return false;
	}
	
	/**
	 * Loads the average percentages for personality types into a map.
	 * @throws Exception if personality type cannot be found or percentages don't add up to 100%.
	 */
	private void loadPersonalityTypes() throws Exception {
		PersonConfig config = SimulationConfig.instance().getPersonConfiguration();
		personalityTypes = new HashMap<String, Double>(16);
		
		try {
			personalityTypes.put(ISTP, config.getPersonalityTypePercentage(ISTP));
			personalityTypes.put(ISTJ, config.getPersonalityTypePercentage(ISTJ));
			personalityTypes.put(ISFP, config.getPersonalityTypePercentage(ISFP));
			personalityTypes.put(ISFJ, config.getPersonalityTypePercentage(ISFJ));
			personalityTypes.put(INTP, config.getPersonalityTypePercentage(INTP));
			personalityTypes.put(INTJ, config.getPersonalityTypePercentage(INTJ));
			personalityTypes.put(INFP, config.getPersonalityTypePercentage(INFP));
			personalityTypes.put(INFJ, config.getPersonalityTypePercentage(INFJ));
			personalityTypes.put(ESTP, config.getPersonalityTypePercentage(ESTP));
			personalityTypes.put(ESTJ, config.getPersonalityTypePercentage(ESTJ));
			personalityTypes.put(ESFP, config.getPersonalityTypePercentage(ESFP));
			personalityTypes.put(ESFJ, config.getPersonalityTypePercentage(ESFJ));
			personalityTypes.put(ENTP, config.getPersonalityTypePercentage(ENTP));
			personalityTypes.put(ENTJ, config.getPersonalityTypePercentage(ENTJ));
			personalityTypes.put(ENFP, config.getPersonalityTypePercentage(ENFP));
			personalityTypes.put(ENFJ, config.getPersonalityTypePercentage(ENFJ));			
		}
		catch (Exception e) {
			throw new Exception("PersonalityType.loadPersonalityTypes(): unable to load a personality type.");
		}
		
		Iterator<String> i = personalityTypes.keySet().iterator();
		double count = 0D;
		while (i.hasNext()) count+= personalityTypes.get(i.next());
		if (count != 100D) 
			throw new Exception("PersonalityType.loadPersonalityTypes(): percentages don't add up to 100%. (total: " + count + ")");
	}
	
	/**
	 * Updates a person's stress based on his/her personality.
	 * @param time the time passing (millisols)
	 * @throws Exception if problem updating stress.
	 */
	public void updateStress(double time) throws Exception {
		
		Collection localGroup = person.getLocalGroup();
		PhysicalCondition condition = person.getPhysicalCondition();
		
		// Introverts reduce stess when alone.
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
}