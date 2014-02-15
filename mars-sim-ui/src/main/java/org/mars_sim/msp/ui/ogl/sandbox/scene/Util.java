package org.mars_sim.msp.ui.ogl.sandbox.scene;

/**
 * some utility functions for opengl vector math.
 * @author stpa
 */
public class Util {

	/**
	 * @return a three component vector with zeros.
	 */
	public static final double[] nul3() {
		return new double[3];
	}

	/**
	 * a random integer from the given range.
	 * @param min {@link Integer}
	 * @param max {@link Integer}
	 * @return {@link Integer}
	 */
	public static int rnd(int min,int max) {
		return min + (int) Math.floor(Math.random() * (max - min));
	}

	/**
	 * a three dimensional random array with values in between min and max.
	 * @param min {@link Double}
	 * @param max {@link Double}
	 */
	public static final double[] rnd3(double min, double max) {
		double delta = max - min;
		return new double[] {
			min + delta * Math.random(),
			min + delta * Math.random(),
			min + delta * Math.random()
		};
	}

	/**
	 * @param max {@link Double}
	 * @return a four component random vector with
	 * components 0..2 between 0.0 and max and the
	 * last component value one.
	 */
	public static final double[] rnd31(double max) {
		return new double[] {
			max * Math.random(),
			max * Math.random(),
			max * Math.random(),
			1.0f
		};
	}

	/**
	 * @param min {@link Double}
	 * @param max {@link Double}
	 * @return a four component random vector with
	 * components 0..2 between min and max and the
	 * last component value one.
	 */
	public static final double[] rnd31(double min, double max) {
		double delta = max - min;
		return new double[] {
			min + delta * Math.random(),
			min + delta * Math.random(),
			min + delta * Math.random(),
			1.0
		};
	}

	/**
	 * expects two non-null double arrays of equal length without checking these conditions.
	 * @param a {@link Double}[]
	 * @param b {@link Double}[]
	 */
	public static final double[] add(final double[] a, final double[] b) {
		double[] c = new double[a.length];
		for (int i = 0; i < a.length; i++) {
			c[i] = a[i] + b[i];
		}
		return c;
	}

	/**
	 * expects a double array of length three.
	 * @param v {@link Double}[]
	 */
	public static final void normalize3(double[] v) {
		double d = Math.sqrt(v[0]*v[0]+v[1]*v[1]+v[2]*v[2]);
		if (d != 0) {
			d = 1.0d / d;
			v[0] = v[0] * d;
			v[1] = v[1] * d;
			v[2] = v[2] * d;
		}
	}
}
