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

	
//		assertEquals("Location state type", lon, source.getLocationStateType());
//		assertTrue(msg + ": InSettlement", source.isInSettlement());
//		assertFalse(msg + ": IsOutside", source.isOutside());
//		assertNull(msg + ": Vehicle", source.getVehicle());
	}
    

    public void testGaussianRandom() {
    	List<Double> list = new ArrayList<>();
		for (int i=0; i<1000; i++) {
			double rand = RandomUtil.getGaussianDouble();
			list.add(rand);
		}

		SortedMap<Integer, Integer> map = new TreeMap<>();
		
		int total = 0;
		int onePosVariance = 1;
		int oneNegVariance = -1;	
		int twoPosVariance = 2;
		int twoNegVariance = -2;
		int threeMoreVariance = 3;
		int threeLessVariance = -3;
		
		for (double d: list) {
			total++;
			if (d >= 0) {
				if (d < 1) {
					if (map.containsKey(1)) {
						int num = map.get(1);
						map.put(1, num + 1);
					}
					else {
						map.put(1, 1);
					}
					onePosVariance++;
				}
				else if (d < 2) {
					if (map.containsKey(2)) {
						int num = map.get(2);
						map.put(2, num + 1);
					}
					else {
						map.put(2, 1);
					}
					twoPosVariance++;
				}
				else {
					if (map.containsKey(3)) {
						int num = map.get(3);
						map.put(3, num + 1);
					}
					else {
						map.put(3, 1);
					}
					threeMoreVariance++;
				}
			}
			else {
				if (d > -1) {
					if (map.containsKey(-1)) {
						int num = map.get(-1);
						map.put(-1, num + 1);
					}
					else {
						map.put(-1, 1);
					}
					oneNegVariance++;
				}
				else if (d > -2) {
					if (map.containsKey(-2)) {
						int num = map.get(-2);
						map.put(-2, num + 1);
					}
					else {
						map.put(-2, 1);
					}
					twoNegVariance++;
				}
				else {
					if (map.containsKey(-3)) {
						int num = map.get(-3);
						map.put(-3, num + 1);
					}
					else {
						map.put(-3, 1);
					}
					threeLessVariance++;
				}
			}
		}
		
		// This should be converted into Asserts not printed out
		// System.out.println();
		// System.out.println("GAUSSIAN DISTRIBUTION");
		// System.out.println("threeLessVariance: " + threeLessVariance);
		// System.out.println("twoNegVariance: " + twoNegVariance);
		// System.out.println("oneNegVariance: " + oneNegVariance);
		// System.out.println("onePosVariance: " + onePosVariance);
		// System.out.println("twoPosVariance: " + twoPosVariance);
		// System.out.println("threeMoreVariance: " + threeMoreVariance);
		// System.out.println("total: " + total);
		
		// System.out.println(map);
    }
       
}