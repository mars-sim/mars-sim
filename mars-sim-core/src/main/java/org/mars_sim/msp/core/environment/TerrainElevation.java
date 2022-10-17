/*
 * Mars Simulation Project
 * TerrainElevation.java
 * @date 2022-07-29
 * @author Scott Davis
 */

package org.mars_sim.msp.core.environment;

import java.awt.Color;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.mars_sim.mapdata.MapData;
import org.mars_sim.mapdata.MapDataUtil;
import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Direction;
import org.mars_sim.msp.core.tool.RandomUtil;

// Note: the newly surveyed ice deposit spans latitudes from 39 to 49 deg
// within the Utopia Planitia plains, as estimated by SHARAD, an subsurface
// sounding radar ice that penetrate below the surface. SHARAD was mounted
// on the Mars Reconnaissance Orbiter.
//
// See https://www.jpl.nasa.gov/news/news.php?feature=6680

/**
 * The TerrainElevation class represents the surface terrain of the virtual
 * Mars. It provides information about elevation and terrain ruggedness and
 * calculate ice collection rate at a location on its vast surface.
 */
public class TerrainElevation implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final double DEG_TO_RAD = Math.PI/180;

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
	private static final double PAVONIS_MONS_THETA = 4.305273;

	private static final double HECATES_THOLUS_PHI = 1.015563;
	private static final double HECATES_THOLUS_THETA = 2.615812;

	private static final double ALBOR_THOLUS_PHI = 1.245184;
	private static final double ALBOR_THOLUS_THETA = 2.615812;

	private static final double RATE = 1;

	private static MapData mapdata;

	private static MapDataUtil mapDataUtil = MapDataUtil.instance();

	private static Set<CollectionSite> sites;

	/**
	 * Constructor.
	 */
	public TerrainElevation() {
		sites = new HashSet<>();
	}

	/**
	 * Returns terrain steepness angle from location by sampling 11.1 km in given
	 * direction.
	 *
	 * @param currentLocation  the coordinates of the current location
	 * @param currentDirection the current direction (in radians)
	 * @return terrain steepness angle (in radians)
	 */
	public static double determineTerrainSteepness(Coordinates currentLocation, Direction currentDirection) {
		double newY = -1.5D * currentDirection.getCosDirection();
		double newX = 1.5D * currentDirection.getSinDirection();
		Coordinates sampleLocation = currentLocation.convertRectToSpherical(newX, newY);
		double elevationChange = getMOLAElevation(sampleLocation) - getMOLAElevation(currentLocation);
		return Math.atan(elevationChange / 11.1D);
	}

	/**
	 * Determines the terrain steepness angle from location by sampling 11.1 km in given
	 * direction and elevation.
	 *
	 * @param currentLocation
	 * @param elevation
	 * @param currentDirection
	 * @return
	 */
	public static double determineTerrainSteepness(Coordinates currentLocation, double elevation, Direction currentDirection) {
		double newY = - 1.5 * currentDirection.getCosDirection();
		double newX = 1.5 * currentDirection.getSinDirection();
		Coordinates sampleLocation = currentLocation.convertRectToSpherical(newX, newY);
		double elevationChange = getMOLAElevation(sampleLocation) - elevation;
		return Math.atan(elevationChange / 11.1D);
	}

	/**
	 * Determines the terrain steepness angle from location by sampling a random coordinate set and 11.1 km in given
	 * direction and elevation.
	 *
	 * @param currentLocation
	 * @param elevation
	 * @param currentDirection
	 * @return
	 */
	public static double determineTerrainSteepnessRandom(Coordinates currentLocation, double elevation, Direction currentDirection) {
		double newY = - RandomUtil.getRandomDouble(1.5) * currentDirection.getCosDirection();
		double newX = RandomUtil.getRandomDouble(1.5) * currentDirection.getSinDirection();
		Coordinates sampleLocation = currentLocation.convertRectToSpherical(newX, newY);
		double elevationChange = getMOLAElevation(sampleLocation) - elevation;
		return Math.atan(elevationChange / 11.1D);
	}

	/**
	 * Computes the terrain profile of a site at a coordinate
	 * direction and elevation.
	 *
	 * @param {@link CollectionSite} site
	 * @param {@link Coordinates} currentLocation
	 * @return an array of two doubles, namely elevation and steepness
	 */
	public static double[] computeTerrainProfile(CollectionSite site, Coordinates currentLocation) {
		double steepness = 0;
		double elevation = getMOLAElevation(currentLocation);
		for (int i=0 ; i <= 360 ; i++) {
			double rad = i * DEG_TO_RAD;
			steepness += Math.abs(determineTerrainSteepness(currentLocation, elevation, new Direction(rad)));
		}
//		// Create a new site
//		site.setElevation(elevation);
//		site.setSteepness(steepness);
//
//		// Save this site
//		surfaceFeatures.setSites(currentLocation, site);

		return new double[] {elevation, steepness};
	}

	/**
	 * Gets the terrain profile of a location.
	 *
	 * @param {@link Coordinates}
	 * @return an array of two doubles, namely elevation and steepness
	 */
	public static double[] getTerrainProfile(Coordinates currentLocation) {
		return computeTerrainProfile(null, currentLocation);
	}


	/**
	 * Computes the regolith collection rate of a location.
	 *
	 * @param site
	 * @param currentLocation
	 * @return regolith collection rate
	 */
	public static void computeRegolithCollectionRate(CollectionSite site, Coordinates currentLocation) {

		// Get the elevation and terrain gradient factor
		double[] terrainProfile = getTerrainProfile(currentLocation);

		double elevation = terrainProfile[0];
		double steepness = terrainProfile[1];
		double latitude = currentLocation.getLatitudeDouble();

		site.setElevation(elevation);
		site.setSteepness(steepness);

		double rate = RATE;

		// Note: Add seasonal variation for north and south hemisphere
		// Note: The collection rate may be increased by relevant scientific studies

		if (latitude < 60 && latitude > -60) {
			// The steeper the slope, the harder it is to retrieve the deposit
			rate *= RandomUtil.getRandomDouble(10) + (- 0.639 * elevation + 14.2492) / 5D  - Math.abs(steepness) / 10D;
		}

		else if ((latitude >= 60 && latitude < 75)
			|| (latitude <= -60 && latitude > -75)) {
			rate *= RandomUtil.getRandomDouble(5) + Math.abs(elevation) / 20.0  - Math.abs(latitude) / 100.0 - Math.abs(steepness) / 10D;
		}

		else if ((latitude >= 75 && latitude <= 90)
				|| (latitude <= -75 && latitude >= -90)) {
				rate *= Math.abs(elevation) / 50.0  - Math.abs(latitude) / 50.0;
		}

		if (rate > 200)
			rate = 200;

		if (rate < 1)
			rate = 1;

		site.setRegolithCollectionRate(rate);
	}

	/**
	 * Computes the ice collection rate of a location.
	 *
	 * @param site
	 * @param currentLocation
	 * @return ice collection rate
	 */
	public static void computeIceCollectionRate(CollectionSite site, Coordinates currentLocation) {

		// Get the elevation and terrain gradient factor
		double[] terrainProfile = getTerrainProfile(currentLocation);

		double elevation = terrainProfile[0];
		double steepness = terrainProfile[1];
		double latitude = currentLocation.getLatitudeDouble();

		site.setElevation(elevation);
		site.setSteepness(steepness);

		double rate = RATE;

		// Note: Add seasonal variation for north and south hemisphere
		// Note: The collection rate may be increased by relevant scientific studies

		if (latitude < 60 && latitude > -60) {
			// The steeper the slope, the harder it is to retrieve the ice deposit
			rate *= (- 0.639 * elevation + 14.2492) / 20D + Math.abs(steepness) / 10D;
		}

		else if ((latitude >= 60 && latitude < 75)
			|| (latitude <= -60 && latitude > -75)) {
			rate *= RandomUtil.getRandomDouble(5) + Math.abs(elevation) / 2.0 + Math.abs(latitude) / 75.0 - Math.abs(steepness) / 10D;
		}

		else if ((latitude >= 75 && latitude <= 90)
				|| (latitude <= -75 && latitude >= -90)) {
				rate *= RandomUtil.getRandomDouble(10) + Math.abs(elevation) + Math.abs(latitude) / 75.0;
		}

		if (rate > 200)
			rate = 200;

		if (rate < 1)
			rate = 1;

		site.setIceCollectionRate(rate);
	}

	/**
	 * Obtains the ice collection rate of a location.
	 *
	 * @param loc
	 * @return the collection rate
	 */
	public static double obtainIceCollectionRate(Coordinates loc) {
		CollectionSite site = getCollectionSite(loc);
		if (site.getIceCollectionRate() == -1)
			computeIceCollectionRate(site, loc);
		return site.getIceCollectionRate();
	}

	/**
	 * Obtains the regolith collection rate of a location.
	 *
	 * @param loc
	 * @return the collection rate
	 */
	public static double obtainRegolithCollectionRate(Coordinates loc) {
		CollectionSite site = getCollectionSite(loc);
		if (site.getRegolithCollectionRate() == -1)
			computeRegolithCollectionRate(site, loc);
		return site.getRegolithCollectionRate();
	}

	public static int[] getRGB(Coordinates location) {
		// Find hue and saturation color components at location.
		if (mapdata == null)
			mapdata = mapDataUtil.getTopoMapData();
		Color color = mapdata.getRGBColor(location.getPhi(), location.getTheta());
		int red = color.getRed();
		int green = color.getGreen();
		int blue = color.getBlue();

		return new int[] {red, green, blue};
	}

	public static float[] getHSB(int[] rgb) {
		float[] hsb = Color.RGBtoHSB(rgb[0], rgb[1], rgb[2], null);
		float hue = hsb[0];
		float saturation = hsb[1];
		float brightness = hsb[2];

//		String s1 = String.format(" %5.3f %5.3f %5.3f  ",
//				Math.round(hue*1000.0)/1000.0,
//				Math.round(saturation*1000.0)/1000.0,
//				Math.round(brightness*1000.0)/1000.0);
//		System.out.print(s1);

		return new float[] {hue, saturation, brightness};
	}

	/**
	 * Returns the elevation in km at the given location, based on MOLA's dataset.
	 *
	 * @param location the location in question
	 * @return the elevation at the location (in km)
	 */
	public static double getMOLAElevation(Coordinates location) {
		return getMOLAElevation(location.getPhi(), location.getTheta());
	}

	/**
	 * Returns the elevation in km at the given location, based on MOLA's dataset.
	 *
	 * @param phi
	 * @param theta
	 * @return the elevation at the location (in km)
	 */
	public static double getMOLAElevation(double phi, double theta) {
		return mapDataUtil.getElevationInt(phi, theta)/1000.0;
	}
	/**
	 * Returns the patched elevation in km at the given location.
	 *
	 * @param location the location in question
	 * @return the elevation at the location (in km)
	 */
	public static double getPatchedElevation(Coordinates location) {
		// Patch elevation problems at certain locations.
		return patchElevation(getRawElevation(location), location);
	}

	/**
	 * Returns the raw elevation in km at the given location.
	 *
	 * @param location the location in question
	 * @return the elevation at the location (in km)
	 */
	public static double getRawElevation(Coordinates location) {
		// Find hue and saturation color components at location.
		int rgb[] = getRGB(location);
//		int red = rgb[0];
		int green = rgb[1];
		int blue = rgb[2];

		float[] hsb = getHSB(rgb);
		float hue = hsb[0];
		float saturation = hsb[1];
		float brightness = hsb[2];

		// Determine elevation in meters.
		// NOTE: This code (calculate terrain elevation) needs updating.
		double elevation = 0;

		// The minimum and maximum topography observations for the entire data set are -8068 and 21134 meters.

//		Note 1: Color Legends at https://astropedia.astrogeology.usgs.gov/download/Mars/GlobalSurveyor/MOLA/ancillary/colorhillshade_mola_lut.gif
//		Note 2: Lookup Table at https://user-images.githubusercontent.com/1168584/65486713-0c855b80-de5a-11e9-9777-1ed3943c433b.gif

//		if (red == 255 && green == 255 & blue == 255) {
//			// Note: at Color.white, it's still not the highest point but very close.
//			return 19;
//		}

		// Determine elevation in kilometers.

		// Between 19 km to 22+ km
		if (saturation >= 0 && saturation <= 0.4326
				&& brightness >= .9921 && brightness <= 1
				&& green >= 249
				&& blue >= 253)
			elevation = saturation * .1422 + 1.0204;

		// Between -9 km to 0 km
		else if ((hue < 1) && (hue >= 0.1752))
			elevation = -13.6219 * hue + 2.3866;

		// Between 0 km to 19 km
		else if ((saturation <= .8504) && (saturation >= 0))
			elevation = saturation * -22.3424 + 19;


		return elevation;
	}

	/**
	 * Patches elevation errors around mountain tops.
	 *
	 * @param elevation the original elevation for the location.
	 * @param location  the coordinates
	 * @return the patched elevation for the location
	 */
	private static double patchElevation(double elevation, Coordinates location) {
		// Patch errors at Olympus Mons
		// Patch the smallest cauldera at the center
		if (Math.abs(location.getTheta() - OLYMPUS_MONS_CALDERA_THETA) < .0176
			 && Math.abs(location.getPhi() - OLYMPUS_MONS_CALDERA_PHI) < .0174) {
				return (elevation + 19) / 2.0;
		}

		// Patch errors at Olympus Mons caldera.
		// Patch the larger white cauldera
		if (Math.abs(location.getTheta() - OLYMPUS_MONS_CALDERA_THETA) < .0796
			&& Math.abs(location.getPhi() - OLYMPUS_MONS_CALDERA_PHI) < .0796) {
			if (elevation > 19 && elevation < 21.2870)
				return (elevation + 21.287D) / 2.0;
			else if (elevation >= 21.2870)
				return elevation;
			
			return (elevation + 19) / 2.0;
		}

		// Patch errors at Olympus Mons caldera.
		// Patch the red base cauldera
		if (Math.abs(location.getTheta() - OLYMPUS_MONS_CALDERA_THETA) < .1731
			&& Math.abs(location.getPhi() - OLYMPUS_MONS_CALDERA_PHI) < .1731) {
			if (elevation < 19 && elevation > 3)
				return elevation;
			else if (elevation >= 19)
				return elevation;
			
			return (elevation + 3) / 2.0;
		}

		// Patch errors at Ascraeus Mons.
		if (Math.abs(location.getTheta() - ASCRAEUS_MONS_THETA) < .04D
			&& Math.abs(location.getPhi() - ASCRAEUS_MONS_PHI) < .04D) {
			if (elevation < 3.4D)
				return (elevation + 18.219) / 2.0;
		}

		else if (Math.abs(location.getTheta() - ARSIA_MONS_THETA) < .04D
			&& Math.abs(location.getPhi() - ARSIA_MONS_PHI) < .04D) {
				if (elevation < 3D)
					return (elevation + 17.781) / 2.0;
		}

		else if (Math.abs(location.getTheta() - ELYSIUM_MONS_THETA) < .04D
			&& Math.abs(location.getPhi() - ELYSIUM_MONS_PHI) < .04D) {
				if (elevation < 3D)
					return (elevation + 14.127) / 2.0;
		}

		if (Math.abs(location.getTheta() - PAVONIS_MONS_THETA) < .04D
			&& Math.abs(location.getPhi() - PAVONIS_MONS_PHI) < .04D) {
				if (elevation < 3D)
					return (elevation + 14.057) / 2.0;
		}

		if (Math.abs(location.getTheta() - HECATES_THOLUS_THETA) < .04D
			&& Math.abs(location.getPhi() - HECATES_THOLUS_PHI) < .04D) {
//				if (elevation < 2.5D)
					return (elevation + 4.853) / 2.0;
		}

		// Patch errors at Ascraeus Mons.
		if (Math.abs(location.getTheta() - ALBOR_THOLUS_THETA) < .04D
			&& Math.abs(location.getPhi() - ALBOR_THOLUS_PHI) < .04D) {
//				if (elevation < 2D)
					return (elevation + 3.925) / 2.0;
		}

//		// Patch errors at the north pole.
//		else if (Math.abs(location.getTheta() - NORTH_POLE_THETA) < .2D) {
//			if (Math.abs(location.getPhi() - NORTH_POLE_PHI) < .04D) {
////				if (elevation < 2D)
//					return 1.015;
//			}
//		}
//
//		// Patch errors at the south pole.
//		else if (Math.abs(location.getTheta() - SOUTH_POLE_THETA) < .04D) {
//			if (Math.abs(location.getPhi() - SOUTH_POLE_PHI) < .04D) {
////				if (elevation < 2D)
//					return .783;
//			}
//		}

		return elevation;
	}

	public Set<CollectionSite> getCollectionSites() {
		return sites;
	}

	public static void addCollectionSite(CollectionSite site) {
		sites.add(site);
	}

	public static synchronized CollectionSite getCollectionSite(Coordinates newLocation) {
		// Create a shallow copy of sites to avoid ConcurrentModificationException
		for (CollectionSite s:  sites) {
			if (s.getLocation().equals(newLocation)) {
				return s;
			}
		}
		CollectionSite site = new CollectionSite(newLocation);
		addCollectionSite(site);
		return site;
	}

	/**
	 * Prepares object for garbage collection.
	 */
	public void destroy() {
		//nothing
	}
}
