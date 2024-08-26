/*
 * Mars Simulation Project
 * MeteoriteImpactProperty.java
 * @date 2023-12-25
 * @author Manny Kung
 */

package com.mars_sim.core.environment;

import java.io.Serializable;

import com.mars_sim.core.time.MarsTime;
import com.mars_sim.tools.util.RandomUtil;

/**
 *   Assumptions:
 *	 a. Use the research basis that meteorites have a spherical sphere with 8 um radius
 *	 b. velocity of impact < 1 km/s -- Atmospheric entry simulations indicate that
 *	 particles from 10 to 1000 mm in diameter are slowed
 *	 below 1 km/s before impacting the surface of the planet (Flynn and McKay,
 *	 1990).
 *
 *   References:
 *	 1. Inflatable Transparent Structures for Mars Greenhouse Applications
 *	 2005-01-2846. SAE International.
 *	 data.spaceappschallenge.org/ICES.pdf
 *
 *	 2. 1963 NASA Technical Note D-1463 Meteoroid Hazard
 *	 http://ntrs.nasa.gov/archive/nasa/casi.ntrs.nasa.gov/19630002110.pdf
 *
 *	 3. Meteorite Accumulations on Mars. P.A.Bland and T.B.Smith. Revised Sep 10 1999
 *	 https://www.researchgate.net/publication/222568152_Meteorite_Accumulations_on_Mars
 * 
 * @author mkhelios
 */
public class MeteoriteImpactProperty implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	// Set up the initial params for each settlement
	
	// A. Critical Diameter is 0.0016 cm for meteorites having a spherical
	// sphere with 8 um radius
	private final double CRITICAL_DIAMETER = .0016 * RandomUtil.getRandomDouble(.95, 1.05);
	
	private double cDiaCache = CRITICAL_DIAMETER;
			
	// B. Density Range from 0.7 to 2.2 gram/cm^3
	private final double AVERAGE_DENSITY = RandomUtil.getRandomDouble(.7, 2.2);
	
	private double aRhoCache = AVERAGE_DENSITY;
	
	// C. Velocity of impact < 1 km/s
	// Note: atmospheric entry simulations indicate particles from 10 to 1000 mm in
	// diameter are slowed to usually below 1 km/s before impacting the surface of the planet
	// (Flynn and McKay, 1990)
	private final double IMPACT_VELOCITY = RandomUtil.getRandomDouble(.25, 1); 

	private double impVelCache = IMPACT_VELOCITY;

	private double totalMassPerSqkm;

	private double wallPenetrationThickness;

	private double probabilityOfImpactPerSQMPerSol;
	
	/**
	 * Calculates the meteorite impact probability for the whole settlement once a
	 * sol
	 */
	public void calculateMeteoriteProbability() {
		
		// Compute the incoming meteorite unique profile with an arbitrary degree of randomness 
		// for each settlement locale.
		
		// FUTURE: May vary per orbit oer per season

		// Part I
		// Assuming size and penetration speed of the meteorites are homogeneous,
		// Find the probability of impact per square meter per sol on the settlement

		// a. Update average critical diameter 
		double cDia = 0.5 * cDiaCache + 0.5 * CRITICAL_DIAMETER * RandomUtil.getRandomDouble(.95, 1.05);

		cDiaCache = cDia;
		
		// b. Update average density
		double aRho = 0.5 * aRhoCache + 0.5 * AVERAGE_DENSITY * RandomUtil.getRandomDouble(.95, 1.05);
		
		aRhoCache = aRho;
		
		// c. Update velocity of impact 
		double impVel = 0.5 * impVelCache + 0.5 * IMPACT_VELOCITY * RandomUtil.getRandomDouble(.95, 1.05); 	
		
		impVelCache = impVel;
		
		// d. spherical volume 4/3 * pi * (r/2)^3
		// 1.33333 * Math.PI *.5*.5*.5 = .5236
		double sphericalVolume = 0.5236 * cDia * cDia * cDia; // .125 = *.5*.5*.5;

		// e. mass of a meteorite
		double massPerMeteorite = aRho * sphericalVolume;
		
		// f. logN
		// The influx of meteorites entering Mars atmosphere can be estimated as
		// log10 N = -0.689* log(m) + 4.17
		// m grams incident on an area 10^6 sq km
		// with N being the number of meteorites per year having masses greater than m grams
		// incident on an area of 10^6 km2 (Bland and Smith, 2000).
		double logN = -0.689 * Math.log10(massPerMeteorite) + 4.17;

		// Note: The Mars’s total surface area = 144.8 million km²
		

		// The epsilon. per 10^6 sq km, need to convert to per sq meter by dividing 10^12
		double numMeteoritesPerYearPerMeter = Math.pow(10, logN - 12D); 
		
		// g. # of meteorites per year per meter
		totalMassPerSqkm = .8 * totalMassPerSqkm + .2 * massPerMeteorite * numMeteoritesPerYearPerMeter * 1_000_000;
		
		// h. probability of impact per square meter per year
		double probabilityOfImpactPerSQMPerYear = Math.exp(-numMeteoritesPerYearPerMeter);

		// i. probability of impact per square meter per sol
		probabilityOfImpactPerSQMPerSol = .8 * probabilityOfImpactPerSQMPerSol + .2 * probabilityOfImpactPerSQMPerYear / MarsTime.SOLS_PER_ORBIT_NON_LEAPYEAR;

		// Part II
		// Assuming size and impact speed of the meteorites are homogeneous,
		// determine how far the meteorites may penetrate the wall
		double penetrationThicknessOnAL = 1.09 * Math.pow(massPerMeteorite * impVel, 1 / 3D);

		// a. gets the wall penetration thickness on average
		wallPenetrationThickness = .8 * wallPenetrationThickness + .2 * 1.5 * penetrationThicknessOnAL;
		
		// FUTURE: does it account for all angles of penetration on average ?
	}

	/**
	 * Gets how much debris is created per sq km when a meteorite hits.
	 * 
	 * @return
	 */
	public double getDebrisMass() {
		return totalMassPerSqkm;
	}

	/**
	 * Gets how deep does meteorite penetration through a building wall.
	 * 
	 * @return
	 */
	public double getWallPenetration() {
		return wallPenetrationThickness;
	}

	/**
	 * Gets the probability of impact per square meter per sol. 
	 * Called by each building once a sol to see if an impact is imminent.
	 * 
	 * @return
	 */
	public double getProbabilityOfImpactPerSQMPerSol() {
		return probabilityOfImpactPerSQMPerSol;
	}
}
