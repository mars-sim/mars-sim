/**
 * Mars Simulation Project
 * RandomUtil.java
 * @version 2.70 2000-02-20
 * @author Scott Davis
 */

/** The RandomUtil class is a library of various random-related
 *  methods
 */
public final class RandomUtil {

    /** Returns true if given number is less than a random percentage. */
    public static boolean lessThanRandPercent(int randomLimit) {
	int rand = (int) Math.round(Math.random() * 100 + 1);
	if (rand < randomLimit) return true;
	else return false;
    }
	
    /** Returns a random integer number from 0 to (and including) the
     *  number given. */
    public static int getRandomInteger(int ceiling) {
	return (int) Math.round(Math.random() * ceiling);
    }
}
