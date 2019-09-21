/**
 * Mars Simulation Project
 * Conversion.java
 * @version 3.1.0 2019-09-20
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.swing.tool;

public class Conversion {

	public Conversion() {

	}

//	public void capitalize(String nameStr) {
//
//		// convert nameStr down into an array
//		// create new String at each whitespace
//
//		// at each word, do
//
//		String word = null;
//		word = word.substring(0,1).toUpperCase()+ word.substring(1).toLowerCase();
//
//		// convert the array back to one single String
//
//	}

	public static String capitalize(String input) {
	    StringBuilder titleCase = new StringBuilder();
	    boolean nextTitleCase = true;

	    if (input != null) {
		    for (char c : input.toCharArray()) {
		        if (Character.isSpaceChar(c) || c == '(' ) {
		            nextTitleCase = true;
		        } else if (nextTitleCase) {
		            c = Character.toTitleCase(c);
		            nextTitleCase = false;
		        }
	
		        titleCase.append(c);
		    }
	    }
	    
	    return titleCase.toString();
	}

}
