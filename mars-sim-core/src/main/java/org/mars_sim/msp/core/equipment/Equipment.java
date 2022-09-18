/*
 * Mars Simulation Project
 * Equipment.java
 * @date 2021-10-10
 * @author Scott Davis
 */
package org.mars_sim.msp.core.equipment;

import java.util.ArrayList;
import java.util.Collection;

import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.location.LocationStateType;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.manufacture.Salvagable;
import org.mars_sim.msp.core.manufacture.SalvageInfo;
import org.mars_sim.msp.core.manufacture.SalvageProcessInfo;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.Maintenance;
import org.mars_sim.msp.core.person.ai.task.Repair;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Indoor;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * The Equipment class is an abstract class that represents a useful piece of
 * equipment, such as a EVA suit or a medpack.
 */
public abstract class Equipment extends Unit implements Indoor, Salvagable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/* default logger. */
	private static final SimLogger logger = SimLogger.getLogger(Equipment.class.getName());

	public static final int OXYGEN_ID = ResourceUtil.oxygenID;
	public static final int WATER_ID = ResourceUtil.waterID;
	public static final int CO2_ID = ResourceUtil.co2ID;

	// Data members.
	/** is this equipment being salvage. */
	private boolean isSalvaged;

	/** Unique identifier for the settlement that owns this equipment. */
	private int associatedSettlementID;
	/** The identifier for the last owner of this equipment. */
	private int lastOwner;

	/** The equipment type enum. */
	private final EquipmentType equipmentType;
	/** The SalvageInfo instance. */
	private SalvageInfo salvageInfo;

	/**
	 * Constructs an Equipment object
	 *
	 * @param name     the name of the unit
	 * @param type     the type of the unit
	 * @param settlement the unit's owner
	 */
	protected Equipment(String name, String type, Settlement settlement) {
		this(name, EquipmentType.convertName2Enum(type), type, settlement);
	}

	/**
	 * Constructs an Equipment object
	 *
	 * @param name     the name of the unit
	 * @param type     the type of the unit
	 * @param settlement the unit's owner
	 */
	protected Equipment(String name, EquipmentType eType, String type, Settlement settlement) {
		super(name, settlement.getCoordinates());

		// Initialize data members.
		this.equipmentType = eType;
		isSalvaged = false;
		salvageInfo = null;

		lastOwner = -1;

		// Gets the settlement id
		associatedSettlementID = settlement.getIdentifier();
	}

	/**
	 * Mass of Equipment is the base mass plus what every it is storing
	 */
	@Override
	public double getMass() {
		// Note stored mass may be have a different implementation in subclasses
		return getStoredMass() + getBaseMass();
	}

	/**
	 * Gets the total weight of the stored resources
	 *
	 * @return
	 */
	public abstract double getStoredMass();

	/**
     * Gets the total capacity of resource that this container can hold.
     *
     * @return total capacity (kg).
     */
	public double getCargoCapacity() {
		return ContainerUtil.getContainerCapacity(equipmentType);
	}

	/**
	 * Stores the resource
	 *
	 * @param resource
	 * @param quantity
	 * @return excess quantity that cannot be stored
	 */
	public abstract double storeAmountResource(int resource, double quantity);
		// Question: if a bag was filled with regolith and later was emptied out
		// should it be tagged for only regolith and NOT for another resource ?

	/**
	 * Retrieves the resource
	 *
	 * @param resource
	 * @param quantity
	 * @return quantity that cannot be retrieved
	 */
	public abstract double retrieveAmountResource(int resource, double quantity);

	/**
	 * Gets the capacity of a particular amount resource
	 *
	 * @param resource
	 * @return capacity
	 */
	public abstract double getAmountResourceCapacity(int resource);

	/**
	 * Gets the amount resource stored
	 *
	 * @param resource
	 * @return quantity
	 */
	public abstract double getAmountResourceStored(int resource);

	/**
	 * Is this equipment empty ?
	 *
	 * @param brandNew true if it needs to be brand new
	 * @return
	 */
	public abstract boolean isEmpty(boolean brandNew);

	/**
	 * Gets a collection of people affected by this entity.
	 *
	 * @return person collection
	 */
	public Collection<Person> getAffectedPeople() {
		Collection<Person> people = new ArrayList<>();

		if (lastOwner != -1) {
			people.add(unitManager.getPersonByID(lastOwner));
		}

		// Check all people.
		for (Person person : unitManager.getPeople()) {
			Task task = person.getMind().getTaskManager().getTask();

			// Add all people maintaining this equipment.
			if (task instanceof Maintenance) {
				if (((Maintenance) task).getEntity() == this) {
					if (!people.contains(person))
						people.add(person);
				}
			}

			// Add all people repairing this equipment.
			if (task instanceof Repair) {
				if (((Repair) task).getEntity() == this) {
					if (!people.contains(person))
						people.add(person);
				}
			}
		}

		return people;
	}

	/**
	 * Checks if the item is salvaged.
	 *
	 * @return true if salvaged.
	 */
	public boolean isSalvaged() {
		return isSalvaged;
	}

	/**
	 * Indicate the start of a salvage process on the item.
	 *
	 * @param info       the salvage process info.
	 * @param settlement the settlement where the salvage is taking place.
	 */
	public void startSalvage(SalvageProcessInfo info, int settlement) {
		salvageInfo = new SalvageInfo(this, info, settlement);
		isSalvaged = true;
	}

	/**
	 * Gets the salvage info.
	 *
	 * @return salvage info or null if item not salvaged.
	 */
	public SalvageInfo getSalvageInfo() {
		return salvageInfo;
	}

	/**
	 * Get vehicle the equipment is in, null if not in vehicle
	 *
	 * @return {@link Vehicle} the equipment's vehicle
	 */
	@Override
	public Vehicle getVehicle() {
		Unit container = getContainerUnit();
		if (container.getUnitType() == UnitType.VEHICLE)
			return (Vehicle) container;
		if (container.getContainerUnit().getUnitType() == UnitType.VEHICLE)
			return (Vehicle) (container.getContainerUnit());

		return null;
	}

	/**
	 * Is the equipment's outside of a settlement but within its vicinity
	 *
	 * @return true if the equipment's is just right outside of a settlement
	 */
	public boolean isRightOutsideSettlement() {
        return LocationStateType.WITHIN_SETTLEMENT_VICINITY == currentStateType;
    }

	/**
	 * Sets the last owner of this equipment
	 *
	 * @param unit
	 */
	public void setLastOwner(Unit unit) {
		if (unit != null) {
			 if (unit.getIdentifier() != lastOwner)
				 lastOwner = unit.getIdentifier();
		}
		else
			lastOwner = Unit.UNKNOWN_UNIT_ID;
	}

	/**
	 * Gets the last owner of this equipment
	 *
	 * @return
	 */
	public Person getLastOwner() {
		if (lastOwner != -1)
			return unitManager.getPersonByID(lastOwner);
		return null;
	}

	public int getLastOwnerID() {
		return lastOwner;
	}

	public EquipmentType getEquipmentType() {
		return equipmentType;
	}


	/**
	 * Gets the equipment's settlement, null if equipment is not at a settlement.
	 *
	 * @return {@link Settlement} the equipment's settlement
	 */
	@Override
	public Settlement getSettlement() {

		if (getContainerID() == 0)
			return null;

		Unit c = getContainerUnit();

		if (c == null)
			return null;
		
		if (c.getUnitType() == UnitType.SETTLEMENT) {
			return (Settlement) c;
		}

		if (c.getUnitType() == UnitType.PERSON || c.getUnitType() == UnitType.ROBOT) {
			return c.getSettlement();
		}

		if (isInVehicleInGarage()) {
			return ((Vehicle)c).getSettlement();
		}

		return null;
	}

	/**
	 * Gets the settlement the person is currently associated with.
	 *
	 * @return associated settlement or null if none.
	 */
	@Override
	public Settlement getAssociatedSettlement() {
		return unitManager.getSettlementByID(associatedSettlementID);
	}

	@Override
	public UnitType getUnitType() {
		return UnitType.EQUIPMENT;
	}

	public static String generateName(String baseName) {
		if (baseName == null) {
			throw new IllegalArgumentException("Must specify a baseName");
		}

		int number = unitManager.incrementTypeCount(baseName);
		return String.format("%s %03d", baseName, number);
	}

	/**
	 * Sets the unit's container unit.
	 *
	 * @param newContainer the unit to contain this unit.
	 */
	@Override
	public void setContainerUnit(Unit newContainer) {
		if (newContainer != null) {
			if (newContainer.equals(getContainerUnit())) {
				return;
			}
			// 1. Set Coordinates
			setCoordinates(newContainer.getCoordinates());
			// 2. Set LocationStateType
			updateEquipmentState(newContainer);
			// 3. Set containerID
			// Q: what to set for a deceased person ?
			setContainerID(newContainer.getIdentifier());
			// 4. Fire the container unit event
			fireUnitUpdate(UnitEventType.CONTAINER_UNIT_EVENT, newContainer);
		}
	}

	/**
	 * Updates the location state type of an equipment.
	 *
	 * @param newContainer
	 */
	public void updateEquipmentState(Unit newContainer) {
		if (newContainer == null) {
			currentStateType = LocationStateType.UNKNOWN;
			return;
		}

		currentStateType = getNewLocationState(newContainer);
	}

	/**
	 * Gets the location state type based on the type of the new container unit.
	 *
	 * @param newContainer
	 * @return {@link LocationStateType}
	 */
	@Override
	public LocationStateType getNewLocationState(Unit newContainer) {

		if (newContainer.getUnitType() == UnitType.SETTLEMENT)
			return LocationStateType.INSIDE_SETTLEMENT;

		if (newContainer.getUnitType() == UnitType.BUILDING)
			return LocationStateType.INSIDE_SETTLEMENT;

		if (newContainer.getUnitType() == UnitType.VEHICLE)
			return LocationStateType.INSIDE_VEHICLE;

		if (newContainer.getUnitType() == UnitType.CONSTRUCTION)
			return LocationStateType.WITHIN_SETTLEMENT_VICINITY;

		if (newContainer.getUnitType() == UnitType.PERSON)
			return LocationStateType.ON_PERSON_OR_ROBOT;

		if (newContainer.getUnitType() == UnitType.PLANET)
			return LocationStateType.MARS_SURFACE;

		return null;
	}

	/**
	 * Is this unit inside a settlement ?
	 *
	 * @return true if the unit is inside a settlement
	 */
	@Override
	public boolean isInSettlement() {

		if (containerID == MARS_SURFACE_UNIT_ID)
			return false;

		// if the unit is in a settlement
		if (LocationStateType.INSIDE_SETTLEMENT == currentStateType)
			return true;

		if (LocationStateType.ON_PERSON_OR_ROBOT == currentStateType)
			return getContainerUnit().isInSettlement();

		if (LocationStateType.INSIDE_VEHICLE == currentStateType) {
			// if the vehicle is parked in a garage
			if (LocationStateType.INSIDE_SETTLEMENT == ((Vehicle)getContainerUnit()).getLocationStateType()) {
				return true;
			}
		}

		if (getContainerUnit().getUnitType() == UnitType.PERSON) {
			// if the unit is on a person
			return ((Person)getContainerUnit()).isInSettlement();
		}

		if (getContainerUnit().getUnitType() == UnitType.ROBOT) {
			// if the unit is on a robot
			return ((Robot)getContainerUnit()).isInSettlement();
		}

		// Note: may consider the scenario of this unit
		// being carried in by another person or a robot
//		if (LocationStateType.ON_PERSON_OR_ROBOT == currentStateType)
//			return getContainerUnit().isInSettlement();

		return false;
	}

	/**
	 * Transfers the unit from one owner to another owner.
	 *
	 * @param origin {@link Unit} the original container unit
	 * @param destination {@link Unit} the destination container unit
	 */
	public boolean transfer(Unit destination) {
		boolean transferred = false;
		Unit cu = getContainerUnit();

		if (cu.getUnitType() == UnitType.SETTLEMENT) {
			transferred = ((Settlement)cu).removeEquipment(this);
		}
		else if (cu.getUnitType() == UnitType.PLANET) {
			// do nothing. mars surface currently doesn't track equipment
			transferred = true;
		}
		else {
			transferred = ((EquipmentOwner)cu).removeEquipment(this);
		}

		if (transferred) {

			if (destination.getUnitType() == UnitType.SETTLEMENT) {
				transferred = ((Settlement)destination).addEquipment(this);
			}
			else if (destination.getUnitType() == UnitType.PLANET) {
				// do nothing. mars surface currently doesn't track equipment
				transferred = true;
			}
			else {
				transferred = ((EquipmentOwner)destination).addEquipment(this);
			}

			if (!transferred) {
				logger.warning(this, "Could not be transferred from '"
						+ cu + " to '" 
						+ destination + "'.");
				// NOTE: need to revert back the storage action
			}
			else {
				// Set the new container unit (which will internally set the container unit id)
				setContainerUnit(destination);
				// Fire the unit event type
				getContainerUnit().fireUnitUpdate(UnitEventType.INVENTORY_STORING_UNIT_EVENT, this);
				// Fire the unit event type
				getContainerUnit().fireUnitUpdate(UnitEventType.INVENTORY_RETRIEVING_UNIT_EVENT, this);
			}
		}
		else {
			logger.warning(this + " could not be retrieved from " + cu + ".");
			// NOTE: need to revert back the retrieval action
		}

		return transferred;
	}

	/**
	 * Compares if an object is the same as this equipment.
	 *
	 * @param obj
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (this.getClass() != obj.getClass()) return false;
		Equipment e = (Equipment) obj;
		return this.getIdentifier() == e.getIdentifier();
	}

	/**
	 * Gets the hash code for this object.
	 *
	 * @return hash code.
	 */
	@Override
	public int hashCode() {
		int hashCode = getEquipmentType().hashCode() ;
		hashCode *= getIdentifier();
		return hashCode % 64;
	}

	@Override
	public void destroy() {
		super.destroy();
		if (salvageInfo != null)
			salvageInfo.destroy();
		salvageInfo = null;
	}
}
