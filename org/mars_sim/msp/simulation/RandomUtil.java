/**
 * Mars Simulation Project
 * RandomUtil.java
 * @version 2.78 2005-04-17
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation;

import java.util.Iterator;
import java.util.Map;

/** The RandomUtil class is a library of various random-related
 *  methods
 */
public final class RandomUtil {

    /** Returns true if given number is less than a random percentage. 
     *  @param the random percentage limit
     *  @return true if random percent is less than percentage limit
     */
    public static boolean lessThanRandPercent(int randomLimit) {
        int rand = (int) Math.round(Math.random() * 100 + 1);
        if (rand < randomLimit) return true;
        else return false;
    }

    /** Returns true if given number is less than a random percentage. 
     *  @param the random percentage limit
     *  @return true if random percent is less than percentage limit
     */
    public static boolean lessThanRandPercent(double randomLimit) {
        double rand = Math.random() * 100D;
        if (rand < randomLimit) return true;
        else return false;
    }

    /** Returns a random int number from 0 to (and including) the
     *  number given. 
     *  @param ceiling the int limit for the random number
     *  @return the random number
     */
    public static int getRandomInt(int ceiling) {
        return (int) Math.round(Math.random() * ceiling);
    }
    
    /** Returns a random int number from a given base number
     *  to (and including) the ceiling number given.
     *  @param base the minimum number result 
     *  @param ceiling the maximum number result 
     *  @return the random number
     */
    public static int getRandomInt(int base, int ceiling) {
        return (int) Math.round(Math.random() * (ceiling - base)) + base;
    }

    /** Returns a random double number from 0 
     *  to the ceiling number given.
     *  @param ceiling the maximum number result 
     *  @return the random number
     */
    public static double getRandomDouble(double ceiling) {
        return Math.random() * ceiling;
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
     * Gets a random weighted object from a map.
     * @param weightedMap a map of objects and their weights as Double values.
     * @return randomly selected object from the list (or null if empty map).
     */
    public static Object getWeightedRandomObject(Map weightedMap) {
    	if (weightedMap == null) throw new IllegalArgumentException("weightedMap is null");
    
    	Object result = null;
    	
    	// Get the total weight of all the objects in the map.
    	double totalWeight = 0D;
    	Iterator i = weightedMap.values().iterator();
    	while (i.hasNext()) totalWeight += ((Double) i.next()).doubleValue();
    	
    	// Randomly select a weight value.
    	double randWeight = getRandomDouble(totalWeight);
    	
    	// Determine which object the weight applies to.
    	Iterator j = weightedMap.keySet().iterator();
    	while (j.hasNext()) {
    		Object key = j.next();
    		double weight = ((Double) weightedMap.get(key)).doubleValue();
    		if (randWeight <= weight) {
    			result = key;
    			break;
    		}
    		else randWeight -= weight;
    	}
    	
    	return result;
    }
}