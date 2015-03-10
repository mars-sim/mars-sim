/**
 * Mars Simulation Project
 * Weather.java
 * @version 3.07 2015-01-19
 * @author Scott Davis
 * @author Hartmut Prochaska
 */
package org.mars_sim.msp.core.mars;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.time.MarsClock;

/** Weather represents the weather on Mars */
public class Weather
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Static data
	/** Sea level air pressure in kPa. */
	//2014-11-22 Set the unit of air pressure to kPa
	private static final double SEA_LEVEL_AIR_PRESSURE = .8D;
	/** Sea level air density in kg/m^3. */
	private static final double SEA_LEVEL_AIR_DENSITY = .0115D;
	/** Mars' gravitational acceleration at sea level in m/sec^2. */
	private static final double SEA_LEVEL_GRAVITY = 3.0D;
	/** extreme cold temperatures at Mars. */
	private static final double EXTREME_COLD = -120D;
	
	private static final double VIKING_LONGITUDE_OFFSET_IN_MILLISOLS = 138.80D; 
	// Calculation : 49.97W/180 deg * 500 millisols;
	private static final double VIKING_LATITUDE = 22.48D; 
	
	private static final int TEMPERATURE = 0;
	private static final int AIR_PRESSURE = 1;
	
	//private double MILLISOLS_ON_FIRST_SOL = MarsClock.THE_FIRST_SOL;
	
	//2015-02-19 Added MILLISOLS_PER_UPDATE 
	private static final int MILLISOLS_PER_UPDATE = 5 ; // one update per x millisols  

	private double viking_dt;
	
	private double final_temperature = EXTREME_COLD;
	
	private double TEMPERATURE_DELTA_PER_DEG_LAT = 0D;
	
	private int millisols;
	
	//private int count = 0;
	
	private MarsClock marsClock;
	private SurfaceFeatures surfaceFeatures;
	
	private static Map<Coordinates, Double> temperatureCacheMap = new HashMap<Coordinates, Double>();
	private static Map<Coordinates, Double> airPressureCacheMap = new HashMap<Coordinates, Double>();
	

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
	 * Gets the air pressure at a given location.
	 * @return air pressure in Pa.
	 */
	// 2015-03-06 Added getAirPressure()
	public double getAirPressure(Coordinates location) {

		marsClock = Simulation.instance().getMasterClock().getMarsClock();		
	    millisols =  (int) marsClock.getMillisols() ;
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
		Mars mars = Simulation.instance().getMars();
		TerrainElevation terrainElevation = mars.getSurfaceFeatures().getSurfaceTerrain();
		double elevation = terrainElevation.getElevation(location);

		// p = pressure0 * e(-((density0 * gravitation) / pressure0) * h)
		// P = 0.009 * e(-(0.0155 * 3.0 / 0.009) * elevation)
		double pressure = SEA_LEVEL_AIR_PRESSURE * Math.exp(-1D *
				SEA_LEVEL_AIR_DENSITY * SEA_LEVEL_GRAVITY / (SEA_LEVEL_AIR_PRESSURE * 1000)
				* elevation);

		return pressure;
	}

	/**
	 * Gets the surface temperature at a given location.
	 * @return temperature in Celsius.
	 */
	public double getTemperature(Coordinates location) {
		
		marsClock = Simulation.instance().getMasterClock().getMarsClock();	
	    millisols =  (int) marsClock.getMillisols() ;
		
		if (millisols % MILLISOLS_PER_UPDATE == 0) {	
			double temperatureCache = updateTemperature(location);
			temperatureCacheMap.put(location,temperatureCache);
			//System.out.println("Weather.java: temperatureCache is " + temperatureCache);
			return temperatureCache;
		}
		
		else return getCacheValue(temperatureCacheMap, location, TEMPERATURE);
			
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
		
		surfaceFeatures = Simulation.instance().getMars().getSurfaceFeatures();

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
			
			// 1. Time of day and longitude
			double theta = location.getTheta(); // theta is the longitude in radian
			theta = theta /Math.PI * 500D; // in millisols;
			//System.out.println(" theta: " + theta);			
	        double time  = marsClock.getMillisol();
			//double time = (double) oneTenthmillisols * 10D;
	        double x_offset = time - 360 - VIKING_LONGITUDE_OFFSET_IN_MILLISOLS + theta;     
	        double equatorial_temperature = 27.5 * Math.sin  ( 2*Math.PI/1000D * x_offset ) -58.5 ;  			
			equatorial_temperature = Math.round (equatorial_temperature * 100.0)/100.0; 
			//System.out.print("Time: " + Math.round (time) + "  T: " + standard_temperature);

		/*
			// + getSurfaceSunlight * (80D / 127D (max sun))
			// if sun full we will get -40D the avg, if night or twilight we will get 
			// a smooth temperature change and in the night -120D
		    temperature = temperature + surfaceFeatures.getSurfaceSunlight(location) * 80D;
		*/
			
			// 2. Terrain Elevation 
			// Not correct but guess: - (elevation * 5)
			TerrainElevation terrainElevation = surfaceFeatures.getSurfaceTerrain();
			double terrain_dt =  Math.abs(terrainElevation.getElevation(location) * 5D);
			terrain_dt = Math.round (terrain_dt * 100.0)/ 100.0;
			//System.out.print("  terrain_dt: " + terrain_dt );			
						
			// 3. Latitude 
			double piHalf = Math.PI / 2.0;
			double lat_degree = 0; 
			//int hemisphere = 0;
			double phi = location.getPhi();	
			if (phi < piHalf) {
			    lat_degree = ((piHalf - phi) / piHalf) * 90;
			    //hemisphere = 1;
			} else if (phi > piHalf){
				lat_degree = ((phi - piHalf) / piHalf) * 90; 
				//hemisphere = 2;
			}			
			//System.out.print("  degree: " + Math.round (degree * 10.0)/10.0 ); 
			double lat_dt = -15D - 15D * Math.sin( 2D * lat_degree * Math.PI/180D + Math.PI/2D) ;
			lat_dt = Math.round (lat_dt * 100.0)/ 100.0;
			//System.out.println("  lat_dt: " + lat_dt );

			// 4. Seasonal variation 
			double lat_adjustment = TEMPERATURE_DELTA_PER_DEG_LAT * lat_degree; // an educated guess			
	        int solElapsed = MarsClock.getSolOfYear(marsClock);
			double seasonal_dt = lat_adjustment * Math.sin( 2 * Math.PI/1000D * ( solElapsed - 142));
			seasonal_dt = Math.round (seasonal_dt * 100.0)/ 100.0;
			//System.out.println("  seasonal_dt: " + seasonal_dt ); 
			 
			
			// 5. Add randomness
			double up = RandomUtil.getRandomDouble(3);
			double down = RandomUtil.getRandomDouble(3);
			
			final_temperature = equatorial_temperature + viking_dt - lat_dt - terrain_dt + seasonal_dt + up - down;
			final_temperature = Math.round (final_temperature * 100.0)/100.0;
			//System.out.println("  final T: " + final_temperature );
		}

		return final_temperature;
	}

	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		// Do nothing
	}
}