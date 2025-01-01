package com.mars_sim.core.unit;

import com.mars_sim.core.location.LocationStateType;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.map.location.SurfacePOI;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.vehicle.Vehicle;

/**
 * Represents an entity that can be mobile.
 */
public interface MobileUnit extends SurfacePOI {

	/**
	 * Where is this mobile unit residing
	 * @return
	 */
	public UnitHolder getContainerUnit();

	/**
	 * Transfer this mobile unit to a new container
	 * @param destination
	 * @return
	 */
	public boolean transfer(UnitHolder destination);

	/**
	 * Is the worker in a vehicle ?
	 *
	 * @return true if the worker in a vehicle
	 */
	public boolean isInVehicle();

	/**
	 * Gets vehicle worker is in, null if member is not in vehicle/
	 *
	 * @return the worker's vehicle
	 */
	public Vehicle getVehicle();
    	
	/**
	 * Is the worker in a settlement ?
	 *
	 * @return true if the worker in a settlement
	 */
	public boolean isInSettlement();
    
	/**
	 * Gets the current Settlement of the worker; may be different from the associated Settlement.
	 * 
	 * @return
	 */
	public Settlement getSettlement();

    /**
	 * Gets the Worker's building.
	 * 
	 * @return building
	 */
	public Building getBuildingLocation();
	
	/**
	 * Sets the building the worker is located at.
	 *
	 * @param position
	 */
	public void setCurrentBuilding(Building building);

	/**
	 * Gets the Worker's position within the Settlement/Vehicle.
	 * 
	 * @return
	 */
	public LocalPosition getPosition();
	
	/**
	 * Sets the worker's position at a settlement.
	 *
	 * @param position
	 */
	public void setPosition(LocalPosition position);

	/**
	 * Is the Worker outside ?
	 *
	 * @return true if the worker is on the MarsSurface
	 */
	public boolean isOutside();

	/**
	 * Get the definitive assessment of the physical state os this mobile unit.
	 * @return
	 */
	public LocationStateType getLocationStateType();
}
