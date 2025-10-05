/*
 * Mars Simulation Project
 * RescueSalvageVehicle.java
 * @date 2021-10-20
 * @author Scott Davis
 */

package com.mars_sim.core.person.ai.mission;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.events.HistoricalEvent;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.malfunction.Malfunction;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.mission.objectives.RescueVehicleObjective;
import com.mars_sim.core.person.EventType;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.vehicle.Crewable;
import com.mars_sim.core.vehicle.Rover;
import com.mars_sim.core.vehicle.Vehicle;

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
public class RescueSalvageVehicle extends RoverMission {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(RescueSalvageVehicle.class.getName());

	// Static members
	public static final int MIN_STAYING_MEMBERS = 1;
	private static final int MIN_GOING_MEMBERS = 2;

	public static final double BASE_RESCUE_MISSION_WEIGHT = 100D;
	private static final double RESCUE_RESOURCE_BUFFER = 1D;

	// Mission phases
	private static final MissionPhase RENDEZVOUS = new MissionPhase("Mission.phase.rendezvous");

	private static final MissionStatus TARGET_VEHICLE_NOT_FOUND = new MissionStatus("Mission.status.noTargetVehicle");
	
	// Data members
	private RescueVehicleObjective objective;

	/**
	 * Constructor.
	 * 
	 * @param startingPerson the person starting the mission.
	 * @throws MissionException if error constructing mission.
	 */
	public RescueSalvageVehicle(Person startingPerson, boolean needsReview) {
		// Use RoverMission constructor
		super(MissionType.RESCUE_SALVAGE_VEHICLE, startingPerson, null);

		setPriority(5);
		
		if (!isDone()) {			
			var vehicleTarget = findBeaconVehicle(getStartingSettlement(), getVehicle().getRange());

			if (vehicleTarget != null) {
				setName(Msg.getString("Mission.description.rescueSalvageVehicle.rescue", // $NON-NLS-1$
						vehicleTarget.getName()));
						
				// Add navpoints for target vehicle and back home again.
				addNavpoint(vehicleTarget.getCoordinates(), vehicleTarget.getName());
				addNavpoint(getStartingSettlement());

				// Recruit additional members to mission.
				if (!recruitMembersForMission(startingPerson, MIN_GOING_MEMBERS)) {
					return;
				}

				addObjectives(needsReview, vehicleTarget, true);
				
			}
			else {
				endMission(TARGET_VEHICLE_NOT_FOUND);
			}
		}
	}

	private void addObjectives(boolean needsReview, Vehicle target, boolean isRescue) {
		if (!hasVehicle()) {
			endMission(NO_AVAILABLE_VEHICLE);
			return;
		}

		// Need objective to calculate resources needed.
		objective = new RescueVehicleObjective(target, isRescue);
		addObjective(objective);
		
		// Check if vehicle can carry enough supplies for the mission.
		if (hasVehicle() && !isVehicleLoadable()) {
			endMission(CANNOT_LOAD_RESOURCES);
			return;
		}
	


		setInitialPhase(needsReview);
	}
	
	/**
	 * Constructor with explicit data.
	 * 
	 * @param members            collection of mission members.
	 * @param vehicleTarget      the vehicle to rescue/salvage.
	 * @param rover              the rover to use.
	 * @throws MissionException if error constructing mission.
	 */
	public RescueSalvageVehicle(Collection<Worker> members, Vehicle vehicleTarget,
			Rover rover) {

		// Use RoverMission constructor.
		super(MissionType.RESCUE_SALVAGE_VEHICLE, (Worker) members.toArray()[0], rover);
		
		boolean rescue = false;
		if (getRescuePeopleNum(vehicleTarget) > 0) {
			rescue = true;
		}

		// Add navpoints for target vehicle and back home again.
		addNavpoint(vehicleTarget.getCoordinates(), vehicleTarget.getName());
		Settlement s = getStartingSettlement();
		addNavpoint(s);

		// Add mission members.
		addMembers(members, false);

		addObjectives(false, vehicleTarget, rescue);
	}

	@Override
	protected boolean isUsableVehicle(Vehicle vehicle) {
		// Filter off the vehicleTarget as the candidate vehicle to be used for rescue
		if ((objective != null) && vehicle.equals(objective.getRecoverVehicle())
			|| !(vehicle instanceof Rover)
			|| vehicle.isReservedForMission()) {
			return false;
		}

		return vehicle.isUsableVehicle();
	}


	/**
	 * Determines a new phase for the mission when the current phase has ended.
	 * 
	 * @throws MissionException if problem setting a new phase.
	 */
	@Override
	protected boolean determineNewPhase() {
		boolean handled = true;
	
		if (!super.determineNewPhase()) {
			if (TRAVELLING.equals(getPhase())) {
				if (isCurrentNavpointSettlement()) {
					startDisembarkingPhase();
				}
				else {
					setPhase(RENDEZVOUS, objective.getName());
				}
			}
	
			else if (RENDEZVOUS.equals(getPhase())) {
				startTravellingPhase();
			}

			else {
				handled = false;
			}
		}
		return handled;
	}

	@Override
	protected void performPhase(Worker member) {
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
	private void rendezvousPhase(Worker member) {
		var vehicleTarget = objective.getRecoverVehicle();
		logger.info(member, 5000, "Has arrived to rendezvous with " + vehicleTarget.getName() + ".");

		// If rescuing vehicle crew, load rescue life support resources into vehicle (if
		// possible).
		if (objective.isRescue()) {
			Map<Integer, Number> rescueResources = determineRescueResourcesNeeded(vehicleTarget, true);

			for (var required : rescueResources.entrySet()) {
				int resource = required.getKey();
				double amount = required.getValue().doubleValue();
				double amountNeeded = amount - vehicleTarget.getSpecificAmountResourceStored(resource);

				if ((amountNeeded > 0) && (getRover().getSpecificAmountResourceStored(resource) > amountNeeded)) {
					getRover().retrieveAmountResource(resource, amountNeeded);
					vehicleTarget.storeAmountResource(resource, amountNeeded);
				}
			}
		}

		// Hook vehicle up for towing.
		getRover().setTowedVehicle(vehicleTarget);
		vehicleTarget.setTowingVehicle(getRover());

		setPhaseEnded(true);

		// The member of the rescuing vehicle will turn off the target vehicle's
		// emergency beacon.
		if (vehicleTarget.isBeaconOn()) {
			vehicleTarget.setEmergencyBeacon(false);
		}

		// Set mission event.
		HistoricalEvent newEvent = new MissionHistoricalEvent(EventType.MISSION_RENDEZVOUS, this,
					(objective.isRescue() ? "Rescue Stranded Vehicle"
							: "Salvage Vehicle"),
					getName(), // during
					member.getName(), // member
					vehicleTarget
		);
		eventManager.registerNewEvent(newEvent);
	}

	/**
	 * Performs the disembark to settlement phase of the mission.
	 * 
	 * @param person              the person currently performing the mission.
	 * @param disembarkSettlement the settlement to be disembarked to.
	 * @throws MissionException if error performing phase.
	 */
	protected void performDisembarkToSettlementPhase(Person person, Settlement disembarkSettlement) {

		super.performDisembarkToSettlementPhase(person, disembarkSettlement);
		
		reportMalfunction(getRover());
	}

	/**
	 * Reports the vehicle malfunction.
	 * 
	 * @param rover               the towing rover.
	 * @param disembarkSettlement the settlement to store the towed vehicle in.
	 * @throws MissionException if error disembarking towed vehicle.
	 */
	private void reportMalfunction(Rover rover) {

    	Malfunction serious = objective.getRecoverVehicle().getMalfunctionManager().getMostSeriousMalfunction();
		if (serious != null) {
			HistoricalEvent salvageEvent = new MissionHistoricalEvent(
					EventType.MISSION_SALVAGE_VEHICLE, 
					this, 
					serious.getName(),
				this.getName(), 
				null,
				rover
				);
			eventManager.registerNewEvent(salvageEvent);
		}

	}

	/**
	 * Gets the resources needed for the crew to be rescued.
	 * 
	 * @param useBuffer use time buffers in estimation if true.
	 * @return map of amount resources and their amounts.
	 * @throws MissionException if error determining resources.
	 */
	private Map<Integer, Number> determineRescueResourcesNeeded(Vehicle vehicleTarget, boolean useBuffer) {
		Map<Integer, Number> result = new HashMap<>();

		// Determine estimate time for trip.
		double distance = vehicleTarget.getCoordinates().getDistance(getStartingSettlement().getCoordinates());
		double time = getEstimatedTripTime(true, distance);
		double timeSols = time / 1000D;

		int peopleNum = getRescuePeopleNum(vehicleTarget);

		// Determine life support supplies needed for to support rescued people
		result = addLifeSupportResources(result, peopleNum, timeSols, useBuffer);

		// Add extra EVA Suits based on how many people to be rescued
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

		Collection<Vehicle> emergencyBeaconVehicles = new ConcurrentLinkedQueue<>();
		Collection<Vehicle> vehiclesNeedingRescue = new ConcurrentLinkedQueue<>();

		// Find all available vehicles.
		for (Vehicle vehicle : unitManager.getVehicles()) {
			if (vehicle.isBeaconOn() && (getRescueingVehicle(vehicle) == null)) {
				emergencyBeaconVehicles.add(vehicle);

				if (vehicle instanceof Crewable crew
						&& (crew.getCrewNum() > 0 || crew.getRobotCrewNum() > 0)) {
					vehiclesNeedingRescue.add(vehicle);
				}			
			}
		}

		// Check for vehicles with crew needing rescue first.
		if (!vehiclesNeedingRescue.isEmpty()) {
			Vehicle vehicle = findClosestVehicle(settlement.getCoordinates(), vehiclesNeedingRescue);
			if (vehicle != null) {
				double vehicleRange = settlement.getCoordinates().getDistance(vehicle.getCoordinates());
				if (vehicleRange <= halfRange) {
					result = vehicle;
				}
			}
		}

		// Check for vehicles needing salvage next.
		if ((result == null) && (!emergencyBeaconVehicles.isEmpty())) {
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
	 * Find if this vehicle is already the target of a rescue/salvage vehicle mission.
	 * 
	 * @param vehicle the vehicle to check.
	 * @return The vehcile doing the rescue
	 */
	public static Vehicle getRescueingVehicle(Vehicle vehicle) {
		var mMgr = Simulation.instance().getMissionManager();

		return mMgr.getMissions().stream()
			.filter(RescueSalvageVehicle.class::isInstance)
			.map(m -> (RescueSalvageVehicle)m)
			.filter(mo -> (mo.objective != null
						&& vehicle.equals(mo.objective.getRecoverVehicle())))
			.map(tv -> tv.getVehicle())
			.findAny()
			.orElse(null);
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
		for(Vehicle vehicle : vehicles) {
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
		if (vehicle instanceof Crewable crew)
			return crew.getCrewNum();
		return 0;
	}

	/**
	 * Gets a map of resources needed for the remaining mission.
	 * 
	 * @param useBuffer
	 * @return a map of resources
	 */
	@Override
	protected Map<Integer, Number> getResourcesNeededForRemainingMission(boolean useBuffer) {

		Map<Integer, Number> result = super.getResourcesNeededForRemainingMission(useBuffer);

		// Include rescue resources if needed.
		if (objective.isRescue() && (getRover().getTowedVehicle() == null)) {
			Map<Integer, Number> rescueResources = determineRescueResourcesNeeded(objective.getRecoverVehicle(), useBuffer);

			for (var needed : rescueResources.entrySet()) {
				var id = needed.getKey();
				if (id < ResourceUtil.FIRST_ITEM_RESOURCE_ID) {
					double amount = (Double) needed.getValue();
					if (useBuffer) {
						amount *= RESCUE_RESOURCE_BUFFER;
					}
					if (result.containsKey(id)) {
						amount += (Double) result.get(id);
					}

					result.put(id, amount);
					
				}  // Check if these resources are Parts
				else if (id < ResourceUtil.FIRST_VEHICLE_RESOURCE_ID) {
					int num = (Integer) needed.getValue();
					if (result.containsKey(id)) {
						num += (Integer) result.get(id);
					}
					result.put(id, num);
				}
			}
		}

		return result;
	}

	/**
	 * Gets the mission qualification score.
	 * 
	 * @param member
	 * @return the score
	 */
	@Override
	public double getMissionQualification(Worker member) {
		double result = super.getMissionQualification(member);

		if (member instanceof Person person && person.getMind().getJobType() == JobType.PILOT) {
			result += 1D;
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

		double distance = thisSettlement.getCoordinates().getDistance(thisVehicle.getCoordinates());

		for(Settlement settlement : unitManager.getSettlements()) {
			if (!settlement.equals(thisSettlement)) {
				double settlementDistance = settlement.getCoordinates().getDistance(thisVehicle.getCoordinates());
				if ((settlementDistance < distance) && (settlement.getIndoorPeopleCount() >= MIN_GOING_MEMBERS)) {
					for(Vehicle vehicle : settlement.getParkedGaragedVehicles()) {
						if (vehicle instanceof Rover && vehicle.getEstimatedRange() >= (settlementDistance * 2D)) {
							return false;
						}
					}
				}
			}
		}

		return true;
	}
}
