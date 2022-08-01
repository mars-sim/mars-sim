/*
 * Mars Simulation Project
 * SurfaceFeatures.java
 * @date 2022-07-25
 * @author Scott Davis
 */
package org.mars_sim.msp.core.environment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.Temporal;
import org.mars_sim.msp.core.tool.RandomUtil;


/**
 * SurfaceFeatures represents the surface terrain and landmarks of the virtual
 * Mars.
 */
public class SurfaceFeatures implements Serializable, Temporal {

	private static final long serialVersionUID = 1L;

	// May add back SimLogger logger = SimLogger.getLogger(SurfaceFeatures.class.getName())

	public static final int MEAN_SOLAR_IRRADIANCE = 586; // in flux or [W/m2] = 1371 / (1.52*1.52)

	/** Maximum mineral concentration estimation diff from actual. */
	private static final int MINERAL_ESTIMATION_VARIANCE = 2;

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

	public static final int MAX_SOLAR_IRRADIANCE = 717;

	private static final double HALF_PI = Math.PI / 2d;

	private static final double FACTOR = MEAN_SOLAR_IRRADIANCE * OrbitInfo.SEMI_MAJOR_AXIS * OrbitInfo.SEMI_MAJOR_AXIS;

	private static final double OPTICAL_DEPTH_STARTING = 0.2342;

	private boolean isNewMSol;

	// non static instances
	private MineralMap mineralMap;
	private AreothermalMap areothermalMap;

	/** The locations that have been explored and/or mined. */
	private List<ExploredLocation> exploredLocations;
	/** The most recent value of optical depth by Coordinate. */
	private Map<Coordinates, Double> opticalDepthMap;
	/** The most recent value of solar irradiance by Coordinate. */
	private Map<Coordinates, Double> currentIrradiance;
	/** The cache value of solar irradiance by Coordinate. */
	private Map<Coordinates, Double> irradianceCache;
	/** The trend value of lightness by Coordinate. */
	private Map<Coordinates, Double> lightTrend;

	private static MarsClock currentTime;

	private static TerrainElevation terrainElevation;
	private static Weather weather;
	private static OrbitInfo orbitInfo;

	private static List<Landmark> landmarks = null;

	/**
	 * Constructor.
	 *
	 * @throws Exception when error in creating surface features.
	 */
	public SurfaceFeatures() {

		// Initialize instances.
		terrainElevation = new TerrainElevation();
		mineralMap = new RandomMineralMap();
		exploredLocations = new ArrayList<>(); // will need to make sure explored locations are serialized
		areothermalMap = new AreothermalMap();
		irradianceCache = new HashMap<>();
		currentIrradiance = new HashMap<>();
		opticalDepthMap = new HashMap<>();
		lightTrend = new HashMap<>();

//		double a = OrbitInfo.SEMI_MAJOR_AXIS;
//		factor = MEAN_SOLAR_IRRADIANCE * a * a;
	}

	/**
	 * Initializes transient data in the simulation.
	 *
	 * @param landmarkConfig
	 * @param mc
	 * @param oi
	 * @param w
	 */
	public static void initializeInstances(LandmarkConfig landmarkConfig, 
			MarsClock mc, OrbitInfo oi, Weather w) {
		landmarks = landmarkConfig.getLandmarkList();
		orbitInfo = oi;
		weather = w;
		currentTime = mc;
	}

	/**
	 * Returns the terrain elevation
	 *
	 * @return terrain elevation
	 */
	public TerrainElevation getTerrainElevation() {
		return terrainElevation;
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
		double result = 1D;

		// Method 1:
//        double angleFromSun = sunDirection.getAngle(location);
//        //System.out.print ("z1 : "+  Math.round(angleFromSun * 180D / Math.PI * 1000D)/1000D + "   ");
//
//        double twilightzone = .2D; // or ~6 deg // Angle width of twilight border (radians)
//
//        if (angleFromSun < (HALF_PI) - (twilightzone / 2D)) {
//            result = 1D;
//        } else if (angleFromSun > (HALF_PI) + (twilightzone / 2D)) {
//            result = 0D;
//        } else {
//            double twilightAngle = angleFromSun - ((HALF_PI) - (twilightzone / 2D));
//            result = 1D - (twilightAngle / twilightzone);
//        }
//
//        // NOTE: Below is the numerically shortened equivalent of the above if-else block
//        if (angleFromSun  < 1.4708) {
//            result = 1D;
//        } else if (angleFromSun  > 1.6708) {
//            result = 0D;
//        } else {
//            //double twilightAngle = z - 1.6708;
//            result = 8.354 - 5 * angleFromSun;
//        }

		// Method 2:
		double z = orbitInfo.getSolarZenithAngle(location);
		// System.out.println("z2 : " + Math.round(z * 180D / Math.PI * 1000D)/1000D);

		// double twilight zone = .2D;
		if (z < 1.4708) {
			result = 1D;
		} else if (z > 1.6708) {
			result = 0D;
		} else {
			// double twilightAngle = z - 1.6708;
			result = 8.354 - 5 * z;
		}

		return result;
	}

	/**
	 * Gets the optical depth due to the martian dust.
	 *
	 * @param location
	 * @return
	 */
	public double getOpticalDepth(Coordinates location) {
		if (opticalDepthMap.containsKey(location))
			return opticalDepthMap.get(location);
		else {
			return computeOpticalDepth(location);
		}
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

		// Equation: tau = 0.2342 + 0.2247 * yestersolAirPressureVariation;
		// the starting value for opticalDepth is 0.2342. See Ref below
		if (opticalDepthMap.containsKey(location))
			tau = (opticalDepthMap.get(location) + OPTICAL_DEPTH_STARTING + newTau) / 1.9D;
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
		double areoLon = Math.round(orbitInfo.getL_s() * 1000.0)/1000.0;
		
		double phi = location.getPhi();
		boolean isNorthern = false;

		if (phi >= 0 && phi <= HALF_PI) {
			isNorthern = true;
		}
		if (isNorthern && (areoLon > 140 && areoLon < 250)
				|| (areoLon > 300 && areoLon < 360))
			tau = tau + RandomUtil.getRandomDouble((tau - .1)/36.0);
		else if (!isNorthern && (areoLon > 10 && areoLon < 70)
				|| (areoLon > 120 && areoLon < 180)) {
			tau = tau + RandomUtil.getRandomDouble((tau - .1)/36.0);
		}
		
		// Save tau onto opticalDepthMap
		opticalDepthMap.put(location, tau);
		
		return tau;
	}


	/**
	 * Gets the trend (positive if getting bright, negative if getting dark).
	 *
	 * @param location
	 * @return a number
	 */
	public double getTrend(Coordinates location) {
		// If the radiance is below 12 and is decreasing
		// then it's getting dark

		if (lightTrend.containsKey(location)) {
			return lightTrend.get(location);
		}

		// If no trend then assume constant light
		return 0;
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
		if (location == null)
			return 0;

		if (isNewMSol) {
			if (!currentIrradiance.isEmpty() && !irradianceCache.isEmpty()) {
				double current = currentIrradiance.get(location);
				double past = irradianceCache.get(location);
				double pastTrend = 0;
				if (!lightTrend.isEmpty()) {
					pastTrend = lightTrend.get(location);
				}
				// Save the trend value
				lightTrend.put(location, ((current - past) + pastTrend)/2.0);
				// Update the irradiance
				irradianceCache = currentIrradiance;
				// Clear the current cache value of solar irradiance of all settlements
				currentIrradiance.clear();
			}

			// If location is not in cache, calculate the solar irradiance
			return calculateSolarIrradiance(location);
		}

		Double d = currentIrradiance.get(location);
		if (d != null) {
			return d;
		}

		// If location is not in cache, calculate the solar irradiance
		return calculateSolarIrradiance(location);
	}

	/**
	 * Calculates the solar irradiance.
	 *
	 * @param location
	 * @return
	 */
	private double calculateSolarIrradiance(Coordinates location) {
		// Approach 1
//		double s1 = 0;
//      double L_s = mars.getOrbitInfo().getL_s();
//      double e = OrbitInfo.ECCENTRICITY;
//    	double z = mars.getOrbitInfo().getSolarZenithAngle(phi);
//    	double num =  1 + e * Math.cos( (L_s - 248) /180D * Math.PI);
//    	double den = 1 - e * e;
//    	s1 = MEAN_SOLAR_IRRADIANCE * Math.cos(z) * num / den * num / den  ;

		// Approach 2 consists of 5 parts
		// PART 1 : COSINE SOLAR ZENITH ANGLE
		
		// G_0: direct solar irradiance at the top of the atmosphere
		double G_0 = 0;
		// G_h: global irradiance on a horizontal surface
		double G_h = 0;
		// G_bh: direct beam irradiance on a horizontal surface
		double G_bh = 0;
		// G_dh: diffuse irradiance on a horizontal surface
		double G_dh = 0;

		// Obtains the cosine solar zenith angle
		double cos_z = orbitInfo.getCosineSolarZenithAngle(location);
		// Find zenith
		double z = Math.acos(cos_z);

		if (z >= HALF_PI) {
			// if Mars is in the so-called twilight zone,
			// then set it to a maximum of 12 degree below the horizon
			
			// Indirect sunlight such as diffuse/scattering/reflective sunlight will light
			// up the Martian sky
			if (z <= HALF_PI + .1) {
				// Note: twilight zone is defined as between 0.1 to -0.1 in radians above and below
				// the horizon
				G_h = Math.round((-200 * z + 100 * Math.PI + 20) * 100.00) / 100.00; 
				
				// Note: need to keep a minimum of G_h at 20 W/m2 if the sun is within the twilight zone
				// G_h = Math.round((0.2094 + z)*100D);
				// This an arbitrary model set G_0 to 41.8879 W/ m-2 when Mars is at the horizon
			}
		}

		else {

			// Part 2: get the new average solar irradiance as a result of the changing
			// distance between Mars and Sun with respect to the value of L_s.
			// double L_s = orbitInfo.getL_s();

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
			// Equation: 135% * (.5 * sin (L_s - 251.2774 + 180 - 90) + .5 )

			// Part 3: get the instantaneous radius and semi major axis
			double r = orbitInfo.getDistanceToSun();

			G_0 = cos_z * FACTOR / r / r;

			// if (G_0 <= 0)
			// G_0 = 0;

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

			// Choice 1 : if using Beer's law : transmissivity = Math.exp(-tau/cos_z);
			// G_bh = G_0 * cos_z * Math.exp(-tau/cos_z);

			// Choice 2 : The pure scattering transmissivity = (1 + tau / 2 / cos_z)^ -1
			G_bh = G_0 * cos_z / (1 + tau / 2.0 / cos_z);

			// Assuming the reflection from the surface is negligible
	
			// Note: m(z), the air mass, is estimated as ~ 1/cos_z

			// In future, one can estimate m(z), the air mass, as ~ 1/cos_z

			// PART 5 : DIFFUSE SOLAR IRRADIANCE

			// G_h = G_direct + G_diffuse
			// On earth, the direct solar irradiance plays the major role of bringing in
			// sunlight
			// On Mars, the role of diffuse solar irradiance is more prominent than that on
			// Earth.

			// Future: Modeling the diffuse effect of solar irradiance with formula
			// Note: the value of G_dh to decrease more slowly when value cos_z is
			// diminishing

			// Assume a linear relationship, use the formula y = mx + c
			// Let x = G_dh/G_bh, y = cos_z
	    	// Set cos_z = .9, G_dh = G_bh / 6 = 0.167 * G_bh;
	    	// Set cos_z = 0, G_dh = G_bh;	
			
			if (cos_z > 0)
				G_dh = G_bh * (-0.822 * cos_z + 1);
			else
				G_dh = G_bh;
			
			// Finally,
			G_h = G_bh + G_dh;

			if (G_h > MAX_SOLAR_IRRADIANCE)
				G_h = MAX_SOLAR_IRRADIANCE;

			else if (G_h < 20.94)
				G_h = 20.94;

			// System.out.println(" radiusAndAxis : " + fmt3.format(radiusAndAxis)
			// + " cos_z : "+ fmt3.format(cos_z)
			// + " G_0 : " + fmt3.format(G_0)
			// + " G_bh : " + fmt3.format(G_bh)
			// + " G_dh : " + fmt3.format(G_dh)
			// + " G_h : " + fmt3.format(G_h));

			// For future,
			// Part 6 : calculate other components on Mars such as twilight and
			// reflective irradiance

			// Note: A lot of code use of this method depends on dark night time = 0 solar
			// irradiance. If we want to
			// have scattering produce > 0 irradiance at full night time, we need to modify
			// code calling this method
			// as necessary for night time indication. - Scott
		}

		if (G_h < 0)
			G_h = 0;

		// Save the value in the cache
		currentIrradiance.put(location, G_h);
		
		return G_h;
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
		double darkPhi = 0D;

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
	 * Estimates when the sunrise is for this location.
	 * 
	 * @param location
	 * @return
	 */
	public MarsClock getSunRise(Coordinates location) {
		Coordinates sunDirection = orbitInfo.getSunDirection();

		// Sun Theta decreases over time so the Sun theta will be greater
		// Move the location 1/4 global closer to the sun so sunrise.
		// Allow for twilight
		double sunTheta = sunDirection.getTheta();
		double gapTheta = sunTheta - (location.getTheta() + HALF_PI - 0.2);

		if (gapTheta < 0) {
			// Gone round the planet,
			gapTheta += (2 * Math.PI);
		}

		// Get the time as a ratio of the global times msols per day
		double timeToSunRise = (gapTheta * 1000D)/(2 * Math.PI);

		MarsClock sunRise = (MarsClock) currentTime.clone();
		sunRise.addTime(timeToSunRise);
		return sunRise;
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
	 * Gets a list of landmarks on Mars.
	 *
	 * @return list of landmarks.
	 */
	public List<Landmark> getLandmarks() {
		return landmarks;
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
	 * Adds an explored location.
	 *
	 * @param location                       the location coordinates.
	 * @param estimationImprovement			 The number times the estimates have been improved
	 * @param estimatedMineralConcentrations a map of all mineral types and their
	 *                                       estimated concentrations (0% -100%)
	 * @param settlement                     the settlement the exploring mission is
	 *                                       from.
	 * @return the explored location
	 */
	public ExploredLocation addExploredLocation(Coordinates location,
			int estimationImprovement, Settlement settlement) {
		String [] mineralTypes = mineralMap.getMineralTypeNames();
		Map<String, Double> initialMineralEstimations = new HashMap<>(mineralTypes.length);
		for (String mineralType : mineralTypes) {
			double estimation = mineralMap.getMineralConcentration(mineralType, location);

			// Estimations are zero for initial site.
			int varianceMax = (10 - estimationImprovement) * MINERAL_ESTIMATION_VARIANCE;
			if (varianceMax > 0) {
 				estimation += RandomUtil.getRandomDouble(varianceMax);
			}

			// With no improvements the estimates are capped
			if (estimation < 0D)
				estimation = 0D - estimation;
			else if (estimation > MINERAL_ESTIMATION_MAX) {
				estimation = MINERAL_ESTIMATION_MAX - estimation;
			}
			initialMineralEstimations.put(mineralType, estimation);
		}

		ExploredLocation result = new ExploredLocation(location, estimationImprovement, initialMineralEstimations, settlement);
		synchronized (exploredLocations) {
			exploredLocations.add(result);
		}
		return result;
	}

	/**
	 * Check if an explored location already exists.
	 * 
	 * @param c
	 * @return
	 */
	public ExploredLocation getExploredLocation(Coordinates c) {
		synchronized (exploredLocations) {
		return exploredLocations.stream()
				  .filter(e -> c.equals(e.getLocation()))
				  .findAny()
				  .orElse(null);
		}
	}

	/**
	 * Gets a list of all explored locations on Mars.
	 *
	 * @return list of explored locations.
	 */
	public List<ExploredLocation> getExploredLocations() {
		return exploredLocations;
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
	 * Time passing in the simulation.
	 *
	 * @param time time in millisols
	 * @throws Exception if error during time.
	 */
	@Override
	public boolean timePassing(ClockPulse pulse) {

		isNewMSol = pulse.isNewMSol();

		if (isNewMSol) {
			// Avoid ConcurrentModificationException with a new HashSet
			Set<Coordinates> coords = new HashSet<>(opticalDepthMap.keySet());
			// Recompute the optical depth for each msol
			for (Coordinates location: coords) {
				computeOpticalDepth(location);
			}
		}
		
		return true;
	}

	/**
	 * Prepares object for garbage collection.
	 */
	public void destroy() {

		opticalDepthMap.clear();
		opticalDepthMap = null;
		irradianceCache.clear();
		irradianceCache = null;
		currentIrradiance.clear();
		currentIrradiance = null;

		mineralMap.destroy();
		mineralMap = null;
		exploredLocations.clear();
		exploredLocations = null;
		areothermalMap.destroy();
		areothermalMap = null;

		weather = null;
		orbitInfo = null;

		landmarks.clear();
		landmarks = null;
	}
}
