/**
 * Mars Simulation Project
 * DustStorm.java
 * @version 3.1.2 2020-09-02
 * @author Manny Kung
 */

package org.mars_sim.msp.core.mars;

import java.io.Serializable;
import java.util.List;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.structure.CompositionOfAir;
import org.mars_sim.msp.core.structure.Settlement;
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

	// Max size of a dust devil
	private static final int DUST_DEVIL_MAX = 20;

	// Max size of a local storm
	private static final int LOCAL_MAX = 2000;

	// Max size of a regional
	private static final int REGIONAL_MAX = 4000;

	private static final double DEFAULT_MEAN_PRESSURE = 650D; // [in Pa]

	private static final double DEFAULT_DELTA_PRESSURE = 1D; // [in Pa]

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
	// its way along the Amazonis Planitia region of Northern Mars on March 14, 2012
	// by the High Resolution Imaging Science Experiment (HiRISE)
	// camera on NASA's Mars Reconnaissance Orbiter.

	private DustStormType type;

	private List<Settlement> settlements;

	
	public DustStorm(DustStormType type, int id, Weather weather,
			List<Settlement> settlements) {
		this.id = id;
		this.settlements = settlements;
		setType(type);
		
		if (settlements.isEmpty()) {
			throw new IllegalArgumentException("Settlements can not be empty");
		}
		
		// Logic only assigns a meaningful size & speed for DUST_DEVIL
		if (type == DustStormType.DUST_DEVIL) {
			Settlement s = settlements.get(0);
			double meanPressure = (DEFAULT_MEAN_PRESSURE
					+ weather.calculateAirPressure(s.getCoordinates(), HEIGHT)) / 2D;
			double t = s.getOutsideTemperature() + CompositionOfAir.C_TO_K;
			speed = Math.sqrt(R * t * DEFAULT_DELTA_PRESSURE / meanPressure);
			size = RandomUtil.getRandomInt(3);
		}
		else {
			// Should never get here as DustStorms always start as Dust Devils but need to set default
			speed = RandomUtil.getRandomInt(10);
			size = RandomUtil.getRandomInt(3); 
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
	public int computeNewSize(boolean allowPlanetStorm) {
		int newSize = 0;
		double newSpeed = speed;

		// a dust devil column can tower kilometers high and hundreds of meters wide,
		// 10 times larger than any tornado on Earth. Red-brown sand and dust whipping
		// around faster than 30 meters per second
		switch (type) {
		case DUST_DEVIL:
			int up = RandomUtil.getRandomInt(0, 5);
			int down = RandomUtil.getRandomInt(0, 5);
			int s = size;

			newSize = s + up - down;

			if (newSize < 0)
				newSize = 0;

			// arbitrary speed determination
			newSpeed = 8 + .4 * newSize;
			break;
		

		case LOCAL:
			int up1 = RandomUtil.getRandomInt(0, 50);
			int down1 = RandomUtil.getRandomInt(0, 50);
			int s1 = size;

			newSize = s1 + up1 - down1;
			// arbitrary speed determination
			newSpeed = 15 + .05 * newSize;
			break;
			
		case REGIONAL:
			// Typically, within 10-15 days a storm can become planet-encircling
			// Source: http://mars.jpl.nasa.gov/odyssey/mgs/sci/fifthconf99/6011.pdf
			// Assuming it can grow up to 100 km per sol
			int up2 = RandomUtil.getRandomInt(0, 100);
			int down2 = RandomUtil.getRandomInt(0, 100);
			int s2 = size;

			newSize = s2 + up2 - down2;
			// From http://mars.jpl.nasa.gov/odyssey/mgs/sci/fifthconf99/6011.pdf
			// the probability of a planetary-encircling storm occurring in any particular
			// Mars year
			// is 33% based on Earth-based observational record in 1956, 1971, 1973, 1977
			// (two), and 1982.
			// TODO: how to incorporate this 33% into the algorithm here?

			// if there are already 2 planet encircling dust storm,
			// do not create the next one and reduce it to 3900
			if (!allowPlanetStorm && (newSize > REGIONAL_MAX)) {
					newSize = REGIONAL_MAX - 100;
			}
			
			// arbitrary speed determination
			newSpeed = 100 + .02 * newSize;
			break;
		

		case PLANET_ENCIRCLING:
			int up3 = RandomUtil.getRandomInt(0, 500);
			int down3 = RandomUtil.getRandomInt(0, 500);
			int s3 = size;

			newSize = s3 + up3 - down3;

			if (newSize > 6787 * Math.PI)
				newSize = (int) (6787 * Math.PI);
			// arbitrary speed determination
			newSpeed = 200 + .01 * newSize;
			break;
		}

		// Check classification for new size
		DustStormType newType = type;
		if (newSize == 0) {
			// Storm is done.
			for (Settlement s : settlements) {
				if (s.getDustStorm() == this) {
					s.setDustStorm(null);
				}
			}
		}
		else if (newSize < DUST_DEVIL_MAX) {
			newType = DustStormType.DUST_DEVIL;
		}
		else if ((DUST_DEVIL_MAX <= newSize) && (newSize < LOCAL_MAX)) {
			newType = DustStormType.LOCAL;
		}
		else if ((LOCAL_MAX <= newSize) && (newSize < REGIONAL_MAX)) {
			newType = DustStormType.REGIONAL;
		}
		else if (REGIONAL_MAX < newSize) {
			newType = DustStormType.REGIONAL;
		}
		
		if (newType != type) {
			setType(newType);
		}
		size = newSize;
		speed = newSpeed;

		return size;
	}

	private void setType(DustStormType newType) {
		String newName = null;
		switch(newType) {
			case PLANET_ENCIRCLING:
				newName = "Planet Encircling Storm-" + id;
				break;
			case DUST_DEVIL:
				newName = "Dust Devil-" + id;
				break;
			case REGIONAL:
				newName = "Regional Storm-" + id;
				break;
			case LOCAL:
				newName = "Local Storm-" + id;
				break;
			default:
				newName = name;
		}		
		name = newName;
		type = newType;
	}

	public void setCoordinates(Coordinates location) {
		this.location = location;
	}

	public Coordinates getCoordinates() {
		return location;
	}
}
