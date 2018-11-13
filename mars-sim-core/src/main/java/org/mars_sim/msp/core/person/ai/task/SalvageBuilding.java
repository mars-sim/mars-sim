/**
 * Mars Simulation Project
 * SalvageBuilding.java
 * @version 3.08 2015-06-17
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
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.mars.Mars;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.person.NaturalAttributeType;
import org.mars_sim.msp.core.person.NaturalAttributeManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.mission.BuildingSalvageMission;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;

import org.mars_sim.msp.core.structure.Airlock;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.construction.ConstructionSite;
import org.mars_sim.msp.core.structure.construction.ConstructionStage;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.GroundVehicle;
import org.mars_sim.msp.core.vehicle.LightUtilityVehicle;

/**
 * Task for salvaging a building construction site stage.
 */
public class SalvageBuilding
extends EVAOperation
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(SalvageBuilding.class.getName());

	/** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.salvageBuilding"); //$NON-NLS-1$

    /** Task phases. */
    private static final TaskPhase SALVAGE = new TaskPhase(Msg.getString(
            "Task.phase.salvage")); //$NON-NLS-1$

	/** The base chance of an accident while operating LUV per millisol. */
	public static final double BASE_LUV_ACCIDENT_CHANCE = .001;

	// Data members.
	private boolean operatingLUV;
	
	private ConstructionStage stage;
	private ConstructionSite site;
	private LightUtilityVehicle luv;
	
	private List<GroundVehicle> vehicles;
	


	/**
     * Constructor.
     * @param person the person performing the task.
     */
    public SalvageBuilding(Person person) {
        // Use EVAOperation parent constructor.
        super(NAME, person, true, RandomUtil.getRandomDouble(50D) + 10D);

        BuildingSalvageMission mission = getMissionNeedingAssistance();
        if ((mission != null) && canSalvage(person)) {

            // Initialize data members.
            this.stage = mission.getConstructionStage();
            this.site = mission.getConstructionSite();
            this.vehicles = mission.getConstructionVehicles();

            // Determine location for salvage site.
            Point2D salvageSiteLoc = determineSalvageLocation();
            setOutsideSiteLocation(salvageSiteLoc.getX(), salvageSiteLoc.getY());

            // Add task phase
            addPhase(SALVAGE);

            logger.fine(person.getName() + " has started the SalvageBuilding task.");
        }
        else {
            endTask();
        }
    }
    
//    public SalvageBuilding(Robot robot) {
//        // Use EVAOperation parent constructor.
//        super(NAME, robot, true, RandomUtil.getRandomDouble(50D) + 10D);
//
//        BuildingSalvageMission mission = getMissionNeedingAssistance();
//        if ((mission != null) && canSalvage(robot)) {
//
//            // Initialize data members.
//            this.stage = mission.getConstructionStage();
//            this.site = mission.getConstructionSite();
//            this.vehicles = mission.getConstructionVehicles();
//
//            // Determine location for salvage site.
//            Point2D salvageSiteLoc = determineSalvageLocation();
//            setOutsideSiteLocation(salvageSiteLoc.getX(), salvageSiteLoc.getY());
//
//            // Add task phase
//            addPhase(SALVAGE);
//
//            logger.fine(robot.getName() + " has started the SalvageBuilding task.");
//        }
//        else {
//            endTask();
//        }
//    }
    
	/**
	 * Constructor.
	 * @param person the person performing the task.
	 * @param stage the construction site salvage stage.
	 * @param vehicles the construction vehicles.
	 */
	public SalvageBuilding(Person person, ConstructionStage stage,
			ConstructionSite site, List<GroundVehicle> vehicles) {
		// Use EVAOperation parent constructor.
        super(NAME, person, true, RandomUtil.getRandomDouble(50D) + 10D);

        // Initialize data members.
        this.stage = stage;
        this.site = site;
        this.vehicles = vehicles;

        init();

        logger.fine(person.getName() + " has started the SalvageBuilding task.");
    }
//	public SalvageBuilding(Robot robot, ConstructionStage stage,
//			ConstructionSite site, List<GroundVehicle> vehicles) {
//		// Use EVAOperation parent constructor.
//        super(NAME, robot, true, RandomUtil.getRandomDouble(50D) + 10D);
//
//	    // Initialize data members.
//	    this.stage = stage;
//	    this.site = site;
//	    this.vehicles = vehicles;
//
//        init();
//
//        logger.fine(robot.getName() + " has started the SalvageBuilding task.");
//    }

	public void init() {

        // Determine location for salvage site.
        Point2D salvageSiteLoc = determineSalvageLocation();
        setOutsideSiteLocation(salvageSiteLoc.getX(), salvageSiteLoc.getY());

        // Add task phase
        addPhase(SALVAGE);
	}

    /**
     * Checks if a given person can work on salvaging a building at this time.
     * @param person the person.
     * @return true if person can salvage.
     */
    public static boolean canSalvage(Person person) {

        // Check if person can exit the settlement airlock.
        Airlock airlock = getWalkableAvailableAirlock(person);
        if (airlock != null) {
            if(!ExitAirlock.canExitAirlock(person, airlock))
            	return false;
        }

		// Check if it is night time.
		SurfaceFeatures surface = Simulation.instance().getMars().getSurfaceFeatures();
		if (surface.getSolarIrradiance(person.getCoordinates()) == 0D) {
			if (!surface.inDarkPolarRegion(person.getCoordinates())) {
				return false;
			}
		}

        // Check if person's medical condition will not allow task.
        if (person.getPerformanceRating() < .5D)
        	return false;

        return true;
    }

//    public static boolean canSalvage(Robot robot) {
//
//        // Check if robot can exit the settlement airlock.
//        Airlock airlock = getWalkableAvailableAirlock(robot);
//        if (airlock != null) {
//            if(!ExitAirlock.canExitAirlock(robot, airlock))
//            	return false;
//        }
//
//        Mars mars = Simulation.instance().getMars();
//        if (mars.getSurfaceFeatures().getSolarIrradiance(robot.getCoordinates()) == 0D) {
//            logger.fine(robot.getName() + " end salvaging building : night time");
//            if (!mars.getSurfaceFeatures().inDarkPolarRegion(robot.getCoordinates()))
//                return false;
//        }
//
//        // Check if person's medical condition will not allow task.
//        if (robot.getPerformanceRating() < .5D)
//        	return false;
//
//        return true;
//    }

    /**
     * Gets a random building salvage mission that needs assistance.
     * @return salvage mission or null if none found.
     */
    private BuildingSalvageMission getMissionNeedingAssistance() {

        BuildingSalvageMission result = null;

        List<BuildingSalvageMission> salvageMissions = null;

        if (person != null)
        	salvageMissions = getAllMissionsNeedingAssistance(
                person.getSettlement());
        else if (robot != null)
        	salvageMissions = getAllMissionsNeedingAssistance(
                robot.getSettlement());

        if (salvageMissions.size() > 0) {
            int index = RandomUtil.getRandomInt(salvageMissions.size() - 1);
            result = (BuildingSalvageMission) salvageMissions.get(index);
        }

        return result;
    }

    /**
     * Gets a list of all building salvage missions that need assistance at a settlement.
     * @param settlement the settlement.
     * @return list of building salvage missions.
     */
    public static List<BuildingSalvageMission> getAllMissionsNeedingAssistance(
            Settlement settlement) {

        List<BuildingSalvageMission> result = new ArrayList<BuildingSalvageMission>();

        MissionManager manager = Simulation.instance().getMissionManager();
        Iterator<Mission> i = manager.getMissionsForSettlement(settlement).iterator();
        while (i.hasNext()) {
            Mission mission = (Mission) i.next();
            if (mission instanceof BuildingSalvageMission) {
                result.add((BuildingSalvageMission) mission);
            }
        }

        return result;
    }

    /**
     * Determine location to go to at salvage site.
     * @return location.
     */
    private Point2D determineSalvageLocation() {

        Point2D.Double relativeLocSite = LocalAreaUtil.getRandomInteriorLocation(site, false);
        Point2D.Double settlementLocSite = LocalAreaUtil.getLocalRelativeLocation(relativeLocSite.getX(),
                relativeLocSite.getY(), site);

        return settlementLocSite;
    }

    @Override
    protected void addExperience(double time) {
    	SkillManager manager = null;
//    	if (person != null)
    		manager = person.getMind().getSkillManager();
//		else if (robot != null)
//			manager = robot.getBotMind().getSkillManager();

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
        manager.addExperience(SkillType.EVA_OPERATIONS, evaExperience);

        // If phase is salvage, add experience to construction skill.
        if (SALVAGE.equals(getPhase())) {
            // 1 base experience point per 10 millisols of construction time spent.
            // Experience points adjusted by person's "Experience Aptitude" attribute.
            double constructionExperience = time / 10D;
            constructionExperience += constructionExperience * experienceAptitudeModifier;
            manager.addExperience(SkillType.CONSTRUCTION, constructionExperience);

            // If person is driving the light utility vehicle, add experience to driving skill.
            // 1 base experience point per 10 millisols of mining time spent.
            // Experience points adjusted by person's "Experience Aptitude" attribute.
            if (operatingLUV) {
                double drivingExperience = time / 10D;
                drivingExperience += drivingExperience * experienceAptitudeModifier;
                manager.addExperience(SkillType.DRIVING, drivingExperience);
            }
        }
    }

    @Override
    public List<SkillType> getAssociatedSkills() {
        List<SkillType> results = new ArrayList<SkillType>(2);
        results.add(SkillType.EVA_OPERATIONS);
        results.add(SkillType.CONSTRUCTION);
        return results;
    }

    @Override
    public int getEffectiveSkillLevel() {

        SkillManager manager = null;
//        if (person != null)
        	manager = person.getMind().getSkillManager();
//		else if (robot != null)
//			manager = robot.getBotMind().getSkillManager();

        int EVAOperationsSkill = manager.getEffectiveSkillLevel(SkillType.EVA_OPERATIONS);
        int constructionSkill = manager.getEffectiveSkillLevel(SkillType.CONSTRUCTION);
        return (int) Math.round((double)(EVAOperationsSkill + constructionSkill) / 2D);
    }

    @Override
    protected TaskPhase getOutsideSitePhase() {
        return SALVAGE;
    }

    @Override
    protected double performMappedPhase(double time) {

        time = super.performMappedPhase(time);

        if (getPhase() == null) {
            throw new IllegalArgumentException("Task phase is null");
        }
        else if (SALVAGE.equals(getPhase())) {
            return salvage(time);
        }
        else {
            return time;
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
            	skill = person.getMind().getSkillManager().getEffectiveSkillLevel(SkillType.EVA_OPERATIONS);
//    		else if (robot != null)
//    			skill = robot.getBotMind().getSkillManager().getEffectiveSkillLevel(SkillType.EVA_OPERATIONS);

            if (skill <= 3) {
                chance *= (4 - skill);
            }
            else {
                chance /= (skill - 2);
            }

            // Modify based on the LUV's wear condition.
            chance *= luv.getMalfunctionManager().getWearConditionAccidentModifier();

            if (RandomUtil.lessThanRandPercent(chance * time)) {
//    			if (person != null) {
 //   				logger.info("[" + person.getLocationTag().getShortLocationName() +  "] " +person.getName() + " ran into an accident while salvage a building");
                    luv.getMalfunctionManager().createASeriesOfMalfunctions(person);
//    			}
//    			else if (robot != null) {
//    				logger.info("[" + robot.getLocationTag().getShortLocationName() +  "] " + robot.getName() + " ran into an accident while salvage a building");
//    				luv.getMalfunctionManager().createASeriesOfMalfunctions(robot);
//    			}
            }
        }
    }

    @Override
    protected boolean shouldEndEVAOperation() {
        boolean result = super.shouldEndEVAOperation();

        // If operating LUV, check if LUV has malfunction.
        if (operatingLUV && luv.getMalfunctionManager().hasMalfunction())
            result = true;

        return result;
    }

    /**
     * Perform the salvage phase of the task.
     * @param time amount (millisols) of time to perform the phase.
     * @return time (millisols) remaining after performing the phase.
     */
    private double salvage(double time) {

        // 2015-05-29 Check for radiation exposure during the EVA operation.
        // 2015-05-29 Check for radiation exposure during the EVA operation.
        if (isRadiationDetected(time)){
            setPhase(WALK_BACK_INSIDE);
            return time;
        }

        // Check for an accident during the EVA operation.
        checkForAccident(time);

        if (shouldEndEVAOperation() || stage.isComplete() || addTimeOnSite(time)) {
            // End operating light utility vehicle.
            if (person != null) {
            	if ((luv != null) && luv.getInventory().containsUnit(person)) {
                    returnVehicle();
            	}
            }
			else if (robot != null) {
				if ((luv != null) && luv.getInventory().containsUnit(robot)) {
					returnVehicle();
				}
			}

            setPhase(WALK_BACK_INSIDE);
            return time;
        }

        // Operate light utility vehicle if no one else is operating it.
        if (!operatingLUV) {
            obtainVehicle();
        }

        // Determine effective work time based on "Construction" and "EVA Operations" skills.
        double workTime = time;
        int skill = getEffectiveSkillLevel();
        if (skill == 0) {
            workTime /= 2;
        }
        else if (skill > 1) {
            workTime += workTime * (.2D * skill);
        }

        // Work on salvage.
        stage.addWorkTime(workTime);

        // Add experience points
        addExperience(time);

        // Check if an accident happens during salvage.
        checkForAccident(time);

        return 0D;
    }

    /**
     * Obtains a construction vehicle from the settlement if possible.
     */
    private void obtainVehicle() {
        Iterator<GroundVehicle> i = vehicles.iterator();
        while (i.hasNext() && (luv == null)) {
            GroundVehicle vehicle = i.next();
            if (!vehicle.getMalfunctionManager().hasMalfunction()) {
                if (vehicle instanceof LightUtilityVehicle) {
                    LightUtilityVehicle tempLuv = (LightUtilityVehicle) vehicle;
                    if (tempLuv.getOperator() == null) {

//	                   	 if (person != null) {
	                		 tempLuv.getInventory().storeUnit(person);
	                         tempLuv.setOperator(person);
//	                	 }
//
//	                     else if (robot != null) {
//	                        //tempLuv.getInventory().storeUnit(robot);
//	                        //tempLuv.setOperator(robot);
//	                     }

                        luv = tempLuv;
                        operatingLUV = true;

                        // Place light utility vehicles at random location in construction site.
                        Point2D.Double relativeLocSite = LocalAreaUtil.getRandomInteriorLocation(site);
                        Point2D.Double settlementLocSite = LocalAreaUtil.getLocalRelativeLocation(
                                relativeLocSite.getX(), relativeLocSite.getY(), site);
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
     */
    private void returnVehicle() {
//    	if (person != null)
            luv.getInventory().retrieveUnit(person);
//		else if (robot != null)
//	        luv.getInventory().retrieveUnit(robot);

        luv.setOperator(null);
        operatingLUV = false;
    }

    /**
     * Gets the construction stage that is being worked on.
     * @return construction stage.
     */
    public ConstructionStage getConstructionStage() {
        return stage;
    }

    @Override
    public void destroy() {
        super.destroy();

        stage = null;
        if (vehicles != null) {
            vehicles.clear();
        }
        vehicles = null;
        luv = null;
    }
}