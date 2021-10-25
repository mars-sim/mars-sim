/*
 * Mars Simulation Project
 * ReturnLightUtilityVehicle.java
 * @date 2021-10-21
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.Iterator;
import java.util.logging.Level;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.equipment.EquipmentOwner;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.RoverMission;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.LightUtilityVehicle;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * A task for returning a light utility vehicle (LUV) to a rover or settlement
 * when a person finds oneself operating one.
 */
public class ReturnLightUtilityVehicle extends Task implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(ReturnLightUtilityVehicle.class.getName());

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.returnLightUtilityVehicle"); //$NON-NLS-1$

	/** Task phases. */
	private static final TaskPhase RETURN_LUV = new TaskPhase(Msg.getString("Task.phase.returnLUV")); //$NON-NLS-1$

	// Static members
	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = .5D;

	// Data members.
	LightUtilityVehicle luv = null;
	Unit returnContainer = null;

	/**
	 * Constructor.
	 * 
	 * @param person the person starting the task.
	 */
	public ReturnLightUtilityVehicle(Person person) {
		super(NAME, person, false, false, STRESS_MODIFIER, null, 0D);
		
		Vehicle personVehicle = person.getVehicle();
		if ((personVehicle != null) && (personVehicle instanceof LightUtilityVehicle)) {
			luv = (LightUtilityVehicle) personVehicle;
		} else {
			endTask();
			logger.severe(person, "Is not in a light utility vehicle.");
		}

		// Return container may be settlement or rover.
		returnContainer = null;

		// Attempt to determine return container based on mission.
		Mission mission = person.getMind().getMission();
		if (mission != null) {
			if (mission instanceof RoverMission) {
				RoverMission roverMission = (RoverMission) mission;
				returnContainer = roverMission.getRover();
			} else {
				returnContainer = mission.getAssociatedSettlement();
			}
		}

		// If returnContainer hasn't been found, look for local settlement.
		if (returnContainer == null) {
			Iterator<Settlement> i = unitManager.getSettlements().iterator();
			while (i.hasNext()) {
				Settlement settlement = i.next();
				if (person.getCoordinates().equals(settlement.getCoordinates())) {
					returnContainer = settlement;
					break;
				}
			}
		}

		// If returnContainer hasn't been found, look for local rover.
		if (returnContainer == null) {
			Iterator<Vehicle> i = unitManager.getVehicles().iterator();
			while (i.hasNext()) {
				Vehicle vehicle = i.next();
				if (vehicle instanceof Rover) {
					returnContainer = vehicle;
					break;
				}
			}
		}

		// Initialize task phase
		addPhase(RETURN_LUV);
		setPhase(RETURN_LUV);

		// If returnContainer still hasn't been found, end task.
		if (returnContainer == null) {
			endTask();
			logger.severe(person, "Cannot find a settlement or rover to return light utility vehicle.");
		} else {
			if (luv != null) {
				setDescription(Msg.getString("Task.description.returnLightUtilityVehicle.detail", luv.getName(),
					returnContainer.getName())); // $NON-NLS-1$
				logger.log(person, Level.FINE, 500, "Is starting to return light utility vehicle: " + luv.getName() + " to "
					+ returnContainer.getName());
			}
		}
	}

	@Override
	protected double performMappedPhase(double time) {
		if (getPhase() == null) {
			throw new IllegalArgumentException("Task phase is null");
		} else if (RETURN_LUV.equals(getPhase())) {
			return returnLUVPhase(time);
		} else {
			return time;
		}
	}

	/**
	 * Perform the return LUV phase.
	 * 
	 * @param time the time to perform the task phase.
	 * @return remaining time after performing the phase.
	 */
	private double returnLUVPhase(double time) {

		Mission mission = worker.getMission();
		
		if (mission == null) {
			boolean done = false;
			Rover r = null;
			Settlement s = null;
			
			// Put light utility vehicle in return container.
			if (returnContainer.getUnitType() == UnitType.VEHICLE) {
				r = (Rover)returnContainer;
				done = r.setLUV(luv);
				// Unload any attachment parts or inventory from light utility vehicle.
				unloadLUVInventory(r);
			}
			else if (returnContainer.getUnitType() == UnitType.SETTLEMENT) {
				s = (Settlement)returnContainer;
				done = s.addParkedVehicle(r);
//				luv.findNewParkingLoc();
				// Unload any attachment parts or inventory from light utility vehicle.
				unloadLUVInventory(s);
			}
	
			if (done) {
				if (person != null)
					// Remove person from light utility vehicle.
					luv.removePerson(person);
				else if (robot != null)
					// Remove robot from light utility vehicle.
					luv.removeRobot(robot);

				luv.setOperator(null);
				
			} else {
				logger.severe(luv, "Light utility vehicle: could not be stored in "
						+ returnContainer.getName());
			}
		}

		endTask();

		return time;
	}

	/**
	 * Unload all attachment parts and inventory from light utility vehicle.
	 */
	private void unloadLUVInventory(EquipmentOwner eo) {

//		Inventory rcInv = returnContainer.getInventory();

		// Unload all units.
		Iterator<Equipment> j = luv.getEquipmentList().iterator();
		while (j.hasNext()) {
			Equipment unit = j.next();
			if (!unit.transfer(returnContainer)) {
				logger.severe(unit, "Cannot be stored in " + returnContainer.getName());
			}
		}

		// Unload all parts.
		for(int id : luv.getItemResourceIDs()) { 
			int num = luv.getItemResourceStored(id);
			Part part = ItemResourceUtil.findItemResource(id);
			double mass = part.getMassPerItem() * num;
			if (eo.getRemainingCargoCapacity() >= mass) {
				luv.retrieveItemResource(id, num);
				eo.storeItemResource(id, num);
			} else {
				logger.severe(returnContainer, part.getName() + " numbered " + num
							+ " cannot be stored due to insufficient remaining general capacity.");
			}
		}

		// Unload all amount resources.
		for(int id : luv.getAmountResourceIDs()) {
			double amount = luv.getAmountResourceStored(id);
			if (eo.getAmountResourceCapacity(id) >= amount) {
				luv.retrieveAmountResource(id, amount);
				eo.storeAmountResource(id, amount);
			} else {
				logger.severe(returnContainer, ResourceUtil.findAmountResourceName(id)
							  + " of amount " + amount + " kg. cannot be stored");
			}
		}
	}
}
