/**
 * Mars Simulation Project
 * ConstructBuilding.java
 * @version 3.02 2011-11-26
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import org.mars_sim.msp.core.Airlock;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.person.NaturalAttributeManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.Skill;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.construction.ConstructionStage;
import org.mars_sim.msp.core.structure.construction.ConstructionVehicleType;
import org.mars_sim.msp.core.vehicle.GroundVehicle;
import org.mars_sim.msp.core.vehicle.LightUtilityVehicle;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Task for constructing a building construction site stage.
 */
public class ConstructBuilding extends EVAOperation implements Serializable {

    // Task phases
    private static final String CONSTRUCTION = "Construction";
    
    // The base chance of an accident while operating LUV per millisol.
    public static final double BASE_LUV_ACCIDENT_CHANCE = .001;
    
    // Data members.
    private Settlement settlement;
    private ConstructionStage stage;
    private List<GroundVehicle> vehicles;
    private Airlock airlock;
    private LightUtilityVehicle luv;
    private boolean operatingLUV;
    
    /**
     * Constructor
     * @param person the person performing the task.
     * @param stage the construction site stage.
     * @param vehicles the construction vehicles.
     * @throws Exception if error constructing task.
     */
    public ConstructBuilding(Person person, ConstructionStage stage, 
            List<GroundVehicle> vehicles) {
        // Use EVAOperation parent constructor.
        super("Construct Building", person);
        
        // Initialize data members.
        this.stage = stage;
        this.vehicles = vehicles;
        settlement = person.getSettlement();
        
        // Get an available airlock.
        airlock = getAvailableAirlock(person);
        if (airlock == null) endTask();
        
        // Add task phase
        addPhase(CONSTRUCTION);
    }
    
    /**
     * Checks if a given person can work on construction at this time.
     * @param person the person.
     * @return true if person can construct.
     */
    public static boolean canConstruct(Person person) {
        
        // Check if person can exit the settlement airlock.
        boolean exitable = false;
        Airlock airlock = getAvailableAirlock(person);
        if (airlock != null) exitable = ExitAirlock.canExitAirlock(person, airlock);

        SurfaceFeatures surface = Simulation.instance().getMars().getSurfaceFeatures();

        // Check if it is night time outside.
        boolean sunlight = surface.getSurfaceSunlight(person.getCoordinates()) > 0;
        
        // Check if in dark polar region.
        boolean darkRegion = surface.inDarkPolarRegion(person.getCoordinates());

        // Check if person's medical condition will not allow task.
        boolean medical = person.getPerformanceRating() < .5D;
    
        return (exitable && (sunlight || darkRegion) && !medical);
    }
    
    @Override
    protected double performMappedPhase(double time) {
        if (getPhase() == null) throw new IllegalArgumentException("Task phase is null");
        if (EVAOperation.EXIT_AIRLOCK.equals(getPhase())) return exitEVA(time);
        if (CONSTRUCTION.equals(getPhase())) return construction(time);
        if (EVAOperation.ENTER_AIRLOCK.equals(getPhase())) return enterEVA(time);
        else return time;
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
        
        if (exitedAirlock) setPhase(CONSTRUCTION);
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
        
        if (enteredAirlock) endTask();
        return time;
    }
    
    /**
     * Perform the construction phase of the task.
     * @param time amount (millisols) of time to perform the phase.
     * @return time (millisols) remaining after performing the phase.
     * @throws Exception
     */
    private double construction(double time) {
        
        // Check for an accident during the EVA operation.
        checkForAccident(time);
        
        if (shouldEndEVAOperation() || stage.isComplete()) {
            // End operating light utility vehicle.
            if ((luv != null) && luv.getInventory().containsUnit(person))  
                returnVehicle();
            
            setPhase(EVAOperation.ENTER_AIRLOCK);
            return time;
        }
        
        // Operate light utility vehicle if no one else is operating it.
        if (!operatingLUV) obtainVehicle();
        
        // Determine effective work time based on "Construction" and "EVA Operations" skills.
        double workTime = time;
        int skill = getEffectiveSkillLevel();
        if (skill == 0) workTime /= 2;
        if (skill > 1) workTime += workTime * (.2D * skill);
        
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
     * @throws Exception if error obtaining construction vehicle.
     */
    private void obtainVehicle() {
        Iterator<GroundVehicle> i = vehicles.iterator();
        while (i.hasNext() && (luv == null)) {
            GroundVehicle vehicle = i.next();
            if (vehicle instanceof LightUtilityVehicle) {
                LightUtilityVehicle tempLuv = (LightUtilityVehicle) vehicle;
                if (tempLuv.getOperator() == null) {
                    if (settlement.getInventory().containsUnit(tempLuv))
                        settlement.getInventory().retrieveUnit(tempLuv);
                    tempLuv.getInventory().storeUnit(person);
                    tempLuv.setOperator(person);
                    luv = tempLuv;
                    operatingLUV = true;
                    
                    // Load attachment parts on vehicle.
                    loadAttachmentParts();
                }
            }
        }
    }
    
    /**
     * Loads any needed attachment parts on the construction vehicle.
     * @throws Exception if error loading the parts.
     */
    private void loadAttachmentParts() {
        if (luv != null) {
            int index = vehicles.indexOf(luv);
            ConstructionVehicleType vehicleType = stage.getInfo().getVehicles().get(index);
            Iterator<Part> i = vehicleType.getAttachmentParts().iterator();
            while (i.hasNext()) {
                // Assume part has already be retrieved from settlement at 
                // construction mission start.
                luv.getInventory().storeItemResources(i.next(), 1);
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
        settlement.getInventory().storeUnit(luv);
        
        // Unload attachment parts from vehicle.
        unloadAttachmentParts();
    }
    
    /**
     * Unloads attachment parts from the construction vehicle.
     * @throws Exception if error unloading parts.
     */
    private void unloadAttachmentParts() {
        if (luv != null) {
            int index = vehicles.indexOf(luv);
            ConstructionVehicleType vehicleType = stage.getInfo().getVehicles().get(index);
            Iterator<Part> i = vehicleType.getAttachmentParts().iterator();
            while (i.hasNext()) {
                // Assume part will be stored in the settlement when 
                // construction mission ends.
                luv.getInventory().retrieveItemResources(i.next(), 1);
            }
        }
    }
    
    @Override
    public int getEffectiveSkillLevel() {
        SkillManager manager = person.getMind().getSkillManager();
        int EVAOperationsSkill = manager.getEffectiveSkillLevel(Skill.EVA_OPERATIONS);
        int constructionSkill = manager.getEffectiveSkillLevel(Skill.CONSTRUCTION);
        return (int) Math.round((double)(EVAOperationsSkill + constructionSkill) / 2D); 
    }

    @Override
    public List<String> getAssociatedSkills() {
        List<String> results = new ArrayList<String>(2);
        results.add(Skill.EVA_OPERATIONS);
        results.add(Skill.CONSTRUCTION);
        return results;
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
        
        // If phase is construction, add experience to construction skill.
        if (CONSTRUCTION.equals(getPhase())) {
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
    
    @Override
    public void destroy() {
        super.destroy();
        
        settlement = null;
        stage = null;
        if (vehicles != null) vehicles.clear();
        vehicles = null;
        airlock = null;
        luv = null;
    }
}