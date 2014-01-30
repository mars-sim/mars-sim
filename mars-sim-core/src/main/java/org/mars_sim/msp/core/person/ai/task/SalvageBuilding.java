/**
 * Mars Simulation Project
 * SalvageBuilding.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import org.mars_sim.msp.core.Airlock;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.LocalBoundedObject;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.person.NaturalAttributeManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.Skill;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.structure.construction.ConstructionSite;
import org.mars_sim.msp.core.structure.construction.ConstructionStage;
import org.mars_sim.msp.core.vehicle.GroundVehicle;
import org.mars_sim.msp.core.vehicle.LightUtilityVehicle;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 * Task for salvaging a building construction site stage.
 */
public class SalvageBuilding extends EVAOperation implements Serializable {

    // Logger
    private static Logger logger = Logger.getLogger(SalvageBuilding.class.getName());
    
    // Task phases
    private static final String WALK_TO_SITE = "Walk to Site";
    private static final String SALVAGE = "Salvage";
    private static final String WALK_TO_AIRLOCK = "Walk to Airlock";
    
    // The base chance of an accident while operating LUV per millisol.
    public static final double BASE_LUV_ACCIDENT_CHANCE = .001;
    
    // Data members.
    private ConstructionStage stage;
    private ConstructionSite site;
    private List<GroundVehicle> vehicles;
    private Airlock airlock;
    private LightUtilityVehicle luv;
    private boolean operatingLUV;
    private double salvageXLoc;
    private double salvageYLoc;
    private double enterAirlockXLoc;
    private double enterAirlockYLoc;
    
    /**
     * Constructor
     * @param person the person performing the task.
     * @param stage the construction site salvage stage.
     * @param vehicles the construction vehicles.
     * @throws Exception if error constructing task.
     */
    public SalvageBuilding(Person person, ConstructionStage stage, 
            ConstructionSite site, List<GroundVehicle> vehicles) {
        // Use EVAOperation parent constructor.
        super("Salvage Building", person);
        
        // Initialize data members.
        this.stage = stage;
        this.site = site;
        this.vehicles = vehicles;
        
        // Get an available airlock.
        airlock = getClosestWalkableAvailableAirlock(person, site.getXLocation(), 
                site.getYLocation());
        if (airlock == null) endTask();
        
        // Determine location for salvage site.
        Point2D salvageSiteLoc = determineSalvageLocation();
        salvageXLoc = salvageSiteLoc.getX();
        salvageYLoc = salvageSiteLoc.getY();
        
        // Determine location for reentering building airlock.
        Point2D enterAirlockLoc = determineAirlockEnteringLocation();
        enterAirlockXLoc = enterAirlockLoc.getX();
        enterAirlockYLoc = enterAirlockLoc.getY();
        
        // Add task phase
        addPhase(WALK_TO_SITE);
        addPhase(SALVAGE);
        addPhase(WALK_TO_AIRLOCK);
    }
    
    /**
     * Checks if a given person can work on salvaging a building at this time.
     * @param person the person.
     * @return true if person can salvage.
     */
    public static boolean canSalvage(Person person) {
        
        // Check if person can exit the settlement airlock.
        boolean exitable = false;
        Airlock airlock = getWalkableAvailableAirlock(person);
        if (airlock != null) {
            exitable = ExitAirlock.canExitAirlock(person, airlock);
        }

        SurfaceFeatures surface = Simulation.instance().getMars().getSurfaceFeatures();

        // Check if it is night time outside.
        boolean sunlight = surface.getSurfaceSunlight(person.getCoordinates()) > 0;
        
        // Check if in dark polar region.
        boolean darkRegion = surface.inDarkPolarRegion(person.getCoordinates());

        // Check if person's medical condition will not allow task.
        boolean medical = person.getPerformanceRating() < .5D;
    
        return (exitable && (sunlight || darkRegion) && !medical);
    }
    
    /**
     * Determine location to go to at salvage site.
     * @return location.
     */
    private Point2D determineSalvageLocation() {
        
        Point2D.Double relativeLocSite = LocalAreaUtil.getRandomInteriorLocation(site);
        Point2D.Double settlementLocSite = LocalAreaUtil.getLocalRelativeLocation(relativeLocSite.getX(), 
                relativeLocSite.getY(), site);
        
        return settlementLocSite;
    }
    
    /**
     * Determine location outside building airlock.
     * @return location.
     */
    private Point2D determineAirlockEnteringLocation() {
        
        Point2D result = null;
        
        // Move the person to a random location outside the airlock entity.
        if (airlock.getEntity() instanceof LocalBoundedObject) {
            LocalBoundedObject entityBounds = (LocalBoundedObject) airlock.getEntity();
            Point2D.Double newLocation = null;
            boolean goodLocation = false;
            for (int x = 0; (x < 20) && !goodLocation; x++) {
                Point2D.Double boundedLocalPoint = LocalAreaUtil.getRandomExteriorLocation(entityBounds, 1D);
                newLocation = LocalAreaUtil.getLocalRelativeLocation(boundedLocalPoint.getX(), 
                        boundedLocalPoint.getY(), entityBounds);
                goodLocation = LocalAreaUtil.checkLocationCollision(newLocation.getX(), newLocation.getY(), 
                        person.getCoordinates());
            }
            
            result = newLocation;
        }
        
        return result;
    }
    
    @Override
    protected void addExperience(double time) {
        SkillManager manager = person.getMind().getSkillManager();
        
        // Add experience to "EVA Operations" skill.
        // (1 base experience point per 100 millisols of time spent)
        double evaExperience = time / 100D;
        
        // Experience points adjusted by person's "Experience Aptitude" attribute.
        NaturalAttributeManager nManager = person.getNaturalAttributeManager();
        int experienceAptitude = nManager.getAttribute(NaturalAttributeManager.EXPERIENCE_APTITUDE);
        double experienceAptitudeModifier = (((double) experienceAptitude) - 50D) / 100D;
        evaExperience += evaExperience * experienceAptitudeModifier;
        evaExperience *= getTeachingExperienceModifier();
        manager.addExperience(Skill.EVA_OPERATIONS, evaExperience);
        
        // If phase is salvage, add experience to construction skill.
        if (SALVAGE.equals(getPhase())) {
            // 1 base experience point per 10 millisols of construction time spent.
            // Experience points adjusted by person's "Experience Aptitude" attribute.
            double constructionExperience = time / 10D;
            constructionExperience += constructionExperience * experienceAptitudeModifier;
            manager.addExperience(Skill.CONSTRUCTION, constructionExperience);
            
            // If person is driving the light utility vehicle, add experience to driving skill.
            // 1 base experience point per 10 millisols of mining time spent.
            // Experience points adjusted by person's "Experience Aptitude" attribute.
            if (operatingLUV) {
                double drivingExperience = time / 10D;
                drivingExperience += drivingExperience * experienceAptitudeModifier;
                manager.addExperience(Skill.DRIVING, drivingExperience);
            }
        }
    }

    @Override
    public List<String> getAssociatedSkills() {
        List<String> results = new ArrayList<String>(2);
        results.add(Skill.EVA_OPERATIONS);
        results.add(Skill.CONSTRUCTION);
        return results;
    }

    @Override
    public int getEffectiveSkillLevel() {
        SkillManager manager = person.getMind().getSkillManager();
        int EVAOperationsSkill = manager.getEffectiveSkillLevel(Skill.EVA_OPERATIONS);
        int constructionSkill = manager.getEffectiveSkillLevel(Skill.CONSTRUCTION);
        return (int) Math.round((double)(EVAOperationsSkill + constructionSkill) / 2D);
    }

    @Override
    protected double performMappedPhase(double time) {
        if (getPhase() == null) {
            throw new IllegalArgumentException("Task phase is null");
        }
        else if (WALK_TO_SITE.equals(getPhase())) {
            return walkToSalvageSitePhase(time);
        }
        else if (EVAOperation.EXIT_AIRLOCK.equals(getPhase())) {
            return exitEVA(time);
        }
        else if (SALVAGE.equals(getPhase())) {
            return salvage(time);
        }
        else if (WALK_TO_AIRLOCK.equals(getPhase())) {
            return walkToAirlockPhase(time);
        }
        else if (EVAOperation.ENTER_AIRLOCK.equals(getPhase())) {
            return enterEVA(time);
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
            int skill = person.getMind().getSkillManager().getEffectiveSkillLevel(Skill.EVA_OPERATIONS);
            if (skill <= 3) chance *= (4 - skill);
            else chance /= (skill - 2);
            
            // Modify based on the LUV's wear condition.
            chance *= luv.getMalfunctionManager().getWearConditionAccidentModifier();
            
            if (RandomUtil.lessThanRandPercent(chance * time))
                luv.getMalfunctionManager().accident();
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
     * Perform the exit airlock phase of the task.
     * @param time the time (millisols) to perform this phase.
     * @return the time (millisols) remaining after performing this phase.
     * @throws Exception if error exiting the airlock.
     */
    private double exitEVA(double time) {
        
        try {
            time = exitAirlock(time, airlock);
        
            // Add experience points
            addExperience(time);
        }
        catch (Exception e) {
            // Person unable to exit airlock.
            endTask();
        }
        
        if (exitedAirlock) {
            setPhase(WALK_TO_SITE);
        }
        return time;
    }

    /**
     * Perform the enter airlock phase of the task.
     * @param time amount (millisols) of time to perform the phase
     * @return time (millisols) remaining after performing the phase
     * @throws Exception if error entering airlock.
     */
    private double enterEVA(double time) {
        time = enterAirlock(time, airlock);
        
        // Add experience points
        addExperience(time);
        
        if (enteredAirlock) {
            endTask();
        }
        
        return time;
    }
    
    /**
     * Perform the walk to salvage site phase.
     * @param time the time available (millisols).
     * @return remaining time after performing phase (millisols).
     */
    private double walkToSalvageSitePhase(double time) {
        
        // Check for an accident during the EVA walk.
        checkForAccident(time);
        
        // Check if there is reason to cut the EVA walk phase short and return
        // to the rover.
        if (shouldEndEVAOperation()) {
            setPhase(WALK_TO_AIRLOCK);
            return time;
        }
        
        // If not at salvage site location, create walk outside subtask.
        if ((person.getXLocation() != salvageXLoc) || (person.getYLocation() != salvageYLoc)) {
            Task walkingTask = new WalkOutside(person, person.getXLocation(), person.getYLocation(), 
                    salvageXLoc, salvageYLoc, false);
            addSubTask(walkingTask);
        }
        else {
            setPhase(SALVAGE);
        }
        
        return time;
    }
    
    /**
     * Perform the walk to airlock phase.
     * @param time the time available (millisols).
     * @return remaining time after performing phase (millisols).
     */
    private double walkToAirlockPhase(double time) {
        
        // Check for an accident during the EVA walk.
        checkForAccident(time);
        
        // If not at outside airlock location, create walk outside subtask.
        if ((person.getXLocation() != enterAirlockXLoc) || (person.getYLocation() != enterAirlockYLoc)) {
            Task walkingTask = new WalkOutside(person, person.getXLocation(), person.getYLocation(), 
                    enterAirlockXLoc, enterAirlockYLoc, true);
            addSubTask(walkingTask);
        }
        else {
            setPhase(EVAOperation.ENTER_AIRLOCK);
        }
        
        return time;
    }
    
    /**
     * Perform the salvage phase of the task.
     * @param time amount (millisols) of time to perform the phase.
     * @return time (millisols) remaining after performing the phase.
     * @throws Exception
     */
    private double salvage(double time) {
        
        // Check for an accident during the EVA operation.
        checkForAccident(time);
        
        if (shouldEndEVAOperation() || stage.isComplete()) {
            // End operating light utility vehicle.
            if ((luv != null) && luv.getInventory().containsUnit(person))  
                returnVehicle();
            
            setPhase(WALK_TO_AIRLOCK);
            return time;
        }
        
        // Operate light utility vehicle if no one else is operating it.
        if (!operatingLUV) obtainVehicle();
        
        // Determine effective work time based on "Construction" and "EVA Operations" skills.
        double workTime = time;
        int skill = getEffectiveSkillLevel();
        if (skill == 0) workTime /= 2;
        if (skill > 1) workTime += workTime * (.2D * skill);
        
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
                        tempLuv.getInventory().storeUnit(person);
                        tempLuv.setOperator(person);
                        luv = tempLuv;
                        operatingLUV = true;
                        
                        // Place light utility vehicles at random location in construction site.
                        Point2D.Double relativeLocSite = LocalAreaUtil.getRandomInteriorLocation(site);
                        Point2D.Double settlementLocSite = LocalAreaUtil.getLocalRelativeLocation(relativeLocSite.getX(), 
                                relativeLocSite.getY(), site);
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
     * @throws Exception if error returning construction vehicle.
     */
    private void returnVehicle() {
        luv.getInventory().retrieveUnit(person);
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
        if (vehicles != null) vehicles.clear();
        vehicles = null;
        airlock = null;
        luv = null;
    }
}