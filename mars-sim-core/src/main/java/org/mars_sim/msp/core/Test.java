package org.mars_sim.msp.core;

public class Test {

    public static void main(String [] args) {

    	for (int i= 0; i < 100; i++) {
        	double rand = RandomUtil.getGaussianDouble();
    		System.out.println(rand);
    	}
    }
	
}
