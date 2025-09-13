/*
 * Mars Simulation Project
 * MarsZone.java
 * @date 2025-09-13
 * @author Barry Evans
 */
package com.mars_sim.core.time;

import java.io.Serializable;

import com.mars_sim.core.map.location.Coordinates;

/**
 * THis class represents a time zone on the Mars surface
 */
public class MarsZone implements Serializable {
    /**
     * The number of time zones on mars
     * The number of zones must be a multiple of 1000
     */
    public static final int NUM_ZONES = 20;

    // The millisols per zone
    public static final int MSOLS_PER_ZONE = 1000/20;

    private String id;
    private int offset;

    /**
     * Private constructor only used by factory method
     * @param zoneid
     */
    private MarsZone(int zoneid) {
        offset = zoneid * MSOLS_PER_ZONE;

        int zonecode;
        if (zoneid < NUM_ZONES/2) {
            zonecode = zoneid;
        }
        else {
            zonecode = -1 * (NUM_ZONES - zoneid);
        }                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       
        id = String.format("MCT%+d", zonecode);
    }

    /**
     * Gets the unique id for this zone
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the millisol offset from the Mars Central Time (MCT).
     * @return
     */
    public int getMSolOffset() {
        return offset;
    }

    /**
	 * Gets the Mars time zone time offset for a given point on the surface.
	 * 
	 * @param point
	 * @return
	 */
	public static MarsZone getMarsZone(Coordinates point) {
		// Get the rotation about the planet and convert that to a fraction of the Sol.
		double fraction = point.getTheta()/(Math.PI * 2D); 

        int zoneid = (int) Math.floor(NUM_ZONES * fraction);
		return new MarsZone(zoneid);
	}

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + offset;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (obj instanceof MarsZone other) {
            return (offset == other.offset);
        }
        return false;
    }
}
