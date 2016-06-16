package org.mars_sim.msp.core.malfunction;

import java.util.HashMap;
import java.util.List;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.equipment.EquipmentFactory;
import org.mars_sim.msp.core.person.medical.ComplaintType;

import junit.framework.TestCase;

public class TestMalfunctionManager extends TestCase {
    @Override
    public void setUp() throws Exception {
        SimulationConfig.loadConfig();
    }

    public void testGetEVAMalfunctions() throws Exception {
		EVASuit suit = (EVASuit) EquipmentFactory.getEquipment(EVASuit.TYPE, new Coordinates(0D, 0D), false);
		MalfunctionManager manager = suit.getMalfunctionManager();
		Malfunction malfunction1 = new MockMalfunction("test malfunction1", 10, 0D, 100D, 50D);
		Malfunction malfunction2 = new MockMalfunction("test malfunction2", 50, 0D, 100D, 50D);
		Malfunction malfunction3 = new MockMalfunction("test malfunction3", 90, 0D, 100D, 50D);
		manager.addMalfunction(malfunction1);
		manager.addMalfunction(malfunction2);
		manager.addMalfunction(malfunction3);
		List<Malfunction> sorted = manager.getEVAMalfunctions();
		assertEquals("Size of sorted malfunctions is correct.", 3, sorted.size(), 0D);
		assertEquals("First malfunction is malfunction3", malfunction3, sorted.get(0));
		assertEquals("Second malfunction is malfunction2", malfunction2, sorted.get(1));
		assertEquals("Third malfunction is malfunction1", malfunction1, sorted.get(2));
	}
	
	private class MockMalfunction extends Malfunction {
		
		private MockMalfunction(String name, int severity, double emergencyWorkTime, double workTime, 
				double evaWorkTime) {
			super(name, severity, 0D, emergencyWorkTime, workTime, evaWorkTime, null, null, null, 
					new HashMap<ComplaintType, Double>());
		}
	}
}