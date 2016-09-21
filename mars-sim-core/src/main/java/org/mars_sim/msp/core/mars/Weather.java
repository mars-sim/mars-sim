/**
 * Mars Simulation Project
 * Weather.java
 * @version 3.08 2015-06-15
 * @author Scott Davis
 * @author Hartmut Prochaska
 */
package org.mars_sim.msp.core.mars;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.MasterClock;

/** Weather represents the weather on Mars */
public class Weather
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Static data
	/** Sea level air pressure in kPa. */
	//2014-11-22 Set the unit of air pressure to kPa
	//private static final double SEA_LEVEL_AIR_PRESSURE = .8D;
	/** Sea level air density in kg/m^3. */
	//private static final double SEA_LEVEL_AIR_DENSITY = .0115D;
	/** Mars' gravitational acceleration at sea level in m/sec^2. */
	//private static final double SEA_LEVEL_GRAVITY = 3.0D;
	/** extreme cold temperatures at Mars. */
	private static final double EXTREME_COLD = -120D;

	/** Viking 1's longitude (49.97 W) in millisols  */
	private static final double VIKING_LONGITUDE_OFFSET_IN_MILLISOLS = 138.80D; 	// = 49.97W/180 deg * 500 millisols;
	private static final double VIKING_LATITUDE = 22.48D; // At 22.48E

	public static final double PARTIAL_PRESSURE_CARBON_DIOXIDE_MARS = 0.57D; 	// in kPa
	public static final double PARTIAL_PRESSURE_CARBON_DIOXIDE_EARTH = 0.035D; 	// in kPa
	public static final double PARTIAL_PRESSURE_WATER_VAPOR_ROOM_CONDITION = 1.6D; 	// in kPa. under Earth's atmosphere, at 25 C, 50% relative humidity

	private static final int TEMPERATURE = 0;
	private static final int AIR_PRESSURE = 1;
	private static final int AIR_DENSITY = 2;
	private static final int WIND_SPEED = 3;

	private static final int MILLISOLS_PER_UPDATE = 5 ; // one update per x millisols

	private static final int RECORDING_FREQUENCY = 50; // in millisols

	private int quotientCache;

	private int millisols;

	private int numlocalDustStorm = 0;
	//numRegionalDustStorm = 0;
	//private int numPlanetEncirclingDustStorm = 0;

	private int checkStorm = 0;

	/** Current sol since the start of sim. */
	private int solCache = 1;

	private double sum;

	private double viking_dt;

	private double final_temperature = EXTREME_COLD;

	private double TEMPERATURE_DELTA_PER_DEG_LAT = 0D;

	private double dailyVariationAirPressure =  RandomUtil.getRandomDouble(.05); // tentatively only
	//TODO: compute the true dailyVariationAirPressure by the end of the day


	private Map <Coordinates, Map<Integer, List<DailyWeather>>> weatherDataMap = new ConcurrentHashMap<>();
	private Map <Integer, List<DailyWeather>> dailyRecordMap = new ConcurrentHashMap<>();

	private List<DailyWeather> todayWeather = new CopyOnWriteArrayList<>();
	private List<Coordinates> coordinateList = new CopyOnWriteArrayList<>();

	private transient Map<Coordinates, Double> temperatureCacheMap;
	private transient Map<Coordinates, Double> airPressureCacheMap;
	private transient Map<Coordinates, Double> windSpeedCacheMap;
	private transient Map<Coordinates, Integer> windDirCacheMap;


	private static Map<Integer, DustStorm> planetEncirclingDustStormMap = new ConcurrentHashMap<>();
	private static Map<Integer, DustStorm> regionalDustStormMap = new ConcurrentHashMap<>();
	private static Map<Integer, DustStorm> localDustStormMap = new ConcurrentHashMap<>();
	private static Map<Integer, DustStorm> dustDevilMap = new ConcurrentHashMap<>();

	private Simulation sim = Simulation.instance();
	private MarsClock marsClock;
	private SurfaceFeatures surfaceFeatures;
	private TerrainElevation terrainElevation;
	private MasterClock masterClock;
	private OrbitInfo orbitInfo;
	private Mars mars;


	/** Constructs a Weather object */
	public Weather() {
		//System.out.println("Starting Weather constructor");

		viking_dt = 28D - 15D * Math.sin(2 * Math.PI/180D * VIKING_LATITUDE + Math.PI/2D) - 13D;
		viking_dt = Math.round (viking_dt * 100.0)/ 100.00;
		//System.out.print("  viking_dt: " + viking_dt );

		// Opportunity Rover landed at coordinates 1.95 degrees south, 354.47 degrees east.
		// From the chart, it has an average of 8 C temperature variation on the maximum and minimum temperature curves

		// Spirit Rover landed at 14.57 degrees south latitude and 175.47 degrees east longitude.
		// From the chart, it has an average of 25 C temperature variation on the maximum and minimum temperature curves

		double del_latitude = 14.57-1.95;
		int del_temperature = 25 - 8;

		// assuming a linear relationship
		TEMPERATURE_DELTA_PER_DEG_LAT = del_temperature / del_latitude;

		if (masterClock == null) {
			if (sim.getMasterClock() != null)
				masterClock = sim.getMasterClock();
		}
		//orbitInfo = Simulation.instance().getMars().getOrbitInfo();
	}

	/**
	 * Checks if a location with certain coordinates already exists and add any new location
	 * @param location
	 */
	// 2015-03-17 Added computeAirDensity()
	public void checkLocation(Coordinates location) {
		if (!coordinateList.contains(location))
			coordinateList.add(location);
	}

	/**
	 * Gets the air density at a given location.
	 * @return air density in g/m3.
	 */
	// 2015-03-17 Added computeAirDensity()
	public double computeAirDensity(Coordinates location) {
		double result = 0;
		checkLocation(location);
		//The air density is derived from the equation of state : d = p / .1921 / (t + 273.1)
		result = 1000D * getAirPressure(location) / (.1921 * (getTemperature(location) + 273.1));
		result = Math.round(result *10.0)/10.0;
	 	return result;
	}

	/**
	 * Gets the air density at a given location.
	 * @return air density in kg/m3.
	 */
	// 2015-04-08 Added getAirDensity()
	public double getAirDensity(Coordinates location) {
		return computeAirDensity(location);
	}

	/**
	 * Gets the wind speed at a given location.
	 * @return wind speed in m/s.
	 */
	// 2015-05-01 Added computeWindSpeed()
	public double computeWindSpeed(Coordinates location) {
		double result = 0;
		//checkLocation(location);

		double rand = RandomUtil.getRandomDouble(1) - RandomUtil.getRandomDouble(1);

		if (windSpeedCacheMap == null)
			windSpeedCacheMap = new ConcurrentHashMap<>();

		if (windSpeedCacheMap.containsKey(location))
			// TODO: get wind speed using theoretical model and/or empirical data
			result = windSpeedCacheMap.get(location) + rand;
		else {
			result = rand;
		}

		if (result > 15)
			result = 15;
		if (result < 0)
			result = 0;

		windSpeedCacheMap.put(location, result);

		return result;
	}

	/**
	 * Gets the wind speed at a given location.
	 * @return wind speed in m/s.
	 */
	// 2015-05-01 Added getWindSpeed()
	public double getWindSpeed(Coordinates location) {
		return computeWindSpeed(location);
	}

	// 2015-05-01 Added getWindDirection()
	public int getWindDirection(Coordinates location) {
		return computeWindDirection(location);
	}

	// 2015-05-01 Added computeWindDirection()
	public int computeWindDirection(Coordinates location) {
		int result = 0;
		//checkLocation(location);

		int newDir = RandomUtil.getRandomInt(359);

		if (windDirCacheMap == null)
			windDirCacheMap = new ConcurrentHashMap<>();

		if (windDirCacheMap.containsKey(location))
			// TODO: should the ratio of the weight of the past direction and present direction of the wind be 9 to 1 ?
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
	 * @return air pressure in Pa.
	 */
	// 2015-04-08 Added getAirPressure()
	public double getAirPressure(Coordinates location) {
		return getCachedAirPressure(location);
	}

	/**
	 * Gets the cached air pressure at a given location.
	 * @return air pressure in kPa.
	 */
	// 2015-03-06 Added getCachedAirPressure()
	public double getCachedAirPressure(Coordinates location) {
		checkLocation(location);
/*
		if (masterClock == null)
			masterClock = Simulation.instance().getMasterClock();
		if (marsClock == null)
			marsClock = masterClock.getMarsClock();
	    millisols =  (int) marsClock.getMillisol() ;
		//System.out.println("oneTenthmillisols : " + oneTenthmillisols);
*/
	    // Lazy instantiation of airPressureCacheMap.
	    if (airPressureCacheMap == null) {
	        airPressureCacheMap = new ConcurrentHashMap<Coordinates, Double>();
        }


		if (millisols % MILLISOLS_PER_UPDATE == 1) {
			//System.out.println("marsClock : "+ marsClock);
			//System.out.println("millisols : "+ millisols);
			double newP = calculateAirPressure(location);
			airPressureCacheMap.put(location, newP);
			//System.out.println("air pressure : "+cache);
			return newP;
		}
		else {
		    return getCachedReading(airPressureCacheMap, location);//, AIR_PRESSURE);
		}
	}

	/**
	 * Calculates the air pressure at a given location.
	 * @return air pressure in kPa.
	 */
	public double calculateAirPressure(Coordinates location) {
		// Get local elevation in meters.
		if (terrainElevation == null)
			terrainElevation = sim.getMars().getSurfaceFeatures().getTerrainElevation();

		double elevation = terrainElevation.getElevation(location) ; // in km since getElevation() return the value in km


		// p = pressure0 * e(-((density0 * gravitation) / pressure0) * h)
		//  Q: What are these enclosed values ==>  P = 0.009 * e(-(0.0155 * 3.0 / 0.009) * elevation)
		//double pressure = SEA_LEVEL_AIR_PRESSURE * Math.exp(-1D *
		//		SEA_LEVEL_AIR_DENSITY * SEA_LEVEL_GRAVITY / (SEA_LEVEL_AIR_PRESSURE * 1000)* elevation);

		// why * 1000 ?

		// elevation is in km. it should probably read
		// double pressure = SEA_LEVEL_AIR_PRESSURE * Math.exp(-1D *
		//		SEA_LEVEL_AIR_DENSITY * SEA_LEVEL_GRAVITY / (SEA_LEVEL_AIR_PRESSURE)* elevation * 1000);

		// If using the precalculated values at http://www.grc.nasa.gov/WWW/k-12/airplane/atmosmrm.html for modeling Mars ,
		// p = .699 * exp(-0.00009 * h) in kilo-pascal or kPa
		double pressure2 = .699 * Math.exp(-0.00009 * elevation * 1000);
		//System.out.println("elevation is " + elevation  + "   pressure2 is " + pressure2);

		// Added randomness
		double up = RandomUtil.getRandomDouble(.05);
		double down = RandomUtil.getRandomDouble(.05);

		pressure2 = pressure2 + up - down;

		return pressure2;
	}

	/**
	 * Gets the temperature at a given location.
	 * @return temperature in deg Celsius.
	 */
	public double getTemperature(Coordinates location) {
		return getCachedTemperature(location);
	}

	/**
	 * Gets the cached temperature at a given location.
	 * @return temperature in deg Celsius.
	 */
	public double getCachedTemperature(Coordinates location) {
		checkLocation(location);
/*
		if (masterClock == null)
			masterClock = Simulation.instance().getMasterClock();
		if (marsClock == null)
			marsClock = masterClock.getMarsClock();
	    millisols =  (int) marsClock.getMillisol() ;
*/
	    // Lazy instantiation of temperatureCacheMap.
	    if (temperatureCacheMap == null) {
            temperatureCacheMap = new ConcurrentHashMap<Coordinates, Double>();
        }

		if (millisols % MILLISOLS_PER_UPDATE == 0) {
			double newT = calculateTemperature(location);
			temperatureCacheMap.put(location, newT);
			//System.out.println("Weather.java: temperatureCache is " + temperatureCache);
			return newT;
		}
		else {
		    return getCachedReading(temperatureCacheMap, location);//, TEMPERATURE);
		}
	}


	/**
	 * Calculates the surface temperature at a given location.
	 * @return temperature in Celsius.
	 */
	public double calculateTemperature(Coordinates location) {

		if (surfaceFeatures == null)
			surfaceFeatures = sim.getMars().getSurfaceFeatures();

		if (terrainElevation == null)
			terrainElevation = surfaceFeatures.getTerrainElevation();

		if (surfaceFeatures.inDarkPolarRegion(location)){
			//known temperature for cold days at the pole
			final_temperature = -150D + RandomUtil.getRandomDouble(3) - RandomUtil.getRandomDouble(3);

		} else {
			// 2015-01-28 We arrived at this temperature model based on Viking 1 & Opportunity Rover
			// by assuming the temperature is the linear combination of the following factors:
			// 1. Time of day, longitude and solar irradiance,
			// 2. Terrain elevation,
			// 3. Latitude,
			// 4. Seasonal variation (dependent upon latitude)
			// 5. Randomness
			// 6. Wind speed
			/*
			if (masterClock == null)
				masterClock = Simulation.instance().getMasterClock();

			marsClock = masterClock.getMarsClock();

			// (1). Time of day, longitude (included inside in solar irradiance)
			double theta = location.getTheta() / Math.PI * 500D; // convert theta in longitude in radian to millisols;
			//System.out.println(" theta: " + theta);
	        double time  = marsClock.getMillisol();
	        double x_offset = time + theta - VIKING_LONGITUDE_OFFSET_IN_MILLISOLS ;
	        double equatorial_temperature = 27.5D * Math.sin  ( Math.PI * x_offset / 500D) - 58.5D ;
			equatorial_temperature = Math.round (equatorial_temperature * 100.0)/100.0;
			System.out.println("factor : " + Math.sin  ( Math.PI * x_offset / 500D) + "\tequatorial T: "+ equatorial_temperature);
			//System.out.print("Time: " + Math.round (time) + "  T: " + standard_temperature);
*/
			double light_factor = 0;
			double sunlight = 0 ;
			sunlight = surfaceFeatures.getSolarIrradiance(location);

			//light_factor = 2D * ( sunlight / SurfaceFeatures.MEAN_SOLAR_IRRADIANCE - .5);
			light_factor = sunlight / SurfaceFeatures.MEAN_SOLAR_IRRADIANCE;

			// Equation below is modeled after Viking's data.
			double equatorial_temperature = 27.5D * light_factor - 58.5D ;
			equatorial_temperature = Math.round (equatorial_temperature * 100.0)/100.0;
			//System.out.println("sunlight : " + sunlight + "\t\tequatorial T: "+ equatorial_temperature);


		/*
			// ...getSurfaceSunlight * (80D / 127D (max sun))
			// if sun full we will get -40D the avg, if night or twilight we will get
			// a smooth temperature change and in the night -120D
		    temperature = temperature + surfaceFeatures.getSurfaceSunlight(location) * 80D;
		*/

			// (2). Terrain Elevation
			// use http://www.grc.nasa.gov/WWW/k-12/airplane/atmosmrm.html for modeling Mars with precalculated values
			// The lower atmosphere runs from the surface of Mars to 7,000 meters.
			// 	T = -31 - 0.000998 * h
			// The upper stratosphere model is used for altitudes above 7,000 meters.
			// T = -23.4 - 0.00222 * h
			double elevation =  terrainElevation.getElevation(location); // in km from getElevation(location)
			double terrain_dt;

			// Assume a typical temperature of -31 deg celsius
			if (elevation < 7)
				terrain_dt = - 0.000998 * elevation * 1000;
			else // delta = -31 + 23.4 = 7.6
				terrain_dt = 7.6 - 0.00222 * elevation * 1000;

			terrain_dt = Math.round (terrain_dt * 100.0)/ 100.0;
			//System.out.print("  terrain_dt: " + terrain_dt );


			// (3). Latitude
			double lat_degree = location.getPhi2Lat();

			//System.out.print("  degree: " + Math.round (degree * 10.0)/10.0 );
			double lat_dt = -15D - 15D * Math.sin( 2D * lat_degree * Math.PI/180D + Math.PI/2D) ;
			lat_dt = Math.round (lat_dt * 100.0)/ 100.0;
			//System.out.println("  lat_dt: " + lat_dt );

			// (4). Seasonal variation
			double lat_adjustment = TEMPERATURE_DELTA_PER_DEG_LAT * lat_degree; // an educated guess
			//marsClock = masterClock.getMarsClock();
			//System.out.println(marsClock);
			int solElapsed = MarsClock.getSolOfYear(marsClock);
			double seasonal_dt = lat_adjustment * Math.sin( 2 * Math.PI/1000D * ( solElapsed - 142));
			seasonal_dt = Math.round (seasonal_dt * 100.0)/ 100.0;
			//System.out.println("  seasonal_dt: " + seasonal_dt );


			// (5). Add randomness
			double up = RandomUtil.getRandomDouble(2);
			double down = RandomUtil.getRandomDouble(2);

			// (6). Add Windspped

			double wind_dt = 0;
			if (windSpeedCacheMap == null)
				windSpeedCacheMap = new ConcurrentHashMap<>();

			if (windSpeedCacheMap.containsKey(location))
				wind_dt = windSpeedCacheMap.get(location) * 1.5D;

			final_temperature = equatorial_temperature + viking_dt - lat_dt - terrain_dt + seasonal_dt - wind_dt + up - down;

			double previous_t = 0;
			if (temperatureCacheMap == null) {
			    temperatureCacheMap = new ConcurrentHashMap<Coordinates, Double>();
			}

			if (temperatureCacheMap.containsKey(location)) {
				previous_t = temperatureCacheMap.get(location);
			}

			final_temperature = Math.round ((final_temperature + previous_t )/2.0 *100.0)/100.0;
			//System.out.println("  final T: " + final_temperature );
		}

		//temperatureCacheMap.put(location, final_temperature);

		return final_temperature;
	}

	/**
	 * Clears weather-related parameter cache map to prevent excessive build-up of key-value sets
	 */
	// 2015-03-06 Added clearMap()
    public synchronized void clearMap() {
        if (temperatureCacheMap != null) {
            temperatureCacheMap.clear();
        }

        if (airPressureCacheMap != null) {
            airPressureCacheMap.clear();
        }
    }

	/**
	 * Provides the surface temperature /air pressure at a given location from the temperatureCacheMap.
	 * If calling the given location for the first time from the cache map, call update temperature/air pressure instead
	 * @return temperature or pressure
	 */
	// 2015-03-06 Added getCachedReading()
    public double getCachedReading(Map<Coordinates, Double> map, Coordinates location) {//, int value) {
    	double result;

       	if (map.containsKey(location)) {
       		result = map.get(location);
    	}
    	else {
    		double cache = 0;
    		//if (value == TEMPERATURE )
    		if (map.equals(temperatureCacheMap))
    			cache = calculateTemperature(location);
    		//else if (value == AIR_PRESSURE )
    		else  if (map.equals(airPressureCacheMap))
    			cache = calculateAirPressure(location);

			map.put(location, cache);

    		result = cache;
    	}
		//System.out.println("air pressure cache : "+ result);
    	return result;
    }

	/**
	 * Time passing in the simulation.
	 * @param time time in millisols
	 * @throws Exception if error during time.
	 */
	public void timePassing(double time) {

		if (masterClock == null)
			masterClock = Simulation.instance().getMasterClock();

		if (marsClock == null)
			marsClock = masterClock.getMarsClock();

		// Sample a data point every RECORDING_FREQUENCY (in millisols)
	    millisols =  (int) marsClock.getMillisol();

	    int quotient = millisols / RECORDING_FREQUENCY;

	    if (quotientCache != quotient) {

	    	coordinateList.forEach(location -> {
	    		DailyWeather weather = new DailyWeather(marsClock, getTemperature(location), getAirPressure(location),
	    				getAirDensity(location), getWindSpeed(location),
	    				surfaceFeatures.getSolarIrradiance(location), surfaceFeatures.getOpticalDepth(location));
		    	todayWeather.add(weather);
	    	});

	    	quotientCache = quotient;

	    }

	    // check for the passing of each day
	    int newSol = MarsClock.getSolOfYear(marsClock);
		if (newSol != solCache) {

			if (mars == null)
				mars = sim.getMars();
			if (orbitInfo == null)
				orbitInfo = mars.getOrbitInfo();
			
			double L_s = orbitInfo.getL_s();
			// When L_s = 250, Mars is at perihelion--when the sun is closed to Mars.
			// Mars has the highest liklihood of producing a global dust storm
			// All of the observed storms have begun within 50-60 degrees of Ls of perihelion (Ls ~ 250);
			// more often observed from mid-southern summer, between 241 deg and 270 deg Ls, with a peak period at 255 deg Ls.

			// Assuming all storms start out as a local storm
			// Artificially limit the # of local storm to 20
			if (L_s > 240 && L_s < 271 && localDustStormMap.size() <= 30 && checkStorm < 20) {
				checkStorm++;

				int num = RandomUtil.getRandomInt(100);
				if (num < 6) { // arbitrarily set to 5% chance of generating a regional dust storm on each sol
					//numlocalDustStorm++;
					DustStorm ds = new DustStorm("Local Storm " + localDustStormMap.size() + 1, DustStormType.LOCAL, marsClock);
					ds.setWeather(this);
					localDustStormMap.put(localDustStormMap.size(), ds);
				}
			}

			checkOnDustDevils();

			checkOnLocalStorms();

			checkOnRegionalStorms();

			checkOnPlanetEncirclingStorms();

	    	coordinateList.forEach(location -> {
	    		// compute the average pressure
	    		//todayWeather.forEach( d -> {
	    		//	sum = d.getPressure();
	    		//});
	    		//double ave = sum / todayWeather.size();
	    		//dailyVariationAirPressure = Math.abs(dailyVariationAirPressure - ave);
	    		// save the todayWeather into dailyRecordMap
	    		dailyRecordMap.put(solCache, todayWeather);
	    		// save the dailyRecordMap into weatherDataMap
	       		weatherDataMap.put(location, dailyRecordMap);
	    	});
    		// create a brand new list
    		todayWeather = new CopyOnWriteArrayList<>();
			solCache = newSol;
			//computeDailyVariationAirPressure();
		}

	}

	public void checkOnDustDevils() {

		for (Object value : dustDevilMap.values()) {
			DustStorm d = (DustStorm) value;
			if (d.getNewSize() == 0) {
				//Coordinates location = d.getCoordinates();

				// remove this dust devil
				dustDevilMap.remove(value);
			}
		}
	}

	public void checkOnLocalStorms() {

		for (Object value : localDustStormMap.values()) {
			DustStorm d = (DustStorm) value;
			if (d.getNewSize() > 2000) {
				Coordinates location = d.getCoordinates();
				// if the size of this local storm grows beyond 2000km, upgrade it to regional storm
				DustStorm dd = new DustStorm("Storm " + regionalDustStormMap.size() + 1, DustStormType.REGIONAL, marsClock);
				dd.setWeather(this);
				dd.setCoordinates(location);
				// upgrade this regional storm to regional storm
				regionalDustStormMap.put(regionalDustStormMap.size(), dd);
				// remove this oversize local storm
				localDustStormMap.remove(value);
			}
			else if (d.getNewSize() < 15) {
				Coordinates location = d.getCoordinates();
				// if the size of a regional shrink below 2000km, downgrade it to planet encircling storm
				DustStorm dd = new DustStorm("Dust Devil " + dustDevilMap.size() + 1, DustStormType.DUST_DEVIL, marsClock);
				dd.setWeather(this);
				dd.setCoordinates(location);
				// downgrade this regional storm to local
				dustDevilMap.put(dustDevilMap.size(), dd);
				// remove this undersize local storm
				localDustStormMap.remove(value);
			}
		}
	}


	public void checkOnRegionalStorms() {

		//regionalDustStormMap.entrySet().removeIf(e-> ... );

		for (Object value : regionalDustStormMap.values()) {
			DustStorm d = (DustStorm) value;
			if (d.getNewSize() > 4000) {
				Coordinates location = d.getCoordinates();
				// if the size of a regional grows beyond 2000km, upgrade it to planet encircling storm
				DustStorm dd = new DustStorm("Planet Encircling Storm " + planetEncirclingDustStormMap.size() + 1, DustStormType.PLANET_ENCIRCLING, marsClock);
				dd.setWeather(this);
				dd.setCoordinates(location);
				// upgrade this regional storm to planet-encircling
				planetEncirclingDustStormMap.put(planetEncirclingDustStormMap.size(), dd);
				// remove this oversize regional storm
				regionalDustStormMap.remove(value);
			}
			else if (d.getNewSize() < 2000) {
				Coordinates location = d.getCoordinates();
				// if the size of a regional shrink below 2000km, downgrade it to planet encircling storm
				DustStorm dd = new DustStorm("Local Storm " + localDustStormMap.size() + 1, DustStormType.LOCAL, marsClock);
				dd.setWeather(this);
				dd.setCoordinates(location);
				// downgrade this regional storm to local
				planetEncirclingDustStormMap.put(localDustStormMap.size(), dd);
				// remove this undersize regional storm
				regionalDustStormMap.remove(value);
			}
		}
	}


	public void checkOnPlanetEncirclingStorms() {

		for (Object value : planetEncirclingDustStormMap.values()) {
			DustStorm d = (DustStorm) value;
			if (d.getNewSize() < 4000) {
				Coordinates location = d.getCoordinates();
				// if the size of this storm drops below 2000km, downgrade it to become regional storm
				DustStorm dd = new DustStorm("Storm " + regionalDustStormMap.size() + 1, DustStormType.REGIONAL, marsClock);
				dd.setWeather(this);
				dd.setCoordinates(location);
				// downgrade this regional storm to regional
				regionalDustStormMap.put(regionalDustStormMap.size(), dd);
				// remove this undersize storm
				planetEncirclingDustStormMap.remove(value);
			}
		}
	}

	public Map<Integer, DustStorm> getPlanetEncirclingDustStormMap() {
		return planetEncirclingDustStormMap;
	}

	public Map<Integer, DustStorm> getLocalDustStormMap() {
		return localDustStormMap;
	}

	public Map<Integer, DustStorm> getRegionalDustStormMap() {
		return regionalDustStormMap;
	}

	//public int getNumRegionalDustStorm() {
	//	return numRegionalDustStorm;
	//}

	//public int getNumPlanetEncirclingDustStorm() {
	//	return numPlanetEncirclingDustStorm;
	//}

	public double getDailyVariationAirPressure(Coordinates location) {
		return dailyVariationAirPressure;
	}

/*
	public void computeDailyVariationAirPressure() {
    	coordinateList.forEach(location -> {

    		List<DailyWeather> list = dailyRecordMap.get(solCache-1);
			int i = 0;
    		list.forEach( d -> {

    			if (solCache > 1)
    				if (d.getSol() == solCache - 1)
    					sum += d.getPressure();

    		});
    		double ave = sum ;
	    	DailyWeather d = list.get(list.size()-1);
	    	d.getPressure();

    		//
    		//t = ;
    		//p;
    		//d;
    		//s;
    		//d.setDailyAverage(double t, double p, double d, double s);
    	});
	}
*/

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