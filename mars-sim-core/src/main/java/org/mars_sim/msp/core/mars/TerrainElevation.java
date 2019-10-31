/**
 * Mars Simulation Project
 * TerrainElevation.java
 * @version 3.1.0 2017-10-03
 * @author Scott Davis
 */

package org.mars_sim.msp.core.mars;

import java.awt.Color;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.mars_sim.mapdata.MapData;
import org.mars_sim.mapdata.MapDataUtil;
import org.mars_sim.msp.core.CollectionUtils;
import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Direction;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * The TerrainElevation class represents the surface terrain of the virtual
 * Mars. It can provide information about elevation and terrain ruggedness at
 * any location on the surface of virtual Mars.
 */
public class TerrainElevation implements Serializable {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(TerrainElevation.class.getName());

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

//	private static final double NORTH_POLE_PHI = 0; 
//	private static final double NORTH_POLE_THETA = 0;
//
//	private static final double SOUTH_POLE_PHI = Math.PI; 
//	private static final double SOUTH_POLE_THETA = 0;
	
	private static MapData mapdata = MapDataUtil.instance().getTopoMapData();
	
	private static MarsSurface marsSurface = Simulation.instance().getMars().getMarsSurface();
	
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
	public static double determineTerrainSteepness(Coordinates currentLocation, Direction currentDirection) {
		double newY = -1.5D * currentDirection.getCosDirection();
		double newX = 1.5D * currentDirection.getSinDirection();
		Coordinates sampleLocation = currentLocation.convertRectToSpherical(newX, newY);
		double elevationChange = getPatchedElevation(sampleLocation) - getPatchedElevation(currentLocation);
		double result = Math.atan(elevationChange / 11.1D);
		return result;
	}

	/**
	 * Determines the terrain steepness angle from location by sampling 11.1 km in given
	 * direction and elevation
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
		double elevationChange = getPatchedElevation(sampleLocation) - elevation;
		return Math.atan(elevationChange / 11.1D);
	}
	
	/**
	 * Determines the terrain steepness angle from location by sampling a random coordinate set and 11.1 km in given
	 * direction and elevation
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
		double elevationChange = getPatchedElevation(sampleLocation) - elevation;
		return Math.atan(elevationChange / 11.1D);
	}
	
	/**
	 * Gets the terrain profile of a location
	 * 
	 * @param currentLocation
	 * @return
	 */
	public static double[] getTerrainProfile(Coordinates currentLocation) {
//		if (marsSurface.getSites().containsKey(currentLocation)) {
//			Site site = marsSurface.getSites().get(currentLocation);
//			return new double[] {site.getElevation(), site.getSteepness()};
//		}
//		
//		else {
			double steepness = 0;
			double elevation = getPatchedElevation(currentLocation);
			for (int i=0 ; i <= 360 ; i++) {
				double rad = i * DEG_TO_RAD;
				steepness += Math.abs(determineTerrainSteepness(currentLocation, elevation, new Direction(rad)));
			}
//			// Create a new site
//			Site site = new Site(currentLocation);
//			site.setElevation(elevation);
//			site.setSteepness(steepness);
//			
//			// Save this site
//			marsSurface.setSites(currentLocation, site);
			
			return new double[] {elevation, steepness};
//		}
	}
	
	/**
	 * Obtains the ice collection rate of a location
	 * 
	 * @param currentLocation
	 * @return
	 */
	public static double getIceCollectionRate(Coordinates currentLocation) {
		if (marsSurface.getSites().containsKey(currentLocation)) {
			IceSite site = (IceSite) marsSurface.getSites().get(currentLocation);
			return site.getIceCollectionRate();
		}
		
		else {
			// Get the elevation and terrain gradient factor
			double[] terrainProfile = getTerrainProfile(currentLocation);
					
			double elevation = terrainProfile[0];
			double steepness = terrainProfile[1];		
			
			double iceCollectionRate = (- 0.639 * elevation + 14.2492) / 10D  + steepness / 250;
			
	//		https://science.nasa.gov/science-news/science-at-nasa/2002/28may_marsice/
	//		The ice-rich layer is about 60 centimeters (two feet) beneath the surface at 60 degrees south 
	//		latitude, and gets to within about 30 centimeters (one foot) of the surface at 75 degrees south latitude.
			
			if (iceCollectionRate < 0)
				iceCollectionRate = 0;	
			
			// Create a new site
			Site site = new IceSite(currentLocation);
			site.setElevation(elevation);
			site.setSteepness(steepness);
			
			// Save this site
			marsSurface.setSites(currentLocation, site);
			
			String nameLoc = "";
			Settlement s = CollectionUtils.findSettlement(currentLocation);
			if (s != null) {
				nameLoc = "At " + s.getName() + ",";
				logger.info(nameLoc + "           elevation : " + Math.round(elevation*1000.0)/1000.0 + " km");
				logger.info(nameLoc + "   terrain steepness : " + Math.round(steepness*10.0)/10.0);
				logger.info(nameLoc + " ice collection rate : " + Math.round(iceCollectionRate*100.0)/100.0 + " kg/millisol");
				
			}
	//		else
	//			nameLoc = "At " + currentLocation.getCoordinateString() + ",";
			
			return iceCollectionRate;
		}
	}
	
	public static int[] getRGB(Coordinates location) {
		// Find hue and saturation color components at location.
		Color color = mapdata.getRGBColor(location.getPhi(), location.getTheta());
		int red = color.getRed();
		int green = color.getGreen();
		int blue = color.getBlue();
		
//		String s0 = String.format("  %3d %3d %3d  ", 
//						red,
//						green,
//						blue); 
//		System.out.print(s0);
		
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
	 * Returns the patched elevation in km at the given location
	 * 
	 * @param location the location in question
	 * @return the elevation at the location (in km)
	 */
	public static double getPatchedElevation(Coordinates location) {
		
		// Patch elevation problems at certain locations.
		double elevation = patchElevation(getRawElevation(location), location);

//		String s3 = String.format("%10.3f ", 
//				Math.round(elevation*1_000.0)/1_000.0);
//		System.out.print(s3);
		
		return elevation;
	}

	/**
	 * Returns the raw elevation in km at the given location
	 * 
	 * @param location the location in question
	 * @return the elevation at the location (in km)
	 */
	public static double getRawElevation(Coordinates location) {

		// Find hue and saturation color components at location.
		int rgb[] = getRGB(location);
		int red = rgb[0];
		int green = rgb[1];
		int blue = rgb[2];
		
		float[] hsb = getHSB(rgb);
		float hue = hsb[0];
		float saturation = hsb[1];
		float brightness = hsb[2];
	
		// Determine elevation in meters.
		// TODO This code (calculate terrain elevation) needs updating.
		double elevation = 0;
		
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
		double result = elevation;

		// Patch errors at Olympus Mons
		// Patch the smallest cauldera at the center
		if (Math.abs(location.getTheta() - OLYMPUS_MONS_CALDERA_THETA) < .0176
			 && Math.abs(location.getPhi() - OLYMPUS_MONS_CALDERA_PHI) < .0174) {
//				System.out.println("elevation at Olympus : " + elevation);
				result = 19; 
		}
		
		// Patch errors at Olympus Mons caldera.
		// Patch the larger white cauldera 
		else if (Math.abs(location.getTheta() - OLYMPUS_MONS_CALDERA_THETA) < .0796
			&& Math.abs(location.getPhi() - OLYMPUS_MONS_CALDERA_PHI) < .0796) {
//				System.out.println("elevation at Olympus : " + elevation);
			if (elevation > 19 && elevation < 21.2870)
				result = elevation;
			else
				result = 21.287D;
		}
		
		// Patch errors at Olympus Mons caldera.
		// Patch the red base cauldera 
		else if (Math.abs(location.getTheta() - OLYMPUS_MONS_CALDERA_THETA) < .1731
			&& Math.abs(location.getPhi() - OLYMPUS_MONS_CALDERA_PHI) < .1731) {
//				System.out.println("elevation at Olympus : " + elevation);
			if (elevation < 19 && elevation > 3)
				result = elevation;
			else
				result = 3;
		}
		
		// Patch errors at Ascraeus Mons.
		else if (Math.abs(location.getTheta() - ASCRAEUS_MONS_THETA) < .04D) {
			if (Math.abs(location.getPhi() - ASCRAEUS_MONS_PHI) < .04D) {
				if (elevation < 3.4D)
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
//				if (elevation < 2.5D)
					result = 4.853;
			}
		}
		
		// Patch errors at Ascraeus Mons.
		else if (Math.abs(location.getTheta() - ALBOR_THOLUS_THETA) < .04D) {
			if (Math.abs(location.getPhi() - ALBOR_THOLUS_PHI) < .04D) {
//				if (elevation < 2D)
					result = 3.925;
			}
		}
		
//		// Patch errors at the north pole.
//		else if (Math.abs(location.getTheta() - NORTH_POLE_THETA) < .2D) {
//			if (Math.abs(location.getPhi() - NORTH_POLE_PHI) < .04D) {
////				if (elevation < 2D)
//					result = 1.015;
//			}
//		}
//		
//		// Patch errors at the south pole.
//		else if (Math.abs(location.getTheta() - SOUTH_POLE_THETA) < .04D) {
//			if (Math.abs(location.getPhi() - SOUTH_POLE_PHI) < .04D) {
////				if (elevation < 2D)
//					result = .783;
//			}
//		}

		return result;
	}
	
	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		mapdata = null;
	}
}