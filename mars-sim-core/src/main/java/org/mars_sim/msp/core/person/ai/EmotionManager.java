package org.mars_sim.msp.core.person.ai;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.tool.Conversion;
import org.mars_sim.msp.core.tool.RandomUtil;

public class EmotionManager implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static final double RANGE = .8;

//	private double stressCache;
//	private double fatigueCache;

	private Person person;

	// row : level of appeal
	// column : level of engagement
	private String[][] description // = new String[5][5];
//	description 
			= { { "restless", "sad", "rejected", "crushed", "deceived", "reckless", "terrified", "hated" },
					{ "oppressed", "gloomy", "weary", "insecure", "disappointed", "troubled", "disgusted", "upset" },
					{ "fatigued", "bored", "nonchalant", "displeased", "disbelieving", "suspicious", "irritated",
							"defiant" },
					{ "numbing", "sedated", "skeptical", "alert", "placid", "anxious", "startled", "tense" },

					{ "detached", "guarded", "nonchalant", "modest", "compliant", "discreet", "energetic",
							"aggressive" },
					{ "restrained", "serene", "firm", "calm", "content", "cooperative", "bold", "daring" },
					{ "tranquil", "relaxed", "polite", "thankful", "warm", "masterful", "stimulated", "powerful" },
					{ "introspective", "agreeable", "genial", "kind", "happy", "cheerful", "joyful", "victorious" } };

	private String[] states = {
			// + ve
			"engagement", // arousal, x-axis
//				"joy",
//				"hope",
//				"relief",
//				"pride",
//				"gratitude",
//				"love",
//				"surprise",
			// -ve
			"appeal" // valence, y-axis
//				"distress",
//				"fear",
//				"disappointment",
//				"remorse",
//				"anger",
//				"hate",
//				"disgust"
	};

	/** The Emotional State vector */
	private double[] eVector = new double[states.length];

	/** The Emotion Influence vector */
	private double[] aVector = new double[states.length];

//	private double[][] wVector;

	private List<double[]> wVector = new ArrayList<>();

	private PhysicalCondition pc;

	public EmotionManager(Person person) {
		this.person = person;

//		int numberOfIterations = 2;
		// Create big five personality traits using random values
//		for (int i=0 ; i < eVector.length ; i++) {
//			int value = 0;
//			for (int y = 0; y < numberOfIterations; y++)
//				value += RandomUtil.getRandomDouble(RANGE);
//			value /= numberOfIterations * 1D;
//			eVector[i] = value;
//		}	

		// .4 is the mid-point
		eVector[0] = .4 + RandomUtil.getRandomDouble(.1) - RandomUtil.getRandomDouble(.1);
		eVector[1] = .4 + RandomUtil.getRandomDouble(.1) - RandomUtil.getRandomDouble(.1);

		wVector = new ArrayList<>();
//		wVector = new double[states.length][];

		// Saves the first set of emotional states
		saveEmotion();
	}

	/**
	 * Backs up the emotional states
	 */
	public void saveEmotion() {
//		int size = wVector.length;
//		wVector[size] = eVector;
//		int size = wVector.size();
		wVector.add(eVector);
	}

	public void updateEmotion(double[] v) {
		saveEmotion();
		eVector = v;
	}

	public void checkStimulus() {
		if (pc == null)
			pc = person.getPhysicalCondition();

		double stress = pc.getStress(); // 0 to 100%
		double perf = pc.getPerformanceFactor(); // 0 to 1

		// Modify level of engagement
		aVector[0] = .001 * (1 - stress / 100D) + .005 * perf;

		if (aVector[0] > .8)
			aVector[0] = .8;
		else if (aVector[0] < 0)
			aVector[0] = 0;

		// Modify level of appeal
		aVector[1] = .005 * (1 - stress / 100D) + .001 * perf;

		if (aVector[1] > .8)
			aVector[1] = .8;
		else if (aVector[1] < 0)
			aVector[1] = 0;

	}

	/**
	 * Returns the description of the emotional state
	 * 
	 * @return description string
	 */
	public String getDescription() {
		double e0 = eVector[0];
		double e1 = eVector[1];
//		System.out.print("e0 : " + e0);
//		System.out.print("   e1 : " + e1);
		int row = (int) (e0 * 9D);
		int col = (int) (e1 * 9D);
//		System.out.print("   row : " + row);
//		System.out.println("   col : " + col);
		return Conversion.capitalize(description[row][col]);
	}

//	public double[][] getOmegaVector() {
//		return wVector;
//	}

	public List<double[]> getOmegaVector() {
		return wVector;
	}

	public double[] getEmotionInfoVector() {
		return aVector;
	}

	public double[] getEmotionVector() {
		return eVector;
	}

	public int getDimension() {
		return states.length;
	}
}
