/*
 * Mars Simulation Project
 * EmergencySupply.java
 * @date 2022-07-14
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.mission;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import com.mars_sim.core.LocalAreaUtil;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.equipment.EVASuitUtil;
import com.mars_sim.core.goods.Good;
import com.mars_sim.core.goods.GoodCategory;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.mission.objectives.EmergencySupplyObjective;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.task.EVAOperation;
import com.mars_sim.core.person.ai.task.Walk;
import com.mars_sim.core.person.ai.task.WalkingSteps;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskJob;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.RandomUtil;
import com.mars_sim.core.vehicle.Rover;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.core.vehicle.comparators.CargoRangeComparator;
import com.mars_sim.core.vehicle.task.LoadVehicleMeta;
import com.mars_sim.core.vehicle.task.UnloadVehicleEVA;
import com.mars_sim.core.vehicle.task.UnloadVehicleGarage;

/**
 * A mission for delivering emergency supplies from one settlement to another.
 */
public class EmergencySupply extends RoverMission {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(EmergencySupply.class.getName());

	// Static members
	private static final int MAX_MEMBERS = 2;

	/** Mission phases. */
	private static final MissionPhase SUPPLY_DELIVERY_DISEMBARKING = new MissionPhase("Mission.phase.supplyDeliveryDisembarking");
	private static final MissionPhase SUPPLY_DELIVERY = new MissionPhase("Mission.phase.supplyDelivery");
	private static final MissionPhase LOAD_RETURN_TRIP_SUPPLIES = new MissionPhase("Mission.phase.loadReturnTripSupplies");
	private static final MissionPhase RETURN_TRIP_EMBARKING = new MissionPhase("Mission.phase.returnTripEmbarking");

	// Data members.
	private boolean outbound;
	private EmergencySupplyObjective supplies;

	/**
	 * Constructor with explicit parameters.
	 *
	 * @param members             collection of mission members.
	 * @param emergencySettlement the starting settlement.
	 * @param rover               the rover used on the mission.
	 */
	public EmergencySupply(Collection<Worker> members, Settlement emergencySettlement,
			Map<Good, Integer> emergencyGoods, Rover rover) {
		// Use RoverMission constructor.
		super(MissionType.EMERGENCY_SUPPLY, (Person) members.toArray()[0], rover);

		outbound = true;

		// Sets the mission capacity.
		calculateMissionCapacity(MAX_MEMBERS);

		// Set emergency settlement.
		addNavpoint(emergencySettlement);
		
		// Add mission members.
		addMembers(members, false);

		supplies = new EmergencySupplyObjective(emergencySettlement, emergencyGoods);
		addObjective(supplies);

		setInitialPhase(false);
	}

	@Override
	protected boolean determineNewPhase() {
		boolean handled = true;
		var emergencySettlement = supplies.getDestination();
		if (!super.determineNewPhase()) {
			if (TRAVELLING.equals(getPhase())) {
				if (isCurrentNavpointSettlement()) {
					if (outbound) {
						setPhase(SUPPLY_DELIVERY_DISEMBARKING, emergencySettlement.getName());
					} else {
						startDisembarkingPhase();
					}
				}
			}

			else if (SUPPLY_DELIVERY_DISEMBARKING.equals(getPhase())) {
				setPhase(SUPPLY_DELIVERY, emergencySettlement.getName());
			}

			else if (SUPPLY_DELIVERY.equals(getPhase())) {
				// Check if vehicle can hold enough supplies for mission.
				if (!isVehicleLoadable()) {
					endMission(CANNOT_LOAD_RESOURCES);
				}
				else {
					setPhase(LOAD_RETURN_TRIP_SUPPLIES, emergencySettlement.getName());
				}
			}

			else if (LOAD_RETURN_TRIP_SUPPLIES.equals(getPhase())) {
				setPhase(RETURN_TRIP_EMBARKING, emergencySettlement.getName());
			}

			else if (RETURN_TRIP_EMBARKING.equals(getPhase())) {
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
		// NOTE: The following 4 phases are unique to this mission
		if (SUPPLY_DELIVERY_DISEMBARKING.equals(getPhase())) {
			performSupplyDeliveryDisembarkingPhase(member);
		} else if (SUPPLY_DELIVERY.equals(getPhase())) {
			performSupplyDeliveryPhase(member);
		} else if (LOAD_RETURN_TRIP_SUPPLIES.equals(getPhase())) {
			performLoadReturnTripSuppliesPhase(member);
		} else if (RETURN_TRIP_EMBARKING.equals(getPhase())) {
			performReturnTripEmbarkingPhase(member);
		}
	}

	/**
	 * Perform the supply delivery disembarking phase.
	 *
	 * @param member the member performing the phase.
	 */
	private void performSupplyDeliveryDisembarkingPhase(Worker member) {

		var emergencySettlement = supplies.getDestination();
		
		// If rover is not parked at settlement, park it.
		if ((getVehicle() != null) && (getVehicle().getSettlement() == null)) {
			// Add the vehicle to the emergency settlement
			emergencySettlement.addVicinityVehicle(getVehicle());
		}

		// Have member exit rover if necessary.
		if (member.isInSettlement()) {

			// Get random inhabitable building at emergency settlement.
			Building destinationBuilding = emergencySettlement.getBuildingManager().getRandomAirlockBuilding();
			if (destinationBuilding != null) {
				LocalPosition adjustedLoc = LocalAreaUtil.getRandomLocalPos(destinationBuilding);

				if (member instanceof Person person) {
					
					WalkingSteps walkingSteps = new WalkingSteps(person, adjustedLoc, destinationBuilding);
					boolean canWalk = Walk.canWalkAllSteps(person, walkingSteps);
					
					if (canWalk) {
						boolean canDo = assignTask(person, new Walk(person, walkingSteps));
						if (!canDo) {
							logger.severe("Unable to start walking to building " + destinationBuilding);
						}
					}
					else {
						logger.severe("Unable to walk to building " + destinationBuilding);
					}
				}
			}
			else {
				endMissionProblem(emergencySettlement, "No inhabitable buildings");
			}
		}

		// End the phase when everyone is out of the rover.
		if (isNoOneInRover()) {
			setPhaseEnded(true);
		}
	}

	/**
	 * Perform the supply delivery phase.
	 *
	 * @param member the mission member performing the phase.
	 */
	private void performSupplyDeliveryPhase(Worker member) {

		// Unload rover if necessary.
		boolean roverUnloaded = getRover().isEmpty();
		if (!roverUnloaded) {
			// Random chance of having person unload (this allows person to do other things
			// sometimes)
			if (member.isInSettlement() && RandomUtil.lessThanRandPercent(50)) {
				
				if (member instanceof Person person) {
					if (isInAGarage()) {
						assignTask(person, new UnloadVehicleGarage(person, getRover()));
					} else if (!EVAOperation.isGettingDark(person)) {
						assignTask(person, new UnloadVehicleEVA(person, getRover()));
					}
				}
				else if (member instanceof Robot robot && isInAGarage()) {
					assignTask(robot, new UnloadVehicleGarage(robot, getRover()));
				}
				
			}
		} else {
			outbound = false;
			addNavpoint(getStartingSettlement());
			setPhaseEnded(true);
		}
	}

	/**
	 * Perform the load return trip supplies phase.
	 *
	 * @param member the mission member performing the phase.
	 */
	private void performLoadReturnTripSuppliesPhase(Worker member) {
		var emergencySettlement = supplies.getDestination();

		if (!isDone() && !isVehicleLoaded()) {
			// Random chance of having person load (this allows person to do other things
			// sometimes)
			if (member.isInSettlement() && RandomUtil.lessThanRandPercent(50)) {

				TaskJob job = LoadVehicleMeta.createLoadJob(getVehicle(), emergencySettlement);
		        if (job != null) {
		            Task task = null;
		            // Create the Task ready for assignment
		            if (member instanceof Person p) {
		                task = job.createTask(p);
		                // Task may be rejected because of the Worker's profile
		                assignTask(p, task);
		            }
		            else if (member instanceof Robot r && isInAGarage()) {
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
	 * Perform the return trip embarking phase.
	 *
	 * @param member the mission member performing the phase.
	 */
	private void performReturnTripEmbarkingPhase(Worker member) {
		var emergencySettlement = supplies.getDestination();
		Vehicle v = getVehicle();
		
		if (member.isInVehicle()) {

			// Move person to random location within rover.
			LocalPosition adjustedLoc = LocalAreaUtil.getRandomLocalPos(v);

			if (member instanceof Person person) {
				if (!person.isDeclaredDead()) {
					
					if (v == null)
						v = person.getVehicle();
					
					// Check if an EVA suit is available
					EVASuitUtil.fetchEVASuitFromAny(person, v, emergencySettlement);

					// If person is not aboard the rover, board rover.
					
					WalkingSteps walkingSteps = new WalkingSteps(person, adjustedLoc, v);
					boolean canWalk = Walk.canWalkAllSteps(person, walkingSteps);
					
					if (canWalk) {
						boolean canDo = assignTask(person, new Walk(person, walkingSteps));
						if (!canDo) {
							logger.warning(person, "Unable to start walk to " + v + ".");
						}
					}
					
					else {
						endMissionProblem(person, "Cannot enter " + v.getName());
					}
				}
			}
		}

		// If rover is loaded and everyone is aboard, embark from settlement.
		if (isEveryoneInRover()) {
			
			// Embark from settlement
			if (v.transfer(unitManager.getMarsSurface())) {
				setPhaseEnded(true);
			}
			else {
				endMissionProblem(v, "Could not transfer to the surface.");
			}
		}
	}

	@Override
	public Map<Integer, Integer> getEquipmentNeededForRemainingMission(boolean useBuffer) {
		return new HashMap<>();
	}

	/**
	 * Get the Vehicle comparator that is based on largest cargo
	 */
	@Override
	protected  Comparator<Vehicle> getVehicleComparator() {
		return new CargoRangeComparator();
	}
	
	@Override
	protected Map<Integer, Number> getRequiredResourcesToLoad() {
		Map<Integer, Number> result = super.getResourcesNeededForRemainingMission(true);

		// Add any emergency resources needed.
		if (outbound) {
			for (var e : supplies.getSupplies().entrySet()) {
				Good good = e.getKey();
				if (good.getCategory() == GoodCategory.AMOUNT_RESOURCE) {
					int amount = e.getValue();
					result.merge(good.getID(), amount, (a,b) -> a.doubleValue() + b.doubleValue());
				}
			}
		}

		return result;
	}

	@Override
	protected Map<Integer, Number> getOptionalResourcesToLoad() {

		Map<Integer, Number> result = super.getOptionalResourcesToLoad();

		// Add any emergency parts needed.
		if (outbound) {
			for (var e : supplies.getSupplies().entrySet()) {
				Good good = e.getKey();
				if (good.getCategory() == GoodCategory.ITEM_RESOURCE) {
					int amount = e.getValue();
					result.merge(good.getID(), amount, (a,b) -> a.intValue() + b.intValue());
				}
			}
		}

		return result;
	}

	@Override
	public Map<Integer, Integer> getRequiredEquipmentToLoad() {

		Map<Integer, Integer> result = getEquipmentNeededForRemainingMission(true);

		// Add any emergency equipment needed.
		if (outbound) {
			for (var e : supplies.getSupplies().entrySet()) {
				Good good = e.getKey();
				if (good.getCategory() == GoodCategory.EQUIPMENT) {
					int amount = e.getValue();
					result.merge(good.getID(), amount, (a,b) -> a.intValue() + b.intValue());
				}
			}
		}

		return result;
	}
}
