/*
 * Mars Simulation Project
 * VehicleMission.java
 * @date 2021-08-29
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.UnitEvent;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.UnitListener;
import org.mars_sim.msp.core.environment.TerrainElevation;
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
import org.mars_sim.msp.core.vehicle.VehicleOperator;

/**
 * A mission that involves driving a vehicle along a series of navpoints.
 */
public abstract class VehicleMission extends TravelMission implements UnitListener {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(VehicleMission.class.getName());

	/** Mission phases. */
	public static final MissionPhase REVIEWING = new MissionPhase(Msg.getString("Mission.phase.reviewing")); //$NON-NLS-1$
	public static final MissionPhase EMBARKING = new MissionPhase(Msg.getString("Mission.phase.embarking")); //$NON-NLS-1$
	public static final MissionPhase TRAVELLING = new MissionPhase(Msg.getString("Mission.phase.travelling")); //$NON-NLS-1$
	public static final MissionPhase DISEMBARKING = new MissionPhase(Msg.getString("Mission.phase.disembarking")); //$NON-NLS-1$
	public static final MissionPhase COMPLETED = new MissionPhase(Msg.getString("Mission.phase.completed")); //$NON-NLS-1$
	public static final MissionPhase INCOMPLETED = new MissionPhase(Msg.getString("Mission.phase.incompleted")); //$NON-NLS-1$
	
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
	private static final String TRAVEL = "Mission.phase.travelling.description";
	
	/** The factor for determining how many more EVA suits are needed for a trip. */
	private static final double EXTRA_EVA_SUIT_FACTOR = .2;
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
	
	/** True if a person is submitting the mission plan request. */
	private boolean isMissionPlanReady;
	/** Vehicle traveled distance at start of mission. */
	private double startingTravelledDistance;
	/** Total traveled distance. */
	private double distanceTravelled;

	// Data members
	/** The vehicle recorded as being used in the mission. */	
	private Vehicle vehicleCache;
	/** The vehicle currently used in the mission. */
	private Vehicle vehicle;
	/** The last operator of this vehicle in the mission. */
	private VehicleOperator lastOperator;
	/** The mission lead of this mission. */
	private MissionMember startingMember;
	/** The current operate vehicle task. */
	private OperateVehicle operateVehicleTask;
	/** Details of the loading operation */
	private LoadingController loadingPlan;

	/** Caches */
	protected transient Map<Integer, Integer> equipmentNeededCache;

	private transient Map<Integer, Number> cachedParts = null;

	private transient double cachedDistance = -1;
	
	protected static TerrainElevation terrainElevation;
	
	/**
	 * Constructor 1. Started by RoverMission or DroneMission constructor 1.
	 * 
	 * @param missionName
	 * @param startingMember
	 * @param minPeople
	 */
	protected VehicleMission(String missionName, MissionType missionType, MissionMember startingMember, int minPeople) {
		// Use TravelMission constructor.
		super(missionName, missionType, startingMember, minPeople);
	
		this.startingMember = startingMember;
		
		if (reserveVehicle()) {
			// Add mission phases.
			addPhase(REVIEWING);
			addPhase(EMBARKING);
			addPhase(TRAVELLING);
			addPhase(DISEMBARKING);
			addPhase(COMPLETED);
		}
	}

	/**
	 * Constructor 2. Manually initiated by player.
	 *  
	 * @param missionName
	 * @param startingMember
	 * @param minPeople
	 * @param vehicle
	 */
	protected VehicleMission(String missionName, MissionType missionType, MissionMember startingMember, int minPeople, Vehicle vehicle) {
		// Use TravelMission constructor.
		super(missionName, missionType, startingMember, minPeople);
	
		this.startingMember = startingMember;

		// Add mission phases.
		addPhase(REVIEWING);
		addPhase(EMBARKING);
		addPhase(TRAVELLING);
		addPhase(DISEMBARKING);
		addPhase(COMPLETED);
		
		// Set the vehicle.
		setVehicle(vehicle);
	}

	/**
	 * Reserve a vehicle
	 * 
	 * @return
	 */
	protected boolean reserveVehicle() {
		// Reserve a vehicle.
		if (!reserveVehicle(startingMember)) {
			addMissionStatus(MissionStatus.NO_RESERVABLE_VEHICLES);
			logger.warning(startingMember, "Could not reserve a vehicle for " + getTypeID() + ".");
			endMission();
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
		if (getVehicle().haveStatusType(StatusType.MAINTENANCE)) {
			addMissionStatus(MissionStatus.VEHICLE_UNDER_MAINTENANCE);
			logger.warning(startingMember, getVehicle() + " under maintenance and not ready for " + getTypeID() + ".");
			endMission();
			return false;
		}
		return true;
		
	}
	
	
	/**
	 * Gets the mission's vehicle if there is one.
	 * 
	 * @return vehicle or null if none.
	 */
	@Override
	public final Vehicle getVehicle() {
		return vehicle;
	}

	/**
	 * Gets the vehicle being used in this mission if there is one.
	 * 
	 * @return vehicle or null if none.
	 */
	public final Vehicle getVehicleCache() {
		return vehicleCache;
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
			logger.info(vehicle, "Prepared a loading plan sourced from " + loadingSite.getName());
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
				vehicleCache = newVehicle;
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

			if (vehicle.getInventory().getTotalInventoryMass(false) > 0D)
				usable = false;

			logger.log(startingMember, Level.FINER, 1000, "Was checking on the status: (available : "
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
		
		if (this instanceof Delivery) {
			Collection<Drone> list = settlement.getParkedDrones();
			if (!list.isEmpty()) {
				for (Drone v : list) {
					if (!v.haveStatusType(StatusType.MAINTENANCE)
							&& v.getMalfunctionManager().getMalfunctions().isEmpty()
							&& isUsableVehicle(v)) {
						result.add(v);
					}
				}
			}
		}
		else {
			Collection<Vehicle> vList = settlement.getParkedVehicles();
			if (!vList.isEmpty()) {
				for (Vehicle v : vList) {
					if (v instanceof Rover
							&& !v.haveStatusType(StatusType.MAINTENANCE)
							&& v.getMalfunctionManager().getMalfunctions().isEmpty()
							&& isUsableVehicle(v)) {
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
	public void endMission() {
		if (hasVehicle()) {
			// if user hit the "End Mission" button to abort the mission
			// Check if user aborted the mission and if
			// the vehicle has been disembarked.

			if (needHelp()) {
				getHelp();
			}
		
			else {
				
				// What if a vehicle is still at a settlement and Mission is not approved ?
				
				// for ALL OTHER REASONS
				setPhaseEnded(true);
				
				if (vehicleCache instanceof Drone) {
					if (!vehicleCache.getInventory().isEmpty(false)) {
						addPhase(VehicleMission.DISEMBARKING);
						setPhase(VehicleMission.DISEMBARKING);
					}
					else {
						leaveVehicle();
						super.endMission();
					}
				}
				
				else if (vehicleCache instanceof Rover) {
					if (((Rover)vehicleCache).getCrewNum() != 0 || !vehicleCache.getInventory().isEmpty(false)) {
						addPhase(VehicleMission.DISEMBARKING);
						setPhase(VehicleMission.DISEMBARKING);
					}
					else {
						leaveVehicle();
						super.endMission();
					}
				}
			}
		}
		
		else if (haveMissionStatus(MissionStatus.MISSION_ACCOMPLISHED)) {
			setPhaseEnded(true);
			leaveVehicle();
			super.endMission();
		}

		else {
			// if vehicles are NOT available
			// Questions : what are the typical cases here ?

			// if a vehicle is parked at a settlement and had an accident and was
			// repaired,
			// somehow this mission did not end and the Mission Tool shows the Regolith
			// mission was still on-going
			// and the occupants did not leave the vehicle.
			setPhaseEnded(true);
			super.endMission();
		}
	}

	public void getHelp() {
		logger.info(startingMember, 20_000, "Asked for help.");
		
		// Set emergency beacon if vehicle is not at settlement.
		// Note: need to find out if there are other matching reasons for setting
		// emergency beacon.
		if (vehicle.getSettlement() == null) {
			// if the vehicle somewhere on Mars 
			if (!vehicle.isBeaconOn()) {
				var message = new StringBuilder();
				
				// if the emergency beacon is off
				// Question: could the emergency beacon itself be broken ?
				message.append("Turned on ").append(vehicle.getName())
					.append("'s emergency beacon. Request for towing with status flag(s) :");
				
				for (int i=0; i< getMissionStatus().size(); i++) {
					message.append(" (")
							.append((i+1))
							.append(") ")
							.append(getMissionStatus().get(i).getName());
				}
				
				logger.info(startingMember, 20_000, message.toString());
				
				vehicle.setEmergencyBeacon(true);

				if (vehicle instanceof Rover && vehicle.isBeingTowed()) {
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
				// if the emergency beacon is off
				// Question: could the emergency beacon itself be broken ?
				var message = new StringBuilder();
				message.append("Turned on ")
					.append(vehicle.getName())
					.append("'s emergency beacon. Request for towing with status flag(s) :");
				
				for (int i=0; i< getMissionStatus().size(); i++) {
					message.append(" (").append((i+1)).append(")->")
					       .append(getMissionStatus().get(i).getName());
				}
				logger.info(startingMember, 20_000, message.toString());
				vehicle.setEmergencyBeacon(true);
			}
			
			// if the vehicle is still somewhere inside the settlement when it got broken
			// down
			// Note: consider to wait till the repair is done and the mission may resume ?!?
			
			else if (vehicle.getSettlement() != null) {
				// if a vehicle is at a settlement			
				setPhaseEnded(true);
				
				if (!vehicle.getInventory().isEmpty(false))
					setPhase(VehicleMission.DISEMBARKING);
				
				leaveVehicle();
				super.endMission();
			}		
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
			throw new IllegalStateException(getPhase().getName() + ": vehicle is null.");
		}

		try {
			return (loadingPlan != null) && loadingPlan.isCompleted();
		} catch (Exception e) {
			logger.severe(vehicle, "Cannot test if this vehicle is fully loaded: ", e);
		}
		
		return true;
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
		Settlement settlement = vehicle.getSettlement();
		double tripTime = getEstimatedRemainingMissionTime(true);
		if (tripTime == 0) {
			// Disapprove this mission
			setApproval(false);	
			logger.warning(settlement, "Has estimated zero trip time");
			return false;
		}

		boolean settlementSupplies = LoadVehicleGarage.hasEnoughSupplies(settlement, vehicle, resources, equipment,
				getPeopleNumber(), tripTime);

		if (!settlementSupplies) {
			logger.warning(settlement, "Not enough supplies for "
							+ startingMember.getName() + "'s proposed excursion.");
			// Disapprove this mission
			setApproval(false);		
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

		double cap = vehicle.getInventory().getAmountResourceCapacity(vehicle.getFuelType(), false);
		if (result > cap)
			// Make sure the amount requested is less than the max resource cap of this vehicle 
			result = cap;
		
		logger.info(vehicle, 30_000, "tripDistance: " + Math.round(tripDistance * 10.0)/10.0 + " km   "
				+ "fuel economy: " + Math.round(fuelEconomy * 10.0)/10.0 + " km/kg   "
				+ "composite fuel margin factor: " + Math.round(factor * 10.0)/10.0 + "   "
				+ "Amount of fuel: " + Math.round(result * 10.0)/10.0 + " kg");
		
		return result;
	}

	/**
	 * Determines a new phase for the mission when the current phase has ended.
	 * 
	 * @throws MissionException if problem setting a new phase.
	 */
	protected void determineNewPhase() {
		if (REVIEWING.equals(getPhase())) {
			setPhase(VehicleMission.EMBARKING);
			setPhaseDescription(
					Msg.getString("Mission.phase.embarking.description", getCurrentNavpoint().getDescription()));//startingMember.getSettlement().toString())); // $NON-NLS-1$
		}

		else if (EMBARKING.equals(getPhase())) {
			startTravelToNextNode();
			setPhase(VehicleMission.TRAVELLING);
			setPhaseDescription(
					Msg.getString(TRAVEL, getNextNavpoint().getDescription())); // $NON-NLS-1$
		} 
		
		else if (TRAVELLING.equals(getPhase())) {
			if (getCurrentNavpoint().isSettlementAtNavpoint()) {
				setPhase(VehicleMission.DISEMBARKING);
				setPhaseDescription(
						Msg.getString("Mission.phase.disembarking.description", getCurrentNavpoint().getDescription())); // $NON-NLS-1$
			}
		} 
		
		else if (DISEMBARKING.equals(getPhase())) {
			setPhase(VehicleMission.COMPLETED);
			setPhaseDescription(
					Msg.getString("Mission.phase.completed.description")); // $NON-NLS-1$
		}
		
		else if (COMPLETED.equals(getPhase())) {
			addMissionStatus(MissionStatus.MISSION_ACCOMPLISHED);
			endMission();
		}
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
				requestReviewPhase(member);
				computeEstimatedTotalDistance();
			}
		}
		else if (EMBARKING.equals(getPhase())) {
			checkVehicleMaintenance();
			performEmbarkFromSettlementPhase(member);
		} 
		else if (TRAVELLING.equals(getPhase())) {
			createDateEmbarked();
			performTravelPhase(member);
		} 
		else if (DISEMBARKING.equals(getPhase())) {
			performDisembarkToSettlementPhase(member, getCurrentNavpoint().getSettlement());
		}
		else if (COMPLETED.equals(getPhase())
				|| INCOMPLETED.equals(getPhase())) {
			createDateCompleted();
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
	
		if (vehicle != null 
				&& destination != null 
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
				operateVehicleTask.setDestination(destination.getLocation());
				setPhaseDescription(Msg.getString(TRAVEL,
						getNextNavpoint().getDescription())); // $NON-NLS-1$
		}
		
		// Choose a driver
		if (!reachedDestination && !malfunction) {
			boolean becomeDriver = false;

			if (operateVehicleTask != null) {
				// Someone should be driving or it's me !!!
				becomeDriver = vehicle != null &&
					((vehicle.getOperator() == null) 
						|| (vehicle.getOperator().getOperatorName().equals(member.getName())));
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
					// Bad forgive me !!!
					if (member instanceof Person) {
						assignTask((Person)member, operateVehicleTask);
					}
					else {
						assignTask((Robot)member, operateVehicleTask);

					}
					lastOperator = (VehicleOperator)member;
					return;
				}
			}
		}

		// If the destination has been reached, end the phase.
		if (reachedDestination) {
			reachedNextNode();
			setPhaseEnded(true);
		}

		if (vehicle instanceof Rover) {
			// Check the remaining trip if there's enough resource 
			// Must set margin to false since it's not needed.
			if (!hasEnoughResourcesForRemainingMission(false)) {
				// If not, determine an emergency destination.
				determineEmergencyDestination(member);
			}
	
			// If vehicle has unrepairable malfunction, end mission.
			if (hasUnrepairableMalfunction()) {
				addMissionStatus(MissionStatus.UNREPAIRABLE_MALFUNCTION);
				getHelp();
			}
		}
	}

	public VehicleOperator getLastOperator() {
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
	 * Obtains approval from the commander of the settlement for the mission.
	 * 
	 * @param member the mission member currently performing the mission.
	 */	
	@Override
	protected void requestReviewPhase(MissionMember member) {
		super.requestReviewPhase(member);	
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
	public double getEstimatedRemainingMissionTime(boolean useMargin) {
		double distance = getEstimatedTotalRemainingDistance();
		if (distance > 0) {
			logger.info(startingMember, 20_000, "2. " + this + " has an estimated remaining distance of " + Math.round(distance * 10.0)/10.0 + " km.");
			return getEstimatedTripTime(useMargin, distance);
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
	private double getAverageVehicleSpeedForOperator(VehicleOperator operator) {
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
	public Map<Integer, Number> getResourcesNeededForRemainingMission(boolean useMargin) {
		double distance = getEstimatedTotalRemainingDistance(); // getEstimatedTotalDistance();
		if (distance > 0) {
			logger.info(startingMember, 20_000, "1. " + this + " has an estimated remaining distance of " 
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
			if (getPhase() == null || getPhase().equals(VehicleMission.EMBARKING) || getPhase().equals(VehicleMission.REVIEWING))
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
	
				StringBuilder buffer = new StringBuilder();
	
				buffer.append("Fetching spare parts - ");

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
						buffer
						.append("ID: ").append(id).append(" ")
						.append(ItemResourceUtil.findItemResourceName(id)).append(" ")
						.append("x").append(number).append("   ");
					}
				}
				
				// Manually override the number of wheel and battery needed for each mission
				if (vehicle instanceof Rover) { 
					Integer wheel = ItemResourceUtil.findIDbyItemResourceName(ROVER_WHEEL);
					Integer battery = ItemResourceUtil.findIDbyItemResourceName(ROVER_BATTERY);
					result.put(wheel, 2);
					result.put(battery, 1);
				}

				logger.info(vehicle, buffer.toString());
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
	public Map<Integer, Double> removeParts(Map<Integer, Double> parts, String... names) {
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

			for (Map.Entry<Integer, Number> entry : neededResources.entrySet()) {
				int id = entry.getKey();
				Object value = entry.getValue();

				if (id < ResourceUtil.FIRST_ITEM_RESOURCE_ID) {

					double amount = (Double) value;
					double amountStored = inv.getAmountResourceStored(id, false);

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
					int numStored = inv.getItemResourceNum(id);

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
			addNavpoint(new NavPoint(newDestination.getCoordinates(), newDestination,
					"emergency destination: " + newDestination.getName()));
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
					
					endCollectionPhase();		
					// Don't have enough resources and can't go anywhere, turn on beacon next
					if (hasMedicalEmergency) {
						addMissionStatus(MissionStatus.MEDICAL_EMERGENCY);
						getHelp();
					}
					else {
						addMissionStatus(MissionStatus.NOT_ENOUGH_RESOURCES);
						getHelp();
					}
				}
			}

		} else { 
			// newDestination is null. Can't find a destination
			
			endCollectionPhase();
			
			if (hasMedicalEmergency) {
				addMissionStatus(MissionStatus.MEDICAL_EMERGENCY);
				getHelp();
			}
			else {
				addMissionStatus(MissionStatus.NO_EMERGENCY_SETTLEMENT_DESTINATION_FOUND);
				getHelp();
			}
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
			
			String settlement = ((Person)member).getAssociatedSettlement().getName();
					
			if (member instanceof Robot)
				settlement = ((Robot)member).getAssociatedSettlement().getName();
				
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
	 * Update mission to the next navpoint destination.
	 */
	public void updateTravelDestination() {
		if (operateVehicleTask != null) {
			operateVehicleTask.setDestination(getNextNavpoint().getLocation());
		}
		setPhaseDescription(Msg.getString(TRAVEL, getNextNavpoint().getDescription())); // $NON-NLS-1$
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
		if (vehicleCache != null) {
			double dist = vehicleCache.getOdometerMileage() - startingTravelledDistance;
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
	 * Gets the starting travelled distance.
	 * 
	 * @return distance (km)
	 */
	public final double getStartingTravelledDistance() {
		if (vehicle != null) {
			return startingTravelledDistance;
		} else {
			return 0D;
		}
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
	public Map<Integer, Number> getRequiredResourcesToLoad() {
		return getResourcesNeededForRemainingMission(true);
	}

	/**
	 * Gets the optional resources needed for loading the vehicle.
	 * 
	 * @return resources and their number.
	 */
	public Map<Integer, Number> getOptionalResourcesToLoad() {
		// Also load EVA suit related parts
		return getSparePartsForTrip(getEstimatedTotalRemainingDistance());
	}
	

	/**
	 * Gets the required type of equipment needed for loading the vehicle.
	 * 
	 * @return type of equipment and their number.
	 */
	public Map<Integer, Integer> getRequiredEquipmentToLoad() {
		return getEquipmentNeededForRemainingMission(true);
	}

	/**
	 * Gets the number and types of equipment needed for the mission.
	 * 
	 * @param useBuffer use time buffers in estimation if true.
	 * @return map of equipment types and number.
	 */
	public Map<Integer, Integer> getEquipmentNeededForRemainingMission(boolean useBuffer) {
		return ((Mission)this).getEquipmentNeededForRemainingMission(useBuffer);
	}
	

	/**
	 * Gets the optional containers needed for storing the optional resources when loading up the vehicle.
	 * 
	 * @return the containers needed.
	 */
	public Map<Integer, Integer> getOptionalEquipmentToLoad() {

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
				int containerID = ContainerUtil.getContainerClassIDToHoldResource(id);
				double capacity = ContainerUtil.getContainerCapacity(containerID);
				int numContainers = (int) Math.ceil(amount / capacity);

				if (result.containsKey(containerID)) {
					numContainers += result.get(containerID);
				}

				result.put(containerID, numContainers);				
			} 
			
			// Note: containers are NOT designed to hold parts 
			// Parts do not need a container. Any exceptions for that ? 
	
			// Gets a spare EVA suit for each 4 members in a mission
			int numEVA = (int) (getPeopleNumber() * EXTRA_EVA_SUIT_FACTOR);
			result.put(EquipmentType.getResourceID(EquipmentType.EVA_SUIT), numEVA);

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
						vehicle.getInventory().addItemDemand(part, number);
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
	 * Gets the mission lead of this mission.
	 * @return the member
	 */
	public MissionMember getStartingMember() {;
		return startingMember;
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
}
