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

import com.mars_sim.core.EntityEventType;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.PhysicalCondition;
import com.mars_sim.core.person.ai.social.RelationshipUtil;
import com.mars_sim.core.tool.Conversion;
import com.mars_sim.core.tool.MathUtils;
import com.mars_sim.core.tool.RandomUtil;

public class EmotionManager implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(EmotionManager.class.getName());
	
	private static final double FLATTENNING_FACTOR = 0.95;
	private static final double RANGE = 1.6;
	private static final double PERSONALITY_FACTOR = 2.0;
	
	private Person person;

	
	private static final String[][] EMOTIONAL_DESCRIPTIONS = {
		// Note that each row is a degree of appeal
		// and each column is a degree of engagement
		{ "crushed", "rejected", "deceived", "sad", 		"hated", "restless", "reckless", "manipulative"}, 
		{ "oppressed", "gloomy", "weary", "insecure", 		"disappointed", "troubled", "disgusted", "defiant" },
		{ "fatigued", "guarded", "skeptical", "displeased", "disbelieving", "suspecting", "irritated", "upset" },
		{ "numbing", "sedated", "nonchalant" , "placid", 	"restrained", "anxious", "startled", "alert"},
		
		{ "detached", "bored", "nonchalant", "modest", 		"compliant", "discreet", "energetic", "stimulated"},
		{ "complacent", "serene", "firm", "calm", 			"content","cooperative", "bold", "daring" },
		{ "tranquil", "relaxed", "polite", "relieved", 		"warm", "appreciative", "overcoming", "victorious"},
		{ "introspective", "agreeable", "genial", "grateful", "joyful", "cheerful", "empowering", "masterful"} 
		};

	private static final String[] EMOTIONAL_AXES = {
			// Note: x and y must be of the same length for now
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
	private List<double[]> oVectorList = new CopyOnWriteArrayList<>();

	private String descriptionCache = "";
	
	private PhysicalCondition pc;

	public EmotionManager(Person person) {
		this.person = person;
		
//		may println(Arrays.deepToString(description))
//		may println("# of rows: " + description.length)
//		may println("# of cols: " + description[0].length)
	
		// Create emotional state vectors using random values
		// Note that .4 is the mid-point
		eVector[0] = .4; // + RandomUtil.getRandomDouble(-.2, .2);
		eVector[1] = .4; // + RandomUtil.getRandomDouble(-.2, .2);

//		descriptionCache = computeDescription();
				
		oVectorList = new CopyOnWriteArrayList<>();
		
//		updateEmotion(person.getMind().getTraitManager().getPersonalityVector());
		
		// Save the emotional states
		recordEmotion();
	}

	/**
	 * Records the emotional states.
	 */
	public void recordEmotion() {
		oVectorList.add(eVector);
	}

	/**
	 * Updates the emotion states.
	 * 
	 * @param pVector the personality vector
	 */
	public void updateEmotion(double[] pVector) {
		// Check for physical stimulus
		// Note: physical stimulus affecting iVector
		checkPhysicalStimulus();
	
		// Get the prior history vector
		List<double[]> oVectorList = getOmegaVector(); 
		// Get the new emotional stimulus/Influence vector
		double[] iVector = getInfoVector(); 
	
		// Get Psi Function to incorporate new stimulus
		double[] psi = callPsi(iVector, pVector);
		// Get Omega Function to normalize internal changes such as decay of emotional states
		double[] omega =  MathUtils.normalize(oVectorList);
		
//		int index = oVectorList.size() - 2;
//		if (index < 0)
//			index = 0;
//			
//		// Get the eVector value from the last's last if available
//		double[] omega = oVectorList.get(index);
		
		int dim = getDimension();
		// Construct a new emotional state function modified by psi and omega functions
		double[] newE = new double[dim];

		for (int i = 0; i < 2; i++) {
			newE[i] = RandomUtil.getRandomDouble(-.05, .05) + (eVector[i] + psi[i] / 2.0 + omega[i] / 2.0) / FLATTENNING_FACTOR;
		}

		// Find the new emotion vector
		// java.lang.OutOfMemoryError: Java heap space
//		double[] e_tt = DoubleStream.concat(Arrays.stream(eVector),
//				Arrays.stream(psi)).toArray();
//		double[] e_tt2 = DoubleStream.concat(Arrays.stream(e_tt),
//				Arrays.stream(omega)).toArray();
		// java.lang.OutOfMemoryError: Java heap space
//		double[] e_tt = MathUtils.concatAll(eVector, psi, omega);

		if (newE[0] > RANGE / 2)
			newE[0] = RANGE/  2;
		else if (newE[0] < 0)
			newE[0] = 0;

		if (newE[1] > RANGE / 2)
			newE[1] = RANGE / 2;
		else if (newE[1] < 0)
			newE[1] = 0;

		// Save the emotional states
		recordEmotion();
		
		// Update the emotional states
		eVector = newE;
		
		String oldDes = descriptionCache;
		
		String newDes = computeDescription();
		
		if (!oldDes.equalsIgnoreCase(newDes)) {
			logger.info(person, 0, oldDes + " -> " + newDes);
			person.fireUnitUpdate(EntityEventType.EMOTION_EVENT);
		}
	}

	/**
	 * Checks for physical stimulus.
	 */
	public void checkPhysicalStimulus() {
		if (pc == null)
			pc = person.getPhysicalCondition();

		// TODO: add moral changes due to external events such as death of settlers and triumphant return of a mission
		
		double stress = pc.getStress(); // 0 to 100%
		double perf = pc.getPerformanceFactor(); // 0 to 1
		double fatigue = pc.getFatigue();
		double energy = pc.getEnergy();
		
		// Add effect of my social expectation
		double myOpinionOfThem = (RelationshipUtil.getMyAverageOpinionOfThem(person) - 50)/250;
		
		// Modify level of engagement
		double av0 = (perf - 0.5)/2.5 
				- (fatigue - 250)/2_500 
				+ myOpinionOfThem;

		if (av0 > RANGE)
			av0 = RANGE;
		else if (av0 < -RANGE)
			av0 = -RANGE;

		iVector[0] = av0;
		
		// Add effect of their social expectation
		double theirOpinionOfMe = (RelationshipUtil.getAverageOpinionOfMe(person) - 50)/250;
		
		// Modify level of appeal
		double av1 = (50 - stress)/250 
				+ (energy - 2 * PhysicalCondition.ENERGY_THRESHOLD)/25_000 
				+ theirOpinionOfMe;

		if (av1 > RANGE)
			av1 = RANGE;
		else if (av1 < -RANGE)
			av1 = -RANGE;

		iVector[1] = av1;

	}

	/**
	 * Returns the description of the emotional state.
	 * 
	 * @return description string
	 */
	public String getDescription() {
		return descriptionCache;
	}
	
	/**
	 * Computes the description of the emotional state.
	 * 
	 * @return description string
	 */
	public String computeDescription() {
		double e0 = eVector[0];
		double e1 = eVector[1];
		int row = (int) (Math.round(e0 * 9));
		int col = (int) (Math.round(e1 * 9));
		
		String newDes = Conversion.capitalize(EMOTIONAL_DESCRIPTIONS[row][col] 
				+ " [" + row + ", " + col + "]");
		descriptionCache = newDes;
		return newDes;
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
	 * @param iv iVector
	 * @param pv pVector
	 * @return
	 */
	private static double[] callPsi(double[] iv, double[] pv) {
		double[] v = new double[2];

		for (int i = 0; i < pv.length; i++) {
			if (i == 0) { // Openness
				if (pv[0] > .5) {
					v[0] += iv[0] + pv[0]/2D * PERSONALITY_FACTOR; // Engagement
					v[1] += iv[1] + pv[0]/2D * PERSONALITY_FACTOR; // Valence
				}
				else if (pv[0] < .5) {
					v[0] += iv[0] - pv[0]/2D * PERSONALITY_FACTOR; // Engagement
					v[1] += iv[1] - pv[0]/2D * PERSONALITY_FACTOR; // Valence
				}
				else {
					v[0] += iv[0]; // Engagement
					v[1] += iv[1]; // Valence
				}
					
			}
			else if (i == 1) { // Conscientiousness
				if (pv[1] > .5) {
//					v[0] += av[0] + pv[1]/2D * FACTOR; // Engagement
					v[1] += iv[1] + pv[1]/2D * PERSONALITY_FACTOR; // Valence
				}
				else if (pv[1] < .5) {
//					v[0] += av[0] - pv[1]/2D * FACTOR; // Engagement
					v[1] += iv[1] - pv[1]/2D * PERSONALITY_FACTOR; // Valence
				}
				else {
					v[0] += iv[0]; // Engagement
					v[1] += iv[1]; // Valence
				}

			}
			else if (i == 2) { // Extraversion
				if (pv[2] > .5) {
					v[0] += iv[0] + pv[2] * PERSONALITY_FACTOR;
//					v[1] += av[1] + pv[2] * FACTOR;
				}
				else if (pv[2] < .5) {
					v[0] += iv[0] - pv[2] * PERSONALITY_FACTOR;
//					v[1] += av[1] - pv[2] * FACTOR;
				}
				else {
					v[0] += iv[0]; // Engagement
					v[1] += iv[1]; // Valence
				}

			}
			else if (i == 3) { // Agreeableness
				if (pv[3] > .5) {
//					v[0] += av[0] + pv[3]/2D * FACTOR; // Engagement
					v[1] += iv[1] + pv[3]/2D * PERSONALITY_FACTOR; // Valence
				}
				else if (pv[3] < .5) {
//					v[0] += av[0] - pv[3]/2D * FACTOR;
					v[1] += iv[1] - pv[3]/2D * PERSONALITY_FACTOR;
				}
				else {
					v[0] += iv[0]; // Engagement
					v[1] += iv[1]; // Valence
				}
			}
			else if (i == 4) { // Neuroticism
				if (pv[4] > .5) {
					v[0] += iv[0] - pv[4]/2D * PERSONALITY_FACTOR;
					v[1] += iv[1] - pv[4]/2D * PERSONALITY_FACTOR;
				}
				else if (pv[4] < .5) {
					v[0] += iv[0] + pv[4]/2D * PERSONALITY_FACTOR;
					v[1] += iv[1] + pv[4]/2D * PERSONALITY_FACTOR;
				}
				else {
					v[0] += iv[0]; // Engagement
					v[1] += iv[1]; // Valence
				}
			}

			v[0] = v[0] / 4;
			v[1] = v[1] / 4;
			
			if (v[0] > RANGE)
				v[0] = RANGE;
			else if (v[0] < -RANGE)
				v[0] = 0;

			if (v[1] > RANGE)
				v[1] = RANGE;
			else if (v[1] < -RANGE)
				v[1] = 0;

		}

		return v;
	}

	
	public List<double[]> getOmegaVector() {
		return oVectorList;
	}

	public double[] getInfoVector() {
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
		oVectorList  = null;
		pc = null;
	}
}
