/*
 * Mars Simulation Project
 * Mining.java
 * @date 2022-07-14
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.environment.ExploredLocation;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.JobType;
import org.mars_sim.msp.core.person.ai.task.CollectMinedMinerals;
import org.mars_sim.msp.core.person.ai.task.MineSite;
import org.mars_sim.msp.core.person.ai.task.utils.Worker;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.vehicle.Crewable;
import org.mars_sim.msp.core.vehicle.LightUtilityVehicle;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.StatusType;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.core.vehicle.VehicleType;

/**
 * Mission for mining mineral concentrations at an explored site.
 */
public class Mining extends EVAMission
	implements SiteMission {

	private static final Set<JobType> PREFERRED_JOBS = Set.of(JobType.AREOLOGIST, JobType.ASTRONOMER, JobType.PILOT);

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(Mining.class.getName());
	
	/** Mission phases */
	private static final MissionPhase MINING_SITE = new MissionPhase("Mission.phase.miningSite");
	private static final MissionStatus MINING_SITE_NOT_BE_DETERMINED = new MissionStatus("Mission.status.miningSite");
	private static final MissionStatus LUV_NOT_AVAILABLE = new MissionStatus("Mission.status.noLUV");
	private static final MissionStatus LUV_ATTACHMENT_PARTS_NOT_LOADABLE = new MissionStatus("Mission.status.noLUVAttachments");

	private static final int MAX = 200;
	
	/** Number of large bags needed for mission. */
	public static final int NUMBER_OF_LARGE_BAGS = 20;

	/** Base amount (kg) of a type of mineral at a site. */
	static final double MINERAL_BASE_AMOUNT = 2500D;

	/** Amount of time(millisols) to spend at the mining site. */
	private static final double MINING_SITE_TIME = 4000D;

	/** Minimum amount (kg) of an excavated mineral that can be collected. */
	private static final double MINIMUM_COLLECT_AMOUNT = 10D;


	/**
	 * The minimum number of mineral concentration estimation improvements for an
	 * exploration site for it to be considered mature enough to mine.
	 */
	public static final int MATURE_ESTIMATE_NUM = 10;

	
	private ExploredLocation miningSite;
	private LightUtilityVehicle luv;
	
	private Map<AmountResource, Double> excavatedMinerals;
	private Map<AmountResource, Double> totalExcavatedMinerals;

	/**
	 * Constructor
	 * 
	 * @param startingPerson the person starting the mission.
	 * @throws MissionException if error creating mission.
	 */
	public Mining(Person startingPerson, boolean needsReview) {

		// Use RoverMission constructor.
		super(MissionType.MINING, startingPerson, null, MINING_SITE);
		
		if (!isDone()) {
			// Initialize data members.
			excavatedMinerals = new HashMap<>(1);
			totalExcavatedMinerals = new HashMap<>(1);
			setEVAEquipment(EquipmentType.LARGE_BAG, NUMBER_OF_LARGE_BAGS);

			// Recruit additional members to mission.
			if (!recruitMembersForMission(startingPerson, MIN_GOING_MEMBERS))
				return;

			Settlement s = getStartingSettlement();
			
			// Determine mining site.
			try {
				if (hasVehicle()) {
					miningSite = determineBestMiningSite(getRover(), s);
					miningSite.setReserved(true);
					addNavpoint(miningSite.getLocation(), "mining site");
				}
			} catch (Exception e) {
				logger.severe(startingPerson, "Mining site could not be determined.", e);
				endMission(MINING_SITE_NOT_BE_DETERMINED);
			}

			// Add home settlement
			addNavpoint(s);

			// Check if vehicle can carry enough supplies for the mission.
			if (hasVehicle() && !isVehicleLoadable()) {
				endMission(CANNOT_LOAD_RESOURCES);
			}

			if (!isDone()) {
				// Reserve light utility vehicle.
				luv = reserveLightUtilityVehicle();
				if (luv == null) {
					endMission(LUV_NOT_AVAILABLE);
					return;
				}
				setInitialPhase(needsReview);
			}
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
	public Mining(Collection<Worker> members, ExploredLocation miningSite,
			Rover rover, LightUtilityVehicle luv) {

		// Use RoverMission constructor.,  
		super(MissionType.MINING, (Worker) members.toArray()[0], rover, MINING_SITE);
		
		// Initialize data members.
		this.miningSite = miningSite;
		miningSite.setReserved(true);
		excavatedMinerals = new HashMap<>(1);
		totalExcavatedMinerals = new HashMap<>(1);
		setEVAEquipment(EquipmentType.LARGE_BAG, NUMBER_OF_LARGE_BAGS);

		addMembers(members, false);

		// Add mining site nav point.
		addNavpoint(miningSite.getLocation(), "mining site");

		// Add home settlement
		Settlement s = getStartingSettlement();
		addNavpoint(s);

		// Check if vehicle can carry enough supplies for the mission.
		if (hasVehicle() && !isVehicleLoadable()) {
			endMission(CANNOT_LOAD_RESOURCES);
		}

		// Reserve light utility vehicle.
		this.luv = luv;
		if (luv == null) {
			logger.warning("Light utility vehicle not available.");
			endMission(LUV_NOT_AVAILABLE);
		} else {
			luv.setReservedForMission(true);
		}

		// Set initial mission phase.
		setInitialPhase(false);

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
			if (vehicle.getVehicleType() == VehicleType.LUV) {
				boolean usable = !vehicle.isReserved();
                usable = vehicle.isVehicleReady();

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
			if (!settlement.getItemResourceIDs().contains(ItemResourceUtil.pneumaticDrillID)) {
				result = false;
			}
			if (!settlement.getItemResourceIDs().contains(ItemResourceUtil.backhoeID)) {
				result = false;
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error in getting parts.");
		}

		return result;
	}


	@Override
	protected void performEmbarkFromSettlementPhase(Worker member) {
		super.performEmbarkFromSettlementPhase(member);
		performEmbarkFrom();
	}

	private void performEmbarkFrom() {
		// Attach light utility vehicle for towing.
		if (!isDone() && (getRover().getTowedVehicle() == null)) {

			Settlement settlement = getStartingSettlement();

			getRover().setTowedVehicle(luv);
			luv.setTowingVehicle(getRover());
			settlement.removeParkedVehicle(luv);

			if (!settlement.hasItemResource(ItemResourceUtil.pneumaticDrillID)
					|| !settlement.hasItemResource(ItemResourceUtil.backhoeID)) {
				logger.warning(luv, 
						"Could not load LUV and/or its attachment parts for mission " + getName());
				endMission(LUV_ATTACHMENT_PARTS_NOT_LOADABLE);
				return;
			}
			
			// Load light utility vehicle with attachment parts.
			settlement.retrieveItemResource(ItemResourceUtil.pneumaticDrillID, 1);
			luv.storeItemResource(ItemResourceUtil.pneumaticDrillID, 1);

			settlement.retrieveItemResource(ItemResourceUtil.backhoeID, 1);
			luv.storeItemResource(ItemResourceUtil.backhoeID, 1);
		}
	}

	@Override
	protected void performDisembarkToSettlementPhase(Worker member, Settlement disembarkSettlement) {
		performDisembarkTo();
		super.performDisembarkToSettlementPhase(member, disembarkSettlement);
	}

	protected void performDisembarkTo() {
		// Unload towed light utility vehicle.
		if (!isDone() && (getRover().getTowedVehicle() != null)) {
			Settlement settlement = getStartingSettlement();
			
			getRover().setTowedVehicle(null);
			luv.setTowingVehicle(null);
			settlement.removeParkedVehicle(luv);
			luv.findNewParkingLoc();

			// Unload attachment parts.
			luv.retrieveItemResource(ItemResourceUtil.pneumaticDrillID, 1);
			settlement.storeItemResource(ItemResourceUtil.pneumaticDrillID, 1);

			luv.retrieveItemResource(ItemResourceUtil.backhoeID, 1);
			settlement.storeItemResource(ItemResourceUtil.backhoeID, 1);
		}
	}

	/**
	 * Perform the EVA
	 */
	@Override
	protected boolean performEVA(Person person) {
		// Detach towed light utility vehicle if necessary.
		if (getRover().getTowedVehicle() != null) {
			getRover().setTowedVehicle(null);
			luv.setTowingVehicle(null);
		}

		// Determine if no one can start the mine site or collect resources tasks.
		boolean nobodyMineOrCollect = true;
		for(Worker tempMember : getMembers()) {
			if (MineSite.canMineSite(tempMember, getRover())) {
				nobodyMineOrCollect = false;
			}
			if (canCollectExcavatedMinerals(tempMember)) {
				nobodyMineOrCollect = false;
			}
		}

		// Nobody can do anything so stop
		if (nobodyMineOrCollect) {
			logger.warning(getRover(), "No one can do mining Task in mission " + getName());
			return false;
		}

		// 75% chance of assigning task, otherwise allow break.
		if (canCollectExcavatedMinerals(person)) {
			AmountResource mineralToCollect = getMineralToCollect(person);
			assignTask(person, new CollectMinedMinerals(person, getRover(), mineralToCollect));
		}
		else {
			assignTask(person, new MineSite(person, miningSite.getLocation(), getRover(), luv));
		}

		return true;
	}


	/**
	 * Close down the mining activities
	 */
	@Override
	protected void endEVATasks() {
		super.endEVATasks();

		// Mark site as mined.
		miningSite.setMined(true);

		// Attach light utility vehicle for towing.
		Rover rover = getRover();
		if (!luv.equals(rover.getTowedVehicle())) {
			rover.setTowedVehicle(luv);
			luv.setTowingVehicle(rover);
		}
	}

	/**
	 * Checks if a person can collect minerals from the excavation pile.
	 * 
	 * @param member the member collecting.
	 * @return true if can collect minerals.
	 */
	private boolean canCollectExcavatedMinerals(Worker member) {
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
			double roverRange = rover.getRange(MissionType.MINING);
			double tripTimeLimit = getTotalTripTimeLimit(rover, rover.getCrewCapacity(), true);
			double tripRange = getTripTimeRange(tripTimeLimit, rover.getBaseSpeed() / 2D);
			double range = roverRange;
			if (tripRange < range) {
				range = tripRange;
			}

			for(ExploredLocation site : surfaceFeatures.getExploredLocations()) {
				boolean isMature = (site.getNumEstimationImprovement() >= MATURE_ESTIMATE_NUM);

				if (!site.isMined() && !site.isReserved() && site.isExplored() && isMature) {
					// Only mine from sites explored from home settlement.
					Settlement owner = site.getSettlement();
					if ((owner == null) || homeSettlement.equals(site.getSettlement())) {
						Coordinates siteLocation = site.getLocation();
						Coordinates homeLocation = homeSettlement.getCoordinates();
						if (Coordinates.computeDistance(homeLocation, siteLocation) <= range) {
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

		for( Map.Entry<String,Double> conc : site.getEstimatedMineralConcentrations().entrySet()) {
			int mineralResource = ResourceUtil.findIDbyAmountResourceName(conc.getKey());
			double mineralValue = settlement.getGoodsManager().getGoodValuePoint(mineralResource);
			double mineralAmount = (conc.getValue() / 100D) * MINERAL_BASE_AMOUNT;
			result += mineralValue * mineralAmount;
		}
			
		result = Math.min(MAX, result);
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
		double averageSpeedMillisol = averageSpeed / MarsClock.MILLISOLS_PER_HOUR;
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
	protected void endMission(MissionStatus endStatus) {
		super.endMission(endStatus);

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
			if (vehicle.getVehicleType() == VehicleType.LUV) {
				LightUtilityVehicle luvTemp = (LightUtilityVehicle) vehicle;
				if (((luvTemp.getPrimaryStatus() == StatusType.PARKED) || (luvTemp.getPrimaryStatus() == StatusType.GARAGED))
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
		return excavatedMinerals.getOrDefault(mineral, 0D);
	}

	/**
	 * Gets the total amount of a mineral that has been excavated so far.
	 * 
	 * @param mineral the mineral resource.
	 * @return amount (kg)
	 */
	public double getTotalMineralExcavatedAmount(AmountResource mineral) {
		return totalExcavatedMinerals.getOrDefault(mineral, 0D);
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
	protected Set<JobType> getPreferredPersonJobs() {
		return PREFERRED_JOBS;
	}

	@Override
	public double getTotalSiteScore(Settlement reviewerSettlement) {
		return getMiningSiteValue(miningSite, reviewerSettlement);
	}

	@Override
	protected double getEstimatedTimeAtEVASite(boolean buffer) {
		return MINING_SITE_TIME;
	}
}
