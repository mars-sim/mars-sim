/**
 * Mars Simulation Project
 * MeteoriteImpactImpl.java
 * @version 3.08 2015-06-04
 * @author Manny Kung
 */

package org.mars_sim.msp.core.mars;

import org.mars_sim.msp.core.structure.building.BuildingManager;

public class MeteoriteImpactImpl implements MeteoriteImpact{

	double meteoriteCriticalDiameter = .0016; // in cm
	double averageDensity = 2.7D; // in gram/cm^3
	double impactVelocity = 1D; // in km/s
	double sphericalVolume ; // in cm^3
	double massPerMeteorite ;
	double logN ;
	double numMeteoritesPerYearPerMeter ;
	double probabilityOfImpactPerSQMPerYear ;

	double probabilityOfImpactPerSQMPerSol;
	double penetrationRate ;
	double penetrationThicknessOnAL ;
	double wallThicknessAL;

	private Meteorite meteorite;
	//private BuildingManager buildingManager;

	//public MeteoriteImpactImpl(BuildingManager buildingManager) {
	//	this.buildingManager = buildingManager;
	//	this.meteorite = buildingManager.getMeteorite();
	//}

	public void calculateMeteoriteProbability(BuildingManager buildingManager) {
		//System.out.println("starting calculateMeteoriteProbability()");
		// The influx of meteorites entering Mars atmosphere can be estimated as
		// log N = -0.689* log(m) + 4.17
		// N is the number of meteorites per year having masses greater than m grams incident on an area of 10^6 km2 (Bland and Smith, 2000).

		// Currently Assumptions:
		// a. Meteorites having a spherical sphere with 8 um radius
		meteoriteCriticalDiameter = .0016; // in cm

		// b. density range from 0.7 to 2.2g/cm^3
		averageDensity = 1D; // in gram/cm^3

		// c. velocity of impact < 1 km/s
		// Note: atmospheric entry simulations indicate particles from 10 to 1000 mm in diameter are slowed to
		// usually below 1 km/s before impacting the surface of the planet (Flynn and McKay, 1990)
		impactVelocity = 1D; // in km/s

		sphericalVolume = 4D * Math.PI / 3D * meteoriteCriticalDiameter*meteoriteCriticalDiameter*meteoriteCriticalDiameter*.5*.5*.5;
		massPerMeteorite = averageDensity * sphericalVolume;
		logN = -0.689 * Math.log10(massPerMeteorite) + 4.17;
		numMeteoritesPerYearPerMeter = Math.pow(10, logN-12D); // = epsilon
		probabilityOfImpactPerSQMPerYear = Math.exp(-numMeteoritesPerYearPerMeter);

		probabilityOfImpactPerSQMPerSol = probabilityOfImpactPerSQMPerYear/365.24;

		buildingManager.setProbabilityOfImpactPerSQMPerSol(probabilityOfImpactPerSQMPerSol);

		penetrationRate = numMeteoritesPerYearPerMeter/365.24;
		penetrationThicknessOnAL = 1.09 * Math.pow(massPerMeteorite*impactVelocity, 1/3D);
		wallThicknessAL = 1.5 * penetrationThicknessOnAL;

		//System.out.println("probabilityOfImpactPerYear : " + probabilityOfImpactPerSQMPerYear);
		//System.out.println("probabilityOfImpactPerSol : " + probabilityOfImpactPerSQMPerSol);
		//System.out.println("wallThicknessAL : " + wallThicknessAL);

		// Source 1: Inflatable Transparent Structures for Mars Greenhouse Applications 2005-01-2846. SAE International.
		// data.spaceappschallenge.org/ICES.pdf

		// Source 2: 1963 NASA Technical Note D-1463 Meteoroid Hazard
		// http://ntrs.nasa.gov/archive/nasa/casi.ntrs.nasa.gov/19630002110.pdf


	}

	public double getProbabilityOfImpactPerSQMPerSol() {
		return probabilityOfImpactPerSQMPerSol;
	}

	public Meteorite getMeteorite() {
		return meteorite;
	}

}
