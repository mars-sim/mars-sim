/**
 * Mars Simulation Project
 * MeteoriteImpactImpl.java
 * @version 3.1.0 2017-10-03
 * @author Manny Kung
 */

package org.mars_sim.msp.core.mars;

import java.io.Serializable;

import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.tool.RandomUtil;

public class MeteoriteImpactImpl implements MeteoriteImpact, Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Assumptions:
	// a. Meteorites having a spherical sphere with 8 um radius
	// b. velocity of impact < 1 km/s -- Atmospheric entry simulations indicate that
	// particles from 10 to 1000 mm in diameter are slowed
	// below 1 km/s before impacting the surface of the planet (Flynn and McKay,
	// 1990).

	// Source 1: Inflatable Transparent Structures for Mars Greenhouse Applications
	// 2005-01-2846. SAE International.
	// data.spaceappschallenge.org/ICES.pdf

	// Source 2: 1963 NASA Technical Note D-1463 Meteoroid Hazard
	// http://ntrs.nasa.gov/archive/nasa/casi.ntrs.nasa.gov/19630002110.pdf

	// Source 3: Meteorite Accumulations on Mars
	// https://www.researchgate.net/publication/222568152_Meteorite_Accumulations_on_Mars

	private static final double CRITICAL_DIAMETER = .0016; // in cm
	private static final double AVERAGE_DENSITY = 1D; // in gram/cm^3
	private static final double IMPACT_VELOCITY = 1D; // in km/s

	// private Meteorite meteorite;
	// private BuildingManager buildingManager;

	// public MeteoriteImpactImpl(BuildingManager buildingManager) {
	// this.buildingManager = buildingManager;
	// this.meteorite = buildingManager.getMeteorite();
	// }

	/*
	 * Calculates the meteorite impact probability for the whole settlement once a
	 * sol
	 * 
	 * @param BuildingManager
	 */
	// Revise calculateMeteoriteProbability() to give the incoming meteorite
	// a unique profile with arbitrary degrees of randomness for each sol
	public void calculateMeteoriteProbability(BuildingManager buildingManager) {
		// System.out.println("Starting MeteoriteImpactImpl's
		// calculateMeteoriteProbability()");
		// The influx of meteorites entering Mars atmosphere can be estimated as
		// log N = -0.689* log(m) + 4.17

		// N is the number of meteorites per year having masses greater than m grams
		// incident on an area of 10^6 km2 (Bland and Smith, 2000).

		// Part I
		// Assuming size and penetration speed of the meteorites are homogeneous,
		// Find the probability of impact per square meter per sol on the settlement

		// a. average critical diameter is 0.0016 for meteorites having a spherical
		// sphere with 8 um radius
		double c_d = CRITICAL_DIAMETER * (1 + RandomUtil.getRandomDouble(.5) - RandomUtil.getRandomDouble(.5)); // in cm

		// b. density range from 0.7 to 2.2g/cm^3
		double a_rho = AVERAGE_DENSITY - RandomUtil.getRandomDouble(.3) + RandomUtil.getRandomDouble(1.2); // in
																											// gram/cm^3

		// c. velocity of impact < 1 km/s
		// Note: atmospheric entry simulations indicate particles from 10 to 1000 mm in
		// diameter are slowed to
		// usually below 1 km/s before impacting the surface of the planet (Flynn and
		// McKay, 1990)
		double impact_vel = IMPACT_VELOCITY * (1 + RandomUtil.getRandomDouble(.3) - RandomUtil.getRandomDouble(.3)); // in
																														// km/s

		// d. spherical volume 4/3 * pi * (r/2)^3
		// 1.33333 * Math.PI *.5*.5*.5 = .5236
		double sphericalVolume = 0.5236 * c_d * c_d * c_d; // .125 = *.5*.5*.5;

		// e. mass of a meteorite
		double massPerMeteorite = a_rho * sphericalVolume;

		// f. logN
		double logN = -0.689 * Math.log10(massPerMeteorite) + 4.17;

		// g. # of meteorite per year per meter
		// per 10^6 km2, need to convert to per sq meter by dividing 10^12
		double numMeteoritesPerYearPerMeter = Math.pow(10, logN - 12D); // = epsilon

		// h. probability of impact per square meter per year
		double probabilityOfImpactPerSQMPerYear = Math.exp(-numMeteoritesPerYearPerMeter);
		// System.out.println("probabilityOfImpactPerSQMPerYear : " +
		// probabilityOfImpactPerSQMPerYear);

		// i. probability of impact per square meter per sol
		double probabilityOfImpactPerSQMPerSol = probabilityOfImpactPerSQMPerYear / 668.6;
		// System.out.println("probabilityOfImpactPerSQMPerSol : " +
		// probabilityOfImpactPerSQMPerSol);

		// save it in the BuildingManager for all buildings in this settlement to apply
		// this value
		buildingManager.setProbabilityOfImpactPerSQMPerSol(probabilityOfImpactPerSQMPerSol);

		// Part II
		// Assuming size and impact speed of the meteorites are homogeneous,
		// determine how far the meteorites may penetrate the wall
		double penetrationRate = numMeteoritesPerYearPerMeter / 668.6;
		double penetrationThicknessOnAL = 1.09 * Math.pow(massPerMeteorite * impact_vel, 1 / 3D);

		// TODO: does it account for all angles of penetration on average ?

		double wallPenetrationThicknessAL = 1.5 * penetrationThicknessOnAL;
		// System.out.println("penetrationThicknessOnAL : " + penetrationThicknessOnAL);
		buildingManager.setWallPenetration(wallPenetrationThicknessAL);

		// Part III
		// TODO : Need helps in finding equations of the probability distribution of
		// different sizes and impact speed of the meteorites

	}

	// public double getProbabilityOfImpactPerSQMPerSol() {
	// return probabilityOfImpactPerSQMPerSol;
	// }

	// public Meteorite getMeteorite() {
	// return meteorite;
	// }

}
