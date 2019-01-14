/**
 * Mars Simulation Project
 * Equipment.java
 * @version 3.1.0 2017-09-07
 * @author Scott Davis
 */

package org.mars_sim.msp.core.equipment;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.location.LocationSituation;
import org.mars_sim.msp.core.location.LocationStateType;
import org.mars_sim.msp.core.manufacture.Salvagable;
import org.mars_sim.msp.core.manufacture.SalvageInfo;
import org.mars_sim.msp.core.manufacture.SalvageProcessInfo;
import org.mars_sim.msp.core.mars.MarsSurface;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.Maintenance;
import org.mars_sim.msp.core.person.ai.task.Repair;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.Indoor;
import org.mars_sim.msp.core.vehicle.Vehicle;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * The Equipment class is an abstract class that represents a useful piece of
 * equipment, such as a EVA suit or a medpack.
 */
public abstract class Equipment extends Unit implements Indoor, Salvagable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members.
	private boolean isSalvaged;

	private SalvageInfo salvageInfo;

	private int lastOwner = -1;

	/**
	 * Constructs an Equipment object
	 * 
	 * @param name     the name of the unit
	 * @param location the unit's location
	 */
	protected Equipment(String name, Coordinates location) {
		super(name, location);
		// Initialize data members.
		isSalvaged = false;
		salvageInfo = null;
		
		if (mars != null) {// For passing maven test
			marsSurface = mars.getMarsSurface();
			// Initially set container unit to the mars surface
			setContainerUnit(marsSurface);
			// Add this equipment to the equipment lookup map		
			unitManager.addEquipmentID(this);
		}

		
//		// Place this person within a settlement
//		enter(LocationCodeType.SETTLEMENT);
//		// Place this person within a building
//		enter(LocationCodeType.BUILDING);

		
	}
	
	/**
	 * Gets a collection of people affected by this entity.
	 * 
	 * @return person collection
	 */
	public Collection<Person> getAffectedPeople() {
		Collection<Person> people = new ConcurrentLinkedQueue<Person>();

		Person owner = null;
		if (lastOwner != -1) {// && lastOwner instanceof Person) {
			owner = unitManager.getPersonID(lastOwner);
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

	// public String getName() {
	// return name;
	// }

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

//	/**
//	 * Get settlement equipment is at, null if not at a settlement
//	 *
//	 * @return the equipment's settlement
//	 */
//    @Override
//	  public Settlement getSettlement() {
//		LocationSituation ls = getLocationSituation();
//		if (LocationSituation.IN_SETTLEMENT == ls) {
//			return (Settlement) getContainerUnit();
//		}
//
//		else if (LocationSituation.OUTSIDE == ls)
//			return null;
//
//		else if (LocationSituation.IN_VEHICLE == ls) {
//			Vehicle vehicle = (Vehicle) getContainerUnit();
//			Settlement settlement = (Settlement) vehicle.getContainerUnit();
//			return settlement;
//		}
//
//		else if (LocationSituation.BURIED == ls) {
//			// should not be the case
//			return null;
//		}
//
//		else {
//			System.err.println("Equipment : error in determining " + getName() + "'s getSettlement() ");
//			return null;
//		}
//	}

	/**
	 * Is the person inside a settlement or a vehicle
	 * 
	 * @return true if the person is inside a settlement or a vehicle
	 */
	public boolean isInside() {
		Unit c = getContainerUnit();
		if (c instanceof Settlement
			|| c instanceof Vehicle)
			return true;
		else if (c instanceof Person) {
			Unit cc = ((Person) c).getContainerUnit();
			if (cc instanceof Settlement
				|| cc instanceof Vehicle) {
				return true;
			}
		}
			
		return false;
	}
	
	
	/**
	 * Get the equipment's settlement, null if equipment is not at a settlement
	 *
	 * @return {@link Settlement} the equipment's settlement
	 */
	public Settlement getSettlement() {

		Unit c = getContainerUnit();

		if (c instanceof Settlement) {
			return (Settlement) c;
		}

		else if (c instanceof Person) {
			Unit cc = ((Person) c).getContainerUnit();
			if (cc instanceof Settlement) {
				return (Settlement) cc;
			}
//		} else if (c instanceof Vehicle && ((Vehicle) c).getStatus() == StatusType.GARAGED) {
//			Unit cc = ((Vehicle) c).getContainerUnit();
//			if (cc instanceof Settlement) {
//				return (Settlement) c;
//			}
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
		return null;
	}

	/**
	 * Is the equipment's outside of a settlement but within its vicinity
	 * 
	 * @return true if the equipment's is just right outside of a settlement
	 */
	public boolean isRightOutsideSettlement() {
		if (LocationStateType.OUTSIDE_SETTLEMENT_VICINITY  == currentStateType)
			return true;
		return false;
	}
	
	/**
	 * Get the equipment's location
	 * 
	 * @deprecated use other more efficient methods
	 * @return {@link LocationSituation} the person's location
	 */
	public LocationSituation getLocationSituation() {
		Unit container = getContainerUnit();
		if (container instanceof Settlement)
			return LocationSituation.IN_SETTLEMENT;
		else if (container instanceof Vehicle)
			return LocationSituation.IN_VEHICLE;
		else if (container instanceof MarsSurface)
			return LocationSituation.OUTSIDE;
		else
			return LocationSituation.UNKNOWN;
	}

	/**
	 * Is the equipment's immediate container a settlement ?
	 * 
	 * @return true if yes
	 */
	public boolean isInSettlement() {
		if (LocationStateType.INSIDE_SETTLEMENT == currentStateType)
			return true;
		return false;		
//		Unit c = getTopContainerUnit();
//		if (c instanceof Settlement)
//			return true;
////		else if (c instanceof Vehicle && ((Vehicle) c).getStatus() == StatusType.GARAGED)
////			return true;
//		return false;
	}

	/**
	 * Is the equipment's immediate container a person ?
	 * 
	 * @return true if yes
	 */
	public boolean isInPerson() {
		if (LocationStateType.ON_A_PERSON == currentStateType)
			return true;
		
		return false;	
//		if (getContainerUnit() instanceof Person)
//			return true;
//		return false;
	}

	/**
	 * Is the equipment's immediate container a vehicle ?
	 * 
	 * @return true if yes
	 */
	public boolean isInVehicle() {
		if (LocationStateType.INSIDE_VEHICLE == currentStateType)
			return true;
		
		return false;	
//		if (getContainerUnit() instanceof Vehicle)
//			return true;
//		return false;
	}

	/**
	 * Is the equipment outside on the surface of Mars
	 * 
	 * @return true if the equipment is outside
	 */
	public boolean isOutside() {
		if (LocationStateType.OUTSIDE_ON_MARS == currentStateType
				|| LocationStateType.OUTSIDE_SETTLEMENT_VICINITY == currentStateType)
			return true;
		
		return false;	
//		if (getContainerUnit() instanceof MarsSurface)
//			return true;
//		return false;
	}

	public void setLastOwner(Unit unit) {
		if (unit != null)
			lastOwner = unit.getIdentifier();
		else
			lastOwner = -1;
	}

	public Unit getLastOwner() {
		return unitManager.getPersonID(lastOwner);
	}

	@Override
	public void destroy() {
		super.destroy();
		if (salvageInfo != null)
			salvageInfo.destroy();
		salvageInfo = null;
	}
}