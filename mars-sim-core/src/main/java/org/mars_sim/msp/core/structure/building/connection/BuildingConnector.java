/**
 * Mars Simulation Project
 * BuildingConnector.java
 * @version 3.1.0 2017-04-15
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.connection;

import java.io.Serializable;

import org.mars_sim.msp.core.structure.building.Building;

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

    /**
     * Constructor
     * @param building1 The first connected building.
     * @param building1HatchXLocation The hatch X location connecting the first building.
     * @param building1HatchYLocation The hatch Y location connecting the first building.
     * @param building1HatchFacing The hatch facing (degrees) connecting the first building.
     * @param building2 The second connected building.
     * @param building2HatchXLocation The hatch X location connecting the second building.
     * @param building2HatchYLocation The hatch y location connecting the second building.
     * @param building2HatchFacing The hatch facing (degrees) connecting the second building.
     */
    public BuildingConnector(
    		Building building1, 
    		double building1HatchXLocation,
            double building1HatchYLocation, double building1HatchFacing, 
            Building building2,
            double building2HatchXLocation, double building2HatchYLocation,
            double building2HatchFacing) {

        this.building1 = building1;
        hatch1 = new Hatch(building1, this, building1HatchXLocation, building1HatchYLocation,
                building1HatchFacing);
        this.building2 = building2;
        hatch2 = new Hatch(building2, this, building2HatchXLocation, building2HatchYLocation,
                building2HatchFacing);

        // Check if building 1 and 2 locations are off by a small amount.
        if (Math.abs(building1HatchXLocation - building2HatchXLocation) < SMALL_AMOUNT_COMPARISON) {
            hatch2.setXLocation(building1HatchXLocation);
        }
        if (Math.abs(building1HatchYLocation - building2HatchYLocation) < SMALL_AMOUNT_COMPARISON) {
            hatch2.setYLocation(building1HatchYLocation);
        }
    }

    /**
     * Gets the first connected building.
     * @return building.
     */
    public Building getBuilding1() {
        return building1;
    }

    /**
     * Gets the second connected building.
     * @return building.
     */
    public Building getBuilding2() {
        return building2;
    }

    /**
     * The hatch connecting the first building.
     * @return hatch.
     */
    public Hatch getHatch1() {
        return hatch1;
    }

    /**
     * The hatch connecting the second building.
     * @return hatch.
     */
    public Hatch getHatch2() {
        return hatch2;
    }

    /**
     * Checks if the two hatches are not at the same location for the connection.
     * @return true if hatches are split.
     */
    public boolean isSplitConnection() {

        return ((hatch1.getXLocation() != hatch2.getXLocation()) ||
                (hatch1.getYLocation() != hatch2.getYLocation()));
    }

    /**
     * Gets the X location of the center of the building connection.
     * @return x location.
     */
    public double getXLocation() {

        double result = 0D;

        if (isSplitConnection()) {
            result = (hatch1.getXLocation() + hatch2.getXLocation()) / 2D;
        }
        else {
            result = hatch1.getXLocation();
        }

        return result;
    }

    /**
     * Gets the Y location of the center of the building connection.
     * @return y location.
     */
    public double getYLocation() {

        double result = 0D;

        if (isSplitConnection()) {
            result = (hatch1.getYLocation() + hatch2.getYLocation()) / 2D;
        }
        else {
            result = hatch1.getYLocation();
        }

        return result;
    }

    @Override
    public boolean equals(Object other) {

        boolean result = false;

        // Note: building 1 and building 2 can be reversed in equal connectors.
        if (other instanceof BuildingConnector) {
            BuildingConnector otherConnector = (BuildingConnector) other;
            if (building1.equals(otherConnector.getBuilding1()) &&
                    hatch1.equals(otherConnector.getHatch1()) &&
                    building2.equals(otherConnector.getBuilding2()) &&
                    hatch2.equals(otherConnector.getHatch2())) {
                result = true;
            }
            else if (building1.equals(otherConnector.getBuilding2()) &&
                    hatch1.equals(otherConnector.getHatch2()) &&
                    building2.equals(otherConnector.getBuilding1()) &&
                    hatch2.equals(otherConnector.getHatch1())) {
                result = true;
            }
        }

        return result;
    }

    /**
     * Prepare object for garbage collection.
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