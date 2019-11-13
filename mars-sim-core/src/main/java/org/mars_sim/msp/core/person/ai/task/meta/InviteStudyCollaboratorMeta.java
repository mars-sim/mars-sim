/**
 * Mars Simulation Project
 * InviteStudyCollaboratorMeta.java
 * @version 3.1.0 2017-10-23
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.io.Serializable;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.FavoriteType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.task.InviteStudyCollaborator;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.core.science.ScientificStudyUtil;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Vehicle;

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

            // Probability affected by the person's stress and fatigue.
            PhysicalCondition condition = person.getPhysicalCondition();
            double fatigue = condition.getFatigue();
            double stress = condition.getStress();
            double hunger = condition.getHunger();
            
            if (fatigue > 1000 || stress > 50 || hunger > 500)
            	return 0;

            // Check if study is in invitation phase.
            ScientificStudy study = scientificStudyManager.getOngoingPrimaryStudy(person);
            if (study == null)
            	return 0;
            		
            else if (study.getPhase().equals(ScientificStudy.INVITATION_PHASE)) {

                // Check that there isn't a full set of open invitations already sent out.
                int collabNum = study.getCollaborativeResearchers().size();
                int openInvites = study.getNumOpenResearchInvitations();
                if ((openInvites + collabNum) < study.getMaxCollaborators()) {

                    // Check that there's scientists available for invitation.
                    if (ScientificStudyUtil.getAvailableCollaboratorsForInvite(study).size() > 0) {

                        result += 25D;

                        if (person.isInVehicle()) {	
                	        // Check if person is in a moving rover.
                	        if (Vehicle.inMovingRover(person)) {
                		        // the bonus for proposing scientific study inside a vehicle, 
                	        	// rather than having nothing to do if a person is not driving
                	        	result += 20;
                	        } 	       
                	        else
                		        // the bonus for proposing scientific study inside a vehicle, 
                	        	// rather than having nothing to do if a person is not driving
                	        	result += 5;
                        }
                        
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
                            result += RandomUtil.getRandomInt(1, 20);
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