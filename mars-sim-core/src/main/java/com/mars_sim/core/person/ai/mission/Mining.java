/*
 * Mars Simulation Project
 * Mining.java
 * @date 2023-06-30
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.mission;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Stream;

import com.mars_sim.core.environment.MineralSite;
import com.mars_sim.core.equipment.Container;
import com.mars_sim.core.equipment.ContainerUtil;
import com.mars_sim.core.equipment.EVASuit;
import com.mars_sim.core.equipment.EVASuitUtil;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.mission.objectives.MiningObjective;
import com.mars_sim.core.mission.objectives.MiningObjective.MineralStats;
import com.mars_sim.core.mission.task.CollectMinedMinerals;
import com.mars_sim.core.mission.task.MineSite;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.ObjectiveType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.tool.RandomUtil;
import com.mars_sim.core.vehicle.Crewable;
import com.mars_sim.core.vehicle.LightUtilityVehicle;
import com.mars_sim.core.vehicle.Rover;
import com.mars_sim.core.vehicle.StatusType;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.core.vehicle.VehicleType;
import com.mars_sim.core.vehicle.comparators.CargoRangeComparator;

/**
 * Mission for mining mineral concentrations at an explored site.
 */
public class Mining extends EVAMission
	implements SiteMission {

	private static final Set<JobType> PREFERRED_JOBS = Set.of(JobType.AREOLOGIST, JobType.ASTRONOMER, JobType.PILOT);

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(Mining.class.getName());
	
	/** Mission phases */
	private static final MissionPhase MINING_SITE = new MissionPhase("Mission.phase.miningSite");
	private static final MissionStatus MINING_SITE_NOT_BE_DETERMINED = new MissionStatus("Mission.status.miningSite");
	private static final MissionStatus LUV_NOT_AVAILABLE = new MissionStatus("Mission.status.noLUV");
	private static final MissionStatus LUV_ATTACHMENT_PARTS_NOT_LOADABLE = new MissionStatus("Mission.status.noLUVAttachments");

	private static final int MAX = 3000;
	
	/** Number of large bags needed for mission. */
	public static final int NUMBER_OF_LARGE_BAGS = 4;

	/** The good value factor of a site. */
	public static final double MINERAL_GOOD_VALUE_FACTOR = 500;
	
	/** The average good value of a site. */
	static final double AVERAGE_RESERVE_GOOD_VALUE = 50_000;

	/** Amount of time(millisols) to spend at the mining site. */
	private static final double MINING_SITE_TIME = 4000D;

	/** Minimum amount (kg) of an excavated mineral that can be collected. */
	private static final double MINIMUM_COLLECT_AMOUNT = .01;


	/**
	 * The minimum number of mineral concentration estimation improvements for an
	 * exploration site for it to be considered mature enough to mine.
	 */
	public static final int MATURE_ESTIMATE_NUM = 75;

	private static final Set<ObjectiveType> OBJECTIVES = Set.of(ObjectiveType.BUILDERS_HAVEN, ObjectiveType.MANUFACTURING_DEPOT);

	private MiningObjective objective;


	/**
	 * Constructor.
	 * 
	 * @param startingPerson the person starting the mission.
	 * @throws MissionException if error creating mission.
	 */
	public Mining(Person startingPerson, boolean needsReview) {

		// Use RoverMission constructor.
		super(MissionType.MINING, startingPerson, null, MINING_SITE, MineSite.LIGHT_LEVEL);

		if (!isDone()) {
			// Recruit additional members to mission.
			if (!recruitMembersForMission(startingPerson, MIN_GOING_MEMBERS))
				return;
			
			Settlement s = getStartingSettlement();
			
			// Determine mining site.
			var miningSite = determineBestMiningSite(getRover(), s);
			if (miningSite == null) {
				logger.severe(startingPerson, "Mining site could not be determined.");
				endMission(MINING_SITE_NOT_BE_DETERMINED);
				return;
			}

			var luv = reserveLightUtilityVehicle();

			prepMining(luv, miningSite, needsReview);
		}
	}

	/**
	 * Constructor with explicit data.
	 * 
	 * @param members            collection of mission members.
	 * @param miningSite         the site to mine.
	 * @param rover              the rover to use.
	 * @param description        the mission's description.
	 */
	public Mining(Collection<Worker> members, MineralSite miningSite,
			Rover rover, LightUtilityVehicle luv) {

		// Use RoverMission constructor.,  
		super(MissionType.MINING, (Worker) members.toArray()[0], rover, MINING_SITE, MineSite.LIGHT_LEVEL);
		addMembers(members, false);

		prepMining(luv, miningSite, false);
	}
	
	private void prepMining(LightUtilityVehicle l, MineralSite miningSite, boolean needsReview) {
		// Initialize data members.
		miningSite.setReserved(true);
		
		int numMembers = (getMissionCapacity() + getMembers().size()) / 2;
		int buffer = (int)(numMembers * 1.5);
		int newContainerNum = Math.max(buffer, NUMBER_OF_LARGE_BAGS);
		
		setEVAEquipment(EquipmentType.LARGE_BAG, newContainerNum);

		// Add mining site nav point.
		addNavpoint(miningSite.getLocation(), "a mining site");
		
		// Add home settlement
		Settlement s = getStartingSettlement();
		addNavpoint(s);

		// Check if vehicle can carry enough supplies for the mission.
		if (hasVehicle() && !isVehicleLoadable()) {
			endMission(CANNOT_LOAD_RESOURCES);
		}

		// Reserve light utility vehicle.
		if (l == null) {
			logger.warning("Light utility vehicle not available.");
			endMission(LUV_NOT_AVAILABLE);
			return;
		} 

		claimVehicle(l);

		// Set initial mission phase.
		setInitialPhase(needsReview);

		objective = new MiningObjective(l, miningSite);
		addObjective(objective);
	}

	/**
	 * Checks if a light utility vehicle (LUV) is available for the mission.
	 * 
	 * @param settlement the settlement to check.
	 * @return true if LUV available.
	 */
	public static boolean isLUVAvailable(Settlement settlement) {
		boolean result = false;

		Iterator<Vehicle> i = settlement.getParkedGaragedVehicles().iterator();
		while (i.hasNext()) {
			Vehicle vehicle = i.next();
			if (vehicle.getVehicleType() == VehicleType.LUV) {
				boolean usable = !vehicle.isReserved();				
                usable = usable && vehicle.isVehicleReady() && !vehicle.isBeingTowed();

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

		try {
			if (!settlement.getItemResourceIDs().contains(ItemResourceUtil.PNEUMATIC_DRILL_ID)) {
				result = false;
			}
			if (!settlement.getItemResourceIDs().contains(ItemResourceUtil.BACKHOE_ID)) {
				result = false;
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error in getting parts.");
		}

		return result;
	}



	@Override
	protected void performDepartingFromSettlementPhase(Worker member) {
		super.performDepartingFromSettlementPhase(member);
		performEmbarkFrom();
	}

	private void performEmbarkFrom() {
		// Attach light utility vehicle for towing.
		if (!isDone() && (getRover().getTowedVehicle() == null)) {

			Settlement settlement = getStartingSettlement();

			var luv = attachLUV(true);
			settlement.removeVicinityParkedVehicle(luv);

			if (!settlement.hasItemResource(ItemResourceUtil.PNEUMATIC_DRILL_ID)
					|| !settlement.hasItemResource(ItemResourceUtil.BACKHOE_ID)) {
				logger.warning(luv, 
						"Could not load LUV and/or its attachment parts for mission " + getName());
				endMission(LUV_ATTACHMENT_PARTS_NOT_LOADABLE);
				return;
			}
			
			// Load light utility vehicle with attachment parts.
			settlement.retrieveItemResource(ItemResourceUtil.PNEUMATIC_DRILL_ID, 1);
			luv.storeItemResource(ItemResourceUtil.PNEUMATIC_DRILL_ID, 1);

			settlement.retrieveItemResource(ItemResourceUtil.BACKHOE_ID, 1);
			luv.storeItemResource(ItemResourceUtil.BACKHOE_ID, 1);
		}
	}

	@Override
	protected void performDisembarkToSettlementPhase(Worker member, Settlement disembarkSettlement) {
		// Disconnect the LUV
		disengageLUV();
		
		super.performDisembarkToSettlementPhase(member, disembarkSettlement);
	}

	/**
	 * Disconnects the LUV and return the attachment parts prior to disembarking.
	 */
	protected void disengageLUV() {
		// Unload towed light utility vehicle.
		if (!isDone() && (getRover().getTowedVehicle() != null)) {
			Settlement settlement = getStartingSettlement();

			var luv = attachLUV(false);
			settlement.removeVicinityParkedVehicle(luv);
			luv.findNewParkingLoc();

			// Unload attachment parts.
			luv.retrieveItemResource(ItemResourceUtil.PNEUMATIC_DRILL_ID, 1);
			settlement.storeItemResource(ItemResourceUtil.PNEUMATIC_DRILL_ID, 1);

			luv.retrieveItemResource(ItemResourceUtil.BACKHOE_ID, 1);
			settlement.storeItemResource(ItemResourceUtil.BACKHOE_ID, 1);
		}
	}

	/**
	 * Performs the EVA.
	 */
	@Override
	protected boolean performEVA(Person person) {

		Rover rover = getRover();
		double roverRemainingCap = rover.getCargoCapacity() - rover.getStoredMass();

		if (roverRemainingCap <= 0) {
			logger.info(getRover(), "No more room in " + rover.getName());
			addMissionLog("No remaining rover capacity", person.getName());
			return false;
		}

		double weight = person.getMass();
		if (roverRemainingCap < weight) {
			logger.info(getRover(), "No enough capacity to fit " + person.getName() + "(" + weight + " kg).");
			addMissionLog("Rover capacity full", person.getName());
			return false;
		}
		
		if (person.isEVAFit()) {
			attachLUV(false);

			// Is there extractable minerals ?
			if (getExtractMineralsStream(person).findAny().isPresent()) {
				int mineralToCollect = getMineralToCollect(person);
				if (mineralToCollect > 0) {
					assignTask(person, new CollectMinedMinerals(person, objective, getRover(), mineralToCollect));
				}
			}
			else {
				assignTask(person, new MineSite(person, objective, getRover()));
			}	
		}
		return true;
	}

	/**
	 * Attaches or releases the LUV from a rover.
	 * 
	 * @param attach
	 * @return
	 */
	private LightUtilityVehicle attachLUV(boolean attach) {
		LightUtilityVehicle luv = objective.getLUV();
		if (attach) {
			Rover rover = getRover();
			rover.setTowedVehicle(luv);
			luv.setTowingVehicle(rover);
		}
		else if (luv.getTowingVehicle() != null) {
			getRover().setTowedVehicle(null);
			luv.setTowingVehicle(null);
		}

		return luv;
	}

	/**
	 * Closes down the mining activities.
	 */
	@Override
	protected void endEVATasks() {
		super.endEVATasks();

		// Attach light utility vehicle for towing.
		attachLUV(true);
	}

	/**
	 * Gets the Vehicle comparator that is based on largest cargo.
	 */
	@Override
	protected  Comparator<Vehicle> getVehicleComparator() {
		return new CargoRangeComparator();
	}

	/**
	 * Checks if a person can perform a CollectMinedMinerals task.
	 * 
	 * @param member      the member to perform the task
	 * @param mineralType the resource to collect.
	 * @return true if person can perform the task.
	 */
	private  boolean canCollectMinerals(Person member, Rover rover, int mineralType) {
		
		// Checks if available bags with remaining capacity for resource.
		Container bag = ContainerUtil.findLeastFullContainer(rover,
															EquipmentType.LARGE_BAG,
															mineralType);
		boolean bagAvailable = (bag != null);

		// Check if bag and full EVA suit can be carried by person or is too heavy.
		double carryMass = 0D;
		if (bag != null) {
			carryMass += bag.getBaseMass();
		}

		EVASuit suit = EVASuitUtil.findRegisteredOrGoodEVASuit(member);
		if (suit != null) {
			carryMass += suit.getMass();
			carryMass += suit.getRemainingCombinedCapacity(ResourceUtil.OXYGEN_ID);
			carryMass += suit.getRemainingCombinedCapacity(ResourceUtil.WATER_ID);
		}
		double carryCapacity = member.getCarryingCapacity();
		boolean canCarryEquipment = (carryCapacity >= carryMass);

		return (bagAvailable && canCarryEquipment);
	}

	private Stream<Entry<Integer, MineralStats>> getExtractMineralsStream(Person p) {
		return objective.getMineralStats().entrySet().stream()
					.filter(e -> e.getValue().getAvailable() > MINIMUM_COLLECT_AMOUNT)
					.filter(e -> canCollectMinerals(p, getRover(), e.getKey()));
	}

	/**
	 * Gets the mineral resource to collect from the excavation pile.
	 * 
	 * @param person the person collecting.
	 * @return mineral
	 */
	private int getMineralToCollect(Person person) {
		var candidates = getExtractMineralsStream(person).toList();
		
		int selected = -1;
		double largest = 0;
		for(var e : candidates) {
			if ((e.getValue().getAvailable() > largest) ||
					(selected == -1)) {
				largest = e.getValue().getAvailable();
				selected = e.getKey();
			}
		}
		return selected;
	}

	/**
	 * Determines the best available mining site.
	 * 
	 * @param rover          the mission rover.
	 * @param homeSettlement the mission home settlement.
	 * @return best explored location for mining, or null if none found.
	 */
	private static MineralSite determineBestMiningSite(Rover rover, Settlement homeSettlement) {

		MineralSite result = null;
		double bestValue = 0D;
		var authority = homeSettlement.getReportingAuthority();

		try {
			double roverRange = rover.getEstimatedRange();
			double tripTimeLimit = rover.getTotalTripTimeLimit(true);
			double tripRange = getTripTimeRange(tripTimeLimit, rover.getBaseSpeed() / 2D);
			double range = roverRange;
			if (tripRange < range) {
				range = tripRange;
			}

			for(MineralSite site : surfaceFeatures.getAllPossibleRegionOfInterestLocations()) {
				boolean isMature = (site.getNumEstimationImprovement() >= 
						RandomUtil.getRandomDouble(MATURE_ESTIMATE_NUM/2.0, 1.0 * MATURE_ESTIMATE_NUM));

				if (site.isMinable() && !site.isReserved() && site.isExplored() && isMature
					// Only mine from sites explored from home authority.
					&& authority.equals(site.getOwner())
					&& homeSettlement.getCoordinates().getDistance(site.getLocation()) <= range) {
						double value = getMiningSiteValue(site, homeSettlement);
						if (value > bestValue) {
							result = site;
							bestValue = value;
					}
				}
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error determining best mining site.");
		}

		return result;
	}

	/**
	 * Determines the total mature mining sites score.
	 * 
	 * @param rover          the mission rover.
	 * @param homeSettlement the mission home settlement.
	 * @return the total score
	 */
	public static double getMatureMiningSitesTotalScore(Rover rover, Settlement homeSettlement) {

		double total = 0;

		try {
			double roverRange = rover.getEstimatedRange();
			double tripTimeLimit = rover.getTotalTripTimeLimit(true);
			double tripRange = getTripTimeRange(tripTimeLimit, rover.getBaseSpeed() / 2D);
			double range = roverRange;
			if (tripRange < range) {
				range = tripRange;
			}

			var authority = homeSettlement.getReportingAuthority();
			for (MineralSite site : surfaceFeatures.getAllPossibleRegionOfInterestLocations()) {
				boolean isMature = (site.getNumEstimationImprovement() >= 
						RandomUtil.getRandomDouble(MATURE_ESTIMATE_NUM/2.0, 1.0 * MATURE_ESTIMATE_NUM));
				if (site.isMinable() && site.isClaimed() && !site.isReserved() && site.isExplored() && isMature
					// Only mine from sites explored from home settlement.
					&& (site.getOwner() == null || authority.equals(site.getOwner()))
					&& homeSettlement.getCoordinates().getDistance(site.getLocation()) <= range) {
						double value = getMiningSiteValue(site, homeSettlement);
						total += value;
				}
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error determining best mining site.");
		}

		return total;
	}
	
	/**
	 * Gets the estimated mineral value of a mining site.
	 * 
	 * @param site       the mining site.
	 * @param settlement the settlement valuing the minerals.
	 * @return estimated value of the minerals at the site (VP).
	 * @throws MissionException if error determining the value.
	 */
	public static double getMiningSiteValue(MineralSite site, Settlement settlement) {

		double result = 0D;

		for (Map.Entry<String, Double> conc : site.getEstimatedMineralConcentrations().entrySet()) {
			int mineralResource = ResourceUtil.findIDbyAmountResourceName(conc.getKey());
			double mineralValue = settlement.getGoodsManager().getGoodValuePoint(mineralResource);
			double reserve = site.getRemainingMass();
			double mineralAmount = (conc.getValue() / 100) * reserve / AVERAGE_RESERVE_GOOD_VALUE * MINERAL_GOOD_VALUE_FACTOR;
			result += mineralValue * mineralAmount;
		}

		result = Math.min(MAX, result);
		
		logger.info(settlement, 30_000L, site.getLocation() 
			+ " has a Mining Value of " + Math.round(result * 100.0)/100.0 + ".");
		
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
		double averageSpeedMillisol = averageSpeed / MarsTime.MILLISOLS_PER_HOUR;
		return tripTimeTravellingLimit * averageSpeedMillisol;
	}


	@Override
	protected void endMission(MissionStatus endStatus) {
		if (objective != null) {
			objective.getSite().setReserved(false);
			releaseVehicle(objective.getLUV());
		}

		super.endMission(endStatus);
	}

	/**
	 * Reserves a light utility vehicle for the mission.
	 * 
	 * @return reserved light utility vehicle or null if none.
	 */
	private LightUtilityVehicle reserveLightUtilityVehicle() {
		for(Vehicle vehicle : getStartingSettlement().getParkedGaragedVehicles()) {
			if (vehicle instanceof LightUtilityVehicle luvTemp
					&& ((luvTemp.getPrimaryStatus() == StatusType.PARKED) || (luvTemp.getPrimaryStatus() == StatusType.GARAGED))
					&& !luvTemp.isReserved()
					&& (luvTemp.getCrewNum() == 0) && (luvTemp.getRobotCrewNum() == 0)) {
				claimVehicle(luvTemp);
				return luvTemp;
			}
			
		}

		return null;
	}

	@Override
	protected Set<JobType> getPreferredPersonJobs() {
		return PREFERRED_JOBS;
	}

	@Override
	public Set<ObjectiveType> getObjectiveSatisified() {
		return OBJECTIVES;
	}
	
	@Override
	public double getTotalSiteScore(Settlement reviewerSettlement) {
		return getMiningSiteValue(objective.getSite(), reviewerSettlement);
	}

	@Override
	protected double getEstimatedTimeAtEVASite(boolean buffer) {
		return MINING_SITE_TIME;
	}
}
