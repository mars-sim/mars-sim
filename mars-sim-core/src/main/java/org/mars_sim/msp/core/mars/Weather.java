/**
 * Mars Simulation Project
 * Weather.java
 * @version 3.07 2015-04-08
 * @author Scott Davis
 * @author Hartmut Prochaska
 */
package org.mars_sim.msp.core.mars;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	public static final double PARTIAL_PRESSURE_CARBON_DIOXIDE_MARS = 0.57D; 	// in kPa
	public static final double PARTIAL_PRESSURE_CARBON_DIOXIDE_EARTH = 0.035D; 	// in kPa
	public static final double PARTIAL_PRESSURE_WATER_VAPOR_ROOM_CONDITION = 1.6D; 	// in kPa. under Earth's atmosphere, at 25 C, 50% relative humidity


	private static final double VIKING_LATITUDE = 22.48D;

	private static final int TEMPERATURE = 0;
	private static final int AIR_PRESSURE = 0;

	//private double MILLISOLS_ON_FIRST_SOL = MarsClock.THE_FIRST_SOL;

	//2015-02-19 Added MILLISOLS_PER_UPDATE
	private static final int MILLISOLS_PER_UPDATE = 5 ; // one update per x millisols

	private int millisols;

	/** Current sol since the start of sim. */
	private int solCache = 1;

	private double sum;

	private double viking_dt;

	private double final_temperature = EXTREME_COLD;

	private double TEMPERATURE_DELTA_PER_DEG_LAT = 0D;

	private double dailyVariationAirPressure =  RandomUtil.getRandomDouble(.05); // tentatively only
	//TODO: compute the true dailyVariationAirPressure by the end of the day

	private Map <Coordinates, Map<Integer, List<DailyWeather>>> weatherDataMap = new HashMap<>();
	private Map <Integer, List<DailyWeather>> dailyRecordMap = new HashMap<>();

	private List<DailyWeather> todayWeather = new ArrayList<>();
	private List<Coordinates> coordinateList = new ArrayList<>();

	private static Map<Coordinates, Double> temperatureCacheMap = new HashMap<Coordinates, Double>();
	private static Map<Coordinates, Double> airPressureCacheMap = new HashMap<Coordinates, Double>();

	private MarsClock marsClock;
	private SurfaceFeatures surfaceFeatures;
	private TerrainElevation terrainElevation;
	private MasterClock masterClock;


	/** Constructs a Weather object */
	public Weather() {

		//count++;
		//System.out.print(" calling Weather.java " + count + " times");

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
	 * @return air density in kg/m3.
	 */
	// 2015-03-17 Added computeAirDensity()
	public double computeAirDensity(Coordinates location) {
		double result = 0;
		checkLocation(location);
		//The air density is derived from the equation of state : d = p / .1921 / (t + 273.1)
		result = getAirPressure(location) / (.1921 * (getTemperature(location) + 273.1));
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
	 * Computes the air pressure at a given location.
	 * @return air pressure in Pa.
	 */
	// 2015-04-08 Added getAirPressure()
	public double getAirPressure(Coordinates location) {
		return computeAirPressure(location);
	}

	/**
	 * Gets the air pressure at a given location.
	 * @return air pressure in Pa.
	 */
	// 2015-03-06 Added computeAirPressure()
	public double computeAirPressure(Coordinates location) {
		checkLocation(location);
		//masterClock = Simulation.instance().getMasterClock();
		if (marsClock == null)
			marsClock = Simulation.instance().getMasterClock().getMarsClock();
	    millisols =  (int) marsClock.getMillisol() ;
		//System.out.println("oneTenthmillisols : " + oneTenthmillisols);

		if (millisols % MILLISOLS_PER_UPDATE == 1) {
			double cache = updateAirPressure(location);
			airPressureCacheMap.put(location, cache);
			//System.out.println("air pressure : "+cache);
			return cache;
		}

		else return getCacheValue(airPressureCacheMap, location, AIR_PRESSURE);

	}

	/**
	 * Updates the air pressure at a given location.
	 * @return air pressure in Pa.
	 */
	public double updateAirPressure(Coordinates location) {
		// Get local elevation in meters.
		if (terrainElevation == null)
			terrainElevation = Simulation.instance().getMars().getSurfaceFeatures().getSurfaceTerrain();

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
	 * Gets the surface temperature at a given location.
	 * @return temperature in Celsius.
	 */
	public double getTemperature(Coordinates location) {
		return computeTemperature(location);
	}

	/**
	 * Computes the surface temperature at a given location.
	 * @return temperature in Celsius.
	 */
	public double computeTemperature(Coordinates location) {
		checkLocation(location);

		if (marsClock == null)
			marsClock = Simulation.instance().getMasterClock().getMarsClock();

	    millisols =  (int) marsClock.getMillisol() ;

		if (millisols % MILLISOLS_PER_UPDATE == 0) {
			double temperatureCache = updateTemperature(location);
			temperatureCacheMap.put(location,temperatureCache);
			//System.out.println("Weather.java: temperatureCache is " + temperatureCache);
			return temperatureCache;
		}

		else return getCacheValue(temperatureCacheMap, location, TEMPERATURE);

	}

	/*
    public double getTemperatureCache(Coordinates location) {
    	double result;

       	if (temperatureCacheMap.containsKey(location)) {
       		result = temperatureCacheMap.get(location);
    	}
    	else {
    		double temperatureCache = updateTemperature(location);
			temperatureCacheMap.put(location,temperatureCache);
    		result = temperatureCache;
    	}

    	return result;
    }
    */


	/**
	 * Computes the surface temperature at a given location.
	 * @return temperature in Celsius.
	 */
	public double updateTemperature(Coordinates location) {

		if (surfaceFeatures == null)
			surfaceFeatures = Simulation.instance().getMars().getSurfaceFeatures();

		if (terrainElevation == null)
			terrainElevation = surfaceFeatures.getSurfaceTerrain();

		if (surfaceFeatures.inDarkPolarRegion(location)){
			//known temperature for cold days at the pole
			final_temperature = -150D;

		} else {
			// 2015-01-28 We arrived at this temperature model based on Viking 1 & Opportunity Rover
			// by assuming the temperature is the linear combination of the following factors:
			// 1. Time of day and longitude,
			// 2. Terrain elevation,
			// 3. Latitude,
			// 4. Seasonal variation (dependent upon latitude)
			// 5. Randomness

			// (1). Time of day and longitude
			double theta = location.getTheta() / Math.PI * 500D; // convert theta in longitude in radian to millisols;
			//System.out.println(" theta: " + theta);
	        double time  = marsClock.getMillisol();
	        double x_offset = time + theta - VIKING_LONGITUDE_OFFSET_IN_MILLISOLS ;
	        double equatorial_temperature = 27.5D * Math.sin  ( Math.PI * x_offset / 500D) - 58.5D ;
			equatorial_temperature = Math.round (equatorial_temperature * 100.0)/100.0;
			//System.out.print("Time: " + Math.round (time) + "  T: " + standard_temperature);

		/*
			// + getSurfaceSunlight * (80D / 127D (max sun))
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

			// assume a typical temperature of -31 deg celsiu
			if (elevation < 7)
				terrain_dt = - 0.000998 * elevation * 1000;
			else // delta = -31 + 23.4 = 7.6
				terrain_dt = 7.6 - 0.00222 * elevation * 1000;

			terrain_dt = Math.round (terrain_dt * 100.0)/ 100.0;
			//System.out.print("  terrain_dt: " + terrain_dt );


			// (3). Latitude
			double lat_degree = location.getPhi2Lat(location.getPhi());

			//System.out.print("  degree: " + Math.round (degree * 10.0)/10.0 );
			double lat_dt = -15D - 15D * Math.sin( 2D * lat_degree * Math.PI/180D + Math.PI/2D) ;
			lat_dt = Math.round (lat_dt * 100.0)/ 100.0;
			//System.out.println("  lat_dt: " + lat_dt );

			// (4). Seasonal variation
			double lat_adjustment = TEMPERATURE_DELTA_PER_DEG_LAT * lat_degree; // an educated guess
	        int solElapsed = MarsClock.getSolOfYear(marsClock);
			double seasonal_dt = lat_adjustment * Math.sin( 2 * Math.PI/1000D * ( solElapsed - 142));
			seasonal_dt = Math.round (seasonal_dt * 100.0)/ 100.0;
			//System.out.println("  seasonal_dt: " + seasonal_dt );


			// (5). Add randomness
			double up = RandomUtil.getRandomDouble(2);
			double down = RandomUtil.getRandomDouble(2);

			final_temperature = equatorial_temperature + viking_dt - lat_dt - terrain_dt + seasonal_dt + up - down;
			final_temperature = Math.round (final_temperature * 100.0)/100.0;
			//System.out.println("  final T: " + final_temperature );
		}

		return final_temperature;
	}

	/**
	 * Clears weather-related parameter cache map to prevent excessive build-up of key-value sets
	 */
	// 2015-03-06 Added clearMap()
    public synchronized void clearMap() {
    	temperatureCacheMap.clear();
    	airPressureCacheMap.clear();
    }

	/**
	 * Provides the surface temperature /air pressure at a given location from the temperatureCacheMap.
	 * If calling the given location for the first time from the cache map, call update temperature/air pressure instead
	 * @return temperature or pressure
	 */
	// 2015-03-06 Added getCacheValue()
    public double getCacheValue(Map<Coordinates, Double> map, Coordinates location, int value) {
    	double result;

       	if (map.containsKey(location)) {
       		result = map.get(location);
    	}
    	else {
    		double cache = 0;
    		if (value == TEMPERATURE )
    			cache = updateTemperature(location);
    		else if (value == AIR_PRESSURE )
    			cache = updateAirPressure(location);

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

		//computeTemperature();
		//computeAirPressure();
		//computeAirDensity();
		//comuteSolarIrradiance();

		if (marsClock == null)
			marsClock = Simulation.instance().getMasterClock().getMarsClock();

	    // collect a number of sample data point throughout the date and also get an average
	    double currentMillisols =  (int) marsClock.getMillisol();
	    if (currentMillisols % 100 == 0) {
	    	//List<DailyWeather> todayWeather = new ArrayList<>();
	    	//Iterator<Coordinates> list = coordinateList.iterator();
	    	coordinateList.forEach(location -> {
	    		DailyWeather weather = new DailyWeather(marsClock, getTemperature(location), getAirPressure(location), getAirDensity(location), surfaceFeatures.getSolarIrradiance(location));
		    	todayWeather.add(weather);
	    	});
	    }
	    // check for the passing of each day
	    int newSol = MarsClock.getSolOfYear(marsClock);
		if (newSol != solCache) {
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
    		todayWeather = new ArrayList<>();
			solCache = newSol;
			//computeDailyVariationAirPressure();
		}

	}


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
		// Do nothing
	}
}