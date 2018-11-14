/**
 * Mars Simulation Project
 * DustStorm.java
 * @version 3.1.0 2017-08-31
 * @author Manny Kung
 */

package org.mars_sim.msp.core.mars;

import java.io.Serializable;
import java.util.List;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.structure.CompositionOfAir;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.tool.RandomUtil;

public class DustStorm implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Martian global dust storms tend to start in the southern hemisphere with a
	// local dust storm.

	// Southern spring and summer seem to be the season for global dust storms.

	// Local dust storms seem to be swept into huge storms which envelope the entire
	// planet, as was discovered
	// by the Viking mission.

	// Global dust storms do not seem to occur every Martian spring or summer,
	// however.

	// https://www.windows2universe.org/mars/atmosphere/global_dust_storms.html

//	private static final int NORTHERN = 0;
//	private static final int SOUTHERN = 1;
//	private static final int SYRIA_PLANUM = 0; //(12.1 S, 256.1 E)
//	private static final int SINAI_PLANUM = 1; //Sinai Planum (13.7 S, 272.2 E),
//	private static final int SOLIS_PLANUM = 3; // Solis Planum (26.4 S, 270 E),
//	private static final int THAUMASIA_FOSSAE = 4; //Planum/Thaumasia Fossae (21.7 S, 294.8 E)
//	private static final int HELLESPONTUE = 5; // Hellespontus ( 49.7 S, 35 E)
//	private static final int NOACHIS_TERRA = 6; // Noachis Terra

	private static final double R = 187D; // [in J/kg/K]

	private static final double HEIGHT = 5D; // [in km]

	private double mean_pressure = 650D; // [in Pa]

	private double delta_pressure = 1D; // [in Pa]

	private int size;

	private int id;

	private int hemisphere;

	/***
	 * The surface wind speed of the dust storm
	 */
	private double speed = 0;

	private String name;

	private Coordinates location;

	// private int tall;
	// In case of dust devil, for simplicity, height = size
	// Martian dust devil roughly 12 miles (20 kilometers) high was captured winding
	// its way along
	// the Amazonis Planitia region of Northern Mars on March 14, 2012 by the High
	// Resolution Imaging Science Experiment (HiRISE)
	// camera on NASA's Mars Reconnaissance Orbiter.

	private DustStormType type;

	private Weather weather;
	private MarsClock marsClock;
	private SurfaceFeatures surface;

	private List<Settlement> settlements;

	public DustStorm(String name, DustStormType type, int id, MarsClock marsClock, Weather weather,
			List<Settlement> settlements) {
		this.marsClock = marsClock;
		this.name = name;
		this.type = type;
		this.id = id;
		this.marsClock = marsClock;
		this.settlements = settlements;

		if (weather == null)
			weather = Simulation.instance().getMars().getWeather();

		if (type == DustStormType.PLANET_ENCIRCLING) {

		} else if (type == DustStormType.REGIONAL) {

		} else if (type == DustStormType.LOCAL) {
			// Assume the start size between 0 to 100 km
			// size = RandomUtil.getRandomInt(100);
			// Check if this is the first dust storm of this year
			// if (weather.getLocalDustStormMap().size() == 0)
			// hemisphere = RandomUtil.getRandomInt(0, 1);
			// }
			// else {
			// Check which hemisphere the previous dust storm is located at and avoid that
			// hemisphere
			// TODO: need to fix the NullPointerException below

//				int hemisphereUsed = weather.getPlanetEncirclingDustStormMap().get(0).getHemisphere();
//				if (hemisphereUsed == NORTHERN)
//					hemisphere = SOUTHERN;
//				else
//					hemisphere = NORTHERN;
			// }
		} else if (type == DustStormType.DUST_DEVIL) {

			for (Settlement s : settlements) {
				mean_pressure = (mean_pressure
						+ weather.calculateAirPressure(settlements.get(0).getCoordinates(), HEIGHT)) / 2D;
				double t = s.getOutsideTemperature() + CompositionOfAir.C_TO_K;
				speed = Math.sqrt(R * t * delta_pressure / mean_pressure);
				size = RandomUtil.getRandomInt(3);
				break;
			}

		}

	}

	public String getName() {
		return name;
	}

	public double getSpeed() {
		return speed;
	}

	public List<Settlement> getSettlements() {
		return settlements;
	}

	// Almost all of the planet-encircling storms have been observed to start in one
	// of two regions (a-d, e) on Mars:
	// Coordinates from http://planetarynames.wr.usgs.gov/Feature/5954
	// a. Syria Planum (12.1 S, 256.1 E)
	// b. Sinai Planum (13.7 S, 272.2 E),
	// c. Solis Planum (26.4 S, 270 E),
	// d. Planum/Thaumasia Fossae (21.7 S, 294.8 E)
	// e. Hellespontus ( 49.7 S, 35 E)
	// f. Noachis Terra (50.4 S, 354.8 E)
	// All in the southern subtropics between ~ 12-40 S.

	public int getHemisphere() {
		return hemisphere;
	}

	public int getSize() {
		return size;
	}

	public int getID() {
		return id;
	}

	public DustStormType getType() {
		return type;
	}

	/***
	 * Computes the new size of a storm in spherical diameter [in km]
	 * 
	 * @return size in km
	 */
	public int computeNewSize() {
		int newSize = 0;
		double newSpeed = speed;

		// a dust devil column can tower kilometers high and hundreds of meters wide,
		// 10 times larger than any tornado on Earth. Red-brown sand and dust whipping
		// around faster than 30 meters per second

		if (type == DustStormType.DUST_DEVIL) {
			int up = RandomUtil.getRandomInt(0, 5);
			int down = RandomUtil.getRandomInt(0, 5);
			int s = size;

			newSize = s + up - down;

			if (newSize < 0)
				newSize = 0;

			// arbitrary speed determination
			newSpeed = 8 + .4 * newSize;

		}

		else if (type == DustStormType.LOCAL) {
			int up = RandomUtil.getRandomInt(0, 50);
			int down = RandomUtil.getRandomInt(0, 50);
			int s = size;

			newSize = s + up - down;
			// arbitrary speed determination
			newSpeed = 15 + .05 * newSize;

		} else if (type == DustStormType.REGIONAL) {
			// Typically, within 10-15 days a storm can become planet-encircling
			// Source: http://mars.jpl.nasa.gov/odyssey/mgs/sci/fifthconf99/6011.pdf
			// Assuming it can grow up to 100 km per sol
			int up = RandomUtil.getRandomInt(0, 100);
			int down = RandomUtil.getRandomInt(0, 100);
			int s = size;

			newSize = s + up - down;
			// From http://mars.jpl.nasa.gov/odyssey/mgs/sci/fifthconf99/6011.pdf
			// the probability of a planetary-encircling storm occurring in any particular
			// Mars year
			// is 33% based on Earth-based observational record in 1956, 1971, 1973, 1977
			// (two), and 1982.
			// TODO: how to incorporate this 33% into the algorithm here?

			// if there are already 2 planet encircling dust storm,
			// do not create the next one and reduce it to 3900
			if (weather.getPlanetEncirclingDustStorms().size() > 1)
				if (newSize > 4000)
					newSize = 3900;

			// arbitrary speed determination
			newSpeed = 100 + .02 * newSize;

		}

		else if (type == DustStormType.PLANET_ENCIRCLING) {
			int up3 = RandomUtil.getRandomInt(0, 500);
			int down3 = RandomUtil.getRandomInt(0, 500);
			int sss = size;

			newSize = sss + up3 - down3;

			if (newSize > 6787 * Math.PI)
				newSize = (int) (6787 * Math.PI);
			// arbitrary speed determination
			newSpeed = 200 + .01 * newSize;

		}

		size = newSize;
		speed = newSpeed;

		return size;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setType(DustStormType type) {
		this.type = type;
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
