/*
 * Mars Simulation Project
 * SurfaceFeatures.java
 * @date 2024-07-23
 * @author Scott Davis
 */
package com.mars_sim.core.environment;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.MasterClock;
import com.mars_sim.core.time.Temporal;
import com.mars_sim.core.tool.RandomUtil;


/**
 * SurfaceFeatures represents the surface terrain and landmarks of the virtual
 * Mars.
 */
public class SurfaceFeatures implements Serializable, Temporal {

	private static final long serialVersionUID = 1L;

	public static final SimLogger logger = SimLogger.getLogger(SurfaceFeatures.class.getName());

	public static final int OPTICAL_DEPTH_REFRESH = 3;

	/** Maximum mineral estimation */
	private static final double MINERAL_ESTIMATION_MAX = 100D;

	// This is the so-called "solar constant" of Mars (not really a constant per
	// se), which is the flux of solar radiation at the top of the atmosphere (TOA)
	// at the mean distance a between Mars and the sun.
	// Note: at the top of the Mars atmosphere
	// The solar irradiance at Mars' mean distance from the Sun (1.52 AU) is S0 =
	// 590 Wm-2.
	// This is about 44% of the Earth's solar constant (1350 Wm-2).
	// At perihelion (1.382 AU), the maximum available irradiance is S = 717 Wm-2,
	// while at apohelion (1.666 AU) the maximum is S = 493 Wm-2.
	// see
	// http://ccar.colorado.edu/asen5050/projects/projects_2001/benoit/solar_irradiance_on_mars.htm

	private static final double COSZ_THRESHOLD = 0.04;
	
	public static final int SUNLIGHT_THRESHOLD = 20;
	
	public static final int MEAN_SOLAR_IRRADIANCE = 586; // in flux or [W/m2] = 1371 / (1.52*1.52)
	
	public static final int MAX_SOLAR_IRRADIANCE = 717;
	
	public static final int MIN_SOLAR_IRRADIANCE = 493;

	private static final double HALF_PI = Math.PI / 2;

	private static final double THREE_HALF_PI = 1.5 * Math.PI;

	private static final double OPTICAL_DEPTH_STARTING = 0.2342;

	/** The most recent value of optical depth by Coordinate. */
	private transient Map<Coordinates, Double> opticalDepthMap = new HashMap<>();
	/** The most recent value of solar irradiance by Coordinate. */
	private transient Map<Coordinates, Double> currentIrradiance = new HashMap<>();
	
	// non-static instances
	private MineralMap mineralMap;
	private AreothermalMap areothermalMap;
	private Weather weather;
	private OrbitInfo orbitInfo;
	private TerrainElevation terrainElevation;
	
	private final ReentrantLock opticalDepthLock = new ReentrantLock(true);
	private final ReentrantLock sunlightLock = new ReentrantLock(true);

	/** The set of locations that have been declared as Region of Interest (ROI). */
	private List<ExploredLocation> regionOfInterestLocations;

	/**
	 * Constructor.
	 *
	 * @throws Exception when error in creating surface features.
	 */
	public SurfaceFeatures(MasterClock mc, OrbitInfo oi, Weather w) {
		orbitInfo = oi;
		weather = w;
		w.setSurfaceFeatures(this);
		
		terrainElevation = new TerrainElevation();
		mineralMap = new RandomMineralMap();
		regionOfInterestLocations = new ArrayList<>();
		areothermalMap = new AreothermalMap();
	}

	/**
	 * Returns the terrain elevation.
	 *
	 * @return terrain elevation
	 */
	public TerrainElevation getTerrainElevation() {
		if (terrainElevation == null) {
			terrainElevation = new TerrainElevation();
		}
		return terrainElevation;
	}


	/**
	 * Gets the optical depth due to the martian dust.
	 *
	 * @param location
	 * @return
	 */
	public double getOpticalDepth(Coordinates location) {
		Double value = opticalDepthMap.get(location);
		if (value != null)
			return value.doubleValue();

		opticalDepthLock.lock();
		
		double result = computeOpticalDepth(location);

		// Make cache thread safe as this may be done on demand
//		synchronized(opticalDepthMap)
			opticalDepthMap.put(location, result);
		
		opticalDepthLock.unlock();

		return result;
	}

	/**
	 * Computes the optical depth due to the martian dust.
	 *
	 * @param location
	 * @return tau
	 */
	private double computeOpticalDepth(Coordinates location) {

		double tau = 0;

		// Reference :
		// See Chapter 2.3.1 and equation (2.44,45) on page 63 from the book "Mars:
		// Prospective Energy and Material Resources" by Badescu, Springer 2009.
				
		// Optical depth is well correlated to the daily variation of surface pressure
		// and to the standard deviation of daily surface pressure
		// The lower the value of tau, the clearer the sky
				
		// Note: tau has an "inverse" relationship with the daily global solar
		// irradiance in Fig 2.8.

		double newTau = 0.2237 * weather.getDailyVariationAirPressure(location);

		// Equation: tau = 0.2342 + 0.2247 * yestersolAirPressureVariation
		// the starting value for opticalDepth is 0.2342. See Ref below
		if (opticalDepthMap.containsKey(location))
			tau = (.9 * opticalDepthMap.get(location) 
				 + .1 * (OPTICAL_DEPTH_STARTING 
						+ newTau
						+ weather.getWindSpeed(location) / 20));
		else {
			tau = OPTICAL_DEPTH_STARTING + newTau;
		}

		// Make tau oscillate between .1 and 6 
		if (tau > 3.0)
			tau = tau - RandomUtil.getRandomDouble((6.0 - tau)/36.0);
		else 
			tau = tau + RandomUtil.getRandomDouble((tau - .1)/36.0);
		
		// According to R. M. Haberlet Et al. Page 860, Table IV,
		// tau is usually between .1 and 6 on the surface of Mars
		
		// Notes:
		// (1) during relatively periods of clear sky, typical values for optical depth
		// were between 0.2 and 0.5
		// (2) typical observable range is between .32 and .52 (average is 42%).
		// (3) From Viking data, at no time did the optical depth fall below 0.18

		// Based on https://www.researchgate.net/publication/343794062_The_Mars_Dust_Activity_Database_MDAD_A_comprehensive_statistical_study_of_dust_storm_sequences
		
		// 1. Period of major dust storms Between L_s = 215 to L_s = 305
		// 2. At L_s = 250.87, which is the perihelion, dust storm occurrence is the highest
		// 3. In northern hemisphere, organized dust storm sequences are most prevalent 
		//    during fall and late winter between Ls (140 to 250) and Ls (300 to 360).
		// 4. For southern hemisphere, it's between Ls (10 to 70) and Ls (120 to 180).
		// 5. Dust storm sequences most often originate in Acidalia, the ASV region, Utopia, Hellas, Arcadia.
		// 6. Dust storm sequences occur in three seasonal windows, with L=140—250 the primary season.
		
		// Get the instantaneous areocentric longitude
		double areoLon = Math.round(orbitInfo.getSunAreoLongitude());
		
		double phi = location.getPhi();
		boolean isNorthern = false;

		if (phi >= 0 && phi <= HALF_PI) {
			isNorthern = true;
		}
		
		if ((isNorthern && (areoLon > 140 && areoLon < 250) || (areoLon > 300 && areoLon < 360))
			|| (!isNorthern && (areoLon > 10 && areoLon < 70) || (areoLon > 120 && areoLon < 180))) {
			tau = tau + RandomUtil.getRandomDouble((tau - .1)/36.0);
		}
		
		tau = Math.round(tau * 1000.0)/1000.0;
		
		return tau;
	}

	/**
	 * Returns a float value representing the current sunlight conditions at a
	 * particular location.
	 *
	 * @return value from 0.0 - 1.0; 0.0 represents night time darkness. 1.0
	 *         represents daylight. Values in between 0.0 and 1.0 represent twilight
	 *         conditions.
	 */
	public double getSurfaceSunlightRatio(Coordinates location) {
		double result;

		// Method 2:
		double z = orbitInfo.getSolarZenithAngle(location);

		if (z < 1.4708) {
			result = 1D;
		} else if (z > 1.6708) {
			result = 0D;
		} else {
			result = 8.354 - 5 * z;
		}

		return result;
	}

	/**
	 * Calculates the solar irradiance ratio (between 0 and 1) at a particular location on Mars.
	 *
	 * @param location the coordinate location on Mars.
	 * @return (between 0 and 1)
	 */
	public double getSunlightRatio(Coordinates location) {
		  return Math.round(getSolarIrradiance(location)
				  / MAX_SOLAR_IRRADIANCE * 100D)/100D;
	}

	/**
	 * Calculates the solar irradiance at a particular location on Mars.
	 *
	 * @param location the coordinate location on Mars.
	 * @return solar irradiance (W/m2)
	 */
	public double getSolarIrradiance(Coordinates location) {
		Double cachedValue = currentIrradiance.get(location);
		if (cachedValue != null)
			return cachedValue.doubleValue();

		sunlightLock.lock();
			
		double result = calculateSolarIrradiance(location);
		// Make cache thread safe as this may be done on demand
//		synchronized(currentIrradiance)
			currentIrradiance.put(location, result);
			
		sunlightLock.unlock();

		return result;
	}

	/**
	 * Calculates the solar irradiance.
	 *
	 * @param location
	 * @return
	 */
	private double calculateSolarIrradiance(Coordinates location) {
		
		// Approach 2 consists of 5 parts
		
		// g0: direct solar irradiance at the top of the atmosphere
		double g0 = 0;
		// gh: global irradiance on a horizontal surface
		double gh = 0;
		// gbh: direct beam irradiance on a horizontal surface
		double gbh = 0;
		// gdh: diffuse irradiance on a horizontal surface
		double gdh = 0;
		
		// PART 1 : COSINE SOLAR ZENITH ANGLE

		// Obtains the cosine solar zenith angle
		double cosZ = orbitInfo.getCosineSolarZenithAngle(location);
		// Find zenith angle (from 0 to 2 pi)
		double z = Math.acos(cosZ);
		// Gets the areocentric longitude
		double areoLon = orbitInfo.getSunAreoLongitude();
		// Gets the sunlight mod due to areoLon
		double mod = 1.35 * (.5 * Math.sin((areoLon - 251.2774) * OrbitInfo.DEGREE_TO_RADIAN) + .5);
			
		if ((z >= HALF_PI - 0.1) && (z <= HALF_PI + 0.1)) {
			// Case A : sunrise twilight zone
			// Set it to a maximum of 12 degree below the horizon
			// Note: 0.1 radian is ~5.7 deg
			
			// Indirect sunlight such as diffuse/scattering/reflective sunlight will light
			// up the Martian sky
			
			// Note: twilight zone is defined as between 0.1 to -0.1 in radians above and below
			// the horizon
			// z0 ranges from 0 to 40 at sunrise (from 342 msol to 358 msol)
			double z0 = Math.round((-200 * z + 100 * Math.PI + SUNLIGHT_THRESHOLD)* 10.0)/10.0;
			
			// Note: need to keep a minimum of G_h at 20 W/m2 if the sun is within the twilight zone
			// z1 ranges from 188 to 178 at sunrise
			
			if (cosZ < 0)
				cosZ = COSZ_THRESHOLD * z0/SUNLIGHT_THRESHOLD;
		}
		
		else if ((z >= THREE_HALF_PI - 0.1) && (z <= THREE_HALF_PI + 0.1)) {
			// Case B : sunset twilight zone
			// Set it to a maximum of 12 degree below the horizon
			// Note: 0.1 radian is ~5.7 deg
			
			// Indirect sunlight such as diffuse/scattering/reflective sunlight will light
			// up the Martian sky
			
			// Note: twilight zone is defined as between 0.1 to -0.1 in radians above and below
			// the horizon
			// z0 ranges from 0 to 40 at sunrise (from 342 msol to 358 msol)
			double z0 = Math.round((-200 * z + 100 * Math.PI + 20)* 10.0)/10.0;
			
			// Note: need to keep a minimum of G_h at 20 W/m2 if the sun is within the twilight zone
			// z1 ranges from 188 to 178 at sunrise
			// This is an arbitrary model, set G_0 to 41.8879 W/ m-2 when Mars is at the horizon
			if (cosZ < 0)
				cosZ = COSZ_THRESHOLD * z0/SUNLIGHT_THRESHOLD;
		}
		
		if ((z < HALF_PI + 0.1) || (z > THREE_HALF_PI - 0.1)) {
			// Case C : sun-in-the sky zone
			
			// Also cosZ becomes +ve
			
			// Part 2: get the new average solar irradiance as a result of the changing
			// distance between Mars and Sun with respect to the value of L_s.
			
			// Note a: Because of Mars's orbital eccentricity, L_s advances somewhat
			// unevenly with time, but can be evaluated
			// as a trigonometric power series for the orbital eccentricity and the orbital
			// mean anomaly measured with respect to the perihelion.
			// The areocentric longitude at perihelion, L_s = 251.000 + 0.00645 * (year -
			// 2000),
			// indicates a near alignment of the planet's closest approach to the Sun in its
			// orbit with its winter solstice season,

			// Note b: In 2043, there is 35% (max is 45.4%) on average more sunlight at
			// perihelion (L_s = 251.2774 deg) than at aphelion (L_s = 71.2774 deg)
			// Equation: 135% * (.5 * sin (L_s - 251.2774 + 180 - 90) + .5)
			
			// Part 3: get the instantaneous radius and semi major axis	
			if (cosZ < 0)
				cosZ = COSZ_THRESHOLD;
			
			g0 = cosZ * MIN_SOLAR_IRRADIANCE * mod;		

			// PART 4 : OPTICAL DEPTH - CALCULATING ABSORPTION AND SCATTERING OF SOLAR
			// RADIATION

			double tau = getOpticalDepth(location);
		
			// For future,
			// Part 4a : Reduce the opacity of the Martian atmosphere due to local dust
			// storm

			// Note 1 : The extinction of radiation through the Martian atmosphere is caused
			// mainly by suspended dust particles.
			
			// Although dust particles are effective at scattering direct solar irradiance,
			// a substantial amount of diffuse light is able to penetrate to the surface of
			// the planet.
			
			// The amount of PAR available on the Martian surface can then be calculated to
			// be 42% of the total PAR to reach the surface.

			// Note 2: Based on Viking observation, it's estimated approximately 100 local
			// dust storms (each last a few days) can occur in a given Martian year
			// Duration of a global dust storm is 35 - 70 sols. Local dust storms last a few
			// days.

			// Note 3: In future, model how dust clouds, water/ice clouds, CO2 clouds affects tau
			// differently
			
			// REFERENCE: http://www.sciencedirect.com/science/article/pii/S0019103514001559
			// The solar longitude (LS) 20 deg 136 deg period is also characterized by the
			// presence of cirriform clouds at the Opportunity site,
			// especially near LS = 50 deg and 115 deg. In addition to water ice clouds, a
			// water ice haze may also be present, and carbon dioxide clouds
			// may be present early in the season.

			// Choice 1 : if using Beer's law : transmissivity = Math.exp(-tau/cos_z)
			// G_bh = G_0 * cos_z * Math.exp(-tau/cos_z)

			// Choice 2 : The pure scattering transmissivity = (1 + tau / 2 / cos_z)^ -1

			if (cosZ != 0)
				gbh = g0 * cosZ / (1 + tau / 2.0 / cosZ);

			// Assuming the reflection from the surface is negligible
	
			// Note: m(z), the air mass, is estimated as ~ 1/cos_z

			// In future, one can estimate m(z), the air mass, as ~ 1/cos_z

			// PART 5 : DIFFUSE SOLAR IRRADIANCE

			// G_h = G_direct + G_diffuse
			// On earth, the direct solar irradiance plays the major role of bringing in
			// sunlight
			// On Mars, the role of diffuse solar irradiance is more prominent than that on
			// Earth.
			
			gdh = gbh * (-0.822 * cosZ + 1);
			
			// Finally,
			gh = gbh + gdh;
			
			// Future: Modeling the diffuse effect of solar irradiance with formula
			// Note: the value of G_dh to decrease more slowly when value cos_z is
			// diminishing

			// Assume a linear relationship, use the formula y = mx + c
			// Let x = G_dh/G_bh, y = cos_z
	    	// Set cos_z = .9, G_dh = G_bh / 6 = 0.167 * G_bh
	    	// Set cos_z = 0, G_dh = G_bh

			// For future,
			// Part 6 : calculate other components on Mars such as twilight and
			// reflective irradiance

			// Note: A lot of code use of this method depends on dark night time = 0 solar
			// irradiance. If we want to
			// have scattering produce > 0 irradiance at full night time, we need to modify
			// code calling this method
			// as necessary for night time indication. - Scott

		}

		if (gh > MAX_SOLAR_IRRADIANCE)
			gh = MAX_SOLAR_IRRADIANCE;
		
		else if (gh < 0)
			gh = 0;

		// Take the average of the last and current irradiance
		Double lastGh = currentIrradiance.get(location);
		if (lastGh != null)
			gh = (gh + lastGh.doubleValue()) / 2;
		return gh;
	}

	/**
	 * Returns true if location is in a dark polar region. A dark polar region is
	 * where the sun doesn't rise in the current sol.
	 *
	 * @return true if location is in dark polar region
	 */
	public boolean inDarkPolarRegion(Coordinates location) {

		boolean result = false;

		Coordinates sunDirection = orbitInfo.getSunDirection();

		double sunPhi = sunDirection.getPhi();
		double darkPhi;

		if (sunPhi < (HALF_PI)) {
			darkPhi = Math.PI - ((HALF_PI) - sunPhi);
			if (location.getPhi() >= darkPhi) {
				result = true;
			}
		} else {
			darkPhi = sunPhi - (HALF_PI);
			if (location.getPhi() < darkPhi) {
				result = true;
			}
		}

		return result;
	}



	/**
	 * Checks if location is within a polar region of Mars.
	 *
	 * @param location the location to check.
	 * @return true if in polar region.
	 */
	public boolean inPolarRegion(Coordinates location) {
		double polarPhi = .1D * Math.PI;

		return (location.getPhi() < polarPhi) || (location.getPhi() > Math.PI - polarPhi);
	}

	/**
	 * Gets the mineral map.
	 *
	 * @return mineral map.
	 */
	public MineralMap getMineralMap() {
		return mineralMap;
	}

	/**
	 * Declares a site a region of interest and generates an initial estimate of mineral contents.
	 *
	 * @param location		the location coordinates.
	 * @param skill			the skill
	 * @param settlement    the settlement staking the claim on this location
	 * @return the explored location
	 */
	public synchronized ExploredLocation declareRegionOfInterest(Coordinates location,
			int skill, Settlement settlement) {
		
		if (location == null) {
			logger.info(settlement, "location is null.");
			return null;
		}
		
		ExploredLocation result = null;
		
		String [] mineralTypes = mineralMap.getMineralTypeNames();
		
		Map<String, Double> initialMineralEstimations = new HashMap<>(mineralTypes.length);
		
		double totalConc = 0;
		
		for (String mineralType : mineralTypes) {
			
			double initialEst = mineralMap.getMineralConcentration(mineralType, location);

			if (initialEst <= 0) {
				continue;
			}
			
			double variance = 10.0 / (1 + skill);
			// With no improvements the estimates are capped
			if (variance > 10) {
				variance = 10;
			}
			
			double newEst = initialEst + initialEst * RandomUtil.getRandomDouble(-variance, variance);
			
			if (newEst < 0)
				newEst = 0;
			else if (newEst > MINERAL_ESTIMATION_MAX) {
				newEst = MINERAL_ESTIMATION_MAX;
			}
			
			initialMineralEstimations.put(mineralType, newEst);
			
			totalConc += newEst;
		}

		if (totalConc > 0) {
						
			result = new ExploredLocation(location, skill, initialMineralEstimations, settlement);
			
			regionOfInterestLocations.add(result);
		}
		else {
			logger.info(settlement, "Initially found no mineral concentrations in " + location.getFormattedString() + ".");
		}
		
		return result;
	}

	/**
	 * Has this site been declared as a region of interest with a specified claimed status ?
	 *
	 * @param siteLocation
	 * @return
	 */
	public boolean isDeclaredARegionOfInterest(Coordinates coord, Settlement settlement, boolean isClaimed) {
		for (ExploredLocation el: regionOfInterestLocations) {
			if (el.getLocation().equals(coord)
				&& el.isClaimed() == isClaimed
				&& el.getSettlement().equals(settlement)) {
				return true;
			}
		}

		return false;
	}
	
	/**
	 * Check if a location already been declared/considered as a Region Of Interest (ROI) by a settlement with a specified claimed status.
	 * 
	 * @param coord
	 * @param settlement
	 * @param isClaimed
	 * @return
	 */
	private ExploredLocation checkDeclaredLocation(Coordinates coord, Settlement settlement, boolean isClaimed) {
		return regionOfInterestLocations.stream()
				  .filter(e -> e.getLocation().equals(coord)
						  && e.isClaimed() == isClaimed
						  && e.getSettlement().equals(settlement))
				  .findFirst()
				  .orElse(null);
	}

	
	/**
	 * Creates a Region of Interest (ROI) at a given location and
	 * estimate its mineral concentrations.
	 * 
	 * @param siteLocation
	 * @param skill
	 * @return ExploredLocation
	 */
	public ExploredLocation createARegionOfInterest(Coordinates siteLocation, Settlement settlement, int skill) {

		// Check if this siteLocation has already been added or not to SurfaceFeatures
		ExploredLocation el = checkDeclaredLocation(siteLocation, settlement, false);
		if (el == null) {
			// If it hasn't been claimed yet, then claim it
			el = declareRegionOfInterest(siteLocation,
					skill, settlement);
		}
		
		return el;
	}

	/**
	 * Gets a set of all Regions of Interest (ROI) available in a simulation, regardless of its claimed status.
	 *
	 * @return list of explored locations.
	 */
	public List<ExploredLocation> getAllPossibleRegionOfInterestLocations() {
		return regionOfInterestLocations;
	}

	/**
	 * Gets the areothermal heat potential for a given location.
	 *
	 * @param location the coordinate location.
	 * @return areothermal heat potential as percentage (0% - low, 100% - high).
	 */
	public double getAreothermalPotential(Coordinates location) {
		return areothermalMap.getAreothermalPotential(location);
	}

	/**
	 * Gets the orbit info instance.
	 * 
	 * @return
	 */
	public OrbitInfo getOrbitInfo() {
		return orbitInfo;
	}
	
	/**
	 * Time passing in the simulation.
	 *
	 * @param time time in millisols
	 * @throws Exception if error during time.
	 */
	@Override
	public boolean timePassing(ClockPulse pulse) {

		if (pulse.isNewIntMillisol()) {
			double msol = pulse.getMarsTime().getMillisolInt();
			
			// the value of optical depth doesn't need to be refreshed too often
			if (msol % OPTICAL_DEPTH_REFRESH == 0) {
				// Clear entries
				opticalDepthMap.clear();
			}
			
			// Clear entries
			currentIrradiance.clear();
		}
		
		return true;
	}

	/**
	 * Executes the followings when deserializing this class. 
	 * 
	 * @param aInputStream
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	private void readObject(ObjectInputStream aInputStream) throws ClassNotFoundException, IOException {   
		// Perform the default de-serialization first
	    aInputStream.defaultReadObject();
	    
		opticalDepthMap = new HashMap<>();
		currentIrradiance = new HashMap<>();
	}
	 
	/**
	 * Prepares object for garbage collection.
	 */
	public void destroy() {

		opticalDepthMap.clear();
		opticalDepthMap = null;
		currentIrradiance.clear();
		currentIrradiance = null;
		mineralMap.destroy();
		mineralMap = null;
		regionOfInterestLocations.clear();
		regionOfInterestLocations = null;
		areothermalMap.destroy();
		areothermalMap = null;
		
		weather.destroy();
		weather = null;
		orbitInfo.destroy();
		orbitInfo = null;

	}
}
