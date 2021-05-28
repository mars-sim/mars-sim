/**
 * Mars Simulation Project
 * InviteStudyCollaboratorMeta.java
 * @version 3.1.2 2020-09-02
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.JobType;
import org.mars_sim.msp.core.person.ai.task.InviteStudyCollaborator;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskTrait;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.core.science.ScientificStudyUtil;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * Meta task for the InviteStudyCollaborator task.
 */
public class InviteStudyCollaboratorMeta extends MetaTask {

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.inviteStudyCollaborator"); //$NON-NLS-1$

    public InviteStudyCollaboratorMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.WORK_HOUR);
		setTrait(TaskTrait.LEADERSHIP);
		setPreferredJob(JobType.ACADEMICS);
	}

    @Override
    public Task constructInstance(Person person) {
        return new InviteStudyCollaborator(person);
    }

    @Override
    public double getProbability(Person person) {

        double result = 0D;
        
        if (person.isInside()) {

            if (!person.getPhysicalCondition().isFitByLevel(1000, 50, 500))
            	return 0;

            // Check if study is in invitation phase.
            ScientificStudy study = person.getStudy();
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
                        JobType job = person.getMind().getJob();
                        ScienceType science = study.getScience();
                        if (science == ScienceType.getJobScience(job)) {
                            result *= 2D;
                        }
                        result *= person.getAssociatedSettlement().getGoodsManager().getResearchFactor();

                        result = applyPersonModifier(result, person);
	                }
	            }
	        }
        }

        return result;
    }
}
