/**
 * Mars Simulation Project
 * RandomUtil.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */
package org.mars_sim.msp.core;

import java.util.Iterator;
import java.util.Map;

/**
 * The RandomUtil class is a library of various random-related
 * methods.
 */
public final class RandomUtil {

	// Random generator.
	//	private final static Random random = new Random();
	/**
	 * MersenneTwisterFast provides a fast, much "more" random than the linear congruential
	 * of the java.util.Random 
	 */
	private final static MersenneTwisterFast random = new MersenneTwisterFast();

	/**
	 * Returns true if given number is less than a random percentage. 
	 * @param randomLimit the random percentage limit
	 * @return true if random percent is less than percentage limit
	 */
	public static boolean lessThanRandPercent(int randomLimit) {
		int rand = random.nextInt(100) + 1;
		return rand < randomLimit;
	}

	/**
	 * Returns true if given number is less than a random percentage. 
	 * @param randomLimit the random percentage limit
	 * @return true if random percent is less than percentage limit
	 */
	public static boolean lessThanRandPercent(double randomLimit) {
		double rand = random.nextDouble() * 100D;
		return rand < randomLimit;
	}

	/**
	 * Returns a random int number from 0 to (and including) the
	 * number given. 
	 * @param ceiling the int limit for the random number
	 * @return the random number
	 */
	public static int getRandomInt(int ceiling) {
		if (ceiling < 0) throw new IllegalArgumentException(Msg.getString("RandomUtil.log.ceilingMustBePositive") + ceiling); //$NON-NLS-1$
		return random.nextInt(ceiling + 1);
	}

	/**Returns a random int number from a given base number
	 * to (and including) the ceiling number given.
	 * @param base the minimum number result 
	 * @param ceiling the maximum number result 
	 * @return the random number
	 */
	public static int getRandomInt(int base, int ceiling) {
		if (ceiling < base) throw new IllegalArgumentException(Msg.getString("RandomUtil.log.ceilingMustGreaterBase")); //$NON-NLS-1$
		return random.nextInt(ceiling - base + 1) + base;
	}

	/**Returns a random double number from 0 
	 * to the ceiling number given.
	 * @param ceiling the maximum number result 
	 * @return the random number
	 */
	public static double getRandomDouble(double ceiling) {
		return random.nextDouble() * ceiling;
	}

	/** 
	 * Returns a random integer from 1 to the given integer.
	 * -breakiterator
	 * 1 has twice the chance of being chosen as 2 and so forth
	 * to the given integer.
	 * @param ceiling the maximum integer result, ( ceiling > 0 )
	 * @return the random integer
	 */
	public static int getRandomRegressionInteger(int ceiling) {

		double totalWeight = 0D;
		double weight = 1D;

		for (int x=0; x < ceiling; x++) {
			totalWeight += weight;
			weight /= 2D;
		}

		double randWeight = getRandomDouble(totalWeight);

		totalWeight = 0D;
		weight = 1D;
		int result = 0; 
		for (int x=0; x < ceiling; x++) {
			totalWeight += weight;
			weight /= 2D;
			if (randWeight < totalWeight) {
				result = x + 1;
				break;
			}
		}

		return result;
	}

	/**
	 * Gets the average value returned from the getRandomRegressionInteger method.
	 * @param ceiling the maximum integer result, (ceiling > 0)
	 * @return average value.
	 */
	public static double getRandomRegressionIntegerAverageValue(int ceiling) {

		double totalProbability = 0D;
		double totalValue = 0D;

		double probability = 1D;
		for (int x = 0; x < ceiling; x++) {
			totalValue += (x + 1) * probability;
			totalProbability += probability;
			probability /= 2D;
		}

		return totalValue / totalProbability;
	}

	/**
	 * Gets a random weighted object from a map.
	 * @param weightedMap a map of objects and their weights as Double values.
	 * @return randomly selected object from the list (or null if empty map).
	 */
	public static <T extends Object> T getWeightedRandomObject(Map<T, Double> weightedMap) {
		if (weightedMap == null) throw new IllegalArgumentException(Msg.getString("RandomUtil.log.weightMapIsNull")); //$NON-NLS-1$

		T result = null;

		// Get the total weight of all the objects in the map.
		double totalWeight = 0D;
		Iterator<Double> i = weightedMap.values().iterator();
		while (i.hasNext()) {
			totalWeight += i.next();
		}

		// Randomly select a weight value.
		double randWeight = getRandomDouble(totalWeight);

		// Determine which object the weight applies to.
		Iterator<T> j = weightedMap.keySet().iterator();
		while (j.hasNext()) {
			T key = j.next();
			double weight = weightedMap.get(key);
			if (randWeight <= weight) {
				result = key;
				break;
			}
			else randWeight -= weight;
		}

		return result;
	}
}