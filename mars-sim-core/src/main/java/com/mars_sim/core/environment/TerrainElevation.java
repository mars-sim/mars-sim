/*
 * Mars Simulation Project
 * TerrainElevation.java
 * @date 2023-05-09
 * @author Scott Davis
 */

package com.mars_sim.core.environment;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.UnitManager;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.map.location.Direction;
import com.mars_sim.core.map.megdr.MEGDRFactory;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.MathUtils;
import com.mars_sim.core.tool.RandomUtil;

// Note: the newly surveyed ice deposit spans latitudes from 39 to 49 deg
// within the Utopia Planitia plains, as estimated by SHARAD, an subsurface
// sounding radar ice that penetrate below the surface. SHARAD was mounted
// on the Mars Reconnaissance Orbiter.

// See https://www.jpl.nasa.gov/news/news.php?feature=6680

// See https://github.com/mars-sim/mars-sim/issues/225 on past effort in finding elevation via color shaded maps

/**
 * The TerrainElevation class represents the surface terrain of the virtual
 * Mars. It provides information about elevation and terrain ruggedness and
 * calculate ice collection rate at a location on its vast surface.
 */
public class TerrainElevation implements Serializable {

	private static final long serialVersionUID = 1L;

//	private static final SimLogger logger = SimLogger.getLogger(TerrainElevation.class.getName());
	
	private static final double STEP_KM = 2;
	private static final double DEG_TO_RAD = Math.PI/180;
	private static final double RATE = 1;
	
	private Set<CollectionSite> sites;
	
	private Map<Coordinates, double[]> terrainProfileMap;

	private static UnitManager unitManager;

	
	/**
	 * Constructor.
	 */
	public TerrainElevation() {
		sites = new HashSet<>();
		terrainProfileMap = new HashMap<>();
	}

	/**
	 * Returns terrain steepness angle (in radians) from location by sampling a step distance in given
	 * direction.
	 *
	 * @param currentLocation  the coordinates of the current location
	 * @param currentDirection the current direction (in radians)
	 * @return terrain steepness angle (in radians) e.g. 180 deg is 3.14 rad
	 */
	public static double determineTerrainSteepness(Coordinates currentLocation, Direction currentDirection) {
		return determineTerrainSteepness(currentLocation, getMEGDRElevation(currentLocation), currentDirection);
	}

	/**
	 * Determines the terrain steepness angle (in radians) from location by sampling a step distance in given
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
		double elevationChange = getAverageElevation(sampleLocation) - elevation;
		// Compute steepness
		return Math.atan(elevationChange / STEP_KM);
	}

	/**
	 * Determines the terrain steepness angle (in radians) from location by sampling a random coordinate set and a step distance in given
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
		double elevationChange = getAverageElevation(sampleLocation) - elevation;
		return Math.atan(elevationChange / STEP_KM);
	}



	/**
	 * Gets the terrain profile of a location.
	 *
	 * @param {@link Coordinates} currentLocation
	 * @return an array of two doubles, namely elevation and steepness
	 */
	public double[] getTerrainProfile(Coordinates currentLocation) {
		if (!terrainProfileMap.containsKey(currentLocation)) {

			double steepness = 0;
			double elevation = getAverageElevation(currentLocation);
			for (int i=0 ; i <= 360 ; i++) {
				double rad = i * DEG_TO_RAD;
				steepness += Math.abs(determineTerrainSteepness(currentLocation, elevation, new Direction(rad)));
			}
	
			double[] terrain = {elevation, steepness};
					
			terrainProfileMap.put(currentLocation, terrain);
			
			return terrain;
		}
		
		return terrainProfileMap.get(currentLocation);
	}


	/**
	 * Computes the regolith collection rate of a location.
	 *
	 * @param site
	 * @param currentLocation
	 * @return regolith collection rate
	 */
	public void computeRegolithCollectionRate(CollectionSite site, Coordinates currentLocation) {

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
	public void computeIceCollectionRate(CollectionSite site, Coordinates currentLocation) {

		// Get the elevation and terrain gradient factor
		double[] terrainProfile = getTerrainProfile(currentLocation);

		// elevation in km
		double elevation = terrainProfile[0];
		double steepness = terrainProfile[1];
		double latitude = currentLocation.getLatitudeDouble();

		site.setElevation(elevation);
		site.setSteepness(steepness);

		double rate = RATE;

		// Note 1: Investigate how to adjust for seasonal variation of finding ice for north and south hemisphere
		// Note 2: The collection rate may be adjusted by relevant scientific studies

		// See https://agupubs.onlinelibrary.wiley.com/doi/full/10.1002/2013JE004482
		
		if (latitude < 50 && latitude > 40
				|| latitude > -74 && latitude < -70) {
			// At the edge of a region where impact exposures between 40-50°N are common
			// Ice becomes stable beneath a desiccated layer poleward of some point in the mid-latitudes, currently around ±40–50°.
			
			// The steeper the slope, the harder it is to retrieve the ice deposit
			rate *= 0.4 * (24 - elevation) + Math.abs(steepness) / 3.14 * 4;
		}
		
		else if (latitude < 65 && latitude > 50) {
			// In the mid-latitudes, surface ice is present in impact craters, steep scarps and gullies.
			
			// The steeper the slope, the harder it is to retrieve the ice deposit
			rate *= 0.1 * (24 - elevation) + Math.abs(steepness) / 3.14 * 4;
		}

		else if ((latitude >= 65 && latitude < 75)
			|| (latitude <= -65 && latitude > -75)) {
			
			rate *= 2 + RandomUtil.getRandomDouble(3) + Math.abs(24 - elevation) / 2.0 
					+ Math.abs(latitude) / 75.0 - Math.abs(steepness) / 3.14 * 4;
		}

		else if ((latitude >= 75 && latitude <= 90)
				|| (latitude <= -75 && latitude >= -90)) {
			
			// At latitudes near the poles, ice is present in glaciers.
			
			// Abundant ice is present beneath the permanent carbon dioxide ice cap at the Martian south pole.
		
			rate *= 5 + RandomUtil.getRandomDouble(5) + Math.abs(24 - elevation) + Math.abs(latitude) / 75.0;
		}

		rate = MathUtils.between(rate, 1, 100);

		site.setIceCollectionRate(rate);
	}

	/**
	 * Obtains the ice collection rate of a location.
	 *
	 * @param loc
	 * @return the collection rate
	 */
	public double obtainIceCollectionRate(Coordinates loc) {
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
	public double obtainRegolithCollectionRate(Coordinates loc) {
		CollectionSite site = getCollectionSite(loc);
		
		if (site.getRegolithCollectionRate() == -1)
			computeRegolithCollectionRate(site, loc);
		
		return site.getRegolithCollectionRate();
	}

	
    /** 
     * Returns the average elevation using both the topo map and MOLA data set.
     * 
     *  @return elevation in km.
     */
    public static double getAverageElevation(Coordinates location) {
    	return getMEGDRElevation(location);
    }

	/**
	 * Returns the elevation in km at the given location, based on MEGDR's dataset.
	 *
	 * @param location the location in question
	 * @return the elevation at the location (in km)
	 */
	public static double getMEGDRElevation(Coordinates location) {	
		
		if (unitManager == null)
			unitManager = Simulation.instance().getUnitManager();
			
		// Check if this location is a settlement
		Settlement s = unitManager.findSettlement(location);
				
		double MOLAHeight = 0;
		if (s != null) {
			MOLAHeight = s.getElevation();
		}
		else {
			MOLAHeight = getMOLAElevation(location.getPhi(), location.getTheta());	
		}
			
		return MOLAHeight;
	}

	/**
	 * Returns the elevation in km at the given location, based on MOLA's MEDGR dataset.
	 *
	 * @param phi
	 * @param theta
	 * @return the elevation at the location (in km)
	 */
	public static double getMOLAElevation(double phi, double theta) {
		return MEGDRFactory.getElevation(phi, theta)/1000.0;
	}

	
	public Set<CollectionSite> getCollectionSites() {
		return sites;
	}

	public void addCollectionSite(CollectionSite site) {
		sites.add(site);
	}

	public synchronized CollectionSite getCollectionSite(Coordinates newLocation) {
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
}
