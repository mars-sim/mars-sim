/*
 * Mars Simulation Project
 * VehicleMission.java
 * @date 2021-10-17
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.UnitEvent;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.UnitListener;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.equipment.ContainerUtil;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.events.HistoricalEvent;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.malfunction.Malfunction;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.person.EventType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.LoadVehicleGarage;
import org.mars_sim.msp.core.person.ai.task.LoadingController;
import org.mars_sim.msp.core.person.ai.task.OperateVehicle;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.person.ai.task.utils.Worker;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Drone;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.StatusType;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.core.vehicle.VehicleType;

/**
 * A mission that involves driving a vehicle along a series of navpoints.
 */
public abstract class VehicleMission extends TravelMission implements UnitListener {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(VehicleMission.class.getName());

	/** Mission phases. */
	public static final MissionPhase REVIEWING = new MissionPhase("Mission.phase.reviewing");
	public static final MissionPhase EMBARKING = new MissionPhase("Mission.phase.embarking");
	public static final MissionPhase TRAVELLING = new MissionPhase("Mission.phase.travelling");
	public static final MissionPhase DISEMBARKING = new MissionPhase("Mission.phase.disembarking");

	// Static members
	private static final String ROVER_WHEEL = "rover wheel";
	private static final String ROVER_BATTERY = "rover battery";
	private static final String LASER = "laser";
	private static final String STEPPER_MOTOR = "stepper motor";
	private static final String OVEN = "oven";
	private static final String BLENDER = "blender";
	private static final String AUTOCLAVE = "autoclave";
	private static final String REFRIGERATOR = "refrigerator";
	private static final String STOVE = "stove";
	private static final String MICROWAVE = "microwave";
	private static final String POLY_ROOFING = "polycarbonate roofing";
	private static final String LENS = "lens";
	private static final String FIBERGLASS = "fiberglass";
	private static final String SHEET = "sheet";
	private static final String PRISM = "prism";


	/** The small insignificant amount of distance in km. */
	private static final double SMALL_DISTANCE = .1;

	/** Modifier for number of parts needed for a trip. */
	private static final double PARTS_NUMBER_MODIFIER = MalfunctionManager.PARTS_NUMBER_MODIFIER;
	/** Estimate number of broken parts per malfunctions */
	private static final double AVERAGE_NUM_MALFUNCTION = MalfunctionManager.AVERAGE_NUM_MALFUNCTION;
	/** Estimate number of broken parts per malfunctions for EVA suits. */
	protected static final double AVERAGE_EVA_MALFUNCTION = MalfunctionManager.AVERAGE_EVA_MALFUNCTION;
	/** Default speed if no operators have ever driven */
	private static final double DEFAULT_SPEED = 10D;

	// How often are remaning resources checked
	private static final int RESOURCE_CHECK_DURATION = 40;

	/** True if a person is submitting the mission plan request. */
	private boolean isMissionPlanReady;
	/** Vehicle traveled distance at start of mission. */
	private double startingTravelledDistance;
	/** Total traveled distance. */
	private double distanceTravelled;

	// Data members
	/** The vehicle currently used in the mission. */
	private Vehicle vehicle;
	/** The last operator of this vehicle in the mission. */
	private Worker lastOperator;
	/** The current operate vehicle task. */
	private OperateVehicle operateVehicleTask;
	/** Details of the loading operation */
	private LoadingController loadingPlan;

	/** Caches */
	private transient Map<Integer, Integer> equipmentNeededCache;

	private transient Map<Integer, Number> cachedParts = null;

	private transient double cachedDistance = -1;

	private Settlement startingSettlement;

	// When was the last check on the remaining resources
	private int lastResourceCheck = 0;

	private String dateEmbarked;

	/**
	 * Constructor 1. Started by RoverMission or DroneMission constructor 1.
	 *
	 * @param missionName
	 * @param startingMember
	 * @param minPeople
	 */
	protected VehicleMission(String missionName, MissionType missionType, MissionMember startingMember) {
		// Use TravelMission constructor.
		super(missionName, missionType, startingMember);

		setStartingSettlement(getStartingPerson().getAssociatedSettlement());
		reserveVehicle();
	}

	/**
	 * Constructor 2. Manually initiated by player.
	 *
	 * @param missionName
	 * @param startingMember
	 * @param minPeople
	 * @param vehicle
	 */
	protected VehicleMission(String missionName, MissionType missionType, MissionMember startingMember, Vehicle vehicle) {
		// Use TravelMission constructor.
		super(missionName, missionType, startingMember);

		setStartingSettlement(getStartingPerson().getAssociatedSettlement());

		// Set the vehicle.
		setVehicle(vehicle);
	}

	/**
	 * Reserve a vehicle
	 *
	 * @return
	 */
	protected boolean reserveVehicle() {
		MissionMember startingMember = getStartingPerson();
		// Reserve a vehicle.
		if (!reserveVehicle(startingMember)) {
			endMission(MissionStatus.NO_RESERVABLE_VEHICLES);
			logger.warning(startingMember, "Could not reserve a vehicle for " + getTypeID() + ".");
			return false;
		}
		return true;
	}

	/**
	 * Is the vehicle under maintenance and unable to be embarked ?
	 *
	 * @return
	 */
	private boolean checkVehicleMaintenance() {
		if (vehicle.haveStatusType(StatusType.MAINTENANCE)) {
			logger.warning(vehicle, "Under maintenance and not ready for " + getTypeID() + ".");

			endMission(MissionStatus.VEHICLE_UNDER_MAINTENANCE);
			return false;
		}
		return true;

	}


	/**
	 * Gets the mission's vehicle if there is one.
	 *
	 * @return vehicle or null if none.
	 */
	public Vehicle getVehicle() {
		return vehicle;
	}

	/**
	 * Get the current loading plan for this Mission phase.
	 * @return
	 */
	public LoadingController getLoadingPlan() {
		return loadingPlan;
	}

	/**
	 * Prepare a loading plan taking resources from a site. If a plan for the same
	 * site is already in place then it is re-used.
	 * @param loadingSite
	 */
	public LoadingController prepareLoadingPlan(Settlement loadingSite) {
		if ((loadingPlan == null) || !loadingPlan.getSettlement().equals(loadingSite)) {
			logger.info(vehicle, 10_000L, "Prepared a loading plan sourced from " + loadingSite.getName() + ".");
			loadingPlan = new LoadingController(loadingSite, vehicle,
												getRequiredResourcesToLoad(),
												getOptionalResourcesToLoad(),
												getRequiredEquipmentToLoad(),
												getOptionalEquipmentToLoad());
		}
		return loadingPlan;
	}

	/**
	 * Clear the current loading plan
	 */
	public void clearLoadingPlan() {
		loadingPlan = null;
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
				startingTravelledDistance = vehicle.getOdometerMileage();
				newVehicle.setReservedForMission(true);
				vehicle.addUnitListener(this);
				// Record the name of this vehicle in Mission
				setReservedVehicle(newVehicle.getName());
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

			boolean usable = vehicle.isVehicleReady();

			if (vehicle.getStoredMass() > 0D)
				usable = false;

			logger.log(vehicle, Level.FINER, 1000, "Was checked on the status: (available : "
						+ usable + ") for " + getTypeID() + ".");
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

		Collection<Vehicle> bestVehicles = new ConcurrentLinkedQueue<>();
		if (member.getSettlement() == null)
			return false;
		Collection<Vehicle> vList = getAvailableVehicles(member.getSettlement());

		if (vList.isEmpty()) {
			return false;
		} else {
			for (Vehicle v : vList) {
				if (!bestVehicles.isEmpty()) {
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
			if (!bestVehicles.isEmpty()) {
				Vehicle selected = null;
				int bestVehicleIndex = RandomUtil.getRandomInt(bestVehicles.size() - 1);
				try {
					selected = (Vehicle) bestVehicles.toArray()[bestVehicleIndex];
					setVehicle(selected);
				} catch (Exception e) {
					logger.severe(selected, "Cannot set the best vehicle: ", e);
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
		Collection<Vehicle> result = new ConcurrentLinkedQueue<>();

		if (getMissionType() == MissionType.DELIVERY) {
			Collection<Drone> list = settlement.getParkedDrones();
			if (!list.isEmpty()) {
				for (Drone v : list) {
					if (!v.haveStatusType(StatusType.MAINTENANCE)
							&& v.getMalfunctionManager().getMalfunctions().isEmpty()
							&& isUsableVehicle(v)
							&& !v.isReserved()) {
						result.add(v);
					}
				}
			}
		}
		else {
			Collection<Vehicle> vList = settlement.getParkedVehicles();
			if (!vList.isEmpty()) {
				for (Vehicle v : vList) {
					if (VehicleType.isRover(v.getVehicleType())
							&& !v.haveStatusType(StatusType.MAINTENANCE)
							&& v.getMalfunctionManager().getMalfunctions().isEmpty()
							&& isUsableVehicle(v)
							&& !v.isReserved()) {
						result.add(v);
					}
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
	@Override
	protected void endMission(MissionStatus endStatus) {

		// Release the loading plan if it exists
		loadingPlan = null;
		equipmentNeededCache = null;
		cachedParts = null;

		boolean continueToEndMission = true;
		if (hasVehicle()) {
			// if user hit the "End Mission" button to abort the mission
			// Check if user aborted the mission and if
			// the vehicle has been disembarked.

			// What if a vehicle is still at a settlement and Mission is not approved ?

			// for ALL OTHER REASONS
			setPhaseEnded(true);

			if (vehicle.getVehicleType() == VehicleType.DELIVERY_DRONE) {
				if (vehicle.getStoredMass() != 0D) {
					continueToEndMission = false;
					startDisembarkingPhase();
				}
			}

			else if (VehicleType.isRover(vehicle.getVehicleType())) {
				if (((Rover)vehicle).getCrewNum() != 0
						|| (vehicle.getStoredMass() != 0D)) {
					continueToEndMission = false;
					startDisembarkingPhase();
				}
			}
		}

		if (continueToEndMission) {
			setPhaseEnded(true);
			leaveVehicle();
			super.endMission(endStatus);
		}
	}

	/**
	 * Get help for the mission. The reason becomes a Mission Status.
	 * @param reason The reason why help is needed.
	 */
	public void getHelp(MissionStatus reason) {
		logger.info(vehicle, 20_000, "Needs help.");
		addMissionStatus(reason);

		// Set emergency beacon if vehicle is not at settlement.
		// Note: need to find out if there are other matching reasons for setting
		// emergency beacon.
		if (vehicle.getSettlement() == null) {

			// if the vehicle somewhere on Mars
			if (!vehicle.isBeaconOn()) {
				triggerEmergencyBeacon();

				if (VehicleType.isRover(vehicle.getVehicleType()) && vehicle.isBeingTowed()) {
					// Note: the vehicle is being towed, wait till the journey is over
					// don't end the mission yet
					logger.log(vehicle, Level.INFO, 20_000, "Currently being towed by "
									+ vehicle.getTowingVehicle().getName());
				}
			}

			else {
				// Note : if the emergency beacon is on, don't end the mission yet
				// May use logger.info(vehicle + "'s emergency beacon is on. awaiting the response for rescue right now.");
			}
		}

		else { // Vehicle is still in the settlement vicinity or has arrived in a settlement

			if (!vehicle.isBeaconOn()) {
				triggerEmergencyBeacon();
			}

			// if the vehicle is still somewhere inside the settlement when it got broken
			// down
			// Note: consider to wait till the repair is done and the mission may resume ?!?

			else if (vehicle.getSettlement() != null) {
				// if a vehicle is at a settlement
				setPhaseEnded(true);
				endMission(reason);
			}
		}
	}

	/**
	 * Trigger the emergency beacon on the Mission vehicle
	 */
	private void triggerEmergencyBeacon() {
		var message = new StringBuilder();

		// if the emergency beacon is off
		// Question: could the emergency beacon itself be broken ?
		message.append("Turned on emergency beacon. Request for towing with status flag(s) :");
		message.append(getMissionStatus().stream().map(MissionStatus::getName).collect(Collectors.joining(", ")));

		logger.info(vehicle, 20_000, message.toString());

		vehicle.setEmergencyBeacon(true);
	}

	/**
	 * Determine if a vehicle is sufficiently loaded with fuel and supplies.
	 *
	 * @return true if rover is loaded.
	 * @throws MissionException if error checking vehicle.
	 */
	public final boolean isVehicleLoaded() {
		if (vehicle == null) {
			throw new IllegalStateException(getPhase().getName() + ": vehicle is null.");
		}

		if (loadingPlan != null) {
			if (loadingPlan.isFailure()) {
				logger.warning(vehicle, "Loading has failed");
				endMission(MissionStatus.CANNOT_LOAD_RESOURCES);
			}
			return loadingPlan.isCompleted();
		}
		return false;
	}

	/**
	 * Checks if a vehicle can load the supplies needed by the mission.
	 *
	 * @return true if vehicle is loadable.
	 * @throws Exception if error checking vehicle.
	 */
	protected final boolean isVehicleLoadable() {

		Map<Integer, Number> resources = getRequiredResourcesToLoad();
		Map<Integer, Integer> equipment = getRequiredEquipmentToLoad();
		Settlement settlement = vehicle.getSettlement();
		double tripTime = getEstimatedRemainingMissionTime(true);
		if (tripTime == 0) {
			// Disapprove this mission
			logger.warning(settlement, "Has estimated zero trip time");
			return false;
		}

		boolean settlementSupplies = LoadVehicleGarage.hasEnoughSupplies(settlement, vehicle, resources, equipment,
				getPeopleNumber(), tripTime);

		if (!settlementSupplies) {
			logger.warning(settlement, "Not enough supplies for "
							+ vehicle.getName() + "'s proposed excursion.");
		}

		return settlementSupplies;
	}

	/**
	 * Gets the amount of fuel (kg) needed for a trip of a given distance (km).
	 *
	 * @param tripDistance   the distance (km) of the trip.
	 * @param fuelEconomy the vehicle's instantaneous fuel economy (km/kg).
	 * @param useMargin      Apply safety margin when loading resources before embarking if true.
	 * @return amount of fuel needed for trip (kg)
	 */
	public static double getFuelNeededForTrip(Vehicle vehicle, double tripDistance, double fuelEconomy, boolean useMargin) {
		double result = tripDistance / fuelEconomy;
		double factor = 1;
		if (useMargin) {
			if (tripDistance < 1000) {
				// Note: use formula below to add more extra fuel for short travel distance on top of the fuel margin
				// For short trips, there has been a history of getting stranded and require 4x more fuel to come back home
				factor = Vehicle.getFuelRangeErrorMargin() + (2 - 0.002 * tripDistance);
				}
			else {
				// beyond 1000 km, no more modding on top of the fuel margin
				factor = Vehicle.getFuelRangeErrorMargin();
			}

			result *= factor;
		}

//		double cap = vehicle.getAmountResourceCapacity(vehicle.getFuelType());
//		if (result > cap)
//			// Make sure the amount requested is less than the max resource cap of this vehicle
//			result = cap;

		logger.info(vehicle, 30_000, "Trip Distance: " + Math.round(tripDistance * 10.0)/10.0 + " km   "
				+ "Fuel Economy: " + Math.round(fuelEconomy * 10.0)/10.0 + " km/kg   "
				+ "Fuel Margin: " + Math.round(factor * 10.0)/10.0 + "   "
				+ "Fuel: " + Math.round(result * 10.0)/10.0 + " kg");

		return result;
	}

	/**
	 * Determines a new phase for the mission when the current phase has ended.
	 * Subclass are expected to determine the  next Phase for TRAVELLING
	 *
	 * @throws MissionException if problem setting a new phase.
	 */
	protected boolean determineNewPhase() {
		boolean handled = true;
		if (REVIEWING.equals(getPhase())) {
			// Check the vehicle is loadable before starting the embarking
			if (isVehicleLoadable()) {
				setPhase(EMBARKING, getCurrentNavpoint().getDescription());
			}
			else {
				logger.warning(vehicle, getName() + " cannot load Resources.");
				endMission(MissionStatus.CANNOT_LOAD_RESOURCES);
			}
		}

		else if (EMBARKING.equals(getPhase())) {
			startTravellingPhase();
		}

		else if (DISEMBARKING.equals(getPhase())) {
			setPhase(COMPLETED, null);
		}

		else if (COMPLETED.equals(getPhase())) {
			endMission(MissionStatus.MISSION_ACCOMPLISHED);
		}
		else {
			handled = false;
		}

		return handled;
	}


	/**
	 * Gets the date embarked timestamp of the mission.
	 *
	 * @return
	 */
	@Override
	public String getDateEmbarked() {
		return dateEmbarked;
	}

	public void flag4Submission() {
		isMissionPlanReady = true;
	}

	public void recordStartMass() {
		vehicle.recordStartMass();
	}

	@Override
	protected void performPhase(MissionMember member) {
		super.performPhase(member);
		if (REVIEWING.equals(getPhase())) {
			if (isMissionPlanReady) {
				computeEstimatedTotalDistance();
				requestReviewPhase(member);
			}
		}
		else if (EMBARKING.equals(getPhase())) {
			checkVehicleMaintenance();
			performEmbarkFromSettlementPhase(member);
		}
		else if (TRAVELLING.equals(getPhase())) {
			performTravelPhase(member);
		}
		else if (DISEMBARKING.equals(getPhase())) {
			performDisembarkToSettlementPhase(member, getCurrentNavpoint().getSettlement());
		}
		else if (COMPLETED.equals(getPhase())) {
			setPhaseEnded(true);
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
		boolean allCrewHasMedical = hasDangerousMedicalProblemsAllCrew();
		boolean hasEmergency = hasEmergency();

		if (destination != null
				&& vehicle != null
				&& vehicle.getCoordinates() != null
				&& destination.getLocation() != null) {

			reachedDestination = vehicle.getCoordinates().equals(destination.getLocation())
					|| Coordinates.computeDistance(vehicle.getCoordinates(), destination.getLocation()) < SMALL_DISTANCE;

			malfunction = vehicle.getMalfunctionManager().hasMalfunction();
		}

		// If emergency, make sure the current operateVehicleTask is pointed home.
		if ((allCrewHasMedical || hasEmergency || malfunction) &&
			operateVehicleTask != null &&
			destination != null &&
			destination.getLocation() != null &&
			operateVehicleTask.getDestination() != null &&
			!operateVehicleTask.getDestination().equals(destination.getLocation())) {
				updateTravelDestination();
		}

		// Choose a driver
		if (!reachedDestination && !malfunction) {
			boolean becomeDriver = false;

			if (operateVehicleTask != null) {
				// Someone should be driving or it's me !!!
				becomeDriver = vehicle != null &&
					((vehicle.getOperator() == null)
						|| (vehicle.getOperator().equals(member)));
			}
			else {
				// None is driving
				becomeDriver = true;
			}

			// Take control
			if (becomeDriver) {
				if (operateVehicleTask != null) {
					operateVehicleTask = createOperateVehicleTask(member, operateVehicleTask.getPhase());
				} else {
					operateVehicleTask = createOperateVehicleTask(member, null);
				}

				if (operateVehicleTask != null) {
					if (member.getUnitType() == UnitType.PERSON) {
						assignTask((Person)member, operateVehicleTask);
					}
					else {
						assignTask((Robot)member, operateVehicleTask);

					}
					lastOperator = member;
					return;
				}
			}
		}

		// If the destination has been reached, end the phase.
		if (reachedDestination) {
			Settlement base = destination.getSettlement();
			if (vehicle.getAssociatedSettlement().equals(base)) {
				logger.info(vehicle, "Arrived back home " + base.getName());
				vehicle.transfer(base);

				// TODO There is a problem with the Vehicle not being on the
				// surface vehicle list. The problem is a lack of transfer at the start of TRAVEL phase
				// This is temporary fix pending #474 which will revisit transfers
				if (!base.equals(vehicle.getContainerUnit())) {
					vehicle.setContainerUnit(base);
					logger.warning(vehicle, "Had to force container to home base");
				}
			}

			reachedNextNode();
			setPhaseEnded(true);
		}

		if (vehicle != null && VehicleType.isRover(vehicle.getVehicleType())) {
			// Check the remaining trip if there's enough resource
			// Must set margin to false since it's not needed.
			if (!hasEnoughResourcesForRemainingMission(false)) {
				// If not, determine an emergency destination.
				determineEmergencyDestination(member);
			}

			// If vehicle has unrepairable malfunction, end mission.
			if (hasUnrepairableMalfunction()) {
				getHelp(MissionStatus.UNREPAIRABLE_MALFUNCTION);
			}
		}
	}

	public Worker getLastOperator() {
		return lastOperator;
	}

	/**
	 * Gets a new instance of an OperateVehicle task for the person.
	 *
	 * @param member the mission member operating the vehicle.
	 * @return an OperateVehicle task for the person.
	 */
	protected abstract OperateVehicle createOperateVehicleTask(MissionMember member,
			TaskPhase lastOperateVehicleTaskPhase);

	/**
	 * Performs the embark from settlement phase of the mission.
	 *
	 * @param member the mission member currently performing the mission.
	 */
	protected abstract void performEmbarkFromSettlementPhase(MissionMember member);

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
		if (TRAVELLING.equals(getPhase())
				&& operateVehicleTask != null
				&& vehicle.getOperator() != null) {
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
		double result = 0;
		// Determine average driving speed for all mission members.
		double averageSpeed = getAverageVehicleSpeedForOperators();
		if (averageSpeed > 0) {
			double averageSpeedMillisol = averageSpeed / MarsClock.MILLISOLS_PER_HOUR;
			result = distance / averageSpeedMillisol;
		}

		// If buffer, add one sol.
		if (useMargin) {
			result += 500D;
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
	protected double getEstimatedRemainingMissionTime(boolean useMargin) {
		double distance = getEstimatedTotalRemainingDistance();
		if (distance > 0) {
			double time = getEstimatedTripTime(useMargin, distance);
			logger.info(vehicle, 20_000, this + " has an estimated remaining trip time of " + time);
			return time;
		}

		return 0;
	}

	/**
	 * Gets the average operating speed of the mission vehicle for all of the
	 * mission members. This returns a default if no one has ever driven a vehicle.
	 *
	 * @return average operating speed (km/h)
	 */
	protected final double getAverageVehicleSpeedForOperators() {

		double result = DEFAULT_SPEED;

		double totalSpeed = 0D;
		int count = 0;
		for (MissionMember member : getMembers()) {
			if (member instanceof Person) {
				totalSpeed += getAverageVehicleSpeedForOperator((Person) member);
				count++;
			}
		}

		if (count > 0) {
			result = totalSpeed / count;
		}
		if (result == 0) {
			result = DEFAULT_SPEED;
		}
		return result;
	}

	/**
	 * Gets the average speed of a vehicle with a given person operating it.
	 *
	 * @param operator the vehicle operator.
	 * @return average speed (km/h)
	 */
	private double getAverageVehicleSpeedForOperator(Worker operator) {
		return OperateVehicle.getAverageVehicleSpeed(vehicle, operator, this);
	}

	/**
	 * Gets the number and amounts of resources needed for the mission.
	 *
	 * @param useMargin Apply safety margin when loading resources before embarking if true.
	 *        Note : True if estimating trip. False if calculating remaining trip.
	 * @return map of amount and item resources and their Double amount or Integer
	 *         number.
	 */
	protected Map<Integer, Number> getResourcesNeededForRemainingMission(boolean useMargin) {
		double distance = getEstimatedTotalRemainingDistance();
		if (distance > 0) {
			logger.info(vehicle, 20_000, "1. " + this + " has an estimated remaining distance of "
					+ Math.round(distance * 10.0)/10.0 + " km.");
			return getResourcesNeededForTrip(useMargin, distance);
		}

		return new HashMap<>();
	}

	/**
	 * Gets the number and amounts of resources needed for a trip.
	 *
	 * @param useMargin Apply safety margin when loading resources before embarking if true.
	 * Note : True if estimating trip. False if calculating remaining trip.
	 *
	 * @param distance  the distance (km) of the trip.
	 * @return map of amount and item resources and their Double amount or Integer
	 *         number.
	 */
	public Map<Integer, Number> getResourcesNeededForTrip(boolean useMargin, double distance) {
		Map<Integer, Number> result = new HashMap<>();
		if (vehicle != null) {
			// Add the methane resource
			if (getPhase() == null || getPhase().equals(VehicleMission.EMBARKING) || getPhase().equals(VehicleMission.REVIEWING)
					|| useMargin)
				// Use margin only when estimating how much fuel needed before starting the mission
				result.put(vehicle.getFuelType(), getFuelNeededForTrip(vehicle, distance, vehicle.getEstimatedAveFuelEconomy(), true));
			else
				// When the vehicle is already on the road, do NOT use margin
				// or else it would constantly complain not having enough fuel
				result.put(vehicle.getFuelType(), getFuelNeededForTrip(vehicle, distance, vehicle.getIFuelEconomy(), false));
		}
		return result;
	}

	/**
	 * Gets spare parts for the trip.
	 *
	 * @param distance the distance of the trip.
	 * @return map of part resources and their number.
	 */
	protected Map<Integer, Number> getSparePartsForTrip(double distance) {
		Map<Integer, Number> result = null;

		// Determine vehicle parts. only if there is a change of distance
		if (vehicle != null) {

			// If the distance is the same as last time then use the cached value
			if ((cachedDistance == distance) && (cachedParts != null)) {
				result = cachedParts;
			}
			else {
				result = new HashMap<>();
				cachedParts = result;
				cachedDistance = distance;

				double drivingTime = getEstimatedTripTime(true, distance);
				double numberAccidents = drivingTime * OperateVehicle.BASE_ACCIDENT_CHANCE;
				double numberMalfunctions = numberAccidents * AVERAGE_NUM_MALFUNCTION;

				Map<Integer, Double> parts = vehicle.getMalfunctionManager().getRepairPartProbabilities();

				// Note: need to figure out why a vehicle's scope would contain the following parts :
				parts = removeParts(parts,
						LASER,
						STEPPER_MOTOR,
						OVEN,
						BLENDER,
						AUTOCLAVE,
						REFRIGERATOR,
						STOVE,
						MICROWAVE,
						POLY_ROOFING,
						LENS,
						FIBERGLASS,
						SHEET,
						PRISM);

				for (Integer id : parts.keySet()) {

					double freq = parts.get(id) * numberMalfunctions * PARTS_NUMBER_MODIFIER;
					int number = (int) Math.round(freq);
					if (number > 0) {
						result.put(id, number);
					}
				}

				// Manually override the number of wheel and battery needed for each mission
				if (VehicleType.isRover(vehicle.getVehicleType())) {
					Integer wheel = ItemResourceUtil.findIDbyItemResourceName(ROVER_WHEEL);
					Integer battery = ItemResourceUtil.findIDbyItemResourceName(ROVER_BATTERY);
					result.put(wheel, 2);
					result.put(battery, 1);
				}
			}
		}
		else {
			result = new HashMap<>();
		}

		return result;
	}


	/**
	 * Removes a variable list of parts from a resource part
	 *
	 * @param parts a map of parts
	 * @param names
	 * @return a map of parts
	 */
	private static Map<Integer, Double> removeParts(Map<Integer, Double> parts, String... names) {
		for (String n : names) {
			Integer i = ItemResourceUtil.findIDbyItemResourceName(n);
			if (i != null) {
				parts.remove(i);
			}
		}

		return parts;
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
		int currentMSols = marsClock.getMillisolInt();
		if ((currentMSols - lastResourceCheck ) > RESOURCE_CHECK_DURATION) {
			lastResourceCheck = currentMSols;
			return hasEnoughResources(getResourcesNeededForRemainingMission(useMargin));
		}

		// Assume it is still fine
		return true;
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

			for (Map.Entry<Integer, Number> entry : neededResources.entrySet()) {
				int id = entry.getKey();
				Object value = entry.getValue();

				if (id < ResourceUtil.FIRST_ITEM_RESOURCE_ID) {

					double amount = (Double) value;
					double amountStored = vehicle.getAmountResourceStored(id);

					if (amountStored < amount) {
						String newLog = "Not enough "
								+ ResourceUtil.findAmountResourceName(id) + " to continue with "
								+ getTypeID() + "  Required: " + Math.round(amount * 100D) / 100D + " kg  Vehicle stored: "
								+ Math.round(amountStored * 100D) / 100D + " kg";
						logger.log(vehicle, Level.WARNING, 10_000, newLog);
						return false;
					}
				}

				else if (id >= ResourceUtil.FIRST_ITEM_RESOURCE_ID && id < ResourceUtil.FIRST_VEHICLE_RESOURCE_ID) {
					int num = (Integer) value;
					int numStored = vehicle.getItemResourceStored(id);

					if (numStored < num) {
						String newLog = "Not enough "
								+ ItemResourceUtil.findItemResource(id).getName() + " to continue with "
								+ getTypeID() + "  Required: " + num + "  Vehicle stored: " + numStored + ".";
						logger.log(vehicle, Level.WARNING, 10_000,  newLog);
						return false;
					}
				}

				else {
					throw new IllegalStateException(getPhase() + " : issues with the resource type of "
							+ ResourceUtil.findAmountResourceName(id));
				}
			}
		}
		return result;
	}

	/**
	 * Gets the closet distance
	 *
	 * @return
	 */
	protected double getClosestDistance() {
		Settlement settlement = findClosestSettlement();
		if (settlement != null)
			return Coordinates.computeDistance(getCurrentMissionLocation(), settlement.getCoordinates());
		return 0;
	}

	/**
	 * Determines a new route for traveling
	 *
	 * @param reason
	 * @param member
	 * @param oldHome
	 * @param newDestination
	 * @param oldDistance
	 * @param newDistance
	 */
	protected void travel(String reason, MissionMember member, Settlement oldHome, Settlement newDestination, double oldDistance, double newDistance) {
		double newTripTime = getEstimatedTripTime(false, newDistance);

		NavPoint nextNav = getNextNavpoint();

		if ((nextNav != null) && (newDestination == nextNav.getSettlement())) {
			// If the closest settlement is already the next navpoint.
			logger.log(vehicle, Level.WARNING, 10000, "Emergency encountered.  Returning to home settlement (" + newDestination.getName()
					+ ") : " + Math.round(newDistance * 100D) / 100D
					+ " km    Duration : "
					+ Math.round(newTripTime * 100.0 / 1000.0) / 100.0 + " sols");

			endCollectionPhase();
			returnHome();
		}

		else {
			// If the closet settlement is not the home settlement
			logger.log(vehicle, Level.WARNING, 10000, "Emergency encountered.  Home settlement (" + oldHome.getName() + ") : "
					+ Math.round(oldDistance * 100D) / 100D
					+ " km    Going to nearest Settlement (" + newDestination.getName() + ") : "
					+ Math.round(newDistance * 100D) / 100D
					+ " km    Duration : "
					+ Math.round(newTripTime * 100.0 / 1000.0) / 100.0 + " sols");

			// Creating emergency destination mission event for going to a new settlement.
			HistoricalEvent newEvent = new MissionHistoricalEvent(EventType.MISSION_EMERGENCY_DESTINATION,
					this,
					reason,
					this.getTypeID(),
					member.getName(),
					vehicle.getName(),
					vehicle.getCoordinates().getCoordinateString(),
					oldHome.getName()
					);
			eventManager.registerNewEvent(newEvent);

			// Note: use Mission.goToNearestSettlement() as reference

			// Set the new destination as the travel mission's next and final navpoint.
			clearRemainingNavpoints();
			addNavpoint(newDestination);
			// each member to switch the associated settlement to the new destination
			// Note: need to consider if enough beds are available at the destination settlement
			// Note: can they go back to the settlement of their origin ?

			// Added updateTravelDestination() below
			updateTravelDestination();
			endCollectionPhase();
		}
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
		String reason = "";

		if (member instanceof Person
				&& (hasAnyPotentialMedicalProblems() ||
						person.getPhysicalCondition().hasSeriousMedicalProblems() || hasEmergencyAllCrew())
				) {
			reason = EventType.MISSION_MEDICAL_EMERGENCY.getName();
			hasMedicalEmergency = true;
			// 1. Create the medical emergency mission event.
			HistoricalEvent newEvent = new MissionHistoricalEvent(EventType.MISSION_MEDICAL_EMERGENCY,
					this,
					person.getName() + " had " + person.getPhysicalCondition().getHealthSituation(),
					this.getTypeID(),
					member.getName(),
					vehicle.getName(),
					vehicle.getCoordinates().getCoordinateString(),
					person.getAssociatedSettlement().getName()
					);
			eventManager.registerNewEvent(newEvent);
		} else {
			reason = EventType.MISSION_NOT_ENOUGH_RESOURCES.getName();

			// 2. Create the resource emergency mission event.
			HistoricalEvent newEvent = new MissionHistoricalEvent(EventType.MISSION_NOT_ENOUGH_RESOURCES,
					this,
					"Dwindling resource(s)",
					this.getTypeID(),
					member.getName(),
					vehicle.getName(),
					vehicle.getCoordinates().getCoordinateString(),
					person.getAssociatedSettlement().getName()
					);
			eventManager.registerNewEvent(newEvent);
		}

		Settlement oldHome = person.getAssociatedSettlement();
		double oldDistance = Coordinates.computeDistance(getCurrentMissionLocation(), oldHome.getCoordinates());

		// Determine closest settlement.
		boolean requestHelp = false;
		Settlement newDestination = findClosestSettlement();
		if (newDestination != null) {

			double newDistance = Coordinates.computeDistance(getCurrentMissionLocation(), newDestination.getCoordinates());
			boolean enough = true;

			// for delivery mission, Will need to alert the player differently if it runs out of fuel
			if (!(this instanceof Delivery)) {

				enough = hasEnoughResources(getResourcesNeededForTrip(false, newDistance));

				// Check if enough resources to get to settlement.
				if (newDistance > 0 && enough) {

					travel(reason, member, oldHome, newDestination, oldDistance, newDistance);

				} else if (newDistance > 0 && hasEnoughResources(getResourcesNeededForTrip(false, newDistance * 0.667))) {

					travel(reason, member, oldHome, newDestination, oldDistance, newDistance * 0.667);

				} else if (newDistance > 0 && hasEnoughResources(getResourcesNeededForTrip(false, newDistance * 0.333))) {

					travel(reason, member, oldHome, newDestination, oldDistance, newDistance * 0.333);

				} else {
					requestHelp = true;
				}
			}

		}
		else {
			requestHelp = true;
		}

		// Need help
		if (requestHelp) {
			endCollectionPhase();
			getHelp(hasMedicalEmergency ? MissionStatus.MEDICAL_EMERGENCY :
				MissionStatus.NO_EMERGENCY_SETTLEMENT_DESTINATION_FOUND);
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
			String settlement = null;

			if (member instanceof Person) {
				settlement = ((Person)member).getAssociatedSettlement().getName();
			}
			else {
				settlement = ((Robot)member).getAssociatedSettlement().getName();
			}

			// Creating mission emergency beacon event.
			HistoricalEvent newEvent = new MissionHistoricalEvent(EventType.MISSION_EMERGENCY_BEACON_ON,
					this,
					reason,
					this.getTypeID(),
					member.getName(),
					vehicle.getName(),
					vehicle.getCoordinates().getCoordinateString(),
					settlement
					);

			eventManager.registerNewEvent(newEvent);
			logger.info(vehicle, member.getName()
					+ " activated emergency beacon on");
		} else {
			logger.info(vehicle, member.getName()
					+ " deactivated emergency beacon on");
		}

		vehicle.setEmergencyBeacon(beaconOn);

	}

	/**
	 * Go to the nearest settlement and end collection phase if necessary.
	 */
	public void goToNearestSettlement() {
		Settlement nearestSettlement = findClosestSettlement();
		if (nearestSettlement != null) {
			clearRemainingNavpoints();
			addNavpoint(nearestSettlement);
			// Note: Not sure if they should become citizens of another settlement
			updateTravelDestination();
			endCollectionPhase();
		}
	}

	/**
	 * Update mission to the next navpoint destination.
	 */
	public void updateTravelDestination() {
		NavPoint nextPoint = getNextNavpoint();

		if (operateVehicleTask != null) {
			operateVehicleTask.setDestination(nextPoint.getLocation());
		}
		setPhaseDescription(MessageFormat.format(TRAVELLING.getDescriptionTemplate(),
										  getNextNavpoint().getDescription())); // $NON-NLS-1$
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

		for (Settlement settlement : unitManager.getSettlements()) {
			double distance = Coordinates.computeDistance(settlement.getCoordinates(), location);
			if (distance < closestDistance) {
				result = settlement;
				closestDistance = distance;
			}
		}

		return result;
	}

	/**
	 * Gets the actual total distance travelled during the mission so far.
	 *
	 * @return distance (km)
	 */
	public final double getActualTotalDistanceTravelled() {
		if (vehicle != null) {
			double dist = vehicle.getOdometerMileage() - startingTravelledDistance;
			if (dist > distanceTravelled) {
				// Update or record the distance
				distanceTravelled = dist;
				fireMissionUpdate(MissionEventType.DISTANCE_EVENT);
				return dist;
			}
			else {
				return distanceTravelled;
			}
		}

		return distanceTravelled;
	}

	/**
	 * Time passing for mission.
	 *
	 * @param time the amount of time passing (in millisols)
	 */
	@Override
	public boolean timePassing(ClockPulse pulse) {
		// Add this mission as a vehicle listener (does nothing if already listening to
		// vehicle).
		// Note : this is needed so that mission will re-attach itself as a vehicle
		// listener after deserialization
		// since listener collection is transient. - Scott
		if (hasVehicle() && !vehicle.hasUnitListener(this)) {
			vehicle.addUnitListener(this);
		}
		return true;
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
	protected Map<Integer, Number> getRequiredResourcesToLoad() {
		return getResourcesNeededForRemainingMission(true);
	}

	/**
	 * Gets the optional resources needed for loading the vehicle.
	 *
	 * @return resources and their number.
	 */
	protected Map<Integer, Number> getOptionalResourcesToLoad() {
		// Also load EVA suit related parts
		return getSparePartsForTrip(getEstimatedTotalRemainingDistance());
	}


	/**
	 * Gets the required type of equipment needed for loading the vehicle.
	 *
	 * @return type of equipment and their number.
	 */
	protected Map<Integer, Integer> getRequiredEquipmentToLoad() {
		if (equipmentNeededCache == null) {
			equipmentNeededCache = getEquipmentNeededForRemainingMission(true);
		}

		return equipmentNeededCache;
	}

	/**
	 * Gets the optional containers needed for storing the optional resources when loading up the vehicle.
	 *
	 * @return the containers needed.
	 */
	protected Map<Integer, Integer> getOptionalEquipmentToLoad() {

		Map<Integer, Integer> result = new ConcurrentHashMap<>();

		// Figure out the type of containers needed by optional amount resources.
		Map<Integer, Number> optionalResources = getOptionalResourcesToLoad();
		Iterator<Integer> i = optionalResources.keySet().iterator();
		while (i.hasNext()) {
			Integer id = i.next();
			// Check if it's an amount resource that can be stored inside
			if (id < ResourceUtil.FIRST_ITEM_RESOURCE_ID) {
				double amount = (double) optionalResources.get(id);

				// Obtain a container for storing the amount resource
				EquipmentType containerType = ContainerUtil.getContainerClassToHoldResource(id);
				int containerID = EquipmentType.getResourceID(containerType);
				double capacity = ContainerUtil.getContainerCapacity(containerType);
				int numContainers = (int) Math.ceil(amount / capacity);

				if (result.containsKey(containerID)) {
					numContainers += result.get(containerID);
				}

				result.put(containerID, numContainers);
			}

			// Note: containers are NOT designed to hold parts
			// Parts do not need a container. Any exceptions for that ?
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
					if (vehicle.getItemResourceStored(part) < number) {
//						vehicle.getInventory().addItemDemand(part, number);
						result = true;
					}
				}
			}
		}

		return result;
	}

	/**
	 * If the mission is in TRAVELLING phase then only the current driver can participate.
	 * If the mission is in REVIEWING phase then only the leader can participate.
	 *
	 * @param worker Worker requesting to help
	 */
	@Override
	public boolean canParticipate(MissionMember worker) {
		boolean valid = true;

        if (REVIEWING.equals(getPhase())) {
        	valid = getStartingPerson().equals(worker);
        }
        else if (TRAVELLING.equals(getPhase())) {
			// Note: may check if vehicle operator() is null or not
        }
		return valid && super.canParticipate(worker);
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
		Iterator<Mission> i = missionManager.getMissionsForSettlement(settlement).iterator();
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
		Iterator<Mission> i = missionManager.getMissionsForSettlement(settlement).iterator();
		while (i.hasNext()) {
			if (EMBARKING.equals(i.next().getPhase())) {
				result++;
			}
		}

		return result;
	}

	/**
	 * Checks to see how many missions currently under the approval phase at the settlement.
	 *
	 * @param settlement the settlement.
	 * @return true if embarking missions.
	 */
	public static int numApprovingMissions(Settlement settlement) {
		int result = 0;
		Iterator<Mission> i = missionManager.getMissionsForSettlement(settlement).iterator();
		while (i.hasNext()) {
			if (REVIEWING.equals(i.next().getPhase())) {
				result++;
			}
		}

		return result;
	}

	/**
	 * Gets the settlement associated with the vehicle.
	 *
	 * @return settlement or null if none.
	 */
	@Override
	public Settlement getAssociatedSettlement() {
		return vehicle.getAssociatedSettlement();
	}


	/**
	 * For a Vehicle Mission used the vehicles position directly
	 */
	@Override
	public Coordinates getCurrentMissionLocation() {
		if (vehicle != null) {
			return vehicle.getCoordinates();
		}
		return super.getCurrentMissionLocation();
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

	/**
	 * Can the mission vehicle be unloaded at this Settlement
	 *
	 * @param settlement
	 * @return
	 */
	public boolean isVehicleUnloadableHere(Settlement settlement) {
		// It is either a local mission unloading
		return DISEMBARKING.equals(getPhase())
					&& getAssociatedSettlement().equals(settlement);
	}

	/**
	 * Can the mission vehicle be loaded at a Settlement. Must be in
	 * the EMBARKING phase at the mission starting point.
	 *
	 * @param settlement
	 * @return
	 */
	public boolean isVehicleLoadableHere(Settlement settlement) {
		return EMBARKING.equals(getPhase())
					&& getAssociatedSettlement().equals(settlement);
	}

	/**
	 * Start the TRAVELLING phase of the mission. This will advanced to the
	 * next navigation point.
	 */
	protected void startTravellingPhase() {
		if (dateEmbarked == null) {
			dateEmbarked = marsClock.getTrucatedDateTimeStamp();
		}
		startTravelToNextNode();
		setPhase(TRAVELLING, getNextNavpoint().getDescription());
	}

	/**
	 * Start the disembarking phase
	 */
	protected void startDisembarkingPhase() {
		NavPoint np = getCurrentNavpoint();

		setPhase(DISEMBARKING, (np != null ? np.getDescription() : "Unknown"));
	}

	/**
	 * Sets the starting settlement.
	 *
	 * @param startingSettlement the new starting settlement
	 */
	private final void setStartingSettlement(Settlement startingSettlement) {
		this.startingSettlement = startingSettlement;
		fireMissionUpdate(MissionEventType.STARTING_SETTLEMENT_EVENT);
	}

	/**
	 * Gets the starting settlement.
	 *
	 * @return starting settlement
	 */
	public final Settlement getStartingSettlement() {
		return startingSettlement;
	}
}
