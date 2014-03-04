/**
 * Mars Simulation Project
 * AreologyFieldWork.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.person.NaturalAttributeManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * A task for the EVA operation of performing areology field work at a research site 
 * for a scientific study.
 */
public class AreologyStudyFieldWork
extends EVAOperation
implements Serializable {

    /** default serial id. */
	private static final long serialVersionUID = 1L;

	// TODO Task phases should be enums
    private static final String WALK_TO_SITE = "Walk to Site";
    private static final String FIELD_WORK = "Performing Field Work";
    private static final String WALK_TO_ROVER = "Walk to Rover";
    
    // Data members
    private Person leadResearcher;
    private ScientificStudy study;
    private Rover rover;
    private double fieldWorkXLoc;
    private double fieldWorkYLoc;
    private double enterAirlockXLoc;
    private double enterAirlockYLoc;
    
    /**
     * Constructor.
     * @param person the person performing the task.
     * @param leadResearcher the researcher leading the field work.
     * @param study the scientific study the field work is for.
     * @param rover the rover
     * @throws Exception if error creating task.
     */
    public AreologyStudyFieldWork(Person person, Person leadResearcher, ScientificStudy study, 
            Rover rover) {
        
        // Use EVAOperation parent constructor.
        super("Areology Study Field Work", person);
        
        // Initialize data members.
        this.leadResearcher = leadResearcher;
        this.study = study;
        this.rover = rover;
        
        // Determine location for field work.
        Point2D fieldWorkLoc = determineFieldWorkLocation();
        fieldWorkXLoc = fieldWorkLoc.getX();
        fieldWorkYLoc = fieldWorkLoc.getY();
        
        // Determine location for reentering rover airlock.
        Point2D enterAirlockLoc = determineRoverAirlockEnteringLocation();
        enterAirlockXLoc = enterAirlockLoc.getX();
        enterAirlockYLoc = enterAirlockLoc.getY();
        
        // Add task phases
        addPhase(WALK_TO_SITE);
        addPhase(FIELD_WORK);
        addPhase(WALK_TO_ROVER);
    }
    
    /**
     * Determine location for field work.
     * @return field work X and Y location outside rover.
     */
    private Point2D determineFieldWorkLocation() {
        
        Point2D newLocation = null;
        boolean goodLocation = false;
        for (int x = 0; (x < 5) && !goodLocation; x++) {
            for (int y = 0; (y < 10) && !goodLocation; y++) {

                double distance = RandomUtil.getRandomDouble(100D) + (x * 100D) + 50D;
                double radianDirection = RandomUtil.getRandomDouble(Math.PI * 2D);
                double newXLoc = rover.getXLocation() - (distance * Math.sin(radianDirection));
                double newYLoc = rover.getYLocation() + (distance * Math.cos(radianDirection));
                Point2D boundedLocalPoint = new Point2D.Double(newXLoc, newYLoc);

                newLocation = LocalAreaUtil.getLocalRelativeLocation(boundedLocalPoint.getX(), 
                        boundedLocalPoint.getY(), rover);
                goodLocation = LocalAreaUtil.checkLocationCollision(newLocation.getX(), newLocation.getY(), 
                        person.getCoordinates());
            }
        }

        return newLocation;
    }
    
    /**
     * Determine location for returning to rover airlock.
     * @return X and Y location outside rover.
     */
    private Point2D determineRoverAirlockEnteringLocation() {
        
        Point2D vehicleLoc = LocalAreaUtil.getRandomExteriorLocation(rover, 1D);
        Point2D newLocation = LocalAreaUtil.getLocalRelativeLocation(vehicleLoc.getX(), 
                vehicleLoc.getY(), rover);
        
        return newLocation;
    }
    
    /**
     * Checks if a person can research a site.
     * @param person the person
     * @param rover the rover
     * @return true if person can research a site.
     */
    public static boolean canResearchSite(Person person, Rover rover) {
        // Check if person can exit the rover.
        boolean exitable = ExitAirlock.canExitAirlock(person, rover.getAirlock());

        SurfaceFeatures surface = Simulation.instance().getMars().getSurfaceFeatures();

        // Check if it is night time outside.
        boolean sunlight = surface.getSurfaceSunlight(rover.getCoordinates()) > 0;
        
        // Check if in dark polar region.
        boolean darkRegion = surface.inDarkPolarRegion(rover.getCoordinates());

        // Check if person's medical condition will not allow task.
        boolean medical = person.getPerformanceRating() < .5D;
    
        return (exitable && (sunlight || darkRegion) && !medical);
    }
    
    /**
     * Perform the exit rover phase of the task.
     * @param time the time to perform this phase (in millisols)
     * @return the time remaining after performing this phase (in millisols)
     * @throws Exception if error exiting rover.
     */
    private double exitRover(double time) {
        
        try {
            time = exitAirlock(time, rover.getAirlock());
        
            // Add experience points
            addExperience(time);
        }
        catch (Exception e) {
            // Person unable to exit airlock.
            endTask();
        }
        
        if (exitedAirlock) {
            
            // Set task phase to walk to field work site.
            setPhase(WALK_TO_SITE);
        }
        
        return time;
    }
    
    /**
     * Perform the enter rover phase of the task.
     * @param time the time to perform this phase (in millisols)
     * @return the time remaining after performing this phase (in millisols)
     * @throws Exception if error entering rover.
     */
    private double enterRover(double time) {

        time = enterAirlock(time, rover.getAirlock());

        // Add experience points
        addExperience(time);
        
        if (enteredAirlock) {
            endTask();
            return time;
        }
        
        return 0D;
    }
    
    /**
     * Perform the walk to field work site phase.
     * @param time the time available (millisols).
     * @return remaining time after performing phase (millisols).
     */
    private double walkToFieldWorkSitePhase(double time) {
        
        // Check for an accident during the EVA walk.
        checkForAccident(time);
        
        // Check if there is reason to cut the EVA walk phase short and return
        // to the rover.
        if (shouldEndEVAOperation()) {
            setPhase(WALK_TO_ROVER);
            return time;
        }
        
        // If not at field work site location, create walk outside subtask.
        if ((person.getXLocation() != fieldWorkXLoc) || (person.getYLocation() != fieldWorkYLoc)) {
            Task walkingTask = new WalkOutside(person, person.getXLocation(), person.getYLocation(), 
                    fieldWorkXLoc, fieldWorkYLoc, false);
            addSubTask(walkingTask);
        }
        else {
            setPhase(FIELD_WORK);
        }
        
        return time;
    }
    
    /**
     * Perform the walk to rover airlock phase.
     * @param time the time available (millisols).
     * @return remaining time after performing phase (millisols).
     */
    private double walkToRoverAirlock(double time) {
        
        // Check for an accident during the EVA walk.
        checkForAccident(time);
        
        // If not at outside rover airlock location, create walk outside subtask.
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
     * Perform the field work phase of the task.
     * @param time the time available (millisols).
     * @return remaining time after performing phase (millisols).
     */
    private double fieldWorkPhase(double time) {
        
        // Check for an accident during the EVA operation.
        checkForAccident(time);
        
        // Check if there is reason to cut the field work phase short and return
        // to the rover.
        if (shouldEndEVAOperation()) {
            setPhase(WALK_TO_ROVER);
            return time;
        }
        
        // Add research work to the scientific study for lead researcher.
        addResearchWorkTime(time);
        
        // Add experience points
        addExperience(time);
        
        return 0D;
    }
    
    /**
     * Adds research work time to the scientific study for the lead researcher.
     * @param time the time (millisols) performing field work.
     */
    private void addResearchWorkTime(double time) {
        // Determine effective field work time.
        double effectiveFieldWorkTime = time;
        int skill = getEffectiveSkillLevel();
        if (skill == 0) effectiveFieldWorkTime /= 2D;
        if (skill > 1) effectiveFieldWorkTime += effectiveFieldWorkTime * (.2D * skill);
        
        // If person isn't lead researcher, divide field work time by two.
        if (!person.equals(leadResearcher)) effectiveFieldWorkTime /= 2D;
        
        // Add research to study for primary or collaborative researcher.
        if (study.getPrimaryResearcher().equals(leadResearcher)) 
            study.addPrimaryResearchWorkTime(effectiveFieldWorkTime);
        else study.addCollaborativeResearchWorkTime(leadResearcher, effectiveFieldWorkTime);
    }
    
    @Override
    protected void addExperience(double time) {
        // Add experience to "EVA Operations" skill.
        // (1 base experience point per 100 millisols of time spent)
        double evaExperience = time / 100D;
        
        // Experience points adjusted by person's "Experience Aptitude" attribute.
        NaturalAttributeManager nManager = person.getNaturalAttributeManager();
        int experienceAptitude = nManager.getAttribute(NaturalAttributeManager.EXPERIENCE_APTITUDE);
        double experienceAptitudeModifier = (((double) experienceAptitude) - 50D) / 100D;
        evaExperience += evaExperience * experienceAptitudeModifier;
        evaExperience *= getTeachingExperienceModifier();
        person.getMind().getSkillManager().addExperience(SkillType.EVA_OPERATIONS, evaExperience);
        
        // If phase is performing field work, add experience to areology skill.
        if (FIELD_WORK.equals(getPhase())) {
            // 1 base experience point per 10 millisols of field work time spent.
            // Experience points adjusted by person's "Experience Aptitude" attribute.
            double areologyExperience = time / 10D;
            areologyExperience += areologyExperience * experienceAptitudeModifier;
            person.getMind().getSkillManager().addExperience(SkillType.AREOLOGY, areologyExperience);
        }
    }

    @Override
    public List<SkillType> getAssociatedSkills() {
        List<SkillType> results = new ArrayList<SkillType>(2);
        results.add(SkillType.EVA_OPERATIONS);
        results.add(SkillType.AREOLOGY);
        return results;
    }

    @Override
    public int getEffectiveSkillLevel() {
        SkillManager manager = person.getMind().getSkillManager();
        int EVAOperationsSkill = manager.getEffectiveSkillLevel(SkillType.EVA_OPERATIONS);
        int areologySkill = manager.getEffectiveSkillLevel(SkillType.AREOLOGY);
        return (int) Math.round((double)(EVAOperationsSkill + areologySkill) / 2D); 
    }

    @Override
    protected double performMappedPhase(double time) {
        if (getPhase() == null) {
            throw new IllegalArgumentException("Task phase is null");
        }
        else if (EVAOperation.EXIT_AIRLOCK.equals(getPhase())) {
            return exitRover(time);
        }
        else if (WALK_TO_SITE.equals(getPhase())) {
            return walkToFieldWorkSitePhase(time);
        }
        else if (FIELD_WORK.equals(getPhase())) {
            return fieldWorkPhase(time);
        }
        else if (WALK_TO_ROVER.equals(getPhase())) {
            return walkToRoverAirlock(time);
        }
        else if (EVAOperation.ENTER_AIRLOCK.equals(getPhase())) {
            return enterRover(time);
        }
        else {
            return time;
        }
    }
    
    @Override
    public void destroy() {
        super.destroy();
        
        leadResearcher = null;
        study = null;
        rover = null;
    }
}