/**
 * Mars Simulation Project
 * Conversion.java
 * @date 2021-12-22
 * @author Manny Kung
 */
package org.mars_sim.msp.core.tool;

public class Conversion {

	public Conversion() {
	}
	
		// Convert nameStr down into an array
		// Create new String at each whitespace

		// At each word, 
		// Do this: String word = word.substring(0,1).toUpperCase()+ word.substring(1).toLowerCase()

		// Convert the array back to one single String

	/**
	 * Checks if the initial of the string is a vowel
	 * 
	 * @param word
	 * @return true/false
	 */
	public static boolean isVowel(String word) {
        return word.toLowerCase().startsWith("a") || word.toLowerCase().startsWith("e")
                || word.toLowerCase().startsWith("i") || word.toLowerCase().startsWith("o")
                || word.toLowerCase().startsWith("u");
	}

	/**
	 * Sets the first word to lower case
	 * 
	 * @param input the word
	 * @return modified word
	 */
	public static String setFirstWordLowercase(String input) {
		if (input != null) {
			StringBuilder titleCase = new StringBuilder();
			boolean nextTitleCase = true;

			for (char c : input.toCharArray()) {
				if (Character.isSpaceChar(c) || c == '/') {
					nextTitleCase = true;
				} else if (nextTitleCase) {
					c = Character.toLowerCase(c);
					nextTitleCase = false;
				}

				titleCase.append(c);
			}

			return titleCase.toString().replaceAll("eVA", "EVA");
		} else
			return null;
	}

	/**
	 * Capitalizes the first letter of each word in the input phase
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
			char[] charArray = input.toCharArray();
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
							&& charArray[index+1] == 'n'
							&& charArray[index+2] == 'd'
							&& Character.isSpaceChar(charArray[index+3])) {
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
	 * Obtains the initials of each word in the input phase
	 * 
	 * @param input The input phase
	 * @return The initials
	 */
	public static String getInitials(String input) {
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
	 * Is this character an integer ?
	 * 
	 * @param s
	 * @param radix
	 * @return
	 */
	public static boolean isInteger(String s, int radix) {
	    if(s.isEmpty()) return false;
	    for(int i = 0; i < s.length(); i++) {
	        if(i == 0 && s.charAt(i) == '-') {
	            if(s.length() == 1) return false;
	            else continue;
	        }
	        if(Character.digit(s.charAt(i),radix) < 0) return false;
	    }
	    return true;
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
	 * Maps a number to an alphabet
	 *
	 * @param a number
	 * @return a String
	 */
	public static String getCharForNumber(int i) {
		// Do note delete. Will use it
		// NOTE: i must be > 1, if i = 0, return null
		return i > 0 && i < 27 ? String.valueOf((char) (i + 'A' - 1)) : null;
	}
	
    public static void main(String[] args) {
			new Conversion();
			
			String testString = "he and she are from the galaxy andromeda ! ";
			String result = capitalize(testString);
			System.out.println(result);
    }
}
