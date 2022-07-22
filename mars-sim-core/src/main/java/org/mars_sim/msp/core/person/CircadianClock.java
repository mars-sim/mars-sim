/*
 * Mars Simulation Project
 * CircadianClock.java
 * @date 2022-07-21
 * @author Manny Kung
 */

package org.mars_sim.msp.core.person;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.mars_sim.msp.core.LifeSupportInterface;
import org.mars_sim.msp.core.data.SolSingleMetricDataLogger;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * The CircadianClock class simulates the circadian rhythm of a person. It
 * regulates the eat/sleep cycle.
 */
public class CircadianClock implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** Sleep Habit Map resolution. */
	private static double SLEEP_INFLATION = 1.15;

	/** Sleep Habit Map resolution. */
	private static int SLEEP_MAP_RESOLUTION = 20;

	/** Sleep Habit maximum value. */
	private static int SLEEP_MAX_FACTOR = 30;

	private boolean awake = true;

	private int solCache;
	private int numSleep;
	private int suppressHabit;
	private int spaceOut;

	// From http://www.webmd.com/diet/features/your-hunger-hormones#1,

	// From http://www.webmd.com/diet/obesity/features/the-facts-on-leptin-faq#1 :
	// When your leptin level is above that threshold, your brain senses that you
	// have energy sufficiency, meaning
	// (1) you can burn energy at a normal rate,
	// (2) you eat food at a normal amount,
	// (3) you engage in exercise at a normal rate.

	/** 
	 * The current leptin level. 
	 * Leptin sends signals to our brains that We have enough fuel and we are 
	 * satisfied with our sleep and we lose interest in both food and sleep.
	 * Most humans have a “diurnal” rhythm to their day. That means they are 
	 * active during the day and sleep at night. Leptin levels tend to peak 
	 * between midnight and dawn, making you less hungry. It's because there’s 
	 * not much one can do about being hungry when being asleep in the middle 
	 * of the night.
	 */
	private double leptinLevel;
	
	/** 
	 * The leptin threshold of each person is tuned by genetics. 
	 * Leptin, aka the safety hormone, is a hormone, made by fat cells, that 
	 * decreases our appetite.
	 */
	private double leptinThreshold = 400;

	/** 
	 * The current ghrelin level. 
	 * Ghrelin, the appetite/sleep increaser, is released primarily in the stomach 
	 * and is thought to signal hunger to the brain. Expect the body to increase 
	 * ghrelin if a person is under-eating and decrease it if one is over-eating.
	 */
	private double ghrelinLevel;
	
	/** 
	 * The ghrelin threshold of each person is tuned by genetics. 
	 * Ghrelin is a hormone that increases appetite, and makes us wanting to eat. 
	 * It also plays a role in body weight.
	 */
	private double ghrelinThreshold = 400;

	private Person person;
	
	/** Sleep habit map keeps track of the sleep cycle */
	private Map<Integer, Integer> sleepCycleMap;

	/** The amount of sleep [millisols] a person on each sol */
	private SolSingleMetricDataLogger sleepHistory = new SolSingleMetricDataLogger(5);

	/** The amount of exercise [millisols] a person on each sol */
	private SolSingleMetricDataLogger exerciseHistory = new SolSingleMetricDataLogger(5);


	/**
	 * Constructors.
	 * 
	 * @param person
	 */
	public CircadianClock(Person person) {
		this.person = person;
		sleepCycleMap = new ConcurrentHashMap<>();		
	}
	
	/**
	 * Initializes field data.
	 */
	public void initialize() {
		// Modify thresholds
		modifyThresholds(person);
		
		double hunger = person.getPhysicalCondition().getHunger();
		double ghrelin = Math.max(hunger * .5, ghrelinThreshold);
		ghrelinLevel = RandomUtil.getRandomDouble(ghrelin * .5 , ghrelin * .75);
		
		double leptin = Math.max(hunger * .5, leptinThreshold);
		leptinLevel = RandomUtil.getRandomDouble(leptin * .5 , leptin * .75);
	}
	
	
	/**
	 * Time passing.
	 * 
	 * @param pulse
	 * @param support
	 */
	public void timePassing(ClockPulse pulse, LifeSupportInterface support) {

		if (pulse.isNewSol()) {
			// Reset numSleep back to zero at the beginning of each sol
			numSleep = 0;
			suppressHabit = 0;
			solCache = pulse.getMarsTime().getMissionSol();
		}


		if (pulse.isNewMSol()) {
			
			// Bring leptin and ghrelin back to equilibrium
			leptinLevel = leptinLevel + .005 * (leptinThreshold/2 - leptinLevel);
			ghrelinLevel = ghrelinLevel + .005 * (ghrelinThreshold/2 - ghrelinLevel);
			
			// Adjust hormones if not having sleep for 12 hours 
			if (awake && person.getPhysicalCondition().getFatigue() > 500) {
				depriveSleep(pulse.getElapsed());
			}
		}
	}

	/**
	 * Calculates the initial thresholds.
	 * 
	 * @param person
	 */
	private void modifyThresholds(Person person) {
		double dev = Math.sqrt(
				person.getBaseMass() / Person.getAverageWeight() * person.getHeight() / Person.getAverageHeight()); 
		// condition.getBodyMassDeviation();
		// person.getBaseMass()
		// Person.AVERAGE_WEIGHT;
		double age = person.getAge();

		// Leptin threshold, the appetite/sleep suppressor, are lower when you're thin
		// and higher when you're fat.
		leptinThreshold *= dev;

		// But many obese people have built up a resistance to the appetite-suppressing
		// effects of leptin.
		// TODO: how to code this resistance ?

		// Ghrelin levels have been found to increase in children with anorexia nervosa
		// and decrease in children who are obese.
		if (age <= 21)
			ghrelinThreshold /= dev;

		// LogConsolidated.log(logger, Level.INFO, 2000, sourceName, person
		// + " LL " + Math.round(leptin_level*10.0)/10.0
		// + " GL " + Math.round(ghrelin_level*10.0)/10.0
		// + " LT " + Math.round(leptin_threshold*10.0)/10.0
		// + " GT " + Math.round(ghrelin_threshold*10.0)/10.0
		// , null);
	}
	

	/**
	 * Gets the key of the Sleep Cycle Map with the highest weight
	 * 
	 * @return int[] the 3 best sleep time in integer
	 */
	public int[] getPreferredSleepHours() {
		int[] largestKey = {0, 0, 0};
		int[] largestValue = {0, 0, 0};
		for (Map.Entry<Integer, Integer> entry : sleepCycleMap.entrySet()) {
			int key = entry.getKey();
			int value = entry.getValue();
			if (value > largestValue[2]) {
				largestValue[0] = largestValue[1];
				largestValue[1] = largestValue[2];
				largestValue[2] = value;
				largestKey[0] = largestKey[1];
				largestKey[1] = largestKey[2];
				largestKey[2] = key;
			} else if (value > largestValue[1]) {
				largestValue[0] = largestValue[1];
				largestValue[1] = value;
				largestKey[0] = largestKey[1];
				largestKey[1] = key;
			} else if (value > largestValue[0]) {
				largestValue[0] = value;
				largestKey[0] = key;
			}
		}

		return largestKey;
	}

	/**
	 * Returns the weight/desire for sleep at a msol.
	 * 
	 * @param index
	 * @return
	 */
	public int getSleepWeight(int msol) {
		if (sleepCycleMap.containsKey(msol)) {
			return sleepCycleMap.get(msol);
		}
		return 0;
	}
	
	/**
	 * Updates the weight of the Sleep Cycle Map
	 * 
	 * @param millisols the time
	 * @param type      increase/encouraged if true; reduce/discouraged if false
	 */
	public void updateSleepCycle(int millisols, boolean type) {
		// set HEAT_MAP_RESOLUTION of discrete sleep periods
		int msols = (millisols / SLEEP_MAP_RESOLUTION) * SLEEP_MAP_RESOLUTION;
		int currentValue = 0;

		int d = msols - SLEEP_MAP_RESOLUTION;
		int d2 = msols - 2 * SLEEP_MAP_RESOLUTION;

		if (d <= 0)
			d = 1000 + msols - SLEEP_MAP_RESOLUTION;

		if (d2 <= 0)
			d2 = 1000 + msols - 2 * SLEEP_MAP_RESOLUTION;

		int a = msols + SLEEP_MAP_RESOLUTION;
		int a2 = msols + 2 * SLEEP_MAP_RESOLUTION;

		if (a > 1000)
			a = msols + SLEEP_MAP_RESOLUTION - 1000;

		if (a2 > 1000)
			a2 = msols + 2 * SLEEP_MAP_RESOLUTION - 1000;

		if (sleepCycleMap.containsKey(msols)) {
			currentValue = sleepCycleMap.get(msols);

			if (type) {
				// Increase the central weight value by 30%
				sleepCycleMap.put(msols, (int) (currentValue * .7 + SLEEP_MAX_FACTOR * .3));

				int c2 = (int) (currentValue * .95 + SLEEP_MAX_FACTOR * .075);
				int c = (int) (currentValue * .85 + SLEEP_MAX_FACTOR * .15);

				sleepCycleMap.put(d2, c2);
				sleepCycleMap.put(d, c);
				sleepCycleMap.put(a, c);
				sleepCycleMap.put(a2, c2);

			} else {

				// Reduce the central weight value by 10%
				sleepCycleMap.put(msols, (int) (currentValue / 1.1));

				int b = (int) (currentValue / 1.05);
				int b2 = (int) (currentValue / 1.025);

				sleepCycleMap.put(d2, b2);
				sleepCycleMap.put(d, b);
				sleepCycleMap.put(a, b);
				sleepCycleMap.put(a2, b2);

			}
		} else {
			// For the first time, create the central weight value with 10% of MAX_WEIGHT
			sleepCycleMap.put(msols, (int) (SLEEP_MAX_FACTOR * .1));

			int e = (int) (SLEEP_MAX_FACTOR * .05);
			int e2 = (int) (SLEEP_MAX_FACTOR * .025);

			sleepCycleMap.put(d2, e2);
			sleepCycleMap.put(d, e);
			sleepCycleMap.put(a, e);
			sleepCycleMap.put(a2, e2);
		}

		// System.out.println(person + "'s sleepHabitMap : " + sleepHabitMap);
	}

	/**
	 * Scales down the weight of the Sleep Habit Map
	 */
	public void inflateSleepHabit() {
		// Iterator<Integer> i = sleepCycleMap.keySet().iterator();
		// while (i.hasNext()) {
		for (int key : sleepCycleMap.keySet()) {// int key = i.next();
			int value = sleepCycleMap.get(key);

			if (value > SLEEP_MAX_FACTOR) {
				value = SLEEP_MAX_FACTOR;
				// need to scale down all values, just in case
				Iterator<Integer> j = sleepCycleMap.keySet().iterator();
				while (j.hasNext()) {
					int key1 = j.next();
					int value1 = sleepCycleMap.get(key1);
					value1 = (int) (value1 / SLEEP_INFLATION / SLEEP_INFLATION);
					sleepCycleMap.put(key1, value1);
				}
			}

			value = (int) (value / SLEEP_INFLATION);
			sleepCycleMap.put(key, value);
		}
	}

	public int getNumSleep() {
		return numSleep;
	}

	public int getSuppressHabit() {
		return suppressHabit;
	}

	public int getSpaceOut() {
		return spaceOut;
	}

	public void setNumSleep(int value) {
		numSleep = value;
	}

	/**
	 * Suppresses the desire to sleep. (Usually when a person is on shift)
	 * 
	 * @param value
	 */
	public void setSuppressHabit(int value) {
		suppressHabit = value;
	}

	public void setSpaceOut(int value) {
		spaceOut = value;
	}

	/**
	 * Increases the leptin to its threshold.
	 * 
	 * @param time
	 */
	private void increaseLeptinToThreshold(double time) {
		if (leptinLevel + time * leptinThreshold / 500.0 >= leptinThreshold)
			leptinLevel = leptinThreshold;
		else
			leptinLevel += time * leptinThreshold / 500.0;
	}

	private void increaseGhrelinToThreshold(double time) {
		if (ghrelinLevel + time * ghrelinThreshold / 500.0 >= ghrelinThreshold)
			ghrelinLevel = ghrelinThreshold;
		else
			ghrelinLevel += time * ghrelinThreshold / 500.0;
	}
	
	private void increaseLeptinBounded(double time) {
		if (leptinThreshold + .01 * time >= 1000D)
			leptinThreshold = 1000D;
		else
			leptinThreshold += .01 * time;
	}
	
	private void decreaseLeptinBounded(double time) {
		if (leptinThreshold > .01 * time)
			leptinThreshold -= .01 * time;
		else
			leptinThreshold = .01 * time;
	}

	private void increaseGhrelinBounded(double time) {
		if (ghrelinThreshold + .01 * time >= 1000D)
			ghrelinThreshold = 1000D;
		else
			ghrelinThreshold += .01 *  time;
	}

	private void decreaseGhrelinBounded(double time) {
		if (ghrelinThreshold > .01 * time)
			ghrelinThreshold -= .01 * time;
		else
			ghrelinThreshold = .01 * time;
	}
	
	private void decreaseLeptin(double time) {
		if (leptinLevel >= time * leptinThreshold / 500.0)
			leptinLevel -= time * leptinThreshold / 500.0;
	}

	private void decreaseGhrelin(double time) {
		if (ghrelinLevel >= time * ghrelinThreshold / 500.0)
			ghrelinLevel -= time * ghrelinThreshold / 500.0;
	}

	/**
	 * Returns the surplus amount of leptin (more than half of its threshold).
	 * 
	 * @return
	 */
	public double getSurplusLeptin() {
		double value = leptinLevel - leptinThreshold/2;
		if (value > 0)
			return value;
		else
			return 0;
	}

	/**
	 * Returns the surplus amount of ghrelin (more than half of its threshold).
	 * 
	 * @return
	 */
	public double getSurplusGhrelin() {
		double value = ghrelinLevel - ghrelinThreshold/2;
		if (value > 0)
			return value;
		else
			return 0;
	}

	public boolean isAwake() {
		return awake;
	}

	public void setAwake(boolean value) {
		awake = value;
	}

	/***
	 * Eats food to regulate hormones
	 * 
	 * @param time
	 */
	public void eatFood(double time) {
		// When eating food, the level of leptin increases in order to 
		// keep you from over-eating
		increaseLeptinToThreshold(time);
		// When one is hungry, stomach is (g)rowling because ghrelin is released.
		// Ghrelin is released from the stomach when the stomach is empty
		// When eating food, the level of ghrelin decreases.
		decreaseGhrelin(time);
	}

	/**
	 * Exercises to regulate hormones
	 * 
	 * @param time
	 */
	public void exercise(double time) {
		// It's been demonstrated that concentrations of ghrelin increase following 
		// exercise, boosting endurance and influencing food intake.
		// In general, exercise increases the production of ghrelin as 
		// workouts naturally make you hungry, in order to replace lost fuel stores. 
		increaseGhrelinToThreshold(time * 0.5);
		
		// Acute exercise significantly lowers plasma ghrelin levels, with higher 
		// intensity exercise associated with greater ghrelin suppression. 
		decreaseGhrelinBounded(time);
		
		// When leptin levels are low we become hungry and when leptin levels are 
		// high we should be satisfied.
		// In a study done on rats, leptin levels decreased following four weeks of 
		// voluntary wheel running
		decreaseLeptin(time * .5);
		
		// Physically fit people have less adipose tissues and thus having lower 
		// level leptin to be released.
		decreaseLeptinBounded(time);
	
		// In future, model leptin and insulin sensivity as follows: 
		// One way leptin sensitivity can be regained is through proper exercise. 
		// Leptin and insulin communicate with one another and work collectively 
		// with other hormones to control our energy balance. As insulin levels rise 
		// so do leptin levels. Exercise has been proven to increase insulin sensitivity. 
		// Exercise increases the need for the muscle cells to replenish lost fuel. 
		// Eating a diet that is too high in sugar can lead to excessive amounts of 
		// insulin being secreted. This can lead to insulin resistance, which means 
		// we will need even more insulin. Remember, that as insulin levels rise so do 
		// leptin levels. This is how insulin resistance induces leptin resistance.
	}

	/**
	 * Sleeps to regulate hormones.
	 * 
	 * @param time
	 */
	public void setRested(double time) {
		// During sleep, levels of ghrelin decrease, 
		// because sleep requires far less energy than being awake does.
		decreaseGhrelin(time);
		
		// During sleep, leptin levels increase, telling your brain you have plenty of
		// energy for the time being and there's no need to trigger the feeling of 
		// hunger or the burning of calories.
		
		// Increase leptin to threshold
		increaseLeptinToThreshold(time);
	}

	/**
	 * When there is not enough sleep, it changes the hormones.
	 * 
	 * @param time
	 */
	private void depriveSleep(double time) {
		// The decrease in leptin brought on by sleep deprivation can result 
		// in a constant feeling of hunger and
		// a general slow-down of your metabolism.
		
		// When you don't get enough sleep, you end up with too little leptin
		// in your body, which, through a series of steps, makes your brain think
		// you don't have enough energy for your needs.
		decreaseLeptin(time *.5);
		
		// A single night of sleep deprivation increases ghrelin levels and 
		// feelings of hunger in normal-weight healthy men
		increaseGhrelinToThreshold(time *.5);
		
		// Level of ghrelin bound increase
		increaseGhrelinBounded(time * .5);
	}
	
	public double getGhrelin() {
		return ghrelinLevel;
	}

	public double getLeptin() {
		return leptinLevel;
	}

	public double getGhrelinT() {
		return ghrelinThreshold;
	}

	public double getLeptinT() {
		return leptinThreshold;
	}
	
	/**
	 * Records the sleep time
	 * 
	 * @param time in millisols
	 */
	public void recordSleep(double time) {
		sleepHistory.increaseDataPoint(time);
	}
	
	/**
	 * Returns the sleep history.
	 * 
	 * @return
	 */
	public Map<Integer, Double> getSleepHistory() {
		return sleepHistory.getHistory();
	}
	
	/**
	 * Gets today's sleep time.
	 * 
	 * @return
	 */
	public double getTodaySleepTime() {
		double time = 0;
		if (getSleepHistory().containsKey(solCache)) {
			time = getSleepHistory().get(solCache);
		}
		return time;
	}
	
	/**
	 * Records the exercise time
	 * 
	 * @param time in millisols
	 */
	public void recordExercise(double time) {
		exerciseHistory.increaseDataPoint(time);
	}
	
	/**
	 * Returns the exercise history.
	 * 
	 * @return
	 */
	public Map<Integer, Double> getExerciseHistory() {
		return exerciseHistory.getHistory();
	}
	
	/**
	 * Gets today's exercise time.
	 * 
	 * @return
	 */
	public double getTodayExerciseTime() {
		double time = 0;
		if (getExerciseHistory().containsKey(solCache)) {
			time = getExerciseHistory().get(solCache);
		}
		return time;
	}
	
	/**
	 * Prepares object for garbage collection.
	 */
	public void destroy() {
		// condition = null;
		sleepCycleMap.clear();
		sleepCycleMap = null;

	}

}
