package com.mars_sim.core.tool;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RandomUtilTest {
  
    @Test
    void testGaussianPositive() {
    	for (int i=0; i< 100; i++) {
    		double rand = RandomUtil.getGaussianPositive(100, 5);
            assertTrue(rand >= 0, "Positive");
    	}
    }
    
    @Test
    void testRandomDouble() {
    	for (int i=0; i< 100; i++) {
    		double rand = RandomUtil.getRandomDouble(1);
            assertTrue(rand >= 0 && rand <= 1, "Positive");
    	}
    }
    
    @Test
    void testRandomInt() {
    	for (int i=0; i< 100; i++) {
    		int rand = RandomUtil.getRandomInt(2, 10);
            assertTrue(rand >= 2 && rand <= 10, "Within range");
    	}
    }
}
