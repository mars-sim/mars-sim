package org.mars_sim.msp.simulation.person;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.swingui.TestRunner;

import org.mars_sim.msp.simulation.Coordinates;
import org.mars_sim.msp.simulation.Mars;
import org.mars_sim.msp.simulation.SimulationProperties;
import org.mars_sim.msp.simulation.structure.Settlement;

/**
 * JUnit test suite
 */
public class JUnitTests extends TestCase {

	private static final Class thisClass = JUnitTests.class;

	// Get test properites.
	private static final java.util.Properties properties =
		System.getProperties();
	{
		try {
			properties.load(
				new java.io.BufferedInputStream(
					new java.io.FileInputStream(
						thisClass.getName() + ".properties")));
		}
		catch (java.io.IOException e) {
			// do nothing
		}
	}

	/**
	 * Any initialization necessary for all tests.
	 */
	public JUnitTests() {
		super();
	}

	/**
	 * Run all JUnit tests.
	 */
	public static void main(String[] args) {
		TestRunner.run(thisClass);
	}

	/**
	 * Collection of external test suites to be included in current testing.
	 */
	public static Test suite() {
		return new TestSuite(thisClass);
	}

	/**
	 * Every JUnit test suite needs at least one test.This one obviously does nothing.
	 * Any others begining with "test..." will be automatically included as well.
	 */
	public void testNothing() {
	}

	public void testCreatePerson1() throws Exception {

		Mars mars = new Mars(new SimulationProperties());
		String personName = "Person Name";
		String settlementName = "Settlement Name";
		Coordinates coords = new Coordinates(0, 0);
		Settlement settlement = new Settlement(settlementName, coords, mars);

		Person person = new Person(personName, settlement, mars);
		
		assertEquals("Name: ", personName, person.getName());
		assertEquals("Coords: ", coords, person.getCoordinates());
	}
}
