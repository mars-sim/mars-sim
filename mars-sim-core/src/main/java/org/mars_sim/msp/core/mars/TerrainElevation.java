/**
 * Mars Simulation Project
 * TerrainElevation.java
 * @version 3.1.0 2017-10-03
 * @author Scott Davis
 */

package org.mars_sim.msp.core.mars;

import java.awt.Color;
import java.io.Serializable;

import org.mars_sim.mapdata.MapData;
import org.mars_sim.mapdata.MapDataUtil;
import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Direction;

/**
 * The TerrainElevation class represents the surface terrain of the virtual
 * Mars. It can provide information about elevation and terrain ruggedness at
 * any location on the surface of virtual Mars.
 */
public class TerrainElevation implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final double OLYMPUS_MONS_CALDERA_PHI = 1.267990;
	private static final double OLYMPUS_MONS_CALDERA_THETA = 3.949854;
	
	private static final double ASCRAEUS_MONS_PHI = 1.363102D;
	private static final double ASCRAEUS_MONS_THETA = 4.459316D;
	
	private static final double ARSIA_MONS_PHI = 1.411494; 
	private static final double ARSIA_MONS_THETA = 4.158439;
//
	private static final double ELYSIUM_MONS_PHI = 1.138866; 
	private static final double ELYSIUM_MONS_THETA = 2.555808;
//	
	private static final double PAVONIS_MONS_PHI = 1.569704; 
	private static final double PAVONIS_MONS_THETA = 4.305273 ;

	private static final double HECATES_THOLUS_PHI = 1.015563; 
	private static final double HECATES_THOLUS_THETA = 2.615812;

	private static final double ALBOR_THOLUS_PHI = 1.245184; 
	private static final double ALBOR_THOLUS_THETA = 2.615812;

	private static MapData mapdata = MapDataUtil.instance().getTopoMapData();
	
	/**
	 * Constructor
	 */
//	@JsonIgnoreProperties
	public TerrainElevation() {
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
		Color color = mapdata.getRGBColor(location.getPhi(), location.getTheta());
		float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
		float hue = hsb[0];
		float saturation = hsb[1];

		// Determine elevation in meters.
		// TODO This code (calculate terrain elevation) needs updating.
		double elevation = 0D;
		if ((hue < .792F) && (hue > .033F))
			elevation = (-13_801.99D * hue) + 2_500D;
		else
			elevation = (-21_527.78D * saturation) + 19_375D + 2_500D;

		// Determine elevation in kilometers.
		elevation = elevation / 1_000D;

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
		if (Math.abs(location.getTheta() - OLYMPUS_MONS_CALDERA_THETA) < .06D) {
			if (Math.abs(location.getPhi() - OLYMPUS_MONS_CALDERA_PHI) < .06D) {
//				System.out.println("elevation at Olympus : " + elevation);
				if (elevation < 3D)
					result = 21.287D;
			}
		}

		// Patch errors at Ascraeus Mons.
		else if (Math.abs(location.getTheta() - ASCRAEUS_MONS_THETA) < .04D) {
			if (Math.abs(location.getPhi() - ASCRAEUS_MONS_PHI) < .04D) {
				if (elevation < 3D)
					result = 18.219;
			}
		}


		else if (Math.abs(location.getTheta() - ARSIA_MONS_THETA) < .04D) {
			if (Math.abs(location.getPhi() - ARSIA_MONS_PHI) < .04D) {
				if (elevation < 3D)
					result = 17.781;
			}
		}

		else if (Math.abs(location.getTheta() - ELYSIUM_MONS_THETA) < .04D) {
			if (Math.abs(location.getPhi() - ELYSIUM_MONS_PHI) < .04D) {
				if (elevation < 3D)
					result = 14.127;
			}
		}
		
		else if (Math.abs(location.getTheta() - PAVONIS_MONS_THETA) < .04D) {
			if (Math.abs(location.getPhi() - PAVONIS_MONS_PHI) < .04D) {
				if (elevation < 3D)
					result = 14.057;
			}
		}
		
		else if (Math.abs(location.getTheta() - HECATES_THOLUS_THETA) < .04D) {
			if (Math.abs(location.getPhi() - HECATES_THOLUS_PHI) < .04D) {
				if (elevation < 2.5D)
					result = 4.853;
			}
		}
		
		// Patch errors at Ascraeus Mons.
		else if (Math.abs(location.getTheta() - ALBOR_THOLUS_THETA) < .04D) {
			if (Math.abs(location.getPhi() - ALBOR_THOLUS_PHI) < .04D) {
				if (elevation < 2D)
					result = 3.925;
			}
		}
		
		return result;
	}
}