/**
 * Mars Simulation Project
 * InviteStudyCollaborator.java
 * @version 3.06 2013-12-12
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.NaturalAttributeManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.social.Relationship;
import org.mars_sim.msp.core.person.ai.social.RelationshipManager;
import org.mars_sim.msp.core.science.*;
import org.mars_sim.msp.core.structure.Settlement;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 * A task for inviting a researcher to collaborate on a scientific study.
 */
public class InviteStudyCollaborator extends Task implements Serializable {

    private static Logger logger = Logger.getLogger(InviteStudyCollaborator.class.getName());
    
    // The stress modified per millisol.
    private static final double STRESS_MODIFIER = 0D;
    
    // Duration (millisols) of task.
    private static final double DURATION = 25D;
    
    // Task phase.
    private static final String WRITING_INVITATION = "Writing Invitation";
    
    private ScientificStudy study; // The scientific study.
    private Person invitee; // The collaborative researcher to invite.
    
    /**
     * Constructor
     * @param person the person performing the task.
     * @throws Exception if error creating task.
     */
    public InviteStudyCollaborator(Person person) {
        super("Writing Study Collaboration Invitation", person, false, true, 
                STRESS_MODIFIER, true, DURATION);
        
        ScientificStudyManager manager = Simulation.instance().getScientificStudyManager();
        study = manager.getOngoingPrimaryStudy(person);
        if (study != null) {
            
            // Determine best invitee.
            invitee = determineBestInvitee();
            
            if (invitee == null) {
                logger.severe("No available collaborative researchers available for invitation.");
                endTask();
            }
        }
        else {
            logger.severe(person.getName() + " does not have a primary scientific study.");
            endTask();
        }
        
        // Initialize phase
        addPhase(WRITING_INVITATION);
        setPhase(WRITING_INVITATION);
    }
    
    /** 
     * Returns the weighted probability that a person might perform this task.
     * @param person the person to perform the task
     * @return the weighted probability that a person might perform this task
     */
    public static double getProbability(Person person) {
        double result = 0D;
        
        ScientificStudyManager manager = Simulation.instance().getScientificStudyManager();
        ScientificStudy study = manager.getOngoingPrimaryStudy(person);
        if (study != null) {
            
            // Check if study is in invitation phase.
            if (study.getPhase().equals(ScientificStudy.INVITATION_PHASE)) {
                
                // Check that there isn't a full set of open invitations already sent out.
                int collabNum = study.getCollaborativeResearchers().size();
                int openInvites = study.getNumOpenResearchInvitations();
                if ((openInvites + collabNum) < ScientificStudy.MAX_NUM_COLLABORATORS) {
                    
                    // Check that there's scientists available for invitation.
                    if (ScientificStudyUtil.getAvailableCollaboratorsForInvite(study).size() > 0) {
                        
                        result = 25D;
                        
                        // Increase probability if person's current job is related to study's science.
                        Job job = person.getMind().getJob();
                        Science science = study.getScience();
                        if (science.equals(ScienceUtil.getAssociatedScience(job))) result*= 2D;
                    }
                }
            }
        }
        
        // Job modifier.
        Job job = person.getMind().getJob();
        if (job != null) {
            result *= job.getStartTaskProbabilityModifier(InviteStudyCollaborator.class);
        }
        
        return result;
    }
    
    /**
     * Determines the best available researcher to invite for collaboration on a study.
     * @return best collaborative invitee or null if none.
     * @throws Exception if error determining invitee.
     */
    private Person determineBestInvitee() {
        Person bestInvitee = null;
        double bestValue = Double.NEGATIVE_INFINITY;
        
        Iterator<Person> i = ScientificStudyUtil.getAvailableCollaboratorsForInvite(study).iterator();
        while (i.hasNext()) {
            Person invitee = i.next();
            double inviteeValue = 0D;
            
            Science jobScience = ScienceUtil.getAssociatedScience(invitee.getMind().getJob());
            
            // Modify based on invitee level in job science.
            String skillName = ScienceUtil.getAssociatedSkill(jobScience);
            int skillLevel = invitee.getMind().getSkillManager().getEffectiveSkillLevel(skillName);
            inviteeValue += skillLevel;
            
            // Modify based on invitee achievement in job science.
            double achievement = invitee.getScientificAchievement(jobScience);
            inviteeValue += achievement;
            
            // Modify based on if invitee's job science is not study primary science.
            if (!jobScience.equals(study.getScience())) inviteeValue /= 2D;
            
            // Modify based on invitee total scientific achievement.
            double totalAchievement = invitee.getTotalScientificAchievement();
            inviteeValue += (totalAchievement / 10D);
            
            // Modify based on study researcher's personal opinion of invitee.
            RelationshipManager relationshipManager = Simulation.instance().getRelationshipManager();
            double opinion = relationshipManager.getOpinionOfPerson(study.getPrimaryResearcher(), invitee);
            inviteeValue *= (opinion / 100D);
            
            // Modify based on current number of studies researcher is currently collaborating on.
            ScientificStudyManager studyManager = Simulation.instance().getScientificStudyManager();
            int numCollaborativeStudies = studyManager.getOngoingCollaborativeStudies(invitee).size();
            inviteeValue /= (numCollaborativeStudies + 1D);
            
            // Modify based on if researcher and primary researcher are at same settlement.
            Settlement researcherSettlement = person.getAssociatedSettlement();
            Settlement primarySettlement = study.getPrimaryResearcher().getAssociatedSettlement();
            if ((researcherSettlement != null) && researcherSettlement.equals(primarySettlement)) 
                inviteeValue *= 2D;
            
            if (inviteeValue > bestValue) {
                bestInvitee = invitee;
                bestValue = inviteeValue;
            }
        }
        
        return bestInvitee;
    }
    
    /**
     * Performs the writing invitation phase.
     * @param time the time (millisols) to perform the phase.
     * @return the remaining time (millisols) after performing the phase.
     * @throws Exception if error performing phase.
     */
    private double writingInvitationPhase(double time) {
        
        if (isDone()) return time;
        
        // If duration, send invitation.
        if (getDuration() <= (getTimeCompleted() + time)) {
            
            // Add invitation to study.
            study.addInvitedResearcher(invitee);
            
            // Check if existing relationship between primary researcher and invitee.
            RelationshipManager relationshipManager = Simulation.instance().getRelationshipManager();
            if (!relationshipManager.hasRelationship(person, invitee)) {
                // Add new communication meeting relationship.
                relationshipManager.addRelationship(person, invitee, Relationship.COMMUNICATION_MEETING);
            }
            
            // Add 10 points to invitee's opinion of primary researcher due to invitation. 
            Relationship relationship = relationshipManager.getRelationship(invitee, person);
            double currentOpinion = relationship.getPersonOpinion(invitee);
            relationship.setPersonOpinion(invitee, currentOpinion + 10D);
            
            logger.fine(person.getName() + " inviting " + invitee.getName() + 
                    " to collaborate in " + study.toString());
        }
        
        return 0D;
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
        
        String skillName = ScienceUtil.getAssociatedSkill(study.getScience());
        person.getMind().getSkillManager().addExperience(skillName, newPoints);
    }

    @Override
    public List<String> getAssociatedSkills() {
        List<String> skills = new ArrayList<String>(1);
        skills.add(ScienceUtil.getAssociatedSkill(study.getScience()));
        return skills;
    }

    @Override
    public int getEffectiveSkillLevel() {
        SkillManager manager = person.getMind().getSkillManager();
        String skillName = ScienceUtil.getAssociatedSkill(study.getScience());
        return manager.getEffectiveSkillLevel(skillName);
    }

    @Override
    protected double performMappedPhase(double time) {
        if (getPhase() == null) throw new IllegalArgumentException("Task phase is null");
        if (WRITING_INVITATION.equals(getPhase())) return writingInvitationPhase(time);
        else return time;
    }
    
    @Override
    public void destroy() {
        super.destroy();
        
        study = null;
        invitee = null;
    }
}