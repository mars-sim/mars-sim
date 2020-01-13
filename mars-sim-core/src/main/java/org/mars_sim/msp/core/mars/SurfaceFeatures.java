/**
 * Mars Simulation Project
 * SurfaceFeatures.java
 * @version 3.1.0 2017-10-18
 * @author Scott Davis
 */
package org.mars_sim.msp.core.mars;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.person.ai.mission.Mining;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.core.tool.RandomUtil;


/**
 * SurfaceFeatures represents the surface terrain and landmarks of the virtual
 * Mars.
 */
public class SurfaceFeatures implements Serializable {

	private static final long serialVersionUID = 1L;

	public static double MEAN_SOLAR_IRRADIANCE = 586D; // in flux or [W/m2] = 1371 / (1.52*1.52)

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

	private static final double HALF_PI = Math.PI / 2d;

	private static double factor = MEAN_SOLAR_IRRADIANCE * OrbitInfo.SEMI_MAJOR_AXIS * OrbitInfo.SEMI_MAJOR_AXIS;

	private static double opticalDepthStartingValue = 0.2342;

	// Data members
	// non static instances
	private MineralMap mineralMap;
	private AreothermalMap areothermalMap;
	
	// The locations that have been explored and/or mined
	private List<ExploredLocation> exploredLocations;

	private Map<Coordinates, Double> opticalDepthMap;
	private Map<Coordinates, List<Double>> solarIrradianceCaches;
	private Map<Coordinates, Double> solarIrradiance;

	// The sites map for ice and regolith collection mission
//	private static Map<Coordinates, CollectionSite> sites;
	

	// static instances
	private static Simulation sim = Simulation.instance();
	private static SimulationConfig simulationConfig = SimulationConfig.instance();
	private static MissionManager missionManager;
	
	private static MarsClock solarIrradianceMapCacheTime;
	private static MasterClock masterClock;
	
	private static TerrainElevation terrainElevation;
	private static Mars mars;
	private static Weather weather;
	private static OrbitInfo orbitInfo;
	
	private static Coordinates sunDirection;
	
//	@JsonIgnore // Need to have both @JsonIgnore and transient for Jackson to ignore converting this list
	private static List<Landmark> landmarks = simulationConfig.getLandmarkConfiguration().getLandmarkList();
	
	// private DecimalFormat fmt3 = new DecimalFormat("#0.000");

	/**
	 * Constructor
	 * 
	 * @throws Exception when error in creating surface features.
	 */
	public SurfaceFeatures() {
		// Initialize instances.
		terrainElevation = new TerrainElevation();
		mineralMap = new RandomMineralMap();
		exploredLocations = new CopyOnWriteArrayList<>(); // will need to make sure explored locations are serialized
//		sites = new ConcurrentHashMap<>();
		areothermalMap = new AreothermalMap();

		missionManager = sim.getMissionManager();

//		try {
//			landmarks = simulationConfig.getLandmarkConfiguration().getLandmarkList();
//		} catch (Exception e) {
//			throw new IllegalStateException("Landmarks could not be loaded: " + e.getMessage(), e);
//		}

		if (solarIrradianceCaches == null)
			solarIrradianceCaches = new ConcurrentHashMap<>();

		solarIrradiance = new ConcurrentHashMap<>();
		
		if (opticalDepthMap == null)
			opticalDepthMap = new ConcurrentHashMap<>();
		
//		double a = OrbitInfo.SEMI_MAJOR_AXIS;
//		factor = MEAN_SOLAR_IRRADIANCE * a * a;
	}

	/**
	 * Initialize transient data in the simulation.
	 * 
	 * @throws Exception if transient data could not be constructed.
	 */
	public void initializeTransientData() {

		// Initialize surface terrain.
		terrainElevation = new TerrainElevation();

		mars = sim.getMars();
		orbitInfo = mars.getOrbitInfo();
		weather = mars.getWeather();
		
		sunDirection = orbitInfo.getSunDirection();
		
//		masterClock = sim.getMasterClock();
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

		// double twilightzone = .2D;
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

	public double getOpticalDepth(Coordinates location) {
		if (opticalDepthMap.containsKey(location))
			return opticalDepthMap.get(location);
		else {
			return computeOpticalDepth(location); // opticalDepthStartingValue
		}
	}

	/***
	 * Computes the optical depth of the martian dust
	 * 
	 * @param location
	 * @return tau
	 */
	public double computeOpticalDepth(Coordinates location) {

		double tau = 0;

		double newTau = 0.2237 * weather.getDailyVariationAirPressure(location);
		
		// Equation: tau = 0.2342 + 0.2247 * yestersolAirPressureVariation;
		// the starting value for opticalDepth is 0.2342. See Ref below
		if (opticalDepthMap.containsKey(location))
			tau = (opticalDepthMap.get(location) + opticalDepthStartingValue + newTau) / 1.9D;
		else {
			tau = opticalDepthStartingValue + newTau;
		}

		// Reference :
		// see Chapter 2.3.1 and equation (2.44,45) on page 63 from the book "Mars:
		// Prospective Energy and Material Resources" by Badescu, Springer 2009.
		// Optical depth is well correlated to the daily variation of surface pressure
		// and to the standard deviation of daily surface pressure
		// The lower the value of tau, the clearer the sky
		// Note: tau has an "inverse" relationship with the daily global solar
		// irradiance in Fig 2.8.

		// Add randomness to optical depth
		tau = tau + RandomUtil.getRandomDouble(.03) - RandomUtil.getRandomDouble(.03);
		// Notes:
		// (1) during relatively periods of clear sky, typical values for optical depth
		// were between 0.2 and 0.5
		// (2) typical observable range is between .32 and .52 (average is 42%).
		// (3) From Viking data, at no time did the optical depth fall below 0.18,

		// tau is usually between .1 and 6, Page 860, Table IV of R. M. Haberlet Et al.
		if (tau > 6)
			tau = 6;
		if (tau < .1)
			tau = .1;

		return tau;
	}

	
	/**
	 * Gets the trend (positive if increasing, negative if decreasing)
	 * 
	 * @param location
	 * @return a number
	 */
	public int getTrend(Coordinates location) {
		int trend = 0;
		
		if (solarIrradianceCaches.containsKey(location)) {
			List<Double> sequence = solarIrradianceCaches.get(location);
//			double avg = sequence.get(0); 
			int size = sequence.size();
//			for (int i=0; i < size - 1; ++i) {
//				if (i > 0) { 
//					avg = (avg + sequence.get(0)) / 2; 
//				}
//				
//				if ((sequence.get(i+1) - avg) > 0)
//					++trend;
//				else
//					--trend;   
//			}
			
			double diff = 0;
			if (!sequence.isEmpty() && size > 1) {
				for (int i=0; i < size - 1; ++i) {
				    diff = sequence.get(i+1) - sequence.get(i);
				    if (diff > 0) {
				       trend++;
				    }
				    else if (diff < 0) {
				       trend--;
				    }
				}
			}
			
//			trend = (int)(trend/2D);
			
		}
		
		return trend;
	}

	/**
	 * Calculate the solar irradiance ratio (between 0 and 1) at a particular location on Mars
	 * 
	 * @param location the coordinate location on Mars.
	 * @return (between 0 and 1)
	 */
	public double getSunlightRatio(Coordinates location) {
		  return Math.round(getSolarIrradiance(location) 
				  / MEAN_SOLAR_IRRADIANCE * 100D)/100D;
	}
	
	/**
	 * Calculate the solar irradiance at a particular location on Mars
	 * 
	 * @param location the coordinate location on Mars.
	 * @return solar irradiance (W/m2)
	 */
	public double getSolarIrradiance(Coordinates location) {
		if (masterClock == null)
			masterClock = sim.getMasterClock();
		MarsClock currentTime = masterClock.getMarsClock();
		if (!currentTime.equals(solarIrradianceMapCacheTime)) {
			// Call here once per frame because getSolarIrradiance()
			// is called many times in 
//			double G_h = 0;
//			if (solarIrradiance.containsKey(location))
//					G_h = Math.round(solarIrradiance.get(location)*10.0)/10.0;
//			System.out.println("At " + currentTime.getMillisolInt() 
//				+ "   Light : " + G_h
//				+ "   Trend : " + getTrend(location));
			solarIrradiance.clear();
			solarIrradianceMapCacheTime = (MarsClock) currentTime.clone();
		}
//		else {
//			// It's mostly this case
//		}
		
		List<Double> list = null;
//		int size = 0;
		double G_h = 0;
		
		if (!solarIrradiance.containsKey(location)) {
			
			// If location is not in cache, calculate solar irradiance.
			if (solarIrradianceCaches.containsKey(location)) {
				list = solarIrradianceCaches.get(location);
				
				G_h = calculateSolarIrradiance(location);
				list.add(G_h);
				
				if (list.size() > 20)
					list.remove(0);
				
//				size = solarIrradianceMapCache.get(location).size();
				solarIrradianceCaches.put(location, list);
			}
			
			else {
				list = new ArrayList<>();
				
				G_h = calculateSolarIrradiance(location);
				list.add(G_h);
	
//				size = 1;
				solarIrradianceCaches.put(location, list);
			}
			
			solarIrradiance.put(location, G_h);
		}
		
		else {
			G_h = solarIrradiance.get(location);
		}	
		
		return G_h;
	}

	/**
	 * Calculates the solar irradiance
	 * 
	 * @param location
	 * @return
	 */
	public double calculateSolarIrradiance(Coordinates location) {
		// Approach 1
//		double s1 = 0;
//        double L_s = mars.getOrbitInfo().getL_s();
//        double e = OrbitInfo.ECCENTRICITY;
//    	double z = mars.getOrbitInfo().getSolarZenithAngle(phi);
//    	double num =  1 + e * Math.cos( (L_s - 248) /180D * Math.PI);
//    	double den = 1 - e * e;
//    	s1 = MEAN_SOLAR_IRRADIANCE * Math.cos(z) * num / den * num / den  ;
//    	System.out.println("solar irradiance s1 is " + s1);

		// Approach 2 consists of 5 parts
		// PART 1 : COSINE SOLAR ZENITH ANGLE
		double G_0 = 0;
		double G_h = 0;
		double G_bh = 0;
		double G_dh = 0;
		// G_0: direct solar irradiance at the top of the atmosphere
		// G_h: global irradiance on a horizontal surface
		// G_bh: direct beam irradiance on a horizontal surface
		// G_dh: diffuse irradiance on a horizontal surface
		
		if (mars == null)
			mars = sim.getMars();
		if (orbitInfo == null)
			orbitInfo = mars.getOrbitInfo();

		double cos_z = orbitInfo.getCosineSolarZenithAngle(location);
		double z = Math.acos(cos_z);

		if (z >= Math.PI / 2D) {
			// if Mars is in the so-called twilight zone,
			// Set it to a maximum of 12 degree below the horizon
			// indirect sunlight such as diffuse/scattering/reflective sunlight will light
			// up the Martian sky
			if (z <= Math.PI / 2D + .1) {
				// twilight zone is defined as bwtween 0.1 to -0.1 in radians above and below
				// the horizon
				G_h = Math.round((-200 * z + 100 * Math.PI + 20) * 100.00) / 100.00; // keep a minimum of G_h at 20
																						// W/m2 if the sun is within
																						// the twilight zone
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

			G_0 = cos_z * factor / r / r;

			// if (G_0 <= 0)
			// G_0 = 0;

			// PART 4 : OPTICAL DEPTH - CALCULATING ABSORPTION AND SCATTERING OF SOLAR
			// RADIATION

			double tau = computeOpticalDepth(location);

			// TODO: Part 4a : reducing opacity of the Martian atmosphere due to local dust
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

			// Note 3: TODO: Model how dust clouds, water/ice clouds, CO2 clouds affects tau
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

			G_bh = G_0 * cos_z / (1 + tau / 2 / cos_z);

			// assuming the reflection from the surface is negligible
			// ref:
			// http://www.uapress.arizona.edu/onlinebks/ResourcesNearEarthSpace/resources30.pdf

			// Note: m(z), the air mass, is estimated as ~ 1/cos_z

			// save tau onto opticalDepthMap
			opticalDepthMap.put(location, tau);

			// Note: one can estimate m(z), the air mass, as ~ 1/cos_z

			// PART 5 : DIFFUSE SOLAR IRRADIANCE

			// G_h = G_direct + G_diffuse
			// On earth, the direct solar irradiance plays the major role of bringing in
			// sunlight
			// On Mars, the role of diffuse solar irradiance is more prominent than that on
			// Earth.

			// TODO: Modeling the diffuse effect of solar irradiance with formula
			// Note: the value of G_dh to decrease more slowly when value cos_z is
			// diminishing

//    	    	if (cos_z > .9)
//    	    		G_dh = G_bh / 6;
//    	    	else if (cos_z > .8)
//    	    		G_dh = G_bh / 4.8;
//    	    	else if (cos_z > .7)
//    	    		G_dh = G_bh / 3.7D;
//    	    	else if (cos_z > .6)
//    	    		G_dh = G_bh / 2.5;
//    	    	else if (cos_z > .5)
//    	    		G_dh = G_bh / 2.2D;
//    	    	else if (cos_z > .4)
//    	    		G_dh = G_bh / 1.8D;
//    	    	else if (cos_z > .3)
//    	    		G_dh = G_bh / 1.6D;
//    	    	else if (cos_z > .2)
//    	    		G_dh = G_bh / 1.4D;
//    	    	else if (cos_z > .1)
//    	    		G_dh = G_bh / 1.2D;
//    	    	else if (cos_z > .05)
//    	    		G_dh = G_bh;

			G_dh = G_bh * .3;

			// Finally,
			G_h = G_bh + G_dh;

			if (G_h > SurfaceFeatures.MEAN_SOLAR_IRRADIANCE)
				G_h = SurfaceFeatures.MEAN_SOLAR_IRRADIANCE;

			else if (G_h < 20.94)
				G_h = 20.94;

			// System.out.println(" radiusAndAxis : " + fmt3.format(radiusAndAxis)
			// + " cos_z : "+ fmt3.format(cos_z)
			// + " G_0 : " + fmt3.format(G_0)
			// + " G_bh : " + fmt3.format(G_bh)
			// + " G_dh : " + fmt3.format(G_dh)
			// + " G_h : " + fmt3.format(G_h));

			// TODO: Part 6 : calculate other components on Mars such as twilight and
			// reflective irradiance

			// Note: A lot of code use of this method depends on dark night time = 0 solar
			// irradiance. If we want to
			// have scattering produce > 0 irradiance at full night time, we need to modify
			// code calling this method
			// as necessary for night time indication. - Scott
		}

		if (G_h < 0)
			G_h = 0;
		
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

//		if (mars == null)
//			mars = sim.getMars();
		
		if (sunDirection == null)
			sunDirection = orbitInfo.getSunDirection();

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
	 * @param estimatedMineralConcentrations a map of all mineral types and their
	 *                                       estimated concentrations (0% -100%)
	 * @param settlement                     the settlement the exploring mission is
	 *                                       from.
	 * @return the explored location
	 */
	public ExploredLocation addExploredLocation(Coordinates location,
			Map<String, Double> estimatedMineralConcentrations, Settlement settlement) {
		
		ExploredLocation result = new ExploredLocation(location, estimatedMineralConcentrations, settlement);
		exploredLocations.add(result);
		return result;
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
	public void timePassing(double time) {

		// TODO: clear the total solar irradiance map and save data in DailyWeather.
		// check for the passing of each day
//	    int newSol = MarsClock.getSolOfYear(marsClock);
//		if (newSol != solCache) {
//			totalSolarIrradianceMap.clear();
//			solCache = newSol;
//		}

		// Update any reserved explored locations.
		Iterator<ExploredLocation> i = exploredLocations.iterator();
		while (i.hasNext()) {
			ExploredLocation site = i.next();
			if (site.isReserved()) {
				// Check if site is reserved by a current mining mission.
				// If not, mark as unreserved.
				boolean goodMission = false;
				if (missionManager == null)
					missionManager = sim.getMissionManager();
				Iterator<Mission> j = missionManager.getMissions().iterator();
				while (j.hasNext()) {
					Mission mission = j.next();
					if (mission instanceof Mining) {
						if (site.equals(((Mining) mission).getMiningSite())) {
							goodMission = true;
						}
					}
				}
				if (!goodMission) {
					site.setReserved(false);
				}
			}
		}
	}

//	/**
//	 * Gets the sites map
//	 * 
//	 * @return
//	 */
//	public Map<Coordinates, CollectionSite> getSites() {
//		return sites;
//	}
//	
//	/**
//	 * Set the sites map
//	 * 
//	 * @param location
//	 * @param site
//	 */
//	public void setSites(Coordinates location, CollectionSite site) {
//		if (!sites.containsKey(location)) {
//			sites.put(location, site);
//		}
//	}
	
	
	/**
	 * Reloads instances
	 * 
	 * @param c {@link MasterClock}
	 * @param m {@link Mars}
	 * @param s {@link Simulation}
	 * @param w {@link Weather}
	 * @param o {@link OrbitInfo}
	 * @param mr {@link MissionManager}
	 */
	public static void initializeInstances(MasterClock c, Mars m, Simulation s, Weather w, OrbitInfo o, MissionManager mr) {
		masterClock = c;
		mars = m;
		orbitInfo = o;
		weather = w;
		sim = s;
		missionManager = mr;
		sunDirection = o.getSunDirection();
		landmarks = simulationConfig.getLandmarkConfiguration().getLandmarkList();
	}
	
	
	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
//		sites.clear();
//		sites = null;
		opticalDepthMap.clear();
		opticalDepthMap = null;
		solarIrradianceCaches.clear();
		solarIrradianceCaches = null;
		solarIrradiance.clear();
		solarIrradiance = null;
			
		mineralMap.destroy();
		mineralMap = null;
		exploredLocations.clear();
		exploredLocations = null;
		areothermalMap.destroy();
		areothermalMap = null;
		
		sim = null;
		simulationConfig = null;
		missionManager = null;
		solarIrradianceMapCacheTime = null;
		masterClock = null;
		weather = null;
		orbitInfo = null;
		sunDirection = null;
		terrainElevation.destroy();
		terrainElevation = null;
		orbitInfo = null;
		mars = null;
		missionManager = null;
		sunDirection = null;
		
		landmarks.clear();
		landmarks = null;
	}
}
