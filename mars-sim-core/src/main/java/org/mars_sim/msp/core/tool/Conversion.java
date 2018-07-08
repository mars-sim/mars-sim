/**
 * Mars Simulation Project
 * Conversion.java
 * @version 3.1.0 2017-03-31
 * @author Manny Kung
 */

package org.mars_sim.msp.core.tool;

public class Conversion {

	public Conversion() {

	}
/*
	public void capitalize(String nameStr) {

		// convert nameStr down into an array
		// create new String at each whitespace

		// at each word, do

		String word = null;
		word = word.substring(0,1).toUpperCase()+ word.substring(1).toLowerCase();

		// convert the array back to one single String

	}
*/


	public static boolean checkVowel(String name) {
		if (name.toLowerCase().startsWith("a")
				|| name.startsWith("e")
				|| name.startsWith("i")
				|| name.startsWith("o")
				|| name.startsWith("u"))
			return true;
		else
			return false;
	}

	public static String setFirstWordLowercase(String input) {
		if (input != null) {
		    StringBuilder titleCase = new StringBuilder();
		    boolean nextTitleCase = true;

		    for (char c : input.toCharArray()) {
		        if (Character.isSpaceChar(c)|| c == '/' ) {
		            nextTitleCase = true;
		        } else if (nextTitleCase) {
		            c = Character.toLowerCase(c);
		            nextTitleCase = false;
		        }

		        titleCase.append(c);
		    }

		    return titleCase.toString().replaceAll("eVA", "EVA");
		}
		else
			return null;
//		
//		StringBuilder s = new StringBuilder();
//		if (!input.substring(0, 3).equals("EVA")) {
//			s.append(input.substring(0, 1).toLowerCase());
//			s.append(input.substring(1, input.length()));
//		}
//
//		return s.toString();
	}
	
	
	public static String capitalize(String input) {
		if (input != null) {
		    StringBuilder titleCase = new StringBuilder();
		    boolean nextTitleCase = true;

		    for (char c : input.toCharArray()) {
		        if (Character.isSpaceChar(c) || c == '(' ) {
		            nextTitleCase = true;
		        } else if (nextTitleCase) {
		            c = Character.toTitleCase(c);
		            nextTitleCase = false;
		        }

		        titleCase.append(c);
		    }

		    return titleCase.toString();
		}
		else
			return null;
	}

}
