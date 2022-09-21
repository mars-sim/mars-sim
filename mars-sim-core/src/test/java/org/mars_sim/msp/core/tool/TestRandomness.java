/*
 * Mars Simulation Project
 * TestRandomness.java
 * @date 2022-09-20
 * @author Manny Kung
 */

package org.mars_sim.msp.core.tool;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import junit.framework.TestCase;

public class TestRandomness
extends TestCase {

    public TestRandomness() {
		super();
	}
    
    protected void setUp() throws Exception {
		// nothing
    }

    public void testDoubleRandom() {
		List<Double> list = new ArrayList<>();
		for (int i=0; i<1000; i++) {
			double rand = RandomUtil.getRandomDouble(100.0);
			list.add(rand);
		}

		SortedMap<Integer, Integer> map = new TreeMap<>();
		
		int total = 0;
		int lessThanOne = 0;
		
		for (double d: list) {
			for (int j = 0; j <= 90; j = j + 10) {
//				if (d > 0 && d <= 1) {
//					if (map.containsKey(1)) {
//						int num = map.get(1);
//						map.put(1, num + 1);
//					}
//					else {
//						map.put(1, 1);
//					}
//					lessThanOne++;
//					total++;
//					continue;
//				}
				
				if (d > j && d <= j + 10) {
					
					if (d > 0 && d <= 1) {
						if (map.containsKey(1)) {
							int num = map.get(1);
							map.put(1, num + 1);
						}
						else {
							map.put(1, 1);
						}
						lessThanOne++;
						total++;
						continue;
					}
					else {
					
						if (map.containsKey(j + 10)) {
							int num = map.get(j + 10);
							map.put(j + 10, num + 1);
						}
						else {
							map.put(j + 10, 1);
						}	
						total++;
					}
				}
			}
		}
		System.out.println("lessThanOne: " + lessThanOne);
		System.out.println("total: " + total);
		
		System.out.println(map);
		
		
//		assertEquals("Location state type", lon, source.getLocationStateType());
//		assertTrue(msg + ": InSettlement", source.isInSettlement());
//		assertFalse(msg + ": IsOutside", source.isOutside());
//		assertNull(msg + ": Vehicle", source.getVehicle());

	}
}