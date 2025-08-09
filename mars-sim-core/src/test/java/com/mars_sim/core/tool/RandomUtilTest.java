package com.mars_sim.core.tool;

import static org.junit.Assert.assertTrue;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.sampling.distribution.PoissonSampler;
import org.apache.commons.rng.simple.RandomSource;
import org.apache.commons.rng.simple.ThreadLocalRandomSource;
import org.junit.jupiter.api.Test;

class RandomUtilTest {
  
    @Test
    void testGaussianPositive() {
    	for (int i=0; i< 100; i++) {
    		double rand = RandomUtil.getGaussianPositive(100, 5);
//    		System.out.println(rand + " ");
            assertTrue("Positive", rand >= 0);
    	}
    }
    
//    @Test
//    void testGaussianSampler() {
//    	for (int i=0; i< 100; i++) {
//	    	double rand = RandomUtil.getGaussian(0, 1);
//	    	System.out.println(rand);
//    	}
//    }
    
    @Test
    void testRandomDouble() {
    	for (int i=0; i< 100; i++) {
    		double rand = RandomUtil.getRandomDouble(1);
//    		System.out.println(rand + " ");
            assertTrue("Positive", rand >= 0 && rand <= 1);
    	}
    }
    
    @Test
    void testRandomInt() {
    	for (int i=0; i< 100; i++) {
    		int rand = RandomUtil.getRandomInt(2, 10);
//    		System.out.println(rand + " ");
            assertTrue("Within range", rand >= 2 && rand <= 10);
    	}
    }
    
    @Test
    void testNormalDist() {
    	NormalDistribution normal = new NormalDistribution(10, 1);
    	
    	double prob = normal.probability(8, 9);
    	System.out.println("Between 8 and 9: " + prob); 
//    	assertTrue("Within probability range", prob >= .1 && prob <= .2);
    	
    	prob = normal.probability(9, 10);
    	System.out.println("Between 9 and 10: " + prob); 
//    	assertTrue("Within probability range", prob >= .3 && prob <= .45);
    	
    	prob = normal.probability(10, 11);
    	System.out.println("Between 10 and 11: " + prob); 
//    	assertTrue("Within probability range", prob >= .3 && prob <= .45);
    	
    	prob = normal.probability(11, 12);
    	System.out.println("Between 11 and 12: " + prob); 
//    	assertTrue("Within probability range", prob >= .1 && prob <= .2);
    	   	
    	double var = normal.getNumericalVariance();
    	System.out.println("var: " + var); 
    	
    	double cum = normal.cumulativeProbability(10);
    	System.out.println("cum: " + cum);
    }
    

//    @Test
//    void testThreadSafePoisson1() {	
//    	 // Provide a thread-safe random number generator with data arguments
//    	 ThreadLocal<UniformRandomProvider> rng =
//    	     new ThreadLocal<UniformRandomProvider>() {
//    	         @Override
//    	         protected UniformRandomProvider initialValue() {
//    	             return RandomSource.TWO_CMRES_SELECT.create(null, 3, 4);
//    	         }
//    	     };
//
//        	 // One-time Poisson sample using a thread-safe random number generator
//        	 double mean = 12.3;
//        	 int counts = PoissonSampler.of(rng.get(), mean).sample();
//        	 System.out.println("poison counts: " + counts);
//    }
    
    @Test
    void testThreadSafePoisson2() {	 	 
    	 // Access a thread-safe random number generator
    	 UniformRandomProvider rng = ThreadLocalRandomSource.current(RandomSource.SPLIT_MIX_64);
  
    	 // One-time Poisson sample using a thread-safe random number generator
    	 double mean = 12.3;
    	 int counts = PoissonSampler.of(rng, mean).sample();
    	 System.out.println("poison counts: " + counts);
    }
}
