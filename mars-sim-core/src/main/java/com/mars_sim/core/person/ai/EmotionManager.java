/*
 * Mars Simulation Project
 * EmotionManager.java
 * @date 2023-05-24
 * @author Manny Kung
 */

package com.mars_sim.core.person.ai;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.PhysicalCondition;
import com.mars_sim.core.person.ai.social.RelationshipUtil;
import com.mars_sim.core.tool.Conversion;
import com.mars_sim.core.tool.MathUtils;
import com.mars_sim.core.tool.RandomUtil;

public class EmotionManager implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static final double FLATTENNING_FACTOR = 1.95;
	private static final double RANGE = .5;
	private static final double FACTOR = .05;
	
	private Person person;

	private static final String[][] EMOTIONAL_DESCRIPTIONS = {
		// Note that each row is a degree of appeal
		// and each column is a degree of engagement
		{ "crushed", "rejected", "deceived", "sad", 		"hated", "restless", "reckless", "manipulative"}, 
		{ "oppressed", "gloomy", "weary", "insecure", 		"disappointed", "troubled", "disgusted", "defiant" },
		{ "fatigued", "guarded", "skeptical", "displeased", 	"disbelieving", "suspecting", "irritated", "upset" },
		{ "numbing", "sedated", "nonchalant" , "placid", 	"restrained", "anxious", "startled", "alert"},
		{ "detached", "bored", "nonchalant", "modest", 	"compliant", "discreet", "energetic", "stimulated"},
		{ "complacent", "serene", "firm", "calm", 			"content","cooperative", "bold", "daring" },
		{ "tranquil", "relaxed", "polite", "relieved", 		"warm", "appreciative", "overcoming", "victorious"},
		{ "introspective", "agreeable", "genial", "grateful", "joyful", "cheerful", "empowering", "masterful"} 
		};

	private static final String[] EMOTIONAL_AXES = {
			// row or x-axis
			"appeal", // (or valence) from unpleasant (0) to pleasant (7)		
			// column or y-axis
			"engagement" // (or arousal) from deactivation (0) to activation (7) 
	};

	/** The existing emotional State vector. */
	private double[] eVector = new double[EMOTIONAL_AXES.length];

	/** The influence vector. */
	private double[] iVector = new double[EMOTIONAL_AXES.length];
	
	/** The prior history omega vectors. */
	private List<double[]> oVector = new CopyOnWriteArrayList<>();

	private PhysicalCondition pc;

	public EmotionManager(Person person) {
		this.person = person;
		
//		may println(Arrays.deepToString(description))
//		may println("# of rows: " + description.length)
//		may println("# of cols: " + description[0].length)
	
		// Create emotional state vectors using random values
		// Note that .4 is the mid-point
		eVector[0] = .4 + RandomUtil.getRandomDouble(-.3, .3);
		eVector[1] = .4 + RandomUtil.getRandomDouble(-.3, .3);

		oVector = new CopyOnWriteArrayList<>();

		// Save the first set of emotional states
		saveEmotion();
	}

	/**
	 * Backs up the emotional states.
	 */
	public void saveEmotion() {
		oVector.add(eVector);
	}

	/**
	 * Updates the emotion states.
	 * 
	 * @param pVector the personality vector
	 */
	public void updateEmotion(double[] pVector) {
		// Check for physical stimulus
		checkStimulus();
	
		// Get the prior history vector
		List<double[]> oVector = getOmegaVector(); 
		// Get the new emotional stimulus/Influence vector
		double[] iVector = getEmotionInfoVector(); 
		// Get the existing emotional State vector
		double[] eVector = getEmotionVector();
		
		// Get Psi Function to incorporate new stimulus
		double[] psi = callPsi(iVector, pVector);
		// Get Omega Function to normalize internal changes such as decay of emotional states
		double[] omega = MathUtils.normalize(oVector.get(oVector.size()-1));
		
		int dim = getDimension();
		// Construct a new emotional state function modified by psi and omega functions
		double[] newE = new double[dim];

		for (int i = 0; i < 2; i++) {
			newE[i] = (eVector[i] + psi[i] + omega[i]) / FLATTENNING_FACTOR;
		}

		// Find the new emotion vector
		// java.lang.OutOfMemoryError: Java heap space
//		double[] e_tt = DoubleStream.concat(Arrays.stream(eVector),
//				Arrays.stream(psi)).toArray();
//		double[] e_tt2 = DoubleStream.concat(Arrays.stream(e_tt),
//				Arrays.stream(omega)).toArray();
		// java.lang.OutOfMemoryError: Java heap space
//		double[] e_tt = MathUtils.concatAll(eVector, psi, omega);

		if (newE[0] > RANGE)
			newE[0] = RANGE;
		else if (newE[0] < 0)
			newE[0] = 0;

		if (newE[1] > RANGE)
			newE[1] = RANGE;
		else if (newE[1] < 0)
			newE[1] = 0;

		// Save the emotional states
		saveEmotion();
		// Update the emotional states
		eVector = newE;
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
		double theirOpinionOfMe = (RelationshipUtil.getAverageOpinionOfMe(person) - 50)/10_000;
		
		// Modify level of engagement
		double av0 = (perf - 0.5)/100_000 
				- fatigue/50_000 
				+ theirOpinionOfMe;

		if (av0 > RANGE)
			av0 = RANGE;
		else if (av0 < -RANGE/2)
			av0 = -RANGE/2;

		iVector[0] = av0;
		
		// Add effect of my social expectation
		double myOpinionOfThem = (RelationshipUtil.getMyAverageOpinionOfThem(person) - 50)/10_000;
		
		// Modify level of appeal
		double av1 = (50 - stress)/10_000_000 
				+ (energy - PhysicalCondition.ENERGY_THRESHOLD)/1_000_000 
				+ myOpinionOfThem;

		if (av1 > RANGE)
			av1 = RANGE;
		else if (av1 < -RANGE/2)
			av1 = -RANGE/2;

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
		return Conversion.capitalize(EMOTIONAL_DESCRIPTIONS[row][col] 
				+ " [" + row + ", " + col + "]");
	}

	public int geteVector0() {
		double e0 = eVector[0];
		return (int) (Math.round(e0 * 9D));
	}
	
	public int geteVector1() {
		double e1 = eVector[1];
		return (int) (Math.round(e1 * 9D));
	}
	
	/**
	 * Calls the psi function.
	 *
	 * @param av
	 * @param pv
	 * @return
	 */
	private static double[] callPsi(double[] av, double[] pv) {
		double[] v = new double[2];

		for (int i=0; i<pv.length; i++) {
			if (i == 0) { // Openness
				if (pv[0] > .5) {
					v[0] = av[0] + pv[0]/2D * FACTOR; // Engagement
					v[1] = av[1] + pv[0]/2D * FACTOR; // Valence
				}
				else if (pv[0] < .5) {
					v[0] = av[0] - pv[0] * FACTOR; // Engagement
					v[1] = av[1] - pv[0] * FACTOR; // Valence
				}
			}
			else if (i == 1) { // Conscientiousness
				if (pv[1] > .5) {
//					v[0] = av[0] + pv[1]/2D * FACTOR; // Engagement
					v[1] = av[1] + pv[1]/2D * FACTOR; // Valence
				}
				else if (pv[1] < .5) {
//					v[0] = av[0] - pv[1] * FACTOR; // Engagement
					v[1] = av[1] - pv[1] * FACTOR; // Valence
				}

			}
			else if (i == 2) { // Extraversion
				if (pv[2] > .5) {
					v[0] = av[0] + pv[2]/2D * FACTOR;
//					v[1] = av[1] + pv[2]/2D * FACTOR;
				}
				else if (pv[2] < .5) {
					v[0] = av[0] - pv[2] * FACTOR;
//					v[1] = av[1] - pv[2] * FACTOR;
				}

			}
			else if (i == 3) { // Agreeableness
				if (pv[3] > .5) {
//					v[0] = av[0] + pv[3]/2D * FACTOR; // Engagement
					v[1] = av[1] + pv[3]/2D * FACTOR; // Valence
				}
				else if (pv[3] < .5) {
//					v[0] = av[0] - pv[3] * FACTOR;
					v[1] = av[1] - pv[3] * FACTOR;
				}
			}
			else if (i == 4) { // Neuroticism
				if (pv[4] > .5) {
					v[0] = av[0] - pv[4]/2D * FACTOR;
					v[1] = av[1] - pv[4]/2D * FACTOR;
				}
				else if (pv[4] < .5) {
					v[0] = av[0] + pv[4] * FACTOR;
					v[1] = av[1] + pv[4] * FACTOR;
				}
			}

			if (v[0] > RANGE)
				v[0] = RANGE;
			else if (v[0] < 0)
				v[0] = 0;

			if (v[1] > RANGE)
				v[1] = RANGE;
			else if (v[1] < 0)
				v[1] = 0;

		}

		return v;
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
	
	/**
	 * Prepares object for garbage collection.
	 */
	public void destroy() {
		person = null;
		eVector  = null;
		iVector  = null;
		oVector  = null;
		pc = null;
	}
}
