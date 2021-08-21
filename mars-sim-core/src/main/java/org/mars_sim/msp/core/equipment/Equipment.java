/*
 * Mars Simulation Project
 * Equipment.java
 * @date 2021-08-20
 * @author Scott Davis
 */

package org.mars_sim.msp.core.equipment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.location.LocationStateType;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.manufacture.Salvagable;
import org.mars_sim.msp.core.manufacture.SalvageInfo;
import org.mars_sim.msp.core.manufacture.SalvageProcessInfo;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.Maintenance;
import org.mars_sim.msp.core.person.ai.task.Repair;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
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
	/** The equipment type. */
	private final String type;
	
	private int resource = -1;
	private double quantity;
	
	/** The SalvageInfo instatnce. */	
	private SalvageInfo salvageInfo;
	/** The equipment type enum. */
	private final EquipmentType equipmentType;
	
	
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
		this.type = type;
		this.equipmentType = EquipmentType.convertName2Enum(type);
		isSalvaged = false;
		salvageInfo = null;
		
		lastOwner = -1;
		
		if (unitManager == null)
			unitManager = Simulation.instance().getUnitManager();

		// if it's a container, proceed
		if (equipmentType != null) {
			// Gets the settlement id
			associatedSettlementID = settlement.getIdentifier();
			// Stores this equipment into its settlement
			settlement.getInventory().storeUnit(this);
			// Add this equipment as being owned by this settlement
			settlement.addOwnedEquipment(this);
		}
	}

	@Override
	public boolean timePassing(ClockPulse pulse) {
		// By default Equipment are passive objects
		return false;
	}

	public int getResource() {
		return resource;
	}
	
	public double getQuanity() {
		return quantity;
	}
	
	/**
	 * Stores the resource
	 * 
	 * @param resource
	 * @param quantity
	 * @return excess quantity that cannot be stored
	 */
	public double storeAmountResource(int resource, double quantity) {
		// If this container has never been used before
		if (this.resource == -1) {
			// Question: if a bag was filled with regolith and later was emptied out
			// should it be tagged for only regolith and NOT for another resource ?
			this.resource = resource;
			String name = ResourceUtil.findAmountResourceName(resource);
			if (name != null) {
				logger.config(this, "Initialized for storing " + name + ".");
			}
			else {
				logger.warning(this, "Resource " + resource + " is invalid.");
				return quantity;
			}
		}
		
		if (this.resource == resource) {
			
			double newQ = this.quantity + quantity;
			String name = ResourceUtil.findAmountResourceName(resource);
			
			if (newQ > getTotalCapacity()) {
				double excess = newQ - getTotalCapacity();
				logger.warning(this, "Storage is full. Excess " + Math.round(excess * 10.0)/10.0 + " kg " + name + ".");
				this.quantity = getTotalCapacity();
				return excess;
			}
			else {
				this.quantity = newQ;
//				logger.config(this, "Just added " + Math.round(quantity * 10.0)/10.0 + " kg " + name + ".");
				return 0;
			}
		}
		else {
			String name = ResourceUtil.findAmountResourceName(resource);
			String storedResource = ResourceUtil.findAmountResourceName(this.resource);
			logger.warning(this, "Invalid request. Not for " + name 
					+ ". Already storing " + storedResource + " " + Math.round(this.quantity* 10.0)/10.0 + " kg.");
			return quantity;
		}
	}
	
	/**
	 * Retrieve the resource 
	 * 
	 * @param resource
	 * @param quantity
	 * @return quantity that cannot be retrieved
	 */
	public double retrieveAmountResource(int resource, double quantity) {
		if (this.resource == resource || this.resource == -1) {
			double diff = this.quantity - quantity;
			if (diff < 0) {
				String name = ResourceUtil.findAmountResourceName(resource);
				logger.warning(this, "Just retrieved all " + this.quantity + " kg " 
						+ name + " but still lacking " + Math.round(-diff * 10.0)/10.0 + " kg.");
				this.quantity = 0;
				return diff;
			}
			else {
				this.quantity = diff;
				return 0;
			}
		}
		else {
			String storedResource = ResourceUtil.findAmountResourceName(this.resource);
			String requestedResource = ResourceUtil.findAmountResourceName(resource);
			logger.warning(this, "Cannot retrieve " + requestedResource 
					+ ". Storing " + storedResource + ": " + Math.round(this.quantity* 10.0)/10.0 + " kg.");
			return quantity;
		}
	}
	
	public double getAmountResourceCapacity(int resource) {
		if (this.resource == resource || this.resource == -1) {
			return getTotalCapacity();
		}
		else {
//			String storedResource = ResourceUtil.findAmountResourceName(this.resource);
//			String requestedResource = ResourceUtil.findAmountResourceName(resource);
//			logger.warning(this, "Invalid request. Not for " + requestedResource 
//					+ ". Already storing " + storedResource + " " + Math.round(this.quantity* 10.0)/10.0 + " kg.");
			return 0;
		}
	}
	
	/**
	 * Obtains the remaining storage space 
	 * 
	 * @param resource
	 * @return
	 */
	public double getAmountResourceRemainingCapacity(int resource) {
		if (this.resource == resource || this.resource == -1) {
			return getTotalCapacity() - this.quantity;
		}
		else {
//			String storedResource = ResourceUtil.findAmountResourceName(this.resource);
//			String requestedResource = ResourceUtil.findAmountResourceName(resource);
//			logger.warning(this, "Invalid request. Not for " + requestedResource 
//					+ ". Already storing " + storedResource + " " + Math.round(this.quantity* 10.0)/10.0 + " kg.");
			return 0;
		}
	}
	
	
	/**
	 * Gets the amount resource stored
	 * 
	 * @param resource
	 * @return
	 */
	public double getAmountResourceStored(int resource) {
		if (this.resource == resource) {
			return this.quantity;
		}
		else if (this.resource == - 1) {
			return 0;
		}
		else {
//			String storedResource = ResourceUtil.findAmountResourceName(this.resource);
//			String requestedResource = ResourceUtil.findAmountResourceName(resource);
//			logger.warning(this, "Invalid request. Not for " + requestedResource 
//					+ ". Already storing " + storedResource + " " + Math.round(this.quantity* 10.0)/10.0 + " kg.");
			return 0;
		}
	}
	
	public double getStoredMass() {
		return this.quantity;
	}
	

	/**
	 * Is this equipment empty ? 
	 * 
	 * @param brandNew true if it needs to be brand new
	 * @return
	 */
	public boolean isEmpty(boolean brandNew) {
		// if resource is -1, it's never been used before
		// if it's not brand new, it doesn't matter if source is -1 or not
		if ((brandNew && resource == -1) || this.quantity == 0)
			return true;
		return false;
	}	
	
	public boolean isBrandNew() {
		if (resource == -1 && this.quantity == 0)
			return true;
		return false;
	}	
	
	public boolean isUsed() {
		if (resource != -1)
			return true;
		return false;
	}	

	public boolean hasContent() {
		if (quantity > 0)
			return true;
		return false;
	}	
	
	public boolean isEmpty(int resource) {
		if (resource == -1)
			return true;
		if (this.resource == resource || this.quantity == 0)
			return true;
		
		return false;
	}	
	
	public abstract double getTotalCapacity();
	
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
	public Unit getLastOwner() {
		return unitManager.getPersonByID(lastOwner);
	}

	public int getLastOwnerID() {
		return lastOwner;
	}
	
	public String getType() {
		return type;
	}
	
	public EquipmentType getEquipmentType() {
		return equipmentType;
	}
	
	public Settlement getAssociatedSettlement() {
		return unitManager.getSettlementByID(associatedSettlementID);
	}
	
	@Override
	protected UnitType getUnitType() {
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
		this.resource = -1;
		this.quantity = 0;
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
		hashCode *= type.hashCode();
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
