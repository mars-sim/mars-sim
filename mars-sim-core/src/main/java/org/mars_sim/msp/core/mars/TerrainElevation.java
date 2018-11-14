/**
 * Mars Simulation Project
 * TerrainElevation.java
 * @version 3.1.0 2017-10-03
 * @author Scott Davis
 */

package org.mars_sim.msp.core.mars;

import org.mars_sim.mapdata.MapDataUtil;
import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Direction;

import java.awt.Color;
import java.io.Serializable;

/**
 * The TerrainElevation class represents the surface terrain of the virtual
 * Mars. It can provide information about elevation and terrain ruggedness at
 * any location on the surface of virtual Mars.
 */
public class TerrainElevation implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final double OLYMPUS_MONS_CALDERA_PHI = 1.246165D;
	private static final double OLYMPUS_MONS_CALDERA_THETA = 3.944444D;
	private static final double ASCRAEUS_MONS_PHI = 1.363102D;
	private static final double ASCRAEUS_MONS_THETA = 4.459316D;

	/**
	 * Constructor
	 */
	TerrainElevation() {

	}

	/**
	 * Returns terrain steepness angle from location by sampling 11.1 km in given
	 * direction
	 * 
	 * @param currentLocation  the coordinates of the current location
	 * @param currentDirection the current direction (in radians)
	 * @return terrain steepness angle (in radians)
	 */
	public double determineTerrainDifficulty(Coordinates currentLocation, Direction currentDirection) {
		double newY = -1.5D * currentDirection.getCosDirection();
		double newX = 1.5D * currentDirection.getSinDirection();
		Coordinates sampleLocation = currentLocation.convertRectToSpherical(newX, newY);
		double elevationChange = getElevation(sampleLocation) - getElevation(currentLocation);
		double result = Math.atan(elevationChange / 11.1D);

		return result;
	}

	/**
	 * Returns elevation in km at the given location
	 * 
	 * @param location the location in question
	 * @return the elevation at the location (in km)
	 */
	public double getElevation(Coordinates location) {

		// Find hue and saturation color components at location.
		Color color = MapDataUtil.instance().getTopoMapData().getRGBColor(location.getPhi(), location.getTheta());
		float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
		float hue = hsb[0];
		float saturation = hsb[1];

		// Determine elevation in meters.
		// TODO This code (calculate terrain elevation) needs updating.
		double elevation = 0D;
		if ((hue < .792F) && (hue > .033F))
			elevation = (-13801.99D * hue) + 2500D;
		else
			elevation = (-21527.78D * saturation) + 19375D + 2500D;

		// Determine elevation in kilometers.
		elevation = elevation / 1000D;

		// Patch elevation problems at certain locations.
		elevation = patchElevation(elevation, location);

		return elevation;
	}

	/**
	 * Patches elevation errors around mountain tops.
	 * 
	 * @param elevation the original elevation for the location.
	 * @param location  the coordinates
	 * @return the patched elevation for the location
	 */
	private double patchElevation(double elevation, Coordinates location) {
		double result = elevation;

		// Patch errors at Olympus Mons caldera.
		if (Math.abs(location.getTheta() - OLYMPUS_MONS_CALDERA_THETA) < .04D) {
			if (Math.abs(location.getPhi() - OLYMPUS_MONS_CALDERA_PHI) < .04D) {
				if (elevation < 3D)
					result = 20D;
			}
		}

		// Patch errors at Ascraeus Mons.
		if (Math.abs(location.getTheta() - ASCRAEUS_MONS_THETA) < .02D) {
			if (Math.abs(location.getPhi() - ASCRAEUS_MONS_PHI) < .02D) {
				if (elevation < 3D)
					result = 20D;
			}
		}

		return result;
	}
}