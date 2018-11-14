/**
 * Mars Simulation Project
 * Mining.java
 * @version 3.1.0 2017-08-08
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person.ai.mission;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Inventory;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.equipment.Bag;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.mars.ExploredLocation;
import org.mars_sim.msp.core.mars.Mars;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PersonConfig;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.task.CollectMinedMinerals;
import org.mars_sim.msp.core.person.ai.task.MineSite;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.goods.Good;
import org.mars_sim.msp.core.structure.goods.GoodsUtil;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Crewable;
import org.mars_sim.msp.core.vehicle.LightUtilityVehicle;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.StatusType;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * Mission for mining mineral concentrations at an explored site.
 */
public class Mining extends RoverMission {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(Mining.class.getName());

	/** Default description. */
	public static final String DEFAULT_DESCRIPTION = Msg.getString("Mission.description.mining"); //$NON-NLS-1$

	/** Mission phases */
	final public static MissionPhase MINING_SITE = new MissionPhase(Msg.getString("Mission.phase.miningSite")); //$NON-NLS-1$

	/** Number of bags needed for mission. */
	public static final int NUMBER_OF_BAGS = 20;

	/** Base amount (kg) of a type of mineral at a site. */
	private static final double MINERAL_BASE_AMOUNT = 1000D;

	/** Amount of time(millisols) to spend at the mining site. */
	private static final double MINING_SITE_TIME = 3000D;

	/** Minimum amount (kg) of an excavated mineral that can be collected. */
	private static final double MINIMUM_COLLECT_AMOUNT = 10D;

	/**
	 * The minimum number of mineral concentration estimation improvements for an
	 * exploration site for it to be considered mature enough to mine.
	 */
	private static final int MATURE_ESTIMATE_NUM = 10;

	// Data members
	private ExploredLocation miningSite;
	private MarsClock miningSiteStartTime;
	private boolean endMiningSite;
	private Map<AmountResource, Double> excavatedMinerals;
	private Map<AmountResource, Double> totalExcavatedMinerals;
	private LightUtilityVehicle luv;

	private static int oxygenID = ResourceUtil.oxygenID;
	private static int waterID = ResourceUtil.waterID;
	private static int foodID = ResourceUtil.foodID;

	/**
	 * Constructor
	 * 
	 * @param startingPerson the person starting the mission.
	 * @throws MissionException if error creating mission.
	 */
	public Mining(Person startingPerson) {

		// Use RoverMission constructor.
		super(DEFAULT_DESCRIPTION, startingPerson, RoverMission.MIN_GOING_MEMBERS);

		if (!isDone()) {
			// Set mission capacity.
			if (hasVehicle()) {
				setMissionCapacity(getRover().getCrewCapacity());
			}
			int availableSuitNum = Mission.getNumberAvailableEVASuitsAtSettlement(startingPerson.getSettlement());
			if (availableSuitNum < getMissionCapacity()) {
				setMissionCapacity(availableSuitNum);
			}

			// Initialize data members.
			setStartingSettlement(startingPerson.getSettlement());
			excavatedMinerals = new HashMap<>(1);
			totalExcavatedMinerals = new HashMap<>(1);

			// Recruit additional members to mission.
			recruitMembersForMission(startingPerson);

			// Determine mining site.
			try {
				if (hasVehicle()) {
					miningSite = determineBestMiningSite(getRover(), getStartingSettlement());
					miningSite.setReserved(true);
					addNavpoint(new NavPoint(miningSite.getLocation(), "mining site"));
				}
			} catch (Exception e) {
				endMission("Mining site could not be determined.");
			}

			// Add home settlement
			addNavpoint(new NavPoint(getStartingSettlement().getCoordinates(), getStartingSettlement(),
					getStartingSettlement().getName()));

			// Check if vehicle can carry enough supplies for the mission.
			if (hasVehicle() && !isVehicleLoadable()) {
				endMission(VEHICLE_NOT_LOADABLE);// "Vehicle is not loadable. (Mining)");
			}

			if (!isDone()) {
				// Reserve light utility vehicle.
				luv = reserveLightUtilityVehicle();
				if (luv == null)
					endMission("Light utility vehicle not available.");
			}
		}

		// Add mining site phase.
		addPhase(MINING_SITE);

		// Set initial mission phase.
		setPhase(VehicleMission.APPROVAL);//.EMBARKING);
		setPhaseDescription(Msg.getString("Mission.phase.approval.description", getStartingSettlement().getName())); // $NON-NLS-1$
	}

	/**
	 * Constructor with explicit data.
	 * 
	 * @param members            collection of mission members.
	 * @param startingSettlement the starting settlement.
	 * @param miningSite         the site to mine.
	 * @param rover              the rover to use.
	 * @param description        the mission's description.
	 * @throws MissionException if error constructing mission.
	 */
	public Mining(Collection<MissionMember> members, Settlement startingSettlement, ExploredLocation miningSite,
			Rover rover, LightUtilityVehicle luv, String description) {

		// Use RoverMission constructor.
		super(description, (MissionMember) members.toArray()[0], RoverMission.MIN_GOING_MEMBERS, rover);

		// Initialize data members.
		setStartingSettlement(startingSettlement);
		this.miningSite = miningSite;
		miningSite.setReserved(true);
		excavatedMinerals = new HashMap<AmountResource, Double>(1);
		totalExcavatedMinerals = new HashMap<AmountResource, Double>(1);

		// Set mission capacity.
		setMissionCapacity(getRover().getCrewCapacity());
		int availableSuitNum = Mission.getNumberAvailableEVASuitsAtSettlement(startingSettlement);
		if (availableSuitNum < getMissionCapacity()) {
			setMissionCapacity(availableSuitNum);
		}

		Person person = null;
		Robot robot = null;

		// Add mission members.
		// TODO refactor this.
		Iterator<MissionMember> i = members.iterator();
		while (i.hasNext()) {
			MissionMember member = i.next();
			if (member instanceof Person) {
				person = (Person) member;
				person.getMind().setMission(this);
			} else if (member instanceof Robot) {
				robot = (Robot) member;
				robot.getBotMind().setMission(this);
			}
		}

		// Add mining site nav point.
		addNavpoint(new NavPoint(miningSite.getLocation(), "mining site"));

		// Add home settlement
		addNavpoint(new NavPoint(getStartingSettlement().getCoordinates(), getStartingSettlement(),
				getStartingSettlement().getName()));

		// Check if vehicle can carry enough supplies for the mission.
		if (hasVehicle() && !isVehicleLoadable()) {
			endMission(VEHICLE_NOT_LOADABLE);// "Vehicle is not loadable. (Mining)");
		}

		// Reserve light utility vehicle.
		this.luv = luv;
		if (luv == null) {
			endMission("Light utility vehicle not available.");
		} else {
			luv.setReservedForMission(true);
		}

		// Add mining site phase.
		addPhase(MINING_SITE);

		// Set initial mission phase.
		setPhase(VehicleMission.APPROVAL);//.EMBARKING);
		setPhaseDescription(Msg.getString("Mission.phase.approval.description", getStartingSettlement().getName())); // $NON-NLS-1$
	}

	/**
	 * Checks if a light utility vehicle (LUV) is available for the mission.
	 * 
	 * @param settlement the settlement to check.
	 * @return true if LUV available.
	 */
	public static boolean isLUVAvailable(Settlement settlement) {
		boolean result = false;

		Iterator<Vehicle> i = settlement.getParkedVehicles().iterator();
		while (i.hasNext()) {
			Vehicle vehicle = i.next();

			if (vehicle instanceof LightUtilityVehicle) {
				boolean usable = true;
				if (vehicle.isReserved())
					usable = false;

				if (vehicle.getStatus() != StatusType.PARKED && vehicle.getStatus() != StatusType.GARAGED)
					usable = false;

				if (((Crewable) vehicle).getCrewNum() > 0 || ((Crewable) vehicle).getRobotCrewNum() > 0)
					usable = false;

				if (usable)
					result = true;

			}
		}

		return result;
	}

	/**
	 * Checks if the required attachment parts are available.
	 * 
	 * @param settlement the settlement to check.
	 * @return true if available attachment parts.
	 */
	public static boolean areAvailableAttachmentParts(Settlement settlement) {
		boolean result = true;

		Inventory inv = settlement.getInventory();

		try {
			if (!inv.hasItemResource(ItemResourceUtil.pneumaticDrillID)) {
				result = false;
			}
			if (!inv.hasItemResource(ItemResourceUtil.backhoeID)) {
				result = false;
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error in getting parts.");
		}

		return result;
	}

	@Override
	protected void determineNewPhase() {
		if (APPROVAL.equals(getPhase())) {
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
				setPhaseDescription(Msg.getString("Mission.phase.disembarking.description",
						getCurrentNavpoint().getSettlement().getName())); // $NON-NLS-1$
			} else {
				setPhase(MINING_SITE);
				setPhaseDescription(
						Msg.getString("Mission.phase.miningSite.description", getCurrentNavpoint().getDescription())); // $NON-NLS-1$
			}
		} 
		
		else if (MINING_SITE.equals(getPhase())) {
			startTravelToNextNode();
			setPhase(VehicleMission.TRAVELLING);
			setPhaseDescription(
					Msg.getString("Mission.phase.travelling.description", getNextNavpoint().getDescription())); // $NON-NLS-1$
		} else if (DISEMBARKING.equals(getPhase())) {
			endMission(ALL_DISEMBARKED);
		}
	}

	@Override
	protected void performPhase(MissionMember member) {
		super.performPhase(member);
		if (MINING_SITE.equals(getPhase())) {
			miningPhase(member);
		}
	}

//    @Override
//    protected void performPhase(Robot robot) {
//        super.performPhase(robot);
//        if (MINING_SITE.equals(getPhase())) {
//            miningPhase(robot);
//        }
//    }
	@Override
	protected void performEmbarkFromSettlementPhase(MissionMember member) {
		super.performEmbarkFromSettlementPhase(member);
		performEmbarkFrom();
	}

//    @Override
//    protected void performEmbarkFromSettlementPhase(Robot robot) {
//        super.performEmbarkFromSettlementPhase(robot);
//        performEmbarkFrom();
//    }

	protected void performEmbarkFrom() {
		// Attach light utility vehicle for towing.
		if (!isDone() && (getRover().getTowedVehicle() == null)) {
			try {
				Inventory settlementInv = getStartingSettlement().getInventory();
				Inventory luvInv = luv.getInventory();
				getRover().setTowedVehicle(luv);
				luv.setTowingVehicle(getRover());
				settlementInv.retrieveUnit(luv);

				// Load light utility vehicle with attachment parts.
				settlementInv.retrieveItemResources(ItemResourceUtil.pneumaticDrillAR, 1);
				luvInv.storeItemResources(ItemResourceUtil.pneumaticDrillAR, 1);

				settlementInv.retrieveItemResources(ItemResourceUtil.backhoeAR, 1);
				luvInv.storeItemResources(ItemResourceUtil.backhoeAR, 1);
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Error loading light utility vehicle and attachment parts.");
				endMission("Light utility vehicle and attachment parts could not be loaded.");
			}
		}
	}

	@Override
	protected void performDisembarkToSettlementPhase(MissionMember member, Settlement disembarkSettlement) {
		performDisembarkTo();
		super.performDisembarkToSettlementPhase(member, disembarkSettlement);
	}

//    @Override
//    protected void performDisembarkToSettlementPhase(Robot robot, Settlement disembarkSettlement) {
//    	performDisembarkTo();
//        super.performDisembarkToSettlementPhase(robot, disembarkSettlement);
//    }

	protected void performDisembarkTo() {
		// Unload towed light utility vehicle.
		if (!isDone() && (getRover().getTowedVehicle() != null)) {
			try {
				Inventory settlementInv = getStartingSettlement().getInventory();
				Inventory luvInv = luv.getInventory();
				getRover().setTowedVehicle(null);
				luv.setTowingVehicle(null);
				settlementInv.storeUnit(luv);
				luv.determinedSettlementParkedLocationAndFacing();

				// Unload attachment parts.
				luvInv.retrieveItemResources(ItemResourceUtil.pneumaticDrillAR, 1);
				settlementInv.storeItemResources(ItemResourceUtil.pneumaticDrillAR, 1);

				luvInv.retrieveItemResources(ItemResourceUtil.backhoeAR, 1);
				settlementInv.storeItemResources(ItemResourceUtil.backhoeAR, 1);
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Error unloading light utility vehicle and attachment parts.");
				endMission("Light utility vehicle and attachment parts could not be unloaded.");
			}
		}
	}

	/**
	 * Perform the mining phase.
	 * 
	 * @param member the mission member performing the mining phase.
	 * @throws MissionException if error performing the mining phase.
	 */
	private void miningPhase(MissionMember member) {

		// Set the mining site start time if necessary.
		if (miningSiteStartTime == null) {
			miningSiteStartTime = (MarsClock) Simulation.instance().getMasterClock().getMarsClock().clone();
		}

		// Detach towed light utility vehicle if necessary.
		if (getRover().getTowedVehicle() != null) {
			getRover().setTowedVehicle(null);
			luv.setTowingVehicle(null);
		}

		// Check if crew has been at site for more than three sols.
		boolean timeExpired = false;
		MarsClock currentTime = (MarsClock) Simulation.instance().getMasterClock().getMarsClock().clone();
		if (MarsClock.getTimeDiff(currentTime, miningSiteStartTime) >= MINING_SITE_TIME) {
			timeExpired = true;
		}

		if (isEveryoneInRover()) {

			// Check if end mining flag is set.
			if (endMiningSite) {
				endMiningSite = false;
				setPhaseEnded(true);
			}

			// Check if crew has been at site for more than three sols, then end this phase.
			if (timeExpired) {
				setPhaseEnded(true);
			}

			// Determine if no one can start the mine site or collect resources tasks.
			boolean nobodyMineOrCollect = true;
			Iterator<MissionMember> j = getMembers().iterator();
			while (j.hasNext()) {
				MissionMember tempMember = j.next();
				if (MineSite.canMineSite(tempMember, getRover())) {
					nobodyMineOrCollect = false;
				}
				if (canCollectExcavatedMinerals(tempMember)) {
					nobodyMineOrCollect = false;
				}
			}

			// If no one can mine or collect minerals at the site and this is not due to it
			// just being
			// night time, end the mining phase.
			Mars mars = Simulation.instance().getMars();
			boolean inDarkPolarRegion = mars.getSurfaceFeatures().inDarkPolarRegion(getCurrentMissionLocation());
			double sunlight = mars.getSurfaceFeatures().getSolarIrradiance(getCurrentMissionLocation());
			if (nobodyMineOrCollect && ((sunlight > 0D) || inDarkPolarRegion)) {
				setPhaseEnded(true);
			}

			// Anyone in the crew or a single person at the home settlement has a dangerous
			// illness, end phase.
			if (hasEmergency()) {
				setPhaseEnded(true);
			}

			// Check if enough resources for remaining trip.
			if (!hasEnoughResourcesForRemainingMission(false)) {
				// If not, determine an emergency destination.
				determineEmergencyDestination(member);
				setPhaseEnded(true);
			}
		} else {
			// If mining time has expired for the site, have everyone end their
			// mining and collection tasks.
			if (timeExpired) {
				Iterator<MissionMember> i = getMembers().iterator();
				while (i.hasNext()) {
					MissionMember tempMember = i.next();
					if (member instanceof Person) {
						Person tempPerson = (Person) tempMember;

						Task task = tempPerson.getMind().getTaskManager().getTask();
						if (task instanceof MineSite) {
							((MineSite) task).endEVA();
						}
						if (task instanceof CollectMinedMinerals) {
							((CollectMinedMinerals) task).endEVA();
						}
					}
				}
			}
		}

		if (!getPhaseEnded()) {

			// 75% chance of assigning task, otherwise allow break.
			if (RandomUtil.lessThanRandPercent(75D)) {
				// If mining is still needed at site, assign tasks.
				if (!endMiningSite && !timeExpired) {
					// If person can collect minerals the site, start that task.
					if (canCollectExcavatedMinerals(member)) {
						if (member instanceof Person) {
							Person person = (Person) member;
							AmountResource mineralToCollect = getMineralToCollect(person);
							assignTask(person, new CollectMinedMinerals(person, getRover(), mineralToCollect));
						}
					}
//                    // Otherwise start the mining task if it can be done.
//                    else if (MineSite.canMineSite(member, getRover())) {
//                        assignTask(member, new MineSite(member, miningSite
//                                .getLocation(), (Rover) getVehicle(), luv));
//                    }
				}
			}
		} else {
			// Mark site as mined.
			miningSite.setMined(true);

			// Attach light utility vehicle for towing.
			getRover().setTowedVehicle(luv);
			luv.setTowingVehicle(getRover());
		}
	}

//    private void miningPhase(Robot robot) {
//
//        // Set the mining site start time if necessary.
//        if (miningSiteStartTime == null) {
//            miningSiteStartTime = (MarsClock) Simulation.instance()
//                    .getMasterClock().getMarsClock().clone();
//        }
//
//        // Detach towed light utility vehicle if necessary.
//        if (getRover().getTowedVehicle() != null) {
//            getRover().setTowedVehicle(null);
//            luv.setTowingVehicle(null);
//        }
//
//        // Check if crew has been at site for more than three sols.
//        boolean timeExpired = false;
//        MarsClock currentTime = (MarsClock) Simulation.instance()
//                .getMasterClock().getMarsClock().clone();
//        if (MarsClock.getTimeDiff(currentTime, miningSiteStartTime) >= MINING_SITE_TIME) {
//            timeExpired = true;
//        }
//
//        if (isEveryoneInRover()) {
//
//            // Check if end mining flag is set.
//            if (endMiningSite) {
//                endMiningSite = false;
//                setPhaseEnded(true);
//            }
//
//            // Check if crew has been at site for more than three sols, then end this phase.
//            if (timeExpired) {
//                setPhaseEnded(true);
//            }
//
//            // Determine if no one can start the mine site or collect resources tasks.
//            boolean nobodyMineOrCollect = true;
//            Iterator<Robot> i = getRobots().iterator();
//            while (i.hasNext()) {
//                Robot robotTemp = i.next();
//                if (MineSite.canMineSite(robotTemp, getRover())) {
//                    nobodyMineOrCollect = false;
//                }
//                if (canCollectExcavatedMinerals(robotTemp)) {
//                    nobodyMineOrCollect = false;
//                }
//            }
//
//            // If no one can mine or collect minerals at the site and this is not due to it just being
//            // night time, end the mining phase.
//            Mars mars = Simulation.instance().getMars();
//            boolean inDarkPolarRegion = mars.getSurfaceFeatures()
//                    .inDarkPolarRegion(getCurrentMissionLocation());
//            double sunlight = mars.getSurfaceFeatures().getSolarIrradiance(
//                    getCurrentMissionLocation());
//            if (nobodyMineOrCollect && ((sunlight > 0D) || inDarkPolarRegion)) {
//                setPhaseEnded(true);
//            }
//
//            // Anyone in the crew or a single robot at the home settlement has a dangerous illness, end phase.
//            if (hasEmergency()) {
//                setPhaseEnded(true);
//            }
//
//            // Check if enough resources for remaining trip.
//            if (!hasEnoughResourcesForRemainingMission(false)) {
//                // If not, determine an emergency destination.
//                determineEmergencyDestination(robot);
//                setPhaseEnded(true);
//            }
//        } else {
//            // If mining time has expired for the site, have everyone end their
//            // mining and collection tasks.
//            if (timeExpired) {
//                Iterator<Person> i = getPeople().iterator();
//                while (i.hasNext()) {
//                    Task task = i.next().getMind().getTaskManager().getTask();
//                    if (task instanceof MineSite) {
//                        ((MineSite) task).endEVA();
//                    }
//                    if (task instanceof CollectMinedMinerals) {
//                        ((CollectMinedMinerals) task).endEVA();
//                    }
//                }
//            }
//        }
//
//        if (!getPhaseEnded()) {
//
//            // 75% chance of assigning task, otherwise allow break.
//            if (RandomUtil.lessThanRandPercent(75D)) {
//                // If mining is still needed at site, assign tasks.
//                if (!endMiningSite && !timeExpired) {
//                    // If robot can collect minerals the site, start that task.
//                    if (canCollectExcavatedMinerals(robot)) {
//                        AmountResource mineralToCollect = getMineralToCollect(robot);
//                        assignTask(robot, new CollectMinedMinerals(robot,
//                                getRover(), mineralToCollect));
//                    }
//                    // Otherwise start the mining task if it can be done.
//                    else if (MineSite.canMineSite(robot, getRover())) {
//                        assignTask(robot, new MineSite(robot, miningSite
//                                .getLocation(), (Rover) getVehicle(), luv));
//                    }
//                }
//            }
//        } else {
//            // Mark site as mined.
//            miningSite.setMined(true);
//
//            // Attach light utility vehicle for towing.
//            getRover().setTowedVehicle(luv);
//            luv.setTowingVehicle(getRover());
//        }
//    }
	/**
	 * Checks if a person can collect minerals from the excavation pile.
	 * 
	 * @param member the member collecting.
	 * @return true if can collect minerals.
	 */
	private boolean canCollectExcavatedMinerals(MissionMember member) {
		boolean result = false;

		Iterator<AmountResource> i = excavatedMinerals.keySet().iterator();
		while (i.hasNext()) {
			AmountResource resource = i.next();
			if ((excavatedMinerals.get(resource) >= MINIMUM_COLLECT_AMOUNT)
					&& CollectMinedMinerals.canCollectMinerals(member, getRover(), resource)) {
				result = true;
			}
		}

		return result;
	}

//    private boolean canCollectExcavatedMinerals(Robot robot) {
//        boolean result = false;
//
//        Iterator<AmountResource> i = excavatedMinerals.keySet().iterator();
//        while (i.hasNext()) {
//            AmountResource resource = i.next();
//            if ((excavatedMinerals.get(resource) >= MINIMUM_COLLECT_AMOUNT)
//                    && CollectMinedMinerals.canCollectMinerals(robot,
//                            getRover(), resource)) {
//                result = true;
//            }
//        }
//
//        return result;
//    }
	/**
	 * Gets the mineral resource to collect from the excavation pile.
	 * 
	 * @param person the person collecting.
	 * @return mineral
	 */
	private AmountResource getMineralToCollect(Person person) {
		AmountResource result = null;
		double largestAmount = 0D;

		Iterator<AmountResource> i = excavatedMinerals.keySet().iterator();
		while (i.hasNext()) {
			AmountResource resource = i.next();
			if ((excavatedMinerals.get(resource) >= MINIMUM_COLLECT_AMOUNT)
					&& CollectMinedMinerals.canCollectMinerals(person, getRover(), resource)) {
				double amount = excavatedMinerals.get(resource);
				if (amount > largestAmount) {
					result = resource;
					largestAmount = amount;
				}
			}
		}

		return result;
	}

//    private AmountResource getMineralToCollect(Robot robot) {
//        AmountResource result = null;
//        double largestAmount = 0D;
//
//        Iterator<AmountResource> i = excavatedMinerals.keySet().iterator();
//        while (i.hasNext()) {
//            AmountResource resource = i.next();
//            if ((excavatedMinerals.get(resource) >= MINIMUM_COLLECT_AMOUNT)
//                    && CollectMinedMinerals.canCollectMinerals(robot,
//                            getRover(), resource)) {
//                double amount = excavatedMinerals.get(resource);
//                if (amount > largestAmount) {
//                    result = resource;
//                    largestAmount = amount;
//                }
//            }
//        }
//
//        return result;
//    }
	/**
	 * Ends mining at a site.
	 */
	public void endMiningAtSite() {
		logger.info("Mining site phase ended due to external trigger.");
		endMiningSite = true;

		// End each member's mining site task.
		Iterator<MissionMember> i = getMembers().iterator();
		while (i.hasNext()) {
			MissionMember member = i.next();
			if (member instanceof Person) {
				Person person = (Person) member;

				Task task = person.getMind().getTaskManager().getTask();
				if (task instanceof MineSite) {
					((MineSite) task).endEVA();
				}
				if (task instanceof CollectMinedMinerals) {
					((CollectMinedMinerals) task).endEVA();
				}
			}
		}

//        // End each bot's mining site task.
//        Iterator<Robot> j = getRobots().iterator();
//        while (j.hasNext()) {
//            Task task = j.next().getBotMind().getTaskManager().getTask();
//            if (task instanceof MineSite) {
//                ((MineSite) task).endEVA();
//            }
//            if (task instanceof CollectMinedMinerals) {
//                ((CollectMinedMinerals) task).endEVA();
//            }
//        }
	}

	/**
	 * Determines the best available mining site.
	 * 
	 * @param rover          the mission rover.
	 * @param homeSettlement the mission home settlement.
	 * @return best explored location for mining, or null if none found.
	 */
	public static ExploredLocation determineBestMiningSite(Rover rover, Settlement homeSettlement) {

		ExploredLocation result = null;
		double bestValue = 0D;

		try {
			double roverRange = rover.getRange();
			double tripTimeLimit = getTotalTripTimeLimit(rover, rover.getCrewCapacity(), true);
			double tripRange = getTripTimeRange(tripTimeLimit, rover.getBaseSpeed() / 2D);
			double range = roverRange;
			if (tripRange < range) {
				range = tripRange;
			}

			Iterator<ExploredLocation> i = Simulation.instance().getMars().getSurfaceFeatures().getExploredLocations()
					.iterator();
			while (i.hasNext()) {
				ExploredLocation site = i.next();

				boolean isMature = (site.getNumEstimationImprovement() >= MATURE_ESTIMATE_NUM);

				if (!site.isMined() && !site.isReserved() && site.isExplored() && isMature) {
					// Only mine from sites explored from home settlement.
					if (homeSettlement.equals(site.getSettlement())) {
						Coordinates siteLocation = site.getLocation();
						Coordinates homeLocation = homeSettlement.getCoordinates();
						if (homeLocation.getDistance(siteLocation) <= (range / 2D)) {
							double value = getMiningSiteValue(site, homeSettlement);
							if (value > bestValue) {
								result = site;
								bestValue = value;
							}
						}
					}
				}
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error determining best mining site.");
		}

		return result;
	}

	/**
	 * Gets the estimated mineral value of a mining site.
	 * 
	 * @param site       the mining site.
	 * @param settlement the settlement valuing the minerals.
	 * @return estimated value of the minerals at the site (VP).
	 * @throws MissionException if error determining the value.
	 */
	public static double getMiningSiteValue(ExploredLocation site, Settlement settlement) {

		double result = 0D;

		Map<String, Double> concentrations = site.getEstimatedMineralConcentrations();
		Iterator<String> i = concentrations.keySet().iterator();
		while (i.hasNext()) {
			String mineralType = i.next();
//            AmountResource mineralResource = AmountResource
//                    .findAmountResource(mineralType);
//          int mineralResource = ResourceUtil.findIDbyAmountResourceName(mineralType);
			Good mineralGood = GoodsUtil.getResourceGood(AmountResource.findAmountResource(mineralType));
			double mineralValue = settlement.getGoodsManager().getGoodValuePerItem(mineralGood);
			double concentration = concentrations.get(mineralType);
			double mineralAmount = (concentration / 100D) * MINERAL_BASE_AMOUNT;
			result += mineralValue * mineralAmount;
		}

		return result;
	}

	/**
	 * Gets the time limit of the trip based on life support capacity.
	 * 
	 * @param useBuffer use time buffer in estimation if true.
	 * @return time (millisols) limit.
	 * @throws MissionException if error determining time limit.
	 */
	private static double getTotalTripTimeLimit(Rover rover, int memberNum, boolean useBuffer) {

		Inventory vInv = rover.getInventory();

		double timeLimit = Double.MAX_VALUE;

		PersonConfig config = SimulationConfig.instance().getPersonConfiguration();

		// Check food capacity as time limit.
		// AmountResource food =
		// AmountResource.findAmountResource(LifeSupportType.FOOD);
		double foodConsumptionRate = config.getFoodConsumptionRate() * Mission.FOOD_MARGIN;
		double foodCapacity = vInv.getARCapacity(foodID, false);
		double foodTimeLimit = foodCapacity / (foodConsumptionRate * memberNum);
		if (foodTimeLimit < timeLimit) {
			timeLimit = foodTimeLimit;
		}

		// Check dessert1 capacity as time limit.
//        AmountResource dessert1 = AmountResource.findAmountResource("Soymilk");
//        double dessert1ConsumptionRate = config.getFoodConsumptionRate() / 6D;
//        double dessert1Capacity = vInv.getAmountResourceCapacity(dessert1, false);
//        double dessert1TimeLimit = dessert1Capacity / (dessert1ConsumptionRate * memberNum);
//        if (dessert1TimeLimit < timeLimit)
//            timeLimit = dessert1TimeLimit;

		// Check water capacity as time limit.
		// AmountResource water =
		// AmountResource.findAmountResource(LifeSupportType.WATER);
		double waterConsumptionRate = config.getWaterConsumptionRate() * Mission.WATER_MARGIN;
		double waterCapacity = vInv.getARCapacity(waterID, false);
		double waterTimeLimit = waterCapacity / (waterConsumptionRate * memberNum);
		if (waterTimeLimit < timeLimit) {
			timeLimit = waterTimeLimit;
		}

		// Check oxygen capacity as time limit.
		// AmountResource oxygen =
		// AmountResource.findAmountResource(LifeSupportType.OXYGEN);
		double oxygenConsumptionRate = config.getHighO2ConsumptionRate() * Mission.OXYGEN_MARGIN;
		double oxygenCapacity = vInv.getARCapacity(oxygenID, false);
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

	@Override
	public Map<Integer, Integer> getEquipmentNeededForRemainingMission(boolean useBuffer) {
		if (equipmentNeededCache != null) {
			return equipmentNeededCache;
		} else {
			Map<Integer, Integer> result = new HashMap<>();

			// Include required number of bags.
			result.put(EquipmentType.str2int(Bag.TYPE), NUMBER_OF_BAGS);

			equipmentNeededCache = result;
			return result;
		}
	}

	@Override
	public Settlement getAssociatedSettlement() {
		return getStartingSettlement();
	}

	@Override
	protected boolean isCapableOfMission(MissionMember member) {
		boolean result = super.isCapableOfMission(member);

		if (result) {
			boolean atStartingSettlement = false;
			if (member.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
				if (member.getSettlement() == getStartingSettlement()) {
					atStartingSettlement = true;
				}
			}
			result = atStartingSettlement;
		}

		return result;
	}

	@Override
	public double getEstimatedRemainingMissionTime(boolean useBuffer) {
		double result = super.getEstimatedRemainingMissionTime(useBuffer);
		result += getEstimatedRemainingMiningSiteTime();
		return result;
	}

	/**
	 * Gets the estimated time remaining at mining site in the mission.
	 * 
	 * @return time (millisols)
	 */
	private double getEstimatedRemainingMiningSiteTime() {
		double result = 0D;

		// Use estimated remaining mining time at site if still there.
		if (MINING_SITE.equals(getPhase())) {
			MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();
			double timeSpentAtMiningSite = MarsClock.getTimeDiff(currentTime, miningSiteStartTime);
			double remainingTime = MINING_SITE_TIME - timeSpentAtMiningSite;
			if (remainingTime > 0D) {
				result = remainingTime;
			}
		} else {
			// If mission hasn't reached mining site yet, use estimated mining site time.
			if (miningSiteStartTime == null) {
				result = MINING_SITE_TIME;
			}
		}

		return result;
	}

	@Override
	public Map<Integer, Number> getResourcesNeededForRemainingMission(boolean useBuffer) {
		Map<Integer, Number> result = super.getResourcesNeededForRemainingMission(useBuffer);

		double miningSiteTime = getEstimatedRemainingMiningSiteTime();
		double timeSols = miningSiteTime / 1000D;

		int crewNum = getPeopleNumber();

		// Determine life support supplies needed for trip.
		// AmountResource oxygen =
		// AmountResource.findAmountResource(LifeSupportType.OXYGEN);
		double oxygenAmount = PhysicalCondition.getOxygenConsumptionRate() * timeSols * crewNum;
		if (result.containsKey(oxygenID)) {
			oxygenAmount += (Double) result.get(oxygenID);
		}
		result.put(oxygenID, oxygenAmount);

		// AmountResource water =
		// AmountResource.findAmountResource(LifeSupportType.WATER);
		double waterAmount = PhysicalCondition.getWaterConsumptionRate() * timeSols * crewNum;
		if (result.containsKey(waterID)) {
			waterAmount += (Double) result.get(waterID);
		}
		result.put(waterID, waterAmount);

		// AmountResource food =
		// AmountResource.findAmountResource(LifeSupportType.FOOD);
		double foodAmount = PhysicalCondition.getFoodConsumptionRate() * timeSols * crewNum;
		if (result.containsKey(foodID)) {
			foodAmount += (Double) result.get(foodID);
		}
		result.put(foodID, foodAmount);
		/*
		 * // 2015-01-04 Added Soymilk AmountResource dessert1 =
		 * AmountResource.findAmountResource("Soymilk"); double dessert1Amount =
		 * PhysicalCondition.getFoodConsumptionRate() / 6D timeSols * crewNum; if
		 * (result.containsKey(dessert1)) dessert1Amount += (Double)
		 * result.get(dessert1); result.put(dessert1, dessert1Amount);
		 */
		return result;
	}

	/**
	 * Gets the range of a trip based on its time limit and mining site.
	 * 
	 * @param tripTimeLimit time (millisols) limit of trip.
	 * @param averageSpeed  the average speed of the vehicle.
	 * @return range (km) limit.
	 */
	private static double getTripTimeRange(double tripTimeLimit, double averageSpeed) {
		double tripTimeTravellingLimit = tripTimeLimit - MINING_SITE_TIME;
		double millisolsInHour = MarsClock.convertSecondsToMillisols(60D * 60D);
		double averageSpeedMillisol = averageSpeed / millisolsInHour;
		return tripTimeTravellingLimit * averageSpeedMillisol;
	}

	/**
	 * Gets the mission mining site.
	 * 
	 * @return mining site.
	 */
	public ExploredLocation getMiningSite() {
		return miningSite;
	}

	@Override
	public void endMission(String reason) {
		super.endMission(reason);

		if (miningSite != null) {
			miningSite.setReserved(false);
		}
		if (luv != null) {
			luv.setReservedForMission(false);
		}
	}

	/**
	 * Reserves a light utility vehicle for the mission.
	 * 
	 * @return reserved light utility vehicle or null if none.
	 */
	private LightUtilityVehicle reserveLightUtilityVehicle() {
		LightUtilityVehicle result = null;

		Iterator<Vehicle> i = getStartingSettlement().getParkedVehicles().iterator();
		while (i.hasNext() && (result == null)) {
			Vehicle vehicle = i.next();

			if (vehicle instanceof LightUtilityVehicle) {
				LightUtilityVehicle luvTemp = (LightUtilityVehicle) vehicle;
				if ((luvTemp.getStatus() == StatusType.PARKED || luvTemp.getStatus() == StatusType.GARAGED)
						&& !luvTemp.isReserved() && (luvTemp.getCrewNum() == 0) && (luvTemp.getRobotCrewNum() == 0)) {
					result = luvTemp;
					luvTemp.setReservedForMission(true);
				}
			}
		}

		return result;
	}

	/**
	 * Gets the mission's light utility vehicle.
	 * 
	 * @return light utility vehicle.
	 */
	public LightUtilityVehicle getLightUtilityVehicle() {
		return luv;
	}

	/**
	 * Gets the amount of a mineral currently excavated.
	 * 
	 * @param mineral the mineral resource.
	 * @return amount (kg)
	 */
	public double getMineralExcavationAmount(AmountResource mineral) {
		if (excavatedMinerals.containsKey(mineral)) {
			return excavatedMinerals.get(mineral);
		} else {
			return 0D;
		}
	}

	/**
	 * Gets the total amount of a mineral that has been excavated so far.
	 * 
	 * @param mineral the mineral resource.
	 * @return amount (kg)
	 */
	public double getTotalMineralExcavatedAmount(AmountResource mineral) {
		if (totalExcavatedMinerals.containsKey(mineral)) {
			return totalExcavatedMinerals.get(mineral);
		} else {
			return 0D;
		}
	}

	/**
	 * Excavates an amount of a mineral.
	 * 
	 * @param mineral the mineral resource.
	 * @param amount  the amount (kg)
	 */
	public void excavateMineral(AmountResource mineral, double amount) {
		double currentExcavated = amount;
		if (excavatedMinerals.containsKey(mineral)) {
			currentExcavated += excavatedMinerals.get(mineral);
		}
		excavatedMinerals.put(mineral, currentExcavated);

		double totalExcavated = amount;
		if (totalExcavatedMinerals.containsKey(mineral)) {
			totalExcavated += totalExcavatedMinerals.get(mineral);
		}
		totalExcavatedMinerals.put(mineral, totalExcavated);

		fireMissionUpdate(MissionEventType.EXCAVATE_MINERALS_EVENT);
	}

	/**
	 * Collects an amount of a mineral.
	 * 
	 * @param mineral the mineral resource.
	 * @param amount  the amount (kg)
	 * @throws Exception if error collecting mineral.
	 */
	public void collectMineral(AmountResource mineral, double amount) {
		double currentExcavated = 0D;
		if (excavatedMinerals.containsKey(mineral)) {
			currentExcavated = excavatedMinerals.get(mineral);
		}
		if (currentExcavated >= amount) {
			excavatedMinerals.put(mineral, (currentExcavated - amount));
		} else {
			throw new IllegalStateException(
					mineral.getName() + " amount: " + amount + " more than currently excavated.");
		}
		fireMissionUpdate(MissionEventType.COLLECT_MINERALS_EVENT);
	}

	@Override
	public void destroy() {
		super.destroy();

		miningSite = null;
		miningSiteStartTime = null;
		if (excavatedMinerals != null) {
			excavatedMinerals.clear();
		}
		excavatedMinerals = null;
		if (totalExcavatedMinerals != null) {
			totalExcavatedMinerals.clear();
		}
		totalExcavatedMinerals = null;
		luv = null;
	}
}