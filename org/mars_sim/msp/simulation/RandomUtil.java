/**
 * Mars Simulation Project
 * RandomUtil.java
 * @version 2.72 2001-07-08
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation;

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
        double rand = (Math.random() * 100D) + 1D;
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
}

