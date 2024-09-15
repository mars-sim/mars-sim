/*
 * Mars Simulation Project
 * MobileUnit.java
 * @date 2024-09-15
 * @author Barry Evans
 */
package com.mars_sim.core.unit;

import com.mars_sim.core.Unit;
import com.mars_sim.core.UnitEventType;
import com.mars_sim.core.UnitType;
import com.mars_sim.core.location.LocationStateType;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.vehicle.Vehicle;

/**
 * Represent a Unit that can move around the surface
 */
public abstract class MobileUnit extends Unit  {
    
    private Settlement owner;
    private LocalPosition localPosn = LocalPosition.DEFAULT_POSITION;
    private int currentBuildingInt;
    private Coordinates surfacePosn;

    /**
	 * Constructor.
	 * 
	 * @param name the name of the unit
	 * @param owner the unit's location
	 */
	protected MobileUnit(String name, Settlement owner) {
		super(name, owner.getCoordinates()); // TODO Unit constructor needs to drop coordinates from params
        this.surfacePosn = owner.getCoordinates();
        this.owner = owner;

        // TODO Place holder; once completed this can be removed
		setContainerID(owner.getIdentifier());
	}

	/**
	 * Gets the position at a settlement.
	 *
	 * @return distance (meters) from the settlement's center.
	 */
	public LocalPosition getPosition() {
		return localPosn;
	}

	/**
	 * Sets the settlement-wide position.
	 *
	 * @param position
	 */
	public void setPosition(LocalPosition position) {
		this.localPosn = position;
	}

    /**
	 * Computes the building the person is currently located at.
	 * Returns null if outside of a settlement.
	 *
	 * @return building
	 */
	@Override
	public Building getBuildingLocation() {
		if (currentBuildingInt == -1)
			return null;
		return unitManager.getBuildingByID(currentBuildingInt);
	}

	/**
	 * Computes the building the person is currently located at.
	 * Returns null if outside of a settlement.
	 *
	 * @return building
	 */
	public void setCurrentBuilding(Building building) {
		if (building == null) {
			currentBuildingInt = -1;
		}
		else {
			currentBuildingInt = building.getIdentifier();
		}
	}


	/**
	 * Gets the unit's location.
	 *
	 * @return the unit's location
	 */
	public Coordinates getCoordinates() {
		Unit cu = getContainerUnit();
		if (cu.getUnitType() == UnitType.MARS) {	
			// Since Mars surface has no coordinates, 
			// Get from its previously setting location
			return surfacePosn;
		}
		
		// Unless it's on Mars surface, get its container unit's coordinates
		return cu.getCoordinates();
	}

	/**
	 * Sets unit's location coordinates.
	 *
	 * @param newLocation the new location of the unit
	 */
	public void setCoordinates(Coordinates newLocation) {
        // TODO the null check can be removed once Unit stop managing coordinates
		if ((surfacePosn == null) || !surfacePosn.equals(newLocation)) {
			surfacePosn = newLocation;
			fireUnitUpdate(UnitEventType.LOCATION_EVENT, newLocation);
		}
	}
    
    /**
	 * This method assumes the Unit could be movable and change container. It identifies the
	 * appropriate container it can use. Ideally this method should be moved to a 
	 * new subclass called 'MovableUnit' that encapsulates some positioning methods 
	 * not applicable to Structures.
	 */
	public String getContext() {
		// Check vehicle first because could be in a Vehicle in a Settlement
		if (isInVehicle()) {
			return getVehicle().getChildContext();
		}
		else if (isInSettlement()) {
			var b = getBuildingLocation();
			if (b != null) {
				return b.getChildContext();
			}
			else {
				return getAssociatedSettlement().getName();
			}
		}
		else if (isOutside()) {
			return getCoordinates().getFormattedString();
		}
		else {
			return getContainerUnit().getName();
		}
	}

    /**
     * Settlement that is associated with this Unit.
     */
    @Override
    public Settlement getAssociatedSettlement() {
        return owner;
    }   
    
    /**
	 * Gets the settlement the Unit is at.
	 * Returns null if unit is not at a settlement.
	 *
	 * @return the settlement
	 */
	@Override
	public Settlement getSettlement() {

		if (getContainerID() <= Unit.MARS_SURFACE_UNIT_ID)
			return null;

		Unit c = getContainerUnit();

		if (c.getUnitType() == UnitType.SETTLEMENT) {
			return (Settlement) c;
		}

		if (c.getUnitType() == UnitType.VEHICLE) {
			// Will see if vehicle is inside a garage or not
			Vehicle v = (Vehicle) c;
			return (v.isInVehicleInGarage() ? v.getSettlement() : null);
		}

		if (c.getUnitType() == UnitType.BUILDING || c.getUnitType() == UnitType.PERSON
				|| c.getUnitType() == UnitType.ROBOT) {
			return c.getSettlement();
		}

		return null;
	}

    /**
	 * Is this unit inside a settlement ?
	 *
	 * @return true if the unit is inside a settlement
	 */
	@Override
	public boolean isInSettlement() {
		return (getSettlement() != null);
	}

    
	/**
	 * Gets vehicle person is in, null if person is not in vehicle.
	 *
	 * @return the person's vehicle
	 */
	@Override
	public Vehicle getVehicle() {
		if (getLocationStateType() == LocationStateType.INSIDE_VEHICLE) {
			return (Vehicle) getContainerUnit();
		}

		return null;
	}
    
	/**
	 * Is this unit inside a vehicle ?
	 *
	 * @return true if the unit is inside a vehicle
	 */
	public boolean isInVehicle() {
		if (LocationStateType.INSIDE_VEHICLE == currentStateType)
			return true;

		if (LocationStateType.ON_PERSON_OR_ROBOT == currentStateType)
			return getContainerUnit().isInVehicle();

		return false;
	}

    /**
	 * Updates the location state type of a person.
	 *
	 * @param newContainer
	 */
	protected void updateLocationState(Unit newContainer) {
		if (newContainer == null) {
			currentStateType = LocationStateType.UNKNOWN;
			return;
		}

		currentStateType = switch (newContainer.getUnitType()) {
            case SETTLEMENT -> LocationStateType.INSIDE_SETTLEMENT;
            case BUILDING -> LocationStateType.INSIDE_SETTLEMENT;
            case VEHICLE -> LocationStateType.INSIDE_VEHICLE;
            case CONSTRUCTION -> LocationStateType.MARS_SURFACE;
            case PERSON -> LocationStateType.ON_PERSON_OR_ROBOT;
            case MARS -> LocationStateType.MARS_SURFACE;
            default -> null;
        };
	}
}
