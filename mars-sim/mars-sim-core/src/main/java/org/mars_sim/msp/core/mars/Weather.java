/**
 * Mars Simulation Project
 * Weather.java
 * @version 3.07 2015-01-19
 * @author Scott Davis
 * @author Hartmut Prochaska
 */
package org.mars_sim.msp.core.mars;

import java.io.Serializable;

import org.mars_sim.msp.core.Coordinates;
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
	
	private double final_temperature = EXTREME_COLD;
	
	private MarsClock marsClock;
	private SurfaceFeatures surfaceFeatures;
	
	/** Constructs a Weather object */
	public Weather() {

	}

	/**
	 * Gets the air pressure at a given location.
	 * @return air pressure in Pa.
	 */
	public double getAirPressure(Coordinates location) {

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
		
		double final_temperature = 0;
		marsClock = Simulation.instance().getMasterClock().getMarsClock();
		surfaceFeatures = Simulation.instance().getMars().getSurfaceFeatures();

		if (surfaceFeatures.inDarkPolarRegion(location)){
			//known temperature for cold days at the pole
			final_temperature = -150D;
			
		} else {
			
			double theta = location.getTheta(); // theta is the longitude in radian

			theta = theta /Math.PI * 500D; // in millisols;
			//System.out.println(" theta: " + theta);
			
	        double time  = marsClock.getMillisol();
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
			
			// not correct but guess: - (elevation * 5)
			TerrainElevation terrainElevation = surfaceFeatures.getSurfaceTerrain();
			double terrain_dt =  Math.abs(terrainElevation.getElevation(location) * 5D);
			terrain_dt = Math.round (terrain_dt * 100.0)/ 100.0;
			//System.out.print("  terrain_dt: " + terrain_dt );
			
			
			double viking_dt = 28D - 15D * Math.sin(2 * Math.PI/180D * VIKING_LATITUDE + Math.PI/2D) - 13D;			
			viking_dt = Math.round (viking_dt * 100.0)/ 100.00;
			//System.out.print("  viking_dt: " + viking_dt );
			
			// - ((math.pi/2) / (phi of location)) * 20
			// guess, but could work, later we can implement real physics

			double piHalf = Math.PI / 2.0;
			double lat_degree = 0; 
			double phi = location.getPhi();	

			
			if (phi < piHalf) {
			    lat_degree = ((piHalf - phi) / piHalf) * 90;
			} else if (phi > piHalf){
				lat_degree = ((phi - piHalf) / piHalf) * 90; 
			}
			
			//System.out.print("  degree: " + Math.round (degree * 10.0)/10.0 ); 
			double lat_dt = 15D * Math.sin( 2D * lat_degree * Math.PI/180D + Math.PI/2D) + 13D;
			lat_dt = 28D - lat_dt;
			lat_dt = Math.round (lat_dt * 100.0)/ 100.0;
			
			// 
			final_temperature = equatorial_temperature + viking_dt - lat_dt - terrain_dt;
			final_temperature = Math.round (final_temperature * 100.0)/100.0;
			//System.out.println("  settlement_dt: " + settlement_dt + "  final T: " + final_temperature );
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