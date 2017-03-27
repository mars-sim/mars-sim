package org.mars_sim.msp.ui.swing.demo;

import java.util.Locale;

public class LocaleDemo {

    public static void main(String[] args) {
		System.out.println("My locale is " + Locale.getDefault(Locale.Category.FORMAT));
		// "My locale is en_US"
    }


/*
	public LocaleDemo() {
		System.out.println("My locale is " + Locale.getDefault(Locale.Category.FORMAT));
		// "My locale is en_US"
	}
    public static void main(String[] args) {
    	new LocaleDemo();
    }
 */
}
