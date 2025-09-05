/*
 * Mars Simulation Project
 * AbstractVehicleMission.java
 * @date 2024-07-15
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.mission;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.IntFunction;
import java.util.logging.Level;
import java.util.stream.Collectors;

import com.mars_sim.core.UnitEvent;
import com.mars_sim.core.UnitEventType;
import com.mars_sim.core.UnitListener;
import com.mars_sim.core.UnitType;
import com.mars_sim.core.equipment.ContainerUtil;
import com.mars_sim.core.equipment.Equipment;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.events.HistoricalEvent;
import com.mars_sim.core.goods.GoodsUtil;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.malfunction.Malfunction;
import com.mars_sim.core.malfunction.MalfunctionManager;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.person.EventType;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.task.Sleep;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskJob;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.project.Stage;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.resource.MaintenanceScope;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.resource.SuppliesManifest;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.tool.RandomUtil;
import com.mars_sim.core.vehicle.GroundVehicle;
import com.mars_sim.core.vehicle.Rover;
import com.mars_sim.core.vehicle.StatusType;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.core.vehicle.VehicleController;
import com.mars_sim.core.vehicle.VehicleType;
import com.mars_sim.core.vehicle.comparators.RangeComparator;
import com.mars_sim.core.vehicle.task.DriveGroundVehicle;
import com.mars_sim.core.vehicle.task.LoadVehicleGarage;
import com.mars_sim.core.vehicle.task.LoadVehicleMeta;
import com.mars_sim.core.vehicle.task.LoadingController;
import com.mars_sim.core.vehicle.task.OperateVehicle;
import com.mars_sim.core.vehicle.task.PilotDrone;

/**
 * A mission that involves driving a vehicle along a series of navpoints.
 */
public abstract class AbstractVehicleMission extends AbstractMission implements UnitListener, VehicleMission {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(AbstractVehicleMission.class.getName());
	
	/** How often are remaining resources checked. */
	private static final int RESOURCE_CHECK_DURATION = 40;
	/** The speed mod due to driving at night. */
	private static final double NIGHT_TIME_SPEED_MOD = 0.3;
	
	/** The small insignificant amount of distance in km. */
	private static final double SMALL_DISTANCE = .1;
	/** Modifier for number of parts needed for a trip. */
	private static final double PARTS_NUMBER_MODIFIER = MalfunctionManager.PARTS_NUMBER_MODIFIER;
	/** Estimate number of broken parts per malfunctions */
	private static final double AVERAGE_NUM_MALFUNCTION = MalfunctionManager.AVERAGE_NUM_MALFUNCTION;
	/** Default speed if no operators have ever driven. */
	private static final double DEFAULT_SPEED = 10D;
		
	// Travel Mission status
	protected static final String AT_NAVPOINT = "At a navpoint";
	protected static final String TRAVEL_TO_NAVPOINT = "Traveling to navpoint";

	/** Mission phases. */
	private static final MissionPhase LOADING = new MissionPhase("loading", Stage.PREPARATION);
	private static final MissionPhase DEPARTING = new MissionPhase("departing", Stage.PREPARATION);
	protected static final MissionPhase TRAVELLING = new MissionPhase("travelling");
	protected static final MissionPhase DISEMBARKING = new MissionPhase("disembarking", Stage.CLOSEDOWN);
	
	// Mission Status
	protected static final MissionStatus NO_AVAILABLE_VEHICLE = new MissionStatus("Mission.status.noVehicle");
	protected static final MissionStatus VEHICLE_BEACON_ACTIVE = new MissionStatus("Mission.status.vehicleBeacon");
	private static final MissionStatus VEHICLE_UNDER_MAINTENANCE = new MissionStatus("Mission.status.vehicleMaintenance");
	protected static final MissionStatus CANNOT_LOAD_RESOURCES = new MissionStatus("Mission.status.loadResources");
	private static final MissionStatus UNREPAIRABLE_MALFUNCTION = new MissionStatus("Mission.status.unrepairable");
	protected static final MissionStatus MISSION_LEAD_NO_SHOW = new MissionStatus("Mission.status.leaderNoShow");

	// Static members
	private static final Integer WHEEL_ID = ItemResourceUtil.findIDbyItemResourceName(ItemResourceUtil.ROVER_WHEEL);
	private static Set<Integer> unNeededParts = ItemResourceUtil.convertNameArray2ResourceIDs(
															new String[] {ItemResourceUtil.FIBERGLASS});
	// Data members
	/** The current navpoint index. */
	private int navIndex = 0;
	// When was the last check on the remaining resources
	private int lastResourceCheck = 0;
	
	/** Vehicle traveled distance at start of mission. */
	private double startingTravelledDistance = 0D;
	/** Total traveled distance. */
	private double distanceTravelled = 0D;
	/** The estimated total distance for this mission. */
	private double distanceProposed = 0D;
	/** The current leg remaining distance at this moment. */
	private double distanceCurrentLegRemaining = 0D;
	/** The current leg travelled distance at this moment. */
	private double distanceCurrentLegTravelled = 0D;
	/** The estimated total remaining distance at this moment. */
	private double distanceTotalRemaining = 0D;

	private transient double cachedDistance = -1;
	/** The current traveling status of the mission. */
	private String travelStatus;
	/** The vehicle currently used in the mission. */
	private Vehicle vehicle;
	/** The current operate vehicle task. */
	private OperateVehicle operateVehicleTask;
	/** Details of the loading operation */
	private LoadingController loadingPlan;

	private Settlement startingSettlement;
	/** The last navpoint the mission stopped at. */
	private NavPoint lastStopNavpoint;
	
	/** Equipment Caches */
	private transient Map<Integer, Integer> equipmentNeededCache;

	private transient Map<Integer, Number> cachedParts = null;
	
	/** A collection of locations that make up the vehicle's trail. */
	private List<Coordinates> trail = new ArrayList<>();
	/** List of navpoints for the mission. */
	private List<NavPoint> navPoints = new ArrayList<>();
		
	/**
	 * Creates a Vehicle mission.
	 *
	 * @param missionType
	 * @param startingMember
	 * @param minPeople
	 * @param vehicle Optional, if null then reserve a Vehicle
	 */
	protected AbstractVehicleMission(MissionType missionType, Worker startingMember, Vehicle vehicle) {
		super(missionType, startingMember);

		init(startingMember);

		// Set the vehicle.
		if (vehicle != null) {
			setVehicle(vehicle);
		}
		
		else if (reserveVehicle()) {
			// Charge the vehicle
			getVehicle().setCharging(true);
		}
	}
	
	/**
	 * Sets up starting NavPoints.
	 * 
	 * @param startingMember
	 */
	private void init(Worker startingMember) {

		NavPoint startingNavPoint = null;

		if (startingMember.getSettlement() != null) {
			startingNavPoint = new NavPoint(startingMember.getSettlement(), null);
		}
		else {
			startingNavPoint = new NavPoint(getCurrentMissionLocation(), "starting location", null);
		}

		addNavpoint(startingNavPoint);
		
		lastStopNavpoint = startingNavPoint;

		setTravelStatus(AT_NAVPOINT);
		
		setStartingSettlement(startingMember.getAssociatedSettlement());
	}

	/**
	 * Sets the starting state of the mission.
	 * 
	 * @return
	 */
	protected void setInitialPhase(boolean needsReview) {
		// NavPoints are fixed in so how far to go?
		computeTotalDistanceProposed();

		if (needsReview) {
			// Set initial mission phase.
			startReview();
		}
		else {
			// Set initial mission phase.
			createDesignationString();
			startLoadingPhase();
		}

		Worker startingMember = getStartingPerson();
		if (getVehicle() != null)
			logger.info(startingMember, "Assessing " + getName() + " using " + getVehicle().getName() + ".");
	}

	/**
	 * Reserves a vehicle for the mission if possible.
	 *
	 * @return true if vehicle is reserved, false if unable to.
	 */
	private final boolean reserveVehicle() {
		Collection<Vehicle> vList = getAvailableVehicles(getStartingSettlement());
		var best = vList.stream()
			.filter(this::isUsableVehicle)
			.sorted(getVehicleComparator().reversed())  // Reverse so we find the best one
			.findFirst().orElse(null);

		boolean result;
		if (best != null) {
			setVehicle(best);
			result = true;
		}
		else {
			endMission(NO_AVAILABLE_VEHICLE);
			logger.warning(getStartingPerson(), "Could not reserve a vehicle for " + getName() + ".");
			result = false;
		}

		return result;
	}
		
	/**
	 * Get a comparator of Vehicles for ths mission. The default takes the Vehicle wioth the lognest range
	 */
	protected  Comparator<Vehicle> getVehicleComparator() {
		return new RangeComparator();
	}

	/**
	 * Is the vehicle under maintenance and unable to be embarked ?
	 *
	 * @return
	 */
	private boolean checkVehicleMaintenance() {
		if (vehicle.haveStatusType(StatusType.MAINTENANCE)) {
			logger.warning(vehicle, "Under maintenance and not ready for " + getName() + ".");

			endMission(VEHICLE_UNDER_MAINTENANCE);
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
	public Vehicle getVehicle() {
		return vehicle;
	}

	/**
	 * Prepares a loading plan taking resources from a site. If a plan for the same
	 * site is already in place then it is re-used.
	 * 
	 * @param loadingSite
	 */
	protected LoadingController prepareLoadingPlan(Settlement loadingSite) {
		if ((loadingPlan == null) || !loadingPlan.getSettlement().equals(loadingSite)) {
			logger.info(vehicle, 10_000L, "Prepared a loading plan sourced from " + loadingSite.getName() + ".");
			var manifest = new SuppliesManifest(getRequiredResourcesToLoad(),
												getOptionalResourcesToLoad(),
												getRequiredEquipmentToLoad(),
												getOptionalEquipmentToLoad());
			loadingPlan = vehicle.setLoading(manifest);													
		}
		return loadingPlan;
	}

	/**
	 * Sets the vehicle for this mission.
	 *
	 * @param newVehicle the vehicle to use.
	 * @throws MissionException if vehicle cannot be used.
	 */
	protected void setVehicle(Vehicle newVehicle) {
		if (newVehicle != null) {
			vehicle = newVehicle;
			startingTravelledDistance = vehicle.getOdometerMileage();
			claimVehicle(vehicle);
		}
		else {
			throw new IllegalArgumentException("newVehicle is null.");
		}
	}

	/**
	 * Checks if the mission has a vehicle.
	 *
	 * @return true if vehicle.
	 */
	protected boolean hasVehicle() {
		return (vehicle != null);
	}

	/**
	 * Leaves the mission's vehicle and unreserves it.
	 * 
	 * @param v Vehicle to be released
	 */
	protected final void releaseVehicle(Vehicle v) {
		if ((v != null) && this.equals(v.getMission())) {
			v.setReservedForMission(false);
			v.setMission(null);
			v.removeUnitListener(this);
			fireMissionUpdate(MissionEventType.VEHICLE_EVENT);
		}
	}

	/**
	 * Claims the mission's vehicle and reserve it.
	 * 
	 * @param v Vehicle to be claimed
	 */
	protected final void claimVehicle(Vehicle v) {
		if (v.getMission() != null) {
			logger.warning(v, "Aready assigned to a Mission when assigning " + getName() + ".");
		}

		v.setReservedForMission(true);
		v.addUnitListener(this);
		v.setMission(this);
		
		fireMissionUpdate(MissionEventType.VEHICLE_EVENT);
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
		return vehicle.isVehicleReady() && vehicle.isEmpty();
	}

	/**
	 * Gets a collection of available vehicles at a settlement that are usable for
	 * this mission.
	 *
	 * @param settlement the settlement to find vehicles.
	 * @return list of available vehicles.
	 * @throws MissionException if problem determining if vehicles are usable.
	 */
	protected abstract Collection<Vehicle> getAvailableVehicles(Settlement settlement);

	/**
	 * Finalizes the mission.
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
			if (!isDroneDone() && !isRoverDone()) {
				// Either the drone is still unloading or the rover is still unloading
				continueToEndMission = false;
				if (isCurrentNavpointSettlement()
						&& getPhase() != DISEMBARKING) 
					startDisembarkingPhase();
			}
			else {
				// for ALL OTHER REASONS
				setPhaseEnded(true);
			}
		}

		if (continueToEndMission) {
			setPhaseEnded(true);
			releaseVehicle(vehicle);
			super.endMission(endStatus);
		}
	}

	/**
	 * Is the unloading done on this drone ?
	 * 
	 * @return
	 */
	public boolean isDroneDone() {
		if (VehicleType.isDrone(vehicle.getVehicleType())
				&& vehicle.isEmpty())
			return true;
		return false;
	}
	
	/**
	 * Is the unloading done on this rover ?
	 * 
	 * @return
	 */
	public boolean isRoverDone() {
		if (VehicleType.isRover(vehicle.getVehicleType())
				&& (((Rover)vehicle).getCrewNum() == 0
				|| vehicle.isEmpty()))
			return true;
		return false;
	}
	
	/**
	 * Gets help for the mission. The reason becomes a Mission Status.
	 * 
	 * @param reason The reason why help is needed.
	 */
	public void getHelp(MissionStatus reason) {
		logger.info(vehicle, 20_000, "Requesting help.");
		addMissionStatus(reason);
		
		// Set emergency beacon if vehicle is not at settlement.
		// Note: need to find out if there are other matching reasons for setting
		// emergency beacon.
		if (vehicle.getSettlement() == null) {
			// Case 1: if the vehicle somewhere on Mars
			
			if (!vehicle.isBeaconOn()) {
				var message = new StringBuilder();

				// Question: could the emergency beacon itself be broken ?
				message.append("Turned on emergency beacon to request for towing. Status flag(s): ");
				message.append(getMissionStatus().stream().map(MissionStatus::getName).collect(Collectors.joining(", ")));
		
				logger.info(vehicle, 20_000, message.append(".").toString().toLowerCase());
		
				vehicle.setEmergencyBeacon(true);
		
				// Creating mission emergency beacon event.
				HistoricalEvent newEvent = new MissionHistoricalEvent(EventType.MISSION_EMERGENCY_BEACON_ON,
						this,
						reason.getName(),
						getName(),
						getStartingPerson().getName(),
						vehicle
						);
		
				eventManager.registerNewEvent(newEvent);
			}
		}

		else { 
			// Case 2: if the vehicle in the settlement vicinity or has arrived in a settlement
			
			// if a vehicle is at a settlement
			setPhaseEnded(true);
			endMission(reason);
		}
	}

	/**
	 * Determines if a vehicle is sufficiently loaded with fuel and supplies.
	 *
	 * @return true if rover is loaded.
	 * @throws MissionException if error checking vehicle.
	 */
	public final boolean isVehicleLoaded() {
		if (loadingPlan != null) {
			if (loadingPlan.isFailure()) {
				logger.warning(vehicle, "Loading has failed.");
				endMission(CANNOT_LOAD_RESOURCES);
				return false;
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
		if (settlement == null) {
			logger.severe(vehicle, "Not at Settlement for loading");
			return false;
		}
		
		double tripTime = getEstimatedRemainingMissionTime(true);
		if (tripTime == 0) {
			// Disapprove this mission
			logger.warning(settlement, "Estimated zero trip time.");
			return false;
		}

		boolean settlementSupplies = LoadVehicleGarage.hasEnoughSupplies(settlement, vehicle, resources, equipment,
				getMembers().size(), tripTime);

		if (!settlementSupplies) {
			logger.warning(settlement, "Not enough supplies for "
							+ vehicle.getName() + "'s proposed excursion.");
		}

		return settlementSupplies;
	}

	/**
	 * Determines a new phase for the mission when the current phase has ended.
	 * Subclass are expected to determine the next Phase for TRAVELLING.
	 *
	 * @throws MissionException if problem setting a new phase.
	 */
	protected boolean determineNewPhase() {
		boolean handled = true;
		MissionPhase phase = getPhase();
		if (REVIEWING.equals(phase)) {
			// Check the vehicle is loadable before starting the embarking
			if (isVehicleLoadable()) {
				startLoadingPhase();
			}
			else {
				logger.warning(vehicle, getName() + " cannot load Resources.");
				endMission(CANNOT_LOAD_RESOURCES);
			}
		}

		else if (LOADING.equals(phase)) {
			setPhase(DEPARTING, getStartingSettlement().getName());	
		}
		
		// Note: Add a new phase called RESTING here right after loading in done and prior to departing, 
		//       Create a mission schedule and have everyone sync up to it. 
		//       Have everyone sleep sufficiently and depart at the start of the next sol during daylight.
		
		else if (DEPARTING.equals(phase)) {
			startTravellingPhase();
		}

		else if (DISEMBARKING.equals(phase)) {
			// Update the distances only once per msol
			computeDistanceCurrentLegTravelled();
			computeTotalDistanceRemaining();
			computeTotalDistanceTravelled();
			
			endMission(null);
		}
		else {
			handled = false;
		}

		return handled;
	}

	protected void recordStartMass() {
		vehicle.recordStartMass();
	}

	@Override
	protected void performPhase(Worker member) {
		super.performPhase(member);

		MissionPhase phase = getPhase();
		if (LOADING.equals(phase)) {
			performLoadingPhase(member);

		} else if (DEPARTING.equals(phase)) {
			checkVehicleMaintenance();
			performDepartingFromSettlementPhase(member);
		}
		else if (TRAVELLING.equals(phase)) {
			performTravelPhase(member);
		}
		else if (DISEMBARKING.equals(phase)) {
			// After arriving at the settlement
			if (isCurrentNavpointSettlement()) {
				performDisembarkToSettlementPhase(member, getCurrentNavpointSettlement());
			}
			else
				logger.severe(getName() + ": Current navpoint is not a settlement.");
			
//			int msol = getMarsTime().getMillisolInt();
//			if (msolCache != msol) {
//				msolCache = msol;
//				// Update the distances only once per msol
//				computeDistanceCurrentLegTravelled();
//				computeTotalDistanceRemaining();
//				computeTotalDistanceTravelled();
//			}
		}
	}

	/**
	 * Performs the loading phase.
	 * 
	 * @param member
	 */
	private void performLoadingPhase(Worker member) {
		Vehicle v = getVehicle();

		if (v == null) {
			endMission(NO_AVAILABLE_VEHICLE);
			return;
		}

		Settlement settlement = v.getSettlement();
		if (settlement == null) {
			logger.warning(member,
					Msg.getString("RoverMission.log.notAtSettlement", getPhase().getName())); //$NON-NLS-1$
			endMission(NO_AVAILABLE_VEHICLE);
			return;
		}

		// While still in the settlement, check if the beacon is turned on and and endMission()
		else if (v.isBeaconOn()) {
			endMission(VEHICLE_BEACON_ACTIVE);
			return;
		}

		if (!isVehicleLoaded()) {
			// Load vehicle if not fully loaded.
			if (member.isInSettlement()
				// Note: randomly select this member to load resources for the rover
				// This allows person to do other important things such as eating
				&& RandomUtil.lessThanRandPercent(75)) {
								
				TaskJob job = LoadVehicleMeta.createLoadJob(vehicle, settlement);
		        if (job != null) {
		            Task task = null;
		            // Create the Task ready for assignment
		            if (member instanceof Person p) {
		                task = job.createTask(p);
		                // Task may be rejected because of the Worker's profile
		                assignTask(p, task);
		            }
		            else if (member instanceof Robot r && v.isInGarage()) {
		                task = job.createTask(r);
		                // Task may be rejected because of the Worker's profile
		                assignTask(r, task);
		            }
		        }
			}
		}
		else {
			setPhaseEnded(true);
		}
	}

	/**
	 * Performs the travel phase of the mission.
	 *
	 * @param member the mission member currently performing the mission.
	 */
	protected final void performTravelPhase(Worker member) {
		NavPoint destination = getNextNavpoint();

		// If vehicle has not reached destination and isn't broken down, travel to
		// destination.
		boolean reachedDestination = false;
		boolean malfunction = false;
		boolean allCrewHasMedical = hasDangerousMedicalProblemsAllCrew();
		boolean hasEmergency = hasEmergency();
		boolean lowPower = false;
		
		if (destination != null && vehicle != null) {

			Coordinates current = vehicle.getCoordinates();
			Coordinates target =  destination.getLocation();
			double distance = current.getDistance(target);
			
			reachedDestination = current.equals(target)
					|| distance < SMALL_DISTANCE;
			
			// Add the location to the trail if outside on a mission
			addToTrail(current);

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
			
			if (member instanceof Robot robot) {
				lowPower = robot.getSystemCondition().isLowPower();
			}

			// how to choose a pilot based on piloting skill ?
			
			boolean becomeDriver = false;

			if (!lowPower && operateVehicleTask != null) {
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
					operateVehicleTask = createOperateVehicleTask(member, OperateVehicle.MOBILIZE);
				}

				if (operateVehicleTask != null) {
					boolean canAssign = false;
					
					if (member instanceof Person person) {
						canAssign = assignTask(person, operateVehicleTask, true);				
					}
					else if (member instanceof Robot robot) {
						canAssign = assignTask(robot, operateVehicleTask, true);
					}
					
					if (canAssign) {
						logger.info(member, 20_000, "Assigned to operate " + vehicle.getName() + ".");
					}
					else {
						logger.info(member, 20_000, "Not ready to be reassigned to operate " + vehicle.getName() + ".");	
						
						if (operateVehicleTask instanceof PilotDrone pd) {
							pd.endTask();//clearDown();
						} else if (operateVehicleTask instanceof DriveGroundVehicle dgv) {
							dgv.endTask();//clearDown();
						}
					}
						
//					lastOperator = member;
					return;
				}
			}
		}

		// If the destination has been reached, end the phase.
		if (reachedDestination) {
			Settlement base = destination.getSettlement();
			if (vehicle.getAssociatedSettlement().equals(base)) {
				logger.info(vehicle, "Arrived back home " + base.getName() + ".");
				vehicle.transfer(base);

				// Note: There is a problem with the Vehicle not being on the
				// surface vehicle list. The problem is a lack of transfer at the start of TRAVEL phase
				// This is temporary fix pending #474 which will revisit transfers
				if (!base.equals(vehicle.getContainerUnit())) {
					// Avoid calling vehicle.setContainerUnit(base)
					vehicle.transfer(base);
					logger.severe(vehicle, "Forced its container unit to become its home base.");
				}
			}

			reachedNextNode();
			setPhaseEnded(true);
		}

		if (vehicle != null && VehicleType.isRover(vehicle.getVehicleType())) {
			// Check the remaining trip if there's enough resource
			// Must set margin to false since it's not needed.
			if (hasEnoughResourcesForRemainingMission() && hasUnrepairableMalfunction()) {
				// If vehicle has unrepairable malfunction, end mission.
				getHelp(UNREPAIRABLE_MALFUNCTION);
			}
		}
	}

	/**
	 * Gets the vehicle's trail as a collection of coordinate locations.
	 *
	 * @return trail collection
	 */
	public Collection<Coordinates> getTrail() {
		return trail;
	}
	
	/**
	 * Adds a location to the vehicle's trail if appropriate.
	 *
	 * @param location location to be added to trail
	 */
	public void addToTrail(Coordinates location) {
		if (!trail.isEmpty()) {
			Coordinates lastLocation = trail.get(trail.size() - 1);
			if (!lastLocation.equals(location) 
//					&& (lastLocation.getDistance(location) >= TerrainElevation.STEP_KM
					&& !trail.contains(location))
				trail.add(location);
		} else if (!trail.contains(location)) {
			trail.add(location);
		}
	}

	/**
	 * Gets a new instance of an OperateVehicle task for the person.
	 *
	 * @param member the mission member operating the vehicle.
	 * @return an OperateVehicle task for the person.
	 */
	protected abstract OperateVehicle createOperateVehicleTask(Worker member,
			TaskPhase lastOperateVehicleTaskPhase);

	/**
	 * Performs the departing from settlement phase of the mission.
	 *
	 * @param member the mission member currently performing the mission.
	 */
	protected abstract void performDepartingFromSettlementPhase(Worker member);

	/**
	 * Performs the disembark to settlement phase of the mission.
	 *
	 * @param member              the mission member currently performing the
	 *                            mission.
	 * @param disembarkSettlement the settlement to be disembarked to.
	 */
	protected abstract void performDisembarkToSettlementPhase(Worker member, Settlement disembarkSettlement);

	/**
	 * Calls the members to join the mission.
	 * 
	 * @param deadline How many mSols to they have to join; could be zero if no deadline
	 */
	protected void callMembersToMission(int deadline) {
	
		// Set the members' work shift to on-call to get ready
		for (Worker m : getMembers()) {
			if (m instanceof Person person) {
				// If first time this person has been called and there is a limit interrupt them
				if (!person.getShiftSlot().setOnCall(true) && (deadline > 0)) {
					// First call so 
					Task active = person.getTaskManager().getTask();
					if (active instanceof Sleep sp) {
						// Not create but the only way
						sp.setAlarm(deadline);
					}
				}
			}
		}
	}

	/**
	 * Gets the estimated time of arrival (ETA) for the current leg of the mission.
	 *
	 * @return time (MarsTime) or null if not applicable.
	 */
	@Override
	public MarsTime getLegETA() {
		if (TRAVELLING.equals(getPhase())
				&& operateVehicleTask != null
				&& vehicle.getOperator() != null) {
			return operateVehicleTask.getETA();
		} else {
			return null;
		}
	}


	/**
	 * Gets the estimated time remaining for the mission.
	 *
	 * @param useMargin Use time buffer in estimations if true.
	 * @return time (millisols)
	 * @throws MissionException
	 */
	protected double getEstimatedRemainingMissionTime(boolean useMargin) {
		double distance = computeTotalDistanceRemaining();
		if (distance > 0) {
			double time = getEstimatedTripTime(useMargin, distance);
			logger.log(vehicle, Level.FINE, 20_000L, this 
					+ " - Projected remaining waypoint time: " 
					+  Math.round(time * MarsTime.HOURS_PER_MILLISOL * 10.0)/10.0 + " hrs ("
					+  Math.round(time * 10.0)/10.0 + " millisols)");
			return time;
		}

		return 0;
	}
	
	/**
	 * Gets the estimated time for a trip.
	 *
	 * @param useMargin use time buffers in estimation if true.
	 * @param distance  the distance of the trip.
	 * @return time (millisols)
	 * @throws MissionException
	 */
	protected final double getEstimatedTripTime(boolean useMargin, double distance) {
		double result = 0;
		// Determine average driving speed for all mission members.
		double averageSpeed = getAverageVehicleSpeedForOperators() * ((1 + NIGHT_TIME_SPEED_MOD) / 2);
		logger.log(vehicle, Level.FINE, 10_000, "Estimated average speed: " + Math.round(averageSpeed * 100.0)/100.0 + " kph.");
		if (averageSpeed > 0) {
			result = distance / averageSpeed * MarsTime.MILLISOLS_PER_HOUR;
		}

		// If buffer, multiply by the the life support margin
		if (useMargin) {
			result *= Vehicle.getLifeSupportRangeErrorMargin();
		}
		return result;
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
		for (Worker member : getMembers()) {
			if (member.getUnitType() == UnitType.PERSON) {
				totalSpeed += getAverageVehicleSpeedForOperator(member);
				count++;
			}
		}

		if (totalSpeed < 0)
			totalSpeed = 0;
		
		if (count > 0) {
			result = totalSpeed / count;
		}
		if (result == 0) {
			result = vehicle.getBaseSpeed();
		}
	
		return result;
	}

	/**
	 * Gets the average speed of a vehicle with a given person operating it.
	 *
	 * @param operator the vehicle operator.
	 * @return average speed (kph or km/h)
	 */
	private double getAverageVehicleSpeedForOperator(Worker operator) {
		return OperateVehicle.getAverageVehicleSpeed(vehicle, operator);
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
		double distance = computeTotalDistanceRemaining();
		if (distance > 0) {
			return getResourcesNeededForTrip(useMargin, distance);
		}

		return new HashMap<>();
	}

	/**
	 * Gets the number and amounts of resources needed for a trip.
	 *
	 * @param useMargin Apply safety margin when loading resources before embarking if true.
	 * Note : True if estimating trip only. False if calculating for the remaining trip.
	 *
	 * @param distance  the distance (km) of the trip.
	 * @return map of amount and item resources and their Double amount or Integer
	 *         number.
	 */
	protected Map<Integer, Number> getResourcesNeededForTrip(boolean useMargin, double distance) {
		Map<Integer, Number> result = new HashMap<>();
		if (vehicle != null) {
			double amount = 0;

			// Must use the same logic in all cases otherwise too few fuel will be loaded
			amount = vehicle.getFuelNeededForTrip(distance, useMargin);
	
			int fuelTypeID = vehicle.getFuelTypeID();
			double amountOxygen = 0;
			
			if (fuelTypeID == ResourceUtil.METHANOL_ID) {
				// if useMargin is true, include more oxygen
				amountOxygen = VehicleController.RATIO_OXIDIZER_METHANOL * amount;
			}
			else if (fuelTypeID == ResourceUtil.METHANE_ID) {
				// if useMargin is true, include more oxygen
				amountOxygen = VehicleController.RATIO_OXIDIZER_METHANE * amount;
			}
			
			if (!useMargin)	
				amountOxygen = amount;
			
			result.put(fuelTypeID, amount);
			result.put(ResourceUtil.OXYGEN_ID, amountOxygen);
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
		// Determine vehicle parts. only if there is a change of distance

		// If the distance is the same as last time then use the cached value
		if ((cachedDistance == distance) && (cachedParts != null)) {
			return cachedParts;
		}

		Map<Integer, Number> result = new HashMap<>();
		cachedParts = result;
		cachedDistance = distance;

		double drivingTime = getEstimatedTripTime(true, distance);
		double numberAccidents = drivingTime * OperateVehicle.BASE_ACCIDENT_CHANCE;
		double numberMalfunctions = numberAccidents * AVERAGE_NUM_MALFUNCTION;

		Map<Integer, Double> parts = vehicle.getMalfunctionManager().getRepairPartProbabilities();

		// Note: need to figure out why a mission vehicle's scope would contain 
		// the following unneeded parts that must be removed:
		parts = ItemResourceUtil.removePartMap(parts, unNeededParts);

		for (Map.Entry<Integer, Double> entry : parts.entrySet()) {
			Integer id = entry.getKey();
			double value = entry.getValue();
			double freq = value * numberMalfunctions * PARTS_NUMBER_MODIFIER;
			int number = (int) Math.round(freq);
			if (number > 0) {
				result.put(id, number);
			}
		}

		// Manually override the number of wheels and battery needed for each mission
		// since the automated process is not reliable
		switch(vehicle.getVehicleType()) {
			case EXPLORER_ROVER:
				result.computeIfAbsent(WHEEL_ID, k -> 2);
				break;
			case CARGO_ROVER, TRANSPORT_ROVER:
				result.computeIfAbsent(WHEEL_ID, k -> 4);
				break;
			default:
				break;
		}
		
		return result;
	}

	/**
	 * Checks if there are enough resources available in the vehicle for the
	 * remaining mission. If there is not then the Mission is aborted and rerouted
	 * to an Emergency settlement if possible. Otherwise a beacon is activated.

	 * @return true if enough resources.
	 */
	protected final boolean hasEnoughResourcesForRemainingMission() {
		int currentMSols = getMarsTime().getMillisolInt();
		if ((currentMSols - lastResourceCheck ) > RESOURCE_CHECK_DURATION) {
			lastResourceCheck = currentMSols;
			int missingResourceId = hasEnoughResources(getResourcesNeededForRemainingMission(false));
			if (missingResourceId >= 0) {
				// Create Mission Flag
				MissionStatus status = MissionStatus.createResourceStatus(missingResourceId);
				abortMission(status, EventType.MISSION_NOT_ENOUGH_RESOURCES);
			}
		}

		// Assume it is still fine
		return true;
	}

	/**
	 * Checks if there are enough resources available in the vehicle.
	 *
	 * @param neededResources map of amount and item resources and their Double
	 *                        amount or Integer number.
	 * @return The resourceId of the 1st resource that is lacking; otherwise -1
	 */
	private int hasEnoughResources(Map<Integer, Number> neededResources) {

		for (Map.Entry<Integer, Number> entry : neededResources.entrySet()) {
			int id = entry.getKey();
			Object value = entry.getValue();

			if (id < ResourceUtil.FIRST_ITEM_RESOURCE_ID) {

				double amount = (Double) value;
				double amountStored = vehicle.getSpecificAmountResourceStored(id);

				// Check inside vehicle
				if (VehicleType.isRover(vehicle.getVehicleType())) {
					Rover rover = (Rover) vehicle;
					// Check people's possession
					for (Person person: rover.getCrew()) {
						amountStored += person.getSpecificAmountResourceStored(id);
					}
					// Check vehicle's equipment
					for (Equipment equipment: rover.getContainerSet()) {
						amountStored += equipment.getSpecificAmountResourceStored(id);
					}
				}
				
				if (amountStored < amount) {
					String newLog = "Not enough "
							+ ResourceUtil.findAmountResourceName(id) + " to continue with "
							+ getName() + " - Required: " + Math.round(amount * 100D) / 100D + " kg - Vehicle stored: "
							+ Math.round(amountStored * 100D) / 100D + " kg.";
					logger.log(vehicle, Level.WARNING, 10_000, newLog);
					return id;
				}
			}

			else if (id < ResourceUtil.FIRST_VEHICLE_RESOURCE_ID) {
				int num = (Integer) value;
				int numStored = vehicle.getItemResourceStored(id);

				if (numStored < num) {
					String newLog = "Not enough "
							+ ItemResourceUtil.findItemResource(id).getName() + " to continue with "
							+ getName() + " - Required: " + num + " - Vehicle stored: " + numStored + ".";
					logger.log(vehicle, Level.WARNING, 10_000,  newLog);
					return id;
				}
			}

			else
				logger.warning(vehicle, "Phase: " + getPhase() + ": unable to process the resource '"
						+ GoodsUtil.getGood(id) + "'.");
		}
		return -1;
	}

	/**
	 * Determines the emergency destination settlement for the mission if one is
	 * reachable, otherwise sets the emergency beacon and ends the mission.
	 * 
	 * @param reason
	 */
	protected final void determineEmergencyDestination(MissionStatus reason) {
		Settlement oldHome = getStartingSettlement();

		// Determine closest settlement.
		boolean requestHelp = false;
		Settlement newDestination = MissionUtil.findClosestSettlement(getCurrentMissionLocation());
		
		if (newDestination != null) {
			double newDistance = getCurrentMissionLocation().getDistance(newDestination.getCoordinates());
			boolean enough = true;

			// for drone mission, Will need to alert the player differently if it runs out of fuel
			if (vehicle instanceof GroundVehicle) {

				enough = hasEnoughResources(getResourcesNeededForTrip(false, newDistance)) < 0;

				// Check if enough resources to get to settlement.
				if (enough) {
					travelDirectToSettlement(newDestination);

					logger.info(getVehicle(), "Returning to " + newDestination.getName() + ".");
					// Creating emergency destination mission event for going to a new settlement.
					if (!newDestination.equals(oldHome)) {
						HistoricalEvent newEvent = new MissionHistoricalEvent(EventType.MISSION_EMERGENCY_DESTINATION,
								this,
								reason.getName(),
								getName(),
								getStartingPerson().getName(),
								vehicle
								);
						eventManager.registerNewEvent(newEvent);
					}
				}
				else {
					requestHelp = true;
				}
			}
		}
		else {
			requestHelp = true;
		}

		// Need help
		if (requestHelp) {
			abortPhase();
			getHelp(reason);
		}
	}

	/**
	 * Sets the vehicle's emergency beacon on or off.
	 *
	 * @param member   the mission member performing the mission.
	 * @param vehicle  the vehicle on the mission.
	 * @param beaconOn true if beacon is on, false if not.
	 */
	public void setEmergencyBeacon(Worker member, Vehicle vehicle, boolean beaconOn, String reason) {

		if (beaconOn) {
			// Creating mission emergency beacon event.
			HistoricalEvent newEvent = new MissionHistoricalEvent(EventType.MISSION_EMERGENCY_BEACON_ON,
					this,
					reason,
					this.getName(),
					member.getName(),
					vehicle
					);

			eventManager.registerNewEvent(newEvent);
			logger.info(vehicle, member.getName()
					+ " activated emergency beacon.");
		} else {
			logger.info(vehicle, member.getName()
					+ " deactivated emergency beacon.");
		}

		vehicle.setEmergencyBeacon(beaconOn);

	}

	/**
	 * Gets to the nearest settlement and end collection phase if necessary.
	 */
	public void goToNearestSettlement() {
		Settlement nearestSettlement = MissionUtil.findClosestSettlement(getCurrentMissionLocation());
		if (nearestSettlement != null) {
			clearRemainingNavpoints();
			addNavpoint(nearestSettlement);
			// Note: Not sure if they should become citizens of another settlement
			updateTravelDestination();
			abortPhase();
		}
	}

	/**
	 * Updates mission to the next navpoint destination.
	 */
	protected void updateTravelDestination() {
		NavPoint nextPoint = getNextNavpoint();

		if (operateVehicleTask != null && nextPoint != null) {
			operateVehicleTask.setDestination(nextPoint.getLocation());
		}
		setPhaseDescription(MessageFormat.format(TRAVELLING.getDescriptionTemplate(),
										  getNextNavpointDescription()));
	}

	/**
	 * Time passing for mission.
	 *
	 * @param pulse the clock pulse showing the amount of time passing (in millisols)
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
		if (type == UnitEventType.COORDINATE_EVENT) {
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
		return getSparePartsForTrip(computeTotalDistanceRemaining());
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
				EquipmentType containerType = ContainerUtil.getEquipmentTypeForContainer(id);
				int containerID = EquipmentType.getResourceID(containerType);
				double capacity = ContainerUtil.getContainerCapacity(containerType);
				int numContainers = (int) Math.ceil(amount / capacity);

				result.computeIfAbsent(containerID, k -> numContainers);
				result.computeIfPresent(containerID, (key, value) -> value + numContainers);
			}

			// Note: containers are NOT designed to hold parts
			// Parts do not need a container. Any exceptions for that ?
		}

		return result;
	}

	/**
	 * Checks if the vehicle has a malfunction that cannot be repaired.
	 *
	 * @return true if unrepairable malfunction due to lack of parts for repair.
	 */
	private boolean hasUnrepairableMalfunction() {
		if (vehicle != null) {
			vehicle.getMalfunctionManager();
			Iterator<Malfunction> i = vehicle.getMalfunctionManager().getMalfunctions().iterator();
			while (i.hasNext()) {
				Malfunction malfunction = i.next();
				Map<MaintenanceScope, Integer> map = malfunction.getRepairParts();
				Iterator<MaintenanceScope> j = map.keySet().iterator();
				while (j.hasNext()) {
					MaintenanceScope ms = j.next();
					int number = map.get(ms);
					if (vehicle.getItemResourceStored(ms.getPart().getID()) < number) {
						return true;
					}
				}
			}
		}

		return false;
	}

	/**
	 * If the mission is in TRAVELLING phase then only the current driver can participate.
	 * If the mission is in REVIEWING phase then only the leader can participate.
	 *
	 * @param worker Worker requesting to help
	 */
	@Override
	protected boolean canParticipate(Worker worker) {
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
	 * Gets the settlement associated with the vehicle.
	 *
	 * @return settlement or null if none.
	 */
	@Override
	public Settlement getAssociatedSettlement() {
		return startingSettlement;
	}


	/**
	 * Returns the current mission location. 
	 * For a Vehicle Mission used the vehicles position directly.
	 */
	@Override
	public Coordinates getCurrentMissionLocation() {
		if (vehicle != null) {
			return vehicle.getCoordinates();
		}
		return super.getCurrentMissionLocation();
	}

	/**
	 * Resets the trip statistics to return home.
	 * 
	 * @param currentNavPoint
	 * @param destNavPoint
	 */
	protected void resetToReturnTrip(NavPoint currentNavPoint, NavPoint destNavPoint) {
	
		navPoints.clear();
		
		navIndex = 0;
		 
		addNavpoint(currentNavPoint);
		
		addNavpoint(destNavPoint);
		
		lastStopNavpoint = currentNavPoint;

		setTravelStatus(AT_NAVPOINT);

		logger.info(vehicle, "Set return to " + destNavPoint);

		// Need to recalculate what is left to travel to get resources loaded
		// for return
		distanceProposed = 0D;
		
		computeTotalDistanceProposed();
	}
	
	/**
	 * Adds a navpoint to the mission.
	 * 
	 * @param navPoint the new nav point location to be added.
	 */
	private final void addNavpoint(NavPoint navPoint) {
		navPoints.add(navPoint);
		fireMissionUpdate(MissionEventType.NAVPOINTS_EVENT);
	}

	/**
	 * Gets the last navpoint.
	 * 
	 * @return
	 */
	private Coordinates getLastNavpoint() {
		if (navPoints.isEmpty()) {
			return null;
		}

		return navPoints.get(navPoints.size() - 1).getLocation();
	}

	/**
	 * Add a Nav point for a Settlement.
	 * 
	 * @param s
	 */
	protected void addNavpoint(Settlement s) {
		addNavpoint(new NavPoint(s, getLastNavpoint()));
	}
	

	/**
	 * Adds a Nav point for a Coordinate.
	 * 
	 * @param c Coordinate to visit
	 * @param n Name
	 */
	protected void addNavpoint(Coordinates c, String n) {
		addNavpoint(new NavPoint(c, n, getLastNavpoint()));
	}
	
	
	/**
	 * Adds a list of Coordinates as NavPoints. Use the function to give.
	 * 
	 * a description to each new NavPoint
	 * @param points Coordinates to add
	 * @param nameFunc Function takes the index of the Coordinate
	 */
	protected void addNavpoints(List<Coordinates> points, IntFunction<String> nameFunc) {
		Coordinates prev = getLastNavpoint();
		for (int x = 0; x < points.size(); x++) {
			Coordinates location = points.get(x);
			navPoints.add(new NavPoint(location, nameFunc.apply(x), prev));
			prev = location;
		}
		fireMissionUpdate(MissionEventType.NAVPOINTS_EVENT);
	}

	/**
	 * Clears out any unreached nav points.
	 */
	private final void clearRemainingNavpoints() {
		int index = getNextNavpointIndex();

		// REmove all points that are after the current point
		for (int x = navPoints.size()-1; x >= index; x--) {
			navPoints.remove(x);
		}
		
		// Note: how to compensate the shifted index upon removal of this navPoint
		fireMissionUpdate(MissionEventType.NAVPOINTS_EVENT);

	}

	/**
	 * Gets the mission's next navpoint.
	 * 
	 * @return navpoint or null if no more navpoints.
	 */
	public final NavPoint getNextNavpoint() {
		if (navIndex < navPoints.size())
			return navPoints.get(navIndex);
		else
			return null;
	}

	/**
	 * Gets the next navpoint the mission is stopped at.
	 * 
	 * @return the description of the next navpoint.
	 */
	public final String getNextNavpointDescription() {
		if (navIndex < navPoints.size()) {
			return navPoints.get(navIndex).getDescription();
		}
		
		return "Null Next Location";
	}
			
	/**
	 * Gets the mission's next navpoint index.
	 * 
	 * @return navpoint index or -1 if none.
	 */
	protected final int getNextNavpointIndex() {
		if (navIndex < navPoints.size())
			return navIndex;
		else
			return -1;
	}

	/**
	 * Set the next navpoint index.
	 * 
	 * @param newNavIndex the next navpoint index.
	 * @throws MissionException if the new navpoint is out of range.
	 */
	protected final void setNextNavpointIndex(int newNavIndex) {
		if (newNavIndex < navPoints.size()) {
			navIndex = newNavIndex;
		} else
			logger.severe(getPhase() + "'s newNavIndex " + newNavIndex + " is out of bounds.");
	}

	/**
	 * Gets a list of navpoints.
	 * 
	 * @return navpoint
	 */
	@Override
	public List<NavPoint> getNavpoints() {
		return navPoints;
	}
	
	/**
	 * Gets the current navpoint the mission is stopped at.
	 * 
	 * @return navpoint or null if mission is not stopped at a navpoint.
	 */
	public final NavPoint getCurrentNavpoint() {
		if (travelStatus != null && AT_NAVPOINT.equals(travelStatus)
			&& navIndex < navPoints.size()) {
			return navPoints.get(navIndex);
		}
		
		return null;
	}
	
	/**
	 * Is the current navpoint the mission is stopped at a settlement ?
	 * 
	 * @return navpoint or null if mission is not stopped at a navpoint.
	 */
	protected boolean isCurrentNavpointSettlement() {
		if (travelStatus != null && AT_NAVPOINT.equals(travelStatus)
			&& navIndex < navPoints.size()
			&& navPoints.get(navIndex).getSettlement() != null) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * Gets the settlement at the current navpoint the mission is stopped at.
	 * 
	 * @return Settlement or null if mission is not stopped at a Settlement.
	 */
	private Settlement getCurrentNavpointSettlement() {
		if (travelStatus != null && AT_NAVPOINT.equals(travelStatus)
			&& navIndex < navPoints.size()) {
			return navPoints.get(navIndex).getSettlement();
		}
		
		return null;
	}

	/**
	 * Gets the current navpoint the mission is stopped at.
	 * 
	 * @return the description of the current navpoint.
	 */
	public final String getCurrentNavpointDescription() {
		if (travelStatus != null && AT_NAVPOINT.equals(travelStatus)
			&& navIndex < navPoints.size()) {
			return navPoints.get(navIndex).getDescription();
		}
		
		return "Unknown";
	}
	
	/**
	 * Gets the index of the current navpoint the mission is stopped at.
	 * 
	 * @return index of current navpoint or -1 if mission is not stopped at a
	 *         navpoint.
	 */
	public final int getCurrentNavpointIndex() {
		if (travelStatus != null && AT_NAVPOINT.equals(travelStatus))
			return navIndex;
		else
			return -1;
	}

	/**
	 * Gets the current destination of the Mission. The isTravelling flag
	 * identifies if the Mission is on the way.
	 */
	@Override
	public NavPoint getCurrentDestination() {
		if (isTravelling()) {
			return getNextNavpoint();
		}
		return getCurrentNavpoint();
	}

	/**
	 * Gets the travel mission's current status.
	 * 
	 * @return travel status
	 */
	@Override
	public boolean isTravelling() {
		return TRAVEL_TO_NAVPOINT.equals(travelStatus);
	}

	/**
	 * Sets the travel mission's current status.
	 * 
	 * @param newTravelStatus the mission travel status.
	 */
	private void setTravelStatus(String newTravelStatus) {
		travelStatus = newTravelStatus;
		fireMissionUpdate(MissionEventType.TRAVEL_STATUS_EVENT);
	}

	/**
	 * Starts travel to the next navpoint in the mission.
	 * 
	 * @throws MissionException if no more navpoints.
	 */
	protected final void startTravelToNextNode() {
		setNextNavpointIndex(navIndex + 1);
		setTravelStatus(TRAVEL_TO_NAVPOINT);
	}

	/**
	 * The mission has reached the next navpoint.
	 * 
	 * @throws MisisonException if error determining mission location.
	 */
	protected final void reachedNextNode() {
		setTravelStatus(AT_NAVPOINT);
		lastStopNavpoint = getCurrentNavpoint();
	}

	/**
	 * Aborts the mission via established reasons and/or events.
	 * If possible return to the starting Settlement.
	 * 
	 * @param status Reason for the abort.
	 * @param eventType Optional register an event
	 */
	@Override
	public void abortMission(MissionStatus status, EventType eventType) {

		if (addMissionStatus(status)) {
			// If the MissionFlag is not present then do it
			
			// If mission is still at home then leave the vehicle
			if (getStage() == Stage.PREPARATION) {
				releaseVehicle(vehicle);
			}
			else {
				determineEmergencyDestination(status);
			}

			// Create an event if needed
			if (eventType != null) {
				HistoricalEvent newEvent = new MissionHistoricalEvent(eventType,
						this,
						status.getName(),
						getName(),
						getStartingPerson().getName(),
						vehicle
						);
				eventManager.registerNewEvent(newEvent);
			}

			super.abortMission(status, eventType);
		}
	}

	/**
	 * Have the mission return home and end collection phase if necessary.
	 */
	protected void travelDirectToSettlement(Settlement newDestination) {

		// Clear remaining route and add a new one
		// Set the new destination as the travel mission's next and final navpoint.
		clearRemainingNavpoints();

		addNavpoint(new NavPoint(newDestination, vehicle.getCoordinates()));
		if (getPhase().equals(TRAVELLING)) {
			// Already travelling so just change destination
			startTravelToNextNode();
		}
		else {
			// Abort what we are doing
			abortPhase();
		}

		updateTravelDestination();
	}

	/**
	 * Gets the starting time of the current leg of the mission.
	 * 
	 * @return starting time
	 */
	protected final MarsTime getCurrentLegStartingTime() {
		return getPhaseStartTime();
	}

	/**
	 * Gets the distance of the current leg of the mission, or 0 if not in the
	 * travelling phase.
	 * 
	 * @return distance (km)
	 */
	public final double getCurrentLegDistance() {
		if (travelStatus != null 
				&& TRAVEL_TO_NAVPOINT.equals(travelStatus) 
				&& lastStopNavpoint != null) {
			
			NavPoint next = getNextNavpoint();
			if (next != null) {
				return lastStopNavpoint.getLocation().getDistance(next.getLocation());
			}
		}
		return 0D;
	}

	/**
	 * Computes the remaining distance for the current leg of the mission.
	 * 
	 * @return distance (km) or 0 if not in the travelling phase.
	 * @throws MissionException if error determining distance.
	 */
	public final double computeDistanceCurrentLegRemaining() {
		
		if (travelStatus != null 
				&& TRAVEL_TO_NAVPOINT.equals(travelStatus)
				&& lastStopNavpoint != null) {

			if (getNextNavpoint() == null) {
				// Arrived at the destination
				int offset = 2;
				if (getPhase().equals(TRAVELLING))
					offset = 1;
				
				setNextNavpointIndex(navPoints.size() - offset);
				updateTravelDestination();
				
				distanceCurrentLegRemaining = 0D;
				fireMissionUpdate(MissionEventType.DISTANCE_EVENT);
				
				return 0D;
			}
			
			Coordinates c1 = null;
			
			// In case of TravelToSettlement, it's an one-way trip
			if (this instanceof TravelToSettlement travelToSettlement) {
				c1 = travelToSettlement.getDestinationSettlement().getCoordinates();	
			}
			else {
				NavPoint next = getNextNavpoint();
				if (next != null) {
					c1 = next.getLocation();
				}
			}

			double dist = 0D;
			
			if (c1 != null) {
				dist = getCurrentMissionLocation().getDistance(c1);
			
				if (Double.isNaN(dist)) {
					logger.severe(getName() + 
							": current leg's remaining distance is NaN.");
					dist = 0D;
				}
			}
			
			if (distanceCurrentLegRemaining != dist) {
				distanceCurrentLegRemaining = dist;
				fireMissionUpdate(MissionEventType.DISTANCE_EVENT);
			}
			
			return dist;
		}

		return 0D;
	}

	/**
	 * Computes the travelled distance in the current leg of the mission.
	 * 
	 * @return distance (km) or 0 if not in the travelling phase.
	 * @throws MissionException if error determining distance.
	 */
	public final double computeDistanceCurrentLegTravelled() {	
		// Note: contrast with getCurrentLegDistance() 
		
		if (travelStatus != null 
				&& TRAVEL_TO_NAVPOINT.equals(travelStatus)
				&& lastStopNavpoint != null) {

			if (getNextNavpoint() == null) {
				// Arrived at the destination
				int offset = 2;
				if (getPhase().equals(TRAVELLING))
					offset = 1;
				
				setNextNavpointIndex(navPoints.size() - offset);
				updateTravelDestination();
				
				distanceCurrentLegTravelled = 0D;
				fireMissionUpdate(MissionEventType.DISTANCE_EVENT);
				
				return 0D;
			}
			
			Coordinates c0 = null;
			double dist = 0D;
			
			// In case of TravelToSettlement, it's an one-way trip
			if (this instanceof TravelToSettlement travelToSettlement) {
				c0 = travelToSettlement.getAssociatedSettlement().getCoordinates();	
			}
			else {
				c0 = lastStopNavpoint.getLocation();
				dist = getCurrentMissionLocation().getDistance(c0);
				
				if (Double.isNaN(dist)) {
					logger.severe(getName() + 
							": current leg's travelled distance is NaN.");
					dist = 0D;
				}
			}
			
			if (distanceCurrentLegTravelled != dist) {
				distanceCurrentLegTravelled = dist;
				fireMissionUpdate(MissionEventType.DISTANCE_EVENT);
			}
			
			return dist;
		}

		return 0D;
	}
	
	/**
	 * Gets the remaining distance for the current leg of the mission.
	 * 
	 * @return distance (km) or 0 if not in the travelling phase.
	 * @throws MissionException if error determining distance.
	 */
	public final double getDistanceCurrentLegRemaining() {
		double legRemaining = computeDistanceCurrentLegRemaining();
		return distanceCurrentLegRemaining;
	}
	
	/**
	 * Gets the travelled distance for the current leg of the mission.
	 * 
	 * @return distance (km) or 0 if not in the travelling phase.
	 * @throws MissionException if error determining distance.
	 */
	public final double getDistanceCurrentLegTravelled() {
		double legTravelled = computeDistanceCurrentLegTravelled();
		return distanceCurrentLegTravelled;
	}
	
	/**
	 * Computes the proposed distance (or estimated total distance) of the trip.
	 * 
	 * @return distance (km)
	 */
	public final void computeTotalDistanceProposed() {
		if (navPoints.size() > 1) {
			double result = 0D;
			
			for (int x = 1; x < navPoints.size(); x++) {
				result += navPoints.get(x).getPointToPointDistance();
			}
			
			if (distanceProposed != result) {
				// Record the distance
				distanceProposed = result;
				
				fireMissionUpdate(MissionEventType.DISTANCE_EVENT);	
			}
		}
	}

	/**
	 * Gets the estimated total distance of the trip.
	 * 
	 * @return distance (km)
	 */
	@Override
	public final double getTotalDistanceProposed() {
		return distanceProposed;
	}
	
	/**
	 * Computes the estimated total remaining distance to travel in the mission.
	 * 
	 * @return distance (km).
	 * @throws MissionException if error determining distance.
	 */
	protected final double computeTotalDistanceRemaining() {
		double legRemaining = computeDistanceCurrentLegRemaining();

		int index = 0;
		double remainingNavPointDistance = 0D;
		if (AT_NAVPOINT.equals(travelStatus))
			index = getCurrentNavpointIndex();
		else if (TRAVEL_TO_NAVPOINT.equals(travelStatus))
			index = getNextNavpointIndex();

		for (int x = index + 1; x < navPoints.size(); x++) {
			NavPoint next = navPoints.get(x); 
			if (next != null)
				remainingNavPointDistance += next.getPointToPointDistance();
		}
		
		double total = legRemaining + remainingNavPointDistance;
			
		if (distanceTotalRemaining != total) {
			// Record the distance
			distanceTotalRemaining = total;
			
			fireMissionUpdate(MissionEventType.DISTANCE_EVENT);
		}
		
		return total;
	}

	/**
	 * Gets the estimated total remaining distance to travel in the mission.
	 * 
	 * @return distance (km).
	 * @throws MissionException if error determining distance.
	 */
	@Override
	public final double getTotalDistanceRemaining() {
		return computeTotalDistanceRemaining();
	}

	/**
	 * Gets the actual total distance travelled during the mission so far.
	 *
	 * @return distance (km)
	 */
	@Override
	public double getTotalDistanceTravelled() {
		return computeTotalDistanceTravelled();
	}
	
	/**
	 * Computes the actual total distance travelled during the mission so far.
	 *
	 * @return distance (km)
	 */
	private double computeTotalDistanceTravelled() {
		if (vehicle != null) {
			double dist = vehicle.getOdometerMileage() - startingTravelledDistance;
			if (dist != distanceTravelled) {
				// Update or record the distance
				distanceTravelled = dist;
				fireMissionUpdate(MissionEventType.DISTANCE_EVENT);
				return dist;
			}
		}

		return distanceTravelled;
	}

	/**
	 * Starts the TRAVELLING phase of the mission. This will advanced to the
	 * next navigation point.
	 */
	protected void startTravellingPhase() {
		if (getLog().getDateEmbarked() == null) {
			// If the embarked date has already been set, do not call it again
			getLog().generatedDateEmbarked();
		}
		startTravelToNextNode();
		setPhase(TRAVELLING, getNextNavpointDescription());
	}

	/**
	 * Starts the Embarking phase.
	 */
	private void startLoadingPhase() {
		setPhase(LOADING, getStartingSettlement().getName());
		prepareLoadingPlan(getStartingSettlement());

		// Move to a Garage if possible
		Vehicle v = getVehicle();
		Settlement settlement = v.getSettlement();
		settlement.getBuildingManager().addToGarage(v);
	}

	/**
	 * Starts the disembarking phase with the default next phase
	 */
	protected void startDisembarkingPhase() {
		startDisembarkingPhase(DISEMBARKING);
	}

	/**
	 * Starts the disembarking phase with the next phase.
	 * @param nextPhase The next Mission phase.
	 */
	protected void startDisembarkingPhase(MissionPhase nextPhase) {
		// Vehicle needs unloading
		vehicle.addSecondaryStatus(StatusType.UNLOADING);

		Settlement settlement =	getCurrentNavpointSettlement();
		if (settlement != null) {
			// Add the vehicle to the settlement
			settlement.addVicinityVehicle(vehicle);
		}
		setPhase(nextPhase, (settlement != null ? settlement.getName() : "Unknown"));
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
