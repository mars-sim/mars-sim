/*
 * Mars Simulation Project
 * Conversion.java
 * @date 2025-07-05
 * @author Manny Kung
 */
package com.mars_sim.core.tool;

public final class Conversion {

	private Conversion() {
		// Prevent creation
	}

	/**
	 * Capitalizes the first letter of each word in the input phase.
	 * 
	 * @param input The input phase
	 * @return The modified phase
	 */
	public static String capitalize0(String input) {
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
	
	/**
	 * Capitalizes the first letter of each word in the input phase
	 * 
	 * @param input The input phase
	 * @return The modified phase
	 */
	public static String capitalize(String input) {
		if (input != null) {
			StringBuilder s = new StringBuilder();
			boolean nextTitleCase = true;
			char[] charArray = input.toLowerCase().toCharArray();
			int index = 0;
			for (char c : charArray) {
				if (Character.isSpaceChar(c) 
						|| c == '('
						|| c == '-') {
					nextTitleCase = true;
				} else if (nextTitleCase) {
					// Check if it is "And" string and skip making the 'a' upper-case
					if (input.length() > 2 
							&& charArray[index] == 'a'
							&& (index+1 < charArray.length && charArray[index+1] == 'n')
							&& (index+2 < charArray.length && charArray[index+2] == 'd')
							&& (index+3 < charArray.length && Character.isSpaceChar(charArray[index+3]))) {
						nextTitleCase = false;	
					}
					else {
						c = Character.toTitleCase(c);
						nextTitleCase = false;
					}
				}
				s.append(c);
				index++;
			}
			return s.toString();
			
		} else
			return null;
	}

	/**
	 * Obtains the one-letter initial of each word in the input phase.
	 * 
	 * @param input The input phase
	 * @return The initials
	 */
	public static String getOneLetterInitial(String input) {
		if (input != null) {
			StringBuilder titleCase = new StringBuilder();
			boolean nextTitleCase = true;

			for (char c : input.toCharArray()) {
				if (Character.isSpaceChar(c) || c == '(') {
					nextTitleCase = true;
				} else if (nextTitleCase) {
					c = Character.toTitleCase(c);
					titleCase.append(c);
					nextTitleCase = false;
				}
			}

			return titleCase.toString();
		} else
			return null;
	}


	/**
	 * <p>
	 * Checks if a String is whitespace, empty ("") or null.
	 * </p>
	 *
	 * <pre>
	 * StringUtils.isBlank(null)      = true
	 * StringUtils.isBlank("")        = true
	 * StringUtils.isBlank(" ")       = true
	 * StringUtils.isBlank("bob")     = false
	 * StringUtils.isBlank("  bob  ") = false
	 * </pre>
	 *
	 * @param str the String to check, may be null
	 * @return <code>true</code> if the String is null, empty or whitespace
	 * @since 2.0
	 * @author commons.apache.org
	 */
	public static boolean isBlank(String str) {
		int strLen;
		if (str == null || (strLen = str.length()) == 0) {
			return true;
		}
		for (int i = 0; i < strLen; i++) {
			if (!(Character.isWhitespace(str.charAt(i)))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Trims a string to a length and suffix with ...
	 * 
	 * @param source
	 * @param length
	 * @return
	 */
	public static String trim(String source, int length) {
		if ((source != null) && (source.length() > length))
			source = source.substring(0, length-3) + "...";
		return source;
	}

	/**
	 * Splits the seed into separate words on a capital boundary.
	 * 
	 * @param seed
	 * @return
	 */
    public static String split(String seed) {
		StringBuilder result = new StringBuilder();

		boolean lastIsUpper = true;
		for(var c : seed.toCharArray()) {
			boolean isUpper = Character.isUpperCase(c);
			if (!lastIsUpper && isUpper) {
				result.append(' ');
			}
			result.append(c);
			lastIsUpper = isUpper;
		}

        return result.toString();
    }
}
