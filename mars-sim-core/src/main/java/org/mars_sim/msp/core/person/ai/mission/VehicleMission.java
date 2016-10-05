/**
 * Mars Simulation Project
 * VehicleMission.java
 * @version 3.08 2015-07-09
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitEvent;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.UnitListener;
import org.mars_sim.msp.core.equipment.Container;
import org.mars_sim.msp.core.equipment.ContainerUtil;
import org.mars_sim.msp.core.events.HistoricalEvent;
import org.mars_sim.msp.core.malfunction.Malfunction;
import org.mars_sim.msp.core.person.EventType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.LoadVehicleGarage;
import org.mars_sim.msp.core.person.ai.task.OperateVehicle;
import org.mars_sim.msp.core.person.ai.task.TaskPhase;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ItemResource;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.resource.Resource;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.core.vehicle.VehicleOperator;

/**
 * A mission that involves driving a vehicle along a series of navpoints.
 */
public abstract class VehicleMission
extends TravelMission
implements UnitListener {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(VehicleMission.class.getName());

	/** Mission phases. */
	final public static MissionPhase EMBARKING = new MissionPhase(Msg.getString(
			"Mission.phase.embarking")); //$NON-NLS-1$
	final public static MissionPhase TRAVELLING = new MissionPhase(Msg.getString(
			"Mission.phase.travelling")); //$NON-NLS-1$
	final public static MissionPhase DISEMBARKING = new MissionPhase(Msg.getString(
			"Mission.phase.disembarking")); //$NON-NLS-1$

	// Static members

	/** Modifier for number of parts needed for a trip. */
	private static final double PARTS_NUMBER_MODIFIER = 2D;

	// Data members
	private Vehicle vehicle;
	/** The last operator of this vehicle in the mission. */
	private VehicleOperator lastOperator;
	
	private MissionMember startingMember;
	/** True if vehicle has been loaded. */
	protected boolean loadedFlag = false;
	/** True if vehicle's emergency beacon has been turned on */
    //private boolean isBeaconOn = false;
    
	/** Vehicle traveled distance at start of mission. */
	private double startingTravelledDistance;

	// Mission tasks tracked
	/** The current operate vehicle task. */
	private OperateVehicle operateVehicleTask;

	/** Caches */
	protected Map<Class, Integer> equipmentNeededCache;

	protected VehicleMission(String name, MissionMember startingMember, int minPeople) {
		// Use TravelMission constructor.
		super(name, startingMember, minPeople);

		this.startingMember = startingMember;
		
		// Add mission phases.
		addPhase(EMBARKING);
		addPhase(TRAVELLING);
		addPhase(DISEMBARKING);


		// Reserve a vehicle.	 
		if (!reserveVehicle(startingMember)) {
		    endMission(NO_RESERVABLE_VEHICLES);
		}

	}
	

	protected VehicleMission(String name, MissionMember startingMember, int minPeople,
			Vehicle vehicle) {
		// Use TravelMission constructor.
		super(name, startingMember, minPeople);

		this.startingMember = startingMember;
		
		// Add mission phases.
		addPhase(EMBARKING);
		addPhase(TRAVELLING);
		addPhase(DISEMBARKING);

		// Set the vehicle.
		setVehicle(vehicle);
	}
	/**
	 * Gets the mission's vehicle if there is one.
	 * @return vehicle or null if none.
	 */
	public final Vehicle getVehicle() {
		return vehicle;
	}

	/**
	 * Sets the vehicle for this mission.
	 * @param newVehicle the vehicle to use.
	 * @throws MissionException if vehicle cannot be used.
	 */
	protected void setVehicle(Vehicle newVehicle) {
		if (newVehicle != null) {
			boolean usable = false;
			usable = isUsableVehicle(newVehicle);
			if (usable) {
				vehicle = newVehicle;
				startingTravelledDistance = vehicle.getTotalDistanceTraveled();
				newVehicle.setReservedForMission(true);
				vehicle.addUnitListener(this);
				fireMissionUpdate(MissionEventType.VEHICLE_EVENT);
			}
			if (!usable) {
				throw new IllegalStateException(getPhase()
						+ " : newVehicle is not usable for this mission.");
			}
		} else {
			throw new IllegalArgumentException("newVehicle is null.");
		}
	}

	/**
	 * Checks if the mission has a vehicle.
	 * @return true if vehicle.
	 */
	public final boolean hasVehicle() {
		return (vehicle != null);
	}

	/**
	 * Leaves the mission's vehicle and unreserves it.
	 */
	protected final void leaveVehicle() {
		//logger.info("Calling leaveVehicle()");
		if (hasVehicle()) {
			vehicle.setReservedForMission(false);
			vehicle.removeUnitListener(this);
			vehicle = null;
			fireMissionUpdate(MissionEventType.VEHICLE_EVENT);
		}
	}

	/**
	 * Checks if vehicle is usable for this mission. (This method should be added to by children)
	 * @param newVehicle the vehicle to check
	 * @return true if vehicle is usable.
	 * @throws IllegalArgumentException if newVehicle is null.
	 * @throws MissionException if problem checking vehicle is loadable.
	 */
	protected boolean isUsableVehicle(Vehicle newVehicle) {
		if (newVehicle != null) {
			boolean usable = true;
			if (newVehicle.isReserved()) {
				usable = false;
			}
			if (!newVehicle.getStatus().equals(Vehicle.PARKED)) {
				usable = false;
			}
			if (newVehicle.getInventory().getTotalInventoryMass(false) > 0D) {
				usable = false;
			}
			return usable;
		} else {
			throw new IllegalArgumentException(
					"isUsableVehicle: newVehicle is null.");
		}
	}

	/**
	 * Compares the quality of two vehicles for use in this mission. (This method should be added to by children)
	 * @param firstVehicle the first vehicle to compare
	 * @param secondVehicle the second vehicle to compare
	 * @return -1 if the second vehicle is better than the first vehicle, 0 if vehicle are equal in quality, and 1 if
	 *         the first vehicle is better than the second vehicle.
	 * @throws MissionException if error determining vehicle range.
	 */
	protected int compareVehicles(Vehicle firstVehicle, Vehicle secondVehicle) {
		if (isUsableVehicle(firstVehicle)) {
			if (isUsableVehicle(secondVehicle)) {
				return 0;
			}
			else {
				return 1;
			}
		} else {
			if (isUsableVehicle(secondVehicle)) {
				return -1;
			}
			else {
				return 0;
			}
		}
	}

	/**
	 * Reserves a vehicle for the mission if possible.
	 * @param person the person reserving the vehicle.
	 * @return true if vehicle is reserved, false if unable to.
	 * @throws MissionException if error reserving vehicle.
	 */
	protected final boolean reserveVehicle(MissionMember member) {

		Collection<Vehicle> bestVehicles = new ConcurrentLinkedQueue<Vehicle>();

		// Create list of best unreserved vehicles for the mission.
		Iterator<Vehicle> i = getAvailableVehicles(member.getSettlement())
				.iterator();
		while (i.hasNext()) {
			Vehicle availableVehicle = i.next();
			if (bestVehicles.size() > 0) {
				int comparison = compareVehicles(availableVehicle,
						(Vehicle) bestVehicles.toArray()[0]);
				if (comparison == 0) {
					bestVehicles.add(availableVehicle);
				}
				else if (comparison == 1) {
					bestVehicles.clear();
					bestVehicles.add(availableVehicle);
				}
			} else
				bestVehicles.add(availableVehicle);
		}

		// Randomly select from the best vehicles.
		if (bestVehicles.size() > 0) {
			int bestVehicleIndex = RandomUtil
					.getRandomInt(bestVehicles.size() - 1);
			try {
				setVehicle((Vehicle) bestVehicles.toArray()[bestVehicleIndex]);
			} catch (Exception e) {
			}
		}

		return hasVehicle();
	}
//	protected final boolean reserveVehicle(Robot robot) {
//
//		Collection<Vehicle> bestVehicles = new ConcurrentLinkedQueue<Vehicle>();
//
//		// Create list of best unreserved vehicles for the mission.
//		Iterator<Vehicle> i = getAvailableVehicles(robot.getSettlement())
//				.iterator();
//		while (i.hasNext()) {
//			Vehicle availableVehicle = i.next();
//			if (bestVehicles.size() > 0) {
//				int comparison = compareVehicles(availableVehicle,
//						(Vehicle) bestVehicles.toArray()[0]);
//				if (comparison == 0) {
//					bestVehicles.add(availableVehicle);
//				}
//				else if (comparison == 1) {
//					bestVehicles.clear();
//					bestVehicles.add(availableVehicle);
//				}
//			} else
//				bestVehicles.add(availableVehicle);
//		}
//
//		// Randomly select from the best vehicles.
//		if (bestVehicles.size() > 0) {
//			int bestVehicleIndex = RandomUtil
//					.getRandomInt(bestVehicles.size() - 1);
//			try {
//				setVehicle((Vehicle) bestVehicles.toArray()[bestVehicleIndex]);
//			} catch (Exception e) {
//			}
//		}
//
//		return hasVehicle();
//	}
	/**
	 * Gets a collection of available vehicles at a settlement that are usable for this mission.
	 * @param settlement the settlement to find vehicles.
	 * @return list of available vehicles.
	 * @throws MissionException if problem determining if vehicles are usable.
	 */
	private Collection<Vehicle> getAvailableVehicles(Settlement settlement) {
		Collection<Vehicle> result = new ConcurrentLinkedQueue<Vehicle>();

		Iterator<Vehicle> i = settlement.getParkedVehicles().iterator();
		while (i.hasNext()) {
			Vehicle vehicle = i.next();
			if (isUsableVehicle(vehicle)) {
				result.add(vehicle);
			}
		}

		return result;
	}

	/**
	 * Finalizes the mission
	 * @param reason the reason of ending the mission.
	 */
	//2016-09-19 Revised endMission() to check if user aborted the mission and if the vehicle has been disembarked.
	public void endMission(String reason) {
		//logger.info("Reason : " + reason);
		if (hasVehicle()) {
			// if user hit the "End Mission" button to abort the mission
			if (reason.equals(Mission.USER_ABORTED_MISSION)) {
				logger.info("User just aborted the mission. Switching to emergency mode to go to the nearest settlement.");	
				// will recursively call endMission() with a brand new "reason"
				determineEmergencyDestination(startingMember);	
				
			}
			
			else if (reason.equals(Mission.NOT_ENOUGH_RESOURCES_TO_CONTINUE)
					|| reason.equals(Mission.UNREPAIRABLE_MALFUNCTION)
					|| reason.equals(Mission.NO_EMERGENCY_SETTLEMENT_DESTINATION_FOUND)) {
				// Set emergency beacon if vehicle is not at settlement.
				// TODO: need to find out if there are other matching reasons for setting emergency beacon.
				if (vehicle.getSettlement() == null) {
					if (!vehicle.isEmergencyBeacon())
						if (!vehicle.isBeingTowed())
							setEmergencyBeacon(null, vehicle, true);
					
				}

			}
			
			else { // for all other reasons
	            setPhaseEnded(true);
				leaveVehicle();
				super.endMission(reason);
			}
		}
		
		//else if (reason.equals(Mission.SUCCESSFULLY_DISEMBARKED)) {
			//logger.info("Returning the control of " + vehicle + " to the settlement");
       //     setPhaseEnded(true);
			//leaveVehicle();
		//	super.endMission(reason);
		//}
		
		else { // if vehicles are NOT available 
            setPhaseEnded(true); // TODO: is it important ?
			super.endMission(reason);
		}
	}

	/**
	 * Determine if a vehicle is sufficiently loaded with fuel and supplies.
	 * @return true if rover is loaded.
	 * @throws MissionException if error checking vehicle.
	 */
	public final boolean isVehicleLoaded() {
		if (vehicle == null) {
			throw new IllegalStateException(getPhase() + " : vehicle is null");
		}

		try {
			return LoadVehicleGarage.isFullyLoaded(getRequiredResourcesToLoad(), 
					getOptionalResourcesToLoad(), getRequiredEquipmentToLoad(), 
					getOptionalEquipmentToLoad(), vehicle, vehicle.getSettlement());
		} catch (Exception e) {
			throw new IllegalStateException(getPhase().getName(), e);
		}
	}

	/**
	 * Checks if a vehicle can load the supplies needed by the mission.
	 * @return true if vehicle is loadable.
	 * @throws Exception if error checking vehicle.
	 */
	public final boolean isVehicleLoadable() {

		Map<Resource, Number> resources = getRequiredResourcesToLoad();
		Map<Class, Integer> equipment = getRequiredEquipmentToLoad();
		Vehicle vehicle = this.vehicle;
		Settlement settlement = vehicle.getSettlement();
		double tripTime = getEstimatedRemainingMissionTime(true);

		boolean vehicleCapacity = LoadVehicleGarage.enoughCapacityForSupplies(
				resources, equipment, vehicle, settlement);
		boolean settlementSupplies = LoadVehicleGarage.hasEnoughSupplies(settlement,
				vehicle, resources, equipment, getPeopleNumber(), tripTime);
		if (!vehicleCapacity) {
			logger.info("Vehicle doesn't have capacity.");
		}
		if (!settlementSupplies) {
			logger.info("Settlement doesn't have supplies.");
		}

		return vehicleCapacity && settlementSupplies;
	}

	/**
	 * Gets the amount of fuel (kg) needed for a trip of a given distance (km).
	 * @param tripDistance the distance (km) of the trip.
	 * @param fuelEfficiency the vehicle's fuel efficiency (km/kg).
	 * @param useBuffer use time buffers in estimation if true.
	 * @return amount of fuel needed for trip (kg)
	 */
	public static double getFuelNeededForTrip(double tripDistance,
			double fuelEfficiency, boolean useBuffer) {
		double result = tripDistance / fuelEfficiency;
		if (useBuffer) {
			result *= Vehicle.RANGE_ERROR_MARGIN;
		}

		return result;
	}

	/**
	 * Determines a new phase for the mission when the current phase has ended.
	 * @throws MissionException if problem setting a new phase.
	 */
	protected void determineNewPhase() {
		if (EMBARKING.equals(getPhase())) {
			startTravelToNextNode();
			setPhase(VehicleMission.TRAVELLING);
			setPhaseDescription(Msg.getString("Mission.phase.travelling.description", 
					getNextNavpoint().getDescription())); //$NON-NLS-1$
		} else if (TRAVELLING.equals(getPhase())) {
			if (getCurrentNavpoint().isSettlementAtNavpoint()) {
				setPhase(VehicleMission.DISEMBARKING);
				setPhaseDescription(Msg.getString("Mission.phase.disembarking.description", 
						getCurrentNavpoint().getDescription())); //$NON-NLS-1$
			}
		} else if (DISEMBARKING.equals(getPhase())) {
			endMission(SUCCESSFULLY_DISEMBARKED);
		}
	}

	@Override
	protected void performPhase(MissionMember member) {
		super.performPhase(member);
		if (EMBARKING.equals(getPhase())) {
			performEmbarkFromSettlementPhase(member);
		}
		else if (TRAVELLING.equals(getPhase())) {
			performTravelPhase(member);
		}
		else if (DISEMBARKING.equals(getPhase())) {
			performDisembarkToSettlementPhase(member, getCurrentNavpoint()
					.getSettlement());
		}
	}

	/**
	 * Performs the travel phase of the mission.
	 * @param member the mission member currently performing the mission.
	 */
	protected final void performTravelPhase(MissionMember member) {

		NavPoint destination = getNextNavpoint();

		// If vehicle has not reached destination and isn't broken down, travel to destination.
		boolean reachedDestination = vehicle.getCoordinates().equals(
				destination.getLocation());
		boolean malfunction = vehicle.getMalfunctionManager().hasMalfunction();
		if (!reachedDestination && !malfunction) {
		    
		    if (member instanceof Person) {
		        Person person = (Person) member;

		        // Don't operate vehicle if person was the last operator.
		        if (person != lastOperator) {
		            // If vehicle doesn't currently have an operator, set this person as the operator.
		            if (vehicle.getOperator() == null) {
		                if (operateVehicleTask != null) {
		                    operateVehicleTask = getOperateVehicleTask(person,
		                            operateVehicleTask.getTopPhase());
		                } else {
		                    operateVehicleTask = getOperateVehicleTask(person, null);
		                }
		                assignTask(person, operateVehicleTask);
		                lastOperator = person;
		            } else {
		                // If emergency, make sure current operate vehicle task is pointed home.
		                if (!operateVehicleTask.getDestination().equals(
		                        destination.getLocation())) {
		                    operateVehicleTask.setDestination(destination
		                            .getLocation());
		                    setPhaseDescription(Msg.getString("Mission.phase.travelling.description", 
		                            getNextNavpoint().getDescription())); //$NON-NLS-1$
		                }
		            }
		        } else {
		            lastOperator = null;
		        }
		    }
		}

		// If the destination has been reached, end the phase.
		if (reachedDestination) {
			reachedNextNode();
			setPhaseEnded(true);
		}

		// Check if enough resources for remaining trip
		// or if there is an emergency medical problem.
		if (!hasEnoughResourcesForRemainingMission(false) || hasEmergency()) {

			// If not, determine an emergency destination.
			determineEmergencyDestination(member);
		}

		// If vehicle has unrepairable malfunction, end mission.
		if (hasUnrepairableMalfunction()) {
			endMission(UNREPAIRABLE_MALFUNCTION);
		}
	}
	
	/**
	 * Gets a new instance of an OperateVehicle task for the person.
	 * @param member the mission member operating the vehicle.
	 * @return an OperateVehicle task for the person.
	 */
	protected abstract OperateVehicle getOperateVehicleTask(MissionMember member,
			TaskPhase lastOperateVehicleTaskPhase);

	/**
	 * Performs the embark from settlement phase of the mission.
	 * @param member the mission member currently performing the mission.
	 */
	protected abstract void performEmbarkFromSettlementPhase(MissionMember member);

	/**
	 * Performs the disembark to settlement phase of the mission.
	 * @param member the mission member currently performing the mission.
	 * @param disembarkSettlement the settlement to be disembarked to.
	 */
	protected abstract void performDisembarkToSettlementPhase(MissionMember member,
			Settlement disembarkSettlement);

	/**
	 * Gets the estimated time of arrival (ETA) for the current leg of the mission.
	 * @return time (MarsClock) or null if not applicable.
	 */
	public final MarsClock getLegETA() {
		if (TRAVELLING.equals(getPhase()) && (operateVehicleTask != null)) {
			return operateVehicleTask.getETA();
		}
		else {
			return null;
		}
	}

	/**
	 * Gets the estimated time for a trip.
	 * @param useBuffer use time buffers in estimation if true.
	 * @param distance the distance of the trip.
	 * @return time (millisols)
	 * @throws MissionException
	 */
	public final double getEstimatedTripTime(boolean useBuffer, double distance) {

		// Determine average driving speed for all mission members.
		double averageSpeed = getAverageVehicleSpeedForOperators();
		double millisolsInHour = MarsClock.convertSecondsToMillisols(60D * 60D);
		double averageSpeedMillisol = averageSpeed / millisolsInHour;

		double result = distance / averageSpeedMillisol;

		// If buffer, add one sol.
		if (useBuffer) {
			result += 1000D;
		}

		return result;
	}

	/**
	 * Gets the estimated time remaining for the mission.
	 * @param useBuffer Use time buffer in estimations if true.
	 * @return time (millisols)
	 * @throws MissionException
	 */
	public double getEstimatedRemainingMissionTime(boolean useBuffer) {
		return getEstimatedTripTime(useBuffer, getTotalRemainingDistance());
	}

	/**
	 * Gets the average operating speed of the mission vehicle for all of the mission members.
	 * @return average operating speed (km/h)
	 */
	protected final double getAverageVehicleSpeedForOperators() {

	    double result = 0D;
	    
		double totalSpeed = 0D;
		int count = 0;
		Iterator<MissionMember> i = getMembers().iterator();
		while (i.hasNext()) {
		    MissionMember member = i.next();
		    if (member instanceof Person) {
		        totalSpeed += getAverageVehicleSpeedForOperator((Person) member);
		        count++;
		    }
		}

		if (count > 0) {
		    result = totalSpeed / (double) count;
		}
		
		return result;
	}

	/**
	 * Gets the average speed of a vehicle with a given person operating it.
	 * @param operator the vehicle operator.
	 * @return average speed (km/h)
	 */
	private double getAverageVehicleSpeedForOperator(VehicleOperator operator) {
		return OperateVehicle.getAverageVehicleSpeed(vehicle, operator);
	}
//	private double getAverageVehicleSpeedForOperator(Robot robot) {
//		return OperateVehicle.getAverageVehicleSpeed(vehicle, robot);
//	}
	/**
	 * Gets the number and amounts of resources needed for the mission.
	 * @param useBuffer use time buffers in estimation if true.
	 * @return map of amount and item resources and their Double amount or Integer number.
	 */
	public Map<Resource, Number> getResourcesNeededForRemainingMission(
			boolean useBuffer) {
		return getResourcesNeededForTrip(useBuffer, getTotalRemainingDistance());
	}

	/**
	 * Gets the number and amounts of resources needed for a trip.
	 * @param useBuffer use time buffers in estimation if true.
	 * @param distance the distance (km) of the trip.
	 * @return map of amount and item resources and their Double amount or Integer number.
	 */
	public Map<Resource, Number> getResourcesNeededForTrip(boolean useBuffer,
			double distance) {
		Map<Resource, Number> result = new HashMap<Resource, Number>();
		if (vehicle != null) {
			result.put(vehicle.getFuelType(), getFuelNeededForTrip(distance,
					vehicle.getFuelEfficiency(), useBuffer));
		}
		return result;
	}

	/**
	 * Gets the parts needed for the trip.
	 * @param distance the distance of the trip.
	 * @return map of part resources and their number.
	 */
	protected Map<Resource, Number> getPartsNeededForTrip(double distance) {
		Map<Resource, Number> result = new HashMap<Resource, Number>();

		// Determine vehicle parts.
		if (vehicle != null) {
			double drivingTime = getEstimatedTripTime(false, distance);
			double numberAccidents = drivingTime
					* OperateVehicle.BASE_ACCIDENT_CHANCE;
			// Average number malfunctions per accident is two.
			double numberMalfunctions = numberAccidents * 2D;

			Map<Part, Double> parts = vehicle.getMalfunctionManager()
					.getRepairPartProbabilities();
			Iterator<Part> i = parts.keySet().iterator();
			while (i.hasNext()) {
				Part part = i.next();
				int number = (int) Math.round(parts.get(part)
						* numberMalfunctions * PARTS_NUMBER_MODIFIER);
				if (number > 0) {
					result.put(part, number);
				}
			}
		}

		return result;
	}

	/**
	 * Checks if there are enough resources available in the vehicle for the remaining mission.
	 * @param useBuffers use time buffers for estimation if true.
	 * @return true if enough resources.
	 */
	protected final boolean hasEnoughResourcesForRemainingMission(
			boolean useBuffers) {
		return hasEnoughResources(getResourcesNeededForRemainingMission(
				useBuffers));
	}

	/**
	 * Checks if there are enough resources available in the vehicle.
	 * @param neededResources map of amount and item resources and their Double amount or Integer number.
	 * @return true if enough resources.
	 */
	private boolean hasEnoughResources(Map<Resource, Number> neededResources) {
		boolean result = true;

		Inventory inv = vehicle.getInventory();

		Iterator<Resource> iR = neededResources.keySet().iterator();
		while (iR.hasNext() && result) {
			Resource resource = iR.next();
			if (resource instanceof AmountResource) {

			    double amount = (Double) neededResources.get(resource);
			    double amountStored = inv
			            .getAmountResourceStored((AmountResource) resource, false);
			    if (amountStored < amount) {
			        logger.severe(vehicle.getName() + " does not have enough " + resource + 
			                " to continue with " + getName() + " (required: " + amount + 
			                " kg, stored: " + amountStored + " kg)");
			        result = false;
			    }
			} 
			else if (resource instanceof ItemResource) {
				int num = (Integer) neededResources.get(resource);
				int numStored = inv.getItemResourceNum((ItemResource) resource);
				if (numStored < num) {
					logger.severe(vehicle.getName() + " does not have enough " + resource + 
							" to continue with " + getName() + " (required: " + num +
							", stored: " + numStored + ")");
					result = false;
				}
			} else {
				throw new IllegalStateException(getPhase()
						+ " : Unknown resource type: " + resource);
			}
		}

		return result;
	}

	/**
	 * Determines the emergency destination settlement for the mission if one is reachable, 
	 * otherwise sets the emergency beacon and ends the mission.
	 * @param member the mission member performing the mission.
	 */
	protected final void determineEmergencyDestination(MissionMember member) {

		// Determine closest settlement.
		Settlement newDestination = findClosestSettlement();
		if (newDestination != null) {

			// Check if enough resources to get to settlement.
			double distance = getCurrentMissionLocation().getDistance(
					newDestination.getCoordinates());
			if (hasEnoughResources(getResourcesNeededForTrip(false, distance))
					&& !hasEmergencyAllCrew()) {

				// Check if closest settlement is already the next navpoint.
				boolean sameDestination = false;
				NavPoint nextNav = getNextNavpoint();
				if ((nextNav != null) && (newDestination == nextNav.getSettlement())) {
					sameDestination = true;
				}

				if (!sameDestination) {
					logger.severe(vehicle.getName()
							+ " setting emergency destination to "
							+ newDestination.getName() + ".");

					// Creating emergency destination mission event.
					HistoricalEvent newEvent = new MissionHistoricalEvent(
							member, this,
							EventType.MISSION_EMERGENCY_DESTINATION);
					Simulation.instance().getEventManager().registerNewEvent(
							newEvent);

					// Set the new destination as the travel mission's next and final navpoint.
					clearRemainingNavpoints();
					addNavpoint(new NavPoint(newDestination.getCoordinates(),
							newDestination, "emergency destination: "
									+ newDestination.getName()));
					associateAllMembersWithSettlement(newDestination);
					// 2016-09-19 Added updateTravelDestination() below
					updateTravelDestination();
				}
			} else {
				endMission(NOT_ENOUGH_RESOURCES_TO_CONTINUE);

			}
		} else {
			endMission(NO_EMERGENCY_SETTLEMENT_DESTINATION_FOUND);
		}
	}
	
	/**
     * Sets the vehicle's emergency beacon on or off.
     * @param member the mission member performing the mission.
     * @param vehicle the vehicle on the mission.
     * @param beaconOn true if beacon is on, false if not.
     */
	public void setEmergencyBeacon(MissionMember member, Vehicle vehicle, boolean beaconOn) {
	    
		// Creating mission emergency beacon event.
		HistoricalEvent newEvent = new MissionHistoricalEvent(member, this, EventType.MISSION_EMERGENCY_BEACON);
         
		Simulation.instance().getEventManager().registerNewEvent(newEvent);
		if (beaconOn) {
			logger.info("Emergency beacon activated on " + vehicle.getName());
		}
		else {
			logger.info("Emergency beacon deactivated on " + vehicle.getName());
		}

		vehicle.setEmergencyBeacon(beaconOn);

	}
	
	/**
	 * Update mission to the next navpoint destination.
	 */
	public void updateTravelDestination() {
		if (operateVehicleTask != null) {
			operateVehicleTask.setDestination(getNextNavpoint().getLocation());
		}
		setPhaseDescription(Msg.getString("Mission.phase.travelling.description", 
				getNextNavpoint().getDescription())); //$NON-NLS-1$
	}

	/**
	 * Finds the closest settlement to the mission.
	 * @return settlement
	 */
	public final Settlement findClosestSettlement() {
		Settlement result = null;
		Coordinates location = getCurrentMissionLocation();
		double closestDistance = Double.MAX_VALUE;

		Iterator<Settlement> i = Simulation.instance().getUnitManager()
				.getSettlements().iterator();
		while (i.hasNext()) {
			Settlement settlement = i.next();
			double distance = settlement.getCoordinates().getDistance(location);
			if (distance < closestDistance) {
				result = settlement;
				closestDistance = distance;
			}
		}

		return result;
	}

	/**
	 * Gets the total distance travelled during the mission so far.
	 * @return distance (km)
	 */
	public final double getTotalDistanceTravelled() {
		if (vehicle != null) {
			return vehicle.getTotalDistanceTraveled()
					- startingTravelledDistance;
		}
		else {
			return 0D;
		}
	}

	/**
	 * Time passing for mission.
	 * @param time the amount of time passing (in millisols)
	 */
	public void timePassing(double time) {
		// Add this mission as a vehicle listener (does nothing if already listening to vehicle).
		// Note this is needed so that mission will reattach itself as a vehicle listener after deserialization
		// since listener collection is transient. - Scott
		if (hasVehicle() && !vehicle.hasUnitListener(this)) {
			vehicle.addUnitListener(this);
		}
	}

	/**
	 * Catch unit update event.
	 * @param event the unit event.
	 */
	public void unitUpdate(UnitEvent event) {
		UnitEventType type = event.getType();
		if (type == UnitEventType.LOCATION_EVENT) {
			fireMissionUpdate(MissionEventType.DISTANCE_EVENT);
		}
		else if (type == UnitEventType.NAME_EVENT) {
			fireMissionUpdate(MissionEventType.VEHICLE_EVENT);
		}
	}

	/**
	 * Gets the required resources needed for loading the vehicle.
	 * @return resources and their number.
	 */
	public Map<Resource, Number> getRequiredResourcesToLoad() {
		return getResourcesNeededForRemainingMission(true);
	}

	/**
	 * Gets the optional resources needed for loading the vehicle.
	 * @return resources and their number.
	 */
	public Map<Resource, Number> getOptionalResourcesToLoad() {
		return getPartsNeededForTrip(getTotalRemainingDistance());
	}

	/**
	 * Gets the required equipment needed for loading the vehicle.
	 * @return equipment and their number.
	 */
	public Map<Class, Integer> getRequiredEquipmentToLoad() {
		return getEquipmentNeededForRemainingMission(true);
	}

	/**
	 * Gets the optional equipment needed for loading the vehicle.
	 * @return equipment and their number.
	 */
	public Map<Class, Integer> getOptionalEquipmentToLoad() {
	    
	    Map<Class, Integer> result = new HashMap<Class, Integer>();
	    
	    // Add containers needed for optional amount resources.
	    Map<Resource, Number> optionalResources = getOptionalResourcesToLoad();
	    Iterator<Resource> i = optionalResources.keySet().iterator();
	    while (i.hasNext()) {
	        Resource resource = i.next();
	        if (resource instanceof AmountResource) {
	            AmountResource amountResource = (AmountResource) resource;
	            double amount = (double) optionalResources.get(amountResource);
	            Class<? extends Container> containerClass = ContainerUtil.getContainerClassToHoldResource(amountResource);
	            double capacity = ContainerUtil.getContainerCapacity(containerClass);
	            int numContainers = (int) Math.ceil(amount / capacity);
	            
	            if (result.containsKey(containerClass)) {
	                numContainers += result.get(containerClass);
	            }
	            
	            result.put(containerClass, numContainers);
	        }
	    }
	    
		return result;
	}

	/**
	 * Checks if the vehicle has a malfunction that cannot be repaired.
	 * @return true if unrepairable malfunction.
	 */
	private boolean hasUnrepairableMalfunction() {
		boolean result = false;

		if (vehicle != null) {
			vehicle.getMalfunctionManager();
			Iterator<Malfunction> i = vehicle.getMalfunctionManager()
					.getMalfunctions().iterator();
			while (i.hasNext()) {
				Malfunction malfunction = i.next();
				Map<Part, Integer> parts = malfunction.getRepairParts();
				Iterator<Part> j = parts.keySet().iterator();
				while (j.hasNext()) {
					Part part = j.next();
					int number = parts.get(part);
					if (vehicle.getInventory().getItemResourceNum(part) < number) {
						result = true;
					}
				}
			}
		}

		return result;
	}

	/**
	 * Checks to see if there are any currently embarking missions at the settlement.
	 * @param settlement the settlement.
	 * @return true if embarking missions.
	 */
	public static boolean hasEmbarkingMissions(Settlement settlement) {
		boolean result = false;

		MissionManager manager = Simulation.instance().getMissionManager();
		Iterator<Mission> i = manager.getMissionsForSettlement(settlement)
				.iterator();
		while (i.hasNext()) {
			if (EMBARKING.equals(i.next().getPhase())) {
				result = true;
			}
		}

		return result;
	}

	@Override
	public void destroy() {
		super.destroy();

		vehicle = null;
		lastOperator = null;
		operateVehicleTask = null;
		if (equipmentNeededCache != null) {
			equipmentNeededCache.clear();
		}
		equipmentNeededCache = null;
	}
}