/*
 * Mars Simulation Project
 * LightUtilityVehicle.java
 * @date 2026-08-15
 * @author Sebastien Venot
 */
package com.mars_sim.core.vehicle;

import java.util.Collection;

import com.mars_sim.core.EntityEventType;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.resource.Part;
import com.mars_sim.core.structure.Settlement;

/**
 * A light utility vehicle that can be used for construction, loading and
 * mining.
 */
public class LightUtilityVehicle extends GroundVehicle {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** Vehicle name. */
	public static final String NAME = VehicleType.LUV.getName();

	/** The amount of work time to perform maintenance (millisols). */
	public static final double MAINTENANCE_WORK_TIME = 75D;
	
	// Data members.
	/** The LightUtilityVehicle's capacity for crewmembers. */
	private int crewCapacity = 1;
//	private int robotCrewCapacity = 0;
	private int slotNumber = 0;
	
	/** A collections of attachment parts */
	private Collection<Part> attachments;
//	/** The occupant. */
	private	Worker occupant;
	
	public LightUtilityVehicle(String name, VehicleSpec spec, Settlement settlement) {
		// Use GroundVehicle constructor.
		super(name, spec, settlement, MAINTENANCE_WORK_TIME);
		
		if (spec.hasPartAttachments()) {
			attachments = spec.getAttachableParts();
			slotNumber = spec.getAttachmentSlots();
		}
	}

	/**
	 * Gets the number of crewmembers the vehicle can carry.
	 * 
	 * @return capacity
	 */
//	@Override
	public int getCrewCapacity() {
		return crewCapacity;
	}

	/**
	 * Gets the current number of crewmembers.
	 * 
	 * @return number of crewmembers
	 */
//	@Override
	public int getCrewNum() {
		if (occupant != null)
			return 1;
		return 0;
	}

	/**
	 * Checks if worker is a crewmember.
	 * 
	 * @param worker the worker to check
	 * @return true if worker is a crewmember
	 */
//	@Override
	public boolean isCrewmember(Worker worker) {
		if (occupant == null)
			return false;
		return occupant.equals(worker);
	}

	/**
	 * Does the luv have no occupants ?
	 */
	public boolean hasNoCrew() {
		return occupant == null;
	}
	
	/**
	 * is it full ?
	 * 
	 * @return
	 */
	public boolean isFull() {
		return this.getCrewCapacity() <= getCrewNum();
	}
	
	/**
	 * Adds a worker as crewmember.
	 *
	 * @param worker
	 * @param true if the worker can be added
	 */
	public boolean addOccupant(Worker worker) {
		if (!isFull() && !isCrewmember(worker) && occupant == null && getOperator() == null) {
			occupant = worker;
			setOperator(worker);
			// Fire the unit event type
			fireUnitUpdate(EntityEventType.INVENTORY_STORING_UNIT_EVENT, worker);
			return true;
		}
		return false;
	}

	/**
	 * Gets the occupant.
	 * 
	 * @return
	 */
	public Worker getOccupant() {
		return occupant;
	}
	
	/**
	 * Removes a worker as crewmember.
	 *
	 * @param worker
	 * @param true if the worker can be removed
	 */
	public boolean removeOccupant(Worker worker) {
		if (isCrewmember(worker)) {
			occupant = null;
			if (getOperator() != null && getOperator().equals(worker))
				setOperator(null);
			fireUnitUpdate(EntityEventType.INVENTORY_RETRIEVING_UNIT_EVENT, worker);
			return true;
		}
		return false;
	}
	
	/**
	 * Gets a collection of parts that can be attached to this vehicle.
	 * 
	 * @return collection of parts.
	 */
	public Collection<Part> getPossibleAttachmentParts() {
		return attachments;
	}
	
	/**
	 * Gets the number of part slots in the vehicle.
	 * 
	 * @return number of part slots.
	 */
	public int getAtachmentSlotNumber() {
		return slotNumber;
	}

	@Override
	public void destroy() {
		super.destroy();

		attachments.clear();
		attachments = null;	

		occupant = null;

	}
	 
}

