/**
 * Planet Class
 */
package org.mars_sim.msp.core.astroarts;


public class Planet {
	public static final int SUN     = 0;
	public static final int MERCURY = 1;
	public static final int VENUS   = 2;
	public static final int EARTH   = 3;
	public static final int MARS    = 4;
	public static final int JUPITER = 5;
	public static final int SATURN  = 6;
	public static final int URANUS  = 7;
	public static final int NEPTUNE = 8;
	public static final int PLUTO   = 9;

	private static final double R_JD_START = 2433282.5;	// 1950.0
	private static final double R_JD_END   = 2473459.5;	// 2060.0
	
	/**
	 * Get Planet Position in Ecliptic Coordinates (Equinox Date)
	 */
	public static Xyz getPos(int planetNo, ATime atime) {
		if (R_JD_START < atime.getJd() && atime.getJd() < R_JD_END) {
			return PlanetExp.getPos(planetNo, atime);
		} else {
			PlanetElm planetElm = new PlanetElm(planetNo, atime);
			return planetElm.getPos();
		}
	}
}
