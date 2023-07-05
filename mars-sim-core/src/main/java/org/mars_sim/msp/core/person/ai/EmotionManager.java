/*
 * Mars Simulation Project
 * EmotionManager.java
 * @date 2023-05-24
 * @author Manny Kung
 */

package org.mars_sim.msp.core.person.ai;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.mars.sim.tools.util.RandomUtil;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.social.RelationshipUtil;
import org.mars_sim.msp.core.tool.Conversion;

public class EmotionManager implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static final double RANGE = .8;

	private Person person;

	// row : level of appeal
	// column : level of engagement
	private static final String[][] EMOTIONAL_DESCRIPTIONS = {
					{ "restless", "sad", "rejected", "crushed", 
							"deceived", "reckless", "terrified", "hated" },
					{ "oppressed", "gloomy", "weary", "insecure", 
								"disappointed", "troubled", "disgusted", "upset" },
					{ "fatigued", "bored", "nonchalant", "displeased", 
								"disbelieving", "suspicious", "irritated", "defiant" },
					{ "numbing", "sedated", "skeptical", "alert", 
								"placid", "anxious", "startled", "tense" },

					{ "detached", "guarded", "nonchalant", "modest", 
								"compliant", "discreet", "energetic", "aggressive" },
					{ "restrained", "serene", "firm", "calm", 
								"content", "cooperative", "bold", "daring" },
					{ "tranquil", "relaxed", "polite", "thankful", 
								"warm", "masterful", "stimulated", "powerful" },
					{ "introspective", "agreeable", "genial", "kind", 
								"happy", "cheerful", "joyful", "victorious" } 
			};

	private static final String[] EMOTIONAL_AXES = {
			// + ve
			"engagement", // arousal - activation or deactivation, on x-axis
//				"joy",
//				"hope",
//				"relief",
//				"pride",
//				"gratitude",
//				"love",
//				"surprise",
			
			// -ve
			"appeal" // valence - pleasant or unpleasant, on y-axis
//				"distress",
//				"fear",
//				"disappointment",
//				"remorse",
//				"anger",
//				"hate",
//				"disgust"
	};

	/** The existing Emotional State vector. */
	private double[] eVector = new double[EMOTIONAL_AXES.length];

	/** The Influence vector. */
	private double[] iVector = new double[EMOTIONAL_AXES.length];
	
	/** The prior history omega vectors. */
	private List<double[]> oVector = new CopyOnWriteArrayList<>();

	private PhysicalCondition pc;

	public EmotionManager(Person person) {
		this.person = person;
		
//		System.out.println(Arrays.deepToString(description));
//		System.out.println("# of rows: " + description.length);
//		System.out.println("# of cols: " + description[0].length);
//		
		// Create emotional state vectors using random values
		// Note that .4 is the mid-point
		eVector[0] = .4 + RandomUtil.getRandomDouble(-.3, .3);
		eVector[1] = .4 + RandomUtil.getRandomDouble(-.3, .3);

		oVector = new CopyOnWriteArrayList<>();

		// Saves the first set of emotional states
		saveEmotion();
	}

	/**
	 * Backs up the emotional states.
	 */
	public void saveEmotion() {
		oVector.add(eVector);
	}

	public void updateEmotion(double[] v) {
		saveEmotion();
		eVector = v;
	}

	/**
	 * Checks for physical stimulus.
	 */
	public void checkStimulus() {
		if (pc == null)
			pc = person.getPhysicalCondition();

		// TODO: add moral changes due to external events such as death of settlers and triumphant return of a mission
		
		double stress = pc.getStress(); // 0 to 100%
		double perf = pc.getPerformanceFactor(); // 0 to 1
		double fatigue = pc.getFatigue();
		double energy = pc.getEnergy();
		
		// Add effect of their social expectation
		double theirOpinionOfMe = (RelationshipUtil.getAverageOpinionOfMe(person) - 50) / 10_000;
		
		// Modify level of engagement
		double av0 = (perf - 0.5)/100_000 - fatigue/50_000 + theirOpinionOfMe;

		if (av0 > RANGE)
			av0 = RANGE;
		else if (av0 < 0)
			av0 = 0;

		iVector[0] = av0;
		
		// Add effect of my social expectation
		double myOpinionOfThem = (RelationshipUtil.getMyAverageOpinionOfThem(person) - 50) / 10_000;
		
		// Modify level of appeal
		double av1 = (50 - stress)/10_000_000 + (energy - PhysicalCondition.ENERGY_THRESHOLD)/1_000_000 + myOpinionOfThem;

		if (av1 > RANGE)
			av1 = RANGE;
		else if (av1 < 0)
			av1 = 0;

		iVector[1] = av1;

	}

	/**
	 * Returns the description of the emotional state.
	 * 
	 * @return description string
	 */
	public String getDescription() {
		double e0 = eVector[0];
		double e1 = eVector[1];
		int row = (int) (Math.round(e0 * 9D));
		int col = (int) (Math.round(e1 * 9D));
		return Conversion.capitalize(EMOTIONAL_DESCRIPTIONS[row][col]);
	}

	public List<double[]> getOmegaVector() {
		return oVector;
	}

	public double[] getEmotionInfoVector() {
		return iVector;
	}

	public double[] getEmotionVector() {
		return eVector;
	}

	public int getDimension() {
		return EMOTIONAL_AXES.length;
	}
}
