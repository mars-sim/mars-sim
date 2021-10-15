/*
 * Mars Simulation Project
 * Equipment.java
 * @date 2021-10-10
 * @author Scott Davis
 */
package org.mars_sim.msp.core.equipment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.data.MicroInventory;
import org.mars_sim.msp.core.location.LocationStateType;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.manufacture.Salvagable;
import org.mars_sim.msp.core.manufacture.SalvageInfo;
import org.mars_sim.msp.core.manufacture.SalvageProcessInfo;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.Maintenance;
import org.mars_sim.msp.core.person.ai.task.Repair;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ItemResource;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.Indoor;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.Temporal;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * The Equipment class is an abstract class that represents a useful piece of
 * equipment, such as a EVA suit or a medpack.
 */
public abstract class Equipment extends Unit implements Indoor, Salvagable, Temporal {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/* default logger. */
	private static final SimLogger logger = SimLogger.getLogger(Equipment.class.getName());

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
	/** The MicroInventory instance. */
	protected MicroInventory microInventory;

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
		
		microInventory = new MicroInventory(this);
	}

	@Override
	public boolean timePassing(ClockPulse pulse) {
		// By default Equipment are passive objects
		return false;
	}

	/**
	 * Gets a list of supported resources
	 * 
	 * @return a list of resource ids
	 */
	public Set<Integer> getResourceIDs() {
		return microInventory.getResourcesStored();
	}
	
	/**
	 * Gets all stored amount resources 
	 * 
	 * @return
	 */
	public Set<AmountResource> getAllAmountResourcesStored() {
		return microInventory.getAllAmountResourcesStored();
	}
	
	/**
	 * Gets all stored item resources 
	 * 
	 * @return
	 */
	public Set<ItemResource> getAllItemResourcesStored() {
		return microInventory.getAllItemResourcesStored();
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
	public double getStoredMass() {
		return microInventory.getStoredMass();
	}
	
	/**
     * Gets the total capacity of resource that this container can hold.
     * 
     * @return total capacity (kg).
     */
	public double getTotalCapacity() {
		return ContainerUtil.getContainerCapacity(equipmentType);
	}
	
	/**
     * Gets the capacity of this resource that this container can hold.
     * @return capacity (kg).
     */
    public double getCapacity(int resource) {
        return microInventory.getCapacity(resource);
    }
	
	/**
	 * Sets the capacity of a particular resource
	 * 
	 * @param resource
	 * @param capacity
	 */
	public void setCapacity(int resource, double capacity) {
		microInventory.setCapacity(resource, capacity);
	}
	
	/**
	 * Is this resource supported ?
	 * 
	 * @param resource
	 * @return true if this resource is supported
	 */
	public boolean isResourceSupported(int resource) {
		return microInventory.isResourceSupported(resource);
	}
	
	/**
	 * Stores the resource
	 * 
	 * @param resource
	 * @param quantity
	 * @return excess quantity that cannot be stored
	 */
	public double storeAmountResource(int resource, double quantity) {
		// Question: if a bag was filled with regolith and later was emptied out
		// should it be tagged for only regolith and NOT for another resource ?
		if (!isResourceSupported(resource)) {
			// Allocate the capacity to this new resource
			microInventory.setCapacity(resource, getTotalCapacity());
		}
		
		return microInventory.storeAmountResource(resource, quantity);
	}
	
	/**
	 * Retrieves the resource 
	 * 
	 * @param resource
	 * @param quantity
	 * @return quantity that cannot be retrieved
	 */
	public double retrieveAmountResource(int resource, double quantity) {
		if (isResourceSupported(resource)) {
			return microInventory.retrieveAmountResource(resource, quantity);
		}
		
		else {
			String name = ResourceUtil.findAmountResourceName(resource);
			logger.warning(this, "No such resource. Cannot retrieve " 
					+ Math.round(quantity* 1_000.0)/1_000.0 + " kg "+ name + ".");
			return quantity;
		}
	}
	
	/**
	 * Gets the capacity of a particular amount resource
	 * 
	 * @param resource
	 * @return capacity
	 */
	public double getAmountResourceCapacity(int resource) {
		// Note: this method is different from 
		// EVASuit's getAmountResourceRemainingCapacity 
		if (isResourceSupported(resource)) {
			return getTotalCapacity();
		}
		return 0;
	}
	
	/**
	 * Obtains the remaining storage space of a particular amount resource
	 * 
	 * @param resource
	 * @return quantity
	 */
	public double getAmountResourceRemainingCapacity(int resource) {
		return microInventory.getAmountResourceRemainingCapacity(resource);
	}
	
	
	/**
	 * Obtains the remaining storage quantity of a particular item resource
	 * 
	 * @param resource
	 * @return quantity
	 */
	public int getItemResourceRemainingCapacity(int resource) {
		return microInventory.getItemResourceRemainingCapacity(resource);
	}
	
	/**
	 * Gets the amount resource stored
	 * 
	 * @param resource
	 * @return quantity
	 */
	public double getAmountResourceStored(int resource) {
		return microInventory.getAmountResourceStored(resource);
	}
    
	/**
	 * Gets the item resource stored
	 * 
	 * @param resource
	 * @return quantity
	 */
	public double getItemResourceStored(int resource) {
		return microInventory.getItemResourceStored(resource);
	}
			
	/**
	 * Is this suit empty ? 
	 * 
	 * @param brandNew true if it needs to be brand new
	 * @return
	 */
	public boolean isEmpty(boolean brandNew) {
		if (brandNew) {
			return (getLastOwnerID() == -1);
		}

		return microInventory.isEmpty();
	}	

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
	 * Get the equipment's settlement, null if equipment is not at a settlement
	 *
	 * @return {@link Settlement} the equipment's settlement
	 */
	public Settlement getSettlement() {

		if (getContainerID() == 0)
			return null;
		
		Unit c = getContainerUnit();

		if (c instanceof Settlement) {
			return (Settlement) c;
		}

		else if (c instanceof Person) {
			return c.getSettlement();
		}
		else if (c instanceof Vehicle) {
			Building b = BuildingManager.getBuilding((Vehicle) getContainerUnit());
			if (b != null)
				// still inside the garage
				return b.getSettlement();
		}

		return null;
	}

	/**
	 * Get vehicle the equipment is in, null if not in vehicle
	 *
	 * @return {@link Vehicle} the equipment's vehicle
	 */
	public Vehicle getVehicle() {
		Unit container = getContainerUnit();
		if (container instanceof Vehicle)
			return (Vehicle) container;
		if (container.getContainerUnit() instanceof Vehicle)
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
	 * Clean this container for future use
	 */
	public void clean() {
		microInventory.clean();
	}

	/**
	 * Compares if an object is the same as this equipment 
	 * 
	 * @param obj
	 */
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
	public int hashCode() {
		int hashCode = getNickName().hashCode();
		hashCode *= getEquipmentType().hashCode() ;
		hashCode *= getIdentifier();
		return hashCode;
	}
	
	@Override
	public void destroy() {
		super.destroy();
		if (salvageInfo != null)
			salvageInfo.destroy();
		salvageInfo = null;
	}

}
