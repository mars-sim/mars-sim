/**
 * Mars Simulation Project
 * Landmark.java
 * @date 2023-06-22
 * @author Dalen Kruse
 */

package com.mars_sim.core.environment;

import java.io.Serializable;

import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.map.location.SurfacePOI;

/**
 * The Landmark class represents a surface landmark on virtual Mars. It contains
 * information related to the landmark.
 */
public class Landmark implements Serializable, SurfacePOI {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Artifical object landmark type; other do exist
	public static final String AO_TYPE = "AO";

	// Data members
	/** Name of the landmark. */
	private String landmarkName;
	/** Description of the landmark. */
	private String description;
	/** Landing location of the landmark. */
	private String landingLocation;
	/** Coordinates of the landmark. */
	private Coordinates coord;
	/** Diameter of the landmark, rounded to int. */
	private int landmarkDiameter;
	/** Origin of the designation. */
	private String landmarkOrigin;
	/** Type of feature, e.g. Mons (MO). */
	private String landmarkType;

	/**
	 * Constructs a landmark object with the given name at the given location.
	 * 
	 * @param name
	 * @param description
	 * @param landingLocation
	 * @param coord
	 * @param diameter
	 * @param origin
	 * @param type
	 */
	public Landmark(String name, String description, String landingLocation, Coordinates coord, int diameter, String origin, String type) {

		this.landmarkName = name;
		this.description = description;
		this.landingLocation = landingLocation;
		this.coord = coord;
		this.landmarkDiameter = diameter;
		this.landmarkOrigin = origin;
		this.landmarkType = type;
	}

	/**
	 * Gets the landmark name.
	 * 
	 * @return name of the landmark
	 */
	public String getName() {
		return landmarkName;
	}

	/**
	 * Gets the description.
	 * 
	 * @return  description of the landmark
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * Gets the landmark landing location.
	 * 
	 * @return landing location of the landmark
	 */
	public String getLandingLocation() {
		return landingLocation;
	}
	
	/**
	 * Gets the landmark coordinates.
	 * 
	 * @return coordinates of the landmark
	 */
	@Override
	public Coordinates getLocation() {
		return coord;
	}

	/**
	 * Gets the landmark diameter.
	 * 
	 * @return location of the landmark
	 */
	public int getDiameter() {
		return landmarkDiameter;
	}

	/**
	 * Gets the origin.
	 * 
	 * @return origin of the landmark name
	 */
	public String getOrigin() {
		return landmarkOrigin;
	}

	/**
	 * Gets the type.
	 * 
	 * @return type of the landmark
	 */
	public String getType() {
		return landmarkType;
	}
}
