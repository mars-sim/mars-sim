/*
 * Mars Simulation Project
 * MobileUnit.java
 * @date 2024-09-15
 * @author Barry Evans
 */
package com.mars_sim.core.unit;

import com.mars_sim.core.Unit;
import com.mars_sim.core.UnitEventType;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.environment.MarsSurface;
import com.mars_sim.core.location.LocationStateType;
import com.mars_sim.core.location.LocationTag;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.map.location.SurfacePOI;
import com.mars_sim.core.mission.Construction;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.vehicle.Vehicle;

/**
 * Represents a Unit that can move around the surface.
 */
@SuppressWarnings("serial")
public abstract class AbstractMobileUnit extends Unit
	implements MobileUnit  {

	private Settlement owner;
    private LocalPosition localPosn = LocalPosition.DEFAULT_POSITION;
    private int currentBuildingInt;
	private double baseMass = 0D;
	private LocationTag tag;
	private LocationStateType locnState;
	private Coordinates location;
	private UnitHolder container;

    /**
	 * Constructor.
	 * 
	 * @param name the name of the unit
	 * @param owner the unit's location
	 */
	protected AbstractMobileUnit(String name, Settlement owner) {
		super(name); 
        this.owner = owner;
		this.location = owner.getCoordinates();
		this.tag = new LocationTag(this);
		setContainer(owner, LocationStateType.INSIDE_SETTLEMENT);
	}

	/**
	 * Gets the container of this mobile unit.
	 * 
	 * @return Should never be null
	 */
	@Override
	public UnitHolder getContainerUnit() {
		return container;
	}

	/**
	 * Sets the container of this mobile unit.
	 * 
	 * @param destination New destination of container
	 * @param newState 
	 */
	protected void setContainer(UnitHolder destination, LocationStateType newState) {
		this.locnState = newState;
		container = destination;
	}

	/**
	 * Is this unit outside on the surface of Mars, including wearing an EVA Suit
	 * and being just right outside in a settlement/building/vehicle vicinity
	 * Note: being inside a vehicle (that's on a mission outside) doesn't count being outside
	 *
	 * @return true if the unit is outside
	 */
	public boolean isOutside() {
		if (LocationStateType.MARS_SURFACE == locnState
				|| LocationStateType.SETTLEMENT_VICINITY == locnState
				|| LocationStateType.VEHICLE_VICINITY == locnState)
			return true;

		if (LocationStateType.ON_PERSON_OR_ROBOT == locnState)
			return ((Worker) getContainerUnit()).isOutside();

		return false;
	}

	
	/**
	 * Is this unit inside an environmentally enclosed breathable living space such
	 * as inside a settlement or a vehicle (NOT including in an EVA Suit) ?
	 *
	 * @return true if the unit is inside a breathable environment
	 */
	public boolean isInside() {
		if (LocationStateType.INSIDE_SETTLEMENT == locnState
				|| LocationStateType.INSIDE_VEHICLE == locnState)
			return true;

		if (LocationStateType.ON_PERSON_OR_ROBOT == locnState)
			return ((AbstractMobileUnit)getContainerUnit()).isInside();

		return false;
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
		fireUnitUpdate(UnitEventType.LOCAL_POSITION_EVENT, position);
	}

    /**
	 * Computes the building the unit is currently located at. Accounts for the mobile unit
	 * being in another MobileUnit
	 * Returns null if outside of a settlement.
	 *
	 * @return building
	 */
	@Override
	public Building getBuildingLocation() {
		if (getContainerUnit() instanceof MobileUnit mu) {
			return mu.getBuildingLocation();
		}
	
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
	@Override
	public Coordinates getCoordinates() {
		var cu = getContainerUnit();
		if (cu instanceof SurfacePOI mu) {
			// Inside a container that is on the surface
			return mu.getCoordinates();
		}

		// Since Mars surface has no coordinates, 
		// Get from its previously setting location
		return location;
	}

	/**
	 * Sets unit's location coordinates.
	 *
	 * @param newLocation the new location of the unit
	 */
	public void setCoordinates(Coordinates newLocation) {
		if (!location.equals(newLocation)) {
			location = newLocation;
			fireUnitUpdate(UnitEventType.COORDINATE_EVENT, newLocation);
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
				return b.getContext();
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

		var c = getContainerUnit();

		if (c instanceof Settlement s) {
			return s;
		}

		else if (c instanceof Vehicle v) {
			// Will see if vehicle is inside a garage or not
			return (v.isInVehicleInGarage() ? v.getSettlement() : null);
		}

		else if (c instanceof FixedUnit b) {
			return b.getAssociatedSettlement();
		}
		else if (c instanceof AbstractMobileUnit m) {
			return m.getSettlement();
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
	 * Is this unit in the vicinity of a settlement ?
	 *
	 * @return true if the unit is inside a settlement
	 */
	public boolean isInSettlementVicinity() {
		return tag.isInSettlementVicinity();
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
	@Override
	public boolean isInVehicle() {
		if (LocationStateType.INSIDE_VEHICLE == locnState)
			return true;

		if (LocationStateType.ON_PERSON_OR_ROBOT == locnState)
			return ((Worker)getContainerUnit()).isInVehicle();

		return false;
	}

	/**
	 * Is this unit inside a vehicle in a garage ?
	 *
	 * @return true if the unit is in a vehicle inside a garage
	 */
	public boolean isInVehicleInGarage() {
		var cu = getContainerUnit();
		if (cu instanceof Vehicle v) {
			// still inside the garage
			return v.isInGarage();
		}
		return false;
	}

	/**
	 * Gets the location tag which refines the vicinity of the Unit.
	 * 
	 * @return
	 */
	public LocationTag getLocationTag() {
		return tag;
	}
	
	/**
	 * Gets the current location state for this mobile unit. It will be a refinement of the container.
	 */
	public LocationStateType getLocationStateType() {
		return locnState;
	}

	public void setLocationStateType(LocationStateType locationStateType) {
		locnState = locationStateType;
	}

    /**
	 * Returns the default Location state for a new container.
	 *
	 * @param newContainer
	 */
	protected static LocationStateType defaultLocationState(Object newContainer) {
		return switch (newContainer) {
            case Settlement s -> LocationStateType.INSIDE_SETTLEMENT;
            case Building b -> LocationStateType.INSIDE_SETTLEMENT;
            case Vehicle v -> LocationStateType.INSIDE_VEHICLE;
            case Construction c -> LocationStateType.MARS_SURFACE;
            case Person p -> LocationStateType.ON_PERSON_OR_ROBOT;
            case MarsSurface m -> LocationStateType.MARS_SURFACE;
            default -> null;
        };
	}

	/**
	 * Gets the unit's mass including inventory mass.
	 * This method will be overridden by those inheriting this Unit.
	 * By default it returns the base mass.
	 *
	 * @return mass of unit
	 */
	public double getMass() {
		return getBaseMass();
	}

	/**
	 * Sets the unit's base mass.
	 *
	 * @param base mass (kg)
	 */
	protected void setBaseMass(double baseMass) {
		this.baseMass = baseMass;
	}

	/**
	 * Gets the base mass of the unit.
	 *
	 * @return base mass (kg)
	 */
	public double getBaseMass() {
		return baseMass;
	}
}
