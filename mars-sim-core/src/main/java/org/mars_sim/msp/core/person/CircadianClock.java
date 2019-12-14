/**
 * Mars Simulation Project
 * CircadianClock.java
 * @version 3.1.0 2017-09-02
 * @author Manny Kung
 */

package org.mars_sim.msp.core.person;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.mars_sim.msp.core.LifeSupportInterface;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.time.MarsClock;

/**
 * The CircadianClock class simulates the circadian rhythm of a person. It
 * regulates the eat/sleep cycle.
 */
public class CircadianClock implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
//	private static Logger logger = Logger.getLogger(CircadianClock.class.getName());

//	private static String sourceName = logger.getName().substring(logger.getName().lastIndexOf(".") + 1,
//			logger.getName().length());

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
	// Leptin, aka the safety hormone, is a hormone, made by fat cells, that
	// decreases our appetite.

	// Ghrelin is a hormone that increases appetite, and makes us wanting to eat. It
	// also plays a role in body weight.

	// From http://www.webmd.com/diet/obesity/features/the-facts-on-leptin-faq#1 :
	// When your leptin level is above that threshold, your brain senses that you
	// have energy sufficiency, meaning
	// (1) you can burn energy at a normal rate,
	// (2) you eat food at a normal amount,
	// (3) you engage in exercise at a normal rate.

	// Leptin sends signals to our brains that we are satisfied with our sleep and
	// we lose interest in food and sleep.

	/** The current leptin level */
	private double leptin_level = 400;
	/** The leptin threshold of each person is tuned by genetics. */
	private double leptin_threshold = 400;

	// Ghrelin, the appetite/sleep increaser, is released primarily in the stomach
	// and is thought to signal hunger to
	// the brain.

	// You'd expect the body to increase ghrelin if a person is under-eating and
	// decrease it if one is over-eating.

	/** The current ghrelin level */
	private double ghrelin_level = 400;
	/** The ghrelin threshold of each person is tuned by genetics. */
	private double ghrelin_threshold = 400;

	private Person person;

	/** Sleep habit map keeps track of the sleep cycle */
	private Map<Integer, Integer> sleepCycleMap;

	/** The amount of Sleep [millisols] a person on each mission sol */
	private Map<Integer, Double> sleepTime;

	private static MarsClock marsClock;

	static {
		if (marsClock == null)
			marsClock = Simulation.instance().getMasterClock().getMarsClock();
	}
	
	public CircadianClock(Person person) {
		this.person = person;

		sleepCycleMap = new HashMap<>();
		sleepTime = new HashMap<>();
		
	}
	
	public void timePassing(double time, LifeSupportInterface support) {

		int solElapsed = marsClock.getMissionSol();

		if (solCache != solElapsed) {

			// Reset numSleep back to zero at the beginning of each sol
			numSleep = 0;
			suppressHabit = 0;

			if (solCache == 0) {
				double dev = Math.sqrt(
						person.getBaseMass() / Person.getAverageWeight() * person.getHeight() / Person.getAverageHeight()); 
				// condition.getBodyMassDeviation();
				// person.getBaseMass()
				// Person.AVERAGE_WEIGHT;
				double age = person.updateAge();

				// Leptin threshold, the appetite/sleep suppressor, are lower when you're thin
				// and higher when you're fat.
				leptin_threshold *= dev;

				// But many obese people have built up a resistance to the appetite-suppressing
				// effects of leptin.
				// TODO: how to code this resistance ?

				// Ghrelin levels have been found to increase in children with anorexia nervosa
				// and decrease in children who are obese.
				if (age <= 21)
					ghrelin_threshold /= dev;

				// LogConsolidated.log(logger, Level.INFO, 2000, sourceName, person
				// + " LL " + Math.round(leptin_level*10.0)/10.0
				// + " GL " + Math.round(ghrelin_level*10.0)/10.0
				// + " LT " + Math.round(leptin_threshold*10.0)/10.0
				// + " GT " + Math.round(ghrelin_threshold*10.0)/10.0
				// , null);

			}

			solCache = solElapsed;
			
		}

		// People who don't sleep enough end up with too much ghrelin in their system,
		// so the body thinks it's hungry
		// and it needs more calories, and it stops burning those calories because it
		// thinks there's a shortage.
		if (awake) {
			stayAwake(time);
		}

		// int month = marsClock.getSolOfMonth();
		// if (month == 1) {
		// check if the person always have a lot of energy
		// if yes, increase weight
		// }

	}
	

	/**
	 * Gets the key of the Sleep Cycle Map with the highest weight
	 * 
	 * @return int[] the two best times in integer
	 */
	public int[] getPreferredSleepHours() {
		int largest[] = { 0, 0 };
		// Iterator<Integer> i = sleepCycleMap.keySet().iterator();
		// while (i.hasNext()) {
		for (int key : sleepCycleMap.keySet()) {// int key = i.next();
			int value = sleepCycleMap.get(key);
			if (value > largest[0]) {
				largest[1] = largest[0];
				largest[0] = key;
			} else if (value > largest[1])
				largest[1] = key;

		}

		return largest;
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
	 * Gets the person with this circadian clock
	 * 
	 * @return
	 */
	public Person getPerson() {
		return person;
	}

	// During sleep, leptin levels increase, telling your brain you have plenty of
	// energy for the time being and
	// there's no need to trigger the feeling of hunger or the burning of calories.
	// When you don't get enough sleep,
	// you end up with too little leptin in your body, which, through a series of
	// steps, makes your brain think
	// you don't have enough energy for your needs.

	public void increaseLeptinToThreshold(double time) {
		// if (leptin_level >= 1000)
		// leptin_level = 1000;
		// else
		if (leptin_level + time >= leptin_threshold)
			leptin_level = leptin_threshold;
		else
			leptin_level += time;

		// LogConsolidated.log(logger, Level.INFO, 1000, sourceName, person
		// + " LL " + Math.round(leptin_level*10.0)/10.0
		// //+ " G " + Math.round(ghrelin_level*10.0)/10.0
		// , null);
	}

	public void increaseLeptinBounded(double time) {
		// if (leptin_level >= 1000)
		// leptin_level = 1000;
		// else
		if (leptin_level + time >= 1000D)
			leptin_level = 1000D;
		else
			leptin_level += time;

		// LogConsolidated.log(logger, Level.INFO, 1000, sourceName, person
		// + " LL " + Math.round(leptin_level*10.0)/10.0
		// //+ " G " + Math.round(ghrelin_level*10.0)/10.0
		// , null);
	}

	public void increaseGhrelinToThreshold(double time) {
		// if (ghrelin_level >= 1000)
		// ghrelin_level = 1000;
		// else
		if (ghrelin_level + time >= ghrelin_threshold)
			ghrelin_level = ghrelin_threshold;
		else
			ghrelin_level += time;

		// LogConsolidated.log(logger, Level.INFO, 1000, sourceName, person
		// //+ " L " + Math.round(leptin_level*10.0)/10.0
		// + " GL " + Math.round(ghrelin_level*10.0)/10.0
		// , null);
	}

	public void increaseGhrelinBounded(double time) {
		// if (ghrelin_level >= 1000)
		// ghrelin_level = 1000;
		// else
		if (ghrelin_level + time >= 1000D)
			ghrelin_level = 1000D;
		else
			ghrelin_level += time;

		// LogConsolidated.log(logger, Level.INFO, 1000, sourceName, person
		// //+ " L " + Math.round(leptin_level*10.0)/10.0
		// + " GL " + Math.round(ghrelin_level*10.0)/10.0
		// , null);
	}

	public void decreaseLeptin(double time) {
		if (leptin_level >= time)
			leptin_level -= time;

		// LogConsolidated.log(logger, Level.INFO, 1000, sourceName, person
		// + " LL " + Math.round(leptin_level*10.0)/10.0
		// //+ " G " + Math.round(ghrelin_level*10.0)/10.0
		// , null);
	}

	public void decreaseGhrelin(double time) {
		if (ghrelin_level >= time)
			ghrelin_level -= time;

		// LogConsolidated.log(logger, Level.INFO, 1000, sourceName, person
		// //+ " L " + Math.round(leptin_level*10.0)/10.0
		// + " GL " + Math.round(ghrelin_level*10.0)/10.0
		// , null);
	}

	public double getSurplusLeptin() {
		double value = leptin_level - leptin_threshold;
		if (value > 0)
			return value;
		else
			return 0;
	}

	public double getSurplusGhrelin() {
		double value = ghrelin_level - ghrelin_threshold;
		if (value > 0)
			return value;
		else
			return 0;
	}

	public boolean isAwake() {
		return awake;
	}

	public void setAwake(boolean value) {
		if (awake != value)
			awake = value;
	}

	/***
	 * Eats food to regulate hormones
	 * 
	 * @param time
	 */
	public void eatFood(double time) {
		increaseLeptinToThreshold(time);
		decreaseGhrelin(time);
	}

	/***
	 * Exercises to regulate hormones
	 * 
	 * @param time
	 */
	public void exercise(double time) {
		increaseLeptinToThreshold(time);
		decreaseGhrelin(time);
	}

	/***
	 * Sleeps to regulate hormones
	 * 
	 * @param time
	 */
	// During sleep, levels of ghrelin decrease, because sleep requires far less
	// energy than being awake does.
	// Level of leptin increases
	public void getRested(double time) {
		increaseLeptinToThreshold(time);
		decreaseGhrelin(time);
	}

	/***
	 * Stays awake and change hormones
	 * 
	 * @param time
	 */
	// The decrease in leptin brought on by sleep deprivation can result in a
	// constant feeling of hunger and
	// a general slow-down of your metabolism.
	public void stayAwake(double time) {
		decreaseLeptin(time);
		increaseGhrelinBounded(time);
	}

	public double getGhrelin() {
		return ghrelin_level;
	}

	public double getLeptin() {
		return leptin_level;
	}

	/**
	 * Records the sleep time
	 * 
	 * @param time in millisols
	 */
	public void recordSleep(double time) {
		int today = marsClock.getMissionSol();
		if (sleepTime.containsKey(today)) {
			double oldTime = sleepTime.get(today);
			double newTime = oldTime + time;
			sleepTime.put(today, newTime);
		}
		else {
			sleepTime.put(today, time);
		}
	}
	
	public Map<Integer, Double> getSleepTime() {
		return sleepTime;
	}
	
	/**
	 * Reloads instances after loading from a saved sim
	 * 
	 * @param clock
	 */
	public static void initializeInstances(MarsClock clock) {
		marsClock = clock;
	}
	
	/**
	 * Prepares object for garbage collection.
	 */
	public void destroy() {
		person = null;
		// personConfig = null;
		marsClock = null;
		// condition = null;
		sleepCycleMap.clear();
		sleepCycleMap = null;
		
		sleepTime.clear();
		sleepTime = null;

	}

}
