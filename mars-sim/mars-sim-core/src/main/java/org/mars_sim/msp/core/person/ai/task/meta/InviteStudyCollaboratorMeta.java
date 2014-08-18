/**
 * Mars Simulation Project
 * InviteStudyCollaboratorMeta.java
 * @version 3.07 2014-08-07
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.task.InviteStudyCollaborator;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.core.science.ScientificStudyManager;
import org.mars_sim.msp.core.science.ScientificStudyUtil;
import org.mars_sim.msp.core.structure.building.Building;

/**
 * Meta task for the InviteStudyCollaborator task.
 */
public class InviteStudyCollaboratorMeta implements MetaTask {

    // TODO: Use enum instead of string for name for internationalization.
    private static final String NAME = "Writing Study Collaboration Invitation";
    
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Task constructInstance(Person person) {
        return new InviteStudyCollaborator(person);
    }

    @Override
    public double getProbability(Person person) {
        
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
                        ScienceType science = study.getScience();
                        if (science == ScienceType.getJobScience(job)) {
                            result*= 2D;
                        }
                    }
                }
            }
        }
        
        // Crowding modifier
        if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
            Building adminBuilding = InviteStudyCollaborator.getAvailableAdministrationBuilding(person);
            if (adminBuilding != null) {
                result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, adminBuilding);
                result *= TaskProbabilityUtil.getRelationshipModifier(person, adminBuilding);
            }
        }
        
        // Job modifier.
        Job job = person.getMind().getJob();
        if (job != null) {
            result *= job.getStartTaskProbabilityModifier(InviteStudyCollaborator.class);
        }
        
        return result;
    }
}