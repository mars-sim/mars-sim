/*
 * Mars Simulation Project
 * Equipment.java
 * @date 2023-06-25
 * @author Scott Davis
 */
package com.mars_sim.core.equipment;

import java.util.ArrayList;
import java.util.Collection;

import com.mars_sim.core.Unit;
import com.mars_sim.core.UnitEventType;
import com.mars_sim.core.UnitType;
import com.mars_sim.core.location.LocationStateType;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.manufacture.Salvagable;
import com.mars_sim.core.manufacture.SalvageInfo;
import com.mars_sim.core.manufacture.SalvageProcessInfo;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.task.MaintainBuilding;
import com.mars_sim.core.person.ai.task.Repair;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.Indoor;
import com.mars_sim.core.vehicle.Vehicle;

/**
 * The Equipment class is an abstract class that represents a useful piece of
 * equipment, such as a kit, a container, an EVA suit or a medpack.
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
	/** The identifier for the registered owner of this equipment. */
	private int registeredOwner;

	/** The equipment type enum. */
	private final EquipmentType equipmentType;
	/** The SalvageInfo instance. */
	private SalvageInfo salvageInfo;

	/**
	 * Constructs an Equipment object.
	 *
	 * @param name     the name of the unit
	 * @param type     the type of the unit
	 * @param settlement the unit's owner
	 */
	protected Equipment(String name, String type, Settlement settlement) {
		this(name, EquipmentType.convertName2Enum(type), type, settlement);
	}

	/**
	 * Constructs an Equipment object.
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

		registeredOwner = -1;

		// Gets the settlement id
		associatedSettlementID = settlement.getIdentifier();
	}

	/**
	 * Returns the mass of Equipment. The base mass plus the mass of whatever it is carrying.
	 */
	@Override
	public double getMass() {
		// Note stored mass may be have a different implementation in subclasses
		return getStoredMass() + getBaseMass();
	}

	/**
	 * Gets the total weight of the stored resources.
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
	 * Stores the resource.
	 *
	 * @param resource
	 * @param quantity
	 * @return excess quantity that cannot be stored
	 */
	public abstract double storeAmountResource(int resource, double quantity);
		// Question: if a bag was filled with regolith and later was emptied out
		// should it be tagged for only regolith and NOT for another resource ?

	/**
	 * Retrieves the resource.
	 *
	 * @param resource
	 * @param quantity
	 * @return quantity that cannot be retrieved
	 */
	public abstract double retrieveAmountResource(int resource, double quantity);

	/**
	 * Gets the capacity of a particular amount resource.
	 *
	 * @param resource
	 * @return capacity
	 */
	public abstract double getAmountResourceCapacity(int resource);

	/**
	 * Gets the amount resource stored.
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

		if (registeredOwner != -1) {
			people.add(unitManager.getPersonByID(registeredOwner));
		}

		// Check all people.
		for (Person person : unitManager.getPeople()) {
			Task task = person.getMind().getTaskManager().getTask();

			// Add all people maintaining this equipment.
			if (task instanceof MaintainBuilding maintain) {
				if (maintain.getEntity() == this) {
					if (!people.contains(person))
						people.add(person);
				}
			}

			// Add all people repairing this equipment.
			if (task instanceof Repair repair) {
				if (repair.getEntity() == this) {
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
	 * Indicates the start of a salvage process on the item.
	 *
	 * @param info       the salvage process info.
	 * @param settlement the settlement where the salvage is taking place.
	 */
	public void startSalvage(SalvageProcessInfo info, int settlement) {
		salvageInfo = new SalvageInfo(this, info, settlement, masterClock.getMarsTime());
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
	 * Gets vehicle the equipment is in, null if not in vehicle.
	 *
	 * @return {@link Vehicle} the equipment's vehicle
	 */
	@Override
	public Vehicle getVehicle() {
		Unit container = getContainerUnit();
		if (container instanceof Vehicle v)
			return v;
		if (container.getContainerUnit() instanceof Vehicle v)
			return v;

		return null;
	}

	/**
	 * Is the equipment's outside of a settlement but within its vicinity ?
	 *
	 * @return true if the equipment's is just right outside of a settlement
	 */
	public boolean isRightOutsideSettlement() {
        return LocationStateType.SETTLEMENT_VICINITY == currentStateType;
    }

	/**
	 * Sets the registered owner of this equipment.
	 *
	 * @param person
	 */
	public void setRegisteredOwner(Person person) {
		if (person != null) {
			 if (person.getIdentifier() != registeredOwner)
				 registeredOwner = person.getIdentifier();
		}
		else
			registeredOwner = Unit.UNKNOWN_UNIT_ID;
	}

	/**
	 * Gets the registered owner of this equipment.
	 *
	 * @return
	 */
	public Person getRegisteredOwner() {
		if (registeredOwner != -1)
			return unitManager.getPersonByID(registeredOwner);
		return null;
	}

	/**
	 * Gets the registered owner's id.
	 * 
	 * @return
	 */
	public int getRegisteredOwnerID() {
		return registeredOwner;
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
	 * @return Was a changed applied
	 */
	boolean setContainerUnit(Unit newContainer) {
		if (newContainer != null) {
			Unit cu = getContainerUnit();
			
			if (newContainer.equals(cu)) {
				return false;
			}

			// 1. Set Coordinates
			if (newContainer.getUnitType() == UnitType.MARS) {
				// Since it's on the surface of Mars,
				// First set its initial location to its old parent's location as it's leaving its parent.
				// Later it may move around and updates its coordinates by itself
				setCoordinates(cu.getCoordinates());
			}
			else {
				// Null its coordinates since it's now slaved after its parent
				setCoordinates(newContainer.getCoordinates());
			}
			
			// 2. Set LocationStateType
			if (cu != null) { 
				// 2a. If the previous cu is a settlement
				//     and this person's new cu is mars surface,
				//     then location state is within settlement vicinity
				if (cu.getUnitType() == UnitType.SETTLEMENT
					&& newContainer.getUnitType() == UnitType.MARS) {
						currentStateType = LocationStateType.SETTLEMENT_VICINITY;
				}	
				// 2b. If the previous cu is a vehicle
				//     and this vehicle is in within settlement vicinity
				//     and this person's new cu is mars surface,
				//     then location state is within settlement vicinity
				else if ((cu.getUnitType() == UnitType.VEHICLE
						|| cu.getUnitType() == UnitType.PERSON)
						&& cu.isInSettlementVicinity()
						&& newContainer.getUnitType() == UnitType.MARS) {
							currentStateType = LocationStateType.SETTLEMENT_VICINITY;
				}
				else {
					updateEquipmentState(newContainer);
				}
			}
			else {
				updateEquipmentState(newContainer);
			}

			// 3. Set containerID
			setContainerID(newContainer.getIdentifier());
			
			// 4. Fire the container unit event
			fireUnitUpdate(UnitEventType.CONTAINER_UNIT_EVENT, newContainer);
		}
		return true;
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
	public LocationStateType getNewLocationState(Unit newContainer) {

		if (newContainer.getUnitType() == UnitType.SETTLEMENT)
			return LocationStateType.INSIDE_SETTLEMENT;

		if (newContainer.getUnitType() == UnitType.BUILDING)
			return LocationStateType.INSIDE_SETTLEMENT;

		if (newContainer.getUnitType() == UnitType.VEHICLE)
			return LocationStateType.INSIDE_VEHICLE;

		if (newContainer.getUnitType() == UnitType.CONSTRUCTION)
			return LocationStateType.MARS_SURFACE;

		if (newContainer.getUnitType() == UnitType.PERSON)
			return LocationStateType.ON_PERSON_OR_ROBOT;

		if (newContainer.getUnitType() == UnitType.MARS)
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

		if (containerID <= MARS_SURFACE_UNIT_ID)
			return false;

		// if the unit is in a settlement
		if (LocationStateType.INSIDE_SETTLEMENT == currentStateType)
			return true;

		if (LocationStateType.ON_PERSON_OR_ROBOT == currentStateType)
			return getContainerUnit().isInSettlement();

		// if the vehicle is parked in a garage
		if ((LocationStateType.INSIDE_VEHICLE == currentStateType) 
				&& (LocationStateType.INSIDE_SETTLEMENT == ((Vehicle)getContainerUnit()).getLocationStateType())) {
			return true;
		}

		if (getContainerUnit().getUnitType() == UnitType.PERSON) {
			// if the unit is on a person
			return ((Person)getContainerUnit()).isInSettlement();
		}

		if (getContainerUnit().getUnitType() == UnitType.ROBOT) {
			// if the unit is on a robot
			return ((Robot)getContainerUnit()).isInSettlement();
		}

		return false;
	}

	/**
	 * Transfers the unit from one owner to another owner.
	 *
	 * @param origin {@link Unit} the original container unit
	 * @param destination {@link Unit} the destination container unit
	 */
	public boolean transfer(Unit destination) {
		boolean canRetrieve = false;
		boolean canStore = false;
		Unit cu = getContainerUnit();

		if (cu instanceof EquipmentOwner deo) {
			canRetrieve = deo.removeEquipment(this);
		}
		else {
			// do nothing. mars surface currently doesn't track equipment
			canRetrieve = true;
		}

		if (!canRetrieve) {
				logger.warning(this, 60_000L, "Could not be retrieved/transferred from '"
						+ cu + "'.");
		}
		else {	
			if (destination.getUnitType() == UnitType.BUILDING) {
				// Turn a building destination to a settlement to avoid 
				// casting issue with making containerUnit a building instance
				destination = ((Building)destination).getSettlement();
			}

			if (destination instanceof EquipmentOwner eo) {
				canStore = eo.addEquipment(this);
			}
			else {
				// do nothing. mars surface currently doesn't track equipment
				canStore = true;
			}

			if (!canStore) {
				logger.warning(this, 60_000L, "Could not be stored into '"
						+ destination + "'.");
				
				// Need to go back the original container
				boolean canStoreBack = ((EquipmentOwner)cu).addEquipment(this);
				if (canStoreBack) {
					logger.warning(this, 60_000L, "Just stored back into '"
							+ cu + "'.");
				}
				else {
					logger.warning(this, 60_000L, "Could not be stored back into '"
							+ cu + "'.");
				}
				
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
		
		return canRetrieve && canStore;
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
		int hashCode = getIdentifier();
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
