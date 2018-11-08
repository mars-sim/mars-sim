/**
 * Mars Simulation Project
 * VehicleMission.java
 * @version 3.1.0 2017-10-21
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitEvent;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.UnitListener;
import org.mars_sim.msp.core.equipment.ContainerUtil;
import org.mars_sim.msp.core.events.HistoricalEvent;
import org.mars_sim.msp.core.malfunction.Malfunction;
import org.mars_sim.msp.core.person.EventType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.LoadVehicleGarage;
import org.mars_sim.msp.core.person.ai.task.OperateVehicle;
import org.mars_sim.msp.core.person.ai.task.TaskPhase;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.goods.GoodsManager;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.StatusType;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.core.vehicle.VehicleOperator;

/**
 * A mission that involves driving a vehicle along a series of navpoints.
 */
public abstract class VehicleMission extends TravelMission implements UnitListener {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(VehicleMission.class.getName());

	private static String sourceName = logger.getName().substring(logger.getName().lastIndexOf(".") + 1,
			logger.getName().length());

	/** Mission phases. */
	final public static MissionPhase APPROVAL = new MissionPhase(Msg.getString("Mission.phase.approval")); //$NON-NLS-1$
	final public static MissionPhase EMBARKING = new MissionPhase(Msg.getString("Mission.phase.embarking")); //$NON-NLS-1$
	final public static MissionPhase TRAVELLING = new MissionPhase(Msg.getString("Mission.phase.travelling")); //$NON-NLS-1$
	final public static MissionPhase DISEMBARKING = new MissionPhase(Msg.getString("Mission.phase.disembarking")); //$NON-NLS-1$

	// Static members

	/** Modifier for number of parts needed for a trip. */
	private static final double PARTS_NUMBER_MODIFIER = 5D;
	/** Estimate number of broken parts per malfunctions */
	private static final double AVERAGE_NUM_MALFUNCTION = 3;

	/** True if vehicle's emergency beacon has been turned on */
	// private boolean isBeaconOn = false;
	/** True if vehicle has been loaded. */
	protected boolean loadedFlag = false;
	/** Vehicle traveled distance at start of mission. */
	private double startingTravelledDistance;
	/** Description of the mission */
	private String description;

	// Data members
	private Vehicle vehicle;
	/** The last operator of this vehicle in the mission. */
	private VehicleOperator lastOperator;

	private MissionMember startingMember;
	/** The current operate vehicle task. */
	private OperateVehicle operateVehicleTask;

	/** Caches */
	protected Map<Integer, Integer> equipmentNeededCache;

	private static MissionManager manager = Simulation.instance().getMissionManager();

	protected VehicleMission(String missionName, MissionMember startingMember, int minPeople) {
		// Use TravelMission constructor.
		super(missionName, startingMember, minPeople);

		description = missionName;
		this.startingMember = startingMember;

		// Add mission phases.
		addPhase(APPROVAL);
		addPhase(EMBARKING);
		addPhase(TRAVELLING);
		addPhase(DISEMBARKING);

		reserveVehicle();

	}

	protected boolean reserveVehicle() {
		// Reserve a vehicle.
		if (!reserveVehicle(startingMember)) {
			endMission(NO_RESERVABLE_VEHICLES);
			return false;
		}
		return true;
	}

	protected VehicleMission(String missionName, MissionMember startingMember, int minPeople, Vehicle vehicle) {
		// Use TravelMission constructor.
		super(missionName, startingMember, minPeople);

		description = missionName;
		this.startingMember = startingMember;

		// Add mission phases.
		addPhase(APPROVAL);
		addPhase(EMBARKING);
		addPhase(TRAVELLING);
		addPhase(DISEMBARKING);

		// Set the vehicle.
		setVehicle(vehicle);
	}

	/**
	 * Gets the mission's vehicle if there is one.
	 * 
	 * @return vehicle or null if none.
	 */
	public final Vehicle getVehicle() {
		return vehicle;
	}

	/**
	 * Sets the vehicle for this mission.
	 * 
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
				throw new IllegalStateException(getPhase() + " : newVehicle is not usable for this mission.");
			}
		} else {
			throw new IllegalArgumentException("newVehicle is null.");
		}
	}

	/**
	 * Checks if the mission has a vehicle.
	 * 
	 * @return true if vehicle.
	 */
	public final boolean hasVehicle() {
		return (vehicle != null);
	}

	/**
	 * Leaves the mission's vehicle and unreserves it.
	 */
	protected final void leaveVehicle() {
		// logger.info("Calling leaveVehicle()");
		if (hasVehicle()) {
			vehicle.setReservedForMission(false);
			vehicle.removeUnitListener(this);
			vehicle = null;
			fireMissionUpdate(MissionEventType.VEHICLE_EVENT);
		}
	}

	/**
	 * Checks if vehicle is usable for this mission. (This method should be added to
	 * by children)
	 * 
	 * @param newVehicle the vehicle to check
	 * @return true if vehicle is usable.
	 * @throws IllegalArgumentException if newVehicle is null.
	 * @throws MissionException         if problem checking vehicle is loadable.
	 */
	protected boolean isUsableVehicle(Vehicle vehicle) {
		if (vehicle != null) {
			boolean usable = true;

			if (vehicle.isReserved())
				usable = false;

			if (vehicle.getStatus() != StatusType.PARKED && vehicle.getStatus() != StatusType.GARAGED)
				usable = false;

			if (vehicle.getInventory().getTotalInventoryMass(false) > 0D)
				usable = false;

			return usable;
		} else {
			throw new IllegalArgumentException("isUsableVehicle: newVehicle is null.");
		}
	}

	/**
	 * Compares the quality of two vehicles for use in this mission. (This method
	 * should be added to by children)
	 * 
	 * @param firstVehicle  the first vehicle to compare
	 * @param secondVehicle the second vehicle to compare
	 * @return -1 if the second vehicle is better than the first vehicle, 0 if
	 *         vehicle are equal in quality, and 1 if the first vehicle is better
	 *         than the second vehicle.
	 * @throws MissionException if error determining vehicle range.
	 */
	protected int compareVehicles(Vehicle firstVehicle, Vehicle secondVehicle) {
		if (isUsableVehicle(firstVehicle)) {
			if (isUsableVehicle(secondVehicle)) {
				return 0;
			} else {
				return 1;
			}
		} else {
			if (isUsableVehicle(secondVehicle)) {
				return -1;
			} else {
				return 0;
			}
		}
	}

	/**
	 * Reserves a vehicle for the mission if possible.
	 * 
	 * @param person the person reserving the vehicle.
	 * @return true if vehicle is reserved, false if unable to.
	 * @throws MissionException if error reserving vehicle.
	 */
	protected final boolean reserveVehicle(MissionMember member) {

		Collection<Vehicle> bestVehicles = new ConcurrentLinkedQueue<Vehicle>();
		if (member.getSettlement() == null)
			return false;
		Collection<Vehicle> vList = getAvailableVehicles(member.getSettlement());
		// Create list of best unreserved vehicles for the mission.

		if (vList == null || vList.isEmpty()) {
			return false;
		} else {
			for (Vehicle v : vList) {
				if (bestVehicles.size() > 0) {
					int comparison = compareVehicles(v, (Vehicle) bestVehicles.toArray()[0]);
					if (comparison == 0) {
						bestVehicles.add(v);
					} else if (comparison == 1) {
						bestVehicles.clear();
						bestVehicles.add(v);
					}
				} else
					bestVehicles.add(v);
			}

			// Randomly select from the best vehicles.
			if (bestVehicles.size() > 0) {
				int bestVehicleIndex = RandomUtil.getRandomInt(bestVehicles.size() - 1);
				try {
					setVehicle((Vehicle) bestVehicles.toArray()[bestVehicleIndex]);
				} catch (Exception e) {
				}
			}

			return hasVehicle();
		}
	}

	/**
	 * Gets a collection of available vehicles at a settlement that are usable for
	 * this mission.
	 * 
	 * @param settlement the settlement to find vehicles.
	 * @return list of available vehicles.
	 * @throws MissionException if problem determining if vehicles are usable.
	 */
	private Collection<Vehicle> getAvailableVehicles(Settlement settlement) {
		Collection<Vehicle> result = new ConcurrentLinkedQueue<Vehicle>();
		Collection<Vehicle> vList = settlement.getParkedVehicles();
		if (vList != null && !vList.isEmpty()) {
			for (Vehicle v : vList) {
				if (isUsableVehicle(v)) {
					result.add(v);
				}
			}
		}
		return result;
	}

	/**
	 * Finalizes the mission
	 * 
	 * @param reason the reason of ending the mission.
	 */

	public void endMission(String reason) {
		// logger.info("Reason : " + reason);
		if (hasVehicle()) {
			// if user hit the "End Mission" button to abort the mission
			// Check if user aborted the mission and if
			// the vehicle has been disembarked.
			if (reason.equals(Mission.USER_ABORTED_MISSION)) {
				if (vehicle.getSettlement() == null) { // if the vehicle has not arrived or departed a settlement
					String s = null;
					if (description.startsWith("A") || description.startsWith("I")) {
						s = " an " + description;
					} else
						s = " a " + description;
					logger.info(
							"User just aborted" + s + ". Switching to emergency mode to go to the nearest settlement.");
					// will recursively call endMission() with a brand new "reason"
					determineEmergencyDestination(startingMember);
				}

				setPhaseEnded(true);
				super.endMission(reason);
				
//				 if (EMBARKING.equals(getPhase())) { 
//					 setPhaseEnded(true); 
//				 }
//
//				 else if (TRAVELLING.equals(getPhase())) { 
//					 setPhaseEnded(true); 
//				 }
//				 
//				 else if (DISEMBARKING.equals(getPhase())) { 
//					 logger.info("Can't be aborted. This mission is at the very last phase of the mission. "
//							 + "Members are unloading resources and being disembarked. Please be patient!");
//				 }
				 //else { // setPhaseEnded(true); // super.endMission(reason); //} 
			}

			else if (reason.equals(Mission.NOT_ENOUGH_RESOURCES) || reason.equals(Mission.UNREPAIRABLE_MALFUNCTION)
					|| reason.equals(Mission.NO_EMERGENCY_SETTLEMENT_DESTINATION_FOUND)
					|| reason.equals(Mission.MEDICAL_EMERGENCY)) {
				// Set emergency beacon if vehicle is not at settlement.
				// TODO: need to find out if there are other matching reasons for setting
				// emergency beacon.
				if (vehicle.getSettlement() == null) {
					// if the vehicle somewhere on Mars and is outside the settlement vicinity
					if (!vehicle.isBeaconOn()) {
						// if the emergency beacon is off
						// Question: could the emergency beacon itself be broken ?
						LogConsolidated.log(logger, Level.WARNING, 0, sourceName,
								"[" + startingMember.getLocationTag().getQuickLocation() + "] " + startingMember
										+ " turned on " + vehicle
										+ "'s emergency beacon and request for towing. Reason : " + reason,
								null);
						vehicle.setEmergencyBeacon(true);

						if (!vehicle.isBeingTowed()) {

//							if (reason.equals(Mission.NOT_ENOUGH_RESOURCES)) {
//								LogConsolidated.log(logger, Level.WARNING, 5000, sourceName, 
//										"[" + startingMember.getLocationTag().getShortLocationName() + "] " 
//										+ startingMember + " turned on " + vehicle + "'s emergency beacon and request for towing. Reason : "
//										+ reason, null);
//							}
//							else {
//								setEmergencyBeacon(startingMember, vehicle, true, reason);
//								logger.warning("[" + startingMember.getLocationTag().getShortLocationName() + "] " 
//										+ startingMember + " turned on " + vehicle + "'s emergency beacon and request for towing. Reason : "
//										+ reason);
//								// Note : don't end the mission yet
//							}
						}

						else {
							// Note: the vehicle is being towed, wait till the journey is over
							// don't end the mission yet
//							 logger.info(vehicle + " is currently being towed by " +
//							 vehicle.getTowingVehicle());
							LogConsolidated.log(logger, Level.WARNING, 2000, sourceName,
									"[" + vehicle + "] Currently being towed by " + vehicle.getTowingVehicle(), null);
//									+ " Remaining distance : " + getClosestDistance() + " km.", null);
						}
					}

					else {
						// Note : if the emergency beacon is on, don't end the mission yet
//						 logger.info(vehicle + "'s emergency beacon is on. awaiting the response for
//						 rescue right now.");
					}
				}

				else { // e.g. unrepairable malfunction
					logger.info(vehicle.getName() + " is currently at " + vehicle.getSettlement()
							+ " and its mission ended. Reason : " + reason);
					// if the vehicle is still somewhere inside the settlement when it got broken
					// down
					// TODO: wait till the repair is done and the mission may resume ?!?
					leaveVehicle();
					setPhaseEnded(true);
					super.endMission(reason);
				}
			} // end if for the 4 different reasons

			else {
				// for ALL OTHER REASONS
				// setPhaseEnded(true); // TODO: will setPhaseEnded cause NullPointerException ?
				leaveVehicle();
				setPhaseEnded(true);
				super.endMission(reason);
			}
		}

		else if (reason.equals(Mission.SUCCESSFULLY_DISEMBARKED)) {
			// logger.info("Returning the control of " + vehicle + " to the settlement");
			setPhaseEnded(true);
			// leaveVehicle();
			super.endMission(reason);
		}

		else { // if vehicles are NOT available
				// Questions : what are the typical cases here ?
				// logger.info("No vehicle is available. reason for ending the mission : " +
				// reason);
				// setPhaseEnded(true); // TODO: will setPhaseEnded cause NullPointerException ?

			// Case : if a vehicle is parked at a settlement and had an accident and was
			// repaired,
			// somehow this mission did not end and the Mission Tool shows the Regolith
			// mission was still on-going
			// and the occupants did not leave the vehicle.
			setPhaseEnded(true);
			super.endMission(reason);

		}
	}

	/**
	 * Determine if a vehicle is sufficiently loaded with fuel and supplies.
	 * 
	 * @return true if rover is loaded.
	 * @throws MissionException if error checking vehicle.
	 */
	public final boolean isVehicleLoaded() {
		if (vehicle == null) {
			throw new IllegalStateException(getPhase() + " : vehicle is null");
		}

		try {
			return LoadVehicleGarage.isFullyLoaded(getRequiredResourcesToLoad(), getOptionalResourcesToLoad(),
					getRequiredEquipmentToLoad(), getOptionalEquipmentToLoad(), vehicle, vehicle.getSettlement());
		} catch (Exception e) {
			throw new IllegalStateException(getPhase().getName(), e);
		}
	}

	/**
	 * Checks if a vehicle can load the supplies needed by the mission.
	 * 
	 * @return true if vehicle is loadable.
	 * @throws Exception if error checking vehicle.
	 */
	public final boolean isVehicleLoadable() {

		Map<Integer, Number> resources = getRequiredResourcesToLoad();
		Map<Integer, Integer> equipment = getRequiredEquipmentToLoad();
		Vehicle vehicle = this.vehicle;
		Settlement settlement = vehicle.getSettlement();
		double tripTime = getEstimatedRemainingMissionTime(true);

		boolean vehicleCapacity = LoadVehicleGarage.enoughCapacityForSupplies(resources, equipment, vehicle,
				settlement);
		boolean settlementSupplies = LoadVehicleGarage.hasEnoughSupplies(settlement, vehicle, resources, equipment,
				getPeopleNumber(), tripTime);
		if (!vehicleCapacity) {
			LogConsolidated.log(logger, Level.WARNING, 5000, sourceName,
					"[ " + vehicle.getName() + " ] Doesn't have enough capacity for the proposed excursion.", null);
		}
		if (!settlementSupplies) {
			LogConsolidated.log(logger, Level.WARNING, 5000, sourceName,
					"[ " + settlement.getName() + " ] Doesn't have supplies for the proposed excursion.", null);
		}

		return vehicleCapacity && settlementSupplies;
	}

	/**
	 * Gets the amount of fuel (kg) needed for a trip of a given distance (km).
	 * 
	 * @param tripDistance   the distance (km) of the trip.
	 * @param fuelEfficiency the vehicle's fuel efficiency (km/kg).
	 * @param useMargin      use time buffers in estimation if true.
	 * @return amount of fuel needed for trip (kg)
	 */
	public static double getFuelNeededForTrip(double tripDistance, double fuelEfficiency, boolean useMargin) {
		double result = tripDistance / fuelEfficiency;
		if (useMargin) {
			result *= Vehicle.getErrorMargin();
		}

		return result;
	}

	/**
	 * Determines a new phase for the mission when the current phase has ended.
	 * 
	 * @throws MissionException if problem setting a new phase.
	 */
	protected void determineNewPhase() {
		if (APPROVAL.equals(getPhase())) {
			//startTravelToNextNode();
			setPhase(VehicleMission.EMBARKING);
			setPhaseDescription(
					Msg.getString("Mission.phase.embarking.description", getCurrentNavpoint().getDescription()));//startingMember.getSettlement().toString())); // $NON-NLS-1$
		}
		
		else if (EMBARKING.equals(getPhase())) {
			startTravelToNextNode();
			setPhase(VehicleMission.TRAVELLING);
			setPhaseDescription(
					Msg.getString("Mission.phase.travelling.description", getNextNavpoint().getDescription())); // $NON-NLS-1$
		} 
		
		else if (TRAVELLING.equals(getPhase())) {
			if (getCurrentNavpoint().isSettlementAtNavpoint()) {
				setPhase(VehicleMission.DISEMBARKING);
				setPhaseDescription(
						Msg.getString("Mission.phase.disembarking.description", getCurrentNavpoint().getDescription())); // $NON-NLS-1$
			}
		} 
		
		else if (DISEMBARKING.equals(getPhase())) {
			endMission(SUCCESSFULLY_DISEMBARKED);
		}
	}

	@Override
	protected void performPhase(MissionMember member) {
		super.performPhase(member);
		if (APPROVAL.equals(getPhase())) {
			requestApprovalPhase(member);
		}
		else if (EMBARKING.equals(getPhase())) {
			performEmbarkFromSettlementPhase(member);
		} 
		else if (TRAVELLING.equals(getPhase())) {
			performTravelPhase(member);
		} 
		else if (DISEMBARKING.equals(getPhase())) {
			performDisembarkToSettlementPhase(member, getCurrentNavpoint().getSettlement());
		}
	}

	/**
	 * Performs the travel phase of the mission.
	 * 
	 * @param member the mission member currently performing the mission.
	 */
	protected final void performTravelPhase(MissionMember member) {

		NavPoint destination = getNextNavpoint();

		// If vehicle has not reached destination and isn't broken down, travel to
		// destination.
		boolean reachedDestination = false;
		boolean malfunction = false;
		// Avoid NullPointerException by checking if vehicle/destination is
		// null
		if (vehicle != null && destination != null) {
			if (vehicle.getCoordinates() != null && destination.getLocation() != null) {
				reachedDestination = vehicle.getCoordinates().equals(destination.getLocation());
			}

			malfunction = vehicle.getMalfunctionManager().hasMalfunction();
		}

		if (!reachedDestination && !malfunction) {

			if (member instanceof Person) {
				Person person = (Person) member;

				// Drivers should rotate. Filter out this person if he/she was the last
				// operator.
				if (person != lastOperator && vehicle != null) {
					// If vehicle doesn't currently have an operator, set this person as the
					// operator.
					if (vehicle.getOperator() == null) {
						if (operateVehicleTask != null) {
							operateVehicleTask = getOperateVehicleTask(person, operateVehicleTask.getTopPhase());
						} else {
							operateVehicleTask = getOperateVehicleTask(person, null);
						}

						if (operateVehicleTask != null) {
							assignTask(person, operateVehicleTask);
							lastOperator = person;
						}
					}

					else {
						// If emergency, make sure current operate vehicle task is pointed home.
						if (operateVehicleTask != null
								&& !operateVehicleTask.getDestination().equals(destination.getLocation())) {
							operateVehicleTask.setDestination(destination.getLocation());
							setPhaseDescription(Msg.getString("Mission.phase.travelling.description",
									getNextNavpoint().getDescription())); // $NON-NLS-1$
						}
					}
				}

				else {
					lastOperator = null;
				}
			}
		}

		// If the destination has been reached, end the phase.
		if (reachedDestination) {
			reachedNextNode();
			setPhaseEnded(true);
		}

		// if there is an emergency medical problem or not enough resources for
		// remaining trip
		// if (hasEmergency() ||
		if (!hasEnoughResourcesForRemainingMission(false)) {
			// If not, determine an emergency destination.
			determineEmergencyDestination(member);
//			setPhaseEnded(true);
		}

		// If vehicle has unrepairable malfunction, end mission.
		if (hasUnrepairableMalfunction()) {
			endMission(UNREPAIRABLE_MALFUNCTION);
		}
	}

	/**
	 * Gets a new instance of an OperateVehicle task for the person.
	 * 
	 * @param member the mission member operating the vehicle.
	 * @return an OperateVehicle task for the person.
	 */
	protected abstract OperateVehicle getOperateVehicleTask(MissionMember member,
			TaskPhase lastOperateVehicleTaskPhase);

	/**
	 * Performs the embark from settlement phase of the mission.
	 * 
	 * @param member the mission member currently performing the mission.
	 */
	protected abstract void performEmbarkFromSettlementPhase(MissionMember member);

	/**
	 * Obtains approval from the commander of the settlement for the mission.
	 * 
	 * @param member the mission member currently performing the mission.
	 */	
	protected void requestApprovalPhase(MissionMember member) {
		super.requestApprovalPhase(member);	
	}
	
	/**
	 * Performs the disembark to settlement phase of the mission.
	 * 
	 * @param member              the mission member currently performing the
	 *                            mission.
	 * @param disembarkSettlement the settlement to be disembarked to.
	 */
	protected abstract void performDisembarkToSettlementPhase(MissionMember member, Settlement disembarkSettlement);

	/**
	 * Gets the estimated time of arrival (ETA) for the current leg of the mission.
	 * 
	 * @return time (MarsClock) or null if not applicable.
	 */
	public final MarsClock getLegETA() {
		if (TRAVELLING.equals(getPhase()) && (operateVehicleTask != null)) {
			return operateVehicleTask.getETA();
		} else {
			return null;
		}
	}

	/**
	 * Gets the estimated time for a trip.
	 * 
	 * @param useMargin use time buffers in estimation if true.
	 * @param distance  the distance of the trip.
	 * @return time (millisols)
	 * @throws MissionException
	 */
	public final double getEstimatedTripTime(boolean useMargin, double distance) {

		// Determine average driving speed for all mission members.
		double averageSpeed = getAverageVehicleSpeedForOperators();
		double millisolsInHour = MarsClock.convertSecondsToMillisols(60D * 60D);
		double averageSpeedMillisol = averageSpeed / millisolsInHour;

		double result = distance / averageSpeedMillisol;

		// If buffer, add one sol.
		if (useMargin) {
			result += 1000D;
		}

		return result;
	}

	/**
	 * Gets the estimated time remaining for the mission.
	 * 
	 * @param useMargin Use time buffer in estimations if true.
	 * @return time (millisols)
	 * @throws MissionException
	 */
	public double getEstimatedRemainingMissionTime(boolean useMargin) {
		return getEstimatedTripTime(useMargin, getTotalRemainingDistance());
	}

	/**
	 * Gets the average operating speed of the mission vehicle for all of the
	 * mission members.
	 * 
	 * @return average operating speed (km/h)
	 */
	protected final double getAverageVehicleSpeedForOperators() {

		double result = 0D;

		double totalSpeed = 0D;
		int count = 0;
		// Iterator<MissionMember> i = getMembers().iterator();
		// while (i.hasNext()) {
		for (MissionMember member : getMembers()) {// = i.next();
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
	 * 
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
	 * 
	 * @param useMargin True if estimating trip. False if calculating remaining
	 *                  trip.
	 * @return map of amount and item resources and their Double amount or Integer
	 *         number.
	 */
	public Map<Integer, Number> getResourcesNeededForRemainingMission(boolean useMargin) {
		return getResourcesNeededForTrip(useMargin, getTotalRemainingDistance());
	}

	/**
	 * Gets the number and amounts of resources needed for a trip.
	 * 
	 * @param useMargin True if estimating trip. False if calculating remaining
	 *                  trip.
	 * @param distance  the distance (km) of the trip.
	 * @return map of amount and item resources and their Double amount or Integer
	 *         number.
	 */
	public Map<Integer, Number> getResourcesNeededForTrip(boolean useMargin, double distance) {
		Map<Integer, Number> result = new HashMap<Integer, Number>();
		if (vehicle != null) {
			result.put(vehicle.getFuelType(), getFuelNeededForTrip(distance,
					vehicle.getDrivetrainEfficiency() * GoodsManager.SOFC_CONVERSION_EFFICIENCY, useMargin));
		}
		return result;
	}

	/**
	 * Gets the parts needed for the trip.
	 * 
	 * @param distance the distance of the trip.
	 * @return map of part resources and their number.
	 */
	protected Map<Integer, Number> getPartsNeededForTrip(double distance) {
		Map<Integer, Number> result = new HashMap<Integer, Number>();

		// Determine vehicle parts.
		if (vehicle != null) {
			double drivingTime = getEstimatedTripTime(false, distance);
			double numberAccidents = drivingTime * OperateVehicle.BASE_ACCIDENT_CHANCE;
			// Average number malfunctions per accident is 3.
			double numberMalfunctions = numberAccidents * AVERAGE_NUM_MALFUNCTION;

			Map<Integer, Double> parts = vehicle.getMalfunctionManager().getRepairPartProbabilities();
			for (Integer part : parts.keySet()) {
				int number = (int) Math.round(parts.get(part) * numberMalfunctions * PARTS_NUMBER_MODIFIER);
				if (number > 0) {
					result.put(part, number);
				}
			}
		}

		return result;
	}

	/**
	 * Checks if there are enough resources available in the vehicle for the
	 * remaining mission.
	 * 
	 * @param useMargin True if estimating trip. False if calculating remaining
	 *                  trip.
	 * @return true if enough resources.
	 */
	protected final boolean hasEnoughResourcesForRemainingMission(boolean useMargin) {
		return hasEnoughResources(getResourcesNeededForRemainingMission(useMargin));
	}

	/**
	 * Checks if there are enough resources available in the vehicle.
	 * 
	 * @param neededResources map of amount and item resources and their Double
	 *                        amount or Integer number.
	 * @return true if enough resources.
	 */
	private boolean hasEnoughResources(Map<Integer, Number> neededResources) {
		boolean result = true;

		if (vehicle != null) {
			Inventory inv = vehicle.getInventory();

			for (Integer resource : neededResources.keySet()) {
				if (resource < MAX_AMOUNT_RESOURCE) {

					double amount = (Double) neededResources.get(resource);
					double amountStored = inv.getARStored(resource, false);

					if (amountStored < amount) {
						String newLog = vehicle.getName() + " does not have enough " 
								+ ResourceUtil.findAmountResource(resource).getName() + " to continue with "
								+ getName() + " (Required: " + Math.round(amount * 100D) / 100D + " kg  Stored: "
								+ Math.round(amountStored * 100D) / 100D + " kg).";
						LogConsolidated.log(logger, Level.WARNING, 10000, sourceName, newLog, null);
						result = false;
					}
				}

				else if (resource >= MAX_AMOUNT_RESOURCE) {
					int num = (Integer) neededResources.get(resource);
					int numStored = inv.getItemResourceNum(resource);

					if (numStored < num) {
						String newLog = vehicle.getName() + " does not have enough " 
								+ ResourceUtil.findAmountResource(resource).getName() + " to continue with "
								+ getName() + " (Required: " + num + "  Stored: " + numStored + ").";
						LogConsolidated.log(logger, Level.WARNING, 10000, sourceName, newLog, null);
						result = false;
					}
				}

				else {
					throw new IllegalStateException(getPhase() + " : issues with the resource type of " 
							+ ResourceUtil.findAmountResource(resource).getName());
				}
			}

		}
		return result;
	}

	protected double getClosestDistance() {

		return getCurrentMissionLocation().getDistance(findClosestSettlement().getCoordinates());
	}

	/**
	 * Determines the emergency destination settlement for the mission if one is
	 * reachable, otherwise sets the emergency beacon and ends the mission.
	 * 
	 * @param member the mission member performing the mission.
	 */
	protected final void determineEmergencyDestination(MissionMember member) {

		boolean hasMedicalEmergency = false;
		Person person = (Person) member;

		if ((member instanceof Person && ((Person) member).getPhysicalCondition().hasSeriousMedicalProblems())
				|| hasEmergencyAllCrew()) {
			hasMedicalEmergency = true;
			// Creating medical emergency mission event.
			HistoricalEvent newEvent = new MissionHistoricalEvent(EventType.MISSION_MEDICAL_EMERGENCY, this,
					person.getName() + " had " + person.getPhysicalCondition().getHealthSituation(), this.getName(),
					member.getName(), member.getVehicle().getName(), member.getLocationTag().getLocale());
			Simulation.instance().getEventManager().registerNewEvent(newEvent);
		} else {
			// Creating 'Not enough resources' mission event.
			HistoricalEvent newEvent = new MissionHistoricalEvent(EventType.MISSION_NOT_ENOUGH_RESOURCES, this,
					"Dwindling resource(s)", this.getName(), member.getName(), member.getVehicle().getName(),
					member.getLocationTag().getLocale());
			Simulation.instance().getEventManager().registerNewEvent(newEvent);
		}


		Settlement oldHome = ((Person) member).getAssociatedSettlement();
		
		double oldDistance = getCurrentMissionLocation().getDistance(oldHome.getCoordinates());

		
		// Determine closest settlement.
		Settlement newDestination = findClosestSettlement();
		if (newDestination != null) {

			double newDistance = getCurrentMissionLocation().getDistance(newDestination.getCoordinates());

			// Check if enough resources to get to settlement.
			if (newDistance > 0 && hasEnoughResources(getResourcesNeededForTrip(false, newDistance))) {

				double newTripTime = getEstimatedTripTime(false, newDistance);
				
				// Check !hasEmergencyAllCrew() ? 

				// Check if closest settlement is already the next navpoint.
				boolean sameDestination = false;
				NavPoint nextNav = getNextNavpoint();

				if ((nextNav != null) && (newDestination == nextNav.getSettlement())) {
					sameDestination = true;

					LogConsolidated.log(logger, Level.WARNING, 5000, sourceName,
							"[" + vehicle.getName() + "]  Home Settlement (" + newDestination.getName() + ") : " 
							+ Math.round(newDistance * 100D) / 100D
							+ " km    Duration : " + Math.round(newTripTime * 100.0 / 1000.0) / 100.0 + " sols",
							null);
					
					returnHome();

				}

				if (!sameDestination) {

					LogConsolidated.log(logger, Level.WARNING, 5000, sourceName,
							"[" + vehicle.getName() + "]  Home Settlement (" + oldHome.getName() + ") : " 
							+ Math.round(oldDistance * 100D) / 100D
							+ " km    Nearest Settlement (" + newDestination.getName() + ") : " 
							+ Math.round(newDistance * 100D) / 100D
							+ " km    Duration : " + Math.round(newTripTime * 100.0 / 1000.0) / 100.0 + " sols",
							null);

					// Creating emergency destination mission event.
					HistoricalEvent newEvent = new MissionHistoricalEvent(EventType.MISSION_EMERGENCY_DESTINATION, this,
							"Dwindling Resource(s)", this.getName(), member.getName(), member.getVehicle().getName(),
							member.getLocationTag().getLocale());
					Simulation.instance().getEventManager().registerNewEvent(newEvent);

					// Note: use Mission.goToNearestSettlement() as reference

					// Set the new destination as the travel mission's next and final navpoint.
					clearRemainingNavpoints();
					addNavpoint(new NavPoint(newDestination.getCoordinates(), newDestination,
							"emergency destination: " + newDestination.getName()));
					// each member to switch the associated settlement to the new destination
					associateAllMembersWithSettlement(newDestination);
					// Added updateTravelDestination() below
					updateTravelDestination();
					endCollectionPhase();
				}

			} else if (newDistance > 0 && !hasEnoughResources(getResourcesNeededForTrip(false, newDistance * 2 / 3))
					&& hasEnoughResources(getResourcesNeededForTrip(false, newDistance * 1 / 3))) {

				// if it has enough resources to traverse between 2/3 and 1/3 of the distance
				// toward the new destination
				double newTripTime = getEstimatedTripTime(false, newDistance * 2 / 3);

				// && !hasEmergencyAllCrew()) {

				// Check if closest settlement is already the next navpoint.
				boolean sameDestination = false;
				NavPoint nextNav = getNextNavpoint();

				if ((nextNav != null) && (newDestination == nextNav.getSettlement())) {
					sameDestination = true;

					LogConsolidated.log(logger, Level.WARNING, 5000, sourceName,
							"[" + vehicle.getName() 
							+ "]  Home Settlement (" + newDestination.getName() + ") : " 
							+ Math.round(newDistance * 2 / 3 * 100D) / 100D 
							+ " km    Duration : "
							+ Math.round(newTripTime * 100.0 / 1000.0) / 100.0 + " sols",
							null);

					returnHome();
				}

				if (!sameDestination) {

					LogConsolidated.log(logger, Level.WARNING, 5000, sourceName,
							"[" + vehicle.getName() 
							+ "]  Home Settlement (" + oldHome.getName() + ") : " 
							+ Math.round(oldDistance * 100D) / 100D
							+ " km    Next Routing Stop : " + Math.round(newDistance * 2 / 3 * 100D) / 100D
							+ " km    Duration : " + Math.round(newTripTime * 100.0 / 1000.0) / 100.0 + " sols",
							null);

					// Creating emergency destination mission event.
					HistoricalEvent newEvent = new MissionHistoricalEvent(EventType.MISSION_EMERGENCY_DESTINATION, this,
							"Dwindling Resource(s)", this.getName(), member.getName(), member.getVehicle().getName(),
							member.getLocationTag().getLocale());
					Simulation.instance().getEventManager().registerNewEvent(newEvent);

					// Set the new destination as the travel mission's next and final navpoint.
					clearRemainingNavpoints();
					addNavpoint(new NavPoint(newDestination.getCoordinates(), newDestination,
							"emergency destination: " + newDestination.getName()));

					// each member to switch the associated settlement to the new destination
					associateAllMembersWithSettlement(newDestination);
					updateTravelDestination();
					endCollectionPhase();
				}

			} else {
				// Don't have enough resources and can't go anywhere, turn on beacon next
				if (hasMedicalEmergency)
					endMission(MEDICAL_EMERGENCY);
				else
					endMission(NOT_ENOUGH_RESOURCES);
			}

		} else { // newDestination is null. Can't find a destination
			if (hasMedicalEmergency)
				endMission(MEDICAL_EMERGENCY);
			else
				endMission(NO_EMERGENCY_SETTLEMENT_DESTINATION_FOUND);

		}
	}

	/**
	 * Sets the vehicle's emergency beacon on or off.
	 * 
	 * @param member   the mission member performing the mission.
	 * @param vehicle  the vehicle on the mission.
	 * @param beaconOn true if beacon is on, false if not.
	 */
	public void setEmergencyBeacon(MissionMember member, Vehicle vehicle, boolean beaconOn, String reason) {

		if (beaconOn) {
			// Creating mission emergency beacon event.
			HistoricalEvent newEvent = new MissionHistoricalEvent(EventType.MISSION_EMERGENCY_BEACON_ON, this, reason,
					this.getName(), member.getName(), member.getVehicle().getName(),
					member.getLocationTag().getLocale());

			Simulation.instance().getEventManager().registerNewEvent(newEvent);
			logger.info("[" + vehicle.getLocationTag().getQuickLocation() + "] " + member
					+ " activated emergency beacon on " + vehicle.getName() + ".");
		} else {
			logger.info("[" + vehicle.getLocationTag().getQuickLocation() + "] " + member
					+ " deactivated emergency beacon on " + vehicle.getName() + ".");
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
		setPhaseDescription(Msg.getString("Mission.phase.travelling.description", getNextNavpoint().getDescription())); // $NON-NLS-1$
	}

	/**
	 * Finds the closest settlement to the mission.
	 * 
	 * @return settlement
	 */
	public final Settlement findClosestSettlement() {
		Settlement result = null;
		Coordinates location = getCurrentMissionLocation();
		double closestDistance = Double.MAX_VALUE;

		for (Settlement settlement : Simulation.instance().getUnitManager().getSettlements()) {
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
	 * 
	 * @return distance (km)
	 */
	public final double getTotalDistanceTravelled() {
		if (vehicle != null) {
			return vehicle.getTotalDistanceTraveled() - startingTravelledDistance;
		} else {
			return 0D;
		}
	}

	/**
	 * Time passing for mission.
	 * 
	 * @param time the amount of time passing (in millisols)
	 */
	public void timePassing(double time) {
		// Add this mission as a vehicle listener (does nothing if already listening to
		// vehicle).
		// Note : this is needed so that mission will re-attach itself as a vehicle
		// listener after deserialization
		// since listener collection is transient. - Scott
		if (hasVehicle() && !vehicle.hasUnitListener(this)) {
			vehicle.addUnitListener(this);
		}
	}

	/**
	 * Catch unit update event.
	 * 
	 * @param event the unit event.
	 */
	public void unitUpdate(UnitEvent event) {
		UnitEventType type = event.getType();
		if (type == UnitEventType.LOCATION_EVENT) {
			fireMissionUpdate(MissionEventType.DISTANCE_EVENT);
		} else if (type == UnitEventType.NAME_EVENT) {
			fireMissionUpdate(MissionEventType.VEHICLE_EVENT);
		}
	}

	/**
	 * Gets the required resources needed for loading the vehicle.
	 * 
	 * @return resources and their number.
	 */
	public Map<Integer, Number> getRequiredResourcesToLoad() {
		return getResourcesNeededForRemainingMission(true);
	}

	/**
	 * Gets the optional resources needed for loading the vehicle.
	 * 
	 * @return resources and their number.
	 */
	public Map<Integer, Number> getOptionalResourcesToLoad() {
		return getPartsNeededForTrip(getTotalRemainingDistance());
	}

	/**
	 * Gets the required equipment needed for loading the vehicle.
	 * 
	 * @return equipment and their number.
	 */
	public Map<Integer, Integer> getRequiredEquipmentToLoad() {
		return getEquipmentNeededForRemainingMission(true);
	}

	/**
	 * Gets the optional equipment needed for loading the vehicle.
	 * 
	 * @return equipment and their number.
	 */
	public Map<Integer, Integer> getOptionalEquipmentToLoad() {

		Map<Integer, Integer> result = new HashMap<>();

		// Add containers needed for optional amount resources.
		Map<Integer, Number> optionalResources = getOptionalResourcesToLoad();
		Iterator<Integer> i = optionalResources.keySet().iterator();
		while (i.hasNext()) {
			int resource = i.next();

			if (resource < MAX_AMOUNT_RESOURCE) {
				// AmountResource amountResource = (AmountResource) resource;
				double amount = (double) optionalResources.get(resource);
				// Class<? extends Container> containerClass =
				// ContainerUtil.getContainerClassToHoldResource(resource);
				int containerID = ContainerUtil.getContainerClassIDToHoldResource(resource);
				double capacity = ContainerUtil.getContainerCapacity(resource);
				int numContainers = (int) Math.ceil(amount / capacity);

//	            int id = EquipmentType.str2int(containerClass.getClass().getName());

				if (result.containsKey(containerID)) {
					numContainers += (int) (result.get(resource));
				}

				result.put(containerID, numContainers);
			}
		}

		return result;
	}

	/**
	 * Checks if the vehicle has a malfunction that cannot be repaired.
	 * 
	 * @return true if unrepairable malfunction.
	 */
	private boolean hasUnrepairableMalfunction() {
		boolean result = false;

		if (vehicle != null) {
			vehicle.getMalfunctionManager();
			Iterator<Malfunction> i = vehicle.getMalfunctionManager().getMalfunctions().iterator();
			while (i.hasNext()) {
				Malfunction malfunction = i.next();
				Map<Integer, Integer> parts = malfunction.getRepairParts();
				Iterator<Integer> j = parts.keySet().iterator();
				while (j.hasNext()) {
					Integer part = j.next();
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
	 * Checks to see if there are any currently embarking missions at the
	 * settlement.
	 * 
	 * @param settlement the settlement.
	 * @return true if embarking missions.
	 */
	public static boolean hasEmbarkingMissions(Settlement settlement) {
		boolean result = false;
//		MissionManager manager = Simulation.instance().getMissionManager();
		if (manager == null)
			manager = Simulation.instance().getMissionManager();
		Iterator<Mission> i = manager.getMissionsForSettlement(settlement).iterator();
		while (i.hasNext()) {
			if (EMBARKING.equals(i.next().getPhase())) {
				result = true;
				break;
			}
		}

		return result;
	}

	/**
	 * Checks to see how many currently embarking missions at the settlement.
	 * 
	 * @param settlement the settlement.
	 * @return true if embarking missions.
	 */
	public static int numEmbarkingMissions(Settlement settlement) {
		int result = 0;
		if (manager == null)
			manager = Simulation.instance().getMissionManager();
		Iterator<Mission> i = manager.getMissionsForSettlement(settlement).iterator();
		while (i.hasNext()) {
			if (EMBARKING.equals(i.next().getPhase())) {
				result++;
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