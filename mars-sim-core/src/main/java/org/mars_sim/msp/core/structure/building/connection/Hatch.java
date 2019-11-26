/**
 * Mars Simulation Project
 * Hatch.java
 * @version 3.1.0 2017-08-17
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.connection;

import java.io.Serializable;

import org.mars_sim.msp.core.LocalBoundedObject;
import org.mars_sim.msp.core.structure.building.Building;

/**
 * A hatch on one side of a building connection.
 */
public class Hatch implements Serializable, LocalBoundedObject, InsidePathLocation {

	private static final long serialVersionUID = 1L;
	
	// Static members.
	public static final double LENGTH = .6D;
	public static final double WIDTH = 2.76D;

	// Data members
	private Building building;
	private BuildingConnector connector;
	private double xLoc;
	private double yLoc;
	private double facing;

	/**
	 * Constructor.
	 * 
	 * @param building  the building the hatch is connected to.
	 * @param connector the building connector for the hatch.
	 * @param xLoc      The X location of the center point of the hatch.
	 * @param yLoc      The Y location of the center point of the hatch.
	 * @param facing    The facing of the hatch (degrees).
	 */
	public Hatch(Building building, BuildingConnector connector, double xLoc, double yLoc, double facing) {
		this.building = building;
		this.connector = connector;
		this.xLoc = xLoc;
		this.yLoc = yLoc;
		this.facing = facing;
	}

	/**
	 * Gets the building the hatch is connected to.
	 * 
	 * @return building.
	 */
	public Building getBuilding() {
		return building;
	}

	/**
	 * Gets the building connector for the hatch.
	 * 
	 * @return connector.
	 */
	public BuildingConnector getBuildingConnector() {
		return connector;
	}

	@Override
	public double getXLocation() {
		return xLoc;
	}

	/**
	 * Sets the X location of the center point of the hatch.
	 * 
	 * @param xLoc the X location (meters).
	 */
	void setXLocation(double xLoc) {
		this.xLoc = xLoc;
	}

	@Override
	public double getYLocation() {
		return yLoc;
	}

	/**
	 * Sets the X location of the center point of the hatch.
	 * 
	 * @param xLoc the X location (meters).
	 */
	void setYLocation(double yLoc) {
		this.yLoc = yLoc;
	}

	@Override
	public double getWidth() {
		return WIDTH;
	}

	@Override
	public double getLength() {
		return LENGTH;
	}

	@Override
	public double getFacing() {
		return facing;
	}

	@Override
	public boolean equals(Object other) {

		boolean result = false;

		if (other instanceof Hatch) {
			Hatch otherHatch = (Hatch) other;

			if ((xLoc == otherHatch.getXLocation()) && (yLoc == otherHatch.getYLocation())
					&& (facing == otherHatch.getFacing())) {
				result = true;
			}
		}

		return result;
	}

	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		building = null;
		connector = null;
	}
}