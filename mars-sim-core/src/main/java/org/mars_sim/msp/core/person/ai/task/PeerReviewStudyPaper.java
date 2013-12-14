/**
 * Mars Simulation Project
 * PeerReviewStudyPaper.java
 * @version 3.05 2013-06-03
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
 * A task for peer reviewing a compiled study's paper.
 */
public class PeerReviewStudyPaper extends Task implements Serializable {

    private static Logger logger = Logger.getLogger(PeerReviewStudyPaper.class.getName());

    // The stress modified per millisol.
    private static final double STRESS_MODIFIER = 0D;

    // Task phase.
    private static final String REVIEW = "Reviewing Study Paper";

    private ScientificStudy study; // The scientific study to review.
    
    /**
     * Constructor
     * @param person the person performing the task.
     */
    public PeerReviewStudyPaper(Person person) {
        // Use task constructor.
        super("Peer Review Compiled Study Paper", person, true, false, 
                STRESS_MODIFIER, true, 10D + RandomUtil.getRandomDouble(300D));
        
        // Determine study to review.
        study = determineStudy();
        if (study != null) {
            setDescription("Peer Review " + study.toString());
        }
        else {
            logger.info("study could not be determined");
            endTask();
        }
        
        // Initialize phase
        addPhase(REVIEW);
        setPhase(REVIEW);
    }
    
    /** 
     * Returns the weighted probability that a person might perform this task.
     * @param person the person to perform the task
     * @return the weighted probability that a person might perform this task
     */
    public static double getProbability(Person person) {
        double result = 0D;
        
        // Get all studies in the peer review phase.
        ScientificStudyManager studyManager = Simulation.instance().getScientificStudyManager();
        Iterator<ScientificStudy> i = studyManager.getOngoingStudies().iterator();
        while (i.hasNext()) {
            ScientificStudy study = i.next();
            if (ScientificStudy.PEER_REVIEW_PHASE.equals(study.getPhase())) {
                
                // Check that person isn't a researcher in the study.
                if (!person.equals(study.getPrimaryResearcher()) && 
                        !study.getCollaborativeResearchers().keySet().contains(person)) {
                
                    // If person's current job is related to study primary science, 
                    // add chance to review.
                    Job job = person.getMind().getJob();
                    if (job != null) {
                        Science jobScience = ScienceUtil.getAssociatedScience(job);
                        if (study.getScience().equals(jobScience)) result += 50D;
                    }
                }
            }
        }
        
        // Effort-driven task modifier.
        result *= person.getPerformanceRating();
        
        // Job modifier.
        Job job = person.getMind().getJob();
        if (job != null) {
            result *= job.getStartTaskProbabilityModifier(PeerReviewStudyPaper.class);
        }
        
        return result;
    }
    
    /**
     * Determines the scientific study that will be reviewed.
     * @return study or null if none available.
     */
    private ScientificStudy determineStudy() {
        ScientificStudy result = null;
        
        List<ScientificStudy> possibleStudies = new ArrayList<ScientificStudy>();
        
        // Get all studies in the peer review phase.
        ScientificStudyManager studyManager = Simulation.instance().getScientificStudyManager();
        Iterator<ScientificStudy> i = studyManager.getOngoingStudies().iterator();
        while (i.hasNext()) {
            ScientificStudy study = i.next();
            if (ScientificStudy.PEER_REVIEW_PHASE.equals(study.getPhase())) {
                
                // Check that person isn't a researcher in the study.
                if (!person.equals(study.getPrimaryResearcher()) && 
                        !study.getCollaborativeResearchers().keySet().contains(person)) {
                
                    // Check if person's current job is related to study primary science.
                    Job job = person.getMind().getJob();
                    if (job != null) {
                        Science jobScience = ScienceUtil.getAssociatedScience(job);
                        if (study.getScience().equals(jobScience)) possibleStudies.add(study);
                    }
                }
            }
        }
        
        // Randomly select study.
        if (possibleStudies.size() > 0) {
            int selected = RandomUtil.getRandomInt(possibleStudies.size() - 1);
            result = possibleStudies.get(selected);
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
        String scienceSkill = ScienceUtil.getAssociatedSkill(study.getScience());
        person.getMind().getSkillManager().addExperience(scienceSkill, newPoints);
    }

    @Override
    public List<String> getAssociatedSkills() {
        List<String> results = new ArrayList<String>(1);
        String scienceSkill = ScienceUtil.getAssociatedSkill(study.getScience());
        results.add(scienceSkill);
        return results;
    }

    @Override
    public int getEffectiveSkillLevel() {
        String scienceSkill = ScienceUtil.getAssociatedSkill(study.getScience());
        SkillManager manager = person.getMind().getSkillManager();
        return manager.getEffectiveSkillLevel(scienceSkill);
    }

    @Override
    protected double performMappedPhase(double time) {
        if (getPhase() == null) throw new IllegalArgumentException("Task phase is null");
        if (REVIEW.equals(getPhase())) return reviewingPhase(time);
        else return time;
    }
    
    /**
     * Performs the study peer reviewing phase.
     * @param time the amount of time (millisols) to perform the phase.
     * @return the amount of time (millisols) left over after performing the phase.
     * @throws Exception if error performing the phase.
     */
    private double reviewingPhase(double time) {
        
        // If person is incapacitated, end task.
        if (person.getPerformanceRating() == 0D) endTask();
        
        // Check if peer review phase in study is completed.
        if (study.isCompleted()) endTask();
        
        if (isDone()) return time;
        
        // Peer review study. (No operation required for this)
        
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