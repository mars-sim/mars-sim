/**
 * Mars Simulation Project
 * MBTIPersonality.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person.ai;

//import java.util.Optional;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PersonConfig;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.tool.RandomUtil;

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

	public enum MBTIType {
		ISTP,
		ISTJ,
		ISFP,
		ISFJ, 
		
		INTP,
		INTJ,
		INFP,
		INFJ,
		
		ESTP,
		ESTJ,
		ESFP,
		ESFJ,
		
		ENTP,
		ENTJ,
		ENFP,
		ENFJ
	}
	
	/** The person's MBTI */
	public MBTIType mbtiType;

	private static Map<MBTIType, String> descriptor;
	
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
	/** The percent Breakdown of MBTI type of a general population, loading from people.xml. */
	private static Map<String, Double> personalityDistribution = null;
	/** The person's score map */
	private Map<Integer, Integer> scores = null;
	/** The person's MBTI */
//	private String personalityType;
	/** The person's ID. */
	private Integer personID;
	/** The unit manager instance. */
	private static UnitManager unitManager = Simulation.instance().getUnitManager();
	/** The person config instance. */
	private static PersonConfig config = SimulationConfig.instance().getPersonConfig();

	static {
		// Load personality type map if necessary.
		if (personalityDistribution == null)
			personalityDistribution = config.loadPersonalityDistribution();
	}
	
	static {
		descriptor = new ConcurrentHashMap<>();
		descriptor.put(MBTIType.ISTP, "Analyzer");
		descriptor.put(MBTIType.ISTJ, "Inspector");
		descriptor.put(MBTIType.ISFP, "Supporter");
		descriptor.put(MBTIType.ISFJ, "Protector");
			
		descriptor.put(MBTIType.INTP, "Architect");
		descriptor.put(MBTIType.INTJ, "Investigator");
		descriptor.put(MBTIType.INFP, "Idealist");
		descriptor.put(MBTIType.INFJ, "Counselor");
			
		descriptor.put(MBTIType.ESTP, "Troubleshooter");
		descriptor.put(MBTIType.ESTJ, "Coordinator");
		descriptor.put(MBTIType.ESFP, "Energizer");
		descriptor.put(MBTIType.ESFJ, "Harmonizer");
			
		descriptor.put(MBTIType.ENTP, "Catalyst");
		descriptor.put(MBTIType.ENTJ, "Strategist");
		descriptor.put(MBTIType.ENFP, "Improviser");
		descriptor.put(MBTIType.ENFJ, "Mentor");
	}
	
	/**
	 * Constructor
	 */
	MBTIPersonality(Person person) {
		personID = person.getIdentifier();
	}

	/**
	 * Obtains a random MBTI type
	 */
	public void setRandomMBTI() {
		// Determine personality type.
		double randValue = RandomUtil.getRandomDouble(100D);
		
		List<String> distribution = new CopyOnWriteArrayList<>(personalityDistribution.keySet());
		Collections.shuffle(distribution);
		
		Iterator<String> i = distribution.iterator();
		String selected = "";
		
		while (i.hasNext()) {
			String type = i.next();
			double percentage = personalityDistribution.get(type);
			if (randValue <= percentage) {
				selected = type;
				break;
			} else {
				randValue -= percentage;
			}
		}

		// Set the MBTI string
		setTypeString(selected);
	}

	/*
	 * Sets the personality score pairs.
	 */
	public void setScorePairs() {

		// Add computing the scores
		scores = new ConcurrentHashMap<Integer, Integer>(4);

		for (int j = 0; j < 4; j++) {

			int score = 0;
			int rand = -RandomUtil.getRandomInt(1, 50);
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
	 * Gets the one word descriptor of this person's MBTI
	 * 
	 * @return
	 */
	public String getDescriptor() {
		return getDescriptor(mbtiType);
	}
	
	/**
	 * 	Gets the one word descriptor of this person's MBTI
	 * 
	 * @param type
	 * @return
	 */
	public String getDescriptor(MBTIType type) {
		return descriptor.get(type);
	}
	
	/**
	 * Gets the personality type as a four letter code. Ex. "ISTJ"
	 * 
	 * @return personality type.
	 */
	public String getTypeString() {
		return mbtiType.toString();
	}

	/**
	 * Sets the personality type
	 * 
	 * @param newPersonalityType for letter MBTI code.
	 */
	public void setTypeString(String selected) {
		if (personalityDistribution.containsKey(selected)) {

			// Obtain the enum
			for (MBTIType type: MBTIType.values()) {
				if (selected.equalsIgnoreCase(type.toString()))
					mbtiType = type;
			}
			
			setScorePairs();
		} else
			throw new IllegalArgumentException("MBTI Personality type '" + selected + "' is invalid in people.xml.");
	}

	/**
	 * Gets the I-E pair score
	 * 
	 * @return the score
	 */
	public int getIntrovertExtrovertScore() {
		return scores.get(0);
	}
	
	/*
	 * Sync up with the I-E pair score in MBTI
	 */
	public void syncUpIntrovertExtravertScore(int value) {
//		int value = getPerson().getMind().getTraitManager().getPersonalityTraitMap().get(PersonalityTraitType.EXTRAVERSION);
		scores.put(0, value - 50);
	}

	/**
	 * Get this object as a string.
	 */
	public String toString() {
		return mbtiType.toString();
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
			if (!mbtiType.toString().substring(x, (x + 1)).equals(otherPersonality.substring(x, (x + 1))))
				diff++;

		return diff;
	}

	/**
	 * Checks if the personality is introvert.
	 * 
	 * @return true if introvert
	 */
	public boolean isIntrovert() {
		return mbtiType.toString().substring(0, 1).equals("I");
	}

	/**
	 * Checks if the personality is extrovert.
	 * 
	 * @return true if extrovert
	 */
	public boolean isExtrovert() {
		return mbtiType.toString().substring(0, 1).equals("E");
	}

	/**
	 * Checks if the personality is sensor.
	 * 
	 * @return true if sensor
	 */
	public boolean isSensor() {
		return mbtiType.toString().substring(1, 2).equals("S");
	}

	/**
	 * Checks if the personality is intuitive.
	 * 
	 * @return true if intuitive
	 */
	public boolean isIntuitive() {
		return mbtiType.toString().substring(1, 2).equals("N");
	}

	/**
	 * Checks if the personality is thinker.
	 * 
	 * @return true if thinker
	 */
	public boolean isThinker() {
		return mbtiType.toString().substring(2, 3).equals("T");
	}

	/**
	 * Checks if the personality is feeler.
	 * 
	 * @return true if feeler
	 */
	public boolean isFeeler() {
		return mbtiType.toString().substring(2, 3).equals("F");
	}

	/**
	 * Checks if the personality is judger.
	 * 
	 * @return true if judger
	 */
	public boolean isJudger() {
		return mbtiType.toString().substring(3, 4).equals("J");
	}

	/**
	 * Checks if the personality is perceiver.
	 * 
	 * @return true if perceiver
	 */
	public boolean isPerceiver() {
		return mbtiType.toString().substring(3, 4).equals("P");
	}

	/**
	 * Updates a person's stress based on his/her personality.
	 * 
	 * @param time the time passing (millisols)
	 * @throws Exception if problem updating stress.
	 */
	public void updateStress(double time) {
		Person p = getPerson();
		if (p == null) return; // TODO: why getting NPE when loading from a sim ?
		
		Collection<Person> localGroup = p.getLocalGroup();
		PhysicalCondition condition = p.getPhysicalCondition();

		// Introverts reduce stress when alone.
		if (isIntrovert() && (localGroup.size() == 0)) {
			double solitudeStressModifier = BASE_SOLITUDE_STRESS_MODIFIER * time;
			condition.addStress(-solitudeStressModifier);
		}

		// Extroverts reduce stress when with company.
		if (isExtrovert() && (localGroup.size() > 0)) {
			double companyStressModifier = BASE_COMPANY_STRESS_MODIFIER * time;
			condition.addStress(-companyStressModifier);
		}

	}

	/**
	 * Gets the description of lens
	 * 
	 * @param value
	 * @return
	 */
	public static String interpretLens(String value) {
		StringBuffer sb = new StringBuffer();
		
		// Trait 2 & 3
		if (value.contains("ST")) {
			sb.append("Function Lens").append(System.lineSeparator())
			.append(" ST : Prefer to use proven methods of communication.").append(System.lineSeparator());
		}
		else if (value.contains("SF")) {
			sb.append("Function Lens").append(System.lineSeparator())
			.append(" SF : Love to share their experience to help others.").append(System.lineSeparator());
		}
		else if (value.contains("NF")) {
			sb.append("Function Lens").append(System.lineSeparator())
			.append(" NF : Prefer to communicate in creative ways.").append(System.lineSeparator());
		}
		else if (value.contains("NT")) {
			sb.append("Function Lens").append(System.lineSeparator())
			.append(" NT : Love to debate challenging questions.").append(System.lineSeparator());
		}
		
		// Trait 1 & 2
		if (value.contains("IS")) {
			sb.append("Culture Lens").append(System.lineSeparator())
			.append(" IS : Be careful and mindful of details when involved in change.").append(System.lineSeparator());
		}
		else if (value.contains("ES")) {
			sb.append("Culture Lens").append(System.lineSeparator())
			.append(" ES : Love to see and discuss the practical results of change.").append(System.lineSeparator());
		}
		else if (value.contains("IN")) {
			sb.append("Culture Lens").append(System.lineSeparator())
			.append(" IN : Reflect and digest ideas and concepts around the change.").append(System.lineSeparator());
		}
		else if (value.contains("EN")) {
			sb.append("Culture Lens").append(System.lineSeparator())
			.append(" EN : Maximize variety, discuss avenues and implications of change long-term.").append(System.lineSeparator());
		}
		
		// Trait 2 & 4
		if (value.contains("S") && value.contains("J")) {
			sb.append("Temperament Lens").append(System.lineSeparator())
			.append(" SJ : Value responsibility and loyalty.").append(System.lineSeparator());
		}
		else if (value.contains("S") && value.contains("P")) {
			sb.append("Temperament Lens").append(System.lineSeparator())
			.append(" SP : Value cleverness and timeliness.").append(System.lineSeparator());
		}
		else if (value.contains("N") && value.contains("P")) {
			sb.append("Temperament Lens").append(System.lineSeparator())
			.append(" NP : Value inspiration and a personal approach.").append(System.lineSeparator());
		}
		else if (value.contains("N") && value.contains("J")) {
			sb.append("Temperament Lens").append(System.lineSeparator())
			.append(" NJ : Value ingenuity and logic.").append(System.lineSeparator());
		}
		
		return sb.toString();
	}
	
	/**
	 * Obtains the scores map
	 * 
	 * @return
	 */
	public Map<Integer, Integer> getScores() {
		return scores;
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
		if (personalityDistribution != null)
			personalityDistribution.clear();
		personalityDistribution = null;
		mbtiType = null;
		scores = null;
		config = null;
		unitManager = null;
	}
}
