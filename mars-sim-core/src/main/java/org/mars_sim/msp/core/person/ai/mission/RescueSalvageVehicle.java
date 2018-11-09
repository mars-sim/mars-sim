/**
 * Mars Simulation Project
 * RescueSalvageVehicle.java
 * @version 3.1.0 2017-04-19
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person.ai.mission;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.events.HistoricalEvent;
import org.mars_sim.msp.core.person.EventType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.job.Driver;

import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.vehicle.Crewable;
import org.mars_sim.msp.core.vehicle.GroundVehicle;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.StatusType;
import org.mars_sim.msp.core.vehicle.Vehicle;

//   Current Definition :
//1. 'Rescue' a vehicle -- if crew members are inside the vehicle and the 
//   towing vehicle will supply and transfer the needed life support
//   resources or broken parts to the vehicle. It may involve towing the 
//   vehicle back to the settlement.
//2. 'Salvage' a vehicle -- if crew members are absent from the vehicle.
//   It always involves towing the vehicle back to the settlement. 
//   For now, it can NOT break down the broken vehicle to salvage 
//   its parts. On the other hand, it will wait for the broken parts to 
//   be manufactured and be replaced so that the vehicle will function 
//   again.
   
/**
 * The RescueSalvageVehicle class serves the purpose of (1) rescuing the crew of
 * a vehicle that has an emergency beacon on due to medical issues or lack of
 * resources, (2) supplying life support resources into the vehicle, (3) towing
 * the vehicle back if the crew is disable or dead and (4) salvaging the vehicle
 * if it's beyond repair.
 */
public class RescueSalvageVehicle extends RoverMission implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(RescueSalvageVehicle.class.getName());

	/** Default description. */
	public static final String DEFAULT_DESCRIPTION = Msg.getString("Mission.description.rescueSalvageVehicle"); //$NON-NLS-1$

	// Static members
	public static final int MIN_STAYING_MEMBERS = 1;
	public static final int MIN_GOING_MEMBERS = 2;
	private static final int MAX_GOING_MEMBERS = 3;

	public static final double BASE_RESCUE_MISSION_WEIGHT = 1000D;
	public static final double BASE_SALVAGE_MISSION_WEIGHT = 5D;
	private static final double RESCUE_RESOURCE_BUFFER = 1D;

	// Mission phases
	final public static MissionPhase RENDEZVOUS = new MissionPhase(Msg.getString("Mission.phase.rendezvous")); //$NON-NLS-1$

	// Data members
	private boolean rescue = false;

	private Vehicle vehicleTarget;

	private static MissionManager missionManager;

	private static int oxygenID = ResourceUtil.oxygenID;
	private static int waterID = ResourceUtil.waterID;
	private static int foodID = ResourceUtil.foodID;

	/**
	 * Constructor
	 * 
	 * @param startingPerson the person starting the mission.
	 * @throws MissionException if error constructing mission.
	 */
	public RescueSalvageVehicle(Person startingPerson) {
		// Use RoverMission constructor
		super(DEFAULT_DESCRIPTION, startingPerson, MIN_GOING_MEMBERS);

//		unitManager = Simulation.instance().getUnitManager();
		missionManager = Simulation.instance().getMissionManager();

		if (!isDone()) {
			setStartingSettlement(startingPerson.getSettlement());
			setMissionCapacity(MAX_GOING_MEMBERS);

			if (hasVehicle()) {
				if (vehicleTarget == null)
					vehicleTarget = findBeaconVehicle(getStartingSettlement(), getVehicle().getRange());

				// Obtain a rescuing vehicle and ensure that vehicleTarget is not included.
				if (!reserveVehicle())
					return;

				int capacity = getRover().getCrewCapacity();
				if (capacity < MAX_GOING_MEMBERS) {
					setMissionCapacity(capacity);
				}

				int availableSuitNum = Mission.getNumberAvailableEVASuitsAtSettlement(startingPerson.getSettlement());
				if (availableSuitNum < getMissionCapacity()) {
					setMissionCapacity(availableSuitNum);
				}
			}

			if (vehicleTarget != null) {
				if (getRescuePeopleNum(vehicleTarget) > 0) {
					rescue = true;
					setMinMembers(1);
					setDescription(
							Msg.getString("Mission.description.rescueSalvageVehicle.rescue", vehicleTarget.getName())); // $NON-NLS-1$)
				} else {
					setDescription(
							Msg.getString("Mission.description.rescueSalvageVehicle.salvage", vehicleTarget.getName())); // $NON-NLS-1$)
				}
//				
//				setDescription(
//						Msg.getString("Mission.description.rescueSalvageVehicle.rescue", vehicleTarget.getName())); // $NON-NLS-1$)
				
				// Add navpoints for target vehicle and back home again.
				addNavpoint(new NavPoint(vehicleTarget.getCoordinates(), vehicleTarget.getName()));
				addNavpoint(new NavPoint(getStartingSettlement().getCoordinates(), getStartingSettlement(),
						getStartingSettlement().getName()));

				// Recruit additional members to mission.
				if (!isDone()) {
					recruitMembersForMission(startingPerson);
				}

				// Check if vehicle can carry enough supplies for the mission.
				if (hasVehicle() && !isVehicleLoadable()) {
					endMission(VEHICLE_NOT_LOADABLE);// "Vehicle is not loadable. (RescueSalvageVehicle)");
				}

				// Add rendezvous phase.
				addPhase(RENDEZVOUS);

				// Set initial phase
				setPhase(VehicleMission.APPROVAL);// .EMBARKING);
				setPhaseDescription(
						Msg.getString("Mission.phase.approval.description", getStartingSettlement().getName())); // $NON-NLS-1$
			} else {
				endMission(TARGET_VEHICLE_NOT_FOUND);
			}
		}
	}

	/**
	 * Constructor with explicit data.
	 * 
	 * @param members            collection of mission members.
	 * @param startingSettlement the starting settlement.
	 * @param vehicleTarget      the vehicle to rescue/salvage.
	 * @param rover              the rover to use.
	 * @param description        the mission's description.
	 * @throws MissionException if error constructing mission.
	 */
	public RescueSalvageVehicle(Collection<MissionMember> members, Settlement startingSettlement, Vehicle vehicleTarget,
			Rover rover, String description) {

		// Use RoverMission constructor.
		super(description, (MissionMember) members.toArray()[0], RoverMission.MIN_GOING_MEMBERS, rover);

//		unitManager = Simulation.instance().getUnitManager();
		missionManager = Simulation.instance().getMissionManager();

		setStartingSettlement(startingSettlement);
		this.vehicleTarget = vehicleTarget;
		setMissionCapacity(getRover().getCrewCapacity());

		if (getRescuePeopleNum(vehicleTarget) > 0) {
			rescue = true;
		}

		// Add navpoints for target vehicle and back home again.
		addNavpoint(new NavPoint(vehicleTarget.getCoordinates(), vehicleTarget.getName()));
		addNavpoint(
				new NavPoint(startingSettlement.getCoordinates(), startingSettlement, startingSettlement.getName()));

		Person person = null;
		Robot robot = null;

		// Add mission members.
		Iterator<MissionMember> i = members.iterator();
		while (i.hasNext()) {
			MissionMember member = i.next();
			// TODO refactor
			if (member instanceof Person) {
				person = (Person) member;
				person.getMind().setMission(this);
			} else if (member instanceof Robot) {
				robot = (Robot) member;
				robot.getBotMind().setMission(this);
			}
		}

		// Add rendezvous phase.
		addPhase(RENDEZVOUS);

		// Set initial phase
		setPhase(VehicleMission.APPROVAL);// .EMBARKING);
		setPhaseDescription(Msg.getString("Mission.phase.approval.description", getStartingSettlement().getName())); // $NON-NLS-1$

		// Check if vehicle can carry enough supplies for the mission.
		if (hasVehicle() && !isVehicleLoadable()) {
			endMission("Vehicle is not loadable. (RescueSalvageVehicle)");
		}
	}

	@Override
	protected boolean isUsableVehicle(Vehicle vehicle) {
		if (vehicle != null) {
			boolean usable = true;

//			 if (vehicleTarget == null)
//			 vehicleTarget = findAvailableBeaconVehicle(getStartingSettlement(),
//			 getVehicle().getRange());

			// Filter off the vehicleTarget as the candidate vehicle to be used for rescue
			if (vehicleTarget != null && vehicleTarget.equals(vehicle))
				return false;

			if (!(vehicle instanceof Rover))
				usable = false;

			if (vehicle.isReservedForMission())
				usable = false;

			StatusType status = vehicle.getStatus();
			if (!(vehicle.getStatus() == StatusType.PARKED || vehicle.getStatus() == StatusType.GARAGED)
					&& !status.equals(StatusType.MAINTENANCE))
				usable = false;

			if (vehicle.getInventory().getTotalInventoryMass(false) > 0D)
				usable = false;

			return usable;
		} else {
			throw new IllegalArgumentException("isUsableVehicle: newVehicle is null.");
		}
	}

	@Override
	protected void setVehicle(Vehicle newVehicle) {
		super.setVehicle(newVehicle);
		if (getVehicle() == newVehicle) {
			if (newVehicle.isReservedForMaintenance()) {
				newVehicle.setReservedForMaintenance(false);
			}
		}
	}

	/**
	 * Check if mission is a rescue mission or a salvage mission.
	 * 
	 * @return true if rescue mission
	 */
	public boolean isRescueMission() {
		return rescue;
	}

	/**
	 * Gets the vehicle being rescued/salvaged by this mission.
	 * 
	 * @return vehicle
	 */
	public Vehicle getVehicleTarget() {
		return vehicleTarget;
	}

	/**
	 * Determines a new phase for the mission when the current phase has ended.
	 * 
	 * @throws MissionException if problem setting a new phase.
	 */
	protected void determineNewPhase() {
		if (APPROVAL.equals(getPhase())) {
			setPhase(VehicleMission.EMBARKING);
			setPhaseDescription(
					Msg.getString("Mission.phase.embarking.description", getStartingSettlement().getDescription())); // $NON-NLS-1$
		}

		else if (EMBARKING.equals(getPhase())) {
			startTravelToNextNode();
			setPhase(VehicleMission.TRAVELLING);
			setPhaseDescription(
					Msg.getString("Mission.phase.travelling.description", getNextNavpoint().getDescription())); // $NON-NLS-1$
			if (rescue) {
				logger.info(getVehicle().getName() + " has commenced a rescue mission for " + vehicleTarget.getName());
			} else {
				logger.info(getVehicle().getName() + " has commenced a salvage mission for " + vehicleTarget.getName());
			}
		}

		else if (TRAVELLING.equals(getPhase())) {
			if (null != getCurrentNavpoint() && getCurrentNavpoint().isSettlementAtNavpoint()) {
				setPhase(VehicleMission.DISEMBARKING);
				setPhaseDescription(Msg.getString("Mission.phase.disembarking.description",
						getCurrentNavpoint().getSettlement().getName())); // $NON-NLS-1$
			} else {
				setPhase(RENDEZVOUS);
				if (rescue) {
					setPhaseDescription(
							Msg.getString("Mission.phase.rendezvous.descriptionRescue", vehicleTarget.getName())); // $NON-NLS-1$
				} else {
					setPhaseDescription(
							Msg.getString("Mission.phase.rendezvous.descriptionSalvage", vehicleTarget.getName())); // $NON-NLS-1$
				}
			}
		}

		else if (RENDEZVOUS.equals(getPhase())) {
			startTravelToNextNode();
			setPhase(VehicleMission.TRAVELLING);
			setPhaseDescription(
					Msg.getString("Mission.phase.travelling.description", getNextNavpoint().getDescription())); // $NON-NLS-1$
		}

		else if (DISEMBARKING.equals(getPhase())) {
			endMission(SUCCESSFULLY_DISEMBARKED);
		}
	}

	@Override
	protected void performPhase(MissionMember member) {
		super.performPhase(member);
		if (RENDEZVOUS.equals(getPhase())) {
			rendezvousPhase(member);
		}
	}

	/**
	 * Performs the rendezvous phase of the mission.
	 * 
	 * @param member the mission member currently performing the mission.
	 */
	private void rendezvousPhase(MissionMember member) {

		logger.info("[" + member.getLocationTag().getQuickLocation() + "] " + getVehicle().getName()
				+ " has arrived to rendezvous with " + vehicleTarget.getName() + ".");

		// If rescuing vehicle crew, load rescue life support resources into vehicle (if
		// possible).
		if (rescue) {
			Map<Integer, Number> rescueResources = determineRescueResourcesNeeded(true);

			for (Integer resource : rescueResources.keySet()) {
				double amount = (Double) rescueResources.get(resource);
				Inventory roverInv = getRover().getInventory();
				Inventory targetInv = vehicleTarget.getInventory();
				double amountNeeded = amount - targetInv.getARStored(resource, false);

				if ((amountNeeded > 0) && (roverInv.getARStored(resource, false) > amountNeeded)) {
					roverInv.retrieveAR(resource, amountNeeded);

					targetInv.storeAR(resource, amountNeeded, true);

				}
			}
		}

		// Hook vehicle up for towing.
		getRover().setTowedVehicle(vehicleTarget);
		vehicleTarget.setTowingVehicle(getRover());

		setPhaseEnded(true);

//        String issue = ((Person)member).getPhysicalCondition().getHealthSituation();
//        if (vehicleTarget.getMalfunctionManager().getMostSeriousMalfunction() != null)
//        String issue = vehicleTarget.getMalfunctionManager().getMostSeriousMalfunction().getName();
//        if (issue == null)
//        	issue = vehicleTarget.getMalfunctionManager().getMostSeriousEmergencyMalfunction().getName();

		// The member of the rescuing vehicle will turn off the target vehicle's
		// emergency beacon.
		if (vehicleTarget.isBeaconOn())
			setEmergencyBeacon(member, vehicleTarget, false, "None");

		// Set mission event.
		if (rescue) {
			HistoricalEvent newEvent = new MissionHistoricalEvent(EventType.MISSION_RENDEZVOUS, this,
					"Stranded Vehicle", // cause
					this.getName(), // during
					member.getName(), // member
					member.getVehicle().getName(), // loc0
					vehicleTarget.getLocationTag().getLocale() // loc1
			);
			Simulation.instance().getEventManager().registerNewEvent(newEvent);
		} else {
			HistoricalEvent newEvent = new MissionHistoricalEvent(EventType.MISSION_RENDEZVOUS, this,
					"Salvaged Vehicle", this.getName(), member.getName(), member.getVehicle().getName(),
					vehicleTarget.getLocationTag().getLocale());
			Simulation.instance().getEventManager().registerNewEvent(newEvent);
		}

	}

	/**
	 * Performs the disembark to settlement phase of the mission.
	 * 
	 * @param person              the person currently performing the mission.
	 * @param disembarkSettlement the settlement to be disembarked to.
	 * @throws MissionException if error performing phase.
	 */
	protected void performDisembarkToSettlementPhase(Person person, Settlement disembarkSettlement) {

		// Put towed vehicle and crew in settlement if necessary.
		if (hasVehicle()) {
			disembarkTowedVehicles(person, getRover(), disembarkSettlement);
		}

		super.performDisembarkToSettlementPhase(person, disembarkSettlement);
	}

	/**
	 * Stores the towed vehicle and any crew at settlement.
	 * 
	 * @param rover               the towing rover.
	 * @param disembarkSettlement the settlement to store the towed vehicle in.
	 * @throws MissionException if error disembarking towed vehicle.
	 */
	private void disembarkTowedVehicles(Person person, Rover rover, Settlement disembarkSettlement) {

		if (rover.getTowedVehicle() != null) {
			Vehicle towedVehicle = rover.getTowedVehicle();

			// Unhook towed vehicle.
			rover.setTowedVehicle(null);
			towedVehicle.setTowingVehicle(null);
			logger.info(rover + " is being unhooked from " + towedVehicle + " at " + disembarkSettlement);

			// Re-orient the location/position of the vehicle to avoid being placed inside a
			// garage.
			rover.determinedSettlementParkedLocationAndFacing();
			towedVehicle.determinedSettlementParkedLocationAndFacing();

			// Store towed vehicle in settlement.
			disembarkSettlement.getInventory().storeUnit(towedVehicle);

			// Add vehicle to a garage if available.
			BuildingManager.addToRandomBuilding((GroundVehicle) towedVehicle, disembarkSettlement);

			// towedVehicle.determinedSettlementParkedLocationAndFacing();
			logger.info(towedVehicle + " has been towed to " + disembarkSettlement.getName());

			String issue = "";
			if (vehicleTarget.getMalfunctionManager().getMostSeriousMalfunction() != null)
				issue = vehicleTarget.getMalfunctionManager().getMostSeriousMalfunction().getName();
			if (issue == null && vehicleTarget.getMalfunctionManager().getMostSeriousEmergencyMalfunction() != null)
				issue = vehicleTarget.getMalfunctionManager().getMostSeriousEmergencyMalfunction().getName();

			HistoricalEvent salvageEvent = new MissionHistoricalEvent(EventType.MISSION_SALVAGE_VEHICLE, this, issue,
					this.getName(), towedVehicle.getName(), // person.getName(),
					person.getLocationTag().getImmediateLocation(), // .getVehicle().getName(),
					person.getLocationTag().getLocale());
			Simulation.instance().getEventManager().registerNewEvent(salvageEvent);

			// Unload any crew at settlement.
			if (towedVehicle instanceof Crewable) {
				Crewable crewVehicle = (Crewable) towedVehicle;

				for (Person p : crewVehicle.getCrew()) {
					towedVehicle.getInventory().retrieveUnit(p);

					if (p.isDeclaredDead()) {
						p.setBuriedSettlement(disembarkSettlement);
						p.getPhysicalCondition().getDeathDetails().setBodyRetrieved(true);
						logger.info(p.getName() + "'s body has been retrieved during the rescue operation.");
					} else {
						disembarkSettlement.getInventory().storeUnit(p);
						logger.info(p.getName() + " has been rescued.");
					}

					BuildingManager.addToRandomBuilding(p, disembarkSettlement);
					p.setAssociatedSettlement(disembarkSettlement);
					p.getMind().getTaskManager().clearTask();

					HistoricalEvent rescueEvent = new MissionHistoricalEvent(EventType.MISSION_RESCUE_PERSON, this,
							p.getPhysicalCondition().getHealthSituation(), p.getTaskDescription(), p.getName(),
							p.getVehicle().getName(), p.getLocationTag().getLocale());
					Simulation.instance().getEventManager().registerNewEvent(rescueEvent);
				}
			}

			// Retrieve the person if he/she is dead
			for (Person p : rover.getCrew()) {
				rover.getInventory().retrieveUnit(p);

				if (p.isDeclaredDead()) {
					p.setBuriedSettlement(disembarkSettlement);
					p.getPhysicalCondition().getDeathDetails().setBodyRetrieved(true);
					logger.info(p.getName() + "'s body has been retrieved during the rescue operation.");
				} else {
					disembarkSettlement.getInventory().storeUnit(p);
				}

				BuildingManager.addToRandomBuilding(p, disembarkSettlement);
				p.setAssociatedSettlement(disembarkSettlement);
				p.getMind().getTaskManager().clearTask();

//				logger.info(p.getName() + " has completed the rescue operation.");
//				 HistoricalEvent rescueEvent = new MissionHistoricalEvent(p,
//				 this, p.getSettlement().getName(), EventType..MISSION_RESCUE_PERSON);
//				 Simulation.instance().getEventManager().registerNewEvent(rescueEvent);

			}

			// Unhook the towed vehicle this vehicle is towing if any.
//			 if (towedVehicle instanceof Rover) {
//			 disembarkTowedVehicles(person, (Rover) towedVehicle, disembarkSettlement);
//			 }
		}
	}

	/**
	 * Gets the resources needed for the crew to be rescued.
	 * 
	 * @param useBuffer use time buffers in estimation if true.
	 * @return map of amount resources and their amounts.
	 * @throws MissionException if error determining resources.
	 */
	private Map<Integer, Number> determineRescueResourcesNeeded(boolean useBuffer) {
		Map<Integer, Number> result = new HashMap<Integer, Number>(3);

		// Determine estimate time for trip.
		double distance = vehicleTarget.getCoordinates().getDistance(getStartingSettlement().getCoordinates());
		double time = getEstimatedTripTime(true, distance);
		double timeSols = time / 1000D;

		int peopleNum = getRescuePeopleNum(vehicleTarget);

		// Determine life support supplies needed for trip.
		double oxygenAmount = PhysicalCondition.getOxygenConsumptionRate() * timeSols * peopleNum
				* Mission.OXYGEN_MARGIN;
		if (useBuffer) {
			oxygenAmount *= Vehicle.getLifeSupportRangeErrorMargin();
		}
		result.put(oxygenID, oxygenAmount);

		double waterAmount = PhysicalCondition.getWaterConsumptionRate() * timeSols * peopleNum * Mission.WATER_MARGIN;
		if (useBuffer) {
			waterAmount *= Vehicle.getLifeSupportRangeErrorMargin();
		}
		result.put(waterID, waterAmount);

		double foodAmount = PhysicalCondition.getFoodConsumptionRate() * timeSols * peopleNum * Mission.FOOD_MARGIN;
		if (useBuffer) {
			foodAmount *= Vehicle.getLifeSupportRangeErrorMargin();
		}
		result.put(foodID, foodAmount);

		return result;
	}

	/**
	 * Finds the closest available rescue or salvage vehicles within range.
	 * 
	 * @param settlement the starting settlement.
	 * @param range      the available range (km).
	 * @return vehicle or null if none available.
	 */
	public static Vehicle findBeaconVehicle(Settlement settlement, double range) {
		Vehicle result = null;
		double halfRange = range / 2D;

		Collection<Vehicle> emergencyBeaconVehicles = new ConcurrentLinkedQueue<Vehicle>();
		Collection<Vehicle> vehiclesNeedingRescue = new ConcurrentLinkedQueue<Vehicle>();

//		if (unitManager == null)
//			unitManager = Simulation.instance().getUnitManager();
		// Find all available vehicles.
		// Iterator<Vehicle> iV = unitManager.getVehicles().iterator();
		// while (iV.hasNext()) {
		for (Vehicle vehicle : Simulation.instance().getUnitManager().getVehicles()) {// = iV.next();
			if (vehicle.isBeaconOn() && !isVehicleAlreadyMissionTarget(vehicle)) {
				emergencyBeaconVehicles.add(vehicle);

				if (vehicle instanceof Crewable) {
					if (((Crewable) vehicle).getCrewNum() > 0 || ((Crewable) vehicle).getRobotCrewNum() > 0) {
						vehiclesNeedingRescue.add(vehicle);
					}
				}
			}
		}

		// Check for vehicles with crew needing rescue first.
		if (vehiclesNeedingRescue.size() > 0) {
			Vehicle vehicle = findClosestVehicle(settlement.getCoordinates(), vehiclesNeedingRescue);
			if (vehicle != null) {
				double vehicleRange = settlement.getCoordinates().getDistance(vehicle.getCoordinates());
				if (vehicleRange <= halfRange) {
					result = vehicle;
				}
			}
		}

		// Check for vehicles needing salvage next.
		if ((result == null) && (emergencyBeaconVehicles.size() > 0)) {
			Vehicle vehicle = findClosestVehicle(settlement.getCoordinates(), emergencyBeaconVehicles);
			if (vehicle != null) {
				double vehicleRange = settlement.getCoordinates().getDistance(vehicle.getCoordinates());
				if (vehicleRange <= halfRange) {
					result = vehicle;
				}
			}
		}

		return result;
	}

	/**
	 * Checks if vehicle is already the target of a rescue/salvage vehicle mission.
	 * 
	 * @param vehicle the vehicle to check.
	 * @return true if already mission target.
	 */
	private static boolean isVehicleAlreadyMissionTarget(Vehicle vehicle) {
		boolean result = false;

		if (missionManager == null)
			missionManager = Simulation.instance().getMissionManager();
		// MissionManager manager = Simulation.instance().getMissionManager();
		Iterator<Mission> i = missionManager.getMissions().iterator();
		while (i.hasNext() && !result) {
			Mission mission = i.next();
			if (mission instanceof RescueSalvageVehicle) {
				Vehicle vehicleTarget = ((RescueSalvageVehicle) mission).vehicleTarget;
				if (vehicle == vehicleTarget) {
					result = true;
				}
			}
		}

		return result;
	}

	/**
	 * Gets the closest vehicle in a vehicle collection
	 * 
	 * @param location the location to measure from.
	 * @param vehicles the vehicle collection.
	 * @return closest vehicle.
	 */
	private static Vehicle findClosestVehicle(Coordinates location, Collection<Vehicle> vehicles) {
		Vehicle closest = null;
		double closestDistance = Double.MAX_VALUE;
		Iterator<Vehicle> i = vehicles.iterator();
		while (i.hasNext()) {
			Vehicle vehicle = i.next();
			double vehicleDistance = location.getDistance(vehicle.getCoordinates());
			if (vehicleDistance < closestDistance) {
				closest = vehicle;
				closestDistance = vehicleDistance;
			}
		}
		return closest;
	}

	/**
	 * Gets the number of people in the vehicle who needs to be rescued.
	 * 
	 * @param vehicle the vehicle.
	 * @return number of people.
	 */
	public static int getRescuePeopleNum(Vehicle vehicle) {
		int result = 0;

		if (vehicle instanceof Crewable)
			result = ((Crewable) vehicle).getCrewNum();
		return result;
	}

	/**
	 * Gets the number of robots in the vehicle that needs to be rescued.
	 * 
	 * @param vehicle
	 * @return
	 */
	public static int getRescueRobotsNum(Vehicle vehicle) {
		int result = 0;

		if (vehicle instanceof Crewable)
			result = ((Crewable) vehicle).getRobotCrewNum();
		return result;
	}

	/**
	 * Gets the settlement associated with the mission.
	 * 
	 * @return settlement or null if none.
	 */
	public Settlement getAssociatedSettlement() {
		return getStartingSettlement();
	}

	/**
	 * Gets a map of resources needed for the remaining mission.
	 * 
	 * @param useBuffer
	 * @return a map of resources
	 */
	@Override
	public Map<Integer, Number> getResourcesNeededForRemainingMission(boolean useBuffer) {

		Map<Integer, Number> result = super.getResourcesNeededForRemainingMission(useBuffer);

		// Include rescue resources if needed.
		if (rescue && (getRover().getTowedVehicle() == null)) {
			Map<Integer, Number> rescueResources = determineRescueResourcesNeeded(useBuffer);

//            int cutOffID = SimulationConfig.instance().getResourceConfiguration().getNextID();

			for (Integer resource : rescueResources.keySet()) {// = i.next();
				if (resource < 1000) {
//                if (resource instanceof AmountResource) {
					double amount = (Double) rescueResources.get(resource);
					if (result.containsKey(resource)) {
						amount += (Double) result.get(resource);
					}
					if (useBuffer) {
						amount *= RESCUE_RESOURCE_BUFFER;
					}
					result.put(resource, amount);
				} else {
					int num = (Integer) rescueResources.get(resource);
					if (result.containsKey(resource)) {
						num += (Integer) result.get(resource);
					}
					result.put(resource, num);
				}
			}
		}

		return result;
	}

	/**
	 * Gets a map of equipment needed for the remaining mission.
	 * 
	 * @param useBuffer
	 * @return a map of equipment
	 */
	@Override
	public Map<Integer, Integer> getEquipmentNeededForRemainingMission(boolean useBuffer) {
		if (equipmentNeededCache != null) {
			return equipmentNeededCache;
		} else {
			Map<Integer, Integer> result = new HashMap<>();
			equipmentNeededCache = result;
			return result;
		}
	}

	/**
	 * Gets the mission qualification score
	 * 
	 * @param member
	 * @return the score
	 */
	@Override
	public double getMissionQualification(MissionMember member) {
		double result = 0D;

//        if (isCapableOfMission(member)) {
		result = super.getMissionQualification(member);

		if (member instanceof Person) {
			Person person = (Person) member;

			// If person has the "Driver" job, add 1 to their qualification.
			if (person.getMind().getJob() instanceof Driver) {
				result += 1D;
			}
		}
//        }

		return result;
	}

	/**
	 * Is this member capable of doing the mission
	 * 
	 * @param member
	 * @return true or false
	 */
	@Override
	protected boolean isCapableOfMission(MissionMember member) {
		boolean result = super.isCapableOfMission(member);

		if (result) {
			boolean atStartingSettlement = false;
//			if (member.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
			if (member.getSettlement() == getStartingSettlement()) {
				atStartingSettlement = true;
			}
//			}
			result = atStartingSettlement;
		}

		return result;
	}

	/**
	 * Checks if this is the closest settlement to a beacon vehicle that could
	 * rescue/salvage it.
	 * 
	 * @param thisSettlement this settlement.
	 * @param thisVehicle    the beacon vehicle.
	 * @return true if this is the closest settlement.
	 * @throws MissionException if error in checking settlements.
	 */
	public static boolean isClosestCapableSettlement(Settlement thisSettlement, Vehicle thisVehicle) {
		boolean result = true;

		double distance = thisSettlement.getCoordinates().getDistance(thisVehicle.getCoordinates());

		Iterator<Settlement> iS = Simulation.instance().getUnitManager().getSettlements().iterator();
		while (iS.hasNext() && result) {
			Settlement settlement = iS.next();
			if (settlement != thisSettlement) {
				double settlementDistance = settlement.getCoordinates().getDistance(thisVehicle.getCoordinates());
				if (settlementDistance < distance) {
					if (settlement.getIndoorPeopleCount() >= MIN_GOING_MEMBERS) {
						Iterator<Vehicle> iV = settlement.getParkedVehicles().iterator();
						while (iV.hasNext() && result) {
							Vehicle vehicle = iV.next();
							if (vehicle instanceof Rover) {
								if (vehicle.getRange() >= (settlementDistance * 2D)) {
									result = false;
								}
							}
						}
					}
				}
			}
		}

		return result;
	}

	/**
	 * Gets the resources needed for loading the vehicle.
	 * 
	 * @return resources and their number.
	 * @throws MissionException if error determining resources.
	 */
	public Map<Integer, Number> getResourcesToLoad() {
		// Override and full rover with fuel and life support resources.
		Map<Integer, Number> result = new HashMap<Integer, Number>(4);
		Inventory inv = getVehicle().getInventory();
		result.put(getVehicle().getFuelType(), inv.getARCapacity(getVehicle().getFuelType(), false));
		result.put(oxygenID, inv.getARCapacity(oxygenID, false));
		result.put(waterID, inv.getARCapacity(waterID, false));
		result.put(foodID, inv.getARCapacity(foodID, false));

		// Get parts too.
		result.putAll(getPartsNeededForTrip(getTotalRemainingDistance()));

		return result;
	}

	@Override
	public void destroy() {
		super.destroy();

		vehicleTarget = null;
		missionManager = null;
	}
}