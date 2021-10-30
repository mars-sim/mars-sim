/*
 * Mars Simulation Project
 * RoverMission.java
 * @date 2021-10-29
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.mars_sim.msp.core.InventoryUtil;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.equipment.EquipmentFactory;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.events.HistoricalEvent;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.EventType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ShiftType;
import org.mars_sim.msp.core.person.ai.task.DriveGroundVehicle;
import org.mars_sim.msp.core.person.ai.task.EVAOperation;
import org.mars_sim.msp.core.person.ai.task.LoadVehicleEVA;
import org.mars_sim.msp.core.person.ai.task.LoadVehicleGarage;
import org.mars_sim.msp.core.person.ai.task.OperateVehicle;
import org.mars_sim.msp.core.person.ai.task.RequestMedicalTreatment;
import org.mars_sim.msp.core.person.ai.task.UnloadVehicleEVA;
import org.mars_sim.msp.core.person.ai.task.UnloadVehicleGarage;
import org.mars_sim.msp.core.person.ai.task.Walk;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Crewable;
import org.mars_sim.msp.core.vehicle.GroundVehicle;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.StatusType;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * A mission that involves driving a rover vehicle along a series of navpoints.
 * TODO externalize life support strings
 */
public abstract class RoverMission extends VehicleMission {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(RoverMission.class.getName());
	
	// Static members
	public static final int MIN_STAYING_MEMBERS = 1;
	public static final int MIN_GOING_MEMBERS = 2;
	
	/** Comparison to indicate a small but non-zero amount of fuel (methane) in kg that can still work on the fuel cell to propel the engine. */
    public static final double LEAST_AMOUNT = GroundVehicle.LEAST_AMOUNT;

	public static final double MIN_STARTING_SETTLEMENT_METHANE = 500D;

	protected static final int OXYGEN_ID = ResourceUtil.oxygenID;
	protected static final int WATER_ID = ResourceUtil.waterID;
	protected static final int FOOD_ID = ResourceUtil.foodID;

	public static final String PHASE_1 = "phase 1";
	public static final String MINING = "mining";
	public static final String TRADING = "trading";

	// Data members
	private Settlement startingSettlement;
	
	/**
	 * Constructor 1.
	 * 
	 * @param name           the name of the mission.
	 * @param startingMember the mission member starting the mission.
	 */
	protected RoverMission(String name, MissionType missionType, MissionMember startingMember) {
		// Use VehicleMission constructor.
		super(name, missionType, startingMember, MIN_GOING_MEMBERS);
	}

	/**
	 * Constructor with min people.
	 * 
	 * @param missionName    the name of the mission.
	 * @param startingMember the mission member starting the mission.
	 * @param minPeople      the minimum number of members required for mission.
	 */
	protected RoverMission(String missionName, MissionType missionType, MissionMember startingMember, int minPeople) {
		// Use VehicleMission constructor.
		super(missionName, missionType, startingMember, minPeople);
	}

	/**
	 * Constructor with min people and rover. Initiated by MissionDataBean.
	 * 
	 * @param missionName    the name of the mission.
	 * @param startingMember the mission member starting the mission.
	 * @param minPeople      the minimum number of people required for mission.
	 * @param rover          the rover to use on the mission.
	 */
	protected RoverMission(String missionName, MissionType missionType, MissionMember startingMember, int minPeople, Rover rover) {
		// Use VehicleMission constructor.
		super(missionName, missionType, startingMember, minPeople, rover);
	}

	/**
	 * Gets the mission's rover if there is one.
	 * 
	 * @return vehicle or null if none.
	 */
	public final Rover getRover() {
		return (Rover) getVehicle();
	}

	/**
	 * Sets the starting settlement.
	 * 
	 * @param startingSettlement the new starting settlement
	 */
	protected final void setStartingSettlement(Settlement startingSettlement) {
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

	/**
	 * Gets the available vehicle at the settlement with the greatest range.
	 * 
	 * @param settlement         the settlement to check.
	 * @param allowMaintReserved allow vehicles that are reserved for maintenance.
	 * @return vehicle or null if none available.
	 * @throws Exception if error finding vehicles.
	 */
	public static Vehicle getVehicleWithGreatestRange(MissionType missionType, Settlement settlement, boolean allowMaintReserved) {
		Vehicle result = null;

		Iterator<Vehicle> i = settlement.getParkedVehicles().iterator();
		while (i.hasNext()) {
			Vehicle vehicle = i.next();

			boolean usable = !vehicle.isReservedForMission();

            if (!allowMaintReserved && vehicle.isReserved())
				usable = false;

			usable = vehicle.isVehicleReady();
			
			if (vehicle.getStoredMass() > 0D)
				usable = false;
			
			if (!(vehicle instanceof Rover))
				usable = false;

			if (usable) {
				if (result == null)
					// so far, this is the first vehicle being picked
					result = vehicle;
				else if (vehicle.getRange(missionType) > result.getRange(missionType))
					// This vehicle has a better range than the previously selected vehicle
					result = vehicle;
			}
		}

		return result;
	}

	/**
	 * Checks to see if any vehicles are available at a settlement.
	 * 
	 * @param settlement         the settlement to check.
	 * @param allowMaintReserved allow vehicles that are reserved for maintenance.
	 * @return true if vehicles are available.
	 */
	public static boolean areVehiclesAvailable(Settlement settlement, boolean allowMaintReserved) {

		boolean result = false;

		Iterator<Vehicle> i = settlement.getParkedVehicles().iterator();
		while (i.hasNext()) {
			Vehicle vehicle = i.next();

			boolean usable = !vehicle.isReservedForMission();

            if (!allowMaintReserved && vehicle.isReserved())
				usable = false;
			
			usable = vehicle.isVehicleReady();
				
			if (!(vehicle instanceof Rover))
				usable = false;
			
			if (vehicle.getStoredMass() > 0D)
				usable = false;

			if (usable)
				result = true;
		}

		return result;
	}

	/**
	 * Checks if vehicle is usable for this mission. (This method should be
	 * overridden by children)
	 * 
	 * @param newVehicle the vehicle to check
	 * @return true if vehicle is usable.
	 * @throws MissionException if problem determining if vehicle is usable.
	 */
	protected boolean isUsableVehicle(Vehicle newVehicle) {
		boolean usable = super.isUsableVehicle(newVehicle);
		if (!(newVehicle instanceof Rover))
			usable = false;
		return usable;
	}

	/**
	 * Checks that everyone in the mission is aboard the rover.
	 * 
	 * @return true if everyone is aboard
	 */
	protected final boolean isEveryoneInRover() {
		Iterator<MissionMember> i = getMembers().iterator();
		while (i.hasNext()) {
			if (!getRover().isCrewmember((Person) i.next())) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks that no one in the mission is aboard the rover.
	 * 
	 * @return true if no one is aboard
	 */
	protected final boolean isNoOneInRover() {
        return getRover().getCrewNum() == 0
                && getRover().getRobotCrewNum() == 0;

//		Iterator<MissionMember> i = getMembers().iterator();
//		while (i.hasNext()) {
//			if (i.next().isInVehicle()) {
//				return false;
//			}
//		}
//		return true;
	}

	/**
	 * Checks if the rover is currently in a garage or not.
	 * 
	 * @return true if rover is in a garage.
	 */
	protected boolean isInAGarage() {
		return BuildingManager.isInAGarage(getVehicle());
	}

	/**
	 * Does the vehicle have at least the baseline numbers of EVA suits ?
	 * 
	 * @param vehicle
	 * @return
	 */
	protected boolean hasBaselineNumEVASuit(Vehicle vehicle) {
		boolean result = false;
		
		int numV = vehicle.findNumContainersOfType(EquipmentType.EVA_SUIT);
		
		int baseline = (int)(getMembers().size() * 1.5);

		int numP = 0;
		
		for (Person p: ((Crewable)vehicle).getCrew()) {
			if (p.getSuit() != null)
				numP++;
			else if (p.getInventorySuit() != null)
				numP++;
		}
		
		if (numV + numP > baseline)
			return true;
		
		return result;
	}
	
	/**
	 * Performs the embark from settlement phase of the mission.
	 * 
	 * @param member the mission member currently performing the mission
	 */
	@Override
	protected void performEmbarkFromSettlementPhase(MissionMember member) {
		Vehicle v = getVehicle();
		
		if (v == null) {
			addMissionStatus(MissionStatus.NO_AVAILABLE_VEHICLES);
			endMission();
			return;
		}
			
		Settlement settlement = v.getSettlement();
		if (settlement == null) {
			logger.warning(member, 
					Msg.getString("RoverMission.log.notAtSettlement", getPhase().getName())); //$NON-NLS-1$
			addMissionStatus(MissionStatus.NO_AVAILABLE_VEHICLES);
			endMission();
			return;
		}
		
		// While still in the settlement, check if the beacon is turned on and and endMission()
		else if (v.isBeaconOn()) {
			endMission();
			return;
		}

		// Add the rover to a garage if possible.
		boolean	isRoverInAGarage = settlement.getBuildingManager().addToGarage(v);
		
		// Load vehicle if not fully loaded.
		if (!isVehicleLoaded()) {
			// Check if vehicle can hold enough supplies for mission.
			if (isVehicleLoadable()) {
				
				if (member.isInSettlement()) {
					// Note: randomly select this member to load resources for the rover
					// This allows person to do other important things such as eating
					if (RandomUtil.lessThanRandPercent(75)) {
						if (member instanceof Person) {
							Person person = (Person) member;
							
							boolean hasAnotherMission = false; 
							Mission m = person.getMission();
							if (m != null && m != this)
								hasAnotherMission = true; 
							
							if (!hasAnotherMission && isRoverInAGarage) {

								assignTask(person,
											new LoadVehicleGarage(person, this));
							} else if (!hasAnotherMission) {
								// Note: Should check if it is day time to do EVA
								assignTask(person, new LoadVehicleEVA(person, this));
							}
						}
					}
				}
			}
			
			else {
				addMissionStatus(MissionStatus.CANNOT_LOAD_RESOURCES);
				endMission();
				return;
			}
		}
		
		else {

			// Gets a random location within rover.
			Point2D.Double vehicleLoc = LocalAreaUtil.getRandomInteriorLocation(v);
			Point2D.Double adjustedLoc = LocalAreaUtil.getLocalRelativeLocation(vehicleLoc.getX(),
					vehicleLoc.getY(), v);

			if (member instanceof Person) {
				Person person = (Person) member;
				
				// If person is not aboard the rover, board the rover and be ready to depart.
				if (!getRover().isCrewmember(person)) {

					if (Walk.canWalkAllSteps(person, adjustedLoc.getX(), adjustedLoc.getY(), 0, v)) {
					
						boolean canDo = assignTask(person, new Walk(person, adjustedLoc.getX(), adjustedLoc.getY(), 0, v));
						if (!canDo) {
							logger.warning(person, "Unable to walk to " + v + ".");
						}
						
						if (!isDone() && isRoverInAGarage) {
							
							if (settlement.findNumContainersOfType(EquipmentType.EVA_SUIT) > 1
									&& !hasBaselineNumEVASuit(v)) {
								
								EVASuit suit = InventoryUtil.getGoodEVASuitNResource(settlement, person);
								if (suit != null) {									
									boolean success = suit.transfer(v);									
									if (success)
										logger.info(person, "Transferred " + suit + " as spares from " 
												+ settlement + " to " + v + ".");
									else
										logger.warning(person, "Unable to transfer a spare EVA suit from " 
												+ settlement + " to " + v + ".");
								}
							}
						}
					}
				
					else { // this crew member cannot find the walking steps to enter the rover
						logger.severe(member, Msg.getString("RoverMission.log.unableToEnter", person.getName(), //$NON-NLS-1$
								v.getName()));
					}
				}
			}
			
			else if (member instanceof Robot) {
				Robot robot = (Robot) member;
				if (Walk.canWalkAllSteps(robot, adjustedLoc.getX(), adjustedLoc.getY(), 0, v)) {
					boolean canDo = assignTask(robot, new Walk(robot, adjustedLoc.getX(), adjustedLoc.getY(), 0, v));			
					if (!canDo) {
						logger.warning(robot, "Unable to walk to " + v + ".");
					}
				} else {
					logger.severe(member, Msg.getString("RoverMission.log.unableToEnter", robot.getName(), //$NON-NLS-1$
							v.getName()));
				}
			}
		}
		
		// If rover is loaded and everyone is aboard, embark from settlement.
		if (!isDone()) {
			
			// Set the members' work shift to on-call to get ready
			for (MissionMember m : getMembers()) {
				Person pp = (Person) m;
				if (pp.getShiftType() != ShiftType.ON_CALL)
					pp.setShiftType(ShiftType.ON_CALL);
			}

			if (isEveryoneInRover()) {
		
				// Remove from garage if in garage.
				Building garage = BuildingManager.getBuilding(v);
				if (garage != null) {
					garage.getVehicleMaintenance().removeVehicle(v);
				}

				// Record the start mass right before departing the settlement
				recordStartMass();
				
				// Embark from settlement
				if (v.transfer(unitManager.getMarsSurface())) {
					setPhaseEnded(true);						
				}
				else {
					addMissionStatus(MissionStatus.COULD_NOT_EXIT_SETTLEMENT);
					endMission();
				}
			}
		}
	}

	/**
	 * Performs the disembark to settlement phase of the mission.
	 * 
	 * @param member              the mission member currently performing the
	 *                            mission.
	 * @param disembarkSettlement the settlement to be disembarked to.
	 */
	@Override
	protected void performDisembarkToSettlementPhase(MissionMember member, Settlement disembarkSettlement) {
		
		Vehicle v0 = getVehicle();
		disembark(member, v0, disembarkSettlement);
		
		// If v0 is being towed by a vehicle, gets the towing vehicle
		Vehicle v1 = v0.getTowingVehicle();
		if (v1 != null)
			disembark(member, v1, disembarkSettlement);
		
		// If v0 is towing a vehicle, gets the towed vehicle
		Vehicle v2 = ((Rover)v0).getTowedVehicle();
		if (v2 != null)
			disembark(member, v2, disembarkSettlement);
	}
	
	/**
	 * Disembarks the vehicle and unload cargo
	 * 
	 * @param member
	 * @param v
	 * @param disembarkSettlement
	 */
	public void disembark(MissionMember member, Vehicle v, Settlement disembarkSettlement) {
		logger.info(v, 10_000, "Disembarked at " + disembarkSettlement.getName()
					+ " triggered by " + member.getName() +  ".");
		
		Rover rover = (Rover) v;

		if (v != null) {
			Settlement currentSettlement = v.getSettlement();
			if ((currentSettlement == null) || !currentSettlement.equals(disembarkSettlement)) {
				// If rover has not been parked at settlement, park it.
				boolean done = v.transfer(disembarkSettlement);
				
				if (done) {
					logger.info(v, "Done transferring to " + disembarkSettlement.getName() + ".");
				}
				else {
					logger.info(v, "Unable to transfer to " + disembarkSettlement.getName() + ".");
				}
//				disembarkSettlement.addParkedVehicle(v);	
			}
			
			// Test if this rover is towing another vehicle or is being towed
	        boolean tethered = v.isBeingTowed() || rover.isTowingAVehicle();
	        
			// Add vehicle to a garage if available.
			boolean isRoverInAGarage = false;
	        if (!tethered) {// && v.getGarage() == null) {
	        	isRoverInAGarage = disembarkSettlement.getBuildingManager().addToGarage(v);
	        }

			// Make sure the rover chasis is not overlapping a building structure in the settlement map
	        if (!isRoverInAGarage)
	        	rover.findNewParkingLoc();
	        
	        List<Person> currentCrew = new ArrayList<>(rover.getCrew());
			for (Person p : currentCrew) {
				if (p.isDeclaredDead()) {
					logger.fine(p, "Dead body will be retrieved from rover " + v.getName() + ".");
				}
				
				else {
					// the person is still inside the vehicle
					// See if this person can come home 
				}
				
				// Initiate an rescue operation
				// Note : Gets a lead person to perform it and give him a rescue badge
				if (!p.getPhysicalCondition().isFitByLevel(1500, 90, 1500))	
					rescueOperation(rover, p, disembarkSettlement);
			}
			
			// Reset the vehicle reservation
			v.correctVehicleReservation();

			// Check if any people still aboard the rover who aren't mission members
			// and direct them into the settlement.
			if (rover.getCrewNum() > 0) {
				
				Iterator<Person> i = rover.getCrew().iterator();
				while (i.hasNext()) {
					Person p = i.next();
					checkPersonStatus(rover, p, disembarkSettlement);
				}
			}

			// Check to see if no one is in the rover, unload the resources and end phase.
			if (isNoOneInRover()) {

				// Unload rover if necessary.
				boolean roverUnloaded = rover.getStoredMass() == 0D;
				if (!roverUnloaded) {
					if (member.isInSettlement() && ((Person)member).isFit()) {
						// Note : Random chance of having person unload (this allows person to do other things
						// sometimes)
						if (RandomUtil.lessThanRandPercent(50)) {
							unloadCargo((Person)member, rover);
						}				
					}		
				}
				
				else {
					// End the phase.

					// If the rover is in a garage, put the rover outside.
					if (isInAGarage()) {
						Building garage = BuildingManager.getBuilding(v);
						if (garage != null)
							garage.getVehicleMaintenance().removeVehicle(v);
					}
					
					// Leave the vehicle.
					leaveVehicle();
					setPhaseEnded(true);
				}
			}
		}
	}

	/**
	 * Give a person the task from unloading the vehicle
	 * 
	 * @param p
	 * @param rover
	 */
	private void unloadCargo(Person p, Rover rover) {
		
		Mission m = p.getMission();
		if (m != null && !m.equals(this))
			return; 

		if (RandomUtil.lessThanRandPercent(50)) {
			if (isInAGarage()) {
				assignTask(p, new UnloadVehicleGarage(p, rover));
			} 
			
			else {
				// Check if it is day time.
				if (!EVAOperation.isGettingDark(p)) {
					assignTask(p, new UnloadVehicleEVA(p, rover));
				}
			}			
		}	
	}
	
	/**
	 * Checks on a person's status to see if he can walk home or else be rescued
	 * 
	 * @param rover
	 * @param p
	 * @param disembarkSettlement
	 */
	private void checkPersonStatus(Rover rover, Person p, Settlement disembarkSettlement) {
		
		if (p.getSuit() == null && p.isInVehicle()) {
			// Checks to see if the rover has any EVA suit	
			EVASuit suit = ((Vehicle)rover).findEVASuit(p);
			
			if (suit == null) {

				logger.warning(p, "Could not find a working EVA suit in " + rover + " and needed to wait.");
			
				// If the person does not have an EVA suit	
				int availableSuitNum = Mission.getNumberAvailableEVASuitsAtSettlement(disembarkSettlement);
			
				if (availableSuitNum > 0) {
					// Deliver an EVA suit from the settlement to the rover
					// Note: Need to generate a task for a person to hand deliver an extra suit
					suit = InventoryUtil.getGoodEVASuitNResource(disembarkSettlement, p);
					if (suit != null) {				
						boolean success = suit.transfer(rover);
						if (success)
							logger.warning(p, "Just borrowed " + suit + " from " + disembarkSettlement + " to " + rover);
						else
							logger.warning(p, "Unable to borrow a spare EVA suit from " + disembarkSettlement + " to " + rover);
					}
				}
			}
		}
		
		if (p.isInVehicle() || p.isOutside()) {
			// Get random inhabitable building at emergency settlement.
			Building destinationBuilding = disembarkSettlement.getBuildingManager().getRandomAirlockBuilding();
			
			if (destinationBuilding != null) {
				Point2D destinationLoc = LocalAreaUtil.getRandomInteriorLocation(destinationBuilding);
				Point2D adjustedLoc = LocalAreaUtil.getLocalRelativeLocation(destinationLoc.getX(),
						destinationLoc.getY(), destinationBuilding);

				boolean hasStrength = p.getPhysicalCondition().isFitByLevel(1500, 90, 1500);
				
				if (Walk.canWalkAllSteps(p, adjustedLoc.getX(), adjustedLoc.getY(), 0, destinationBuilding)) {
			
					if (!hasStrength) {
						logger.info(p, 20_000, "No more strength left. Walking back to the settlement.");
						// walk back home
						assignTask(p, new Walk(p, adjustedLoc.getX(), adjustedLoc.getY(), 0, destinationBuilding));
					}
					
				} 
				
				else if (!hasStrength) {
					// Note 1: Help this person put on an EVA suit
					// Note 2: consider inflatable medical tent for emergency transport of incapacitated personnel
					logger.info(p, 
							 Msg.getString("RoverMission.log.emergencyEnterSettlement", p.getName(), 
									disembarkSettlement.getNickName())); //$NON-NLS-1$
					
					// Initiate an rescue operation
					// Note: Gets a lead person to perform it and give him a rescue badge
					rescueOperation(rover, p, disembarkSettlement);
					
					logger.info(p, "Transported to ("
							+ Math.round(p.getXLocation()*10.0)/10.0 + ", " 
							+ Math.round(p.getYLocation()*10.0)/10.0 + ") in "
							+ p.getBuildingLocation().getNickName()); //$NON-NLS-1$
					
					// Note: how to force the person to receive some form of medical treatment ?
					p.getMind().getTaskManager().clearAllTasks("Rover rescue");
					p.getMind().getTaskManager().addTask(new RequestMedicalTreatment(p));		
				}
			}
			
			else {
				logger.severe("No inhabitable buildings at " + disembarkSettlement);
				addMissionStatus(MissionStatus.NO_INHABITABLE_BUILDING);
				endMission();
			}
		}
	}
		
	/**
	 * Rescue the person from the rover
	 * 
	 * @param r the rover
	 * @param p the person
	 * @param s the settlement
	 */
	private void rescueOperation(Rover r, Person p, Settlement s) {
		
		if (p.isDeclaredDead()) {
//			Unit cu = p.getPhysicalCondition().getDeathDetails().getContainerUnit();
			if (p.transfer(s)) {
				logger.info(p, "Done emergency transfer of the body from " 
						+ r + " to " + s + ".");
			}
			else
				logger.info(p, "Unable to do emergency transfer of the body from " 
						+ r + " to " + s + ".");
		}
		// Retrieve the person from the rover
		else if (r != null && !p.isInSettlement()) {
			if (p.transfer(s)) {
				logger.info(p, "Done emergency transfer from " 
						+ r + " to " + s + ".");
			}
			else
				logger.info(p, "Unable to do emergency transfer from " 
						+ r + " to " + s + ".");
		}
		else if (p.isOutside()) {
			if (p.transfer(s)) {
				logger.info(p, "Done emergency transfer to " + s + ".");
			}
			else
				logger.info(p, "Unable to do emergency transfer to " + s + ".");
		}
				
		// Gets the settlement id
		int id = s.getIdentifier();
		// Store the person into a medical building
		BuildingManager.addToMedicalBuilding(p, id);
		
		// Register the historical event
		HistoricalEvent rescueEvent = new MissionHistoricalEvent(EventType.MISSION_RESCUE_PERSON, 
				this,
				p.getPhysicalCondition().getHealthSituation(), 
				p.getTaskDescription(), 
				p.getName(),
				(r != null ? r.getNickName() : "Outside"), 
				p.getLocationTag().getLocale(),
				p.getAssociatedSettlement().getName()
				);
		eventManager.registerNewEvent(rescueEvent);
	}
	
	
	/**
	 * Gets a new instance of an OperateVehicle task for the mission member.
	 * 
	 * @param member the mission member operating the vehicle.
	 * @return an OperateVehicle task for the person.
	 */
	@Override
	protected OperateVehicle createOperateVehicleTask(MissionMember member, TaskPhase lastOperateVehicleTaskPhase) {
		OperateVehicle result = null;
		if (member instanceof Person) {
			Person person = (Person) member;
			Vehicle v = (Vehicle)getRover();
			// Note: should it check for fatigue only ?
			if (!v.haveStatusType(StatusType.OUT_OF_FUEL)) {
				if (lastOperateVehicleTaskPhase != null) {
					result = new DriveGroundVehicle(person, getRover(), getNextNavpoint().getLocation(),
							getCurrentLegStartingTime(), getCurrentLegDistance(), lastOperateVehicleTaskPhase);
				} else {
					result = new DriveGroundVehicle(person, getRover(), getNextNavpoint().getLocation(),
							getCurrentLegStartingTime(), getCurrentLegDistance());
				}
			}
		}

		return result;
	}

	/**
	 * Checks to see if at least one inhabitant a settlement is remaining there.
	 * 
	 * @param settlement the settlement to check.
	 * @param member     the mission member checking
	 * @return true if at least one person left at settlement.
	 */
	protected static boolean atLeastOnePersonRemainingAtSettlement(Settlement settlement, MissionMember member) {
		boolean result = false;

		if (settlement != null) {
			Iterator<Person> i = settlement.getIndoorPeople().iterator();
			while (i.hasNext()) {
				Person inhabitant = i.next();
				if ((inhabitant != member) && !inhabitant.getMind().hasActiveMission()) {
					result = true;
				}
			}
		}

		return result;
	}

	/**
	 * Checks to see if at least a minimum number of people are available for a
	 * mission at a settlement.
	 * 
	 * @param settlement the settlement to check.
	 * @param minNum     minimum number of people required.
	 * @return true if minimum people available.
	 */
	public static boolean minAvailablePeopleAtSettlement(Settlement settlement, int minNum) {
		boolean result = false;
		int min = minNum;
		if (settlement != null) {

			String template = settlement.getTemplate();
			// Override the mininum num req if the settlement is too small
			if (template.toLowerCase().contains(PHASE_1) 
					|| template.toLowerCase().contains(MINING)
					|| template.toLowerCase().contains(TRADING)) 
				min = 0;

			int numAvailable = 0;
			Iterator<Person> i = settlement.getIndoorPeople().iterator();
			while (i.hasNext()) {
				Person inhabitant = i.next();
				if (!inhabitant.getMind().hasActiveMission())
					numAvailable++;
			}
			if (numAvailable >= min)
				result = true;
		}

		return result;
	}

	/**
	 * Checks if there is only one person at the associated settlement and he/she
	 * has a serious medical problem.
	 * 
	 * @return true if serious medical problem
	 */
	protected final boolean hasDangerousMedicalProblemAtAssociatedSettlement() {
		boolean result = false;
		if (getAssociatedSettlement() != null) {
			if (getAssociatedSettlement().getIndoorPeopleCount() == 1) {
				Person person = (Person) getAssociatedSettlement().getIndoorPeople().toArray()[0];
				if (person.getPhysicalCondition().hasSeriousMedicalProblems())
					result = true;
			}
		}
		return result;
	}

	/**
	 * Checks if the mission has an emergency situation.
	 * 
	 * @return true if emergency.
	 */
	protected final boolean hasEmergency() {
		boolean result = super.hasEmergency();
		if (hasDangerousMedicalProblemAtAssociatedSettlement())
			result = true;
		return result;
	}

	
	/**
	 * Gets the number and amounts of resources needed for the mission.
	 * 
	 * @param useMargin Apply safety margin when loading resources before embarking if true.
	 *        Note : True if estimating trip. False if calculating remaining trip.
	 * @return map of amount and item resources and their Double amount or Integer
	 *         number.
	 */
	@Override
	public Map<Integer, Number> getResourcesNeededForTrip(boolean useBuffer, double distance) {
		// Note: currently, it has methane resource only
		Map<Integer, Number> result = super.getResourcesNeededForTrip(useBuffer, distance);

		// Determine estimate time for trip.
		double time = getEstimatedTripTime(useBuffer, distance);
		double timeSols = time / 1000D;
		
		addLifeSupportResources(result, getPeopleNumber(), timeSols, useBuffer);
		
		return result;
	}
	
	/**
	 * add life support resources to a map to support a number of peopel for a number of days.
	 * @param result
	 * @param crewNum
	 * @param timeSols
	 * @param useBuffer
	 */
	protected static void addLifeSupportResources(Map<Integer, Number> result,
												  int crewNum, double timeSols,
												  boolean useBuffer) {

		// Determine life support supplies needed for trip.
		double oxygenAmount = PhysicalCondition.getOxygenConsumptionRate() * timeSols * crewNum ;
		if (useBuffer)
			oxygenAmount *= Vehicle.getLifeSupportRangeErrorMargin() * Mission.OXYGEN_MARGIN;
		result.merge(OXYGEN_ID, oxygenAmount, (a,b) -> (a.doubleValue() + b.doubleValue()));

		double waterAmount = PhysicalCondition.getWaterConsumptionRate() * timeSols * crewNum ;
		if (useBuffer)
			waterAmount *= Vehicle.getLifeSupportRangeErrorMargin() * Mission.WATER_MARGIN;
		result.merge(WATER_ID, waterAmount, (a,b) -> (a.doubleValue() + b.doubleValue()));

		double foodAmount = PhysicalCondition.getFoodConsumptionRate() * timeSols * crewNum ; 
		if (useBuffer)
			foodAmount *= Vehicle.getLifeSupportRangeErrorMargin() * Mission.FOOD_MARGIN;
		result.merge(FOOD_ID, foodAmount, (a,b) -> (a.doubleValue() + b.doubleValue()));
	}

	/**
	 * Gets EVA suit parts for the trip
	 * 
	 * @param numberMalfunctions
	 * @return
	 */
	protected Map<Integer, Number> getEVASparePartsForTrip(double numberMalfunctions) {
		Map<Integer, Number> map = new HashMap<>();
		
		// Get an EVA suit from the staring settlement as an example of an EVA Suit
		EVASuit suit = (EVASuit) EquipmentFactory.createEquipment(EquipmentType.EVA_SUIT, getStartingPerson().getAssociatedSettlement(), true);

		if (suit != null) {
			// Determine needed repair parts for EVA suits.
			Map<Integer, Double> parts = suit.getMalfunctionManager().getRepairPartProbabilities();
			Iterator<Integer> i = parts.keySet().iterator();
			while (i.hasNext()) {
				Integer id = i.next();
				String name = ItemResourceUtil.findItemResourceName(id);
				for (String n : ItemResourceUtil.EVASUIT_PARTS) {
					if (n.equalsIgnoreCase(name)) {
						int number = (int) Math.round(parts.get(id) * numberMalfunctions);
						if (number > 0) {
							if (map.containsKey(id))
								number += map.get(id).intValue();
							map.put(id, number);
						}
					}
				}
			}
		}

		return map;
	}
	

	/**
	 * Checks if there is an available backup rover at the settlement for the
	 * mission.
	 * 
	 * @param settlement the settlement to check.
	 * @return true if available backup rover.
	 */
	public static boolean hasBackupRover(Settlement settlement) {
		int availableVehicleNum = 0;
		Iterator<Vehicle> i = settlement.getParkedVehicles().iterator();
		while (i.hasNext()) {
			Vehicle vehicle = i.next();
			if ((vehicle instanceof Rover) && !vehicle.isReservedForMission())
				availableVehicleNum++;
		}
		return (availableVehicleNum >= 2);
	}

	/**
	 * Gets the time limit of the trip based on life support capacity.
	 * 
	 * @param useBuffer use time buffer in estimation if true.
	 * @return time (millisols) limit.
	 * @throws MissionException if error determining time limit.
	 */
	public static double getTotalTripTimeLimit(Rover rover, int memberNum, boolean useBuffer) {
	
		double timeLimit = Double.MAX_VALUE;
	
		// Check food capacity as time limit.
		double foodConsumptionRate = personConfig.getFoodConsumptionRate();
		double foodCapacity = rover.getAmountResourceCapacity(FOOD_ID);
		double foodTimeLimit = foodCapacity / (foodConsumptionRate * memberNum);
		if (foodTimeLimit < timeLimit) {
			timeLimit = foodTimeLimit;
		}
	
		// Check water capacity as time limit.
		double waterConsumptionRate = personConfig.getWaterConsumptionRate();
		double waterCapacity = rover.getAmountResourceCapacity(WATER_ID);
		double waterTimeLimit = waterCapacity / (waterConsumptionRate * memberNum);
		if (waterTimeLimit < timeLimit) {
			timeLimit = waterTimeLimit;
		}
	
		// Check oxygen capacity as time limit.
		double oxygenConsumptionRate = personConfig.getNominalO2ConsumptionRate();
		double oxygenCapacity = rover.getAmountResourceCapacity(OXYGEN_ID);
		double oxygenTimeLimit = oxygenCapacity / (oxygenConsumptionRate * memberNum);
		if (oxygenTimeLimit < timeLimit) {
			timeLimit = oxygenTimeLimit;
		}
	
		// Convert timeLimit into millisols and use error margin.
		timeLimit = (timeLimit * 1000D);
		if (useBuffer) {
			timeLimit /= Vehicle.getLifeSupportRangeErrorMargin();
		}
	
		return timeLimit;
	}

	/**
	 * Find members for a mission, for RoverMissions all members must be at the same
	 * settlement.
	 * @param startingMember
	 * @return
	 */
	protected boolean recruitMembersForMission(MissionMember startingMember) {
		return recruitMembersForMission(startingMember, true);
	}
	
	@Override
	protected boolean recruitMembersForMission(MissionMember startingMember, boolean sameSettlement) {
		super.recruitMembersForMission(startingMember, sameSettlement);

		// Make sure there is at least one person left at the starting
		// settlement.
		if (!atLeastOnePersonRemainingAtSettlement(getStartingSettlement(), startingMember)) {
			// Remove last person added to the mission.
			Person lastPerson = null;
			Iterator<MissionMember> i = getMembers().iterator();
			while (i.hasNext()) {
				MissionMember member = i.next();
				if (member instanceof Person) {
					lastPerson = (Person) member;
				}
			}

			if (lastPerson != null) {
				lastPerson.getMind().setMission(null);
				if (getMembersNumber() < getMinMembers()) {
					addMissionStatus(MissionStatus.NOT_ENOUGH_MEMBERS);
					endMission();
					return false;
				} else if (getPeopleNumber() == 0) {
					addMissionStatus(MissionStatus.NO_MEMBERS_AVAILABLE);
					endMission();
					return false;
				}
			}
		}
		
		return true;
	}
	
	@Override
	public void destroy() {
		super.destroy();

		startingSettlement = null;
	}
}
