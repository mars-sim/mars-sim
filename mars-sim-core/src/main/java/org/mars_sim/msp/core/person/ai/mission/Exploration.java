/**
 * Mars Simulation Project
 * Exploration.java
 * @version 3.1.0 2017-08-08
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Direction;
import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.equipment.EquipmentFactory;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.equipment.SpecimenBox;
import org.mars_sim.msp.core.mars.ExploredLocation;
import org.mars_sim.msp.core.mars.MineralMap;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.EVAOperation;
import org.mars_sim.msp.core.person.ai.task.ExploreSite;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * The Exploration class is a mission to travel in a rover to several random
 * locations around a settlement and collect rock samples.
 */
public class Exploration extends RoverMission implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static final Logger logger = Logger.getLogger(Exploration.class.getName());
	private static final String loggerName = logger.getName();
	private static final String sourceName = loggerName.substring(loggerName.lastIndexOf(".") + 1, loggerName.length());
	
	/** Default description. */
	public static final String DEFAULT_DESCRIPTION = Msg.getString("Mission.description.exploration"); //$NON-NLS-1$

	/** Mission Type enum. */
	public static final MissionType missionType = MissionType.EXPLORATION;
	
	/** Mission phase. */
	public static final MissionPhase EXPLORE_SITE = new MissionPhase(Msg.getString("Mission.phase.exploreSite")); //$NON-NLS-1$

	/** Number of specimen containers required for the mission. */
	public static final int REQUIRED_SPECIMEN_CONTAINERS = 20;

	/** Number of collection sites. */
	private static final int NUM_SITES = 5;

	/** Amount of time to explore a site. */
	public static final double EXPLORING_SITE_TIME = 1000D;

	/** Maximum mineral concentration estimation diff from actual. */
	private static final double MINERAL_ESTIMATION_CEILING = 20D;

	// Data members
	/** Map of exploration sites and their completion. */
	private Map<String, Double> explorationSiteCompletion;
	/** The start time at the current exploration site. */
	private MarsClock explorationSiteStartTime;
	/** The current exploration site. */
	private ExploredLocation currentSite;
	/** List of sites explored by this mission. */
	private List<ExploredLocation> exploredSites;
	/** External flag for ending exploration at the current site. */
	private boolean endExploringSite;

	// Static members
	private static final int oxygenID = ResourceUtil.oxygenID;
	private static final int waterID = ResourceUtil.waterID;
	private static final int foodID = ResourceUtil.foodID;
	
	/**
	 * Constructor.
	 * 
	 * @param startingPerson the person starting the mission.
	 * @throws MissionException if problem constructing mission.
	 */
	public Exploration(Person startingPerson) {

		// Use RoverMission constructor.
		super(DEFAULT_DESCRIPTION, missionType, startingPerson, RoverMission.MIN_GOING_MEMBERS);

		// Check if it has a vehicle 
//		if (!hasVehicle()) {
//			endMission(Mission.NO_AVAILABLE_VEHICLES);
//		}
		
		Settlement s = startingPerson.getSettlement();

		if (s != null & !isDone()) {

			// Set mission capacity.
			if (hasVehicle())
				setMissionCapacity(getRover().getCrewCapacity());
			int availableSuitNum = Mission.getNumberAvailableEVASuitsAtSettlement(s);
			if (availableSuitNum < getMissionCapacity())
				setMissionCapacity(availableSuitNum);

			// Initialize data members.
			setStartingSettlement(s);
			exploredSites = new ArrayList<ExploredLocation>(NUM_SITES);
			explorationSiteCompletion = new HashMap<String, Double>(NUM_SITES);

			// Recruit additional members to mission.
			recruitMembersForMission(startingPerson);

			// Determine exploration sites
			try {
				if (hasVehicle()) {
					int skill = startingPerson.getSkillManager().getEffectiveSkillLevel(SkillType.AREOLOGY);
					determineExplorationSites(getVehicle().getRange(missionType),
							getTotalTripTimeLimit(getRover(), getPeopleNumber(), true), NUM_SITES, skill);
					if (explorationSiteCompletion.size() == 0) {
						addMissionStatus(MissionStatus.NO_EXPLORATION_SITES);
						endMission();
					}
				}
			} catch (Exception e) {
				addMissionStatus(MissionStatus.NO_EXPLORATION_SITES);
				endMission();
			}

			// Add home settlement
			addNavpoint(new NavPoint(getStartingSettlement().getCoordinates(), s, s.getName()));

			// Check if vehicle can carry enough supplies for the mission.
			if (hasVehicle() && !isVehicleLoadable()) {
				addMissionStatus(MissionStatus.VEHICLE_NOT_LOADABLE);
				endMission();
			}
		}

		if (s != null) {
			// Add exploring site phase.
			addPhase(EXPLORE_SITE);

			// Set initial mission phase.
			setPhase(VehicleMission.REVIEWING);
			setPhaseDescription(Msg.getString("Mission.phase.reviewing.description"));//, s.getName())); // $NON-NLS-1$
		}
		
		logger.finer(getStartingMember() + " had just finished creating an Exploration mission.");
	}

	/**
	 * Constructor with explicit data.
	 * 
	 * @param members            collection of mission members.
	 * @param startingSettlement the starting settlement.
	 * @param explorationSites   the sites to explore.
	 * @param rover              the rover to use.
	 * @param description        the mission's description.
	 * @throws MissionException if error constructing mission.
	 */
	public Exploration(Collection<MissionMember> members, Settlement startingSettlement,
			List<Coordinates> explorationSites, Rover rover, String description) {

		// Use RoverMission constructor.
		super(description, missionType, (MissionMember) members.toArray()[0], RoverMission.MIN_GOING_MEMBERS, rover);

		setStartingSettlement(startingSettlement);

		// Set mission capacity.
		setMissionCapacity(getRover().getCrewCapacity());
		int availableSuitNum = Mission.getNumberAvailableEVASuitsAtSettlement(startingSettlement);
		if (availableSuitNum < getMissionCapacity())
			setMissionCapacity(availableSuitNum);

		// Initialize explored sites.
		exploredSites = new ArrayList<ExploredLocation>(NUM_SITES);
		explorationSiteCompletion = new HashMap<String, Double>(NUM_SITES);

		// Set exploration navpoints.
		for (int x = 0; x < explorationSites.size(); x++) {
			String siteName = "exploration site " + (x + 1);
			addNavpoint(new NavPoint(explorationSites.get(x), siteName));
			explorationSiteCompletion.put(siteName, 0D);
		}

		// Add home navpoint.
		addNavpoint(
				new NavPoint(startingSettlement.getCoordinates(), startingSettlement, startingSettlement.getName()));

		Person person = null;
//		Robot robot = null;

		// Add mission members.
		// TODO Refactor this.
		Iterator<MissionMember> i = members.iterator();
		while (i.hasNext()) {

			MissionMember member = i.next();
			if (member instanceof Person) {
				person = (Person) member;
				person.getMind().setMission(this);
			} else if (member instanceof Robot) {
//				robot = (Robot) member;
//				robot.getBotMind().setMission(this);
			}
		}

		// Add exploring site phase.
		addPhase(EXPLORE_SITE);

		// Set initial mission phase.
		setPhase(VehicleMission.EMBARKING);
		setPhaseDescription(Msg.getString("Mission.phase.embarking.description", startingSettlement.getName())); // $NON-NLS-1$

		// Check if vehicle can carry enough supplies for the mission.
		if (hasVehicle() && !isVehicleLoadable()) {
			addMissionStatus(MissionStatus.VEHICLE_NOT_LOADABLE);
			endMission();
		}
	}

	/**
	 * Checks if there are any mineral locations within rover/mission range.
	 * 
	 * @param rover          the rover to use.
	 * @param homeSettlement the starting settlement.
	 * @return true if mineral locations.
	 * @throws Exception if error determining mineral locations.
	 */
	public static boolean hasNearbyMineralLocations(Rover rover, Settlement homeSettlement) {

		double roverRange = rover.getRange(missionType);
		double tripTimeLimit = getTotalTripTimeLimit(rover, rover.getCrewCapacity(), true);
		double tripRange = getTripTimeRange(tripTimeLimit, rover.getBaseSpeed() / 1.25D);
		double range = roverRange;
		if (tripRange < range)
			range = tripRange;

		MineralMap map = surfaceFeatures.getMineralMap();
		Coordinates mineralLocation = map.findRandomMineralLocation(homeSettlement.getCoordinates(), range / 2D);
		
		boolean result = (mineralLocation != null);

		return result;
	}

	/**
	 * Checks if there are any mineral locations within rover/mission range.
	 * 
	 * @param rover          the rover to use.
	 * @param homeSettlement the starting settlement.
	 * @return true if mineral locations.
	 * @throws Exception if error determining mineral locations.
	 */
	public static Map<String, Double> getNearbyMineral(Rover rover, Settlement homeSettlement) {
		Map<String, Double> minerals = new HashMap<>();
		
		double roverRange = rover.getRange(missionType);
		double tripTimeLimit = getTotalTripTimeLimit(rover, rover.getCrewCapacity(), true);
		double tripRange = getTripTimeRange(tripTimeLimit, rover.getBaseSpeed() / 1.25D);
		double range = roverRange;
		if (tripRange < range)
			range = tripRange;

		MineralMap map = surfaceFeatures.getMineralMap();
		Coordinates mineralLocation = map.findRandomMineralLocation(homeSettlement.getCoordinates(), range / 2D);
		
		if (mineralLocation != null)
			minerals = map.getAllMineralConcentrations(mineralLocation);
		
		return minerals;
	}
	
	/**
	 * Gets the range of a trip based on its time limit and exploration sites.
	 * 
	 * @param tripTimeLimit time (millisols) limit of trip.
	 * @param averageSpeed  the average speed of the vehicle.
	 * @return range (km) limit.
	 */
	private static double getTripTimeRange(double tripTimeLimit, double averageSpeed) {
		double tripTimeTravellingLimit = tripTimeLimit - (NUM_SITES * EXPLORING_SITE_TIME);
		double millisolsInHour = MarsClock.convertSecondsToMillisols(60D * 60D);
		double averageSpeedMillisol = averageSpeed / millisolsInHour;
		return tripTimeTravellingLimit * averageSpeedMillisol;
	}

	@Override
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
					Msg.getString("Mission.phase.travelling.description", getNextNavpoint().getDescription())); // $NON-NLS-1$
		} 
		
		else if (TRAVELLING.equals(getPhase())) {
			if (getCurrentNavpoint().isSettlementAtNavpoint()) {
				setPhase(VehicleMission.DISEMBARKING);
				setPhaseDescription(Msg.getString("Mission.phase.disembarking.description",
						getCurrentNavpoint().getSettlement().getName())); // $NON-NLS-1$
			} else {
				setPhase(EXPLORE_SITE);
				setPhaseDescription(
						Msg.getString("Mission.phase.exploreSite.description", getCurrentNavpoint().getDescription())); // $NON-NLS-1$
			}
		} 
		
		else if (EXPLORE_SITE.equals(getPhase())) {
			startTravelToNextNode();
			setPhase(VehicleMission.TRAVELLING);
			setPhaseDescription(
					Msg.getString("Mission.phase.travelling.description", getNextNavpoint().getDescription())); // $NON-NLS-1$
		} 
		
//		else if (DISEMBARKING.equals(getPhase()))
//			endMission(ALL_DISEMBARKED);
		
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

	@Override
	protected void performPhase(MissionMember member) {
		super.performPhase(member);
		if (EXPLORE_SITE.equals(getPhase())) {
			exploringPhase(member);
		}
	}

	/**
	 * Ends the exploration at a site.
	 */
	public void endExplorationAtSite() {
		logger.info("Explore site phase ended due to external trigger.");
		endExploringSite = true;

		// End each member's explore site task.
		Iterator<MissionMember> i = getMembers().iterator();
		while (i.hasNext()) {
			MissionMember member = i.next();
			if (member instanceof Person) {
				Person person = (Person) member;
				Task task = person.getMind().getTaskManager().getTask();
				if (task instanceof ExploreSite) {
					((ExploreSite) task).endEVA();
				}
			}
		}
	}

	/**
	 * Performs the explore site phase of the mission.
	 * 
	 * @param member the mission member currently performing the mission
	 * @throws MissionException if problem performing phase.
	 */
	private void exploringPhase(MissionMember member) {

		// Add new explored site if just starting exploring.
		if (currentSite == null) {
			createNewExploredSite();
			explorationSiteStartTime = (MarsClock) marsClock.clone();
		}

		// Check if crew has been at site for more than one sol.
		boolean timeExpired = false;
//		MarsClock ms = (MarsClock) marsClock.clone();
		double timeDiff = MarsClock.getTimeDiff((MarsClock) marsClock.clone(), explorationSiteStartTime);
		if (timeDiff >= EXPLORING_SITE_TIME) {
			timeExpired = true;
		}

		// Update exploration site completion.
		double completion = timeDiff / EXPLORING_SITE_TIME;
		if (completion > 1D) {
			completion = 1D;
		} else if (completion < 0D) {
			completion = 0D;
		}
		explorationSiteCompletion.put(getCurrentNavpoint().getDescription(), completion);
		fireMissionUpdate(MissionEventType.SITE_EXPLORATION_EVENT, getCurrentNavpoint().getDescription());

		if (isEveryoneInRover()) {

			// Check if end exploring flag is set.
			if (endExploringSite) {
				endExploringSite = false;
				setPhaseEnded(true);
			}

			// Check if crew has been at site for more than one sol, then end this phase.
			if (timeExpired)
				setPhaseEnded(true);

			// Determine if no one can start the explore site task.
			boolean nobodyExplore = true;
			Iterator<MissionMember> j = getMembers().iterator();
			while (j.hasNext()) {
				if (ExploreSite.canExploreSite(j.next(), getRover())) {
					nobodyExplore = false;
				}
			}

			// If no one can explore the site and this is not due to it just being
			// night time, end the exploring phase.
			boolean inDarkPolarRegion = surfaceFeatures.inDarkPolarRegion(getCurrentMissionLocation());
			double sunlight = surfaceFeatures.getSolarIrradiance(getCurrentMissionLocation());
			if (nobodyExplore && ((sunlight > 0D) || inDarkPolarRegion))
				setPhaseEnded(true);

			// Anyone in the crew or a single person at the home settlement has a dangerous
			// illness, end phase.
			if (hasEmergency())
				setPhaseEnded(true);

			// Check if enough resources for remaining trip. false = not using margin.
			if (!hasEnoughResourcesForRemainingMission(false)) {
				// If not, determine an emergency destination.
				determineEmergencyDestination(member);
				setPhaseEnded(true);
			}
		} else {
			// If exploration time has expired for the site, have everyone end their
			// exploration tasks.
			if (timeExpired) {
				Iterator<MissionMember> i = getMembers().iterator();
				while (i.hasNext()) {
					MissionMember tempMember = i.next();
					if (tempMember instanceof Person) {
						Person tempPerson = (Person) tempMember;
						Task task = tempPerson.getMind().getTaskManager().getTask();
						if ((task != null) && (task instanceof ExploreSite)) {
							((ExploreSite) task).endEVA();
						}
					}
				}
			}
		}

		if (!getPhaseEnded()) {

			if (!endExploringSite && !timeExpired) {
				// TODO Refactor this.
				if (member instanceof Person) {
					Person person = (Person) member;
					// If person can explore the site, start that task.
					if (ExploreSite.canExploreSite(person, getRover())) {
						assignTask(person, new ExploreSite(person, currentSite, (Rover) getVehicle()));
					}
				}
			}
		} else {
			currentSite.setExplored(true);
			currentSite = null;
		}
	}

	/**
	 * Creates a new explored site at the current location, creates initial
	 * estimates for mineral concentrations, and adds it to the explored site list.
	 * 
	 * @throws MissionException if error creating explored site.
	 */
	private void createNewExploredSite() {
//		SurfaceFeatures surfaceFeatures = Simulation.instance().getMars().getSurfaceFeatures();
		MineralMap mineralMap = surfaceFeatures.getMineralMap();
		String[] mineralTypes = mineralMap.getMineralTypeNames();
		Map<String, Double> initialMineralEstimations = new HashMap<String, Double>(mineralTypes.length);
		for (String mineralType : mineralTypes) {
			double estimation = RandomUtil.getRandomDouble(MINERAL_ESTIMATION_CEILING * 2D)
					- MINERAL_ESTIMATION_CEILING;
			double actualConcentration = mineralMap.getMineralConcentration(mineralType, getCurrentMissionLocation());
			estimation += actualConcentration;
			if (estimation < 0D)
				estimation = 0D - estimation;
			else if (estimation > 100D)
				estimation = 100D - estimation;
			initialMineralEstimations.put(mineralType, estimation);
		}
		currentSite = surfaceFeatures.addExploredLocation(new Coordinates(getCurrentMissionLocation()),
				initialMineralEstimations, getAssociatedSettlement());
		exploredSites.add(currentSite);
	}

	@Override
	protected boolean isCapableOfMission(MissionMember member) {
		boolean result = super.isCapableOfMission(member);

		if (result) {
			boolean atStartingSettlement = false;
			if (member.isInSettlement()) {
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
		result += getEstimatedRemainingExplorationSiteTime();
		return result;
	}

	/**
	 * Gets the estimated time remaining for exploration sites in the mission.
	 * 
	 * @return time (millisols)
	 * @throws MissionException if error estimating time.
	 */
	private double getEstimatedRemainingExplorationSiteTime() {
		double result = 0D;

		// Add estimated remaining exploration time at current site if still there.
		if (EXPLORE_SITE.equals(getPhase())) {
//			MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();
			double timeSpentAtExplorationSite = MarsClock.getTimeDiff(marsClock, explorationSiteStartTime);
			double remainingTime = EXPLORING_SITE_TIME - timeSpentAtExplorationSite;
			if (remainingTime > 0D)
				result += remainingTime;
		}

		// Add estimated exploration time at sites that haven't been visited yet.
		int remainingExplorationSites = getNumExplorationSites() - getNumExplorationSitesVisited();
		result += EXPLORING_SITE_TIME * remainingExplorationSites;

		return result;
	}

	@Override
	public Map<Integer, Number> getResourcesNeededForRemainingMission(boolean useBuffer) {
		Map<Integer, Number> result = super.getResourcesNeededForRemainingMission(useBuffer);

		double explorationSitesTime = getEstimatedRemainingExplorationSiteTime();
		double timeSols = explorationSitesTime / 1000D;

		int crewNum = getPeopleNumber();

		// Determine life support supplies needed for trip.
		double oxygenAmount = PhysicalCondition.getOxygenConsumptionRate()// * Mission.OXYGEN_MARGIN
				* timeSols * crewNum;
		if (result.containsKey(oxygenID))
			oxygenAmount += (Double) result.get(oxygenID);
		result.put(oxygenID, oxygenAmount);

		double waterAmount = PhysicalCondition.getWaterConsumptionRate()// * Mission.WATER_MARGIN
				* timeSols * crewNum;
		if (result.containsKey(waterID))
			waterAmount += (Double) result.get(waterID);
		result.put(waterID, waterAmount);

		double foodAmount = PhysicalCondition.getFoodConsumptionRate()// * Mission.FOOD_MARGIN
				* timeSols * crewNum;
		if (result.containsKey(foodID))
			foodAmount += (Double) result.get(foodID);
		result.put(foodID, foodAmount);

		return result;
	}

	@Override
	public Settlement getAssociatedSettlement() {
		return getStartingSettlement();
	}

	@Override
	protected int compareVehicles(Vehicle firstVehicle, Vehicle secondVehicle) {
		int result = super.compareVehicles(firstVehicle, secondVehicle);

		// Check of one rover has a research lab and the other one doesn't.
		if ((result == 0) && (isUsableVehicle(firstVehicle)) && (isUsableVehicle(secondVehicle))) {
			boolean firstLab = ((Rover) firstVehicle).hasLab();
			boolean secondLab = ((Rover) secondVehicle).hasLab();
			if (firstLab && !secondLab)
				result = 1;
			else if (!firstLab && secondLab)
				result = -1;
		}

		return result;
	}

	/**
	 * Gets the estimated time spent at all exploration sites.
	 * 
	 * @return time (millisols)
	 */
	protected double getEstimatedTimeAtExplorationSites() {
		return EXPLORING_SITE_TIME * getNumExplorationSites();
	}

	/**
	 * Gets the total number of exploration sites for this mission.
	 * 
	 * @return number of sites.
	 */
	public final int getNumExplorationSites() {
		return getNumberOfNavpoints() - 2;
	}

	/**
	 * Gets the number of exploration sites that have been currently visited by the
	 * mission.
	 * 
	 * @return number of sites.
	 */
	public final int getNumExplorationSitesVisited() {
		int result = getCurrentNavpointIndex();
		if (result == (getNumberOfNavpoints() - 1))
			result -= 1;
		return result;
	}

	@Override
	public Map<Integer, Integer> getEquipmentNeededForRemainingMission(boolean useBuffer) {
		if (equipmentNeededCache != null)
			return equipmentNeededCache;
		else {
			Map<Integer, Integer> result = new HashMap<>();

			// Include required number of specimen containers.
			result.put(EquipmentType.convertName2ID(SpecimenBox.TYPE), REQUIRED_SPECIMEN_CONTAINERS);

			equipmentNeededCache = result;
			return result;
		}
	}

	/**
	 * Gets the time limit of the trip based on life support capacity.
	 * 
	 * @param useBuffer use time buffer in estimation if true.
	 * @return time (millisols) limit.
	 * @throws MissionException if error determining time limit.
	 */
	public static double getTotalTripTimeLimit(Rover rover, int memberNum, boolean useBuffer) {

		Inventory vInv = rover.getInventory();

		double timeLimit = Double.MAX_VALUE;

		// Check food capacity as time limit.
		double foodConsumptionRate = personConfig.getFoodConsumptionRate();
		double foodCapacity = vInv.getAmountResourceCapacity(foodID, false);
		double foodTimeLimit = foodCapacity / (foodConsumptionRate * memberNum);
		if (foodTimeLimit < timeLimit)
			timeLimit = foodTimeLimit;

		// Check water capacity as time limit.
		double waterConsumptionRate = personConfig.getWaterConsumptionRate();
		double waterCapacity = vInv.getAmountResourceCapacity(waterID, false);
		double waterTimeLimit = waterCapacity / (waterConsumptionRate * memberNum);
		if (waterTimeLimit < timeLimit)
			timeLimit = waterTimeLimit;

		// Check oxygen capacity as time limit.
		double oxygenConsumptionRate = personConfig.getNominalO2ConsumptionRate();
		double oxygenCapacity = vInv.getAmountResourceCapacity(oxygenID, false);
		double oxygenTimeLimit = oxygenCapacity / (oxygenConsumptionRate * memberNum);
		if (oxygenTimeLimit < timeLimit)
			timeLimit = oxygenTimeLimit;

		// Convert timeLimit into millisols and use error margin.
		timeLimit = (timeLimit * 1000D);
		if (useBuffer)
			timeLimit /= Vehicle.getLifeSupportRangeErrorMargin();

		return timeLimit;
	}

	/**
	 * Determine the locations of the exploration sites.
	 * 
	 * @param roverRange    the rover's driving range
	 * @param numSites      the number of exploration sites
	 * @param areologySkill the skill level in areology for the areologist starting
	 *                      the mission.
	 * @throws MissionException if exploration sites can not be determined.
	 */
	private void determineExplorationSites(double roverRange, double tripTimeLimit, int numSites, int areologySkill) {

		List<Coordinates> unorderedSites = new ArrayList<Coordinates>();

		// Determining the actual traveling range.
		double range = roverRange;
		double timeRange = getTripTimeRange(tripTimeLimit);
		if (timeRange < range) {
			range = timeRange;
		}
		
		// Get the current location.
		Coordinates startingLocation = getCurrentMissionLocation();

		// Determine the first exploration site.
		Coordinates newLocation = determineFirstExplorationSite((range / 2D), areologySkill);
		if (newLocation != null) {
			unorderedSites.add(newLocation);
		} else
			throw new IllegalStateException(getPhase() + " : Could not determine first exploration site.");

		double siteDistance = Coordinates.computeDistance(startingLocation, newLocation);
		Coordinates currentLocation = newLocation;

		// Determine remaining exploration sites.
		double remainingRange = (range / 2D) - siteDistance;
		for (int x = 1; x < numSites; x++) {
			if (remainingRange > 1D) {
				Direction direction = new Direction(RandomUtil.getRandomDouble(2D * Math.PI));
				siteDistance = RandomUtil.getRandomDouble(remainingRange);
				newLocation = currentLocation.getNewLocation(direction, siteDistance);
				unorderedSites.add(newLocation);
				currentLocation = newLocation;
				remainingRange -= siteDistance;
			}
		}

		List<Coordinates> sites = null;

		if (unorderedSites.size() > 1) {
			double unorderedSitesTotalDistance = getTotalDistance(startingLocation, unorderedSites);

			// Try to reorder sites for shortest distance.
			List<Coordinates> unorderedSites2 = new ArrayList<Coordinates>(unorderedSites);
			List<Coordinates> orderedSites = new ArrayList<Coordinates>(unorderedSites2.size());
			currentLocation = startingLocation;
			while (unorderedSites2.size() > 0) {
				Coordinates shortest = unorderedSites2.get(0);
				double shortestDistance = Coordinates.computeDistance(currentLocation, shortest);
				Iterator<Coordinates> i = unorderedSites2.iterator();
				while (i.hasNext()) {
					Coordinates site = i.next();
					double distance = Coordinates.computeDistance(currentLocation, site);
					if (distance < shortestDistance) {
						shortest = site;
						shortestDistance = distance;
					}
				}

				unorderedSites2.remove(shortest);
				orderedSites.add(shortest);
				currentLocation = shortest;
			}

			double orderedSitesTotalDistance = getTotalDistance(startingLocation, orderedSites);

			sites = unorderedSites;
			if (orderedSitesTotalDistance < unorderedSitesTotalDistance) {
				sites = orderedSites;
			} else {
				sites = unorderedSites;
			}
		} else {
			sites = unorderedSites;
			// double totalDistance = getTotalDistance(startingLocation, unorderedSites);
		}

		int explorationSiteNum = 1;
		Iterator<Coordinates> j = sites.iterator();
		while (j.hasNext()) {
			Coordinates site = j.next();
			String siteName = "exploration site " + explorationSiteNum;
			addNavpoint(new NavPoint(site, siteName));
			explorationSiteCompletion.put(siteName, 0D);
			explorationSiteNum++;
		}
	}

	private double getTotalDistance(Coordinates startingLoc, List<Coordinates> sites) {
		double result = 0D;

		Coordinates currentLoc = startingLoc;
		Iterator<Coordinates> i = sites.iterator();
		while (i.hasNext()) {
			Coordinates site = i.next();
			result += currentLoc.getDistance(site);
			currentLoc = site;
		}

		// Add return trip to starting loc.
		result += currentLoc.getDistance(startingLoc);

		return result;
	}

	/**
	 * Determine the first exploration site.
	 * 
	 * @param range         the range (km) for site.
	 * @param areologySkill the skill level in areology of the areologist starting
	 *                      the mission.
	 * @return first exploration site or null if none.
	 * @throws MissionException if error determining site.
	 */
	private Coordinates determineFirstExplorationSite(double range, int areologySkill) {
		Coordinates result = null;

		Coordinates startingLocation = getCurrentMissionLocation();
		MineralMap map = surfaceFeatures.getMineralMap();
		Coordinates randomLocation = map.findRandomMineralLocation(startingLocation, range);
		if (randomLocation != null) {
			Direction direction = new Direction(RandomUtil.getRandomDouble(2D * Math.PI));
			if (areologySkill <= 0) {
				areologySkill = 1;
			}
			double distance = RandomUtil.getRandomDouble(500D / areologySkill);
			result = randomLocation.getNewLocation(direction, distance);
			double distanceFromStart = Coordinates.computeDistance(startingLocation, result);
			if (distanceFromStart > range) {
				Direction direction2 = startingLocation.getDirectionToPoint(result);
				result = startingLocation.getNewLocation(direction2, range);
			}
		} else {
			// Use random direction and distance for first location
			// if no minerals found within range.
			Direction direction = new Direction(RandomUtil.getRandomDouble(2D * Math.PI));
			double distance = RandomUtil.getRandomDouble(range);
			result = startingLocation.getNewLocation(direction, distance);
		}

		return result;
	}

	/**
	 * Gets the range of a trip based on its time limit and exploration sites.
	 * 
	 * @param tripTimeLimit time (millisols) limit of trip.
	 * @return range (km) limit.
	 */
	private double getTripTimeRange(double tripTimeLimit) {
		double timeAtSites = getEstimatedTimeAtExplorationSites();
		double tripTimeTravellingLimit = tripTimeLimit - timeAtSites;
		double averageSpeed = getAverageVehicleSpeedForOperators();
		double millisolsInHour = MarsClock.MILLISOLS_PER_HOUR;
		double averageSpeedMillisol = averageSpeed / millisolsInHour;
		return tripTimeTravellingLimit * averageSpeedMillisol;
	}

	/**
	 * Gets a list of sites explored by the mission so far.
	 * 
	 * @return list of explored sites.
	 */
	public List<ExploredLocation> getExploredSites() {
		return exploredSites;
	}

	/**
	 * Gets a map of exploration site names and their level of completion.
	 * 
	 * @return map of site names and completion level (0.0 - 1.0).
	 */
	public Map<String, Double> getExplorationSiteCompletion() {
		return new HashMap<String, Double>(explorationSiteCompletion);
	}

	/**
	 * Gets the estimated total mineral value of a mining site.
	 * 
	 * @param site       the mining site.
	 * @param settlement the settlement valuing the minerals.
	 * @return estimated value of the minerals at the site (VP).
	 * @throws MissionException if error determining the value.
	 */
	public static double getTotalMineralValue(Settlement settlement, Map<String, Double> minerals) {

		double result = 0D;		

		for (String mineralType : minerals.keySet()) {
			int mineralResource = ResourceUtil.findIDbyAmountResourceName(mineralType);
			double mineralValue = settlement.getGoodsManager().getGoodValuePerItem(mineralResource);
			double concentration = minerals.get(mineralType);
			double mineralAmount = (concentration / 100D) * Mining.MINERAL_BASE_AMOUNT;
			result += mineralValue * mineralAmount;
		}

		return result;
	}
	
	@Override
	protected Map<Integer, Number> getPartsNeededForTrip(double distance) {
		// Load the standard parts from VehicleMission.
		Map<Integer, Number> result = super.getPartsNeededForTrip(distance); // new HashMap<>();

		// Determine repair parts for EVA Suits.
		double evaTime = getEstimatedRemainingExplorationSiteTime();
		double numberAccidents = evaTime * getPeopleNumber() * EVAOperation.BASE_ACCIDENT_CHANCE;

		// Assume the average number malfunctions per accident is 1.5.
		double numberMalfunctions = numberAccidents * VehicleMission.AVERAGE_EVA_MALFUNCTION;

		// Get temporary EVA suit.
		EVASuit suit = (EVASuit) EquipmentFactory.createEquipment(EVASuit.class, new Coordinates(0, 0), true);

		// Determine needed repair parts for EVA suits.
		Map<Integer, Double> parts = suit.getMalfunctionManager().getRepairPartProbabilities();
		Iterator<Integer> i = parts.keySet().iterator();
		while (i.hasNext()) {
			Integer part = i.next();
			String name = ItemResourceUtil.findItemResourceName(part);
			for (String n : EVASuit.getParts()) {
				if (n.equalsIgnoreCase(name)) {
					int number = (int) Math.round(parts.get(part) * numberMalfunctions);
					if (number > 0) {
						if (result.containsKey(part))
							number += result.get(part).intValue();
						result.put(part, number);
					}
				}
			}
		}

		return result;
	}

	@Override
	public void destroy() {
		super.destroy();

		if (explorationSiteCompletion != null)
			explorationSiteCompletion.clear();
		explorationSiteCompletion = null;
		explorationSiteStartTime = null;
		currentSite = null;
		if (exploredSites != null)
			exploredSites.clear();
		exploredSites = null;
	}
}