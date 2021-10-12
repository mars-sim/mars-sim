/*
 * Mars Simulation Project
 * Equipment.java
 * @date 2021-10-10
 * @author Scott Davis
 */
package org.mars_sim.msp.core.equipment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.mars_sim.msp.core.resource.ItemResourceUtil;
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

	/** A list of resource id's. */
	private List<Integer> resourceIDs = new ArrayList<>();
	/** A list of Amount Resources. */
	private transient Map<Integer, AmountResource> resourceARs = new HashMap<>();
	/** A list of Amount Resources. */
	private transient Map<Integer, ItemResource> resourceIRs = new HashMap<>();
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
		super(name, settlement.getCoordinates());
		
		// Initialize data members.
		this.equipmentType = EquipmentType.convertName2Enum(type);
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
	public List<Integer> getResourceIDs() {
		return resourceIDs;
	}
	
	
	/**
	 * Gets the id of the resource
	 * 
	 * @return Amount Resource id
	 */
	public int getResource() {
		return (resourceIDs.isEmpty() ? -1 : resourceIDs.get(0));
	}
	
	/**
	 * Gets the AmountResource of a particular resource
	 * 
	 * @param resource
	 * @return Amount Resource
	 */
	public AmountResource getResourceAR(int resource) {
		return resourceARs.get(resource);
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
     * @return total capacity (kg).
     */
	public abstract double getTotalCapacity();
	
	/**
     * Gets the capacity of this resource that this container can hold.
     * @return capacity (kg).
     */
    public double getCapacity(int resource) {
        return getTotalCapacity();
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
		for (int r: resourceIDs) {
			if (r == resource)
				return true;
		}
		
		return false;
	}
	
	/**
	 * Gets the index of a particular resource
	 * 
	 * @param resource
	 * @return index
	 */
	private int getIndex(int resource) {
		if (resourceIDs != null && !resourceIDs.isEmpty()) {
			for (int r: resourceIDs) {
				if (r == resource)
					return resourceIDs.indexOf(r);
			}
		}
		
		return -1;
	}
	
	/**
	 * Stores the resource
	 * 
	 * @param resource
	 * @param quantity
	 * @return excess quantity that cannot be stored
	 */
	public double storeAmountResource(int resource, double quantity) {
		int index = getIndex(resource);
//		System.out.println("EVASuit index: " + index + " resource: " + resource + " quantity: " + quantity);
		// Question: if a bag was filled with regolith and later was emptied out
		// should it be tagged for only regolith and NOT for another resource ?
		// index = -1 means it's brand new
		if (index == -1) {
			resourceIDs.add(resource);
			resourceARs.put(resource, ResourceUtil.findAmountResource(resource));
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
		int index = getIndex(resource);
		if (resourceIDs.contains(resource)) {
			return microInventory.retrieveAmountResource(resource, quantity);
		}
		
		else if (index == -1) {
			String name = findAmountResourceName(resource);
			logger.warning(this, 10_000L, "Cannot retrieve " + quantity + " kg of " 
					+ name + ". Not being used for storing anything yet.");
			return 0;
		}

		else {
			String name = findAmountResourceName(resource);
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
		int index = getIndex(resource);
		
		if (index == -1 || resourceIDs.contains(resource)) {
			return getTotalCapacity();
		}
		else {
			return 0;
		}
	}
	
	/**
	 * Obtains the remaining storage space of a particular amount resource
	 * 
	 * @param resource
	 * @return quantity
	 */
	public double getAmountResourceRemainingCapacity(int resource) {
		int index = getIndex(resource);
		
		if (index == -1 || resourceIDs.contains(resource)) {	
			return microInventory.getAmountResourceRemainingCapacity(resource);
		}
		else {
			return 0;
		}
	}
	
	/**
	 * Gets the amount resource stored
	 * 
	 * @param resource
	 * @return quantity
	 */
	public double getAmountResourceStored(int resource) {
		if (resourceIDs.contains(resource)) {
			return microInventory.getAmountResourceStored(resource);
		}
		else {
			return 0;
		}
	}
    
	/**
	 * Is this equipment empty ?
	 * 
	 * @param resource
	 * @return
	 */
	public boolean isEmpty(int resource) {
		int index = getIndex(resource);
		if (index == -1 || (resourceIDs.contains(resource) && microInventory.isEmpty(resource)))
			return true;
		
		return false;
	}


	/**
	 * Is this suit empty ? 
	 * 
	 * @param brandNew true if it needs to be brand new
	 * @return
	 */
	public boolean isEmpty(boolean brandNew) {
		if (brandNew) {
			return isBrandNew();
		}

		return microInventory.isEmpty();
	}	

	public boolean isBrandNew() {
		if (getLastOwnerID() == -1)
			return true;
		return false;
	}

	public boolean isUsed() {
		return !isBrandNew();
	}

	public boolean hasContent() {
		return !microInventory.isEmpty();
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
	
	public String getType() {
		return equipmentType.getName();
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

	/**
	 * Finds the string name of the amount resource
	 * 
	 * @param resource
	 * @return resource string name
	 */
	@Override
	public String findAmountResourceName(int resource) {
		if (resourceARs.containsKey(resource)) {
			return resourceARs.get(resource).getName();
		}
		else {
			AmountResource ar = ResourceUtil.findAmountResource(resource);
			if (resourceIDs.contains(resource)) {
				resourceARs.put(resource, ar);
				return ar.getName();
			}
			else {
				// this resource is not stored in the micro inventory
				// therefore it doesn't need to be saved in resourceARs
				return ar.getName();
			}
		}
	}
	
	/**
	 * Finds the string name of the item resource
	 * 
	 * @param resource
	 * @return resource string name
	 */
	@Override
	public String findItemResourceName(int resource) {
		if (resourceIRs.containsKey(resource)) {
			return resourceIRs.get(resource).getName();
		}
		else {
			ItemResource ir = ItemResourceUtil.findItemResource(resource);
			if (resourceIDs.contains(resource)) {
				resourceIRs.put(resource, ir);
				return ir.getName();
			}
			else {
				// this resource is not stored in the micro inventory
				// therefore it doesn't need to be saved in resourceIRs
				return ir.getName();
			}
		}
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
		resourceARs.clear();
		resourceIDs.clear();
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
		return this.getIdentifier() == e.getIdentifier()
				&& this.getNickName().equals(e.getNickName());
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
