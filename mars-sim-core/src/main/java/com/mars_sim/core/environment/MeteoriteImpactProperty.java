/*
 * Mars Simulation Project
 * MeteoriteImpactProperty.java
 * @date 2025-08-08
 * @author Manny Kung
 */

package com.mars_sim.core.environment;

import java.io.Serializable;

import org.apache.commons.math3.util.FastMath;

import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.tool.RandomUtil;

/**
 *  The scientific consensus is that Mars has a significantly higher probability of 
 *  meteorite impacts than on the Earth or the Moon. This is a result of the proximity 
 *  of Mars to the asteroid belt, the thin Martian atmosphere and the lack of a Martian
 *  magnetic field (Boston, 2009; Bland and Smith, 2000; Schroeder et al., 2008). 
 *  
 *  It has been calculated that no meteorites smaller than approximately one kilogram 
 *  in mass are capable of reaching the Martian surface because of protection provided 
 *  by the atmosphere (Carrermole, 2001). 
 *  
 *  This parameter and the risk of meteorites vary with altitude. The precise flux of 
 *  meteorites and how to calculate these values is still a subject of active debate even 
 *  on Earth (Zolensky et al., 1990). 
 *  
 *  Secondary fragmentation risks would further depend on the impacted terrain. It is 
 *  anticipated that lava tubes buried by tens of meters of basalt would provide excellent
 *  protection from most small impacts and secondary fragmentation (Clifford, 1997). 
 * 
 * 
 *   Assumptions:
 *   
 *	 a. Use the research basis that meteorites have a spherical sphere with 8 um radius
 *
 *	 b. Velocity of impact < 1 km/s : Atmospheric entry simulations indicate that
 *	 particles from 10 to 1000 mm in diameter are slowed below 1 km/s before impacting 
 *   the surface of the planet (Flynn and McKay, 1990).
 *
 *   References:
 *   
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
 *   4. ACCESS Mars Final Report Revised 3. Alexandre Sole Carretero
 *   https://www.academia.edu/4608351/ACCESS_Mars_Final_Report_Revised_3
 *   
 */
public class MeteoriteImpactProperty implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	private static final double HALF_PI = FastMath.PI/2;

	// Set up the initial params for each settlement
	
	// A. Critical Diameter is 0.0016 cm for meteorites having a spherical
	// sphere with 8 um radius
	private final double CRITICAL_DIAMETER = .0016 * RandomUtil.getRandomDouble(.95, 1.05);
	
	// B. Density Range from 0.7 to 2.2 gram/cm^3
	private final double AVERAGE_DENSITY = RandomUtil.getRandomDouble(.7, 2.2);
	
	// C. Velocity of impact < 1 km/s
	// Note: atmospheric entry simulations indicate particles from 10 to 1000 mm in
	//       diameter are slowed to usually below 1 km/s before impacting the surface of the planet
	//       (Flynn and McKay, 1990)
	
	private final double IMPACT_VELOCITY = RandomUtil.getRandomDouble(.25, 1.25); 

	private double cDiaCache = CRITICAL_DIAMETER;
	
	private double aRhoCache = AVERAGE_DENSITY;
	
	private double impVelCache = IMPACT_VELOCITY;

	private double angleDegree = 0;
	
	private double totalMassPerSqkm;

	private double wallPenetrationThickness;

	private double probabilityOfImpactPerSQMPerSol;
	
	
	public MeteoriteImpactProperty(Settlement settlement) {
		// Note: The most typical impact angle for meteorites on Mars is not a single fixed value but 
		// varies significantly with location. While a 45-degree angle is often cited as a general
		// average for Earth due to an isotropic flux of meteors in space, this does not hold 
		// uniformly across Mars.
		
		final double phi = settlement.getCoordinates().getPhi();

		if (phi <= HALF_PI) {
			angleDegree = 30 + 35 * (HALF_PI - phi)/HALF_PI;
		}
		else {
			angleDegree = 30 + 35 * (phi - HALF_PI)/HALF_PI;
		}
		
//		logger.info(this, "Meteorite impact incident angle: " + Math.round(angleDegree * 10.0)/10.0);
		
		// Impacts near the equator on Mars typically have shallower angles, with a mode near 30
		// degrees above the horizontal, while impacts near the poles are steeper, with a mode 
		// close to 65 degrees.
		
		// This variation is due to the geometry of the planet and the direction from which the 
		// impacting objects approach. Furthermore, low-angle impacts, defined as those less than 
		// 15 degrees above the local horizon, are known to produce distinctive oval-shaped craters 
		// with butterfly-shaped ejecta patterns, which make up roughly 5 percent of all craters on Mars.
	}
	
	/**
	 * Calculates the meteorite impact probability for the whole settlement once a
	 * sol
	 */
	public void calculateMeteoriteProbability() {
		
		// Compute the incoming meteorite unique profile with an arbitrary degree of randomness 
		// for each settlement locale.
		
		// FUTURE: May vary per orbit oer per season

		// Part I : Update the params
		
		// Assuming size and penetration speed of the meteorites are homogeneous,
		// Find the probability of impact per square meter per sol on the settlement
		// Based on its past history of these 3 params in this region
		
		// a. Update average critical diameter 
		double cDia = 0.95 * cDiaCache + 0.05 * CRITICAL_DIAMETER * RandomUtil.getGaussian(1, .5);

		cDiaCache = cDia;
		
		// b. Update average density
		double aRho = 0.85 * aRhoCache + 0.15 * AVERAGE_DENSITY * RandomUtil.getGaussian(1, .5);
		
		aRhoCache = aRho;
		
		// c. Update velocity of impact 
		double impVel = 0.75 * impVelCache + 0.25 * IMPACT_VELOCITY * RandomUtil.getGaussian(1, .5);	
		
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

		// g. The epsilon. per 10^6 sq km, need to convert to per sq meter by dividing 10^12
		double numMeteoritesPerYearPerMeter = Math.pow(10, logN - 12D); 
		
		// h. # of meteorites per year per meter
		// Note: 
		totalMassPerSqkm = .9 * totalMassPerSqkm + .1 * massPerMeteorite * numMeteoritesPerYearPerMeter * 1_000_000;
		
		// i. probability of impact per square meter per year
		double probabilityOfImpactPerSQMPerYear = Math.exp(-numMeteoritesPerYearPerMeter);

		// j. probability of impact per square meter per sol
		probabilityOfImpactPerSQMPerSol = .8 * probabilityOfImpactPerSQMPerSol + .2 * probabilityOfImpactPerSQMPerYear / MarsTime.SOLS_PER_ORBIT_NON_LEAPYEAR;

		// Part II
		// Assuming size and impact speed of the meteorites are homogeneous,
		// determine how far the meteorites may penetrate the wall
		double penetrationThicknessOnAL = 1.09 * Math.pow(massPerMeteorite * impVel, 1 / 3D);

		// a. the wall penetration thickness on average
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
	
	/**
	 * Gets the standard incident angle based on latitude (or phi) of a building.
	 * 
	 * @return
	 */
	public double getStandardIncidentAngle() {
		return angleDegree;
	}
}
