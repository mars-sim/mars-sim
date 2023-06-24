/*
 * Mars Simulation Project
 * SurfaceFeatures.java
 * @date 2022-07-25
 * @author Scott Davis
 */
package org.mars_sim.msp.core.environment;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.ai.mission.Mining;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.MarsTime;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.core.time.Temporal;
import org.mars_sim.msp.core.tool.RandomUtil;


/**
 * SurfaceFeatures represents the surface terrain and landmarks of the virtual
 * Mars.
 */
public class SurfaceFeatures implements Serializable, Temporal {

	private static final long serialVersionUID = 1L;

	public static final SimLogger logger = SimLogger.getLogger(SurfaceFeatures.class.getName());

	public static final int OPTICAL_DEPTH_REFRESH = 3;

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

	private static final double COSZ_THRESHOLD = 0.04;
	
	public static final int SUNLIGHT_THRESHOLD = 20;
	
	public static final int MEAN_SOLAR_IRRADIANCE = 586; // in flux or [W/m2] = 1371 / (1.52*1.52)
	
	public static final int MAX_SOLAR_IRRADIANCE = 717;
	
	public static final int MIN_SOLAR_IRRADIANCE = 493;

	private static final double HALF_PI = Math.PI / 2;

	private static final double THREE_HALF_PI = 1.5 * Math.PI;

	private static final double OPTICAL_DEPTH_STARTING = 0.2342;

	// non static instances
	private MineralMap mineralMap;
	private AreothermalMap areothermalMap;

	/** The locations that have been explored and/or mined. */
	private List<ExploredLocation> exploredLocations;
	/** The most recent value of optical depth by Coordinate. */
	private transient Map<Coordinates, Double> opticalDepthMap = new HashMap<>();
	/** The most recent value of solar irradiance by Coordinate. */
	private transient Map<Coordinates, Double> currentIrradiance = new HashMap<>();

	private MasterClock clock;
	private Weather weather;
	private OrbitInfo orbitInfo;
	
	private static TerrainElevation terrainElevation;

	/**
	 * Constructor.
	 *
	 * @throws Exception when error in creating surface features.
	 */
	public SurfaceFeatures(MasterClock mc, OrbitInfo oi, Weather w) {
		clock = mc;
		orbitInfo = oi;
		weather = w;
		w.setSurfaceFeatures(this);
		
		terrainElevation = new TerrainElevation();
		
		mineralMap = new RandomMineralMap();
		exploredLocations = new ArrayList<>(); 
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
		if (!opticalDepthMap.isEmpty()) {
			Double value = opticalDepthMap.get(location);
			if (value != null)
				return value.doubleValue();
		}

		return computeOpticalDepth(location);
	}

	/**
	 * Computes the optical depth due to the martian dust.
	 *
	 * @param location
	 * @return tau
	 */
	private synchronized double computeOpticalDepth(Coordinates location) {

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
		
		// Save tau onto opticalDepthMap
		opticalDepthMap.put(location, tau);
		
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
		if (!currentIrradiance.isEmpty()) {
			Double value = currentIrradiance.get(location);
			if (value != null)
				return value.doubleValue();
			return 0;
		}

		return calculateSolarIrradiance(location);
	}

	/**
	 * Calculates the solar irradiance.
	 *
	 * @param location
	 * @return
	 */
	private synchronized double calculateSolarIrradiance(Coordinates location) {
		
		// Approach 1 (Not in use)
//		double s1 = 0;
//      double L_s = mars.getOrbitInfo().getL_s();
//      double e = OrbitInfo.ECCENTRICITY;
//    	double z = mars.getOrbitInfo().getSolarZenithAngle(phi);
//    	double num =  1 + e * Math.cos( (L_s - 248) /180D * Math.PI);
//    	double den = 1 - e * e;
//    	s1 = MEAN_SOLAR_IRRADIANCE * Math.cos(z) * num / den * num / den  ;

		
		// Approach 2 consists of 5 parts
		
		// g0: direct solar irradiance at the top of the atmosphere
		double g0 = 0;
		// gh: global irradiance on a horizontal surface
		double gh = 0;
		// gbh: direct beam irradiance on a horizontal surface
		double gbh = 0;
		// gdh: diffuse irradiance on a horizontal surface
		double gdh = 0;
		// The effect of being in twilight zones
//		double twilight = 1;
		
		// PART 1 : COSINE SOLAR ZENITH ANGLE

		// Obtains the cosine solar zenith angle
		double cosZ = orbitInfo.getCosineSolarZenithAngle(location);
		// Find zenith angle (from 0 to 2 pi)
		double z = Math.acos(cosZ);
		// Gets the areocentric longitude
		double areoLon = orbitInfo.getSunAreoLongitude();
		// Gets the sunlight mod due to areoLon
		double mod = 1.35 * (.5 * Math.sin((areoLon - 251.2774) * OrbitInfo.DEGREE_TO_RADIAN) + .5);
//		logger.info("areoLon: " + areoLon + "   mod: " + Math.round(mod *100_000)/100_000);
		
//		logger.info("cosZ: " + Math.round(cosZ *1000.0)/1000.0 + "   z: " + Math.round(z *1000.0)/1000.0);
				
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
//			double z1 = Math.round(((0.2094 + z) * 100) * 10.0)/10.0;
			// This is an arbitrary model, set G_0 to 41.8879 W/ m-2 when Mars is at the horizon
			
//			g0 = 41.8879;
//			
//			gh = g0 * mod * z0 / 20;
			
//			twilight = z0 / 20;
			
			if (cosZ < 0)
				cosZ = COSZ_THRESHOLD * z0/SUNLIGHT_THRESHOLD;
			
//			logger.info("Case A - z: " + Math.round(z *1000.0)/1000.0
//					+ "   cosZ: " + Math.round(cosZ *1000.0)/1000.0			
//					+ "   z0: " + Math.round(z0 *1000.0)/1000.0
////					+ "   z1: " + Math.round(z1 *1000.0)/1000.0
////					+ "   twilight: " + Math.round(twilight *1000.0)/1000.0  
////					+ "   gh: " + Math.round(gh *1000.0)/1000.0
//					);
			
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
//			double z1 = Math.round(((0.2094 + z) * 100) * 10.0)/10.0;
			// This is an arbitrary model, set G_0 to 41.8879 W/ m-2 when Mars is at the horizon

//			g0 = 41.8879;
//			
//			gh = g0 * mod * z0 / 20;
			
//			twilight = z0 / 20;
			
			if (cosZ < 0)
				cosZ = COSZ_THRESHOLD * z0/SUNLIGHT_THRESHOLD;
			
//			logger.info("Case B - z: " + Math.round(z *1000.0)/1000.0
//					+ "   cosZ: " + Math.round(cosZ *1000.0)/1000.0  					
//					+ "   z0: " + Math.round(z0 *1000.0)/1000.0
////					+ "   z1: " + Math.round(z1 *1000.0)/1000.0
////					+ "   twilight: " + Math.round(twilight *1000.0)/1000.0
////					+ "   gh: " + Math.round(gh *1000.0)/1000.0 
//					);
		}
		
		if ((z < HALF_PI + 0.1) || (z > THREE_HALF_PI - 0.1)) {
			// Case C : sun-in-the sky zone
			
			// Also cosZ becomes +ve
			
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
			// Equation: 135% * (.5 * sin (L_s - 251.2774 + 180 - 90) + .5)
			
			// Part 3: get the instantaneous radius and semi major axis
//			double r = orbitInfo.getDistanceToSun();

//			double squaredRatio = SQUARED_SEMI_MAJOR_AXIS / r / r;
	
			if (cosZ < 0)
				cosZ = COSZ_THRESHOLD;
			
			g0 = cosZ * MIN_SOLAR_IRRADIANCE * mod;// * SQUARED_SEMI_MAJOR_AXIS / r / r;		
			
//			logger.info("Case C - z: " + Math.round(z *1000.0)/1000.0 
////					+ "   z0: " + Math.round(z0 *1000.0)/1000.0
//					+ "   cosZ: " + Math.round(cosZ *1000.0)/1000.0
//					+ "   mod: " + Math.round(mod *1000.0)/1000.0
////					+ "   squaredRatio: " + Math.round(squaredRatio *1_000.0)/1_000.0
//					+ "   g0: " + Math.round(g0 *1000.0)/1000.0 
//					);
			
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
	    	// Set cos_z = .9, G_dh = G_bh / 6 = 0.167 * G_bh;
	    	// Set cos_z = 0, G_dh = G_bh;	

//			logger.info("Case C - "
//					+ "tau: " + Math.round(tau *1000.0)/1000.0 
//					+ "   gbh: " + Math.round(gbh *1000.0)/1000.0
//					+ "   gdh: " + Math.round(gdh *1000.0)/1000.0
//					+ "   gh: " + Math.round(gh *1000.0)/1000.0 
//					);

			// For future,
			// Part 6 : calculate other components on Mars such as twilight and
			// reflective irradiance

			// Note: A lot of code use of this method depends on dark night time = 0 solar
			// irradiance. If we want to
			// have scattering produce > 0 irradiance at full night time, we need to modify
			// code calling this method
			// as necessary for night time indication. - Scott

		}
		
//		else {
//			 // Case D: dark sky
//			logger.info("Case D - z: " + Math.round(z *1000.0)/1000.0 
//					+ "   cosZ: " + Math.round(cosZ *1000.0)/1000.0 
//					+ "   mod: " + Math.round(mod *1000.0)/1000.0				
//					+ "   g0: " + Math.round(g0 *1000.0)/1000.0 
//					);
//		}

		if (gh > MAX_SOLAR_IRRADIANCE)
			gh = MAX_SOLAR_IRRADIANCE;
		
		else if (gh < 0)
			gh = 0;

		// Take the average of the last and current irradiance
		Double lastGh = currentIrradiance.get(location);
		if (lastGh != null)
			gh = (gh + lastGh.doubleValue()) / 2;
			
		// Save the value in the cache
		currentIrradiance.put(location, gh);
		
//		if (gh > 0) {
//			logger.info("z: " + Math.round(z *1000.0)/1000.0
//					+ "   cosZ: " + Math.round(cosZ *1000.0)/1000.0  
//					+ "   gh: " + Math.round(gh *1000.0)/1000.0);
//		}
		
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
	public MarsTime getSunRise(Coordinates location) {
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

		return clock.getMarsTime().addTime(timeToSunRise);
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
			double actual = mineralMap.getMineralConcentration(mineralType, location);
			double estimated = 0;
			double varianceMax = 0;
			
			// via the use of ExploredCommand			
			if (estimationImprovement == Mining.MATURE_ESTIMATE_NUM) {
				varianceMax = 1 + actual * MINERAL_ESTIMATION_VARIANCE / 2;
			}
			else {
				varianceMax = 1 + actual * MINERAL_ESTIMATION_VARIANCE * 2;
			}

			estimated = actual + RandomUtil.getRandomDouble(-varianceMax, varianceMax);
			
			// With no improvements the estimates are capped
			if (estimated < 0)
				estimated = 0;
			else if (estimated > MINERAL_ESTIMATION_MAX) {
				estimated = MINERAL_ESTIMATION_MAX;
			}
			
			initialMineralEstimations.put(mineralType, estimated);
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

		if (pulse.isNewMSol()) {
			// TODO Resolve this; put Simualtino in ClockPulse
			Collection<Settlement> col = Simulation.instance().getUnitManager().getSettlements();
			Set<Coordinates> sSet = new HashSet<>();
			for (Settlement s: col) {
				sSet.add(s.getCoordinates());
			}
			
			double msol = pulse.getMarsTime().getMillisolInt();
			
			// the value of optical depth doesn't need to be refreshed too often
			if (msol % OPTICAL_DEPTH_REFRESH == 0) {
				Iterator<Coordinates> it = opticalDepthMap.keySet().iterator();
				while (it.hasNext()) {
					Coordinates coord = it.next();
					if (sSet.contains(coord)) {
						computeOpticalDepth(coord);
					}
					else {
						// Clear only those values that are non-settlement
						it.remove();
					}
				}
			}
			
			Iterator<Coordinates> it = currentIrradiance.keySet().iterator();
			while (it.hasNext()) {
				Coordinates coord = it.next();
				if (sSet.contains(coord)) {
					calculateSolarIrradiance(coord);
				}
				else {
					// Clear only those values that are non-settlement
					it.remove();
				}
			}
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
		exploredLocations.clear();
		exploredLocations = null;
		areothermalMap.destroy();
		areothermalMap = null;

		weather = null;
		orbitInfo = null;
	}
}
