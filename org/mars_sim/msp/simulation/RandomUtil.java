/**
 * Mars Simulation Project
 * RandomUtil.java
 * @version 2.72 2001-05-31
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

    /** Returns a random integer number from 0 to (and including) the
     *  number given. 
     *  @param ceiling the integer limit for the random number
     *  @return the random number
     */
    public static int getRandomInteger(int ceiling) {
        return (int) Math.round(Math.random() * ceiling);
    }
}

