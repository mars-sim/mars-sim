/**
 * Mars Simulation Project
 * RandomUtil.java
 * @version 2.71 2000-09-25
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation;

/** The RandomUtil class is a library of various random-related
 *  methods
 */
final class RandomUtil {

    /** Returns true if given number is less than a random percentage. */
    static boolean lessThanRandPercent(int randomLimit) {
        int rand = (int) Math.round(Math.random() * 100 + 1);
        if (rand < randomLimit) return true;
        else return false;
    }

    /** Returns a random integer number from 0 to (and including) the
      *  number given. */
    static int getRandomInteger(int ceiling) {
        return (int) Math.round(Math.random() * ceiling);
    }
}

