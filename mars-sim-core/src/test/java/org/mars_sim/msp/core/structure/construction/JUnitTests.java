package org.mars_sim.msp.core.structure.construction;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class JUnitTests extends TestCase {

    /**
     * Collection of external test suites to be included in current testing.
     */
    public static Test suite() {
        TestSuite suite = new TestSuite();
        
        suite.addTestSuite(ConstructionManagerTest.class);
//        suite.addTestSuite(ConstructionSiteTest.class);
        suite.addTestSuite(ConstructionStageInfoTest.class);
//        suite.addTestSuite(ConstructionStageTest.class);
        suite.addTestSuite(ConstructionVehicleTypeTest.class);
        
        return suite;
    }
}