/**
 * Mars Simulation Project
 * CompileScientificStudyResults.java
 * @version 3.06 2013-12-12
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.NaturalAttributeManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.science.Science;
import org.mars_sim.msp.core.science.ScienceUtil;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.core.science.ScientificStudyManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 * A task for compiling research data for a scientific study.
 */
public class CompileScientificStudyResults extends Task implements Serializable {

    private static Logger logger = Logger.getLogger(CompileScientificStudyResults.class.getName());
    
    // The stress modified per millisol.
    private static final double STRESS_MODIFIER = 0D;
    
    // Task phase.
    private static final String COMPILING_PHASE = "Compiling Study Data";
    
    // Data members
    private ScientificStudy study; // The scientific study to compile.
    
    /**
     * Constructor
     * @param person the person performing the task.
     * @throws Exception if error constructing the class.
     */
    public CompileScientificStudyResults(Person person) {
        // Use task constructor.
        super("Compiling Scientific Study Data Results", person, true, false, 
                STRESS_MODIFIER, true, RandomUtil.getRandomDouble(50D));
        
        // Determine study.
        study = determineStudy();
        if (study != null) {
            setDescription("Compiling Data Results for " + study.toString());
        }
        else {
            logger.severe("Study could not be determined");
            endTask();
        }
        
        // Initialize phase
        addPhase(COMPILING_PHASE);
        setPhase(COMPILING_PHASE);
    }
    
    /** 
     * Returns the weighted probability that a person might perform this task.
     * @param person the person to perform the task
     * @return the weighted probability that a person might perform this task
     */
    public static double getProbability(Person person) {
        double result = 0D;
        
        // Add probability for researcher's primary study (if any).
        ScientificStudyManager studyManager = Simulation.instance().getScientificStudyManager();
        ScientificStudy primaryStudy = studyManager.getOngoingPrimaryStudy(person);
        if ((primaryStudy != null) && ScientificStudy.PAPER_PHASE.equals(primaryStudy.getPhase())) {
            if (!primaryStudy.isPrimaryPaperCompleted()) {
                try {
                    double primaryResult = 50D;
                    
                    // If researcher's current job isn't related to study science, divide by two.
                    Job job = person.getMind().getJob();
                    if (job != null) {
                        Science jobScience = ScienceUtil.getAssociatedScience(job);
                        if (!primaryStudy.getScience().equals(jobScience)) primaryResult /= 2D;
                    }
                    
                    result += primaryResult;
                }
                catch (Exception e) {
                    logger.severe("getProbability(): " + e.getMessage());
                }
            }
        }
        
        // Add probability for each study researcher is collaborating on.
        Iterator<ScientificStudy> i = studyManager.getOngoingCollaborativeStudies(person).iterator();
        while (i.hasNext()) {
            ScientificStudy collabStudy = i.next();
            if (ScientificStudy.PAPER_PHASE.equals(collabStudy.getPhase())) {
                if (!collabStudy.isCollaborativePaperCompleted(person)) {
                    try {
                        Science collabScience = collabStudy.getCollaborativeResearchers().get(person);
                    
                        double collabResult = 25D;
                        
                        // If researcher's current job isn't related to study science, divide by two.
                        Job job = person.getMind().getJob();
                        if (job != null) {
                            Science jobScience = ScienceUtil.getAssociatedScience(job);
                            if (!collabScience.equals(jobScience)) collabResult /= 2D;
                        }
                        
                        result += collabResult;
                    }
                    catch (Exception e) {
                        logger.severe("getProbability(): " + e.getMessage());
                    }
                }
            }
        }
        
        // Effort-driven task modifier.
        result *= person.getPerformanceRating();
        
        // Job modifier.
        Job job = person.getMind().getJob();
        if (job != null) {
            result *= job.getStartTaskProbabilityModifier(CompileScientificStudyResults.class);
        }
        
        return result;
    }
    
    /**
     * Determines the scientific study that will be compiled.
     * @return study or null if none available.
     */
    private ScientificStudy determineStudy() {
        ScientificStudy result = null;
        
        List<ScientificStudy> possibleStudies = new ArrayList<ScientificStudy>();
        
        // Add primary study if in paper phase.
        ScientificStudyManager manager = Simulation.instance().getScientificStudyManager();
        ScientificStudy primaryStudy = manager.getOngoingPrimaryStudy(person);
        if (primaryStudy != null) {
            if (ScientificStudy.PAPER_PHASE.equals(primaryStudy.getPhase()) && 
                    !primaryStudy.isPrimaryPaperCompleted()) {
                // Primary study added twice to double chance of random selection.
                possibleStudies.add(primaryStudy);
                possibleStudies.add(primaryStudy);
            }
        }
        
        // Add all collaborative studies in research phase.
        Iterator<ScientificStudy> i = manager.getOngoingCollaborativeStudies(person).iterator();
        while (i.hasNext()) {
            ScientificStudy collabStudy = i.next();
            if (ScientificStudy.PAPER_PHASE.equals(collabStudy.getPhase()) && 
                    !collabStudy.isCollaborativePaperCompleted(person)) 
                possibleStudies.add(collabStudy);
        }
        
        // Randomly select study.
        if (possibleStudies.size() > 0) {
            int selected = RandomUtil.getRandomInt(possibleStudies.size() - 1);
            result = possibleStudies.get(selected);
        }
        
        return result;
    }
    
    /**
     * Gets the field of science that the researcher is involved with in a study.
     * @return the field of science or null if researcher is not involved with study.
     */
    private Science getScience() {
        Science result = null;
        
        if (study.getPrimaryResearcher().equals(person)) {
            result = study.getScience();
        }
        else if (study.getCollaborativeResearchers().containsKey(person)) {
            result = study.getCollaborativeResearchers().get(person);
        }
        
        return result;
    }
    
    @Override
    protected void addExperience(double time) {
        // Add experience to relevant science skill
        // (1 base experience point per 25 millisols of research time)
        // Experience points adjusted by person's "Academic Aptitude" attribute.
        double newPoints = time / 25D;
        int academicAptitude = person.getNaturalAttributeManager().getAttribute(
            NaturalAttributeManager.ACADEMIC_APTITUDE);
        newPoints += newPoints * ((double) academicAptitude - 50D) / 100D;
        newPoints *= getTeachingExperienceModifier();
        String scienceSkill = ScienceUtil.getAssociatedSkill(getScience());
        person.getMind().getSkillManager().addExperience(scienceSkill, newPoints);
    }

    /**
     * Gets the effective compilation time based on the person's science skill.
     * @param time the real amount of time (millisol) for result data compilation.
     * @return the effective amount of time (millisol) for result data compilation.
     */
    private double getEffectiveCompilationTime(double time) {
        // Determine effective compilation time based on the science skill.
        double compilationTime = time;
        int scienceSkill = getEffectiveSkillLevel();
        if (scienceSkill == 0) compilationTime /= 2D;
        if (scienceSkill > 1) compilationTime += compilationTime * (.2D * scienceSkill);
        
        return compilationTime;
    }
    
    @Override
    public List<String> getAssociatedSkills() {
        List<String> results = new ArrayList<String>(1);
        String scienceSkill = ScienceUtil.getAssociatedSkill(getScience());
        results.add(scienceSkill);
        return results;
    }

    @Override
    public int getEffectiveSkillLevel() {
        String scienceSkill = ScienceUtil.getAssociatedSkill(getScience());
        SkillManager manager = person.getMind().getSkillManager();
        return manager.getEffectiveSkillLevel(scienceSkill);
    }

    @Override
    protected double performMappedPhase(double time) {
        if (getPhase() == null) throw new IllegalArgumentException("Task phase is null");
        if (COMPILING_PHASE.equals(getPhase())) return compilingPhase(time);
        else return time;
    }
    
    /**
     * Performs the data results compilation phase.
     * @param time the amount of time (millisols) to perform the phase.
     * @return the amount of time (millisols) left over after performing the phase.
     * @throws Exception if error performing the phase.
     */
    private double compilingPhase(double time) {
        
        // If person is incapacitated, end task.
        if (person.getPerformanceRating() == 0D) endTask();
        
        // Check if data results compilation in study is completed.
        boolean isPrimary = study.getPrimaryResearcher().equals(person);
        if (isPrimary) {
            if (study.isPrimaryPaperCompleted()) endTask();
        }
        else {
            if (study.isCollaborativePaperCompleted(person)) endTask();
        }
        
        if (isDone()) return time;
        
        // Add paper work time to study.
        double compilingTime = getEffectiveCompilationTime(time);
        if (isPrimary) study.addPrimaryPaperWorkTime(compilingTime);
        else study.addCollaborativePaperWorkTime(person, compilingTime);
        
        // Add experience
        addExperience(time);
        
        return 0D;
    }
    
    @Override
    public void destroy() {
        super.destroy();
        
        study = null;
    }
}