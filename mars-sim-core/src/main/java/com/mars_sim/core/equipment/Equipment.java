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
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.task.MaintainBuilding;
import com.mars_sim.core.location.LocationStateType;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.malfunction.task.Repair;
import com.mars_sim.core.manufacture.Salvagable;
import com.mars_sim.core.manufacture.SalvageInfo;
import com.mars_sim.core.manufacture.SalvageProcessInfo;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.unit.AbstractMobileUnit;
import com.mars_sim.core.unit.MobileUnit;
import com.mars_sim.core.unit.UnitHolder;
import com.mars_sim.core.vehicle.Vehicle;

/**
 * The Equipment class is an abstract class that represents a useful piece of
 * equipment, such as a kit, a container, an EVA suit or a medpack.
 */
public abstract class Equipment extends AbstractMobileUnit implements Salvagable {

	/** Default serial id. */
	private static final long serialVersionUID = 1L;
	/** Default logger. */
	private static final SimLogger logger = SimLogger.getLogger(Equipment.class.getName());

	// Data members.
	/** is this equipment being salvage. */
	private boolean isSalvaged;


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
		super(name, settlement);

		// Initialize data members.
		this.equipmentType = eType;
		isSalvaged = false;
		salvageInfo = null;

		registeredOwner = -1;
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
	public abstract double getSpecificCapacity(int resource);

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
		var container = getContainerUnit();
		if (container instanceof Vehicle v)
			return v;
		if ((container instanceof MobileUnit mu)
				&& mu.getContainerUnit() instanceof Vehicle vu)
			return vu;

		return null;
	}

	/**
	 * Is the equipment's outside of a settlement but within its vicinity ?
	 *
	 * @return true if the equipment's is just right outside of a settlement
	 */
	public boolean isRightOutsideSettlement() {
        return LocationStateType.SETTLEMENT_VICINITY == getLocationStateType();
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
	
	/**
	 * Gets the equipment type.
	 * 
	 * @return
	 */
	public EquipmentType getEquipmentType() {
		return equipmentType;
	}

	public static String generateName(String baseName) {
		if (baseName == null) {
			throw new IllegalArgumentException("Must specify a baseName");
		}

		int number = unitManager.incrementTypeCount(baseName);
		return String.format("%s %03d", baseName, number);
	}

	/**
	 * Transfers the unit from one owner to another owner.
	 *
	 * @param origin {@link Unit} the original container unit
	 * @param destination {@link Unit} the destination container unit
	 */
	@Override
	public boolean transfer(UnitHolder destination) {
		boolean canRetrieve = false;
		boolean canStore = false;
		var cu = getContainerUnit();
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
			if (destination instanceof Building b) {
				// Turn a building destination to a settlement to avoid 
				// casting issue with making containerUnit a building instance
				destination = b.getAssociatedSettlement();
			}

			if (destination instanceof EquipmentOwner eo) {
				canStore = eo.addEquipment(this);
			}
			else {
				// do nothing. mars surface currently doesn't track equipment
				canStore = true;
			}

			if (cu != null && !canStore) {
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
				setContainer(destination, defaultLocationState(destination));
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
