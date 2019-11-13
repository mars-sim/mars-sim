/**
 * Mars Simulation Project
 * Weather.java
 * @version 3.1.0 2017-08-31
 * @author Scott Davis
 * @author Hartmut Prochaska
 */
package org.mars_sim.msp.core.mars;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.structure.CompositionOfAir;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.core.tool.RandomUtil;

/** Weather represents the weather on Mars */
public class Weather implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/* default logger. */
	private static Logger logger = Logger.getLogger(Weather.class.getName());

	private static String sourceName = logger.getName().substring(logger.getName().lastIndexOf(".") + 1,
			logger.getName().length());

	// Static data
	/** Sea level air pressure in kPa. */
	// Set the unit of air pressure to kPa
	// private static final double SEA_LEVEL_AIR_PRESSURE = .8D;
	/** Sea level air density in kg/m^3. */
	// private static final double SEA_LEVEL_AIR_DENSITY = .0115D;
	/** Mars' gravitational acceleration at sea level in m/sec^2. */
	// private static final double SEA_LEVEL_GRAVITY = 3.0D;
	/** Extreme cold surface temperatures on Mars at Kelvin */
	private static final double EXTREME_COLD = 120D; // [ in deg Kelvin] or -153.17 C

	/** Viking 1's longitude (49.97 W) in millisols */
	// private static final double VIKING_LONGITUDE_OFFSET_IN_MILLISOLS = 138.80D;
	// // = 49.97W/180 deg * 500 millisols;
	private static final double VIKING_LATITUDE = 22.48D; // At 22.48E

	public static final double PARTIAL_PRESSURE_CARBON_DIOXIDE_MARS = 0.57D; // in kPa
	public static final double PARTIAL_PRESSURE_CARBON_DIOXIDE_EARTH = 0.035D; // in kPa
	public static final double PARTIAL_PRESSURE_WATER_VAPOR_ROOM_CONDITION = 1.6D; // in kPa. under Earth's atmosphere,
																					// at 25 C, 50% relative humidity

	private static final int MILLISOLS_PER_UPDATE = 5; // one update per x millisols

	private static final int RECORDING_FREQUENCY = 50; // in millisols

	private int quotientCache;

	private int msols;

	private int checkStorm = 0;

	/** The cache value of sol since the start of sim. */
	private int solCache = 0;

	private int L_s_cache = 0;

	private double dx = 255D * Math.PI / 180D - Math.PI;

	private double viking_dt;

	private double TEMPERATURE_DELTA_PER_DEG_LAT = 0D;

	private double dailyVariationAirPressure = RandomUtil.getRandomDouble(.01); // tentatively only
	// TODO: compute the true dailyVariationAirPressure by the end of the day

	private int newStormID = 1;

	private Map<Coordinates, Map<Integer, List<DailyWeather>>> weatherDataMap = new ConcurrentHashMap<>();
	private Map<Integer, List<DailyWeather>> dailyRecordMap = new ConcurrentHashMap<>();

	private List<DailyWeather> todayWeather = new CopyOnWriteArrayList<>();
	private List<Coordinates> coordinateList = new CopyOnWriteArrayList<>();

	private transient Map<Coordinates, Double> temperatureCacheMap;
	private transient Map<Coordinates, Double> airPressureCacheMap;
	private transient Map<Coordinates, Double> windSpeedCacheMap;
	private transient Map<Coordinates, Integer> windDirCacheMap;

	private List<DustStorm> planetEncirclingDustStorms = new CopyOnWriteArrayList<>();
	private List<DustStorm> regionalDustStorms = new CopyOnWriteArrayList<>();
	private List<DustStorm> localDustStorms = new CopyOnWriteArrayList<>();
	private List<DustStorm> dustDevils = new CopyOnWriteArrayList<>();

	private static Simulation sim = Simulation.instance();
	
	private static SurfaceFeatures surfaceFeatures;
	private static TerrainElevation terrainElevation;

	private static OrbitInfo orbitInfo;
	private static Mars mars;
	
	private static MasterClock masterClock;
	private static MarsClock marsClock;
	private static UnitManager unitManager;


	/** Constructs a Weather object */
	public Weather() {

		viking_dt = 28D - 15D * Math.sin(2 * Math.PI / 180D * VIKING_LATITUDE + Math.PI / 2D) - 13D;
		viking_dt = Math.round(viking_dt * 100.0) / 100.00;

		// Opportunity Rover landed at coordinates 1.95 degrees south, 354.47 degrees
		// east.
		// From the chart, it has an average of 8 C temperature variation on the maximum
		// and minimum temperature curves

		// Spirit Rover landed at 14.57 degrees south latitude and 175.47 degrees east
		// longitude.
		// From the chart, it has an average of 25 C temperature variation on the
		// maximum and minimum temperature curves

		double del_latitude = 12.62; // =14.57-1.95;
		int del_temperature = 17; // = 25-8;

		// assuming a linear relationship
		TEMPERATURE_DELTA_PER_DEG_LAT = del_temperature / del_latitude;

		if (masterClock == null) {
			if (sim.getMasterClock() != null) {
				masterClock = sim.getMasterClock();
			}
		}

	}

	/**
	 * Initialize transient data in the simulation.
	 * 
	 * @throws Exception if transient data could not be constructed.
	 */
	public void initializeTransientData() {
		if (mars == null)
			mars = sim.getMars();
		
		if (orbitInfo == null)
			orbitInfo = mars.getOrbitInfo();
		
		if (surfaceFeatures == null)
			surfaceFeatures = mars.getSurfaceFeatures();
		
		if (terrainElevation == null)
			terrainElevation = surfaceFeatures.getTerrainElevation();

//		if (unitManager == null)
//			unitManager = sim.getUnitManager();
		
	}
	
	/**
	 * Checks if a location with certain coordinates already exists and add any new
	 * location
	 * 
	 * @param location
	 */
	public void checkLocation(Coordinates location) {
		if (!coordinateList.contains(location))
			coordinateList.add(location);
	}

	/**
	 * Gets the air density at a given location.
	 * 
	 * @return air density in g/m3.
	 */
	public double computeAirDensity(Coordinates location) {
		checkLocation(location);
		// The air density is derived from the equation of state : d = p / .1921 / (t +
		// 273.1)
		double result = 1000D * getAirPressure(location)
				/ (.1921 * (getTemperature(location) + CompositionOfAir.C_TO_K));
		return Math.round(result * 100.0) / 100.0;
	}

	/**
	 * Gets the air density at a given location.
	 * 
	 * @return air density in kg/m3.
	 */
	public double getAirDensity(Coordinates location) {
		return computeAirDensity(location);
	}

	/**
	 * Gets the wind speed at a given location.
	 * 
	 * @return wind speed in m/s.
	 */
	public double computeWindSpeed(Coordinates location) {
		double new_speed = 0;

		if (windSpeedCacheMap == null)
			windSpeedCacheMap = new ConcurrentHashMap<>();

		// On sol 214 in this list of Viking wind speeds, 25.9 m/sec (93.24 km/hr) was
		// recorded.

		// Viking spacecraft from the surface, "during a global dust storm the diurnal
		// temperature range narrowed
		// sharply,...the wind speeds picked up considerably—indeed, within only an hour
		// of the storm's arrival they
		// had increased to 17 m/s (61 km/h), with gusts up to 26 m/s (94 km/h)
		// https://en.wikipedia.org/wiki/Climate_of_Mars

		if (windSpeedCacheMap.containsKey(location)) {

			double rand = RandomUtil.getRandomDouble(1) - RandomUtil.getRandomDouble(1);

			// check for the passing of each day
			int newSol = marsClock.getMissionSol();
			if (solCache != newSol) {
				// solCache = newSol;
				double ds_speed = 0;

				if (unitManager == null)
					unitManager = sim.getUnitManager();
				
				List<Settlement> settlements = new ArrayList<>(unitManager.getSettlements());
				for (Settlement s : settlements) {
					if (s.getCoordinates().equals(location)) {
						DustStorm ds = s.getDustStorm();
						if (ds != null) {
							DustStormType type = ds.getType();
							ds_speed = ds.getSpeed();

							if (type == DustStormType.DUST_DEVIL) {
								// arbitrary speed determination
								new_speed = .8 * new_speed + .2 * ds_speed;

							}

							else if (type == DustStormType.LOCAL) {
								// arbitrary speed determination
								new_speed = .985 * new_speed + .015 * ds_speed;

							} else if (type == DustStormType.REGIONAL) {

								// arbitrary speed determination
								new_speed = .99 * new_speed + .01 * ds_speed;

							}

							else if (type == DustStormType.PLANET_ENCIRCLING) {

								// arbitrary speed determination
								new_speed = .995 * new_speed + .005 * ds_speed;

							}
						}
					}
				}

				// }

				new_speed = ds_speed + rand;

			} else {

				new_speed = windSpeedCacheMap.get(location) + rand;

			}

			new_speed = windSpeedCacheMap.get(location) + rand;

		} else {

			new_speed = RandomUtil.getRandomDouble(1) - RandomUtil.getRandomDouble(1);

		}

		// Despite secondhand estimates of higher velocities, official observed gust
		// velocities on Mars are
		// in the range of 80-120 mph (120-160 km/hr).
		// At higher altitudes, the movement of dust was measured at 250-300 mph
		// (400-480 km/hr).

		if (new_speed > 50) // assume the max surface wind speed of up to 50 m/s
			new_speed = 50;
		if (new_speed < 0)
			new_speed = 0;

		windSpeedCacheMap.put(location, new_speed);

		return new_speed;
	}

	/**
	 * Gets the wind speed at a given location.
	 * 
	 * @return wind speed in m/s.
	 */
	public double getWindSpeed(Coordinates location) {
		return computeWindSpeed(location);
	}

	/**
	 * Gets the wind direction at a given location.
	 * 
	 * @return wind direction in degree.
	 */
	public int getWindDirection(Coordinates location) {
		return computeWindDirection(location);
	}

	/**
	 * Computes the wind direction at a given location.
	 * 
	 * @return wind direction in degree.
	 */
	public int computeWindDirection(Coordinates location) {
		int result = 0;

		if (getWindSpeed(location) < 0.01)
			return 0;

		// checkLocation(location);

		int newDir = RandomUtil.getRandomInt(359);

		if (windDirCacheMap == null)
			windDirCacheMap = new ConcurrentHashMap<>();

		if (windDirCacheMap.containsKey(location))
			// TODO: should the ratio of the weight of the past direction and present
			// direction of the wind be 9 to 1 ?
			result = (windDirCacheMap.get(location) * 9 + newDir) / 10;
		else {
			result = newDir;
		}

		if (result > 360)
			result = result - 360;

		windDirCacheMap.put(location, result);

		return result;
	}

	/**
	 * Computes the air pressure at a given location.
	 * 
	 * @return air pressure in Pa.
	 */
	public double getAirPressure(Coordinates location) {
		return getCachedAirPressure(location);
	}

	// The air pressure varies from 690 to 780 Pa in daily cycles from Sol 9.5 to 13
	// See chart at
	// https://mars.jpl.nasa.gov/msl/mission/instruments/environsensors/rems/

	// also the air pressure varies 730 to 920 throughout the year (L_s 0 to 360)
	// See chart at
	// http://cab.inta-csic.es/rems/en/weather-report-mars-year-33-month-11/

	/**
	 * Gets the cached air pressure at a given location.
	 * 
	 * @return air pressure in kPa.
	 */
	public double getCachedAirPressure(Coordinates location) {
		checkLocation(location);

		// Lazy instantiation of airPressureCacheMap.
		if (airPressureCacheMap == null) {
			airPressureCacheMap = new ConcurrentHashMap<Coordinates, Double>();
		}

		if (msols % MILLISOLS_PER_UPDATE == 1) {
			double newP = calculateAirPressure(location, 0);
			airPressureCacheMap.put(location, newP);
			return newP;
		} else {
			return getCachedReading(airPressureCacheMap, location);// , AIR_PRESSURE);
		}
	}

	/**
	 * Calculates the air pressure at a given location and/or height
	 * 
	 * @param location
	 * @param height   [in km]
	 * @return air pressure [in kPa]
	 */
	public double calculateAirPressure(Coordinates location, double height) {
		// Get local elevation in meters.
		if (terrainElevation == null)
			terrainElevation = sim.getMars().getSurfaceFeatures().getTerrainElevation();

		double elevation = 0;

		if (height == 0)
			elevation = terrainElevation.getMOLAElevation(location); // in km since getElevation() return the value in km
		else
			elevation = height;

		// p = pressure0 * e(-((density0 * gravitation) / pressure0) * h)
		// Q: What are these enclosed values ==> P = 0.009 * e(-(0.0155 * 3.0 / 0.009) *
		// elevation)
		// double pressure = SEA_LEVEL_AIR_PRESSURE * Math.exp(-1D *
		// SEA_LEVEL_AIR_DENSITY * SEA_LEVEL_GRAVITY / (SEA_LEVEL_AIR_PRESSURE)*
		// elevation * 1000);

		// Use curve-fitting equations at
		// http://www.grc.nasa.gov/WWW/k-12/airplane/atmosmrm.html for modeling Mars
		// p = .699 * exp(-0.00009 * h); p in kPa, h in m

		double pressure = 0.699 * Math.exp(-0.00009 * elevation * 1000);
		// why * 1000 ? The input value of height was in km, but h is in meters

		// Added randomness
		double up = RandomUtil.getRandomDouble(.01);
		double down = RandomUtil.getRandomDouble(.01);

		pressure = pressure + up - down;

		return pressure;
	}

	/**
	 * Gets the temperature at a given location.
	 * 
	 * @return temperature in deg Celsius.
	 */
	public double getTemperature(Coordinates location) {
		return getCachedTemperature(location);
	}

	/**
	 * Gets the cached temperature at a given location.
	 * 
	 * @return temperature in deg Celsius.
	 */
	public double getCachedTemperature(Coordinates location) {
		checkLocation(location);

		// Lazy instantiation of temperatureCacheMap.
		if (temperatureCacheMap == null) {
			temperatureCacheMap = new ConcurrentHashMap<Coordinates, Double>();
		}

		if (msols % MILLISOLS_PER_UPDATE == 0) {
			double newT = calculateTemperature(location);
			temperatureCacheMap.put(location, newT);
			return newT;
		} else {
			return getCachedReading(temperatureCacheMap, location);// , TEMPERATURE);
		}
	}

	/***
	 * Calculates the mid-air temperature
	 * 
	 * @param h is elevation in km
	 * @return temperature at elevation h
	 */
	public double calculateMidAirTemperature(double h) {
		double t = 0;

		// Assume a temperature model with two zones with separate curve fits for the
		// lower atmosphere
		// and the upper atmosphere.

		// In the both lower and upper atmosphere, the temperature decreases linearly
		// and the pressure decreases exponentially.
		// The rate of temperature decrease is called the lapse rate. For the
		// temperature T and the pressure p,
		// the metric units curve fits for the lower atmosphere are:

		if (h <= 7)
			t = -31 - 0.000998 * h;
		else
			t = -23.4 - 0.00222 * h;

		return t;
	}

	/**
	 * Calculates the surface temperature at a given location.
	 * 
	 * @return temperature in Celsius.
	 */
	public double calculateTemperature(Coordinates location) {

		double t = 0;

		if (surfaceFeatures.inDarkPolarRegion(location)) {

			// vs. just in inPolarRegion()
			// see http://www.alpo-astronomy.org/jbeish/Observing_Mars_3.html
			// Note that the polar region may be exposed to more sunlight

			// see https://www.atmos.umd.edu/~ekalnay/pubs/2008JE003120.pdf
			// The swing can be plus and minus 10K deg

			t = EXTREME_COLD + RandomUtil.getRandomDouble(10) - RandomUtil.getRandomDouble(10)
					- CompositionOfAir.C_TO_K;

			// double millisol = marsClock.getMillisol();
			// TODO: how to relate at what millisols are the mean daytime and mean night
			// time at the pole ?

		}

		else if (surfaceFeatures.inPolarRegion(location)) {

			// Based on Surface brightness temperatures at 32 µm retrieved from the MCS data
			// for
			// over five Mars Years (MY), at the “Tleilax” site.

			double L_s = orbitInfo.getL_s();

			// split into 6 zones for linear curve fitting for each martian year
			// See chart at https://www.hou.usra.edu/meetings/marspolar2016/pdf/6012.pdf

			if (L_s < 90)
				t = 0.8333 * L_s + 145;
			else if (L_s <= 180)
				t = -0.8333 * L_s + 295;
			else if (L_s <= 225)
				t = -.3333 * L_s + 205;
			else if (L_s <= 280)
				t = .1818 * L_s + 89.091;
			else if (L_s <= 320)
				t = -.125 * L_s + 175;
			else if (L_s <= 360)
				t = .25 * L_s + 55;

			t = t + RandomUtil.getRandomDouble(3) - RandomUtil.getRandomDouble(3) - CompositionOfAir.C_TO_K;

		} else {
			// We arrived at this temperature model based on Viking 1 & Opportunity Rover
			// by assuming the temperature is the linear combination of the following
			// factors:
			// 1. Time of day, longitude and solar irradiance,
			// 2. Terrain elevation,
			// 3. Latitude,
			// 4. Seasonal variation (dependent upon latitude)
			// 5. Randomness
			// 6. Wind speed

//			// (1). Time of day, longitude (see SurfaceFeature's calculateSolarIrradiance())
//			double theta = location.getTheta() / Math.PI * 500D; // convert theta in longitude in radian to millisols;
			
//	        double time  = marsClock.getMillisol();
//	        double x_offset = time + theta - VIKING_LONGITUDE_OFFSET_IN_MILLISOLS ;
//	        double equatorial_temperature = 27.5D * Math.sin  ( Math.PI * x_offset / 500D) - 58.5D ;
			
			double light_factor = surfaceFeatures.getSunlightRatio(location);

			// Equation below is modeled after Viking's data.
			double equatorial_temperature = 27.5D * light_factor - 58.5D;

			// ...getSurfaceSunlight * (80D / 127D (max sun))
			// if sun full we will get -40D the avg, if night or twilight we will get
			// a smooth temperature change and in the night -120D
//		    temperature = temperature + surfaceFeatures.getSurfaceSunlight(location) * 80D;

			// (2). Terrain Elevation
			// use http://www.grc.nasa.gov/WWW/k-12/airplane/atmosmrm.html for modeling Mars
			// with precalculated values
			// The lower atmosphere runs from the surface of Mars to 7,000 meters.
			// T = -31 - 0.000998 * h
			// The upper stratosphere model is used for altitudes above 7,000 meters.
			// T = -23.4 - 0.00222 * h
			
			if (terrainElevation == null)
				terrainElevation = sim.getMars().getSurfaceFeatures().getTerrainElevation();

			double elevation = terrainElevation.getMOLAElevation(location); // in km from getElevation(location)
			double terrain_dt;

			// Assume a typical temperature of -31 deg celsius
			if (elevation < 7)
				terrain_dt = -0.000998 * elevation * 1000;
			else // delta = -31 + 23.4 = 7.6
				terrain_dt = 7.6 - 0.00222 * elevation * 1000;

//			terrain_dt = Math.round(terrain_dt * 100.0) / 100.0;
			// System.out.print(" terrain_dt: " + terrain_dt );

			// (3). Latitude
			double lat_degree = location.getPhi2Lat();

			// System.out.print(" degree: " + Math.round (degree * 10.0)/10.0 );
			double lat_dt = -15D - 15D * Math.sin(2D * lat_degree * Math.PI / 180D + Math.PI / 2D);
//			lat_dt = Math.round(lat_dt * 100.0) / 100.0;
			// System.out.println(" lat_dt: " + lat_dt );

			// (4). Seasonal variation
			double lat_adjustment = TEMPERATURE_DELTA_PER_DEG_LAT * lat_degree; // an educated guess
			if (masterClock == null)
				masterClock = Simulation.instance().getMasterClock();
			if (marsClock == null)
				marsClock = masterClock.getMarsClock();
			int solElapsed = marsClock.getMissionSol();
			double seasonal_dt = lat_adjustment * Math.sin(2 * Math.PI / 1000D * (solElapsed - 142));
//			seasonal_dt = Math.round(seasonal_dt * 100.0) / 100.0;
			// System.out.println(" seasonal_dt: " + seasonal_dt );

			// (5). Add randomness
			double up = RandomUtil.getRandomDouble(2);
			double down = RandomUtil.getRandomDouble(2);

			// (6). Add Windspped

			double wind_dt = 0;
			if (windSpeedCacheMap == null)
				windSpeedCacheMap = new ConcurrentHashMap<>();

			if (windSpeedCacheMap.containsKey(location))
				wind_dt = windSpeedCacheMap.get(location) * 1.5D;

			t = equatorial_temperature + viking_dt - lat_dt - terrain_dt + seasonal_dt - wind_dt + up - down;

			double previous_t = 0;
			if (temperatureCacheMap == null) {
				temperatureCacheMap = new ConcurrentHashMap<Coordinates, Double>();
			}

			if (temperatureCacheMap.containsKey(location)) {
				previous_t = temperatureCacheMap.get(location);
			}

			t = Math.round((t + previous_t) / 2.0 * 100.0) / 100.0;
			// System.out.println(" final T: " + final_temperature );
		}

		// temperatureCacheMap.put(location, final_temperature);

		return t;
	}

	/**
	 * Clears weather-related parameter cache map to prevent excessive build-up of
	 * key-value sets
	 */
	public synchronized void clearMap() {
		if (temperatureCacheMap != null) {
			temperatureCacheMap.clear();
		}

		if (airPressureCacheMap != null) {
			airPressureCacheMap.clear();
		}
	}

	/**
	 * Provides the surface temperature or air pressure at a given location from the
	 * temperatureCacheMap. If calling the given location for the first time from
	 * the cache map, call update temperature/air pressure instead
	 * 
	 * @return temperature or pressure
	 */
	public double getCachedReading(Map<Coordinates, Double> map, Coordinates location) {
		double result;

		if (map.containsKey(location)) {
			result = map.get(location);
		} else {
			double cache = 0;
			// if (value == TEMPERATURE )
			if (map.equals(temperatureCacheMap))
				cache = calculateTemperature(location);
			// else if (value == AIR_PRESSURE )
			else if (map.equals(airPressureCacheMap))
				cache = calculateAirPressure(location, 0);

			map.put(location, cache);

			result = cache;
		}
		
		return result;
	}

	/**
	 * Time passing in the simulation.
	 * 
	 * @param time time in millisols
	 * @throws Exception if error during time.
	 */
	public void timePassing(double time) {

		if (masterClock == null)
			masterClock = Simulation.instance().getMasterClock();

		if (marsClock == null)
			marsClock = masterClock.getMarsClock();

		// Sample a data point every RECORDING_FREQUENCY (in millisols)
		msols = marsClock.getMillisolInt();

		int quotient = msols / RECORDING_FREQUENCY;

		if (quotientCache != quotient) {

			coordinateList.forEach(location -> {
				DailyWeather weather = new DailyWeather(marsClock, getTemperature(location), getAirPressure(location),
						getAirDensity(location), getWindSpeed(location), surfaceFeatures.getSolarIrradiance(location),
						surfaceFeatures.getOpticalDepth(location));
				todayWeather.add(weather);
			});

			quotientCache = quotient;

		}

		// check for the passing of each day
		int newSol = marsClock.getMissionSol();
		if (newSol != solCache) {

			dailyVariationAirPressure += RandomUtil.getRandomDouble(.01) - RandomUtil.getRandomDouble(.01);
			if (dailyVariationAirPressure > .05)
				dailyVariationAirPressure = .05;
			else if (dailyVariationAirPressure < -.05)
				dailyVariationAirPressure = -.05;

			// more often observed from mid-southern summer, between 241 deg and 270 deg Ls,
			// with a peak period at 255 deg Ls.

			// Note : The Mars dust storm season begins just after perihelion at around Ls =
			// 260°.
			double L_s = Math.round(orbitInfo.getL_s() * 10.0) / 10.0;
			int L_s_int = (int) L_s;

			if (L_s_cache != L_s_int) {
				L_s_cache = L_s_int;
				if (L_s_int == 230)
					// reset the counter once a year
					checkStorm = 0;
			}

			// Arbitrarily assume
			// (1) 5% is the highest chance of forming a storm, if L_s is right at 255 deg
			// (2) 0.05% is the lowest chance of forming a storm, if L_s is right at 75 deg

			// By doing curve-fitting a cosine curve
			// (5% - .05%)/2 = 2.475
			// double dx = 255D * Math.PI/180D - Math.PI;

			double probability = -2.475 * Math.cos(L_s_cache * Math.PI / 180D - dx) + (2.475 + .05);
			// probability is 5% at max
			double size = dustDevils.size();
			// Artificially limit the # of dust storm to 10
			if (L_s_int > 240 && L_s_int < 271 && size <= 10 && checkStorm < 200) {
				// When L_s = 250 (use 255 instead), Mars is at perihelion--when the sun is
				// closed to Mars.

				// All of the observed storms have begun within 50-60 degrees of Ls of
				// perihelion (Ls ~ 250);
				createDustDevils(probability, L_s);
			}

			else if (dustDevils.size() <= 20 && checkStorm < 200) {

				createDustDevils(probability, L_s);
			}

			checkOnPlanetEncirclingStorms();

			checkOnRegionalStorms();

			checkOnLocalStorms();

			checkOnDustDevils();

			coordinateList.forEach(location -> {
				// compute the average pressure
				// todayWeather.forEach( d -> {
				// sum = d.getPressure();
				// });
				// double ave = sum / todayWeather.size();
				// dailyVariationAirPressure = Math.abs(dailyVariationAirPressure - ave);
				// save the todayWeather into dailyRecordMap
				dailyRecordMap.put(solCache, todayWeather);
				// save the dailyRecordMap into weatherDataMap
				weatherDataMap.put(location, dailyRecordMap);
			});
			// create a brand new list
			todayWeather = new CopyOnWriteArrayList<>();
			solCache = newSol;
			// computeDailyVariationAirPressure();
		}

	}

	/***
	 * Checks if a dust devil is formed for each settlement
	 * 
	 * @param probability
	 * @param L_s_int
	 */
	public void createDustDevils(double probability, double L_s) {
		if (unitManager == null)
			unitManager = sim.getUnitManager();
		List<Settlement> settlements = new ArrayList<>(unitManager.getSettlements());
		for (Settlement s : settlements) {
			if (s.getDustStorm() == null) {
				// if settlement doesn't have a dust storm formed near it yet
				List<Settlement> list = new ArrayList<>();

				double chance = RandomUtil.getRandomDouble(100);
				if (chance <= probability) {

					// arbitrarily set to the highest 3% chance (if L_s is 241 or 270) of generating
					// a dust devil
					// on each sol since it is usually created in Martian spring or summer day,
					checkStorm++;

					list.add(s);

					// Assuming all storms start out as a dust devil
					DustStorm ds = new DustStorm("Dust Devil-" + newStormID, DustStormType.DUST_DEVIL, newStormID,
							marsClock, this, list);
					dustDevils.add(ds);
					s.setDustStorm(ds);
					newStormID++;

					LogConsolidated.log(Level.INFO, 1000, sourceName,
							"[" + ds.getSettlements().get(0).getName() + "] On L_s = " + Math.round(L_s * 100.0) / 100.0
									+ ", " + ds.getName() + " was first spotted near " + s + "."); 

				}
			}
		}
	}

	/***
	 * Checks to see if a dust devil has been subsided or upgraded
	 */
	public void checkOnDustDevils() {

		for (DustStorm ds : dustDevils) {
			if (ds.computeNewSize() == 0) {
				// remove this dust devil
				dustDevils.remove(ds);
				// Set the dust storm instance in that settlement to null
				ds.getSettlements().get(0).setDustStorm(null);
			} 
			
			else if (ds.computeNewSize() > 20) {
				int id = ds.getID();
				// if the size of this dust devil grows to 21 km, upgrade it to local storm
				ds.setName("Local Storm-" + id);
				ds.setType(DustStormType.LOCAL);
				// upgrade this to local storm
				localDustStorms.add(ds);
				// remove this oversized dust devil
				dustDevils.remove(ds);
			}

			if (ds.getSize() != 0)
				LogConsolidated.log(Level.INFO, 1000, sourceName,
						"[" + ds.getSettlements().get(0).getName() + "] On Sol " + (solCache + 1) + ", " + ds.getName()
								+ " (size " + ds.getSize() + " with windspeed "
								+ Math.round(ds.getSpeed() * 10.0) / 10.0 + " m/s) was sighted.");
		}
	}

	/***
	 * Checks to see if a local storm should be upgraded or downgraded
	 */
	public void checkOnLocalStorms() {

		for (DustStorm ds : localDustStorms) {
			if (ds.computeNewSize() > 2000) {
				int id = ds.getID();
				// if the size of this local storm grows beyond 2000km, upgrade it to regional
				// storm
				ds.setName("Local Storm-" + id);
				ds.setType(DustStormType.REGIONAL);
				// upgrade this local storm to regional storm
				regionalDustStorms.add(ds);
				// remove this oversized local storm
				localDustStorms.remove(ds);
			} 
			
			else if (ds.computeNewSize() == 0) {
				// remove this local storm
				localDustStorms.remove(ds);
				// Set the dust storm instance in that settlement to null
				ds.getSettlements().get(0).setDustStorm(null);
			} 
			
			else if (ds.computeNewSize() <= 20) {
				int id = ds.getID();
				// if the size of a regional shrink below 2000km, downgrade it to dust devil
				ds.setName("Dust Devil-" + id);
				ds.setType(DustStormType.DUST_DEVIL);
				// downgrade this local storm to dust devil
				dustDevils.add(ds);
				// remove this undersize local storm
				localDustStorms.remove(ds);
			}

			if (ds.getSize() != 0)
				LogConsolidated.log(Level.INFO, 1000, sourceName,
						"[" + ds.getSettlements().get(0).getName() + "] On Sol " + (solCache + 1) + ", " + ds.getName()
								+ " (size " + ds.getSize() + " with windspeed "
								+ Math.round(ds.getSpeed() * 10.0) / 10.0 + " m/s) was sighted.");
		}
	}

	/***
	 * Checks to see if a regional storm should be upgraded or downpgraded
	 */
	public void checkOnRegionalStorms() {

		// regionalDustStormMap.entrySet().removeIf(e-> ... );

		for (DustStorm ds : regionalDustStorms) {
			if (ds.computeNewSize() > 4000) {
				int id = ds.getID();
				// if the size of a regional grows beyond 2000km, upgrade it to planet
				// encircling storm
				ds.setName("Planet Encircling Storm-" + id);
				ds.setType(DustStormType.PLANET_ENCIRCLING);
				// upgrade this regional storm to planet-encircling
				planetEncirclingDustStorms.add(ds);
				// remove this oversize regional storm
				regionalDustStorms.remove(ds);
			} 
			
			else if (ds.computeNewSize() == 0) {
				// remove this local storm
				regionalDustStorms.remove(ds);
				// Set the dust storm instance in that settlement to null
				ds.getSettlements().get(0).setDustStorm(null);
			} 
			
			else if (ds.computeNewSize() <= 2000) {
				int id = ds.getID();
				// if the size of a regional shrink below 2000km, downgrade it to local storm
				ds.setName("Local Storm-" + id);
				ds.setType(DustStormType.LOCAL);
				// downgrade this regional storm to local
				planetEncirclingDustStorms.add(ds);
				// remove this undersize regional storm
				regionalDustStorms.remove(ds);
			}

			if (ds.getSize() != 0)
				LogConsolidated.log(Level.INFO, 1000, sourceName,
						"[" + ds.getSettlements().get(0).getName() + "] On Sol " + (solCache + 1) + ", " + ds.getName()
								+ " (size " + ds.getSize() + " with windspeed "
								+ Math.round(ds.getSpeed() * 10.0) / 10.0 + " m/s) was sighted.");
		}
	}

	/***
	 * Checks to see if a planet-encircling storm should be downgraded. Note that
	 * Mars has the highest likelihood of producing a global dust storm
	 */
	public void checkOnPlanetEncirclingStorms() {

		for (DustStorm ds : planetEncirclingDustStorms) {
			if (ds.computeNewSize() <= 4000) {
				int id = ds.getID();
				// if the size of this storm drops below 2000km, downgrade it to become regional
				// storm
				ds.setName("Regional Storm-" + id);
				ds.setType(DustStormType.REGIONAL);
				// downgrade this regional storm to regional
				regionalDustStorms.add(ds);
				// remove this undersized storm
				planetEncirclingDustStorms.remove(ds);
			} 
			
			else if (ds.computeNewSize() == 0) {
				// remove this local storm
				planetEncirclingDustStorms.remove(ds);
				// Set the dust storm instance in that settlement to null
				ds.getSettlements().get(0).setDustStorm(null);
			}

			if (ds.getSize() != 0)
				LogConsolidated.log(Level.INFO, 1000, sourceName,
						"[" + ds.getSettlements().get(0) + "] On Sol " + (solCache + 1) + ", " + ds.getName()
								+ " (size " + ds.getSize() + " with windspeed "
								+ Math.round(ds.getSpeed() * 10.0) / 10.0 + " m/s) was sighted.");
		}
	}

	public List<DustStorm> getPlanetEncirclingDustStorms() {
		return planetEncirclingDustStorms;
	}

	public double getDailyVariationAirPressure(Coordinates location) {
		return dailyVariationAirPressure;
	}

	/**
	 * Reloads instances after loading from a saved sim
	 * 
	 * @param c0 {@link MasterClock}
	 * @param c1 {@link MarsClock}
	 * @param m {@link Mars}
	 * @param s {@link SurfaceFeatures}
	 * @param o {@link OrbitInfo}
	 * @param u {@link UnitManager}
	 */
	public static void initializeInstances(MasterClock c0, MarsClock c1, Mars m, SurfaceFeatures s, OrbitInfo o, UnitManager u) {
		masterClock = c0;
		marsClock = c1;
		mars = m;	
		surfaceFeatures = s;
		terrainElevation = s.getTerrainElevation();
		orbitInfo = o;
//		unitManager = u;
	}
	
	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		weatherDataMap = null;
		dailyRecordMap = null;
		todayWeather = null;
		coordinateList = null;
		
		if (temperatureCacheMap != null) {
			temperatureCacheMap.clear();
			temperatureCacheMap = null;
		}
		if (airPressureCacheMap != null) {
			airPressureCacheMap.clear();
			airPressureCacheMap = null;
		}
		marsClock = null;
		surfaceFeatures = null;
		terrainElevation = null;
		masterClock = null;
	}
}