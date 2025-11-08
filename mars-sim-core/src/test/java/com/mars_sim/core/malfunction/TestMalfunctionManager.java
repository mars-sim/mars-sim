//package com.mars_sim.core.malfunction;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//
//import java.util.HashMap;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import com.mars_sim.core.Coordinates;
//import com.mars_sim.core.Simulation;
//import com.mars_sim.core.SimulationConfig;
//import com.mars_sim.core.equipment.EVASuit;
//import com.mars_sim.core.person.health.ComplaintType;
//
//public class TestMalfunctionManager {
//    @BeforeEach
//    public void setUp() throws Exception {
//        SimulationConfig.loadConfig();
//        Simulation.instance().testRun();
//    }
//
//    @Test
//    void testGetEVAMalfunctions() throws Exception {
//		EVASuit suit = new EVASuit(new Coordinates(0D, 0D)); //(EVASuit) EquipmentFactory.createEquipment(EVASuit.TYPE, new Coordinates(0D, 0D), false);
//		MalfunctionManager manager = suit.getMalfunctionManager();
////		Malfunction malfunction1 = new MockMalfunction("test malfunction1", 10, 0D, 100D, 50D);
////		Malfunction malfunction2 = new MockMalfunction("test malfunction2", 50, 0D, 100D, 50D);
////		Malfunction malfunction3 = new MockMalfunction("test malfunction3", 90, 0D, 100D, 50D);
////		manager.addMalfunction(malfunction1, false, null);
////		manager.addMalfunction(malfunction2, false, null);
////		manager.addMalfunction(malfunction3, false, null);
////		List<Malfunction> sorted = manager.getEVAMalfunctions();
////		assertEquals("Size of sorted malfunctions is correct.", 3, sorted.size(), 0D);
////		assertEquals("First malfunction is malfunction3", malfunction3, sorted.get(0));
////		assertEquals("Second malfunction is malfunction2", malfunction2, sorted.get(1));
////		assertEquals("Third malfunction is malfunction1", malfunction1, sorted.get(2));
//	}
//	
//    @SuppressWarnings("serial")
//	private class MockMalfunction extends Malfunction {
//		
//		private MockMalfunction(String name, int severity, double emergencyWorkTime, double workTime, 
//				double evaWorkTime) {
//			super(name, 0, severity, 0D, emergencyWorkTime, workTime, evaWorkTime, null, null, null, 
//					new HashMap<ComplaintType, Double>());
//		}
//	}
//}
