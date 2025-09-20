/*
 * Mars Simulation Project
 * BuildingConnector.java
 * @date 2025-07-18
 * @author Scott Davis
 */
package com.mars_sim.core.building.connection;

import java.io.Serializable;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.map.location.LocalPosition;

/**
 * A connection between two buildings.
 */
public class BuildingConnector implements Serializable, InsidePathLocation {

	private static final long serialVersionUID = 1L;

	// Comparison to indicate a small but non-zero amount.
    private static final double SMALL_AMOUNT_COMPARISON = .0000001D;

    // Data members
    private Building building1;
    private Building building2;
    private Hatch hatch1;
    private Hatch hatch2;

	private LocalPosition pos;

    /**
     * Constructor.
     * 
     * @param building1 The first connected building.
     * @param building1HatchPosition The hatch position connecting the first building.
     * @param building1HatchFacing The hatch facing (degrees) connecting the first building.
     * @param building2 The second connected building.
     * @param building2HatchPosition The hatch position connecting the second building.
     * @param building2HatchFacing The hatch facing (degrees) connecting the second building.
     */
    public BuildingConnector(
    		Building building1, 
    		LocalPosition building1HatchPosition, double building1HatchFacing, 
            Building building2,
            LocalPosition building2HatchPosition, double building2HatchFacing) {
        
    	// Check if building 1 and 2 locations are off by a small amount.
        if (building1HatchPosition.getDistanceTo(building2HatchPosition) < SMALL_AMOUNT_COMPARISON) {
        	building2HatchPosition = building1HatchPosition;
        }
        this.building1 = building1;
        hatch1 = new Hatch(building1, this, building1HatchPosition, building1HatchFacing);
        this.building2 = building2;
        hatch2 = new Hatch(building2, this, building2HatchPosition, building2HatchFacing);

        // Finally calculate the local position
        if (isSplitConnection()) {
        	pos = building1HatchPosition.getMidPosition(building2HatchPosition);
        }
        else {
        	pos = hatch1.getPosition();
        }
    }

    /**
     * Gets the first connected building.
     * 
     * @return building.
     */
    public Building getBuilding1() {
        return building1;
    }

    /**
     * Gets the second connected building.
     * 
     * @return building.
     */
    public Building getBuilding2() {
        return building2;
    }

    /**
     * Gets the other end of the connection.
     * 
     * @return
     */
    public Building getOtherBuilding(Building building) {
        if (building.equals(building1)) {
            return building2;
        }
        else if (building.equals(building2)) {
            return building1;
        }
        else {
            throw new IllegalArgumentException("Building not part of this connector.");
        }
    }

    /**
     * Gets the hatch connecting the first building.
     * 
     * @return hatch.
     */
    public Hatch getHatch1() {
        return hatch1;
    }

    /**
     * Gets the hatch connecting the second building.
     * 
     * @return hatch.
     */
    public Hatch getHatch2() {
        return hatch2;
    }

    /**
     * Checks if the two hatches are not at the same location for the connection.
     * 
     * @return true if hatches are split.
     */
    public boolean isSplitConnection() {

        return !hatch1.getPosition().equals(hatch2.getPosition());
    }

    @Override
    public LocalPosition getPosition() {
    	return pos;
    }
    
    @Override
    public boolean equals(Object other) {

        // Note: building 1 and building 2 can be reversed in equal connectors.
        if (other instanceof BuildingConnector otherConnector) {
            if (building1.equals(otherConnector.getBuilding1()) &&
                    hatch1.equals(otherConnector.getHatch1()) &&
                    building2.equals(otherConnector.getBuilding2()) &&
                    hatch2.equals(otherConnector.getHatch2())) {
                return true;
            }
            else {
                return (building1.equals(otherConnector.getBuilding2()) &&
                    hatch1.equals(otherConnector.getHatch2()) &&
                    building2.equals(otherConnector.getBuilding1()) &&
                    hatch2.equals(otherConnector.getHatch1()));
            }
        }

        return false;
    }

	/**
	 * Gets the hash code for this object.
	 *
	 * @return hash code.
	 */
	@Override
	public int hashCode() {
		return super.hashCode();
	}
	
    /**
     * Prepares object for garbage collection.
     */
    public void destroy() {
        building1 = null;
        building2 = null;
        hatch1.destroy();
        hatch1 = null;
        hatch2.destroy();
        hatch2 = null;
    }
}
