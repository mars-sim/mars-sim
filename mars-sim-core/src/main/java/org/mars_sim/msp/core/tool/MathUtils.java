/**
 * Mars Simulation Project
 * MathUtils.java
 * @version 3.1.0 2018-09-27
 * @author Manny Kung
 */

package org.mars_sim.msp.core.tool;

import java.util.Arrays;

public class MathUtils {

	/**
	 * Description : This method checks if input is power of 2 using bitwise
	 * operations.
	 *
	 * Ex: 8 in binary format 1 0 0 0 (8-1) = 7 in binary format 0 1 1 1 So, 8 &
	 * (8-1) will be 0 0 0 0
	 *
	 * @param n , not null
	 * @return true, if input number is power of 2.
	 */
	public static boolean isPowerOf2(final int n) {
		if (n <= 0) {
			return false;
		}
		return (n & (n - 1)) == 0;
	}

	public double exponential(double x) {
		x = 1d + x / 256d;
		x *= x; x *= x; x *= x; x *= x;
		x *= x; x *= x; x *= x; x *= x;
		return x;
	}
	
	@SafeVarargs
	public static <T> T[] concatAll(T[] first, T[]... rest) {
		int totalLength = first.length;
		for (T[] array : rest) {
			totalLength += array.length;
		}
		T[] result = Arrays.copyOf(first, totalLength);
		int offset = first.length;
		for (T[] array : rest) {
			System.arraycopy(array, 0, result, offset, array.length);
			offset += array.length;
		}
		return result;
	}

	public static double[] concatAll(double[] first, double[]... rest) {
		int totalLength = first.length;
		for (double[] array : rest) {
			totalLength += array.length;
		}
		double[] result = Arrays.copyOf(first, totalLength);
		int offset = first.length;
		for (double[] array : rest) {
			System.arraycopy(array, 0, result, offset, array.length);
			offset += array.length;
		}
		return result;
	}

	/**
	 * Normalize the values of the array
	 * 
	 * @param w
	 * @return
	 */
	public static double[] normalize(double[] w) {
		double[] vector = w;
		for (int i = 0; i < w.length; i++) {
			// .4 is the mid-point
			if (w[i] > 0.4)
				vector[i] -= .005;
			else if (w[i] < 0.4)
				vector[i] += .005;
			else
				vector[i] = 0.4;
		}
		return vector;
	}
	
	public static void sortStringBubble(String x[]){
		int j;
		boolean flag = true;  // will determine when the sort is finished
		String temp;

		while (flag){
			flag = false;
			for (j = 0; j < x.length - 1; j++){
				if (x[j].compareToIgnoreCase(x[j+1]) > 0) {
	                // ascending sort
	                temp = x [ j ];
	                x [ j ] = x [ j+1];     
	                // swapping
	                x [ j+1] = temp; 
	                flag = true;
				} 
			} 
		} 
    } 
}
