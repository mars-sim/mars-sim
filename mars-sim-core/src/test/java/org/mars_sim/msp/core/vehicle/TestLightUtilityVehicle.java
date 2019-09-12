package org.mars_sim.msp.core.vehicle;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;

import junit.framework.TestCase;

public class TestLightUtilityVehicle extends TestCase {
	 public void testLUV() {
        SimulationConfig.instance().loadConfig();
        Simulation.instance().testRun();
        
//        LightUtilityVehicle vehicle = new LightUtilityVehicle("Subaru", "Light Utility Vehicle", new MockSettlement());
//        int crewCapacity = vehicle.getCrewCapacity();
//        int slots = vehicle.getAtachmentSlotNumber();
//
//        assertEquals("Wrong crew capacity", 1, crewCapacity);
//        assertEquals("Wrong slot number", 2, slots);
	}
}
