/**
 * Mars Simulation Project
 * RoverMission.java
 * @version 3.1.0 2017-01-19
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.events.HistoricalEvent;
import org.mars_sim.msp.core.person.EventType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ShiftType;
import org.mars_sim.msp.core.person.ai.task.DriveGroundVehicle;
import org.mars_sim.msp.core.person.ai.task.EVAOperation;
import org.mars_sim.msp.core.person.ai.task.ExitAirlock;
import org.mars_sim.msp.core.person.ai.task.LoadVehicleEVA;
import org.mars_sim.msp.core.person.ai.task.LoadVehicleGarage;
import org.mars_sim.msp.core.person.ai.task.OperateVehicle;
import org.mars_sim.msp.core.person.ai.task.RequestMedicalTreatment;
import org.mars_sim.msp.core.person.ai.task.UnloadVehicleEVA;
import org.mars_sim.msp.core.person.ai.task.UnloadVehicleGarage;
import org.mars_sim.msp.core.person.ai.task.Walk;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.Storage;
import org.mars_sim.msp.core.structure.building.function.cooking.PreparingDessert;
import org.mars_sim.msp.core.tool.RandomUtil;
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
	private static Logger logger = Logger.getLogger(RoverMission.class.getName());
	private static String loggerName = logger.getName();
	private static String sourceName = loggerName.substring(loggerName.lastIndexOf(".") + 1, loggerName.length());
	
	// Static members
	public static final int MIN_STAYING_MEMBERS = 1;
	public static final int MIN_GOING_MEMBERS = 2;
	
	/** Comparison to indicate a small but non-zero amount of fuel (methane) in kg that can still work on the fuel cell to propel the engine. */
    public static final double LEAST_AMOUNT = GroundVehicle.LEAST_AMOUNT;

	public static final double MIN_STARTING_SETTLEMENT_METHANE = 500D;

	private static int oxygenID = ResourceUtil.oxygenID;
	private static int waterID = ResourceUtil.waterID;
	private static int foodID = ResourceUtil.foodID;
	private static int methaneID = ResourceUtil.methaneID;

	public static final String PHASE_1 = "phase 1";
	public static final String MINING = "mining";
	public static final String TRADING = "trading";

	public static AmountResource[] availableDesserts = PreparingDessert.getArrayOfDessertsAR();

	// Data members
	private Settlement startingSettlement;
	
	private Map<Integer, Double> dessertResources;
	
	/**
	 * Constructor.
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
//		logger.info(startingMember + " had started RoverMission");
	}

	/**
	 * Constructor with min people and rover.
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

			boolean usable = true;
			if (vehicle.isReservedForMission())
				usable = false;
			
			if (!allowMaintReserved && vehicle.isReserved())
				usable = false;

			usable = vehicle.isVehicleReady();
			
			if (vehicle.getInventory().getTotalInventoryMass(false) > 0D)
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

			boolean usable = true;
			if (vehicle.isReservedForMission())
				usable = false;
			
			if (!allowMaintReserved && vehicle.isReserved())
				usable = false;
			
			usable = vehicle.isVehicleReady();
				
			if (!(vehicle instanceof Rover))
				usable = false;
			
			if (vehicle.getInventory().getTotalInventoryMass(false) > 0D)
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
		if (getRover().getCrewNum() == 0
			&& getRover().getRobotCrewNum() == 0)
			return true;
		
		return false;
		
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
	protected boolean isRoverInAGarage() {
		return (BuildingManager.getBuilding(getVehicle()) != null);
	}

	/**
	 * Performs the embark from settlement phase of the mission.
	 * 
	 * @param member the mission member currently performing the mission
	 */
	protected void performEmbarkFromSettlementPhase(MissionMember member) {
		Vehicle v = getVehicle();
		
		if (v == null) {
			addMissionStatus(MissionStatus.NO_AVAILABLE_VEHICLES);
			endMission();
			return;
		}

		Settlement settlement = v.getSettlement();
		if (settlement == null) {
			//throw new IllegalStateException(
			LogConsolidated.log(Level.WARNING, 0, sourceName, 
					Msg.getString("RoverMission.log.notAtSettlement", getPhase().getName())); //$NON-NLS-1$
			addMissionStatus(MissionStatus.NO_AVAILABLE_VEHICLES);
			endMission();
			return;
		}

		// If the vehicle is currently not in a garage
		if (v.getGarage() == null) {
			// Add the rover to a garage if possible.
			BuildingManager.addToGarage((Rover) v, v.getSettlement());
		}

		// Load vehicle if not fully loaded.
		if (!loadedFlag) {
			if (isVehicleLoaded()) {
				loadedFlag = true;
			} else {
				// Check if vehicle can hold enough supplies for mission.
				if (isVehicleLoadable()) {
					if (member.isInSettlement()) {
						// Load rover
						// Random chance of having person load (this allows person to do other things
						// sometimes)
						if (RandomUtil.lessThanRandPercent(75)) {
							if (member instanceof Person) {
								Person person = (Person) member;
								if (isRoverInAGarage()) {
									// TODO Refactor.
									assignTask(person,
												new LoadVehicleGarage(person, v,
														getRequiredResourcesToLoad(), getOptionalResourcesToLoad(),
														getRequiredEquipmentToLoad(), getOptionalEquipmentToLoad()));
								} else {
									// Check if it is day time.
//										if (!EVAOperation.isGettingDark(person)) {
										assignTask(person, new LoadVehicleEVA(person, v,
													getRequiredResourcesToLoad(), getOptionalResourcesToLoad(),
													getRequiredEquipmentToLoad(), getOptionalEquipmentToLoad()));
//										}
								}
							}
						}
					}
					else {
						if (member instanceof Person) {
							Person person = (Person) member;
							// Check if it is day time.
//								if (!EVAOperation.isGettingDark(person)) {
								assignTask(person, new LoadVehicleEVA(person, v,
											getRequiredResourcesToLoad(), getOptionalResourcesToLoad(),
											getRequiredEquipmentToLoad(), getOptionalEquipmentToLoad()));
//								}
						}
					}
					
				} else {
					addMissionStatus(MissionStatus.VEHICLE_NOT_LOADABLE);
					endMission();
					return;
				}
			}
		}
		
		else {

			// Gets a random location within rover.
			Point2D.Double vehicleLoc = LocalAreaUtil.getRandomInteriorLocation(v);
			Point2D.Double adjustedLoc = LocalAreaUtil.getLocalRelativeLocation(vehicleLoc.getX(),
					vehicleLoc.getY(), v);

			if (member instanceof Person) {
				Person person = (Person) member;
				// If person is not aboard the rover, board rover.
				if (!getRover().isCrewmember(person)) {

					if (Walk.canWalkAllSteps(person, adjustedLoc.getX(), adjustedLoc.getY(), 0, v)) {
					
						assignTask(person, new Walk(person, adjustedLoc.getX(), adjustedLoc.getY(), 0, v));
						
						if (!isDone() && isRoverInAGarage()) {
							// Store one or two EVA suit for person (if possible).
							int limit = RandomUtil.getRandomInt(1, 2);
							for (int i=0; i<limit; i++) {
								if (settlement.getInventory().findNumEVASuits(false, false) > 1) {
									EVASuit suit = settlement.getInventory().findAnEVAsuit();
									if (suit != null && v.getInventory().canStoreUnit(suit, false)) {
										// TODL: should add codes to have a person carries the extra EVA suit physically
										suit.transfer(settlement, v);
									}
								}
							}
						}
					}
				
					else { // this crewmember cannot find the walking steps to enter the rover
						LogConsolidated.log(Level.SEVERE, 10_000, sourceName,
								"[" + person.getLocationTag().getLocale() + "] " 
									+  Msg.getString("RoverMission.log.unableToEnter", person.getName(), //$NON-NLS-1$
								v.getName()));
//								addMissionStatus(MissionStatus.CANNOT_ENTER_ROVER);
//								endMission();
					}
				}
			}
			
			else if (member instanceof Robot) {
				Robot robot = (Robot) member;
				if (Walk.canWalkAllSteps(robot, adjustedLoc.getX(), adjustedLoc.getY(), 0, v)) {
					assignTask(robot, new Walk(robot, adjustedLoc.getX(), adjustedLoc.getY(), 0, v));
				} else {
					LogConsolidated.log(Level.SEVERE, 0, sourceName,
							"[" + robot.getLocationTag().getLocale() + "] " 
								+  Msg.getString("RoverMission.log.unableToEnter", robot.getName(), //$NON-NLS-1$
							v.getName()));
//							logger.warning(Msg.getString("RoverMission.log.unableToEnter", robot.getName(), //$NON-NLS-1$
//									v.getName()));
//							addMissionStatus(MissionStatus.CANNOT_ENTER_ROVER);
//							endMission();
				}
			}

			// If rover is loaded and everyone is aboard, embark from settlement.
			if (!isDone() && loadedFlag) {
				
				// Set the members' work shift to on-call to get ready
				for (MissionMember m : getMembers()) {
					Person pp = (Person) m;
					if (pp.getShiftType() != ShiftType.ON_CALL)
						pp.setShiftType(ShiftType.ON_CALL);
				}

				if (isEveryoneInRover()) {
			
					// Remove from garage if in garage.
					Building garageBuilding = BuildingManager.getBuilding(v);
					if (garageBuilding != null) {
						garageBuilding.getVehicleMaintenance().removeVehicle(v);
					}
	
					// Record the start mass right before departing the settlement
					recordStartMass();
					
					// Embark from settlement
					settlement.getInventory().retrieveUnit(v);
					setPhaseEnded(true);
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
	protected void performDisembarkToSettlementPhase(MissionMember member, Settlement disembarkSettlement) {
		disembark(member, getVehicle(), disembarkSettlement);
	}
	
	
	public void disembark(MissionMember member, Vehicle v, Settlement disembarkSettlement) {
		Rover rover = (Rover) v;

		if (v != null) {// && v.getSettlement() == null) {

			if (v.getSettlement() == null) {
				// If rover has not been parked at settlement, park it.
				disembarkSettlement.getInventory().storeUnit(v);	
			}
			
			// Test if this rover is towing another vehicle or is being towed
	        boolean tethered = v.isBeingTowed() || rover.isTowingAVehicle();
	        
			// Add vehicle to a garage if available.
			boolean garaged = false;
	        if (!tethered && v.getGarage() == null) {
	        	garaged = BuildingManager.addToGarage((GroundVehicle) v, disembarkSettlement);
	        }

			// Make sure the rover chasis is not overlapping a building structure in the settlement map
	        if (!garaged)
	        	rover.determinedSettlementParkedLocationAndFacing();
	        
			for (Person p : rover.getCrew()) {
				if (p.isDeclaredDead()) {
					
					LogConsolidated.log(Level.FINER, 0, sourceName,
							"[" + p.getLocationTag().getLocale() + "] " + p.getName() 
							+ "'s body had been retrieved from rover " + v.getName() + ".");
				}
				
				else {
					// the person is still inside the vehicle
					
//					LogConsolidated.log(Level.INFO, 10_000, sourceName,
//							"[" + p.getLocationTag().getLocale() + "] " + p.getName() 
//							+ " came home safety on the rover "+ rover.getName() + ".");

				}
				
				// Initiate an rescue operation
				// TODO: Gets a lead person to perform it and give him a rescue badge
				rescueOperation(rover, p, disembarkSettlement);
			}
		}
		
		// Reset the vehicle reservation
		v.correctVehicleReservation();

		if (rover != null) {

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
				boolean roverUnloaded = rover.getInventory().getTotalInventoryMass(false) == 0D;
				if (!roverUnloaded) {
					if (member.isInSettlement()) {
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
					if (isRoverInAGarage()) {
						BuildingManager.getBuilding(getVehicle()).getVehicleMaintenance().removeVehicle(getVehicle());
					}

					// Leave the vehicle.
					leaveVehicle();
					setPhaseEnded(true);
				}
			}
		} else {
			// TODO: Everyone needs to be unboarded
			
//			setPhaseEnded(true);
		}
	}

	/**
	 * Give a person the task from unloading the vehicle
	 * 
	 * @param p
	 * @param rover
	 */
	private void unloadCargo(Person p, Rover rover) {
		if (RandomUtil.lessThanRandPercent(50)) {
			if (isRoverInAGarage()) {
				assignTask(p, new UnloadVehicleGarage(p, rover));
			} 
			
			else {
				// Check if it is day time.
				if (!EVAOperation.isGettingDark(p)) {
					assignTask(p, new UnloadVehicleEVA(p, rover));
				}
			}
			
//			return;	
		}	
	}
	
	/**
	 * Checks on a person's status to see if he can walk home or be rescued
	 * 
	 * @param rover
	 * @param p
	 * @param disembarkSettlement
	 */
	private void checkPersonStatus(Rover rover, Person p, Settlement disembarkSettlement) {
		if (p.isInVehicle() || p.isOutside()) {
			// Get random inhabitable building at emergency settlement.
			Building destinationBuilding = disembarkSettlement.getBuildingManager().getRandomAirlockBuilding();
			if (destinationBuilding != null) {
				Point2D destinationLoc = LocalAreaUtil.getRandomInteriorLocation(destinationBuilding);
				Point2D adjustedLoc = LocalAreaUtil.getLocalRelativeLocation(destinationLoc.getX(),
						destinationLoc.getY(), destinationBuilding);

				double fatigue = p.getFatigue(); // 0 to infinity
				double perf = p.getPerformanceRating(); // 0 to 1
				double stress = p.getStress(); // 0 to 100
				double energy = p.getEnergy(); // 100 to infinity
				double hunger = p.getHunger(); // 0 to infinity

				boolean hasStrength = fatigue < 1000 && perf > .4 && stress < 60 && energy > 750 && hunger < 1000;
				
				if (p.isInVehicle()) {// && p.getInventory().findNumUnitsOfClass(EVASuit.class) == 0) {
					// Checks to see if the person has an EVA suit	
					if (!ExitAirlock.goodEVASuitAvailable(rover.getInventory(), p)) {

						LogConsolidated.log(Level.WARNING, 0, sourceName, "[" + p.getLocationTag().getLocale() + "] "
										+ p + " could not find a working EVA suit and needed to wait.");
					
						// If the person does not have an EVA suit	
						int availableSuitNum = Mission.getNumberAvailableEVASuitsAtSettlement(disembarkSettlement);
//						int suitVehicle = rover.getInventory().findNumUnitsOfClass(EVASuit.class);
						
						if (availableSuitNum > 0) {
							// Deliver an EVA suit from the settlement to the rover
							// TODO: Need to generate a task for a person to hand deliver an extra suit
							EVASuit suit = disembarkSettlement.getInventory().findAnEVAsuit(); //(EVASuit) disembarkSettlement.getInventory().findUnitOfClass(EVASuit.class);
							if (suit != null && rover.getInventory().canStoreUnit(suit, false)) {
								
								suit.transfer(disembarkSettlement, rover);
//								disembarkSettlement.getInventory().retrieveUnit(suit);
//								rover.getInventory().storeUnit(suit);
								
								LogConsolidated.log(Level.WARNING, 0, sourceName, "[" + p.getLocationTag().getLocale() + "] "
										+ p + " received a spare EVA suit from the settlement.");
							}
						}
					}
				}
				
				if (Walk.canWalkAllSteps(p, adjustedLoc.getX(), adjustedLoc.getY(), 0, destinationBuilding)) {
			
					if (hasStrength) {
						LogConsolidated.log(Level.INFO, 20_000, sourceName, 
								"[" + disembarkSettlement.getName() + "] "
								+ p.getName() + " still had strength left and would help unload cargo.");
						// help unload the cargo
						unloadCargo(p, rover);
					}	
					else {
						LogConsolidated.log(Level.INFO, 20_000, sourceName, 
								"[" + disembarkSettlement.getName() + "] "
								+ p.getName() + " had no more strength and walked back to the settlement.");
						// walk back home
						assignTask(p, new Walk(p, adjustedLoc.getX(), adjustedLoc.getY(), 0, destinationBuilding));
					}
					
				} 
				
				else if (!hasStrength) {

					// Help this person put on an EVA suit
					// TODO: consider inflatable medical tent for emergency transport of incapacitated personnel
					
					// This person needs to be rescued.
					LogConsolidated.log(Level.INFO, 0, sourceName, 
							"[" + disembarkSettlement.getName() + "] "
							+ Msg.getString("RoverMission.log.emergencyEnterSettlement", p.getName(), 
									disembarkSettlement.getNickName())); //$NON-NLS-1$
					
					// Initiate an rescue operation
					// TODO: Gets a lead person to perform it and give him a rescue badge
					rescueOperation(rover, p, disembarkSettlement);
					
					LogConsolidated.log(Level.INFO, 0, sourceName, 
							"[" + disembarkSettlement.getName() + "] "
							+ p.getName() 
							+ " was transported to ("
							+ Math.round(p.getXLocation()*10.0)/10.0 + ", " 
							+ Math.round(p.getYLocation()*10.0)/10.0 + ") in "
							+ p.getBuildingLocation().getNickName()); //$NON-NLS-1$
					
					// TODO: how to force the person to receive some form of medical treatment ?
					p.getMind().getTaskManager().clearAllTasks();
					p.getMind().getTaskManager().addTask(new RequestMedicalTreatment(p), false);		
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
			Unit cu = p.getPhysicalCondition().getDeathDetails().getContainerUnit();
//			cu.getInventory().retrieveUnit(p);
			p.transfer(cu, s);
		}
		// Retrieve the person from the rover
		else if (r != null) {
//			r.getInventory().retrieveUnit(p);
			p.transfer(r, s);
		}
		else if (p.isOutside()) {
//			unitManager.getMarsSurface().getInventory().retrieveUnit(p);
			p.transfer(unitManager.getMarsSurface(), s);
		}
		
		// Store the person into the settlement
//		s.getInventory().storeUnit(p);
		
		// Gets the settlement id
		int id = s.getIdentifier();
		// Store the person into a medical building
		BuildingManager.addToMedicalBuilding(p, id);
		// Register the person
//		p.setAssociatedSettlement(id);
		// Register the historical event
		HistoricalEvent rescueEvent = new MissionHistoricalEvent(EventType.MISSION_RESCUE_PERSON, 
				this,
				p.getPhysicalCondition().getHealthSituation(), 
				p.getTaskDescription(), 
				p.getName(),
				r.getNickName(), 
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
	protected OperateVehicle createOperateVehicleTask(MissionMember member, TaskPhase lastOperateVehicleTaskPhase) {
		OperateVehicle result = null;
		if (member instanceof Person) {
			Person person = (Person) member;
			Vehicle v = (Vehicle)getRover();
			// TODO: should it check for fatigue only ?
//			if (person.getFatigue() < 750) {
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
		int crewNum = getPeopleNumber();

		// Determine life support supplies needed for trip.
		double oxygenAmount = PhysicalCondition.getOxygenConsumptionRate() * timeSols * crewNum ;
		if (useBuffer)
			oxygenAmount *= Vehicle.getLifeSupportRangeErrorMargin() * Mission.OXYGEN_MARGIN;
		result.put(oxygenID, oxygenAmount);

		double waterAmount = PhysicalCondition.getWaterConsumptionRate() * timeSols * crewNum ;
		if (useBuffer)
			waterAmount *= Vehicle.getLifeSupportRangeErrorMargin() * Mission.WATER_MARGIN;
		result.put(waterID, waterAmount);

		double foodAmount = PhysicalCondition.getFoodConsumptionRate() * timeSols * crewNum ; 
		if (useBuffer)
			foodAmount *= Vehicle.getLifeSupportRangeErrorMargin() * Mission.FOOD_MARGIN;
		result.put(foodID, foodAmount);

		return result;
	}

	@Override
	public Map<Integer, Number> getOptionalResourcesToLoad() {

		Map<Integer, Number> result = super.getOptionalResourcesToLoad();

		int dessertID = -1;
		// Initialize dessert resources if necessary.
		if (dessertResources == null) {
			dessertID = determineDessertResources();
		}

		if (dessertID != -1) {
			// Add any dessert resources to optional resources to load.
			Iterator<Integer> i = dessertResources.keySet().iterator();
			while (i.hasNext()) {
				Integer dessert = i.next();
				double amount = dessertResources.get(dessert);

				if (result.containsKey(dessert)) {
					double initialAmount = (double) result.get(dessert);
					amount += initialAmount;
				}

				result.put(dessert, amount);
			}
		}
		return result;
	}

	/**
	 * Determine an unprepared dessert resource to load on the mission.
	 */
	private Integer determineDessertResources() {

		dessertResources = new HashMap<>(1);

		// Determine estimate time for trip.
		double distance = getTotalRemainingDistance();
		double time = getEstimatedTripTime(true, distance);
		double timeSols = time / 1000D;

		int crewNum = getPeopleNumber();

		// Determine dessert amount for trip.
		double dessertAmount = PhysicalCondition.getDessertConsumptionRate() * crewNum * timeSols
				* Mission.DESSERT_MARGIN;

		// Put together a list of available unprepared dessert resources.
		List<AmountResource> dessertList = new ArrayList<>();
		// availableDesserts = AmountResource.getArrayOfDessertsAR();
		for (AmountResource ar : availableDesserts) {

			// See if an unprepared dessert resource is available
			boolean isAvailable = Storage.retrieveAnResource(dessertAmount, ar, startingSettlement.getInventory(),
					false);
			if (isAvailable) {
				dessertList.add(ar);
			}
		}

		// Randomly choose an unprepared dessert resource from the available resources.
		AmountResource dessertAR = null;
		if (dessertList.size() > 0) {
			// only needs one of the dessert with that amount.
			dessertAR = dessertList.get(RandomUtil.getRandomInt(dessertList.size() - 1));
			// dessert = AmountResource.findAmountResource(dessertName);
		}

		int id = -1;

		if (dessertAR != null) {
			id = ResourceUtil.findIDbyAmountResourceName(dessertAR.getName());
			dessertResources.put(id, dessertAmount);
		}

		return id;
	}

	@Override
	public void endMission() {
//		logger.info(this.getStartingMember() + " ended the " + this);
		super.endMission();
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

	

	@Override
	protected void recruitMembersForMission(MissionMember startingMember) {
		super.recruitMembersForMission(startingMember);

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
				} else if (getPeopleNumber() == 0) {
					addMissionStatus(MissionStatus.NO_MEMBERS_ON_MISSION);
					endMission();
				}
			}
		}
	}

//	/**
//	 * Reloads instances after loading from a saved sim
//	 * 
//	 * @param {{@link HistoricalEventManager}
//	 */
//	public static void justReloaded(HistoricalEventManager event) {
//		eventManager = event;
//	}
	
	@Override
	public void destroy() {
		super.destroy();

		startingSettlement = null;
	}
}