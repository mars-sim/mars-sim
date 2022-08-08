/*
 * Mars Simulation Project
 * Weather.java
 * @date 2022-07-25
 * @author Scott Davis
 * @author Hartmut Prochaska
 */
package org.mars_sim.msp.core.environment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mars_sim.msp.core.CollectionUtils;
import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.air.AirComposition;
import org.mars_sim.msp.core.data.MSolDataItem;
import org.mars_sim.msp.core.data.MSolDataLogger;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.Temporal;
import org.mars_sim.msp.core.tool.RandomUtil;

/** Weather represents the weather on Mars */
public class Weather implements Serializable, Temporal {

	private static final int MAX_RECORDED_DAYS = 2;
	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/* default logger. */
	private static final SimLogger logger = SimLogger.getLogger(Weather.class.getName());

	// Static data
	/** The effect of sunlight on the surface temperatures on Mars. */
	private static final double LIGHT_EFFECT = 1.2;
	/** Extreme cold surface temperatures on Mars at deg Kelvin [or at -153.17 C] */
	private static final double EXTREME_COLD = 120D; 

	/** Viking 1's latitude */
	private static final double VIKING_LATITUDE = 22.48D; // At 22.48E
	private final static double VIKING_DT = Math.round((28D - 15D *
			Math.sin(2 * Math.PI / 180D * VIKING_LATITUDE + Math.PI / 2D) - 13D) * 100.0) / 100.00;
	
	public static final double PARTIAL_PRESSURE_CARBON_DIOXIDE_MARS = 0.57D; // in kPa
	public static final double PARTIAL_PRESSURE_CARBON_DIOXIDE_EARTH = 0.035D; // in kPa
	public static final double PARTIAL_PRESSURE_WATER_VAPOR_ROOM_CONDITION = 1.6D; // in kPa. under Earth's atmosphere,
																					// at 25 C, 50% relative humidity

	private static final int MILLISOLS_PER_UPDATE = 2; // one update per x millisols

	private static final double DX = 255D * Math.PI / 180D - Math.PI;
	
	// Opportunity Rover landed at coordinates 1.95 degrees south, 354.47 degrees
	// east.
	// From the chart, it has an average of 8 C temperature variation on the maximum
	// and minimum temperature curves

	// Spirit Rover landed at 14.57 degrees south latitude and 175.47 degrees east
	// longitude.
	// From the chart, it has an average of 25 C temperature variation on the
	// maximum and minimum temperature curves
	// = 14.57 - 1.95;
	// = 25 - 8;
	private static final double TEMPERATURE_DELTA_PER_DEG_LAT = 17 / 12.62;
	
	// A day has 1000 mSols. Take 500 samples
	private static final int MSOL_PER_SAMPLE = 1000/250;

	private boolean isNewSol = true;

	private int newStormID = 1;

	private int checkStorm = 0;
	// Note: compute the true dailyVariationAirPressure by the end of the day	
	private double dailyVariationAirPressure = RandomUtil.getRandomDouble(.01); // tentatively only
	
	// Singleton only updated in one method
	private Map<Coordinates, MSolDataLogger<DailyWeather>> weatherDataMap;
	
	private List<Coordinates> coordinateList;

	private transient Map<Coordinates, Double> temperatureCacheMap;
	private transient Map<Coordinates, Double> airPressureCacheMap;
	private transient Map<Coordinates, Double> windSpeedCacheMap;
	private transient Map<Coordinates, Integer> windDirCacheMap;

	private List<DustStorm> dustStorms;
	
	private Map<Coordinates, SunData> sunDataMap;
	
	private static Simulation sim;
	private static OrbitInfo orbitInfo;
	private static MarsClock marsClock;

	public Weather() {
		weatherDataMap = new HashMap<>();
		sunDataMap = new HashMap<>();
		
		coordinateList = new ArrayList<>();
		dustStorms = new ArrayList<>();
		
		temperatureCacheMap = new HashMap<>();
		airPressureCacheMap = new HashMap<>();
		windSpeedCacheMap = new HashMap<>();
		windDirCacheMap = new HashMap<>();
	}

	/**
	 * Checks if a location with certain coordinates already exists and add any new
	 * location.
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
				/ (.1921 * (getTemperature(location) + AirComposition.C_TO_K));
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
		double newSpeed = -1;

		if (windSpeedCacheMap == null)
			windSpeedCacheMap = new HashMap<>();

		// On sol 214 in this list of Viking wind speeds, 25.9 m/sec (93.24 km/hr) was
		// recorded.

		// Viking spacecraft from the surface, "during a global dust storm the diurnal
		// temperature range narrowed
		// sharply,...the wind speeds picked up considerably—indeed, within only an hour
		// of the storm's arrival they
		// had increased to 17 m/s (61 km/h), with gusts up to 26 m/s (94 km/h)
		// https://en.wikipedia.org/wiki/Climate_of_Mars

		double rand = RandomUtil.getRandomDouble(.75);

		if (windSpeedCacheMap.containsKey(location)) {
			double currentSpeed = windSpeedCacheMap.get(location);
			
			// check for the passing of each day
			if (isNewSol) {
				Settlement focus = CollectionUtils.findSettlement(location);
				DustStorm ds = (focus != null ? focus.getDustStorm() : null);
				if (ds != null) {
					double dustSpeed = ds.getSpeed();
					switch (ds.getType()) {
						case DUST_DEVIL:
							// arbitrary speed determination
							newSpeed = .8 * currentSpeed + .2 * dustSpeed;
							break;
	
						case LOCAL:
							// arbitrary speed determination
							newSpeed = .985 * currentSpeed + .015 * dustSpeed;
							break;
						
						case REGIONAL:
							// arbitrary speed determination
							newSpeed = .99 * currentSpeed + .01 * dustSpeed;
							break;
	
						case PLANET_ENCIRCLING:
							// arbitrary speed determination
							newSpeed = .995 * currentSpeed + .005 * dustSpeed;
							break;
					}
				}
			}
			if (newSpeed < 0) {
				newSpeed = currentSpeed*(1 - .02 * rand) 
						+ 1.15 * rand - RandomUtil.getRandomDouble(.01);				
			}
		}
		else {
			newSpeed = rand;
		}

		// Despite secondhand estimates of higher velocities, official observed gust
		// velocities on Mars are
		// in the range of 80-120 mph (120-160 km/hr).
		// At higher altitudes, the movement of dust was measured at 250-300 mph
		// (400-480 km/hr).

//		if (new_speed > 50) // assume the max surface wind speed of up to 50 m/s
//			new_speed = 50;
		newSpeed = Math.round(newSpeed *100.0)/100.0;
		
		windSpeedCacheMap.put(location, newSpeed);
				
		return newSpeed;
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

		int newDir = RandomUtil.getRandomInt(359);

		if (windDirCacheMap == null)
			windDirCacheMap = new HashMap<>();

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
			airPressureCacheMap = new HashMap<>();
		}

		if (marsClock.getMillisolInt() % MILLISOLS_PER_UPDATE == 1) {
			double newP = calculateAirPressure(location, 0);
			airPressureCacheMap.put(location, newP);
			return newP;
		} else {
			return getCachedReading(airPressureCacheMap, location);// , AIR_PRESSURE);
		}
	}

	/**
	 * Calculates the air pressure at a given location and/or height.
	 * 
	 * @param location
	 * @param height   [in km]
	 * @return air pressure [in kPa]
	 */
	public double calculateAirPressure(Coordinates location, double height) {
		// Get local elevation in meters.
		double elevation = 0;

		if (height == 0)
			elevation = TerrainElevation.getMOLAElevation(location); // in km since getElevation() return the value in km
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

		checkLocation(location);

		// Lazy instantiation of temperatureCacheMap.
		if (temperatureCacheMap == null) {
			temperatureCacheMap = new HashMap<>();
		}

		double t = 0;
		
		if (marsClock.getMillisolInt() % MILLISOLS_PER_UPDATE == 0) {
			double newT = calculateTemperature(location);
			temperatureCacheMap.put(location, newT);
			t = newT;
		} else {
			t = getCachedReading(temperatureCacheMap, location);
		}
		
		double previousTemperature = 0;
		if (temperatureCacheMap.containsKey(location)) {
			previousTemperature = temperatureCacheMap.get(location);
		}

		t = Math.round((t + previousTemperature) / 2.0 * 100.0) / 100.0;
		
		return t;
	}

	/**
	 * Calculates the mid-air temperature.
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

		if (sim.getSurfaceFeatures().inDarkPolarRegion(location)) {

			// vs. just in inPolarRegion()
			// see http://www.alpo-astronomy.org/jbeish/Observing_Mars_3.html
			// Note that the polar region may be exposed to more sunlight

			// see https://www.atmos.umd.edu/~ekalnay/pubs/2008JE003120.pdf
			// The swing can be plus and minus 10K deg

			t = EXTREME_COLD + RandomUtil.getRandomDouble(10)
					- AirComposition.C_TO_K;

			// double millisol = marsClock.getMillisol();
			// TODO: how to relate at what millisols are the mean daytime and mean night
			// time at the pole ?

		}

		else if (sim.getSurfaceFeatures().inPolarRegion(location)) {

			// Based on Surface brightness temperatures at 32 µm retrieved from the MCS data
			// for
			// over five Mars Years (MY), at the “Tleilax” site.

			double lS = orbitInfo.getL_s();

			// split into 6 zones for linear curve fitting for each martian year
			// See chart at https://www.hou.usra.edu/meetings/marspolar2016/pdf/6012.pdf

			if (lS < 90)
				t = 0.8333 * lS + 145;
			else if (lS <= 180)
				t = -0.8333 * lS + 295;
			else if (lS <= 225)
				t = -.3333 * lS + 205;
			else if (lS <= 280)
				t = .1818 * lS + 89.091;
			else if (lS <= 320)
				t = -.125 * lS + 175;
			else if (lS <= 360)
				t = .25 * lS + 55;

			t = t + RandomUtil.getRandomDouble(-1.0, 1.0) - AirComposition.C_TO_K;

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
			
			double lightFactor = sim.getSurfaceFeatures().getSunlightRatio(location) * LIGHT_EFFECT;

			// Equation below is modeled after Viking's data.
			double equatorialTemperature = 27.5D * lightFactor - 58.5D;

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

			double elevation = TerrainElevation.getMOLAElevation(location); // in km from getElevation(location)
			double terrain_dt;

			// Assume a typical temperature of -31 deg celsius
			if (elevation < 7)
				terrain_dt = -0.000998 * elevation * 1000;
			else // delta = -31 + 23.4 = 7.6
				terrain_dt = 7.6 - 0.00222 * elevation * 1000;

			// (3). Latitude
			double latDegree = location.getPhi2Lat();

			double latDt = -15D * (1 + Math.sin(2D * latDegree * Math.PI / 180D + Math.PI / 2D));

			// (4). Seasonal variation
			double latAdjustment = TEMPERATURE_DELTA_PER_DEG_LAT * latDegree; // an educated guess
			int solElapsed = marsClock.getMissionSol();
			double seasonalDt = latAdjustment * Math.sin(2 * Math.PI / 1000D * (solElapsed - 142));

			// (5). Add windspeed
			double windDt = 0;
			if (windSpeedCacheMap == null)
				windSpeedCacheMap = new HashMap<>();

			if (windSpeedCacheMap.containsKey(location))
				windDt = 10.0 / (1 + Math.exp(-.15 * windSpeedCacheMap.get(location)));

			// Subtotal		
			t = equatorialTemperature + VIKING_DT - latDt - terrain_dt + seasonalDt;

			if (t > 0)
				t = t + windDt;
			else
				t = t - windDt;
			
			// (5). Limit the highest and lowest temperature
			if (t > 40)
				t = 40;
			
			else if (t < -160)
				t = -160;

			// (6). Add randomness
			double rand = RandomUtil.getRandomDouble(-1.0, 1.0);
			
			// (7). Total
			t += rand;  
					
			double previousTemperature = 0;
			if (temperatureCacheMap == null) {
				temperatureCacheMap = new HashMap<>();
			}

			if (temperatureCacheMap.containsKey(location)) {
				previousTemperature = temperatureCacheMap.get(location);
			}

			t = Math.round((t + previousTemperature) / 2.0 * 100.0) / 100.0;
		}

		return t;
	}

	/**
	 * Clears weather-related parameter cache map to prevent excessive build-up of
	 * key-value sets.
	 */
	public synchronized void clearMap() {
		if (temperatureCacheMap != null) {
			temperatureCacheMap.clear();
		}

		if (airPressureCacheMap != null) {
			airPressureCacheMap.clear();
		}
		
		if (windSpeedCacheMap != null) {
			windSpeedCacheMap.clear();
		}
		
		if (windDirCacheMap != null) {
			windDirCacheMap.clear();
		}
	}

	/**
	 * Provides the surface temperature or air pressure at a given location from the
	 * temperatureCacheMap. If calling the given location for the first time from
	 * the cache map, call update temperature/air pressure instead.
	 * 
	 * @return temperature or pressure
	 */
	private double getCachedReading(Map<Coordinates, Double> map, Coordinates location) {
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
	 * Creates a weather record based on yestersol sun data.
	 */
	public void addWeatherDataPoint() {
		coordinateList.forEach(location ->  {			
			MSolDataLogger<DailyWeather> dailyRecordMap = null;				
			if (weatherDataMap.containsKey(location)) {
				dailyRecordMap = weatherDataMap.get(location);
			}
			else {
				dailyRecordMap = new MSolDataLogger<>(MAX_RECORDED_DAYS);
			}	
			
			DailyWeather dailyWeather = new DailyWeather( 
					getTemperature(location), 
					getAirPressure(location),
					getAirDensity(location), 
					getWindSpeed(location), 
					sim.getSurfaceFeatures().getSolarIrradiance(location),
					sim.getSurfaceFeatures().getOpticalDepth(location));
			
			dailyRecordMap.addDataPoint(dailyWeather);
			
			weatherDataMap.put(location, dailyRecordMap);
		});
	}
	
	/**
	 * Time passing in the simulation.
	 * 
	 * @param time time in millisols
	 * @throws Exception if error during time.
	 */
	public boolean timePassing(ClockPulse pulse) {
		isNewSol = pulse.isNewSol();

		// Sample a data point every RECORDING_FREQUENCY (in millisols)
		int msol = marsClock.getMillisolInt();
		int remainder = msol % MSOL_PER_SAMPLE;
		if (isNewSol || remainder == 0) {		
			// Add a data point
			addWeatherDataPoint();
		}

		if (isNewSol) {
			// Calculate the new sun data for each location based on yestersol
			coordinateList.forEach(this::calculateSunRecord);
					
			dailyVariationAirPressure += RandomUtil.getRandomDouble(.01);
			if (dailyVariationAirPressure > .05)
				dailyVariationAirPressure = .05;
			else if (dailyVariationAirPressure < -.05)
				dailyVariationAirPressure = -.05;

			// more often observed from mid-southern summer, between 241 deg and 270 deg Ls,
			// with a peak period at 255 deg Ls.

			// Note : The Mars dust storm season begins just after perihelion at around Ls =
			// 260°.
			double lS = Math.round(orbitInfo.getL_s() * 10.0) / 10.0;
			int lSint = (int) lS;

			if (lSint == 230) {
				// reset the counter once a year
				checkStorm = 0;
			}

			// Arbitrarily assume
			// (1) 5% is the highest chance of forming a storm, if L_s is right at 255 deg
			// (2) 0.05% is the lowest chance of forming a storm, if L_s is right at 75 deg

			// By doing curve-fitting a cosine curve
			// (5% - .05%)/2 = 2.475

			double probability = -2.475 * Math.cos(lS * Math.PI / 180D - DX) + (2.475 + .05);
			// probability is 5% at max
			double size = dustStorms.size();
			// Artificially limit the # of dust storm to 10
			if (lSint > 240 && lSint < 271 && size <= 10 && checkStorm < 200) {
				// When L_s = 250 (use 255 instead), Mars is at perihelion--when the sun is
				// closed to Mars.

				// All of the observed storms have begun within 50-60 degrees of Ls of
				// perihelion (Ls ~ 250);
				createDustDevils(probability, lS);
			}

			else if (dustStorms.size() <= 20 && checkStorm < 200) {

				createDustDevils(probability, lS);
			}

			checkOnDustStorms();
			
			// computeDailyVariationAirPressure();
		}
		return true;
	}


	/**
	 * Calculates the sunlight data of a settlement location.
	 * 
	 * @param c
	 * @return
	 */
	public void calculateSunRecord(Coordinates c) {			
		List<MSolDataItem<DailyWeather>> dailyWeatherList = null;

		if (weatherDataMap.containsKey(c)) {
			MSolDataLogger<DailyWeather> w = weatherDataMap.get(c);
	
			if (!w.isYestersolDataValid()) {
				logger.warning(3_000L, "Weather data from yesterday at " + c + " is not available.");
				return;
			}
			else
				dailyWeatherList = w.getYestersolData();
		}
		else {
			logger.warning(3_000L, "Weather data at " + c + " is not available.");
		}

		int sunrise = 0;
		int sunset = 0;
		int maxIndex0 = 0;
		int maxIndex1 = 0;		
		int maxSun = 0;
		int previous = 0;
		int daylight = 0;
				
		for (MSolDataItem<DailyWeather> dataPoint : dailyWeatherList) {
			// Gets the solar irradiance at this instant of time
			int current = (int)(Math.round(dataPoint.getData().getSolarIrradiance()*10.0)/10.0);
			// Gets this instant of time
			int t = dataPoint.getMsol();
			if (current > 0) {
				// Sun up
				if (current > previous && previous <= 0) {
					sunrise = t;
				}
			}
			else {
				// Sun down
				if (current < previous && previous > 0) {
					sunset = t;
				}
			}

			// Gets maxSun as the max solar irradiance
			// Gets maxIndex0 at this instant of time
			if (current > maxSun && current > previous) {
				maxSun = current;
				maxIndex0 = t;
			}
			
			if (current < maxSun && previous == maxSun) {
				maxIndex1 = t;
			}	
			
			previous = current;
		}
		
		if (sunrise > sunset)
			daylight = sunset + 1000 - sunrise;
		else
			daylight = sunset - sunrise ;
		
		if (sunrise > 1000)
			sunrise = sunrise - 1000;
		if (sunrise < 0)
			sunrise = sunrise + 1000;
		
		if (sunset > 1000)
			sunset = sunset - 1000;
		if (sunset < 0)
			sunset = sunset + 1000;
		
		if (maxIndex1 < maxIndex0)
			maxIndex1 += 1000;
			
		int duration = maxIndex1 - maxIndex0;
		
		int zenith = maxIndex0 + duration/2;
		
		if (zenith > 1000)
			zenith = zenith - 1000;
		if (sunset < 0)
			zenith = zenith + 1000;
		
		SunData sunData = new SunData(sunrise, sunset, daylight, zenith, maxSun);
		// Overwrite the previous data
		sunDataMap.put(c, sunData);
	}
	

	
	/**
	 * Gets the sun data record.
	 * 
	 * @param c
	 * @return
	 */
	public SunData getSunRecord(Coordinates c) {
		if (sunDataMap.containsKey(c))
			return sunDataMap.get(c);
		
		return null;
	}
	

	/**
	 * Checks if a dust devil is formed for each settlement.
	 * 
	 * @param probability
	 * @param L_s_int
	 */
	private void createDustDevils(double probability, double ls) {
		List<Settlement> settlements = new ArrayList<>(sim.getUnitManager().getSettlements());
		for (Settlement s : settlements) {
			
			if (s.getDustStorm() == null) {
				// if settlement doesn't have a dust storm formed near it yet
		
				double chance = RandomUtil.getRandomDouble(100);
				if (chance <= probability) {

					// arbitrarily set to the highest 3% chance (if L_s is 241 or 270) of generating
					// a dust devil
					// on each sol since it is usually created in Martian spring or summer day,
					checkStorm++;

					// Assuming all storms start out as a dust devil
					DustStorm ds = new DustStorm(DustStormType.DUST_DEVIL, newStormID,
							 this, s.getIdentifier());
					dustStorms.add(ds);
					s.setDustStorm(ds);
					newStormID++;

					logger.info(s, "On L_s = " + Math.round(ls * 100.0) / 100.0
									+ ", " + ds.getName()); 
				}
			}
		}
	}


	/**
	 * Checks to DustStorms.
	 */
	private void checkOnDustStorms() {
		boolean allowPlantStorms = (dustStorms.stream()
				.filter(d -> d.getType() == DustStormType.PLANET_ENCIRCLING)
				.count() < 2);
		
		if (!dustStorms.isEmpty()) {
			List<DustStorm> storms = new ArrayList<>(dustStorms);
			for (DustStorm ds : storms) {
				if (ds.computeNewSize(allowPlantStorms) == 0) {
					dustStorms.remove(ds);
				} 
		
				if (ds.getSize() != 0) {
					Settlement closest = ds.getSettlement();
					logger.info(closest, "DustStorm " + ds.getName()
									+ " (size " + ds.getSize() + " with windspeed "
									+ Math.round(ds.getSpeed() * 10.0) / 10.0 + " m/s) was sighted.");
				}
			}
		}
	}

	public double getDailyVariationAirPressure(Coordinates location) {
		return dailyVariationAirPressure;
	}

	/**
	 * Reloads instances after loading from a saved sim.
	 * 
	 * @param sim
	 * @param clock
	 * @param orbitInfo
	 */
	public static void initializeInstances(Simulation s, MarsClock c, OrbitInfo oi) {
		sim = s; 
		marsClock = c;
		orbitInfo = oi;
	}
	
	/**
	 * Prepares object for garbage collection.
	 */
	public void destroy() {
		weatherDataMap.clear();
		weatherDataMap = null;
		coordinateList.clear();
		coordinateList = null;
		
		if (temperatureCacheMap != null) {
			temperatureCacheMap.clear();
			temperatureCacheMap = null;
		}
		if (airPressureCacheMap != null) {
			airPressureCacheMap.clear();
			airPressureCacheMap = null;
		}
		if (windSpeedCacheMap != null) {
			windSpeedCacheMap.clear();
			windSpeedCacheMap = null;
		}
		if (windDirCacheMap != null) {
			windDirCacheMap.clear();
			windDirCacheMap = null;
		}
		if (dustStorms != null) {
			dustStorms.clear();
			dustStorms = null;
		}

		sunDataMap.clear();
		sunDataMap = null;
		sim = null;
		orbitInfo = null;
		marsClock = null;				
	}
}
