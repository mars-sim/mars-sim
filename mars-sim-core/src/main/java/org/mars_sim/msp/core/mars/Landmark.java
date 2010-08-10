/**
 * Mars Simulation Project
 * Landmark.java
 * @version 3.00 2010-08-10
 * @author Dalen Kruse
 */

package org.mars_sim.msp.core.mars;

import java.io.Serializable;

import org.mars_sim.msp.core.Coordinates;


/** The Landmark class represents a surface landmark on virtual Mars.
 *  It contains information related to the landmark.
 */
public class Landmark implements Serializable {

    // Data members
    private String landmarkName;    // Name of the landmark
    private Coordinates landmarkLocation;   // Location of the landmark

    /** Constructs a landmark object with the given name at the given location.
     *  @param name name of the landmark
     *  @param location location of the landmark
     */
    public Landmark(String name, Coordinates location) {

        this.landmarkName = name;
        this.landmarkLocation = location;
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
}