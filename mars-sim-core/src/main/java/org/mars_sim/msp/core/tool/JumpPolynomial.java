/**
 * Mars Simulation Project
 * JumpPolynomial.java
 * @version 3.1.0 2017-11-06
 * @author Manny Kung
 */
package org.mars_sim.msp.core.tool;

/**
 * A jump polynomial.
 */
public class JumpPolynomial {

	private final int[] coefficients;

	/**
	 * Translates the hexadecimal polynomial format into a jump polynomial.
	 * 
	 * @param hexPoly the hexadecimal polynomial format
	 * @throws NullPointerException     if {@code hexPoly == null}
	 * @throws IllegalArgumentException if the hexadecimal polynomial format is
	 *                                  invalid.
	 */
	public JumpPolynomial(String hexPoly) {
		coefficients = new int[hexPoly.length()];
		for (int i = 0; i < coefficients.length; i++) {
			coefficients[i] = Character.digit(hexPoly.charAt(i), 16);
			if (coefficients[i] < 0) {
				throw new IllegalArgumentException();
			}
		}
	}

	/**
	 * Returns the degree of the jump polynomial.
	 * 
	 * @return the degree of the jump polynomial
	 */
	public int degree() {
		return coefficients.length;
	}

	/**
	 * Returns the coefficient of the jump polynomial at the specified index.
	 * 
	 * @param index the index
	 * @return the coefficient of the jump polynomial
	 * @throws IndexOutOfBoundsException if the index is out of range.
	 */
	public int coefficientAt(int index) {
		return coefficients[index];
	}

}
