/**
 * Mars Simulation Project
 * DustStorm.java
 * @version 3.08 2015-06-10
 * @author Manny Kung
 */

package org.mars_sim.msp.core.mars;

import java.io.Serializable;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.time.MarsClock;

public class DustStorm implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;


	private static final int NORTHERN = 0;
	private static final int SOUTHERN = 1;
	private static final int SYRIA_PLANUM = 0; //(12.1 S, 256.1 E)
	private static final int SINAI_PLANUM = 1; //Sinai Planum (13.7 S, 272.2 E),
	private static final int SOLIS_PLANUM = 3; // Solis Planum (26.4 S, 270 E),
	private static final int THAUMASIA_FOSSAE = 4; //Planum/Thaumasia Fossae (21.7 S, 294.8 E)
	private static final int HELLESPONTUE = 5; // Hellespontus ( 49.7 S, 35 E)
	private static final int NOACHIS_TERRA = 6; // Noachis Terra

	private int size;
	private int hemisphere;
	private Coordinates location;
	//private int tall;
	// In case of dust devil, for simplicity, height = size
	// Martian dust devil roughly 12 miles (20 kilometers) high was captured winding its way along
	// the Amazonis Planitia region of Northern Mars on March 14, 2012 by the High Resolution Imaging Science Experiment (HiRISE)
	// camera on NASA's Mars Reconnaissance Orbiter.


	private String name;
	private final DustStormType type;

	private Weather weather;
	private MarsClock marsClock;


	public DustStorm(String name, DustStormType type, MarsClock marsClock) {
		this.marsClock = marsClock;
		this.name = name;
		this.type = type;

		if (type == DustStormType.PLANET_ENCIRCLING) {

		}
		else if (type == DustStormType.REGIONAL) {

		}
		else if (type == DustStormType.LOCAL) {
			// Assume the start size between 0 to 100 km
			size = RandomUtil.getRandomInt(100);

			if (weather == null)
				weather = Simulation.instance().getMars().getWeather();

			// Check if this is the first dust storm of this year
			if (weather.getLocalDustStormMap().size() == 0)
				hemisphere = RandomUtil.getRandomInt(0, 1);
			else {

				// Check which hemisphere the previous dust storm is located at and avoid that hemisphere
				// TODO: need to fix the NullPointerException below
/*
				int hemisphereUsed = weather.getPlanetEncirclingDustStormMap().get(0).getHemisphere();
				if (hemisphereUsed == NORTHERN)
					hemisphere = SOUTHERN;
				else
					hemisphere = NORTHERN;
*/
			}
		}

	}


	// Almost all of the planet-encircling storms have been observed to start in one of two regions (a-d, e) on Mars:
	// Coordinates from http://planetarynames.wr.usgs.gov/Feature/5954
	//  a. Syria Planum (12.1 S, 256.1 E)
	//	b. Sinai Planum (13.7 S, 272.2 E),
	// 	c. Solis Planum (26.4 S, 270 E),
	//	d. Planum/Thaumasia Fossae (21.7 S, 294.8 E)
	//  e. Hellespontus ( 49.7 S, 35 E)
	//  f. Noachis Terra (50.4 S, 354.8 E)
	//  All in the southern subtropics between ~ 12-40 S.

	public int getHemisphere() {
		return hemisphere;
	}

	public int getSize() {
		return size;
	}

	// size of spherical diameter in km
	public int getNewSize() {
		int tempSize = 0;

		if (type == DustStormType.DUST_DEVIL) {
			int up = RandomUtil.getRandomInt(0, 15);
			int down = RandomUtil.getRandomInt(0, 15);
			int s = size;

			tempSize = s + up - down;

			if (tempSize < 0)
				tempSize = 0;

		}

		else if (type == DustStormType.LOCAL) {
			int up = RandomUtil.getRandomInt(0, 70);
			int down = RandomUtil.getRandomInt(0, 70);
			int s = size;

			tempSize = s + up - down;

			if (tempSize < 15)
				tempSize = 15;

		}
		else if (type == DustStormType.REGIONAL) {
			// Typically, within 10-15 days a storm can become planet-encircling
			// Source: http://mars.jpl.nasa.gov/odyssey/mgs/sci/fifthconf99/6011.pdf
			// Assuming it can grow up to 100 km per sol
			int up = RandomUtil.getRandomInt(0, 100);
			int down = RandomUtil.getRandomInt(0, 100);
			int s = size;
			tempSize = s + up - down;
			// From http://mars.jpl.nasa.gov/odyssey/mgs/sci/fifthconf99/6011.pdf
			// the probability of a planetary-encircling storm occurring in any particular Mars year
			// is 33% based on Earth-based observational record in 1956, 1971, 1973, 1977 (two), and 1982.
			// TODO: how to incorporate this 33% into the algorithm here?

			// if there are already 2 planet encircling dust storm,
			// do not create the next one and reduce it to 3900
			if (weather.getPlanetEncirclingDustStormMap().size() > 1)
				if (tempSize > 4000)
					tempSize = 3900;

		}

		else if (type == DustStormType.PLANET_ENCIRCLING) {
			int up3 = RandomUtil.getRandomInt(0, 150);
			int down3 = RandomUtil.getRandomInt(0, 150);
			int sss = size;

			tempSize = sss + up3 - down3;

			if (tempSize > 6000)
				tempSize = 6000;

		}

		size = tempSize ;

		return size;
	}

	public void setWeather(Weather weather) {
		this.weather = weather;
	}

	public void setCoordinates(Coordinates location) {
		this.location = location;
	}

	public Coordinates getCoordinates() {
		return location;
	}
}
