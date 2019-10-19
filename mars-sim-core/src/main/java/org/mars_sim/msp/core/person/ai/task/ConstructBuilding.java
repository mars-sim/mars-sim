/**
 * Mars Simulation Project
 * ConstructBuilding.java
 * @version 3.1.0 2017-09-13
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeManager;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.mission.BuildingConstructionMission;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.structure.Airlock;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.construction.ConstructionSite;
import org.mars_sim.msp.core.structure.construction.ConstructionStage;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.GroundVehicle;
import org.mars_sim.msp.core.vehicle.LightUtilityVehicle;

/**
 * Task for constructing a building construction site stage.
 */
public class ConstructBuilding extends EVAOperation implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(ConstructBuilding.class.getName());

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.constructBuilding"); //$NON-NLS-1$

	/** Task phases. */
	private static final TaskPhase CONSTRUCTION = new TaskPhase(Msg.getString("Task.phase.construction")); //$NON-NLS-1$

	// The base chance of an accident while operating LUV per millisol.
	public static final double BASE_LUV_ACCIDENT_CHANCE = .001;

	// Data members.
	private boolean operatingLUV;

	private ConstructionStage stage;
	private ConstructionSite site;
	private LightUtilityVehicle luv;

	private List<GroundVehicle> vehicles;

	/**
	 * Constructor.
	 * 
	 * @param person the person performing the task.
	 */
	public ConstructBuilding(Person person) {
		// Use EVAOperation parent constructor.
		super(NAME, person, true, RandomUtil.getRandomDouble(5D) + 100D);

		BuildingConstructionMission mission = getMissionNeedingAssistance();
		if ((mission != null) && canConstruct(person, mission.getConstructionSite())) {

			// Initialize data members.
			this.stage = mission.getConstructionStage();
			this.site = mission.getConstructionSite();
			this.vehicles = mission.getConstructionVehicles();

			// Determine location for construction site.
			Point2D constructionSiteLoc = determineConstructionLocation();
			setOutsideSiteLocation(constructionSiteLoc.getX(), constructionSiteLoc.getY());

			// Add task phase
			addPhase(CONSTRUCTION);
		} else {
			endTask();
		}
	}

//    public ConstructBuilding(Robot robot) {
//        // Use EVAOperation parent constructor.
//        super(NAME, robot, true, RandomUtil.getRandomDouble(5D) + 100D);
//
//        BuildingConstructionMission mission = getMissionNeedingAssistance();
//        if ((mission != null) && canConstruct(robot, mission.getConstructionSite())) {
//
//            // Initialize data members.
//            this.stage = mission.getConstructionStage();
//            this.site = mission.getConstructionSite();
//            this.vehicles = mission.getConstructionVehicles();
//
//            // Determine location for construction site.
//            Point2D constructionSiteLoc = determineConstructionLocation();
//            setOutsideSiteLocation(constructionSiteLoc.getX(), constructionSiteLoc.getY());
//
//            // Add task phase
//            addPhase(CONSTRUCTION);
//        }
//        else {
//            endTask();
//        }
//    }

	/**
	 * Constructor.
	 * 
	 * @param person   the person performing the task.
	 * @param stage    the construction site stage.
	 * @param vehicles the construction vehicles.
	 * @throws Exception if error constructing task.
	 */
	public ConstructBuilding(Person person, ConstructionStage stage, ConstructionSite site,
			List<GroundVehicle> vehicles) {
		// Use EVAOperation parent constructor.
		super(NAME, person, true, RandomUtil.getRandomDouble(5D) + 100D);

		// Initialize data members.
		this.stage = stage;
		this.site = site;
		this.vehicles = vehicles;

		// Determine location for construction site.
		Point2D constructionSiteLoc = determineConstructionLocation();
		setOutsideSiteLocation(constructionSiteLoc.getX(), constructionSiteLoc.getY());

		// Add task phase
		addPhase(CONSTRUCTION);
	}

//    public ConstructBuilding(Robot robot, ConstructionStage stage,
//            ConstructionSite site, List<GroundVehicle> vehicles) {
//        // Use EVAOperation parent constructor.
//        super(NAME, robot, true, RandomUtil.getRandomDouble(5D) + 100D);
//
//        // Initialize data members.
//        this.stage = stage;
//        this.site = site;
//        this.vehicles = vehicles;
//
//        // Determine location for construction site.
//        Point2D constructionSiteLoc = determineConstructionLocation();
//        setOutsideSiteLocation(constructionSiteLoc.getX(), constructionSiteLoc.getY());
//
//        // Add task phase
//        addPhase(CONSTRUCTION);
//    }
	/**
	 * Checks if a given person can work on construction at this time.
	 * 
	 * @param person the person.
	 * @return true if person can construct.
	 */
	public static boolean canConstruct(Person person, ConstructionSite site) {

		// Check if person can exit the settlement airlock.
		Airlock airlock = getClosestWalkableAvailableAirlock(person, site.getXLocation(), site.getYLocation());
		if (airlock != null) {
			if (!ExitAirlock.canExitAirlock(person, airlock))
				return false;
		}

		if (EVAOperation.isGettingDark(person))
			return false;
		
//		if (surface.getSolarIrradiance(person.getCoordinates()) == 0D) {
//			logger.fine(person.getName() + " end constructing building : night time");
//			if (!surface.inDarkPolarRegion(person.getCoordinates()))
//				return false;
//		}

		// Check if person's medical condition will not allow task.
		if (person.getPerformanceRating() < .5D)
			return false;

		// Check if there is work that can be done on the construction stage.
		ConstructionStage stage = site.getCurrentConstructionStage();

		// 2015-12-30 java.lang.NullPointerException on the following statement. why?
		boolean workAvailable = false;

		// 2016-06-08 Checking stage for NullPointerException
		if (stage != null)
			workAvailable = stage.getCompletableWorkTime() > stage.getCompletedWorkTime();

		// System.out.println("stage is " + stage); // test if stage is null

		return (workAvailable);
	}

//    public static boolean canConstruct(Robot robot, ConstructionSite site) {
//
//        // Check if robot can exit the settlement airlock.
//        Airlock airlock = getClosestWalkableAvailableAirlock(robot, site.getXLocation(), site.getYLocation());
//        if (airlock != null) {
//            if(!ExitAirlock.canExitAirlock(robot, airlock))
//            	return false;
//        }
//
//
//        Mars mars = Simulation.instance().getMars();
//        if (mars.getSurfaceFeatures().getSolarIrradiance(robot.getCoordinates()) == 0D) {
//            logger.fine(robot.getName() + " should end EVA: night time.");
//            if (!mars.getSurfaceFeatures().inDarkPolarRegion(robot.getCoordinates()))
//                return false;
//        }
//
//        // Check if robot's medical condition will not allow task.
//        if (robot.getPerformanceRating() < .5D)
//        	return false;
//
//        // Check if there is work that can be done on the construction stage.
//        ConstructionStage stage = site.getCurrentConstructionStage();
//        //boolean workAvailable = stage.getCompletableWorkTime() > stage.getCompletedWorkTime();
//
//        boolean workAvailable = false;
//
//        // 2016-06-08 Checking stage for NullPointerException
//        if (stage != null)
//        	workAvailable = stage.getCompletableWorkTime() > stage.getCompletedWorkTime();
//
//        return (workAvailable);
//    }

	/**
	 * Gets a random building construction mission that needs assistance.
	 * 
	 * @return construction mission or null if none found.
	 */
	private BuildingConstructionMission getMissionNeedingAssistance() {

		BuildingConstructionMission result = null;

		List<BuildingConstructionMission> constructionMissions = null;

//        if (person != null) {
		constructionMissions = getAllMissionsNeedingAssistance(person.getAssociatedSettlement());
//        }
//        else if (robot != null) {
//        	constructionMissions = getAllMissionsNeedingAssistance(
//                robot.getAssociatedSettlement());
//        }

		if (constructionMissions.size() > 0) {
			int index = RandomUtil.getRandomInt(constructionMissions.size() - 1);
			result = (BuildingConstructionMission) constructionMissions.get(index);
		}

		return result;
	}

	/**
	 * Gets a list of all building construction missions that need assistance at a
	 * settlement.
	 * 
	 * @param settlement the settlement.
	 * @return list of building construction missions.
	 */
	public static List<BuildingConstructionMission> getAllMissionsNeedingAssistance(Settlement settlement) {

		List<BuildingConstructionMission> result = new ArrayList<BuildingConstructionMission>();

		Iterator<Mission> i = missionManager.getMissionsForSettlement(settlement).iterator();
		while (i.hasNext()) {
			Mission mission = (Mission) i.next();
			if (mission instanceof BuildingConstructionMission) {
				result.add((BuildingConstructionMission) mission);
			}
		}

		return result;
	}

	/**
	 * Determine location to go to at construction site.
	 * 
	 * @return location.
	 */
	private Point2D determineConstructionLocation() {

		Point2D.Double relativeLocSite = LocalAreaUtil.getRandomInteriorLocation(site, false);
		Point2D.Double settlementLocSite = LocalAreaUtil.getLocalRelativeLocation(relativeLocSite.getX(),
				relativeLocSite.getY(), site);

		return settlementLocSite;
	}

	@Override
	protected TaskPhase getOutsideSitePhase() {
		return CONSTRUCTION;
	}

	@Override
	protected double performMappedPhase(double time) {

		time = super.performMappedPhase(time);

		if (getPhase() == null) {
			throw new IllegalArgumentException("Task phase is null");
		} else if (CONSTRUCTION.equals(getPhase())) {
			return constructionPhase(time);
		} else {
			return time;
		}
	}

	/**
	 * Perform the construction phase of the task.
	 * 
	 * @param time amount (millisols) of time to perform the phase.
	 * @return time (millisols) remaining after performing the phase.
	 * @throws Exception
	 */
	private double constructionPhase(double time) {

		// Check for an accident during the EVA operation.
		checkForAccident(time);

		// 2015-05-29 Check for radiation exposure during the EVA operation.
		if (isRadiationDetected(time)) {
			setPhase(WALK_BACK_INSIDE);
			return time;
		}

		boolean availableWork = stage.getCompletableWorkTime() > stage.getCompletedWorkTime();

		// Check if site duration has ended or there is reason to cut the construction
		// phase short and return to the rover.
		if (shouldEndEVAOperation() || addTimeOnSite(time) || stage.isComplete() || !availableWork) {

			// End operating light utility vehicle.
			if (luv != null) {
				if ((person != null) && luv.getInventory().containsUnit(person)) {
					returnVehicle();
				}
//                if ((robot != null) && luv.getInventory().containsUnit(robot)) {
//                    returnVehicle();
//                }
			}

			setPhase(WALK_BACK_INSIDE);
			return time;
		}

		// Operate light utility vehicle if no one else is operating it.
		if (!operatingLUV) {
			obtainVehicle();
		}

		// Determine effective work time based on "Construction" and "EVA Operations"
		// skills.
		double workTime = time;
		int skill = getEffectiveSkillLevel();
		if (skill == 0) {
			workTime /= 2;
		} else if (skill > 1) {
			workTime += workTime * (.2D * skill);
		}

		// Work on construction.
		stage.addWorkTime(workTime);

		// Add experience points
		addExperience(time);

		// Check if an accident happens during construction.
		checkForAccident(time);

		return 0D;
	}

	/**
	 * Obtains a construction vehicle from the settlement if possible.
	 * 
	 * @throws Exception if error obtaining construction vehicle.
	 */
	private void obtainVehicle() {
		Iterator<GroundVehicle> i = vehicles.iterator();
		while (i.hasNext() && (luv == null)) {
			GroundVehicle vehicle = i.next();
			if (!vehicle.getMalfunctionManager().hasMalfunction()) {
				if (vehicle instanceof LightUtilityVehicle) {
					LightUtilityVehicle tempLuv = (LightUtilityVehicle) vehicle;
					if (tempLuv.getOperator() == null) {

//                    	 if (person != null) {
						tempLuv.getInventory().storeUnit(person);
						tempLuv.setOperator(person);
//                    	 }
//
//                         else if (robot != null) {
//	                        //tempLuv.getInventory().storeUnit(robot);
//	                        //tempLuv.setOperator(robot);
//	                     }

						luv = tempLuv;
						operatingLUV = true;

						// Place light utility vehicles at random location in construction site.
						Point2D.Double relativeLocSite = LocalAreaUtil.getRandomInteriorLocation(site);
						Point2D.Double settlementLocSite = LocalAreaUtil
								.getLocalRelativeLocation(relativeLocSite.getX(), relativeLocSite.getY(), site);
						luv.setParkedLocation(settlementLocSite.getX(), settlementLocSite.getY(),
								RandomUtil.getRandomDouble(360D));

						break;
					}
				}
			}
		}
	}

	/**
	 * Returns the construction vehicle used to the settlement.
	 * 
	 * @throws Exception if error returning construction vehicle.
	 */
	private void returnVehicle() {
//    	if (person != null)
		luv.getInventory().retrieveUnit(person);
//		else if (robot != null)
//	        luv.getInventory().retrieveUnit(robot);

		luv.setOperator(null);
		operatingLUV = false;
	}

	@Override
	public int getEffectiveSkillLevel() {
		SkillManager manager = null;
//    	if (person != null)
		manager = person.getSkillManager();
//		else if (robot != null)
//			manager = robot.getBotMind().getSkillManager();

		int EVAOperationsSkill = manager.getEffectiveSkillLevel(SkillType.EVA_OPERATIONS);
		int constructionSkill = manager.getEffectiveSkillLevel(SkillType.CONSTRUCTION);
		return (int) Math.round((double) (EVAOperationsSkill + constructionSkill) / 2D);
	}

	@Override
	public List<SkillType> getAssociatedSkills() {
		List<SkillType> results = new ArrayList<SkillType>(2);
		results.add(SkillType.EVA_OPERATIONS);
		results.add(SkillType.CONSTRUCTION);
		return results;
	}

	@Override
	protected void addExperience(double time) {
		SkillManager manager = null;
//    	if (person != null)
		manager = person.getSkillManager();
//		else if (robot != null)
//        	manager = robot.getBotMind().getSkillManager();

		// Add experience to "EVA Operations" skill.
		// (1 base experience point per 100 millisols of time spent)
		double evaExperience = time / 100D;

		// Experience points adjusted by person's "Experience Aptitude" attribute.
		NaturalAttributeManager nManager = null;
//        RoboticAttributeManager rManager = null;
		int experienceAptitude = 0;
//        if (person != null) {
		nManager = person.getNaturalAttributeManager();
		experienceAptitude = nManager.getAttribute(NaturalAttributeType.EXPERIENCE_APTITUDE);
//        }
//        else if (robot != null) {
//        	rManager = robot.getRoboticAttributeManager();
//            experienceAptitude = rManager.getAttribute(RoboticAttributeType.EXPERIENCE_APTITUDE);
//        }
		double experienceAptitudeModifier = (((double) experienceAptitude) - 50D) / 100D;
		evaExperience += evaExperience * experienceAptitudeModifier;
		evaExperience *= getTeachingExperienceModifier();
		manager.addExperience(SkillType.EVA_OPERATIONS, evaExperience, time);

		// If phase is construction, add experience to construction skill.
		if (CONSTRUCTION.equals(getPhase())) {
			// 1 base experience point per 10 millisols of construction time spent.
			// Experience points adjusted by person's "Experience Aptitude" attribute.
			double constructionExperience = time / 10D;
			constructionExperience += constructionExperience * experienceAptitudeModifier;
			manager.addExperience(SkillType.CONSTRUCTION, constructionExperience, time);

			// If person is driving the light utility vehicle, add experience to driving
			// skill.
			// 1 base experience point per 10 millisols of mining time spent.
			// Experience points adjusted by person's "Experience Aptitude" attribute.
			if (operatingLUV) {
				double drivingExperience = time / 10D;
				drivingExperience += drivingExperience * experienceAptitudeModifier;
				manager.addExperience(SkillType.PILOTING, drivingExperience, time);
			}
		}
	}

	@Override
	protected void checkForAccident(double time) {
		super.checkForAccident(time);

		// Check for light utility vehicle accident if operating one.
		if (operatingLUV) {
			double chance = BASE_LUV_ACCIDENT_CHANCE;

			// Driving skill modification.
			int skill = 0;
//            if (person != null)
			skill = person.getSkillManager().getEffectiveSkillLevel(SkillType.EVA_OPERATIONS);
//			else if (robot != null)
//				skill = robot.getBotMind().getSkillManager().getEffectiveSkillLevel(SkillType.EVA_OPERATIONS);

			if (skill <= 3) {
				chance *= (4 - skill);
			} else {
				chance /= (skill - 2);
			}

			// Modify based on the LUV's wear condition.
			chance *= luv.getMalfunctionManager().getWearConditionAccidentModifier();

			if (RandomUtil.lessThanRandPercent(chance * time)) {

//    			if (person != null) {
//    	            logger.info(person.getName() + " has an accident while constructing the site " + site.getName());
				luv.getMalfunctionManager().createASeriesOfMalfunctions(site.getName(), person);
//    			}
//    			else if (robot != null) {
//    				logger.info(robot.getName() + " has an accident while constructing the site " + site.getName());
//                    luv.getMalfunctionManager().createASeriesOfMalfunctions(site.getName(), robot);
//    			}

			}
		}
	}

	@Override
	protected boolean shouldEndEVAOperation() {
		boolean result = super.shouldEndEVAOperation();

		// If operating LUV, check if LUV has malfunction.
		if (operatingLUV && luv.getMalfunctionManager().hasMalfunction()) {
			result = true;
		}

		return result;
	}

	/**
	 * Gets the construction stage that is being worked on.
	 * 
	 * @return construction stage.
	 */
	public ConstructionStage getConstructionStage() {
		return stage;
	}

	@Override
	public void destroy() {
		super.destroy();

		site = null;
		stage = null;
		if (vehicles != null) {
			vehicles.clear();
		}
		vehicles = null;
		luv = null;
	}
}