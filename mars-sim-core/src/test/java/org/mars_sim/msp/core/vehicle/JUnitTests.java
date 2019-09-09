package org.mars_sim.msp.core.vehicle;

import junit.textui.TestRunner;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.structure.MockSettlement;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

// include any ONE of the following...
//import junit.textui.TestRunner;
//import junit.awtui.TestRunner;

/**
 * JUnit test suite
 */
public class JUnitTests extends TestCase {

    private static final Class<JUnitTests> thisClass = JUnitTests.class;

    // Get test properties.
    private static final java.util.Properties properties =
            System.getProperties();

    {
        try {
            properties.load(
                    new java.io.BufferedInputStream(
                            new java.io.FileInputStream(
                                    thisClass.getName() + ".properties")));
        } catch (java.io.IOException e) {
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
     * Any others beginning with "test..." will be automatically included as well.
     */
    public void testNothing() {
    }
}
