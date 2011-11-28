/**
 * Mars Simulation Project
 * BiologyFieldWork.java
 * @version 3.02 2011-11-26
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person.ai.task;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.person.NaturalAttributeManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.Skill;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.core.vehicle.Rover;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A task for the EVA operation of performing biology field work at a research site 
 * for a scientific study.
 */
public class BiologyStudyFieldWork extends EVAOperation implements Serializable {

    // Task phases
    private static final String FIELD_WORK = "Performing Field Work";
    
    // Data members
    private Person leadResearcher;
    private ScientificStudy study;
    private Rover rover;
    
    /**
     * Constructor
     * @param person the person performing the task.
     * @param leadResearcher the researcher leading the field work.
     * @param study the scientific study the field work is for.
     * @param rover the rover
     * @throws Exception if error creating task.
     */
    public BiologyStudyFieldWork(Person person, Person leadResearcher, ScientificStudy study, 
            Rover rover) {
        
        // Use EVAOperation parent constructor.
        super("Biology Study Field Work", person);
        
        // Initialize data members.
        this.leadResearcher = leadResearcher;
        this.study = study;
        this.rover = rover;
        
        // Add task phase
        addPhase(FIELD_WORK);
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
        
        if (exitedAirlock) setPhase(FIELD_WORK);
        
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
     * Perform the field work phase of the task.
     * @param time the time available (millisols).
     * @return remaining time after performing phase (millisols).
     * @throws Exception if error performing phase.
     */
    private double fieldWorkPhase(double time) {
        
        // Check for an accident during the EVA operation.
        checkForAccident(time);
        
        // Check if there is reason to cut the field work phase short and return
        // to the rover.
        if (shouldEndEVAOperation()) {
            setPhase(EVAOperation.ENTER_AIRLOCK);
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
        person.getMind().getSkillManager().addExperience(Skill.EVA_OPERATIONS, evaExperience);
        
        // If phase is performing field work, add experience to biology skill.
        if (FIELD_WORK.equals(getPhase())) {
            // 1 base experience point per 10 millisols of field work time spent.
            // Experience points adjusted by person's "Experience Aptitude" attribute.
            double biologyExperience = time / 10D;
            biologyExperience += biologyExperience * experienceAptitudeModifier;
            person.getMind().getSkillManager().addExperience(Skill.BIOLOGY, biologyExperience);
        }
    }

    @Override
    public List<String> getAssociatedSkills() {
        List<String> results = new ArrayList<String>(2);
        results.add(Skill.EVA_OPERATIONS);
        results.add(Skill.BIOLOGY);
        return results;
    }

    @Override
    public int getEffectiveSkillLevel() {
        SkillManager manager = person.getMind().getSkillManager();
        int EVAOperationsSkill = manager.getEffectiveSkillLevel(Skill.EVA_OPERATIONS);
        int biologySkill = manager.getEffectiveSkillLevel(Skill.BIOLOGY);
        return (int) Math.round((double)(EVAOperationsSkill + biologySkill) / 2D); 
    }

    @Override
    protected double performMappedPhase(double time) {
        if (getPhase() == null) throw new IllegalArgumentException("Task phase is null");
        if (EVAOperation.EXIT_AIRLOCK.equals(getPhase())) return exitRover(time);
        if (FIELD_WORK.equals(getPhase())) return fieldWorkPhase(time);
        if (EVAOperation.ENTER_AIRLOCK.equals(getPhase())) return enterRover(time);
        else return time;
    }
    
    @Override
    public void destroy() {
        super.destroy();
        
        leadResearcher = null;
        study = null;
        rover = null;
    }
}