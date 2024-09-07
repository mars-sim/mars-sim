/*
 * Mars Simulation Project
 * DustStorm.java
 * @date 2022-07-29
 * @author Manny Kung
 */

package com.mars_sim.core.environment;

import java.io.Serializable;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.air.AirComposition;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.tool.RandomUtil;

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

	private int settlementId;
	
	private int hemisphere;

	/**
	 * The surface wind speed of the dust storm.
	 */
	private double speed = 0;

	private String name;

	private MarsTime startTime;
				
	private Coordinates location;

	// In case of dust devil, for simplicity, height = size
	// Martian dust devil roughly 12 miles (20 kilometers) high was captured winding
	// its way along the Amazonis Planitia region of Northern Mars on March 14, 2012
	// by the High Resolution Imaging Science Experiment (HiRISE)
	// camera on NASA's Mars Reconnaissance Orbiter.

	private DustStormType type;

	private String description;

	/**
	 * Constructor.
	 * 
	 * @param type
	 * @param id
	 * @param weather
	 * @param origin
	 */
	DustStorm(DustStormType type, int id, Weather weather,
					 Settlement origin) {
		this.id = id;
		this.settlementId = origin.getIdentifier();
		this.location = origin.getCoordinates();
		setType(type);
		
		// Logic only assigns a meaningful size & speed for DUST_DEVIL
		if (type == DustStormType.DUST_DEVIL) {
			double meanPressure = (DEFAULT_MEAN_PRESSURE
					+ weather.calculateAirPressure(origin.getCoordinates(), HEIGHT)) / 2D;
			double t = origin.getOutsideTemperature() + AirComposition.C_TO_K;
			speed = Math.sqrt(R * t * DEFAULT_DELTA_PRESSURE / meanPressure);
			size = 1 + RandomUtil.getRandomInt(3);
		}
		else {
			// Should never get here as DustStorms always start as Dust Devils but need to set default
			speed = RandomUtil.getRandomInt(10);
			size = 1 + RandomUtil.getRandomInt(3); 
		}

		updateDescription();
	}

	public String getName() {
		return name;
	}

	public double getSpeed() {
		return speed;
	}
	
	/**
	 * Gets the timestamp it starts.
	 * 
	 * @return start time
	 */
	protected MarsTime getStartTime() {
		return startTime;
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

	/**
	 * Computes the new size of a storm in spherical diameter [in km]
	 * 
	 * @return size in km
	 */
	public int computeNewSize(boolean allowPlanetStorm) {
		int newSize = 0;
		double newSpeed = speed;

		// Future: account for how it would move from place to place by the speed and direction it would travel
		
		// a dust devil column can tower kilometers high and hundreds of meters wide,
		// 10 times larger than any tornado on Earth. Red-brown sand and dust whipping
		// around faster than 30 meters per second
		switch (type) {
		case DUST_DEVIL:
			int change = RandomUtil.getRandomInt(-1, 1);
			int s = size;

			newSize = s + change;

			// arbitrary speed determination
			newSpeed = 1 + .05 * newSize;
			break;
		

		case LOCAL:
			int up1 = RandomUtil.getRandomInt(0, 5);
			int down1 = RandomUtil.getRandomInt(0, 5);
			int s1 = size;

			newSize = s1 + up1 - down1;
			// arbitrary speed determination
			newSpeed = 10 + .1 * newSize;
			break;
			
		case REGIONAL:
			// Typically, within 10-15 days a storm can become planet-encircling
			// Source: http://mars.jpl.nasa.gov/odyssey/mgs/sci/fifthconf99/6011.pdf
			// Assuming it can grow up to 100 km per sol
			int up2 = RandomUtil.getRandomInt(0, 10);
			int down2 = RandomUtil.getRandomInt(0, 10);
			int s2 = size;

			newSize = s2 + up2 - down2;
			// From http://mars.jpl.nasa.gov/odyssey/mgs/sci/fifthconf99/6011.pdf
			// the probability of a planetary-encircling storm occurring in any particular
			// Mars year
			// is 33% based on Earth-based observational record in 1956, 1971, 1973, 1977
			// (two), and 1982.
			// how to incorporate this 33% into the algorithm here?

			// if there are already 2 planet encircling dust storm,
			// do not create the next one and reduce it to 3900
			if (!allowPlanetStorm && (newSize > REGIONAL_MAX)) {
					newSize = REGIONAL_MAX - 100;
			}
			
			// arbitrary speed determination
			newSpeed = 50 + .5 * newSize;
			break;
		

		case PLANET_ENCIRCLING:
			int up3 = RandomUtil.getRandomInt(0, 50);
			int down3 = RandomUtil.getRandomInt(0, 50);
			int s3 = size;

			newSize = s3 + up3 - down3;

			if (newSize > 6787 * Math.PI)
				newSize = (int) (6787 * Math.PI);
			// arbitrary speed determination
			newSpeed = 200 + .5 * newSize;
			break;
		}

		// Check classification for new size
		DustStormType newType = type;
		if (newSize <= 0) {
			// Storm is done.
			Settlement s = getSettlement();
			if (s.getDustStorm() == this) {
				s.setDustStorm(null);
			}
			newSize = 0;
		}
		else if (newSize < DUST_DEVIL_MAX) {
			newType = DustStormType.DUST_DEVIL;
		}
		else if (newSize < LOCAL_MAX) {
			newType = DustStormType.LOCAL;
		}
		else if (newSize < REGIONAL_MAX) {
			newType = DustStormType.REGIONAL;
		}
		else {
			newType = DustStormType.PLANET_ENCIRCLING;
		}
		
		if (newType != type) {
			setType(newType);
		}
		size = newSize;
		speed = newSpeed;

		updateDescription();
		return size;
	}

	/**
	 * Sets the type.
	 * 
	 * @param newType
	 */
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

	public Settlement getSettlement() {
		return (Settlement) Simulation.instance().getUnitManager().getUnitByID(settlementId);
	}
	
	public Coordinates getCoordinates() {
		return location;
	}

	/**
	 * Get the description of this storm
	 * @return
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Update the desxcription which is based on size & speed
	 * @return Updated description
	 */
    private void updateDescription() {
		description = name
				+ " (size " + size + " with wind speed "
				+ Math.round(speed * 10.0) / 10.0 + " m/s) was sighted.";
    }
}
