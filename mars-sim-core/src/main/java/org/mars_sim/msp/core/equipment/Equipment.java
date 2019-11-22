/**
 * Mars Simulation Project
 * Equipment.java
 * @version 3.1.0 2017-09-07
 * @author Scott Davis
 */

package org.mars_sim.msp.core.equipment;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.mars_sim.msp.core.CollectionUtils;
import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.location.LocationStateType;
import org.mars_sim.msp.core.manufacture.Salvagable;
import org.mars_sim.msp.core.manufacture.SalvageInfo;
import org.mars_sim.msp.core.manufacture.SalvageProcessInfo;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.Maintenance;
import org.mars_sim.msp.core.person.ai.task.Repair;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.Indoor;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * The Equipment class is an abstract class that represents a useful piece of
 * equipment, such as a EVA suit or a medpack.
 */
public abstract class Equipment extends Unit implements Indoor, Salvagable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/** The unit count for this person. */
	private static int uniqueCount = Unit.FIRST_EQUIPMENT_UNIT_ID;
	
	// Data members.
	/** is this equipment being salvage. */
	private boolean isSalvaged;
	/** Unique identifier for this equipment. */
	private int identifier;
	/** Unique identifier for the settlement that owns this equipment. */
	private int associatedSettlementID;
	
	/** The identifier for the last owner of this equipment. */
	private Integer lastOwner;
	/** The equipment type. */
	private String type;
	
	/** The SalvageInfo instatnce. */	
	private SalvageInfo salvageInfo;
	/** The equipment type enum. */
	private EquipmentType equipmentType;
	
	/**
	 * Must be synchronised to prevent duplicate ids being assigned via different
	 * threads.
	 * 
	 * @return
	 */
	private static synchronized int getNextIdentifier() {
		return uniqueCount++;
	}
	
	/**
	 * Get the unique identifier for this person
	 * 
	 * @return Identifier
	 */
	public int getIdentifier() {
		return identifier;
	}
	
	/**
	 * Increments the identifier
	 */
	public void incrementID() {
		// Gets the identifier
		this.identifier = getNextIdentifier();
	}
	
	/**
	 * Constructs an Equipment object
	 * 
	 * @param name     the name of the unit
	 * @param type     the type of the unit
	 * @param location the unit's coordinates
	 */
	protected Equipment(String name, String type, Coordinates location) {
		super(name, location);
		
		// Initialize data members.
		this.type = type;
		this.equipmentType = EquipmentType.convertName2Enum(type);
		isSalvaged = false;
		salvageInfo = null;
		
		lastOwner = Integer.valueOf(-1);
		
		if (unitManager == null)
			unitManager = Simulation.instance().getUnitManager();
		
		// Adds this equipment to the equipment lookup map	
		unitManager.addEquipmentID(this);

		if (!(this instanceof Robot) && location != null && !location.equals(ContainerUtil.tempCoordinates)) {
			Settlement s = CollectionUtils.findSettlement(location);
			associatedSettlementID = s.getIdentifier();
			
			// Stores this equipment into its settlement
			s.getInventory().storeUnit(this);
			// Add this equipment as being owned by this settlement
			s.addOwnedEquipment(this);
		}
	}
	
	/**
	 * Gets a collection of people affected by this entity.
	 * 
	 * @return person collection
	 */
	public Collection<Person> getAffectedPeople() {
		Collection<Person> people = new ConcurrentLinkedQueue<Person>();

		Person owner = null;
		if (lastOwner != Integer.valueOf(-1)) {// && lastOwner instanceof Person) {
			owner = unitManager.getPersonByID(lastOwner);
			people.add(owner);
		}

		// Check all people.
		Iterator<Person> i = unitManager.getPeople().iterator();
		while (i.hasNext()) {
			Person person = i.next();
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
//			else
			// either at the vicinity of a settlement or already outside on a mission
			// TODO: need to differentiate which case in future better granularity
//				return null;
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
		if (LocationStateType.WITHIN_SETTLEMENT_VICINITY  == currentStateType)
			return true;
		return false;
	}
	
//	/**
//	 * Get the equipment's location
//	 * 
//	 * @return {@link LocationSituation} the person's location
//	 */
//	public LocationSituation getLocationSituation() {
//		Unit container = getContainerUnit();
//		if (container instanceof Settlement)
//			return LocationSituation.IN_SETTLEMENT;
//		else if (container instanceof Vehicle)
//			return LocationSituation.IN_VEHICLE;
//		else if (container instanceof Person || container instanceof Robot)
//			return container.getLocationSituation();
//		else if (container instanceof MarsSurface)
//			return LocationSituation.OUTSIDE;
//		else
//			return LocationSituation.UNKNOWN;
//	}

	/**
	 * Is a person carrying this equipment? Is this equipment's container a person ?
	 * 
	 * @return true if yes
	 */
	public boolean isCarriedByAPerson() {
		if (LocationStateType.ON_A_PERSON_OR_ROBOT == currentStateType)
			return true;
		
		return false;	
//		if (getContainerUnit() instanceof Person)
//			return true;
//		return false;
	}

	/**
	 * Sets the last owner of this equipment
	 * 
	 * @param unit
	 */
	public void setLastOwner(Unit unit) {
		if (unit != null) {
			 if ((Integer) unit.getIdentifier() != lastOwner)
				 lastOwner = (Integer) unit.getIdentifier();
		}	
		else
			lastOwner = (Integer) Unit.UNKNOWN_UNIT_ID;
	}

	/**
	 * Gets the last owner of this equipment
	 * 
	 * @return
	 */
	public Unit getLastOwner() {
		return unitManager.getPersonByID(lastOwner);
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
	
	/**
	 * Reset uniqueCount to the current number of equipment
	 */
	public static void reinitializeIdentifierCount() {
		uniqueCount = unitManager.getEquipmentNum() + Unit.FIRST_EQUIPMENT_UNIT_ID;
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
		return this.identifier == e.getIdentifier()
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
		hashCode *= identifier;
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