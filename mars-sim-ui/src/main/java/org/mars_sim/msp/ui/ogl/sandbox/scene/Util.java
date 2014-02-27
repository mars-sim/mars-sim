package org.mars_sim.msp.ui.ogl.sandbox.scene;

/**
 * some utility functions for opengl vector math.
 * @author stpa
 */
public class Util {

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

	/** returns the average. */
	public static double[] avg3(double[] p1, double[] p2) {
		return new double[] {
			p1[0] + 0.5 * (p2[0] - p1[0]),
			p1[1] + 0.5 * (p2[1] - p1[1]),
			p1[2] + 0.5 * (p2[2] - p1[2])
		};
	}

	/** returns the length of y - x for vectors with 3 components. */
	public static final double dif3(double[] x, double[] y) {
		double z = y[0] - x[0];
		double sum = z * z;
		z = y[1] - x[1];
		sum += z * z;
		z = y[2] - x[2];
		sum += z * z;
		return Math.sqrt(sum);
	}

	/** prints an error message to the console. */
	public static final void handle(Exception e) {
		System.err.println(e.getMessage());
	}

	/**
	 * expects a double array of length three.
	 * returns euclidian length of this vector.
	 * @param v {@link Double}[]
	 */
	public static final void length3(double[] v) {
		double d = Math.sqrt(v[0]*v[0]+v[1]*v[1]+v[2]*v[2]);
		if (d != 0) {
			d = 1.0d / d;
			v[0] = v[0] * d;
			v[1] = v[1] * d;
			v[2] = v[2] * d;
		}
	}

	/**
	 * normalizes the given vector to unit length.
	 * @param x {@link Double}[]
	 * @return {@link Double}
	 */
	public static final double[] norm(double[] x) {
		double sum = 0d;
		int len = x.length;
		for (int i = 0; i < len; i++) {
			sum += x[i] * x[i];
		}
		if (sum != 0d) {
			sum = 1d / Math.sqrt(sum);
		}
		double[] rezulto = new double[len];
		for (int i = 0; i < len; i++) {
			rezulto[i] = x[i] * sum;
		}
		return rezulto;
	}

	/**
	 * @return a three component vector with zeros.
	 */
	public static final double[] nul3() {
		return new double[3];
	}

	/**
	 * @return a four component vector with zeros.
	 */
	public static final double[] nul4() {
		return new double[4];
	}

	/**
	 * @return a three component vector with ones.
	 */
	public static final double[] one3() {
		return new double[] {1d,1d,1d};
	}

	/**
	 * @return a four component vector with ones.
	 */
	public static final double[] one4() {
		return new double[] {1d,1d,1d,1d};
	}

	/**
	 * a random value between zero and one.
	 * @return {@link Double}
	 */
	public static double rnd() {
		return Math.random();
	}

	/**
	 * a random integer from zero to the given max.
	 * @param min {@link Integer}
	 * @param max {@link Integer}
	 * @return {@link Integer}
	 */
	public static int rnd(int max) {
		return (int) Math.floor(Math.random() * max);
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

	/** prints an array to a string. */
	public static final String toString(double[] x) {
		StringBuffer s = new StringBuffer();
		int len = x.length;
		s.append("(");
		for (int i = 0; i < len; i++) {
			s.append(Double.toString(x[i]));
			if (i < len - 1) {
				s.append(",");
			}
		}
		s.append(")");
		return s.toString();
	}
}
