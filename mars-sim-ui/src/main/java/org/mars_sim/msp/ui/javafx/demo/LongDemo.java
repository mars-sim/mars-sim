package org.mars_sim.msp.ui.javafx.demo;

public class LongDemo {

	static long a = 10000000L;

    public static void main(String[] args) {
        System.out.println("a : " + a);
    	a = (long) (a *.99);
        System.out.println("a : " + a);
    }
}
