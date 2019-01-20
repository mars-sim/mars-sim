/**
 * Common Mathematic Functions
 */
package org.mars_sim.msp.core.astroarts;

public class UdMath {
	/**
	 * modulo for double value
	 */
	static double fmod(double fX, double fY) {
		return fX - Math.ceil(fX / fY) * fY;
	}
	
	/**
	 * sin for degree
	 */
	static double udsin(double fX) {
		return Math.sin(fX * Math.PI / 180.0);
	}

	/**
	 * cos for degree
	 */
	static double udcos(double fX) {
		return Math.cos(fX * Math.PI / 180.0);
	}

	/**
	 * tan for degree
	 */
	static double udtan(double fX) {
		return Math.tan(fX * Math.PI / 180.0);
	}

	/**
	 * Rounding degree angle between 0 to 360
	 */
	static double degmal(double fX) {
		double fY =  360.0 * (fX / 360.0 - Math.floor(fX / 360.0));
		if (fY < 0.0) {
			fY += 360.0;
		}
		if (fY >= 360.0) {
			fY -= 360.0;
		}
		return fY;
	}

	/**
	 * Rounding radian angle between 0 to 2*PI
	 */
	static double radmal(double fX) {
		double fY =  Math.PI * 2.0 * (fX / (Math.PI * 2.0)
						 - Math.floor(fX / (Math.PI * 2.0)));
		if (fY < 0.0) {
			fY += Math.PI * 2.0;
		}
		if (fY >= Math.PI * 2.0) {
			fY -= Math.PI * 2.0;
		}
		return fY;
	}

	/**
	 * Degree to Radian
	 */
	static double deg2rad(double fX) {
		return fX * Math.PI / 180.0;
	}

	/**
	 * Radian to Degree
	 */
	static double rad2deg(double fX) {
		return fX * 180.0 / Math.PI;
	}

	/**
	 * arccosh
	 */
	static double arccosh(double fX) {
		return Math.log(fX + Math.sqrt(fX * fX - 1.0));
	}

	/**
	 * sinh
	 */
	static double sinh(double fX) {
		return (Math.exp(fX) - Math.exp(-fX)) / 2.0;
	}

	/**
	 * cosh
	 */
	static double cosh(double fX) {
		return (Math.exp(fX) + Math.exp(-fX)) / 2.0;
	}
}
