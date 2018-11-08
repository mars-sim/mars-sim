/**
 * Mars Simulation Project
 * InviteStudyCollaboratorMeta.java
 * @version 3.1.0 2017-10-23
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.io.Serializable;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.FavoriteType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.task.InviteStudyCollaborator;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.core.science.ScientificStudyManager;
import org.mars_sim.msp.core.science.ScientificStudyUtil;
import org.mars_sim.msp.core.structure.building.Building;

/**
 * Meta task for the InviteStudyCollaborator task.
 */
public class InviteStudyCollaboratorMeta implements MetaTask, Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;
    
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.inviteStudyCollaborator"); //$NON-NLS-1$

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

        if (person.isInside()) {

	        ScientificStudyManager manager = Simulation.instance().getScientificStudyManager();
	        ScientificStudy study = manager.getOngoingPrimaryStudy(person);

            // Check if study is in invitation phase.
            if (study != null && study.getPhase().equals(ScientificStudy.INVITATION_PHASE)) {

                // Check that there isn't a full set of open invitations already sent out.
                int collabNum = study.getCollaborativeResearchers().size();
                int openInvites = study.getNumOpenResearchInvitations();
                if ((openInvites + collabNum) < ScientificStudy.MAX_NUM_COLLABORATORS) {

                    // Check that there's scientists available for invitation.
                    if (ScientificStudyUtil.getAvailableCollaboratorsForInvite(study).size() > 0) {

                        result = 25D;

                        // Crowding modifier
                        Building adminBuilding = InviteStudyCollaborator.getAvailableAdministrationBuilding(person);
                        if (adminBuilding != null) {
                            result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, adminBuilding);
                            result *= TaskProbabilityUtil.getRelationshipModifier(person, adminBuilding);
                        }

                        // Increase probability if person's current job is related to study's science.
                        Job job = person.getMind().getJob();
                        ScienceType science = study.getScience();
                        if (science == ScienceType.getJobScience(job)) {
                            result *= 2D;
                        }

                        // Job modifier.
                        if (job != null) {
                            result *= job.getStartTaskProbabilityModifier(InviteStudyCollaborator.class)
                            		* person.getAssociatedSettlement().getGoodsManager().getResearchFactor();
                        }


                        // Modify if research is the person's favorite activity.
                        if (person.getFavorite().getFavoriteActivity() == FavoriteType.RESEARCH) {
                            result *= 2D;
                        }

                        // Add Preference modifier
                        if (result > 0)
                        	result += person.getPreference().getPreferenceScore(this);
                        if (result < 0) result = 0;

	                }
	            }
	        }
        }

        return result;
    }

	@Override
	public Task constructInstance(Robot robot) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getProbability(Robot robot) {
		// TODO Auto-generated method stub
		return 0;
	}
}