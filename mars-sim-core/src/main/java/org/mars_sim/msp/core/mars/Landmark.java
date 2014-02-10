/**
 * Mars Simulation Project
 * Landmark.java
 * @version 3.06 2014-01-29
 * @author Dalen Kruse
 */

package org.mars_sim.msp.core.mars;

import java.io.Serializable;

import org.mars_sim.msp.core.Coordinates;


/** The Landmark class represents a surface landmark on virtual Mars.
 *  It contains information related to the landmark.
 */
public class Landmark implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members
	/** Name of the landmark. */
	private String landmarkName;
	/** Location of the landmark. */
	private Coordinates landmarkLocation;
	/** Diameter of the landmark, rounded to int. */
	private int landmarkDiameter;

    /** Constructs a landmark object with the given name at the given location.
     *  @param name name of the landmark
     *  @param location location of the landmark
     */
    public Landmark(String name, Coordinates location, int diameter) {

        this.landmarkName = name;
        this.landmarkLocation = location;
        this.landmarkDiameter = diameter;
    }

    /**
     *  Sets the landmark name.
     *  @param landmarkName name of the landmark
     */
    public void setLandmarkName(String landmarkName) {
        this.landmarkName = landmarkName;
    }

    /**
     *  Sets the landmark location.
     *  @param landmarkLocation location of the landmark
     */
    public void setLandmarkLocation(Coordinates landmarkLocation) {
        this.landmarkLocation = landmarkLocation;
    }

    /**
     *  Sets the landmark diameter.
     *  @param landmarkLocation diameter of the landmark
     */
    public void setLandmarkDiameter(int diameter) {
        this.landmarkDiameter = diameter;
    }

    /**
     *  Gets the landmark name.
     *  @return name of the landmark
     */
    public String getLandmarkName() {
        return landmarkName;
    }

    /**
     *  Gets the landmark location.
     *  @return location of the landmark
     */
    public Coordinates getLandmarkLocation() {
        return landmarkLocation;
    }

    /**
     *  Gets the landmark diameter.
     *  @return location of the landmark
     */
    public int getLandmarkDiameter() {
        return landmarkDiameter;
    }
}