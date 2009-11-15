/**
 * Mars Simulation Project
 * InviteStudyCollaborator.java
 * @version 2.87 2009-07-10
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.person.ai.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.simulation.RandomUtil;
import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.simulation.person.NaturalAttributeManager;
import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.person.ai.job.Job;
import org.mars_sim.msp.simulation.person.ai.social.Relationship;
import org.mars_sim.msp.simulation.person.ai.social.RelationshipManager;
import org.mars_sim.msp.simulation.science.Science;
import org.mars_sim.msp.simulation.science.ScienceUtil;
import org.mars_sim.msp.simulation.science.ScientificStudy;
import org.mars_sim.msp.simulation.science.ScientificStudyManager;
import org.mars_sim.msp.simulation.structure.Settlement;

/**
 * A task for responding to an invitation to collaborate on a scientific study.
 */
public class RespondToStudyInvitation extends Task implements Serializable {

    private static String CLASS_NAME = "org.mars_sim.msp.simulation.person.ai.task.RespondToStudyInvitation";
    private static Logger logger = Logger.getLogger(CLASS_NAME);
    
    // The stress modified per millisol.
    private static final double STRESS_MODIFIER = 0D;
    
    // Duration (millisols) of task.
    private static final double DURATION = 50D;
    
    // Task phase.
    private static final String RESPONDING_INVITATION = "Responding to Invitation";
    
    private ScientificStudy study; // The scientific study.
    
    /**
     * Constructor
     * @param person the person performing the task.
     * @throws Exception if error creating task.
     */
    public RespondToStudyInvitation(Person person) throws Exception {
        super("Responding to a Study Invitation", person, false, true, 
                STRESS_MODIFIER, true, DURATION);
        
        ScientificStudyManager manager = Simulation.instance().getScientificStudyManager();
        List<ScientificStudy> invitedStudies = manager.getOpenInvitationStudies(person);
        if (invitedStudies.size() > 0) {
            study = invitedStudies.get(0);
        }
        else {
            logger.log(Level.SEVERE, person.getName() + " does not have any open invited studies.");
            endTask();
        }
        
        // Initialize phase
        addPhase(RESPONDING_INVITATION);
        setPhase(RESPONDING_INVITATION);
    }
    
    /** 
     * Returns the weighted probability that a person might perform this task.
     * @param person the person to perform the task
     * @return the weighted probability that a person might perform this task
     */
    public static double getProbability(Person person) {
        double result = 0D;
        
        ScientificStudyManager manager = Simulation.instance().getScientificStudyManager();
        List<ScientificStudy> invitedStudies = manager.getOpenInvitationStudies(person);
        if (invitedStudies.size() > 0) result = 50D;
        
        return result;
    }
    
    /**
     * Performs the responding to invitation phase.
     * @param time the time (millisols) to perform the phase.
     * @return the remaining time (millisols) after performing the phase.
     * @throws Exception if error performing phase.
     */
    private double respondingToInvitationPhase(double time) throws Exception {
        
        if (isDone()) return time;
        
        // If duration, send respond to invitation.
        if (getDuration() < (getTimeCompleted() + time)) {
            
            study.respondingInvitedResearcher(person);
            Job job = person.getMind().getJob();
            
            // Get relationship between invitee and primary researcher.
            Person primaryResearcher = study.getPrimaryResearcher();
            RelationshipManager relationshipManager = Simulation.instance().getRelationshipManager();
            Relationship relationship = relationshipManager.getRelationship(person, primaryResearcher);
            
            // Decide response to invitation.
            if (decideResponse()) {
                Science science = ScienceUtil.getAssociatedScience(job);
                study.addCollaborativeResearcher(person, science);
                
                // Add 10 points to primary researcher's opinion of invitee for accepting invitation.
                if (relationship != null) {
                    double currentOpinion = relationship.getPersonOpinion(primaryResearcher);
                    relationship.setPersonOpinion(primaryResearcher, currentOpinion + 10D);
                }
                
                logger.info(job.getName() + " " + person.getName() + 
                        " accepting invitation from " + primaryResearcher.getName() + 
                        " to collaborate on " + study.toString());
            }
            else {
                
                // Subtract 10 points from primary researcher's opinion of invitee for rejecting invitation.
                if (relationship != null) {
                    double currentOpinion = relationship.getPersonOpinion(primaryResearcher);
                    relationship.setPersonOpinion(primaryResearcher, currentOpinion - 10D);
                }
                
                logger.info(job.getName() + " " + person.getName() + 
                        " rejecting invitation from " + primaryResearcher.getName() + 
                        " to collaborate on " + study.toString());
            }
        }
        
        return 0D;
    }
    
    /**
     * Decides is the researcher accepts or rejects invitation.
     * @return true if accepts, false if rejects.
     */
    private boolean decideResponse() {
        boolean result = false;
        
        Science studyScience = study.getScience();
        Science jobScience = ScienceUtil.getAssociatedScience(person.getMind().getJob());
        if (jobScience != null) {
            boolean isPrimaryScience = studyScience.equals(jobScience);
            boolean isCollaborativeScience = ScienceUtil.isCollaborativeScience(studyScience, jobScience);
            if (isPrimaryScience || isCollaborativeScience) {
                double acceptChance = 50D;
                
                // Modify based on study primary researcher's achievement.
                double primaryAchievement = study.getPrimaryResearcher().getScientificAchievement(studyScience);
                acceptChance += primaryAchievement;
                
                // Modify based on study collaborative researchers' achievements.
                Iterator<Person> i = study.getCollaborativeResearchers().keySet().iterator();
                while (i.hasNext()) {
                    Person collaborator = i.next();
                    Science collaborativeScience = study.getCollaborativeResearchers().get(collaborator);
                    acceptChance += (collaborator.getScientificAchievement(collaborativeScience) / 2D);
                }
                
                // Modify if researcher's job science is collaborative.
                if (isCollaborativeScience) acceptChance/= 2D;
                
                // Modify by how many studies researcher is already collaborating on.
                ScientificStudyManager manager = Simulation.instance().getScientificStudyManager();
                int numCollabStudies = manager.getOngoingCollaborativeStudies(person).size();
                acceptChance /= (numCollabStudies + 1D);
                
                // Modify based on difficulty level of study vs researcher's skill.
                String skillName = ScienceUtil.getAssociatedSkill(jobScience);
                int skillLevel = person.getMind().getSkillManager().getSkillLevel(skillName);
                if (skillLevel == 0) skillLevel = 1;
                int difficultyLevel = study.getDifficultyLevel();
                if (difficultyLevel == 0) difficultyLevel = 1;
                acceptChance *= ((double) difficultyLevel / (double) skillLevel);
                
                // Modify based on researchers opinion of primary researcher.
                RelationshipManager relationshipManager = Simulation.instance().getRelationshipManager();
                double researcherOpinion = relationshipManager.getOpinionOfPerson(person, 
                        study.getPrimaryResearcher());
                acceptChance *= (researcherOpinion / 50D);
                
                // Modify based on if researcher and primary researcher are at same settlement.
                Settlement researcherSettlement = person.getAssociatedSettlement();
                Settlement primarySettlement = study.getPrimaryResearcher().getAssociatedSettlement();
                if ((researcherSettlement != null) && researcherSettlement.equals(primarySettlement)) 
                    acceptChance *= 2D;
                
                result = (RandomUtil.getRandomDouble(100D) < acceptChance);
            }
        }
        return result;
    }
    
    @Override
    protected void addExperience(double time) {
        // Add experience to relevant science skill
        // 1 base experience point per 25 millisols of proposal writing time.
        double newPoints = time / 25D;
        
        // Experience points adjusted by person's "Academic Aptitude" attribute.
        int academicAptitude = person.getNaturalAttributeManager().getAttribute(
            NaturalAttributeManager.ACADEMIC_APTITUDE);
        newPoints += newPoints * ((double) academicAptitude - 50D) / 100D;
        newPoints *= getTeachingExperienceModifier();
        
        Science jobScience = ScienceUtil.getAssociatedScience(person.getMind().getJob());
        String skillName = ScienceUtil.getAssociatedSkill(jobScience);
        person.getMind().getSkillManager().addExperience(skillName, newPoints);
    }

    @Override
    public List<String> getAssociatedSkills() {
        List<String> result = new ArrayList<String>(1);
        Science jobScience = ScienceUtil.getAssociatedScience(person.getMind().getJob());
        if (jobScience != null) result.add(ScienceUtil.getAssociatedSkill(jobScience));
        return result;
    }

    @Override
    public int getEffectiveSkillLevel() {
        Science jobScience = ScienceUtil.getAssociatedScience(person.getMind().getJob());
        String skillName = ScienceUtil.getAssociatedSkill(jobScience);
        return person.getMind().getSkillManager().getEffectiveSkillLevel(skillName);
    }

    @Override
    protected double performMappedPhase(double time) throws Exception {
        if (getPhase() == null) throw new IllegalArgumentException("Task phase is null");
        if (RESPONDING_INVITATION.equals(getPhase())) return respondingToInvitationPhase(time);
        else return time;
    }
}