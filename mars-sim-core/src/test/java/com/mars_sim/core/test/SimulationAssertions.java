package com.mars_sim.core.test;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Custom assertion methods for Mars Simulation unit tests.
 * These methods provide additional assertion capabilities beyond standard JUnit assertions.
 */
public class SimulationAssertions {

	/**
	 * Asserts that the actual value is greater than the minimum value.
	 * 
	 * @param message The message to display if the assertion fails
	 * @param minValue The minimum value (exclusive)
	 * @param actual The actual value to test
	 */
	public static void assertGreaterThan(String message, double minValue, double actual) {
		if (actual <= minValue) {
			fail(message + " ==> " +
					"Expected: a value greater than <" + minValue + "> " +
					"Actual was <" + actual + ">");
		}
	}

	/**
	 * Asserts that the actual value is less than the maximum value.
	 * 
	 * @param message The message to display if the assertion fails
	 * @param maxValue The maximum value (exclusive)
	 * @param actual The actual value to test
	 */
	public static void assertLessThan(String message, double maxValue, double actual) {
		if (actual >= maxValue) {
			fail(message + " ==> " +
					"Expected: a value less than <" + maxValue + "> " +
					"Actual was <" + actual + ">");
		}
	}
	
	/**
	 * Asserts that the actual value is less than or equal to the maximum value.
	 * 
	 * @param message The message to display if the assertion fails
	 * @param maxValue The maximum value (inclusive)
	 * @param actual The actual value to test
	 */
	public static void assertEqualLessThan(String message, double maxValue, double actual) {
		if (actual > maxValue) {
			fail(message + " ==> " +
					"Expected: a value less than <" + maxValue + ">\n" +
					"Actual was <" + actual + ">");
		}
	}
	
	// Private constructor to prevent instantiation
	private SimulationAssertions() {
		throw new AssertionError("Utility class should not be instantiated");
	}
}
